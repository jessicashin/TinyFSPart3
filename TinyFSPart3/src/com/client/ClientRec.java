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

	ChunkServer cs = new ChunkServer();
	final static String filePath = "csci485/";
	public final static int ChunkSize = 4 * 1024; //4 KB chunk sizes

	public String lastChunk(FileHandle ofh) {
		// /find the last chunk of filehandle
		return Master.get().GetLastChunk(ofh);
	}
	
	/**
	 * Appends a record to the open file as specified by ofh Returns BadHandle
	 * if ofh is invalid Returns BadRecID if the specified RID is not null
	 * Returns RecordTooLong if the size of payload exceeds chunksize RID is
	 * null if AppendRecord fails
	 *
	 * Example usage: AppendRecord(FH1, obama, RecID1)
	 */
	public FSReturnVals AppendRecord(
			FileHandle ofh,
			byte[] payload,
			RID RecordID)
	{
		if (payload.length > ChunkServer.ChunkSize)
		{
			return FSReturnVals.RecordTooLong;
		}
		
		if (!Master.get().ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		
		if(Master.get().GetLastChunk(ofh) == null)
		{
			Master.get().AppendChunk(ofh);
			cs.createChunk(Master.get().GetLastChunk(ofh));
		}
		
		RecordID.chunk = Master.get().GetLastChunk(ofh);
		
		byte[] bytes = new byte[4];
		
		// Read number of records.
		bytes = cs.readChunk(RecordID.chunk, 0, 4);
		RecordID.slot = ByteBuffer.wrap(bytes).getInt();
		
		// Read next available offset.
		bytes = cs.readChunk(RecordID.chunk, 4, 4);
		int offset = ByteBuffer.wrap(bytes).getInt();
		
		// Write record's slot with offset.
		ByteBuffer.wrap(bytes).putInt(offset);
		cs.writeChunk(RecordID.chunk, bytes, (ChunkServer.ChunkSize - 4) - (RecordID.slot * 4));
		
		// Write record size at offset.
		ByteBuffer.wrap(bytes).putInt(payload.length);
		cs.writeChunk(RecordID.chunk, bytes, offset);
		
		// Write record payload.
		cs.writeChunk(RecordID.chunk, payload, offset + 4);
		
		// Update number of records.
		ByteBuffer.wrap(bytes).putInt(RecordID.slot + 1);
		cs.writeChunk(RecordID.chunk, bytes, 0);
		
		// Update next available offset.
		ByteBuffer.wrap(bytes).putInt(offset + payload.length + 4);
		cs.writeChunk(RecordID.chunk, bytes, 4);
		
		return FSReturnVals.Success;
	}

	/**
	 * Deletes the specified record by RecordID from the open file specified by
	 * ofh Returns BadHandle if ofh is invalid Returns BadRecID if the specified
	 * RID is not valid Returns RecDoesNotExist if the record specified by
	 * RecordID does not exist.
	 *
	 * Example usage: DeleteRecord(FH1, RecID1)
	 */
	public FSReturnVals DeleteRecord(FileHandle ofh, RID RecordID)
	{
		if (RecordID == null)
		{
			return FSReturnVals.BadRecID;
		}
		
		if (!Master.get().ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		
		cs.writeChunk(
				RecordID.chunk,
				ByteBuffer.allocate(4).putInt(-1).array(),
				(ChunkServer.ChunkSize - 4) - (RecordID.getSlot() * 4));
		
		return FSReturnVals.Success;
	}

	/**
	 * Reads the first record of the file specified by ofh into payload Returns
	 * BadHandle if ofh is invalid Returns RecDoesNotExist if the file is empty
	 *
	 * Example usage: ReadFirstRecord(FH1, tinyRec)
	 */
	public FSReturnVals ReadFirstRecord(FileHandle ofh, TinyRec rec)
	{
		if (!Master.get().ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		
		rec.setRID(new RID(Master.get().GetFirstChunk(ofh), -1));
		
		while (rec.getRID().chunk != null)
		{
			byte[] bytes = new byte[4];
			
			bytes = cs.readChunk(rec.getRID().chunk, 0, 4);
			int count = ByteBuffer.wrap(bytes).getInt();
			
			int offset = -1;
			
			for (int i = 0; i < count; ++i)
			{
				bytes = cs.readChunk(rec.getRID().chunk, (ChunkServer.ChunkSize - 4) - (count * 4), 4);
				offset = ByteBuffer.wrap(bytes).getInt();
				if (offset != -1)
				{
					rec.getRID().slot = i;
					break;
				}
			}
			
			if (rec.getRID().slot == -1)
			{
				break;
			}
						
			bytes = cs.readChunk(rec.getRID().chunk, offset, 4);
			int size = ByteBuffer.wrap(bytes).getInt();
			
			rec.setPayload(cs.readChunk(rec.getRID().chunk, offset + 4, size));
			
			return FSReturnVals.Success;
		}
		
		return FSReturnVals.Fail;
	}

	/**
	 * Reads the last record of the file specified by ofh into payload Returns
	 * BadHandle if ofh is invalid Returns RecDoesNotExist if the file is empty
	 *
	 * Example usage: ReadLastRecord(FH1, tinyRec)
	 */
	public FSReturnVals ReadLastRecord(FileHandle ofh, TinyRec rec)
	{
		if (!Master.get().ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		
		rec.setRID(new RID(Master.get().GetLastChunk(ofh), -1));
		
		while (rec.getRID().chunk != null)
		{
			byte[] bytes = new byte[4];
			
			bytes = cs.readChunk(rec.getRID().chunk, 0, 4);
			int count = ByteBuffer.wrap(bytes).getInt();
			
			int offset = -1;
			
			for (int i = count - 1; i >= 0; --i)
			{
				bytes = cs.readChunk(rec.getRID().chunk, (ChunkServer.ChunkSize - 4) - (count * 4), 4);
				offset = ByteBuffer.wrap(bytes).getInt();
				if (offset != -1)
				{
					rec.getRID().slot = i;
					break;
				}
			}
			
			if (rec.getRID().slot == -1)
			{
				break;
			}
						
			bytes = cs.readChunk(rec.getRID().chunk, offset, 4);
			int size = ByteBuffer.wrap(bytes).getInt();
			
			rec.setPayload(cs.readChunk(rec.getRID().chunk, offset + 4, size));
			
			return FSReturnVals.Success;
		}
		
		return FSReturnVals.Fail;
	}

	/**
	 * Reads the next record after the specified pivot of the file specified by
	 * ofh into payload Returns BadHandle if ofh is invalid Returns
	 * RecDoesNotExist if the file is empty or pivot is invalid
	 *
	 * Example usage: 1. ReadFirstRecord(FH1, tinyRec1) 2. ReadNextRecord(FH1,
	 * rec1, tinyRec2) 3. ReadNextRecord(FH1, rec2, tinyRec3)
	 */
	public FSReturnVals ReadNextRecord(FileHandle ofh, RID pivot, TinyRec rec)
	{
		if (!Master.get().ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		
		rec.setRID(new RID(pivot.chunk, -1));
		
		byte[] bytes = new byte[4];
		
		bytes = cs.readChunk(pivot.chunk, 0, 4);
		int count = ByteBuffer.wrap(bytes).getInt();
		
		int offset = -1;
		
		for (int i = pivot.slot + 1; i < count; ++i)
		{
			bytes = cs.readChunk(pivot.chunk, (ChunkServer.ChunkSize - 4) - (count * 4), 4);
			offset = ByteBuffer.wrap(bytes).getInt();
			if (offset != -1)
			{
				rec.getRID().slot = i;
				break;
			}
		}
		
		if (rec.getRID().slot == -1)
		{
			rec.setRID(null);
			return FSReturnVals.Fail;
		}
				
		bytes = cs.readChunk(rec.getRID().chunk, offset, 4);
		int size = ByteBuffer.wrap(bytes).getInt();
		
		rec.setPayload(cs.readChunk(rec.getRID().chunk, offset + 4, size));
		
		return FSReturnVals.Success;
	}

	/**
	 * Reads the previous record after the specified pivot of the file specified
	 * by ofh into payload Returns BadHandle if ofh is invalid Returns
	 * RecDoesNotExist if the file is empty or pivot is invalid
	 *
	 * Example usage: 1. ReadLastRecord(FH1, tinyRec1) 2. ReadPrevRecord(FH1,
	 * recn-1, tinyRec2) 3. ReadPrevRecord(FH1, recn-2, tinyRec3)
	 */
	public FSReturnVals ReadPrevRecord(FileHandle ofh, RID pivot, TinyRec rec)
	{
		if (!Master.get().ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		
		rec.setRID(new RID(pivot.chunk, -1));
		
		byte[] bytes = new byte[4];
		
		bytes = cs.readChunk(pivot.chunk, 0, 4);
		int count = ByteBuffer.wrap(bytes).getInt();
		
		int offset = -1;
		
		for (int i = pivot.slot - 1; i >= 0; --i)
		{
			bytes = cs.readChunk(pivot.chunk, (ChunkServer.ChunkSize - 4) - (count * 4), 4);
			offset = ByteBuffer.wrap(bytes).getInt();
			if (offset != -1)
			{
				rec.getRID().slot = i;
				break;
			}
		}
		
		if (rec.getRID().slot == -1)
		{
			rec.setRID(null);
			return FSReturnVals.Fail;
		}
		
		bytes = cs.readChunk(rec.getRID().chunk, offset, 4);
		int size = ByteBuffer.wrap(bytes).getInt();
		
		rec.setPayload(cs.readChunk(rec.getRID().chunk, offset + 4, size));
		
		return FSReturnVals.Success;
	}

}