package com.buddycloud.pubsub.packetHandlers.Message;

import java.util.ArrayList;
import java.util.Collection;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Message.Type;

import com.buddycloud.pubsub.managers.InQueueManager;
import com.buddycloud.pubsub.packetHandlers.PacketHandlerInterface;
import com.buddycloud.pubsub.subscriber.Resource;
import com.buddycloud.pubsub.subscriber.ResourceType;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;
import com.buddycloud.pubsub.utils.SystemStats;

public class MessageHandler implements PacketHandlerInterface {

	@Override
	public Collection<Packet> ingestPacket(Packet p) {
		
		Collection<Packet> replies =  new ArrayList<Packet>();
		Message msg = (Message)p;
		
		if(msg.getType() == Type.error) {
			return replies;
		}
		
		if(msg.getBody() == null || msg.getBody().equals("")) {
			return replies;
		}

		if( SubscriberEntities.getInstance().getEntity(msg.getFrom().toBareJID()) != null ) {
	

			
			if(msg.getBody().equals("/follow")) {
				// Something here?
				// This is hack, should be done only once.
				if( SubscriberEntities.getInstance().getEntity(msg.getFrom().toBareJID()).addResouce(new Resource(msg.getFrom().getResource(), ResourceType.msg_body)) ) {
					SystemStats.getInstance().increase(msg.getFrom(), SystemStats.KNOWN_USER);
				}
				SubscriberEntities.getInstance().getEntity(msg.getFrom().toBareJID()).presence = null; //means online
				
			} else {
				IQ pubsubPublish = new IQ();
				pubsubPublish.setTo(msg.getTo().toBareJID());
				pubsubPublish.setFrom(msg.getTo().toBareJID());
				pubsubPublish.setType(IQ.Type.set);
				
				Element pubsub = pubsubPublish.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
				Element publish = pubsub.addElement("publish");
				publish.addAttribute("node", "/user/" + msg.getFrom().toBareJID() + "/channel");
				
				Element heads = new DOMElement("headers", new org.dom4j.Namespace("", "http://jabber.org/protocol/shim"));
				Element header = heads.addElement("header");
				header.addAttribute("name", "Source");
				header.setText(msg.getFrom().toBareJID());
				publish.add(heads);
				
				Element item = publish.addElement("item");
				
				Element newEntry = new DOMElement("entry", new org.dom4j.Namespace("", "http://www.w3.org/2005/Atom"));
				Element newAuthor = newEntry.addElement("author");
				newAuthor.setText(msg.getFrom().toBareJID());
				
				Element newContent = newEntry.addElement("content");
				newContent.addAttribute("type", "text");
				newContent.addText(msg.getBody());
				
				item.add(newEntry);
	
				InQueueManager.getInstance().put(pubsubPublish);
			}
		}
		
		// TODO add this to, it's still usable.
//		if( !IncidentManager.getInstance().isAdminJID(msg.getFrom().toBareJID()) ) {
//			return replies;
//		}
//		
//		String bareJID = msg.getBody().trim();
//		if(SubscriberEntities.getInstance().subscriberExists(bareJID)) {
//			PresenceHandler.removeUser(bareJID, true);
//		}
//		IncidentManager.getInstance().addBlockedJID(bareJID);
//		
//		msg.setTo(msg.getFrom());
//		msg.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
//		msg.setBody("Removed and blocked '" + bareJID + "'. Poor boy, he should get a life ...");
//		
//		replies.add(msg);
		return replies;
	}

}
