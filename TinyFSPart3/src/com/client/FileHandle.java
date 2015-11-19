package com.client;

import java.io.Serializable;

public class FileHandle {
	public String handle;
	
	public FileHandle() {
		handle = null;
	}
	
	public FileHandle(String h) {
		handle = h;
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