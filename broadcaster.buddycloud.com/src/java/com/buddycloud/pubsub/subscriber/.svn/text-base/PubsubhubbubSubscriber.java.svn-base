package com.buddycloud.pubsub.subscriber;

public class PubsubhubbubSubscriber {

	private String secret = "";
	private String callback = "";
	
	private volatile int hashCode;
	
	public PubsubhubbubSubscriber(String callback, String secret) {
		this.secret = secret;
		this.callback = callback;
	}

	public String getSecret() {
		return secret;
	}

	public String getCallback() {
		return callback;
	}
	
	@Override public boolean equals(Object aThat) {
		if ( this == aThat ) return true;
		
		if ( !(aThat instanceof PubsubhubbubSubscriber) ) return false;
		
		PubsubhubbubSubscriber ps = (PubsubhubbubSubscriber) aThat;
		
		if(this.secret.equals(ps.getSecret()) && this.callback.equals(ps.getCallback())) {
			return true;
		}
		return false;
	}
	
	@Override public int hashCode() {
		int result = hashCode;
		if (result == 0) {
    		result = 17;
    		result = 31 * result + this.secret.hashCode();
    		result = 31 * result + this.callback.hashCode();
    		hashCode = result;
		}
		return result;
}
	
}
