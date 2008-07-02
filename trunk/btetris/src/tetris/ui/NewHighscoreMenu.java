package tetris.ui;

import javax.microedition.lcdui.*;

import tetris.core.TetrisMIDlet;

public class NewHighscoreMenu extends Form implements CommandListener, ItemStateListener {

	private Command save, back;
	private TextField name;
	private TetrisMIDlet midlet;
	
	public NewHighscoreMenu(TetrisMIDlet midlet, int rank) {
		super("New Highscore");
		this.midlet=midlet;
		save = new Command("Save",Command.OK,0);
		back = new Command("Back",Command.OK,1);
		name = new TextField("Name","",30,TextField.INITIAL_CAPS_WORD);
		
		append("Congratulations!\n" +
				"\n"+
    			"Rank: "+rank+"\n" +
    			"Points: "+midlet.score.getPoints()
			  );
		append(name);
		addCommand(back);
		setCommandListener(this);
		setItemStateListener(this);		
	}

	public void commandAction(Command c, Displayable d) {
		if (c==save) {
			midlet.highscore.saveScore(name.getString(), midlet.score.getPoints());
		} 
		midlet.gui.showInGameMenu(false);
	}

	public void itemStateChanged(Item item) {
		if(((TextField)item).size()==0) {
			removeCommand(save);			
		} else {
			addCommand(save);
		}
		
	}
}
