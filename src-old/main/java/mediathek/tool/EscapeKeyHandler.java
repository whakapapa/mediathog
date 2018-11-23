
package mediathek.tool;

import com.jidesoft.utils.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class EscapeKeyHandler {

	private EscapeKeyHandler(JFrame frame, IAction action) {
		JRootPane rootPane = frame.getRootPane();
		installHandler(rootPane, action);
	}

	private EscapeKeyHandler(JDialog dialog, IAction action) {
		JRootPane rootPane = dialog.getRootPane();
		installHandler(rootPane, action);
	}

	public static void installHandler(JDialog dialog, IAction action) {
		new EscapeKeyHandler(dialog, action);
	}

	public static void installHandler(JFrame frame, IAction action) {
		new EscapeKeyHandler(frame, action);
	}

	private void installHandler(JRootPane rootPane, IAction action) {
		// ESC zum Beenden
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "x");
		rootPane.getActionMap().put("x", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.perform();
			}
		});

	}

	public interface IAction {
		void perform();
	}
}
