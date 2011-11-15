package com.buddycloud.pubsub.packetHandlers.IQ.Namespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.dom.DOMElement;
import org.jivesoftware.whack.util.StringUtils;
import org.xmpp.forms.DataForm;
import org.xmpp.forms.FormField;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketExtension;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.PacketError.Condition;

import com.buddycloud.pubsub.BuddycloudPubsubComponent;
import com.buddycloud.pubsub.Item.Entry;
import com.buddycloud.pubsub.Item.PayloadType;
import com.buddycloud.pubsub.Item.PubsubhubbubPacket;
import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.exceptions.AlreadySubscribedException;
import com.buddycloud.pubsub.exceptions.JidNotInWhiteListException;
import com.buddycloud.pubsub.exceptions.JidOutcastedException;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.managers.LeafnodeManager;
import com.buddycloud.pubsub.managers.OfflineEventStorageWriter;
import com.buddycloud.pubsub.managers.PubsubHubbubManager;
import com.buddycloud.pubsub.node.Leaf;
import com.buddycloud.pubsub.node.LeafState;
import com.buddycloud.pubsub.node.LeafnodeChange;
import com.buddycloud.pubsub.node.LeafnodeChangeAction;
import com.buddycloud.pubsub.node.Nodes;
import com.buddycloud.pubsub.ofllineStorage.StorageItem;
import com.buddycloud.pubsub.subscriber.Affiliation;
import com.buddycloud.pubsub.subscriber.PubsubhubbubSubscriber;
import com.buddycloud.pubsub.subscriber.ResourceType;
import com.buddycloud.pubsub.subscriber.Subscriber;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;
import com.buddycloud.pubsub.subscriber.Subscription;
import com.buddycloud.pubsub.utils.FastDateFormat;

public class PubSub extends AbstractNamespace {
	
	public static final String NAMESPACE_URI = "http://jabber.org/protocol/pubsub";
	public static final String NAMESPACE_PUBSUB_EVENT = "http://jabber.org/protocol/pubsub#event";
	
	public PubSub() {
		setProcessors.put(SetPubSub.ELEMENT_NAME, new SetPubSub());
		getProcessors.put(GetPubSub.ELEMENT_NAME, new GetPubSub());
		this.presenceSubscription = true;
	}
	
	private class SetPubSub implements Action {
		
		public static final String ELEMENT_NAME = "pubsub";

