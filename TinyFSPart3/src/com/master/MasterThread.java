package com.master;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.chunkserver.ChunkServer;
import com.network.Network;

public class MasterThread extends Thread {
	private Master master = null;
	private Socket socket = null;
	
	public MasterThread(Master m, Socket s) {
		super("MasterThread");
		this.master = m;
		this.socket = s;
	}
	
	public void run() {
		ObjectInputStream readInput = null;
		ObjectOutputStream writeOutput = null;
		
		try {
			readInput = new ObjectInputStream(socket.getInputStream());
			writeOutput = new ObjectOutputStream(socket.getOutputStream());
			
			// Use the input and output stream as long as the client is connected.
			while (!socket.isClosed()) {
				int payloadsize =  Network.ReadIntFromInputStream("Master", readInput);
				
				if (payloadsize == -1)
				{
					break;
				}
				
				int reqId = Network.ReadIntFromInputStream("Master", readInput);
				
				switch (reqId) {
					case Master.ReqCreateDir:
						HandleReqCreateDir(readInput, writeOutput);
						break;

					case Master.ReqDeleteDir:
						HandleReqDeleteDir(readInput, writeOutput);
						break;

					case Master.ReqRenameDir:
						HandleReqRenameDir(readInput, writeOutput);
						break;

					case Master.ReqListDir:
						HandleReqListDir(readInput, writeOutput);
						break;

					case Master.ReqCreateFile:
						HandleReqCreateFile(readInput, writeOutput);
						break;

					case Master.ReqDeleteFile:
						HandleReqDeleteFile(readInput, writeOutput);
						break;

					case Master.ReqOpenFile:
						HandleReqOpenFile(readInput, writeOutput);
						break;

					case Master.ReqCloseFile:
						HandleReqCloseFile(readInput, writeOutput);
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
	
	private void HandleReqCreateDir(ObjectInputStream readInput, ObjectOutputStream writeOutput)
	{
		/*
		int offset =  Network.ReadIntFromInputStream("Master", ReadInput);
		int payloadlength =  Network.ReadIntFromInputStream("Master", ReadInput);
		int chunkhandlesize = payloadsize - ChunkServer.PayloadSZ - ChunkServer.CMDlength - (2 * 4);
		if (chunkhandlesize < 0)
			System.out.println("Error in ChunkServer.java, ReadChunkCMD has wrong size.");
		byte[] CHinBytes = Network.RecvPayload("ChunkServer", ReadInput, chunkhandlesize);
		String ChunkHandle = (new String(CHinBytes)).toString();
		
		byte[] res = cs.readChunk(ChunkHandle, offset, payloadlength);
		
		if (res == null)
			WriteOutput.writeInt(ChunkServer.PayloadSZ);
		else {
			WriteOutput.writeInt(ChunkServer.PayloadSZ + res.length);
			WriteOutput.write(res);
		}
		WriteOutput.flush();
		break;
		*/
	}
	
	private void HandleReqDeleteDir(ObjectInputStream readInput, ObjectOutputStream writeOutput)
	{
		
	}
	
	private void HandleReqRenameDir(ObjectInputStream readInput, ObjectOutputStream writeOutput)
	{
		
	}
	
	private void HandleReqListDir(ObjectInputStream readInput, ObjectOutputStream writeOutput)
	{
		
	}
	
	private void HandleReqCreateFile(ObjectInputStream readInput, ObjectOutputStream writeOutput)
	{
		
	}
	
	private void HandleReqDeleteFile(ObjectInputStream readInput, ObjectOutputStream writeOutput)
	{
		
	}
	
	private void HandleReqOpenFile(ObjectInputStream readInput, ObjectOutputStream writeOutput)
	{
		
	}
	
	private void HandleReqCloseFile(ObjectInputStream readInput, ObjectOutputStream writeOutput)
	{
		
	}
}
