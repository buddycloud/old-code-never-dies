package com.buddycloud.pubsub.Item;

import java.io.StringReader;
import java.util.Date;
import java.util.TimeZone;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.xmpp.packet.IQ;

import com.buddycloud.pubsub.BuddycloudPubsubComponent;
import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.managers.InQueueManager;
import com.buddycloud.pubsub.subscriber.Subscriber;
import com.buddycloud.pubsub.utils.FastDateFormat;

public class Entry {

	private Element payload;
	private Long id;
	
	public Entry(Element payload) {
		this.payload = payload;
		this.id = System.currentTimeMillis();
		this.payload.addAttribute("id", Long.toString(this.id));
	}
	
	public Entry(Element payload, Long id) {
		this.payload = payload;
		this.id = id;
		this.payload.addAttribute("id", Long.toString(this.id));
	}
	
	public Entry(String payload, Long id) {
		
		SAXReader xmlReader = new SAXReader();
		Element item = null;
		try {
			item = xmlReader.read(new StringReader(payload)).getRootElement();
		} catch (DocumentException e) {
			LogMe.warning("O'ou!!! Cannot create entry from a string.");
		}
		this.payload = item;
		this.id = id;
	}
	
	public Long getId() {
		return this.id;
	}
	
	public String payloadAsString() {
		return this.payload.asXML();
	}
	
	public Element getPayload() {
		return this.payload;
	}
	
	// TODO
	// Refactor completely this.
	// http://www.w3.org/2005/Atom
	public static Element verifyAndGenerateEntryPayload(Element originalPayload, String authorsJid, Subscriber sb, String nodename, String id) {
		
		if(!originalPayload.getName().equals("item")) {
			return null;
		}
		
		Element entry = originalPayload.element("entry");
		if(entry == null) {
			return null;
		}
		//LogMe.debug("Entry is in namespace '" + entry.getNamespaceURI() +"'.");
		
		if(!PayloadType.atom_entry.getNamespace().equals(entry.getNamespaceURI())) {
			return null;
		}
		
		Element content = entry.element("content");
		if(content == null) {
			return null;
		}
		String contentType = content.attributeValue("type");
		if(contentType == null || contentType.equals("")) {
			contentType = "html";
		}
		
		Element geoloc = entry.element("geoloc");
		Element headers = entry.element("headers");
		Element inReplyTo = entry.element("in-reply-to");

		if( null != entry.element("web-in-reply-to") ) {
			inReplyTo = new DOMElement("in-reply-to", new Namespace("thr", "http://purl.org/syndication/thread/1.0"));
			inReplyTo.addAttribute("ref", entry.element("web-in-reply-to").attributeValue("ref"));
		}
		
		Element newItem = new DOMElement("item", new Namespace("", "http://jabber.org/protocol/pubsub#event"));
		
		Element newEntry = new DOMElement("entry", new Namespace("", "http://www.w3.org/2005/Atom"));
		newEntry.add(new Namespace("thr", "http://purl.org/syndication/thread/1.0"));
		
		Element newAuthor = newEntry.addElement("author");
		
		Element oldAuthor = entry.element("author");
		String name = null;
		String jid = null;
		
		if(oldAuthor != null) {
			if(oldAuthor.element("name") != null) {
				name = oldAuthor.element("name").getTextTrim();
			}
			if(oldAuthor.element("jid") != null) {
				jid = oldAuthor.element("jid").getTextTrim();
			}
		}
		
		if(name != null && !name.equals("") && jid != null && jid.equals(authorsJid)) {
			newAuthor.addElement("name").setText(name);
		}
		
		Element newJid = new DOMElement("jid", new Namespace("", "http://buddycloud.com/atom-elements-0"));
		newJid.setText(authorsJid);
		newAuthor.add(newJid);
		
		if(sb != null) {
			Element newAff = new DOMElement("affiliation", new Namespace("", "http://buddycloud.com/atom-elements-0"));
			newAff.setText(sb.getAffiliationAsString().toString());
			newAuthor.add(newAff);
		}
		
		Element newContent = newEntry.addElement("content");
		newContent.addAttribute("type", contentType);
		
		if( content.getTextTrim().indexOf('#') > -1) {
			newContent.addText( content.getText().replaceAll( "@", " at ") );
		} else {
			newContent.addText( content.getText() );
		}
		
		//TODO
		// This is not the best practise!!!
		String time = FastDateFormat.getInstance(Conf.TIME_TEMPLATE, TimeZone.getTimeZone(Conf.TIME_ZONE)).format(new Date());
		newEntry.addElement("published").addText(time);
		newEntry.addElement("updated").addText(time);
		newEntry.addElement("id").addText(nodename + ":" + id);
		if(geoloc != null) {
			newEntry.add(geoloc.createCopy());
		}
		
		if(headers != null) {
			newEntry.add(headers.createCopy());
		}
		
		if(inReplyTo != null) {
			newEntry.add(inReplyTo.createCopy());
			
//			if(inReplyTo.getText() != null && !inReplyTo.getText().equals("")) {
//				// add thr
//				Element newInReplyTo = new DOMElement("in-reply-to", new Namespace("thr", "http://purl.org/syndication/thread/1.0"));
//				newInReplyTo.addAttribute("ref", inReplyTo.getText());
//				newEntry.add(newInReplyTo);
//			} else {
//				//add old
//				Element oldInReplyTo = new DOMElement("in-reply-to", new Namespace("", "http://buddycloud.com/atom-elements-0"));
//				oldInReplyTo.setText(inReplyTo.attributeValue("ref"));
//				newEntry.add(oldInReplyTo);
//			}
		}
		
		newItem.add(newEntry);
		
		return newItem;
	}
	
