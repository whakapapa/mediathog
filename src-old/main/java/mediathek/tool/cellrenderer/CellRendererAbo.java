
package mediathog.tool.cellrenderer;

import com.jidesoft.utils.SystemInfo;
import mSearch.tool.Log;
import mediathog.config.Daten;
import mediathog.config.MVColor;
import mediathog.daten.DatenAbo;
import mediathog.tool.MVSenderIconCache;
import mediathog.tool.table.MVTable;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class CellRendererAbo extends CellRendererBase {
	public CellRendererAbo(MVSenderIconCache cache) {
		super(cache);
	}

	@Override
	public Component getTableCellRendererComponent(
	JTable table,
	Object value,
	boolean isSelected,
	boolean hasFocus,
	int row,
	int column) {
		setBackground(null);
		setForeground(null);
		setFont(null);
		setIcon(null);
		setHorizontalAlignment(SwingConstants.LEADING);
		super.getTableCellRendererComponent(
		table, value, isSelected, hasFocus, row, column);
		try {
			final int r = table.convertRowIndexToModel(row);
			final int c = table.convertColumnIndexToModel(column);
			DatenAbo abo = Daten.getInstance().getListeAbo().getAboNr(r);
			final boolean aboIstEingeschaltet = abo.aboIstEingeschaltet();

			if (((MVTable) table).lineBreak) {
				switch (c) {
					case DatenAbo.ABO_IRGENDWO:
					case DatenAbo.ABO_NAME:
					case DatenAbo.ABO_THEMA:
					case DatenAbo.ABO_THEMA_TITEL:
					case DatenAbo.ABO_TITEL:
					case DatenAbo.ABO_ZIELPFAD:
					JTextArea textArea = new JTextArea();
					textArea.setLineWrap(true);
					textArea.setWrapStyleWord(true);
					textArea.setText(value.toString());
					textArea.setForeground(getForeground());
					textArea.setBackground(getBackground());
					setSelectionFont(textArea, isSelected);
					if (!aboIstEingeschaltet)
					setBackgroundColor(textArea, isSelected);
					return textArea;
				}
			}

			setSelectionFont(this, isSelected);

			switch (c) {
				case DatenAbo.ABO_NR:
				case DatenAbo.ABO_MINDESTDAUER:
				case DatenAbo.ABO_MIN:
				setHorizontalAlignment(SwingConstants.CENTER);
				break;

				case DatenAbo.ABO_EINGESCHALTET:
				setHorizontalAlignment(SwingConstants.CENTER);
				setCheckedOrUncheckedIcon(aboIstEingeschaltet);
				break;

				case DatenAbo.ABO_SENDER:
				if (((MVTable) table).getShowIcons()) {
					setSenderIcon((String) value, ((MVTable) table).iconKlein);
				}
				break;
			}

			if (!aboIstEingeschaltet)
			setBackgroundColor(this, isSelected);
		} catch (Exception ex) {
			Log.errorLog(630365892, ex);
		}
		return this;
	}

	private void setBackgroundColor(Component c, final boolean isSelected) {
		setFontItalic();
		if (isSelected) {
			c.setBackground(MVColor.ABO_AUSGESCHALTET_SEL.color);
		} else {
			c.setBackground(MVColor.ABO_AUSGESCHALTET.color);
		}
	}

	private void setFontItalic() {
		setFont(new Font("Dialog", Font.ITALIC, getFont().getSize()));

	}
}
