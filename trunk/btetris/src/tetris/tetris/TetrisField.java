package tetris.tetris;

import javax.microedition.lcdui.Graphics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Integer;
import java.util.Enumeration;
import java.util.Vector;

import tetris.core.RecordStoreHandler;
import tetris.core.TetrisMIDlet;

class TetrisField implements RecordStoreHandler.Persistant {

	public static final int ROTATE_LEFT = 1, ROTATE_RIGHT = 2, LEFT = 3;
	public static final int RIGHT = 4, SOFTDROP = 5, HARDDROP = 6, STEP=7;
	public static final int COLS = 10, ROWS = 20;

	private Brick brick, nextBrick = null;  // for preview
	private TetrisMIDlet midlet;
	private Row rows[];
	private Vector rowsToAdd; // send by peer

	public TetrisField(TetrisMIDlet midlet) {		
		this.midlet = midlet;	
		rowsToAdd = new Vector();
	
		rows = new Row[ROWS+1];
		for (int y = 0;y < rows.length;y++) rows[y] = new Row();

		newBrick();
	}

	private void newBrick() {
		brick = (nextBrick != null)? nextBrick : Brick.getRandomBrick();
		nextBrick = Brick.getRandomBrick();
		if (brickCollisionCheck(brick)) midlet.endOfGame();
	}

	public Brick getNextBrick() {
		return nextBrick;
	}

	public synchronized void brickTransition(int type) {
		Brick temp = brick.clone();
		boolean nextPhase = false;

		switch (type) {

		case ROTATE_LEFT:
			temp.rotate(true);
			if (!brickCollisionCheck(temp)) brick = temp;
			break;

		case ROTATE_RIGHT:
			temp.rotate(false);
			if (!brickCollisionCheck(temp)) brick = temp;
			break; 

		case LEFT:
			temp.left();
			if (!brickCollisionCheck(temp)) brick = temp;
			break;

		case RIGHT:
			temp.right();
			if (!brickCollisionCheck(temp)) brick = temp;
			break;

		case STEP:
		case SOFTDROP:
			temp.step();
			if (!brickCollisionCheck(temp)) {
				if(type==SOFTDROP) midlet.score.addPointsSoftDropStep();
				brick = temp;
			} else {
				nextPhase = true;
			}
			break;

		case HARDDROP:
			int n = 0;
			while (!brickCollisionCheck(temp)) {
				n++;
				temp.step();
			}
			temp.stepback();
			brick = temp;
			midlet.score.addPointsHardDrop(n-1);
			nextPhase = true;

		}

		if (nextPhase) {
			addBrickToRows();
			// has a row been completed?
			int count = rowCompleteCheck();
			// notify MIDlet
			if (count>1) midlet.multiRowCompleted(count);
			// notify Scoring
			midlet.score.addLines(count);
			//other player sent rows
			addRandomRows();
			// send new GameHeight
			midlet.sendGameHeight(getGameHeight());
			// Create new Brick
			newBrick();
			midlet.score.addBrick();
		}
		temp = null;
	}


	private boolean brickCollisionCheck(Brick b) {
		for (int i=0; i<b.blocks.length; i++) {
			// block coordinates
			int y = b.blocks[i].y;
			int x = b.blocks[i].x;
			// check if valid or overlapping with field
			if(!(x>=0 && x<COLS &&y>=0 && y<=ROWS)) return true;
			if(rows[y].blocks[x] != null) return true;
		}
		return false;
	}

	private int rowCompleteCheck() {
		int count = 0;
		for(int y = 0; y <= ROWS; y++) {
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
		return count;
	}

	public void rowsToAdd(int i) {
		rowsToAdd.addElement(new Integer(i));
	}
	
	
	private void addRandomRows() {
		for(Enumeration e = rowsToAdd.elements(); e.hasMoreElements(); ) {
			int holePos = TetrisMIDlet.random(COLS);
			int count = ((Integer)e.nextElement()).intValue();
			
			/* move other rows up */
			for (int y = 0; y <= ROWS; y++) {
				if(y < ROWS - count+1) {
					/* move rows up */
					rows[y] = rows[y + count];
				} else {
					/* insert new row at bottom */
					rows[y] = Row.getIncompleteRow(holePos,y);
				}		
				rows[y].update(y);
			}
		}
		rowsToAdd.removeAllElements();
	}
	
	/* add the brick to the rows-Object*/
	public void addBrickToRows() {
		for (int i = 0;i < brick.blocks.length;i++) {
			int x = brick.blocks[i].x;
			int y = brick.blocks[i].y;
			rows[y].blocks[x] = brick.blocks[i];
		}
	}

	private int getGameHeight() {
		for(int y=ROWS;y >= 0; y--) 
			if(rows[y].isEmpty()) return ROWS-y;
		
		return ROWS;
	}

	public void paint(Graphics g, int blockSize) {
		int areaHeight = blockSize * TetrisField.ROWS;
		int areaWidth  = blockSize * TetrisField.COLS;
		
		/* draw small lines */
		g.setColor(TetrisCanvas.GRID_COLOR);
		for (int i = 1;i < TetrisField.COLS;i++) g.drawLine(i*blockSize, 0,i*blockSize, areaHeight);
		for (int i = 1;i < TetrisField.ROWS;i++) g.drawLine(0, i*blockSize, areaWidth, i*blockSize);

		/* draw rows*/
		for (int y = 1; y <= ROWS; y++) rows[y].paint(g, blockSize);

		/* draw brick*/
		brick.paint(g, blockSize);
		
		/* frame*/
		g.setColor(TetrisCanvas.FRAME_COLOR);
		g.drawRect(0, 0, areaWidth, areaHeight);
	}

	public void readObject(DataInputStream stream) throws IOException {
		//Rows
		for(int i=0; i<rows.length;i++)
			rows[i].readObject(stream);
		//Bricks
		brick.readObject(stream);
		nextBrick.readObject(stream);
			
	}

	public void writeObject(DataOutputStream stream) throws IOException {
		//Rows
		for(int i=0;i<rows.length;i++)
			rows[i].writeObject(stream);

		brick.writeObject(stream);
		nextBrick.writeObject(stream);
		
	}

}
