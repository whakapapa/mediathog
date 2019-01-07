package mediathog.gui.dialog;

import mediathog.config.Daten;
import mediathog.gui.ResetSettingsPanel;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class ResetSettingsDialog extends StandardCloseDialog {
	private final Daten daten;

	public ResetSettingsDialog(Frame owner, Daten daten) {
		super(owner, "Programm zurücksetzen", true);
		this.daten = daten;
		setResizable(false);

		pack();
	}

	@Override
	public JComponent createContentPanel() {
		return new ResetSettingsPanel(null, daten);
	}
}
