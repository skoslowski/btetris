package tetris.settings;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;

import tetris.core.RecordStoreHandler;
import tetris.core.TetrisMIDlet;

import java.io.*;

public class Settings implements RecordStoreHandler.Persistant {
	
	private static Settings instance = null;
	
	public static Settings getInstance() {
		if(instance == null) instance = new Settings();
		return instance;
	}	
	public static void showSettingsMenu(TetrisMIDlet midlet) {
		Display d = Display.getDisplay(midlet);
		d.setCurrent(new SettingsMenu(midlet));
	}
	public static void showSettingsKeysMenu(TetrisMIDlet midlet) {
		Display d = Display.getDisplay(midlet);
		d.setCurrent(new SettingsKeysMenu(midlet));
	}
	public static void showSettingsOtherMenu(TetrisMIDlet midlet){
		Display d = Display.getDisplay(midlet);
		d.setCurrent(new SettingsOtherMenu(midlet));
	}
	
	/* -------------------------------------------------------------------- */
	
	public int keys[]= {Canvas.KEY_NUM4, Canvas.KEY_NUM6, Canvas.KEY_NUM1, 
			Canvas.KEY_NUM3, Canvas.KEY_NUM5, Canvas.KEY_NUM8};
	public int fallingSpeed=4, transitionSpeed=2;
	public boolean syncBricks=true;
	
	private RecordStoreHandler rsHandler;

	private Settings() {
		rsHandler = new RecordStoreHandler();
		rsHandler.load(this, "Settings");
	}
	
	public void save() {
		rsHandler.save(this, "Settings");
	}

	
	// Load Settings from Stream
	public void readObject(DataInputStream stream) throws IOException {
		for(int i = 0; i< keys.length; i++)
			keys[i] = stream.readInt();
		
		try { 
			fallingSpeed=stream.readInt(); 
		} catch(Exception e ) {
			/* Backward compatibility*/
			fallingSpeed=4;
		}
		
		try { 
			syncBricks=stream.readBoolean(); 
		} catch(Exception e ) {
			/* Backward compatibility*/
			syncBricks=true;
		}
				
		try { 
			transitionSpeed=stream.readInt(); 
		} catch(Exception e ) {
			/* Backward compatibility*/
			transitionSpeed=2;
		}
	}
	
	// Save Setting to Stream	
	public void writeObject(DataOutputStream stream) throws IOException {		
		for(int i = 0; i< keys.length; i++)
			stream.writeInt(keys[i]);
		stream.writeInt(fallingSpeed);
		stream.writeBoolean(syncBricks);
		stream.writeInt(transitionSpeed);
	}
	
}

