package tetris.core;

import javax.microedition.lcdui.Canvas;
import java.io.*;

public class Settings extends Persistant {
	
	public int keys[]= {Canvas.KEY_NUM4, Canvas.KEY_NUM6, Canvas.KEY_NUM1, 
			Canvas.KEY_NUM3, Canvas.KEY_NUM5, Canvas.KEY_NUM8};
	public int fallingSpeed=4;

	public Settings() {
		super("KeySettings");
		load();
	}
	
	// Load Settings from Stream
	public void readObject(DataInputStream stream) throws IOException {
		for(int i = 0; i< keys.length; i++)
			keys[i] = stream.readInt();
		try { 
			fallingSpeed=stream.readInt(); 
		} catch(Exception e ) {
			fallingSpeed=4;
		}
	}
	
	// Save Setting to Stream	
	public void writeObject(DataOutputStream stream) throws IOException {		
		for(int i = 0; i< keys.length; i++)
			stream.writeInt(keys[i]);

		stream.writeInt(fallingSpeed);
	}
	
}

