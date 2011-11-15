package com.buddycloud.pubsub.packetHandlers.IQ.Namespace;

import java.util.Collection;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

public interface Action {
	public Collection<Packet> process(IQ reqIQ);
}
