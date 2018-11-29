
package mediathog.tool;

import mSearch.tool.Log;
import mediathog.Main;
import mediathog.config.Daten;
import mediathog.config.Konstanten;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.security.CodeSource;

public class MVFunctionSys {

	public static synchronized void startMeldungen() {
		Log.versionMsg(MVFunctionSys.getProgName());
		logger.info("Programmpfad: " + MVFunctionSys.getPathJar());
		logger.info("Verzeichnis Einstellungen: " + Daten.getSettingsDirectory_String());
		logger.info("");
		logger.info("");
		logger.info("");
	}

	private static final Logger logger = LogManager.getLogger(MVFunctionSys.class);

	/**
	* Retrieve the path to the program jar file.
	*
	* @return The program jar file path with a separator added.
	*/
	public static String getPathJar() {
		// macht Probleme bei Win und Netzwerkpfaden, liefert dann Absolute Pfade zB. \\VBOXSVR\share\Mediathog\...
		String pFilePath = "pFile";
		File propFile = new File(pFilePath);
		if (!propFile.exists()) {
			try {
				CodeSource cS = Main.class.getProtectionDomain().getCodeSource();
				File jarFile = new File(cS.getLocation().toURI().getPath());
				String jarDir = jarFile.getParentFile().getPath();
				propFile = new File(jarDir + File.separator + pFilePath);
			} catch (Exception ignored) {
			}
		}
		String s = StringUtils.replace(propFile.getAbsolutePath(), pFilePath, "");
		if (!s.endsWith(File.separator)) {
			s += File.separator;
		}
		return s;
	}

	public static String pathProgramIcons() {
		return getPathJar() + setDirIco;
	}

	public static String getProgVersionString() {
		return setProg + ' ' + setVers;
	}

	public static String getProgName() {
		return setProg + ' ' + setVers;
	}
}
