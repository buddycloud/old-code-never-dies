package com.buddycloud.pubsub.subscriber;

public class SubscriptionChange {

	private String jid;
	private String nodename;
	private Subscriber subscriber;
	private SubscriptionChangeAction action;
	
	public SubscriptionChange(String jid, String node, Subscriber subs, SubscriptionChangeAction action) {
		this.jid = jid;
		this.nodename = node;
		this.subscriber = subs;
		this.action = action;
	}
	
	public String getJid() {
		return jid;
	}

	public String getNodename() {
		return nodename;
	}

	public Subscriber getSubscriber() {
		return subscriber;
	}

	public SubscriptionChangeAction getAction() {
		return action;
	}
	
	public String getActionAsString() {
		return this.action.toString();
	}
	
	public boolean isToInsert() {
		return this.action == SubscriptionChangeAction.insert ? true : false;
	}

	public boolean isToUpdate() {
		return this.action == SubscriptionChangeAction.update ? true : false;
	}
	
	public boolean isToDelete() {
		return this.action == SubscriptionChangeAction.delete ? true : false;
	}
}
