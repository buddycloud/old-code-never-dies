package com.buddycloud.pubsub.managers;

import java.util.concurrent.LinkedBlockingQueue;

import com.buddycloud.pubsub.db.DBRoster;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.roster.RosterChange;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;
import com.buddycloud.pubsub.subscriber.SubscriberEntityType;

public class RosterManager {

	private static RosterManager instance;
	
	protected LinkedBlockingQueue<RosterChange> queue = new LinkedBlockingQueue<RosterChange>();
	
	Thread consumer;
	
	private RosterManager() {
		this.consumer = new Thread(new Consumer());
		this.consumer.start();
	}
	
	public static RosterManager getInstance() {
		if(instance == null) {
			instance = new RosterManager();
		}
		return instance;
	}
	
	public void addChange(RosterChange change) {
		try {
			this.queue.put(change);
		} catch (InterruptedException e) {
			LogMe.warning("InterruptedException on RosterChange: '" + e.getMessage() + "'!");
			e.printStackTrace();
		} catch (Exception e) {
			LogMe.warning("Error adding RosterChange: '" + e.getMessage() + "'!");
		}
	}
	
	private class Consumer implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				try {
					RosterChange change = queue.take();
					LogMe.debug("RosterManager is ready to '" + change.getActionAsString() + "' JID: '" + change.getJid() + "'.");
					
					if(change.isUpdateToLastSeen()) {
						DBRoster.getInstance().updateLastSeen(change.getJid());
					}else if(change.isToAdd()) {
						DBRoster.getInstance().insertJidToRoster(change.getJid(), SubscriberEntityType.normal);
						
					} else if (change.isToAddTemporary()) {
						DBRoster.getInstance().insertJidToRoster(change.getJid(), SubscriberEntityType.temporary);
						
					} else if (change.isUpdateAsNormal()) {
						DBRoster.getInstance().updateJidToNormal(change.getJid());
						
					} else if (change.isToBlock()) {
						DBRoster.getInstance().insertJidToRoster(change.getJid(), SubscriberEntityType.blocked);
						
					} else {
						SubscriberEntities.getInstance().removeEntity(change.getJid());
						DBRoster.getInstance().deleteJidFromRoster(change.getJid());
					}
				} catch (InterruptedException e) {
					LogMe.warning("Error consuming RosterChange: '" + e.getMessage() + "'!");
					e.printStackTrace();
				} catch (Exception e) {
					LogMe.warning("Error consuming RosterQueue: '" + e.getMessage() + "'!");
					//e.printStackTrace();
				}
			}
		}	
	}
	
}
