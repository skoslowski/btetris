package tetris.core;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import tetris.connection.*;
import tetris.opponent.*;
import tetris.tetris.*;

public class TetrisMIDlet 
	extends MIDlet 
	implements TetrisPlayerListener
{

	public static final int SINGLE = 0, MULTI_HOST = 1, MULTI_CLIENT = 2, MULTI_TRAINING = 3;
	public int gameType = SINGLE;
	public final String version;
	
	public final GUI gui;
	
	public Scoring score = null;
	private Opponent opponent = null;

	private final int iconResolutions[] = {16,24,32,48};
	public final int fontColor;

	private static int iconResolution=0;
	public static final TGMrandomizer random = new TGMrandomizer();

	public TetrisMIDlet() {
		gui = new GUI(this);
		
		updateIconResolution();
		version = getAppProperty("MIDlet-Version");
		fontColor = Display.getDisplay(this).getColor(Display.COLOR_FOREGROUND);
	}

	public void startApp() {
		gui.showMainMenu();
	}

	public void pauseApp() {
		pauseGame(false);
	}


	public void destroyApp(boolean unconditional) {
		stopGame(false);
	}

	public void exit() {		
		destroyApp(false);
		notifyDestroyed();
	}

	/*----------------------------------------------------------------------*/
	/*---------------------------- BT-Stuff --------------------------------*/
	/*----------------------------------------------------------------------*/

	public void connectToServer(String url) {
		((BluetoothOpponent)opponent).startGame(MULTI_CLIENT, url);
	}
	
	/*----------------------------------------------------------------------*/
	/*--------------------------- Opponent ---------------------------------*/
	/*----------------------------------------------------------------------*/

	public void recieveRows(int count) {
		gui.gameCanvas.rowsToAdd(count);
		vibrate(200);
	}
	
	public void recieveHeight(int gameheight) {
		gui.gameCanvas.setOpponentsGameHeight(gameheight);
	}

	/*----------------------------------------------------------------------*/
	/*--------------------------- Game-Stuff -------------------------------*/
	/*----------------------------------------------------------------------*/

	public void startGame(int gametype) {
		// check if bluetooth is turned on
		if (gametype != SINGLE)
			if (!BluetoothSocket.isBluetoothOn()) {
				gui.showError("Bluetooth is turned off");
				return;
			}
			
		this.gameType = gametype;

		Scoring.resetWonLost();
		score = new Scoring();
		
		gui.gameCanvas = new TetrisCanvas(this);

		if (gametype == SINGLE) {
			opponent= null;
			// Check if a game was saved
			boolean savedGameLoaded = loadGame();
			
			gui.showTetrisCanvas();
			if(savedGameLoaded) {
				gui.gameCanvas.repaint();
				try{ Thread.sleep(500); } catch (Exception e) {};
				gui.showInGameMenu(true);

			} else {
				gui.gameCanvas.start();
			}
			
		} else {
			
			if (gametype == MULTI_HOST) {
				opponent = new BluetoothOpponent(this);
				opponent.startGame(MULTI_HOST);
				gui.showMultiplayerWaiting(true);
			
			} else if (gametype == MULTI_CLIENT) {
				opponent = new BluetoothOpponent(this);
				gui.showServerSearch();
					
			} else if (gametype == MULTI_TRAINING) {
				opponent = new VirtualOpponent(this);
				opponent.startGame(-1);
				
				gui.showTetrisCanvas();
				gui.gameCanvas.start();
			}
		}
	}

	/* multiple rows completed in one strike */
	public void multiRowCompleted(int count) {
		// Calculate Rows to send.	
		if(--count >= 3) count = 4;
		if(count == 0) return;
	
		if(opponent != null) {
			opponent.recieveRows(count);
			score.addSendRows(count);
		}
	}
	
	/* transmit game height to opponent*/
	public void sendGameHeight(int myHeight) {
		if(opponent != null) opponent.recieveHeight(myHeight);
	}
	
	/* pause Game - notify peer */
	public void pauseGame(boolean byPeer) {
		//stop local game
		gui.gameCanvas.stop();
		//show pause message
		gui.gameCanvas.showPaused();
		// Notify ScoreObject for rate calculation
		score.notifyPaused();
		// notify opponent
		if(!byPeer) {
			if(opponent!=null) opponent.pauseGame(true);
		
			gui.showInGameMenu(true);
		}
	}
	
	/* UnPause Game - notify peer */
	public void unpauseGame(boolean byPeer) {
		// notify peer
		if (!byPeer && opponent!=null) opponent.unpauseGame(true);
		
		gui.gameCanvas.start();
		gui.showTetrisCanvas();
	}

	/* hit the ceiling */
	public void endOfGame(boolean byPeer) {
		gui.gameCanvas.stop();
		
		if(byPeer) {
			gui.gameCanvas.showWon();
			score.addWon();	
		} else {
			gui.gameCanvas.showLost();
			score.addLost();
		}
		vibrate(200);
		
		if(!byPeer && opponent!=null) opponent.endOfGame(true);
	}

	/* request restart of game*/
	public void restartGame(boolean byPeer, long seed) {
		if(!byPeer && opponent != null) {
			/* Generating new seed (points for more randomness ) */
			seed = System.currentTimeMillis() + score.getPoints();		
					
			
			opponent.restartGame(true, seed);
		}
		if(seed != -1) random.reset(seed);	
					
		score = new Scoring();
		gui.gameCanvas = new TetrisCanvas(this);
		gui.showTetrisCanvas();
		gui.gameCanvas.start();
	}

	/* end game before */
	public void stopGame(boolean byPeer) {	
		try {
			if(gui.gameCanvas!=null) gui.gameCanvas.stop();
			if(!byPeer && opponent!=null) opponent.stopGame(false);	
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		
		} finally {
			gui.gameCanvas=null;
			score = null;
			opponent=null;
			
			System.gc();
		}
		
		gui.showMainMenu();
	}	

	
	private boolean loadGame() {
		boolean gameLoaded = false;
		RecordStoreHandler rsHandler = new RecordStoreHandler();
		boolean savedGameLoaded = rsHandler.load(gui.gameCanvas, "SavedGame");
		if(savedGameLoaded) {
			rsHandler.delete("SavedGame");
			
			score = new Scoring();
			boolean savedScoresLoaded = rsHandler.load(score, "SavedGameScore");
			if(savedScoresLoaded) rsHandler.delete("SavedGameScore");

			gameLoaded = savedScoresLoaded;
		}

		return gameLoaded;
	}

	public void saveGame() {
		RecordStoreHandler rsHandler = new RecordStoreHandler();
		rsHandler.save(gui.gameCanvas, "SavedGame");
		rsHandler.save(score, "SavedGameScore");
	}
	

	/*----------------------------------------------------------------------*/
	/*----------------------------------------------------------------------*/
	/*----------------------------------------------------------------------*/

	private void updateIconResolution() {
		Display display = Display.getDisplay(this);
		int bestRes = Math.min(display.getBestImageHeight(Display.LIST_ELEMENT),
				display.getBestImageWidth(Display.LIST_ELEMENT));
		for(int i=0; i<iconResolutions.length; i++) {
			if(bestRes < iconResolutions[i]) break;
			iconResolution = iconResolutions[i];
		}
	}

	public static Image createImage(String filename) {
		Image image = null;
		try {
			image = Image.createImage("/" + iconResolution + filename);
		} catch (java.io.IOException ex) {
			return null;
		}
		return image;
	}

	public void vibrate(int millis) {
		Display.getDisplay(this).vibrate(millis);
	}

}