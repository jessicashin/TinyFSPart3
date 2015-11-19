package com.master;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.client.ClientFS.FSReturnVals;
import com.client.FileHandle;
import com.interfaces.MasterInterface;

public class Master implements MasterInterface
{
	public static long ChunkCounter = 1;
	
	public final static String NamespaceFile = "namespace.txt";
	public final static String FileChunksFile = "filechunks.txt";

	private Hashtable<String, LinkedList<String>> namespace = new Hashtable<String, LinkedList<String>>();
	private Hashtable<String, LinkedList<String>> files = new Hashtable<String, LinkedList<String>>();
	// TODO: Location of each chunk's replicas.
	
	static Master instance = null;
	
	static public Master get()
	{
		if(instance == null)
		{
			instance = new Master();
		}
		return instance;
	}
	
	public Master()
	{
		//ReadMetadata();
		if (namespace.get("/") == null)
		{
			namespace.put("/", new LinkedList<String>());
		}
	}
	
	private synchronized void WriteStrHashTable(String file, Hashtable<String, LinkedList<String>> table)
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
	
	private synchronized void ReadStrHashTable(String file, Hashtable<String, LinkedList<String>> table)
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
	
	private synchronized void WriteMetadata()
	{
		// Brute force write of all metadata.		
		WriteStrHashTable(NamespaceFile, namespace);
		WriteStrHashTable(FileChunksFile, files);
	}
	
	private synchronized void ReadMetadata()
	{
		// Read all of the metadata.
		ReadStrHashTable(NamespaceFile, namespace);
		ReadStrHashTable(FileChunksFile, files);
	}
	
	public synchronized boolean ValidFileHandle(FileHandle file) {		
		LinkedList<String> vals = namespace.get(file.getDirectory());
		
		if (vals == null)
		{
			return false;
		}
		
		if (!vals.contains(file.getFilename()))
		{
			return false;
		}
		
		return true;
	}
	
