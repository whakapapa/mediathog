
package mediathek.tool;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
public class TextCopyPaste extends MouseAdapter {

	private final JPopupMenu popup = new JPopupMenu();

	private final Action cutAction;
	private final Action copyAction;
	private final Action pasteAction;
	private final Action undoAction;
	private final Action selectAllAction;

	private JTextComponent textComponent;
	private String savedString = "";
	private Actions lastActionSelected;

	private enum Actions {
		UNDO, CUT, COPY, PASTE, SELECT_ALL
	}

	public TextCopyPaste() {
		undoAction = new AbstractAction("Zurück") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				textComponent.setText("");
				textComponent.replaceSelection(savedString);

				lastActionSelected = Actions.UNDO;
			}
		};

		popup.add(undoAction);
		popup.addSeparator();

		cutAction = new AbstractAction("Ausschneiden") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				lastActionSelected = Actions.CUT;
				savedString = textComponent.getText();
				textComponent.cut();
			}
		};

		popup.add(cutAction);

		copyAction = new AbstractAction("Kopieren") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				lastActionSelected = Actions.COPY;
				textComponent.copy();
			}
		};

		popup.add(copyAction);

		pasteAction = new AbstractAction("Einfügen") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				lastActionSelected = Actions.PASTE;
				savedString = textComponent.getText();
				textComponent.paste();
			}
		};

		popup.add(pasteAction);
		popup.addSeparator();

		selectAllAction = new AbstractAction("Alles markieren") {
			@Override
			public void actionPerformed(ActionEvent ae) {
				lastActionSelected = Actions.SELECT_ALL;
				textComponent.selectAll();
			}
		};

		popup.add(selectAllAction);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
			if (!(e.getSource() instanceof JTextComponent)) {
				return;
			}
			textComponent = (JTextComponent) e.getSource();
			textComponent.requestFocus();

			boolean enabled = textComponent.isEnabled();
			boolean editable = textComponent.isEditable();
			boolean nonempty = !(textComponent.getText() == null || textComponent.getText().isEmpty());
			boolean marked = textComponent.getSelectedText() != null;

			boolean pasteAvailable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor);

			undoAction.setEnabled(enabled && editable && (lastActionSelected == Actions.CUT || lastActionSelected == Actions.PASTE));
			cutAction.setEnabled(enabled && editable && marked);
			copyAction.setEnabled(enabled && marked);
			pasteAction.setEnabled(enabled && editable && pasteAvailable);
			selectAllAction.setEnabled(enabled && nonempty);

			int nx = e.getX();

			if (nx > 500) {
				nx = nx - popup.getSize().width;
			}

			popup.show(e.getComponent(), nx, e.getY() - popup.getSize().height);
		}
	}

}
