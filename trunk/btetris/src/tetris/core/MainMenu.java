package tetris.core;

import javax.microedition.lcdui.*;
import tetris.settings.*;
import tetris.highscore.*;

public class MainMenu extends List implements CommandListener {

	private TetrisMIDlet midlet;
	private Command select;
	private Command quit;

	public MainMenu(TetrisMIDlet midlet) {
		super("Main Menu", List.IMPLICIT);
		this.midlet=midlet;

		append("Single Player", TetrisMIDlet.createImage("/user.png"));
		append("Server", TetrisMIDlet.createImage("/nw.png"));
		append("Client", TetrisMIDlet.createImage("/group.png"));
		append("Highscore",TetrisMIDlet.createImage("/score.png"));
		append("Settings", TetrisMIDlet.createImage("/config.png"));
		append("Info", TetrisMIDlet.createImage("/info.png"));
		append("Battle", TetrisMIDlet.createImage("/group.png"));

		select=new Command("OK", Command.OK, 1);
		quit=new Command("Exit", Command.EXIT, 2);
		setSelectCommand(select);
		addCommand(quit);

		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {

		if(c == quit) {
			midlet.exit();
		}
		else if(c == select) {

			switch(getSelectedIndex()) {

			case 0:
				midlet.startGame(TetrisMIDlet.SINGLE);
				break;
			case 1:
				midlet.startGame(TetrisMIDlet.MULTI_HOST);
				break;
			case 2:
				midlet.startGame(TetrisMIDlet.MULTI_CLIENT);
				break;
			case 3:
				Highscore.showHighscoreMenu(midlet);
				break;
			case 4:
				Settings.showSettingsMenu(midlet);
				break;
			case 5:
				midlet.gui.showAbout();
				break;
			case 6:
				midlet.startGame(TetrisMIDlet.MULTI_TRAINING);
			default:
				midlet.exit();
			}
		}
	}

}
