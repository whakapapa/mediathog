
package mSearch;

import java.util.concurrent.atomic.AtomicBoolean;

public class Config {

	private static boolean debug; // Debugmodus
	private static final AtomicBoolean stop = new AtomicBoolean(false); // damit kannn das Laden gestoppt werden

	public static void setPortableMode(boolean portableMode) {
		Config.portableMode = portableMode;
	}

	private static boolean portableMode;

	/**
	* Damit kann "stop" gesetzt/rückgesetzt werden.
	* Bei true wird die Suche abgebrochen.
	*
	* @param set
	*/
	public static void setStop(boolean set) {
		stop.set(set);
	}

	/**
	* Abfrage, ob ein Abbruch erfogte
	*
	* @return true/false
	*/
	public static boolean getStop() {
		return stop.get();
	}

	public static void enableDebugMode() {
		debug = true;
	}

	public static boolean isDebuggingEnabled() {
		return debug;
	}
}
