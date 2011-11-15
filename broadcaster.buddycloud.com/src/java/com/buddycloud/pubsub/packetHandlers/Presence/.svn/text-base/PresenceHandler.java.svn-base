package com.buddycloud.pubsub.packetHandlers.Presence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.dom.DOMElement;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.buddycloud.pubsub.BuddycloudPubsubComponent;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.managers.InQueueManager;
import com.buddycloud.pubsub.managers.IncidentManager;
import com.buddycloud.pubsub.managers.OfflineEventStorageConsumer;
import com.buddycloud.pubsub.managers.RosterManager;
import com.buddycloud.pubsub.node.Nodes;
import com.buddycloud.pubsub.node.configs.UserChannelInitBuilder;
import com.buddycloud.pubsub.packetHandlers.PacketHandlerInterface;
import com.buddycloud.pubsub.roster.RosterChange;
import com.buddycloud.pubsub.roster.RosterChangeAction;
import com.buddycloud.pubsub.subscriber.Resource;
import com.buddycloud.pubsub.subscriber.ResourceType;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;
import com.buddycloud.pubsub.subscriber.SubscriberEntity;
import com.buddycloud.pubsub.subscriber.SubscriberEntityType;
import com.buddycloud.pubsub.subscriber.Subscription;
import com.buddycloud.pubsub.utils.SystemStats;

public class PresenceHandler implements PacketHandlerInterface {

	/**
	 * This is a big mess. Write something sane here!
	 * 
	 * TODO number one. If this is done wrong, the whole system might
	 * suffer about speed problems.
	 */
	
