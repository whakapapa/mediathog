
package mSearch.daten;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mSearch.Const;
import mSearch.tool.GermanStringSorter;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



	public ObservableList<String> getSenders() {
		return senderList;
	}

	public synchronized void importFilmliste(DatenFilm film) {
		addInit(film);
	}

	/**
	* Search all themas within list based on sender.
	* If sender is empty, return full list of themas.
	*
	* @param sender sender name as String
	* @return List of themas as String.
	*/
	public List<String> getThemen(String sender) {
		Stream<DatenFilm> mystream = parallelStream();
		//if sender is empty return all themas...
		if (!sender.isEmpty())
		mystream = mystream.filter(f -> f.getSender().equals(sender));

		return mystream.map(DatenFilm::getThema)
		.distinct()
		.sorted(GermanStringSorter.getInstance())
		.collect(Collectors.toList());
	}

	private void addHash(DatenFilm f, HashSet<String> hash, boolean index) {
		if (index) {
			hash.add(f.getIndex());
		} else {
			hash.add(f.getUrl());
		}
	}

	public synchronized void updateListe(tList listeEinsortieren, boolean index /* Vergleich über Index, sonst nur URL */, boolean ersetzen) {
		// in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
		// es werden nur Filme die noch nicht vorhanden sind, einsortiert
		// "ersetzen": true: dann werden gleiche (index/URL) in der Liste durch neue ersetzt
		final HashSet<String> hash = new HashSet<>(listeEinsortieren.size() + 1, 1);

		if (ersetzen) {
			listeEinsortieren.forEach((DatenFilm f) -> addHash(f, hash, index));

			Iterator<DatenFilm> it = this.iterator();
			while (it.hasNext()) {
				DatenFilm f = it.next();
				if (index) {
					if (hash.contains(f.getIndex())) {
						it.remove();
					}
				} else if (hash.contains(f.getUrl())) {
					it.remove();
				}
			}

			listeEinsortieren.forEach(this::addInit);
		} else {
			// ==============================================
			this.forEach(f -> addHash(f, hash, index));

			for (DatenFilm f : listeEinsortieren) {
				if (index) {
					if (!hash.contains(f.getIndex())) {
						addInit(f);
					}
				} else if (!hash.contains(f.getUrl())) {
					addInit(f);
				}
			}
		}
		hash.clear();
	}

	private void addInit(DatenFilm film) {
		film.init();
		add(film);
	}

	@Override
	public boolean add(DatenFilm aFilm) {
		return super.add(aFilm);
	}

	@Override
	public synchronized void clear() {
		neueFilme = false;

		super.clear();
	}

	public synchronized void setMeta(tList listeFilme) {
		System.arraycopy(listeFilme.metaDaten, 0, metaDaten, 0, MAX_ELEM);
	}

	public synchronized DatenFilm getFilmByUrl(final String url) {
		Optional<DatenFilm> opt = this.parallelStream().filter(f -> f.arr[DatenFilm.FILM_URL].equalsIgnoreCase(url)).findAny();
		return opt.orElse(null);
	}

	public synchronized DatenFilm getFilmByUrl_klein_hoch_hd(String url) {
		// Problem wegen gleicher URLs
		// wird versucht, einen Film mit einer kleinen/Hoher/HD-URL zu finden
		DatenFilm ret = null;
		for (DatenFilm f : this) {
			if (f.arr[DatenFilm.FILM_URL].equals(url)) {
				ret = f;
				break;
			} else if (f.getUrlFuerAufloesung(DatenFilm.AUFLOESUNG_HD).equals(url)) {
				ret = f;
				break;
			} else if (f.getUrlFuerAufloesung(DatenFilm.AUFLOESUNG_KLEIN).equals(url)) {
				ret = f;
				break;
			}
		}

		return ret;
	}

	public synchronized String genDate() {
		// Tag, Zeit in lokaler Zeit wann die Filmliste erstellt wurde
		// in der Form "dd.MM.yyyy, HH:mm"
		final String date = metaDaten[tList.FILMLISTE_DATUM_GMT_NR];

		Date filmDate;
		String ret;
		try {
			filmDate = sdf_.parse(date);
			FastDateFormat formatter = FastDateFormat.getInstance(DATUM_ZEIT_FORMAT);
			ret = formatter.format(filmDate);
		} catch (ParseException ignored) {
			ret = date;
		}

		return ret;
	}

	public synchronized String getId() {
		// liefert die ID einer Filmliste
		return metaDaten[tList.FILMLISTE_ID_NR];
	}

	/**
	* Replace the current metadata with new one.
	*
	* @param data the new metadata
	*/
	public synchronized void setMetaDaten(String[] data) {
		metaDaten = data;
	}

	/**
	* Get the age of the film list.
	*
	* @return Age in seconds.
	*/
	public int getAge() {
		int ret = 0;
		Date now = new Date(System.currentTimeMillis());
		Date filmDate = getAgeAsDate();
		if (filmDate != null) {
			ret = Math.round((now.getTime() - filmDate.getTime()) / (1000));
			if (ret < 0) {
				ret = 0;
			}
		}
		return ret;
	}

	/**
	* Get the age of the film list.
	*
	* @return Age as a {@link java.util.Date} object.
	*/
	private Date getAgeAsDate() {
		String date = metaDaten[tList.FILMLISTE_DATUM_GMT_NR];
		sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));

		Date filmDate = null;
		try {
			filmDate = sdf.parse(date);
		} catch (ParseException ignored) {
		}

		return filmDate;
	}

	/**
	* Check if available Filmlist is older than a specified value.
	*
	* @return true if too old or if the list is empty.
	*/
	public synchronized boolean isTooOld() {
		return (isEmpty()) || (isOlderThan(setAUdelay));
	}

	/**
	* Check if Filmlist is too old for using a diff list.
	*
	* @return true if empty or too old.
	*/
	public synchronized boolean isTooOldForDiff() {
		if (isEmpty()) {
			return true;
		}
		try {
			final String dateMaxDiff_str = new SimpleDateFormat("yyyy.MM.dd__").format(new Date()) + killMinAge + ":00:00";
			final Date dateMaxDiff = new SimpleDateFormat("yyyy.MM.dd__HH:mm:ss").parse(dateMaxDiff_str);
			final Date dateFilmliste = getAgeAsDate();
			if (dateFilmliste != null) {
				return dateFilmliste.getTime() < dateMaxDiff.getTime();
			}
		} catch (Exception ignored) {
		}
		return true;
	}

	/**
	* Check if list is older than specified parameter.
	*
	* @param sekunden The age in seconds.
	* @return true if older.
	*/
	public boolean isOlderThan(long sekunden) {
		final long ret = getAge();
		if (ret != 0) {
			logger.info("Die Filmliste ist {} Minuten alt", ret / 60);
		}
		return ret > sekunden;
	}

	public synchronized long countNewFilms() {
		return this.stream().filter(DatenFilm::isNew).count();
	}

	public synchronized void fillSenderList() {
		Platform.runLater(() -> {
			senderList.clear();
			// der erste Sender ist ""
			//senderList.add("");
			senderList.addAll(stream().map(DatenFilm::getSender).distinct().collect(Collectors.toList()));
		});
	}
}
