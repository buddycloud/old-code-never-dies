package com.buddycloud.pubsub;

import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.buddycloud.pubsub.db.DBRoster;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.managers.InQueueManager;
import com.buddycloud.pubsub.managers.OutQueueManager;

public class PubSubEngine implements Component {

	private JID jid = null;
	
	public PubSubEngine() {
	}
	
	// TODO, this should be able to be set in the configuration of the system.
	@Override
	public String getDescription() {
		return "Buddycloud's channel engine.";
	}

	// TODO, this should be able to be set in the configuration of the system.
	@Override
	public String getName() {
		return "Buddycloud's channel pubsub engine.";
	}

	@Override
	public void initialize(JID jid, ComponentManager cp) throws ComponentException {
		// TODO, fix this.
		this.jid = new JID("pubsub-bridge@" + jid.toBareJID());
		//this.jid = new JID(jid.toBareJID());
		LogMe.info("Initializing Pubsub Engine:");
		LogMe.info("* JID: '" + this.jid.toBareJID() + "'");
		
		OutQueueManager.instance(this);
		InQueueManager.instance();
		
		DBRoster.getInstance().probeEveryJidInRoster();
		
		Presence p = new Presence();
		p.setFrom(this.getJID());
		ComponentManagerFactory.getComponentManager().sendPacket(this, p);
		
		LogMe.info("Initialization done!");
	}

	@Override
	public void processPacket(Packet p) {
		Long start = System.currentTimeMillis();

		InQueueManager.getInstance().put(p);
		
		LogMe.debug("Packet added to InQueue in '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
	}

	// TODO, we should close all the queues nicely so possible events are not lost.
	@Override
	public void shutdown() {
		LogMe.info("Shutting down '" + this.jid.toBareJID() + "'...");
	}

	@Override
	public void start() {
		LogMe.info("Starting PubSub Engine!");
	}

	public JID getJID() {
		return this.jid;
	}
}
