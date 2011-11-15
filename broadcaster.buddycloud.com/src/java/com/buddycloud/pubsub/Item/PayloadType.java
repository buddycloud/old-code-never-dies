package com.buddycloud.pubsub.Item;

public enum PayloadType {

	atom_entry("http://www.w3.org/2005/Atom"),
	
	user_mood("http://jabber.org/protocol/mood"),
	
	geoloc("http://jabber.org/protocol/geoloc");
	
	private final String namespace;
	
	PayloadType(String namespace) {
		this.namespace = namespace;
	}
	
	public String getNamespace() {
		return this.namespace;
	}
	
	public static PayloadType getItemTypeFromString(String namespace) {
		if(PayloadType.geoloc.getNamespace().equals(namespace)) {
			
			return PayloadType.geoloc;
		
		} else if (PayloadType.user_mood.getNamespace().equals(namespace)) {
			
			return PayloadType.user_mood;
		
		}
		return PayloadType.atom_entry;
	}
	
	public static PayloadType parseFromString( String payload_type ) {
		if( "geoloc".equals(payload_type) ) {
			
			return PayloadType.geoloc;
		
		} else if ( "user_mood".equals(payload_type) ) {
			
			return PayloadType.user_mood;
		
		}
		return PayloadType.atom_entry;
	}
}
