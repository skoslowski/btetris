package tetris.connection;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import javax.bluetooth.*;

public class BluetoothDiscovery implements DiscoveryListener {
	
	public interface BluetoothServiceSearchListener {
		void bluetoothServiceSearchStarted(String id);
		void bluetoothServiceSearchResult (String id, int respCode, String url);
		void bluetoothError(String e);
	}
	public interface BluetoothInquiryListener {
		void bluetoothInquiryDeviceDiscoved(String name);
		void bluetoothInquiryTerminated();
		void bluetoothInquiryCompleted(int size);
		void bluetoothError(String e);
	}
	private BluetoothServiceSearchListener serviceSearchListener=null;
	private BluetoothInquiryListener inquiryListener=null;
	
	static final String UUID = "7219891290311a92282f0014a7082f09";
	private final int attrSet[] = null;
	private final UUID uuidSet[] = {new UUID(0x0100), new UUID(UUID, false)};
	
	private final int maxServiceSearches;
	
	private DiscoveryAgent agent;
	private Vector currentRemoteDevices = new Vector();
	private Hashtable transIDs = new Hashtable();
	private Hashtable serviceURLs = new Hashtable();

	public BluetoothDiscovery() {
        String p = LocalDevice.getProperty("bluetooth.sd.trans.max");
        maxServiceSearches = (p==null)?1:Integer.parseInt(p);

		try {
			agent = LocalDevice.getLocalDevice().getDiscoveryAgent();
		} catch(BluetoothStateException e) {
			//listener.bluetoothError("Initiating Inquiry failed");
			e.printStackTrace();
		}
	}

	public boolean checkForKnownServers() {
		currentRemoteDevices.removeAllElements();
		boolean cached= getDevices(DiscoveryAgent.CACHED);
		if(!cached) {
			boolean preknown = getDevices(DiscoveryAgent.PREKNOWN);
			if(!preknown) return false;
		}
		return true;
	}
	
	public Hashtable getServers() {
		if(currentRemoteDevices.size() == 0) return null;
		
		Hashtable servers = new Hashtable();
		for(Enumeration e = currentRemoteDevices.elements();e.hasMoreElements();) {
			RemoteDevice rd = (RemoteDevice)e.nextElement();
			servers.put(rd.getBluetoothAddress(), getFriendlyName(rd));
		}
		return servers;
	}
	
	private boolean getDevices(int type) {
		RemoteDevice devs[] = agent.retrieveDevices(type);
		if(devs == null || devs.length==0) return false;
		
		for(int i = 0;i<devs.length;i++) {
			if(!currentRemoteDevices.contains(devs[i]))
				currentRemoteDevices.addElement(devs[i]);
		}
		return true;
	}
	
	/* ------------------ Inquiry Functions ------------------ */
	
	public void startInquiry(BluetoothInquiryListener obj) {
		inquiryListener = obj;
		currentRemoteDevices.removeAllElements();
		try {
			agent.startInquiry(DiscoveryAgent.GIAC, this);

		} catch (BluetoothStateException e) {
			inquiryListener.bluetoothError(e.getMessage());
		}
	}
  	
	public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass cod) {
		boolean isPhone = cod.getMajorDeviceClass()==0x200;
		if (isPhone && !currentRemoteDevices.contains(remoteDevice)) {
			currentRemoteDevices.addElement(remoteDevice);
			
			String devName = getFriendlyName(remoteDevice);
			inquiryListener.bluetoothInquiryDeviceDiscoved(devName);
		}

	}

	public void inquiryCompleted(int discType) {		
		switch(discType) {

		case INQUIRY_COMPLETED:				
			inquiryListener.bluetoothInquiryCompleted(currentRemoteDevices.size());
			break;

		case INQUIRY_TERMINATED:
			inquiryListener.bluetoothInquiryTerminated();
			break;

		case INQUIRY_ERROR:
			inquiryListener.bluetoothError("Inquiry error\n");

		}
	}
	
	public void stopInquiry() {
		agent.cancelInquiry(this);
	}

	/* ------------------ Service Searches ------------------ */
	
		
	public void setBluetoothServiceSearchListener(BluetoothServiceSearchListener obj) {
		serviceSearchListener = obj;
	}
	
	public void startServiceSearch() {
		if(serviceSearchListener==null) return;
		
		try {
			while(transIDs.size()<maxServiceSearches && !currentRemoteDevices.isEmpty()) {
				RemoteDevice rd =(RemoteDevice)currentRemoteDevices.firstElement();
				int transID = agent.searchServices(attrSet, uuidSet, rd, this);
				transIDs.put(new Integer(transID), rd.getBluetoothAddress());
				
				serviceSearchListener.bluetoothServiceSearchStarted(rd.getBluetoothAddress());
				currentRemoteDevices.removeElement(rd);
			}
			
		} catch (Exception e) {
			serviceSearchListener.bluetoothError(e.getMessage());
			e.printStackTrace();
		}	
		System.out.println("ServiceSearch started.");
	}


	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		for (int i = 0; i<servRecord.length; i++) {
			String serverURL = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			serviceURLs.put(new Integer(transID),serverURL);
		}
		System.out.println("ServiceRecord found.");
	}

	public void serviceSearchCompleted(int transID, int respCode) {
		String btAddr = (String)transIDs.get(new Integer(transID));
		transIDs.remove(new Integer(transID));
		
		System.out.println("ServiceSearch done.");
		if(respCode!=SERVICE_SEARCH_TERMINATED) {
			String url = (respCode==SERVICE_SEARCH_COMPLETED)?(String)serviceURLs.get(new Integer(transID)):"";
			serviceSearchListener.bluetoothServiceSearchResult(btAddr, respCode, url);
			startServiceSearch();
		}
	}
	
	public void stopServiceSearch() {
	     for (Enumeration e = transIDs.elements() ; e.hasMoreElements() ;)
	    	 agent.cancelServiceSearch(((Integer)e.nextElement()).intValue());
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