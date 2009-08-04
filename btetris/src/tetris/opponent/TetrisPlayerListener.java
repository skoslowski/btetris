package tetris.opponent;

public interface TetrisPlayerListener {
	public void recieveRows(int count);
	
	public void recieveHeight(int gameheight);
	
	public void startGame(int gametype);
	public void stopGame(boolean byPeer);
		
	public void pauseGame(boolean byPeer);
	public void unpauseGame(boolean byPeer);
	
	public void endOfGame(boolean byPeer);
	public void restartGame(boolean byPeer, long seed);

}
