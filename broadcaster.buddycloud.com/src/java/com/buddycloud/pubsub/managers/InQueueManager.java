package com.buddycloud.pubsub.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.xmpp.packet.Packet;

import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.packetHandlers.PacketHandlerInterface;
import com.buddycloud.pubsub.packetHandlers.IQ.IQHandler;
import com.buddycloud.pubsub.packetHandlers.Message.MessageHandler;
import com.buddycloud.pubsub.packetHandlers.Presence.PresenceHandler;

public class InQueueManager {

	private static InQueueManager instance;
	
	protected LinkedBlockingQueue<Packet> queue = new LinkedBlockingQueue<Packet>();
	
	Thread consumer;
	
	private Map <String, PacketHandlerInterface> packetHandlers = new HashMap<String, PacketHandlerInterface>();
	
	private static final String MESSAGE 	= "org.xmpp.packet.Message"; 
	private static final String PRESENCE 	= "org.xmpp.packet.Presence";
	private static final String IQ 			= "org.xmpp.packet.IQ";
	
	public static void instance() {
		instance = new InQueueManager();
	}
	
	public static InQueueManager getInstance() {
		return instance;
	}
	
	private InQueueManager() {
		
		packetHandlers.put(IQ, new IQHandler());
		packetHandlers.put(PRESENCE, new PresenceHandler());
		packetHandlers.put(MESSAGE, new MessageHandler());
		
		this.consumer = new Thread(new Consumer());
		this.consumer.start();
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
					LogMe.debug("Received Packet. Packet as raw XML '" + p.toXML() + "'.");
					LogMe.debug("Packet is class of:'" + p.getClass().getName() + "'");
					
					if( packetHandlers.get(p.getClass().getName()) != null ) {
						LogMe.debug("Packet will be handled by '" + p.getClass().getName() + "'.");
						Collection<Packet> replyPackets = packetHandlers.get(p.getClass().getName()).ingestPacket(p);
						
						for (Packet replyPacket : replyPackets) {
							OutQueueManager.getInstance().put(replyPacket);	
						}
						
					} else {
						LogMe.debug("Packet was not handled in any way.");
					}
					LogMe.info("Packet handled in '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
					
				} catch (InterruptedException e) {
					LogMe.warning("Interrupted Exception while consuming InQueue: '" + e.getMessage() + "'!");
					//e.printStackTrace();
				} catch (Exception e) {
					LogMe.warning("Error consuming InQueue: '" + e.getMessage() + "'!");
					//e.printStackTrace();
				}
			}
		}
	}
}
