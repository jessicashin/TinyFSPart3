package com.client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import com.chunkserver.ChunkServer;
import com.client.ClientFS.FSReturnVals;
import com.master.Master;

public class ClientRec {

	/**
	 * Appends a record to the open file as specified by ofh Returns BadHandle
	 * if ofh is invalid Returns BadRecID if the specified RID is not null
	 * Returns RecordTooLong if the size of payload exceeds chunksize RID is
	 * null if AppendRecord fails
	 *
	 * Example usage: AppendRecord(FH1, obama, RecID1)
	 */
	// /create new master

	Master m = new Master();
	ChunkServer cs = new ChunkServer();
	final static String filePath = "csci485/";

	public String lastChunk(FileHandle ofh) {
		// /find the last chunk of filehandle
		return m.GetLastChunk(ofh);
	}

	public FSReturnVals AppendRecord(FileHandle ofh, byte[] payload,
			RID RecordID) {
		// /// use the chunkhandle defined in RID class
		// filehandle invalid?
		if (RecordID != null) {
			return FSReturnVals.BadRecID;
		} else if (payload.length > 4096) {
			return FSReturnVals.RecordTooLong;
		}
		// //append to the last chunk
		// //read first 4 bytes to get num of records
		byte[] numOfRecords = new byte[4];
		numOfRecords = cs.readChunk(this.lastChunk(ofh), 0, 4);
		boolean success = false;
		if ((numOfRecords == null)
				|| (ByteBuffer.wrap(numOfRecords).getInt() == 0)) {
			// /append a new record to a empty file

			byte[] numRec = ByteBuffer.allocate(4).putInt(1).array();
			success = cs.writeChunk(this.lastChunk(ofh), numRec, 0);
			if (!success) {
				return FSReturnVals.Fail;
			}
			byte[] payloadSize = ByteBuffer.allocate(4).putInt(payload.length)
					.array();
			success = cs.writeChunk(this.lastChunk(ofh), payloadSize, 8);
			if (!success) {
				return FSReturnVals.Fail;
			}
			// //before write payload payload too long
			if (payload.length > (4096 - 4 - 12)) {
				return FSReturnVals.Fail;
			}
			success = cs.writeChunk(this.lastChunk(ofh), payload, 12);
			if (!success) {
				return FSReturnVals.Fail;
			}
			byte[] nextEmpty = ByteBuffer.allocate(4)
					.putInt(8 + 4 + payload.length).array();
			success = cs.writeChunk(this.lastChunk(ofh), nextEmpty, 4);
			if (!success) {
				return FSReturnVals.Fail;
			}
			// write firstoffset
			byte[] firstOffset = ByteBuffer.allocate(4).putInt(8).array();
			success = cs.writeChunk(this.lastChunk(ofh), firstOffset, 4092);
			if (success) {
				return FSReturnVals.Success;
			} else {
				return FSReturnVals.Fail;
			}

		} else {
			// /has at least one record in the chunk
			int numRec = ByteBuffer.wrap(numOfRecords).getInt();
			byte[] previousOffset = cs.readChunk(this.lastChunk(ofh),
					4096 - numRec * 4, 4);
			int previous_offset = ByteBuffer.wrap(previousOffset).getInt();
			// /next empty location
			byte[] nextEmpty = cs.readChunk(this.lastChunk(ofh), 4, 4);
			int next_empty = ByteBuffer.wrap(nextEmpty).getInt();
			///write payload size for next record 
			byte[] payloadSize = ByteBuffer.allocate(4).putInt(payload.length)
					.array();
			if (next_empty + 4 > (4096 - numRec * 4)) {
				return FSReturnVals.Fail;
			}
			success = cs.writeChunk(this.lastChunk(ofh), payloadSize,
					next_empty);
			if (!success) {
				return FSReturnVals.Fail;
			}
			// /write payload data
			if (next_empty + 4 + payload.length > (4096 - numRec * 4)) {
				return FSReturnVals.Fail;
			}
			success = cs.writeChunk(this.lastChunk(ofh), payload,
					next_empty + 4);
			if (!success) {
				return FSReturnVals.Fail;
			}
			// //numofrec++
			numRec++;
			numOfRecords = ByteBuffer.allocate(4).putInt(numRec).array();
			success = cs.writeChunk(this.lastChunk(ofh), numOfRecords, 0);
			if (!success) {
				return FSReturnVals.Fail;
			}

			// /write next offset address to the end
			byte[] nextOffset = ByteBuffer.allocate(4).putInt(next_empty)
					.array();
			if (next_empty + 4 + payload.length > (4096 - numRec * 4)) {
				return FSReturnVals.Fail;
			}
			success = cs.writeChunk(this.lastChunk(ofh), nextOffset,
					4096 - numRec * 4);
			if (!success) {
				return FSReturnVals.Fail;
			}
			RecordID= new RID(this.lastChunk(ofh), next_empty);
			// //update next empty
			next_empty = next_empty + 4 + payload.length;
			nextEmpty = ByteBuffer.allocate(4).putInt(next_empty).array();
			success = cs.writeChunk(this.lastChunk(ofh), nextEmpty, 4);
			if (!success) {
				return FSReturnVals.Fail;
			} else {
				///make a new RID 
				
				return FSReturnVals.Success;
			}

		}

	}

