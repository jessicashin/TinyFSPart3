package com.client;

import java.io.Serializable;

public class FileHandle implements Serializable {
	final static String root = "csci485";
	public String directory;
	public String filename;
	public String handle;
	
	public FileHandle() {
		directory = null;
		filename = null;
		handle = null;
	}
	
	public FileHandle(String tgtdir, String fn) {
		directory = tgtdir;
		filename = fn;
		if (tgtdir == "/") {
			handle = tgtdir + fn;
		} else {
			handle = tgtdir + "/" + fn;
		}
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