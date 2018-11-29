
package mediathog.tool;

import mSearch.daten.DatenFilm;
import mSearch.tool.Datum;

@SuppressWarnings("serial")
public class TModelFilm extends TModel {
	public TModelFilm(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		Class<?> result;
		switch (columnIndex) {
			case DatenFilm.FILM_NR:
			result = Integer.class;
			break;

			case DatenFilm.FILM_DATUM:
			result = Datum.class;
			break;

			case DatenFilm.FILM_GROESSE:
			result = MVFilmSize.class;
			break;

			case DatenFilm.FILM_REF:
			result = DatenFilm.class;
			break;

			default:
			result = String.class;
			break;
		}

		return result;
	}
}
