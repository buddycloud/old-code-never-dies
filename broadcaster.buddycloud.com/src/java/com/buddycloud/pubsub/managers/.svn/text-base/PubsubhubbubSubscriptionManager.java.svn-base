package com.buddycloud.pubsub.managers;

import java.util.concurrent.LinkedBlockingQueue;

import com.buddycloud.pubsub.db.DBPubsubhubbubSubscriptions;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.subscriber.PubsubhubbubSubscriptionChange;

public class PubsubhubbubSubscriptionManager {

	private static PubsubhubbubSubscriptionManager instance;
	
	protected LinkedBlockingQueue<PubsubhubbubSubscriptionChange> queue = new LinkedBlockingQueue<PubsubhubbubSubscriptionChange>();
	
	Thread consumer;
	
	private PubsubhubbubSubscriptionManager() {
		this.consumer = new Thread(new Consumer());
		this.consumer.start();
	}
	
	public static PubsubhubbubSubscriptionManager getInstance() {
		if(instance == null) {
			instance = new PubsubhubbubSubscriptionManager();
		}
		return instance;
	}
	
	public void addChange(PubsubhubbubSubscriptionChange change) {
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
					PubsubhubbubSubscriptionChange change = queue.take();
					LogMe.debug("PubsubhubbubSubscriptionManager is ready to '" + change.getActionAsString() + "' callbackURL '" + change.getCallback() + "' to/of/from node '" + change.getNodename() + "'.");
					if( change.isToInsert() ) {
						DBPubsubhubbubSubscriptions.getInstance().insertSubscription(change);
						//DBSubscription.getInstance().insertSubscription(change);
						//if(!change.getSubscriber().isUnconfigured()) {
						//	SubscriberEntities.getInstance().getEntity(change.getJid()).addSubscription(change.getNodename());
						//}
					} else if (change.isToUpdate()) {
						//DBSubscription.getInstance().updateSubscription(change);
					} else if (change.isToDelete()) {
						DBPubsubhubbubSubscriptions.getInstance().deleteSubscription(change);
					}
				} catch (InterruptedException e) {
					LogMe.warning("Error consuming PubsubhubbubSubscriptionChange Queue: '" + e.getMessage() + "'!");
					e.printStackTrace();
				} catch (Exception e) {
					LogMe.warning("Error consuming PubsubhubbubSubscriptionChange Queue: '" + e.getMessage() + "'!");
					//e.printStackTrace();
				}
			}
		}
	}
}
