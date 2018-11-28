
package mediathek.gui;

import mediathek.config.Daten;
import mediathek.tool.table.MVTable;

import javax.swing.*;

@SuppressWarnings("serial")
public class PanelVorlage extends JPanel {
	public Daten daten;
	public boolean stopBeob = false;
	public JFrame parentComponent = null;
	MVTable tabelle = null;
	public boolean solo = false; // nicht in einem eigenem Frame

	public PanelVorlage(Daten d, JFrame pparentComponent) {
		daten = d;
		parentComponent = pparentComponent;
		addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentShown(java.awt.event.ComponentEvent evt) {
				isShown();
			}
		});
	}

	public void isShown() {
		//FIXME Das kann nicht wirklich eine korrekte Sache sein hier...
		// immer wenn isShown
	}

	public void tabelleSpeichern() {
		if (tabelle != null) {
			tabelle.tabelleNachDatenSchreiben();
		}
	}
}
