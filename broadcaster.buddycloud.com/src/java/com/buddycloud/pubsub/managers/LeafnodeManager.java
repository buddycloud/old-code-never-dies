package com.buddycloud.pubsub.managers;

import java.util.concurrent.LinkedBlockingQueue;

import com.buddycloud.pubsub.db.DBItem;
import com.buddycloud.pubsub.db.DBNode;
import com.buddycloud.pubsub.db.DBSubscription;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.node.LeafnodeChange;
import com.buddycloud.pubsub.node.Nodes;

public class LeafnodeManager {

	private static LeafnodeManager instance;
	
	protected LinkedBlockingQueue<LeafnodeChange> queue = new LinkedBlockingQueue<LeafnodeChange>();
	
	Thread consumer;
	
	private LeafnodeManager() {
		this.consumer = new Thread(new Consumer());
		this.consumer.start();
	}
	
	public static LeafnodeManager getInstance() {
		if(instance == null) {
			instance = new LeafnodeManager();
		}
		return instance;
	}
	
	public void addChange(LeafnodeChange change) {
		try {
			this.queue.put(change);
		} catch (InterruptedException e) {
			LogMe.warning("InterruptedException on RosterChange: '" + e.getMessage() + "'!");
			e.printStackTrace();
		} catch (Exception e) {
			LogMe.warning("Error adding LeafnodeChange: '" + e.getMessage() + "'!");
		}
	}
	
	private class Consumer implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				try {
					LeafnodeChange change = queue.take();
					LogMe.debug("LeafnodeManager is ready to '" + change.getActionAsString() + "' node: '" + change.getNodename() + "'.");
					if( change.isToInsert() ) {
						DBNode.getInstance().insertLeafnode(change.getNode());
					} else if (change.isToUpdate()) {
						DBNode.getInstance().updateLeafnodeToDB(change);
					} else {
						DBItem.getInstance().deleteAllItemsOfNode(change.getNodename());
						DBSubscription.getInstance().deleteSubscriptionOfNode(change.getNodename());
						DBNode.getInstance().deleteLeafNode(change.getNodename(), change.getNode().getDbId());
						Nodes.getInstance().removeNode(change.getNodename());
					}
				} catch (InterruptedException e) {
					LogMe.warning("Error consuming Laefnode Change: '" + e.getMessage() + "'!");
					e.printStackTrace();
				} catch (Exception e) {
					LogMe.warning("Error consuming LeafnodeQueue: '" + e.getMessage() + "'!");
					//e.printStackTrace();
				}
			}
		}
	}
}
