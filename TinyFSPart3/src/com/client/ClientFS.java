package com.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.interfaces.MasterInterface;
import com.master.Master;
import com.network.Network;
import com.sun.xml.internal.ws.util.StringUtils;

public class ClientFS {

	public enum FSReturnVals {
		DirExists, // Returned by CreateDir when directory exists
		DirNotEmpty, //Returned when a non-empty directory is deleted
		SrcDirNotExistent, // Returned when source directory does not exist
		DestDirExists, // Returned when a destination directory exists
		FileExists, // Returned when a file exists
		FileDoesNotExist, // Returns when a file does not exist
		BadHandle, // Returned when the handle for an open file is not valid
		RecordTooLong, // Returned when a record size is larger than chunk size
		BadRecID, // The specified RID is not valid, used by DeleteRecord
		RecDoesNotExist, // The specified record does not exist, used by DeleteRecord
		NotImplemented, // Specific to CSCI 485 and its unit tests
		Success, //Returned when a method succeeds
		Fail //Returned when a method fails
	}
	
	public FSReturnVals TranslateMasterRetVal(int val)
	{
		switch(val)
		{
			case MasterInterface.DirExists:
				return FSReturnVals.DirExists;
			case MasterInterface.DirNotEmpty:
				return FSReturnVals.DirNotEmpty;
			case MasterInterface.SrcDirNotExistent:
				return FSReturnVals.SrcDirNotExistent;
			case MasterInterface.DestDirExists:
				return FSReturnVals.DestDirExists;
			case MasterInterface.FileExists:
				return FSReturnVals.FileExists;
			case MasterInterface.FileDoesNotExist:
				return FSReturnVals.FileDoesNotExist;
			case MasterInterface.BadHandle:
				return FSReturnVals.BadHandle;
			case MasterInterface.RecordTooLong:
				return FSReturnVals.RecordTooLong;
			case MasterInterface.BadRecID:
				return FSReturnVals.BadRecID;
			case MasterInterface.RecDoesNotExist:
				return FSReturnVals.RecDoesNotExist;
			case MasterInterface.NotImplemented:
				return FSReturnVals.NotImplemented;
			case MasterInterface.Success:
				return FSReturnVals.Success;
			case MasterInterface.Fail:
				return FSReturnVals.Fail;
		}
		
		return FSReturnVals.Fail;
	}
	
	Client client = null;
	
	public ClientFS() {
		client = new Client();
	}