	public synchronized boolean ValidChunkHandle(FileHandle file, String chunk) {
		LinkedList<String> chunks = files.get(file.get());
		if (chunks.contains(chunk)) {
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized String AppendChunk(FileHandle file)
	{
		LinkedList<String> chunks = files.get(file.get());
		
		if (chunks == null)
		{
			return null;
		}
		
		String newChunk = String.valueOf(++ChunkCounter);
		chunks.add(newChunk);
		
		return newChunk;
	}
	
	public synchronized String GetFirstChunk(FileHandle file)
	{
		LinkedList<String> chunks = files.get(file.get());
		
		if (chunks == null)
		{
			return null;
		}
		
		return chunks.getFirst();
	}
	
	public synchronized String GetLastChunk(FileHandle file)
	{
		LinkedList<String> chunks = files.get(file.get());
		
		if (chunks == null)
		{
			return null;
		}
		
		if (chunks.isEmpty())
		{
			return null;
		}
		
		return chunks.getLast();
	}
	
	public synchronized String GetNextChunk(FileHandle file, String pivot)
	{
		LinkedList<String> chunks = files.get(file.get());
		
		if (chunks == null)
		{
			return null;
		}
		
		int index = chunks.indexOf(pivot);
		
		if (index == -1 || index == chunks.size() - 1)
		{
			return null;
		}
		
		return chunks.get(index + 1);
	}
	
	public synchronized String GetPreviousChunk(FileHandle file, String pivot)
	{
		LinkedList<String> chunks = files.get(file.get());
		
		if (chunks == null)
		{
			return null;
		}
		
		int index = chunks.indexOf(pivot);
		
		if (index == -1 || index == 0)
		{
			return null;
		}
		
		return chunks.get(index - 1);
	}
	
	public synchronized int CreateDir(String src, String dirname)
	{
		src = SanitizeStr(src);
		
		LinkedList<String> vals = namespace.get(src);
		
		if (vals == null)
		{
			return SrcDirNotExistent;
		}
		
		if (vals.contains(dirname))
		{
			return DestDirExists;
		}
		
		vals.add(dirname);
		if(src.length() == 1) src = "";
		namespace.put(src + "/" + dirname, new LinkedList<String>());
		
		return Success;
	}

	public synchronized int DeleteDir(String src, String dirname)
	{
		src = SanitizeStr(src);
		
		LinkedList<String> vals = namespace.get(src);
		
		if (vals == null)
		{
			return SrcDirNotExistent;
		}
		
		if (src.length() == 1) src = "";
		LinkedList<String> dirVals = namespace.get(src + "/" + dirname);
		
		if (dirVals == null)
		{
			return SrcDirNotExistent;
		}
		
		if (!dirVals.isEmpty())
		{
			return DirNotEmpty;
		}
		
		vals.remove(dirname);
		namespace.remove(src + "/" + dirname);
		
		return Success;
	}

	public synchronized int RenameDir(String src, String NewName)
	{
		src = SanitizeStr(src);
		NewName = SanitizeStr(NewName);
		
		String parent = src.substring(0, src.lastIndexOf("/"));
		if (parent.isEmpty()) parent = "/";
		LinkedList<String> vals = namespace.get(parent);
		
		if (vals == null)
		{
			return SrcDirNotExistent;
		}
		
		if (vals.contains(NewName))
		{
			return DestDirExists;
		}
		
		// Only rename the directory if it's empty.
		LinkedList<String> dirVals = namespace.get(src);
		
		if (dirVals == null)
		{
			return SrcDirNotExistent;
		}
		
		String oldName = src.substring(src.lastIndexOf("/") + 1, src.length());
		String newName = NewName.substring(NewName.lastIndexOf("/") + 1, NewName.length());
		vals.remove(oldName);
		vals.add(newName);
		
		LinkedList<String> tempList = new LinkedList<String>();
		tempList.addAll(namespace.get(src));
		namespace.remove(src);
		namespace.put(NewName, tempList);
		
		src += "/";
		NewName += "/";
		
		Set<String> keySet = new HashSet<String>();
		keySet.addAll(namespace.keySet());
		for(String key : keySet)
		{
			if(key.contains(src))
			{
				LinkedList<String> list = new LinkedList<String>();
				list.addAll(namespace.get(key));
				namespace.remove(key);
				key.replace(src, NewName);
				namespace.put(key, list);
			}
		}
		
		return Success;
	}

	public synchronized String[] ListDir(String tgt)
	{
		ArrayList<String> retVal = ListDirRecursive(tgt);
		
		if (retVal == null)
		{
			return null;
		}
		
		return retVal.toArray(new String[retVal.size()]);
	}
	
	private synchronized ArrayList<String> ListDirRecursive(String tgt)
	{
		tgt = SanitizeStr(tgt);
		
		LinkedList<String> vals = namespace.get(tgt);
		
		if (vals == null)
		{
			return null;
		}
		
		if (vals.isEmpty())
		{
			return null;
		}
		
		ArrayList<String> retVal = new ArrayList<String>();
		
		for(String val : vals)
		{
			if(namespace.get(tgt + "/" + val) != null)
			{
				String dir = tgt + "/" + val;
				retVal.add(dir);
				
				ArrayList<String> recDir = ListDirRecursive(dir);
				if(recDir != null)
				{
					retVal.addAll(recDir);
				}
			}
		}
		
		return retVal;
	}

	public synchronized int CreateFile(String tgtdir, String filename)
	{
		tgtdir = SanitizeStr(tgtdir);
		
		LinkedList<String> vals = namespace.get(tgtdir);
		
		if (vals == null)
		{
			return SrcDirNotExistent;
		}
		
		if (vals.contains(filename))
		{
			return FileExists;
		}
		
		vals.add(filename);
		files.put(tgtdir + "/" + filename, new LinkedList<String>());
		
		return Success;
	}

	public synchronized int DeleteFile(String tgtdir, String filename)
	{
		tgtdir = SanitizeStr(tgtdir);
		
		LinkedList<String> vals = namespace.get(tgtdir);
		
		if (vals == null)
		{
			return SrcDirNotExistent;
		}
		
		if (!vals.contains(filename))
		{
			return FileDoesNotExist;
		}
		
		vals.remove(filename);
		files.remove(tgtdir + "/" + filename);
		
		return Success;
	}

	public synchronized int OpenFile(String FilePath, String handle)
	{
		/*
		int count = FilePath.length() - FilePath.replace("/", "").length();
		if(count<1) {
			return BadHandle;
		} else if(count==1) {
//			handle="/";
//			ofh.directory="/"; 
//			ofh.filename=FilePath.substring(1, FilePath.length());
//			ofh.handle=FilePath; 
			return FileDoesNotExist;
		} else {*/
//			int lastSlash=FilePath.lastIndexOf('/'); 
			//ofh= new FileHandle (FilePath.substring(0, lastSlash), FilePath.substring(lastSlash+1, FilePath.length())); 
//			handle=FilePath;
//			ofh.directory=FilePath.substring(0, lastSlash); 
//			ofh.filename=FilePath.substring(lastSlash+1, FilePath.length()); 
//			ofh.setDirectory(FilePath.substring(0, lastSlash));
//			ofh.setFilename(FilePath.substring(lastSlash+1, FilePath.length()));
//			ofh.handle=FilePath;
			if(files.containsKey(FilePath))
				return Success;
			else
				return FileDoesNotExist;
		//}

//		System.out.println(ofh.get()+" "+ofh.getDirectory()+" "+ofh.getFilename());
		
//		LinkedList<String> vals = namespace.get(ofh.getDirectory());
//		if ((vals == null) || (!vals.contains(ofh.getDirectory())))
//		{
//			return FileDoesNotExist;
//		}
		
//		return Success;
	}

	public synchronized int CloseFile(FileHandle ofh)
	{
		return 0;
	}
	
	private synchronized String SanitizeStr(String src)
	{
		if (src.endsWith("/") && src.length() > 1)
		{
			return src.substring(0, src.length() - 1);
		}
		
		return src;
	}
	
	public static void ReadAndProcessRequests()
	{
		Master master = new Master();
		
		// Used for communication via the network.
		ServerSocket socket = null;
		
		try {
			socket = new ServerSocket(MasterPort);
		} catch (IOException ex) {
			System.out.println("Error: Failed to open a new socket to listen on.");
			ex.printStackTrace();
		}
		
		while (true) {
			try {
				new MasterThread(master, socket.accept()).start();
				System.out.println("START: New MasterThread.");
			} catch (IOException e) {
				System.out.println("Error: Socket unable to be accepted.");
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[])
	{
		System.out.println("Starting Master...");
		ReadAndProcessRequests();
	}
}
