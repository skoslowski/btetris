package tetris.tetris;

import javax.microedition.lcdui.Graphics;

import tetris.core.TetrisMIDlet;
import tetris.ui.TetrisGame;

/* Brick-Types:
 *
 * 0: ##
 *    ##
 *
 * 1: ####
 *
 * 2:  #
 *    ###
 *
 * 3:  #
 *     #
 *     ##
 *
 * 4:  #
 *     #
 *    ##
 *
 * 5:  #
 *    ##
 *    #
 *
 * 6:  #
 *     ##
 *      #
 *
 */

public class Brick {

	private static int colors[] = {
		0xffd800,
		0x0000ff,
		0xffd800,
		0x00c400,
		0x0000ff,
		0xe93100,
		0x00c400
	};

	/* types[type][rotation][y][x] */
	private int types[][][][] = {
			{
				{
					{0, 1, 1, 0},
					{0, 1, 1, 0},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				}
			},

			{
				{
					{0, 0, 1, 0},
					{0, 0, 1, 0},
					{0, 0, 1, 0},
					{0, 0, 1, 0}
				},
				{
					{0, 0, 0, 0},
					{1, 1, 1, 1},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				}
			},

			{
				{
					{0, 0, 1, 0},
					{0, 1, 1, 1},
					{0, 0, 0, 0},
					{0, 0, 0, 0},
				},
				{
					{0, 0, 1, 0},
					{0, 1, 1, 0},
					{0, 0, 1, 0},
					{0, 0, 0, 0},
				},
				{
					{0, 1, 1, 1},
					{0, 0, 1, 0},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 0, 1, 0},
					{0, 0, 1, 1},
					{0, 0, 1, 0},
					{0, 0, 0, 0},
				},
			},

			{
				{
					{0, 1, 0, 0},
					{0, 1, 0, 0},
					{0, 1, 1, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 0, 0, 1},
					{0, 1, 1, 1},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 1, 0},
					{0, 0, 1, 0},
					{0, 0, 1, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 1, 1},
					{0, 1, 0, 0},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				},
			},

			{
				{
					{0, 0, 1, 0},
					{0, 0, 1, 0},
					{0, 1, 1, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 1, 1},
					{0, 0, 0, 1},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 1, 0},
					{0, 1, 0, 0},
					{0, 1, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 0, 0},
					{0, 1, 1, 1},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				},
			},

			{
				{
					{0, 0, 1, 0},
					{0, 1, 1, 0},
					{0, 1, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 1, 0},
					{0, 0, 1, 1},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				},

			},
			{
				{
					{0, 1, 0, 0},
					{0, 1, 1, 0},
					{0, 0, 1, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 0, 1, 1},
					{0, 1, 1, 0},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				},
			}
	};

	private int type = 0, rotation = 0, xoffset = 0, yoffset = 0 ;
	public Block [] blocks = new Block[4];;

	public Brick(int type, int x, int y) {
		this.type = type % types.length;
		this.xoffset=x;
		this.yoffset=y;

		for (int i = 0;i < 4;i++)
			blocks[i] = new Block(colors[type], 0, 0);
		updateBlocks();
	}

	public static Brick getRandomBrick(int x) {
		return new Brick(TetrisMIDlet.random(7), x, 0);
	}

	public void rotate(boolean left) {
		rotation += (left)? 1 : types[type].length-1;
		rotation %= types[type].length;
		updateBlocks();
	}

	/* one step down */
	public void step() {
		yoffset++;
		updateBlocks();
	}

	public void stepback() {
		yoffset--;
		updateBlocks();		
	}

	/* one step left */
	public void left() {
		xoffset--;
		updateBlocks();
	}

	/* one step right */
	public void right() {
		xoffset++;
		updateBlocks();
	}

	public void paint(Graphics g, int blockSize, boolean isActive) {
		for(int i=0; i<blocks.length; i++)
			blocks[i].paint(g, blockSize);

		/* Draw Active Frame */
		if(isActive) {
			g.setColor(TetrisGame.ACTIVE_BORDER_COLOR);
			int[][] brickMatrix = types[type][rotation];
			for (int y = 0; y < 4; y++)
				for (int x = 0; x < 4; x++)
					if (brickMatrix[y][x] == 1) {
						if(x==0 || brickMatrix[y][x-1]==0)
							g.drawLine((x+xoffset)*blockSize, (y+yoffset)*blockSize, 
									(x+xoffset)*blockSize, (y+1+yoffset)*blockSize);
						if(x==3 || brickMatrix[y][x+1]==0)
							g.drawLine((x+1+xoffset)*blockSize, (y+yoffset)*blockSize, 
									(x+1+xoffset)*blockSize, (y+1+yoffset)*blockSize);
						if(y==0 || brickMatrix[y-1][x]==0)						
							g.drawLine((x+  xoffset)*blockSize, (y+yoffset)*blockSize, 
									(x+1+xoffset)*blockSize, (y+yoffset)*blockSize);
						if(y==3 || brickMatrix[y+1][x]==0)
							g.drawLine((x+  xoffset)*blockSize, (y+1+yoffset)*blockSize, 
									(x+1+xoffset)*blockSize, (y+1+yoffset)*blockSize);

					}
		}
	}

	/* update blocks according to
	 * xoffset, yoffset and rotation */
	private void updateBlocks() {
		int i = 0; /* block counter */

		for (int y = 0; y < 4; y++)
			for (int x = 0; x < 4; x++)
				if (types[type][rotation][y][x] == 1) {
					if (i == blocks.length) break;
					/* update each blocks position */
					blocks[i].update(xoffset + x,yoffset + y);
					i++;
				}
	}

	/* no clone() in J2ME, this is our own */
	public Brick clone() {
		Brick b = new Brick(type, xoffset, yoffset);
		b.rotation = rotation;

		for (int i = 0;i < blocks.length;i++)
			b.blocks[i] = this.blocks[i].clone();

		return b;
	}


}