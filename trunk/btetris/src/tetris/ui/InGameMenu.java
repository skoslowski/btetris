package tetris.ui;

import javax.microedition.lcdui.*;
import tetris.core.TetrisMIDlet;


public class InGameMenu extends List implements CommandListener {

	private TetrisMIDlet midlet;
	private boolean pausing;
	private Command select;

	public InGameMenu(TetrisMIDlet midlet, boolean pausing) {

		super("Game Menu", List.IMPLICIT);

		this.midlet=midlet;
		this.pausing = pausing;
		
		if(pausing) {
			append("Continue", TetrisMIDlet.createImage("/redo.png"));
		} else {
			append("New Game", TetrisMIDlet.createImage("/refresh.png"));			
		}
		append("Main Menu", TetrisMIDlet.createImage("/home.png"));

		select = new Command("Select", Command.OK, 1);
		setSelectCommand(select);
		setCommandListener(this);

	}

	public void commandAction(Command c, Displayable d) {
		if(c == select){
			switch(getSelectedIndex()) {
			case 0:
				if(pausing) {
					midlet.unpauseGame(false);
				} else {
					midlet.restartGame();
				}
				break;
			case 1:
				midlet.stopGame();
				midlet.gui.showMainMenu();
				break;
			default:
				midlet.quit();
			}
		}
	}

}
