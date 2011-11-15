package com.buddycloud.pubsub.roster;

public class RosterChange {

	private String jid;
	private RosterChangeAction action;
	
	public RosterChange(String jid, RosterChangeAction action) {
		this.jid = jid;
		this.action = action;
	}
	
	public String getJid(){
		return this.jid;
	}
	
	public String getActionAsString() {
		return this.action.toString();
	}
	
	public boolean isToAdd() {
		return this.action == RosterChangeAction.add ? true : false;
	}
	
	public boolean isToAddTemporary() {
		return this.action == RosterChangeAction.addTemporary ? true : false;
	}
	
	public boolean isUpdateAsNormal() {
		return this.action == RosterChangeAction.updateAsNormal ? true : false;
	}
	
	public boolean isToBlock() {
		return this.action == RosterChangeAction.block ? true : false;
	}
	
	public boolean isUpdateToLastSeen() {
		return this.action == RosterChangeAction.updateLastSeen ? true : false;
	}
}
