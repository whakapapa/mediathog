
package mediathog.tool;

import java.awt.Color;
import mediathog.config.MVConfig;

public class MVC {

	public MVConfig.Configs configs = null;
	public String text = "";
	public Color color = new Color(0);
	public Color colorReset = new Color(0);

	public MVC(MVConfig.Configs configs, Color ccolor, String ttext) {
		this.configs = configs;
		text = ttext;
		color = ccolor;
		colorReset = ccolor;
	}

	public void set(Color c) {
		color = c;
	}

	public void reset() {
		color = colorReset;
	}
}
