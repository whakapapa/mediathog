package base

// keep all the constants used globally

// program name and version
const setProg = "Mediatheg"
const setVers = "0.7"


public static final String VERSION = "13";



// name for Msearch
//TODO check if this is needed
const setSearch = "MSearch"


// shutdown string linux
const setShutLinux = "shutdown -h now"


// load films automatically
const setFilmsAuto = true

//TODO old cruft remove both update keys with one bool
/*
public static final int UPDATE_FILME_AUS = 0; // nur manuell + URL manuell wählen
public static final int UPDATE_FILME_AUTO = 2; // beim Start automatisch + manuell, Url automatisch wählen
*/


//TODO check if this is necessary
// standard field length
const setFieldLength = 10


//TODO check if this is necessary
// standard file name length
const setFNameLength = 25


//TODO check if this is necessary
// max number path for download
const setMaxDownload = 15


//TODO check if this is necessary
//
const setDividerGUI = "200"


//TODO check if this is necessary
//
const setMinSize = 256 * 1024


//TODO check if this is necessary
// max no. downloads per station
const setMaxDLperStation = 2


//TODO check if this is necessary
// compression formats
const setCzip	= ".zip"
const setCxz	= ".xz"


// auto update time delay film list (10,000 secs approx. 3 hours)
const setAUdelay = 10000


// illegal chars for files and paths
const illChar = "[:\\\\/*|<>]"
const illPath = "[:\\\\*|<>]"
