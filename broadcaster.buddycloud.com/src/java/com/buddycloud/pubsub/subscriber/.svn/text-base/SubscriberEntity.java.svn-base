package com.buddycloud.pubsub.subscriber;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.utils.SystemStats;

public class SubscriberEntity {

	public JID jid;
	public Presence.Type presence;
	
	public List<String> subscriptions = new LinkedList<String>();

	private LinkedHashSet<Resource> resources = new LinkedHashSet<Resource>();
	
	private SubscriberEntityType type = SubscriberEntityType.normal;
	
	public SubscriberEntity(JID jid, Presence.Type presence) {
		this.jid = jid;
		this.presence = presence;
	}
	
	public SubscriberEntity(JID jid, Presence.Type presence, SubscriberEntityType type) {
		this.jid = jid;
		this.presence = presence;
		this.type = type;
	}
	
	public boolean addResouce(Resource resource) {
		if(resource == null)
			return false;
		LogMe.debug("Adding resource '" + resource + "' for user '" + this.jid.toBareJID() +"'.");
		return this.resources.add(resource);
	}
	
	public void removeResouce(String resource) {
		if(resource == null)
			return;
		
		Resource tbr = null;
		for (Resource r : this.resources) {
			if(r.resource.equals(resource)) {
				tbr = r;
			}
		}
		if(tbr != null) {
			LogMe.debug("Removing resource '" + tbr.resource + "' from user '" + this.jid.toBareJID() +"'.");
			this.resources.remove(tbr);
			SystemStats.getInstance().decrease(this.jid, SystemStats.KNOWN_USER);
		} else {
			LogMe.warning("TRYING TO REMOVE RESOUCE THAT WAS NOT FOUND. User was propably not using BC compatible client.");
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Resource> getResources() {
		return (Collection<Resource>)this.resources.clone();
	}
	
	public int amountOfResources() {
		return this.resources.size();
	}

	
	public void addSubscription(String node) {
		if(!this.subscriptions.contains(node))
			this.subscriptions.add(node);
	}
	
	public void removeSubscription(String node) {
		this.subscriptions.remove(node);
	}
	
	public LinkedList<String> getSubscribedNodes(){
		// We return copy here.
		return new LinkedList<String>(this.subscriptions);
	}
	
	public boolean isOnline() {
		return this.presence == null ? true : false;
	}
	
	public boolean isTemporarySubscription() {
		return this.type == SubscriberEntityType.temporary ? true : false;
	}
	
	public void setSubscriberEntityType(SubscriberEntityType type) {
		this.type = type;
	}
}
