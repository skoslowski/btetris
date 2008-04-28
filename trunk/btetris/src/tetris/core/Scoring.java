package tetris.core;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class Scoring {

	private long points = 0;
	private int won = 0;
	private int lost = 0;
	
	private int lines = 0;
	private int sendRows = 0;
	private int level = 0;

	/* reset score counter */
	public void reset() {
		points = 0;
		lines = 0;
		sendRows = 0;
	}

	/* add lines */
	public void addLines(int n) {
		lines += n;
		level = lines%10;
		
		switch (n) {
		case 4: 
			points+=1200*(level+1); 
			break;
		case 3: 
			points+=300*(level+1); 
			break;
		case 2: 
			points+=100*(level+1); 
			break;
		case 1: 
			points+=40*(level+1);
		}
	}
	
	public int getLevel() {
		return level;
	}
	
	public void addPointsSoftDropStep() {
		points++;
	}
	
	public void addPointsHardDrop(int lines) {
		if(lines>0) {
			points+=2*lines;
		}
	}

	public void addLost() {
		lost++;
	}
	
	public int getLost() {
		return lost;
	}
	
	public void addWon() {
		won++;
	}
	
	public int getWon() {
		return won;
	}
	
	public void addSendRows(int n) {
		sendRows += n;
	}
	
	
	
	public int paint(Graphics g, boolean multiplayer) {
		g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));
		g.setColor(0xFFFFFF);
		
		g.drawString(" P: " + String.valueOf(points), 1, 1, Graphics.TOP | Graphics.LEFT);

		if(multiplayer)
			g.drawString(won + " : " + lost,g.getClipWidth()/2,1,Graphics.TOP | Graphics.HCENTER);
		
		String sLines = " L: " + String.valueOf(lines);
		if (multiplayer) 
			sLines += " (" + String.valueOf(sendRows) + ")";

		g.drawString(sLines,g.getClipWidth()-3, 1, Graphics.TOP | Graphics.RIGHT);   
		
		return g.getFont().getHeight() + 2;
	}

}