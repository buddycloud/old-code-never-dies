package com.buddycloud.pubsub.packetHandlers.IQ.Namespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.dom.DOMElement;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError.Condition;

import com.buddycloud.pubsub.managers.RosterManager;
import com.buddycloud.pubsub.node.Configuration;
import com.buddycloud.pubsub.node.Nodes;
import com.buddycloud.pubsub.roster.RosterChange;
import com.buddycloud.pubsub.roster.RosterChangeAction;
import com.buddycloud.pubsub.subscriber.Resource;
import com.buddycloud.pubsub.subscriber.ResourceType;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;
import com.buddycloud.pubsub.subscriber.SubscriberEntity;
import com.buddycloud.pubsub.utils.SystemStats;

public class ServiceDiscovery extends AbstractNamespace {

	public static final String NAMESPACE_URI = "http://jabber.org/protocol/disco#info";
	
	public ServiceDiscovery() {
		getProcessors.put(GetQuery.ELEMENT_NAME, new GetQuery());
		resultProcessors.put(ResultQuery.ELEMENT_NAME, new ResultQuery());
	}
	
	private class GetQuery implements Action {

		public static final String ELEMENT_NAME = "query";
		
		public Collection<Packet> process(IQ reqIQ) {
			Collection<Packet> replyIQs = new ArrayList<Packet>();
			IQ replyIq = IQ.createResultIQ(reqIQ);
			
			Element query = reqIQ.getChildElement();
			String nodename = query.attributeValue("node");
			
			if( nodename == null) {
				replyIq.setChildElement(ELEMENT_NAME, NAMESPACE_URI);
				replyIQs.add(replyIq);
			} else if (!nodename.equals("") ) {
	
				if(Nodes.getInstance().isKnownNode(nodename)) {
					replyIQs.add(getNodeMetaDataResponse(reqIQ, Nodes.getInstance().getConfiguration(nodename), nodename));
				} else {
					replyIq.setType(IQ.Type.error);
					replyIq.setError(Condition.item_not_found);
					replyIQs.add(replyIq);
				}
			} else {
				replyIq.setType(IQ.Type.error);
				replyIq.setError(Condition.bad_request);
				replyIQs.add(replyIq);
			}

			return replyIQs;
		}
		
	}
	
	private class ResultQuery implements Action {

		public static final String ELEMENT_NAME = "query";
		
		@SuppressWarnings("unchecked")
		public Collection<Packet> process(IQ reqIQ) {
			Collection<Packet> replyIQs = new ArrayList<Packet>();
			
			Element query = reqIQ.getChildElement();
			if(query == null) {
				return replyIQs;
			}
			
			List<Element> features = query.elements("feature");
			if(features == null || features.isEmpty()) {
				return replyIQs;
			}
			
			for (Element element : features) {
				if(element.attributeValue("var") != null && element.attributeValue("var").equals("http://jabber.org/protocol/pubsub")) {
					SubscriberEntity e = SubscriberEntities.getInstance().getEntity(reqIQ.getFrom().toBareJID());
					if(e != null) {
						e.presence = null;
						if( e.addResouce(new Resource(reqIQ.getFrom().getResource(), ResourceType.iq_pubsub)) ) {
							SystemStats.getInstance().increase(reqIQ.getFrom(), SystemStats.KNOWN_USER);
						}
						RosterManager.getInstance().addChange(new RosterChange(reqIQ.getFrom().toBareJID(), RosterChangeAction.updateLastSeen));
					}
				}
			}
			
			return replyIQs;
		}
	}
	
	public static IQ getNodeMetaDataResponse(IQ reqIQ, Configuration conf, String nodename) {
		IQ reply = IQ.createResultIQ(reqIQ);
		
		Element query = reply.setChildElement(GetQuery.ELEMENT_NAME, ServiceDiscovery.NAMESPACE_URI);
		query.addAttribute("node", nodename);
		
		Element identity = query.addElement("identity");
		identity.addAttribute("category", "pubsub");
		identity.addAttribute("type", "leaf");
		
		query.addElement("feature").addAttribute("var", "http://jabber.org/protocol/pubsub");
		
		Element x = new DOMElement("x", new Namespace("", "jabber:x:data"));
		x.addAttribute("type", "result");
		
//		<x xmlns='jabber:x:data' type='result'>
//	      <field var='FORM_TYPE' type='hidden'>
//	        <value>http://jabber.org/protocol/pubsub#meta-data</value>
//	      </field>
		Element field = x.addElement("field");
		field.addAttribute("var", "FORM_TYPE");
		field.addAttribute("type", "hidden");
		field.addElement("value").setText("http://jabber.org/protocol/pubsub#meta-data");

//	      <field var='pubsub#type' label='Payload type' type='text-single'>
//	        <value>http://www.w3.org/2005/Atom</value>
//	      </field>
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#type");
		field.addAttribute("label", "Payload type");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.payload_type.getNamespace());
		
//	      <field var='pubsub#title' label='A short name for the node' type='text-single'>
//	        <value>Princely Musings (Atom)</value>
//	      </field>
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#title");
		field.addAttribute("label", "A short name for the node");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.title);
		
