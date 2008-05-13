package tetris.core;

import javax.microedition.lcdui.Canvas;
import java.io.*;

public class Settings extends Persistant {
	
	public int keys[]= {Canvas.KEY_NUM4, Canvas.KEY_NUM6, Canvas.KEY_NUM1, 
			Canvas.KEY_NUM3, Canvas.KEY_NUM5, Canvas.KEY_NUM8};
	public int fallingSpeed=4, transitionSpeed=2;
	public boolean syncBricks=true;

	public Settings() {
		super("Settings");
		load();
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

