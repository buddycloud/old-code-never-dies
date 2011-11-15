package com.buddycloud.pubsub.managers;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import org.jivesoftware.whack.util.StringUtils;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketExtension;

import com.buddycloud.pubsub.BuddycloudPubsubComponent;
import com.buddycloud.pubsub.db.DBOfflineStorage;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.ofllineStorage.StorageItem;

public class OfflineEventStorageConsumer {

	private static OfflineEventStorageConsumer instance;
	
	protected LinkedBlockingQueue<JID> queue = new LinkedBlockingQueue<JID>();
	
	Thread consumer;
	
	private OfflineEventStorageConsumer() {
		this.consumer = new Thread(new Consumer());
		this.consumer.start();
	}
	
	public static OfflineEventStorageConsumer getInstance() {
		if(instance == null) {
			instance = new OfflineEventStorageConsumer();
		}
		return instance;
	}
	
	public void addChange(JID jid) {
		try {
			this.queue.put(jid);
			LogMe.debug("Added item to OfflineEventStorageConsumer's queue.");
		} catch (InterruptedException e) {
			LogMe.warning("Error adding item to offline storage: '" + e.getMessage() + "'!");
			e.printStackTrace();
		}
	}
	
	private class Consumer implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				try {
					JID jid = queue.take();
					
					Collection<StorageItem> items = DBOfflineStorage.getInstance().getOfflineItemsOfJid(jid.toBareJID());
					for (StorageItem storageItem : items) {
						Message msg = new Message();
						msg.setID("bc:" + StringUtils.randomString(5));
						msg.setTo(jid);
						msg.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
						msg.addExtension(new PacketExtension(storageItem.getPayload()));
						OutQueueManager.getInstance().put(msg);
						DBOfflineStorage.getInstance().deleteItem(storageItem.getId());
					}
					
					LogMe.debug("Item handled correclty in OfflineEventStorageConsumer's queue.");
				} catch (InterruptedException e) {
					LogMe.warning("Error consuming OfflineEventStorageConsumer queue: '" + e.getMessage() + "'!");
					e.printStackTrace();
				} catch (Exception e) {
					LogMe.warning("Error consuming OfflineEventQueue: '" + e.getMessage() + "'!");
					//e.printStackTrace();
				}
			}
		}
	}
}
