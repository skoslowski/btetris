package tetris.tetris;

import javax.microedition.lcdui.Graphics;


public class Row {
	public Block blocks[] = new Block[TetrisField.COLS];

	public Row() {
		for(int i=0;i<blocks.length;i++) blocks[i]=null;
	}

	public static Row getIncompleteRow(int holePos, int ypos) {
		Row r = new Row();
		for (int x = 0; x < r.blocks.length; x++)
			r.blocks[x] = (x==holePos)? null : new Block(0x999999, x, ypos);
		return r;
	}
	
	public void update(int y) {
		for (int x = 0; x < blocks.length; x++)
			if (blocks[x] != null) blocks[x].update(x, y);	
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
}
