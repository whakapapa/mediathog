package mediathog.gui.actions;

import javafx.concurrent.Task;
import mSearch.filmlisten.writer.FilmListWriter;
import mediathog.config.Daten;

public class FilmListWriteWorkerTask extends Task<Void> {

	private final Daten daten;

	public FilmListWriteWorkerTask(Daten daten) {
		super();
		this.daten = daten;
	}

	@Override
	protected Void call() {
		FilmListWriter writer = new FilmListWriter();
		updateMessage("Schreibe Filmliste");
		updateProgress(0d, 1d);
		writer.writeFilmList(Daten.getDateiFilmliste(),
		daten.getListeFilme(),
		prog -> updateProgress(prog, 1d));

		return null;
	}
}
