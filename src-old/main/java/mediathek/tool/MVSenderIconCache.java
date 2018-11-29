package mediathog.tool;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

/**
* This class will load only one instance for all used sender icons.
*/
public class MVSenderIconCache {

	private final static Map<String, ImageIcon> iconCache = new HashMap<>();
	private final static Map<String, ImageIcon> iconCache_small = new HashMap<>();
	private final static String cStations = "/mediatheg/res/ico/stations/";
	final static int height = 32;
	final static int height_small = 15;

	static {
		iconCache.put("3Sat", scaleImage(cStations + "3sat.png", height));
		iconCache.put("ARD", scaleImage(cStations + "ard.png", height));
		iconCache.put("ARD.Podcast", scaleImage(cStations + "ard.png", height));
		iconCache.put("ARTE.DE", scaleImage(cStations + "arte-de.png", height));
		iconCache.put("ARTE.FR", scaleImage(cStations + "arte-fr.png", height));
		iconCache.put("BR", scaleImage(cStations + "br.png", height));
		iconCache.put("HR", scaleImage(cStations + "hr.png", height));
		iconCache.put("KiKA", scaleImage(cStations + "kika.png", height));
		iconCache.put("MDR", scaleImage(cStations + "mdr.png", height));
		iconCache.put("DW", scaleImage(cStations + "dw.png", height));
		iconCache.put("NDR", scaleImage(cStations + "ndr.png", height));
		iconCache.put("ORF", scaleImage(cStations + "orf.png", height));
		iconCache.put("RBB", scaleImage(cStations + "rbb.png", height));
		iconCache.put("SR", scaleImage(cStations + "sr.png", height));
		iconCache.put("SRF", scaleImage(cStations + "srf.png", height));
		iconCache.put("SRF.Podcast", scaleImage(cStations + "srf-podcast.png", height));
		iconCache.put("SWR", scaleImage(cStations + "swr.png", height));
		iconCache.put("WDR", scaleImage(cStations + "wdr.png", height));
		iconCache.put("ZDF", scaleImage(cStations + "zdf.png", height));
		iconCache.put("ZDF-tivi", scaleImage(cStations + "zdf-tivi.png", height));
		iconCache.put("PHOENIX", scaleImage(cStations + "phoenix.png", height));
	}

	static {
		iconCache_small.put("3Sat", scaleImage(cStations + "3sat.png", height_small));
		iconCache_small.put("ARD", scaleImage(cStations + "ard.png", height_small));
		iconCache_small.put("ARD.Podcast", scaleImage(cStations + "ard.png", height_small));
		iconCache_small.put("ARTE.DE", scaleImage(cStations + "arte-de.png", height_small));
		iconCache_small.put("ARTE.FR", scaleImage(cStations + "arte-fr.png", height_small));
		iconCache_small.put("BR", scaleImage(cStations + "br.png", height_small));
		iconCache_small.put("HR", scaleImage(cStations + "hr.png", height_small));
		iconCache_small.put("KiKA", scaleImage(cStations + "kika.png", height_small));
		iconCache_small.put("MDR", scaleImage(cStations + "mdr.png", height_small));
		iconCache_small.put("DW", scaleImage(cStations + "dw.png", height_small));
		iconCache_small.put("NDR", scaleImage(cStations + "ndr.png", height_small));
		iconCache_small.put("ORF", scaleImage(cStations + "orf.png", height_small));
		iconCache_small.put("RBB", scaleImage(cStations + "rbb.png", height_small));
		iconCache_small.put("SR", scaleImage(cStations + "sr.png", height_small));
		iconCache_small.put("SRF", scaleImage(cStations + "srf.png", height_small));
		iconCache_small.put("SRF.Podcast", scaleImage(cStations + "srf-podcast.png", height_small));
		iconCache_small.put("SWR", scaleImage(cStations + "swr.png", height_small));
		iconCache_small.put("WDR", scaleImage(cStations + "wdr.png", height_small));
		iconCache_small.put("ZDF", scaleImage(cStations + "zdf.png", height_small));
		iconCache_small.put("ZDF-tivi", scaleImage(cStations + "zdf-tivi.png", height_small));
		iconCache_small.put("PHOENIX", scaleImage(cStations + "phoenix.png", height_small));
	}

	/**
	* Get the icon for a specific sender.
	*
	* @param sender The name of the supported sender.
	* @param small
	* @return The {@link javax.swing.ImageIcon} for the sender or null.
	*/
	public ImageIcon get(String sender, boolean small) {
		if (small) {
			return iconCache_small.get(sender);
		} else {
			return iconCache.get(sender);
		}
	}

	private static ImageIcon scaleImage(String source, int maxHeight) {

		int newWidth, priorHeight, priorWidth; // Variables for the old - new height and width
		Image image;
		ImageIcon sizeImage;

		image = new ImageIcon(MVSenderIconCache.class.getResource(source)).getImage();
		sizeImage = new ImageIcon(image);

		priorHeight = sizeImage.getIconHeight();
		priorWidth = sizeImage.getIconWidth();

		newWidth = (int) (((float) priorWidth / (float) priorHeight) * (float) maxHeight);

		return new ImageIcon(image.getScaledInstance(newWidth, maxHeight, Image.SCALE_AREA_AVERAGING));
	}

}
