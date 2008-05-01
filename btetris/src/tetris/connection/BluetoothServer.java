package tetris.connection;

import java.io.*;

import javax.bluetooth.*;
import javax.microedition.io.*;

public class BluetoothServer extends BluetoothConnection {

	private boolean restoreDiscoverableModeOnExit;
	private int initialDiscoverableMode;
	private final String url;
	private L2CAPConnectionNotifier server = null;

	public BluetoothServer(BluetoothListener listener) {
		super(listener);

		try {
			LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			listener.bluetoothError("Could not access Bluetooth");
		}
		
		try{
			initialDiscoverableMode = LocalDevice.getLocalDevice().getDiscoverable();
			restoreDiscoverableModeOnExit =
				LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
		
		} catch (BluetoothStateException e) {
			restoreDiscoverableModeOnExit = false;
		}

		url = "btl2cap://localhost:" + BluetoothDiscovery.UUID + ";authorize=false;encrypt=false;name=Tetris";
	}

	protected L2CAPConnection getConnection() throws IOException {

		server = (L2CAPConnectionNotifier) Connector.open(url);
		L2CAPConnection connection = server.acceptAndOpen();
		server.close();
		server=null;
		return connection;
	}

	public synchronized void stop() {
		if(server!=null) {
			try {
				server.close();
			} catch(IOException e) {}

		} else {
			super.stop();
		}
		
		try {
			if(restoreDiscoverableModeOnExit)
				LocalDevice.getLocalDevice().setDiscoverable(initialDiscoverableMode);
		} catch (BluetoothStateException e) {}
		
	}

}
