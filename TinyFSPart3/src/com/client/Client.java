package com.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import com.chunkserver.ChunkServer;
import com.interfaces.ClientInterface;
import com.master.Master;
import com.network.Network;

/**
 * implementation of interfaces at the client side
 * @author Shahram Ghandeharizadeh
 *
 */
public class Client implements ClientInterface {
	static Socket ClientSocket;
	static ObjectOutputStream WriteOutput;
	static ObjectInputStream ReadInput;
	
	static Socket ChunkSocket;
	static ObjectOutputStream ChunkWriteOutput;
	static ObjectInputStream ChunkReadInput;
	
	/**
	 * Initialize the client  FileNotFoundException
	 */
	public Client() {
		if (ClientSocket != null) return; //The client is already connected
		if (ChunkSocket != null) return;
		try {
			ClientSocket = new Socket(Master.MasterIpAddr, Master.MasterPort);
			WriteOutput = new ObjectOutputStream(ClientSocket.getOutputStream());
			ReadInput = new ObjectInputStream(ClientSocket.getInputStream());
			
			ChunkSocket = new Socket(ChunkServer.ChunkServerIpAddr, ChunkServer.ChunkServerPort);
			ChunkWriteOutput = new ObjectOutputStream(ChunkSocket.getOutputStream());
			ChunkReadInput = new ObjectInputStream(ChunkSocket.getInputStream());
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a chunk at the chunk server from the client side.
	 */
	public void createChunk(String ChunkHandle) {
		try {
			byte[] CHinBytes = ChunkHandle.getBytes();
			
			ChunkWriteOutput.writeInt(ChunkServer.PayloadSZ + ChunkServer.CMDlength + 4 + CHinBytes.length);
			ChunkWriteOutput.writeInt(ChunkServer.CreateChunkCMD);
			ChunkWriteOutput.writeInt(CHinBytes.length);
			ChunkWriteOutput.write(CHinBytes);
			ChunkWriteOutput.flush();
			
			int payloadSize =  Network.ReadIntFromInputStream("Client", ChunkReadInput);
		} catch (IOException e) {
			System.out.println("Error in Client.createChunk:  Failed to create a chunk.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Write a chunk at the chunk server from the client side.
	 */
	public boolean writeChunk(String ChunkHandle, byte[] payload, int offset) {
		if(offset + payload.length > ChunkServer.ChunkSize){
			System.out.println("The chunk write should be within the range of the file, invalide chunk write!");
			return false;
		}
		try {
			byte[] CHinBytes = ChunkHandle.getBytes();
			
			ChunkWriteOutput.writeInt(ChunkServer.PayloadSZ + ChunkServer.CMDlength + (2*4) + payload.length + CHinBytes.length);
			ChunkWriteOutput.writeInt(ChunkServer.WriteChunkCMD);
			ChunkWriteOutput.writeInt(offset);
			ChunkWriteOutput.writeInt(payload.length);
			ChunkWriteOutput.write(payload);
			ChunkWriteOutput.write(CHinBytes);
			ChunkWriteOutput.flush();
			
			int result =  Network.ReadIntFromInputStream("Client", ChunkReadInput);
			if (result == ChunkServer.FALSE) return false;
			return true;
		} catch (IOException e) {
			System.out.println("Error in Client.createChunk:  Failed to create a chunk.");
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Read a chunk at the chunk server from the client side.
	 */
	public byte[] readChunk(String ChunkHandle, int offset, int NumberOfBytes) {
		if(NumberOfBytes + offset > ChunkServer.ChunkSize){
			System.out.println("The chunk read should be within the range of the file, invalide chunk read!");
			return null;
		}
		
		try {
			byte[] CHinBytes = ChunkHandle.getBytes();
			ChunkWriteOutput.writeInt(ChunkServer.PayloadSZ + ChunkServer.CMDlength + (2*4) + CHinBytes.length);
			ChunkWriteOutput.writeInt(ChunkServer.ReadChunkCMD);
			ChunkWriteOutput.writeInt(offset);
			ChunkWriteOutput.writeInt(NumberOfBytes);
			ChunkWriteOutput.write(CHinBytes);
			ChunkWriteOutput.flush();
			
			int ChunkSize =  Network.ReadIntFromInputStream("Client", ChunkReadInput);
			ChunkSize -= ChunkServer.PayloadSZ;  //reduce the length by the first four bytes that identify the length
			byte[] payload = Network.RecvPayload("Client", ChunkReadInput, ChunkSize); 
			return payload;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	


}
