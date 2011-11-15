package com.buddycloud.pubsub.subscriber;

public class Resource {

	public String resource = "";
	public ResourceType type;
	
	private volatile int hashCode;

	public Resource(String resource, ResourceType type) {
		this.resource = resource;
		this.type = type;
	}

	@Override public boolean equals(Object o) {
    		if (o == this)
        		return true;
    		if (!(o instanceof Resource))
        		return false;
    		Resource pn = (Resource)o;
    
		return pn.resource.equals(resource) && this.type == pn.type;
	}

	@Override public int hashCode() {
    		int result = hashCode;
    		if (result == 0) {
        		result = 17;
        		result = 31 * result + this.resource.hashCode();
        		result = 31 * result + this.type.hashCode();
        		hashCode = result;
    		}
    		return result;
	}
}
