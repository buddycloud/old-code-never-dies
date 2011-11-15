package com.buddycloud.pubsub.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.dom.DOMElement;

import com.buddycloud.pubsub.BuddycloudPubsubComponent;
import com.buddycloud.pubsub.Item.Entry;
import com.buddycloud.pubsub.Item.ItemChange;
import com.buddycloud.pubsub.Item.ItemChangeAction;
import com.buddycloud.pubsub.Item.PayloadType;
import com.buddycloud.pubsub.exceptions.AlreadySubscribedException;
import com.buddycloud.pubsub.exceptions.InvalidAccessModelException;
import com.buddycloud.pubsub.exceptions.InvalidPublishModelException;
import com.buddycloud.pubsub.exceptions.JidNotInWhiteListException;
import com.buddycloud.pubsub.exceptions.JidOutcastedException;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.managers.ItemManager;
import com.buddycloud.pubsub.managers.LeafnodeManager;
import com.buddycloud.pubsub.managers.PubsubhubbubSubscriptionManager;
import com.buddycloud.pubsub.managers.SubscriptionManager;
import com.buddycloud.pubsub.node.configs.AccessModel;
import com.buddycloud.pubsub.node.configs.PublishModel;
import com.buddycloud.pubsub.subscriber.Affiliation;
import com.buddycloud.pubsub.subscriber.PubsubhubbubSubscriber;
import com.buddycloud.pubsub.subscriber.PubsubhubbubSubscriptionChange;
import com.buddycloud.pubsub.subscriber.PubsubhubbubSubscriptionChangeAction;
import com.buddycloud.pubsub.subscriber.Subscriber;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;
import com.buddycloud.pubsub.subscriber.Subscription;
import com.buddycloud.pubsub.subscriber.SubscriptionChange;
import com.buddycloud.pubsub.subscriber.SubscriptionChangeAction;
import com.buddycloud.pubsub.tasks.Welcommer;

public class Leaf implements Cloneable {

	//private ConcurrentHashMap<String, Subscriber> subscribers = new ConcurrentHashMap<String, Subscriber>();
	private LinkedHashMap<String, Subscriber> subscribers = new LinkedHashMap<String, Subscriber>();
	
	private String nodeName = null;
	
	private Configuration configuration = new Configuration();
	
	private LinkedList <Entry>entries = new LinkedList<Entry>();
	
	private Long lastItemId = -1L;
	
	private LeafState state = LeafState.initialized;
	
	private Long lastActionTime = System.currentTimeMillis();
	
	private int dbid = 0;
	
	private LinkedList <PubsubhubbubSubscriber>PubsubhubbubSubscribers = new LinkedList<PubsubhubbubSubscriber>();
	
