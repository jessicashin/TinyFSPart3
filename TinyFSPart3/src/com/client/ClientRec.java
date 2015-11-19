package com.client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import com.chunkserver.ChunkServer;
import com.client.ClientFS.FSReturnVals;
import com.interfaces.MasterInterface;
import com.master.Master;
import com.network.Network;

public class ClientRec {

	ChunkServer cs = new ChunkServer();
	
	Client client = null;
	
	public ClientRec() {
		client = new Client();
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
		/*
		if (!ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		*/
		if(GetLastChunk(ofh) == null)
		{
			cs.createChunk(AppendChunk(ofh));
		}
		
		RecordID.chunk = GetLastChunk(ofh);
		
		byte[] bytes = new byte[4];
		
		// Read number of records.
		bytes = cs.readChunk(RecordID.chunk, 0, 4);
		RecordID.slot = ByteBuffer.wrap(bytes).getInt();
		
		// Read next available offset.
		bytes = cs.readChunk(RecordID.chunk, 4, 4);
		int offset = ByteBuffer.wrap(bytes).getInt();
		
		// Make sure there's enough room.
		// offset + record header + record slot + payload size
		if (offset + 4 + 4 + payload.length > ((ChunkServer.ChunkSize - 4) - (RecordID.slot * 4)))
		{
			RecordID.chunk = AppendChunk(ofh);
			cs.createChunk(RecordID.chunk);
			RecordID.slot = 0;
			offset = 8;
		}
		
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
		/*
		if (!ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		*/
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
		/*
		if (!ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		*/
		rec.setRID(new RID(GetFirstChunk(ofh), -1));
		
		while (rec.getRID().chunk != null)
		{
			byte[] bytes = new byte[4];
			
			bytes = cs.readChunk(rec.getRID().chunk, 0, 4);
			int count = ByteBuffer.wrap(bytes).getInt();
			
			int offset = -1;
			
			for (int i = 0; i < count; ++i)
			{
				bytes = cs.readChunk(rec.getRID().chunk, (ChunkServer.ChunkSize - 4) - (i * 4), 4);
				offset = ByteBuffer.wrap(bytes).getInt();
				if (offset != -1)
				{
					rec.getRID().slot = i;
					break;
				}
			}
			
			if (offset == -1)
			{
				rec.getRID().chunk = GetNextChunk(ofh, rec.getRID().chunk);
				continue;
			}
						
			bytes = cs.readChunk(rec.getRID().chunk, offset, 4);
			int size = ByteBuffer.wrap(bytes).getInt();
			
			rec.setPayload(cs.readChunk(rec.getRID().chunk, offset + 4, size));
			
			return FSReturnVals.Success;
		}
		
		rec.setRID(null);
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
		/*
		if (!ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		*/
		rec.setRID(new RID(GetLastChunk(ofh), -1));
		
		while (rec.getRID().chunk != null)
		{
			byte[] bytes = new byte[4];
			
			bytes = cs.readChunk(rec.getRID().chunk, 0, 4);
			int count = ByteBuffer.wrap(bytes).getInt();
			
			int offset = -1;
			
			for (int i = count - 1; i >= 0; --i)
			{
				bytes = cs.readChunk(rec.getRID().chunk, (ChunkServer.ChunkSize - 4) - (i * 4), 4);
				offset = ByteBuffer.wrap(bytes).getInt();
				if (offset != -1)
				{
					rec.getRID().slot = i;
					break;
				}
			}
			
			if (offset == -1)
			{
				rec.getRID().chunk = GetPreviousChunk(ofh, rec.getRID().chunk);
				continue;
			}
						
			bytes = cs.readChunk(rec.getRID().chunk, offset, 4);
			int size = ByteBuffer.wrap(bytes).getInt();
			
			rec.setPayload(cs.readChunk(rec.getRID().chunk, offset + 4, size));
			
			return FSReturnVals.Success;
		}
		
		rec.setRID(null);
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
		/*
		if (!ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		*/
		rec.setRID(new RID(pivot.chunk, pivot.slot + 1));
		
		while (rec.getRID().chunk != null)
		{
			byte[] bytes = new byte[4];
			
			bytes = cs.readChunk(rec.getRID().chunk, 0, 4);
			int count = ByteBuffer.wrap(bytes).getInt();
			
			int offset = -1;
			
			for (int i = rec.getRID().slot; i < count; ++i)
			{
				bytes = cs.readChunk(rec.getRID().chunk, (ChunkServer.ChunkSize - 4) - (i * 4), 4);
				offset = ByteBuffer.wrap(bytes).getInt();
				if (offset != -1)
				{
					rec.getRID().slot = i;
					break;
				}
			}
			
			if (offset == -1)
			{
				rec.getRID().chunk = GetNextChunk(ofh, rec.getRID().chunk);
				rec.getRID().slot = 0;
				continue;
			}
					
			bytes = cs.readChunk(rec.getRID().chunk, offset, 4);
			int size = ByteBuffer.wrap(bytes).getInt();
			
			rec.setPayload(cs.readChunk(rec.getRID().chunk, offset + 4, size));
			
			return FSReturnVals.Success;
		}
		
		rec.setRID(null);
		return FSReturnVals.Fail;
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
		/*
		if (!ValidFileHandle(ofh))
		{
			return FSReturnVals.BadHandle;
		}
		*/
		rec.setRID(new RID(pivot.chunk, pivot.slot - 1));
		
		while (rec.getRID().chunk != null)
		{
			byte[] bytes = new byte[4];
			
			int offset = -1;
			
			for (int i = rec.getRID().slot; i >= 0; --i)
			{
				bytes = cs.readChunk(rec.getRID().chunk, (ChunkServer.ChunkSize - 4) - (i * 4), 4);
				offset = ByteBuffer.wrap(bytes).getInt();
				if (offset != -1)
				{
					rec.getRID().slot = i;
					break;
				}
			}
			
			if (offset == -1)
			{
				rec.getRID().chunk = GetPreviousChunk(ofh, rec.getRID().chunk);
				
				if (rec.getRID().chunk == null)
				{
					break;
				}
				
				bytes = cs.readChunk(rec.getRID().chunk, 0, 4);
				rec.getRID().slot = ByteBuffer.wrap(bytes).getInt() - 1;
				
				continue;
			}
			
			bytes = cs.readChunk(rec.getRID().chunk, offset, 4);
			int size = ByteBuffer.wrap(bytes).getInt();
			
			rec.setPayload(cs.readChunk(rec.getRID().chunk, offset + 4, size));
			
			return FSReturnVals.Success;
		}
		
		rec.setRID(null);
		return FSReturnVals.Fail;
	}

	
	/**
	 * Interface with Master.
	 */
	
