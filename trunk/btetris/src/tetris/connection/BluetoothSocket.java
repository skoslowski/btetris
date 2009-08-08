package tetris.connection;

import java.io.IOException;
import javax.bluetooth.L2CAPConnection;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.BluetoothStateException;

public abstract class BluetoothSocket implements Runnable {

	public interface BluetoothListener {
		void bluetoothConnected();
		void bluetoothHandleEvent(byte b[]);
		void bluetoothDisconnected(boolean wasRunning);	
		void bluetoothError(String e);
	}

	private static final int WAIT_MILLIS = 250, PING_TICKS = 20;
	private volatile Thread connectionThread = null;

	private int ticksSinceLastEvent = 0;
	private final BluetoothListener listener;
	private L2CAPConnection connection;

	protected abstract L2CAPConnection getConnection() throws IOException;	

	public BluetoothSocket(BluetoothListener listener) {
		this.listener = listener;
	}

	public void run() {
		boolean byPeer = false;
		try {
			//Create Connection
			connection = getConnection();
			listener.bluetoothConnected();

			// Receive Data
			byte inBuf[] = new byte[connection.getReceiveMTU()];
			while(Thread.currentThread() == connectionThread) {
				if(connection.ready()) {

					connection.receive(inBuf);
					ticksSinceLastEvent = 0;

					if(inBuf[0] != Protocol.PING) {
						byte tmpBuf[] = new byte[inBuf.length];
						System.arraycopy(inBuf, 0, tmpBuf, 0, inBuf.length);
						listener.bluetoothHandleEvent(tmpBuf);
					}
				} else {
					if(++ticksSinceLastEvent >= PING_TICKS) {
						send(Protocol.PING);
						ticksSinceLastEvent=0;
					}
					synchronized(this) {
						wait(WAIT_MILLIS);
					}
				}
			}

			// close Connection			
			connection.close();
			connection=null;

		} catch (IOException e) {
			// connection has been closed by peer
			byPeer = true; 

		} catch (NullPointerException e) {
			byPeer = true; 

		} catch (InterruptedException e) {

		}
		listener.bluetoothDisconnected(byPeer);
	}



	public synchronized void start() {
		if (connectionThread==null) {
			connectionThread = new Thread(this);
			connectionThread.start();
		}	
	}

	public synchronized void stop() throws NullPointerException {
		if(connection != null) {
			try {
				connection.close();
			} catch (IOException e) {}
		}
		if(connectionThread!=null) {
			connectionThread.interrupt();
			connectionThread = null;
		}
	}

	public boolean send(byte b) {
		if (connection == null || connectionThread==null) return false;

		byte outBuf[] = {b};
		try {
			connection.send(outBuf);

		} catch (Exception e) {
			return false;
		}
		return true;
	}	

	public boolean send(byte outBuf[]) {
		if (connection == null || connectionThread==null) return false;

		try {
			connection.send(outBuf);

		} catch (Exception e) {
			return false;
		}
		return true;
	}



	public static boolean isBluetoothOn() {
		try {
			LocalDevice.getLocalDevice();

		} catch (BluetoothStateException e) {
			return false; 
		}
		return true;
	}
}
