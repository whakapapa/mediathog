package master

// manage film and list data

//TODO move constants to constants.go
const setNoBackup = 3

// movie resolution enum
type tRes int
const (
	rStd tRes = iota		// standard
	rHD						// HD
	rSD						// SD
)

// geo for viewing
type tAir int
const (
	gGE tAir = iota		// Germany
	gAT						// Austria
	gCH						// Switzerland
	gEU						// EU
	gWO						// world
)

// single movie data
type tMov struct {
	resol		tRes			// resolution
	geo		tAir			// geo tag
}


//TODO java class def - incomplete - see DatenFilm (repl tMov)

public static final String AUFLOESUNG_NORMAL = "normal";
public static final String AUFLOESUNG_HD = "hd";
public static final String AUFLOESUNG_KLEIN = "klein";
public static final String GEO_DE = "DE"; // nur in .. zu sehen
public static final String GEO_AT = "AT";
public static final String GEO_CH = "CH";
public static final String GEO_EU = "EU";
public static final String GEO_WELT = "WELT";
//




public static final int FILM_NR = 0; // wird vor dem Speichern gelöscht!
public static final int FILM_SENDER = 1;
public static final int FILM_THEMA = 2;
public static final int FILM_TITEL = 3;
public static final int FILM_ABSPIELEN = 4;
public static final int FILM_AUFZEICHNEN = 5;
public static final int FILM_DATUM = 6;
public static final int FILM_ZEIT = 7;
public static final int FILM_DAUER = 8;
public static final int FILM_GROESSE = 9;
public static final int FILM_HD = 10;
public static final int FILM_UT = 11;
public static final int FILM_GEO = 12;// Geoblocking
public static final int FILM_URL = 13;
public static final int FILM_ABO_NAME = 14;// wird vor dem Speichern gelöscht!
public static final int FILM_URL_SUBTITLE = 15;
public static final int FILM_URL_KLEIN = 16;
public static final int FILM_URL_HD = 17;
public static final int FILM_URL_HISTORY = 18;
public static final int FILM_DATUM_LONG = 19;// Datum als Long ABER Sekunden!!
public static final int FILM_REF = 20;// Referenz auf this
public static final int MAX_ELEM = 21;

//Indices without storage context !!!
public static final int FILM_NEU = 21;



public static final String[] COLUMN_NAMES = new String[MAX_ELEM];
/**
* The database instance for all descriptions.
*/
private final static AtomicInteger FILM_COUNTER = new AtomicInteger(0);
private static final GermanStringSorter sorter = GermanStringSorter.getInstance();
private static final Logger logger = LogManager.getLogger(DatenFilm.class);
public static boolean[] spaltenAnzeigen = new boolean[MAX_ELEM];

static {
	COLUMN_NAMES[FILM_NR] = "Nr";
	COLUMN_NAMES[FILM_SENDER] = "Sender";
	COLUMN_NAMES[FILM_THEMA] = "Thema";
	COLUMN_NAMES[FILM_TITEL] = "Titel";
	COLUMN_NAMES[FILM_ABSPIELEN] = "";
	COLUMN_NAMES[FILM_AUFZEICHNEN] = "";
	COLUMN_NAMES[FILM_DATUM] = "Datum";
	COLUMN_NAMES[FILM_ZEIT] = "Zeit";
	COLUMN_NAMES[FILM_DAUER] = "Dauer";
	COLUMN_NAMES[FILM_GROESSE] = "Größe [MB]";
	COLUMN_NAMES[FILM_HD] = "HD";
	COLUMN_NAMES[FILM_UT] = "UT";
	COLUMN_NAMES[FILM_GEO] = "Geo";
	COLUMN_NAMES[FILM_URL] = "URL";
	COLUMN_NAMES[FILM_ABO_NAME] = "Abo";
	COLUMN_NAMES[FILM_URL_SUBTITLE] = "URL Untertitel";
	COLUMN_NAMES[FILM_URL_KLEIN] = "URL Klein";
	COLUMN_NAMES[FILM_URL_HD] = "URL HD";
	COLUMN_NAMES[FILM_URL_HISTORY] = "URL History";
	COLUMN_NAMES[FILM_REF] = "Ref";
	COLUMN_NAMES[FILM_DATUM_LONG] = "DatumL";
}

static {
	Database.initializeDatabase();
}

/**
* The magic arr array.
* Here all the film information with some minor exceptions.
* Beware it is a dangerous string collection...
*/

