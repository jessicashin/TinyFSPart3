package com.master;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;

import com.client.FileHandle;
import com.interfaces.MasterInterface;

public class Master implements MasterInterface
{
	public final static String NamespaceFile = "namespace.txt";
	public final static String FileChunksFile = "filechunks.txt";

	private Hashtable<String, LinkedList<String>> namespace = new Hashtable<String, LinkedList<String>>();
	private Hashtable<String, LinkedList<String>> files = new Hashtable<String, LinkedList<String>>();
	// TODO: Location of each chunk's replicas.
	
	public Master()
	{
		namespace.put("/", new LinkedList<String>());
	}
	
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
		//Master master = new Master();
		//master.ReadMetadata();
	}
	
	public String CreateChunk() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int CreateDir(String src, String dirname)
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
		if(src == "/") src = "";
		namespace.put(src + "/" + dirname, new LinkedList<String>());
		
		return Success;
	}

	public int DeleteDir(String src, String dirname)
	{
		src = SanitizeStr(src);
		
		LinkedList<String> vals = namespace.get(src);
		
		if (vals == null)
		{
			return SrcDirNotExistent;
		}
		
		if (src == "/") src = "";
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

	public int RenameDir(String src, String NewName)
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

	public String[] ListDir(String tgt)
	{
		ArrayList<String> retVal = ListDirRecursive(tgt);
		
		if (retVal == null)
		{
			return null;
		}
		
		return retVal.toArray(new String[retVal.size()]);
	}
	
	private ArrayList<String> ListDirRecursive(String tgt)
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

	public int CreateFile(String tgtdir, String filename)
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
		
		return Success;
	}

	public int DeleteFile(String tgtdir, String filename)
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
		
		return Success;
	}

	public int OpenFile(String FilePath, FileHandle ofh)
	{
		return 0;
	}

	public int CloseFile(FileHandle ofh)
	{
		return 0;
	}
	
	private String SanitizeStr(String src)
	{
		if (src != "/" && src.endsWith("/"))
		{
			return src.substring(0, src.length() - 1);
		}
		
		return src;
	}
}
