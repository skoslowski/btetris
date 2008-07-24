package tetris.highscore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.lcdui.Display;

import javax.microedition.rms.*;

import tetris.core.TetrisMIDlet;

public class Highscore implements RecordComparator, RecordFilter {
	private static Highscore instance=null;
	
	public static Highscore getInstance() {
		if(instance == null) instance = new Highscore(); 
		return instance;
	}
	public static void showHighscoreMenu(TetrisMIDlet midlet) {
		Display d = Display.getDisplay(midlet);
		d.setCurrent(new HighscoreMenu(midlet));
	}

	

	/* -------------------------------------------------------------------- */
	private final int LENGTH = 8;
	private final String recordStoreName="highscore";
	
	private String names[] = new String[LENGTH];
	private long points[] = new long[LENGTH];
	private long match_points = 0;

	private Highscore() {
		loadScores();	
	}

	public boolean matches(byte[] rec) {
		DataInputStream recStr = new DataInputStream(new ByteArrayInputStream(rec));
		try {
			recStr.readUTF();
			return (recStr.readLong()>match_points);
		} catch (IOException e) {
			return false;
		}
	}

	private void loadScores() {
		for(int i = 0; i<LENGTH;i++) {
			names[i]="";
			points[i]=-1;
		}
		try {
			RecordStore rs = null;
			try {
				rs = RecordStore.openRecordStore(recordStoreName, true);
				RecordEnumeration re = rs.enumerateRecords(null, this, false);
				int i=0;
				while(re.hasNextElement()) {
					try {
						if(i < LENGTH) {
							ByteArrayInputStream bais = new ByteArrayInputStream(re.nextRecord());
							DataInputStream recStr = new DataInputStream(bais);
							
							names[i] = recStr.readUTF();
							points[i]= recStr.readLong();
							
							recStr.close();
							bais.close();
						} else {
							rs.deleteRecord(re.nextRecordId());
						}
					} catch(IOException e) {}
					i++;
				}

			} catch(InvalidRecordIDException e) {

			}  finally {
				if(rs != null) rs.closeRecordStore();
			}
		} catch(RecordStoreException e) {

		}
	}	

	public String[] getNames() {
		return names;
	}
	public long[] getPoints() {
		return points;
	}

	public int checkScore(long points) {
		match_points = points;
		int rank = 0;
		try {
			RecordStore rs = null;
			try {
				rs = RecordStore.openRecordStore(recordStoreName, true);
				RecordEnumeration re = rs.enumerateRecords(this, null, false);
				rank = re.numRecords()+1;
			} catch(InvalidRecordIDException e) {

			}  finally {
				if(rs != null) rs.closeRecordStore();
			}
		} catch(RecordStoreException e) {
		}
		if(rank>LENGTH) rank = 0;
		return rank;
	}

	public void saveScore(String name, long points) {
		if(name=="") return;

		try {
			RecordStore rs = null;
			try {
				rs = RecordStore.openRecordStore(recordStoreName, true);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream outputStream = new DataOutputStream(baos);			

				outputStream.writeUTF(name);
				outputStream.writeLong(points);
				outputStream.flush();
				byte[] b = baos.toByteArray();

				outputStream.close();
				baos.close();
				
				rs.addRecord(b, 0, b.length);

			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();

			} finally {
				if (rs != null) rs.closeRecordStore();
			}
		} catch (RecordStoreException e) {

		}

		loadScores();
	}


	public int compare(byte[] rec1, byte[] rec2) {
		int points1=0, points2=0;
		try {
			ByteArrayInputStream bias1 = new ByteArrayInputStream(rec1);
			DataInputStream r1 = new DataInputStream(bias1);
			
			r1.readUTF();
			points1 = (int)r1.readLong();
			
			r1.close();
			bias1.close();
			
			ByteArrayInputStream bias2 = new ByteArrayInputStream(rec2);
			DataInputStream r2 = new DataInputStream(bias2);
			
			r2.readUTF();
			points2 = (int)r2.readLong();
			
			r2.close();
			bias2.close();			
		} catch (IOException e) {}

		if (points1 > points2) {
			return RecordComparator.PRECEDES;
		}
		else if (points1 < points2) {
			return RecordComparator.FOLLOWS;
		}
		else {
			return RecordComparator.EQUIVALENT;
		}
	}
}