//TODO here is the MASTER array
public final String[] arr = new String[MAX_ELEM];
/**
* film date stored IN SECONDS!!!
*/
public DatumFilm datumFilm = new DatumFilm(0);
public Object abo = null;
/**
* File size in MByte
*/
private MSLong filmSize;
/**
* Is this film an audio version? (aka Hörfassung)
*/
private boolean isAudioVersion = false;
/**
* film length in seconds.
*/
private long filmLength = 0;
/**
* Internal film number, used for storage in database
*/
private int databaseFilmNumber;
private boolean neuerFilm = false;
private Cleaner cleaner = null;
/**
* Future used for writing description into database.
* Will be checked before each read if finished
*/
private WeakReference<CompletableFuture<Void>> descriptionFutureReference = null;
/**
* Future used for writing website link into database.
* Will be checked before each read if finished
*/
private WeakReference<CompletableFuture<Void>> websiteFutureReference = null;
/**
* Flag that this entry is in sign language (aka Gebärdensprache).
*/
private boolean isSignLanguage = false;
/**
* Flag indicating a trailer, teaser or german Vorschau.
*/
private boolean isTrailerTeaser = false;


public class DatenFilm implements AutoCloseable, Comparable<DatenFilm> {

//TODO exploit or eliminate / reorganize film data
//TODO and integrate into struct tMov



	public DatenFilm() {
		setupArr();

		filmSize = new MSLong(0); // Dateigröße in MByte
		databaseFilmNumber = FILM_COUNTER.getAndIncrement();
		writeFilmNumberToDatabase();

		setupDatabaseCleanup();
	}

