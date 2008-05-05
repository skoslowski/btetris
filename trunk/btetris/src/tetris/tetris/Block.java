package tetris.tetris;

import javax.microedition.lcdui.Graphics;

import tetris.ui.TetrisGame;

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
		if(y > 0) {
			g.setColor(color);
			g.fillRect(x*blockSize,(y-1)*blockSize,blockSize, blockSize);
			g.setColor(TetrisGame.PASSIVE_BORDER_COLOR);
			g.drawRect(x*blockSize,(y-1)*blockSize,blockSize, blockSize);
		}
	}

}