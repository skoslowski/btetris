package tetris.core;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import java.io.*;

import tetris.connection.*;
import tetris.opponent.VirtualOpponent;
import tetris.settings.Settings;
import tetris.tetris.*;

public class TetrisMIDlet 
	extends MIDlet 
	implements BluetoothSocket.BluetoothListener, VirtualOpponent.VirtOpListener
{

	public static final int SINGLE = 0, MULTI_HOST = 1, MULTI_CLIENT = 2, MULTI_TRAINING = 3;
	public int gameType = SINGLE;
	public final String version;
	
	public final GUI gui;
	public Scoring score;
	
	private BluetoothSocket bt = null;
	private VirtualOpponent virtOp = null;

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
		stopGame();
	}

	public void quit() {		
		destroyApp(false);
		notifyDestroyed();
	}

	/*----------------------------------------------------------------------*/
	/*---------------------------- BT-Stuff --------------------------------*/
	/*----------------------------------------------------------------------*/

	public void connectToServer(String url) {
		bt = new BluetoothClient(url,this);
		bt.start();
	}

	public void bluetoothConnected() {
		if(gameType==MULTI_HOST) restartGame();
		/*Client waits for server to request "restart" */
	}
	
	public void bluetoothDisconnected(boolean byPeer) {
		stopGame();
		gui.showMainMenu();
	}

	public void bluetoothError(String s) {
		gui.showError(s);
	}

	public void bluetoothHandleEvent(byte b[]) {
		switch (b[0]) {
		case Protocol.ONE_LINE:
			recieveRows(1);
			break;
		case Protocol.TWO_LINES:
			recieveRows(2);
			break;
		case Protocol.FOUR_LINES:
			recieveRows(4);
			break;
		case Protocol.GAME_HEIHGT:
			if(b.length>1) recieveOpponentHeight((int)b[1]);
			break;
		case Protocol.PAUSE_GAME:
			pauseGame(true);
			break;
		case Protocol.UNPAUSE_GAME:
			unpauseGame(true);
			break;
		case Protocol.I_LOST:
			gameOver();
			break;
		case Protocol.RESTART:
			/* sync random */
			if(Settings.getInstance().syncBricks) {
				try {
					ByteArrayInputStream bias = new ByteArrayInputStream(b);
					DataInputStream inputStream = new DataInputStream(bias);
					
					inputStream.readByte();
					long seed = inputStream.readLong();
					if(Settings.getInstance().syncBricks) random.reset(seed);
					
					inputStream.close();
					bias.close();
				} catch (IOException e) {}
			}
			/* restart game */
			restartGameStart();
			break;
		}
	}
	
	
	/*----------------------------------------------------------------------*/
	/*--------------------------- Opponent ---------------------------------*/
	/*----------------------------------------------------------------------*/

	public void gameOver() {
		gui.gameCanvas.stop();
		gui.gameCanvas.showWon();
		score.addWon();	
	}

	public void recieveRows(int count) {
		gui.gameCanvas.rowsToAdd(count);
		vibrate(200);
	}
	
	public void recieveOpponentHeight(int gameheight) {
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
				bt = new BluetoothServer(this);
				bt.start();
				gui.showMultiplayerWaiting(true);
			
			} else if (gametype == MULTI_CLIENT) {
				gui.showServerSearch();
					
			} else if (gametype == MULTI_TRAINING) {
				virtOp = new VirtualOpponent(this);
				gui.showTetrisCanvas();
				gui.gameCanvas.start();
				virtOp.start();
			}
		}
	}

	/* multiple rows completed in one strike */
	public void multiRowCompleted(int count) {
		// Calculate Rows to send.	
		if(--count >= 3) count = 4;
		if(count == 0) return;
	
		if(gameType == MULTI_HOST || gameType == MULTI_CLIENT) {
			byte code = (count==4)?Protocol.FOUR_LINES:(count==2)?Protocol.TWO_LINES:Protocol.ONE_LINE;
			bt.send(code);
			score.addSendRows(count);
		
		} else if(gameType == MULTI_TRAINING) {
			virtOp.addToRows(count);
			score.addSendRows(count);
		}
	}
	
	/* transmit game height to opponent*/
	public void sendGameHeight(int myHeight) {
		if(gameType == MULTI_HOST || gameType == MULTI_CLIENT) {
			byte buf[] = {Protocol.GAME_HEIHGT,(byte)myHeight};
			bt.send(buf);
		}
	}
	
	/* pause Game - notify peer */
	public void pauseGame(boolean byPeer) {
		gui.gameCanvas.stop();
		
		//show pause message
		gui.gameCanvas.showPaused();
		
		if(!byPeer) {
			//notify peer
			if(gameType == MULTI_HOST || gameType == MULTI_CLIENT) {
				 bt.send(Protocol.PAUSE_GAME);
			} else if(gameType == MULTI_TRAINING) {
				virtOp.stop();
			}
			//Show in game menu
			gui.showInGameMenu(true);
		}
		
		// Notify ScoreObject for rate calculation
		score.notifyPaused();
	}
	
	/* UnPause Game - notify peer */
	public void unpauseGame(boolean byPeer) {
		// notify peer
		if (!byPeer) {
			if(gameType == MULTI_HOST || gameType == MULTI_CLIENT) {
				bt.send(Protocol.UNPAUSE_GAME);
			} else if(gameType == MULTI_TRAINING) {
				virtOp.start();
			}	
		}
	
		gui.gameCanvas.start();
		gui.showTetrisCanvas();
	}

	/* hit the ceiling */
	public void endOfGame() {
		gui.gameCanvas.stop();
		gui.gameCanvas.showLost();
		score.addLost();
		//gui.showGameOver(false);
		vibrate(200);

		if (gameType == MULTI_CLIENT || gameType == MULTI_HOST) {
			bt.send(Protocol.I_LOST); /* you win! */
		} else if(gameType == MULTI_TRAINING) {
			virtOp.stop();
		}
	}

	/* request restart of game*/
	public void restartGame() {
		if (gameType == MULTI_CLIENT || gameType == MULTI_HOST) {
			
			/* Generating new seed (points for more randomness ) */
			long seed = System.currentTimeMillis() + score.getPoints();		
			random.reset(seed);

			/* Transmit new seed */	
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream outputStream = new DataOutputStream(baos);	
				
				outputStream.writeByte(Protocol.RESTART);
				outputStream.writeLong(seed);
				outputStream.flush();
				bt.send(baos.toByteArray()); 
			
				outputStream.close();
				baos.close();
				
			} catch(IOException e) {
				bt.send(Protocol.RESTART);
			}
					
		} else if(gameType == MULTI_TRAINING) {
			virtOp.reset();
			virtOp.start();
		}
		restartGameStart();
	}

	/* restart the game local*/
	public void restartGameStart() {
		score = new Scoring();
		gui.gameCanvas = new TetrisCanvas(this);
		gui.showTetrisCanvas();
		gui.gameCanvas.start();
	}

	/* end game before */
	public void stopGame() {	
		try {
			if(gui.gameCanvas!=null) gui.gameCanvas.stop();
			if(bt!=null) bt.stop();	
			if(virtOp!=null) virtOp.stop();	
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		
		} finally {
			gui.gameCanvas=null;
			score = null;
			bt=null;
			virtOp=null;
			
			System.gc();
		}		
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