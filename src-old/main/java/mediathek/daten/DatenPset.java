
package mediathek.daten;

import com.jidesoft.utils.SystemInfo;
import mSearch.daten.DatenFilm;
import mSearch.tool.Log;
import mediathek.config.Daten;
import mediathek.tool.GuiFunktionenProgramme;
import mediathek.tool.MVMessageDialog;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

public class DatenPset extends MVData<DatenPset> {

	//Tags Programmgruppen
	//TODO heavy cleanup
	public static final int PROGRAMMSET_NAME = 0;
	public static final int PROGRAMMSET_PRAEFIX_DIREKT = 1;
	public static final int PROGRAMMSET_SUFFIX_DIREKT = 2;
	public static final int PROGRAMMSET_FARBE = 3;
	public static final int PROGRAMMSET_ZIEL_PFAD = 4;
	public static final int PROGRAMMSET_ZIEL_DATEINAME = 5;
	public static final int PROGRAMMSET_THEMA_ANLEGEN = 6;
	public static final int PROGRAMMSET_IST_ABSPIELEN = 7;
	public static final int PROGRAMMSET_IST_SPEICHERN = 8;
	public static final int PROGRAMMSET_IST_BUTTON = 9;
	public static final int PROGRAMMSET_IST_ABO = 10;
	public static final int PROGRAMMSET_LAENGE_BESCHRAENKEN = 11;
	public static final int PROGRAMMSET_LAENGE_FIELD_BESCHRAENKEN = 12;
	public static final int PROGRAMMSET_MAX_LAENGE = 13;
	public static final int PROGRAMMSET_MAX_LAENGE_FIELD = 14;
	public static final int PROGRAMMSET_AUFLOESUNG = 15;
	public static final int PROGRAMMSET_ADD_ON = 16;
	public static final int PROGRAMMSET_BESCHREIBUNG = 17;
	public static final int PROGRAMMSET_INFO_URL = 18;
	public static final int PROGRAMMSET_INFODATEI = 19;
	public static final int PROGRAMMSET_SUBTITLE = 21;

	public static final String TAG = "Programmset";
	public static final int MAX_ELEM = 22;

	public static final String[] COLUMN_NAMES = {"Setname", "Präfix", "Suffix", "Farbe", "Zielpfad", "Zieldateiname", "Thema anlegen",
	"Abspielen", "Speichern", "Button", "Abo", "Länge", "Länge Feld", "max Länge", "max Länge Feld", "Auflösung", "AddOn",
	"Beschreibung", "Url Info", "Infodatei", "Spotlight", "Untertitel"};
	public static final String[] XML_NAMES = {"Name", "Praefix", "Suffix", "Farbe", "Zielpfad", "Zieldateiname", "Thema-anlegen",
	"Abspielen", "Speichern", "Button", "Abo", "Laenge", "Laenge-Feld", "max-Laenge", "max-Laenge-Feld", "Aufloesung", "AddOn",
	"Beschreibung", "Info-URL", "Infodatei", "Spotlight", "Untertitel"};
	private ListeProg listeProg = new ListeProg();
	public static boolean[] spaltenAnzeigen = new boolean[MAX_ELEM];
	public String[] arr;

	public DatenPset() {
		makeArray();
	}

	public DatenPset(String name) {
		// neue Pset sind immer gleich Button
		makeArray();
		arr[PROGRAMMSET_NAME] = name;
		arr[PROGRAMMSET_IST_BUTTON] = Boolean.TRUE.toString();
	}

	// public
	public boolean addProg(DatenProg prog) {
		return listeProg.add(prog);
	}

	public ListeProg getListeProg() {
		return listeProg;
	}

	public DatenProg getProg(int i) {
		return listeProg.get(i);
	}

