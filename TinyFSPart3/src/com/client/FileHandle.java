package com.client;

import java.io.File;
import java.util.Arrays;

public class FileHandle {
	final static String filePath = "csci485/";
	String directory;
	String filename;
	String handle;
	
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
