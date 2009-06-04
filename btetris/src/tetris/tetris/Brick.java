package tetris.tetris;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Graphics;

import tetris.core.RecordStoreHandler;
import tetris.core.TetrisMIDlet;


class Brick implements RecordStoreHandler.Persistant{

	/* types[type][rotation][y][x] */
	private static int types[][][][] = {
			{
				{
					{0, 0, 0, 0},
					{0, 1, 1, 0},
					{0, 1, 1, 0},
					{0, 0, 0, 0}
				}
			},

			{
				{
					{0, 0, 0, 0},
					{1, 1, 1, 1},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 0, 1, 0},
					{0, 0, 1, 0},
					{0, 0, 1, 0},
					{0, 0, 1, 0}
				},
				{
					{0, 0, 0, 0},
					{0, 0, 0, 0},
					{1, 1, 1, 1},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 0, 0},
					{0, 1, 0, 0},
					{0, 1, 0, 0},
					{0, 1, 0, 0}
				}
			},

			{
				{
					{0, 0, 0, 0},
					{1, 1, 1, 0},
					{0, 1, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 0, 0},
					{1, 1, 0, 0},
					{0, 1, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 0, 0},
					{1, 1, 1, 0},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 0, 0},
					{0, 1, 1, 0},
					{0, 1, 0, 0},
					{0, 0, 0, 0}
				}
			},

			{
				{
					{0, 0, 0, 0},
					{1, 1, 1, 0},
					{1, 0, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{1, 1, 0, 0},
					{0, 1, 0, 0},
					{0, 1, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 0, 1, 0},
					{1, 1, 1, 0},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 0, 0},
					{0, 1, 0, 0},
					{0, 1, 1, 0},
					{0, 0, 0, 0}
				}
			},

			{
				{
					{0, 0, 0, 0},
					{1, 1, 1, 0},
					{0, 0, 1, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 0, 0},
					{0, 1, 0, 0},
					{1, 1, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{1, 0, 0, 0},
					{1, 1, 1, 0},
					{0, 0, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 1, 0},
					{0, 1, 0, 0},
					{0, 1, 0, 0},
					{0, 0, 0, 0}
				}
			},

			{
				{
					{0, 0, 0, 0},
					{1, 1, 0, 0},
					{0, 1, 1, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 0, 0, 0},
					{0, 0, 1, 0},
					{0, 1, 1, 0},
					{0, 1, 0, 0}
				},
				{
					{0, 0, 0, 0},
					{1, 1, 0, 0},
					{0, 1, 1, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 1, 0, 0},
					{1, 1, 0, 0},
					{1, 0, 0, 0},
					{0, 0, 0, 0}
				}
			},
			{				
				{
					{0, 0, 0, 0},
					{0, 1, 1, 0},
					{1, 1, 0, 0},
					{0, 0, 0, 0}
				},
				{
					{0, 0, 0, 0},
					{0, 1, 0, 0},
					{0, 1, 1, 0},
					{0, 0, 1, 0}
				},
				{
					{0, 0, 0, 0},
					{0, 0, 0, 0},
					{0, 1, 1, 0},
					{1, 1, 0, 0}
				},
				{
					{0, 0, 0, 0},
					{1, 0, 0, 0},
					{1, 1, 0, 0},
					{0, 1, 0, 0}
				}
			}
	};

	private int type = 0, rotation = 0, xoffset = 0, yoffset = 0;
	public Block[] blocks = new Block[4];

	
	public static Brick getRandomBrick() {
		return new Brick(TetrisMIDlet.random(types.length), (TetrisField.COLS/2)-2, 0);
	}
	
	private Brick(int type, int x, int y) {
		this.type = type % types.length;
		this.xoffset=x;
		this.yoffset=y;

		for (int i = 0;i < 4;i++)
			blocks[i] = new Block(type, 0, 0);

		updateBlocks();
	}

	public void rotate(boolean right) {
		rotation += (!right)? 1 : types[type].length-1;
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
	
	/* add the brick to the rows-Object*/
	public void addToRows(Row rows[]) {
		for (int i = 0;i < blocks.length;i++)
			rows[blocks[i].y].blocks[blocks[i].x] = blocks[i];
	}
	
	/* update blocks according to
	 * xOffset, yOffset and rotation */
	private void updateBlocks() {
		int i = 0;
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


	public void paint(Graphics g, int blockSize) {
		for(int i=0; i<blocks.length; i++)
			blocks[i].paint(g, blockSize);
	}

	public void readObject(DataInputStream stream) throws IOException {
		type = stream.readInt();
		rotation = stream.readInt();
		xoffset = stream.readInt();
		yoffset = stream.readInt();
		
		updateBlocks();
	}

	public void writeObject(DataOutputStream stream) throws IOException {
		stream.writeInt(type);
		stream.writeInt(rotation);
		stream.writeInt(xoffset);
		stream.writeInt(yoffset);
	}
}