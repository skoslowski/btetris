package tetris.ui;

import javax.microedition.lcdui.*;
import tetris.core.TetrisMIDlet;

public class SettingsMenu extends Form implements CommandListener,ItemStateListener  {

	private final KeyItem keyFields[] = new KeyItem[6];
	
	
	private final String labels[] = {"Left key","Right key","Rotate left key","Rotate right key","Softdrop key","Harddrop key"};
	private static final int keys[] = {Canvas.KEY_NUM0, Canvas.KEY_NUM1, Canvas.KEY_NUM2, Canvas.KEY_NUM3, 
		Canvas.KEY_NUM4, Canvas.KEY_NUM5, Canvas.KEY_NUM6, Canvas.KEY_NUM7, Canvas.KEY_NUM8, Canvas.KEY_NUM9, 
		Canvas.KEY_STAR, Canvas.KEY_POUND};
	
	private final Gauge fallingSpeedGauge, transtionSpeedGauge;
	private final ChoiceGroup syncBricksCheck;
	
	private final Command store;
	private final TetrisMIDlet midlet;

	public SettingsMenu(TetrisMIDlet midlet) {
		super("Settings");

		this.midlet=midlet;


		for(int i=0; i<keyFields.length; i++) {
			keyFields[i] = new KeyItem(labels[i],midlet.settings.keys[i],midlet.fontColor);
			append(keyFields[i]);
		}	
		
		syncBricksCheck = new ChoiceGroup("Sync Bricks", Choice.EXCLUSIVE);
		syncBricksCheck.append("On", null);
		syncBricksCheck.append("Off", null);
		syncBricksCheck.setSelectedIndex((midlet.settings.syncBricks)?0:1, true);
		append(syncBricksCheck);
		
		fallingSpeedGauge = new Gauge("Softdrop speed",true,5,midlet.settings.fallingSpeed);
		append(fallingSpeedGauge);	
		
		transtionSpeedGauge = new Gauge("Transition speed",true,4, midlet.settings.transitionSpeed);
		append(transtionSpeedGauge);
		
		store=new Command("Store", Command.OK, 1);
		addCommand(store);
		addCommand(new Command("Back", Command.BACK, 2));

		setCommandListener(this);
		setItemStateListener(this);

	}
	
	public void commandAction(Command c, Displayable d) {
		if(c==store) {
			for(int i=0; i<keyFields.length; i++)
				midlet.settings.keys[i] = keyFields[i].keyCode;
			
			midlet.settings.fallingSpeed = fallingSpeedGauge.getValue();
			
			midlet.settings.transitionSpeed = transtionSpeedGauge.getValue();
			
			midlet.settings.syncBricks = (syncBricksCheck.getSelectedIndex()==0)?true:false;
			
			midlet.settings.save();
		}
		midlet.gui.showMainMenu();
	}

	public void itemStateChanged(Item item) {
		if (KeyItem.class.isInstance(item) )  {
			int newKeyCode = ((KeyItem) item).keyCode;
			int oldKeyCode = ((KeyItem) item).oldKeyCode;

			for(int i=0; i<keyFields.length; i++)
				if(keyFields[i] != (KeyItem)item && keyFields[i].keyCode == newKeyCode) {
					keyFields[i].setKeyCode(oldKeyCode);
					break;
				}
		} 
	}

	private class KeyItem extends CustomItem {

		public int keyCode;
		public int oldKeyCode;
		private Canvas c = new Canvas() { public void paint(Graphics g) {}};
		private int textColor;

		public KeyItem(String label, int keyCode, int textColor) {
			super(label);
//			setLayout(LAYOUT_NEWLINE_AFTER );

			this.textColor = textColor;
			this.keyCode = keyCode;
			oldKeyCode = keyCode;
		}

		public void setKeyCode(int newKeyCode) {
			keyCode = newKeyCode;
			repaint();
		}

		public int getMinContentHeight() {
			return (Font.getDefaultFont()).getHeight();
		}

		public int getMinContentWidth() {
			return (Font.getDefaultFont()).stringWidth("Rotate left key");
		}

		public int getPrefContentHeight(int width) {
			return (Font.getDefaultFont()).getHeight()+4;
		}

		public int getPrefContentWidth(int height) {
			return (Font.getDefaultFont()).stringWidth("Rotate left key")+2;
		}

		public void paint(Graphics g, int w, int h) {
			g.setColor(textColor);
			g.setFont(Font.getDefaultFont());
			String keyName = c.getKeyName(keyCode);
			g.drawString(keyName, 5, 2, Graphics.TOP | Graphics.LEFT);
		}

		private boolean isInArray(int needle, int[] haystack) {
			for(int i=0;i<haystack.length;i++)
				if(needle == haystack[i]) return true;
			return false;
		}

		public void keyPressed(int keyCode) {
			if(isInArray(keyCode,keys)) {
				oldKeyCode = this.keyCode;
				this.keyCode=keyCode;
				notifyStateChanged();
				repaint();	
			}
		}

		protected boolean traverse(int dir,int w,int h,int[] visRect_inout) {
			return false;
		}
	}
}