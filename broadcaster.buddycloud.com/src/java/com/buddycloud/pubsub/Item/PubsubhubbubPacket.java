package com.buddycloud.pubsub.Item;

import com.buddycloud.pubsub.subscriber.PubsubhubbubSubscriber;

public class PubsubhubbubPacket {

	public PubsubhubbubSubscriber ps;
	public String feedXML;
	
	public PubsubhubbubPacket(PubsubhubbubSubscriber ps, String feedAsXML) {
		this.ps = ps;
		this.feedXML = feedAsXML;
	}
}
