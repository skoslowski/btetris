package tetris.ui;

import javax.microedition.lcdui.*;

import tetris.core.TetrisMIDlet;

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
	
	public void showHighscoreMenu() {
		display.setCurrent(new HighscoreMenu(midlet));
	}
	
	public void showAbout() {
		display.setCurrent(new AboutMenu(midlet));
	}
	
	public void showServerWaiting() {
		Alert a = new Alert("Multiplayer","Waiting for other player...",null,AlertType.INFO);
		
		a.setTimeout(Alert.FOREVER);
		a.setIndicator(new Gauge(null,false,Gauge.INDEFINITE,Gauge.CONTINUOUS_RUNNING));
		a.addCommand(new Command("Stop",Command.CANCEL,1));
		
		a.setCommandListener(new CommandListener() {
			public void commandAction(Command c, Displayable d) {
				midlet.bluetoothDisconnected(true);
			}
		});
		
		display.setCurrent(a);
	}
	
	public void showServerInquiry() {
		display.setCurrent(new ServerInquiry(midlet, midlet.btDiscovery));
	}
	
	public void showServerServiceSearch() {
		display.setCurrent(new ServerServiceSearch(midlet, midlet.btDiscovery));
	}
	
	public void showInGameMenu(boolean pausing) {
		display.setCurrent(new InGameMenu(midlet, pausing));
	}

	public void showTetrisCanvas() {
		display.setCurrent(gameCanvas);
	}
	
	public void showGameOver(boolean won) {
		String msg=(midlet.gameType==TetrisMIDlet.SINGLE)?"Game over":won?"You won":"You lost";
		Alert a = new Alert("Game over",msg,null,AlertType.WARNING);
		
		a.setTimeout(Alert.FOREVER);
		a.addCommand(new Command("Restart",Command.OK,0));
		a.addCommand(new Command("Back",Command.BACK,1));
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
