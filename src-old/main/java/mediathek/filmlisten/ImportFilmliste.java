
package mediathog.filmlisten;

import mSearch.Config;
import mSearch.daten.tList;
import mSearch.filmeSuchen.ListenerFilmeLaden;
import mSearch.filmeSuchen.ListenerFilmeLadenEvent;
import mSearch.filmlisten.FilmlistenSuchen;
import mSearch.filmlisten.reader.FilmListReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.event.EventListenerList;

public class ImportFilmliste {

	private static final Logger logger = LogManager.getLogger(ImportFilmliste.class);
	private final EventListenerList listeners;
	private final FilmListReader msFilmListReader;
	public FilmlistenSuchen msFilmlistenSuchen;

	public ImportFilmliste() {
		listeners = new EventListenerList();
		msFilmListReader = new FilmListReader();
		msFilmlistenSuchen = new FilmlistenSuchen();
		msFilmListReader.addAdListener(new ListenerFilmeLaden() {
			@Override
			public synchronized void start(ListenerFilmeLadenEvent event) {
				for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
					l.start(event);
				}
			}

			@Override
			public synchronized void progress(ListenerFilmeLadenEvent event) {
				for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
					l.progress(event);

				}
			}

			@Override
			public synchronized void fertig(ListenerFilmeLadenEvent event) {
			}
		});
	}

	/**
	* Filmeliste importieren, URL automatisch wählen
	*/
	public void importFromUrl(tList listeFilme, tList listeFilmeDiff, int days) {
		Config.setStop(false);
		Thread importThread = new FilmeImportierenAutoThread(msFilmlistenSuchen, listeFilme, listeFilmeDiff, days,
		this::urlLaden, this::fertigMelden);
		importThread.start();
	}

	/**
	* Filmeliste importieren, mit fester URL/Pfad
	*/
	public void importFromFile(String pfad, tList listeFilme, int days) {
		Config.setStop(false);
		Thread importThread = new FilmeImportierenDateiThread(pfad, listeFilme, days,
		this::urlLaden, this::fertigMelden);
		importThread.start();
	}

	public void addAdListener(ListenerFilmeLaden listener) {
		listeners.add(ListenerFilmeLaden.class, listener);
	}

	private boolean urlLaden(String dateiUrl, tList listeFilme, int days) {
		boolean ret = false;
		try {
			if (!dateiUrl.isEmpty()) {
				logger.info("Filmliste laden von: {}", dateiUrl);
				msFilmListReader.readFilmListe(dateiUrl, listeFilme, days);
				if (!listeFilme.isEmpty()) {
					ret = true;
				}
			}
		} catch (Exception ex) {
			logger.error("urlLaden", ex);
		}
		return ret;
	}

	private synchronized void fertigMelden(boolean ok) {
		for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
			l.fertig(new ListenerFilmeLadenEvent("", "", 0, 0, 0, !ok));
		}
	}
}
