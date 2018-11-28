
package mediathek.tool;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JTable;

public class SpacerIcon implements Icon {

	private int extraHeight;

	public SpacerIcon(int extraHeight) {
		if (extraHeight < 0) {
			throw new IllegalArgumentException("extraHeight must be >= 0");
		}
		this.extraHeight = extraHeight + new JTable().getFont().getSize();
	}

	@Override
	public int getIconHeight() {
		return extraHeight;
	}

	@Override
	public int getIconWidth() {
		return 0;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
	}
}
