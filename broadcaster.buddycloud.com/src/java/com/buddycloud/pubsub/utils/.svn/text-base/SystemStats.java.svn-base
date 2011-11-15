package com.buddycloud.pubsub.utils;

import org.xmpp.packet.JID;

public class SystemStats {

	private static SystemStats instance;
	
	private int bc_users = 0;
	private int other_users = 0;
	private int anon_users = 0;
	
	public static boolean ANONYMOUS = true;
	public static boolean KNOWN_USER = false;
	
	private SystemStats() {
		
	}
	
	public static SystemStats getInstance() {
		if(instance == null) {
			instance = new SystemStats();
		}
		return instance;
	}
	
	public void increase(JID jid, boolean isAnonymous) {
		if(isAnonymous) {
			this.anon_users++;
			return;
		}
		if(jid.getDomain().equals("buddycloud.com")) {
			this.bc_users++;
		} else {
			this.other_users++;
		}
	}
	
	public void decrease(JID jid, boolean isAnonymous) {
		if(isAnonymous) {
			this.anon_users--;
			return;
		}
		if(jid.getDomain().equals("buddycloud.com")) {
			this.bc_users--;
		} else {
			this.other_users--;
		}
	}
	
	public int getBc_users() {
		return bc_users;
	}

	public int getOther_users() {
		return other_users;
	}

	public int getAnon_users() {
		return anon_users;
	}
}
