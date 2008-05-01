package tetris.connection;

import java.io.IOException;
import javax.bluetooth.L2CAPConnection;

public abstract class BluetoothConnection extends Thread {

	private static final int WAIT_MILLIS = 250, PING_TICKS = 20;
	
	private int ticksSinceLastEvent = 0;
	private final BluetoothListener listener;
	private L2CAPConnection connection;
	private boolean running = false;
	
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
			running = true;
			byte inBuf[] = new byte[connection.getReceiveMTU()];
			while(running) {
				if(connection.ready()) {
					
					connection.receive(inBuf);
					ticksSinceLastEvent = 0;
					
					if(inBuf[0] != Protocol.PING) {
						byte tmpBuf[] = new byte[inBuf.length];
						System.arraycopy(inBuf, 0, tmpBuf, 0, inBuf.length);
						listener.bluetoothReceivedEvent(tmpBuf);
					}
				} else {
					Thread.sleep(WAIT_MILLIS);
					
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
			
		} catch (Exception e) {
			listener.bluetoothError("Bluetooth Fehler: " + e.getMessage());
			e.printStackTrace();
		}
		running = false;
		listener.bluetoothDisconnected(byPeer);
	}
	
	public synchronized void stop() {
		running = false;
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
