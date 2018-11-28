
package mediathek.tool.threads;

import mSearch.daten.tList;
import mSearch.filmeSuchen.ListenerFilmeLadenEvent;
import mSearch.filmlisten.reader.FilmListReader;
import mediathek.config.Daten;
import mediathek.config.Konstanten;
import mediathek.config.MVConfig;
import mediathek.filmlisten.FilmeLaden;
import mediathek.gui.messages.FilmListReadStartEvent;
import mediathek.gui.messages.FilmListReadStopEvent;
import mediathek.tool.GuiFunktionen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UIFilmlistLoaderThread extends Thread {

	private static final Logger logger = LogManager.getLogger(UIFilmlistLoaderThread.class);
	private final Daten daten;
	private final tList listeFilme;
	private final FilmeLaden filmeLaden;
	private final int tageFilmliste;

	public UIFilmlistLoaderThread(Daten daten) {
		this.daten = daten;
		listeFilme = daten.getListeFilme();
		filmeLaden = daten.getFilmeLaden();
		tageFilmliste = Integer.parseInt(MVConfig.get(MVConfig.Configs.SYSTEM_ANZ_TAGE_FILMLISTE));


		setName("UIFilmlistLoaderThread");
	}

	private void readLocalFilmList() {
		try (FilmListReader reader = new FilmListReader()) {
			reader.readFilmListe(Daten.getDateiFilmliste(), listeFilme, tageFilmliste);
			logger.debug("Liste Filme gelesen am: {}", new SimpleDateFormat("dd.MM.yyyy, HH:mm").format(new Date()));
			logger.debug(" erstellt am: {}", listeFilme.genDate());
			logger.debug(" Anzahl Filme: {}", listeFilme.size());
			logger.debug(" Anzahl Neue: {}", listeFilme.countNewFilms());
		}
	}

	private void performFilterOperations() {
		filmeLaden.notifyStart(new ListenerFilmeLadenEvent("", "", 0, 0, 0, false));

		filmeLaden.notifyProgress(new ListenerFilmeLadenEvent("", "Themen suchen", 0, 0, 0, false));
		listeFilme.fillSenderList();

		filmeLaden.notifyProgress(new ListenerFilmeLadenEvent("", "Abos eintragen", 0, 0, 0, false));
		daten.getListeAbo().setAboFuerFilm(daten.getListeFilme(), false /*aboLoeschen*/);

		filmeLaden.notifyProgress(new ListenerFilmeLadenEvent("", "Blacklist filtern", 0, 0, 0, false));
		daten.getListeBlacklist().filterListe();

		filmeLaden.notifyFertig(new ListenerFilmeLadenEvent("", "", 100, 100, 0, false));
	}

	@Override
	public void run() {
		logger.debug("Programmstart Daten laden");
		try {
			//give the UI some time to set up...
			TimeUnit.MILLISECONDS.sleep(500);

			daten.getMessageBus().publishAsync(new FilmListReadStartEvent());

			readLocalFilmList();

			if (GuiFunktionen.getImportArtFilme() == setFilmsAuto && listeFilme.isTooOld()) {
				logger.info("Filmliste zu alt, neue Filmliste laden");
				if (!filmeLaden.loadFilmListFromNetwork()) {
					//if we haven´t loaded anything, let´s continue with the current list and start filtering...
					logger.info("filmlist update not necessary, start filtering current filmlist");
					performFilterOperations();
				}
			} else {
				// beim Neuladen wird es dann erst gemacht
				performFilterOperations();
			}

			daten.getMessageBus().publishAsync(new FilmListReadStopEvent());
		} catch (InterruptedException ignored) {
		}

		logger.debug("Programmende Daten laden");
	}

}
