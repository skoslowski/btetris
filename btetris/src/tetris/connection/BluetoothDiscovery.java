package tetris.connection;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import javax.bluetooth.*;

public class BluetoothDiscovery implements DiscoveryListener {

	public interface BluetoothServerListener {
		void bluetoothInquiryDeviceDiscoved(String id, String name);
		void bluetoothInquiryTerminated();
		void bluetoothInquiryCompleted(int size);

		void bluetoothServiceSearchStarted(String id);
		void bluetoothServiceSearchResult (String id, int respCode, String url);
		void bluetoothServiceSearchCompleted();

		void bluetoothError(String e);
	}
	private BluetoothServerListener listener=null;

	static final String UUID = "7219891290311a92282f0014a7082f09";
	private final int attrSet[] = null;
	private final UUID uuidSet[] = {new UUID(0x0100), new UUID(UUID, false)};

	private final int maxServiceSearches;
	private final Object serviceSearchMonitor = new Object();
	private boolean terminateServiceSearch;

	private DiscoveryAgent agent;
	private final Vector remoteDevices = new Vector();  //devices from Cache/PreKnown/Inquiry
	private final Hashtable transIDs = new Hashtable();        //holds the transID for each Device
	private final Hashtable serviceURLs = new Hashtable();     //cache for ServiceURLS (indexed by transID)

	public BluetoothDiscovery(BluetoothServerListener listener) {
		this.listener = listener;

		// get max number of parallel service searches
		String p = LocalDevice.getProperty("bluetooth.sd.trans.max");
		maxServiceSearches = (p==null)?1:Integer.parseInt(p);

		try {
			agent = LocalDevice.getLocalDevice().getDiscoveryAgent();
		} catch(BluetoothStateException e) {
			listener.bluetoothError("Initiating Discovery failed");
			e.printStackTrace();
		}
	}

	/* to receive devices */
	public Hashtable getDevices() {
		remoteDevices.removeAllElements();

		// check if there are cached oder preKnown devices
		boolean cached= loadDevices(DiscoveryAgent.CACHED);
		if(!cached) {
			boolean preknown = loadDevices(DiscoveryAgent.PREKNOWN);
			if(!preknown || remoteDevices.size()==0) return null;
		}

		// return devices names; btAdress used as IDs
		Hashtable devices = new Hashtable();
		for(Enumeration e = remoteDevices.elements();e.hasMoreElements();) {
			RemoteDevice rd = (RemoteDevice)e.nextElement();
			devices.put(rd.getBluetoothAddress(), getFriendlyName(rd));
		}
		return devices;
	}

	private boolean loadDevices(int type) {
		// get devices
		RemoteDevice devices[] = agent.retrieveDevices(type);
		// return false if no device where found.
		if(devices == null || devices.length==0) return false;
		// add devices to list
		for(int i = 0;i<devices.length;i++) {
			if(!remoteDevices.contains(devices[i]))
				remoteDevices.addElement(devices[i]);
		}
		return true;
	}

	/* ------------------ Inquiry Functions ------------------ */

	public void startInquiry() {
		remoteDevices.removeAllElements();
		try {
			agent.startInquiry(DiscoveryAgent.GIAC, this);

		} catch (BluetoothStateException e) {
			listener.bluetoothError("Inquiry could not be started");
		}
	}

	public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass cod) {
		// only add PhoneDevices
		boolean isPhone = cod.getMajorDeviceClass()==0x200;
		if (isPhone && !remoteDevices.contains(remoteDevice)) {
			remoteDevices.addElement(remoteDevice);
			// notify listener
			String devName = getFriendlyName(remoteDevice);
			String id = remoteDevice.getBluetoothAddress();
			listener.bluetoothInquiryDeviceDiscoved(id, devName);
		}

	}

	public void inquiryCompleted(int discType) {		
		switch(discType) {

		case INQUIRY_COMPLETED:			
			listener.bluetoothInquiryCompleted(remoteDevices.size());
			break;

		case INQUIRY_TERMINATED:
			listener.bluetoothInquiryTerminated();
			break;

		case INQUIRY_ERROR:
			listener.bluetoothError("Inquiry error");

		}
	}

	public void stopInquiry() {
		agent.cancelInquiry(this);
	}

	/* ------------------ Service Searches ------------------ */

	public void startServiceSearch() {
		new Thread() {
			public void run() {
				terminateServiceSearch = false;
				try {
					Enumeration deviceEnum = remoteDevices.elements();
					while(deviceEnum.hasMoreElements() && !terminateServiceSearch) {
						synchronized(serviceSearchMonitor) {
							try {	
								while(transIDs.size()<maxServiceSearches) {
									// get device
									RemoteDevice rd =(RemoteDevice)deviceEnum.nextElement();
									// start Search
									int transID = agent.searchServices(attrSet, uuidSet, rd, BluetoothDiscovery.this);				
									// map transID to device
									transIDs.put(new Integer(transID), rd.getBluetoothAddress());
									listener.bluetoothServiceSearchStarted(rd.getBluetoothAddress());
								}
							} catch (BluetoothStateException e) {
								listener.bluetoothError(e.getMessage());
								e.printStackTrace();
							}
							serviceSearchMonitor.wait();
						}
					}
				} catch(InterruptedException e) {}
				listener.bluetoothServiceSearchCompleted();
			}
		}.start();
	}


	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		for (int i = 0; i<servRecord.length; i++) {
			// Get URL for Connection
			String serverURL = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			// cache it
			serviceURLs.put(new Integer(transID),serverURL);
		}
	}

	public void serviceSearchCompleted(int transID, int respCode) {
		String btAddr = (String)transIDs.get(new Integer(transID));
		transIDs.remove(new Integer(transID));

		if(respCode!=SERVICE_SEARCH_TERMINATED) {
			//check if serviceRecord/URL was found.
			String url = (respCode==SERVICE_SEARCH_COMPLETED)?(String)serviceURLs.get(new Integer(transID)):"";
			listener.bluetoothServiceSearchResult(btAddr, respCode, url);
			// next!
			synchronized(serviceSearchMonitor) {
				serviceSearchMonitor.notifyAll();
			}
		}
	}

	public void stopServiceSearch() {
		terminateServiceSearch=true;
		synchronized(serviceSearchMonitor) {
			serviceSearchMonitor.notifyAll();
		}
	}

	/* ------------------ Misc ------------------ */

	private String getFriendlyName(RemoteDevice rd) {
		String serverName="";
		try {
			serverName = rd.getFriendlyName(false);
		} catch(java.io.IOException e) {
			serverName = rd.getBluetoothAddress();
		}
		return serverName; 
	}


}