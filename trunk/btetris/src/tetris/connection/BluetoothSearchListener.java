package tetris.connection;

import java.util.Hashtable;

public interface BluetoothSearchListener {
	
	void bluetoothSearchComplete(Hashtable servers);
	
	void bluetoothError(String e);
	
	void bluetoothSearchLog(String s);
}
