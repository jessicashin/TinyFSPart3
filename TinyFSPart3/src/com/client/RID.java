package com.client;

public class RID {
	String chunk;
	int offset;
	int length;
	int slot;
	
	public RID() {
		chunk = null;
		offset = -1;
		length = -1;
		slot = -1;
	}
	
	public RID(String chunkHandle, int chunkOffset, int byteLength, int slotNumber) {
		chunk = chunkHandle;
		offset = chunkOffset;
		length = byteLength;
		slot = slotNumber;
	}
}
