package com.client;

import java.io.Serializable;

public class FileHandle {
	public String handle;
	public String directory; 
	public String filename; 
	public FileHandle() {
		handle = null;
		directory =null; 
		filename=null; 
		
	}
	
	public FileHandle(String tgtdir, String fn) {
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
		int slash = handle.lastIndexOf("/");
		return handle.substring(0, slash);
	}
	
	public String getFilename() {
		int slash = handle.lastIndexOf("/");
		return handle.substring(slash + 1, handle.length());
	}
}