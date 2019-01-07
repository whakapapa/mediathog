package mediathog.gui.actions;

import mediathog.config.Daten;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowFilmInformationAction extends AbstractAction {

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!Daten.filmInfo.isVisible()) {
			Daten.filmInfo.showInfo();
		}
	}
}