	// TODO
	// Refactor completely this.
	// http://www.w3.org/2005/Atom
	public static Element verifyAndGenerateGeolocPayload(Element originalPayload) {
		LogMe.debug("Starting verify geoloc element: '" + originalPayload.asXML() + "'.");
		if(!originalPayload.getName().equals("item")) {
			LogMe.debug("Item not found in the original element.");
			return null;
		}
		
		Element geoloc = originalPayload.element("geoloc");
		if(geoloc == null) {
			LogMe.debug("Geoloc element not found.");
			return null;
		}
		
		if(!PayloadType.geoloc.getNamespace().equals(geoloc.getNamespaceURI())) {
			LogMe.debug("Geoloc's namespace is mismatching!");
			return null;
		}
		LogMe.debug("Geoloc element is OK.");
		return originalPayload;
	}
	
	// TODO
	// Refactor completely this.
	// http://www.w3.org/2005/Atom
	public static Element verifyAndGenerateMoodPayload(Element originalPayload) {
		LogMe.debug("Starting verify mood element: '" + originalPayload.asXML() + "'.");
		if(!originalPayload.getName().equals("item")) {
			LogMe.debug("Item not found in the original element.");
			return null;
		}
		
		Element mood = originalPayload.element("mood");
		if(mood == null) {
			LogMe.debug("Mood element not found.");
			return null;
		}
		
		if(!PayloadType.user_mood.getNamespace().equals(mood.getNamespaceURI())) {
			LogMe.debug("Mood's namespace is mismatching!");
			return null;
		}
		
		LogMe.debug("Mood element is OK.");
		return originalPayload;
	}
	
