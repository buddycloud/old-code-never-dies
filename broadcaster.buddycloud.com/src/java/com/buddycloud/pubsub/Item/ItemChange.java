package com.buddycloud.pubsub.Item;

public class ItemChange {
	
	private ItemChangeAction action;
	private Entry entry;
	private String nodename;
	
	public ItemChange(String nodename, Entry entry, ItemChangeAction action) {
		this.nodename = nodename;
		this.entry = entry;
		this.action = action;
	}
	
	public boolean isToInsert() {
		return this.action == ItemChangeAction.insert ? true : false;
	}
	
	public boolean isToUpdate() {
		return this.action == ItemChangeAction.update ? true : false;
	}
	
	public boolean isToDelete() {
		return this.action == ItemChangeAction.delete ? true : false;
	}
	
	public String getActionAsString() {
		return this.action.toString();
	}
	
	public Entry getEntry() {
		return this.entry;
	}
	
	public String getNodename() {
		return nodename;
	}
}
