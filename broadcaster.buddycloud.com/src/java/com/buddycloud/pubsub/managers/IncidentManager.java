package com.buddycloud.pubsub.managers;

import java.util.Collection;
import java.util.LinkedList;

import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.roster.RosterChange;
import com.buddycloud.pubsub.roster.RosterChangeAction;
import com.buddycloud.pubsub.utils.Helpers;

public class IncidentManager {

	private Collection<String> blockedJIDs = new LinkedList<String>();
	
	private Collection<String> adminJIDs = new LinkedList<String>();
	
	private static IncidentManager instance;
	
	private IncidentManager(Collection<String> adminJIDs) {
		this.adminJIDs = adminJIDs;
	}
	
	public static void initialize() {
		if(instance == null) {
			instance = new IncidentManager(Helpers.stringToCollection(Conf.getInstance().getConfString(Conf.ADMINS_KEY, 
																											Conf.ADMINS_DEFAULT)));
		}
	}
	
	public static IncidentManager getInstance() {
		return instance;
	}
	
	public void addBlockedJID(String bareJID) {
		this.blockedJIDs.add(bareJID);
		RosterManager.getInstance().addChange(new RosterChange(bareJID, RosterChangeAction.block));
	}
	
	public void putBlockedJID(String bareJID) {
		this.blockedJIDs.add(bareJID);
		//RosterManager.getInstance().addChange(new RosterChange(bareJID, RosterChangeAction.block));
	}
	
	public boolean isAdminJID(String bareJID) {
		return this.adminJIDs.contains(bareJID);
	}
	
	public boolean isBlockedJID(String bareJID) {
		return this.blockedJIDs.contains(bareJID);
	}
}
