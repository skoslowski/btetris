package tetris.ui;

import javax.microedition.lcdui.*;

import java.util.Enumeration;
import java.util.Hashtable;

import tetris.core.TetrisMIDlet;

class ServerList extends List implements CommandListener {

	private TetrisMIDlet midlet;
	private Command select;
	private Command back;
	private Hashtable servers;

	public ServerList(TetrisMIDlet midlet, Hashtable servers) {

		super("Serverlist", List.IMPLICIT);

		this.midlet=midlet;
		this.servers=servers;
	
		for(Enumeration e=servers.keys();e.hasMoreElements();)
			append((String)e.nextElement(),TetrisMIDlet.createImage("/bt.png"));

		
		select=new Command("Connect", Command.OK, 1);
		back=new Command("Back", Command.EXIT, 2);

		setSelectCommand(select);
		addCommand(back);

		setCommandListener(this);

	}

	public void commandAction(Command c, Displayable d) {

		if(c == back) {
			midlet.gui.showMainMenu();

		}else if(c == select) {
			removeCommand(select);
			midlet.connectToServer((String)servers.get(getString(getSelectedIndex())));
		}

	}

}
