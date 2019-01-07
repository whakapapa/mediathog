package mediathog.update;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import mediathog.config.Konstanten;
import mediathog.gui.dialog.StandardCloseDialog;
import mediathog.tool.GuiFunktionen;

import javax.swing.*;
import java.awt.*;

public class UpdateNotificationDialog extends StandardCloseDialog {
	private final ServerProgramInformation programInformation;
	private final UpdateNotificationPanel panel = new UpdateNotificationPanel();
	private WebView browser;
	private WebEngine webEngine;

	public UpdateNotificationDialog(Frame owner, String title, ServerProgramInformation progInfo) {
		super(owner, title, true);
		programInformation = progInfo;

		setupDialogInformation();
		setupFxWebView();

		pack();
		GuiFunktionen.centerOnScreen(this, false);
	}

	private void setupFxWebView() {
		Platform.runLater(() -> {
			browser = new WebView();
			Scene scene = new Scene(browser);
			webEngine = browser.getEngine();
			webEngine.load("https://mediathekview.de/changelogs");


			panel.getFxPanel().setScene(scene);
		});
	}

	private void setupDialogInformation() {
		String label = "Mediathog " + programInformation.getVersion() + " ist verfügbar - "
		+ "Sie haben Version " + setVers;
		panel.getReleaseInfoLabel().setText(label);

	}

	@Override
	public JComponent createContentPanel() {
		return panel;
	}
}
