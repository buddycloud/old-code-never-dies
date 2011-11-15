package com.buddycloud.pubsub.packetHandlers.IQ.Namespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.PacketError.Condition;

import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.node.Nodes;

public class Pubsubhubbub extends AbstractNamespace {

	public static final String NAMESPACE_URI = "http://pubsubhubbub";
	
	public Pubsubhubbub() {
		setProcessors.put(SetPubSubhubbub.ELEMENT_NAME, new SetPubSubhubbub());
	}
	
	private class SetPubSubhubbub implements Action {
		
		public static final String ELEMENT_NAME = "pubsubhubbub";

		@SuppressWarnings("unchecked")
		@Override
		public synchronized Collection<Packet> process(IQ reqIQ) {
			Collection<Packet> replyIQs = new ArrayList<Packet>();
			Element pubsub = reqIQ.getChildElement();
			List<Element> elements = pubsub.elements();
			
			for (Element x : elements) {
				if (x.getName().equals("subscribe")) {
					replyIQs.addAll(subscribeToPubSubNode(x, reqIQ));
				} else if (x.getName().equals("unsubscribe")) {
					replyIQs.addAll(unsubscribeToPubSubNode(x, reqIQ));
				}
			}
		
			if(replyIQs.isEmpty()) {
				IQ reply = IQ.createResultIQ(reqIQ);
				reply.setType(Type.error);
				reply.setError(Condition.feature_not_implemented);
				replyIQs.add(reply);
			}
			return replyIQs;
		}
		
	}
	
	public static synchronized Collection<Packet> subscribeToPubSubNode(Element subscribe, IQ reqIQ) {
		IQ reply = IQ.createResultIQ(reqIQ);
		Collection<Packet> replies = new ArrayList<Packet>();
		
		String nodename = subscribe.attributeValue("node");
		String topic = subscribe.attributeValue("topic");
		String verify_token = subscribe.attributeValue("verify_token");
		String lease_seconds = subscribe.attributeValue("lease_seconds");
		String secret = subscribe.attributeValue("secret");
		String callback = subscribe.attributeValue("callback");
		
		if(nodename == null || nodename.isEmpty()) {
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			replies.add(reply);
			return replies;
		}
		
		if(!Nodes.getInstance().isKnownNode(nodename)) {
			// 6.1.3.11 Node Does Not Exist
			reply.setType(Type.error);
			reply.setError(Condition.item_not_found);
			replies.add(reply);
			return replies;
		}
		
		if(secret == null) {
			secret = "";
		}
		
		if(lease_seconds == null) {
			lease_seconds = "";
		}
		
		if(!Nodes.getInstance().hasOpenAccessModel(nodename)) {
			reply.setType(Type.error);
			reply.setError(Condition.not_allowed);
			LogMe.debug("Cannot pubsubhubbub subscribe to node '" + nodename + "'. Node does not have open accessmodel.");
			replies.add(reply);
			return replies;
		}
		
		if(!Nodes.getInstance().addPubsubhubbubSubscriber(nodename, topic, verify_token, lease_seconds, secret, callback)) {
			reply.setType(Type.error);
			reply.setError(Condition.conflict);
			LogMe.debug("Cannot pubsubhubbub subscribe to node '" + nodename + "'. Node already exists.");
			replies.add(reply);
			return replies;
		}
		
		
		
		replies.add(reply);
		
		return replies;
	}
	
	public static synchronized Collection<Packet> unsubscribeToPubSubNode(Element subscribe, IQ reqIQ) {
		IQ reply = IQ.createResultIQ(reqIQ);
		Collection<Packet> replies = new ArrayList<Packet>();
		
		String nodename = subscribe.attributeValue("node");
		String topic = subscribe.attributeValue("topic");
		String verify_token = subscribe.attributeValue("verify_token");
		String lease_seconds = subscribe.attributeValue("lease_seconds");
		String secret = subscribe.attributeValue("secret");
		String callback = subscribe.attributeValue("callback");
		
		if(nodename == null || nodename.isEmpty()) {
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			replies.add(reply);
			return replies;
		}
		
		if(!Nodes.getInstance().isKnownNode(nodename)) {
			// 6.1.3.11 Node Does Not Exist
			reply.setType(Type.error);
			reply.setError(Condition.item_not_found);
			replies.add(reply);
			return replies;
		}
		
		if(secret == null) {
			secret = "";
		}
		
		if(lease_seconds == null) {
			lease_seconds = "";
		}

		Nodes.getInstance().removePubsubhubbubSubscriber(nodename, topic, verify_token, lease_seconds, secret, callback);	
		
		replies.add(reply);
		
		return replies;
	}
	
}
