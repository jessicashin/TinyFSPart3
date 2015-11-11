package com.client;

public class FileHandle {
	final static String filePath = "csci485/";
	String directory;
	String filename;
	String handle;
	
	public FileHandle() {
		directory = null;
		filename = null;
		handle = null;
	}
	
	public FileHandle(String tgtdir, String fn) {
		directory = tgtdir;
		filename = fn;
		handle = directory + "/" + filename;
	}
	
	public String get() {
		return handle;
	}
	
	public String getDirectory() {
		return directory;
	}
	
	public String getFilename() {
		return filename;
	}
}