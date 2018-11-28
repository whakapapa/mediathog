
package mediathek.tool;

import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import mediathek.config.Daten;

public class MVMessageDialog {

	public static void showMessageDialog(final Component parent, final String message, final String title, final int messageType) {
		if (!Daten.getInstance().isAuto()) {
			if (SwingUtilities.isEventDispatchThread()) {
				JOptionPane.showMessageDialog(parent, message, title, messageType);
			} else {
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent, message, title, messageType));
			}
		}

	}

	public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType) {
		final int ret;
		if (!Daten.getInstance().isAuto()) {
			ret = JOptionPane.showConfirmDialog(parentComponent, message, title, optionType);
		} else {
			ret = 0;
		}
		return ret;
	}
}
