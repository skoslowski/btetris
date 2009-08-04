package tetris.core;

import java.util.Random;
import java.util.Vector;

public class TGMrandomizer {
	// The tetrominoes =)
	private static final int O=0, S=6, Z=5; // T=2, I=1, J=4, L=3;	
	private static final int numTries = 6;
	
	private final Random random = new Random();
	private final Vector history = new Vector();

	private boolean isFirst = true;
	
	public TGMrandomizer() {
		this(System.currentTimeMillis());
	}	
	public TGMrandomizer(long seed) {
		this.reset(seed);
	}

	
	public void reset(long seed) {
		history.removeAllElements();
		history.addElement(new Integer( S ));
		history.addElement(new Integer( Z ));
		history.addElement(new Integer( S ));
		history.addElement(new Integer( Z ));
		
		random.setSeed(seed);
		
		isFirst = true;
	}
	
	public int getNext() {
		int next=-1;
		
		if(isFirst) {
			// First Run
			while(next==S || next==Z || next==O || next==-1 )
				next = getRandom();
			
			isFirst=false;
			
		} else {
			// Others
			int i = numTries;
			do {
				next = getRandom();
			} while( history.indexOf(new Integer(next))>0 && --i>0 );
			
			while(history.size() >= numTries) history.removeElementAt(0);
			history.addElement(new Integer(next));
			
		}
		
		return next;
	}
	
	private int getRandom() {
		return random.nextInt(7);
	}
	
}
