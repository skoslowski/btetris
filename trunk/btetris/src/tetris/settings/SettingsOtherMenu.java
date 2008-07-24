package tetris.settings;

import javax.microedition.lcdui.*;
import tetris.core.TetrisMIDlet;

class SettingsOtherMenu extends Form implements CommandListener  {

	private final Gauge fallingSpeedGauge, transtionSpeedGauge;
	private final ChoiceGroup syncBricksCheck;
	
	private final Command store;
	private final TetrisMIDlet midlet;
	private final Settings settings;

	public SettingsOtherMenu(TetrisMIDlet midlet) {
		super("Settings - Other");

		this.midlet=midlet;
		settings = Settings.getInstance();

		syncBricksCheck = new ChoiceGroup("Sync Bricks", Choice.EXCLUSIVE);
		syncBricksCheck.append("On", null);
		syncBricksCheck.append("Off", null);
		syncBricksCheck.setSelectedIndex((settings.syncBricks)?0:1, true);
		append(syncBricksCheck);
		
		append(new Spacer(0,30));
		
		fallingSpeedGauge = new Gauge("Softdrop speed",true,5,settings.fallingSpeed);
		append(fallingSpeedGauge);	
		
		transtionSpeedGauge = new Gauge("Transition speed",true,4, settings.transitionSpeed);
		append(transtionSpeedGauge);
		
		store=new Command("Store", Command.OK, 1);
		addCommand(store);
		addCommand(new Command("Back", Command.BACK, 2));

		setCommandListener(this);

	}
	
	public void commandAction(Command c, Displayable d) {
		if(c==store) {
			settings.fallingSpeed = fallingSpeedGauge.getValue();
			
			settings.transitionSpeed = transtionSpeedGauge.getValue();
			
			settings.syncBricks = (syncBricksCheck.getSelectedIndex()==0)?true:false;
			
			settings.save();
			midlet.gui.showMainMenu();
		} else {
			Settings.showSettingsMenu(midlet);
		}
	}
	
}