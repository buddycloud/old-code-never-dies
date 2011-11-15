package com.buddycloud.pubsub.managers;

import java.util.concurrent.LinkedBlockingQueue;

import com.buddycloud.pubsub.db.DBSubscription;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;
import com.buddycloud.pubsub.subscriber.SubscriptionChange;

public class SubscriptionManager {

	private static SubscriptionManager instance;
	
	protected LinkedBlockingQueue<SubscriptionChange> queue = new LinkedBlockingQueue<SubscriptionChange>();
	
	Thread consumer;
	
	private SubscriptionManager() {
		this.consumer = new Thread(new Consumer());
		this.consumer.start();
	}
	
	public static SubscriptionManager getInstance() {
		if(instance == null) {
			instance = new SubscriptionManager();
		}
		return instance;
	}
	
	public void addChange(SubscriptionChange change) {
		try {
			this.queue.put(change);
		} catch (InterruptedException e) {
			LogMe.warning("InterruptedException on SubscriptionChange: '" + e.getMessage() + "'!");
			e.printStackTrace();
		} catch (Exception e) {
			LogMe.warning("Error adding SubscriptionsQueue: '" + e.getMessage() + "'!");
		}
	}
	
	private class Consumer implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				try {
					SubscriptionChange change = queue.take();
					LogMe.debug("SubscriptionManager is ready to '" + change.getActionAsString() + "' subscription of jid: '" + change.getJid() + "' to node '" + change.getNodename() + "'.");
					if( change.isToInsert() ) {
						DBSubscription.getInstance().insertSubscription(change);
						if(!change.getSubscriber().isUnconfigured()) {
							SubscriberEntities.getInstance().getEntity(change.getJid()).addSubscription(change.getNodename());
						}
					} else if (change.isToUpdate()) {
						DBSubscription.getInstance().updateSubscription(change);
					} else {
						DBSubscription.getInstance().deleteSubscription(change);
						if(SubscriberEntities.getInstance().getEntity(change.getJid()) != null) {
							SubscriberEntities.getInstance().getEntity(change.getJid()).removeSubscription(change.getNodename());
						}
					}
				} catch (InterruptedException e) {
					LogMe.warning("Error consuming Subscription Change: '" + e.getMessage() + "'!");
					e.printStackTrace();
				} catch (Exception e) {
					LogMe.warning("Error consuming SubscriptionsQueue: '" + e.getMessage() + "'!");
					//e.printStackTrace();
				}
			}
		}
	}
}
