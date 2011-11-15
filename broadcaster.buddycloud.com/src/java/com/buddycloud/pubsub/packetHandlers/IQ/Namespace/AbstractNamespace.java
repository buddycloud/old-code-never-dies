package com.buddycloud.pubsub.packetHandlers.IQ.Namespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import com.buddycloud.pubsub.log.LogMe;

abstract class AbstractNamespace implements Namespace{
	
	protected Map <String, Action> setProcessors = new HashMap<String, Action>();
	protected Map <String, Action> getProcessors = new HashMap<String, Action>();
	protected Map <String, Action> resultProcessors = new HashMap<String, Action>();
	
	public boolean presenceSubscription = false;
	
	public Collection<Packet> ingestPacket(Packet p) {
		
		IQ reqIQ = (IQ)p;
		Collection<Packet> replyIQs = new ArrayList<Packet>();
		
		LogMe.debug("Packet is handled by '" + this.getClass().getName() + "'");
		
		if(reqIQ.getType() == IQ.Type.get && getProcessors.get(reqIQ.getChildElement().getName()) != null) {

			replyIQs = getProcessors.get(reqIQ.getChildElement().getName()).process(reqIQ);

		} else if(reqIQ.getType() == IQ.Type.set && setProcessors.get(reqIQ.getChildElement().getName()) != null) {
			
			replyIQs = setProcessors.get(reqIQ.getChildElement().getName()).process(reqIQ);
	
		} else if(reqIQ.getType() == IQ.Type.result && resultProcessors.get(reqIQ.getChildElement().getName()) != null) {
			
			replyIQs = resultProcessors.get(reqIQ.getChildElement().getName()).process(reqIQ);
			
		} else if(reqIQ.getType() == IQ.Type.error) {
			LogMe.debug("Errors not yet handled for namespace '" + this.getClass().getName() + "'.");
		} else {
			LogMe.debug("Did not handle element with name '" + reqIQ.getChildElement().getName() + "', type '" + reqIQ.getType().name() +  "'.");
			IQ replyIq = IQ.createResultIQ(reqIQ);
			replyIq.setType(IQ.Type.error);
			replyIq.setError(PacketError.Condition.bad_request);
			replyIQs.add(replyIq);
		}
		
		return replyIQs;
	}
	
	public boolean needsPresenceSubscription() {
		return this.presenceSubscription;
	}
	
	public static Element getRsmElement(String first, String last, String count) {
		Element set = new DOMElement("set", new org.dom4j.Namespace("", "http://jabber.org/protocol/rsm"));
		set.addElement("first").setText(first);
		set.addElement("last").setText(last);
		set.addElement("count").setText(count);
		return set;
	}
}
