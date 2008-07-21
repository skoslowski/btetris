package tetris.connection;

import javax.microedition.lcdui.*;
import javax.bluetooth.DiscoveryListener;
import java.util.Enumeration;
import java.util.Hashtable;

import tetris.core.TetrisMIDlet;

public class ServerSearch 
extends List 
implements CommandListener, BluetoothDiscovery.BluetoothServerListener 
{
	private final TetrisMIDlet midlet;

	private final Command connect, back, rescan, recheck,stop;
	
	private static final int STATUS_ONLINE = 3, STATUS_OFFLINE = 2, STATUS_CHECKING = 1, STATUS_NOTCHECKED = 0;
	private final Image icons[];
	private final String status[]; 
	
	private final BluetoothDiscovery btDiscovery;
	private Hashtable devIdToEntries;
	private Hashtable URLs;

	public ServerSearch(TetrisMIDlet midlet) {
		super("Serverlist", List.IMPLICIT);
		setFitPolicy(Choice.TEXT_WRAP_ON);
		
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
		icons = new Image[4];
		icons[STATUS_NOTCHECKED] = TetrisMIDlet.createImage("/bt.png"); 
		icons[STATUS_CHECKING] = TetrisMIDlet.createImage("/nw.png");
		icons[STATUS_OFFLINE] = TetrisMIDlet.createImage("/delete.png");
		icons[STATUS_ONLINE]  = TetrisMIDlet.createImage("/user.png");
		//Status text
		status = new String[4];
		status[STATUS_NOTCHECKED] = "not checked";
		status[STATUS_CHECKING] = "checking";
		status[STATUS_OFFLINE] = "offline";
		status[STATUS_ONLINE] = "online";
	}
	
	public void init() {
		// Check for Devices0
		Hashtable devices = btDiscovery.getDevices();

		if(devices == null) {
			// Look for Devices
			startInquiry();
		} else {
			for(Enumeration e = devices.keys();e.hasMoreElements();) {
				// Extract info
				String id = (String)e.nextElement();
				String name = (String)devices.get(id);
				//add to list
				int index = append(name,null);
				changeStatus(index,STATUS_NOTCHECKED);
				//map ID to index	
				devIdToEntries.put(id,new Integer(index));
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
			btDiscovery.stopServiceSearch();
			midlet.gui.showMainMenu();
		
		} else if(c == stop) {
			btDiscovery.stopInquiry();
			
		} else if(c == rescan) {
			btDiscovery.stopServiceSearch();
			startInquiry();

		} else if(c== recheck) {
			//reset icons
			for(int i=0;i<size();i++) changeStatus(i,STATUS_NOTCHECKED);;
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
				
				midlet.gui.showMultiplayerWaiting(false);
				
				btDiscovery.stopServiceSearch();
				
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
		int index = append("...",null);
		changeFont(index,-1,Font.STYLE_ITALIC,Font.SIZE_SMALL);
	}
	
	public void bluetoothInquiryDeviceDiscoved(String id, String name) {
		// index just before the inquiry indicator
		int index = size()-1;
		//add to list, map ID to index
		this.insert(index, name, null);
		changeStatus(index,STATUS_NOTCHECKED);
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
		changeStatus(index,STATUS_CHECKING);	
	}

	public void bluetoothServiceSearchResult(String id, int respCode, String url) {
		int index = idToIndex(id);
		if(respCode == DiscoveryListener.SERVICE_SEARCH_COMPLETED) {
			// save URL
			URLs.put(new Integer(index), url);
			//change icon to online
			changeStatus(index,STATUS_ONLINE);				
			// change item font style to bold
			changeFont(index,-1,Font.STYLE_BOLD,-1);
		} else {
			//change icon
			changeStatus(index,STATUS_OFFLINE);	
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

	private void changeStatus(int index, int newStatus) {
		String name = getString(index);
		int position = name.indexOf("\n");
		
		if (position != -1) name = name.substring(0,position);
		name += "\n"+status[newStatus];
		set(index,name,icons[newStatus]);
	}
	
	private void changeFont(int index, int face, int style, int size) {
		Font f = getFont(index);
		
		if(face == -1) face = f.getFace();
		if(style == -1) style = f.getStyle();
		if(size == -1) size = f.getSize();
		
		setFont(index,Font.getFont(face, style, size));
	}

}
