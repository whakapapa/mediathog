
package mSearch.tool;

import mSearch.daten.DatenFilm;
import org.jetbrains.annotations.NotNull;

public class MSLong implements Comparable<MSLong> {

	private long l = 0;

	public MSLong(final long l) {
		this.l = l;
	}

	public MSLong(DatenFilm film) {
		if (film.arr[DatenFilm.FILM_GROESSE].equals("<1")) {
			film.arr[DatenFilm.FILM_GROESSE] = "1";
		}
		try {
			if (!film.arr[DatenFilm.FILM_GROESSE].isEmpty()) {
				l = Long.valueOf(film.arr[DatenFilm.FILM_GROESSE]);
			}
		} catch (NumberFormatException ex) {
			Log.errorLog(649891025, ex, "String: " + film.arr[DatenFilm.FILM_GROESSE]);
			l = 0;
		}
	}

	@Override
	public String toString() {
		return (l == 0) ? "" : Long.toString(l);
	}

	@Override
	public int compareTo(@NotNull MSLong other) {
		return (Long.compare(l, other.l));
	}
}
