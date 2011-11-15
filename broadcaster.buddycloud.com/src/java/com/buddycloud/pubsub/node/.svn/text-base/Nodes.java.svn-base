package com.buddycloud.pubsub.node;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Element;

import com.buddycloud.pubsub.Item.Entry;
import com.buddycloud.pubsub.Item.PayloadType;
import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.db.DBItem;
import com.buddycloud.pubsub.db.DBNode;
import com.buddycloud.pubsub.db.DBPubsubhubbubSubscriptions;
import com.buddycloud.pubsub.db.DBSubscription;
import com.buddycloud.pubsub.exceptions.AlreadySubscribedException;
import com.buddycloud.pubsub.exceptions.JidNotInWhiteListException;
import com.buddycloud.pubsub.exceptions.JidOutcastedException;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.node.configs.AccessModel;
import com.buddycloud.pubsub.subscriber.PubsubhubbubSubscriber;
import com.buddycloud.pubsub.subscriber.Subscriber;
import com.buddycloud.pubsub.subscriber.Subscription;


public class Nodes {
	private static Nodes instance;
	
	private ConcurrentHashMap<String, Leaf> nodes = new ConcurrentHashMap<String, Leaf>();
	
	private ConcurrentHashMap<String, Boolean> nodenames = new ConcurrentHashMap<String, Boolean>();
	
	private Nodes() {
		
	}
	
	public static Nodes getInstance() {
		if(instance == null) {
			instance = new Nodes();
		}
		return instance;
	}
	
	public void addAsKnownNode(String nodename) {
		//this.nodenames.add(nodename);
		this.nodenames.put(nodename, true);
	}
	
	public boolean isKnownNode(String nodename) {
		return this.nodenames.containsKey(nodename);
	}
	
	private void removeFromKnownNode(String nodename) {
		this.nodenames.remove(nodename);
	}
	
	public Leaf addNode(String nodename, Leaf node) {
		this.addAsKnownNode(nodename);
		return this.nodes.putIfAbsent(nodename, node);
	}
	
	public void removeNode(String nodename) {
		this.removeFromKnownNode(nodename);
		this.nodes.remove(nodename);
	}
	
	public boolean nodeExists(String nodename) {
		return this.nodes.containsKey(nodename);
	}
	
	public Leaf getLeafnode(String nodename) {
		return this.nodes.get(nodename);
	}
	
	public Configuration getConfiguration(String nodename) {
		this.initializeNode(nodename);
		return this.nodes.get(nodename).getConfiguration();
	}
	
	public void subscribeJid(String nodename, String jid) throws AlreadySubscribedException, JidOutcastedException, JidNotInWhiteListException {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		this.nodes.get(nodename).setAsSubscriber(jid);
	}
	
