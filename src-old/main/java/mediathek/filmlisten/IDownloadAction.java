package mediathog.filmlisten;

import mSearch.daten.tList;

interface IDownloadAction {
	boolean performDownload(String dateiUrl, tList listeFilme, int days);
}
