package com.buddycloud.pubsub.packetHandlers.IQ.Namespace;

import java.util.Collection;

import org.xmpp.packet.Packet;

public interface Namespace {
	
	public Collection<Packet> ingestPacket(Packet p);

	public boolean needsPresenceSubscription();
}
