package com.buddycloud.pubsub.node;

public class LeafnodeChange {
	Leaf node;
	LeafnodeChangeAction action;
	
	public LeafnodeChange(Leaf node, LeafnodeChangeAction action) {
		this.node = node.clone();
		this.action = action;
	}
	
	public boolean isToInsert() {
		return this.action == LeafnodeChangeAction.insert ? true : false;
	}
	
	public boolean isToUpdate() {
		return this.action == LeafnodeChangeAction.update ? true : false;
	}
	
	public boolean isToDelete() {
		return this.action == LeafnodeChangeAction.delete ? true : false;
	}
	
	public String getActionAsString() {
		return this.action.toString();
	}
	
	public String getNodename() {
		return this.node.getNodeName();
	}
	
	public Leaf getNode() {
		return this.node;
	}
}
