package tetris.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.rms.*;

public class RecordStoreHandler {

	public interface Persistant {
		public void readObject(DataInputStream stream) throws IOException;
		public void writeObject(DataOutputStream stream) throws IOException;		
	}

	// Load Object from DB
	public boolean load(Persistant obj, String rsName) {
		if (obj == null || rsName == "") return false;
		
		try {
			RecordStore rs = null;
			try {
				//RecordStore.deleteRecordStore(name);
				rs = RecordStore.openRecordStore(rsName, false);
				
				ByteArrayInputStream bais = new ByteArrayInputStream(rs.getRecord(1));
				DataInputStream inputStream = new DataInputStream(bais);
				
				obj.readObject(inputStream);
				
				inputStream.close();
				bais.close();
				
			} catch (IOException e) {

			} finally {
				if (rs != null) rs.closeRecordStore();
			}
		} catch (RecordStoreException e) {
			return false;
		}
		return true;
	}

	// Store Data To DB
	public void save(Persistant obj, String rsName) {
		try {
			RecordStore rs = null;
			try {
				rs = RecordStore.openRecordStore(rsName, true);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream outputStream = new DataOutputStream(baos);			

				obj.writeObject(outputStream);
				outputStream.flush();
				byte[] b = baos.toByteArray();
				
				outputStream.close();
				baos.close();
				
				if(rs.getNumRecords() == 0) {
					rs.addRecord(b, 0, b.length);
				} else {
					rs.setRecord(1, b, 0, b.length);
				}

			} catch (IOException ioe) {
				System.out.println(ioe);
				ioe.printStackTrace();

			} finally {
				if (rs != null) rs.closeRecordStore();

			}
		} catch (RecordStoreException e) {
		}
	}
	
	public void delete(String rsName) {
		try {
			RecordStore.deleteRecordStore(rsName);
		} catch (RecordStoreException e) {
		}	
	}
	
}