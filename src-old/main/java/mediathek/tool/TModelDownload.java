
package mediathek.tool;

import mSearch.tool.Datum;
import mediathek.daten.DatenDownload;

@SuppressWarnings("serial")
public class TModelDownload extends TModel {
	private final Class<?>[] types;

	public TModelDownload(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
		types = new Class<?>[DatenDownload.MAX_ELEM];
		for (int i = 0; i < DatenDownload.MAX_ELEM; ++i) {
			switch (i) {
				case DatenDownload.DOWNLOAD_NR:
				types[i] = Integer.class;
				break;
				case DatenDownload.DOWNLOAD_FILM_NR:
				types[i] = Integer.class;
				break;
				case DatenDownload.DOWNLOAD_DATUM:
				types[i] = Datum.class;
				break;
				case DatenDownload.DOWNLOAD_GROESSE:
				types[i] = MVFilmSize.class;
				break;
				case DatenDownload.DOWNLOAD_REF:
				types[i] = DatenDownload.class;
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
