package tetris.connection;

public interface BluetoothListener {
	
	void bluetoothConnected();
	
	void bluetoothReceivedEvent(byte b[]);
	
	void bluetoothDisconnected(boolean wasRunning);
	
	void bluetoothError(String e);
	
}
