
package mediathek.res;

import java.awt.Image;
import java.io.File;
import javax.swing.ImageIcon;
import mSearch.tool.Log;
import mediathek.config.MVConfig;
import mediathek.tool.GuiFunktionen;

public class GetIcon {


	public static ImageIcon getIcon(String strIcon) {
		return getIcon(strIcon, setRes, 0, 0);
	}

	public static ImageIcon getSenderIcon(String strIcon) {
		return getIcon(strIcon, setStations, 0, 0);
	}

	public static ImageIcon getProgramIcon(String strIcon, int w, int h) {
		return getIcon(strIcon, killDirProg, w, h);
	}

	public static ImageIcon getProgramIcon(String strIcon) {
		return getIcon(strIcon, killDirProg, 0, 0);
	}

	public static ImageIcon getIcon(String strIcon, String path, int w, int h) {
		ImageIcon icon;
		if (Boolean.parseBoolean(MVConfig.get(MVConfig.Configs.SYSTEM_ICON_STANDARD))) {
			icon = getStandard(strIcon, path);
		} else {
			try {
				String pfad = GuiFunktionen.addsPfad(MVConfig.get(MVConfig.Configs.SYSTEM_ICON_PFAD), strIcon);
				if (new File(pfad).exists()) {
					icon = new ImageIcon(pfad);
				} else {
					icon = getStandard(strIcon, path);
				}
			} catch (Exception ex) {
				Log.errorLog(932107891, strIcon);
				icon = getStandard(strIcon, path);
			}
		}
		if (w > 0 && h > 0) {
			if (icon.getIconWidth() != w || icon.getIconHeight() != h) {
				// nur dann macht es Sinn
				icon.setImage(icon.getImage().getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING));
			}
		}
		return icon;
	}

	public static ImageIcon getIcon(String strIcon, String path) {
		return getIcon(strIcon, path, 0, 0);
	}

	private static ImageIcon getStandard(String strIcon, String path) {
		return new ImageIcon(GetIcon.class.getResource(path + strIcon));
	}
}
