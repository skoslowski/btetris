package tetris.core;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class Scoring {

	private static int won = 0, lost = 0;
	
	private long points = 0;
	private int lines = 0, sendRows = 0, level = 0, bricks=0;
	private int singles = 0, doubles = 0, triples = 0, tetris = 0;

	/* reset score counter */
	public void resetWonLost() {
		won = 0;
		lost = 0;
	}

	/* add lines */
	public void addLines(int n) {
		lines += n;
		level = lines%10;
		
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
		bricks++;
	}
	
	public int getLevel() {
		return level;
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
	
	
	public int paint(Graphics g, boolean multiplayer) {
		g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));
		g.setColor(0xFFFFFF);
		
		g.drawString(" Points: " + String.valueOf(points), 1, 1, Graphics.TOP | Graphics.LEFT);

		if(multiplayer)
		g.drawString("Score: "+won + " : " + lost,g.getClipWidth()-3, 1, Graphics.TOP | Graphics.RIGHT);   
		
		return g.getFont().getHeight() + 2;
	}

	public void paintStats(Graphics g, boolean multiplayer) {
		g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_SMALL));
		g.setColor(0xFFFFFF);
		int fontHeight = g.getFont().getHeight();
		
		g.drawString("Singles:", 0, 0, Graphics.TOP | Graphics.HCENTER);
		g.drawString(String.valueOf(singles), 0, fontHeight, Graphics.TOP | Graphics.HCENTER);
		g.drawString("Doubles:", 0, 2*fontHeight, Graphics.TOP | Graphics.HCENTER);
		g.drawString(String.valueOf(doubles), 0, 3*fontHeight, Graphics.TOP | Graphics.HCENTER);
		g.drawString("Triples:", 0, 4*fontHeight, Graphics.TOP | Graphics.HCENTER);
		g.drawString(String.valueOf(triples), 0, 5*fontHeight, Graphics.TOP | Graphics.HCENTER);
		g.drawString("Tetris:", 0, 6*fontHeight, Graphics.TOP | Graphics.HCENTER);
		g.drawString(String.valueOf(tetris), 0, 7*fontHeight, Graphics.TOP | Graphics.HCENTER);
		
		g.drawString("Bricks:", 0, 8*fontHeight, Graphics.TOP | Graphics.HCENTER);
		g.drawString(String.valueOf(bricks), 0, 9*fontHeight, Graphics.TOP | Graphics.HCENTER);
		
		g.drawString("Lines:", 0, 10*fontHeight, Graphics.TOP | Graphics.HCENTER);
		String sLines = String.valueOf(lines);
		if (multiplayer) sLines+=" (" + String.valueOf(sendRows) + ")";
		g.drawString(sLines,0, 11*fontHeight, Graphics.TOP | Graphics.HCENTER);  
	}
}