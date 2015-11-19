package com.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.client.FileHandle;
import com.network.Network;

public class MasterThread extends Thread {
	private Master master = null;
	private Socket socket = null;
	private ObjectInputStream readInput = null;
	private ObjectOutputStream writeOutput = null;
	
	public MasterThread(Master m, Socket s) {
		super("MasterThread");
		this.master = m;
		this.socket = s;
	}
	
	public void run() {		
		try {
			readInput = new ObjectInputStream(socket.getInputStream());
			writeOutput = new ObjectOutputStream(socket.getOutputStream());
			
			// Use the input and output stream as long as the client is connected.
			while (!socket.isClosed()) {
				int reqId = Network.ReadIntFromInputStream("Master", readInput);
				
				if (reqId == -1)
				{
					break;
				}
				
				switch (reqId) {
					case Master.ReqCreateDir:
						HandleReqCreateDir();
						break;

					case Master.ReqDeleteDir:
						HandleReqDeleteDir();
						break;

					case Master.ReqRenameDir:
						HandleReqRenameDir();
						break;

					case Master.ReqListDir:
						HandleReqListDir();
						break;

					case Master.ReqCreateFile:
						HandleReqCreateFile();
						break;

					case Master.ReqDeleteFile:
						HandleReqDeleteFile();
						break;

					case Master.ReqOpenFile:
						HandleReqOpenFile();
						break;

					case Master.ReqCloseFile:
						HandleReqCloseFile();
						break;
						
					case Master.ReqAppendChunk:
						HandleReqAppendChunk();
						break;
						
					case Master.ReqGetFirstChunk:
						HandleReqGetFirstChunk();
						break;
						
					case Master.ReqGetLastChunk:
						HandleReqGetLastChunk();
						break;
						
					case Master.ReqGetNextChunk:
						HandleReqGetNextChunk();
						break;
						
					case Master.ReqGetPreviousChunk:
						HandleReqGetPreviousChunk();
						break;
	
					default:
						System.out.println("Error in Master: Specified Request ID "+reqId+" is not recognized.");
						break;
				}
			}
		} catch (IOException ex){
			System.out.println("Client Disconnected");
		} finally {
			try {
				if (socket != null)
					socket.close();
				if (readInput != null)
					readInput.close();
				if (writeOutput != null)
					writeOutput.close();
			} catch (IOException fex){
				System.out.println("Error (Master):  Failed to close either a valid connection or its input/output stream.");
				fex.printStackTrace();
			}
		}
	}
	
	private void HandleReqCreateDir()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String src = (new String(bytes)).toString();
		
		length = Network.ReadIntFromInputStream("Master", readInput);
		bytes = Network.RecvPayload("Master", readInput, length);
		String dirname = (new String(bytes)).toString();
		
		int result = master.CreateDir(src, dirname);
		
		try {
			writeOutput.writeInt(result);
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqDeleteDir()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String src = (new String(bytes)).toString();
		
		length = Network.ReadIntFromInputStream("Master", readInput);
		bytes = Network.RecvPayload("Master", readInput, length);
		String dirname = (new String(bytes)).toString();
		
		int result = master.DeleteDir(src, dirname);
		
		try {
			writeOutput.writeInt(result);
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqRenameDir()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String src = (new String(bytes)).toString();
		
		length = Network.ReadIntFromInputStream("Master", readInput);
		bytes = Network.RecvPayload("Master", readInput, length);
		String NewName = (new String(bytes)).toString();
		
		int result = master.RenameDir(src, NewName);
		
		try {
			writeOutput.writeInt(result);
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqListDir()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String tgt = (new String(bytes)).toString();
		
		String[] results = master.ListDir(tgt);
		
		try {
			if (results == null)
			{
				writeOutput.writeInt(-1);
			}
			else
			{
				writeOutput.writeInt(results.length);
				for(int i = 0; i < results.length; ++i)
				{
					byte[] resultBytes = results[i].getBytes();
					writeOutput.writeInt(resultBytes.length);
					writeOutput.write(resultBytes);
				}
			}
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqCreateFile()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String tgtdir = (new String(bytes)).toString();
		
		length = Network.ReadIntFromInputStream("Master", readInput);
		bytes = Network.RecvPayload("Master", readInput, length);
		String filename = (new String(bytes)).toString();
		
		int result = master.CreateFile(tgtdir, filename);
		
		try {
			writeOutput.writeInt(result);
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqDeleteFile()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String tgtdir = (new String(bytes)).toString();
		
		length = Network.ReadIntFromInputStream("Master", readInput);
		bytes = Network.RecvPayload("Master", readInput, length);
		String filename = (new String(bytes)).toString();
		
		int result = master.DeleteFile(tgtdir, filename);
		
		try {
			writeOutput.writeInt(result);
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqOpenFile()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String FilePath = (new String(bytes)).toString();
		
		int result = master.OpenFile(FilePath);
		
		try {
			writeOutput.writeInt(result);
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqCloseFile()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String FilePath = (new String(bytes)).toString();
		
		int result = master.CloseFile(FilePath);
		
		try {
			writeOutput.writeInt(result);
			writeOutput.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqAppendChunk()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String file = (new String(bytes)).toString();
		
		String result = master.AppendChunk(new FileHandle(file));
		
		try {
			if (result == null)
			{
				writeOutput.writeInt(-1);
			}
			else
			{
				byte[] resultBytes = result.getBytes();
				writeOutput.writeInt(resultBytes.length);
				writeOutput.write(resultBytes);
			}
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqGetFirstChunk()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String file = (new String(bytes)).toString();
		
		String result = master.GetFirstChunk(new FileHandle(file));
		
		try {
			if (result == null)
			{
				writeOutput.writeInt(-1);
			}
			else
			{
				byte[] resultBytes = result.getBytes();
				writeOutput.writeInt(resultBytes.length);
				writeOutput.write(resultBytes);
			}
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqGetLastChunk()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String file = (new String(bytes)).toString();
		
		String result = master.GetLastChunk(new FileHandle(file));
		
		try {
			if (result == null)
			{
				writeOutput.writeInt(-1);
			}
			else
			{
				byte[] resultBytes = result.getBytes();
				writeOutput.writeInt(resultBytes.length);
				writeOutput.write(resultBytes);
			}
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqGetNextChunk()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String file = (new String(bytes)).toString();
		
		length = Network.ReadIntFromInputStream("Master", readInput);
		bytes = Network.RecvPayload("Master", readInput, length);
		String chunk = (new String(bytes)).toString();
		
		String result = master.GetNextChunk(new FileHandle(file), chunk);
		
		try {
			if (result == null)
			{
				writeOutput.writeInt(-1);
			}
			else
			{
				byte[] resultBytes = result.getBytes();
				writeOutput.writeInt(resultBytes.length);
				writeOutput.write(resultBytes);
			}
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void HandleReqGetPreviousChunk()
	{
		int length = Network.ReadIntFromInputStream("Master", readInput);
		byte[] bytes = Network.RecvPayload("Master", readInput, length);
		String file = (new String(bytes)).toString();
		
		length = Network.ReadIntFromInputStream("Master", readInput);
		bytes = Network.RecvPayload("Master", readInput, length);
		String chunk = (new String(bytes)).toString();
		
		String result = master.GetPreviousChunk(new FileHandle(file), chunk);
		
		try {
			if (result == null)
			{
				writeOutput.writeInt(-1);
			}
			else
			{
				byte[] resultBytes = result.getBytes();
				writeOutput.writeInt(resultBytes.length);
				writeOutput.write(resultBytes);
			}
			writeOutput.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
