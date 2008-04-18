package tetris.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.rms.*;

public abstract class Persistant {
	private final String name;

	public Persistant(String recordStoreName) {
		name = recordStoreName;
	}

	// Implemented by Object
	protected abstract void readObject(DataInputStream stream) throws IOException;
	protected abstract void writeObject(DataOutputStream stream) throws IOException;

	// Load Data from DB
	public boolean load() {
		try {
			RecordStore rs = null;
			try {
				//RecordStore.deleteRecordStore(name);
				rs = RecordStore.openRecordStore(name, true);
				readObject(new DataInputStream(new ByteArrayInputStream(rs.getRecord(1))));

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
	public void save() {
		try {
			RecordStore rs = null;
			try {
				rs = RecordStore.openRecordStore(name, true);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream outputStream = new DataOutputStream(baos);			

				writeObject(outputStream);
				outputStream.flush();
				byte[] b = baos.toByteArray();
				
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
	
}