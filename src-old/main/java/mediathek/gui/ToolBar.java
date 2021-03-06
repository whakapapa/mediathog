
package mediathog.gui;

import mSearch.filmeSuchen.ListenerFilmeLaden;
import mSearch.filmeSuchen.ListenerFilmeLadenEvent;
import mSearch.tool.Listener;
import mediathog.MediathekGui;
import mediathog.config.Daten;
import mediathog.config.Icons;
import mediathog.config.MVConfig;
import mediathog.gui.messages.FilmListWriteStartEvent;
import mediathog.gui.messages.FilmListWriteStopEvent;
import net.engio.mbassy.listener.Handler;

import javax.swing.Box.Filler;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

@SuppressWarnings("serial")
public final class ToolBar extends JToolBar {

	private final Filler filler__5 = new Filler(new Dimension(5, 20), new Dimension(5, 20), new Dimension(5, 32767));
	private final Filler filler__10 = new Filler(new Dimension(10, 20), new Dimension(10, 20), new Dimension(10, 32767));
	private final Filler filler__trenner = new Filler(new Dimension(1, 5), new Dimension(1, 5), new Dimension(32767, 5));

	private MVButton jButtonDownloadAktualisieren = null;
	private MVButton jButtonFilmlisteLaden = null;

	private final MVConfig.Configs nrToolbar;
	private MVConfig.Configs nrIconKlein = MVConfig.Configs.SYSTEM_ICON_KLEIN;
	private final Daten daten;
	private final BeobMausToolBar beobMausToolBar = new BeobMausToolBar();
	//private boolean extern;
	private final MediathekGui.TABS state;
	private final ArrayList<MVButton> buttonList = new ArrayList<>();

	public ToolBar(Daten ddaten, MediathekGui.TABS state) {
		// für die Toolbar der Externen Fenster
		//extern = true;
		daten = ddaten;
		this.state = state;
		switch (state) {
			case TAB_FILME:
			nrToolbar = MVConfig.Configs.SYSTEM_TOOLBAR_FILME;
			break;
			case TAB_DOWNLOADS:
			nrToolbar = MVConfig.Configs.SYSTEM_TOOLBAR_DOWNLOAD;
			break;
			case TAB_ABOS:
			nrToolbar = MVConfig.Configs.SYSTEM_TOOLBAR_ABO;
			break;
			default:
			nrToolbar = null;
			nrIconKlein = null;
		}
		startup();
		setToolbar();
		Listener.addListener(new Listener(Listener.EREIGNIS_TOOLBAR_BUTTON_KLEIN, ToolBar.class.getSimpleName() + state) {
			@Override
			public void ping() {
				setIcon(Boolean.parseBoolean(MVConfig.get(nrIconKlein)));
			}
		});

		ddaten.getMessageBus().subscribe(this);
	}

	private void startup() {
		// init
		setFloatable(false);

		switch (state) {
			case TAB_DOWNLOADS:
			startupDownload();
			break;
			case TAB_ABOS:
			startupAbo();
			break;
			default:
			break;
		}

		add(filler__10);
		// Icons
		setIcon(Boolean.parseBoolean(MVConfig.get(nrIconKlein)));
		loadVisible();
		addMouseListener(beobMausToolBar);
	}

