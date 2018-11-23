/*
* MediathekView
* Copyright (C) 2008 W. Xaver
* W.Xaver[at]googlemail.com
* http://zdfmediathk.sourceforge.net/
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package mSearch.tool;

import com.jidesoft.utils.SystemInfo;
import mSearch.Const;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.CodeSource;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class Functions {

	private static final String RBVERSION = "version";
	private static final Logger logger = LogManager.getLogger(Functions.class);
	private static final Pattern PATTERN;

	static {
		PATTERN = Pattern.compile("\\<.*?>");
	}

	public static void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
		while (src.read(buffer) != -1) {
			buffer.flip();
			dest.write(buffer);
			buffer.compact();
		}

		buffer.flip();

		while (buffer.hasRemaining()) {
			dest.write(buffer);
		}
	}

	public static String textLaenge(int max, String text, boolean mitte, boolean addVorne) {
		if (text.length() > max) {
			if (mitte) {
				text = text.substring(0, 25) + " .... " + text.substring(text.length() - (max - 31));
			} else {
				text = text.substring(0, max - 1);
			}
		}
		StringBuilder textBuilder = new StringBuilder(text);
		while (textBuilder.length() < max) {
			if (addVorne) {
				textBuilder.insert(0, ' ');
			} else {
				textBuilder.append(' ');
			}
		}
		text = textBuilder.toString();
		return text;
	}


	public static String getPathJar() {
		// liefert den Pfad der Programmdatei mit File.separator am Schluss
		String pFilePath = "version.properties";
		File propFile = new File(pFilePath);
		if (!propFile.exists()) {
			try {
				CodeSource cS = Const.class.getProtectionDomain().getCodeSource();
				File jarFile = new File(cS.getLocation().toURI().getPath());
				String jarDir = jarFile.getParentFile().getPath();
				propFile = new File(jarDir + File.separator + pFilePath);
			} catch (Exception ignored) {
			}
		} else {
			logger.debug("getPathJar() propFile does exist");
		}
		String s = StringUtils.replace(propFile.getAbsolutePath(), pFilePath, "");
		if (!s.endsWith(File.separator)) {
			s = s + File.separator;
		}
		if (s.endsWith("/lib/")) {
			// dann sind wir in der msearch-lib
			s = StringUtils.replace(s, "/lib/", "");
		}
		return s;
	}



	public static boolean istUrl(String dateiUrl) {
		return dateiUrl.startsWith("http") || dateiUrl.startsWith("www");
	}

	public static String removeHtml(final String in) {
		return PATTERN.matcher(in).replaceAll("");
	}
}
