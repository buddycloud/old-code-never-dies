package com.buddycloud.pubsub.packetHandlers.IQ.Namespace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.PacketError.Condition;

import com.buddycloud.pubsub.BuddycloudPubsubComponent;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.managers.LeafnodeManager;
import com.buddycloud.pubsub.node.Leaf;
import com.buddycloud.pubsub.node.LeafnodeChange;
import com.buddycloud.pubsub.node.LeafnodeChangeAction;
import com.buddycloud.pubsub.node.Nodes;
import com.buddycloud.pubsub.subscriber.Subscriber;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;
import com.buddycloud.pubsub.subscriber.SubscriberEntity;
import com.buddycloud.pubsub.subscriber.Subscription;

public class PubSubOwner extends AbstractNamespace {
	
	public static final String NAMESPACE_URI = "http://jabber.org/protocol/pubsub#owner";

	public PubSubOwner() {
		this.setProcessors.put(SetPubSubOwner.ELEMENT_NAME, new SetPubSubOwner());
		this.getProcessors.put(GetPubSubOwner.ELEMENT_NAME, new GetPubSubOwner());
		this.presenceSubscription = true;
	}
	
	private class SetPubSubOwner implements Action {

		public static final String ELEMENT_NAME = "pubsub";

		@SuppressWarnings("unchecked")
		@Override
		public Collection<Packet> process(IQ reqIQ) {
			Collection<Packet> replyIQs = new ArrayList<Packet>();
			
			Element pubsub = reqIQ.getChildElement();
			List<Element> elements = pubsub.elements();
			for (Element x : elements) {
				if(x.getName().equals("affiliations")) {
					replyIQs.addAll(handleAffiliations(x, reqIQ));
				} else if(x.getName().equals("subscriptions")) {
					replyIQs.addAll(handleSubscriptions(x, reqIQ));
				
				} else if (x.getName().equals("configure")) {
					replyIQs.addAll(configureNode(x, reqIQ));
					
				} else if (x.getName().equals("delete")) {
					replyIQs.addAll(deleteNode(x, reqIQ));
					
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
	
	private class GetPubSubOwner implements Action {
		
		public static final String ELEMENT_NAME = "pubsub";

		@SuppressWarnings("unchecked")
		@Override
		public synchronized Collection<Packet> process(IQ reqIQ) {
			Collection<Packet> replyIQs = new ArrayList<Packet>();
			Element pubsub = reqIQ.getChildElement();
			List<Element> elements = pubsub.elements();
			for (Element x : elements) {
				if (x.getName().equals("subscriptions")) {
					replyIQs.add(getNodesSubscriptions(x, reqIQ));
					
				} else if (x.getName().equals("affiliations")) {
					replyIQs.add(getNodesAffiliations(x, pubsub.element("set"), reqIQ));
				
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
	
	public static synchronized Collection<Packet> deleteNode(Element delete, IQ reqIQ) {
		Collection<Packet> replies = new ArrayList<Packet>();
		IQ reply = IQ.createResultIQ(reqIQ);
		String nodename = delete.attributeValue("node");
		
		if (nodename == null) {
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			LogMe.debug("Cannot delete pubsub node. Nodename not set.");
			replies.add(reply);
			return replies;
		}
		
		if(!Nodes.getInstance().isKnownNode(nodename)) {
			LogMe.debug("Cannot delete pubsub node. Node is not known.");
			reply.setType(Type.error);
			reply.setError(Condition.item_not_found);
			replies.add(reply);
			return replies;
		}
		
		if( !Nodes.getInstance().canDeleteNode(nodename, reqIQ.getFrom().toBareJID()) ) {
			LogMe.debug("Cannot delete pubsub node. User '" + reqIQ.getFrom().toBareJID() +"' is not allowed to do so.");
			reply.setType(Type.error);
			reply.setError(Condition.not_allowed);
			replies.add(reply);
			return replies;
		}
		replies.add(reply);
		
		Collection<String> jids = Nodes.getInstance().getCollectionOfPossibleReceivers(nodename);
		
		Message msg = new Message();
		msg.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
		Element event = msg.addChildElement("event", "http://jabber.org/protocol/pubsub#event");
		event.addElement("delete").addAttribute("node", nodename);
		for (String jid : jids) {
			// TODO add here notifications to everyone!
			msg.setTo(jid);
			msg.setID("bc:" + StringUtils.randomString(5));
			replies.add(msg.createCopy());
			Nodes.getInstance().unsubscribeUser(nodename, jid);
		}
		
		LeafnodeManager.getInstance().addChange(new LeafnodeChange(new Leaf(nodename, Nodes.getInstance().getLeafnode(nodename).getDbId()), LeafnodeChangeAction.delete));
		
		LogMe.debug("Node '" + nodename + "' deleted successfully.");
		
		return replies;
	}
	
	public static synchronized Collection<Packet> configureNode(Element configuration, IQ reqIQ) {
		Collection<Packet> replies = new ArrayList<Packet>();
		IQ reply = IQ.createResultIQ(reqIQ);
		DataForm confForm = null;
		String nodename = configuration.attributeValue("node");
		Map<String, String> nodeConf = new HashMap<String, String>();
		
		if (nodename == null) {
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			LogMe.debug("Cannot re-configure pubsub node. Nodename empty.");
			replies.add(reply);
			return replies;
		}
		
		if (configuration == null) {
			reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			LogMe.debug("Cannot re-configure pubsub node. Configuration missing.");
			replies.add(reply);
			return replies;
        }
		
		Element formElement = configuration.element(QName.get("x", "jabber:x:data"));
        if (formElement == null) {
        	reply.setType(Type.error);
			reply.setError(Condition.bad_request);
			LogMe.debug("Cannot re-configure pubsub node. Configuration malformed");
			replies.add(reply);
			return replies;
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
//        if(nodeConf.get("pubsub#title") == null && nodeConf.get("pubsub#title").equals("")) {
//        	reply.setType(Type.error);
//			reply.setError(Condition.bad_request);
//			LogMe.debug("Cannot re-configure pubsub node. Title missing.");
//			replies.add(reply);
//			return replies;
//        }
        
        if(!Nodes.getInstance().isKnownNode(nodename)) {
			reply.setType(Type.error);
			reply.setError(Condition.item_not_found);
			replies.add(reply);
			return replies;
		}
        
        if( !Nodes.getInstance().canConfigureNode(nodename, reqIQ.getFrom().toBareJID()) ) {
			reply.setType(Type.error);
			reply.setError(Condition.not_allowed);
			replies.add(reply);
			return replies;
		}
        
        Nodes.getInstance().modifyNodeConfiguration(nodename, nodeConf);
        
        Collection<String> jids = Nodes.getInstance().getCollectionOfPossibleReceivers(nodename);
		
		LogMe.debug("We have '" + Integer.toString(jids.size()) + "' of possible receivers.");
		
		Message msg = new Message();
		msg.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
		Element event = msg.addChildElement("event", "http://jabber.org/protocol/pubsub#event");
		Element conf = event.addElement("configuration");
		conf.addAttribute("node", nodename);
		
		Element x = new DOMElement("x", new Namespace("", "jabber:x:data"));
		x.addAttribute("type", "result");
		
		Element field = x.addElement("field");
		field.addAttribute("var", "FORM_TYPE");
		field.addAttribute("type", "hidden");
		field.addElement("value").setText("http://jabber.org/protocol/pubsub#node_config");
		
		if(nodeConf.get("pubsub#title") != null) {
			field = x.addElement("field");
			field.addAttribute("var", "pubsub#title");
			field.addAttribute("label", "A short name for the node");
			field.addAttribute("type", "text-single");
			field.addElement("value").setText(nodeConf.get("pubsub#title"));
		}
		
		if(nodeConf.get("pubsub#description") != null) {
			field = x.addElement("field");
			field.addAttribute("var", "pubsub#description");
			field.addAttribute("label", "Description of the node");
			field.addAttribute("type", "text-single");
			field.addElement("value").setText(nodeConf.get("pubsub#description"));
		}
		
		if(nodeConf.get("x-buddycloud#avatar-hash") != null) {
			field = x.addElement("field");
			field.addAttribute("var", "x-buddycloud#avatar-hash");
			field.addAttribute("label", "Hash of avatar");
			field.addAttribute("type", "text-single");
			field.addElement("value").setText(nodeConf.get("x-buddycloud#avatar-hash"));
		}
		
		conf.add(x);
		
		for (String jid : jids) {
			for (String receiversJID : SubscriberEntities.getInstance().getDeliveryJids(jid)) {
				msg.setID("bc:" + StringUtils.randomString(5));
				msg.setTo(receiversJID);
				replies.add(msg.createCopy());
			}
		}
        
		replies.add(reply);
		
		LogMe.debug("Node '" + nodename + "' re-configured successfully.");
		
		return replies;
	}
	
	public static synchronized IQ getNodesSubscriptions(Element subscribe, IQ reqIQ) {
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
		
		if(!Nodes.getInstance().hasOpenAccessModel(nodename)) {
			SubscriberEntity se = SubscriberEntities.getInstance().getEntity(reqIQ.getFrom().toBareJID());
			if(!se.subscriptions.contains(nodename)) {
				reply.setType(Type.error);
				reply.setError(Condition.forbidden);
				return reply;
			}
		}
		
		boolean owner_requests = Nodes.getInstance().getOwnerOfNode(nodename).equals(reqIQ.getFrom().toBareJID());
		
		LinkedHashMap<String, Subscriber> subs = Nodes.getInstance().getNodesSubscribers(nodename);
		Element pubsub = reply.setChildElement(GetPubSubOwner.ELEMENT_NAME, PubSubOwner.NAMESPACE_URI);
		Element subscriptions = pubsub.addElement("subscriptions");
		subscriptions.addAttribute("node", nodename);
		for (String jid : subs.keySet()) {
			Subscriber sb = subs.get(jid);
			if(sb.getSubscriptionAsString().equals("none")) {
				LogMe.debug("* subscription none for jid '" + jid + "', continuing ...");
				continue;
			} else if (!owner_requests && !sb.getSubscriptionAsString().equals("subscribed") ) {
				LogMe.debug("* subscription is '" + sb.getSubscriptionAsString() + "' for jid '" + jid + "'. User who is requesting this is not owner of the node so we are continuing ...");
				continue;
			} else {
				LogMe.debug("* '" + jid + "'");
			}
			Element subscription = subscriptions.addElement("subscription");
			
			subscription.addAttribute("jid", jid);
			subscription.addAttribute("subscription", sb.getSubscriptionAsString());
			subscription.addAttribute("affiliation", sb.getAffiliationAsString());
		}
		
		LogMe.debug("GetNodesSubscriptions of node '" + nodename + "' handled successfully");
		
		return reply;
	}
	
	public static synchronized IQ getNodesAffiliations(Element affiliations, Element rsm, IQ reqIQ) {
		IQ reply = IQ.createResultIQ(reqIQ);
		
		String nodename = affiliations.attributeValue("node");
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
		
		if(!Nodes.getInstance().hasOpenAccessModel(nodename)) {
			SubscriberEntity se = SubscriberEntities.getInstance().getEntity(reqIQ.getFrom().toBareJID());
			if(!se.subscriptions.contains(nodename)) {
				reply.setType(Type.error);
				reply.setError(Condition.forbidden);
				return reply;
			}
		}
		
		boolean owner_requests = Nodes.getInstance().getOwnerOfNode(nodename).equals(reqIQ.getFrom().toBareJID());
		
		LinkedHashMap<String, Subscriber> subs;
		int count = -1;
		if(rsm != null && rsm.element("max") != null) {
			String after = rsm.element("after") != null ? rsm.element("after").getTextTrim() : null;
			count = Nodes.getInstance().getTotalAmountOfSubscribers(nodename, owner_requests);
			subs = Nodes.getInstance().getNodesSubscribers(nodename, after, Integer.parseInt(rsm.element("max").getTextTrim()), owner_requests);
		} else {
			subs = Nodes.getInstance().getNodesSubscribers(nodename);
		}
		
		//LinkedHashMap<String, Subscriber> subs = Nodes.getInstance().getNodesSubscribers(nodename);
		Element pubsub = reply.setChildElement(GetPubSubOwner.ELEMENT_NAME, PubSubOwner.NAMESPACE_URI);
		Element affs = pubsub.addElement("affiliations");
		affs.addAttribute("node", nodename);

		String first = null;
		String last = null;
		for (String jid : subs.keySet()) {
			
			if(first == null) {
				first = jid;
			}
			
			Subscriber sb = subs.get(jid);
			if(!sb.getAffiliationAsString().equals("outcast") && sb.getSubscriptionAsString().equals("none")) {
				LogMe.debug("* subscription none for jid '" + jid + "', continuing ...");
				continue;
			} else if (!owner_requests && !sb.getSubscriptionAsString().equals("subscribed") ) {
				LogMe.debug("* subscription is '" + sb.getSubscriptionAsString() + "' for jid '" + jid + "'. User who is requesting this is not owner of the node so we are continuing ...");
				continue;
			} else {
				LogMe.debug("* '" + jid + "'");
			}
			Element aff = affs.addElement("affiliation");
			
			//subscription.addAttribute("node", nodename);
			aff.addAttribute("jid", jid);
			//subscription.addAttribute("subscription", sb.getSubscriptionAsString());
			aff.addAttribute("affiliation", sb.getAffiliationAsString());
			last = jid;
		}
		
		if(count > -1) {
			pubsub.add( getRsmElement(first, last, Integer.toString(count)) );
		}
		LogMe.debug("GetNodesAffiliations of node '" + nodename + "' handled successfully.");
		return reply;
	}
	
	//TODO there can be multiple subscriptions. Here we handle only one
	public static synchronized Collection<Packet> handleSubscriptions(Element susbcriptions, IQ reqIQ) {
		Collection<Packet> replies = new ArrayList<Packet>();
		IQ reply = IQ.createResultIQ(reqIQ);
		
		String nodename = susbcriptions.attributeValue("node");
		
		if(nodename == null) {
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
		
		//Element subscription = susbcriptions.element("subscription");
		
		for (Object o : susbcriptions.elements("subscription")) {
		
			Element subscription = (Element)o;
			String jid  = subscription.attributeValue("jid");
			String sub = subscription.attributeValue("subscription");
		
			if(jid == null || jid.isEmpty() || sub == null || sub.isEmpty()) {
				reply.setType(Type.error);
				reply.setError(Condition.bad_request);
				replies.add(reply);
				return replies;
			}
			
			try {
				JID tmp = new JID(jid);
				LogMe.debug("Ready to change jid's '" + tmp.toBareJID() + "'. subscription to '" + sub + "'.");
			} catch (Exception e) {
				LogMe.warning("Cannot change subscription! Invalid JID: '" + jid + "'.");
				reply.setType(Type.error);
				reply.setError(Condition.bad_request);
				replies.add(reply);
				return replies;
			}
			
			if(!Nodes.getInstance().canModifyAffiliationOrSubscription(nodename, reqIQ.getFrom().toBareJID(), jid)) {
				reply.setType(Type.error);
				reply.setError(Condition.forbidden);
				replies.add(reply);
				return replies;
			}
			
			if(sub.equals("none")) {
				//remove affiliation
				Nodes.getInstance().unsubscribeUser(nodename, jid);
			} else if ( sub.equals("subscribed") ){
				// TODO, somethign else?
				Nodes.getInstance().modifyAffiliation(nodename,
						jid, 
						Nodes.getInstance().getUsersAffiliationAsString(nodename, jid), 
						Subscription.subscribed);
			}
			
			/* 
			 * We create notifies for the destinations. 
			 */
			
			Message msg = new Message();
			msg.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
			//msg.setTo(jid);
			msg.setID("bc:" + StringUtils.randomString(5));
			Element event = msg.addChildElement("event", "http://jabber.org/protocol/pubsub#event");
			Element subb = event.addElement("subscription");
			subb.addAttribute("jid", jid);
			subb.addAttribute("subscription", sub);
			subb.addAttribute("node", nodename);
			
			if(sub.equals("none")) {
				subb.addAttribute("affiliation", "none");
			} else {
				subb.addAttribute("affiliation", Nodes.getInstance().getUsersAffiliationAsString(nodename, jid));
			}
			
			for (String receiversJID : SubscriberEntities.getInstance().getDeliveryJids(jid)) {
				msg.setID("bc:" + StringUtils.randomString(5));
				msg.setTo(receiversJID);
				replies.add(msg.createCopy());
			}
			
			LogMe.debug("HandleSubscriptions of node '" + nodename + "' handled successfully.");
		}
		replies.add(reply);
		return replies;
	}
	
	//TODO there can be multiple affliations. Here we handle only one
	public static synchronized Collection<Packet> handleAffiliations(Element affiliations, IQ reqIQ) {
		Collection<Packet> replies = new ArrayList<Packet>();
		IQ reply = IQ.createResultIQ(reqIQ);
		
		String nodename = affiliations.attributeValue("node");
		
		if(nodename == null) {
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
		
		for (Object o : affiliations.elements("affiliation")) {	
			Element affiliation = (Element)o;
			String jid  = affiliation.attributeValue("jid");
			String affi = affiliation.attributeValue("affiliation");
			
			if(jid == null || jid.isEmpty() || affi == null || affi.isEmpty()) {
				reply.setType(Type.error);
				reply.setError(Condition.bad_request);
				replies.add(reply);
				return replies;
			}
			
			try {
				JID tmp = new JID(jid);
				LogMe.debug("Ready to change jid's '" + tmp.toBareJID() + "'. subscription to '" + affi + "'.");
			} catch (Exception e) {
				LogMe.warning("Cannot change affiliation! Invalid JID: '" + jid + "'.");
				reply.setType(Type.error);
				reply.setError(Condition.bad_request);
				replies.add(reply);
				return replies;
			}
			
			if(!Nodes.getInstance().canModifyAffiliationOrSubscription(nodename, reqIQ.getFrom().toBareJID(), jid)) {
				reply.setType(Type.error);
				reply.setError(Condition.forbidden);
				replies.add(reply);
				return replies;
			}
			
			Subscription subs = Subscription.subscribed;
			if(!SubscriberEntities.getInstance().subscriberExists(jid)) {
				// This guy is not subscribed to the system.
				subs = Subscription.unconfigured;
			} 
			
			Nodes.getInstance().modifyAffiliation(nodename, jid, affi, subs);
			//replies.add(reply);
			
			/* 
			 * We create notifies for the destinations. 
			 */
			
			Message msg = new Message();
			msg.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
			msg.setID("bc:" + StringUtils.randomString(5));
			Element event = msg.addChildElement("event", "http://jabber.org/protocol/pubsub#event");
			Element aff = event.addElement("affiliation");
			aff.addAttribute("jid", jid);
			aff.addAttribute("affiliation", affi);
			aff.addAttribute("node", nodename);
			if(affiliation.equals("none")) {
				aff.addAttribute("subscription", "none");
			} else {
				aff.addAttribute("subscription", "subscribed");
			}
			
			for (String receiversJID : SubscriberEntities.getInstance().getDeliveryJids(jid)) {
				msg.setID("bc:" + StringUtils.randomString(5));
				msg.setTo(receiversJID);
				replies.add(msg.createCopy());
			}
			Nodes.getInstance().resetAffiliationRequest(nodename, jid);
		}
		replies.add(reply);
		
		LogMe.debug("HandleAffiliations of node '" + nodename + "' handled successfully");
		
		return replies;
	}
}