	private void writeFilmNumberToDatabase() {
		try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
		PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO mediathekview.film VALUES (?)")
		) {
			insertStatement.setInt(1, databaseFilmNumber);
			insertStatement.executeUpdate();


		} catch (SQLException ex) {
			logger.error("SQLException: ", ex);
		}
	}

	public boolean isTrailerTeaser() {
		return isTrailerTeaser;
	}

	public void setTrailerTeaser(boolean val) {
		isTrailerTeaser = val;
	}

	public boolean isAudioVersion() {
		return isAudioVersion;
	}

	public void setAudioVersion(boolean val) {
		isAudioVersion = val;
	}

	public boolean isSignLanguage() {
		return isSignLanguage;
	}

	public void setSignLanguage(boolean val) {
		isSignLanguage = val;
	}

	/**
	* Get the film number.
	* This is used internally for the database id AND
	* for the old MV code that might access it for various stuff.
	*
	* @return the original internal film number
	*/
	public int getFilmNr() {
		return databaseFilmNumber;
	}

	private void setupDatabaseCleanup() {
		final boolean useCleaner = ApplicationConfiguration.getConfiguration().getBoolean(ApplicationConfiguration.DATABASE_USE_CLEANER_INTERFACE, false);
		if (useCleaner)
		installCleanupTask();
	}

	private void installCleanupTask() {
		DatenFilmCleanupTask task = new DatenFilmCleanupTask(databaseFilmNumber);
		cleaner = Cleaner.create(this, task);
	}

	/**
	* Get the file size of this film.
	*
	* @return The size in MByte
	*/
	public MSLong getFilmSize() {
		return filmSize;
	}

	private void setupArr() {
		for (int i = 0; i < MAX_ELEM; i++)
		arr[i] = "";
	}

	/**
	* Return the title of the film.
	*/
	public String getTitle() {
		return arr[FILM_TITEL];
	}

	public void setTitle(String title) {
		arr[FILM_TITEL] = title;
	}

	public String getThema() {
		return arr[FILM_THEMA];
	}

	/**
	* Return the film size.
	*
	* @return size as a string
	*/
	public String getSize() {
		return arr[FILM_GROESSE];
	}

	public String getSender() {
		return arr[FILM_SENDER];
	}

	public void setSender(String sender) {
		arr[DatenFilm.FILM_SENDER] = sender;
	}

	@Override
	public void close() {

		if (cleaner != null)
		cleaner.clean();
	}

	/**
	* Get the film description from database.
	*
	* @return the film description.
	*/
	public String getDescription() {
		String sqlStr;

		try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
		PreparedStatement statement = connection.prepareStatement("SELECT desc FROM mediathekview.description WHERE id = ?")) {
			statement.setLong(1, databaseFilmNumber);

			descriptionFutureReference = checkFutureCompletion(descriptionFutureReference);

			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					sqlStr = rs.getString(1);
				} else
				sqlStr = "";
			}
		} catch (SQLException | InterruptedException | ExecutionException e) {
			logger.error(e);
			sqlStr = "";
		}

		return sqlStr;
	}

	/**
	* Store description in database.
	* Performs an UPSERT as we may want to update the description later again.
	*
	* @param desc String to be stored.
	*/
	public void setDescription(final String desc) {
		if (desc != null && !desc.isEmpty()) {
			if (MemoryUtils.isLowMemoryEnvironment())
			writeDescriptionToDatabase(desc);
			else {
				final CompletableFuture<Void> descriptionFuture = CompletableFuture.runAsync(() -> writeDescriptionToDatabase(desc), PooledDatabaseConnection.getInstance().getDatabaseExecutor());
				descriptionFutureReference = new WeakReference<>(descriptionFuture);
			}
		}
	}

	private synchronized WeakReference<CompletableFuture<Void>> checkFutureCompletion(WeakReference<CompletableFuture<Void>> refVar) throws ExecutionException, InterruptedException {
		if (refVar != null) {
			final CompletableFuture<Void> refFuture = refVar.get();
			if (refFuture != null) {
				if (!refFuture.isDone()) {
					refFuture.get();
				}
				//set to null when finished as we don´t need it anymore...
				refVar.clear();
				return null;
			}
		}

		return refVar;
	}

	public String getWebsiteLink() {
		String res;

		try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
		PreparedStatement statement = connection.prepareStatement("SELECT link FROM mediathekview.website_links WHERE id = ?")) {
			statement.setLong(1, databaseFilmNumber);

			websiteFutureReference = checkFutureCompletion(websiteFutureReference);

			try (ResultSet rs = statement.executeQuery()) {
				if (rs.next()) {
					res = rs.getString(1);
				} else
				res = "";
			}
		} catch (SQLException | InterruptedException | ExecutionException ex) {
			logger.error(ex);
			res = "";
		}

		return res;
	}

	public void setWebsiteLink(String link) {
		if (link != null && !link.isEmpty()) {
			if (MemoryUtils.isLowMemoryEnvironment())
			writeWebsiteLinkToDatabase(link);
			else {
				CompletableFuture<Void> websiteFuture = CompletableFuture.runAsync(() -> writeWebsiteLinkToDatabase(link), PooledDatabaseConnection.getInstance().getDatabaseExecutor());
				websiteFutureReference = new WeakReference<>(websiteFuture);
			}
		}
	}

	private void writeWebsiteLinkToDatabase(String link) {
		try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
		PreparedStatement mergeStatement = connection.prepareStatement("MERGE INTO mediathekview.website_links KEY(ID) VALUES (?,?)")) {

			mergeStatement.setInt(1, databaseFilmNumber);
			mergeStatement.setString(2, link);
			mergeStatement.executeUpdate();
		} catch (SQLException ex) {
			logger.error(ex);
		}
	}

	private void writeDescriptionToDatabase(String desc) {
		try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
		PreparedStatement mergeStatement = connection.prepareStatement("MERGE INTO mediathekview.description KEY(ID) VALUES (?,?)")
		) {
			String cleanedDesc = cleanDescription(desc, arr[FILM_THEMA], getTitle());
			cleanedDesc = StringUtils.replace(cleanedDesc, "\n", "<br/>");

			mergeStatement.setInt(1, databaseFilmNumber);
			mergeStatement.setString(2, cleanedDesc);
			mergeStatement.executeUpdate();

		} catch (SQLIntegrityConstraintViolationException ignored) {
			//this will happen in UPSERT operation
		} catch (JdbcSQLException ex) {
			if (!ex.getMessage().contains("primary key violation")) {
				logger.error("JdbcSQLException: ", ex);
			}
		} catch (SQLException ex) {
			logger.error("SQLException: ", ex);
		}

	}

	private String cleanDescription(String s, String thema, String titel) {
		// die Beschreibung auf x Zeichen beschränken

		s = Functions.removeHtml(s); // damit die Beschreibung nicht unnötig kurz wird wenn es erst später gemacht wird

		if (s.startsWith(titel)) {
			s = s.substring(titel.length()).trim();
		}
		if (s.startsWith(thema)) {
			s = s.substring(thema.length()).trim();
		}
		if (s.startsWith("|")) {
			s = s.substring(1).trim();
		}
		if (s.startsWith("Video-Clip")) {
			s = s.substring("Video-Clip".length()).trim();
		}
		if (s.startsWith(titel)) {
			s = s.substring(titel.length()).trim();
		}
		if (s.startsWith(":") || s.startsWith(",") || s.startsWith("\n")) {
			s = s.substring(1).trim();
		}

		if (s.contains("\\\"")) { // wegen " in json-Files
		s = StringUtils.replace(s, "\\\"", "\"");
	}

	return s;
}

public boolean isNew() {
	return neuerFilm;
}

public void setNew(final boolean newFilm) {
	neuerFilm = newFilm;
}

public String getUrlSubtitle() {
	return arr[FILM_URL_SUBTITLE];
}

public boolean hasSubtitle() {
	//Film hat Untertitel
	return !arr[DatenFilm.FILM_URL_SUBTITLE].isEmpty();
}

