package tetris.tetris;

import javax.microedition.lcdui.Graphics;
import java.lang.Integer;
import java.util.Enumeration;

import tetris.core.TetrisMIDlet;
import tetris.ui.TetrisGame;

public class TetrisField {

	public static final int ROTATE_LEFT = 1, ROTATE_RIGHT = 2, LEFT = 3;
	public static final int RIGHT = 4, SOFTDROP = 5, HARDDROP = 6, STEP=7;
	public static final int COLS = 10, ROWS = 20;
	
	private Brick brick, nextBrick = null;  // for preview
	private TetrisMIDlet midlet;
	private Row rows[];

	public TetrisField(TetrisMIDlet midlet) {
		this.midlet = midlet;		
		rows = new Row[ROWS+1];
		for (int y = 0;y < rows.length;y++) rows[y] = new Row();

		newBrick();
	}

	private void newBrick() {
		brick = (nextBrick != null)? nextBrick : Brick.getRandomBrick();
		nextBrick = Brick.getRandomBrick();

		if (brickCollisionCheck(brick)) {
			midlet.endOfGame();
		} else {
		}
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

			addBrickToRows(brick);
			// has a row been completed?
			int count = rowCompleteCheck();
			// notify midlet
			if (count > 1) midlet.multiRowCompleted(count);
			// scoring
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
		int x,y;
		for (int i = 0;i < b.blocks.length; i++) {
			y = b.blocks[i].y;
			x = b.blocks[i].x;
			if (y>0 && (y > ROWS || x < 0 || x >= COLS || rows[y].blocks[x] != null)) return true;
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

	private void addRandomRows() {
		if(midlet.rowsToAdd.size() > 0) {
			for(Enumeration counts = midlet.rowsToAdd.elements(); counts.hasMoreElements();) {
				int holePos = TetrisMIDlet.random(COLS);
				int count = ((Integer)counts.nextElement()).intValue();
				/* move other rows up */
				for (int y = 0; y <= ROWS; y++) {
					if(y < ROWS - count+1) {
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
	
	private int getGameHeight() {
		for(int y=ROWS;y >= 0; y--) {
			if(rows[y].isEmpty()) return ROWS-y;
		}
		return ROWS;
	}

	public void paint(Graphics g, int blockSize) {
		/* draw small lines */
		int areaHeight = blockSize * TetrisField.ROWS;
		int areaWidth  = blockSize * TetrisField.COLS;
		g.setColor(TetrisGame.GRID_COLOR);
		for (int i = 1;i < TetrisField.COLS;i++) g.drawLine(i*blockSize, 0,i*blockSize, areaHeight);
		for (int i = 1;i < TetrisField.ROWS;i++) g.drawLine(0, i*blockSize, areaWidth, i*blockSize);

		/* draw rows*/
		for (int y = 1; y <= ROWS; y++)
			rows[y].paint(g, blockSize);

		/* draw brick*/
		brick.paint(g, blockSize,true);
		/* frame*/
		g.setColor(TetrisGame.FRAME_COLOR);
		g.drawRect(0, 0, areaWidth, areaHeight);
	}

}
