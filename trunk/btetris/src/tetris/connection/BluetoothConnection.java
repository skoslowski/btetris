package tetris.connection;

import java.io.IOException;
import javax.bluetooth.L2CAPConnection;

import tetris.core.Protocol;

public abstract class BluetoothConnection implements Runnable {

	private static final int WAIT_MILLIS = 250, PING_TICKS = 20;
	private volatile Thread connectionThread = null;

	private int ticksSinceLastEvent = 0;
	private final BluetoothListener listener;
	private L2CAPConnection connection;

	protected abstract L2CAPConnection getConnection() throws IOException;	

	public BluetoothConnection(BluetoothListener listener) {
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
						listener.bluetoothReceivedEvent(tmpBuf);
					}
				} else {
					synchronized(this) {
						wait(WAIT_MILLIS);
					}
					if(++ticksSinceLastEvent >= PING_TICKS) {
						send(Protocol.PING);
						ticksSinceLastEvent=0;
					}
				}
			}

			// close Connection			
			connection.close();
			connection=null;

		} catch (IOException e) {
			// connection has been closed by peer
			byPeer = true; 

		} catch (InterruptedException e) {

		} catch (Exception e) {
			listener.bluetoothError("Bluetooth Fehler: " + e.getMessage());
			e.printStackTrace();
		}
		listener.bluetoothDisconnected(byPeer);
	}



	public synchronized void start() {
		if (connectionThread==null) {
			connectionThread = new Thread(this);
			connectionThread.start();
		}	
	}

	public synchronized void stop() {
		if(connectionThread != null) {
			connectionThread = null;
			notify();

		}
	}

	public boolean send(byte b) {
		if (connection == null) return false;
		byte outBuf[] = {b};
		try {
			connection.send(outBuf);

		} catch (IOException e) {
			return false;
		}
		return true;
	}	
	public boolean send(byte outBuf[]) {
		if (connection == null) return false;
		try {
			connection.send(outBuf);

		} catch (IOException e) {
			return false;
		}
		return true;
	}


}
