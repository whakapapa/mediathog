package mediathog.javafx.filmlist;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import mSearch.filmeSuchen.ListenerFilmeLaden;
import mSearch.filmeSuchen.ListenerFilmeLadenEvent;
import mediathog.config.Daten;
import mediathog.gui.messages.TimerEvent;
import mediathog.javafx.CenteredBorderPane;
import mediathog.javafx.VerticalSeparator;
import net.engio.mbassy.listener.Handler;

import javax.swing.*;

/**
* Pane which will display common information about the current filmlist.
* Also handles updating the subcomponents based on a TimerEvent.
*/
public class FilmListInfoPane extends HBox {
	private final FilmListCreationDateLabel filmListCreationDateLabel;
	private final FilmListAgeLabel filmListAgeLabel;

	public FilmListInfoPane(Daten daten) {
		super();
		setSpacing(4d);

		daten.getMessageBus().subscribe(this);

		SwingUtilities.invokeLater(() -> daten.getFilmeLaden().addAdListener(new ListenerFilmeLaden() {
			@Override
			public void start(ListenerFilmeLadenEvent event) {
				daten.getMessageBus().unsubscribe(this);
				Platform.runLater(() -> setVisible(false));
			}

			@Override
			public void fertig(ListenerFilmeLadenEvent event) {
				daten.getMessageBus().subscribe(this);
				Platform.runLater(() -> {
					filmListCreationDateLabel.computeCreationDate();
					setVisible(true);
				});
			}
		}));

		filmListCreationDateLabel = new FilmListCreationDateLabel(daten);
		filmListAgeLabel = new FilmListAgeLabel(daten);
		getChildren().addAll(new CenteredBorderPane(filmListCreationDateLabel),
		new VerticalSeparator(),
		new CenteredBorderPane(filmListAgeLabel));
	}

	@Handler
	public void handleTimerEvent(TimerEvent e) {
		Platform.runLater(filmListAgeLabel::computeAge);
	}
}
