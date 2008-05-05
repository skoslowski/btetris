package tetris.ui;

import javax.microedition.lcdui.*;

import java.lang.String;

import tetris.core.TetrisMIDlet;
import tetris.tetris.TetrisField;

public class TetrisGame extends Canvas implements Runnable {

	public static final int FRAME_COLOR = 0xFFFFFF, ACTIVE_BORDER_COLOR = 0xDDDDDD; 
	public static final int PASSIVE_BORDER_COLOR = 0xAAAAAA, GRID_COLOR = 0x101010;

	private static final int DEFAULT_SPEED = 800;
	private int blockSize=0;

	private static final int GAME_WON = 1, GAME_LOST = 2, GAME_NORMAL = 3 ;
	private int gameState = GAME_NORMAL;
	private boolean falling = false;
	private int opponentGameHeight=0;

	private TetrisMIDlet midlet;
	private TetrisField field;
	private volatile Thread gameThread = null;

	public TetrisGame(TetrisMIDlet midlet) {
		this.midlet = midlet;
		setFullScreenMode(true);
		reset();
	}

	public void reset() {
		field = new TetrisField(midlet);
		gameState = GAME_NORMAL;
		opponentGameHeight=0;
		falling = false;
		repaint();
	}

	public void setOpponentsGameHeight(int height) {
		opponentGameHeight=Math.min(TetrisField.ROWS,Math.max(0, height));
	}

	public void run() {
		System.out.println("Game Thread started");

		while (Thread.currentThread() == gameThread) { 
			long timeTick = Math.max(DEFAULT_SPEED - (long)20*midlet.score.getLevel(),50);
			long timeTickFalling = Math.max((timeTick*(6-midlet.settings.fallingSpeed))/20,50);

			long startTime = System.currentTimeMillis();

			if (isShown() && gameState == TetrisGame.GAME_NORMAL) {
				if(!falling) {
					field.brickTransition(TetrisField.STEP);
				} else {
					field.brickTransition(TetrisField.SOFTDROP);
				}

				repaint();
			}

			long timeTaken = System.currentTimeMillis() - startTime;
			synchronized(this) {
				try {

					if(!falling) {
						wait(timeTick - timeTaken);

					} else {
						wait(timeTickFalling- timeTaken);

						if(!falling) {
							wait(timeTick - (System.currentTimeMillis() - startTime));
						}

					}	

				} catch (InterruptedException e) {

				} catch (IllegalArgumentException e) {
					Thread.yield();
				}

			}
		}
		System.out.println("Game Thread done");

	}

	public synchronized void start() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	public synchronized void stop() {
		gameThread = null;	
	}

	/* show lost-game screen */
	public void showLost() {
		gameState = TetrisGame.GAME_LOST;
		repaint();
	}

	/* show won-game screen */
	public void showWon() {
		gameState = TetrisGame.GAME_WON;
		opponentGameHeight=TetrisField.ROWS;
		repaint();
	}


	public void keyPressed(int keyCode) {
		if(gameThread == null) return;
		int keyCodes[] = midlet.settings.keys;

		if(gameState == TetrisGame.GAME_NORMAL) {
			if(keyCode == keyCodes[0] || keyCode==-3) field.brickTransition(TetrisField.LEFT);
			if(keyCode == keyCodes[1] || keyCode==-4) field.brickTransition(TetrisField.RIGHT);
			if(keyCode == keyCodes[2]) 				  field.brickTransition(TetrisField.ROTATE_LEFT);
			if(keyCode == keyCodes[3] || keyCode==-1) field.brickTransition(TetrisField.ROTATE_RIGHT);
			if(keyCode == keyCodes[4] || keyCode==-2) setFalling(true);
			if(keyCode == keyCodes[5]) 				  field.brickTransition(TetrisField.HARDDROP);
		}
		if(keyCode==-5 || keyCode==-6) {
			if(gameState == TetrisGame.GAME_NORMAL) {
				midlet.pauseGame();
			} else {
				int rank = midlet.highscore.checkScore(midlet.score.getPoints());
				if(rank > 0) {
					midlet.gui.showNewHighscoreMenu(rank);
				} else {
					midlet.gui.showInGameMenu(false);
				}
			}
		}

		repaint();
	}

	private void setFalling(boolean falling) {
		this.falling=falling;
		synchronized(this) {
			notify();
		}
	}

	public void keyReleased(int keyCode) {
		if(keyCode == midlet.settings.keys[4] || keyCode==-2) setFalling(false);
	}