	public boolean addPubsubhubbubSubscriber(String nodename, String topic, String verify_token, String lease_seconds, String secret, String callback) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).addPubsubhubbubSubscriber(nodename, topic, verify_token, lease_seconds, secret, callback);
	}	
	
	public boolean removePubsubhubbubSubscriber(String nodename, String topic, String verify_token, String lease_seconds, String secret, String callback) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).removePubsubhubbubSubscriber(nodename, topic, verify_token, lease_seconds, secret, callback);
	}
	
	public boolean canModifyAffiliationOrSubscription(String nodename, String whoJID, String whosJID) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).canModifyAffiliationOrSubscription(whoJID, whosJID);
	}
	
	public boolean canPublishItem(String nodename, String jid) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).canPublishItem(jid);
	}
	
	public boolean canDeleteNode(String nodename, String jid) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).canDeleteNode(jid);
	}
	
	public boolean canConfigureNode(String nodename, String jid) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).canConfigureNode(jid);
	}
	
	public boolean canRemoveItem(String nodename, String jid) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).canRemoveItem(jid);
	}
	
	public void modifyNodeConfiguration(String nodename, Map<String, String> nodeConf) {
		this.initializeNode(nodename);
		this.nodes.get(nodename).modifyConfiguration(nodeConf);
	}
	
	public boolean userIsSubscribedToNode(String nodename, String bareJID) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).userIsSubscribedToNode(bareJID);
	}
	
	public boolean isAffiliationRequested(String nodename, String bareJID) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).isAffiliationRequested(bareJID);
	}
	
	public void setAffiliationRequested(String nodename, String bareJID) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		this.nodes.get(nodename).setAffiliationRequested(bareJID);
	}
	
	public void resetAffiliationRequest(String nodename, String bareJID) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		this.nodes.get(nodename).resetAffiliationRequest(bareJID);
	}
	
	public void modifyAffiliation(String nodename, String jid, String affiliation, Subscription subsrcription) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		this.nodes.get(nodename).modifyAffiliation(jid, affiliation, subsrcription);
	}
	
	public void putAsSubscriber(String nodename, String jid, String affiliation, Subscription subsrcription) {
		this.nodes.get(nodename).putAsSubscriber(jid, affiliation, subsrcription);
	}
	
	public void putAsPubsubhubbubSubscriber(String nodename, PubsubhubbubSubscriber ps) {
		this.nodes.get(nodename).putAsPubsubhubbubSubscriber(ps);
	}
	
	public LinkedList <PubsubhubbubSubscriber> getPubsubhubbubSubscriber(String nodename) {
		return this.nodes.get(nodename).getPubsubhubbubSubscribers();
	}
	
	public String getUsersAffiliationAsString(String nodename, String jid) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).getUsersAffiliationAsString(jid);
	}
	
	public String getNodesDefaultAffiliationAsString(String nodename) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).getDefaultAffiliation().toString();
	}
	
	public Subscriber getUsersNodeSubscriber(String nodename, String jid) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).getSubscriber(jid);
	}
	
	public PayloadType getNodePayload(String nodename) {
		this.initializeNode(nodename);
		return this.nodes.get(nodename).getPayloadType();
	}
	
	public LinkedHashMap<String, Subscriber> getNodesSubscribers(String nodename) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).getNodesSubscribers();
	}
	
	public LinkedHashMap<String, Subscriber> getNodesSubscribers(String nodename, String after, int amount, boolean fromOwner) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).getNodesSubscribers(after, amount, fromOwner);
	}
	
	public int getTotalAmountOfSubscribers(String nodename, boolean fromOwner) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).getTotalAmountOfSubscribers(fromOwner);
	}
	
	public boolean hasOpenAccessModel(String nodename) {
		this.initializeNode(nodename);
		if( this.nodes.get(nodename).getAccessModel().equals(AccessModel.open) ) {
			return true;
		}
		return false;
	}
	
	public void unsubscribeUser(String nodename, String bareJID) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		this.nodes.get(nodename).unsubscribeUser(bareJID);
	}
	
	public boolean canUnsubscribe(String nodename, String bareJID) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).canUnsubscribe(bareJID);
	}
	
	public Long addItem(String nodename, Element item) {
		Entry i = new Entry(item);
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		this.initializeNodesItems(nodename);
		this.nodes.get(nodename).addItem(i);
		return i.getId();
	}
	
	public void addItem(String nodename, Element item, Long id) {
		Entry i = new Entry(item, id);
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		this.initializeNodesItems(nodename);
		this.nodes.get(nodename).addItem(i);
	}
	
	public void addInitItem(String nodename, Entry e) {
		this.nodes.get(nodename).addInitItem(e);
	}
	
	public Collection<String> getCollectionOfPossibleReceivers(String nodename) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).getCollectionOfPossibleReceivers();
	}
	
	public Collection<String> getCollectionOfNodesModerators(String nodename) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).getCollectionOfNodesModerators();
	}
	
	public void removeItem(String nodename, String itemid) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		this.initializeNodesItems(nodename);
		this.nodes.get(nodename).removeItem(itemid);
	}
	
	public Collection<Entry> getEntriesSinceID(String nodename, String itemid) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		this.initializeNodesItems(nodename);
		return this.nodes.get(nodename).getEntriesSinceID(itemid);
	}
	
	public String getOwnerOfNode(String nodename) {
		this.initializeNode(nodename);
		this.initializeNodesSubscriptions(nodename);
		return this.nodes.get(nodename).getOwner();
	}
	
	private void initializeNode(String nodename) {
		if(this.nodeExists(nodename)) {
			this.nodes.get(nodename).updateLastActionTime();
			return;
		}
		Leaf node = DBNode.getInstance().getLeafNode(nodename);
		this.addNode(nodename, node);
	}
	
	private void initializeNodesSubscriptions(String nodename) {
		if( this.nodes.get(nodename).getState().equals(LeafState.subscriptionscached) || 
			this.nodes.get(nodename).getState().equals(LeafState.itemscached)) {
			return;
		} else {
			DBSubscription.getInstance().initNodesSubscriptions(nodename);
			DBPubsubhubbubSubscriptions.getInstance().initNodesPubsubhubbubSubscriptions(nodename);
			if(!this.nodes.get(nodename).getState().equals(LeafState.itemscached)) {
				this.nodes.get(nodename).setState(LeafState.subscriptionscached);
			}
		}
	}
	
	private void initializeNodesItems(String nodename) {
		if(this.nodes.get(nodename).getState().equals(LeafState.itemscached)) {
			return;
		}
		DBItem.getInstance().fillLeafnodeWithItems(nodename);
		this.nodes.get(nodename).setState(LeafState.itemscached);
	}
	
	public void setNodesState(String nodename, LeafState state) {
		this.nodes.get(nodename).setState(state);
	}
	
	private void cleanNode(String nodename) {
		LogMe.debug("* removing node '" + nodename + "' from cache.");
		this.nodes.get(nodename).cleanup();
		this.nodes.remove(nodename);
	}
	
	public void cleanCacheFromUnactiveNodes() {
		Long before = Runtime.getRuntime().freeMemory();
		LogMe.debug("Free memory before cleaning: '" + before +"'.");
		
		Long valid = System.currentTimeMillis() - (Conf.getInstance().getConfInteger(Conf.CACHE_LIFETIME, 15) * 60 * 1000L);
		for (String nodename : this.nodes.keySet()) {
			if(!this.nodes.get(nodename).hasBeenInactiveLongerSince(valid)) {
				continue;
			}
			this.cleanNode(nodename);
		}
		
		Long after = Runtime.getRuntime().freeMemory();
		Long max = Runtime.getRuntime().maxMemory();
		Long total = Runtime.getRuntime().totalMemory();
		LogMe.debug("Free memory after cleaning : '" + after +"', '" + total + "/" + max + "' (freed " + (after - before) +").");
	}
	
	public Element getAsAtomEntrySource(String nodename) {
		this.initializeNode(nodename);
		return this.nodes.get(nodename).getAsAtomEntrySource();
	}
	
	public String getChannelsHTMLURL(String nodename) {
		this.initializeNode(nodename);
		return this.nodes.get(nodename).getChannelsHTMLURL();
	}
	
	public String getChannelAvatar(String nodename) {
                this.initializeNode(nodename);
                return this.nodes.get(nodename).getChannelAvatar();
        }

	public String getDescription(String nodename) {
                this.initializeNode(nodename);
                return this.nodes.get(nodename).getDescription();
        }

	public String getChannelsAtomFeedURL(String nodename) {
		this.initializeNode(nodename);
		return this.nodes.get(nodename).getChannelsAtomFeedURL();
	}
	
	public boolean isHiddenNode(String nodename) {
		this.initializeNode(nodename);
		return this.nodes.get(nodename).isHiddenNode();
	}
}
