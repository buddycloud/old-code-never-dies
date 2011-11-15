import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import com.buddycloud.pubsub.node.Configuration;
import com.buddycloud.pubsub.node.Leaf;
import com.buddycloud.pubsub.node.LeafState;
import com.buddycloud.pubsub.subscriber.Subscriber;
import com.buddycloud.pubsub.subscriber.Subscription;


public class LeafTest extends TestCase {

	public void testNodeCreation() {
		String nodename = "test_node";
		Leaf leaf = new Leaf(nodename);
		assertEquals((String)nodename, (String)leaf.getNodeName());
		assertEquals((int)Configuration.DEFAULT_MAX_ITEMS, (int)leaf.getMaxItems());
		assertEquals(true, leaf.getState().equals(LeafState.initialized));
		assertEquals((int)0, (int)leaf.getEntriesSize());
		assertEquals((long)-1L, (long)leaf.getLastItemId());
	}
	
	public void testHasBeenInactiveLongerThan() {
		//Long lastAction = 200L;
		Long stillActive = (System.currentTimeMillis() - 200000L);
		Long inactive = (System.currentTimeMillis() + 2000L);
		
		String nodename = "test_node";
		Leaf leaf = new Leaf(nodename);
		assertEquals(true, leaf.hasBeenInactiveLongerSince(inactive));
		assertEquals(false, leaf.hasBeenInactiveLongerSince(stillActive));
		
	}
	
	public void testThis() {
		Collection<String> nodes = new LinkedList<String>();
		nodes.add("/user/andygee@buddycloud.com/channel");
		Collection<String> newNodes = new LinkedList<String>();
		
		for (String nodename : nodes) {
			if(nodename.startsWith("/user/") && nodename.endsWith("/channel")) {
				String user = nodename.substring("/user/".length(), nodename.indexOf("/", "/user/".length()));
				newNodes.add("/user/" + user + "/geo/current");
				newNodes.add("/user/" + user + "/geo/future");
				newNodes.add("/user/" + user + "/geo/previous");
				newNodes.add("/user/" + user + "/mood");
				//System.out.println("added lot's of nodes.");
			}
		}
		newNodes.addAll(nodes);
	}
	
	public void testGetNodesSubscribersPart() {
		String nodename = "test_node";
		Leaf leaf = new Leaf(nodename);
		
		Integer amount = 100;
		int i = 1;
		while(i <= amount) {
			leaf.putAsSubscriber(Integer.toString(i) + "@test.com", "member", Subscription.subscribed);
			i++;
		}
		i = 1;
		while(i <= amount) {
			leaf.putAsSubscriber(Integer.toString(i) + "outcast@test.com", "outcast", Subscription.none);
			i++;
		}
		ConcurrentHashMap<String, Boolean> jids = new ConcurrentHashMap<String, Boolean>();
		String lastJID = null;
		int loops = 0;
		while(true) {
			System.out.println("Starting to search next jid with jid '" + lastJID + "'");
			LinkedHashMap<String, Subscriber> list = leaf.getNodesSubscribers(lastJID, 2, true);
			
			if(list.isEmpty()) {
				break;
			}
			
			for (String  jid : list.keySet()) {
				System.out.println("Got JID : '" + jid + "'.");
				lastJID = jid;
				if(jids.containsKey(jid)) {
					System.out.println("JID already added: '" + jid + "'.");
					System.exit(0);
				}
				jids.put(jid, true);
			}
			System.out.println("--");
			loops++;
		}
		System.out.println("We have '" + jids.size() + "' jids in '" + loops + "' loops.");
//		ConcurrentHashMap<String, Subscriber> list = leaf.getNodesSubscribers(null, 2);
//		String lastJID = null;
//		for (String  jid : list.keySet()) {
//			System.out.println("Got JID : '" + jid + "'.");
//			lastJID = jid;
//		}
//		System.out.println("--");
//		System.out.println("Starting to search next jid with jid '" + lastJID + "'");
//		list = leaf.getNodesSubscribers(lastJID, 2);
//		for (String  jid : list.keySet()) {
//			System.out.println("Got JID : '" + jid + "'.");
//			lastJID = jid;
//		}
//		System.out.println("--");
//		list = leaf.getNodesSubscribers(lastJID, 2);
//		for (String  jid : list.keySet()) {
//			System.out.println("Got JID : '" + jid + "'.");
//			lastJID = jid;
//		}
//		System.out.println("--");
	}
	
	public void testGetNodesSubscribersSize() {
		String nodename = "test_node";
		Leaf leaf = new Leaf(nodename);
		
		Integer amount = 100;
		int i = 1;
		while(i <= amount) {
			leaf.putAsSubscriber(Integer.toString(i) + "@test.com", "member", Subscription.subscribed);
			i++;
		}
		i = 1;
		while(i <= amount) {
			leaf.putAsSubscriber(Integer.toString(i) + "outcast@test.com", "outcast", Subscription.none);
			i++;
		}
		ConcurrentHashMap<String, Boolean> jids = new ConcurrentHashMap<String, Boolean>();
		//String lastJID = null;
		
		System.out.println("Size is '" + leaf.getTotalAmountOfSubscribers(true) + "'.");
		System.out.println("Size is '" + leaf.getTotalAmountOfSubscribers(false) + "'.");
		
	}
	
}
