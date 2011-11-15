package com.buddycloud.pubsub.node.configs;

import java.util.HashMap;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.dom.DOMElement;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import com.buddycloud.pubsub.Item.PayloadType;
import com.buddycloud.pubsub.db.DBSubscription;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.node.Configuration;
import com.buddycloud.pubsub.node.Nodes;
import com.buddycloud.pubsub.packetHandlers.IQ.Namespace.PubSub;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;
import com.buddycloud.pubsub.subscriber.Subscription;
import com.buddycloud.pubsub.tasks.Welcommer;

public class UserChannelInitBuilder {

	public static Element getConf(String title, String max_items, String payload_type, boolean openchannel) {
		Element conf = new DOMElement("configure");
		Element x = new DOMElement("x", new Namespace("", "jabber:x:data"));
		x.addAttribute("type", "result");
		
		Element field = x.addElement("field");
		field.addAttribute("var", "pubsub#title");
		field.addAttribute("label", "A short name for the node");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(title);
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#publish_model");
		field.addAttribute("label", "Publish Model");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(PublishModel.publishers.toString());
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#access_model");
		field.addAttribute("label", "Access Model");
		field.addAttribute("type", "text-single");
		//field.addElement("value").setText(AccessModel.whitelist.toString());
		//Open channel will be free for anyone to publish.
		if(openchannel) {
			field.addElement("value").setText(AccessModel.open.toString());
		} else {
			field.addElement("value").setText(AccessModel.whitelist.toString());
		}
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#max_items");
		field.addAttribute("label", "Max Items");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(max_items);
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#type");
		field.addAttribute("label", "Payload Type");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(payload_type);
		
		conf.add(x);
		LogMe.debug("Created conf: '" + conf.asXML() + "'");
		
		return conf;
	}
	
	private static void createUserChannel(String bareJID) {
		
		Element x = new DOMElement("create", new Namespace("", "http://jabber.org/protocol/pubsub"));
		x.addAttribute("node", "/user/" + bareJID + "/channel");
		
		IQ iq = new IQ();
		iq.setFrom(new JID(bareJID));
		
		PubSub.createPubSubNode(x, getConf(bareJID +"'s Buddycloud channel.", Integer.toString(Configuration.DEFAULT_MAX_ITEMS), PayloadType.atom_entry.getNamespace(), true), iq);
	}
	
	private static void createUserMood(String bareJID) {
		
		Element x = new DOMElement("create", new Namespace("", "http://jabber.org/protocol/pubsub"));
		x.addAttribute("node", "/user/" + bareJID + "/mood");
		
		IQ iq = new IQ();
		iq.setFrom(new JID(bareJID));
		
		PubSub.createPubSubNode(x, getConf(bareJID +"'s Mood.", "1", PayloadType.user_mood.getNamespace(), true), iq);
	}
	
	private static void createUserGeoNodes(String bareJID) {
		
		IQ iq = new IQ();
		iq.setFrom(new JID(bareJID));
		
		Element x = new DOMElement("create", new Namespace("", "http://jabber.org/protocol/pubsub"));
		x.addAttribute("node", "/user/" + bareJID + "/geo/current");
		PubSub.createPubSubNode(x, getConf(bareJID +"'s Current Geolocation.", "1", PayloadType.geoloc.getNamespace(), false), iq);
		
		x.addAttribute("node", "/user/" + bareJID + "/geo/previous");
		PubSub.createPubSubNode(x, getConf(bareJID +"'s Previous Geolocation.", "1", PayloadType.geoloc.getNamespace(), false), iq);
		
		x.addAttribute("node", "/user/" + bareJID + "/geo/future");
		PubSub.createPubSubNode(x, getConf(bareJID +"'s Future Geolocation.", "1", PayloadType.geoloc.getNamespace(), false), iq);
	}
	
	public static void createUserChannels(String bareJID) {
		LogMe.debug("Starting to create user channels for JID '" + bareJID + "'.");
		createUserChannel(bareJID);
		createUserGeoNodes(bareJID);
		createUserMood(bareJID);
		
		// This can happen if user accidentally removes the subscription and subscribed component back.
		HashMap<String, String> configuredSubs = DBSubscription.getInstance().getConfiguredSubscriptions(bareJID);
		for (String nodename : configuredSubs.keySet()) {
			SubscriberEntities.getInstance().getEntity(bareJID).addSubscription(nodename);
		}
		
		// This can happen if user is not yet subscribed to the service but had some unconfigured subscriptions left behind.
		HashMap<String, String> unconfiguredSubs = DBSubscription.getInstance().getUnconfiguredSubscriptions(bareJID);
		for (String nodename : unconfiguredSubs.keySet()) {
			SubscriberEntities.getInstance().getEntity(bareJID).addSubscription(nodename);
			Nodes.getInstance().modifyAffiliation(nodename, bareJID, unconfiguredSubs.get(nodename), Subscription.subscribed);
		}
		
		// Send welcome messages.
		Thread t = new Thread(new Welcommer(new JID(bareJID)));
        t.start();
	}
}