	private String AppendChunk(FileHandle file)
	{
		try {
			Client.WriteOutput.writeInt(Master.ReqAppendChunk);
		
			byte[] bytes = file.handle.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int length = Network.ReadIntFromInputStream("ClientRec", Client.ReadInput);
			if (length == -1)
			{
				return null;
			}
			byte[] resultBytes = Network.RecvPayload("ClientRec", Client.ReadInput, length);
			String result = (new String(resultBytes)).toString();
			
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String GetFirstChunk(FileHandle file)
	{
		try {
			Client.WriteOutput.writeInt(Master.ReqGetFirstChunk);
		
			byte[] bytes = file.handle.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int length = Network.ReadIntFromInputStream("ClientRec", Client.ReadInput);
			if (length == -1)
			{
				return null;
			}
			byte[] resultBytes = Network.RecvPayload("ClientRec", Client.ReadInput, length);
			String result = (new String(resultBytes)).toString();
			
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String GetLastChunk(FileHandle file)
	{
		try {
			Client.WriteOutput.writeInt(Master.ReqGetLastChunk);
		
			byte[] bytes = file.handle.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int length = Network.ReadIntFromInputStream("ClientRec", Client.ReadInput);
			if (length == -1)
			{
				return null;
			}
			byte[] resultBytes = Network.RecvPayload("ClientRec", Client.ReadInput, length);
			String result = (new String(resultBytes)).toString();
			
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String GetNextChunk(FileHandle file, String pivot)
	{
		try {
			Client.WriteOutput.writeInt(Master.ReqGetNextChunk);
		
			byte[] bytes = file.handle.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			bytes = pivot.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int length = Network.ReadIntFromInputStream("ClientRec", Client.ReadInput);
			if (length == -1)
			{
				return null;
			}
			byte[] resultBytes = Network.RecvPayload("ClientRec", Client.ReadInput, length);
			String result = (new String(resultBytes)).toString();
			
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String GetPreviousChunk(FileHandle file, String pivot)
	{
		try {
			Client.WriteOutput.writeInt(Master.ReqGetPreviousChunk);
		
			byte[] bytes = file.handle.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			bytes = pivot.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int length = Network.ReadIntFromInputStream("ClientRec", Client.ReadInput);
			if (length == -1)
			{
				return null;
			}
			byte[] resultBytes = Network.RecvPayload("ClientRec", Client.ReadInput, length);
			String result = (new String(resultBytes)).toString();
			
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}