	@Override
	public Collection<Packet> ingestPacket(Packet p) {
		Presence pr = (Presence)p;
		Collection<Packet> replies = new ArrayList<Packet>();
		
		if(pr.getType() == null || pr.getType() == Presence.Type.unavailable) {
			SubscriberEntity e = SubscriberEntities.getInstance().getEntity(pr.getFrom().toBareJID());
			if(e == null && pr.getType() == null) {
				
				e = new SubscriberEntity(pr.getFrom(), pr.getType(), SubscriberEntityType.temporary);
				e.addResouce(new Resource(pr.getFrom().getResource(), ResourceType.iq_pubsub));
				SubscriberEntities.getInstance().addEntity(e);
				RosterManager.getInstance().addChange(new RosterChange(pr.getFrom().toBareJID(), RosterChangeAction.addTemporary));
				
				Presence reply = new Presence();
				reply.setTo(pr.getFrom());
				reply.setFrom(pr.getTo());
				reply.setType(null);
				reply.setStatus("Small happy Buddycloud's pubsub server!");
				replies.add(reply);
				
				SystemStats.getInstance().increase(pr.getFrom(), SystemStats.ANONYMOUS);
				
			} else if (e != null){
				// This means unavailable or available of known user
				if(pr.getType() == Presence.Type.unavailable && e.isTemporarySubscription()) {
					removeUser(pr.getFrom().toBareJID(), true);
					SystemStats.getInstance().decrease(pr.getFrom(), SystemStats.ANONYMOUS);
				} else {
					//e.presence = pr.getType();
					
					if(pr.getType() == null) {
						//e.presence = pr.getType();
						LogMe.debug("Users '" + pr.getFrom().toBareJID() + "' became online. Let's see if it's a buddycloud client.");
					
						//User is online
						//e.addResouce(pr.getFrom().getResource());
							
						// This can be part of the presence, if it is, we query all the
						// users nodes.
						// <set xmlns='http://jabber.org/protocol/rsm'>
						//   <after>id_of_my_last_item_on_this_node</after>
						// </set
						Element set = pr.getChildElement("set", "http://jabber.org/protocol/rsm");
						if(set != null && set.element("after") != null) {
							LogMe.debug("User has set set/after element to the presence. Let's fetch all new possible items of user's nodes.");
							e.presence = pr.getType();
							if ( e.addResouce(new Resource(pr.getFrom().getResource(), ResourceType.iq_pubsub)) ) {
								SystemStats.getInstance().increase(pr.getFrom(), SystemStats.KNOWN_USER);	
							}
							OfflineEventStorageConsumer.getInstance().addChange(pr.getFrom());
							RosterManager.getInstance().addChange(new RosterChange(pr.getFrom().toBareJID(), RosterChangeAction.updateLastSeen));
							
	//						<iq type='get'
	//						    from='pamela.a@gtalk.com/boobsmobile'
	//						    to='pubsub.buddycloud.com'
	//						    id='items1'>
	//						    <pubsub xmlns='http://jabber.org/protocol/pubsub'>
	//						       <items node='/channel/pamela.a@gtalk.com'>
	//						          <set xmlns='http://jabber.org/protocol/rsm'>
	//						             <after>id_of_my_last_item_on_this_node</after>
	//						          </set>
	//						       </items>
	//						    </pubsub>
	//						</iq>
							// TODO, Refactor. Test to see if this can work
							LinkedHashSet<String> nodes = this.addUsersChannels(SubscriberEntities.getInstance().getSubscribedNodes(pr.getFrom().toBareJID()));
							for (String nodename : nodes) {
								LogMe.debug("Starting to create items request for node '" + nodename + "'.");
								IQ iq = new IQ();
								iq.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
								iq.setTo(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
								iq.setType(IQ.Type.get);
								Element pubsub = iq.setChildElement("pubsub", "http://jabber.org/protocol/pubsub");
								Element items = pubsub.addElement("items");
								items.addAttribute("node", nodename);
								items.add(set.createCopy());
								
								Element headers = new DOMElement("headers", new Namespace("", "http://jabber.org/protocol/shim"));
								Element header = headers.addElement("header");
								header.addAttribute("name", "Source");
								if(pr.getFrom().getResource() != null) {
									header.setText(pr.getFrom().toBareJID() + "/" + pr.getFrom().getResource());
								} else {
									header.setText(pr.getFrom().toBareJID());
								}
								items.add(headers);
								
								LogMe.debug("Ready to add IQ to InQueueManager. '" + iq.toXML() + "'.");
								
								InQueueManager.getInstance().put(iq);
							}
						} else {
							IQ iq = new IQ();
							iq.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
							iq.setTo(pr.getFrom());
							iq.setType(IQ.Type.get);
							//<query xmlns='http://jabber.org/protocol/disco#info'/>
							iq.setChildElement("query", "http://jabber.org/protocol/disco#info");
							replies.add(iq);
						}
					} else {
						//User is offline
						e.removeResouce(pr.getFrom().getResource());
						if(e.amountOfResources() < 1) {
							// No resources left.
							LogMe.debug("User '" + pr.getFrom().toBareJID() + "' does not have any resources connected. Will set it offline.");
							e.presence = pr.getType();
						}
					}
				}
			}
			
		} else if (pr.getType() == Presence.Type.probe) {
			
			Presence reply = new Presence();
			reply.setTo(pr.getFrom());
			reply.setFrom(pr.getTo());
			reply.setType(null);
			reply.setStatus("Small happy Buddycloud's pubsub server!");
			replies.add(reply);
			
		} else if (pr.getType() == Presence.Type.subscribe) {
			
			if( IncidentManager.getInstance().isBlockedJID(pr.getFrom().toBareJID()) ) {
				
				Presence reply = new Presence();
				reply.setTo(pr.getFrom());
				reply.setFrom(pr.getTo());
				reply.setType(Presence.Type.unsubscribed);
				replies.add(reply);
				
			} else {
			
				Presence reply = new Presence();
				reply.setTo(pr.getFrom());
				reply.setFrom(pr.getTo());
				reply.setType(Presence.Type.subscribed);
				replies.add(reply);
			
				Presence req = new Presence();
				req.setTo(pr.getFrom());
				req.setFrom(pr.getTo());
				req.setType(Presence.Type.subscribe);
				replies.add(req);
			
			}
			
		} else if (pr.getType() == Presence.Type.subscribed) {
			
			if( IncidentManager.getInstance().isBlockedJID(pr.getFrom().toBareJID()) ) {
				
				Presence reply = new Presence();
				reply.setTo(pr.getFrom());
				reply.setFrom(pr.getTo());
				reply.setType(Presence.Type.unsubscribed);
				replies.add(reply);
				
			} else if(SubscriberEntities.getInstance().getEntity(pr.getFrom().toBareJID()) == null) {
				SubscriberEntities.getInstance().addEntity(new SubscriberEntity(pr.getFrom(), null));
				RosterManager.getInstance().addChange(new RosterChange(pr.getFrom().toBareJID(), RosterChangeAction.add));
				
				UserChannelInitBuilder.createUserChannels(pr.getFrom().toBareJID());
				
			} else if(SubscriberEntities.getInstance().getEntity(pr.getFrom().toBareJID()).isTemporarySubscription()) {
				this.updateUsersAffiliations(pr.getFrom().toBareJID());
			}
			
		} else if (pr.getType() == Presence.Type.unsubscribe) {
			
			SubscriberEntity e = SubscriberEntities.getInstance().getEntity(pr.getFrom().toBareJID());
			if(e != null) {
				replies.addAll( removeUser(pr.getFrom().toBareJID(), false) );
			}
			
			Presence reply = new Presence();
			reply.setTo(pr.getFrom());
			reply.setFrom(pr.getTo());
			reply.setType(Presence.Type.unsubscribed);
			replies.add(reply);
			
			reply = new Presence();
			reply.setTo(pr.getFrom());
			reply.setFrom(pr.getTo());
			reply.setType(Presence.Type.unsubscribe);
			replies.add(reply);
			
			//TODO
			// I think here we need to send some stanzas
		} else if (pr.getType() == Presence.Type.unsubscribed) {
			//eplies.addAll( removeUser(pr.getFrom().toBareJID()) );
			//TODO
			// I think here we need to send some stanzas
			SubscriberEntity e = SubscriberEntities.getInstance().getEntity(pr.getFrom().toBareJID());
			if(e != null) {
				replies.addAll( removeUser(pr.getFrom().toBareJID(), false) );
			}
		} else if (pr.getType() == Presence.Type.error) {
			// TODO
			// Check all the possibilities we might have here.
			// So far possible values are:
			// - forbidden (we should remove)
			// - remove-server-not-found (we should remove)
		}
		return replies;
	}
	
	private LinkedHashSet<String> addUsersChannels(LinkedList<String> nodes) {
		LinkedHashSet<String> newNodes = new LinkedHashSet<String>();
		for (String nodename : nodes) {
			if(nodename.startsWith("/user/") && nodename.endsWith("/channel")) {
				String user = nodename.substring("/user/".length(), nodename.indexOf("/", "/user/".length()));
				newNodes.add("/user/" + user + "/geo/current");
				newNodes.add("/user/" + user + "/geo/future");
				newNodes.add("/user/" + user + "/geo/previous");
				newNodes.add("/user/" + user + "/mood");
			}
		}
		//removing duplicates
		newNodes.addAll(nodes);
		return newNodes;
	}
	
	// TODO refactor this.
	public static Collection<Packet> removeUser(String bareJid, boolean temporarUser) {
		Collection<Packet> replies = new ArrayList<Packet>();
		
		if(temporarUser) {
			LinkedList<String> nodes = SubscriberEntities.getInstance().getSubscribedNodes(bareJid);
			for (String nodename : nodes) {
				Nodes.getInstance().unsubscribeUser(nodename, bareJid);
				// TODO, add here all the nodes user is unsubscribed from.
			}
		}
//		
//		IQ deleteIQ = new IQ();
//		deleteIQ.setType(Type.set);
//		deleteIQ.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
//		deleteIQ.setTo(deleteIQ.getFrom());
//		
//		Element delete = deleteIQ.setChildElement("pubsub", PubSubOwner.NAMESPACE_URI).addElement("delete");
//		
//		if(Nodes.getInstance().isKnownNode("/user/" + bareJid + "/channel")) {
//			delete.addAttribute("node", "/user/" + bareJid + "/channel");
//			//replies.add(deleteIQ.createCopy());
//			InQueueManager.getInstance().put(deleteIQ.createCopy());
//		}
//		
//		if(Nodes.getInstance().isKnownNode("/user/" + bareJid + "/mood")) {
//			delete.addAttribute("node", "/user/" + bareJid + "/mood");
//			//replies.add(deleteIQ.createCopy());
//			InQueueManager.getInstance().put(deleteIQ.createCopy());
//		}
//		
//		if(Nodes.getInstance().isKnownNode("/user/" + bareJid + "/geo/current")) {
//			delete.addAttribute("node", "/user/" + bareJid + "/geo/current");
//			//replies.add(deleteIQ.createCopy());
//			InQueueManager.getInstance().put(deleteIQ.createCopy());
//		}
//		
//		if(Nodes.getInstance().isKnownNode("/user/" + bareJid + "/geo/previous")) {
//			delete.addAttribute("node", "/user/" + bareJid + "/geo/previous");
//			//replies.add(deleteIQ.createCopy());
//			InQueueManager.getInstance().put(deleteIQ.createCopy());
//		}
//		
//		if(Nodes.getInstance().isKnownNode("/user/" + bareJid + "/geo/future")) {	
//			delete.addAttribute("node", "/user/" + bareJid + "/geo/future");
//			//replies.add(deleteIQ.createCopy());
//			InQueueManager.getInstance().put(deleteIQ.createCopy());
//		}
		
		RosterManager.getInstance().addChange(new RosterChange(bareJid, RosterChangeAction.remove));
	
		return replies;
	}
	
	private void updateUsersAffiliations(String bareJID) {
		SubscriberEntities.getInstance().setAsNormal(bareJID);
		LinkedList<String> nodes = SubscriberEntities.getInstance().getSubscribedNodes(bareJID);
		for (String nodename : nodes) {
			// TODO
			// possible bug here:
			// users get's banned => user removes subscription to the server => user follows as anonymous => user subscribes again => will become a publisher 
			// * user channels default membership. Not directly as publisher.
			Nodes.getInstance().modifyAffiliation(nodename, bareJID, "publisher", Subscription.subscribed);
			// TODO
			// we should notify users here too about changed state...
		}
		RosterManager.getInstance().addChange(new RosterChange(bareJID, RosterChangeAction.updateAsNormal));
		
		UserChannelInitBuilder.createUserChannels(bareJID);
	}
}
