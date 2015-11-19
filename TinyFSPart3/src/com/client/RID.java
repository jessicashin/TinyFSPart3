package com.client;

public class RID {
	public String chunk;
	public int slot;
	
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
	public void setSlot(int s){
	this.slot=s; 	
	}
	public void setChunk (String chunk){
		this.chunk=chunk; 
	}
	public String getChunk() {
		return chunk;
	}
}
