package com.interfaces;

import com.client.FileHandle;

public interface MasterInterface
{
	// FSReturnVals
	public static final int DirExists			= 1;	// Returned by CreateDir when directory exists
	public static final int	DirNotEmpty			= 2;	// Returned when a non-empty directory is deleted
	public static final int	SrcDirNotExistent	= 3;	// Returned when source directory does not exist
	public static final int	DestDirExists		= 4;	// Returned when a destination directory exists
	public static final int	FileExists			= 5;	// Returned when a file exists
	public static final int	FileDoesNotExist	= 6;	// Returns when a file does not exist
	public static final int	BadHandle			= 7;	// Returned when the handle for an open file is not valid
	public static final int	RecordTooLong		= 8;	// Returned when a record size is larger than chunk size
	public static final int	BadRecID			= 9;	// The specified RID is not valid, used by DeleteRecord
	public static final int	RecDoesNotExist		= 10;	// The specified record does not exist, used by DeleteRecord
	public static final int	NotImplemented		= 11;	// Specific to CSCI 485 and its unit tests
	public static final int	Success				= 12;	// Returned when a method succeeds
	public static final int	Fail				= 13;	// Returned when a method fails
	
	// Networking Communication Constants
	public static final int ReqCreateDir		= 101;
	public static final int ReqDeleteDir		= 102;
	public static final int ReqRenameDir		= 103;
	public static final int ReqListDir			= 104;
	
	public static final int ReqCreateFile		= 105;
	public static final int ReqDeleteFile		= 106;
	public static final int ReqOpenFile			= 107;
	public static final int ReqCloseFile		= 108;
	
	public static final int ReqAppendChunk		= 109;
	public static final int ReqGetFirstChunk	= 110;
	public static final int ReqGetLastChunk		= 111;
	public static final int ReqGetNextChunk		= 112;
	public static final int ReqGetPreviousChunk	= 113;
	
	// Master Location
	/**
	 * IP Addresses:
	 * Andre = 68.181.174.56
	 * Chris = 68.181.174.86
	 * Jessica = 68.181.174.114
	 * Tong = 68.181.174.43
	 */
	public static final String MasterIpAddr = "127.0.0.1";
	public static final int MasterPort = 3469;
	
	/**
	 * Appends a chunk to the file.
	 */
	public String AppendChunk(FileHandle file);
	
	/**
	 * Gets the first chunk's handle for a file.
	 */
	public String GetFirstChunk(FileHandle file);
	
	/**
	 * Gets the last chunk's handle for a file.
	 */
	public String GetLastChunk(FileHandle file);
	
	/**
	 * Gets the next chunk's handle for a file.
	 */
	public String GetNextChunk(FileHandle file, String pivot);
	
	/**
	 * Gets the previous chunk's handle for a file.
	 */
	public String GetPreviousChunk(FileHandle file, String pivot);
	
	/**
	 * Creates the specified dirname in the src directory Returns
	 * SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if the specified dirname exists
	 *
	 *
	 * Example usage: CreateDir("/", "Shahram"), CreateDir("/Shahram",
	 * "CSCI485"), CreateDir("/Shahram/CSCI485", "Lecture1")
	 */
	public int CreateDir(String src, String dirname);

	/**
	 * Deletes the specified dirname in the src directory Returns
	 * SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if the specified dirname exists
	 *
	 * Example usage: DeleteDir("/Shahram/CSCI485", "Lecture1")
	 */
	public int DeleteDir(String src, String dirname);

	/**
	 * Renames the specified src directory in the specified path to NewName
	 * Returns SrcDirNotExistent if the src directory does not exist Returns
	 * DestDirExists if a directory with NewName exists in the specified path
	 *
	 * Example usage: RenameDir("/Shahram/CSCI485", "CSCI550") changes
	 * "/Shahram/CSCI485" to "/Shahram/CSCI550"
	 */
	public int RenameDir(String src, String NewName);

	/**
	 * Lists the content of the target directory Returns SrcDirNotExistent if
	 * the target directory does not exist Returns null if the target directory
	 * is empty
	 *
	 * Example usage: ListDir("/Shahram/CSCI485")
	 */
	public String[] ListDir(String tgt);

	/**
	 * Creates the specified filename in the target directory Returns
	 * SrcDirNotExistent if the target directory does not exist Returns
	 * FileExists if the specified filename exists in the specified directory
	 *
	 * Example usage: Createfile("/Shahram/CSCI485/Lecture1", "Intro.pptx")
	 */
	public int CreateFile(String tgtdir, String filename);

	/**
	 * Deletes the specified filename from the tgtdir Returns SrcDirNotExistent
	 * if the target directory does not exist Returns FileDoesNotExist if the
	 * specified filename is not-existent
	 *
	 * Example usage: DeleteFile("/Shahram/CSCI485/Lecture1", "Intro.pptx")
	 */
	public int DeleteFile(String tgtdir, String filename);

	/**
	 * Opens the file specified by the FilePath and populates the FileHandle
	 * Returns FileDoesNotExist if the specified filename by FilePath is
	 * not-existent
	 *
	 * Example usage: OpenFile("/Shahram/CSCI485/Lecture1/Intro.pptx")
	 */
	public int OpenFile(String FilePath);

	/**
	 * Closes the specified file handle Returns BadHandle if ofh is invalid
	 *
	 * Example usage: CloseFile(FH1)
	 */
	public int CloseFile(String FilePath);
}
