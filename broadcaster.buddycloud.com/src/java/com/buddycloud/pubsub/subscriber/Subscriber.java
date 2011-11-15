package com.buddycloud.pubsub.subscriber;

public class Subscriber {
	
	private Affiliation affiliation = null;
	private Subscription subscribtion = null;
	
	private boolean affiliationRequested = false;
	
	public Subscriber(Affiliation affiliation, Subscription subscription) {
		this.affiliation = affiliation;
		this.subscribtion = subscription;
	}
	
	public boolean isOutcast() {
		return this.affiliation.equals(Affiliation.outcast);
	}
	
	public boolean isOwner() {
		return this.affiliation.equals(Affiliation.owner);
	}
	
	public boolean isModerator() {
		return this.affiliation.equals(Affiliation.moderator);
	}
	
	public boolean isPublisher() {
		return this.affiliation.equals(Affiliation.publisher);
	}
	
	public boolean isMember() {
		return this.affiliation.equals(Affiliation.member);
	}
	
	public boolean isSubscribed() {
		return this.subscribtion.equals(Subscription.subscribed);
	}
	
	public boolean isUnconfigured() {
		return this.subscribtion.equals(Subscription.unconfigured);
	}
	
	public boolean isNone() {
		if(this.subscribtion.equals(Subscription.none) && this.subscribtion.equals(Affiliation.none)) {
			return true;
		}
		return false;
	}
	
	public String getSubscriptionAsString() {
		return this.subscribtion.toString();
	}
	
	public String getAffiliationAsString() {
		return this.affiliation.toString();
	}
	
	public boolean isAllowedToReceiveItems() {
		if((this.isOwner() || this.isModerator() || this.isPublisher() || this.isMember()) && this.isSubscribed() ) {
			return true;
		}
		return false;
	}
	
	public boolean isAffiliationRequested() {
		return this.affiliationRequested;
	}
	
	public void setAffiliationRequested() {
		this.affiliationRequested = true;
	}
	
	public void resetAffiliationRequest() {
		this.affiliationRequested = false;
	}
}
