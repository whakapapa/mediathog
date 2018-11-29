
package mediathog.tool;

import mSearch.Const;
import mSearch.tool.ApplicationConfiguration;
import mediathog.config.Daten;
import mediathog.config.MVConfig;
import mediathog.controller.starter.RuntimeExec;
import mediathog.daten.DatenProg;
import mediathog.daten.DatenPset;
import mediathog.daten.ListePset;
import mediathog.gui.dialog.DialogHilfe;
import mediathog.gui.dialogEinstellungen.DialogImportPset;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GuiFunktionenProgramme extends GuiFunktionen {


	public static void addSetVorlagen(JFrame parent, Daten daten, ListePset pSet, boolean auto, boolean setVersion) {
		if (pSet == null) {
			if (!auto) {
				MVMessageDialog.showMessageDialog(null, "Die Datei wurde nicht importiert!",
				"Fehler", JOptionPane.ERROR_MESSAGE);
			}
			return;
		}
		if (parent != null) {
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		for (DatenPset ps : pSet) {
			if (!ps.arr[DatenPset.PROGRAMMSET_ADD_ON].isEmpty()) {
				if (!addOnZip(ps.arr[DatenPset.PROGRAMMSET_ADD_ON])) {
					// und Tschüss
					if (!auto) {
						MVMessageDialog.showMessageDialog(null, "Die Datei wurde nicht importiert!",
						"Fehler", JOptionPane.ERROR_MESSAGE);
					}
					return;
				}
			}
		}
		if (parent != null) {
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		if (auto) {
			if (Daten.listePset.addPset(pSet)) {
				if (setVersion) {
					MVConfig.add(MVConfig.Configs.SYSTEM_VERSION_PROGRAMMSET, pSet.version);
				}
			}
		} else {
			DialogImportPset dialog = new DialogImportPset(parent, true, daten, pSet);
			dialog.setVisible(true);
			if (dialog.ok) {
				if (Daten.listePset.addPset(pSet)) {
					if (setVersion) {
						MVConfig.add(MVConfig.Configs.SYSTEM_VERSION_PROGRAMMSET, pSet.version);
					}
					MVMessageDialog.showMessageDialog(null, pSet.size() + " Programmset importiert!",
					"Ok", JOptionPane.INFORMATION_MESSAGE);

				} else {
					MVMessageDialog.showMessageDialog(null, "Die Datei wurde nicht importiert!",
					"Fehler", JOptionPane.ERROR_MESSAGE);

				}
			}
		}
	}

	private static boolean addOnZip(String datei) {
		String zielPfad = addsPfad(getPathJar(), "bin");
		File zipFile;
		int timeout = 10_000; //10 Sekunden
		int n;
		URLConnection conn;
		try {
			if (!GuiFunktionen.istUrl(datei)) {
				zipFile = new File(datei);
				if (!zipFile.exists()) {
					// und Tschüss
					return false;
				}
				if (datei.endsWith(setCzip)) {
					if (!entpacken(zipFile, new File(zielPfad))) {
						// und Tschüss
						return false;
					}
				} else {
					try (FileInputStream in = new FileInputStream(datei);
					FileOutputStream fOut = new FileOutputStream(GuiFunktionen.addsPfad(zielPfad, datei))) {
						final byte[] buffer = new byte[1024];
						while ((n = in.read(buffer)) != -1) {
							fOut.write(buffer, 0, n);
						}
					}
				}
			} else {
				conn = new URL(datei).openConnection();
				conn.setConnectTimeout(timeout);
				conn.setReadTimeout(timeout);
				conn.setRequestProperty("User-Agent", ApplicationConfiguration.getConfiguration()
				.getString(ApplicationConfiguration.APPLICATION_USER_AGENT));
				if (datei.endsWith(setCzip)) {

					File tmpFile = File.createTempFile("mediathek", null);
					tmpFile.deleteOnExit();
					try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
					FileOutputStream fOut = new FileOutputStream(tmpFile)) {
						final byte[] buffer = new byte[1024];
						while ((n = in.read(buffer)) != -1) {
							fOut.write(buffer, 0, n);
						}
					}
					if (!entpacken(tmpFile, new File(zielPfad))) {
						// und Tschüss
						return false;
					}

				} else {
					String file = GuiFunktionen.getDateiName(datei);
					File f = new File(GuiFunktionen.addsPfad(zielPfad, file));
					try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
					FileOutputStream fOut = new FileOutputStream(f)) {
						final byte[] buffer = new byte[1024];
						while ((n = in.read(buffer)) != -1) {
							fOut.write(buffer, 0, n);
						}
					}
				}
			}
		} catch (Exception ignored) {
		}
		return true;
	}

	private static boolean entpacken(File archive, File destDir) throws Exception {
		if (!destDir.exists()) {
			return false;
		}


		try (ZipFile zipFile = new ZipFile(archive)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			byte[] buffer = new byte[16384];
			int len;
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				String entryFileName = entry.getName();

				File dir = buildDirectoryHierarchyFor(entryFileName, destDir);
				if (!dir.exists()) {
					dir.mkdirs();
				}

				if (!entry.isDirectory()) {
					try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(destDir, entryFileName)));
					BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry))) {
						while ((len = bis.read(buffer)) > 0) {
							bos.write(buffer, 0, len);
						}
						bos.flush();
					}
				}
			}
		}

		return true;
	}

	private static File buildDirectoryHierarchyFor(String entryName, File destDir) {
		int lastIndex = entryName.lastIndexOf('/');
		String internalPathToEntry = entryName.substring(0, lastIndex + 1);
		return new File(destDir, internalPathToEntry);
	}

	public static boolean praefixTesten(String str, String uurl, boolean praefix) {
		//prüfen ob url beginnt/endet mit einem Argument in str
		//wenn str leer dann true
		boolean ret = false;
		String url = uurl.toLowerCase();
		String s1 = "";
		if (str.isEmpty()) {
			ret = true;
		} else {
			for (int i = 0; i < str.length(); ++i) {
				if (str.charAt(i) != ',') {
					s1 += str.charAt(i);
				}
				if (str.charAt(i) == ',' || i >= str.length() - 1) {
					if (praefix) {
						//Präfix prüfen
						if (url.startsWith(s1.toLowerCase())) {
							ret = true;
							break;
						}
					} else //Suffix prüfen
					if (url.endsWith(s1.toLowerCase())) {
						ret = true;
						break;
					}
					s1 = "";
				}
			}
		}
		return ret;
	}

	public static boolean checkPfadBeschreibbar(String pfad) {
		boolean ret = false;
		File testPfad = new File(pfad);
		try {
			if (!testPfad.exists()) {
				testPfad.mkdirs();
			}
			if (pfad.isEmpty()) {
			} else if (!testPfad.isDirectory()) {
			} else if (testPfad.canWrite()) {
				File tmpFile = File.createTempFile("mediathek", "tmp", testPfad);
				tmpFile.delete();
				ret = true;
			}
		} catch (Exception ignored) {
		}
		return ret;
	}

	public static boolean programmePruefen(JFrame jFrame, Daten daten) {
		// prüfen ob die eingestellten Programmsets passen
		final String PIPE = "| ";
		final String LEER = " ";
		final String PFEIL = " -> ";
		boolean ret = true;
		String text = "";

		for (DatenPset datenPset : Daten.listePset) {
			ret = true;
			if (!datenPset.isFreeLine() && !datenPset.isLable()) {
				// nur wenn kein Lable oder freeline
				text += "++++++++++++++++++++++++++++++++++++++++++++" + '\n';
				text += PIPE + "Programmgruppe: " + datenPset.arr[DatenPset.PROGRAMMSET_NAME] + '\n';
				String zielPfad = datenPset.arr[DatenPset.PROGRAMMSET_ZIEL_PFAD];
				if (datenPset.progsContainPath()) {
					// beim nur Abspielen wird er nicht gebraucht
					if (zielPfad.isEmpty()) {
						ret = false;
						text += PIPE + LEER + "Zielpfad fehlt!\n";
					} else // Pfad beschreibbar?
					if (!checkPfadBeschreibbar(zielPfad)) {
						//da Pfad-leer und "kein" Pfad schon abgeprüft
						ret = false;
						text += PIPE + LEER + "Falscher Zielpfad!\n";
						text += PIPE + LEER + PFEIL + "Zielpfad \"" + zielPfad + "\" nicht beschreibbar!" + '\n';
					}
				}
				for (DatenProg datenProg : datenPset.getListeProg()) {
					// Programmpfad prüfen
					if (datenProg.arr[DatenProg.PROGRAMM_PROGRAMMPFAD].isEmpty()) {
						ret = false;
						text += PIPE + LEER + "Kein Programm angegeben!\n";
						text += PIPE + LEER + PFEIL + "Programmname: " + datenProg.arr[DatenProg.PROGRAMM_NAME] + '\n';
						text += PIPE + LEER + LEER + "Pfad: " + datenProg.arr[DatenProg.PROGRAMM_PROGRAMMPFAD] + '\n';
					} else if (!new File(datenProg.arr[DatenProg.PROGRAMM_PROGRAMMPFAD]).canExecute()) {
						// dann noch mit RuntimeExec versuchen
						RuntimeExec r = new RuntimeExec(datenProg.arr[DatenProg.PROGRAMM_PROGRAMMPFAD]);
						Process pr = r.exec(false /*log*/);
						if (pr != null) {
							// dann passts ja
							pr.destroy();
						} else {
							// läßt sich nicht starten
							ret = false;
							text += PIPE + LEER + "Falscher Programmpfad!\n";
							text += PIPE + LEER + PFEIL + "Programmname: " + datenProg.arr[DatenProg.PROGRAMM_NAME] + '\n';
							text += PIPE + LEER + LEER + "Pfad: " + datenProg.arr[DatenProg.PROGRAMM_PROGRAMMPFAD] + '\n';
							if (!datenProg.arr[DatenProg.PROGRAMM_PROGRAMMPFAD].contains(File.separator)) {
								text += PIPE + LEER + PFEIL + "Wenn das Programm nicht im Systempfad liegt, " + '\n';
								text += PIPE + LEER + LEER + "wird der Start nicht klappen!" + '\n';
							}
						}
					}
				}
				if (ret) {
					//sollte alles passen
					text += PIPE + PFEIL + "Ok!" + '\n';
				}
				text += "++++++++++++++++++++++++++++++++++++++++++++" + "\n\n\n";
			}
		}
		new DialogHilfe(jFrame, true, text).setVisible(true);
		return ret;
	}
}
