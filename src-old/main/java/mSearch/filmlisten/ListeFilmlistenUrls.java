
package mSearch.filmlisten;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

@SuppressWarnings("serial")
public class ListeFilmlistenUrls extends ArrayList<DatenFilmlisteUrl> {
	// ist die Liste mit den URLs zum Download einer Filmliste
	public void addWithCheck(DatenFilmlisteUrl filmliste) {
		for (DatenFilmlisteUrl datenUrlFilmliste : this) {
			if (datenUrlFilmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR].equals(filmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR])) {
				return;
			}
		}
		add(filmliste);
	}

	public void sort() {
		int nr = 0;
		Collections.sort(this);
		for (DatenFilmlisteUrl datenUrlFilmliste : this) {
			StringBuilder str = new StringBuilder(String.valueOf(nr++));
			while (str.length() < 3) {
				str.insert(0, '0');
			}
			datenUrlFilmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_NR_NR] = str.toString();
		}
	}

	public String getRand(ArrayList<String> bereitsGebraucht) {
		// gibt nur noch akt.xml und diff.xml und da sind alle Listen
		// aktuell, Prio: momentan sind alle Listen gleich gewichtet
		if (this.isEmpty()) {
			return "";
		}

		LinkedList<DatenFilmlisteUrl> listePrio = new LinkedList<>();
		//nach prio gewichten
		for (DatenFilmlisteUrl datenFilmlisteUrl : this) {
			if (bereitsGebraucht != null) {
				if (bereitsGebraucht.contains(datenFilmlisteUrl.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR])) {
					// wurde schon versucht
					continue;
				}
				if (datenFilmlisteUrl.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_NR].equals(DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_1)) {
					listePrio.add(datenFilmlisteUrl);
					listePrio.add(datenFilmlisteUrl);
				} else {
					listePrio.add(datenFilmlisteUrl);
					listePrio.add(datenFilmlisteUrl);
					listePrio.add(datenFilmlisteUrl);
				}
			}
		}

		DatenFilmlisteUrl datenFilmlisteUrl;
		if (!listePrio.isEmpty()) {
			int nr = new Random().nextInt(listePrio.size());
			datenFilmlisteUrl = listePrio.get(nr);
		} else {
			// dann wird irgendeine Versucht
			int nr = new Random().nextInt(this.size());
			datenFilmlisteUrl = this.get(nr);
		}
		return datenFilmlisteUrl.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR];
	}
}
