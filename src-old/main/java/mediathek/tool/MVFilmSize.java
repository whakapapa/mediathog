
package mediathek.tool;

import mSearch.tool.Log;

public class MVFilmSize implements Comparable<MVFilmSize> {

	private long aktSizeL = -1L;
	public Long sizeL = 0L;
	private String sizeStr = "";

	public MVFilmSize() {
	}

	@Override
	public int compareTo(MVFilmSize ll) {
		return (sizeL.compareTo(ll.sizeL));
	}

	@Override
	public String toString() {
		return sizeStr;
	}

	public void setSize(String size) {
		// im Film ist die Größe in "MB" !!
		if (size.isEmpty()) {
			aktSizeL = -1L;
			sizeL = 0L;
			sizeStr = "";
		} else {
			try {
				sizeL = Long.valueOf(size);
				sizeL = sizeL * 1000 * 1000;
				sizeStr = size;
			} catch (Exception ex) {
				Log.errorLog(978745320, ex, "String: " + size);
				sizeL = 0L;
				sizeStr = "";
			}
		}
	}

	public void reset() {
		aktSizeL = -1L;
		setString();
	}

	public void setSize(long l) {
		sizeL = l;
		setString();
	}

	public long getSize() {
		return sizeL;
	}

	public void setAktSize(long l) {
		aktSizeL = l;
		setString();
	}

	public void addAktSize(long l) {
		aktSizeL += l;
		setString();
	}

	public long getAktSize() {
		return aktSizeL;
	}

	public void setString() {
		if (aktSizeL <= 0) {
			if (sizeL > 0) {
				sizeStr = getGroesse(sizeL);
			} else {
				sizeStr = "";
			}
		} else if (sizeL > 0) {
			sizeStr = getGroesse(aktSizeL) + " von " + getGroesse(sizeL);
		} else {
			sizeStr = getGroesse(aktSizeL);
		}
	}

	public static String getGroesse(long l) {
		String ret = "";
		if (l > 1000 * 1000) {
			// größer als 1MB sonst kann ich mirs sparen
			ret = String.valueOf(l / (1000 * 1000));
		} else if (l > 0) {
			ret = "1";
		}
		return ret;
	}

	/**
	* Convert a byte count into a human readable string.
	*
	* @param bytes The number of bytes to convert.
	* @param si Use International System of Units (SI)?
	* @return The string representation
	*/
	public static String humanReadableByteCount(final long bytes, final boolean si) {
		final int unit = si ? 1000 : 1024;
		if (bytes < unit) {
			return bytes + " B";
		}

		final int exp = (int) (Math.log(bytes) / Math.log(unit));

		final String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

}
