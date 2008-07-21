package tetris.core;
/* scriptris - a free j2me tetris (R) clone with
 * bluetooth multiplayer support
 *
 *
 * (c) 2005-2006 Michael "ScriptKiller" Arndt
 * <scriptkiller@gmx.de>
 * http://scriptkiller.de/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 *
 */


import javax.microedition.lcdui.*;



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

		select=new Command("OK", Command.OK, 1);
		quit=new Command("Exit", Command.EXIT, 2);
		setSelectCommand(select);
		addCommand(quit);

		setCommandListener(this);
	}

	public void commandAction(Command c, Displayable d) {

		if(c == quit) {
			midlet.quit();
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
				midlet.gui.showHighscoreMenu();
				break;
			case 4:
				midlet.gui.showSettingsMenu();
				break;
			case 5:
				midlet.gui.showAbout();
				break;
			default:
				midlet.quit();
			}
		}
	}

}
