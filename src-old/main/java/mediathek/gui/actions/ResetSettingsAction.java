package mediathog.gui.actions;

import mediathog.config.Daten;
import mediathog.gui.dialog.ResetSettingsDialog;
import mediathog.tool.GuiFunktionen;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ResetSettingsAction extends AbstractAction {
	private final JFrame owner;
	private final Daten daten;

	public ResetSettingsAction(JFrame parent, Daten daten) {
		super();
		owner = parent;
		this.daten = daten;

		putValue(NAME, "Einstellungen zurücksetzen...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ResetSettingsDialog dialog = new ResetSettingsDialog(owner, daten);
		GuiFunktionen.centerOnScreen(dialog, false);
		dialog.setVisible(true);
	}
}
