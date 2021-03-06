
package mediathog.daten;

import mSearch.tool.Listener;
import mediathog.config.Daten;
import mediathog.config.Konstanten;
import mediathog.config.MVConfig;
import mediathog.tool.Filter;
import mediathog.tool.MVMessageDialog;
import mediathog.tool.TModelMediaDB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class ListeMediaDB extends LinkedList<DatenMediaDB> {
	public final static String TRENNER = " |###| ";
	public final static String FILE_SEPERATOR_MEDIA_PATH = "<>";
	private boolean makeIndex = false;
	private String[] suffix = {""};
	private boolean ohneSuffix = true;

	private final Daten daten;

	public ListeMediaDB(Daten aDaten) {
		daten = aDaten;
	}

	public synchronized void getModelMediaDB(TModelMediaDB modelMediaDB) {
		modelMediaDB.setRowCount(0);
		this.forEach((mdb) -> modelMediaDB.addRow(mdb.getRow()));
	}

	public synchronized void searchFilmInDB(TModelMediaDB foundModel, String title) {
		foundModel.setRowCount(0);
		if (!makeIndex && !title.isEmpty()) {
			Pattern p = Filter.makePattern(title);
			if (p != null) {
				// dann mit RegEx prüfen
				daten.getListeMediaDB().stream().filter(s -> p.matcher(s.arr[DatenMediaDB.MEDIA_DB_NAME]).matches()).forEach(s -> foundModel.addRow(s.getRow()));
			} else {
				title = title.toLowerCase();
				for (DatenMediaDB s : daten.getListeMediaDB()) {
					if (s.arr[DatenMediaDB.MEDIA_DB_NAME].toLowerCase().contains(title)) {
						foundModel.addRow(s.getRow());
					}
				}
			}
		}
	}

	public synchronized void cleanList() {
		CleanerThread thread = new CleanerThread();
		thread.start();
	}

	class CleanerThread extends Thread {
		public CleanerThread() {
			setName("Clean MediaDB");
		}

		@Override
		public void run() {
			logger.info("Clean MediaDB start");
			Listener.notify(Listener.EREIGNIS_MEDIA_DB_START, ListeMediaDB.class.getSimpleName());
			makeIndex = true;

			clean();

			makeIndex = false;
			Listener.notify(Listener.EREIGNIS_MEDIA_DB_STOP, ListeMediaDB.class.getSimpleName());
			logger.info("Clean MediaDB stop");
		}
	}

	private void clean() {
		final HashSet<String> hash = new HashSet<>();
		ListeMediaDB tmp = new ListeMediaDB(daten);
		this.forEach(m -> {
			final String s = m.getEqual();
			if (!hash.contains(s)) {
				hash.add(s);
				tmp.add(m);
			}
		});

		this.clear();
		this.addAll(tmp);
		tmp.clear();
		hash.clear();

		exportListe("");
	}

	public synchronized void delList(boolean ohneSave) {
		Listener.notify(Listener.EREIGNIS_MEDIA_DB_START, ListeMediaDB.class.getSimpleName());
		makeIndex = true;

		del(ohneSave);

		makeIndex = false;
		Listener.notify(Listener.EREIGNIS_MEDIA_DB_STOP, ListeMediaDB.class.getSimpleName());
	}

	private void del(boolean ohneSave) {
		if (ohneSave) {
			this.removeIf(datenMediaDB -> !datenMediaDB.isExtern());
		} else {
			clear();
			exportListe("");
		}
	}

	public synchronized void createMediaDB(String pfad) {
		Listener.notify(Listener.EREIGNIS_MEDIA_DB_START, ListeMediaDB.class.getSimpleName());
		suffix = MVConfig.get(MVConfig.Configs.SYSTEM_MEDIA_DB_SUFFIX).split(",");
		for (int i = 0; i < suffix.length; ++i) {
			suffix[i] = suffix[i].toLowerCase();
			if (!suffix[i].isEmpty() && !suffix[i].startsWith(".")) {
				suffix[i] = '.' + suffix[i];
			}
		}
		ohneSuffix = Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_MEDIA_DB_SUFFIX_OHNE));

		makeIndex = true;
		if (pfad.isEmpty()) {
			del(true /*ohneSave*/);
		}

		createIndexThread(new Index(pfad));
	}

	private void createIndexThread(Index runner) {
		Thread starter = new Thread(runner);
		starter.setName("Index Thread");
		starter.start();
	}

	public synchronized void loadSavedList() {
		Path urlPath = getFilePath();
		//use Automatic Resource Management
		try (LineNumberReader in = new LineNumberReader(Files.newBufferedReader(urlPath))) {
			String zeile;
			while ((zeile = in.readLine()) != null) {
				DatenMediaDB mdb = getUrlAusZeile(zeile);
				if (mdb != null) {
					add(mdb);
				}
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
	}

	public synchronized void exportListe(String datei) {
		Path logFilePath = null;
		boolean export = false;
		logger.info("MediaDB schreiben ({}) Dateien :", daten.getListeMediaDB().size());
		if (!datei.isEmpty()) {
			export = true;
			try {
				File file = new File(datei);
				File dir = new File(file.getParent());
				if (!dir.exists()) {
					if (!dir.mkdirs()) {
						logger.error("Kann den Pfad nicht anlegen: {}", dir.toString());
					}
				}
				logger.info(" --> Start Schreiben nach: {}", datei);
				logFilePath = file.toPath();
			} catch (Exception ex) {
				logger.error("nach: {}", datei, ex);
			}
		} else {
			logger.info(" --> Start Schreiben nach: {}", getFilePath().toString());
			logFilePath = getFilePath();
		}

		try (BufferedWriter bw = Files.newBufferedWriter(logFilePath)) {
			bw.newLine();
			bw.newLine();
			for (DatenMediaDB entry : this) {
				if (!datei.isEmpty()) {
					//dann alles schreiben
					bw.write(getLine(entry, export));
					bw.newLine();
				} else if (entry.isExtern()) {
					//in der Konfig nur die externen
					bw.write(getLine(entry, export));
					bw.newLine();
				}
			}
			bw.newLine();
			//
			bw.flush();
		} catch (Exception ex) {
			SwingUtilities.invokeLater(() -> MVMessageDialog.showMessageDialog(null, "Datei konnte nicht geschrieben werden!",
			"Fehler beim Schreiben", JOptionPane.ERROR_MESSAGE));
		}
		logger.info(" --> geschrieben!");
	}

	private static final Logger logger = LogManager.getLogger(ListeMediaDB.class);

	private class Index implements Runnable {

		String pfad;
		String error = "";
		boolean more = false;

		public Index(String pfad) {
			this.pfad = pfad;
		}

		@Override
		public synchronized void run() {
			logger.debug("Mediensammlung erstellen");
			try {
				if (!pfad.isEmpty()) {
					// dann nur einen Pfad hinzufügen
					File f = new File(pfad);
					if (!f.canRead()) {
						if (!error.isEmpty()) {
							error = error + '\n';
						}
						error = error + f.getPath();
					}
					if (!error.isEmpty()) {
						// Verzeichnisse können nicht durchsucht werden
						errorMsg();
					}
					searchFile(new File(pfad), true);

				} else if (!daten.getListeMediaPath().isEmpty()) {
					for (DatenMediaPath mp : daten.getListeMediaPath()) {
						if (mp.savePath()) {
							continue;
						}
						File f = new File(mp.arr[DatenMediaPath.MEDIA_PATH_PATH]);
						if (!f.canRead()) {
							if (!error.isEmpty()) {
								error = error + '\n';
								more = true;
							}
							error = error + f.getPath();
						}
					}
					if (!error.isEmpty()) {
						// Verzeichnisse können nicht durchsucht werden
						errorMsg();
					}
					daten.getListeMediaPath().stream().filter((mp) -> (!mp.savePath())).forEach((mp) -> searchFile(new File(mp.arr[DatenMediaPath.MEDIA_PATH_PATH]), false));
				}
			} catch (Exception ex) {
				logger.error(ex);
			}

			daten.getListeMediaDB().exportListe("");
			makeIndex = false;
			logger.debug("Ende Mediensammlung erstellen");

			Listener.notify(Listener.EREIGNIS_MEDIA_DB_STOP, ListeMediaDB.class.getSimpleName());
		}

		private void errorMsg() {
			MVMessageDialog.showMessageDialog(null, (more ? "Die Pfade der Mediensammlung können nicht alle gelesen werden:\n"
			: "Der Pfad der Mediensammlung kann nicht gelesen werden:\n")
			+ error, "Fehler beim Erstellen der Mediensammlung", JOptionPane.ERROR_MESSAGE);
		}

		private void searchFile(File dir, boolean save) {
			if (dir == null) {
				return;
			}
			File[] files = dir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						searchFile(file, save);
					} else if (checkSuffix(suffix, file.getName())) {
						daten.getListeMediaDB().add(new DatenMediaDB(file.getName(), file.getParent().intern(), file.length(), save));
					}
				}
			}
		}

	}

	private boolean checkSuffix(String[] str, String uurl) {
		// liefert TRUE wenn die Datei in die Mediensammlung kommt
		// prüfen ob url mit einem Argument in str endet
		// wenn str leer dann true
		if (str.length == 1 && str[0].isEmpty()) {
			return true;
		}

		boolean ret = true;
		final String url = uurl.toLowerCase();
		for (String s : str) {
			//Suffix prüfen
			if (ohneSuffix) {
				if (url.endsWith(s)) {
					ret = false;
					break;
				}
			} else {
				ret = false;
				if (url.endsWith(s)) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}

	private String minTextLaenge(String text) {
		StringBuilder textBuilder = new StringBuilder(text);
		while (textBuilder.length() < 60) {
			textBuilder.append(' ');
		}
		return textBuilder.toString();
	}


	private String getLine(DatenMediaDB med, boolean export) {
		if (export) {
			return med.arr[DatenMediaDB.MEDIA_DB_NAME];
		}
		String ret = "";
		ret += minTextLaenge(med.arr[DatenMediaDB.MEDIA_DB_NAME]) + TRENNER;
		ret += minTextLaenge(med.arr[DatenMediaDB.MEDIA_DB_PATH]) + TRENNER;
		ret += String.valueOf(med.mVMediaDBFileSize.sizeL);
		return ret;
	}

	private Path getFilePath() {
		Path urlPath = null;
		try {
			urlPath = Paths.get(Daten.getSettingsDirectory_String()).resolve(setDB);
			if (Files.notExists(urlPath)) {
				urlPath = Files.createFile(urlPath);
			}
		} catch (IOException ex) {
			logger.warn(ex);
		}
		return urlPath;
	}

	private DatenMediaDB getUrlAusZeile(String zeile) {
		//02-202.mp3 |###| /tmp/John Grisham/Das Komplott 1 |###| 3
		if (zeile.isEmpty()) {
			return null;
		}
		String name = "", pfad = "", s = "";
		long size = 0;
		try {
			if (zeile.contains(TRENNER)) {
				name = zeile.substring(0, zeile.indexOf(TRENNER)).trim();
				pfad = zeile.substring(zeile.indexOf(TRENNER) + TRENNER.length(), zeile.lastIndexOf(TRENNER)).trim();
				s = zeile.substring(zeile.lastIndexOf(TRENNER) + TRENNER.length()).trim();
			}
			if (!s.isEmpty()) {
				try {
					size = Integer.parseInt(s);
				} catch (Exception ignore) {
					size = 0;
				}
			}
			return new DatenMediaDB(name, pfad, size, true /*extern*/);
		} catch (Exception ex) {
			logger.error(ex);
		}
		return null;
	}
}
