package com.buddycloud.pubsub.packetHandlers;

import java.util.Collection;

import org.xmpp.packet.Packet;

public interface PacketHandlerInterface {
	public Collection<Packet> ingestPacket(Packet p);
}
