package tetris.tetris;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import tetris.core.RecordStoreHandler;

public class Scoring implements RecordStoreHandler.Persistant {

	private static int won = 0, lost = 0;
	private static final int RATE_HISTORY_LENGTH = 25;
	
	private long points = 0;
	private int lines = 0, sendRows = 0, level = 0, bricks=0;
	private int singles = 0, doubles = 0, triples = 0, tetris = 0, lineEvents = 0;

	private long lastBrick = 0;
	private float rate = 0;
	private Vector latestRates = new Vector();
	private boolean gameWasPaused=false;
	
	/* reset score counter */
	public static void resetWonLost() {
		won = 0;
		lost = 0;
	}

	public Scoring() {
		latestRates.addElement(new Float(50));
		latestRates.addElement(new Float(50));	
	}
	
	public void notifyPaused() {
		gameWasPaused=true;
	}
	
	/* add lines */
	public void addLines(int n) {
		lines += n;
		lineEvents++; 
		level = lines/10;
		switch (n) {
		case 4: 
			points+=1200*(level+1);
			tetris++;
			break;
		case 3: 
			points+=300*(level+1); 
			triples++;
			break;
		case 2: 
			points+=100*(level+1); 
			doubles++;
			break;
		case 1: 
			points+=40*(level+1);
			singles++;
		}
	}
	
	public void addBrick() {
		if(lastBrick!=0 && !gameWasPaused) {
			float currentRate = 60*1000/(float)(System.currentTimeMillis()-lastBrick);
			//rate = (rate*(bricks-1) + currentRate)/bricks ;
			
			float tempRate = rate * latestRates.size() + currentRate;
			
			if(latestRates.size() >= RATE_HISTORY_LENGTH) {
				tempRate -= ((Float)latestRates.firstElement()).floatValue();
				latestRates.removeElementAt(0);	
			}
			latestRates.addElement(new Float(currentRate));
			rate = tempRate / latestRates.size();
			
			// Neue Rate als rate der letzten N Bricks. 
			// Achtung: Schätzung mit der Annahme Var(X) klein.
			//rate = (rate*(RATE_HISTORY_LENGTH -1) + currentRate) / Math.min(bricks,RATE_HISTORY_LENGTH);
			
			
		}
		gameWasPaused=false;
		bricks++;
		lastBrick = System.currentTimeMillis();
	}
	
	public int getLevel() {
		return level;
	}
	
	public long getPoints() {
		return points;
	}
	
	public void addPointsSoftDropStep() {
		points++;
	}
	
	public void addPointsHardDrop(int lines) {
		if(lines>0) points+=2*lines;
	}

	public void addLost() {
		lost++;
	}

	public void addWon() {
		won++;
	}
	
	public void addSendRows(int n) {
		sendRows += n;
	}
	
	
	public void paint(Graphics g, boolean multiplayer) {
		g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));
		g.setColor(0xFFFFFF);
		int fontHeight = g.getFont().getHeight();
		int line=0;

		/* Points */
		g.drawString("Points:", 0, fontHeight*line++, Graphics.TOP | Graphics.HCENTER);
		g.drawString(String.valueOf(points), 0, fontHeight*line++, Graphics.TOP | Graphics.HCENTER);

		/* Level */
		g.drawString("Level:", 0, fontHeight*line++, Graphics.TOP | Graphics.HCENTER);
		g.drawString(String.valueOf(level), 0, fontHeight*line++, Graphics.TOP | Graphics.HCENTER);
		
		/* Lines */
		g.drawString("Lines:", 0, fontHeight*line++, Graphics.TOP | Graphics.HCENTER);
		String sLines = String.valueOf(lines);
		if (multiplayer) sLines+=" (" + String.valueOf(sendRows) + ")";
			g.drawString(sLines,0, fontHeight*line++, Graphics.TOP | Graphics.HCENTER);  	

		/* Rate */
		String sRate= String.valueOf(rate);
		if(sRate.length()>3) sRate = sRate.substring(0,sRate.indexOf(".")+2);
		g.drawString("Rate:", 0, fontHeight*line++, Graphics.TOP | Graphics.HCENTER);
		g.drawString(sRate, 0, fontHeight*line++, Graphics.TOP | Graphics.HCENTER);
		
		/* Score */
		if(multiplayer) {
			g.drawString("Score:",0, fontHeight*line++, Graphics.TOP | Graphics.HCENTER);   
			g.drawString(won + " : " + lost,0, fontHeight*line++, Graphics.TOP | Graphics.HCENTER);   
		}
		
	}

	public void readObject(DataInputStream stream) throws IOException {
		bricks = stream.readInt();
		doubles = stream.readInt();
		gameWasPaused = true;
		lastBrick = stream.readLong();
		level = stream.readInt();
		lineEvents = stream.readInt();
		lines = stream.readInt();
		points = stream.readLong();
		rate = stream.readFloat();
		singles = stream.readInt();
		tetris = stream.readInt();
		triples = stream.readInt();
		
		latestRates.removeAllElements();
		for(int i=0; i<RATE_HISTORY_LENGTH/2; i++) latestRates.addElement(new Float(rate));
	}

	public void writeObject(DataOutputStream stream) throws IOException {
		stream.writeInt(bricks);
		stream.writeInt(doubles);
		stream.writeLong(lastBrick);
		stream.writeInt(level);
		stream.writeInt(lineEvents);
		stream.writeInt(lines);
		stream.writeLong(points);
		stream.writeFloat(rate);
		stream.writeInt(singles);
		stream.writeInt(tetris);
		stream.writeInt(triples);	
	}
}