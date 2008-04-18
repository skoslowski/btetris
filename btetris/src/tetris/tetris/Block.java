package tetris.tetris;

import javax.microedition.lcdui.Graphics;

import tetris.ui.TetrisCanvas;

public class Block {

	private int color;
	public int x;
	public int y;

	public Block(int color, int x, int y) {
		this.color = color;
		this.x = x;
		this.y = y;
	}

	public void update(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Block clone() {
		return new Block(color, x, y);
	}

	public void paint(Graphics g, int blockSize) {
		g.setColor(color);
		g.fillRect(x*blockSize,y*blockSize,blockSize, blockSize);
		g.setColor(TetrisCanvas.PASSIVE_BORDER_COLOR);
		g.drawRect(x*blockSize,y*blockSize,blockSize, blockSize);
	}

}