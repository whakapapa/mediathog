
package mediathog.controller.starter;

import com.google.common.util.concurrent.RateLimiter;
import mSearch.tool.ApplicationConfiguration;
import mSearch.tool.Listener;
import mSearch.tool.Log;
import mSearch.tool.MVHttpClient;
import mediathog.config.Daten;
import mediathog.config.MVConfig;
import mediathog.controller.MVBandwidthCountingInputStream;
import mediathog.controller.ThrottlingInputStream;
import mediathog.daten.DatenDownload;
import mediathog.gui.dialog.DialogContinueDownload;
import mediathog.gui.dialog.MeldungDownloadfehler;
import mediathog.gui.messages.DownloadFinishedEvent;
import mediathog.gui.messages.DownloadRateLimitChangedEvent;
import mediathog.gui.messages.DownloadStartEvent;
import mediathog.tool.MVInfoFile;
import mediathog.tool.MVSubtitle;
import net.engio.mbassy.listener.Handler;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class DirectHttpDownload extends Thread {

	private static final int HTTP_RANGE_NOT_SATISFIABLE = 416;
	private static final Logger logger = LogManager.getLogger(DirectHttpDownload.class);
	private final Daten daten;
	private final DatenDownload datenDownload;
	private final Start start;
	private final java.util.Timer bandwidthCalculationTimer;
	private final Configuration config = ApplicationConfiguration.getConfiguration();
	/**
	* The number of times how often the system should try a restart of the download.
	*/
	private final int maxRestarts;
	/**
	* Instance which will limit the download speed
	*/
	private final RateLimiter rateLimiter;
	private HttpURLConnection conn;
	private HttpDownloadState state = HttpDownloadState.DOWNLOAD;
	private long downloaded;
	private File file;
	private String responseCode;
	private String exMessage;
	private boolean retAbbrechen;
	private boolean dialogAbbrechenIsVis;
	private CompletableFuture<Void> infoFuture;
	private CompletableFuture<Void> subtitleFuture;

	public DirectHttpDownload(Daten daten, DatenDownload d, java.util.Timer bandwidthCalculationTimer) {
		super();

		rateLimiter = RateLimiter.create(getDownloadLimit());

		daten.getMessageBus().subscribe(this);

		maxRestarts = MVConfig.getInt(MVConfig.Configs.SYSTEM_PARAMETER_DOWNLOAD_MAX_RESTART_HTTP);

		this.daten = daten;
		this.bandwidthCalculationTimer = bandwidthCalculationTimer;
		datenDownload = d;
		start = datenDownload.start;
		setName("DIRECT DL THREAD_" + d.arr[DatenDownload.DOWNLOAD_TITEL]);

		start.status = Start.STATUS_RUN;
		StarterClass.notifyStartEvent(datenDownload);
	}

	/**
	* Handles the rate limit change launched somewhere in the UI
	*
	* @param evt the new limit
	*/
	@Handler
	private void handleRateLimitChanged(DownloadRateLimitChangedEvent evt) {
		final long limit = calculateDownloadLimit(evt.newLimit);
		logger.info("thread changing download speed limit to {} KB", limit);
		rateLimiter.setRate(limit);
	}

	private long calculateDownloadLimit(long limit) {
		long newLimit;

		if (limit <= 0)
		newLimit = 10 * FileUtils.ONE_GB;
		else
		newLimit = limit * FileUtils.ONE_KB;

		return newLimit;
	}

	/**
	* Try to read the download limit from config file, other set to artificial limit 1GB/s!
	*
	* @return the limit in KB/s
	*/
	private long getDownloadLimit() {
		final int downloadLimit = ApplicationConfiguration.getConfiguration().getInt(ApplicationConfiguration.DOWNLOAD_RATE_LIMIT, 0);
		return calculateDownloadLimit(downloadLimit);
	}

	/**
	* Return the content length of the requested Url.
	*
	* @param url {@link java.net.URL} to the specified content.
	* @return Length in bytes or -1 on error.
	*/
	private long getContentLength(final URL url) {
		long contentSize = -1;

		final Request request = new Request.Builder().url(url).get()
		.header("User-Agent", config.getString(ApplicationConfiguration.APPLICATION_USER_AGENT))
		.build();

		try (Response response = MVHttpClient.getInstance().getReducedTimeOutClient().newCall(request).execute();
		ResponseBody body = response.body()) {
			if (response.isSuccessful() && body != null) {
				contentSize = body.contentLength();
				// alles unter 300k sind Playlisten, ...
				if (contentSize < 300_000) {
					contentSize = -1;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.errorLog(643298301, ex);
		}

		return contentSize;
	}

	/**
	* Setup the HTTP connection common settings
	*
	* @param conn The active connection.
	*/
	private void setupHttpConnection(HttpURLConnection conn) {
		conn.setRequestProperty("Range", "bytes=" + downloaded + '-');
		conn.setRequestProperty("User-Agent", config.getString(ApplicationConfiguration.APPLICATION_USER_AGENT));
		conn.setDoInput(true);
		conn.setDoOutput(true);
	}

	private void startInfoFileDownload() {
		final boolean downloadInfoFile = Boolean.parseBoolean(datenDownload.arr[DatenDownload.DOWNLOAD_INFODATEI]);
		if (downloadInfoFile) {
			infoFuture = CompletableFuture.runAsync(() -> {
				MVInfoFile infoFile = new MVInfoFile();
				infoFile.writeInfoFile(datenDownload);
			});
		}
	}

	private void downloadSubtitleFile() {
		if (Boolean.parseBoolean(datenDownload.arr[DatenDownload.DOWNLOAD_SUBTITLE])) {
			subtitleFuture = CompletableFuture.runAsync(() -> {
				MVSubtitle subtitleFile = new MVSubtitle();
				subtitleFile.writeSubtitle(datenDownload);
			});
		}
	}

	/**
	* Start the actual download process here.
	*
	* @throws IOException the io errors that may occur.
	*/
	private void downloadContent() throws IOException {
		startInfoFileDownload();

		downloadSubtitleFile();

		datenDownload.interruptRestart();

		/*
		This is a read-only property. Only experienced users should change it.
		Therefore we don´t save it.
		*/
		final int bufferSize = ApplicationConfiguration.getConfiguration().getInt(ApplicationConfiguration.APPLICATION_HTTP_DOWNLOAD_FILE_BUFFER_SIZE, 64 * 1024);

		try (FileOutputStream fos = new FileOutputStream(file, (downloaded != 0));
		BufferedOutputStream bos = new BufferedOutputStream(fos, bufferSize);
		ThrottlingInputStream tis = new ThrottlingInputStream(conn.getInputStream(), rateLimiter);
		MVBandwidthCountingInputStream mvis = new MVBandwidthCountingInputStream(tis, bandwidthCalculationTimer)) {
			start.mVBandwidthCountingInputStream = mvis;
			datenDownload.mVFilmSize.addAktSize(downloaded);
			final byte[] buffer = new byte[1024];
			long p, pp = 0, startProzent = -1;
			int len;
			long aktBandwidth, aktSize = 0;
			boolean melden = false;

			while ((len = start.mVBandwidthCountingInputStream.read(buffer)) != -1 && (!start.stoppen)) {
				downloaded += len;
				bos.write(buffer, 0, len);
				datenDownload.mVFilmSize.addAktSize(len);

				//für die Anzeige prüfen ob sich was geändert hat
				if (aktSize != datenDownload.mVFilmSize.getAktSize()) {
					aktSize = datenDownload.mVFilmSize.getAktSize();
					melden = true;
				}
				if (datenDownload.mVFilmSize.getSize() > 0) {
					p = (aktSize * (long) 1000) / datenDownload.mVFilmSize.getSize();
					if (startProzent == -1) {
						startProzent = p;
					}
					// p muss zwischen 1 und 999 liegen
					if (p == 0) {
						p = Start.PROGRESS_GESTARTET;
					} else if (p >= 1000) {
						p = 999;
					}
					start.percent = (int) p;
					if (p != pp) {
						pp = p;
						// Restzeit ermitteln
						if (p > 2 && p > startProzent) {
							// sonst macht es noch keinen Sinn
							int diffZeit = start.startZeit.diffInSekunden();
							int restProzent = 1000 - (int) p;
							start.restSekunden = (diffZeit * restProzent / (p - startProzent));
							// anfangen zum Schauen kann man, wenn die Restzeit kürzer ist
							// als die bereits geladene Speilzeit des Films
							bereitsAnschauen(datenDownload);
						}
						melden = true;
					}
				}
				aktBandwidth = start.mVBandwidthCountingInputStream.getBandwidth(); // bytes per second
				if (aktBandwidth != start.bandbreite) {
					start.bandbreite = aktBandwidth;
					melden = true;
				}
				if (melden) {
					Listener.notify(Listener.EREIGNIS_ART_DOWNLOAD_PROZENT, StarterClass.class.getName());
					melden = false;
				}
			}
		}

		start.bandbreite = start.mVBandwidthCountingInputStream.getSumBandwidth();
		if (!start.stoppen) {
			if (datenDownload.quelle == DatenDownload.QUELLE_BUTTON) {
				// direkter Start mit dem Button
				start.status = Start.STATUS_FERTIG;
			} else if (StarterClass.pruefen(daten, datenDownload, start)) {
				//Anzeige ändern - fertig
				start.status = Start.STATUS_FERTIG;
			} else {
				//Anzeige ändern - bei Fehler fehlt der Eintrag
				start.status = Start.STATUS_ERR;
			}
		}
	}

	private void setupHttpTimeout() {
		final int timeout = (int) TimeUnit.MILLISECONDS.convert(MVConfig.getInt(MVConfig.Configs.SYSTEM_PARAMETER_DOWNLOAD_TIMEOUT_SEKUNDEN), TimeUnit.SECONDS);
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(timeout);
	}

	@Override
	public synchronized void run() {
		StarterClass.startmeldung(datenDownload, start);
		daten.getMessageBus().publishAsync(new DownloadStartEvent());

		try {
			Files.createDirectories(Paths.get(datenDownload.arr[DatenDownload.DOWNLOAD_ZIEL_PFAD]));
		} catch (IOException ignored) {
		}

		int restartCount = 0;
		boolean restart = true;

		while (restart) {
			restart = false;
			try {
				final URL url = new URL(datenDownload.arr[DatenDownload.DOWNLOAD_URL]);
				file = new File(datenDownload.arr[DatenDownload.DOWNLOAD_ZIEL_PFAD_DATEINAME]);

				if (!cancelDownload()) {

					datenDownload.mVFilmSize.setSize(getContentLength(url));
					datenDownload.mVFilmSize.setAktSize(0);

					conn = (HttpURLConnection) url.openConnection();

					setupHttpTimeout();
					setupHttpConnection(conn);

					conn.connect();
					final int httpResponseCode = conn.getResponseCode();
					if (httpResponseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
						//Range passt nicht, also neue Verbindung versuchen...
						if (httpResponseCode == HTTP_RANGE_NOT_SATISFIABLE) {
							conn.disconnect();
							//Get a new connection and reset download param...
							conn = (HttpURLConnection) url.openConnection();
							downloaded = 0;
							setupHttpConnection(conn);
							conn.connect();
							//hier war es dann nun wirklich...
							if (conn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST)
							state = HttpDownloadState.ERROR;
							else
							state = HttpDownloadState.DOWNLOAD;
						} else {
							// ==================================
							// dann wars das
							responseCode = "Responsecode: " + conn.getResponseCode() + '\n' + conn.getResponseMessage();
							Log.errorLog(915236798, "HTTP-Fehler: " + conn.getResponseCode() + ' ' + conn.getResponseMessage());
							displayErrorDialog();
							state = HttpDownloadState.ERROR;
						}
					}
				}
				switch (state) {
					case DOWNLOAD:
					downloadContent();
					break;
					case CANCEL:
					break;
					case ERROR:
					start.status = Start.STATUS_ERR;
					break;
				}
			} catch (SocketTimeoutException ex) {
				if (restartCount < maxRestarts) {
					printTimeoutMessage(restartCount);

					restartCount++;
					restart = true;
				}
			} catch (IOException ex) {
				if (restartCount < maxRestarts) {
					restartCount++;
					restart = true;
				} else {
					// dann weiß der Geier!
					exMessage = ex.getLocalizedMessage();
					Log.errorLog(316598941, ex, "Fehler");
					start.status = Start.STATUS_ERR;
					if (!Daten.isAuto()) {
						SwingUtilities.invokeLater(() -> new MeldungDownloadfehler(Daten.getInstance().getMediathekGui(), exMessage, datenDownload).setVisible(true));
					}
				}
			}
		}

		performCleanup();

		StarterClass.finalizeDownload(datenDownload, start, state);

		daten.getMessageBus().publishAsync(new DownloadFinishedEvent());

		daten.getMessageBus().unsubscribe(this);
	}

	private void performCleanup() {
		try {
			if (conn != null)
			conn.disconnect();

			if (infoFuture != null)
			infoFuture.get();

			if (subtitleFuture != null)
			subtitleFuture.get();

		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error performing cleanup.", e);
		}
	}

	private void displayErrorDialog() {
		if (!Daten.isAuto()) {
			//i really don´t know why maxRestarts/2 has to be used...but it works now
			if (!(start.countRestarted < maxRestarts / 2)) {
				SwingUtilities.invokeLater(() -> new MeldungDownloadfehler(Daten.getInstance().getMediathekGui(), "URL des Films:\n"
				+ datenDownload.arr[DatenDownload.DOWNLOAD_URL] + "\n\n"
				+ responseCode + '\n', datenDownload).setVisible(true));
			}
		}
	}

	private void printTimeoutMessage(final int restartCount) {
		logger.error("Timeout, Download Restarts: {}", restartCount);
		logger.error("Ziel: {}", datenDownload.arr[DatenDownload.DOWNLOAD_ZIEL_PFAD_DATEINAME]);
		logger.error("URL: {}", datenDownload.arr[DatenDownload.DOWNLOAD_URL]);
	}

	private boolean cancelDownload() {
		if (!file.exists()) {
			// dann ist alles OK
			return false;
		}
		if (Daten.isAuto()) {
			return false; // immer überschreiben, keine GUI!!!
		}

		dialogAbbrechenIsVis = true;
		retAbbrechen = true;
		if (SwingUtilities.isEventDispatchThread()) {
			retAbbrechen = abbrechen_();
		} else {
			SwingUtilities.invokeLater(() -> {
				retAbbrechen = abbrechen_();
				dialogAbbrechenIsVis = false;
			});
		}
		while (dialogAbbrechenIsVis) {
			try {
				wait(100);
			} catch (Exception ignored) {

			}
		}
		return retAbbrechen;
	}

	private boolean abbrechen_() {
		boolean result = false;
		if (file.exists()) {
			DialogContinueDownload dialogContinueDownload = new DialogContinueDownload(Daten.getInstance().getMediathekGui(), datenDownload, true /*weiterführen*/);
			dialogContinueDownload.setVisible(true);

			switch (dialogContinueDownload.getResult()) {
				case CANCELLED:
				// dann wars das
				state = HttpDownloadState.CANCEL;
				result = true;
				break;

				case CONTINUE:
				downloaded = file.length();
				break;

				case RESTART_WITH_NEW_NAME:
				if (dialogContinueDownload.isNewName()) {
					Listener.notify(Listener.EREIGNIS_LISTE_DOWNLOADS, this.getClass().getSimpleName());
					try {
						Files.createDirectories(Paths.get(datenDownload.arr[DatenDownload.DOWNLOAD_ZIEL_PFAD]));
					} catch (IOException ignored) {
					}
					file = new File(datenDownload.arr[DatenDownload.DOWNLOAD_ZIEL_PFAD_DATEINAME]);
				}
				break;
			}
		}
		return result;
	}

	private void bereitsAnschauen(DatenDownload datenDownload) {
		if (datenDownload.film != null && datenDownload.start != null) {
			final long filmLength = datenDownload.film.getFilmLength();
			if (filmLength > 0
			&& datenDownload.start.restSekunden > 0
			&& datenDownload.mVFilmSize.getAktSize() > 0
			&& datenDownload.mVFilmSize.getSize() > 0) {
				// macht nur dann Sinn
				final long zeitGeladen = filmLength * datenDownload.mVFilmSize.getAktSize() / datenDownload.mVFilmSize.getSize();
				if (zeitGeladen > (datenDownload.start.restSekunden * 1.1 /* plus 10% zur Sicherheit*/)) {
					datenDownload.start.beginnAnschauen = true;
				}
			}
		}
	}

	enum HttpDownloadState {

		CANCEL, ERROR, DOWNLOAD
	}
}
