package tetris.opponent;

import java.util.Random;
import tetris.tetris.TetrisField;

public class VirtualOpponent extends Opponent {
	
	private final Random random = new Random(System.currentTimeMillis());
	
	private TetrisPlayerListener listener;
	
	private int gameHeight=0, rowsNext=0, rowsTotal=0;
	private float rowsDist[] = {65,20,7,8};
	
	public VirtualOpponent(TetrisPlayerListener listener) {
		this.listener = listener;
	}
	
	private class BuildThread extends Thread {
		public boolean running = true;
		public void run() {
			running = true;
			while(running) {
				try {					
					gameHeight++;
					
					int timeTick = Math.max(800 - 20*rowsTotal/10,200);
					
					synchronized(this) {
						wait(timeTick);
					}
				} catch (InterruptedException e) {}
			}
		}
	}
	private volatile BuildThread buildThread = null;;
	
	private class CompletionThread extends Thread {
		public boolean running = true;
		public void run() {
			running = true;
			while(running) {
				try {	
					gameHeight -= rowsNext;
					rowsTotal += rowsNext;
					// Send Rows

					// Wait Random time + figure out what to send next
					// --> Relation?
					long timeTick = 1000 * (random.nextInt(5)+random.nextInt(5)+5);

					synchronized(this) {
						wait(timeTick);
					}
					
					if(rowsNext>1) listener.recieveRows((rowsNext==4)?4:(rowsNext==3)?2:1);
					
					rowsNext = Math.max(gameHeight, getNextNoOfRows(rowsDist));					
					
				} catch (InterruptedException e) {
					gameHeight--;
				}
			}
			System.out.println("done");
		}
	}
	private volatile CompletionThread completionThread = null;
	
	private void checkLost() {
		//if(gameHeight >= TetrisField.ROWS) listener.endOfGame(true);
	}
	
	/* Return 1,2,3,4 with PDF dist */
	private int getNextNoOfRows(float dist[]) {
		if(dist == null || dist.length != 4) return -1;
		
		float vert[] = {dist[0],dist[0]+dist[1],dist[0]+dist[1]+dist[2],dist[0]+dist[1]+dist[2]+dist[3]};
		float rand = random.nextFloat() * (dist[0]+dist[1]+dist[2]+dist[3]);
		
		int i = 0;
		while(i<=3 && rand >= vert[i]) 
			i++;
		
		return i+1;
	}

	/*----------------------------------------------------------------------*/
	/*----------------------------------------------------------------------*/


	public synchronized void startGame(int gametype) {
		gameHeight=0;
		rowsNext = getNextNoOfRows(rowsDist);
		rowsTotal = 0;
		restartGame(false,-1);
	}
	
	public void recieveHeight(int gameheight) {

	}

	public void recieveRows(int count) {
		gameHeight+=count;
		checkLost();	
	}
	
	public void pauseGame(boolean byPeer) {
		stopGame(byPeer);		
	}

	public void unpauseGame(boolean byPeer) {
		restartGame(false, -1);	
	}
	
	public void endOfGame(boolean byPeer) {
		stopGame(byPeer);	
	}

	public synchronized void restartGame(boolean byPeer, long seed) {
		if(buildThread==null) {
			buildThread = new BuildThread();
			buildThread.start();
		}
		
		if(completionThread == null) {
			completionThread = new CompletionThread();
			completionThread.start();
		}	
	}

	public synchronized void stopGame(boolean byPeer) throws NullPointerException {
		if(buildThread!=null) {
			buildThread.running=false;
			buildThread.interrupt();
			try { buildThread.join(); } catch (InterruptedException e) {}
			buildThread=null;
		}
		
		if(completionThread!=null) {
			completionThread.running=false;
			completionThread.interrupt();
			try { completionThread.join(); } catch (InterruptedException e) {}
			completionThread = null;
		}
	}

	
}