	/* try to get the maximum out of available space */
	private int getBestBlockSize(int w, int h) {
		int blockSize = w / (3+TetrisField.COLS);
		if (blockSize * TetrisField.ROWS > h) blockSize = h / TetrisField.ROWS;
		return blockSize;
	}

	public void paint(Graphics g) {

		/*-------------- BACKGROUND --------------*/
		g.setColor(0x000000);
		g.fillRect(0, 0, getWidth(), getHeight());

		/* --------------FIELD AREA-------------- */
		/* get best Size for blocks*/
		if (blockSize == 0) blockSize = getBestBlockSize(getWidth()-3-11, getHeight()-2);

		g.translate(1, (getHeight()-blockSize*TetrisField.ROWS)/2);
		field.paint(g, blockSize);

		/* Won/Lost message */
		int fontAnchorX = blockSize * (TetrisField.COLS / 2);
		int fontAnchorY = blockSize * (TetrisField.ROWS / 2);

		if (gameState == TetrisGame.GAME_LOST)
			drawCenteredTextBox(g, fontAnchorX, fontAnchorY,"You lost!");
		if (gameState == TetrisGame.GAME_WON)
			drawCenteredTextBox(g, fontAnchorX, fontAnchorY,"You won!");	


		g.translate(blockSize * TetrisField.COLS, 0);
		/*------------GAMEHEIGHT AREA---------------*/
		if(midlet.gameType != TetrisMIDlet.SINGLE) {
			g.setColor(FRAME_COLOR);
			g.drawRect(0, 0, 10, TetrisField.ROWS*blockSize);
			g.setColor(ACTIVE_BORDER_COLOR);
			g.fillRect(2, (TetrisField.ROWS-opponentGameHeight)*blockSize+1, 7, opponentGameHeight*blockSize-1);
			g.translate(11, 0);
		}

		/*--------------PREVIEW AREA----------------*/
		int previewTranslate[] = {g.getTranslateX(), g.getTranslateY()};
		// Center Preview area!
		g.translate((getWidth() - g.getTranslateX() - 4*blockSize)/2,0);
		int tr_x = g.getTranslateX(), tr_y = g.getTranslateY();
		/* Center brick in area*/
		int x_min=10, x_max=0, y_min=10, y_max=0;
		for(int i=0; i<field.getNextBrick().blocks.length; i++) {
			x_min = Math.min(x_min, field.getNextBrick().blocks[i].x);
			x_max = Math.max(x_max, field.getNextBrick().blocks[i].x);
			y_min = Math.min(y_min, field.getNextBrick().blocks[i].y);
			y_max = Math.max(y_max, field.getNextBrick().blocks[i].y);
		}
		g.translate(((4-(x_max-x_min+1))*blockSize)/2-x_min*blockSize, ((4-(y_max-y_min+1))*blockSize)/2);
		/* paint nextBrick */
		field.getNextBrick().paint(g, blockSize,false);
		/* frame */
		g.translate(tr_x - g.getTranslateX(), tr_y - g.getTranslateY());
		g.setColor(FRAME_COLOR);
		g.drawRect(0, 0, 4*blockSize, 4*blockSize);              

		/*------------------SCORE AREA--------------------*/
		// reset to preview area
		g.translate(previewTranslate[0]-g.getTranslateX(), previewTranslate[1]-g.getTranslateY());
		// center + move down
		g.translate(0, (3+1)*blockSize+5);
		g.translate((getWidth() - g.getTranslateX())/2,0);
		midlet.score.paint(g, midlet.gameType != TetrisMIDlet.SINGLE);

		/*--------------INCOMING ROW ALERT----------------*/

		g.translate(0 - g.getTranslateX(), 0 - g.getTranslateY());
		if(midlet.rowsToAdd.size()>0) {
			g.setColor(0xFF0000);
			g.fillRect(getWidth()-10,getHeight()-10,8,8);
		}
	}

	private void drawCenteredTextBox(Graphics g, int fontAnchorX, int fontAnchorY, String s) {
		Font f = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
		g.setFont(f);
		int fontHeight = f.getHeight();

		/* background */
		g.setColor(0xFFFFFF);
		g.fillRect(fontAnchorX - f.stringWidth(s) / 2 - 5, fontAnchorY - 5,
				f.stringWidth(s) + 10, fontHeight + 10);

		/* draw box outline */
		g.setColor(0x999999);
		g.drawRect(fontAnchorX - f.stringWidth(s) / 2 - 5, fontAnchorY - 5,
				f.stringWidth(s) + 10, fontHeight + 10);

		/* draw string */
		g.setColor(0x000000);
		g.drawString(s, fontAnchorX, fontAnchorY, Graphics.TOP | Graphics.HCENTER);
	}


}
