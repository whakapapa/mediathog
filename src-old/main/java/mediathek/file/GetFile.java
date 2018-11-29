
package mediathog.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import mSearch.tool.Log;

/**
*
* @author emil
*/
public class GetFile {

	//TODO consolidate or eliminate paths
	public static final String PFAD_HILFETEXT_GEO = "/mediathek/file/geo.txt";
	public static final String PFAD_HILFETEXT_FILTER = "/mediathek/file/filter.txt";
	public static final String PFAD_HILFETEXT_BLACKLIST = "/mediathek/file/blacklist.txt";
	public static final String PFAD_HILFETEXT_BEENDEN = "/mediathek/file/beenden.txt";
	public static final String PFAD_HILFETEXT_PRGRAMME = "/mediathek/file/pset.txt";
	public static final String PFAD_HILFETEXT_STANDARD_PSET = "pset-standard.txt";
	public static final String PFAD_HILFETEXT_EDIT_DOWNLOAD_PROG = "edit-download.txt";
	public static final String PFAD_HILFETEXT_UNICODE = "unicode.txt";
	public static final String PFAD_HILFETEXT_RESET = "reset.txt";
	public static final String PFAD_HILFETEXT_RESET_SET = "pset-reset.txt";
	public static final String PFAD_HILFETEXT_DIALOG_MEDIA_DB = "dialog-media-db.txt";
	public static final String PFAD_HILFETEXT_PANEL_MEDIA_DB = "panel-media-db.txt";
	public static final String PFAD_HILFETEXT_DIALOG_ADD_ABO = "dialog-add-subscr.txt";

	public String getHilfeSuchen(String pfad) {
		String ret = "";
		try (InputStreamReader in = new InputStreamReader(getClass().getResource(pfad).openStream(), StandardCharsets.UTF_8);
		BufferedReader br = new BufferedReader(in)) {
			String strLine;
			while ((strLine = br.readLine()) != null) {
				ret = ret + '\n' + strLine;
			}
		} catch (IOException ex) {
			Log.errorLog(885692213, ex);
		}
		return ret;
	}

	public InputStreamReader getPsets() {
		try {
			return new InputStreamReader(getClass().getResource(setPsets).openStream(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			Log.errorLog(469691002, ex);
		}
		return null;
	}
}
