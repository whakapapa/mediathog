
package mediathog.tool.listener;

import mediathog.tool.GuiFunktionen;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
* This class provides a "copy URL" popup menu for a given {@link javax.swing.JButton}.
*/
public class BeobMausUrl extends MouseAdapter {

	private final BeobUrl beobUrl = new BeobUrl();
	private final String link;
	private final JButton jButton;

	public BeobMausUrl(JButton jButton) {
		this.jButton = jButton;
		this.link = null;
	}

	public BeobMausUrl(String link) {
		jButton = null;
		this.link = link;
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

		//Url
		JMenuItem item = new JMenuItem("URL kopieren");
		item.addActionListener(beobUrl);
		jPopupMenu.add(item);
		//anzeigen
		jPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
	}

	private class BeobUrl implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (jButton == null) {
				GuiFunktionen.copyToClipboard(link);
			} else {
				GuiFunktionen.copyToClipboard(jButton.getText());
			}
		}
	}
}
