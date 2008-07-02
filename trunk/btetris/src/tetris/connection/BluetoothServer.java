package tetris.connection;

import java.io.*;

import javax.bluetooth.*;
import javax.microedition.io.*;

public class BluetoothServer extends BluetoothSocket {

	private boolean restoreDiscoverableMode;
	private int initialDiscoverableMode;
	private final String url;
	private L2CAPConnectionNotifier server = null;

	public BluetoothServer(BluetoothListener listener) {
		super(listener);
		
		try {
			initialDiscoverableMode = LocalDevice.getLocalDevice().getDiscoverable();
			restoreDiscoverableMode =
				LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
		
		} catch (BluetoothStateException e) {
			restoreDiscoverableMode = false;
		}

		url = "btl2cap://localhost:" + BluetoothDiscovery.UUID + ";authorize=false;encrypt=false;name=Tetris";
	}

	protected L2CAPConnection getConnection() throws IOException {

		server = (L2CAPConnectionNotifier) Connector.open(url);
		L2CAPConnection connection = server.acceptAndOpen();
		server.close();
		server=null;
		
		try {
			if(restoreDiscoverableMode) {
				LocalDevice.getLocalDevice().setDiscoverable(initialDiscoverableMode);
				restoreDiscoverableMode=false;
			}
		} catch (BluetoothStateException e) {}	
		
		return connection;
	}

	public synchronized void stop() throws NullPointerException {
		if(server!=null)
			try {
				server.close();
			} catch(IOException e) {}
	
		super.stop();	
		
		try {
			if(restoreDiscoverableMode) {
				LocalDevice.getLocalDevice().setDiscoverable(initialDiscoverableMode);
				restoreDiscoverableMode=false;
			}
		} catch (BluetoothStateException e) {}	
	}

}
