package tetris.core;

import javax.microedition.lcdui.*;

import tetris.connection.ServerSearchMenu;
import tetris.highscore.NewHighscoreMenu;
import tetris.tetris.TetrisCanvas;

public class GUI {

	private final Display display;
	private final TetrisMIDlet midlet;
	private final MainMenu mainMenu;

	public TetrisCanvas gameCanvas;
	

	public GUI (TetrisMIDlet midlet) {
		this.display=Display.getDisplay(midlet);
		this.midlet =midlet;
		this.mainMenu = new MainMenu(midlet);
	}

	public void showMainMenu() {
		mainMenu.show();
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
				midlet.stopGame(false);
			}
		});
		display.setCurrent(a);
	}

	public void showAndStartServerSearch() {
		new ServerSearchMenu(midlet);
	}

	public void showInGameMenu(boolean pausing) {
		Alert a = new Alert("GameMenu",pausing?"Game paused":"Game ended",null,AlertType.CONFIRMATION);
		a.setTimeout(Alert.FOREVER);
		
		a.addCommand(new Command("Stop",Command.STOP,0));
		if(pausing) {
			a.addCommand(new Command("Continue",Command.OK,0));
			
			if(midlet.gameType == TetrisMIDlet.SINGLE)
				a.addCommand(new Command("Save",Command.ITEM,1));
		} else {
			a.addCommand(new Command("Restart",Command.OK,0));
		}
		a.addCommand(new Command("Stop",Command.ITEM,1));
		a.addCommand(new Command("Exit",Command.ITEM,1));

		a.setCommandListener(new CommandListener() {
			public void commandAction(Command c,Displayable d) {
				if(c.getLabel() == "Restart") {
					midlet.restartGame(false,-1);
				} else if(c.getLabel() == "Continue") {
					midlet.unpauseGame(false);
				} else if(c.getLabel() == "Save") {
					midlet.saveGame();
					midlet.stopGame(false);
				} else if(c.getLabel() =="Exit") {
					midlet.stopGame(false);
					midlet.exit();
				} else {
					midlet.stopGame(false);				
				
				}
			}
		});
		display.setCurrent(a);
	}

	public void showTetrisCanvas() {
		display.setCurrent(gameCanvas);
	}

	public void showError(String errText) {
		display.setCurrent(new Alert("Error",errText,null,AlertType.ERROR),new MainMenu(midlet));
	}

	public void showNewHighscoreMenu(int rank) {
		display.setCurrent(new NewHighscoreMenu(midlet,rank));
	}

}