		@SuppressWarnings("unchecked")
		@Override
		public synchronized Collection<Packet> process(IQ reqIQ) {
			Collection<Packet> replyIQs = new ArrayList<Packet>();
			Element pubsub = reqIQ.getChildElement();
			List<Element> elements = pubsub.elements();
			
			for (Element x : elements) {
				if(x.getName().equals("create")) {
					replyIQs.add(createPubSubNode(x, pubsub.element("configure"), reqIQ));
				
				} else if (x.getName().equals("subscribe")) {
					replyIQs.addAll(subscribeToPubSubNode(x, reqIQ));
				
				} else if (x.getName().equals("affiliation")) {
					replyIQs.addAll(requestAffiliationChangeOnNode(x, reqIQ));
				
				} else if (x.getName().equals("unsubscribe")) {
					replyIQs.add(unsubscribeFromPubSubNode(x, reqIQ));
						
				} else if (x.getName().equals("publish")) {
					replyIQs.addAll(publishItem(x, reqIQ));
					
				} else if (x.getName().equals("retract")) {
					replyIQs.addAll(restractItem(x, reqIQ));
					
				} else {
					LogMe.info("Nothing to do for '" + x.getName() + "'!");
					
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

	private class GetPubSub implements Action {
		
		public static final String ELEMENT_NAME = "pubsub";

		@SuppressWarnings("unchecked")
		@Override
		public synchronized Collection<Packet> process(IQ reqIQ) {
			Collection<Packet> replyIQs = new ArrayList<Packet>();
			Element pubsub = reqIQ.getChildElement();
			List<Element> elements = pubsub.elements();
			for (Element x : elements) {
				if (x.getName().equals("subscriptions")) {
					replyIQs.add(getSubscriptions(x, pubsub.element("set"), reqIQ));
				} else if (x.getName().equals("affiliations")) {
					replyIQs.add(getAffiliations(x, reqIQ));
					
				} else if (x.getName().equals("items")) {
					replyIQs.addAll(getItems(x, reqIQ));
				} else {
					LogMe.info("Nothing to do for '" + x.getName() + "'!");
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

	/**
	 * Method to fetch all users subscriptions. The method returns IQ with all the users subscriptions
	 * added to it as specified in  the XEP-0060
	 * 
	 * @param childElement
	 * @param reqIQ
	 * @return
	 */
	public static synchronized IQ getSubscriptions(Element childElement, Element rsm, IQ reqIQ) {
		IQ reply = IQ.createResultIQ(reqIQ);
		
		if(SubscriberEntities.getInstance().getEntity(reqIQ.getFrom().toBareJID()) == null) {
			reply.setType(Type.error);
			reply.setError(Condition.not_allowed);
			return reply;
		}
		
		LinkedList<String> nodes = SubscriberEntities.getInstance().getEntity(reqIQ.getFrom().toBareJID()).getSubscribedNodes();
		LogMe.debug("User is subscribed to nodes:");
		
		Element subscriptions = reply.setChildElement(GetPubSub.ELEMENT_NAME, PubSub.NAMESPACE_URI).addElement("subscriptions");
		
		int count = -1;
		int max = 0;
		String after = null;
		if(rsm != null && rsm.element("max") != null) {
			after = rsm.element("after") != null ? rsm.element("after").getTextTrim() : null;
			max = Integer.parseInt(rsm.element("max").getTextTrim());
			count = nodes.size();
		}
		
		Iterator<String> i = nodes.iterator();
		if(count > -1 && after != null) {
			while(i.hasNext()) {
				String nodename = (String)i.next();
				if(nodename.equals(after)) {
					break;
				}
			}
		}
		
		String first = null;
		String last = null;
		int index = 0;
		while( i.hasNext() ) {
			String nodename = (String)i.next();
			
			if(first == null) {
				first = nodename;
			}
			
			Subscriber sb = Nodes.getInstance().getUsersNodeSubscriber(nodename, reqIQ.getFrom().toBareJID());
			
			if(sb == null) {
				LogMe.warning("We have corruption in our database! Check that all users have mandatory channels!");
				LogMe.warning("User's '" + reqIQ.getFrom().toBareJID() + "' subscription to node '" + nodename + "' was not found!");
				
				continue;
			}
			
			if(sb.getSubscriptionAsString().equals("none")) {
				LogMe.debug("* subscription none for node '" + nodename + "', continuing ...");
				continue;
			} else {
				LogMe.debug("* '" + nodename + "'");
			}
			
			Element subscription = subscriptions.addElement("subscription");
			subscription.addAttribute("node", nodename);
			subscription.addAttribute("jid", reqIQ.getFrom().toBareJID());
			subscription.addAttribute("subscription", sb.getSubscriptionAsString());
			subscription.addAttribute("affiliation", sb.getAffiliationAsString());
		
			last = nodename;
			index++;
			if (count > -1 && index >= max) {
				break;
			}
		}
		if(count > -1 && first != null && last != null) {
			subscriptions.add( getRsmElement(first, last, Integer.toString(count)) );
		}
		return reply;
	}
	
	public static synchronized IQ getAffiliations(Element childElement, IQ reqIQ) {
		IQ reply = IQ.createResultIQ(reqIQ);
		
		if(SubscriberEntities.getInstance().getEntity(reqIQ.getFrom().toBareJID()) == null) {
			reply.setType(Type.error);
			reply.setError(Condition.not_allowed);
			return reply;
		}
		
		Collection<String> nodes = SubscriberEntities.getInstance().getEntity(reqIQ.getFrom().toBareJID()).getSubscribedNodes();
		LogMe.debug("User is subscribed to nodes (checking affiliations):");
		
		Element affiliations = reply.setChildElement(GetPubSub.ELEMENT_NAME, PubSub.NAMESPACE_URI).addElement("affiliations");
		
		for (String nodename : nodes) {
			Subscriber sb = Nodes.getInstance().getUsersNodeSubscriber(nodename, reqIQ.getFrom().toBareJID());
			
			if(sb == null) {
				LogMe.warning("We have corruption in our database! Check that all users have mandatory channels!");
				LogMe.warning("User's '" + reqIQ.getFrom().toBareJID() + "' subscription to node '" + nodename + "' was not found!");
				
				continue;
			}
			
			if(sb.getSubscriptionAsString().equals("none")) {
				LogMe.debug("* subscription none for node '" + nodename + "', continuing ...");
				continue;
			} else {
				LogMe.debug("* '" + nodename + "'");
			}
			
			Element affiliation = affiliations.addElement("affiliation");
			affiliation.addAttribute("node", nodename);
			affiliation.addAttribute("jid", reqIQ.getFrom().toBareJID());
			//subscription.addAttribute("subscription", sb.getSubscriptionAsString());
			affiliation.addAttribute("affiliation", sb.getAffiliationAsString());
		}
		
		return reply;
	}

	public static synchronized Collection<Packet> getItems(Element items, IQ reqIQ) {
		Collection<Packet> replies = new ArrayList<Packet>();
		IQ reply = IQ.createResultIQ(reqIQ);
		
		String nodename = items.attributeValue("node");
		//Element set = items.element("set");
		if(nodename == null || nodename.isEmpty()) {
			LogMe.debug("Cannot getItems, nodename is not set or set item is missing.");
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			replies.add(reply);	
			return replies;
		}
		
		if(!Nodes.getInstance().isKnownNode(nodename)) {
			LogMe.debug("Cannot getItems, nodename '" + nodename +"' is not known.");
			reply.setType(Type.error);
			reply.setError(Condition.item_not_found);
			replies.add(reply);
			return replies;
		}
		
		String after = "1";
		boolean hadAfter = false;
		if(items.element("set") != null && items.element("set").element("after") != null) {
			after = items.element("set").element("after").getTextTrim();
			hadAfter = true;
		}
		
		JID reqJID = reqIQ.getFrom();
		String reqBareJID = reqIQ.getFrom().toBareJID();
		//String reqJID = reqIQ.getFrom().toBareJID();

		// This is used to send IQ's by the component. For example when a user 
		// sends his presence with timestamp, the system automatically multiplies
		// getItem request for every node the user is subscribed.
		// <headers xmlns='http://jabber.org/protocol/shim'>
		//    <header name='Source'>tkoski@gmail.com</header>
		// </headers>
		Element headers = items.element("headers");
		if( headers != null && headers.element("header") != null) {
			if(headers.element("header").attributeValue("name").toLowerCase().equals("source")) {
				reqJID = new JID(headers.element("header").getTextTrim());
				reqBareJID = reqJID.toBareJID();
				LogMe.debug("The real requestor of this message is '" + reqBareJID + "', overriden by header.");
			}
		}
		
		if(!Nodes.getInstance().hasOpenAccessModel(nodename)) {
			
			Subscriber sb = Nodes.getInstance().getUsersNodeSubscriber(nodename, reqBareJID);
			if(sb == null &&
					(nodename.endsWith("/geo/current") || 
					nodename.endsWith("/geo/future") || 
					nodename.endsWith("/geo/previous") ||
					nodename.endsWith("/mood")) ) {
				
				String user = nodename.substring("/user/".length(), nodename.indexOf("/", "/user/".length()));
				String userchannel = "/user/" + user + "/channel";
				LogMe.debug("Node '" + nodename + "' is a '/user/' - node.");
				LogMe.debug("Starting to fetch possible subscription of '" + userchannel + "' - node to check subscription status.");
				
				if(SubscriberEntities.getInstance().getEntity(reqBareJID) != null && 
				   SubscriberEntities.getInstance().getEntity(reqBareJID).isTemporarySubscription() &&
				   Nodes.getInstance().hasOpenAccessModel(userchannel) &&
				   !nodename.endsWith("/mood")) {
					
				   // We are here if the reqBareJID is a temporary user, and he wants to access users geo channels where the geochannel
				   // is is whitelisted but the user's /channel is open.
					LogMe.debug("Temporary user not allowed to access users geo nodes.");
				} else {
					sb = Nodes.getInstance().getUsersNodeSubscriber(userchannel, reqBareJID);
				}
			}
			
			if(sb == null || !sb.getSubscriptionAsString().equals("subscribed")) {	
				LogMe.debug("Cannot getItems, user is not subscribed or pending to whitelisted channel.");
				reply.setType(Type.error);
				reply.setError(Condition.forbidden);
				replies.add(reply);
				return replies;
			}
		}
		
		LogMe.debug("Starting to get items from node '" + nodename +"' that were published after item '" + after +"'");
		
		Collection<Entry> entries = Nodes.getInstance().getEntriesSinceID(nodename, after);
		
		if(entries.isEmpty()) {
			LogMe.debug("Cannot getItems, no entries found. Done.");
			replies.add(reply);
			return replies;
		}
	
		
		if(hadAfter) {
			Element event = new DOMElement("event", new Namespace("", NAMESPACE_PUBSUB_EVENT)); 
			Element is = event.addElement("items");
			is.addAttribute("node", nodename);
			int total = 0;
			for (Iterator<Entry> it = entries.iterator(); it.hasNext();) {
			
				Entry entry = (Entry)it.next();
				is.add(entry.getPayload().createCopy());
				if(total < 5 && it.hasNext()) {
					total++;
					continue;
				}
				IQ iq = new IQ();
				iq.setType(Type.set);
				iq.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
				iq.setTo(reqJID);
				iq.setID("bc:" + StringUtils.randomString(5));
				iq.setChildElement(event.createCopy());
				replies.add(iq.createCopy());
				
				event.clearContent();
				is = event.addElement("items");
				is.addAttribute("node", nodename);
				total = 0;
			}
		} else {
			Element psub = new DOMElement("pubsub", new Namespace("", NAMESPACE_URI));
			//Element is = new DOMElement("items", new Namespace("", NAMESPACE_URI));
			Element is = psub.addElement("items");
			is.addAttribute("node", nodename);
			for (Iterator<Entry> it = entries.iterator(); it.hasNext();) {
				Entry entry = (Entry)it.next();
				is.add(entry.getPayload().createCopy());
			}
			reply.setChildElement(psub.createCopy());
		}
		replies.add(reply);
		LogMe.debug("Get items done, returning '" + Integer.toString(replies.size()) +" stanzas.");
		return replies;
	}
	
	/**
	 * SETS
	 */
	
	public static synchronized Collection<Packet> restractItem(Element retract, IQ reqIQ) {
		Collection<Packet> replies = new ArrayList<Packet>();
		IQ reply = IQ.createResultIQ(reqIQ);
		
		Element item = retract.element("item");
		String nodename = retract.attributeValue("node");
		if(item == null || nodename == null) {
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			replies.add(reply);	
			return replies;
		}
		
		String itemid = item.attributeValue("id");
		if(itemid == null) {
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			replies.add(reply);	
			return replies;
		}	
		
		if(!Nodes.getInstance().isKnownNode(nodename)) {
			reply.setType(Type.error);
			reply.setError(Condition.item_not_found);
			replies.add(reply);
			return replies;
		}
		
		if(!Nodes.getInstance().canRemoveItem(nodename, reqIQ.getFrom().toBareJID())) {
			reply.setType(Type.error);
			reply.setError(Condition.not_allowed);
			replies.add(reply);
			return replies;
		}
		
		replies.add(reply);	
		
		Nodes.getInstance().removeItem(nodename, itemid);
		
		Message msg = new Message();
		msg.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
		msg.setID(reqIQ.getID());
		Element event = msg.addChildElement("event", NAMESPACE_PUBSUB_EVENT);
		Element items = event.addElement("items");
		items.addAttribute("node", nodename);
		Element r = items.addElement("retract");
		r.addAttribute("id", itemid);
		
		Collection<String> jids = Nodes.getInstance().getCollectionOfPossibleReceivers(nodename);
		
		LogMe.debug("We have '" + Integer.toString(jids.size()) + "' of possible receivers.");
		
		for (String jid : jids) {
			
			for (String receiverJID : SubscriberEntities.getInstance().getDeliveryJids(jid)) {
				msg.setID("bc:" + StringUtils.randomString(5));
				msg.setTo(receiverJID);
				replies.add(msg.createCopy());
			}

		}
		
		msg.setID("bc:" + StringUtils.randomString(5));
		msg.setTo("remove.this@tardis.buddycloud.com");
		replies.add(msg.createCopy());
		
		return replies;
	}
	
	public static Collection<String> addPossibleChannelReceivers(Collection<String> jids, String nodename, String requestorsBareJID) {
		// TODO
		// Refactor this. We are not getting the channel's ACL for geo and mood channels from wall
		// This would be fixed by just adding jids.addAll( Nodes.getInstance().getCollectionOfPossibleReceivers(nodename) );
		if(nodename.endsWith("/geo/current") || 
				nodename.endsWith("/geo/future") || 
				nodename.endsWith("/geo/previous") ||
				nodename.endsWith("/mood")) {

			String user = nodename.substring("/user/".length(), nodename.indexOf("/", "/user/".length()));
			
			LogMe.debug("Node '" + nodename + "' is a '/user/' - node.");
			LogMe.debug("Starting to fetch subscribers of '" + "/user/" + user + "/channel" + "' - node.");
			jids.addAll( Nodes.getInstance().getCollectionOfPossibleReceivers("/user/" + user + "/channel") );
			
			// This will remove possible duplicates.
			LogMe.debug("Temporary amount of receivers size is '" + Integer.toString(jids.size()) + "'.");
			jids = new LinkedHashSet<String>(jids);
			LogMe.debug("After removing the duplicates, the receivers size is '" + Integer.toString(jids.size()) + "'.");
		} else {
			LogMe.debug("Node '" + nodename + "' is NOT a '/user/' - node.");
		}
		LogMe.debug("Possible event receivers are now:");
		for (String receiver : jids) {
			LogMe.debug("* '" + receiver + "'.");
		}
		return jids;
	}
	
	public static synchronized Collection<Packet> publishItem(Element publish, IQ reqIQ) {
		Long start = System.currentTimeMillis();
		
		Collection<Packet> replies = new ArrayList<Packet>();
		IQ reply = IQ.createResultIQ(reqIQ);
		
		String nodename = publish.attributeValue("node");
		Element item = publish.element("item");
		
		if(item == null || nodename == null) {
			LogMe.debug("Cannot publish item to node. Nodename not set.");
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			replies.add(reply);	
			return replies;
		}
		
		if(!Nodes.getInstance().isKnownNode(nodename)) {
			LogMe.debug("Cannot publish item to node '" + nodename + "'. Node not found!");
			reply.setType(Type.error);
			reply.setError(Condition.item_not_found);
			replies.add(reply);
			return replies;
		}

		String reqJID = reqIQ.getFrom().toBareJID();
		// This is used to send IQ's by different components who has rights to do so.
		// <headers xmlns='http://jabber.org/protocol/shim'>
		//    <header name='Source'>tkoski@gmail.com</header>
		// </headers>
		Element headers = publish.element("headers");
		if( headers != null && headers.element("header") != null) {
			if(headers.element("header").attributeValue("name").toLowerCase().equals("source")) {
				reqJID = headers.element("header").getTextTrim();
				LogMe.debug("The real requestor of this message is '" + reqJID + "', overriden by header.");
			}
		}
		
		if(!Nodes.getInstance().canPublishItem(nodename, reqJID)) {
			LogMe.debug("Cannot publish item to node'" + nodename + "'. User '" + reqJID + "' is not allowed to do this!");
			reply.setType(Type.error);
			reply.setError(Condition.not_allowed);
			replies.add(reply);
			return replies;
		}
		
		Element itemToPublish = null;
		
		Long id = System.currentTimeMillis();
		boolean isChannelPost = false;
		boolean isGeoPost = false;
		
		if(PayloadType.user_mood.equals(Nodes.getInstance().getNodePayload(nodename))) {
			itemToPublish = Entry.verifyAndGenerateMoodPayload(item);
			if(itemToPublish != null) {
				// This is done to also forward the user mood to user's channel.
				Entry.createAndSendAtomEntryPubsubFromMood(item, reqJID, "/user/" + reqJID + "/channel");
			}
		} else if (PayloadType.geoloc.equals(Nodes.getInstance().getNodePayload(nodename))) {
			itemToPublish = Entry.verifyAndGenerateGeolocPayload(item);
			isGeoPost = true;
		} else {
			itemToPublish = Entry.verifyAndGenerateEntryPayload(item, 
					reqJID, 
					Nodes.getInstance().getUsersNodeSubscriber(nodename, reqJID),
					nodename,
					Long.toString(id));
			isChannelPost = true;
		}
		
		if(itemToPublish == null) {
			LogMe.debug("Cannot publish item to node '" + nodename + "'. Item is not valid.");
			reply.setType(Type.error);
			reply.setError(Condition.not_acceptable);
			replies.add(reply);	
			return replies;
		}
		LogMe.debug("Publish packet validated in '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");

		// Adding success reply.
		replies.add(reply);
		
		//Long id = Nodes.getInstance().addItem(nodename, itemToPublish.createCopy());
		Nodes.getInstance().addItem(nodename, itemToPublish.createCopy(), id);
		itemToPublish.addAttribute("id", Long.toString(id));
		
		Collection<String> jids = Nodes.getInstance().getCollectionOfPossibleReceivers(nodename);
		jids = addPossibleChannelReceivers(jids, nodename, reqJID);		
		LogMe.debug("We have '" + Integer.toString(jids.size()) + "' of possible receivers.");
		
		IQ iq = new IQ();
		iq.setType(Type.set);
		iq.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
		iq.setID(Long.toString(id));
		//Element event = msg.addChildElement("event", NAMESPACE_PUBSUB_EVENT);
		Element event = iq.setChildElement("event", NAMESPACE_PUBSUB_EVENT);
		Element items = event.addElement("items");
		items.addAttribute("node", nodename);
		items.add(itemToPublish.createCopy());
		
		for (String jid : jids) {	
			if(!SubscriberEntities.entityIsOnline(jid)) {
				LogMe.debug("JID '" + jid + "' is not online, will send nothing to that one.");
				continue;
			}
			
			HashMap<String, ResourceType> receivingEntities = SubscriberEntities.getInstance().getDeliveryResources(jid);
			for (String fullJID : receivingEntities.keySet()) {
				if(receivingEntities.get(fullJID) == ResourceType.iq_pubsub) {
					iq.setID("bc:" + StringUtils.randomString(5));
					iq.setTo(fullJID);
					replies.add(iq.createCopy());
				} else {
					Message msg = new Message();
					msg.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
					msg.setTo(fullJID);
					msg.setType(Message.Type.chat);
					
					if(item.element("entry") != null && item.element("entry").element("content") != null) {
						// This is due location changes don't have this info.
						Element content = item.element("entry").element("content");
						msg.setBody("\"" + content.getText() + "\" - " + reqJID + " at \"" + Nodes.getInstance().getLeafnode(nodename).getTitle() + "\" [" + nodename + "]");
						replies.add(msg);
					}
				}
			}
		}
		
		// Here we post to the history element tardis.buddycloud.com
		// Very crazy stuff. Need to clean this all up.
		if(isChannelPost) {
			
			Message historyMsg = new Message();
			historyMsg.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
			historyMsg.setID("bc:" + StringUtils.randomString(5));
			Element hevent = historyMsg.addChildElement("event", NAMESPACE_PUBSUB_EVENT);
			Element hitems = hevent.addElement("items");
			hitems.addAttribute("node", nodename);
			itemToPublish.element("entry").add(Nodes.getInstance().getAsAtomEntrySource(nodename));
			if(!Nodes.getInstance().hasOpenAccessModel(nodename)) {
				Element channelAccessModel = new DOMElement("channel_access_model", new Namespace("", "http://buddycloud.com/atom-elements-0"));
				channelAccessModel.setText("whitelist");
				itemToPublish.element("entry").add(channelAccessModel);
			}
			if(Nodes.getInstance().isHiddenNode(nodename)) {
				Element is_hidden = new DOMElement("channel_is_hidden", new Namespace("", "http://buddycloud.com/atom-elements-0"));
				is_hidden.setText("true");
				itemToPublish.element("entry").add(is_hidden);
			}
			Element leafnode_id = new DOMElement("leafnode_id", new Namespace("", "http://buddycloud.com/atom-elements-0"));
			leafnode_id.setText(Integer.toString(Nodes.getInstance().getLeafnode(nodename).getDbId()));
			itemToPublish.element("entry").add(leafnode_id);
			
			Element title = itemToPublish.element("entry").addElement("title");
			title.setText(itemToPublish.element("entry").element("content").getText());
			
			Element link = itemToPublish.element("entry").addElement("link");
			link.addAttribute("href", Nodes.getInstance().getChannelsHTMLURL(nodename) + "/" + Long.toString(id));
			link.addAttribute("type", "text/html"); 
			link.addAttribute("rel", "alternate");
			
			hitems.add(itemToPublish.createCopy());
			historyMsg.setTo("i.said.this@tardis.buddycloud.com");
			replies.add(historyMsg);
			
			String feedXML = null;
			if(reqJID.equals(Nodes.getInstance().getOwnerOfNode(nodename))) {
			for (PubsubhubbubSubscriber ps : Nodes.getInstance().getLeafnode(nodename).getPubsubhubbubSubscribers()) {
				
				if(feedXML == null) {
					String dateStr = FastDateFormat.getInstance(Conf.TIME_TEMPLATE, TimeZone.getTimeZone(Conf.TIME_ZONE)).format(new Date());
					/*
					Element feed = new DOMElement("feed", new Namespace("", "http://www.w3.org/2005/Atom"));
					feed.addElement("title").setText(Nodes.getInstance().getLeafnode(nodename).getTitle());
					String dateStr = FastDateFormat.getInstance(Conf.TIME_TEMPLATE, TimeZone.getTimeZone(Conf.TIME_ZONE)).format(new Date());
					feed.addElement("updated").setText(dateStr);
					feed.addElement("id").setText("bchubbub," + Nodes.getInstance().getLeafnode(nodename).getChannelsAtomFeedURL() + "," + Long.toString(System.currentTimeMillis()));
					
					feed.add(itemToPublish.element("entry").createCopy());
					//feed.element("entry").addElement("title").setText(feed.element("entry").element("content").getText());
					if(feed.element("entry").element("author").element("name") == null) {
						feed.element("entry").element("author").addElement("name").setText(reqJID);
					}
				
					Element activitySubj = new DOMElement("subject", new Namespace("activity", "http://activitystrea.ms/spec/1.0/"));
					activitySubj.addElement("id").setText(Nodes.getInstance().getLeafnode(nodename).getChannelsHTMLURL());
					Element objectType = new DOMElement("object-type", new Namespace("activity", "http://activitystrea.ms/spec/1.0/"));
					objectType.setText("http://activitystrea.ms/schema/1.0/person");
					activitySubj.add(objectType);
					activitySubj.addElement("title").setText(Nodes.getInstance().getLeafnode(nodename).getTitle());
					Element linkToSubj = activitySubj.addElement("link");
                                        linkToSubj.addAttribute("href", Nodes.getInstance().getLeafnode(nodename).getChannelsHTMLURL());
                                        linkToSubj.addAttribute("type", "text/html");
                                        linkToSubj.addAttribute("rel", "alternate");

					feed.add(activitySubj);

					Element htmlLink = feed.addElement("link");
					htmlLink.addAttribute("href", Nodes.getInstance().getLeafnode(nodename).getChannelsHTMLURL());
					htmlLink.addAttribute("type", "text/html");
					htmlLink.addAttribute("rel", "alternate");
					
					Element atomLink = feed.addElement("link");
					atomLink.addAttribute("href", Nodes.getInstance().getLeafnode(nodename).getChannelsAtomFeedURL());
					atomLink.addAttribute("type", "application/atom+xml");
					atomLink.addAttribute("rel", "self");
				

					Element activityVerb = new DOMElement("verb", new Namespace("activity", "http://activitystrea.ms/spec/1.0/"));
					activityVerb.setText("http://activitystrea.ms/schema/1.0/post");

					Element activityObj = new DOMElement("object", new Namespace("activity", "http://activitystrea.ms/spec/1.0/"));
					activityObj.addElement("id").setText(feed.element("id").getText());
					activityObj.addElement("title").setText(feed.element("entry").element("content").getText());
					activityObj.add(htmlLink.createCopy());
					activityObj.addElement("updated").setText(dateStr);
					activityObj.add(feed.element("entry").element("author").createCopy());
							
					feed.element("entry").add(activityVerb);
					feed.element("entry").add(activityObj);
					*/
					String xml = "";
					xml = xml + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
xml = xml + "<feed xml:lang=\"en-US\" xmlns=\"http://www.w3.org/2005/Atom\" xmlns:thr=\"http://purl.org/syndication/thread/1.0\" xmlns:georss=\"http://www.georss.org/georss\" xmlns:activity=\"http://activitystrea.ms/spec/1.0/\" xmlns:media=\"http://purl.org/syndication/atommedia\" xmlns:poco=\"http://portablecontacts.net/spec/1.0\" xmlns:ostatus=\"http://ostatus.org/schema/1.0\" xmlns:statusnet=\"http://status.net/schema/api/1/\">";
xml = xml + " <generator uri=\"http://buddycloud.com\" version=\"0.1\">buddycloud</generator>";
xml = xml + " <id>" + Nodes.getInstance().getLeafnode(nodename).getChannelsAtomFeedURL() + "</id>";
xml = xml + " <title>" + Nodes.getInstance().getLeafnode(nodename).getTitle() + "</title>";
xml = xml + " <subtitle>" + Nodes.getInstance().getLeafnode(nodename).getDescription() + "</subtitle>";
xml = xml + " <logo>" + Nodes.getInstance().getLeafnode(nodename).getChannelAvatar() + "</logo>";
xml = xml + " <updated>" + dateStr + "</updated>";
xml = xml + "<author>";
xml = xml + " <name>" + Nodes.getInstance().getOwnerOfNode(nodename) + "</name>";
xml = xml + " <uri>" + Nodes.getInstance().getLeafnode(nodename).getChannelsHTMLURL() + "</uri>";
xml = xml + "</author>";
xml = xml + "<link href=\"" + Nodes.getInstance().getLeafnode(nodename).getChannelsAtomFeedURL() + "\" rel=\"self\" type=\"application/atom+xml\"/>";
xml = xml + "<link href=\"" + Nodes.getInstance().getLeafnode(nodename).getChannelsHTMLURL()  + "\" rel=\"alternate\" type=\"text/html\"/>";
xml = xml + "<link href=\"xmpp:pubsub-bridge@buddycloud.com?;node=" + nodename + "\" rel=\"alternate\" />";
xml = xml + "<link href=\"http://api.buddycloud.com/pubsubhubbub?node=" + nodename + "\" rel=\"hub\" />";
//xml = xml + "<link href=\"http://www.lobstermonster.org/examples-work/salmon.php\" rel=\"http://salmon-protocol.org/ns/salmon-replies\"/>";
//xml = xml + "<link href=\"http://www.lobstermonster.org/examples-work/salmon.php\" rel=\"http://salmon-protocol.org/ns/salmon-mention\"/>";
xml = xml + "<activity:subject>";
xml = xml + " <activity:object-type>http://activitystrea.ms/schema/1.0/person</activity:object-type>";
xml = xml + " <id>" + Nodes.getInstance().getLeafnode(nodename).getChannelsHTMLURL() + "</id>";
xml = xml + " <title>" + Nodes.getInstance().getLeafnode(nodename).getTitle() + "</title>";
xml = xml + " <link rel=\"alternate\" type=\"text/html\" href=\"" + Nodes.getInstance().getLeafnode(nodename).getChannelsHTMLURL() + "\"/>";
xml = xml + " <link rel=\"avatar\" type=\"image/png\" media:width=\"54\" media:height=\"54\" href=\"" + Nodes.getInstance().getLeafnode(nodename).getChannelAvatar() + "\"/>";
xml = xml + "</activity:subject>";
xml = xml + "<entry>";
xml = xml + " <title>" + itemToPublish.element("entry").element("content").getText() + "</title>";
xml = xml + " <link rel=\"alternate\" type=\"text/html\" href=\"" + Nodes.getInstance().getLeafnode(nodename).getChannelsHTMLURL() + "/" + Long.toString(id) + "\"/>";
xml = xml + " <id>bc:" + nodename + ":" + Long.toString(id) + "</id>";
xml = xml + " <published>" + dateStr + "</published>";
xml = xml + " <updated>" + dateStr + "</updated>";
xml = xml + " <content type=\"html\">" + itemToPublish.element("entry").element("content").getText() + "</content>";
//xml = xml + " <author>";
//xml = xml + "  <name>" + reqJID + "</name>";
//xml = xml + "  <uri>" + Nodes.getInstance().getLeafnode("/user/" + reqJID + "/channel").getChannelsHTMLURL() + "</uri>";
//xml = xml + " </author>";
//xml = xml + " <activity:actor>";
//xml = xml + "  <activity:object-type>http://activitystrea.ms/schema/1.0/person</activity:object-type>";
//xml = xml + "  <id>" + Nodes.getInstance().getLeafnode("/user/" + reqJID + "/channel").getChannelsHTMLURL() + "</id>";
//xml = xml + "  <title>" + Nodes.getInstance().getLeafnode("/user/" + reqJID + "/channel").getTitle() + "</title>";
//xml = xml + "  <link rel=\"alternate\" type=\"text/html\" href=\"" + Nodes.getInstance().getLeafnode("/user/" + reqJID + "/channel").getChannelsHTMLURL() + "\"/>";
//xml = xml + " <link rel=\"avatar\" type=\"image/png\" media:width=\"54\" media:height=\"54\" href=\"http://media.buddycloud.com/channel/default.png\"/>";
//xml = xml + " <poco:address>";
//xml = xml + "  <poco:formatted>Buddycloud, Interwebs</poco:formatted>";
//xml = xml + " </poco:address>";
//xml = xml + " </activity:actor>";
xml = xml + "</entry>";
xml = xml + "</feed>";
/*
xml = xml + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
xml = xml + "<feed xml:lang=\"en-US\" xmlns=\"http://www.w3.org/2005/Atom\" xmlns:thr=\"http://purl.org/syndication/thread/1.0\" xmlns:georss=\"http://www.georss.org/georss\" xmlns:activity=\"http://activitystrea.ms/spec/1.0/\" xmlns:media=\"http://purl.org/syndication/atommedia\" xmlns:poco=\"http://portablecontacts.net/spec/1.0\" xmlns:ostatus=\"http://ostatus.org/schema/1.0\" xmlns:statusnet=\"http://status.net/schema/api/1/\">";
xml = xml + "<generator uri=\"http://buddycloud.com\" version=\"0.1\">buddycloud</generator>";
xml = xml + "<id>http://api.buddycloud.com/channel/koski.tuomas@gmail.com/atom.xml</id>";
xml = xml + "<title>Koski's test Channel.</title>";
xml = xml + "<logo>http://media.buddycloud.com/channel/default.png</logo>";
xml = xml + "<updated>2010-07-06T09:45:41+00:00</updated>";
xml = xml + "<author>";
xml = xml + "<name>koski.tuomas@gmail.com</name>";
xml = xml + "<uri>http://beta.buddycloud.com/user/gmail.com/koski.tuomas</uri>";
xml = xml + "</author>";
xml = xml + "<link href=\"http://beta.buddycloud.com/user/gmail.com/koski.tuomas\" rel=\"alternate\" type=\"text/html\"/>";
xml = xml + "<link href=\"http://api.buddycloud.com/pubsubhubbub?node=/user/koski.tuomas@gmail.com/channel\" rel=\"hub\"/>";
xml = xml + "<link href=\"http://www.lobstermonster.org/examples-work/salmon.php\" rel=\"http://salmon-protocol.org/ns/salmon-replies\"/>";
xml = xml + "<link href=\"http://www.lobstermonster.org/examples-work/salmon.php\" rel=\"http://salmon-protocol.org/ns/salmon-mention\"/>";
xml = xml + "<link href=\"http://api.buddycloud.com/channel/koski.tuomas@gmail.com/atom.xml\" rel=\"self\" type=\"application/atom+xml\"/>";
xml = xml + "<activity:subject>";
xml = xml + "<activity:object-type>http://activitystrea.ms/schema/1.0/group</activity:object-type>";
xml = xml + "<id>http://beta.buddycloud.com/user/gmail.com/koski.tuomas</id>";
xml = xml + "<title>Koski's test Channel.</title>";
xml = xml + "<link rel=\"alternate\" type=\"text/html\" href=\"http://beta.buddycloud.com/user/gmail.com/koski.tuomas\"/>";
xml = xml + "<link rel=\"avatar\" type=\"image/png\" media:width=\"54\" media:height=\"54\" href=\"http://media.buddycloud.com/channel/default.png\"/>";
xml = xml + "</activity:subject>";
xml = xml + "<entry>";
xml = xml + "<source>";
xml = xml + "<id>http://beta.buddycloud.com/user/buddycloud.com/koski</id>";
xml = xml + "<title>koski@buddycloud.com</title>";
xml = xml + "<link href=\"http://beta.buddycloud.com/user/buddycloud.com/koski\"/>";
xml = xml + "<link rel=\"self\" type=\"text/html\" href=\"http://beta.buddycloud.com/user/buddycloud.com/koski\"/>";
xml = xml + "<icon>http://media.buddycloud.com/channel/default.png</icon>";
xml = xml + "<updated>2010-07-06T09:45:41+00:00</updated>";
xml = xml + "</source>";
xml = xml + "<title>test test test test</title>";
xml = xml + "<author>";
xml = xml + "<name>koski@buddycloud.com</name>";
xml = xml + "<uri>http://beta.buddycloud.com/user/buddycloud.com/koski</uri>";
xml = xml + "</author>";
xml = xml + "<activity:actor>";
xml = xml + "<activity:object-type>http://activitystrea.ms/schema/1.0/person</activity:object-type>";
xml = xml + "<id>http://beta.buddycloud.com/user/buddycloud.com/koski</id>";
xml = xml + "<title>koski@buddycloud.com</title>";
xml = xml + "<link rel=\"alternate\" type=\"text/html\" href=\"http://beta.buddycloud.com/user/buddycloud.com/koski\"/>";
xml = xml + "<link rel=\"avatar\" type=\"image/png\" media:width=\"54\" media:height=\"54\" href=\"http://media.buddycloud.com/channel/default.png\"/>";
xml = xml + "<poco:preferredUsername>koski</poco:preferredUsername>";
xml = xml + "<poco:displayName>Tuomas Koski</poco:displayName>";
xml = xml + "<poco:note>Happy Finnished programmer.</poco:note>";
xml = xml + "<poco:address>";
xml = xml + "<poco:formatted>Paris</poco:formatted>";
xml = xml + "</poco:address>";
xml = xml + "</activity:actor>";
xml = xml + "<link rel=\"alternate\" type=\"text/html\" href=\"http://beta.buddycloud.com/user/gmail.com/koski.tuomas/" + Long.toString(id) + "\"/>";
xml = xml + "<id>http://buddycloud.com/notice/" + Long.toString(id) + "</id>";
xml = xml + "<published>2010-07-06T09:45:41+00:00</published>";
xml = xml + "<updated>2010-07-06T09:45:41+00:00</updated>";
xml = xml + "<content type=\"html\">test test test test</content>";
xml = xml + "</entry>";
xml = xml + "</feed>";
*/
					feedXML = xml;
				}
				PubsubHubbubManager.getInstance().addPacket(new PubsubhubbubPacket(ps, feedXML));
			}
			}
		} else if(isGeoPost) {
			
			Message historyMsg = new Message();
			historyMsg.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
			historyMsg.setID("bc:" + StringUtils.randomString(5));
			Element hevent = historyMsg.addChildElement("event", NAMESPACE_PUBSUB_EVENT);
			Element hitems = hevent.addElement("items");
			hitems.addAttribute("node", nodename);
			//itemToPublish.add(Nodes.getInstance().getAsAtomEntrySource(nodename));
			if(!Nodes.getInstance().hasOpenAccessModel(nodename)) {
				Element channelAccessModel = new DOMElement("channel_access_model", new Namespace("", "http://buddycloud.com/atom-elements-0"));
				channelAccessModel.setText("whitelist");
				itemToPublish.add(channelAccessModel);
			}
//			if(Nodes.getInstance().isHiddenNode(nodename)) {
//				Element is_hidden = new DOMElement("channel_is_hidden", new Namespace("", "http://buddycloud.com/atom-elements-0"));
//				is_hidden.setText("true");
//				itemToPublish.element("entry").add(is_hidden);
//			}
			//Element leafnode_id = new DOMElement("leafnode_id", new Namespace("", "http://buddycloud.com/atom-elements-0"));
			//leafnode_id.setText(Integer.toString(Nodes.getInstance().getLeafnode(nodename).getDbId()));
			//itemToPublish.element("entry").add(leafnode_id);
			hitems.add(itemToPublish.createCopy());
			historyMsg.setTo("i.was.here@tardis.buddycloud.com");
			replies.add(historyMsg);
		}
				
		LogMe.debug("Publish Item handled successfully. Continuing sending replies.");
		return replies;
	}
	
	public static synchronized Collection<Packet> requestAffiliationChangeOnNode(Element affiliation, IQ reqIQ) {
		Long start = System.currentTimeMillis();
		
		Collection<Packet> replies = new ArrayList<Packet>();
		IQ reply = IQ.createResultIQ(reqIQ);
		
		String nodename = affiliation.attributeValue("node");
		String jid = affiliation.attributeValue("jid");
		String aff = affiliation.attributeValue("affiliation");
		
		if(nodename == null || jid == null || aff == null) {
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
	
		LogMe.debug("'" + jid + "' is starting to request affilition '" + aff +"' to node '" + nodename + "'.");
		
		if(!Nodes.getInstance().userIsSubscribedToNode(nodename, jid)) {
			reply.setType(Type.error);
			reply.setError(Condition.not_allowed);
			replies.add(reply);
			return replies;
		}
		
		if(Nodes.getInstance().isAffiliationRequested(nodename, jid)) {
			reply.setType(Type.error);
			reply.setError(Condition.not_allowed);
			replies.add(reply);
			return replies;
		}
		
		reply.setType(Type.result);
		
		String free_text = "";
		if(affiliation.element("text") != null) {
			free_text = affiliation.element("text").getTextTrim();
		}
		
		String request_receivers_bareJID = getRequestsOnlineReceiver(nodename);
		for (String receiverJID : SubscriberEntities.getInstance().getDeliveryJids(request_receivers_bareJID)) {
			Element x = createAffiliationRequestElement(nodename, jid, free_text);
			// Here we have special logic that even is sent the event only to online jids,
			// if JID has a resource, it must be online.
			// This is a bit hack, but it's because needs have changed.
			// After re-factoring this text should not be here anymore.
			JID realJID = new JID(receiverJID);
			if(realJID.getResource() == null) {
				OfflineEventStorageWriter.getInstance().addChange(new StorageItem(receiverJID, x));
			} else {
				replies.add(createAffiliationRequestMessage(realJID, x));
			}
		}
	
		replies.add(reply);
		Nodes.getInstance().setAffiliationRequested(nodename, jid);
		LogMe.debug("Affiliation request handled in '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
		return replies;
	}
	
	public static synchronized IQ unsubscribeFromPubSubNode(Element subscribe, IQ reqIQ) {
		IQ reply = IQ.createResultIQ(reqIQ);
		String nodename = subscribe.attributeValue("node");
		
		if(nodename == null) {
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			return reply;
		}
		
		if(!Nodes.getInstance().isKnownNode(nodename)) {
			reply.setType(Type.error);
			reply.setError(Condition.item_not_found);
			return reply;
		}

		if( !Nodes.getInstance().canUnsubscribe(nodename, reqIQ.getFrom().toBareJID()) ) {
			reply.setType(Type.error);
			reply.setError(Condition.not_allowed);
			return reply;
		}
		
		Nodes.getInstance().unsubscribeUser(nodename, reqIQ.getFrom().toBareJID());
		
		return reply;
	}
	
	public static synchronized Collection<Packet> subscribeToPubSubNode(Element subscribe, IQ reqIQ) {
		IQ reply = IQ.createResultIQ(reqIQ);
		Collection<Packet> replies = new ArrayList<Packet>();
		
		String nodename = subscribe.attributeValue("node");
		String jid = subscribe.attributeValue("jid");
	
		if(nodename == null || nodename.isEmpty() || jid == null || jid.isEmpty() ) {
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
		
		if(!reqIQ.getFrom().toBareJID().equals(jid)) {
			// 6.1.3.1 JIDs Do Not Match
			reply.setType(Type.error);
			reply.setError(Condition.jid_malformed);
			replies.add(reply);
			return replies;
		}
		
		LogMe.debug("Starting to subscribe '" + jid + "' to node '" +nodename + "'.");
		Element pubsub = reply.setChildElement(SetPubSub.ELEMENT_NAME, PubSub.NAMESPACE_URI);
		String subscription_string = "subscribed";
		try {
			Nodes.getInstance().subscribeJid(nodename, jid);
		} catch (AlreadySubscribedException e) {
			reply.setType(Type.error);
			reply.setError(Condition.conflict);
			LogMe.debug("User '" + jid + "' cannot subscribe to node '" +nodename + "' => already subscribed.");
			replies.add(reply);
			return replies;
		} catch (JidOutcastedException e) {
			// 6.1.3.8 Blocked
			reply.setType(Type.error);
			reply.setError(Condition.not_allowed);
			LogMe.debug("User '" + jid + "' cannot subscribe to node '" +nodename + "' => outcasted.");
			replies.add(reply);
			return replies;
		} catch (JidNotInWhiteListException e) {
			// 6.1.3.4 Not on Whitelist
			if(SubscriberEntities.getInstance().getEntity(jid) == null || 
			   SubscriberEntities.getInstance().getEntity(jid).isTemporarySubscription()) {
			
				// temporary users cannot ask subscription to closed channels
				
				LogMe.debug("User '" + jid + "' is not known or temporary. Cannot subscribe to whitelisted channels.");
				reply.setType(Type.error);
				reply.setError(Condition.registration_required);
				replies.add(reply);
				return replies;
			}
			
			LogMe.debug("User '" + jid + "' cannot subscribe to node '" +nodename + "' => not in the whitelist. Answering pending and creating request to owner.");
			reply.setType(Type.result);
			subscription_string = "pending";
		
			String request_receivers_bareJID = getRequestsOnlineReceiver(nodename);
			for (String receiverJID : SubscriberEntities.getInstance().getDeliveryJids(request_receivers_bareJID)) {
//				replies.add(createSubscriptionsRequestMessage(nodename, 
//							receiverJID, 
//							jid));	
				Element x = createSubscriptionsRequestElement(nodename, jid);
				// Here we have special logic that even is sent the event only to online jids,
				// if JID has a resource, it must be online.
				// This is a bit hack, but it's because needs have changed.
				// After re-factoring this text should not be here anymore.
				JID realJID = new JID(receiverJID);
				if(realJID.getResource() == null) {
					OfflineEventStorageWriter.getInstance().addChange(new StorageItem(receiverJID, x));
				} else {
					replies.add(createSubscriptionsRequestMessage(realJID, x));
				}
			}

			Nodes.getInstance().modifyAffiliation(nodename, jid, Nodes.getInstance().getNodesDefaultAffiliationAsString(nodename), Subscription.pending);
		}
		
		Element subscription = pubsub.addElement("subscription");
		subscription.addAttribute("node", nodename);
		subscription.addAttribute("jid", jid);
		subscription.addAttribute("subscription", subscription_string);
		subscription.addAttribute("affiliation", Nodes.getInstance().getUsersAffiliationAsString(nodename, jid));
		
		replies.add(reply);
		return replies;
	}
	
	public static synchronized IQ createPubSubNode(Element create, Element conf, IQ reqIQ) {
		IQ reply = IQ.createResultIQ(reqIQ);
		DataForm confForm = null;
		String nodeName = create.attributeValue("node");
		Map<String, String> nodeConf = new HashMap<String, String>();
		
		if (nodeName == null) {
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			LogMe.debug("Cannot create pubsub node. Nodename empty.");
			return reply;
		}
		
		nodeName = nodeName.toLowerCase();
		
		if(!checkNodename(nodeName)) {
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			LogMe.debug("Cannot create pubsub node! Nodename wrong!");
			return reply;
		}
		
		if (conf == null) {
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			LogMe.debug("Cannot create pubsub node. Configuration missing.");
			return reply;
        }
		
        Element formElement = conf.element(QName.get("x", "jabber:x:data"));
        if (formElement == null) {
        	reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			LogMe.debug("Cannot create pubsub node. Configuration malformed");
        	return reply;
        }
        
        confForm = new DataForm(formElement);
        List<FormField> ff = confForm.getFields();
        for (FormField formField : ff) {
			String var = formField.getVariable();
			String value = "";
			List<String> values = formField.getValues();
	        if (!values.isEmpty()) {
	        	value = values.get(0);
	        }
	        LogMe.debug(" - '" + var + "': '" + value + "'.");
	        nodeConf.put(var, value);
		}
        
        // TODO!
        // Check mandatory values here!
        // Started by tuomas 2009-11-14
        if(nodeConf.get("pubsub#title") == null && nodeConf.get("pubsub#title").equals("")) {
        	reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			LogMe.debug("Cannot create pubsub node. Title missing.");
        	return reply;
        }
        
        if(Nodes.getInstance().isKnownNode(nodeName)) {
        	reply.setType(Type.error);
        	reply.setError(Condition.conflict);
        	LogMe.debug("Cannot create pubsub node. Node already exists.");
        	return reply;
        }
        
        if (SubscriberEntities.getInstance().getEntity(reqIQ.getFrom().toBareJID()).isTemporarySubscription()) {
        	reply.setType(Type.error);
        	reply.setError(Condition.subscription_required);
        	LogMe.debug("Cannot create pubsub node. Subscription required.");
        	return reply;
        }
        
        Leaf node = new Leaf(nodeName);
        
        node.setTitle(nodeConf.get("pubsub#title"));
        node.setDescription(nodeConf.get("pubsub#description"));
        node.setMaxItems(nodeConf.get("pubsub#max_items"));
        node.setPayloadType(PayloadType.getItemTypeFromString(nodeConf.get("pubsub#type")));
        node.setCreated(FastDateFormat.getInstance(Conf.TIME_TEMPLATE, TimeZone.getTimeZone(Conf.TIME_ZONE)).format(new Date()));
        
        try {
        	node.setAccessModel(nodeConf.get("pubsub#access_model"));
        	
        	if(node.isWhitelistAccesslist()) {
        		node.setDefaultAffiliation("publisher");
        	} else {
        		node.setDefaultAffiliation("member");
        	}
        	
        	node.setPublishModel(nodeConf.get("pubsub#publish_model"));
        } catch (Exception e) {
			LogMe.info(e.getMessage());
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			LogMe.debug("Cannot create pubsub node. Accessmodel or publish model all screwed up.");
        	return reply;
		}
        	
        Nodes.getInstance().addNode(nodeName, node);
        Nodes.getInstance().setNodesState(nodeName, LeafState.itemscached);
        LeafnodeManager.getInstance().addChange(new LeafnodeChange(node, LeafnodeChangeAction.insert));
        Nodes.getInstance().modifyAffiliation(node.getNodeName(),
        									  reqIQ.getFrom().toBareJID(),
        									  Affiliation.owner.toString(),
        									  Subscription.subscribed);
        return reply;
	}
	
//	public static Message createSubscriptionsRequestMessage(String nodename, String owner_jid, String subscriber_jid) {
//		Message message = new Message();
//		
//		message.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
//		message.setTo(owner_jid);
//		message.setID("bc:" + StringUtils.randomString(5));
//		
////		<x xmlns='jabber:x:data' type='form'>
////	    <title>PubSub affiliation request</title>
////	    <instructions>
////	      To approve this entity&apos;s affiliation change request, click the OK button. To deny the request, click the cancel button.
////	    </instructions>
////	    <field var='FORM_TYPE' type='hidden'>
////	      <value>http://jabber.org/protocol/pubsub#affiliation_authorization</value>
////	    </field>
////	    <field var='pubsub#node' type='text-single' label='Node ID'>
////	      <value>big_boobies</value>
////	    </field>
////	    <field var='pusub#subscriber_jid' type='jid-single' label='Subscriber Address'>
////	      <value>pamela@gmail.com</value>
////	    </field>
////	    <field var='description' type='text' label='Free text request'>
////	      <value>Please let me join, I know much about the subject.</value>
////	    </field>
////	    <field var='pubsub#allow' type='boolean'
////	           label='Allow this JID to publish to this pubsub node?'>
////	      <value>false</value>
////	    </field>
////	  </x>
//		
//		Element x = message.addChildElement("x", "jabber:x:data");
//		x.addElement("title").addText("PubSub subscriber request");
//		x.addElement("instructions").addText("To approve this entity&apos;s subscription change request, click the OK button. To deny the request, click the cancel button.");
//		
//		Element field = x.addElement("field");
//		field.addAttribute("var", "FORM_TYPE");
//		field.addAttribute("type", "hidden");
//		field.addElement("value").setText("http://jabber.org/protocol/pubsub#subscribe_authorization");
//		
//		field = x.addElement("field");
//		field.addAttribute("var", "pubsub#node");
//		field.addAttribute("label", "Node ID");
//		field.addAttribute("type", "text-single");
//		field.addElement("value").setText(nodename);
//		
//		field = x.addElement("field");
//		field.addAttribute("var", "pubsub#subscriber_jid");
//		field.addAttribute("label", "Subscriber Address");
//		field.addAttribute("type", "jid-single");
//		field.addElement("value").setText(subscriber_jid);
//		
//		field = x.addElement("field");
//		field.addAttribute("var", "pubsub#allow");
//		field.addAttribute("label", "Allow this JID to subscribe to this pubsub node?");
//		field.addAttribute("type", "boolean");
//		field.addElement("value").setText("false");
//		
//		//LogMe.debug("Created conf: '" + conf.asXML() + "'");
//		return message;
//	}
	
	public static Message createSubscriptionsRequestMessage(JID realJID, Element x) {
		Message message = new Message();
		
		message.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
		message.setTo(realJID);
		message.setID("bc:" + StringUtils.randomString(5));
	
		message.addExtension(new PacketExtension(x));
		
		return message;
	}
	
	public static Element createSubscriptionsRequestElement(String nodename, String subscriber_jid) {
		
//		<x xmlns='jabber:x:data' type='form'>
//	    <title>PubSub affiliation request</title>
//	    <instructions>
//	      To approve this entity&apos;s affiliation change request, click the OK button. To deny the request, click the cancel button.
//	    </instructions>
//	    <field var='FORM_TYPE' type='hidden'>
//	      <value>http://jabber.org/protocol/pubsub#affiliation_authorization</value>
//	    </field>
//	    <field var='pubsub#node' type='text-single' label='Node ID'>
//	      <value>big_boobies</value>
//	    </field>
//	    <field var='pusub#subscriber_jid' type='jid-single' label='Subscriber Address'>
//	      <value>pamela@gmail.com</value>
//	    </field>
//	    <field var='description' type='text' label='Free text request'>
//	      <value>Please let me join, I know much about the subject.</value>
//	    </field>
//	    <field var='pubsub#allow' type='boolean'
//	           label='Allow this JID to publish to this pubsub node?'>
//	      <value>false</value>
//	    </field>
//	  </x>
		Element x = new DOMElement("x", new Namespace("", "jabber:x:data"));
		x.addElement("title").addText("PubSub subscriber request");
		x.addElement("instructions").addText("To approve this entity&apos;s subscription change request, click the OK button. To deny the request, click the cancel button.");
		
		Element field = x.addElement("field");
		field.addAttribute("var", "FORM_TYPE");
		field.addAttribute("type", "hidden");
		field.addElement("value").setText("http://jabber.org/protocol/pubsub#subscribe_authorization");
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#node");
		field.addAttribute("label", "Node ID");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(nodename);
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#subscriber_jid");
		field.addAttribute("label", "Subscriber Address");
		field.addAttribute("type", "jid-single");
		field.addElement("value").setText(subscriber_jid);
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#allow");
		field.addAttribute("label", "Allow this JID to subscribe to this pubsub node?");
		field.addAttribute("type", "boolean");
		field.addElement("value").setText("false");
		
		//LogMe.debug("Created conf: '" + conf.asXML() + "'");
		return x;
	}
	
//	public static Message createAffiliationRequestMessage(String nodename, String owner_jid, String subscriber_jid, String free_text) {
//		Message message = new Message();
//		
//		message.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
//		message.setTo(owner_jid);
//		message.setID("bc:" + StringUtils.randomString(5));
//				
//		Element x = message.addChildElement("x", "jabber:x:data");
//		x.addElement("title").addText("PubSub affiliation request");
//		x.addElement("instructions").addText("To approve this entity&apos;s affiliation change request, click the OK button. To deny the request, click the cancel button.");
//		
//		Element field = x.addElement("field");
//		field.addAttribute("var", "FORM_TYPE");
//		field.addAttribute("type", "hidden");
//		field.addElement("value").setText("http://jabber.org/protocol/pubsub#affiliation_authorization");
//		
//		field = x.addElement("field");
//		field.addAttribute("var", "pubsub#node");
//		field.addAttribute("label", "Node ID");
//		field.addAttribute("type", "text-single");
//		field.addElement("value").setText(nodename);
//		
//		field = x.addElement("field");
//		field.addAttribute("var", "pubsub#subscriber_jid");
//		field.addAttribute("label", "Subscriber Address");
//		field.addAttribute("type", "jid-single");
//		field.addElement("value").setText(subscriber_jid);
//		
//		field = x.addElement("field");
//		field.addAttribute("var", "pubsub#allow");
//		field.addAttribute("label", "Allow this JID to publish to this pubsub node?");
//		field.addAttribute("type", "boolean");
//		field.addElement("value").setText("false");
//		
//		field = x.addElement("field");
//		field.addAttribute("var", "description");
//		field.addAttribute("label", "Free text request");
//		field.addAttribute("type", "text");
//		field.addElement("value").setText(free_text);
//		
//		return message;
//	}
	
	public static Message createAffiliationRequestMessage(JID realJID, Element x) {
		Message message = new Message();
		
		message.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
		message.setTo(realJID);
		message.setID("bc:" + StringUtils.randomString(5));
				
		message.addExtension(new PacketExtension(x));
		return message;
	}
	
	public static Element createAffiliationRequestElement(String nodename, String subscriber_jid, String free_text) {
		
		Element x = new DOMElement("x", new Namespace("", "jabber:x:data"));
		x.addElement("title").addText("PubSub affiliation request");
		x.addElement("instructions").addText("To approve this entity&apos;s affiliation change request, click the OK button. To deny the request, click the cancel button.");
		
		Element field = x.addElement("field");
		field.addAttribute("var", "FORM_TYPE");
		field.addAttribute("type", "hidden");
		field.addElement("value").setText("http://jabber.org/protocol/pubsub#affiliation_authorization");
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#node");
		field.addAttribute("label", "Node ID");
		field.addAttribute("type", "text-single");
		field.addElement("value").setText(nodename);
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#subscriber_jid");
		field.addAttribute("label", "Subscriber Address");
		field.addAttribute("type", "jid-single");
		field.addElement("value").setText(subscriber_jid);
		
		field = x.addElement("field");
		field.addAttribute("var", "pubsub#allow");
		field.addAttribute("label", "Allow this JID to publish to this pubsub node?");
		field.addAttribute("type", "boolean");
		field.addElement("value").setText("false");
		
		field = x.addElement("field");
		field.addAttribute("var", "description");
		field.addAttribute("label", "Free text request");
		field.addAttribute("type", "text");
		field.addElement("value").setText(free_text);
		
		return x;
	}
	
	// TODO, fix places where this is called. It might return NULL!
	public static String getRequestsOnlineReceiver(String nodename) {
		LogMe.debug("Starting to find online owner of receiver for node '" + nodename + "'.");
		String request_receivers_bareJID = Nodes.getInstance().getOwnerOfNode(nodename);
		if(!SubscriberEntities.entityIsOnline(request_receivers_bareJID)) {
			LogMe.debug("Owner of the node is not online, looking for online moderators.");
			for (String mod : Nodes.getInstance().getCollectionOfNodesModerators(nodename)) {
				if(SubscriberEntities.entityIsOnline(mod)) {
					request_receivers_bareJID = mod;
					LogMe.debug("Found online moderator: '" + request_receivers_bareJID + "'.");
					break;
				}
				
				if(request_receivers_bareJID == null) {
					request_receivers_bareJID = mod;
				}
			}
		} else {
			LogMe.debug("The receiver of the subs/affiliation request will be '" + request_receivers_bareJID + "'.");
		}
		return request_receivers_bareJID;
	}
	
	public static boolean checkNodename(String nodename) {
	
		if(nodename.startsWith("/user/")) {
			if(nodename.endsWith("/geo/current") || 
			   nodename.endsWith("/geo/future") || 
			   nodename.endsWith("/geo/previous") ||
			   nodename.endsWith("/mood") ||
			   nodename.endsWith("/channel")) {
				return true;
			}
		} else if (nodename.startsWith("/channel/")) {
			System.out.println(nodename.substring("/channel/".length()));
			//return true;
			
			if(nodename.substring("/channel/".length()).matches("[a-z_0-9-]*")) {
				return true;
			}
		} 
		LogMe.warning("Someone trying to create channel in wrong naming conventions! Used nodename: '" + nodename + "'!");
	
		return false;
	}
}
