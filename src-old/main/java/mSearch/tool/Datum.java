
package mSearch.tool;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("serial")
public class Datum extends Date {
	private final static SimpleDateFormat dateFormatter1 = new SimpleDateFormat("dd.MM.yyyy");
	private final static SimpleDateFormat dateFormatter2 = new SimpleDateFormat("yyyy.MM.dd");

	public Datum() {
		super();
	}

	public Datum(long l) {
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

	public String toStringR() {
		if (this.getTime() == 0) {
			return dateFormatter2.format(new Date());
		} else {
			return dateFormatter2.format(this);
		}
	}

	/**
	* Liefert den Betrag der Zeitdifferenz zu jetzt.
	*
	* @return Differenz in Sekunden.
	*/
	public int diffInSekunden() {
		final int ret = new Long((this.getTime() - new Datum().getTime()) / (1000)).intValue();
		return Math.abs(ret);
	}

	/**
	* Liefert den BETRAG! der Zeitdifferenz zu jetzt.
	*
	* @return Differenz in Minuten.
	*/
	public int diffInMinuten() {
		return (diffInSekunden() / 60);
	}
}
