package com.buddycloud.pubsub.packetHandlers.IQ.Namespace;

import java.util.ArrayList;
import java.util.Collection;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.buddycloud.pubsub.utils.SystemStats;

public class Stats extends AbstractNamespace {

	public static final String NAMESPACE_URI = "http://buddycloud.com/stats";
	
	public Stats() {
		getProcessors.put(GetStats.ELEMENT_NAME, new GetStats());
	}
	
	private class GetStats implements Action {
		
		public static final String ELEMENT_NAME = "stats";

		@Override
		public synchronized Collection<Packet> process(IQ reqIQ) {
			Collection<Packet> replyIQs = new ArrayList<Packet>();
			IQ reply = IQ.createResultIQ(reqIQ);
			Element stats = reply.setChildElement(ELEMENT_NAME, NAMESPACE_URI);
			Element online = stats.addElement("online");
			online.addElement("bc_users").setText(Integer.toString(SystemStats.getInstance().getBc_users()));
			online.addElement("other_users").setText(Integer.toString(SystemStats.getInstance().getOther_users()));
			online.addElement("anon_users").setText(Integer.toString(SystemStats.getInstance().getAnon_users()));
			replyIQs.add(reply);
			return replyIQs;
		}
	}
}
