
package mediathog.gui.actions;

import com.jidesoft.utils.SystemInfo;
import mSearch.tool.Listener;
import mSearch.tool.Log;
import mediathog.config.MVConfig;
import mediathog.gui.dialog.DialogProgrammOrdnerOeffnen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("serial")
public class UrlHyperlinkAction extends AbstractAction {

	private final JFrame jFrameParent;

	public UrlHyperlinkAction(JFrame jjFrameParent, String url) throws URISyntaxException {
		jFrameParent = jjFrameParent;
		super.putValue(Action.NAME, url);
		super.putValue(SHORT_DESCRIPTION, url);
		// super.putValue(LONG_DESCRIPTION, url);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			openURL(jFrameParent, e.getActionCommand());
		} catch (URISyntaxException ignored) {
		}
	}

	/**
	* Try to open a browser window.
	* @param paFrame the parent window.
	* @param url URL to be opened. Here in string format.
	* @throws URISyntaxException when URL is malformed.
	*/
	public static void openURL(JFrame paFrame, String url) throws URISyntaxException {
		if (Desktop.isDesktopSupported()) {
			Desktop d = Desktop.getDesktop();
			try {
				if (d.isSupported(Desktop.Action.BROWSE)) {
					d.browse(new URI(url));
					return;
				}
			} catch (RuntimeException ex) {
				//catch bug that certain urls will cause Desktop.browse to fail on OS X.
				//try to use applescript to launch safari
			} catch (IOException ignored) {
			}

			try {
				String programm = "";
				if (MVConfig.get(MVConfig.Configs.SYSTEM_URL_OEFFNEN).isEmpty()) {
					String text = "\n Der Browser zum Anzeigen der URL wird nicht gefunden.\n Browser selbst auswählen.";
					DialogProgrammOrdnerOeffnen dialog = new DialogProgrammOrdnerOeffnen(paFrame, true, "", "Browser suchen", text);
					dialog.setVisible(true);
					if (dialog.ok) {
						programm = dialog.ziel;
					}
				} else {
					programm = MVConfig.get(MVConfig.Configs.SYSTEM_URL_OEFFNEN);
				}
				String[] cmd = {programm, url};
				Runtime.getRuntime().exec(cmd);
				MVConfig.add(MVConfig.Configs.SYSTEM_URL_OEFFNEN, programm);
				Listener.notify(Listener.EREIGNIS_PROGRAMM_OEFFNEN, UrlHyperlinkAction.class.getSimpleName());
			} catch (Exception ex) {
				MVConfig.add(MVConfig.Configs.SYSTEM_URL_OEFFNEN, ""); // dann wars wohl nix
				Log.errorLog(316497658, ex, "URL öffnen: " + url);
			}
		}
	}
}
