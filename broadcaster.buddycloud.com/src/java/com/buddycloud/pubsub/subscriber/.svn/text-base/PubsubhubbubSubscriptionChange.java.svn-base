package com.buddycloud.pubsub.subscriber;

import com.buddycloud.pubsub.log.LogMe;

public class PubsubhubbubSubscriptionChange {
	
	private String nodename;
	private String topic;
	private String verify_token;
	private String lease_seconds;
	private String secret;
	private String callback;
	private long goesold;
	private PubsubhubbubSubscriptionChangeAction action;
	
	public PubsubhubbubSubscriptionChange(String nodename, String topic, String verify_token, String lease_seconds, String secret, String callback, PubsubhubbubSubscriptionChangeAction action) {
		this.nodename = nodename;
		this.topic = topic;
		this.verify_token = verify_token;
		this.lease_seconds = lease_seconds;
		this.secret = secret;
		this.callback = callback;
		this.action = action;
		
		if(this.lease_seconds != null && !this.lease_seconds.equals("")) {
			LogMe.debug("Lease seconds is different than empty: '" + this.lease_seconds + "'.");
			long sec = Long.parseLong(this.lease_seconds);
			this.goesold = System.currentTimeMillis() +sec;
		}
	}
	
	public long getGoesold() {
		return this.goesold;
	}
	
	public String getNodename() {
		return nodename;
	}

	public String getTopic() {
		return topic;
	}

	public String getVerify_token() {
		return verify_token;
	}

	public String getLease_seconds() {
		return lease_seconds;
	}

	public String getSecret() {
		return secret;
	}

	public String getCallback() {
		return callback;
	}
	
	public boolean isToInsert() {
		return this.action == PubsubhubbubSubscriptionChangeAction.insert ? true : false;
	}
	
	public boolean isToDelete() {
		return this.action == PubsubhubbubSubscriptionChangeAction.delete ? true : false;
	}
	
	public boolean isToUpdate() {
		return this.action == PubsubhubbubSubscriptionChangeAction.update ? true : false;
	}
	
	public String getActionAsString() {
		return this.action.toString();
	}
}
