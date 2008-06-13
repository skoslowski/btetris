package tetris.connection;

public interface BluetoothSearchListener {
	void bluetoothInquiryDeviceDiscoved(String name);
	void bluetoothInquiryTerminated();
	void bluetoothInquiryCompleted(String[] servers);
	
	void bluetoothError(String e);
	
	
	void bluetoothServiceSearchStarted(String deviceName, int transID);
	void bluetoothServiceSearchResult (int transID, int respCode, String url);
	
}
