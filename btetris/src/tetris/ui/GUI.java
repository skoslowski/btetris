package tetris.ui;

import javax.microedition.lcdui.*;
import java.util.Hashtable;

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
	
	public void showServerSearch() {
		display.setCurrent(new ServerSearch(midlet));
	}
	
	public void showNoServersFound() {
		display.setCurrent(new Alert("Multiplayer","No servers found.",null,AlertType.WARNING), new MainMenu(midlet));
	}
	
	public void showServerList(Hashtable servers) {
		display.setCurrent(new ServerList(midlet, servers));
	}
	
	public void showInGameMenu(boolean pausing) {
		display.setCurrent(new InGameMenu(midlet, pausing));
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
