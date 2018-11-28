
package mediathek.tool;

import javax.swing.JTable;
import mSearch.tool.Listener;
import mediathek.config.MVConfig;

public class MVFont {

	public static int fontSize = 12;
	private static int fontNormal = 12;

	public static void initFont() {
		int f;
		fontNormal = new JTable().getFont().getSize();
		try {
			f = Integer.parseInt(MVConfig.get(MVConfig.Configs.SYSTEM_FONT_SIZE));
		} catch (Exception ignore) {
			f = 0;
			MVConfig.add(MVConfig.Configs.SYSTEM_FONT_SIZE, "0");
		}
		fontSize = getFontSize(f);
	}

	private static int getFontSize(int size) {
		size = fontNormal + 4 * size;
		if (size < 6) {
			size = 6;
		}
		return size;
	}

	public static void resetFontSize() {
		MVConfig.add(MVConfig.Configs.SYSTEM_FONT_SIZE, "0");
		MVFont.fontSize = getFontSize(0);
		Listener.notify(Listener.EREIGNIS_FONT, GuiFunktionen.class.getSimpleName());
	}

	public static void setFontSize(boolean up) {
		int size;
		try {
			size = Integer.parseInt(MVConfig.get(MVConfig.Configs.SYSTEM_FONT_SIZE));
		} catch (Exception ex) {
			size = 0;
		}
		if (up && size < 10) {
			++size;
		} else if (!up && size > -5) {
			--size;
			if (MVFont.fontSize == getFontSize(size)) {
				// dann gehts nicht mehr kleiner
				++size;
			}
		}
		MVConfig.add(MVConfig.Configs.SYSTEM_FONT_SIZE, String.valueOf(size));
		MVFont.fontSize = getFontSize(size);
		Listener.notify(Listener.EREIGNIS_FONT, GuiFunktionen.class.getSimpleName());
	}

}
