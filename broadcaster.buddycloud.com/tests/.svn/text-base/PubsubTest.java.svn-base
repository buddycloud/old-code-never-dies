import junit.framework.TestCase;

import org.xmpp.packet.JID;

import com.buddycloud.pubsub.packetHandlers.IQ.Namespace.PubSub;


public class PubsubTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testJump() {
		System.out.println("Here we go! Test one!");
	}
	
	public void testNodenameParsing() {
		
		String nodename = "/user/tuomas@xmpp.lobstermonster.org/geo/current";
		String parsed = nodename.substring(nodename.indexOf("/", "/user/".length()));
		parsed = nodename.substring("/user/".length(), nodename.indexOf("/", "/user/".length()));
		System.out.println("'" + parsed + "'");
	}
	
	public void testJID() {
		JID jid = new JID("tuomas@ggg.com/s60/null");
		System.out.println(jid.getNode() + ":" + jid.getDomain() + ":" + jid.getResource());
	}
	
	public void testNodeme() {
	
//		String EXAMPLE_TEST = "ThisismysmallexamplestringwhichImgoingtouseforpatternmatching";
//		
//		System.out.println(EXAMPLE_TEST.matches("\\w*"));
//		String[] splitString = (EXAMPLE_TEST.split("\\s+"));
//		System.out.println(splitString.length);// Should be 14
//		for (String string : splitString) {
//			System.out.println(string);
//		}
//		// Replace all whitespace with tabs
//		System.out.println(EXAMPLE_TEST.replaceAll("\\s+", "\t"));

		
		String user = "/user/tuomas@xmpp.lobstermonster.org/geo/current";
		String channel = "/channel/test";
		
		String channelNOK = "/channel/test/";
		String userNOK = "/user/tuomas@xmpp.lobstermonster.org";
		
		String channelOK = "/channel/test-channel-for-tuomas";
		
		assertTrue(PubSub.checkNodename(user));
		assertFalse(PubSub.checkNodename(channelNOK));
		assertTrue(PubSub.checkNodename(channel));
		assertTrue(PubSub.checkNodename(channelOK));
		
		assertFalse(PubSub.checkNodename(userNOK));
		
	}
}
