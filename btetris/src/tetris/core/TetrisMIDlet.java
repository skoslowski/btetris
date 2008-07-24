package tetris.core;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import java.io.*;
import java.util.*;

import tetris.connection.*;
import tetris.settings.Settings;
import tetris.tetris.*;

public class TetrisMIDlet 
	extends MIDlet 
	implements BluetoothSocket.BluetoothListener 
{

	public static final int SINGLE = 0, MULTI_HOST = 1, MULTI_CLIENT = 2;
	public int gameType = SINGLE;
	public final String version;
	
	public final GUI gui;
	public Scoring score;
	
	private BluetoothSocket bt = null;

	private final int iconResolutions[] = {16,24,32,48};
	public final int fontColor;

	private static int iconResolution=0;
	private static final Random random = new Random();

	public TetrisMIDlet() {
		gui = new GUI(this);
		
		updateIconResolution();
		version = getAppProperty("MIDlet-Version");
		fontColor = Display.getDisplay(this).getColor(Display.COLOR_FOREGROUND);
	}

	public void startApp() {
		gui.showMainMenu();
		System.out.println("startApp");
	}

	public void pauseApp() {
		System.out.println("pauseApp");
		pauseGame(false);
	}


	public void destroyApp(boolean unconditional) {
		System.out.println("stopApp");	
		stopGame();
	}

	public void quit() {		
		destroyApp(false);
		notifyDestroyed();
	}

	/*------------------------------BT-Stuff----------------------------------*/

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

	public void bluetoothReceivedEvent(byte b[]) {
		switch (b[0]) {
		case Protocol.ONE_LINE:
			gui.gameCanvas.rowsToAdd(1);
			vibrate(200);
			break;
		case Protocol.TWO_LINES:
			gui.gameCanvas.rowsToAdd(2);
			vibrate(200);
			break;
		case Protocol.FOUR_LINES:
			gui.gameCanvas.rowsToAdd(4);
			vibrate(200);
			break;
		case Protocol.GAME_HEIHGT:
			if(b.length>1) {
				int gameh = (int)b[1];
				gui.gameCanvas.setOpponentsGameHeight(gameh);
			}
			break;
		case Protocol.PAUSE_GAME:
			pauseGame(true);
			break;
		case Protocol.UNPAUSE_GAME:
			unpauseGame(true);
			break;
		case Protocol.I_LOST:
			gui.gameCanvas.stop();
			gui.gameCanvas.showWon();
			score.addWon();
			break;
		case Protocol.RESTART:
			/* sync random */
			if(Settings.getInstance().syncBricks) {
				try {
					ByteArrayInputStream bias = new ByteArrayInputStream(b);
					DataInputStream inputStream = new DataInputStream(bias);
					
					inputStream.readByte();
					long seed = inputStream.readLong();
					if(Settings.getInstance().syncBricks) random.setSeed(seed);
					
					inputStream.close();
					bias.close();
				} catch (IOException e) {}
			}
			/* restart game */
			restartGameStart();
			break;
		}
	}
	
	/*----------------------------Game-Stuff----------------------------------*/

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
			gui.showTetrisCanvas();
			gui.gameCanvas.start();
			
		} else {
			
			if (gametype == MULTI_HOST) {
				bt = new BluetoothServer(this);
				bt.start();
				gui.showMultiplayerWaiting(true);
			
			} else if (gametype == MULTI_CLIENT) {
					gui.showServerSearch();
			}	
		}
	}

	/* multiple rows completed in one strike */
	public void multiRowCompleted(int count) {
		if(gameType == SINGLE) return;

		if (count >= 4) {
			bt.send(Protocol.FOUR_LINES); 
			score.addSendRows(4);
		} else if (count == 3) {
			bt.send(Protocol.TWO_LINES); 
			score.addSendRows(2);
		} else if (count == 2) {
			bt.send(Protocol.ONE_LINE); 
			score.addSendRows(1);
		}
	}
	
	/* transmit game height to opponent*/
	public void sendGameHeight(int myHeight) {
		if(gameType == SINGLE) return;
		byte buf[] = {Protocol.GAME_HEIHGT,(byte)myHeight};
		bt.send(buf);
	}
	
	/* pause Game - notify peer */
	public void pauseGame(boolean byPeer) {
		gui.gameCanvas.stop();
		
		//show pause message
		gui.gameCanvas.showPaused();
		
		if(!byPeer) {
			//notify peer
			if(gameType!=SINGLE) bt.send(Protocol.PAUSE_GAME);
			//Show in game menu
			gui.showInGameMenu(true);
		}
		
		// Notify ScoreObject for rate calculation
		score.notifyPaused();
	}
	
	/* UnPause Game - notify peer */
	public void unpauseGame(boolean byPeer) {
		// notify peer
		if (!byPeer && gameType!=SINGLE)  bt.send(Protocol.UNPAUSE_GAME);
	
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

		if (gameType == MULTI_CLIENT || gameType == MULTI_HOST)
			bt.send(Protocol.I_LOST); /* you win! */
	}

	/* request restart of game*/
	public void restartGame() {
		if (gameType == MULTI_CLIENT || gameType == MULTI_HOST) {
			
			/* Generating new seed (points for more randomness ) */
			long seed = System.currentTimeMillis() + score.getPoints();		
			random.setSeed(seed);

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
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		
		} finally {
			gui.gameCanvas=null;
			score = null;
			bt=null;
			
			System.gc();
		}		
	}	

	/*------------------------------------------------------------------------------*/

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

	public static int random(int size) {
		//return ((random.nextInt()&0x7FFFFFFF) + 2) % size;
		return (random.nextInt(size) + random.nextInt(3)) % size;
	}

	public void vibrate(int millis) {
		Display.getDisplay(this).vibrate(millis);
	}



}