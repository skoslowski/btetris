package tetris.tetris;

import javax.microedition.lcdui.Graphics;
import java.lang.Integer;
import java.util.Enumeration;

import tetris.core.TetrisMIDlet;
import tetris.ui.TetrisCanvas;

public class TetrisField {
	
	public static final int ROTATE_LEFT = 1, ROTATE_RIGHT = 2, LEFT = 3;
	public static final int RIGHT = 4, STEP = 5, DROP = 6;
	
	private Brick brick;
	public Brick nextBrick = null;  // for preview
	private TetrisMIDlet midlet;
	private Row rows[];
	public static final int COLS = 12, ROWS = 18 ;
	
	public TetrisField(TetrisMIDlet midlet) {
		this.midlet = midlet;		
		rows = new Row[ROWS];
		for (int y = 0;y < rows.length;y++) rows[y] = new Row();

		newBrick();
	}
	
	private void newBrick() {
		brick = (nextBrick != null)? nextBrick : Brick.getRandomBrick((TetrisField.COLS / 2) - 2);
		nextBrick = Brick.getRandomBrick((TetrisField.COLS / 2) - 2);
		
		if (brickCollisionCheck(brick)) midlet.endOfGame();
	}
	
	public synchronized void brickTransition(int type) {
		
		Brick temp = brick.clone();

		switch (type) {
		case ROTATE_LEFT:
			temp.rotate(true);
			break;
		case ROTATE_RIGHT:
			temp.rotate(false);
			break;
		case LEFT:
			temp.left();
			break;
		case RIGHT:
			temp.right();
			break;
		case STEP:
			temp.step();
			break;
		case DROP:
			while (!brickCollisionCheck(temp)) temp.step();
		}

		if (brickCollisionCheck(temp)) {
			if(type==STEP || type==DROP) {
				
				temp.stepback();
				addBrickToRows(temp);

				// has a row been completed?
				int count = rowCompleteCheck();
				if (count > 1) midlet.multiRowCompleted(count);

				// Create new Brick
				newBrick();		
	
			}
		} else {
			brick = temp;
		}
		temp = null;
	}
	
	
	public boolean brickCollisionCheck(Brick b) {
		int x,y;
		for (int i = 0;i < b.blocks.length; i++) {
			y = b.blocks[i].y;
			x = b.blocks[i].x;
			if (y >= ROWS || x < 0 || x >= COLS || rows[y].blocks[x] != null) return true;
		}
		return false;
	}
	
	/* add the brick to the rows-Objects*/
	private void addBrickToRows(Brick b) {
		for (int i = 0;i < b.blocks.length;i++)
			rows[b.blocks[i].y].blocks[b.blocks[i].x] = b.blocks[i];
	}

	private int rowCompleteCheck() {
		int count = 0;
		for(int y = 0; y < ROWS; y++) {
			if( rows[y].isComplete() ) {
				// move Rows down
				for (int i = y; i > 0; i--) {
					rows[i] = rows[i - 1];
					rows[i].update(i);
				}
				/* add new row on top */
				rows[0] = new Row();
				count++;
			}
		}
		//other player sent rows
		addRandomRows();
		
		midlet.score.addLines(count);
		return count;
	}
	
	public void addRandomRows() {
		if(midlet.rowsToAdd.size() > 0) {
			for(Enumeration counts = midlet.rowsToAdd.elements(); counts.hasMoreElements();) {
				int holePos = TetrisMIDlet.random(COLS);
				int count = ((Integer)counts.nextElement()).intValue();
				/* move other rows up */
				for (int y = 0; y < ROWS; y++) {
					if(y < ROWS - count) {
						rows[y] = rows[y + count];
					} else {
						rows[y] = Row.getIncompleteRow(holePos,y);
					}		
					rows[y].update(y);
				}
			}
			midlet.rowsToAdd.removeAllElements();
		}
	}

	public void paint(Graphics g, int blockSize) {
		/* draw small lines */
		int areaHeight = blockSize * TetrisField.ROWS;
		int areaWidth  = blockSize * TetrisField.COLS;
		g.setColor(TetrisCanvas.BORDER_COLOR);
		for (int i = 1;i < TetrisField.COLS;i++) g.drawLine(i*blockSize, 0,i*blockSize, areaHeight);
		for (int i = 1;i < TetrisField.ROWS;i++) g.drawLine(0, i*blockSize, areaWidth, i*blockSize);
		
    	/* draw rows*/
		for (int y = 0; y < ROWS; y++)
			rows[y].paint(g, blockSize);

		/* draw brick*/
    	brick.paint(g, blockSize,true);
		/* frame*/
		g.setColor(TetrisCanvas.FRAME_COLOR);
		g.drawRect(0, 0, areaWidth, areaHeight);
    }
	
}
