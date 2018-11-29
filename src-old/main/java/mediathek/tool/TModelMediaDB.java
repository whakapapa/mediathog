
package mediathog.tool;

import mediathog.daten.DatenMediaDB;

@SuppressWarnings("serial")
public class TModelMediaDB extends TModel {
	private final Class<?>[] types;

	public TModelMediaDB(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
		types = new Class<?>[DatenMediaDB.MAX_ELEM];
		for (int i = 0; i < DatenMediaDB.MAX_ELEM; ++i) {
			switch (i) {
				case DatenMediaDB.MEDIA_DB_SIZE:
				types[i] = MVMediaDBFileSize.class;
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
