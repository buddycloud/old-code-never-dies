package com.buddycloud.pubsub.node;

import com.buddycloud.pubsub.Item.PayloadType;
import com.buddycloud.pubsub.node.configs.AccessModel;
import com.buddycloud.pubsub.node.configs.NotifyConfig;
import com.buddycloud.pubsub.node.configs.NotifyDelete;
import com.buddycloud.pubsub.node.configs.PersistentItems;
import com.buddycloud.pubsub.node.configs.PresenceBasedDelivery;
import com.buddycloud.pubsub.node.configs.PublishModel;
import com.buddycloud.pubsub.subscriber.Affiliation;

public class Configuration {
    
	public static final int DEFAULT_MAX_ITEMS = 50;
	
	public PresenceBasedDelivery presence_based_delivery = PresenceBasedDelivery.yes;
	public NotifyDelete notify_delete 					 = NotifyDelete.yes;
	public PersistentItems persist_items 				 = PersistentItems.yes;
	public int max_items 								 = DEFAULT_MAX_ITEMS;
	public PublishModel publish_model 					 = PublishModel.publishers;
	public NotifyConfig notify_config					 = NotifyConfig.yes;
	public AccessModel access_model 					 = AccessModel.whitelist;

	public PayloadType payload_type						 = PayloadType.atom_entry;
	
	public String title									 = "";
	public String description							 = "";
	
	public String created								 = "";
	
	public Affiliation defaulAffiliation 				 = Affiliation.member;
	
	public String rank									 = ""; 
	public String popularity							 = "";
	public String latitude								 = "";
	public String longitude								 = "";
	public String location								 = "";
	public String followers								 = "";
	
	public String owner									 = "";
	
	public String is_hidden                              = "";
	
	public String avatar_hash							 = "";
	
	public Configuration() {
		
	}
}
