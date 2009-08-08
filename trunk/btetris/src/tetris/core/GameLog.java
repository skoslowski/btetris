package tetris.core;

import java.io.*;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class GameLog {
	
	public String strData="";
	private int rowCounter = 0;

	public boolean saveLogToFile() {
		if (strData=="") return false;
		try {
			String url = "file://E:/GameLog_" + System.currentTimeMillis() + ".txt";
			byte data[] = strData.getBytes();
			FileConnection fconn = (FileConnection)Connector.open(url, Connector.READ_WRITE);
			if (!fconn.exists()) {
				fconn.create();
			}
			OutputStream ops = fconn.openOutputStream();
			ops.write(data);
			ops.close();
			fconn.close();
		
		} catch (IOException ioe) {
			System.out.println("IOException: "+ioe.getMessage());
			return false;
		
		} catch (SecurityException se) {
			System.out.println("Security exception:" + se.getMessage());
			return false;
		}
		clear();
		return true;		
	}
	
	
	private void clear() {
		strData = "";
		rowCounter = 0;
	}

	public void add(String entry) {
		strData += entry + "\n";
		rowCounter++;
	}
	
	public int size() {
		return rowCounter;
	}
}
