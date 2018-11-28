
package mediathek.tool;

import mSearch.tool.Datum;
import mediathek.daten.DatenAbo;

@SuppressWarnings("serial")
public class TModelAbo extends TModel {
	private final Class<?>[] types;

	public TModelAbo(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
		types = new Class<?>[DatenAbo.MAX_ELEM];
		for (int i = 0; i < DatenAbo.MAX_ELEM; ++i) {
			switch (i) {
				case DatenAbo.ABO_NR:
				case DatenAbo.ABO_MINDESTDAUER:
				types[i] = Integer.class;
				break;
				case DatenAbo.ABO_DOWN_DATUM:
				types[i] = Datum.class;
				break;
				default:
				types[i] = String.class;
			}
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return types[columnIndex];
	}
}
