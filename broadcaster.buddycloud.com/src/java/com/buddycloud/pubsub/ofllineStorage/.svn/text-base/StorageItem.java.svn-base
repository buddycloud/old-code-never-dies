package com.buddycloud.pubsub.ofllineStorage;

import java.io.StringReader;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.buddycloud.pubsub.log.LogMe;

public class StorageItem {
	
	private int id = -1;
	private String jid = "";
	private Element payload = null;
	private String datecreated = "";
	
	public StorageItem(String jid, Element payload) {
		this.setJid(jid);
		this.setPayload(payload);
	}

	public StorageItem(int id, String jid, String payload) {
		this.setJid(jid);
		this.setId(id);
		SAXReader xmlReader = new SAXReader();
		try {
			this.payload = xmlReader.read(new StringReader(payload)).getRootElement();
		} catch (DocumentException e) {
			LogMe.warning("O'ou!!! Cannot create entry from a string.");
		}
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public Element getPayload() {
		return payload;
	}

	public void setPayload(Element payload) {
		this.payload = payload;
	}
	
	public String getPayloadAsString() {
		if (this.payload == null) {
			return "";
		}
		return this.payload.asXML();
	}
}
