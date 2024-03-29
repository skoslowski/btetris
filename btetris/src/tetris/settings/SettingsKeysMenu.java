package tetris.settings;

import javax.microedition.lcdui.*;
import tetris.core.TetrisMIDlet;

class SettingsKeysMenu extends Form implements CommandListener,ItemStateListener  {
	
	private final KeyItem keyFields[] = new KeyItem[6];
		
	private final String labels[] = {"Left key","Right key","Rotate left key","Rotate right key","Softdrop key","Harddrop key"};
	private static final int keys[] = {Canvas.KEY_NUM0, Canvas.KEY_NUM1, Canvas.KEY_NUM2, Canvas.KEY_NUM3, 
		Canvas.KEY_NUM4, Canvas.KEY_NUM5, Canvas.KEY_NUM6, Canvas.KEY_NUM7, Canvas.KEY_NUM8, Canvas.KEY_NUM9, 
		Canvas.KEY_STAR, Canvas.KEY_POUND};
	
	private final Command store;
	private final TetrisMIDlet midlet;
	private final Settings settings;

	public SettingsKeysMenu(TetrisMIDlet midlet) {
		super("Settings - Keys");

		this.midlet=midlet;
		this.settings = Settings.getInstance();
		
		for(int i=0; i<keyFields.length; i++) {
			keyFields[i] = new KeyItem(labels[i], settings.keys[i], midlet.fontColor);
			append(keyFields[i]);
		}	
		
		store=new Command("Store", Command.OK, 1);
		addCommand(store);
		addCommand(new Command("Back", Command.BACK, 2));

		setCommandListener(this);
		setItemStateListener(this);

	}
	
	public void commandAction(Command c, Displayable d) {
		if(c==store) {
			for(int i=0; i<keyFields.length; i++)
				settings.keys[i] = keyFields[i].keyCode;
			
			settings.save();
			midlet.gui.showMainMenu();
			
		} else {
			Settings.showSettingsMenu(midlet);
			
		}
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