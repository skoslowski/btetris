package tetris.opponent;

import java.util.Random;
import tetris.tetris.TetrisField;

public class VirtualOpponent {
	
	private final Random random = new Random(System.currentTimeMillis());
	
	public interface VirtOpListener {
		public void recieveRows(int count);
		public void gameOver();
		public void recieveOpponentHeight(int gameheight);
	}
	private VirtOpListener listener;
	
	private int gameHeight=0, rowsNext=0, rowsTotal=0;
	private float rowsDist[] = {4,3,2,1};
	
	public VirtualOpponent(VirtOpListener listener) {
		this.listener = listener;
	}
		
	public void reset() {
		gameHeight=0;
		rowsNext = getNextNoOfRows(rowsDist);
		rowsTotal = 0;		
	}
	
	private volatile Thread buildThread = new Thread() {
		public void run() {
			try {	
				while(true) {				
					gameHeight++;
					
					int timeTick = Math.max(800 - 20*rowsTotal/10,200);
					
					synchronized(this) {
						wait(timeTick);
					}
				}
			} catch (InterruptedException e) {}
		}
	};
	
	private volatile Thread completionThread = new Thread() {
		public void run() {
			try {	
				while(true) {
					gameHeight -= rowsNext;
					rowsTotal += rowsNext;
					// Send Rows
					if(rowsNext>1) listener.recieveRows((rowsNext==4)?4:(rowsNext==3)?2:1);
					
					// Calculate some sort of Height --> How?
					// Send new Height
					// Lost? Won?
					
					rowsNext = Math.max(gameHeight, getNextNoOfRows(rowsDist));
					// Wait Random time + figure out what to send next
					// --> Relation?


					synchronized(this) {
						wait(111);
					}
				}
			} catch (InterruptedException e) {}
		}
	};
	
	public synchronized void start() {
		buildThread.start();
		completionThread.start();
	}

	public synchronized void stop() throws NullPointerException {
		buildThread.interrupt();
		completionThread.interrupt();
	}


	public void addToRows(int count) {
		gameHeight+=count;
		checkLost();	
	}
	
	private void checkLost() {
		if(gameHeight >= TetrisField.ROWS) listener.gameOver();
	}
	
	/* Return 1,2,3,4 with PDF dist */
	private int getNextNoOfRows(float dist[]) {
		if(dist == null || dist.length != 4) return -1;
		
		float vert[] = {dist[0],dist[0]+dist[1],dist[0]+dist[1]+dist[2],dist[0]+dist[1]+dist[2]+dist[3]};
		float rand = random.nextFloat() * (dist[0]+dist[1]+dist[2]+dist[3]);
		
		int i = 0;
		while(rand <= vert[i] || i<=4) i++;
		
		return i;
	}

	
}
