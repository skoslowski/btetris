package tetris.ui;

import javax.microedition.lcdui.*;

import tetris.core.TetrisMIDlet;
import tetris.connection.*;

class ServerInquiry 
	extends Form 
	implements CommandListener, BluetoothDiscovery.BluetoothInquiryListener 
{

	private final TetrisMIDlet midlet;
	private final Command retry, abort, back, next;
	private BluetoothDiscovery btDiscovery;
	
	public ServerInquiry(TetrisMIDlet midlet, BluetoothDiscovery btDiscovery) {
		super("Server inquiry");
		this.midlet=midlet;
		this.btDiscovery = btDiscovery;
		
		abort=new Command("Stop", Command.STOP, 1);
		retry=new Command("Retry", Command.OK, 2);
		next=new Command("Continue", Command.OK, 1);
		back=new Command("Back", Command.EXIT, 2);

		addCommand(abort);
		setCommandListener(this);
		
		addToLog("Inquiry started.\n");		
		btDiscovery.startInquiry(this);

	}
	
	private void addToLog(String msg) {
		StringItem si = new StringItem(null,msg);
		si.setFont(Font.getFont(Font.getDefaultFont().getFace(), Font.getDefaultFont().getStyle(), Font.SIZE_SMALL));
		this.append(si);
	}

	public void commandAction(Command c, Displayable d) {

		if(c == back) {
			midlet.gui.showMainMenu();

		} else if(c == abort) {
			btDiscovery.stopInquiry();
			addToLog("\nTerminating...\n");
			removeCommand(abort);
			addCommand(back);
			
		} else if(c == retry) {
			deleteAll();
			removeCommand(retry);
			addCommand(abort);
			btDiscovery.startInquiry(this);
			addToLog("Inquiry started.\n");
		
		} else if(c==next) {

			midlet.gui.showServerServiceSearch();
			
		}
	}
	
	public void bluetoothError(String e) {
		this.append("Error: "+e+"\n");
	}

	public void bluetoothInquiryCompleted(int found) {
		addToLog("Inquiry completed (found: "+found+" ).\n \n");
		removeCommand(abort);
		
		if(found > 0) {
			addCommand(next);
			addCommand(retry);
		} else {
			addCommand(retry);
		}
		
		addCommand(back);		
	}

	public void bluetoothInquiryDeviceDiscoved(String name) {
		addToLog(" - "+name+"\n");
	}

	public void bluetoothInquiryTerminated() {
		addToLog("Inquiry terminated.\n");		
	}

}