	public static void createAndSendAtomEntryPubsubFromMood(Element originalPayload, String bareJID, String nodename) {
		LogMe.debug("Starting to create atom entry from mood.");
		if(!originalPayload.getName().equals("item")) {
			LogMe.debug("Item not found in the original element. No cluck creating atom entry.");
			return;
		}
		
		Element mood = originalPayload.element("mood");
		if(mood == null) {
			LogMe.debug("Mood element not found. No cluck creating atom entry.");
			return;
		}
		
		if(!PayloadType.user_mood.getNamespace().equals(mood.getNamespaceURI())) {
			LogMe.debug("Mood's namespace is mismatching! No cluck creating atom entry.");
			return;
		}
		
		if(mood.element("text") == null) {
			LogMe.debug("Mood element does not contain text element. No luck creating atom entry.");
			return;
		}
		
		Element newEntry = new DOMElement("entry", new Namespace("", "http://www.w3.org/2005/Atom"));
		Element newAuthor = newEntry.addElement("author");
		
		newAuthor.addElement("name").setText(bareJID);
		
		Element newJid = new DOMElement("jid", new Namespace("", "http://buddycloud.com/atom-elements-0"));
		newJid.setText(bareJID);
		newAuthor.add(newJid);
		
//		if(sb != null) {
//			Element newAff = new DOMElement("affiliation", new Namespace("", "http://buddycloud.com/atom-elements-0"));
//			newAff.setText(sb.getAffiliationAsString().toString());
//			newAuthor.add(newAff);
//			//newAuthor.addElement("affiliation").setText(sb.getAffiliationAsString().toString());
//		}
		
		Element newContent = newEntry.addElement("content");
		newContent.addAttribute("type", "text");
		newContent.addText(mood.element("text").getTextTrim());
		
		Element headers = new DOMElement("headers", new Namespace("", "http://jabber.org/protocol/shim"));
		Element header = headers.addElement("header");
		header.addAttribute("name", "Source");
		header.setText(bareJID);
		
		// <pubsub xmlns='http://jabber.org/protocol/pubsub'>
		Element pubsub = new DOMElement("pubsub", new Namespace("", "http://jabber.org/protocol/pubsub"));
		Element publish = pubsub.addElement("publish");
		publish.addAttribute("node", nodename);
		Element item = publish.addElement("item");
		
		publish.add(headers);
		item.add(newEntry);
		
		IQ iq = new IQ();
		iq.setType(IQ.Type.set);
		iq.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
		iq.setTo(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
		iq.setChildElement(pubsub);
		
		InQueueManager.getInstance().put(iq);
		
		LogMe.debug("New pubsub created and send: '" + iq.toXML() + "'.");
		
	}
	
	public static Element getAsPubSubhubbubEntry(String title, String link, String id, Element extraPayload ) {
		
//		<?xml version="1.0"?>
//		<atom:feed>
//		  <title>Aggregated feed</title>
//		  <updated>2008-08-11T02:17:44Z</updated>
//		  <id>http://myhub.example.com/aggregated?1232427842-39823</id>
//
//		  <entry>
//		    <source>
//		      <id>http://www.example.com/foo</id>
//		      <link rel="self" href="http://publisher.example.com/foo.xml" />
//		      <author>
//		        <name>Mr. Bar</name>
//		      </author>
//		    </source>
//		    <title>Testing Foo</title>
//		    <link href="http://publisher.example.com/foo24.xml" />
//		    <id>http://publisher.example.com/foo24.xml</id>
//		    <updated>2008-08-11T02:15:01Z</updated>
//		    <content>
//		      This is some content from the user named foo.
//		    </content>
//		  </entry>
//		</atom:feed>
		Element deef = new DOMElement("feed", new Namespace("", "http://www.w3.org/2005/Atom"));
		
		
		Element newEntry = new DOMElement("entry", new Namespace("", "http://www.w3.org/2005/Atom"));
		newEntry.addElement("title").setText(title);
		newEntry.addElement("id").setText(id);
		String time = FastDateFormat.getInstance(Conf.TIME_TEMPLATE, TimeZone.getTimeZone(Conf.TIME_ZONE)).format(new Date());
		newEntry.addElement("updated").addText(time);
		newEntry.add(extraPayload);
		return newEntry;
		//TODO add link here when we know it.
	}
	
	public static Element getAsAtomEntry(String content) {
//		<iq to="pubsub-bridge@broadcaster.buddycloud.com" type="set" id="18:96:1">
//	    <pubsub xmlns="http://jabber.org/protocol/pubsub">
//	    <publish node="/channel/' . $node . '">
//	    <item>
//	    <entry xmlns="http://www.w3.org/2005/Atom" xmlns:thr="http://purl.org/syndication/thread/1.0">
//	    <updated>' . date("Y-m-d\TH:i:s\Z") . '</updated>
//	    <author>
//	      <jid xmlns="http://buddycloud.com/atom-elements-0">rss-author@xmpp.lobstermonster.org</jid>
//	    </author>
//	    <content type="text">' . Jabber::jspecialchars($rssItem['title']) . "\n\n" . Jabber::jspecialchars($rssItem['desc']) . "\n\n" . 'Read more: ' . Jabber::jspecialchars($rssItem['link']) . '</content>
//	    <geoloc xmlns="http://jabber.org/protocol/geoloc">
//	      <text>Live from Interwebs</text>
//	      <country>France</country>
//	      </geoloc>
//	    </entry>
//	    </item>
//	    </publish>
//	    </pubsub>
//	</iq>
		
		Element newEntry = new DOMElement("entry", new Namespace("", "http://www.w3.org/2005/Atom"));
		String time = FastDateFormat.getInstance(Conf.TIME_TEMPLATE, TimeZone.getTimeZone(Conf.TIME_ZONE)).format(new Date());
		newEntry.addElement("updated").addText(time);
		
		Element newContent = newEntry.addElement("content");
		newContent.addAttribute("type", "text");
		newContent.addText(content);
		
		Element geoloc = new DOMElement("geoloc", new Namespace("", "http://jabber.org/protocol/geoloc"));
		geoloc.addElement("text").setText("buddycloud");
		
		return newEntry;
	}
}
