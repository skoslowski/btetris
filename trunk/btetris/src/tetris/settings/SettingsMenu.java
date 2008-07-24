package tetris.settings;

import javax.microedition.lcdui.*;

import tetris.core.TetrisMIDlet;

class SettingsMenu extends List implements CommandListener {

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
				Settings.showSettingsKeysMenu(midlet);
				break;
			case 1:
				Settings.showSettingsOtherMenu(midlet);
				break;

			}
		} else {
			midlet.gui.showMainMenu();
		}
	}

}
