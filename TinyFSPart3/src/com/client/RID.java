package com.client;

public class RID {
	String chunk;
	int slot;
	
	public RID() {
		chunk = null;
		slot = -1;
	}
	public RID(String chunkHandle, int slotNumber) {
		chunk = chunkHandle;
		slot = slotNumber;
	}
}
