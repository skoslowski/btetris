package tetris.opponent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import tetris.connection.BluetoothClient;
import tetris.connection.BluetoothServer;
import tetris.connection.BluetoothSocket;
import tetris.connection.Protocol;
import tetris.core.TetrisMIDlet;
import tetris.settings.Settings;

public class BluetoothOpponent 
	extends Opponent
	implements BluetoothSocket.BluetoothListener 
{

	private BluetoothSocket bt = null;
	private TetrisPlayerListener listener = null;
	private int gametype = -1;
	
	public BluetoothOpponent(TetrisPlayerListener listener) {
		this.listener = listener;
	}

	/*----------------------------------------------------------------------*/
	/*---------------------------- BT-Stuff --------------------------------*/
	/*----------------------------------------------------------------------*/

	public void bluetoothConnected() {
		if(gametype==TetrisMIDlet.MULTI_HOST) listener.restartGame(false,-1);
		/*Client waits for server to request "restart" */
	}
	
	public void bluetoothDisconnected(boolean byPeer) {
		listener.stopGame(true);
	}

	public void bluetoothError(String s) {

	}

	public void bluetoothHandleEvent(byte b[]) {
		switch (b[0]) {
		case Protocol.ONE_LINE:
			listener.recieveRows(1);
			break;
		case Protocol.TWO_LINES:
			listener.recieveRows(2);
			break;
		case Protocol.FOUR_LINES:
			listener.recieveRows(4);
			break;
		case Protocol.GAME_HEIHGT:
			if(b.length>1) listener.recieveHeight((int)b[1]);
			break;
		case Protocol.PAUSE_GAME:
			listener.pauseGame(true);
			break;
		case Protocol.UNPAUSE_GAME:
			listener.unpauseGame(true);
			break;
		case Protocol.I_LOST:
			listener.endOfGame(true);
			break;
		case Protocol.RESTART:
			/* sync random */
			long seed=-1;
			if(Settings.getInstance().syncBricks) {
				try {
					ByteArrayInputStream bias = new ByteArrayInputStream(b);
					DataInputStream inputStream = new DataInputStream(bias);
					
					inputStream.readByte();
					seed = inputStream.readLong();
					if(!Settings.getInstance().syncBricks) seed=-1;
					
					inputStream.close();
					bias.close();
				} catch (IOException e) {}
			}
			/* restart game */
			listener.restartGame(true,seed);
			break;
		}
	}		
	
	/*----------------------------------------------------------------------*/
	/*----------------------------------------------------------------------*/

	public synchronized void startGame(int gametype) {
		this.gametype = gametype;
		if (gametype == TetrisMIDlet.MULTI_HOST) {
			bt = new BluetoothServer(this);
			bt.start();
		}
	}
	public synchronized void startGame(int gametype, String url) {
		this.gametype = gametype;
		if (gametype == TetrisMIDlet.MULTI_CLIENT) {
			bt = new BluetoothClient(url,this);
			bt.start();
		}
	}	

	public synchronized void stopGame(boolean byPeer) {
		if(bt instanceof BluetoothSocket) bt.stop();
	}	
	
	public void restartGame(boolean byPeer, long seed) {
		/* Transmit new seed */	
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream outputStream = new DataOutputStream(baos);	
			
			outputStream.writeByte(Protocol.RESTART);
			outputStream.writeLong(seed);
			outputStream.flush();
			bt.send(baos.toByteArray()); 
		
			outputStream.close();
			baos.close();
			
		} catch(IOException e) {
			bt.send(Protocol.RESTART);
		}
	}

	public void recieveHeight(int gameheight) {
		byte buf[] = {Protocol.GAME_HEIHGT,(byte)gameheight};
		bt.send(buf);	
	}

	public void recieveRows(int count) {
		byte code = (count==4)?Protocol.FOUR_LINES:(count==2)?Protocol.TWO_LINES:Protocol.ONE_LINE;
		bt.send(code);		
	}

	public void endOfGame(boolean byPeer) {
		bt.send(Protocol.I_LOST);
	}

	public void pauseGame(boolean byPeer) {
		bt.send(Protocol.PAUSE_GAME);
	}

	public void unpauseGame(boolean byPeer) {
		bt.send(Protocol.UNPAUSE_GAME);	
	}

}
