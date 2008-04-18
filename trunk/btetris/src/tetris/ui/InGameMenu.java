package tetris.ui;
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

import tetris.core.TetrisMIDlet;


public class InGameMenu extends List
implements CommandListener {

	private TetrisMIDlet midlet;
	private boolean pausing;
	private Command back, select;

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
		back = new Command("Back", Command.EXIT, 2);
		addCommand(back);
		setSelectCommand(select);
		setCommandListener(this);

	}

	public void commandAction(Command c, Displayable d) {
		if(c == back) {
			midlet.gui.showTetrisCanvas();
			
		} else if(c == select){
			switch(getSelectedIndex()) {
			case 0:
				if(pausing) {
					midlet.unpauseGame();
				} else {
					midlet.restartGame();
				}
				break;
			case 1:
				midlet.stopGame();
				midlet.gui.showMainMenu();
				break;
			case 2:
				midlet.stopGame();
				midlet.quit();
				break;
			default:
				midlet.quit();
			}
		}
	}

}
