
package mSearch.tool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class Duration {

	private static Date stopZeitStatic = new Date(System.currentTimeMillis());
	private static final DecimalFormat DF = new DecimalFormat("###,##0.00");
	private static int sum = 0;
	private static final ArrayList<Counter> COUNTER_LIST = new ArrayList<>();

	private static class Counter {

		String text;
		int count;
		long time;
		Date start;

		public Counter(String nr, int count) {
			this.text = nr;
			this.count = count;
			start = new Date();
		}
	}

	public static synchronized void counterStart(String text) {
		Counter cc = null;
		for (Counter c : COUNTER_LIST) {
			if (c.text.equals(text)) {
				cc = c;
				break;
			}
		}
		if (cc == null) {
			COUNTER_LIST.add(new Counter(text, 0));
		} else {
			cc.start = new Date();
		}
	}

	public static synchronized void counterStop(String text) {
		final Throwable t = new Throwable();
		final StackTraceElement methodCaller = t.getStackTrace()[2];
		final String klasse = methodCaller.getClassName() + '.' + methodCaller.getMethodName();
		String kl;
		try {
			kl = klasse;
			while (kl.contains(".")) {
				if (Character.isUpperCase(kl.charAt(0))) {
					break;
				} else {
					kl = kl.substring(kl.indexOf('.') + 1);
				}
			}
		} catch (Exception ignored) {
			kl = klasse;
		}

		String extra = "";
		Counter cc = null;
		for (Counter c : COUNTER_LIST) {
			if (c.text.equals(text)) {
				cc = c;
				break;
			}
		}
		if (cc != null) {
			cc.count++;
			try {
				final long time = Math.round(new Date().getTime() - cc.start.getTime());
				cc.time += time;
				extra = cc.text + " Anzahl: " + cc.count + " Dauer: " + roundDuration(time);
			} catch (Exception ignored) {
			}
		}

		staticPing(kl, text, extra);
	}

	public static synchronized void printCounter() {
		int max = 0;
		for (Counter c : COUNTER_LIST) {
			if (c.text.length() > max) {
				max = c.text.length();
			}
		}
		max++;
		for (Counter c : COUNTER_LIST) {
			while (c.text.length() < max) {
				c.text += ' ';
			}
		}

		logger.info("#################################################################");
		for (Counter c : COUNTER_LIST) {
			logger.info(c.text + " Anzahl: " + c.count + " Gesamtdauer: " + roundDuration(c.time));
		}
		logger.info("#################################################################");
	}

	private static final Logger logger = LogManager.getLogger(Duration.class);

	public synchronized static void staticPing(String text) {
		final Throwable t = new Throwable();
		final StackTraceElement methodCaller = t.getStackTrace()[2];
		final String klasse = methodCaller.getClassName() + '.' + methodCaller.getMethodName();
		String kl;
		try {
			kl = klasse;
			while (kl.contains(".")) {
				if (Character.isUpperCase(kl.charAt(0))) {
					break;
				} else {
					kl = kl.substring(kl.indexOf('.') + 1);
				}
			}
		} catch (Exception ignored) {
			kl = klasse;
		}
		staticPing(kl, text, "");
	}

	private static void staticPing(String klasse, String text, String extra) {
		Date now = new Date(System.currentTimeMillis());
		long sekunden;
		try {
			sekunden = Math.round(now.getTime() - stopZeitStatic.getTime());
		} catch (Exception ex) {
			sekunden = -1;
		}

		logger.info("========== ========== ========== ========== ==========");
		logger.info("DURATION " + sum++ + ": " + text + " [" + roundDuration(sekunden) + ']');
		logger.info(" Klasse: " + klasse);
		if (!extra.isEmpty()) {
			logger.info(" " + extra);
		}
		logger.info("========== ========== ========== ========== ==========");

		stopZeitStatic = now;
	}

	public static String roundDuration(long s) {
		String ret;
		if (s > 1_000.0) {
			ret = DF.format(s / 1_000.0) + " s";
		} else {
			ret = DF.format(s) + " ms";
		}

		return ret;
	}

}
