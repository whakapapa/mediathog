
package mediathog.gui.dialog;

import mediathog.config.Daten;
import mediathog.config.MVConfig;
import mediathog.daten.ListePset;
import mediathog.daten.ListePsetVorlagen;
import mediathog.gui.dialogEinstellungen.PanelEinstellungenGeo;
import mediathog.gui.dialogEinstellungen.PanelProgrammPfade;
import mediathog.gui.dialogEinstellungen.PanelPsetKurz;
import mediathog.gui.dialogEinstellungen.PanelPsetLang;
import mediathog.tool.GuiFunktionenProgramme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class DialogStarteinstellungen extends JDialog {
	private final Daten daten;
	private final static int STAT_START = 1;
	private final static int STAT_PFAD = 2;
	private final static int STAT_PSET = 3;
	private final static int STAT_FERTIG = 4;
	private int status = STAT_START;
	private final JFrame parentComponent;
	private boolean anpassen = false;

	public DialogStarteinstellungen(JFrame parent, Daten dd) {
		super(parent, true);
		parentComponent = parent;
		initComponents();
		daten = dd;
		this.setTitle("Erster Start");
		jButtonStandard.addActionListener((ActionEvent e) -> weiter());
		jButtonAnpassen.addActionListener((ActionEvent e) -> {
			anpassen = true;
			weiter();
		});
		jCheckBoxAlleEinstellungen.setVisible(false);
		jCheckBoxAlleEinstellungen.addActionListener(e -> {
			status = STAT_PSET;
			weiter();
		});

		// setzt die Standardpfade für die wichtigsten Programme
		MVConfig.add(MVConfig.Configs.SYSTEM_PFAD_VLC, setVLC);
		MVConfig.add(MVConfig.Configs.SYSTEM_PFAD_FLVSTREAMER, setFLV);
		MVConfig.add(MVConfig.Configs.SYSTEM_PFAD_FFMPEG, setFF);

		createLayout();

		if (MVConfig.get(MVConfig.Configs.SYSTEM_PFAD_VLC).isEmpty()
		|| MVConfig.get(MVConfig.Configs.SYSTEM_PFAD_FLVSTREAMER).isEmpty()
		|| MVConfig.get(MVConfig.Configs.SYSTEM_PFAD_FFMPEG).isEmpty()) {
			//dann fehlt eine Programm
			jButtonStandard.setEnabled(false);
			anpassen = true;
		}
	}

	private void createLayout() {
		PanelEinstellungenGeo panelEinstellungenGeo = new PanelEinstellungenGeo(daten, parentComponent);
		jPanelExtra.setLayout(new BorderLayout());
		jPanelExtra.add(panelEinstellungenGeo, BorderLayout.CENTER);
	}

	private void weiter() {
		jButtonStandard.setEnabled(true);
		switch (status) {
			case STAT_START:
			statusStart();
			break;
			case STAT_PFAD:
			statusPfade();
			break;
			case STAT_PSET:
			statusPset();
			break;
			default:
			beenden();
			break;
		}
	}

	private void statusStart() {
		jButtonStandard.setText("Weiter");
		if (MVConfig.get(MVConfig.Configs.SYSTEM_PFAD_VLC).isEmpty()
		|| MVConfig.get(MVConfig.Configs.SYSTEM_PFAD_FLVSTREAMER).isEmpty()
		|| MVConfig.get(MVConfig.Configs.SYSTEM_PFAD_FFMPEG).isEmpty()) {
			// ein Programm (VLC, flvstreamer) wurde nicht gefunden, muss der Benutzer eintragen
			status = STAT_PFAD;
		} else if (anpassen) {
			// der Benutzer wills verstellen
			status = STAT_PFAD;
		} else // nur dann automatisch Standardprogramme einrichten, sonst fragen
		if (addStandarSet(parentComponent, daten)) {
			status = STAT_FERTIG;
		} else {
			status = STAT_PSET;
		}
		weiter();
	}

	private void statusPfade() {
		// erst Programmpfad prüfen
		jButtonAnpassen.setVisible(false);
		jCheckBoxAlleEinstellungen.setVisible(false);


		jScrollPane1.setViewportView(new PanelProgrammPfade(parentComponent, true /* vlc */, true /* flvstreamer */, true /*ffmpeg*/));

		status = STAT_PSET;
		jButtonStandard.setText("Weiter");
	}

	private void statusPset() {
		// Einstellungen zum Ansehen und Speichern der Filme anpassen
		jButtonAnpassen.setVisible(false);
		jCheckBoxAlleEinstellungen.setVisible(true);
		if (Daten.listePset.isEmpty()) {
			// Standardset hinzufügen
			addStandarSet(parentComponent, daten);
		}
		if (jCheckBoxAlleEinstellungen.isSelected()) {
			jScrollPane1.setViewportView(new PanelPsetLang(daten, parentComponent, Daten.listePset));
		} else {
			jScrollPane1.setViewportView(new PanelPsetKurz(daten, parentComponent, Daten.listePset));
		}
		status = STAT_FERTIG;
		jButtonStandard.setText("Weiter");
	}

	private boolean addStandarSet(JFrame parent, Daten daten) {
		boolean ret = false;
		ListePset pSet = ListePsetVorlagen.getStandarset(parent, daten, true /*replaceMuster*/);
		if (pSet != null) {
			Daten.listePset.addPset(pSet);
			MVConfig.add(MVConfig.Configs.SYSTEM_VERSION_PROGRAMMSET, pSet.version);
			ret = true;
		}
		return ret;
	}

	private void beenden() {
		this.dispose();
	}

	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
		jButtonStandard = new javax.swing.JButton();
		jCheckBoxAlleEinstellungen = new javax.swing.JCheckBox();
		jButtonAnpassen = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanelExtra = new javax.swing.JPanel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 255), 3));

		jButtonStandard.setText("Mit Standardeinstellungen starten");

		jCheckBoxAlleEinstellungen.setText("alle Einstellungen anzeigen");

		jButtonAnpassen.setText("Einstellungen anpassen");

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(
		jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
		.addContainerGap()
		.addComponent(jCheckBoxAlleEinstellungen)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		.addComponent(jButtonAnpassen)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jButtonStandard)
		.addContainerGap())
		);
		jPanel2Layout.setVerticalGroup(
		jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel2Layout.createSequentialGroup()
		.addContainerGap()
		.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
		.addComponent(jButtonStandard)
		.addComponent(jCheckBoxAlleEinstellungen)
		.addComponent(jButtonAnpassen))
		.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);

		javax.swing.GroupLayout jPanelExtraLayout = new javax.swing.GroupLayout(jPanelExtra);
		jPanelExtra.setLayout(jPanelExtraLayout);
		jPanelExtraLayout.setHorizontalGroup(
		jPanelExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGap(0, 788, Short.MAX_VALUE)
		);
		jPanelExtraLayout.setVerticalGroup(
		jPanelExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGap(0, 510, Short.MAX_VALUE)
		);

		jScrollPane1.setViewportView(jPanelExtra);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(layout.createSequentialGroup()
		.addContainerGap()
		.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 791, Short.MAX_VALUE)
		.addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		.addContainerGap())
		);
		layout.setVerticalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(layout.createSequentialGroup()
		.addContainerGap()
		.addComponent(jScrollPane1)
		.addGap(18, 18, 18)
		.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addContainerGap())
		);

		pack();
	}// </editor-fold>//GEN-END:initComponents
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton jButtonAnpassen;
	private javax.swing.JButton jButtonStandard;
	private javax.swing.JCheckBox jCheckBoxAlleEinstellungen;
	private javax.swing.JPanel jPanelExtra;
	private javax.swing.JScrollPane jScrollPane1;
	// End of variables declaration//GEN-END:variables
}
