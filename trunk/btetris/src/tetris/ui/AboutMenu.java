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



public class AboutMenu extends Form
implements CommandListener {

	private TetrisMIDlet midlet;

	public AboutMenu(TetrisMIDlet midlet) {

		super("Info");
		this.midlet=midlet;
		
		StringItem sItem = new StringItem(null,
				"scriptris2\n"+
				"\n"+
				"A free j2me tetris (R) clone with bluetooth multiplayer support\n"+
				"\n"+
				"Version: "+String.valueOf(midlet.version)+"\n"+
				"\n"+
				"Coding:\n"+
				"Sebastian Koslowski\n"+
				"\n"+
				"A mod of 'scriptris' by\n"+
				"Michael Arndt\n"+
				"\n"+
				"Tetris-Idea:\n"+
				"Alexey Pajitnov\n"+
				"\n"+
				"Copyright:\n"+
				"This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by\n"+
				"the Free Software Foundation; either version 2 of the License, or\n"+
				"(at your option) any later version.\n"+
				"This program is distributed in the hope that it will be useful,\n"+
				"but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
				"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"+
				"GNU General Public License for more details.\n"+
				"You should have received a copy of the GNU General Public License\n"+
				"along with this program; if not, write to the Free Software Foundation,\n"+
				"Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.\n"+
				"\n"
		);
		sItem.setFont(Font.getFont((Font.getDefaultFont()).getFace(),Font.STYLE_PLAIN,Font.SIZE_SMALL));
		append(sItem);
		
		addCommand(new Command("Back", Command.EXIT, 2));
		setCommandListener(this);

	}

	public void commandAction(Command c, Displayable d) {
		midlet.gui.showMainMenu();
	}

}
