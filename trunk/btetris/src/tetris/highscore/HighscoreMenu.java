package tetris.highscore;

import javax.microedition.lcdui.*;

import tetris.core.TetrisMIDlet;

class HighscoreMenu extends Form implements CommandListener {

	private final TetrisMIDlet midlet;
	private final Highscore highscore;

	public HighscoreMenu(TetrisMIDlet midlet) {
		super("Highscore");

		this.midlet = midlet;
		highscore = Highscore.getInstance();

		String scores="";
		String names [] = highscore.getNames();
		long points[] = highscore.getPoints();

			for(int i = 0; i<names.length; i++) {
				if(points[i]>=0)
					scores+=(i+1)+") " +names[i] + ": " + points[i] + "\n";
			}
			
		if(scores=="") scores="(empty)";

		append(scores);
		addCommand(new Command("Back", Command.BACK, 2));
		setCommandListener(this);
	}
	public void commandAction(Command c, Displayable d) {
		midlet.gui.showMainMenu();
	}

}