//	      <field var='pubsub#description' label='A description of the node' type='text-single'>
//	        <value>Updates for Hamlet&apos;s Princely Musings weblog.</value>
//	      </field>
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#description");
		field.addAttribute("label", "A description of the node");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.description);
		
//	      <field var='pubsub#owner' label='Node owners' type='jid-multi'>
//	        <value>hamlet@denmark.lit</value>
//	      </field>
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#publish_model");
		field.addAttribute("label", "Publish Model");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.publish_model.toString());
		
//	      <field var='pubsub#publisher' label='Publishers to this node' type='jid-multi'>
//	        <value>hamlet@denmark.lit</value>
//	      </field>
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#access_model");
		field.addAttribute("label", "Access Model");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.access_model.toString());
		
		if(conf.created != null && !conf.created.equals("")) {
			field = x.addElement("field");
			field.addAttribute("var", "pubsub#creation_date");
			field.addAttribute("label", "Creation Date");
			field.addAttribute("type", "text-single");
			field.addElement("value").setText(conf.created);
		}
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#owner");
		field.addAttribute("label", "Node owners");
		field.addAttribute("type", "jid-multi");
		field.addElement("value").setText(conf.owner);
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#default_affiliation");
		field.addAttribute("label", "Default Affiliation");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.defaulAffiliation.toString());
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#num_subscribers");
		field.addAttribute("label", "Number of subscribers to this node");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.followers);
		
		field = x.addElement("field");
		field.addAttribute("var", "x-buddycloud#rank");
		field.addAttribute("label", "Rank");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.rank);
		
		field = x.addElement("field");
		field.addAttribute("var", "x-buddycloud#popularity");
		field.addAttribute("label", "Popularity");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.popularity);
		
//		<field var="x-buddycloud#geoloc-text" label="Textual representation of
//			location" type="text-single">
//			<value>PoolBar</value>
//			</field>
		field = x.addElement("field");
		field.addAttribute("var", "x-buddycloud#geoloc-text");
		field.addAttribute("label", "Textual representation of location");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.location);
		
//			<field var="x-buddycloud#geoloc-lat" label="Latitude" type="text-single">
//			<value>1.00216516</value>
//			</field>
		field = x.addElement("field");
		field.addAttribute("var", "x-buddycloud#geoloc-lat");
		field.addAttribute("label", "Latitude");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.latitude);
		
//			<field var="x-buddycloud#geoloc-lon" label="Longitude" type="text-single">
//			<value>17.1235133</value>
//			</field>
		field = x.addElement("field");
		field.addAttribute("var", "x-buddycloud#geoloc-lon");
		field.addAttribute("label", "Longitude");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(conf.longitude);
		
		if(conf.avatar_hash != null && !conf.avatar_hash.equals("")) {
			field = x.addElement("field");
			field.addAttribute("var", "x-buddycloud#avatar-hash");
			field.addAttribute("label", "Hash of avatar");
			field.addAttribute("type", "text-single");
			field.addElement("value").setText(conf.avatar_hash);
		}
		
//		Element geoloc = new DOMElement("geoloc", new Namespace("", "http://jabber.org/protocol/geoloc"));
//		geoloc.addElement("text").setText(conf.location);
//		geoloc.addElement("lat").setText(conf.latitude);
//		geoloc.addElement("lon").setText(conf.longitude);
//		
//		field = x.addElement("field");
//		field.addAttribute("var", "x-buddycloud#geoloc");
//		field.addAttribute("label", "geoloc");
//		field.addAttribute("type", "hidden");
//		field.addElement("value").add(geoloc);
		
//	      <field var='pubsub#num_subscribers' label='Number of subscribers to this node' type='text-single'>
//	        <value>1066</value>
//	      </field>
//		field = x.addElement("field");
//		field.addAttribute("var", "pubsub#type");
//		field.addAttribute("label", "Payload type");
//		field.addAttribute("type", "text-single");
//		field.addElement("value").setText("http://www.w3.org/2005/Atom");
		
//	    </x>
		query.add(x);		
		return reply;
	}
}
