
package mediathog.tool;

import java.awt.Frame;
import javax.swing.JOptionPane;

public class HinweisKeineAuswahl {

	public void zeigen(Frame parentComponent) {
		MVMessageDialog.showMessageDialog(parentComponent, "Zeile auswählen", "keine Auswahl", JOptionPane.INFORMATION_MESSAGE);
	}
}
