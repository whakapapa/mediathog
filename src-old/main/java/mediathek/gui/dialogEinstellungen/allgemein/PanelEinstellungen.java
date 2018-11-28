
package mediathek.gui.dialogEinstellungen.allgemein;

import com.jidesoft.utils.SystemInfo;
import mSearch.tool.ApplicationConfiguration;
import mSearch.tool.Listener;
import mSearch.tool.Log;
import mediathek.config.Daten;
import mediathek.config.Icons;
import mediathek.config.MVConfig;
import mediathek.gui.PanelVorlage;
import mediathek.gui.dialog.DialogHilfe;
import mediathek.gui.messages.InstallTabSwitchListenerEvent;
import mediathek.tool.MVFunctionSys;
import mediathek.tool.MVMessageDialog;
import org.apache.commons.configuration2.Configuration;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.NoSuchElementException;

@SuppressWarnings("serial")
public class PanelEinstellungen extends PanelVorlage {
	private final static String ICONSET_STANDARD = "Standard";
	private final static String ALLE = " Alle ";
	private final Configuration config = ApplicationConfiguration.getConfiguration();

	private void setupProxySettings() {

		jtfProxyHost.setText(config.getString(ApplicationConfiguration.HTTP_PROXY_HOSTNAME, ""));
		jtfProxyHost.getDocument().addDocumentListener(new ProxyHostListener(jtfProxyHost));

		jtfProxyPort.setText(config.getString(ApplicationConfiguration.HTTP_PROXY_PORT, ""));
		jtfProxyPort.getDocument().addDocumentListener(new ProxyPortListener(jtfProxyPort));

		jtfProxyUser.setText(config.getString(ApplicationConfiguration.HTTP_PROXY_USERNAME, ""));
		jtfProxyUser.getDocument().addDocumentListener(new ProxyUsernameListener(jtfProxyUser));

		jpfProxyPassword.setText(config.getString(ApplicationConfiguration.HTTP_PROXY_PASSWORD, ""));
		jpfProxyPassword.getDocument().addDocumentListener(new ProxyPasswordListener(jpfProxyPassword));
	}

	private void setupUserAgentSettings() {
		jtfUserAgent.setText(ApplicationConfiguration.getConfiguration().getString(ApplicationConfiguration.APPLICATION_USER_AGENT));
		jtfUserAgent.getDocument().addDocumentListener(new UserAgentListener(jtfUserAgent));
	}

	private void setupDays() {
		jButtonHelpDays.setIcon(Icons.ICON_BUTTON_HELP);
		jButtonHelpDays.addActionListener(e -> new DialogHilfe(parentComponent, true, '\n'
		+ "Es werden nur Filme der letzten\n"
		+ "xx Tage geladen."
		+ '\n'
		+ "Bei \"Alle\" werden alle Filme geladen.\n"
		+ '\n'
		+ "(Eine kleinere Filmliste\n"
		+ "kann bei Rechnern mit wenig\n"
		+ "Speicher hilfreich sein.)"
		+ "\n\n"
		+ "Auswirkung hat das erst nach dem\n"
		+ "Neuladen der kompletten Filmliste.").setVisible(true));

		SpinnerListModel lm = new SpinnerListModel(new Object[]{ALLE, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
		"12", "14", "16", "18", "20", "25", "30"});
		jSpinnerDays.setModel(lm);
		((JSpinner.DefaultEditor) jSpinnerDays.getEditor()).getTextField().setEditable(false);
		initSpinner();
		jSpinnerDays.addChangeListener(new BeobSpinnerDays());
	}

