package tetris.connection;

import java.util.Vector;
import java.util.Hashtable;

import javax.bluetooth.*;

public class BluetoothDiscovery implements DiscoveryListener {

	private final BluetoothSearchListener listener;

	static final String UUID = "7219891290311a92282f0014a7082f09";
	//static final int SERVER_VERSION_ID = 0x5432;
	//private final int attrSet[] = {Bluetooth.SERVER_VERSION_ID};
	private final int attrSet[] = null;
	private final UUID uuidSet[] = {new UUID(0x0100), new UUID(UUID, false)};

	private DiscoveryAgent agent;
	private Vector unsearchedRemoteDevices = new Vector();
	private int transID = -1;
	private Hashtable servers = new Hashtable();

	public BluetoothDiscovery(BluetoothSearchListener listener) {
		this.listener = listener;
		try {
			agent = LocalDevice.getLocalDevice().getDiscoveryAgent();
		} catch(BluetoothStateException e) {
			listener.bluetoothError("Initiating Inquiry failed");
		}
	}

	public void start() {

		unsearchedRemoteDevices.removeAllElements();
		servers.clear();

		try {
			agent.startInquiry(DiscoveryAgent.GIAC, this);

			listener.bluetoothSearchLog("Inquiry started...\n");

		} catch (BluetoothStateException e) {
			listener.bluetoothSearchLog(e.getMessage());
		}
	}

	public void stop() {
		listener.bluetoothSearchLog("Terminating...\n");

		agent.cancelInquiry(this);
		agent.cancelServiceSearch(transID);
	}

	public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass cod) {
		boolean isPhone = true || cod.getMajorDeviceClass()==0x200;

		if (isPhone && !unsearchedRemoteDevices.contains(remoteDevice)) {
			unsearchedRemoteDevices.addElement(remoteDevice);

			listener.bluetoothSearchLog(" - "+getFriendlyName(remoteDevice)+"\n");
		}

	}

	public void inquiryCompleted(int discType) {		
		switch(discType) {

		case INQUIRY_COMPLETED:
			listener.bluetoothSearchLog("Inquiry completed (found: "+unsearchedRemoteDevices.size()+")\n \n");

			if(unsearchedRemoteDevices.isEmpty() && transID==-1) {
				listener.bluetoothSearchLog("\nDone!\n");
				listener.bluetoothSearchComplete(servers);
			} else {
				listener.bluetoothSearchLog("Searching for Games...\n");
				startServiceSearch((RemoteDevice)unsearchedRemoteDevices.firstElement());
			}
			break;

		case INQUIRY_TERMINATED:
			listener.bluetoothSearchLog("Inquiry terminated\n");
			break;

		case INQUIRY_ERROR:
			listener.bluetoothError("Inquiry error\n");

		}
	}

	private void startServiceSearch(RemoteDevice rd) {
		try {
			listener.bluetoothSearchLog(" - "+getFriendlyName(rd)+":");
			unsearchedRemoteDevices.removeElement(rd);
			transID = agent.searchServices(attrSet, uuidSet, rd, this);

		} catch (Exception e) {
			listener.bluetoothError(e.getMessage());
			e.printStackTrace();
		}	
	}

	private String getFriendlyName(RemoteDevice rd) {
		String serverName="";
		try {
			serverName = rd.getFriendlyName(false);
		} catch(java.io.IOException e) {
			serverName = rd.getBluetoothAddress();
		}
		return serverName; 
	}

	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		System.out.println("Sevice record found");

		for (int i = 0; i<servRecord.length; i++) {
			String serverName=getFriendlyName(servRecord[i].getHostDevice());
			String serverURL = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);

//			listener.bluetoothSearchLog("...found a bTetris Server.\n");
			servers.put(serverName,new String(serverURL));
		}
	}

	public void serviceSearchCompleted(int transID, int respCode) {
		transID=-1;
		if(respCode==SERVICE_SEARCH_TERMINATED) {
			listener.bluetoothSearchLog("\n...Search terminated\n");
		} else if(respCode==SERVICE_SEARCH_ERROR) {
			listener.bluetoothError("Search Error");
		} else {
			if(respCode==SERVICE_SEARCH_DEVICE_NOT_REACHABLE) {
				listener.bluetoothSearchLog("failure\n");
			} else if(respCode==SERVICE_SEARCH_NO_RECORDS) {
				listener.bluetoothSearchLog("no\n");
			} else if(respCode==SERVICE_SEARCH_COMPLETED) {
				listener.bluetoothSearchLog("yes\n");
			}
			
			if(unsearchedRemoteDevices.isEmpty()) {
				listener.bluetoothSearchLog("Search completed (found: "+servers.size()+")\n \nDone!\n");
				listener.bluetoothSearchComplete(servers);
			} else {
				startServiceSearch((RemoteDevice)unsearchedRemoteDevices.firstElement());
			}
		}
	}


}