	public Leaf(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public Leaf(String nodeName, int dbid) {
		this.nodeName = nodeName;
		this.setDbId(dbid);
	}
	
	@Override 
	public Leaf clone() {
        try {
            final Leaf result = (Leaf) super.clone();
            // copy fields that need to be copied here!
            result.configuration = this.configuration;
			result.entries = new LinkedList<Entry>();
			result.nodeName = this.nodeName;
            return result;
        } catch (final CloneNotSupportedException ex) {
            throw new AssertionError();
        }
	}
	
	public void setDbId(int dbid) {
		LogMe.debug("Setting leafnodes db id to '" + Integer.toString(dbid) + "'.");
		this.dbid = dbid;
	}
	
	public int getDbId() {
		return this.dbid;
	}
	
	public void setCreated(String created) {
		LogMe.debug("Setting leafnode's created date to '" + created +"'.");
		this.configuration.created = created;
	}
	
	public void setMetadata(HashMap<String, String> metadata) {
		if(metadata.get("rank") != null) {
			this.configuration.rank = metadata.get("rank");
		}
		if(metadata.get("popularity") != null) {
			this.configuration.popularity = metadata.get("popularity");
		}
		if(metadata.get("latitude") != null) {
			this.configuration.latitude = metadata.get("latitude");
		}
		if(metadata.get("longitude") != null) {
			this.configuration.longitude = metadata.get("longitude");
		}
		if(metadata.get("location") != null) {
			this.configuration.location = metadata.get("location");
		}
		if(metadata.get("followers") != null) {
			this.configuration.followers = metadata.get("followers");
		}
		if(metadata.get("is_hidden") != null) {
			this.configuration.is_hidden = metadata.get("is_hidden");
		}
	}
	public void setOwner(String owner) {
		this.configuration.owner = owner;
	}
	
	public static LeafType getLeafType(String nodename) {
		if(nodename.startsWith("/channel")) {
			return LeafType.topicchannel;
		} else if(nodename.endsWith("/channel")) {
			return LeafType.userchannel;
		} else if(nodename.endsWith("/mood")) {
			return LeafType.usermood;
		}
		return LeafType.usergeolocation;
	}
	
	public String getCreated() {
		return this.configuration.created;
	}
	
	public boolean isHiddenNode() {
		if("true".equals(this.configuration.is_hidden)) {
			return true;
		}
		return false;
	}
	
	// TODO
	// Refactor this. It's now used only just before removing
	// the node from cache.
	public void cleanup() {
		this.subscribers.clear();
		this.configuration = null;
		this.entries.clear();
		this.state = LeafState.initialized;
	}
	
	public static LinkedHashMap<String, Subscriber> getLinkedHasMapAsSorted(LinkedHashMap<String, Subscriber> subscribers) {
		LinkedHashMap<String, Subscriber> sorted = new LinkedHashMap<String, Subscriber>();
		
		Set<String> orderedSetOfJids = new TreeSet<String>(subscribers.keySet());
		for (String item : orderedSetOfJids) {
			//System.out.print(item + " ");
			sorted.put(item, subscribers.get(item));
		}
		return sorted;
	}
	
	public void sortSubscribersList() {
		this.subscribers = getLinkedHasMapAsSorted(this.subscribers);
	}
	
	/**
	 * Removes entry matching with passed itemID from this Leafnode. After removing
	 * the entry from this node, action to remove the item from the database
	 * is also launched.
	 * 
	 * Leafnode's variable lastItemId is also updated to the correct 
	 * (since this might change).
	 * 
	 * @param itemid
	 */
	public synchronized void removeItem(String itemid) {
		Long id = Long.parseLong(itemid);
		for (int j=0; j < this.entries.size(); j++) {
            if(this.entries.get(j).getId().equals(id)) {
            	Entry entry = this.entries.remove(j);
            	ItemManager.getInstance().addChange(new ItemChange(this.nodeName, entry, ItemChangeAction.delete));
            	if(j == 0 && !this.entries.isEmpty()) {
            		this.lastItemId = this.entries.getFirst().getId();
            	} else if (j == 0) {
            		this.lastItemId = -1L;
            	}
            	break;
            }
        }
	}
	
	public LeafState getState() {
		return this.state;
	}

	public void setState(LeafState state) {
		this.state = state;
	}
	
	public void setLastItemId(Long id) {
		this.lastItemId = id;
	}
	
	public long getLastItemId() {
		return this.lastItemId;
	}
	
	/**
	 * Updates the Leafnodes lastActionTime to System.currentTimeMillis()
	 */
	public void updateLastActionTime() {
		this.lastActionTime = System.currentTimeMillis();
	}
	
	public Integer getEntriesSize() {
		return this.entries.size();
	}
	
	/**
	 * Check if Leaf has been inactive longer than passed timestamp.
	 * 
	 * This method is used at the moment when cleaning the engine's cache.
	 * If Leafnode has been inactive (in other words return true) the node
	 * is cleaned from cache located in memory of the engine.
	 * 
	 * @param valid Long Timestamp. Same format as System.currentTimeMillis() 
	 * 					 returns.;
	 * @return boolean If Leaf has been inactive or not.
	 */
	public boolean hasBeenInactiveLongerSince(Long valid) {
		return this.lastActionTime > valid ? false : true;
	}
	
	public AccessModel getAccessModel() {
		return this.configuration.access_model;
	}
	
	/**
	 * Adds Entry to item's entries -list.
	 * 
	 * When entry is added, the lastItemId of the leaf is automatically updated.
	 * After adding a item to a leaf, node automatically checks if there are too
	 * many entries in the node and removes the extra entries using method 
	 * removeTooMany(). When adding the item to the leaf, action is launched automatically
	 * to add the node to the database.
	 * 
	 * @param entry Entry The new Entry
	 */
	public synchronized void addItem(Entry entry) {
		this.addInitItem(entry);
		ItemManager.getInstance().addChange(new ItemChange(this.nodeName, entry, ItemChangeAction.insert));
	}
	
	/**
	 * Adds Entry to item's entries -list. This method is used when initializing the Leaf.
	 * 
	 * When entry is added, the lastItemId of the leaf is automatically updated.
	 * After adding a item to a leaf, node automatically checks if there are too
	 * many entries in the node and removes the extra entries using method 
	 * removeTooMany().
	 * 
	 * @param entry Entry The new Entry
	 */
	public synchronized void addInitItem(Entry entry) {
		this.entries.addFirst(entry);
		this.lastItemId = entry.getId();
		LogMe.debug("Added entry to node '" + this.nodeName + "'. Size of entries list: '" + Integer.toString(this.entries.size()) + "'.");
		this.removeTooMany();
	}
	
	/**
	 * Removes all entries from the leaf. Total amount of possible maximum items of node is set 
	 * in the configuration of the node. When item is removed from the node, it automatically
	 * launches a action to remove it from the database too.
	 */
	private void removeTooMany() {
		while(this.entries.size() > this.configuration.max_items) {
			Entry entry = this.entries.pollLast();
			ItemManager.getInstance().addChange(new ItemChange(this.nodeName, entry, ItemChangeAction.delete));
		}
	}
	
	/**
	 * Returns a collection of entries that were published since the itemID passed as a
	 * parameter. This can be used to request all the entries of the node too by passing
	 * a non integer string as a parameter.
	 * @param itemid
	 * @return
	 */
	public Collection<Entry> getEntriesSinceID(String itemid) {
		Collection<Entry> newEntries = new LinkedList<Entry>();
		//TODO add try cache here
		Long id = Long.parseLong(itemid);
		
		// TODO there is a possible nullpointer error here. The entries should be copied
		// instead of used directly.
		
		if(id > this.lastItemId) {
			return newEntries;
		}
		
		if(this.lastItemId.equals(id)) {
			return newEntries;
		}
		
		int found = -1;
		for (int j=0; j < this.entries.size(); j++) {
            if(this.entries.get(j).getId().equals(id)) {
            	found = j;
            	break;
            }
            if(id > this.entries.get(j).getId()) {
            	found = j;
            	break;
            }
        }
		
		if(this.entries.size() == 0) {
			LogMe.debug("This node is empty. Returning an empty list :D");
			return newEntries;
		} else if(found == 0) {
			LogMe.debug("Asked item is the latest.Returning an empty list.");
			return newEntries;
		} else if (found == -1) {
			found = (this.entries.size() -1);
		}
		
		for (int j=found; j > -1; j--) {
            newEntries.add(this.entries.get(j));
        }
		
		return newEntries;
	}
	
	public String getNodeName() {
		return this.nodeName;
	}
	
	public String getTitle() {
		return this.configuration.title;
	}
	
	public String getDescription() {
		return this.configuration.description;
	}
	
	public String getAccessModelAsString() {
		return this.configuration.access_model.toString();
	}
	
	public String getPublishModesAsString() {
		return this.configuration.publish_model.toString();
	}
	
	public Integer getMaxItems() {
		return this.configuration.max_items;
	}
	
	public void setPayloadType(PayloadType payload_type) {
		this.configuration.payload_type = payload_type;
	}
	
	public String getPayloadTypeAsString() {
		return this.configuration.payload_type.toString();
	}
	
	public PayloadType getPayloadType() {
		return this.configuration.payload_type;
	}
	
	public Configuration getConfiguration() {
		return this.configuration;
	}
	
	/**
	 * Returns collection of JIDs that can receive events and notifications from this node.
	 * User is considered to be able to receive events and notifications when member is even
	 * "member", "publisher", "moderator" or "owner" and subscribed as well of course. This
	 * method does not check if the user if online or not.
	 * 
	 * @return
	 */
	public Collection<String> getCollectionOfPossibleReceivers() {
		Collection<String> jids = new LinkedList<String>();
		for (String jid : this.subscribers.keySet()) {
			if(this.subscribers.get(jid).isAllowedToReceiveItems()) {
				jids.add(jid);
			}
		}
		return jids;
	}
	
	public Collection<String> getCollectionOfNodesModerators() {
		Collection<String> jids = new LinkedList<String>();
		for (String jid : this.subscribers.keySet()) {
			if(this.subscribers.get(jid).isModerator()) {
				jids.add(jid);
			}
		}
		return jids;
	}
	
	/**
	 * Adds JID as a subscribed of this node.
	 * 
	 * After adding the JID, the system launches automatically tasks to even update or insert
	 * the new subscription to the database.
	 * 
	 * @param jid
	 * @param subscriber
	 * @throws AlreadySubscribedException
	 * @throws JidOutcastedException
	 */
	private void addSubscriber(String jid, Subscriber subscriber) throws AlreadySubscribedException, JidOutcastedException {
		//Subscriber sub = this.subscribers.putIfAbsent(jid, subscriber);
		
		Subscriber sub = this.subscribers.get(jid);
		if (sub == null) {
			this.subscribers.put(jid, subscriber);
			SubscriptionManager.getInstance().addChange(new SubscriptionChange(jid, 
																			   this.nodeName, 
																			   subscriber, 
																			   SubscriptionChangeAction.insert));
			this.sortSubscribersList();
			return;
		} else if (sub.isOutcast()) {
			throw new JidOutcastedException();
		} else {
			throw new AlreadySubscribedException();
		}
	}
	
	public boolean addPubsubhubbubSubscriber(String nodename, String topic, String verify_token, String lease_seconds, String secret, String callback) {
		
		PubsubhubbubSubscriber ps = new PubsubhubbubSubscriber(callback, secret);
		
		if(this.PubsubhubbubSubscribers.contains(ps)) {
			LogMe.debug("Pubsubhubbub subscription to callback '" + callback + "' and secret '" + secret + "' already exists. Will not add another one.");
			
			return false;
		}
	
		PubsubhubbubSubscriptionManager.getInstance().addChange(new PubsubhubbubSubscriptionChange(nodename, 
																								   topic, 
																								   verify_token, 
																								   lease_seconds, 
																								   secret, 
																								   callback, 
																								   PubsubhubbubSubscriptionChangeAction.insert));
		
		this.putAsPubsubhubbubSubscriber(ps);
		
		return true;
	}

	public boolean removePubsubhubbubSubscriber(String nodename, String topic, String verify_token, String lease_seconds, String secret, String callback) {
		
		PubsubhubbubSubscriber ps = new PubsubhubbubSubscriber(callback, secret);
		
		this.PubsubhubbubSubscribers.remove(ps);
		
		PubsubhubbubSubscriptionManager.getInstance().addChange(new PubsubhubbubSubscriptionChange(nodename, 
																								   topic, 
																								   verify_token, 
																								   lease_seconds, 
																								   secret, 
																								   callback, 
																								   PubsubhubbubSubscriptionChangeAction.delete));
		
		return true;
	}
	
	public void setTitle(String title) {
		// TODO!
		// Title cannot be null!
		this.configuration.title = title;
	}
	
	public void setDescription(String desc) {
		if(desc == null) {
			return;
		}
		this.configuration.description = desc;
	}
	
	public void setAvatarHash(String hash) {
		if(hash == null) {
			return;
		}
		this.configuration.avatar_hash = hash;
	}
	
	public String getAvatarHash() {
		return this.configuration.avatar_hash;
	}
	
	public void setMaxItems(String max_items) {
		
		if(max_items == null) {
			this.configuration.max_items = Configuration.DEFAULT_MAX_ITEMS;
			return;
		}
		
		try {
			this.configuration.max_items = Integer.parseInt(max_items);
		} catch (Exception e) {
			this.configuration.max_items = Configuration.DEFAULT_MAX_ITEMS;
		}
	}
	
	/**
	 * Method allows you to set the accessModel the node. Currently only 2 different 
	 * access models are supported: "whitelist" and "open".
	 * 
	 * @param model
	 * @throws InvalidAccessModelException
	 */
	public void setAccessModel(String model) throws InvalidAccessModelException {
		
		if(model == null) {
			return;
		}
		
		if("whitelist".equals(model)) {
			this.configuration.access_model = AccessModel.whitelist;
		} else if ("open".equals(model)) {
			this.configuration.access_model = AccessModel.open;
		} else {
			throw new InvalidAccessModelException();
		}
		
		LogMe.debug("'" + this.nodeName + "'s access_model is set to '" + this.configuration.access_model.toString() + "'.");
	}
	
	/**
	 * Set the publish model of the node.
	 * @param model
	 * @throws InvalidPublishModelException
	 */
	public void setPublishModel(String model) throws InvalidPublishModelException {
		
		if(model == null) {
			return;
		}
		
		if("publishers".equals(model)) {
			this.configuration.publish_model = PublishModel.publishers;
		} else if ("subscribers".equals(model)) {
			this.configuration.publish_model = PublishModel.subscribers;
		} else {
			throw new InvalidPublishModelException();
		}
		LogMe.debug("'" + this.nodeName + "'s publish_model is set to '" + this.configuration.publish_model.toString() + "'.");
	}
	
	public void setDefaultAffiliation(String affiliation) {
		if("publisher".equals(affiliation)) {
			this.configuration.defaulAffiliation = Affiliation.publisher;
		} else {
			this.configuration.defaulAffiliation = Affiliation.member;
		}
		LogMe.debug("'" + this.nodeName + "'s default affiliation is set to '" + this.configuration.defaulAffiliation.toString() + "'.");
	}
	
	public boolean isWhitelistAccesslist() {
		return this.configuration.access_model.equals(AccessModel.whitelist);
	}
	
	// This is done wrong it seems.
	// Users are always memeber, even they are set in the whitelist already for example as a moderator.
	public void setAsSubscriber(String jid) throws AlreadySubscribedException, JidOutcastedException, JidNotInWhiteListException {
		if(this.isWhitelistAccesslist()) {
			Subscriber sub = this.subscribers.get(jid);
			if(sub == null) {
				throw new JidNotInWhiteListException();
			}
		}
		if (SubscriberEntities.getInstance().getEntity(jid).isTemporarySubscription()) {
			this.addSubscriber(jid, new Subscriber(Affiliation.member, Subscription.subscribed));
        } else {
        	//this.addSubscriber(jid, new Subscriber(Affiliation.publisher, Subscription.subscribed));
        	this.addSubscriber(jid, new Subscriber(this.getDefaultAffiliation(), Subscription.subscribed));
        }
	}
	
	public void unsubscribeUser(String jid) {
		this.subscribers.remove(jid);
		SubscriptionManager.getInstance().addChange(new SubscriptionChange(jid, 
				   									this.nodeName, 
				   									null, 
				   									SubscriptionChangeAction.delete));
	}
	
	public boolean canUnsubscribe(String bareJID) {
		if( this.subscribers.get(bareJID) == null ) {
			return false;
		}
		
		if( this.subscribers.get(bareJID).isOutcast() ) {
			LogMe.debug("User '" + bareJID + "' is outcasted on node '" + this.nodeName + "', cannot unsubscribe.");
			return false;
		}
		return true;
	}
	
	//Rename this to canAffiliationOrSubscription
	public boolean canModifyAffiliationOrSubscription(String whoJID, String whosJID) {
		// if user is not a subscriber => not allowed
		if(this.subscribers.get(whoJID) == null) {
			return false;
		}
		// if he is not owner or moderator => not allowed
		if( !(this.subscribers.get(whoJID).isOwner() || this.subscribers.get(whoJID).isModerator()) ) {
			return false;
		}
		// only owner herself can modify her affiliation
		// whosJID exists                           whosJID is owner                            whoJID differs from whosJID
		if(this.subscribers.get(whosJID) != null && this.subscribers.get(whosJID).isOwner() && !whoJID.equals(whosJID)) {
			return false;
		}
		
		return true;
	}
	
	// TODO
	// Not sure if this is the most best way to handle this. Maybe we should throw 
	// notKnown exceptions or something.
	public boolean canPublishItem(String jid) {
		
		if(jid.equals("butler.buddycloud.com") || jid.equals(Welcommer.fromBareJID)) {
			return true;
		}
		
		if(this.subscribers.get(jid) == null) {
			// If user tries to publish an item and he is not temporary and the channel is "ok for everyone" a.k.a open
			// we subscribe the user to the channel as publisher.
			if(!SubscriberEntities.getInstance().getEntity(jid).isTemporarySubscription() && !this.isWhitelistAccesslist()) {
				this.modifyAffiliation(jid, "publisher", Subscription.subscribed);
				return true;
			} else {
				// user is not subscribed to the channel in some way.
				return false;
			}
		} else if(SubscriberEntities.getInstance().getEntity(jid).isTemporarySubscription()) {
			// temporary subscriptions cannot publish anything.
			return false;
		} else if(this.subscribers.get(jid).isSubscribed() &&
				  (this.subscribers.get(jid).isOwner() || 
		          this.subscribers.get(jid).isModerator() ||
		          this.subscribers.get(jid).isPublisher()) ) {
			// All these can.
			return true;
		}
		// By default no luck.
		return false;
	}
	
	public boolean canRemoveItem(String jid) {
		if(this.subscribers.get(jid) == null) {
			return false;
		}
		if(this.subscribers.get(jid).isOwner() || 
		   this.subscribers.get(jid).isModerator()) {
			return true;
		}
		return false;
	}
	
	public boolean canConfigureNode(String jid) {
		if(this.subscribers.get(jid) == null) {
			return false;
		}
		if(this.subscribers.get(jid).isOwner()) {
			return true;
		}
		return false;
	}
	
	public boolean canDeleteNode(String jid) {
		
		if(jid.equals(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID().toBareJID())) {
			return true;
		}
		
		if(this.subscribers.get(jid) == null) {
			return false;
		}
		if(this.subscribers.get(jid).isOwner()) {
			return true;
		}
		return false;
	}
	
	public void putAsPubsubhubbubSubscriber(PubsubhubbubSubscriber ps) {
		this.PubsubhubbubSubscribers.add(ps);
	}
	
	public LinkedList <PubsubhubbubSubscriber> getPubsubhubbubSubscribers() {
		return this.PubsubhubbubSubscribers;
	}
	
	public Subscriber putAsSubscriber(String jid, String affiliation, Subscription subsrcription) {
		Subscriber subs;
		
		if(affiliation.equals("owner")) {
			subs = new Subscriber(Affiliation.owner, Subscription.subscribed);
			
		} else if (affiliation.equals("moderator")) {
			subs = new Subscriber(Affiliation.moderator, subsrcription);
			
		} else if (affiliation.equals("publisher")) {
			subs = new Subscriber(Affiliation.publisher, subsrcription);
			
		} else if (affiliation.equals("member")) {
			subs = new Subscriber(Affiliation.member, subsrcription);
			
		} else if (affiliation.equals("none")) {
			subs = new Subscriber(Affiliation.none, Subscription.none);
			
		} else {	
			subs = new Subscriber(Affiliation.outcast, Subscription.none);
			
		}
		//LogMe.debug("* ready to put '" + jid + "' to node '" + this.getNodeName() + "' as '" + subs.getAffiliationAsString()+":" + subs.getSubscriptionAsString() + "'");
		this.subscribers.put(jid, subs);
		return subs;
	}
	
	public Affiliation getDefaultAffiliation() {
		return this.configuration.defaulAffiliation;
	}
	
	public String getUsersAffiliationAsString(String bareJID) {
		if(this.subscribers.get(bareJID) == null) {
			return this.getDefaultAffiliation().toString();
		}
		return this.subscribers.get(bareJID).getAffiliationAsString();
	}
	
	public void modifyAffiliation(String jid, String affiliation, Subscription subsrcription) {
		
		boolean subscriptionExists = true;
		Subscriber subs;
		
		if(this.subscribers.get(jid) == null) {
			subscriptionExists = false;
		}
		
		subs = this.putAsSubscriber(jid, affiliation, subsrcription);
		
		if(subscriptionExists && subs.isNone()) {
			this.unsubscribeUser(jid);
		} else if (subscriptionExists) {
			SubscriptionManager.getInstance().addChange(new SubscriptionChange(jid, 
					   														   this.nodeName, 
					   														   subs,
					   														   SubscriptionChangeAction.update));
		} else {
			SubscriptionManager.getInstance().addChange(new SubscriptionChange(jid, 
					   														   this.nodeName, 
					   														   subs,
					   														   SubscriptionChangeAction.insert));
		}
	}
	
	public Subscriber getSubscriber(String jid) {
		return this.subscribers.get(jid);
	}
	
	public int getTotalAmountOfSubscribers(boolean fromOwner) {
		
		int size = 0;
		for (String jid : this.subscribers.keySet()) {
			if(!this.subscribers.get(jid).getAffiliationAsString().equals("outcast") && this.subscribers.get(jid).getSubscriptionAsString().equals("none")) {
				continue;
			} else if (!fromOwner && !this.subscribers.get(jid).getSubscriptionAsString().equals("subscribed") ) {
				continue;
			} else if (SubscriberEntities.getInstance().getEntity(jid) != null && SubscriberEntities.getInstance().getEntity(jid).isTemporarySubscription()) {
				continue;
			}
			size++;
		}
		
		return size;
		
	}
	
	public LinkedHashMap<String, Subscriber> getNodesSubscribers() {
		return this.subscribers;
	}
	
	public LinkedHashMap<String, Subscriber> getNodesSubscribers(String afterJID, int amount, boolean fromOwner) {
		LinkedHashMap<String, Subscriber> partOfsubs = new LinkedHashMap<String, Subscriber>();
		//Link
		Iterator<String> it = this.subscribers.keySet().iterator();
		while(it.hasNext()) {
			if(afterJID == null) {
				break;
			}
			String jid = (String)it.next();
			if(jid.equals(afterJID)) {
				break;
			}
		}
		int i = 0;
		while (it.hasNext()) {
			if(i >= amount) {
				break;
			}
			String jid = (String)it.next();
			
			if(!this.subscribers.get(jid).getAffiliationAsString().equals("outcast") && this.subscribers.get(jid).getSubscriptionAsString().equals("none")) {
				continue;
			} else if (SubscriberEntities.getInstance().getEntity(jid) != null && SubscriberEntities.getInstance().getEntity(jid).isTemporarySubscription()) {
				// We don't want to return temporary subscriptions.
				// If we would, this would break the temporary subscriptions on user's channel when the owner is 
				// loggin in/out.
				continue;
			} else if (!fromOwner && !this.subscribers.get(jid).getSubscriptionAsString().equals("subscribed") ) {
				continue;
			}
			
			partOfsubs.put(jid, this.subscribers.get(jid));
			i++;
		}
		return partOfsubs;
	}
	
	public boolean userIsSubscribedToNode(String bareJID) {
		if(this.subscribers.get(bareJID) == null) {
			return false;
		}
		
		if(!this.subscribers.get(bareJID).isSubscribed()) {
			return false;
		}
		
		return true;
	}
	
	public boolean isAffiliationRequested(String bareJID) {
		if(this.subscribers.get(bareJID) == null) {
			return false;
		}
		
		return this.subscribers.get(bareJID).isAffiliationRequested();
	}
	
	public void setAffiliationRequested(String bareJID) {
		if(this.subscribers.get(bareJID) == null) {
			return;
		}
		
		this.subscribers.get(bareJID).setAffiliationRequested();
	}
	
	public void resetAffiliationRequest(String bareJID) {
		if(this.subscribers.get(bareJID) == null) {
			return;
		}
		
		this.subscribers.get(bareJID).resetAffiliationRequest();
	}
	
	public String getOwner() {
		LogMe.debug("Starting to get the owner of a node. (" +  Integer.toString(this.subscribers.size()) + ").");
		for (String jid : this.subscribers.keySet()) {
			//LogMe.debug(" - is '" + jid + "' the owner?");
			if(this.subscribers.get(jid).isOwner()) {
				LogMe.debug("Owner of the node found: '" + jid + "'.");
				return jid;
			}
		}
		LogMe.debug("Owner of the node not found!");
		return "";
	}
	
	public void cleanEntries() {
		this.entries.clear();
		LogMe.debug("Cleaned entries from node '" + this.nodeName + "'.");
	}
	
	/**
	 * configuration of the node can be set by sending the Map<String, String> as parameter.
	 * Following keys are supported: pubsub#title and pubsub#description.
	 * 
	 * @param nodeConf
	 */
	public void modifyConfiguration(Map<String, String> nodeConf) {
		
		if(nodeConf == null || nodeConf.isEmpty()) {
			return;
		}
		
		if( nodeConf.get("pubsub#title") != null ) {
			this.setTitle(nodeConf.get("pubsub#title"));
		}
		
		if( nodeConf.get("pubsub#description") != null ) {
			this.setDescription(nodeConf.get("pubsub#description"));
		}
		
		if( nodeConf.get("pubsub#default_affiliation") != null ) {
			this.setDefaultAffiliation(nodeConf.get("pubsub#default_affiliation"));
		}
		
		if( nodeConf.get("x-buddycloud#avatar-hash") != null ) {
			this.setAvatarHash(nodeConf.get("x-buddycloud#avatar-hash"));
		}
		
		if( nodeConf.get("pubsub#access_model") != null ) { 
			try {
				this.setAccessModel(nodeConf.get("pubsub#access_model"));
			} catch (Exception e) {
				LogMe.info("Cannot set access model to '" + nodeConf.get("pubsub#access_model") + "'.");
			}
		}
		
		LeafnodeManager.getInstance().addChange(new LeafnodeChange(this, LeafnodeChangeAction.update));
	}
	
	public Element getAsAtomEntrySource() {
//		<source>
//        	<title>Benvolio&apos;s Microblog</title>
//        	<link href='http://montague.lit/benvolio'/>
//        	<id>tag:montague.lit,2008:home</id>
//        	<updated>2008-05-08T18:31:21Z</updated>
//        	<author>
//        		<name>Benvolio Montague</name>
//        	</author>
//        </source>
		Element source = new DOMElement("source", new Namespace("", "http://www.w3.org/2005/Atom"));
		source.addElement("title").setText(this.getTitle());
		source.addElement("summary").setText(this.getDescription());
		source.addElement("id").setText(this.getNodeName());

		Element link = source.addElement("link");
		link.addAttribute("href", this.getChannelsAtomFeedURL());
		link.addAttribute("type", "application/atom+xml");
		link.addAttribute("rel", "self");
		
		Element htmlLink = source.addElement("link");
		htmlLink.addAttribute("href", this.getChannelsHTMLURL());
		htmlLink.addAttribute("type", "text/html");
		htmlLink.addAttribute("rel", "alternate");
		
		Element xmppLink = source.addElement("link");
		xmppLink.addAttribute("href", "xmpp:pubsub-bridge@broadcaster.buddycloud.com?;node=" + this.nodeName);
		xmppLink.addAttribute("rel", "alternate");
		
		source.addElement("author").addElement("name").setText(this.configuration.owner);
		
		return source;
	}
	
	public String getChannelsAtomFeedURL() {
		// This could be cached.
		String channel = this.nodeName;
		if( this.nodeName.endsWith("/channel") ) {
			channel = "/channel/" + this.nodeName.substring("/user/".length(), this.nodeName.indexOf("/", "/user/".length()));
		} 
		return "http://api.buddycloud.com" + channel + "/atom.xml";
	}
	
	public String getChannelsHTMLURL() {
		// This could be cached.
		String channel = this.nodeName;
		if( this.nodeName.endsWith("/channel") ) {
			String user = this.nodeName.substring("/user/".length(), this.nodeName.indexOf("/", "/user/".length()));
			String jidParts[] = user.split("@");
			channel = "/user/" + jidParts[1] + "/" + jidParts[0];
		}
		return "http://beta.buddycloud.com" + channel;
	}

	public String getChannelAvatar() {
		// This could be cached.
                String channel = "/channel/54x54/default.png";
                if( this.nodeName.endsWith("/channel") ) {
                        String user = this.nodeName.substring("/user/".length(), this.nodeName.indexOf("/", "/user/".length()));
                        String jidParts[] = user.split("@");
                        channel = "/channel/54x54/" + jidParts[1] + "/" + jidParts[0] + ".png";
                }
                return "http://media.buddycloud.com" + channel;
	}	
}
