package tetris.ui;

import javax.microedition.lcdui.*;
import javax.bluetooth.DiscoveryListener;
import java.util.Enumeration;
import java.util.Hashtable;

import tetris.connection.*;
import tetris.core.TetrisMIDlet;

class ServerServiceSearch 
extends List 
implements CommandListener, BluetoothDiscovery.BluetoothServiceSearchListener 
{

	private final TetrisMIDlet midlet;

	private final Command select, back, rescan, recheck;
	private final Image notchecked,online,querying,offline;

	private Hashtable devIdToEntries;
	private Hashtable URLs;

	public ServerServiceSearch(TetrisMIDlet midlet, BluetoothDiscovery btDiscovery) {
		super("Serverlist", List.IMPLICIT);

		this.midlet=midlet;
		btDiscovery.setBluetoothServiceSearchListener(this);
		devIdToEntries = new Hashtable();
		URLs = new Hashtable();

		select=new Command("Connect", Command.OK, 1);
		back=new Command("Back", Command.EXIT, 2);
		rescan=new Command("Rescan", Command.EXIT, 3);
		recheck=new Command("Recheck", Command.EXIT, 3);

		setSelectCommand(select);
		addCommand(back);
		addCommand(rescan);
		addCommand(recheck);

		setCommandListener(this);

		notchecked  = TetrisMIDlet.createImage("/bt.png"); 
		online  = TetrisMIDlet.createImage("/user.png");
		querying = TetrisMIDlet.createImage("/nw.png");
		offline = TetrisMIDlet.createImage("/delete.png");

		Hashtable servers = btDiscovery.getServers();

		for(Enumeration e = servers.keys();e.hasMoreElements();) {
			// Extract info
			String id = (String)e.nextElement();
			String name = (String)servers.get(id);
			//add to list
			int index = append(name,notchecked);
			// identify entries and server
			devIdToEntries.put(id,new Integer(index));
		}
		
		btDiscovery.startServiceSearch();
		
	}
	

	public void commandAction(Command c, Displayable d) {

		if(c == back) {
			midlet.gui.showMainMenu();

		} else if(c== rescan) {
			midlet.gui.showServerInquiry();
			
		} else if(c== recheck) {
			midlet.gui.showServerInquiry();
			
		} else if(c == select) {
		
			if(URLs.containsKey(new Integer(getSelectedIndex()))) {
				removeCommand(select);
				String url = (String)URLs.get(new Integer(getSelectedIndex()));
				System.out.println(url);
				midlet.connectToServer(url);
			}
		}

	}

	public void bluetoothServiceSearchStarted(String id) {
		// Change Icon to Querying
		int index = idToIndex(id);
		set(index, getString(index), querying);		
	}

	public void bluetoothServiceSearchResult(String id, int respCode, String url) {
		int index = idToIndex(id);
		if(respCode == DiscoveryListener.SERVICE_SEARCH_COMPLETED) {
			URLs.put(new Integer(index), url);
			set(index, getString(index), online);
		} else {
			set(index, getString(index), offline);			
		}
	}

	public void bluetoothError(String e) {
		// TODO Auto-generated method stub

	}

	private int idToIndex(String id) {
		Object index = devIdToEntries.get(new String(id));
		if (index == null) return -1;
		return ((Integer)index).intValue();
	}

}
