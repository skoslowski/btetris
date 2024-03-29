package tetris.tetris;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.*;

import tetris.core.RecordStoreHandler;
import tetris.core.TetrisMIDlet;
import tetris.settings.*;
import tetris.highscore.*;

public class TetrisCanvas 
	extends Canvas 
	implements RecordStoreHandler.Persistant 
{

	/* Colors */
	public static final int FRAME_COLOR = 0xFFFFFF, GRID_COLOR = 0x050505;
	public static final int GAMEHEIGHT_COLOR = 0xDDDDDD;
	//public static final int PASSIVE_BORDER_COLOR = 0xFFFFFF, ACTIVE_BORDER_COLOR = 0xFFFFFF;
	
	/* GameStates */
	private static final int GAME_WON = 1, GAME_LOST = 2, GAME_NORMAL = 3, GAME_PAUSED = 4;

	private int blockSize=0;
	private int opponentGameHeight=0;
	private int gameState = GAME_NORMAL;

	private final TetrisMIDlet midlet;
	private final TetrisField field;
	private final Settings settings;

	public TetrisCanvas(TetrisMIDlet midlet) {
		this.midlet = midlet;
		setFullScreenMode(true);
		
		field = new TetrisField(midlet);
		settings = Settings.getInstance();
		repaint();
	}

	/* Thread for left-right movement */
	private class TransitionThread extends Thread {
		private static final long TRANSITION_SPEED = 200;
		private int action=0;
		private boolean running = true;

		public synchronized void setAction(int action) { 
			this.action = action;
			notify();
		}

		public void run() {
			running = true;
			try {
				long timeTick=TRANSITION_SPEED-50*(settings.transitionSpeed-2);
				do {
					try {
						if(!isShown()) action=0;
						synchronized(this) {
							if(action>0) {
								field.brickTransition(action);
								repaint();
							}
							wait(timeTick);
						}
					} catch (InterruptedException e) {
						if(!running) break;
					}
				} while(running);
			} catch (NullPointerException e) {}
		}		
	}
	private volatile TransitionThread transitionThread = null;

	/* Thread for falling brick */
	private class GameThread extends Thread {
		private static final long DEFAULT_SPEED = 800, SPEED_INCR = 22;
		private boolean falling = false, restart = false, firstTime=true;
		private boolean running = true;

		private synchronized void setFalling(boolean falling) {
			this.falling=falling;
			notify();
		}
		private synchronized void restart() {
			this.restart = true;
			notify();
		}
		public void run() {
			running = true;
			try {
				while (running) {
					try {
						long timeTick = Math.max(DEFAULT_SPEED - (long)SPEED_INCR*midlet.score.getLevel(),60);
						long timeTickFalling = Math.max((timeTick*(6-settings.fallingSpeed))/20,50);
	
						long startTime = System.currentTimeMillis();
						if (isShown() && gameState == TetrisCanvas.GAME_NORMAL && !firstTime) {
							if(!falling)
								field.brickTransition(TetrisField.STEP);
							else
								field.brickTransition(TetrisField.SOFTDROP);
							repaint();
						}
						firstTime=false;
						
						long timeTaken = System.currentTimeMillis() - startTime;
	
						synchronized(this) {
							try {
								do {
									restart = false;
									if(!falling) {
										// Normal Tick
										wait(timeTick - timeTaken);
									} else {
										// Fast Tick (falling)
										wait(timeTickFalling- timeTaken);
										if(!falling && !restart)
											// additional pause if falling was ended during wait(...)
											wait(timeTick - (System.currentTimeMillis() - startTime));
									}
								} while (restart);
							}  catch (IllegalArgumentException e) {
								// if one of wait(...) calls had a negative argument
								Thread.yield();
							}
						} 
					} catch (InterruptedException e) {}
				} 
			} catch (NullPointerException e) {}
		}
	}
	private volatile GameThread gameThread = null;

	public void hideNotify() {
		if(gameState == GAME_NORMAL) midlet.pauseGame(false);
	}

	public void setOpponentsGameHeight(int height) {
		opponentGameHeight=Math.min(TetrisField.ROWS,Math.max(0, height));
	}

	public synchronized void start() {
		if (gameThread != null || transitionThread != null) stop();

		gameState = GAME_NORMAL;
		gameThread = new GameThread();
		transitionThread = new TransitionThread();
		gameThread.start();
		transitionThread.start();
	}

	/* Shutdown the Game */
	public synchronized void stop() throws NullPointerException {
		if(gameThread!=null) {
			gameThread.running=false;
			gameThread.interrupt();
			gameThread = null;
		}
		if(transitionThread!=null) {
			transitionThread.running=false;
			transitionThread.interrupt();
			transitionThread = null;
		}	
	}

	/* show lost-game screen */
	public void showLost() {
		gameState = TetrisCanvas.GAME_LOST;
		repaint();
	}

	/* show won-game screen */
	public void showWon() {
		gameState = TetrisCanvas.GAME_WON;
		opponentGameHeight=TetrisField.ROWS;
		repaint();
	}

	/* show paused-game screen */
	public void showPaused() {
		gameState = TetrisCanvas.GAME_PAUSED;
		repaint();
	}


	public void keyPressed(int keyCode) {
		int keyCodes[] = settings.keys;

		if(gameState == TetrisCanvas.GAME_NORMAL) {
			if(keyCode == keyCodes[0] || keyCode==-3) {
				if(settings.transitionSpeed>0)
					if(transitionThread!=null) transitionThread.setAction(TetrisField.LEFT);
				else
					field.brickTransition(TetrisField.LEFT);	
			}
			if(keyCode == keyCodes[1] || keyCode==-4) {
				if(settings.transitionSpeed>0)
					if(transitionThread!=null) transitionThread.setAction(TetrisField.RIGHT);
				else
					field.brickTransition(TetrisField.RIGHT);;	
			}

			if(keyCode == keyCodes[2])
				field.brickTransition(TetrisField.ROTATE_LEFT);
			if(keyCode == keyCodes[3] || keyCode==-1) 
				field.brickTransition(TetrisField.ROTATE_RIGHT);
			if(keyCode == keyCodes[4] || keyCode==-2)
				if(gameThread!=null) gameThread.setFalling(true);
			if(keyCode == keyCodes[5]) {
				field.brickTransition(TetrisField.HARDDROP);
				if(gameThread!=null) gameThread.restart();
			}
		}

		if(keyCode==-5 || keyCode==-6) {
			if(gameState == TetrisCanvas.GAME_NORMAL || gameState == TetrisCanvas.GAME_PAUSED) {
				midlet.pauseGame(false);
			} else {
				int rank = Highscore.getInstance().checkScore(midlet.score.getPoints());
				if(midlet.gameType == TetrisMIDlet.SINGLE && rank > 0)
					midlet.gui.showNewHighscoreMenu(rank);
				else
					midlet.gui.showInGameMenu(false);
			}
		}

		repaint();
	}	

	public void keyReleased(int keyCode) {
		int keyCodes[] = settings.keys;

		if(keyCode == keyCodes[4] || keyCode==-2) 
			if(gameThread != null) gameThread.setFalling(false);

		if(keyCode == keyCodes[0] || keyCode==-3 || keyCode == keyCodes[1] || 
				keyCode==-4 || keyCode==-5 || keyCode==-6)
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

		/* --------------Won/Lost/Paused message-------------- */
		// Where to show
		int fontAnchorX = blockSize * (TetrisField.COLS / 2);
		int fontAnchorY = blockSize * (TetrisField.ROWS / 2);
		// What to show
		if (gameState == TetrisCanvas.GAME_LOST) {
			String msg = (midlet.gameType==TetrisMIDlet.SINGLE)?"Game over!":"You lost!";
			drawCenteredTextBox(g, fontAnchorX, fontAnchorY,msg);
		} else if (gameState == TetrisCanvas.GAME_WON) {
			drawCenteredTextBox(g, fontAnchorX, fontAnchorY,"You won!");	
		} else if (gameState == TetrisCanvas.GAME_PAUSED) {
			drawCenteredTextBox(g, fontAnchorX, fontAnchorY,"Paused!");	
		}

		// Move to right edge of field
		g.translate(blockSize * TetrisField.COLS, 0);
		/*------------GAMEHEIGHT AREA---------------*/
		if(midlet.gameType != TetrisMIDlet.SINGLE) {
			g.setColor(FRAME_COLOR);
			g.drawRect(0, 0, 10, TetrisField.ROWS*blockSize);
			g.setColor(GAMEHEIGHT_COLOR);
			g.fillRect(2, (TetrisField.ROWS-opponentGameHeight)*blockSize+1, 7, opponentGameHeight*blockSize-1);
			g.translate(11, 0);
		}

		/*--------------PREVIEW AREA----------------*/

		// Save Position for Score Area
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
		field.getNextBrick().paint(g, blockSize);

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
		// show score field
		midlet.score.paint(g, midlet.gameType != TetrisMIDlet.SINGLE);
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

	public void rowsToAdd(int i) {
		field.rowsToAdd(i);
	}

	public void readObject(DataInputStream stream) throws IOException {
		field.readObject(stream);
		gameState = TetrisCanvas.GAME_PAUSED;
	}

	public void writeObject(DataOutputStream stream) throws IOException {
		field.writeObject(stream);
	}
}
