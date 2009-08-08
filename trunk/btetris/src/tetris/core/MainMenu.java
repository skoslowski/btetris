package tetris.core;

import javax.microedition.lcdui.*;

import tetris.settings.*;
import tetris.highscore.*;

public class MainMenu extends List implements CommandListener {
	static final int inMain = 0, in1P = 1, in2P = 2;
	
	private final TetrisMIDlet midlet;
	private final MultiPlayerMenu multiplayerMenu;
	private final SinglePlayerMenu singleplayerMenu;
	
	private Command select;
	private Command quit;
	public int subMenu = inMain;
	

	public MainMenu(TetrisMIDlet midlet) {
		super("Main Menu", List.IMPLICIT);
		this.midlet=midlet;
		this.multiplayerMenu = new MultiPlayerMenu(midlet, this);
		this.singleplayerMenu = new SinglePlayerMenu(midlet, this);
		
		append("Single Player", TetrisMIDlet.createImage("/user2.png"));
		append("Multi Player", TetrisMIDlet.createImage("/group.png"));
		
		append("Highscore",TetrisMIDlet.createImage("/score.png"));
		append("Settings", TetrisMIDlet.createImage("/config.png"));
		append("Info", TetrisMIDlet.createImage("/info.png"));

		select=new Command("OK", Command.OK, 1);
		quit=new Command("Exit", Command.EXIT, 2);
		setSelectCommand(select);
		addCommand(quit);

		setCommandListener(this);
	}
	
	public void show() {
		Displayable d;
		
		if(subMenu == in1P) 
			d = singleplayerMenu;
		else if(subMenu == in2P) 
			d = multiplayerMenu;
		else
			d = this;
		
		Display.getDisplay(midlet).setCurrent(d);
	}
	

	public void commandAction(Command c, Displayable d) {

		if(c == quit) {
			midlet.exit();
		}
		else if(c == select) {

			switch(getSelectedIndex()) {

			case 0:
				subMenu = MainMenu.in1P;
				Display.getDisplay(midlet).setCurrent(singleplayerMenu);
				break;
			case 1:
				subMenu = MainMenu.in2P;
				Display.getDisplay(midlet).setCurrent(multiplayerMenu);
				break;
			case 2:
				Highscore.showHighscoreMenu(midlet);
				break;
			case 3:
				Settings.showSettingsMenu(midlet);
				break;
			case 4:
				midlet.gui.showAbout();
				break;
			default:
				midlet.exit();
			}
		}
	}
}

class MultiPlayerMenu 
	extends List 
	implements CommandListener 
{

	private final TetrisMIDlet midlet;
	private final MainMenu mainMenu;
	private Command select;
	private Command back;
	
	public MultiPlayerMenu(TetrisMIDlet midlet, MainMenu mainMenu) {
		super("Multiplayer Menu", List.IMPLICIT);
		this.midlet=midlet;
		this.mainMenu = mainMenu;
		
		append("Act as Client", TetrisMIDlet.createImage("/user.png"));
		append("Act as Server", TetrisMIDlet.createImage("/nw.png"));
		
		select=new Command("OK", Command.OK, 1);
		back=new Command("Back", Command.BACK, 2);
		setSelectCommand(select);
		addCommand(back);

		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {

		if(c == back) {
			mainMenu.subMenu  = MainMenu.inMain;
			mainMenu.show();
		}
		else if(c == select) {

			switch(getSelectedIndex()) {
			case 0:
				midlet.startGame(TetrisMIDlet.MULTI_CLIENT);
				break;
			case 1: 
				midlet.startGame(TetrisMIDlet.MULTI_HOST);
				break;
			default:
				midlet.exit();
			}
		}
	}
}

class SinglePlayerMenu 
	extends List 
	implements CommandListener 
{

	private final TetrisMIDlet midlet;
	private final MainMenu mainMenu;
	private Command select;
	private Command back;
	
	public SinglePlayerMenu(TetrisMIDlet midlet, MainMenu mainMenu) {
		super("Singleplayer Menu", List.IMPLICIT);
		this.midlet=midlet;
		this.mainMenu = mainMenu;
		
		append("Normal", TetrisMIDlet.createImage("/user.png"));
		append("Battle", TetrisMIDlet.createImage("/bomb.png"));
		
		select=new Command("OK", Command.OK, 1);
		back=new Command("Back", Command.BACK, 2);
		setSelectCommand(select);
		addCommand(back);
	
		setCommandListener(this);
	}
	
	public void commandAction(Command c, Displayable d) {
	
		if(c == back) {
			mainMenu.subMenu  = MainMenu.inMain;
			mainMenu.show();
		}
		else if(c == select) {
	
			switch(getSelectedIndex()) {
			case 0:
				midlet.startGame(TetrisMIDlet.SINGLE);
				break;
			case 1: 
				midlet.startGame(TetrisMIDlet.MULTI_TRAINING);
				break;
			default:
				midlet.exit();
			}
		}
	}
}