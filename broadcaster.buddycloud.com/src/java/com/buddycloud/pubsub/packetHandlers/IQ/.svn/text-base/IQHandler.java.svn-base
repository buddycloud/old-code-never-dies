package com.buddycloud.pubsub.packetHandlers.IQ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.PacketError.Condition;

import com.buddycloud.pubsub.BuddycloudPubsubComponent;
import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.packetHandlers.PacketHandlerInterface;
import com.buddycloud.pubsub.packetHandlers.IQ.Namespace.Namespace;
import com.buddycloud.pubsub.packetHandlers.IQ.Namespace.PubSub;
import com.buddycloud.pubsub.packetHandlers.IQ.Namespace.PubSubOwner;
import com.buddycloud.pubsub.packetHandlers.IQ.Namespace.Pubsubhubbub;
import com.buddycloud.pubsub.packetHandlers.IQ.Namespace.ServiceDiscovery;
import com.buddycloud.pubsub.packetHandlers.IQ.Namespace.Stats;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;

public class IQHandler implements PacketHandlerInterface {

	private Map <String, Namespace> namespaceHandlers = new HashMap<String, Namespace>();
	
	public IQHandler() {
		namespaceHandlers.put(ServiceDiscovery.NAMESPACE_URI, new ServiceDiscovery());
		namespaceHandlers.put(PubSub.NAMESPACE_URI, new PubSub());
		namespaceHandlers.put(PubSubOwner.NAMESPACE_URI, new PubSubOwner());
		namespaceHandlers.put(Pubsubhubbub.NAMESPACE_URI, new Pubsubhubbub());
		namespaceHandlers.put(Stats.NAMESPACE_URI, new Stats());
	}
	
	@Override
	public Collection<Packet> ingestPacket(Packet p) {
		IQ iq = (IQ)p;
		if( iq.getChildElement() != null && iq.getChildElement().getNamespaceURI() != null && namespaceHandlers.get(iq.getChildElement().getNamespaceURI()) != null ) {

			if(!namespaceHandlers.get(iq.getChildElement().getNamespaceURI()).needsPresenceSubscription() ||
			   iq.getFrom().equals(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID()) ||
			   SubscriberEntities.getInstance().getEntity(iq.getFrom().toBareJID()) != null ||
			   Conf.getInstance().isAllowedSender(iq.getFrom().toBareJID())) {
					return namespaceHandlers.get(iq.getChildElement().getNamespaceURI()).ingestPacket(p);
			} else {
				IQ reply = IQ.createResultIQ(iq);
				reply.setType(Type.error);
				reply.setError(Condition.not_allowed);
				Collection<Packet> replies = new ArrayList<Packet>();
				replies.add(reply);
				return replies;
			}
		} 
		
		Collection<Packet> replies = new ArrayList<Packet>();
		
		if(iq.getType() == IQ.Type.error) {
			LogMe.debug("Quietly skipping error.");
			return replies;
		} else if (iq.getType() == IQ.Type.result) {
			LogMe.debug("Quietly skipping result IQ's without namespace.");
                        return replies;
		}
		
		LogMe.debug("'IQHandler' received IQ with childElement with namespace '" + iq.getChildElement().getNamespaceURI() + "' that was not handled!");
		IQ reply = IQ.createResultIQ(iq);
		reply.setType(Type.error);
		reply.setError(Condition.feature_not_implemented);
		replies.add(reply);
		return replies;
	}

}
