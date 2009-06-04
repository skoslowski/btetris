package tetris.core;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;

public class Images {
	
	private static final int iconResolutions[] = {16,24,32,48};

	public static int iconResolution=16;
	
	public static void updateIconResolution(MIDlet midlet) {
		Display display = Display.getDisplay(midlet);
		int bestRes = Math.min(display.getBestImageHeight(Display.LIST_ELEMENT),
				display.getBestImageWidth(Display.LIST_ELEMENT));
		for(int i=0; i<iconResolutions.length; i++) {
			if(bestRes < iconResolutions[i]) break;
			iconResolution = iconResolutions[i];
		}
	}

	public static Image createImage(String filename) {
		Image image = null;
		try {
			image = Image.createImage("/" + iconResolution + filename);
		} catch (java.io.IOException ex) {
			return null;
		}
		return image;
	}	

}
