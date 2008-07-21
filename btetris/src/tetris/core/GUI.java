package tetris.core;

import javax.microedition.lcdui.*;

import tetris.connection.ServerSearch;
import tetris.highscore.HighscoreMenu;
import tetris.highscore.NewHighscoreMenu;
import tetris.settings.SettingsKeysMenu;
import tetris.settings.SettingsMenu;
import tetris.settings.SettingsOtherMenu;
import tetris.tetris.TetrisGame;

public class GUI {

	private final Display display;
	private final TetrisMIDlet midlet;

	public TetrisGame gameCanvas;

	public GUI (TetrisMIDlet midlet) {
		this.display=Display.getDisplay(midlet);
		this.midlet =midlet;

		gameCanvas = new TetrisGame(midlet);
	}

	public void showMainMenu() {
		display.setCurrent(new MainMenu(midlet));
	}

	public void showSettingsMenu() {
		display.setCurrent(new SettingsMenu(midlet));
	}
	
	public void showSettingsKeysMenu() {
		display.setCurrent(new SettingsKeysMenu(midlet));
	}
	
	public void showSettingsOtherMenu() {
		display.setCurrent(new SettingsOtherMenu(midlet));
	}

	public void showHighscoreMenu() {
		display.setCurrent(new HighscoreMenu(midlet));
	}

	public void showAbout() {
		display.setCurrent(new AboutMenu(midlet));
	}

	public void showMultiplayerWaiting(boolean showGauge) {
		Alert a = new Alert("Multiplayer","Waiting for other player...",null,AlertType.INFO);
		a.setTimeout(Alert.FOREVER);
		if(showGauge)
			a.setIndicator(new Gauge(null,false,Gauge.INDEFINITE,Gauge.CONTINUOUS_RUNNING));
		a.addCommand(new Command("Stop",Command.CANCEL,1));
		a.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				midlet.bluetoothDisconnected(true);
			}
		});
		display.setCurrent(a);
	}

	public void showServerSearch() {
		ServerSearch serverSearch = new ServerSearch(midlet);
		display.setCurrent(serverSearch);
		//try { Thread.sleep(200); } catch(Exception e) {}
		serverSearch.init();
	}

	public void showInGameMenu(boolean pausing) {
		Alert a = new Alert("GameMenu",pausing?"Game paused":"Game ended",null,AlertType.CONFIRMATION);
		a.setTimeout(Alert.FOREVER);
		if(pausing)
			a.addCommand(new Command("Continue",Command.OK,0));
		else
			a.addCommand(new Command("Restart",Command.OK,0));
		a.addCommand(new Command("Stop",Command.STOP,1));
		a.setCommandListener(new CommandListener() {
			public void commandAction(Command c,Displayable d) {
				if(c.getLabel() == "Restart") {
					midlet.restartGame();
				} else if(c.getLabel() == "Continue") {
					midlet.unpauseGame(false);
				} else {
					midlet.stopGame();
					midlet.gui.showMainMenu();
				}
			}
		});
		display.setCurrent(a);
		//display.setCurrent(new InGameMenu(midlet, pausing));
	}

	public void showTetrisCanvas() {
		display.setCurrent(gameCanvas);
	}

	public void showGameOver(boolean won) {
		String msg=(midlet.gameType==TetrisMIDlet.SINGLE)?"Game over":won?"You won":"You lost";
		Alert a = new Alert("Game over",msg,null,AlertType.WARNING);

		a.setTimeout(Alert.FOREVER);
		a.addCommand(new Command("Restart",Command.OK,0));
		a.addCommand(new Command("Main Menu",Command.STOP,1));
		a.setCommandListener(new CommandListener() {
			public void commandAction(Command c,Displayable d) {
				if(c.getLabel() == "Restart") {
					midlet.restartGame();
				} else {
					midlet.stopGame();
					midlet.gui.showMainMenu();
				}
			}
		});
		display.setCurrent(a);
	}

	public void showError(String errText) {
		display.setCurrent(new Alert("Error",errText,null,AlertType.ERROR),new MainMenu(midlet));
	}

	public void showNewHighscoreMenu(int rank) {
		display.setCurrent(new NewHighscoreMenu(midlet,rank));
	}

}