	public boolean progsContainPath() {
		// ein Programmschalter mit
		// "**" (Pfad/Datei) oder %a (Pfad) oder %b (Datei)
		// damit ist es ein Set zum Speichern
		boolean ret = false;
		Iterator<DatenProg> it = listeProg.iterator();
		DatenProg prog;
		while (it.hasNext()) {
			prog = it.next();
			if (prog.arr[DatenProg.PROGRAMM_SCHALTER].contains("**")
			|| prog.arr[DatenProg.PROGRAMM_SCHALTER].contains("%a")
			|| prog.arr[DatenProg.PROGRAMM_SCHALTER].contains("%b")) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	public boolean isEmpty() {
		boolean ret = true;
		for (String s : arr) {
			if (!s.isEmpty()) {
				ret = false;
			}
		}
		if (!listeProg.isEmpty()) {
			ret = false;
		}
		return ret;
	}

	public boolean istAbspielen() {
		return Boolean.parseBoolean(arr[PROGRAMMSET_IST_ABSPIELEN]);
	}

	public boolean istSpeichern() {
		return Boolean.parseBoolean(arr[PROGRAMMSET_IST_SPEICHERN]);
	}

	public boolean istButton() {
		return Boolean.parseBoolean(arr[PROGRAMMSET_IST_BUTTON]);
	}

	public boolean istAbo() {
		return Boolean.parseBoolean(arr[PROGRAMMSET_IST_ABO]);
	}

	public boolean isLable() {
		// wenn die Programmliste leer ist und einen Namen hat, ist es ein Lable
		if (this.listeProg.isEmpty()) {
			if (!this.arr[PROGRAMMSET_NAME].equals("")) {
				return true;
			}
		}
		return false;
	}

	public boolean isFreeLine() {
		//Wenn die Programmgruppe keinen Namen hat, leere Zeile
		return this.arr[PROGRAMMSET_NAME].equals("");
	}

	public void setAbspielen(Daten daten) {
		for (DatenPset datenPset : Daten.listePset) {
			datenPset.arr[DatenPset.PROGRAMMSET_IST_ABSPIELEN] = Boolean.FALSE.toString();
		}
		arr[DatenPset.PROGRAMMSET_IST_ABSPIELEN] = Boolean.TRUE.toString();
	}

	public void setSpeichern(boolean set) {
		arr[DatenPset.PROGRAMMSET_IST_SPEICHERN] = Boolean.toString(set);
	}

	public void setButton(boolean set) {
		arr[DatenPset.PROGRAMMSET_IST_BUTTON] = Boolean.toString(set);
	}

	public void setAbo(boolean set) {
		arr[DatenPset.PROGRAMMSET_IST_ABO] = Boolean.toString(set);
	}

	public DatenProg getProgUrl(String url) {
		//mit einer Url das Passende Programm finden
		//passt nichts, wird das letzte Programm genommen
		//ist nur ein Programm in der Liste wird dieses genommen
		DatenProg ret = null;
		if (listeProg.isEmpty()) {
			MVMessageDialog.showMessageDialog(null, "Programme einrichten!",
			"Kein Programm", JOptionPane.INFORMATION_MESSAGE);
		} else if (listeProg.size() == 1) {
			ret = listeProg.getFirst();
		} else {
			Iterator<DatenProg> it = listeProg.iterator();
			DatenProg prog;
			while (it.hasNext()) {
				prog = it.next();
				if (prog.urlTesten(url)) {
					ret = prog;
					break;
				}
			}
			if (!listeProg.isEmpty() && ret == null) {
				ret = listeProg.getLast();
			}
		}
		return ret;
	}

	public String getZielDateiname(String url) {
		//gibt den Zieldateinamen für den Film zurück
		DatenProg prog = this.getProgUrl(url);
		String ret = arr[PROGRAMMSET_ZIEL_DATEINAME];
		if (!checkDownloadDirekt(url) && prog != null) {
			// nur wenn kein direkter Download und ein passendes Programm
			if (!prog.arr[DatenProg.PROGRAMM_ZIEL_DATEINAME].equals("")) {
				ret = prog.arr[DatenProg.PROGRAMM_ZIEL_DATEINAME];
			}
		}
		return ret;
	}

	public String getZielPfad() {
		//gibt den Zielpfad für den Film zurück
		return arr[PROGRAMMSET_ZIEL_PFAD];
	}

	public DatenPset copy() {
		DatenPset ret = new DatenPset();
		System.arraycopy(this.arr, 0, ret.arr, 0, arr.length);
		//es darf nur einen geben!
		ret.arr[PROGRAMMSET_NAME] = "Kopie-" + arr[PROGRAMMSET_NAME];
		ret.arr[PROGRAMMSET_IST_ABSPIELEN] = Boolean.toString(false);
		for (DatenProg prog : getListeProg()) {
			ret.addProg(prog.copy());
		}
		return ret;
	}

	public Color getFarbe() {
		Color ret = null;
		String r, g, b;
		if (!arr[PROGRAMMSET_FARBE].equals("")) {
			r = arr[PROGRAMMSET_FARBE].substring(0, arr[PROGRAMMSET_FARBE].indexOf(','));
			g = arr[PROGRAMMSET_FARBE].substring(arr[PROGRAMMSET_FARBE].indexOf(',') + 1,
			arr[PROGRAMMSET_FARBE].lastIndexOf(','));
			b = arr[PROGRAMMSET_FARBE].substring(arr[PROGRAMMSET_FARBE].lastIndexOf(',') + 1);
			try {
				ret = new Color(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
			} catch (Exception ex) {
				Log.errorLog(669254033, ex);
			}
		}
		return ret;
	}

	public void setFarbe(Color farbe) {
		arr[PROGRAMMSET_FARBE]
		= farbe.getRed() + "," + farbe.getGreen() + "," + farbe.getBlue();
	}

	public boolean checkDownloadDirekt(String url) {
		//auf direkte prüfen, pref oder suf: wenn angegeben dann muss es stimmen
		if (!this.arr[PROGRAMMSET_PRAEFIX_DIREKT].equals("")
		|| !this.arr[PROGRAMMSET_SUFFIX_DIREKT].equals("")) {
			if (GuiFunktionenProgramme.praefixTesten(this.arr[PROGRAMMSET_PRAEFIX_DIREKT], url, true)
			&& GuiFunktionenProgramme.praefixTesten(this.arr[PROGRAMMSET_SUFFIX_DIREKT], url, false)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		String ret = "";
		ret += "================================================" + Daten.LINE_SEPARATOR;
		ret += "| Programmset" + Daten.LINE_SEPARATOR;
		for (int i = 0; i < MAX_ELEM; ++i) {
			ret += "| " + COLUMN_NAMES[i] + ": " + arr[i] + Daten.LINE_SEPARATOR;
		}
		for (Object aListeProg : listeProg) {
			ret += "|" + Daten.LINE_SEPARATOR;
			ret += aListeProg.toString();
		}
		ret += "|_______________________________________________" + Daten.LINE_SEPARATOR;
		return ret;
	}
	//===================================
	// Private
	//===================================

	private void makeArray() {
		arr = new String[MAX_ELEM];
		for (int i = 0; i < arr.length; ++i) {
			arr[i] = "";
		}
		arr[PROGRAMMSET_THEMA_ANLEGEN] = Boolean.toString(true);
		arr[PROGRAMMSET_IST_ABSPIELEN] = Boolean.toString(false);
		arr[PROGRAMMSET_IST_SPEICHERN] = Boolean.toString(false);
		arr[PROGRAMMSET_IST_BUTTON] = Boolean.toString(false);
		arr[PROGRAMMSET_IST_ABO] = Boolean.toString(false);
		arr[PROGRAMMSET_LAENGE_BESCHRAENKEN] = Boolean.toString(false);
		arr[PROGRAMMSET_LAENGE_FIELD_BESCHRAENKEN] = Boolean.toString(false);
		arr[PROGRAMMSET_INFODATEI] = Boolean.toString(false);
		arr[PROGRAMMSET_SUBTITLE] = Boolean.toString(false);
		arr[PROGRAMMSET_AUFLOESUNG] = DatenFilm.AUFLOESUNG_NORMAL;
	}
}
