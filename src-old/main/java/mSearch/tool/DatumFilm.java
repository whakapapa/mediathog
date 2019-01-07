
package mSearch.tool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@SuppressWarnings("serial")
public class DatumFilm extends Datum {
	// die Filme werden immer in der Zeitzone "Europe/Berlin" gesucht

	private final static SDF dateFormatter1 = new SDF("dd.MM.yyyy");
	private final static SDF dateFormatter2 = new SDF("yyyy.MM.dd");

	public DatumFilm(long l) {
		super(l);
	}

	@Override
	public String toString() {
		if (this.getTime() == 0) {
			return "";
		} else {
			return dateFormatter1.format(this);
		}
	}

	@Override
	public String toStringR() {
		if (this.getTime() == 0) {
			return dateFormatter2.format(new Date());
		} else {
			return dateFormatter2.format(this);
		}
	}

	private static class SDF extends SimpleDateFormat {
		private final static TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");

		SDF(String str) {
			super(str);
			this.setTimeZone(tz);
		}
	}
}
