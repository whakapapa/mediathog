
package mediathog.daten;

public class DatenMediaPath extends MVData<DatenMediaPath> {

	public final static int MEDIA_PATH_PATH = 0;
	public final static int MEDIA_PATHE_SAVE = 1;

	public final static String[] COLUMN_NAMES = {"Pfad", "Speichern"};
	public final static String[] XML_NAMES = COLUMN_NAMES;
	public static final String TAG = "MediaPath";
	public final static int MAX_ELEM = 2;
	public String[] arr;

	public DatenMediaPath(String pfad, boolean sichern) {
		makeArr();
		arr[MEDIA_PATH_PATH] = pfad;
		arr[MEDIA_PATHE_SAVE] = Boolean.toString(sichern);
	}

	public DatenMediaPath() {
		makeArr();
	}

	public boolean savePath() {
		return Boolean.parseBoolean(arr[MEDIA_PATHE_SAVE]);
	}

	//===================================
	// Private
	//===================================
	private void makeArr() {
		arr = new String[MAX_ELEM];
		for (int i = 0; i < arr.length; ++i) {
			arr[i] = "";
		}
	}

}