	/**
	 * Creates the specified dirname in the src directory Returns
	 * SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if the specified dirname exists
	 *
	 * Example usage: CreateDir("/", "Shahram"), CreateDir("/Shahram",
	 * "CSCI485"), CreateDir("/Shahram/CSCI485", "Lecture1")
	 */
	public FSReturnVals CreateDir(String src, String dirname) {
		try {
			Client.WriteOutput.writeInt(Master.ReqCreateDir);
		
			byte[] bytes = src.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			bytes = dirname.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int result = Network.ReadIntFromInputStream("ClientFS", Client.ReadInput);
			
			return TranslateMasterRetVal(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return TranslateMasterRetVal(MasterInterface.Fail);
	}

	/**
	 * Deletes the specified dirname in the src directory Returns
	 * SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if the specified dirname exists
	 *
	 * Example usage: DeleteDir("/Shahram/CSCI485", "Lecture1")
	 */
	public FSReturnVals DeleteDir(String src, String dirname) {
		try {
			Client.WriteOutput.writeInt(Master.ReqDeleteDir);
		
			byte[] bytes = src.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			bytes = dirname.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int result = Network.ReadIntFromInputStream("ClientFS", Client.ReadInput);
			
			return TranslateMasterRetVal(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return TranslateMasterRetVal(MasterInterface.Fail);
	}

	/**
	 * Renames the specified src directory in the specified path to NewName
	 * Returns SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if a directory with NewName exists in the specified path
	 *
	 * Example usage: RenameDir("/Shahram/CSCI485", "CSCI550") changes
	 * "/Shahram/CSCI485" to "/Shahram/CSCI550"
	 */
	//pass to master
	public FSReturnVals RenameDir(String src, String NewName) {
		try {
			Client.WriteOutput.writeInt(Master.ReqRenameDir);
		
			byte[] bytes = src.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			bytes = NewName.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int result = Network.ReadIntFromInputStream("ClientFS", Client.ReadInput);
			
			return TranslateMasterRetVal(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return TranslateMasterRetVal(MasterInterface.Fail);
	}

	/**
	 * Lists the content of the target directory 
	 * XXXXXXXXXXXXXXXXXXXXXXXX   HOW   XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 * Returns SrcDirNotExistent if the target directory does not exist 
	 * XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	 * Returns null if the target directory is empty
	 *
	 * Example usage: ListDir("/Shahram/CSCI485")
	 */
	//pass to master
	public String[] ListDir(String tgt) {
		try {
			Client.WriteOutput.writeInt(Master.ReqListDir);
		
			byte[] bytes = tgt.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int numStr = Network.ReadIntFromInputStream("ClientFS", Client.ReadInput);
			if(numStr == -1)
			{
				return null;
			}
			else
			{
				String[] results = new String[numStr];
				for(int i = 0; i < numStr; ++i)
				{
					int length = Network.ReadIntFromInputStream("ClientFS", Client.ReadInput);
					byte[] resultBytes = Network.RecvPayload("ClientFS", Client.ReadInput, length);
					results[i] = (new String(resultBytes)).toString();
				}
				return results;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Creates the specified filename in the target directory 
	 * Returns SrcDirNotExistent if the target directory does not exist 
	 * Returns FileExists if the specified filename exists in the specified directory
	 *
	 * Example usage: Createfile("/Shahram/CSCI485/Lecture1", "Intro.pptx")
	 */
	public FSReturnVals CreateFile(String tgtdir, String filename) {
		try {
			Client.WriteOutput.writeInt(Master.ReqCreateFile);
		
			byte[] bytes = tgtdir.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			bytes = filename.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int result = Network.ReadIntFromInputStream("ClientFS", Client.ReadInput);
			
			return TranslateMasterRetVal(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return TranslateMasterRetVal(MasterInterface.Fail);
	}

	/**
	 * Deletes the specified filename from the tgtdir 
	 * Returns SrcDirNotExistent if the target directory does not exist 
	 * Returns FileDoesNotExist if the specified filename is not-existent
	 *
	 * Example usage: DeleteFile("/Shahram/CSCI485/Lecture1", "Intro.pptx")
	 */
	public FSReturnVals DeleteFile(String tgtdir, String filename) {
		try {
			Client.WriteOutput.writeInt(Master.ReqDeleteFile);
		
			byte[] bytes = tgtdir.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			bytes = filename.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int result = Network.ReadIntFromInputStream("ClientFS", Client.ReadInput);
			
			return TranslateMasterRetVal(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return TranslateMasterRetVal(MasterInterface.Fail);
	}

	/**
	 * Opens the file specified by the FilePath and populates the FileHandle
	 * Returns FileDoesNotExist if the specified filename by FilePath is not-existent
	 *
	 * Example usage: OpenFile("/Shahram/CSCI485/Lecture1/Intro.pptx")
	 */
	public FSReturnVals OpenFile(String FilePath, FileHandle ofh) {
		try {
			Client.WriteOutput.writeInt(Master.ReqOpenFile);
		
			byte[] bytes = FilePath.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int result = Network.ReadIntFromInputStream("ClientFS", Client.ReadInput);
			
			if (result == Master.Success)
			{
				ofh.handle = FilePath;
			}
			
			return TranslateMasterRetVal(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return TranslateMasterRetVal(MasterInterface.Fail);
	}

	/**
	 * Closes the specified file handle
	 * Returns BadHandle if ofh is invalid
	 *
	 * Example usage: CloseFile(FH1)
	 */
	public FSReturnVals CloseFile(FileHandle ofh) {
		try {
			Client.WriteOutput.writeInt(Master.ReqCloseFile);
		
			byte[] bytes = ofh.handle.getBytes();
			Client.WriteOutput.writeInt(bytes.length);
			Client.WriteOutput.write(bytes);
			
			Client.WriteOutput.flush();
			
			int result = Network.ReadIntFromInputStream("ClientFS", Client.ReadInput);
			
			return TranslateMasterRetVal(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return TranslateMasterRetVal(MasterInterface.Fail);
	}

}
