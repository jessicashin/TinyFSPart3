package com.client;

import java.io.Serializable;

public class FileHandle implements Serializable {
	public String handle;
	
	public FileHandle() {
		handle = null;
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
	public void setDirectory(String dir){
		this.directory=dir; 
	}
	public void setFilename (String  filename){
		this.filename=filename; 
	}
}