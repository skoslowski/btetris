package tetris.core;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

import tetris.connection.*;
import tetris.ui.*;

public class TetrisMIDlet extends MIDlet implements BluetoothListener {

	public static final int SINGLE = 0, MULTI_HOST = 1, MULTI_CLIENT = 2;
	public int gameType = SINGLE;

	public final String version;
	public final Settings settings;
	public final GUI gui;

	public Scoring score;
	public Vector rowsToAdd = new Vector();

	private boolean gamePaused = false;
	private BluetoothConnection bt = null;

	private final int iconResolutions[] = {16,24,32,48};
	public final int fontColor;

	private static int iconResolution=0;
	private static final Random random = new Random(java.lang.System.currentTimeMillis());

	public TetrisMIDlet() {
		settings = new Settings();
		gui = new GUI(this);

		updateIconResolution();
		version = getAppProperty("MIDlet-Version");
		fontColor = Display.getDisplay(this).getColor(Display.COLOR_FOREGROUND);
	}

	public void startApp() {
		gui.showMainMenu();
	}

	public void pauseApp() {
		if(gui.gameCanvas.isShown()) pauseGame();
	}


	public void destroyApp(boolean unconditional) {
		stopGame();
	}

	/*------------------------------------------------------------------------------*/

	public void quit() {
		destroyApp(false);
		notifyDestroyed();
	}

	public void bluetoothSearchComplete(Hashtable servers) {
		if (servers == null || servers.isEmpty()) {
			gui.showNoServersFound();
		} else {
			gui.showServerList(servers);
		}
	}

	public void connectToServer(String url) {
		System.out.println("connecting: "+url);
		bt = new BluetoothClient(url,this);
		bt.start();
	}

	public void bluetoothConnected() {
		gui.showTetrisCanvas();
		gui.gameCanvas.start();
	}

	public void bluetoothDisconnected(boolean byPeer) {
		gui.gameCanvas.stop();
		gui.showMainMenu();

		if(byPeer) bt.stop();
	}

	public void bluetoothError(String s) {
		gui.showError(s);
	}

	public void bluetoothReceivedEvent(byte b) {
		switch (b) {
		case Protocol.ONE_LINE:
			rowsToAdd.addElement(new Integer(1));
			vibrate(200);
			break;
		case Protocol.TWO_LINES:
			rowsToAdd.addElement(new Integer(2));
			vibrate(200);
			break;
		case Protocol.FOUR_LINES:
			rowsToAdd.addElement(new Integer(4));
			vibrate(200);
			break;
		case Protocol.PAUSE_GAME:
			pauseGame();
			break;
		case Protocol.UNPAUSE_GAME:
			unpauseGame();
			break;
		case Protocol.I_LOST:
			gui.gameCanvas.showWon();
			score.addWon();
			break;
		case Protocol.RESTART:
			/* restart game */
			restartGameStart();
			break;
		}
	}

	public void startGame(int gametype) {
		this.gameType = gametype;

		gui.gameCanvas.reset();
		gamePaused = false;
		score = new Scoring();

		if (gametype == SINGLE) {
			gui.showTetrisCanvas();
			gui.gameCanvas.start();
		} else {

			//if (bt != null) bt.stop();

			if (gametype == MULTI_HOST) {
				bt = new BluetoothServer(this);
				bt.start();
				System.out.println("started");
				gui.showServerWaiting();

				System.out.println("waitung");
			}
			if (gametype == MULTI_CLIENT) {
				gui.showServerSearch();
			}	
			

			System.out.println("done");

		}
	}

	/* pause Game - notify peer */
	public void pauseGame() {
		if(!gamePaused) {
			if(gameType == MULTI_CLIENT || gameType == MULTI_HOST) 
				bt.send(Protocol.PAUSE_GAME);
			gamePaused = true;
		}
		gui.showInGameMenu(true);
	}

	/* unpause Game - notify peer */
	public void unpauseGame() {
		if(gamePaused) {
			if (gameType == MULTI_CLIENT || gameType == MULTI_HOST) 
				bt.send(Protocol.UNPAUSE_GAME);
			gamePaused = false;
		}
		gui.showTetrisCanvas();
	}

	/* hit the ceiling */
	public void endOfGame() {
		gui.gameCanvas.showLost();
		score.addLost();
		vibrate(200);

		if (gameType == MULTI_CLIENT || gameType == MULTI_HOST) 
			bt.send(Protocol.I_LOST); /* you win! */
	}

	/* request restart of game*/
	public void restartGame() {
		if (gameType == MULTI_CLIENT || gameType == MULTI_HOST) 
			bt.send(Protocol.RESTART); 

		restartGameStart();
	}

	/* restart the game local*/
	public void restartGameStart() {
		gui.gameCanvas.reset();
		score.reset();
		rowsToAdd.removeAllElements();
		gui.showTetrisCanvas();
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

	/* end game */
	public void stopGame() {		
		if (bt != null) bt.stop();
		score = null;
		gui.gameCanvas.stop();
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
		return ((random.nextInt()&0x7FFFFFFF) + 1) % size;
	}

	public void vibrate(int millis) {
		Display.getDisplay(this).vibrate(millis);
	}



}