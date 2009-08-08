package tetris.core;

import javax.microedition.lcdui.*;




public class AboutMenu extends Form
implements CommandListener {

	private TetrisMIDlet midlet;
	private Command save;
	
	public AboutMenu(TetrisMIDlet midlet) {

		super("Info");
		this.midlet=midlet;
		
		StringItem sItem = new StringItem(null,
				"BTetris\n"+
				"\n"+
				"A free j2me tetris (R) clone with bluetooth multiplayer support\n"+
				"\n"+
				"Version: "+String.valueOf(midlet.version)+"\n"+
				"\n"+
				"Coding:\n"+
				"Sebastian Koslowski\n"+
				"\n"+
				"Website:\n"+
				"http://btetris.googlecode.com\n"+
				"\n"+
				"Tetris-Idea:\n"+
				"Alexey Pajitnov\n"+
				"\n"+
				"Copyright:\n"+
				"This program is free software; you can redistribute it and/or modify "+
				"it under the terms of the GNU General Public License as published by "+
				"the Free Software Foundation; either version 2 of the License, or "+
				"(at your option) any later version. "+
				"This program is distributed in the hope that it will be useful, "+
				"but WITHOUT ANY WARRANTY; without even the implied warranty of "+
				"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "+
				"GNU General Public License for more details. "+
				"You should have received a copy of the GNU General Public License "+
				"along with this program; if not, write to the Free Software Foundation, "+
				"Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. "+
				"\n"
		);
		sItem.setFont(Font.getFont((Font.getDefaultFont()).getFace(),Font.STYLE_PLAIN,Font.SIZE_SMALL));
		append(sItem);

		addCommand(new Command("Back", Command.BACK, 2));
		
		save = new Command("Save Log","Save Log to File", Command.EXIT, 2);
		if (midlet.gameLog.size()>0) addCommand(save);
		setCommandListener(this);

	}

	public void commandAction(Command c, Displayable d) {
		if(c == save) {
			boolean result = midlet.gameLog.saveLogToFile();
			Alert a = new Alert("Save Log",result?"Saved Log successfully":"Failed",null,
					result?AlertType.CONFIRMATION:AlertType.ERROR);
			a.setTimeout(2000);
			Display.getDisplay(midlet).setCurrent(a, this);
			
		} else {
			midlet.gui.showMainMenu();
		}
	}

}
