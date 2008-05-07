package tetris.ui;

import javax.microedition.lcdui.*;

import tetris.core.TetrisMIDlet;

public class HighscoreMenu extends Form implements CommandListener {

	private TetrisMIDlet midlet;

	public HighscoreMenu(TetrisMIDlet midlet) {
		super("Highscore");
		this.midlet = midlet;

		String scores="";

		String names [] = midlet.highscore.getNames();
		long points[] = midlet.highscore.getPoints();

			for(int i = 0; i<names.length; i++) {
				if(points[i]>=0)
					scores+=(i+1)+") " +names[i] + ": " + points[i] + "\n";
			}
			
		if(scores=="") scores="(empty)";

		append(scores);
		addCommand(new Command("Back", Command.EXIT, 2));
		setCommandListener(this);
	}
	public void commandAction(Command arg0, Displayable arg1) {
		midlet.gui.showMainMenu();
	}

}
