package tetris.core;

public class Protocol {
	public static final byte ONE_LINE = (byte)0x01;
	public static final byte TWO_LINES = (byte)0x02;
	public static final byte FOUR_LINES = (byte)0x03;
	public static final byte PAUSE_GAME = (byte)0x04;
	public static final byte GAME_HEIHGT = (byte)0x05;

	public static final byte UNPAUSE_GAME = (byte)0x06;
	
	public static final byte I_LOST = (byte)0x08;
	
	public static final byte RESTART = (byte)0x0C;
	
	public static final byte PING = (byte)0xFF;
	public static final byte DISCONNECT = (byte)0xFE;
	
}
