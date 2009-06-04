package tetris.tetris;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Graphics;

import tetris.core.RecordStoreHandler;

class Row  implements RecordStoreHandler.Persistant{	
	public Block blocks[] = new Block[TetrisField.COLS];

	public Row() {
		for(int i=0;i<blocks.length;i++) blocks[i]=null;
	}

	public static Row getIncompleteRow(int holePos, int ypos) {
		Row r = new Row();
		for (int x = 0; x < r.blocks.length; x++)
			r.blocks[x] = (x==holePos)? null : new Block(7, x, ypos);
		return r;
	}
	
	public void update(int y) {
		for (int x = 0; x < blocks.length; x++)
			if (blocks[x] != null) blocks[x].update(x, y);	
	}

	public boolean isEmpty() {
		for (int x = 0; x < blocks.length; x++)
			if (blocks[x] != null) return false;
		return true;	
	}
	
	public boolean isComplete() {
		for (int x = 0; x < blocks.length; x++)
			if (blocks[x] == null) return false;
		return true;
	}
	
    public void paint(Graphics g, int blockSize) {
		for (int x = 0; x < blocks.length; x++)
			if (blocks[x] != null) blocks[x].paint(g, blockSize);   		
    }
    

	public void readObject(DataInputStream stream) throws IOException {
		for(int i=0;i<blocks.length;i++) {
			boolean blockFollowing = stream.readBoolean();
			if(blockFollowing) {
				blocks[i] = new Block(0, 0, 0);
				blocks[i].readObject(stream);
			}
		}
	}

	public void writeObject(DataOutputStream stream) throws IOException {
		for(int i=0;i<blocks.length;i++) {
			boolean blockExists = (blocks[i] != null);
			stream.writeBoolean(blockExists);
			if(blockExists) blocks[i].writeObject(stream);
		}
	}
}