	private void setupTabUI() {
		jCheckBoxTabsTop.setSelected(Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_TABS_TOP)));
		jCheckBoxTabsTop.addActionListener(ae -> {
			MVConfig.add(MVConfig.Configs.SYSTEM_TABS_TOP, Boolean.toString(jCheckBoxTabsTop.isSelected()));
			Listener.notify(Listener.EREIGNIS_TABS_TOP, PanelEinstellungen.class.getSimpleName());
		});
		jCheckBoxTabIcon.setSelected(Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_TABS_ICON)));
		jCheckBoxTabIcon.addActionListener(ae -> {
			MVConfig.add(MVConfig.Configs.SYSTEM_TABS_ICON, Boolean.toString(jCheckBoxTabIcon.isSelected()));
			Listener.notify(Listener.EREIGNIS_TABS_TOP, PanelEinstellungen.class.getSimpleName());
		});
	}

	private void setupTray() {

		Listener.addListener(new Listener(Listener.EREIGNIS_TRAYICON, PanelEinstellungen.class.getSimpleName()) {
			@Override
			public void ping() {
				jCheckBoxTray.setSelected(Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_USE_TRAY)));
			}
		});
		jCheckBoxTray.setSelected(Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_USE_TRAY)));
		jCheckBoxTray.addActionListener(ae -> {
			MVConfig.add(MVConfig.Configs.SYSTEM_USE_TRAY, Boolean.toString(jCheckBoxTray.isSelected()));
			daten.getMediathekGui().setTray();
		});

	}

	private void setupDatabaseCleanerCheckbox() {
		final Configuration config = ApplicationConfiguration.getConfiguration();
		cbUseDatabaseCleaner.setSelected(config.getBoolean(ApplicationConfiguration.DATABASE_USE_CLEANER_INTERFACE, false));
		cbUseDatabaseCleaner.addActionListener(l -> config.setProperty(ApplicationConfiguration.DATABASE_USE_CLEANER_INTERFACE, cbUseDatabaseCleaner.isSelected()));
	}

	private void setupSaveHumanReadableFilmlistCheckbox() {
		final Configuration config = ApplicationConfiguration.getConfiguration();
		cbSaveHumanReadableFilmlist.setSelected(config.getBoolean(ApplicationConfiguration.FILMLISTE_SAVE_HUMAN_READABLE, false));
		cbSaveHumanReadableFilmlist.addActionListener(l -> config.setProperty(ApplicationConfiguration.FILMLISTE_SAVE_HUMAN_READABLE, cbSaveHumanReadableFilmlist.isSelected()));
	}

	public PanelEinstellungen(Daten d, JFrame parent) {
		super(d, parent);
		daten = d;

		initComponents();

		setupUserAgentSettings();

		setupProxySettings();

		setupDatabaseCleanerCheckbox();

		setupSaveHumanReadableFilmlistCheckbox();

		jButtonLoad.addActionListener(ae -> {
			daten.getListeFilme().clear(); // sonst wird evtl. nur eine Diff geladen
			daten.getFilmeLaden().loadFilmlist("");
		});

		setupLookAndFeelComboBox();

		setupDays();

		Listener.addListener(new Listener(Listener.EREIGNIS_ANZAHL_DOWNLOADS, PanelEinstellungen.class.getSimpleName()) {
			@Override
			public void ping() {
				initSpinner();
			}
		});

		setupTabUI();

		setupTray();

		jButtonRefresh.addActionListener(e -> fillIconList());

		fillIconList();
		jComboBoxIcons.addItemListener(evt -> {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				cbxIconPackagesItemStateChanged(evt);
			}
		});

		setupTabSwitchListener();
	}

	private void setupTabSwitchListener() {
		boolean installed;
		try {
			installed = config.getBoolean(ApplicationConfiguration.APPLICATION_INSTALL_TAB_SWITCH_LISTENER);
		} catch (NoSuchElementException ex) {
			installed = true;
			config.setProperty(ApplicationConfiguration.APPLICATION_INSTALL_TAB_SWITCH_LISTENER, true);
		}
		cbAutomaticMenuTabSwitching.setSelected(installed);

		cbAutomaticMenuTabSwitching.addActionListener(e -> {
			final boolean isOn = cbAutomaticMenuTabSwitching.isSelected();
			config.setProperty(ApplicationConfiguration.APPLICATION_INSTALL_TAB_SWITCH_LISTENER, isOn);
			final InstallTabSwitchListenerEvent evt = new InstallTabSwitchListenerEvent();
			if (isOn) {
				evt.event = InstallTabSwitchListenerEvent.INSTALL_TYPE.INSTALL;
			} else {
				evt.event = InstallTabSwitchListenerEvent.INSTALL_TYPE.REMOVE;
			}
			daten.getMessageBus().publishAsync(evt);
		});

	}

	private void cbxIconPackagesItemStateChanged(ItemEvent evt) {
		MVMessageDialog.showMessageDialog(this, "Sie müssen die Applikation neu starten damit die Icons genutzt werden können.", "Mediathog", JOptionPane.WARNING_MESSAGE);
		String iconName = jComboBoxIcons.getModel().getElementAt(jComboBoxIcons.getSelectedIndex());
		if (iconName.equals(ICONSET_STANDARD)) {
			MVConfig.add(MVConfig.Configs.SYSTEM_ICON_STANDARD, Boolean.TRUE.toString());
			MVConfig.add(MVConfig.Configs.SYSTEM_ICON_PFAD, "");
		} else {
			MVConfig.add(MVConfig.Configs.SYSTEM_ICON_STANDARD, Boolean.FALSE.toString());
		}
		try {
			File[] files = new File(MVFunctionSys.pathProgramIcons()).listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory() && file.getName().equals(iconName)) {
						MVConfig.add(MVConfig.Configs.SYSTEM_ICON_PFAD, file.getAbsolutePath());
						break;
					}
				}
			}
		} catch (Exception ex) {
			MVConfig.add(MVConfig.Configs.SYSTEM_ICON_STANDARD, Boolean.TRUE.toString());
			MVConfig.add(MVConfig.Configs.SYSTEM_ICON_PFAD, "");
			Log.errorLog(829304789, ex);
		}
	}

	private void initSpinner() {
		if (MVConfig.get(MVConfig.Configs.SYSTEM_ANZ_TAGE_FILMLISTE).isEmpty()) {
			MVConfig.add(MVConfig.Configs.SYSTEM_ANZ_TAGE_FILMLISTE, "0");
		}
		String s = MVConfig.get(MVConfig.Configs.SYSTEM_ANZ_TAGE_FILMLISTE);
		if (s.equals("0")) {
			s = ALLE;
		}
		jSpinnerDays.setValue(s);
	}

	private void setupLookAndFeelComboBox() {
		try {
			//query all installed LAFs
			final UIManager.LookAndFeelInfo info[];
			info = UIManager.getInstalledLookAndFeels();
			LookAndFeel aktLaf = UIManager.getLookAndFeel();
			String classNameAktLaf = aktLaf.getClass().getName();
			int idx = 0;

			//fill in the combobox model
			ArrayList<String> themeList = new ArrayList<>(); // list of "UIManager.LookAndFeelInfo" names
			for (int i = 0; i < info.length; ++i) {
				themeList.add(info[i].getName());
				// LookAndFeelInfo.getName und LookAndFeel.getName sind beim GTK LF nicht gleich??
				if (info[i].getClassName().equals(classNameAktLaf)) {
					idx = i;
				}
			}
			DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(themeList.toArray(new String[0]));
			jComboBoxLookAndFeel.setModel(model);
			jComboBoxLookAndFeel.setSelectedIndex(idx);

			ActionListener lst = actionEvent -> {
				String lafName = jComboBoxLookAndFeel.getModel().getElementAt(jComboBoxLookAndFeel.getSelectedIndex());
				String lafClass = "";
				//retrieve class name for selected LAF
				for (UIManager.LookAndFeelInfo i : info) {
					if (i.getName().equals(lafName)) {
						lafClass = i.getClassName();
						break;
					}
				}
				//and now switch it...
				try {
					UIManager.setLookAndFeel(lafClass);
					SwingUtilities.updateComponentTreeUI(daten.getMediathekGui());
					for (Frame f : Frame.getFrames()) {
						SwingUtilities.updateComponentTreeUI(f);
						for (Window w : f.getOwnedWindows()) {
							SwingUtilities.updateComponentTreeUI(w);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				MVConfig.add(MVConfig.Configs.SYSTEM_LOOK, lafClass); //
			};
			jComboBoxLookAndFeel.addActionListener(lst);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void fillIconList() {
		final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
		model.addElement(ICONSET_STANDARD);
		try {
			File[] files = new File(MVFunctionSys.pathProgramIcons()).listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						model.addElement(file.getName());
					}
				}
			}
		} catch (Exception ex) {
			Log.errorLog(636875409, ex);
		}

		jComboBoxIcons.setModel(model);

		if (!Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_ICON_STANDARD))) {
			if (!MVConfig.get(MVConfig.Configs.SYSTEM_ICON_PFAD).isEmpty()) {
				File f = new File(MVConfig.get(MVConfig.Configs.SYSTEM_ICON_PFAD));
				jComboBoxIcons.setSelectedItem(f.getName());
			}
		}
	}


	private class BeobSpinnerDays implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			String s = jSpinnerDays.getModel().getValue().toString();
			if (s.equals(ALLE)) {
				s = "0";
			}
			MVConfig.add(MVConfig.Configs.SYSTEM_ANZ_TAGE_FILMLISTE, s);
		}
	}

	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		javax.swing.JPanel jPanel5 = new javax.swing.JPanel();
		jCheckBoxTabsTop = new javax.swing.JCheckBox();
		jCheckBoxTabIcon = new javax.swing.JCheckBox();
		cbAutomaticMenuTabSwitching = new javax.swing.JCheckBox();
		javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
		javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
		jtfUserAgent = new javax.swing.JTextField();
		javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
		javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
		jtfProxyHost = new javax.swing.JTextField();
		javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
		jtfProxyPort = new javax.swing.JTextField();
		javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
		jtfProxyUser = new javax.swing.JTextField();
		javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
		jpfProxyPassword = new javax.swing.JPasswordField();
		javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
		jCheckBoxTray = new javax.swing.JCheckBox();
		javax.swing.JPanel jPanel6 = new javax.swing.JPanel();
		javax.swing.JLabel jLabel6 = new javax.swing.JLabel();
		jSpinnerDays = new javax.swing.JSpinner();
		jButtonHelpDays = new javax.swing.JButton();
		jButtonLoad = new javax.swing.JButton();
		javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
		javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
		jComboBoxLookAndFeel = new javax.swing.JComboBox<>();
		javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
		jComboBoxIcons = new javax.swing.JComboBox<>();
		jButtonRefresh = new javax.swing.JButton();
		javax.swing.JPanel jPanel7 = new javax.swing.JPanel();
		cbUseDatabaseCleaner = new javax.swing.JCheckBox();
		javax.swing.JPanel jPanel8 = new javax.swing.JPanel();
		cbSaveHumanReadableFilmlist = new javax.swing.JCheckBox();

		setMaximumSize(new java.awt.Dimension(10, 10));
		setMinimumSize(getMaximumSize());

		jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Tab-Verhalten"));

		jCheckBoxTabsTop.setText("Tabs oben anzeigen");

		jCheckBoxTabIcon.setText("Icons anzeigen");
		jCheckBoxTabIcon.setToolTipText("Im Tab keine Icons anzeigen");

		cbAutomaticMenuTabSwitching.setText("Tabs schalten automatisch bei Menünutzung um");

		javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
		jPanel5.setLayout(jPanel5Layout);
		jPanel5Layout.setHorizontalGroup(
		jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel5Layout.createSequentialGroup()
		.addGap(5, 5, 5)
		.addComponent(jCheckBoxTabsTop)
		.addGap(5, 5, 5)
		.addComponent(jCheckBoxTabIcon)
		.addGap(5, 5, 5)
		.addComponent(cbAutomaticMenuTabSwitching))
		);
		jPanel5Layout.setVerticalGroup(
		jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel5Layout.createSequentialGroup()
		.addGap(5, 5, 5)
		.addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addComponent(jCheckBoxTabsTop)
		.addComponent(jCheckBoxTabIcon)
		.addComponent(cbAutomaticMenuTabSwitching)))
		);

		jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Download"));

		jLabel3.setText("User-Agent:");

		jtfUserAgent.setMinimumSize(new java.awt.Dimension(200, 26));
		jtfUserAgent.setPreferredSize(new java.awt.Dimension(520, 26));

		javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(
		jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel3Layout.createSequentialGroup()
		.addGap(5, 5, 5)
		.addComponent(jLabel3)
		.addGap(5, 5, 5)
		.addComponent(jtfUserAgent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
		);
		jPanel3Layout.setVerticalGroup(
		jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel3Layout.createSequentialGroup()
		.addGap(10, 10, 10)
		.addComponent(jLabel3))
		.addGroup(jPanel3Layout.createSequentialGroup()
		.addGap(5, 5, 5)
		.addComponent(jtfUserAgent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
		);

		jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("HTTP-Proxy (Neustart erforderlich!)"));
		jPanel4.setToolTipText("");

		jLabel4.setText("Host:");

		jLabel5.setText("Port:");

		jLabel7.setText("User:");

		jLabel8.setText("Passwort:");

		javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
		jPanel4.setLayout(jPanel4Layout);
		jPanel4Layout.setHorizontalGroup(
		jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel4Layout.createSequentialGroup()
		.addContainerGap()
		.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
		.addGroup(jPanel4Layout.createSequentialGroup()
		.addComponent(jLabel4)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jtfProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
		.addGroup(jPanel4Layout.createSequentialGroup()
		.addComponent(jLabel7)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jtfProxyUser)))
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
		.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
		.addComponent(jLabel8)
		.addComponent(jLabel5))
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel4Layout.createSequentialGroup()
		.addComponent(jtfProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addGap(0, 152, Short.MAX_VALUE))
		.addComponent(jpfProxyPassword))
		.addContainerGap())
		);
		jPanel4Layout.setVerticalGroup(
		jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel4Layout.createSequentialGroup()
		.addContainerGap()
		.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
		.addComponent(jLabel4)
		.addComponent(jtfProxyHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addComponent(jLabel5)
		.addComponent(jtfProxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
		.addComponent(jLabel7)
		.addComponent(jtfProxyUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addComponent(jLabel8)
		.addComponent(jpfProxyPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
		.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);

		jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

		jCheckBoxTray.setText("Programm ins Tray minimieren");

		jLabel6.setText("nur die Filme der letzten Tage laden:");

		jSpinnerDays.setModel(new javax.swing.SpinnerListModel(new String[]{"Alles", "1", "2", "10", "15"}));

		jButtonHelpDays.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ico/buttons/help.png"))); // NOI18N
		jButtonHelpDays.setToolTipText("Hilfe anzeigen");

		jButtonLoad.setText("Filmliste jetzt neu laden");

		javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
		jPanel6.setLayout(jPanel6Layout);
		jPanel6Layout.setHorizontalGroup(
		jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel6Layout.createSequentialGroup()
		.addGap(5, 5, 5)
		.addComponent(jLabel6)
		.addGap(5, 5, 5)
		.addComponent(jSpinnerDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addGap(5, 5, 5)
		.addComponent(jButtonHelpDays)
		.addGap(5, 5, 5)
		.addComponent(jButtonLoad))
		);
		jPanel6Layout.setVerticalGroup(
		jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel6Layout.createSequentialGroup()
		.addGap(11, 11, 11)
		.addComponent(jLabel6))
		.addGroup(jPanel6Layout.createSequentialGroup()
		.addGap(6, 6, 6)
		.addComponent(jSpinnerDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
		.addGroup(jPanel6Layout.createSequentialGroup()
		.addGap(5, 5, 5)
		.addComponent(jButtonHelpDays))
		.addGroup(jPanel6Layout.createSequentialGroup()
		.addGap(5, 5, 5)
		.addComponent(jButtonLoad))
		);

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(
		jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel2Layout.createSequentialGroup()
		.addGap(5, 5, 5)
		.addComponent(jCheckBoxTray)
		.addGap(5, 5, 5)
		.addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
		);
		jPanel2Layout.setVerticalGroup(
		jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel2Layout.createSequentialGroup()
		.addGap(13, 13, 13)
		.addComponent(jCheckBoxTray))
		.addGroup(jPanel2Layout.createSequentialGroup()
		.addGap(5, 5, 5)
		.addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
		);

		jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

		jLabel2.setText("Look&Feel:");

		jLabel1.setText("Icon-Pack:");

		jButtonRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/res/ico/buttons/update.png"))); // NOI18N
		jButtonRefresh.setToolTipText("neue Icons suchen");

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(
		jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel1Layout.createSequentialGroup()
		.addContainerGap()
		.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addComponent(jLabel2)
		.addComponent(jLabel1))
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
		.addComponent(jComboBoxIcons, 0, 487, Short.MAX_VALUE)
		.addComponent(jComboBoxLookAndFeel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jButtonRefresh)
		.addContainerGap())
		);
		jPanel1Layout.setVerticalGroup(
		jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel1Layout.createSequentialGroup()
		.addContainerGap()
		.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
		.addComponent(jComboBoxLookAndFeel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addComponent(jLabel2))
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
		.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
		.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
		.addComponent(jComboBoxIcons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addComponent(jLabel1))
		.addComponent(jButtonRefresh))
		.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);

		jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Datenbank (Neustart erforderlich!)"));

		cbUseDatabaseCleaner.setText("Bereinigung während Laufzeit");

		javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
		jPanel7.setLayout(jPanel7Layout);
		jPanel7Layout.setHorizontalGroup(
		jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel7Layout.createSequentialGroup()
		.addContainerGap()
		.addComponent(cbUseDatabaseCleaner)
		.addContainerGap(381, Short.MAX_VALUE))
		);
		jPanel7Layout.setVerticalGroup(
		jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel7Layout.createSequentialGroup()
		.addContainerGap()
		.addComponent(cbUseDatabaseCleaner)
		.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);

		jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Speicherung der Filmliste"));

		cbSaveHumanReadableFilmlist.setText("in les- und editierbarem Format speichern");

		javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
		jPanel8.setLayout(jPanel8Layout);
		jPanel8Layout.setHorizontalGroup(
		jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel8Layout.createSequentialGroup()
		.addContainerGap()
		.addComponent(cbSaveHumanReadableFilmlist)
		.addContainerGap(300, Short.MAX_VALUE))
		);
		jPanel8Layout.setVerticalGroup(
		jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(jPanel8Layout.createSequentialGroup()
		.addContainerGap()
		.addComponent(cbSaveHumanReadableFilmlist)
		.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(layout.createSequentialGroup()
		.addContainerGap()
		.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
		.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		layout.setVerticalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(layout.createSequentialGroup()
		.addContainerGap()
		.addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
	}// </editor-fold>//GEN-END:initComponents


	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JCheckBox cbAutomaticMenuTabSwitching;
	private javax.swing.JCheckBox cbSaveHumanReadableFilmlist;
	private javax.swing.JCheckBox cbUseDatabaseCleaner;
	private javax.swing.JButton jButtonHelpDays;
	private javax.swing.JButton jButtonLoad;
	private javax.swing.JButton jButtonRefresh;
	private javax.swing.JCheckBox jCheckBoxTabIcon;
	private javax.swing.JCheckBox jCheckBoxTabsTop;
	private javax.swing.JCheckBox jCheckBoxTray;
	private javax.swing.JComboBox<String> jComboBoxIcons;
	private javax.swing.JComboBox<String> jComboBoxLookAndFeel;
	private javax.swing.JSpinner jSpinnerDays;
	private javax.swing.JPasswordField jpfProxyPassword;
	private javax.swing.JTextField jtfProxyHost;
	private javax.swing.JTextField jtfProxyPort;
	private javax.swing.JTextField jtfProxyUser;
	private javax.swing.JTextField jtfUserAgent;
	// End of variables declaration//GEN-END:variables
}
