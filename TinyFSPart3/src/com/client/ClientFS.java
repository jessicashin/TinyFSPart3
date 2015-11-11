package com.client;

import com.master.Master;

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
	private Master m=null;
	
	public ClientFS() {
		m = new Master();
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
		switch(m.CreateDir(src, dirname)) {
			case 12:
				return FSReturnVals.Success;
			case 13:
				return FSReturnVals.Fail;
			case 3:
				return FSReturnVals.SrcDirNotExistent;
			case 1:
				return FSReturnVals.DirExists;
			default:
				return FSReturnVals.Fail;
		}
	}

	/**
	 * Deletes the specified dirname in the src directory Returns
	 * SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if the specified dirname exists
	 *
	 * Example usage: DeleteDir("/Shahram/CSCI485", "Lecture1")
	 */
	public FSReturnVals DeleteDir(String src, String dirname) {
		switch(m.DeleteDir(src, dirname)) {
			case 12:
				return FSReturnVals.Success;
			case 13:
				return FSReturnVals.Fail;
			case 3:
				return FSReturnVals.SrcDirNotExistent;
			case 2:
				return FSReturnVals.DirNotEmpty;
			default:
				return FSReturnVals.Fail;
		}
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
		switch(m.RenameDir(src, NewName)) {
			case 12:
				return FSReturnVals.Success;
			case 13:
				return FSReturnVals.Fail;
			case 3:
				return FSReturnVals.SrcDirNotExistent;
			case 4:
				return FSReturnVals.DestDirExists;
			default:
				return FSReturnVals.Fail;
		}
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
		String[]  targetDirContent = m.ListDir(tgt);
		//if(targetDirContent[0].equals(FSReturnVals.SrcDirNotExistent)) { return FSReturnVals.SrcDirNotExistent; }
		if(targetDirContent[0].equals(3)) { return targetDirContent; }
		if(targetDirContent.length == 0) { return null; }
		return targetDirContent;
	}

	/**
	 * Creates the specified filename in the target directory 
	 * Returns SrcDirNotExistent if the target directory does not exist 
	 * Returns FileExists if the specified filename exists in the specified directory
	 *
	 * Example usage: Createfile("/Shahram/CSCI485/Lecture1", "Intro.pptx")
	 */
	public FSReturnVals CreateFile(String tgtdir, String filename) {
		switch(m.CreateFile(tgtdir, filename)) {
			case 12:
				return FSReturnVals.Success;
			case 13:
				return FSReturnVals.Fail;
			case 3:
				return FSReturnVals.SrcDirNotExistent;
			case 5:
				return FSReturnVals.FileExists;
			default:
				return FSReturnVals.Fail;
		}
	}

	/**
	 * Deletes the specified filename from the tgtdir 
	 * Returns SrcDirNotExistent if the target directory does not exist 
	 * Returns FileDoesNotExist if the specified filename is not-existent
	 *
	 * Example usage: DeleteFile("/Shahram/CSCI485/Lecture1", "Intro.pptx")
	 */
	public FSReturnVals DeleteFile(String tgtdir, String filename) {
		switch(m.RenameDir(tgtdir, filename)) {
			case 12:
				return FSReturnVals.Success;
			case 13:
				return FSReturnVals.Fail;
			case 3:
				return FSReturnVals.SrcDirNotExistent;
			case 6:
				return FSReturnVals.FileDoesNotExist;
			default:
				return FSReturnVals.Fail;
		}
	}

	/**
	 * Opens the file specified by the FilePath and populates the FileHandle
	 * Returns FileDoesNotExist if the specified filename by FilePath is not-existent
	 *
	 * Example usage: OpenFile("/Shahram/CSCI485/Lecture1/Intro.pptx")
	 */
	public FSReturnVals OpenFile(String FilePath, FileHandle ofh) {
		switch(m.OpenFile(FilePath, ofh)) {
			case 12:
				return FSReturnVals.Success;
			case 13:
				return FSReturnVals.Fail;
			case 6:
				return FSReturnVals.FileDoesNotExist;
			default:
				return FSReturnVals.Fail;
		}
	}

	/**
	 * Closes the specified file handle
	 * Returns BadHandle if ofh is invalid
	 *
	 * Example usage: CloseFile(FH1)
	 */
	public FSReturnVals CloseFile(FileHandle ofh) {
		switch(m.CloseFile(ofh)) {
			case 12:
				return FSReturnVals.Success;
			case 13:
				return FSReturnVals.Fail;
			case 7:
				return FSReturnVals.BadHandle;
			default:
				return FSReturnVals.Fail;
		}
	}

}
