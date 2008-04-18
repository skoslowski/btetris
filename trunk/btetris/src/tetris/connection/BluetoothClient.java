package tetris.connection;

import java.io.*;

import javax.bluetooth.*;
import javax.microedition.io.*;

public class BluetoothClient extends BluetoothConnection {
	
	private final String url;

	public BluetoothClient(String url,BluetoothListener listener) {
		super(listener);
		this.url = url;
	}
	
	protected L2CAPConnection getConnection() throws IOException {
		return (L2CAPConnection)Connector.open(url);
	}

}
