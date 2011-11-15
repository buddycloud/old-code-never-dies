package com.buddycloud.pubsub.managers;

import java.util.concurrent.LinkedBlockingQueue;

import com.buddycloud.pubsub.Item.ItemChange;
import com.buddycloud.pubsub.db.DBItem;
import com.buddycloud.pubsub.log.LogMe;

public class ItemManager {
	private static ItemManager instance;
	
	protected LinkedBlockingQueue<ItemChange> queue = new LinkedBlockingQueue<ItemChange>();
	
	Thread consumer;
	
	private ItemManager() {
		this.consumer = new Thread(new Consumer());
		this.consumer.start();
	}
	
	public static ItemManager getInstance() {
		if(instance == null) {
			instance = new ItemManager();
		}
		return instance;
	}
	
	public void addChange(ItemChange change) {
		try {
			this.queue.put(change);
		} catch (InterruptedException e) {
			LogMe.warning("InterruptedException on ItemChange: '" + e.getMessage() + "'!");
			e.printStackTrace();
		} catch (Exception e) {
			LogMe.warning("Error adding ItemChange: '" + e.getMessage() + "'!");
		}
	}
	
	private class Consumer implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				try {
					ItemChange change = queue.take();
					LogMe.debug("ItemManager is ready to '" + change.getActionAsString() + "' and item to/from node: '" + change.getNodename() + "'.");
					if( change.isToInsert() ) {
						DBItem.getInstance().insertItem(change);
					} else if (change.isToUpdate()) {
						//DBRoster.getInstance().updateLeafnodeToDB();
					} else {
						DBItem.getInstance().deleteItem(change);
					}
				} catch (InterruptedException e) {
					LogMe.warning("Error consuming Laefnode Change: '" + e.getMessage() + "'!");
					e.printStackTrace();
				} catch (Exception e) {
					LogMe.warning("Error consuming ItemQueue: '" + e.getMessage() + "'!");
					//e.printStackTrace();
				}
			}
		}
	}
}
