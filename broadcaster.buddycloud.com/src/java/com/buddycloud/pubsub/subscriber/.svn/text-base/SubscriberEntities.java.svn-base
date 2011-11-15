package com.buddycloud.pubsub.subscriber;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import com.buddycloud.pubsub.log.LogMe;

public class SubscriberEntities {

	public static SubscriberEntities instance;
	
	private ConcurrentHashMap<String, SubscriberEntity> entities = new ConcurrentHashMap<String, SubscriberEntity>();
	
	private SubscriberEntities() {
		
	}
	
	public static SubscriberEntities getInstance(){
		if(instance == null) {
			instance = new SubscriberEntities();
		}
		return instance;
	}
	
	public SubscriberEntity addEntity(SubscriberEntity se) {
		return this.entities.putIfAbsent(se.jid.toBareJID(), se);
	}
	
	public SubscriberEntity getEntity(String bareJid) {
		return this.entities.get(bareJid);
	}
	
	public void removeEntity(String bareJid) {
		this.entities.remove(bareJid);
	}
	
	public boolean subscriberExists(String bareJID) {
		return this.entities.containsKey(bareJID);
	}
	
	public static boolean entityIsOnline(String jid) {
		if(jid == null ||
		   SubscriberEntities.getInstance().getEntity(jid) == null ||
		   !SubscriberEntities.getInstance().getEntity(jid).isOnline()) {
			return false;
		}
		return true;
	}
	
	public Collection<Resource> getResources(String bareJID) {
		if(SubscriberEntities.getInstance().getEntity(bareJID) == null) {
			return new LinkedHashSet<Resource>();
		}
		return (Collection<Resource>) SubscriberEntities.getInstance().getEntity(bareJID).getResources();
	}
	
	public HashMap<String, ResourceType> getDeliveryResources(String jid) {
		HashMap<String, ResourceType> jids = new HashMap<String, ResourceType>();
		int i = 0;
		for (Resource resource : SubscriberEntities.getInstance().getResources(jid)) {
			String fullJID = jid + "/" + resource.resource;
			LogMe.debug("Adding fullJID '" + fullJID + "' to delivered JIDs.");
			jids.put(fullJID, resource.type);
			i++;
		}
		
		return jids;
	}
	
	public Collection<String> getDeliveryJids(String jid) {
		Collection<String> jids = new LinkedList<String>();
		int i = 0;
		for (Resource resource : SubscriberEntities.getInstance().getResources(jid)) {
			if(resource.type == ResourceType.iq_pubsub) {
				String fullJID = jid + "/" + resource.resource;
				LogMe.debug("Adding fullJID '" + fullJID + "' to delivered JIDs.");
				jids.add(fullJID);
				i++;
			}
		}
		if(i == 0) {
			LogMe.debug("NO RESOURCES FOR USER! Adding bareJID '" + jid + "' to delivered JIDs.");
			jids.add(jid);
		}
		return jids;
	}
	
	public LinkedList<String> getSubscribedNodes(String bareJid) {
		if(SubscriberEntities.getInstance().getEntity(bareJid) != null) {
			return SubscriberEntities.getInstance().getEntity(bareJid).getSubscribedNodes();
		}
		return new LinkedList<String>();
	}
	
	public void setAsNormal(String bareJid) {
		this.entities.get(bareJid).setSubscriberEntityType(SubscriberEntityType.normal);
	}
}
