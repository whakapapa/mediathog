
package mediathek.tool.listener;

import mediathek.config.Icons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BeobMpanel implements ActionListener {

	private int hoehe = -1;
	private final JCheckBox box;
	private final JPanel panel;

	public BeobMpanel(JCheckBox bbox, JPanel ppanel, String text) {
		box = bbox;
		box.setSelected(true);
		panel = ppanel;
		box.setText(text);
		box.setIcon(Icons.ICON_DIALOG_MINUS);
		box.setFont(new java.awt.Font("Dialog", 0, 11));
	}

	private void setPanel() {
		if (hoehe == -1) {
			hoehe = panel.getSize().height;
		}
		if (box.isSelected()) {
			panel.setSize(panel.getSize().width, hoehe);
			panel.setPreferredSize(new Dimension(panel.getSize().width, hoehe));
			box.setIcon(Icons.ICON_DIALOG_MINUS);
		} else {
			panel.setSize(panel.getSize().width, box.getSize().height + 2);
			panel.setPreferredSize(new Dimension(panel.getSize().width, box.getSize().height + 2));
			box.setIcon(Icons.ICON_DIALOG_PLUS);
		}
		panel.updateUI();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setPanel();
	}
}
