
package mediathog.daten;

import mSearch.tool.Log;
import mSearch.tool.MVHttpClient;
import mediathog.config.Daten;
import mediathog.config.Konstanten;
import mediathog.file.GetFile;
import mediathog.tool.GuiFunktionen;
import mediathog.tool.TModel;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.swing.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class ListePsetVorlagen extends LinkedList<String[]> {
	public static final String PGR = "Vorlage";
	public static final String PGR_NAME = "Name";
	public static final int PGR_NAME_NR = 0;
	public static final String PGR_BESCHREIBUNG = "Beschreibung";
	public static final int PGR_BESCHREIBUNG_NR = 1;
	public static final String PGR_VERSION = "Version";
	public static final int PGR_VERSION_NR = 2;
	public static final String PGR_BS = "Bs";
	public static final int PGR_BS_NR = 3;
	public static final String PGR_URL = "URL";
	public static final int PGR_URL_NR = 4;
	public static final String PGR_INFO = "Info";
	public static final int PGR_INFO_NR = 5;
	public static final int PGR_MAX_ELEM = 6;
	public static final String[] PGR_COLUMN_NAMES = {PGR_NAME, PGR_BESCHREIBUNG, PGR_VERSION, PGR_BS, PGR_URL, PGR_INFO};
	private final static int TIMEOUT = 10000;

	public TModel getTModel(String bs) {
		LinkedList<String[]> tmp = new LinkedList<>();
		String[][] object;
		if (this.size() > 0) {
			if (!bs.isEmpty()) {
				tmp.addAll(this.stream().filter(aThi -> aThi[PGR_BS_NR].contains(bs)).collect(Collectors.toList()));
				object = new String[tmp.size()][PGR_MAX_ELEM];
				for (int i = 0; i < tmp.size(); i++) {
					object[i] = tmp.get(i);
				}
			} else {
				object = new String[this.size()][PGR_MAX_ELEM];
				for (int i = 0; i < this.size(); i++) {
					object[i] = this.get(i);
				}
			}
			return new TModel(object, PGR_COLUMN_NAMES);
		} else {
			return new TModel(new Object[][]{}, PGR_COLUMN_NAMES);
		}
	}

	public static ListePset getStandarset(JFrame parent, Daten ddaten, boolean replaceMuster) {
		ListePset listePset = null;
		String[] vorlage = null;
		ListePsetVorlagen listePsetVorlagen = new ListePsetVorlagen();
		if (listePsetVorlagen.loadListOfSets()) {
			for (String[] ar : listePsetVorlagen) {
				vorlage = ar;

			}
			if (!vorlage[PGR_URL_NR].isEmpty()) {
				listePset = ListePsetVorlagen.importPsetFile(parent, vorlage[ListePsetVorlagen.PGR_URL_NR], true);
				if (listePset != null) {
					listePset.version = vorlage[PGR_VERSION_NR];
				}
			}

		}
		if (listePset == null) {
			// dann nehmen wir halt die im jar-File
			// liefert das Standard Programmset für das entsprechende
			InputStreamReader inReader;
			inReader = new GetFile().getPsets();

			// Standardgruppen laden
			listePset = ListePsetVorlagen.importPset(inReader, true);
		}

		if (replaceMuster && listePset != null) {
			// damit die Variablen ersetzt werden
			ListePset.progMusterErsetzen(parent, listePset);
		}
		return listePset;
	}

	public boolean loadListOfSets() {
		try {
			this.clear();

			XMLStreamReader parser = null;
			XMLInputFactory inFactory = XMLInputFactory.newInstance();
			inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);

			final Request request = new Request.Builder().url(killTemplate).get().build();
			try (Response response = MVHttpClient.getInstance().getReducedTimeOutClient().newCall(request).execute();
			ResponseBody body = response.body()) {
				if (response.isSuccessful() && body != null) {
					try (InputStream is = body.byteStream();
					InputStreamReader inReader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
						parser = inFactory.createXMLStreamReader(inReader);
						while (parser.hasNext()) {
							final int event = parser.next();
							if (event == XMLStreamConstants.START_ELEMENT) {
								if (parser.getLocalName().equals(PGR)) {
									//wieder ein neuer Server, toll
									String[] p = new String[PGR_MAX_ELEM];
									get(parser, PGR, PGR_COLUMN_NAMES, p);
									if (!p[PGR_URL_NR].isEmpty()) {
										this.add(p);
									}
								}
							}
						}
					} finally {
						if (parser != null)
						parser.close();
					}
				} else //unsuccessful...
				return false;
			}
		} catch (UnknownHostException | ConnectException ignored) {
			return false;
		} catch (Exception ex) {
			Log.errorLog(398001963, ex);
			return false;
		}

		return true;
	}

	public static ListePset importPsetFile(JFrame parent, String dateiUrl, boolean log) {
		try {
			ListePset result = null;

			if (GuiFunktionen.istUrl(dateiUrl)) {
				final Request request = new Request.Builder().url(dateiUrl).get().build();
				try (Response response = MVHttpClient.getInstance().getReducedTimeOutClient().newCall(request).execute();
				ResponseBody body = response.body()) {
					if (response.isSuccessful() && body != null) {
						try (InputStream is = body.byteStream();
						InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
							result = ListePsetVorlagen.importPset(isr, log);
						}
					}
				}
			} else {
				try (FileInputStream fis = new FileInputStream(dateiUrl);
				InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
					result = ListePsetVorlagen.importPset(isr, log);
				}
			}

			return result;
		} catch (Exception ex) {
			if (log) {
				Log.errorLog(630048926, ex);
			}
			return null;
		}
	}

	public static ListePset importPsetText(Daten dd, String text, boolean log) {
		ListePset result = null;

		try (ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes());
		InputStreamReader ir = new InputStreamReader(bais)) {
			result = ListePsetVorlagen.importPset(ir, log);
		} catch (IOException ignored) {
		}

		return result;
	}

	private static ListePset importPset(InputStreamReader in, boolean log) {
		DatenPset datenPset = null;
		ListePset liste = new ListePset();
		try {
			XMLInputFactory inFactory = XMLInputFactory.newInstance();
			inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
			XMLStreamReader parser;
			parser = inFactory.createXMLStreamReader(in);
			while (parser.hasNext()) {
				final int event = parser.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					switch (parser.getLocalName()) {
						case DatenPset.TAG:
						datenPset = new DatenPset();
						if (!get(parser, DatenPset.TAG, DatenPset.XML_NAMES, datenPset.arr)) {
							datenPset = null;
						} else {
							if (!datenPset.isEmpty()) {
								//kann beim Einlesen der Konfigdatei vorkommen
								liste.add(datenPset);
							}
						}
						break;
						case DatenProg.TAG:
						if (datenPset != null) {
							DatenProg datenProg = new DatenProg();
							if (get(parser, DatenProg.TAG, DatenProg.XML_NAMES, datenProg.arr)) {
								datenPset.addProg(datenProg);
							}
						}
						break;
					}
				}
			}
		} catch (Exception ex) {
			if (log) {
				Log.errorLog(467810360, ex);
			}
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignored) {
				}
			}
		}
		if (liste.isEmpty()) {
			return null;
		} else {
			return liste;
		}
	}

	private static boolean get(XMLStreamReader parser, String xmlElem, String[] xmlNames, String[] strRet) {
		boolean ret = true;
		int maxElem = strRet.length;
		for (int i = 0; i < maxElem; ++i) {
			strRet[i] = "";
		}
		try {
			while (parser.hasNext()) {
				final int event = parser.next();
				if (event == XMLStreamConstants.END_ELEMENT) {
					if (parser.getLocalName().equals(xmlElem)) {
						break;
					}
				}
				if (event == XMLStreamConstants.START_ELEMENT) {
					for (int i = 0; i < maxElem; ++i) {
						if (parser.getLocalName().equals(xmlNames[i])) {
							strRet[i] = parser.getElementText();
							break;
						}
					}
				}
			}
		} catch (Exception ex) {
			ret = false;
			Log.errorLog(467256394, ex);
		}
		return ret;
	}

}
