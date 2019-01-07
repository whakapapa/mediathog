
package mediathog.gui.bandwidth;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterAutoUnits;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyForcedPoint;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import mSearch.tool.Listener;
import mediathog.config.Daten;
import mediathog.config.MVConfig;
import mediathog.tool.GuiFunktionen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.TimerTask;

@SuppressWarnings("serial")
public class MVBandwidthMonitorLWin extends JPanel implements IBandwidthMonitor {
	private final Daten daten;
	private double counter = 0; // double sonst "läuft" die Chart nicht
	private final Trace2DLtd m_trace = new Trace2DLtd(300);
	private final IAxis<?> x_achse;
	private final JDialog jDialog;
	private final JFrame frameParent;
	private static Point mouseDownCompCoords;
	private final JPanel panel;

	/**
	* Timer for collecting sample data.
	*/
	private final java.util.Timer timer = new java.util.Timer(false);
	private TimerTask timerTask = null;

	/**
	* Creates new form MVBandwidthMonitorLWin
	*
	**/
	public MVBandwidthMonitorLWin(JFrame parent) {
		initComponents();
		daten = Daten.getInstance();
		this.frameParent = parent;
		this.panel = this;
		jDialog = new JDialog(MVConfig.getBool(MVConfig.Configs.SYSTEM_DOWNLOAD_INFO_TOP) ? parent : null, "Bandbreite");
		jDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		jDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				beenden();
			}
		});

		Chart2D chart = new Chart2D();
		chart.setPaintLabels(true);
		chart.setUseAntialiasing(true);
		chart.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
		jDialog.setBackground(null);
		chart.setOpaque(true);
		this.setOpaque(true);


		x_achse = chart.getAxisX();
		x_achse.getAxisTitle().setTitle("Minuten");
		x_achse.setPaintScale(true);
		x_achse.setVisible(true);
		x_achse.setPaintGrid(false);
		x_achse.setMajorTickSpacing(10);
		x_achse.setMinorTickSpacing(1);

		IAxis<?> y_achse = chart.getAxisY();
		y_achse.getAxisTitle().setTitle("");
		y_achse.setPaintScale(true);
		y_achse.setVisible(true);
		y_achse.setPaintGrid(true);
		y_achse.setMajorTickSpacing(5);
		y_achse.setMinorTickSpacing(1);
		y_achse.setFormatter(new LabelFormatterAutoUnits());
		y_achse.setRangePolicy(new RangePolicyForcedPoint());

		m_trace.setName("");
		m_trace.setColor(Color.RED);
		chart.addTrace(m_trace);
		jPanelChart.setBackground(Color.WHITE);
		jPanelChart.setLayout(new BorderLayout(0, 0));
		jPanelChart.add(chart, BorderLayout.CENTER);

		Listener.addListener(new Listener(Listener.EREIGNIS_BANDWIDTH_MONITOR, MVBandwidthMonitorLWin.class.getSimpleName()) {
			@Override
			public void ping() {
				setVisibility();
			}
		});

		jDialog.setContentPane(this);

		// size
		jPanelChart.setMinimumSize(new Dimension());
		if (!GuiFunktionen.setSize(MVConfig.Configs.SYSTEM_GROESSE_INFODIALOG, jDialog, parent)) {
			// erster Programmstart
			final Dimension dim = jDialog.getSize();
			dim.height = 250;
			dim.width = 400;
			jDialog.setSize(dim);
		}
		BeobMaus bom = new BeobMaus();
		chart.addMouseListener(bom);

		mouseDownCompCoords = null;
		chart.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				mouseDownCompCoords = null;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				mouseDownCompCoords = e.getPoint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		chart.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				Point currCoords = e.getLocationOnScreen();
				jDialog.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
			}
		});

		GuiFunktionen.setDialogDecorated(jDialog, this, MVConfig.getBool(MVConfig.Configs.SYSTEM_DOWNLOAD_INFO_DECORATED));
		setVisibility();
	}

	private void beenden() {
		MVConfig.add(MVConfig.Configs.SYSTEM_BANDWIDTH_MONITOR_VISIBLE, Boolean.toString(false));
		Listener.notify(Listener.EREIGNIS_BANDWIDTH_MONITOR, MVBandwidthMonitorLWin.class.getSimpleName());
		setVisibility();
	}

	public JDialog getDialog() {
		return jDialog;
	}

	/**
	* Show/hide bandwidth display. Take also care about the used timer.
	*/
	private void setVisibility() {
		final boolean isVis = Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_BANDWIDTH_MONITOR_VISIBLE));
		jDialog.setVisible(isVis);
		try {
			if (isVis) {
				timerTask = new TimerTask() {

					@Override
					public void run() {
						counter++;
						m_trace.addPoint(counter / 60, daten.getDownloadInfos().bandwidth); // minutes
						x_achse.getAxisTitle().setTitle(daten.getDownloadInfos().roundBandwidth((long) counter));
					}
				};
				timer.schedule(timerTask, 0, 1_000);
			} else {
				if (timerTask != null) {
					timerTask.cancel();
				}
				timer.purge();
			}
		} catch (IllegalStateException ex) {
			logger.debug(ex);
		}
		if (!isVis) {
			jDialog.dispose();
		}
	}

	private static final Logger logger = LogManager.getLogger(MVBandwidthMonitorLWin.class);

	@Override
	public void writeConfig() {
		GuiFunktionen.getSize(MVConfig.Configs.SYSTEM_GROESSE_INFODIALOG, getDialog());
	}

	private class BeobMaus extends MouseAdapter {
		private final JCheckBox cbkTop = new JCheckBox("Immer im Vordergrund");
		private final JCheckBox cbkBorder = new JCheckBox("Rand anzeigen");
		private final JMenuItem itemClose = new JMenuItem("Ausblenden");

		public BeobMaus() {
			cbkTop.setSelected(MVConfig.getBool(MVConfig.Configs.SYSTEM_DOWNLOAD_INFO_TOP));
			cbkTop.addActionListener(l -> {
				MVConfig.add(MVConfig.Configs.SYSTEM_DOWNLOAD_INFO_TOP, Boolean.toString(cbkTop.isSelected()));
				GuiFunktionen.setParent(jDialog, MVConfig.getBool(MVConfig.Configs.SYSTEM_DOWNLOAD_INFO_TOP) ? frameParent : null);
			});
			cbkBorder.setSelected(MVConfig.getBool(MVConfig.Configs.SYSTEM_DOWNLOAD_INFO_DECORATED));
			cbkBorder.addActionListener(l -> {
				MVConfig.add(MVConfig.Configs.SYSTEM_DOWNLOAD_INFO_DECORATED, Boolean.toString(cbkBorder.isSelected()));
				GuiFunktionen.setDialogDecorated(jDialog, panel, MVConfig.getBool(MVConfig.Configs.SYSTEM_DOWNLOAD_INFO_DECORATED));
			});
			itemClose.addActionListener(l -> beenden());
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

			jPopupMenu.add(cbkTop);
			jPopupMenu.add(cbkBorder);
			jPopupMenu.addSeparator();
			jPopupMenu.add(itemClose);

			//anzeigen
			jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jPanelChart = new javax.swing.JPanel();

		jPanelChart.setPreferredSize(new java.awt.Dimension(32767, 32767));

		javax.swing.GroupLayout jPanelChartLayout = new javax.swing.GroupLayout(jPanelChart);
		jPanelChart.setLayout(jPanelChartLayout);
		jPanelChartLayout.setHorizontalGroup(
		jPanelChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGap(0, 400, Short.MAX_VALUE)
		);
		jPanelChartLayout.setVerticalGroup(
		jPanelChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGap(0, 250, Short.MAX_VALUE)
		);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addComponent(jPanelChart, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addComponent(jPanelChart, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
		);
	}// </editor-fold>//GEN-END:initComponents


	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPanel jPanelChart;
	// End of variables declaration//GEN-END:variables
}