	private void startupDownload() {
		// init
		setFilmlisteLaden();
		MVButton jButtonInfo = new MVButton("Filminformation anzeigen", "Filminformation anzeigen", Icons.ICON_TOOLBAR_DOWNLOAD_FILM_INFO_GR, Icons.ICON_TOOLBAR_DOWNLOAD_FILM_INFO_KL);
		jButtonDownloadAktualisieren = new MVButton("Liste der Downloads aktualisieren", "Liste der Downloads aktualisieren", Icons.ICON_TOOLBAR_DOWNLOAD_REFRESH_GR, Icons.ICON_TOOLBAR_DOWNLOAD_REFRESH_KL);
		MVButton jButtonDownloadAlleStarten = new MVButton("alle Downloads starten", "alle Downloads starten", Icons.ICON_TOOLBAR_DOWNLOAD_ALLE_STARTEN_GR, Icons.ICON_TOOLBAR_DOWNLOAD_ALLE_STARTEN_KL);
		MVButton jButtonDownloadFilmStarten = new MVButton("Film Starten", "gespeicherten Film abspielen", Icons.ICON_TOOLBAR_DOWNLOAD_FILM_START_GR, Icons.ICON_TOOLBAR_DOWNLOAD_FILM_START_KL);
		MVButton jButtonDownloadZurueckstellen = new MVButton("Downloads zurückstellen", "Downloads zurückstellen", Icons.ICON_TOOLBAR_DOWNLOAD_UNDO_GR, Icons.ICON_TOOLBAR_DOWNLOAD_UNDO_KL);
		MVButton jButtonDownloadLoeschen = new MVButton("Downloads aus Liste entfernen", "Downloads aus Liste entfernen", Icons.ICON_TOOLBAR_DOWNLOAD_DEL_GR, Icons.ICON_TOOLBAR_DOWNLOAD_DEL_KL);
		MVButton jButtonDownloadAufraeumen = new MVButton("Liste der Downloads aufräumen", "Liste der Downloads aufräumen", Icons.ICON_TOOLBAR_DOWNLOAD_CLEAR_GR, Icons.ICON_TOOLBAR_DOWNLOAD_CLEAR_KL);
		this.add(filler__10);
		this.add(jButtonInfo);
		this.add(filler__10);
		this.add(jButtonDownloadAktualisieren);
		this.add(jButtonDownloadAlleStarten);
		this.add(jButtonDownloadFilmStarten);
		this.add(jButtonDownloadZurueckstellen);
		this.add(jButtonDownloadLoeschen);
		this.add(jButtonDownloadAufraeumen);
		jButtonInfo.addActionListener(e -> Daten.filmInfo.showInfo());
		jButtonDownloadAktualisieren.addActionListener(e -> daten.getMediathekGui().tabDownloads.aktualisieren());
		jButtonDownloadAufraeumen.addActionListener(e -> daten.getMediathekGui().tabDownloads.aufraeumen());
		jButtonDownloadLoeschen.addActionListener(e -> daten.getMediathekGui().tabDownloads.loeschen());
		jButtonDownloadAlleStarten.addActionListener(e -> daten.getMediathekGui().tabDownloads.starten(true));
		jButtonDownloadFilmStarten.addActionListener(e -> daten.getMediathekGui().tabDownloads.filmAbspielen());
		jButtonDownloadZurueckstellen.addActionListener(e -> daten.getMediathekGui().tabDownloads.zurueckstellen());

		this.add(filler__trenner);

		// Button Filter
		JButton jButtonFilterPanel = new JButton();
		jButtonFilterPanel.setToolTipText("Filter anzeigen/ausblenden");
		jButtonFilterPanel.setBorder(null);
		jButtonFilterPanel.setBorderPainted(false);
		jButtonFilterPanel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		jButtonFilterPanel.setMaximumSize(new java.awt.Dimension(40, 40));
		jButtonFilterPanel.setMinimumSize(new java.awt.Dimension(40, 40));
		jButtonFilterPanel.setOpaque(false);
		jButtonFilterPanel.setPreferredSize(new java.awt.Dimension(40, 40));
		jButtonFilterPanel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		jButtonFilterPanel.setIcon(Icons.ICON_BUTTON_FILTER_ANZEIGEN);
		this.add(jButtonFilterPanel);
		jButtonFilterPanel.addActionListener(e -> {
			boolean b = !Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_TAB_DOWNLOAD_FILTER_VIS));
			MVConfig.add(MVConfig.Configs.SYSTEM_TAB_DOWNLOAD_FILTER_VIS, Boolean.toString(b));
			Listener.notify(Listener.EREIGNIS_PANEL_DOWNLOAD_FILTER_ANZEIGEN, ToolBar.class.getName());
		});

	}

	private void startupAbo() {
		// init
		MVButton jButtonAbosEinschalten = new MVButton("Abos einschalten", "Abos einschalten", Icons.ICON_TOOLBAR_ABO_EIN_GR, Icons.ICON_TOOLBAR_ABO_EIN_KL);
		MVButton jButtonAbosAusschalten = new MVButton("Abos ausschalten", "Abos ausschalten", Icons.ICON_TOOLBAR_ABO_AUS_GR, Icons.ICON_TOOLBAR_ABO_AUS_KL);
		MVButton jButtonAbosLoeschen = new MVButton("Abos löschen", "Abos löschen", Icons.ICON_TOOLBAR_ABO_DEL_GR, Icons.ICON_TOOLBAR_ABO_DEL_KL);
		MVButton jButtonAboAendern = new MVButton("Abos ändern", "Abos ändern", Icons.ICON_TOOLBAR_ABO_CONFIG_GR, Icons.ICON_TOOLBAR_ABO_CONFIG_KL);
		this.add(filler__10);
		this.add(jButtonAbosEinschalten);
		this.add(jButtonAbosAusschalten);
		this.add(jButtonAbosLoeschen);
		this.add(jButtonAboAendern);
		jButtonAbosEinschalten.addActionListener(e -> daten.getMediathekGui().tabAbos.einAus(true));
		jButtonAbosAusschalten.addActionListener(e -> daten.getMediathekGui().tabAbos.einAus(false));
		jButtonAbosLoeschen.addActionListener(e -> daten.getMediathekGui().tabAbos.loeschen());
		jButtonAboAendern.addActionListener(e -> daten.getMediathekGui().tabAbos.aendern());

		this.add(filler__trenner);

		// Button Filter
		JButton jButtonFilterPanel = new JButton();
		jButtonFilterPanel.setToolTipText("Filter anzeigen/ausblenden");
		jButtonFilterPanel.setBorder(null);
		jButtonFilterPanel.setBorderPainted(false);
		jButtonFilterPanel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		jButtonFilterPanel.setMaximumSize(new java.awt.Dimension(40, 40));
		jButtonFilterPanel.setMinimumSize(new java.awt.Dimension(40, 40));
		jButtonFilterPanel.setOpaque(false);
		jButtonFilterPanel.setPreferredSize(new java.awt.Dimension(40, 40));
		jButtonFilterPanel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		jButtonFilterPanel.setIcon(Icons.ICON_BUTTON_FILTER_ANZEIGEN);
		this.add(jButtonFilterPanel);
		jButtonFilterPanel.addActionListener(e -> {
			boolean b = !Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_TAB_ABO_FILTER_VIS));
			MVConfig.add(MVConfig.Configs.SYSTEM_TAB_ABO_FILTER_VIS, Boolean.toString(b));
			Listener.notify(Listener.EREIGNIS_PANEL_ABO_FILTER_ANZEIGEN, ToolBar.class.getName());
		});

	}

	@Handler
	private void handleFilmListWriteStartEvent(FilmListWriteStartEvent e) {
		SwingUtilities.invokeLater(() -> {
			if (jButtonFilmlisteLaden != null)
			jButtonFilmlisteLaden.setEnabled(false);
		});
	}

	@Handler
	private void handleFilmListWriteStopEvent(FilmListWriteStopEvent e) {
		SwingUtilities.invokeLater(() -> {
			if (jButtonFilmlisteLaden != null)
			jButtonFilmlisteLaden.setEnabled(true);
		});
	}

	private void setFilmlisteLaden() {
		jButtonFilmlisteLaden = new MVButton("Filmliste laden", "neue Filmliste laden", Icons.ICON_TOOLBAR_FILME_FILMLISTE_LADEN_GR, Icons.ICON_TOOLBAR_FILME_FILMLISTE_LADEN_KL);
		this.add(filler__5);
		this.add(jButtonFilmlisteLaden);
		daten.getFilmeLaden().addAdListener(new ListenerFilmeLaden() {
			@Override
			public void start(ListenerFilmeLadenEvent event) {
				//ddaten.infoPanel.setProgress();
				jButtonFilmlisteLaden.setEnabled(false);
				if (jButtonDownloadAktualisieren != null) {
					jButtonDownloadAktualisieren.setEnabled(false);
				}
			}

			@Override
			public void fertig(ListenerFilmeLadenEvent event) {
				jButtonFilmlisteLaden.setEnabled(true);
				if (jButtonDownloadAktualisieren != null) {
					jButtonDownloadAktualisieren.setEnabled(true);
				}
			}
		});
		jButtonFilmlisteLaden.addActionListener(e -> daten.getFilmeLaden().loadFilmlistDialog(daten, false));
		jButtonFilmlisteLaden.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				if (arg0.isPopupTrigger()) {
					if (jButtonFilmlisteLaden.isEnabled()) {
						daten.getFilmeLaden().loadFilmlistDialog(daten, true);
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				if (arg0.isPopupTrigger()) {
					if (jButtonFilmlisteLaden.isEnabled()) {
						daten.getFilmeLaden().loadFilmlistDialog(daten, true);
					}
				}
			}
		});

	}

	private void setIcon(boolean klein) {
		MVConfig.add(nrIconKlein, Boolean.toString(klein));
		beobMausToolBar.itemKlein.setSelected(klein);
		for (MVButton b : buttonList) {
			b.setIcon();
		}
		this.repaint();
	}

	private void setToolbar() {
		for (MVButton b : buttonList) {
			b.setVisible(b.anzeigen);
		}
	}

	private void loadVisible() {
		if (nrToolbar != null) {
			String[] b = MVConfig.get(nrToolbar).split(":");
			if (buttonList.size() == b.length) {
				// ansonsten gibt es neue Button: dann alle anzeigen
				for (int i = 0; i < b.length; ++i) {
					buttonList.get(i).anzeigen = Boolean.parseBoolean(b[i]);
					buttonList.get(i).setVisible(Boolean.parseBoolean(b[i]));
				}
			}
		}
		setToolbar();
		if (nrIconKlein != null) {
			setIcon(Boolean.parseBoolean(MVConfig.get(nrIconKlein)));
		}
	}

	private void storeVisible() {
		if (nrToolbar != null) {
			MVConfig.add(nrToolbar, "");
			for (MVButton b : buttonList) {
				if (!MVConfig.get(nrToolbar).isEmpty()) {
					MVConfig.add(nrToolbar, MVConfig.get(nrToolbar) + ':');
				}
				MVConfig.add(nrToolbar, MVConfig.get(nrToolbar) + Boolean.toString(b.anzeigen));
			}
		}
	}

	private class MVButton extends JButton {
		boolean anzeigen = true;
		private final String name;
		private final ImageIcon imageIconKlein;
		private final ImageIcon imageIconNormal;

		public MVButton(String nname, String ttoolTip,
		ImageIcon iimageIconNormal, ImageIcon iimageIconKlein) {
			setToolTipText(ttoolTip);
			name = nname;
			imageIconKlein = iimageIconKlein;
			imageIconNormal = iimageIconNormal;
			setOpaque(false);
			setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
			setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
			setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
			buttonList.add(this);
		}

		void setIcon() {
			if (nrIconKlein != null) {
				if (Boolean.parseBoolean(MVConfig.get(nrIconKlein))) {
					this.setIcon(imageIconKlein);
				} else {
					this.setIcon(imageIconNormal);
				}
			}
		}
	}

	private class BeobMausToolBar extends MouseAdapter {

		JCheckBoxMenuItem itemKlein = new JCheckBoxMenuItem("kleine Icons");
		JMenuItem itemReset = new JMenuItem("zurücksetzen");
		JCheckBoxMenuItem[] checkBoxMenuItems;

		public BeobMausToolBar() {
			if (nrIconKlein != null) {
				itemKlein.setSelected(Boolean.parseBoolean(MVConfig.get(nrIconKlein)));
			}
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			if (arg0.isPopupTrigger()) {
				showMenu(arg0);
			}
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if (arg0.isPopupTrigger()) {
				showMenu(arg0);
			}
		}

		private void showMenu(MouseEvent evt) {
			JPopupMenu jPopupMenu = new JPopupMenu();
			itemKlein.addActionListener(e -> {
				setIcon(itemKlein.isSelected());
				Listener.notify(Listener.EREIGNIS_TOOLBAR_BUTTON_KLEIN, ToolBar.class.getSimpleName() + state);
			});
			jPopupMenu.add(itemKlein);
			//##Trenner##
			jPopupMenu.addSeparator();
			//##Trenner##

			// Spalten ein-ausschalten
			checkBoxMenuItems = new JCheckBoxMenuItem[buttonList.size()];
			for (int i = 0; i < checkBoxMenuItems.length; ++i) {
				checkBoxMenuItems[i] = null;
				checkBoxMenuItems[i] = new JCheckBoxMenuItem(buttonList.get(i).name);
				if (checkBoxMenuItems[i] != null) {
					checkBoxMenuItems[i] = new JCheckBoxMenuItem(buttonList.get(i).name);
					checkBoxMenuItems[i].setIcon(buttonList.get(i).imageIconKlein);
					checkBoxMenuItems[i].setSelected(buttonList.get(i).anzeigen);
					checkBoxMenuItems[i].addActionListener(e -> {
						setButtonList();
						storeVisible();
					});
					jPopupMenu.add(checkBoxMenuItems[i]);
				}
			}
			//##Trenner##
			jPopupMenu.addSeparator();
			//##Trenner##
			itemReset.addActionListener(e -> {
				resetToolbar();
				storeVisible();
				Listener.notify(Listener.EREIGNIS_TOOLBAR_BUTTON_KLEIN, ToolBar.class.getSimpleName() + state);
			});
			jPopupMenu.add(itemReset);

			//anzeigen
			jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}

		private void setButtonList() {
			if (checkBoxMenuItems == null) {
				return;
			}
			for (int i = 0; i < checkBoxMenuItems.length; ++i) {
				if (checkBoxMenuItems[i] == null) {
					continue;
				}
				buttonList.get(i).anzeigen = checkBoxMenuItems[i].isSelected();
				buttonList.get(i).setVisible(checkBoxMenuItems[i].isSelected());
			}
			setToolbar();
		}

		private void resetToolbar() {
			if (checkBoxMenuItems == null) {
				return;
			}
			for (int i = 0; i < checkBoxMenuItems.length; ++i) {
				if (checkBoxMenuItems[i] == null) {
					continue;
				}
				buttonList.get(i).anzeigen = true;
				buttonList.get(i).setVisible(true);
			}
			setToolbar();
			setIcon(false);
		}

	}
}
