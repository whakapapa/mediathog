
package mediathog.daten;

import mSearch.tool.Log;
import org.jetbrains.annotations.NotNull;

public class DatenAbo extends MVData<DatenAbo> {
	//Tags Abo

	public static final int ABO_NR = 0;
	public static final int ABO_EINGESCHALTET = 1;
	public static final int ABO_NAME = 2;
	public static final int ABO_SENDER = 3;
	public static final int ABO_THEMA = 4;
	public static final int ABO_TITEL = 5;
	public static final int ABO_THEMA_TITEL = 6;
	public static final int ABO_IRGENDWO = 7;
	public static final int ABO_MINDESTDAUER = 8;
	public static final int ABO_MIN = 9;
	public static final int ABO_ZIELPFAD = 10;
	public static final int ABO_DOWN_DATUM = 11;
	public static final int ABO_PSET = 12;
	public static final String[] COLUMN_NAMES = {"Nr", "aktiv", "Name",
	"Sender", "Thema", "Titel", "Thema-Titel",
	"Irgendwo", "Dauer", "min/max", "Zielpfad", "letztes Abo", "Programmset"};
	public static final String[] XML_NAMES = {"Nr", "aktiv", "Name",
	"Sender", "Thema", "Titel", "Thema-Titel",
	"Irgendwo", "Mindestdauer", "min_max", "Zielpfad", "letztes_Abo", "Programmset"};

	public static final int MAX_ELEM = 13;
	public static final String TAG = "Abonnement";
	public static boolean[] spaltenAnzeigen = new boolean[MAX_ELEM];
	public int mindestdauerMinuten;
	public boolean min = true;
	public String[] arr;
	public int nr;
	String[] titel, thema, irgendwo;

	public DatenAbo() {
		makeArr();
	}

	public DatenAbo(String name, String sender, String thema, String titel, String themaTitel, String irgendwo, int mmindestdauerMinuten, boolean min, String ziel, String pset) {
		makeArr();
		arr[ABO_NAME] = name;
		arr[ABO_SENDER] = sender;
		arr[ABO_THEMA] = thema;
		arr[ABO_TITEL] = titel;
		arr[ABO_THEMA_TITEL] = themaTitel;
		arr[ABO_IRGENDWO] = irgendwo;
		setMindestDauerMinuten(mmindestdauerMinuten);
		arr[ABO_MIN] = Boolean.toString(min);
		this.min = min;
		arr[ABO_ZIELPFAD] = ziel;
		arr[ABO_PSET] = pset;
	}

	public DatenAbo getCopy() {
		DatenAbo ret = new DatenAbo();
		System.arraycopy(this.arr, 0, ret.arr, 0, arr.length);
		ret.mindestdauerMinuten = this.mindestdauerMinuten;
		ret.min = this.min;
		return ret;
	}

	public boolean isEmpty() {
		//liefert TRUE wenn das Abo leer ist, also bei jedem Film ansprechen würde
		//ist dann offensichtlich falsch!!
		if (arr[ABO_SENDER].isEmpty()
		&& arr[ABO_THEMA].isEmpty()
		&& arr[ABO_TITEL].isEmpty()
		&& arr[ABO_THEMA_TITEL].isEmpty()
		&& arr[ABO_IRGENDWO].isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public final void setMindestDauerMinuten(int d) {
		mindestdauerMinuten = d;
		arr[ABO_MINDESTDAUER] = String.valueOf(d);
	}

	public void setMindestDauerMinuten() {
		if (this.arr[DatenAbo.ABO_MINDESTDAUER].isEmpty()) {
			// für den ProgUpdate
			mindestdauerMinuten = 0;
			arr[ABO_MINDESTDAUER] = "0";
		}
		try {
			mindestdauerMinuten = Integer.parseInt(this.arr[DatenAbo.ABO_MINDESTDAUER]);
		} catch (Exception ex) {
			Log.errorLog(462558700, ex);
			mindestdauerMinuten = 0;
			arr[ABO_MINDESTDAUER] = "0";
		}
	}

	public boolean aboIstEingeschaltet() {
		if (arr[DatenAbo.ABO_EINGESCHALTET].isEmpty()) {
			aboEin();
			return true;
		}
		return Boolean.parseBoolean(arr[DatenAbo.ABO_EINGESCHALTET]);
	}

	public static boolean anzeigen(int i) {
		return spaltenAnzeigen == null || spaltenAnzeigen[i];
	}

	private void aboEin() {
		arr[DatenAbo.ABO_EINGESCHALTET] = String.valueOf(true);
	}

	private void makeArr() {
		arr = new String[MAX_ELEM];
		for (int i = 0; i < arr.length; ++i) {
			arr[i] = "";
		}
		// neue Abos sind immer ein
		aboEin();
	}

	@Override
	public int compareTo(@NotNull DatenAbo arg0) {
		return sorter.compare(arr[ABO_NAME], arg0.arr[ABO_NAME]);
	}
}
