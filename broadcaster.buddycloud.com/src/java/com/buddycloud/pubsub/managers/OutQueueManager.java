package com.buddycloud.pubsub.managers;

import java.util.concurrent.LinkedBlockingQueue;

import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.Packet;

import com.buddycloud.pubsub.PubSubEngine;
import com.buddycloud.pubsub.log.LogMe;

public class OutQueueManager {

	private static OutQueueManager instance;
	
	protected LinkedBlockingQueue<Packet> queue = new LinkedBlockingQueue<Packet>();
	
	Thread consumer;

	private PubSubEngine pubsubEngine;
	
	private OutQueueManager(PubSubEngine e) {
		this.pubsubEngine = e;
		this.consumer = new Thread(new Consumer());
		this.consumer.start();
	}
	
	public static void instance(PubSubEngine e) {
		instance = new OutQueueManager(e);
	}
	
	public static OutQueueManager getInstance() {
		return instance;
	}
	
	public void put(Packet p) {
		try {
			this.queue.put(p);
		} catch (InterruptedException e) {
			LogMe.warning("Error adding Packet to InQueue: '" + e.getMessage() + "'!");
			e.printStackTrace();
		}
	}
	
	private class Consumer implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				try {
					Packet p = queue.take();
					
					Long start = System.currentTimeMillis();
	
					if(p == null) {
						return;
					}
				
					try {
						LogMe.debug("Sending packet '" + p.toXML() + "'");
						
						if(p.getFrom().equals(p.getTo())) {
							LogMe.warning("Sender and the receiver is the same! Skipping!");
							continue;
						}
						
						ComponentManagerFactory.getComponentManager().sendPacket(pubsubEngine, p);
					} catch (ComponentException e) {
						LogMe.warning("Error: '" + e.getMessage() + "'");
					}
					LogMe.debug("Packet sent in '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
					
				} catch (InterruptedException e) {
					LogMe.warning("Error consuming OutQueue: '" + e.getMessage() + "'!");
					e.printStackTrace();
				} catch (Exception e) {
					LogMe.warning("Error consuming OutQueue: '" + e.getMessage() + "'!");
					//e.printStackTrace();
				}
			}
		}
	}
}
