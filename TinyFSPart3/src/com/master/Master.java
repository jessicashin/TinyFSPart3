package com.master;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;

import com.client.FileHandle;
import com.interfaces.MasterInterface;

public class Master implements MasterInterface
{
	public final static String NamespaceFile = "namespace.txt";
	public final static String FileChunksFile = "filechunks.txt";

	Hashtable<String, LinkedList<String>> namespace;
	Hashtable<String, LinkedList<String>> files;
	// TODO: Location of each chunk's replicas.
	
	void WriteStrHashTable(String file, Hashtable<String, LinkedList<String>> table)
	{
		try {
			PrintWriter outWrite = new PrintWriter(new FileOutputStream(file));
			Set<String> keys = table.keySet();
	        for(String key : keys)
	        {
	        	String str = key;
	        	LinkedList<String> values = table.get(key);
	        	boolean first = true;
	    		for(String value : values)
	        	{
	    			if(first)
	    			{
	    				str += ":";
	    				first = false;
	    			}
	    			else
	    			{
	    				str += ",";
	    			}
	    			
	        		str += value;
	        	}
	    		outWrite.println(str);
	        }
			outWrite.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void ReadStrHashTable(String file, Hashtable<String, LinkedList<String>> table)
	{
		try {
			BufferedReader binput = new BufferedReader(new FileReader(file));
			while(binput.ready())
			{
				String line = binput.readLine();
				if(line.indexOf(":") == -1)
				{
					table.put(line, new LinkedList<String>());
				}
				else
				{
					String[] result = line.split(":");
					LinkedList<String> list = new LinkedList<String>();
					table.put(result[0], list);
					if(result.length > 1)
					{
						String[] result2 = result[1].split(",");
						for(String str : result2)
						{
							list.push(str);
						}
					}
				}
			}
			binput.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void WriteMetadata()
	{
		// Brute force write of all metadata.		
		WriteStrHashTable(NamespaceFile, namespace);
		WriteStrHashTable(FileChunksFile, files);
	}
	
	void ReadMetadata()
	{
		// Read all of the metadata.
		ReadStrHashTable(NamespaceFile, namespace);
		ReadStrHashTable(FileChunksFile, files);
	}
	
	public static void main(String args[])
	{
		Master master = new Master();
		master.ReadMetadata();
	}

	@Override
	public String CreateChunk() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int CreateDir(String src, String dirname) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int DeleteDir(String src, String dirname) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int RenameDir(String src, String NewName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] ListDir(String tgt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int CreateFile(String tgtdir, String filename) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int DeleteFile(String tgtdir, String filename) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int OpenFile(String FilePath, FileHandle ofh) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int CloseFile(FileHandle ofh) {
		// TODO Auto-generated method stub
		return 0;
	}
}
