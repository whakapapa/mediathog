
package mediathek.tool;

import mediathek.MediathekGui;
import mediathek.config.Daten;
import mediathek.config.Konstanten;
import mediathek.config.MVConfig.Configs;
import mediathek.gui.PanelVorlage;
import mediathek.res.GetIcon;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class MVFrame extends JFrame {
	private final Daten daten;
	private final MediathekGui.TABS tabsState;
	private Configs nrGroesse = null;

	public MVFrame(Daten ddaten, PanelVorlage jPanel, MediathekGui.TABS astate) {
		initComponents();
		daten = ddaten;
		tabsState = astate;

		this.setIconImage(GetIcon.getIcon("mediatheg-l.png", "/mediathek/res/", 58, 58).getImage());
		switch (tabsState) {
			case TAB_DOWNLOADS:
			this.setTitle("Downloads");
			break;
			case TAB_ABOS:
			this.setTitle("Abos");
			break;
			default:
			this.setTitle(setProg);
		}
		jPanelExtra.setLayout(new BorderLayout());
		jPanelExtra.add(jPanel, BorderLayout.CENTER);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		pack();
	}

	@Override
	public void dispose() {
		if (nrGroesse == null) {
			GuiFunktionen.getSize(nrGroesse, this);
		}
		super.dispose();
	}

	public void setSize(Configs nr) {
		nrGroesse = nr;
		GuiFunktionen.setSize(nr, this, daten.getMediathekGui());
	}


	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jPanelExtra = new javax.swing.JPanel();
		javax.swing.JPanel jPanelInfo = new javax.swing.JPanel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

		javax.swing.GroupLayout jPanelExtraLayout = new javax.swing.GroupLayout(jPanelExtra);
		jPanelExtra.setLayout(jPanelExtraLayout);
		jPanelExtraLayout.setHorizontalGroup(
		jPanelExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGap(0, 606, Short.MAX_VALUE)
		);
		jPanelExtraLayout.setVerticalGroup(
		jPanelExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGap(0, 435, Short.MAX_VALUE)
		);

		javax.swing.GroupLayout jPanelInfoLayout = new javax.swing.GroupLayout(jPanelInfo);
		jPanelInfo.setLayout(jPanelInfoLayout);
		jPanelInfoLayout.setHorizontalGroup(
		jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGap(0, 0, Short.MAX_VALUE)
		);
		jPanelInfoLayout.setVerticalGroup(
		jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGap(0, 0, Short.MAX_VALUE)
		);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addComponent(jPanelExtra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		.addComponent(jPanelInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
		.addGap(6, 6, 6)
		.addComponent(jPanelExtra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jPanelInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
		);

		pack();
	}// </editor-fold>//GEN-END:initComponents
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanelExtra;
	// End of variables declaration//GEN-END:variables
}
