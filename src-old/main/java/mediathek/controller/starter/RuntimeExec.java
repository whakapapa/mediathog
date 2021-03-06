
package mediathog.controller.starter;

import mSearch.tool.Listener;
import mSearch.tool.Log;
import mSearch.tool.SysMsg;
import mediathog.tool.MVFilmSize;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuntimeExec {

	public static final String TRENNER_PROG_ARRAY = "<>";
	private static final int INPUT = 1;
	private static final int ERROR = 2;
	private Process process = null;
	private Start start;
	private static int procnr = 0; //TH
	private static final Pattern patternFlvstreamer = Pattern.compile("([0-9]*.[0-9]{1}%)");
	private static final Pattern patternFlvstreamerComplete = Pattern.compile("Download complete");
	private static final Pattern patternFfmpeg = Pattern.compile("(?<= Duration: )[^,]*"); // Duration: 00:00:30.28, start: 0.000000, bitrate: N/A
	private static final Pattern patternZeit = Pattern.compile("(?<=time=)[^ ]*"); // frame= 147 fps= 17 q=-1.0 size= 1588kB time=00:00:05.84 bitrate=2226.0kbits/s
	private static final Pattern patternSize = Pattern.compile("(?<=size=)[^k]*"); // frame= 147 fps= 17 q=-1.0 size= 1588kB time=00:00:05.84 bitrate=2226.0kbits/s

	private double totalSecs = 0;
	private long oldSize = 0;
	private long oldSecs = 0;
	// private DatenDownload datenDownload = null;
	private MVFilmSize mVFilmSize = null;
	private final String strProgCall;
	private String[] arrProgCallArray = null;
	private String strProgCallArray = "";

	public RuntimeExec(MVFilmSize mVFilmSize, Start start,
	String strProgCall, String strProgCallArray) {
		this.mVFilmSize = mVFilmSize;
		this.start = start;
		this.strProgCall = strProgCall;
		this.arrProgCallArray = strProgCallArray.split(TRENNER_PROG_ARRAY);
		this.strProgCallArray = strProgCallArray;
		if (arrProgCallArray.length <= 1) {
			arrProgCallArray = null;
		}
	}

	public RuntimeExec(String p) {
		strProgCall = p;
	}

	private static final Logger logger = LogManager.getLogger(RuntimeExec.class);

	public Process exec(boolean log) {
		try {
			if (arrProgCallArray != null) {
				if (log) {
					logger.info("=====================");
					logger.info("Starte Array: ");
					logger.info(" -> " + strProgCallArray);
					logger.info("=====================");
				}
				process = Runtime.getRuntime().exec(arrProgCallArray);
			} else {
				if (log) {
					logger.info("=====================");
					logger.info("Starte nicht als Array:");
					logger.info(" -> " + strProgCall);
					logger.info("=====================");
				}
				process = Runtime.getRuntime().exec(strProgCall);
			}

			Thread clearIn = new Thread(new ClearInOut(INPUT, process));
			Thread clearOut = new Thread(new ClearInOut(ERROR, process));
			clearIn.start();
			clearOut.start();
		} catch (Exception ex) {
			Log.errorLog(450028932, ex, "Fehler beim Starten");
		}
		return process;
	}

	//===================================
	// Private
	//===================================
	private class ClearInOut implements Runnable {

		private final int art;
		private BufferedReader buff;
		private InputStream in;
		private final Process process;
		private int percent = 0;
		private int percent_start = -1;

		public ClearInOut(int a, Process p) {
			art = a;
			process = p;
		}

		@Override
		public void run() {
			String titel = "";
			try {
				switch (art) {
					case INPUT:
					in = process.getInputStream();
					titel = "INPUTSTREAM";
					break;
					case ERROR:
					in = process.getErrorStream();
					//TH
					synchronized (this) {
						titel = "ERRORSTREAM [" + (++procnr) + ']';
					}
					break;
				}
				buff = new BufferedReader(new InputStreamReader(in));
				String inStr;
				while ((inStr = buff.readLine()) != null) {
					GetPercentageFromErrorStream(inStr);
					SysMsg.playerMsg(titel + ": " + inStr);
				}
			} catch (IOException ignored) {
			} finally {
				try {
					buff.close();
				} catch (IOException ignored) {
				}
			}
		}

		private void GetPercentageFromErrorStream(String input) {
			// by: siedlerchr für den flvstreamer und rtmpdump
			Matcher matcher;
			matcher = patternFlvstreamer.matcher(input);
			if (matcher.find()) {
				try {
					String prozent = matcher.group();
					prozent = prozent.substring(0, prozent.length() - 1);
					double d = Double.parseDouble(prozent);
					meldenDouble(d);
				} catch (Exception ex) {
					Listener.notify(Listener.EREIGNIS_ART_DOWNLOAD_PROZENT, RuntimeExec.class.getName());
					Log.errorLog(912036780, input);
				}
				return;
			}
			matcher = patternFlvstreamerComplete.matcher(input);
			if (matcher.find()) {
				// dann ist der Download fertig, zur sicheren Erkennung von 100%
				meldenDouble(100);
				return;
			}

			// für ffmpeg
			// ffmpeg muss dazu mit dem Parameter -i gestartet werden:
			// -i %f -acodec copy -vcodec copy -y **
			try {
				// Gesamtzeit
				matcher = patternFfmpeg.matcher(input);
				if (matcher.find()) {
					// Find duration
					String dauer = matcher.group().trim();
					String[] hms = dauer.split(":");
					totalSecs = Integer.parseInt(hms[0]) * 3600
					+ Integer.parseInt(hms[1]) * 60
					+ Double.parseDouble(hms[2]);
				}
				// Bandbreite
				matcher = patternSize.matcher(input);
				if (matcher.find()) {
					String s = matcher.group().trim();
					if (!s.isEmpty()) {
						try {
							final long aktSize = Integer.parseInt(StringUtils.replace(s, "kB", ""));
							mVFilmSize.setAktSize(aktSize * 1_000);
							long akt = start.startZeit.diffInSekunden();
							if (oldSecs < akt - 5) {
								start.bandbreite = (aktSize - oldSize) * 1_000 / (akt - oldSecs);
								oldSecs = akt;
								oldSize = aktSize;
							}
						} catch (NumberFormatException ignored) {
						}
					}
				}
				// Fortschritt
				matcher = patternZeit.matcher(input);
				if (totalSecs > 0 && matcher.find()) {
					// ffmpeg 1611kB time=00:00:06.73 bitrate=1959.7kbits/s
					// avconv size= 26182kB time=100.96 bitrate=2124.5kbits/s
					String zeit = matcher.group();
					if (zeit.contains(":")) {
						String[] hms = zeit.split(":");
						final double aktSecs = Integer.parseInt(hms[0]) * 3600
						+ Integer.parseInt(hms[1]) * 60
						+ Double.parseDouble(hms[2]);
						double d = aktSecs / totalSecs * 100;
						meldenDouble(d);
					} else {
						double aktSecs = Double.parseDouble(zeit);
						double d = aktSecs / totalSecs * 100;
						meldenDouble(d);
					}
				}
			} catch (Exception ex) {
				Listener.notify(Listener.EREIGNIS_ART_DOWNLOAD_PROZENT, RuntimeExec.class.getName());
				Log.errorLog(912036780, input);
			}
		}

		private void meldenDouble(double d) {
			// nur ganze Int speichern, und 1000 Schritte
			d *= 10;
			int pNeu = (int) d;
			start.percent = pNeu;
			if (pNeu != percent) {
				percent = pNeu;
				if (percent_start == -1) {
					// für wiedergestartete Downloads
					percent_start = percent;
				}
				if (percent > (percent_start + 5)) {
					// sonst macht es noch keinen Sinn
					int diffZeit = start.startZeit.diffInSekunden();
					int diffProzent = percent - percent_start;
					int restProzent = 1000 - percent;
					start.restSekunden = (diffZeit * restProzent / diffProzent);
				}
				Listener.notify(Listener.EREIGNIS_ART_DOWNLOAD_PROZENT, RuntimeExec.class.getName());
			}
		}
	}
}
