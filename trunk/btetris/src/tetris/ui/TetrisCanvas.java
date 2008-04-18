package tetris.ui;

import javax.microedition.lcdui.*;

import java.lang.String;

import tetris.core.TetrisMIDlet;
import tetris.tetris.TetrisField;

public class TetrisCanvas extends Canvas implements Runnable {

	private static final int DEFAULT_SPEED = 500;
	private int blockSize=0;
	
	public static final int FRAME_COLOR = 0xFFFFFF, ACTIVE_BORDER_COLOR = 0xAAAAAA; 
	public static final int PASSIVE_BORDER_COLOR = 0x777777, BORDER_COLOR = 0x111111;

	private static final int GAME_WON = 1, GAME_LOST = 2, GAME_NORMAL = 3 ;
	public int gameState;
	private boolean falling;

	private TetrisMIDlet midlet;
	private TetrisField field;
	private volatile Thread gameThread = null;

	public TetrisCanvas(TetrisMIDlet midlet) {
		this.midlet = midlet;
		setFullScreenMode(true);
		reset();
	}
	
	public void reset() {
		field = new TetrisField(midlet);
		gameState = GAME_NORMAL;
		falling = false;
		repaint();
	}

	public void run() {
		System.out.println("Game Thread started");
		try {
			while (Thread.currentThread() == gameThread) {
				long startTime = System.currentTimeMillis();
				
				if (isShown() && gameState == TetrisCanvas.GAME_NORMAL) {
					field.brickTransition(TetrisField.STEP);
					repaint();
				}
				
				long timeTaken = System.currentTimeMillis() - startTime;
				long timeTick = DEFAULT_SPEED - (long)Math.sqrt(16000*(midlet.score.lines/10));
				timeTick  = Math.max(falling ? (timeTick*(6-midlet.settings.fallingSpeed))/10 : timeTick,50);
				
				if (timeTaken < timeTick) {
					Thread.sleep(timeTick - timeTaken);
				} else {
					Thread.yield();
					System.out.println("game thread yield");
				}
			}
			System.out.println("Game Thread done");
			
		} catch (InterruptedException e) {}
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
		gameState = TetrisCanvas.GAME_LOST;
		repaint();
	}

	/* show won-game screen */
	public void showWon() {
		gameState = TetrisCanvas.GAME_WON;
		repaint();
	}

	public void keyPressed(int keyCode) {
		if(gameThread == null) return;
		int keyCodes[] = midlet.settings.keys;

		if(gameState == TetrisCanvas.GAME_NORMAL) {
			if(keyCode == keyCodes[0] || keyCode==-3) field.brickTransition(TetrisField.LEFT);
			if(keyCode == keyCodes[1] || keyCode==-4) field.brickTransition(TetrisField.RIGHT);
			if(keyCode == keyCodes[2] || keyCode==-1) field.brickTransition(TetrisField.ROTATE_LEFT);
			if(keyCode == keyCodes[3]) 				  field.brickTransition(TetrisField.ROTATE_RIGHT);
			if(keyCode == keyCodes[4] || keyCode==-2) {
				field.brickTransition(TetrisField.STEP);
				synchronized(this) {
					falling=true;
				}
			}
			if(keyCode == keyCodes[5]) field.brickTransition(TetrisField.DROP);
		}
		if(keyCode==-5 || keyCode==-6) {
			if(gameState == TetrisCanvas.GAME_NORMAL) {
				midlet.pauseGame();
			} else {
				midlet.gui.showInGameMenu(false);
			}
		}

		repaint();
	}

	public void keyReleased(int keyCode) {
		if(keyCode == midlet.settings.keys[4] || keyCode==-2) 
			synchronized(this) {
				falling=false;
			}
	}

	/* try to get the maximum out of available space
	 * add 3 COLS for Preview*/
	private int getBestBlockSize(int w, int h) {
		int blockSize = w / (3+TetrisField.COLS);
		if (blockSize * TetrisField.ROWS > h) blockSize = h / TetrisField.ROWS;
		return blockSize;
	}

	public void paint(Graphics g) {

		/*-------------- BACKGROUND --------------*/
		g.setColor(0x000000);
		g.fillRect(0, 0, getWidth(), getHeight());

		/*-------------- SCORE --------------*/
		int scoreHeight = midlet.score.paint(g, midlet.gameType != TetrisMIDlet.SINGLE);

		/* --------------FIELD AREA-------------- */

		/* get best Size for blocks*/
		if (blockSize == 0) blockSize = getBestBlockSize(getWidth()-3, getHeight()-2-scoreHeight);

		g.translate(1, (1+scoreHeight)+(getHeight()-(1+ scoreHeight)-blockSize*TetrisField.ROWS)/2);
		field.paint(g, blockSize);

		/* Won/Lost message */
		int fontAnchorX = blockSize * (TetrisField.COLS / 2);
		int fontAnchorY = blockSize * (TetrisField.ROWS / 2);
		String gameScore = "";
		if (midlet.gameType != TetrisMIDlet.SINGLE)
			gameScore = " (" + midlet.score.won + ":" + midlet.score.lost + ")";
		if (gameState == TetrisCanvas.GAME_LOST)
			drawCenteredTextBox(g, fontAnchorX, fontAnchorY,"Verloren!" + gameScore);
		if (gameState == TetrisCanvas.GAME_WON)
			drawCenteredTextBox(g, fontAnchorX, fontAnchorY,"Gewonnen!" + gameScore);	


		/*--------------PREVIEW AREA----------------*/

		g.translate(blockSize * TetrisField.COLS+2, 0);
		// Center Preview area!
		g.translate((getWidth() - g.getTranslateX() - 3*blockSize)/2,0);
		int tr_x = g.getTranslateX(), tr_y = g.getTranslateY();
		/* Center brick in area*/
		int x_min=10, x_max=0, y_min=10, y_max=0;
		for(int i=0; i<field.nextBrick.blocks.length; i++) {
			x_min = Math.min(x_min, field.nextBrick.blocks[i].x);
			x_max = Math.max(x_max, field.nextBrick.blocks[i].x);
			y_min = Math.min(y_min, field.nextBrick.blocks[i].y);
			y_max = Math.max(y_max, field.nextBrick.blocks[i].y);
		}
		g.translate(((3-(x_max-x_min+1))*blockSize)/2-x_min*blockSize, ((4-(y_max-y_min+1))*blockSize)/2);
		/* paint nextBrick */
		field.nextBrick.paint(g, blockSize,false);
		/* frame */
		g.translate(tr_x - g.getTranslateX(), tr_y - g.getTranslateY());
		g.setColor(FRAME_COLOR);
		g.drawRect(0, 0, 3*blockSize, 4*blockSize);              


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