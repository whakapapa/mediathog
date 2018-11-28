
package mediathek.daten;

import mSearch.tool.GermanStringSorter;

public class MVData<E> implements Comparable<E> {

	public static String TAG;
	public static String[] COLUMN_NAMES;
	public static String[] XML_NAMES;
	public static int MAX_ELEM;
	public String[] arr;

	public static GermanStringSorter sorter = GermanStringSorter.getInstance();

	public MVData() {
	}

	public String[] makeArr(int max) {
		String[] a = new String[max];
		for (int i = 0; i < max; ++i) {
			a[i] = "";
		}
		return a;
	}

	@Override
	public int compareTo(E o) {
		return 0;
	}

}
