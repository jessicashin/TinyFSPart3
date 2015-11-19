package com.client;

public class RID {
	private String chunk;
	private int slot;
	
	public RID() {
		chunk = null;
		slot = -1;
	}
	public RID(String chunkHandle, int slotNumber) {
		chunk = chunkHandle;
		slot = slotNumber;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public String getChunk() {
		return chunk;
	}
}
