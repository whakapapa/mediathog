
package mediathog.tool;

import mSearch.tool.ApplicationConfiguration;
import mSearch.tool.Log;
import mSearch.tool.TimedTextMarkupLanguageParser;
import mediathog.daten.DatenDownload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class MVSubtitle {

	private static final int TIMEOUT = 10000;
	private static final String SUFFIX_SRT = "srt";
	private static final String SUFFIX_TTML = "ttml";
	private static final String SUFFIX_VTT = "vtt";

	private static final Logger logger = LogManager.getLogger(MVSubtitle.class);

	public void writeSubtitle(DatenDownload datenDownload) {
		String suffix;// txt käme dem Infofile in die Quere

		String urlSubtitle = "";
		String strSubtitelFile;
		File subtitelFile;
		HttpURLConnection conn = null;
		InputStream in = null;
		String encoding;

		if (datenDownload.arr[DatenDownload.DOWNLOAD_URL_SUBTITLE].isEmpty()) {
			return;
		}
		try {
			logger.info("Untertitel {} schreiben nach {}", datenDownload.arr[DatenDownload.DOWNLOAD_URL_SUBTITLE],
			datenDownload.arr[DatenDownload.DOWNLOAD_ZIEL_PFAD]);

			urlSubtitle = datenDownload.arr[DatenDownload.DOWNLOAD_URL_SUBTITLE];
			suffix = GuiFunktionen.getSuffixFromUrl(urlSubtitle);
			if (!suffix.endsWith(SUFFIX_SRT) && !suffix.endsWith(SUFFIX_VTT)) {
				suffix = SUFFIX_TTML;
			}
			strSubtitelFile = datenDownload.getFileNameWithoutSuffix() + '.' + suffix;
			subtitelFile = new File(strSubtitelFile);

			new File(datenDownload.arr[DatenDownload.DOWNLOAD_ZIEL_PFAD]).mkdirs();

			conn = (HttpURLConnection) new URL(urlSubtitle).openConnection();
			conn.setRequestProperty("User-Agent",
			ApplicationConfiguration.getConfiguration()
			.getString(ApplicationConfiguration.APPLICATION_USER_AGENT));

			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			conn.setReadTimeout(TIMEOUT);
			conn.setConnectTimeout(TIMEOUT);

			// the encoding returned by the server
			encoding = conn.getContentEncoding();
			if ((conn.getResponseCode()) < 400) {
				in = conn.getInputStream();
			} else {
				// dann wars das
				Log.errorLog(752301248, "url: " + urlSubtitle);
			}

			if (in == null) {
				return;
			}

			if (encoding != null) {
				switch (encoding.toLowerCase()) {
					case "gzip":
					in = new GZIPInputStream(in);
					break;
					case "deflate":
					in = new InflaterInputStream(in, new Inflater(true));
					break;
				}
			}

			try (FileOutputStream fos = new FileOutputStream(subtitelFile)) {
				final byte[] buffer = new byte[64 * 1024];
				int n;
				while ((n = in.read(buffer)) != -1) {
					fos.write(buffer, 0, n);
				}
				logger.info("Untertitel wurde geschrieben");
			}
		} catch (IOException ex) {
			strSubtitelFile = null;
			if (conn != null) {
				try {
					if (in != null) {
						in.close();
					}
				} catch (Exception ignored) {
				}
			}
		} catch (Exception ignored) {
			strSubtitelFile = null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ignored) {
			}
		}

		try (TimedTextMarkupLanguageParser ttmlp = new TimedTextMarkupLanguageParser()) {
			if (strSubtitelFile != null) {
				if (!strSubtitelFile.endsWith('.' + SUFFIX_SRT) && !strSubtitelFile.endsWith("." + SUFFIX_VTT)) {

					Path p = new File(strSubtitelFile).toPath();
					Path srt = new File(datenDownload.getFileNameWithoutSuffix() + "." + SUFFIX_SRT).toPath();
					if (ttmlp.parse(p)) {
						ttmlp.toSrt(srt);
					} else if (ttmlp.parseXmlFlash(p)) {
						ttmlp.toSrt(srt);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Fehler bei Untertitel schreiben:", ex);
		}
	}
}
