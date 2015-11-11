package com.client;

import java.io.File;
import java.util.Arrays;

public class FileHandle {
	final static String filePath = "csci485/";
	public static long counter;
	long handle;
	
	
	public FileHandle() {
		File dir = new File(filePath);
		File[] fs = dir.listFiles();
		
		if(fs.length == 0){
			counter = 0;
		} else {
			int num = numFiles(fs, 0);
			long[] allHandles = new long[num];
			
			findFiles(fs, allHandles);
			Arrays.sort(allHandles);
			counter = allHandles[num - 1];
		}
		handle = counter++;
	}
	
	public static int numFiles(File[] files, int num) {
		for (File file : files) {
			if (file.isDirectory()) {
				numFiles(file.listFiles(), num);
			} else { num++; }
		}
		return num;
	}
	
	public static void findFiles(File[] start, long[] end) {
		for (File file : start) {
			int i = 0;
			if (file.isDirectory()) {
				findFiles(file.listFiles(), end);
			} else {
				end[i] = Long.valueOf(file.getName());
				i++;
			}
		}
	}
	
	public long getHandle() {
		return handle;
	}
	
}