public String getUrlFuerAufloesung(String aufloesung) {
	final String ret;
	switch (aufloesung) {
		case AUFLOESUNG_KLEIN:
		ret = getUrlNormalOrRequested(DatenFilm.FILM_URL_KLEIN);
		break;

		case AUFLOESUNG_HD:
		ret = getUrlNormalOrRequested(DatenFilm.FILM_URL_HD);
		break;

		default://AUFLOESUNG_NORMAL
		ret = arr[DatenFilm.FILM_URL];
		break;
	}

	return ret;
}

public String getDateigroesse(String url) {
	if (url.equals(arr[DatenFilm.FILM_URL])) {
		return arr[DatenFilm.FILM_GROESSE];
	} else {
		return FileSize.laengeString(url);
	}
}

public String getUrlHistory() {
	if (arr[DatenFilm.FILM_URL_HISTORY].isEmpty()) {
		return arr[DatenFilm.FILM_URL];
	} else {
		return arr[DatenFilm.FILM_URL_HISTORY];
	}
}

public String getIndex() {
	// liefert einen eindeutigen Index für die Filmliste
	// URL beim KiKa und ORF ändern sich laufend!
	return (getSender() + arr[FILM_THEMA]).toLowerCase() + getUrl();
}

public String getUrl() {
	return arr[DatenFilm.FILM_URL];
}

public boolean isHD() {
	//Film gibts in HD
	return !arr[DatenFilm.FILM_URL_HD].isEmpty();
}

public DatenFilm getCopy() {
	DatenFilm ret = new DatenFilm();
	System.arraycopy(this.arr, 0, ret.arr, 0, arr.length);
	ret.datumFilm = this.datumFilm;
	ret.databaseFilmNumber = this.databaseFilmNumber;
	ret.filmSize = this.filmSize;
	ret.filmLength = this.filmLength;
	ret.abo = this.abo;
	return ret;
}

@Override
public int compareTo(@NotNull DatenFilm other) {
	int ret;
	if ((ret = sorter.compare(getSender(), other.getSender())) == 0) {
		return sorter.compare(arr[FILM_THEMA], other.arr[FILM_THEMA]);
	}
	return ret;
}

/**
* Get the filmlength in seconds.
*
* @return filmlength in seconds, or 0.
*/
public long getFilmLength() {
	return filmLength;
}

/**
* Convert HH:MM:SS string into seconds.
*
* @return result in seconds or 0.
*/
private long parseTimeToSeconds() {
	long seconds = 0;
	final String[] split = StringUtils.split(arr[FILM_DAUER], ':');

	try {
		seconds += Long.parseLong(split[0]) * 3600; //hour
		seconds += Long.parseLong(split[1]) * 60; //minute
		seconds += Long.parseLong(split[2]); //second
	} catch (Exception e) {
		seconds = 0;
	}

	return seconds;
}


public void init() {
	filmSize = new MSLong(this);

	// get movie length;
	filmLength = parseTimeToSeconds();

	// set the date
	if (!arr[DatenFilm.FILM_DATUM].isEmpty()) {
		// nur dann gibts ein Datum
		try {
			final long l = Long.parseLong(arr[DatenFilm.FILM_DATUM_LONG]);
			datumFilm = new DatumFilm(l * 1000); // sind SEKUNDEN!!
		} catch (Exception ex) {
			logger.error("Datum: {}, Zeit: {}, Datum_LONG: {}", arr[DatenFilm.FILM_DATUM], arr[DatenFilm.FILM_ZEIT], arr[DatenFilm.FILM_DATUM_LONG], ex);
			datumFilm = new DatumFilm(0);
			arr[DatenFilm.FILM_DATUM] = "";
			arr[DatenFilm.FILM_ZEIT] = "";
		}
	}
}

private String getUrlNormalOrRequested(int indexUrl) {
	// liefert die kleine normale URL
	if (!arr[indexUrl].isEmpty()) {
		try {
			// Prüfen, ob Pipe auch in URL enthalten ist. Beim ZDF ist das nicht der Fall.
			final int indexPipe = arr[indexUrl].indexOf('|');
			if (indexPipe < 0) {
				return arr[indexUrl];
			}

			final int i = Integer.parseInt(arr[indexUrl].substring(0, indexPipe));
			return arr[DatenFilm.FILM_URL].substring(0, i) + arr[indexUrl].substring(arr[indexUrl].indexOf('|') + 1);
		} catch (Exception e) {
			Log.errorLog(915236703, e, arr[indexUrl]);
		}
	}
	return arr[DatenFilm.FILM_URL];
}



//TODO see if this is needed or becomes a slice later...
// movie listing
type tList struct {
	mov
}

//TODO maybe a slice of type tMov
var movieList tList


//TODO java class def

@SuppressWarnings("serial")
public class tList extends ArrayList<DatenFilm> {
	public static final String THEMA_LIVE = "Livestream";
	public static final String FILMLISTE = "Filmliste";

