package tetris.ui;

import javax.microedition.lcdui.*;

import tetris.core.TetrisMIDlet;
import tetris.tetris.TetrisField;

public class TetrisGame extends Canvas {

	/* Colors */
	public static final int FRAME_COLOR = 0xFFFFFF, ACTIVE_BORDER_COLOR = 0xDDDDDD; 
	public static final int PASSIVE_BORDER_COLOR = 0xAAAAAA, GRID_COLOR = 0x101010;
	/* GameStates */
	private static final int GAME_WON = 1, GAME_LOST = 2, GAME_NORMAL = 3;

	private int blockSize=0;
	private int opponentGameHeight=0;
	private int gameState = GAME_NORMAL;

	private final TetrisMIDlet midlet;
	private final TetrisField field;

	public TetrisGame(TetrisMIDlet midlet) {
		this.midlet = midlet;
		setFullScreenMode(true);
		field = new TetrisField(midlet);
		repaint();
	}

	/* Thread for left-right movement */
	private class TransitionThread extends Thread {
		private static final long TRANSITION_SPEED = 200;
		private int action=0;
		public synchronized void setAction(int action) { 
			this.action = action; 
			notify();
		}
		public void run() {
			try {
				long timeTick=TRANSITION_SPEED-50*(midlet.settings.transitionSpeed-2);

				do {
					if(!isShown()) action=0;

					if(action>0) {
						field.brickTransition(action);
						repaint();
					}
					synchronized(this) {
						wait(timeTick);
					}
				} while(transitionThread == Thread.currentThread());
					
			} catch (InterruptedException e) {}
		}		
	}
	private volatile TransitionThread transitionThread = null;

	/* Thread for falling brick */
	private class GameThread extends Thread {
		private static final long DEFAULT_SPEED = 800;
		private boolean falling = false;

		private synchronized void setFalling(boolean falling) {
			this.falling=falling;
			notify();
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
	}
	private volatile GameThread gameThread = null;

	public void setOpponentsGameHeight(int height) {
		opponentGameHeight=Math.min(TetrisField.ROWS,Math.max(0, height));
	}

	public synchronized void start() {
		if (gameThread != null || transitionThread != null) stop();

		gameThread = new GameThread();
		transitionThread = new TransitionThread();
		gameThread.start();
		transitionThread.start();
	}

	public synchronized void stop() {
		gameThread = null;
		//transitionThread.setAction(0);
		transitionThread = null;
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
		int keyCodes[] = midlet.settings.keys;

		if(gameState == TetrisGame.GAME_NORMAL) {
			if(keyCode == keyCodes[0] || keyCode==-3) {
				if(midlet.settings.transitionSpeed>0)
					transitionThread.setAction(TetrisField.LEFT);
				else
					field.brickTransition(TetrisField.LEFT);	
			}
			if(keyCode == keyCodes[1] || keyCode==-4) {
				if(midlet.settings.transitionSpeed>0)
					transitionThread.setAction(TetrisField.RIGHT);
				else
					field.brickTransition(TetrisField.RIGHT);;	
			}

			if(keyCode == keyCodes[2]) 				  field.brickTransition(TetrisField.ROTATE_LEFT);
			if(keyCode == keyCodes[3] || keyCode==-1) field.brickTransition(TetrisField.ROTATE_RIGHT);
			if(keyCode == keyCodes[4] || keyCode==-2) gameThread.setFalling(true);
			if(keyCode == keyCodes[5]) 				  field.brickTransition(TetrisField.HARDDROP);
		}
		if(keyCode==-5 || keyCode==-6) {
			if(gameState == TetrisGame.GAME_NORMAL) {
				midlet.pauseGame();
			} else {
				int rank = midlet.highscore.checkScore(midlet.score.getPoints());
				if(midlet.gameType == TetrisMIDlet.SINGLE && rank > 0) {
					midlet.gui.showNewHighscoreMenu(rank);
				} else {
					midlet.gui.showInGameMenu(false);
				}

			}
		}

		repaint();
	}	

	public void keyReleased(int keyCode) {
		int keyCodes[] = midlet.settings.keys;

		if(keyCode == keyCodes[4] || keyCode==-2) 
			if(gameThread != null) gameThread.setFalling(false);

		if(keyCode == keyCodes[0] || keyCode==-3 || keyCode == keyCodes[1] || keyCode==-4 || keyCode==-5 || keyCode==-6)
			if(transitionThread != null) transitionThread.setAction(0);

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

		if (gameState == TetrisGame.GAME_LOST) {
			String msg = (midlet.gameType==TetrisMIDlet.SINGLE)?"Game over!":"You lost!";
			drawCenteredTextBox(g, fontAnchorX, fontAnchorY,msg);
		}
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