	/**
	 * Deletes the specified record by RecordID from the open file specified by
	 * ofh Returns BadHandle if ofh is invalid Returns BadRecID if the specified
	 * RID is not valid Returns RecDoesNotExist if the record specified by
	 * RecordID does not exist.
	 *
	 * Example usage: DeleteRecord(FH1, RecID1)
	 */
	public FSReturnVals DeleteRecord(FileHandle ofh, RID RecordID) {
		///invalid filehandle????
		
		if (RecordID == null) {
			return FSReturnVals.BadRecID;
		}else if (RecordID.slot <0) {
			return FSReturnVals.RecDoesNotExist;
		}
		///make offsize -1 
		///slot 0 4092-4096
		byte[]invalidOffset= ByteBuffer.allocate(4).putInt(-1).array();
		boolean success= cs.writeChunk(this.lastChunk(ofh),invalidOffset , (4096-(RecordID.slot+1)*4));
		
		if (success){
			return FSReturnVals.Success;
		}		
		else {
			return FSReturnVals.Fail;
		}
	}

	/**
	 * Reads the first record of the file specified by ofh into payload Returns
	 * BadHandle if ofh is invalid Returns RecDoesNotExist if the file is empty
	 *
	 * Example usage: ReadFirstRecord(FH1, tinyRec)
	 */
	public FSReturnVals ReadFirstRecord(FileHandle ofh, TinyRec rec) {
		return null;
	}

	/**
	 * Reads the last record of the file specified by ofh into payload Returns
	 * BadHandle if ofh is invalid Returns RecDoesNotExist if the file is empty
	 *
	 * Example usage: ReadLastRecord(FH1, tinyRec)
	 */
	public FSReturnVals ReadLastRecord(FileHandle ofh, TinyRec rec) {
		return null;
	}

	/**
	 * Reads the next record after the specified pivot of the file specified by
	 * ofh into payload Returns BadHandle if ofh is invalid Returns
	 * RecDoesNotExist if the file is empty or pivot is invalid
	 *
	 * Example usage: 1. ReadFirstRecord(FH1, tinyRec1) 2. ReadNextRecord(FH1,
	 * rec1, tinyRec2) 3. ReadNextRecord(FH1, rec2, tinyRec3)
	 */
	public FSReturnVals ReadNextRecord(FileHandle ofh, RID pivot, TinyRec rec) {
		return null;
	}

	/**
	 * Reads the previous record after the specified pivot of the file specified
	 * by ofh into payload Returns BadHandle if ofh is invalid Returns
	 * RecDoesNotExist if the file is empty or pivot is invalid
	 *
	 * Example usage: 1. ReadLastRecord(FH1, tinyRec1) 2. ReadPrevRecord(FH1,
	 * recn-1, tinyRec2) 3. ReadPrevRecord(FH1, recn-2, tinyRec3)
	 */
	public FSReturnVals ReadPrevRecord(FileHandle ofh, RID pivot, TinyRec rec) {
		return null;
	}

}
