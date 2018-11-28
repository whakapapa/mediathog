
package mediathek.tool;

import mediathek.config.MVConfig;
import mSearch.tool.Listener;
import java.awt.Desktop;
import java.awt.Frame;
import java.io.File;
import javax.swing.JOptionPane;
import mSearch.tool.Log;
import mediathek.gui.GuiDownloads;
import mediathek.gui.dialog.DialogProgrammOrdnerOeffnen;

public class OpenPlayerAction {

	public static void filmAbspielen(Frame parent, String datei) {
		boolean gut = false;
		File sFile;
		if (datei.isEmpty()) {
			return;
		}
		sFile = new File(datei);
		if (!sFile.exists()) {
			MVMessageDialog.showMessageDialog(parent, "Film existiert noch nicht!",
			"Fehler", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			if (!MVConfig.get(MVConfig.Configs.SYSTEM_PLAYER_ABSPIELEN).isEmpty()) {
				String programm = MVConfig.get(MVConfig.Configs.SYSTEM_PLAYER_ABSPIELEN);
				String[] cmd = {programm, sFile.getAbsolutePath()};
				Runtime.getRuntime().exec(cmd);
				gut = true;
			} else {
				if (Desktop.isDesktopSupported()) {
					Desktop d = Desktop.getDesktop();
					if (d.isSupported(Desktop.Action.OPEN)) {
						d.open(sFile);
						gut = true;
					}
				}
			}
		} catch (Exception ex) {
			try {
				gut = false;
				String programm = "";
				String text = "\n Ein Videoplayer zum Abspielen wird nicht gefunden.\n Videoplayer selbst auswählen.";
				DialogProgrammOrdnerOeffnen dialog = new DialogProgrammOrdnerOeffnen(parent, true, "", "Videoplayer suchen", text);
				dialog.setVisible(true);
				if (dialog.ok) {
					programm = dialog.ziel;
				}
				String[] cmd = {programm, sFile.getAbsolutePath()};
				Runtime.getRuntime().exec(cmd);
				MVConfig.add(MVConfig.Configs.SYSTEM_PLAYER_ABSPIELEN, programm);
				Listener.notify(Listener.EREIGNIS_PROGRAMM_OEFFNEN, GuiDownloads.class.getSimpleName());
				gut = true;
			} catch (Exception eex) {
				Log.errorLog(959632369, ex, "Ordner öffnen: " + datei);
			}
		} finally {
			if (!gut) {
				MVConfig.add(MVConfig.Configs.SYSTEM_PLAYER_ABSPIELEN, "");
				Listener.notify(Listener.EREIGNIS_PROGRAMM_OEFFNEN, GuiDownloads.class.getSimpleName());
				MVMessageDialog.showMessageDialog(parent, "Kann den Videoplayer nicht öffnen!",
				"Fehler", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
