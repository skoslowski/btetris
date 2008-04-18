package tetris.ui;

import javax.microedition.lcdui.*;
import java.util.Hashtable;

import tetris.core.TetrisMIDlet;
import tetris.connection.*;

class ServerSearch 
	extends Form 
	implements CommandListener, BluetoothSearchListener 
{

	private TetrisMIDlet midlet;
	private Command retry, abort, back, next;
	private BluetoothDiscovery btDiscovery = new BluetoothDiscovery(this);
	private Hashtable servers;

	public ServerSearch(TetrisMIDlet midlet) {
		super("Server search");
		this.midlet=midlet;

		btDiscovery.start();
		
		abort=new Command("Stop", Command.STOP, 1);
		retry=new Command("Retry", Command.OK, 2);
		next=new Command("Continue", Command.OK, 1);
		back=new Command("Back", Command.EXIT, 2);

		
		addCommand(abort);

		setCommandListener(this);

	}
	
	

	public void commandAction(Command c, Displayable d) {

		if(c == back) {
			midlet.gui.showMainMenu();

		} else if(c == abort) {
			btDiscovery.stop();
			removeCommand(abort);
			addCommand(back);
			
		} else if(c == retry) {
			deleteAll();
			removeCommand(retry);
			addCommand(abort);
			btDiscovery.start();
		
		} else if(c==next) {
			midlet.gui.showServerList(servers);
			
		}

	}

	
	public void bluetoothSearchComplete(Hashtable servers) {
		removeCommand(abort);
		if(servers.size() > 0) {
			this.servers=servers;
			addCommand(next);
			addCommand(retry);
		} else {
			addCommand(retry);
		}
		addCommand(back);
	}
		
	
	public void bluetoothError(String e) {
		this.append("Error: "+e+"\n");
	}
	
	public void bluetoothSearchLog(String s) {
		StringItem si = new StringItem(null,s);
		si.setFont(Font.getFont(Font.getDefaultFont().getFace(), Font.getDefaultFont().getStyle(), Font.SIZE_SMALL));
		this.append(si);
	}

}
