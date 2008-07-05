package tetris.ui;

import javax.microedition.lcdui.*;

import tetris.core.TetrisMIDlet;

public class SettingsMenu extends List implements CommandListener {

	private final TetrisMIDlet midlet;
	private final Command select;
	
	public SettingsMenu(TetrisMIDlet midlet) {
		super("Settings",List.IMPLICIT);
		
		this.midlet=midlet;
		
		append("Keys",null);
		append("Other",null);

		select=new Command("OK", Command.OK, 1);
		setSelectCommand(select);
		addCommand(new Command("Back", Command.BACK, 2));
		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {
		if( c == select ) {
			switch(getSelectedIndex()) {
			case 0:
				midlet.gui.showSettingsKeysMenu();
				break;
			case 1:
				midlet.gui.showSettingsOtherMenu();
				break;

			}
		} else {
			midlet.gui.showMainMenu();
		}
	}

}
