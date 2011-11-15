package com.buddycloud.pubsub.managers;

import java.util.concurrent.LinkedBlockingQueue;

import com.buddycloud.pubsub.db.DBOfflineStorage;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.ofllineStorage.StorageItem;

public class OfflineEventStorageWriter {

	private static OfflineEventStorageWriter instance;
	
	protected LinkedBlockingQueue<StorageItem> queue = new LinkedBlockingQueue<StorageItem>();
	
	Thread consumer;
	
	private OfflineEventStorageWriter() {
		this.consumer = new Thread(new Consumer());
		this.consumer.start();
	}
	
	public static OfflineEventStorageWriter getInstance() {
		if(instance == null) {
			instance = new OfflineEventStorageWriter();
		}
		return instance;
	}
	
	public void addChange(StorageItem change) {
		try {
			this.queue.put(change);
			LogMe.debug("Added item to OfflineEventStorageWriter's queue.");
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
					StorageItem item = queue.take();
					DBOfflineStorage.getInstance().insertItem(item);
					LogMe.debug("Item handled correclty in OfflineEventStorageWriter's queue.");
				} catch (InterruptedException e) {
					LogMe.warning("Error consuming Offline Storage Writer: '" + e.getMessage() + "'!");
					e.printStackTrace();
				} catch (Exception e) {
					LogMe.warning("Error consuming OfflineEventWriterQueue: '" + e.getMessage() + "'!");
					//e.printStackTrace();
				}
			}
		}
	}
	
}
