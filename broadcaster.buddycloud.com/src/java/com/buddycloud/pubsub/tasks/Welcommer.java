package com.buddycloud.pubsub.tasks;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import com.buddycloud.pubsub.BuddycloudPubsubComponent;
import com.buddycloud.pubsub.Item.Entry;
import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.managers.InQueueManager;
import com.buddycloud.pubsub.node.Nodes;
import com.buddycloud.pubsub.packetHandlers.IQ.Namespace.PubSubOwner;

public class Welcommer implements Runnable {

	JID jid;
	
	public static String fromBareJID = "welcome.bot@buddycloud.com";
	
	public Welcommer(JID jid) {
		this.jid = jid;
	}
	
	public void run() {
        
		String possibleWEBURL = "http://buddycloud.com/user/" + this.jid.getDomain() + "/" + this.jid.getNode();
		
    	Long start = System.currentTimeMillis();
		
        try {
        	//Pause for 10 seconds
        	Thread.sleep(10*1000);
	
	        //<iq type='set'
	        //    from='hamlet@denmark.lit/elsinore'
	        //    to='pubsub.shakespeare.lit'
	        //    id='ent2'>
	        //    <pubsub xmlns='http://jabber.org/protocol/pubsub#owner'>
	        //      <subscriptions node='princely_musings'>
	        //              <subscription jid='bard@shakespeare.lit' subscription='subscription'/>
	        //      </subscriptions>
	        //    </pubsub>
	        //</iq>
        	
        	for (String nodename : Conf.getInstance().getPredefinedChannels()) {
	        	String fromJID = Nodes.getInstance().getOwnerOfNode(nodename);
	        	if(fromJID == null || fromJID.equals("")) {
	        		LogMe.warning("No owner found for node '" + nodename + "'! Cannot autosubscribe.");
	        		continue;
	        	}
	        	IQ iq = new IQ();
				iq.setFrom(fromJID + "/bc-internal");
				iq.setTo(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
				iq.setType(IQ.Type.set);
				Element pubsub = iq.setChildElement("pubsub", PubSubOwner.NAMESPACE_URI);
				Element subs = pubsub.addElement("subscriptions");
				subs.addAttribute("node", nodename);
				Element sub = subs.addElement("subscription");
				sub.addAttribute("jid", this.jid.toBareJID());
				sub.addAttribute("subscription", "subscribed");	
        	
			InQueueManager.getInstance().put(iq);
		}
        	
        	for (String message : Conf.getInstance().getWelcomeMessages()) {
				if(message == null || message.equals("")) {
					LogMe.debug("Empty or null welcome message. Let's quit sending them.");
					break;
				}
				
				message = message.replaceAll("#USER_WEB_CHANNEL", possibleWEBURL);
				
				IQ welcomeMSG = new IQ();
				welcomeMSG.setType(IQ.Type.set);
				welcomeMSG.setTo(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
				welcomeMSG.setFrom(fromBareJID);
				
				Element publish = welcomeMSG.setChildElement("pubsub", "http://jabber.org/protocol/pubsub").addElement("publish");
				publish.addAttribute("node", "/user/" + this.jid.toBareJID() + "/channel");
				publish.addElement("item").add(Entry.getAsAtomEntry(message));
			
				//Pause for 5 seconds
	        	Thread.sleep(5*1000);
				InQueueManager.getInstance().put(welcomeMSG);
        	}
            
        	LogMe.info("Welcome messages send to '" + this.jid.toBareJID() + "' in '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
        
        } catch (InterruptedException e) {
        	LogMe.info("Welcommer interrupted!");
        }
	}
	
}