	public static final int FILMLISTE_DATUM_GMT_NR = 1;
	public static final int FILMLISTE_ID_NR = 4;
	public static final int MAX_ELEM = 5; //5 although not many indices are declared

	private final static String DATUM_ZEIT_FORMAT = "dd.MM.yyyy, HH:mm";
	private static final SimpleDateFormat sdf_ = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
	private static final Logger logger = LogManager.getLogger(tList.class);
	private final SimpleDateFormat sdf = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
	/**
	* List of available senders which notifies its users.
	*/
	private final ObservableList<String> senderList = FXCollections.observableArrayList();
	public String[] metaDaten = new String[]{"", "", "", "", ""};
	public boolean neueFilme = false;

	public tList() {
		super();
		sdf_.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
	}








//TODO extract datamodel from here...
//TODO from here old java
//TODO strip this file down

package mediathek.config;

import com.jidesoft.utils.SystemInfo;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import mSearch.Config;
import mSearch.daten.tList;
import mSearch.filmlisten.writer.FilmListWriter;
import mSearch.tool.Listener;
import mSearch.tool.ReplaceList;
import mediathek.MediathekGui;
import mediathek.controller.IoXmlLesen;
import mediathek.controller.IoXmlSchreiben;
import mediathek.controller.MVUsedUrls;
import mediathek.controller.starter.StarterClass;
import mediathek.daten.*;
import mediathek.filmlisten.FilmeLaden;
import mediathek.gui.actions.FilmListWriteWorkerTask;
import mediathek.gui.dialog.DialogMediaDB;
import mediathek.gui.filmInformation.InfoDialog;
import mediathek.gui.messages.BaseEvent;
import mediathek.gui.messages.TimerEvent;
import mediathek.javafx.CenteredBorderPane;
import mediathek.javafx.VerticalSeparator;
import mediathek.tool.GuiFunktionen;
import mediathek.tool.MVFont;
import mediathek.tool.MVMessageDialog;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Daten {

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	// zentrale Klassen
	public static final MVColor mVColor = new MVColor(); // verwendete Farben
	private static final Logger logger = LogManager.getLogger(Daten.class);

	public static ListePset listePset;
	//alle Programmeinstellungen
	public static InfoDialog filmInfo; // Infos zum Film
	private static Daten instance;
	// flags
	private static boolean startMaximized; // Fenster maximieren
	private static boolean auto; // Version: MediathekAuto
	private static boolean reset; // Programm auf Starteinstellungen zurücksetzen
	// Verzeichnis zum Speichern der Programmeinstellungen
	private static String basisverzeichnis;
	private MediathekGui mediathekGui; // JFrame der Gui
	public MVUsedUrls history; // alle angesehenen Filme
	public MVUsedUrls erledigteAbos; // erfolgreich geladenen Abos
	public StarterClass starterClass; // Klasse zum Ausführen der Programme (für die Downloads): VLC, flvstreamer, ...
	private FilmeLaden filmeLaden; // erledigt das updaten der Filmliste
	private tList listeFilme;
	private tList listeFilmeNachBlackList; // ist DIE Filmliste
	private tList listeFilmeHistory; // für die HEUTIGE HISTORY
	private ListeDownloads listeDownloads; // Filme die als "Download: Tab Download" geladen werden sollen
	private ListeDownloads listeDownloadsButton; // Filme die über "Tab Filme" als Button/Film abspielen gestartet werden
	private ListeBlacklist listeBlacklist;
	private ListeMediaDB listeMediaDB;
	private ListeMediaPath listeMediaPath;
	private ListeAbo listeAbo;
	private DownloadInfos downloadInfos;
	private DialogMediaDB dialogMediaDB;
	private boolean alreadyMadeBackup;
	private MBassador<BaseEvent> messageBus;
	private FilmListWriteWorkerTask writerTask;

	private Daten() {
		mediathekGui = null;
		start();
	}

	private Daten(MediathekGui aMediathekGui) {
		mediathekGui = aMediathekGui;
		start();
	}

	public static boolean isStartMaximized() {
		return startMaximized;
	}

	public static void setStartMaximized(final boolean aIsStartMaximized) {
		startMaximized = aIsStartMaximized;
	}

	public static boolean isAuto() {
		return auto;
	}

	public static void setAuto(final boolean aIsAuto) {
		auto = aIsAuto;
	}

	public static boolean isReset() {
		return reset;
	}

	public static void setReset(final boolean aIsReset) {
		reset = aIsReset;
	}

	public static Daten getInstance(String aBasisverzeichnis) {
		basisverzeichnis = aBasisverzeichnis;
		return getInstance();
	}

	public static Daten getInstance(String aBasisverzeichnis, MediathekGui aMediathekGui1) {
		basisverzeichnis = aBasisverzeichnis;
		return getInstance(aMediathekGui1);
	}

	private static Daten getInstance(MediathekGui aMediathekGui) {
		return instance == null ? instance = new Daten(aMediathekGui) : instance;
	}

	public static Daten getInstance() {
		return instance == null ? instance = new Daten() : instance;
	}

	/**
	* Liefert den Pfad zur Filmliste
	*
	* @return Den Pfad als String
	*/
	public static String getDateiFilmliste() {
		String strFile;

		strFile = getSettingsDirectory_String() + File.separator + setDBjson;

		return strFile;
	}

	/**
	* Return the location of the settings directory.
	* If it does not exist, create one.
	*
	* @return Path to the settings directory
	* @throws IllegalStateException Will be thrown if settings directory don't exist and if there is an error on creating it.
	*/
	private static Path getSettingsDirectory() throws IllegalStateException {
		final Path baseDirectoryPath;
		if (basisverzeichnis == null || basisverzeichnis.isEmpty()) {
			baseDirectoryPath = Paths.get(System.getProperty("user.home"), setDirSett);
		} else {
			baseDirectoryPath = Paths.get(basisverzeichnis);
		}

		if (Files.notExists(baseDirectoryPath)) {
			try {
				Files.createDirectories(baseDirectoryPath);
			} catch (IOException ioException) {
				Messages.logMessage(Messages.ERROR_CANT_CREATE_FOLDER, ioException, baseDirectoryPath.toString());
				throw new IllegalStateException(Messages.ERROR_CANT_CREATE_FOLDER.getTextFormatted(baseDirectoryPath.toString()), ioException);
			}
		}

		return baseDirectoryPath;
	}

	public static String getSettingsDirectory_String() {
		return getSettingsDirectory().toString();
	}

	/**
	* Return the path to "mediathek.xml"
	*
	* @return Path object to mediathek.xml file
	*/
	public static Path getMediathekXmlFilePath() {
		return Daten.getSettingsDirectory().resolve(setConf);
	}

	/**
	* Return the path to "mediathek.xml_copy_"
	* first copy exists
	*
	* @param xmlFilePath Path to file.
	*/
	private static void getMediathekXmlCopyFilePath(ArrayList<Path> xmlFilePath) {
		for (int i = 0; i < setNoBackup; ++i) {
			Path path = Daten.getSettingsDirectory().resolve(setConfCp + i);
			if (Files.exists(path)) {
				xmlFilePath.add(path);
			}
		}
	}

	/**
	* Return the number of milliseconds from today´s midnight.
	*
	* @return Number of milliseconds from today´s midnight.
	*/
	private static long getHeute_0Uhr() {
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTimeInMillis();
	}

	public FutureTask<Void> getWriterTask() {
		return writerTask;
	}

	public MBassador<BaseEvent> getMessageBus() {
		return messageBus;
	}

	/**
	* Set up message bus to log errors to our default logger
	*/
	private void setupMessageBus() {
		messageBus = new MBassador<>(new BusConfiguration()
		.addFeature(Feature.SyncPubSub.Default())
		.addFeature(Feature.AsynchronousHandlerInvocation.Default())
		.addFeature(Feature.AsynchronousMessageDispatch.Default())
		.addPublicationErrorHandler(error -> logger.error(error.getMessage(), error.getCause()))
		.setProperty(IBusConfiguration.Properties.BusId, "global bus"));
	}

	private void start() {
		setupMessageBus();

		listeFilme = new tList();
		filmeLaden = new FilmeLaden(this);
		listeFilmeHistory = new tList();

		updateSplashScreen("Lade Blacklist...");
		listeFilmeNachBlackList = new tList();
		listeBlacklist = new ListeBlacklist();

		updateSplashScreen("Lade Programmsets...");
		listePset = new ListePset();

		updateSplashScreen("Lade Abos...");
		listeAbo = new ListeAbo(this);

		updateSplashScreen("Lade Downloads...");
		listeDownloads = new ListeDownloads(this);
		listeDownloadsButton = new ListeDownloads(this);

		updateSplashScreen("Lade erledigte Abos...");
		erledigteAbos = new MVUsedUrls(setSubscrDone, getSettingsDirectory_String(), Listener.EREIGNIS_LISTE_ERLEDIGTE_ABOS);

		updateSplashScreen("Lade History...");
		history = new MVUsedUrls(setHistory, getSettingsDirectory_String(), Listener.EREIGNIS_LISTE_HISTORY_GEAENDERT);

		listeMediaDB = new ListeMediaDB(this);
		listeMediaPath = new ListeMediaPath();

		downloadInfos = new DownloadInfos(this);
		starterClass = new StarterClass(this);

		Timer timer = new Timer(1000, e ->
		{
			downloadInfos.makeDownloadInfos();
			messageBus.publishAsync(new TimerEvent());
		});
		timer.setInitialDelay(4000); // damit auch alles geladen ist
		timer.start();
	}

	public void filmlisteSpeichern() {
		try {
			if (writerTask != null) {
				logger.info("Waiting for worker task");
				writerTask.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			logger.error("Error waiting for worker task", e);
		}

		if (mediathekGui != null) {
			Platform.runLater(() -> {
				HBox hb = new HBox();
				hb.setSpacing(4d);
				Label lb = new Label("");
				ProgressBar prog = new ProgressBar();
				prog.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
				hb.getChildren().addAll(
				new VerticalSeparator(),
				new CenteredBorderPane(lb),
				new CenteredBorderPane(prog)
				);

				writerTask = new FilmListWriteWorkerTask(this);
				writerTask.setOnRunning(e -> {
					mediathekGui.getStatusBarController().getStatusBar().getRightItems().add(hb);
					lb.textProperty().bind(writerTask.messageProperty());
					prog.progressProperty().bind(writerTask.progressProperty());
				});
				writerTask.setOnSucceeded(e -> mediathekGui.getStatusBarController().getStatusBar().getRightItems().remove(hb));
				writerTask.setOnFailed(e -> mediathekGui.getStatusBarController().getStatusBar().getRightItems().remove(hb));
				new Thread(writerTask).start();
			});
		} else {
			FilmListWriter writer = new FilmListWriter();
			writer.writeFilmList(getDateiFilmliste(), listeFilme, null);
		}
	}

	/**
	* Update the {@link java.awt.SplashScreen} only if we have a Swing UI.
	*
	* @param text The displayed text on the splash graphics.
	*/
	private void updateSplashScreen(String text) {
		if (mediathekGui != null) {
			mediathekGui.updateSplashScreenText(text);
		}
	}

	public boolean allesLaden() {
		updateSplashScreen("Lade Konfigurationsdaten...");

		if (!load()) {
			logger.info("Weder Konfig noch Backup konnte geladen werden!");
			// teils geladene Reste entfernen
			clearKonfig();
			return false;
		}
		logger.info("Konfig wurde gelesen!");
		mVColor.load(); // Farben einrichten
		MVFont.initFont(); // Fonts einrichten

		// erst die Systemdaten, dann die Filmliste
		updateSplashScreen("Lade Filmliste...");
		return true;
	}

	private void clearKonfig() {
		listePset.clear();
		ReplaceList.list.clear();
		listeAbo.clear();
		listeDownloads.clear();
		listeBlacklist.clear();
	}

	private boolean load() {
		boolean ret = false;
		Path xmlFilePath = Daten.getMediathekXmlFilePath();

		if (Files.exists(xmlFilePath)) {
			final IoXmlLesen configReader = new IoXmlLesen();
			if (configReader.datenLesen(xmlFilePath)) {
				return true;
			} else {
				// dann hat das Laden nicht geklappt
				logger.info("Konfig konnte nicht gelesen werden!");
			}
		} else {
			// dann hat das Laden nicht geklappt
			logger.info("Konfig existiert nicht!");
		}

		// versuchen das Backup zu laden
		if (loadBackup()) {
			ret = true;
		}
		return ret;
	}

	private boolean loadBackup() {
		boolean ret = false;
		ArrayList<Path> path = new ArrayList<>();
		Daten.getMediathekXmlCopyFilePath(path);
		if (path.isEmpty()) {
			logger.info("Es gibt kein Backup");
			return false;
		}

		// dann gibts ein Backup
		logger.info("Es gibt ein Backup");
		mediathekGui.closeSplashScreen();
		int r = JOptionPane.showConfirmDialog(null, "Die Einstellungen sind beschädigt\n"
		+ "und können nicht geladen werden.\n"
		+ "Soll versucht werden, mit gesicherten\n"
		+ "Einstellungen zu starten?\n\n"
		+ "(ansonsten startet das Programm mit\n"
		+ "Standardeinstellungen)", "Gesicherte Einstellungen laden?", JOptionPane.YES_NO_OPTION);

		if (r != JOptionPane.OK_OPTION) {
			logger.info("User will kein Backup laden.");
			return false;
		}

		for (Path p : path) {
			// teils geladene Reste entfernen
			clearKonfig();
			logger.info("Versuch Backup zu laden: {}", p.toString());
			final IoXmlLesen configReader = new IoXmlLesen();
			if (configReader.datenLesen(p)) {
				logger.info("Backup hat geklappt: {}", p.toString());
				ret = true;
				break;
			}

		}
		return ret;
	}

	public void allesSpeichern() {
		konfigCopy();

		final IoXmlSchreiben configWriter = new IoXmlSchreiben();
		configWriter.writeConfigurationFile(getMediathekXmlFilePath());

		if (Daten.isReset()) {
			// das Programm soll beim nächsten Start mit den Standardeinstellungen gestartet werden
			// dazu wird den Ordner mit den Einstellungen umbenannt
			String dir1 = getSettingsDirectory_String();
			if (dir1.endsWith(File.separator)) {
				dir1 = dir1.substring(0, dir1.length() - 1);
			}

			try {
				final Path path1 = Paths.get(dir1);
				final String dir2 = dir1 + "--" + new SimpleDateFormat("yyyy.MM.dd__HH.mm.ss").format(new Date());

				Files.move(path1, Paths.get(dir2), StandardCopyOption.REPLACE_EXISTING);
				Files.deleteIfExists(path1);
			} catch (IOException e) {
				logger.error("Die Einstellungen konnten nicht zurückgesetzt werden.", e);
				if (mediathekGui != null) {
					MVMessageDialog.showMessageDialog(mediathekGui, "Die Einstellungen konnten nicht zurückgesetzt werden.\n"
					+ "Sie müssen jetzt das Programm beenden und dann den Ordner:\n"
					+ getSettingsDirectory_String() + '\n'
					+ "von Hand löschen und dann das Programm wieder starten.\n\n"
					+ "Im Forum finden Sie weitere Hilfe.", "Fehler", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	* Create backup copies of settings file.
	*/
	private void konfigCopy() {
		if (!alreadyMadeBackup) {
			// nur einmal pro Programmstart machen
			logger.info("-------------------------------------------------------");
			logger.info("Einstellungen sichern");

			try {
				final Path xmlFilePath = Daten.getMediathekXmlFilePath();
				long creatTime = -1;

				Path xmlFilePathCopy_1 = Daten.getSettingsDirectory().resolve(setConfCp + 1);
				if (Files.exists(xmlFilePathCopy_1)) {
					BasicFileAttributes attrs = Files.readAttributes(xmlFilePathCopy_1, BasicFileAttributes.class);
					FileTime d = attrs.lastModifiedTime();
					creatTime = d.toMillis();
				}

				if (creatTime == -1 || creatTime < getHeute_0Uhr()) {
					// nur dann ist die letzte Kopie älter als einen Tag
					for (int i = setNoBackup; i > 1; --i) {
						xmlFilePathCopy_1 = Daten.getSettingsDirectory().resolve(setConfCp + (i - 1));
						final Path xmlFilePathCopy_2 = Daten.getSettingsDirectory().resolve(setConfCp + i);
						if (Files.exists(xmlFilePathCopy_1)) {
							Files.move(xmlFilePathCopy_1, xmlFilePathCopy_2, StandardCopyOption.REPLACE_EXISTING);
						}
					}
					if (Files.exists(xmlFilePath)) {
						Files.move(xmlFilePath, Daten.getSettingsDirectory().resolve(setConfCp + 1), StandardCopyOption.REPLACE_EXISTING);
					}
					logger.info("Einstellungen wurden gesichert");
				} else {
					logger.info("Einstellungen wurden heute schon gesichert");
				}
			} catch (IOException e) {
				logger.error("Die Einstellungen konnten nicht komplett gesichert werden!", e);
			}

			alreadyMadeBackup = true;
			logger.info("-------------------------------------------------------");
		}
	}

	public FilmeLaden getFilmeLaden() {
		return filmeLaden;
	}

	public tList getListeFilme() {
		return listeFilme;
	}

	public tList getListeFilmeNachBlackList() {
		return listeFilmeNachBlackList;
	}

	public tList getListeFilmeHistory() {
		return listeFilmeHistory;
	}

	public ListeDownloads getListeDownloads() {
		return listeDownloads;
	}

	public ListeDownloads getListeDownloadsButton() {
		return listeDownloadsButton;
	}

	public ListeBlacklist getListeBlacklist() {
		return listeBlacklist;
	}

	public ListeMediaDB getListeMediaDB() {
		return listeMediaDB;
	}

	public ListeMediaPath getListeMediaPath() {
		return listeMediaPath;
	}

	public ListeAbo getListeAbo() {
		return listeAbo;
	}

	public DownloadInfos getDownloadInfos() {
		return downloadInfos;
	}

	public MediathekGui getMediathekGui() {
		return mediathekGui;
	}

	public void setMediathekGui(MediathekGui gui) {
		mediathekGui = gui;
	}

	public DialogMediaDB getDialogMediaDB() {
		return dialogMediaDB;
	}

	public void setDialogMediaDB(final DialogMediaDB aDialogMediaDB) {
		dialogMediaDB = aDialogMediaDB;
	}

}
