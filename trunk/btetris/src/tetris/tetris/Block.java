package tetris.tetris;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

class Block {

	/* [block color][shadow color] */
	private static int colors[][] = {
		{0xf0f000, 0xACAC26},
		{0x00f0f0, 0x00A8A8},
		{0xa000f0, 0x8026AC},
		{0xf0a000, 0xD89000},
		{0x0000f0, 0x2626AC},
		{0xf00000, 0xAC2626},
		{0x00f000, 0x26AC26},
		{0x999999, 0x555555}  //grey blocks in multiPlayer mode
	};
	private static Image[] bitmaps = new Image[colors.length];

	private int type;
	public int x;
	public int y;

	public Block clone() {
		return new Block(type, x, y);
	}

	public Block(int type, int x, int y) {
		this.type = type;
		this.x = x;
		this.y = y;
	}

	public void update(int x, int y) {
		this.x = x;
		this.y = y;
	}

	private void createBitmap(int blockSize) {
		bitmaps[type] = Image.createImage(blockSize, blockSize);
		Graphics g = bitmaps[type].getGraphics();

		g.setColor(colors[type][0]);
		g.fillRect(0,0,blockSize, blockSize);

		g.setColor(0xFFFFFF);
		/* Left */
		g.drawLine(0, 0, 0, blockSize-2);
		/* Top */
		g.drawLine(0, 0, blockSize-2, 0);

		g.setColor(colors[type][1]);
		/* Right */
		g.drawLine(blockSize-1, 1, blockSize-1, blockSize-1);
		/* Bottom */
		g.drawLine(1, blockSize-1, blockSize-1, blockSize-1);
	}

	public void paint(Graphics g, int blockSize) {
		if(y > 0) {
			if(bitmaps[type] == null)
				createBitmap(blockSize);

			g.drawImage(bitmaps[type], x*blockSize, (y-1)*blockSize, Graphics.TOP|Graphics.LEFT);
		}
	}

}