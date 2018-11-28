
package mSearch.tool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class MSStringBuilder {

	private final StringBuilder cont;

	public MSStringBuilder() {
		cont = new StringBuilder();
	}

	public MSStringBuilder(int capacity) {
		cont = new StringBuilder(capacity);
	}

	//=====================================================
	//StringBuilder Kram
	public String substring(int start) {
		return cont.substring(start);
	}

	public int lastIndexOf(String of) {
		return cont.lastIndexOf(of);
	}

	public int length() {
		return cont.length();
	}

	public String substring(int start, int end) {
		return cont.substring(start, end);
	}

	public synchronized void append(char[] str) {
		cont.append(str);
	}

	public synchronized void append(char[] str, int offset, int len) {
		cont.append(str, offset, len);
	}

	public synchronized void setLength(int newLength) {
		cont.setLength(newLength);
	}

	public synchronized int indexOf(String str, int fromIndex) {
		return cont.indexOf(str, fromIndex);
	}

	public synchronized int indexOf(String str) {
		return cont.indexOf(str);
	}

	//=====================================================
	//=====================================================
	//=====================================================
	public String extract(String musterStart, String musterEnde) {
		return extract(musterStart, "", musterEnde, 0, 0, "");
	}

	public String extract(String musterStart, String musterEnde, int abPos) {
		return extract(musterStart, "", musterEnde, abPos, 0, "");
	}

	public String extract(String musterStart, String musterEnde, int abPos, int bisPos) {
		return extract(musterStart, "", musterEnde, abPos, bisPos, "");
	}

	public String extract(String musterStart1, String musterStart2, String musterEnde) {
		return extract(musterStart1, musterStart2, musterEnde, 0, 0, "");
	}

	public String extract(String musterStart1, String musterStart2, String musterEnde, String addUrl) {
		return extract(musterStart1, musterStart2, musterEnde, 0, 0, addUrl);
	}

	public String extract(String musterStart1, String musterStart2, String musterEnde, int abPos, int bisPos, String addUrl) {
		int pos1, pos2;
		if ((pos1 = cont.indexOf(musterStart1, abPos)) == -1) {
			return "";
		}
		pos1 += musterStart1.length();
		if (!musterStart2.isEmpty() && (pos1 = cont.indexOf(musterStart2, pos1)) == -1) {
			return "";
		}
		pos1 += musterStart2.length();
		if ((pos2 = cont.indexOf(musterEnde, pos1)) == -1) {
			return "";
		}
		if (bisPos > 0 && pos2 > bisPos) {
			return "";
		}
		String ret = cont.substring(pos1, pos2);
		if (!ret.isEmpty()) {
			// damit nicht nur! addUrl zurückkommt
			return addUrl + ret;
		}
		return "";
	}

	public void extractList(String musterStart, String musterEnde, ArrayList<String> result) {
		extractList("", "", musterStart, "", musterEnde, "", result);
	}

	public void extractList(String musterStart1, String musterStart2, String musterEnde, ArrayList<String> result) {
		extractList("", "", musterStart1, musterStart2, musterEnde, "", result);
	}

	public void extractList(String abMuster, String bisMuster, String musterStart, String musterEnde, String addUrl, ArrayList<String> result) {
		extractList(abMuster, bisMuster, musterStart, "", musterEnde, addUrl, result);
	}

	private static final Logger logger = LogManager.getLogger(MSStringBuilder.class);

	public void extractList(String abMuster, String bisMuster, String musterStart1, String musterStart2, String musterEnde, String addUrl, ArrayList<String> result) {
		int pos1, pos2, stopPos, count = 0;
		String str;
		pos1 = abMuster.isEmpty() ? 0 : cont.indexOf(abMuster);
		if (pos1 == -1) {
			return;
		}

		stopPos = bisMuster.isEmpty() ? -1 : cont.indexOf(bisMuster, pos1);

		while ((pos1 = cont.indexOf(musterStart1, pos1)) != -1) {
			++count;
			if (count > 10_000) {
				logger.debug("count > 10_000");
				break;
			}
			pos1 += musterStart1.length();

			if (!musterStart2.isEmpty()) {
				if ((pos2 = cont.indexOf(musterStart2, pos1)) == -1) {
					continue;
				}
				pos1 = pos2 + musterStart2.length();
			}

			if ((pos2 = cont.indexOf(musterEnde, pos1)) == -1) {
				continue;
			}
			if (stopPos > 0 && pos2 > stopPos) {
				continue;
			}

			if ((str = cont.substring(pos1, pos2)).isEmpty()) {
				continue;
			}

			str = addUrl + str;
			addStr(str, result);
		}
	}

	private void addStr(String str, ArrayList<String> result) {
		if (!result.contains(str)) {
			result.add(str);
			if (result.size() > 1000) {
				logger.debug("result.size() > 1000");
			}
		}
	}

	public void extractList(int ab, int bis, String musterStart1, String musterStart2, String musterEnde, String addUrl, ArrayList<String> result) {
		int pos1, pos2, stopPos, count = 0;
		String str;
		pos1 = ab;
		stopPos = bis;
		if (pos1 == -1) {
			return;
		}

		while ((pos1 = cont.indexOf(musterStart1, pos1)) != -1) {
			++count;
			if (count > 10_000) {
				logger.debug("count > 10_000");
				break;
			}
			pos1 += musterStart1.length();

			if (!musterStart2.isEmpty()) {
				if ((pos2 = cont.indexOf(musterStart2, pos1)) == -1) {
					continue;
				}
				pos1 = pos2 + musterStart2.length();
			}

			if ((pos2 = cont.indexOf(musterEnde, pos1)) == -1) {
				continue;
			}
			if (stopPos > 0 && pos2 > stopPos) {
				continue;
			}

			if ((str = cont.substring(pos1, pos2)).isEmpty()) {
				continue;
			}

			str = addUrl + str;
			if (!result.contains(str)) {
				result.add(str);
				if (result.size() > 1000) {
					logger.debug("result.size() > 1000");
				}
			}

		}
	}

}
