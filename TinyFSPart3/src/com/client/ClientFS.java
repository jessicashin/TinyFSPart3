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

	/**
	 * Creates the specified dirname in the src directory Returns
	 * SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if the specified dirname exists
	 *
	 * Example usage: CreateDir("/", "Shahram"), CreateDir("/Shahram",
	 * "CSCI485"), CreateDir("/Shahram/CSCI485", "Lecture1")
	 */
	public FSReturnVals CreateDir(String src, String dirname) {
		switch(Master.CreateDir(src, dirname)) {
		case FSReturnVals.Success:
			return FSReturnVals.Success;
		case FSReturnVals.SrcDirNotExistent:
			return FSReturnVals.SrcDirNotExistent;
		case FSReturnVals.DestDirExists:
			return FSReturnVals.DestDirExists;
		default:
			return FSReturnVals.Fail;
		}
		return null;
	}

	/**
	 * Deletes the specified dirname in the src directory Returns
	 * SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if the specified dirname exists
	 *
	 * Example usage: DeleteDir("/Shahram/CSCI485", "Lecture1")
	 */
	public FSReturnVals DeleteDir(String src, String dirname) {
		switch(Master.DeleteDir(src, dirname)) {
		case FSReturnVals.Success:
			return FSReturnVals.Success;
		case FSReturnVals.SrcDirNotExistent:
			return FSReturnVals.SrcDirNotExistent;
		case FSReturnVals.DirNotEmpty:
			return FSReturnVals.DirNotEmpty;
		default:
			return FSReturnVals.Fail;
		}
		return null;
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
		switch(Master.RenameDir(src, NewName)) {
		case FSReturnVals.Success:
			return FSReturnVals.Success;
		case FSReturnVals.SrcDirNotExistent:
			return FSReturnVals.SrcDirNotExistent;
		case FSReturnVals.DestDirExists:
			return FSReturnVals.DestDirExists;
		default:
			return FSReturnVals.Fail;
		}
		return null;
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
		String[]  targetDirContent = Master.ListDir(tgt);
		//if(targetDirContent[0].equals(FSReturnVals.SrcDirNotExistent)) { return FSReturnVals.SrcDirNotExistent; }
		if(targetDirContent[0].equals(FSReturnVals.SrcDirNotExistent)) { return targetDirContent; }
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
		switch(Master.CreateFile(tgtdir, filename)) {
		case FSReturnVals.Success:
			return FSReturnVals.Success;
		case FSReturnVals.SrcDirNotExistent:
			return FSReturnVals.SrcDirNotExistent;
		case FSReturnVals.FileExists:
			return FSReturnVals.FileExists;
		default:
			return FSReturnVals.Fail;
		}
		return null;
	}

	/**
	 * Deletes the specified filename from the tgtdir 
	 * Returns SrcDirNotExistent if the target directory does not exist 
	 * Returns FileDoesNotExist if the specified filename is not-existent
	 *
	 * Example usage: DeleteFile("/Shahram/CSCI485/Lecture1", "Intro.pptx")
	 */
	public FSReturnVals DeleteFile(String tgtdir, String filename) {
		switch(Master.RenameDir(tgtdir, filename)) {
		case FSReturnVals.Success:
			return FSReturnVals.Success;
		case FSReturnVals.SrcDirNotExistent:
			return FSReturnVals.SrcDirNotExistent;
		case FSReturnVals.FileDoesNotExist:
			return FSReturnVals.FileDoesNotExist;
		default:
			return FSReturnVals.Fail;
		}
		return null;
	}

	/**
	 * Opens the file specified by the FilePath and populates the FileHandle
	 * Returns FileDoesNotExist if the specified filename by FilePath is not-existent
	 *
	 * Example usage: OpenFile("/Shahram/CSCI485/Lecture1/Intro.pptx")
	 */
	public FSReturnVals OpenFile(String FilePath, FileHandle ofh) {
		switch(Master.OpenFile(FilePath, ofh)) {
		case FSReturnVals.Success:
			return FSReturnVals.Success;
		case FSReturnVals.FileDoesNotExist:
			return FSReturnVals.FileDoesNotExist;
		default:
			return FSReturnVals.Fail;
		}
		return null;
	}

	/**
	 * Closes the specified file handle
	 * Returns BadHandle if ofh is invalid
	 *
	 * Example usage: CloseFile(FH1)
	 */
	public FSReturnVals CloseFile(FileHandle ofh) {
		switch(Master.CloseFile(ofh)) {
		case FSReturnVals.Success:
			return FSReturnVals.Success;
		case FSReturnVals.BadHandle:
			return FSReturnVals.BadHandle;
		default:
			return FSReturnVals.Fail;
		}
		return null;
	}

}