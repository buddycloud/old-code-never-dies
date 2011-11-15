package com.buddycloud.pubsub;

import org.apache.log4j.PropertyConfigurator;
import org.jivesoftware.whack.ExternalComponentManager;
import org.xmpp.component.ComponentException;

import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.db.DBNode;
import com.buddycloud.pubsub.db.DBSubscription;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.managers.IncidentManager;
import com.buddycloud.pubsub.tasks.CacheCleaner;

public class BuddycloudPubsubComponent {
	
	// TODO
	// make the component name to be a default, but also to be possible to be set
	// in the engine's configuration file.
	private static String COMPONENT_NAME = "broadcaster.buddycloud.com";
	public static PubSubEngine PUBSUB_ENGINE = new PubSubEngine();
	
	public static void main(String[] args) {
	
		initialize();
		
		final ExternalComponentManager manager = new ExternalComponentManager(Conf.getInstance().getConfString(Conf.XMPP_HOST_KEY), 
				  Conf.getInstance().getConfInteger(Conf.XMPP_PORT_KEY));

		manager.setSecretKey(COMPONENT_NAME, Conf.getInstance().getConfString(Conf.XMPP_SHAREDKEY_KEY));
		
		try {
			// Register that this component will be serving the given subdomain of the server
			manager.addComponent(COMPONENT_NAME, PUBSUB_ENGINE);
		
			// TODO, make this better. We need to support signals.
			// Quick trick to ensure that this application will be running for ever. To stop the
			// application you will need to kill the process
			while (true) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (ComponentException e) {
			e.printStackTrace();
			manager.getLog().error(e);
		}
	
	}
	
	public static void initialize() {
		
		// TODO
		// This is still hardcoded. Make it as default and possible to be set
		// from command line when starting the program.
		PropertyConfigurator.configure("/opt/buddycloud-broadcaster/log4j.properties");
		
		LogMe.debug("XMPP Settings: ");
		LogMe.debug("* host   '" + Conf.getInstance().getConfString(Conf.XMPP_HOST_KEY) + "'");
		LogMe.debug("* port   '" + Integer.toString(Conf.getInstance().getConfInteger(Conf.XMPP_PORT_KEY)) + "'");
		
		
		IncidentManager.initialize();
		
		// TODO
		// This one must be called in this order.
		// It still sucks.
		DBNode.getInstance().initKnownNodes();
		DBSubscription.getInstance().initSubscriptions();
		
		Thread t = new Thread(new CacheCleaner());
        t.start();
	}
}
