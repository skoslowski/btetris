package tetris.ui;

import javax.microedition.lcdui.*;
import javax.bluetooth.DiscoveryListener;
import java.util.Enumeration;
import java.util.Hashtable;

import tetris.connection.*;
import tetris.core.TetrisMIDlet;

class ServerSearch 
extends List 
implements CommandListener, BluetoothDiscovery.BluetoothServerListener 
{
	private final TetrisMIDlet midlet;

	private final Command connect, back, rescan, recheck,stop;
	private final Image notchecked,online,querying,offline;
	private final BluetoothDiscovery btDiscovery;

	private Hashtable devIdToEntries;
	private Hashtable URLs;

	public ServerSearch(TetrisMIDlet midlet) {
		super("Serverlist", List.IMPLICIT);

		this.midlet=midlet;
		btDiscovery = new BluetoothDiscovery(this);
		devIdToEntries = new Hashtable();
		URLs = new Hashtable();

		// create/add Commands
		connect=new Command("Connect", Command.ITEM, 1);
		stop=new Command("Stop", Command.STOP, 1);
		back=new Command("Back", Command.BACK, 2);
		rescan=new Command("Rescan","Scan for Devices", Command.SCREEN, 3);
		recheck=new Command("Recheck","Recheck Devices", Command.SCREEN, 3);
		setSelectCommand(null);		
		addCommand(back);
		setCommandListener(this);

		// Status icons
		notchecked  = TetrisMIDlet.createImage("/bt.png"); 
		online  = TetrisMIDlet.createImage("/user.png");
		querying = TetrisMIDlet.createImage("/nw.png");
		offline = TetrisMIDlet.createImage("/delete.png");
	}
	
	public void start() {
		// Check for Devices
		Hashtable devices = btDiscovery.getDevices();

		if(devices == null) {
			// Look for Devices
			startInquiry();
		} else {
			// Append Devices, map IDs to index
			for(Enumeration e = devices.keys();e.hasMoreElements();) {
				// Extract info
				String id = (String)e.nextElement();
				String name = (String)devices.get(id);
				//add to list
				devIdToEntries.put(id,new Integer(append(name,notchecked)));
			}

			// remove/add commands
			addCommand(rescan);
			addCommand(recheck);
			setSelectCommand(connect);
			// start looking for Tetris Servers
			btDiscovery.startServiceSearch();
		}
	}

	public void commandAction(Command c, Displayable d) {
		if(c == back) {
			midlet.gui.showMainMenu();
		
		} else if(c == stop) {
			btDiscovery.stopInquiry();
			
		} else if(c == rescan) {
			btDiscovery.stopServiceSearch();
			startInquiry();

		} else if(c== recheck) {
			//reset icons
			for(int i=0;i<size();i++) set(i,getString(i),notchecked);
			// clear URL mappings
			URLs.clear();
			//start searching for servers
			btDiscovery.startServiceSearch();

		} else if(c == connect) {
			if(URLs.containsKey(new Integer(getSelectedIndex()))) {
				
				setSelectCommand(null);
				removeCommand(back);
				removeCommand(rescan);
				removeCommand(recheck);
				
				String url = (String)URLs.get(new Integer(getSelectedIndex()));
				midlet.connectToServer(url);
			}
		}

	}
	
	/* ----------------------------------------------------------------------- */
	private void startInquiry() {
		// clear URL,ID mappings
		URLs.clear();
		devIdToEntries.clear();
		
		//remove all items
		deleteAll();
		
		// remove/add commands
		setSelectCommand(null);
		removeCommand(back);
		removeCommand(rescan);
		removeCommand(recheck);
		addCommand(stop);

		// Start Inquiry for Devices
		btDiscovery.startInquiry();
		
		// add Inquiry indicator
		int index = append("Inquiry started",null);
		changeFont(index,-1,Font.STYLE_ITALIC,Font.SIZE_SMALL);
	}
	
	public void bluetoothInquiryDeviceDiscoved(String id, String name) {
		// change inquiry indicator
		if(size()==1) set(0,"...",null);
		// index just before the inquiry indicator
		int index = size()-1;
		//add to list, map ID to index
		this.insert(index, name, notchecked);
		devIdToEntries.put(id, new Integer(index));	
	}
	
	public void bluetoothInquiryCompleted(int size) {
		// remove indicator
		this.delete(size()-1);
		
		// remove/add commands
		removeCommand(stop);
		addCommand(back);
		addCommand(rescan);
		
		// start looking for Tetris servers
		if(size>0) {
			btDiscovery.startServiceSearch();
			addCommand(recheck);
			setSelectCommand(connect);
		} else {		
			//remove all items
			deleteAll();
			setSelectCommand(null);
		}
	}
	
	public void bluetoothInquiryTerminated() {
		// same behavior as InquiryCompleted
		bluetoothInquiryCompleted(size()-1);
	}
	
	/* ----------------------------------------------------------------------- */
	public void bluetoothServiceSearchStarted(String id) {
		// Change Icon to Querying
		int index = idToIndex(id);
		set(index, getString(index), querying);		
	}

	public void bluetoothServiceSearchResult(String id, int respCode, String url) {
		int index = idToIndex(id);
		if(respCode == DiscoveryListener.SERVICE_SEARCH_COMPLETED) {
			// save URL
			URLs.put(new Integer(index), url);
			//change icon to online
			set(index, getString(index), online);			
			// change item font style to bold
			changeFont(index,-1,Font.STYLE_BOLD,-1);
		} else {
			//change icon
			set(index, getString(index), offline);
		}
	}
	public void bluetoothServiceSearchCompleted() {
				
	}
	
	public void bluetoothError(String e) {
		midlet.gui.showError(e);
	}

	/* ----------------------------------------------------------------------- */
	private int idToIndex(String id) {
		Object index = devIdToEntries.get(new String(id));
		if (index == null) return -1;
		return ((Integer)index).intValue();
	}

	private void changeFont(int index, int face, int style, int size) {
		Font f = getFont(index);
		
		if(face == -1) face = f.getFace();
		if(style == -1) style = f.getStyle();
		if(size == -1) size = f.getSize();
		
		setFont(index,Font.getFont(face, style, size));
	}

}
