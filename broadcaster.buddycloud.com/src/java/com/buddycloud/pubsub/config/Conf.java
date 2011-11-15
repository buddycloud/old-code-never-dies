package com.buddycloud.pubsub.config;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.utils.Helpers;

public class Conf {
	private static Conf instance;
	
	public static String TIME_TEMPLATE= "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static String TIME_ZONE = "UTC";
	
	public static String XMPP_HOST_KEY = "xmpp.host";
	public static String XMPP_PORT_KEY = "xmpp.port";
	public static String XMPP_SHAREDKEY_KEY = "xmpp.sharedkey";
	
	public static String DB_ROSTER_USER_KEY = "dbroster.username";
	public static String DB_ROSTER_PW_KEY = "dbroster.password";
	public static String DB_ROSTER_DRIVER_KEY = "dbroster.driver";
	public static String DB_ROSTER_HOST_KEY = "dbroster.host";
	public static String DB_ROSTER_NAME_KEY = "dbroster.dbname";
	
	public static String DB_METADATA_USER_KEY = "dbmetadata.username";
	public static String DB_METADATA_PW_KEY = "dbmetadata.password";
	public static String DB_METADATA_DRIVER_KEY = "dbmetadata.driver";
	public static String DB_METADATA_HOST_KEY = "dbmetadata.host";
	public static String DB_METADATA_NAME_KEY = "dbmetadata.dbname";
	
	public static String CACHE_LIFETIME = "cache.lifetime";
	
	public static String ALLOWED_SENDERS_KEY = "access.allowed_senders";
	public static String ALLOWED_SENDERS_DEFAULT = "";
	
	public static String PREDEFINED_CHANNELS_KEY = "predefined.channels";
	public static String PREDEFINED_CHANNELS_DEFAULT = "";
	
	public static String ADMINS_KEY = "admin.admins";
	public static String ADMINS_DEFAULT = "";
	
	public static String WELCOME_MSG_1_KEY = "welcomemsg.one";
	public static String WELCOME_MSG_2_KEY = "welcomemsg.two";
	public static String WELCOME_MSG_3_KEY = "welcomemsg.three";
	public static String WELCOME_MSG_4_KEY = "welcomemsg.four";
	public static String WELCOME_MSG_5_KEY = "welcomemsg.five";
	public static String WELCOME_MSG_6_KEY = "welcomemsg.six";
	
	private Collection<String> allowedJIDs = new ArrayList<String>();
	
	private Properties p = new Properties();
	
	public static Conf getInstance() {
		if (instance == null) {
			instance = new Conf();
		}
		return instance;
	}
	
	public Conf() {
		this.initialize();
	}
	
	private void initialize() {
		try{
			// TODO, the location of the configuration can be a default but should be also
			// able to be read from the command line arguments.
		    this.p.load(new FileInputStream("/opt/buddycloud-broadcaster/conf.ini"));
		} catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
	}
	
	public String getConfString(String confKey, String defaultValue) {
		return this.p.getProperty(confKey, defaultValue);
	}
	
	public String getConfString(String confKey) {
		return this.p.getProperty(confKey);
	}

	public String[] getWelcomeMessages() {
		String[] messages = new String[6];
		
		messages[0] = this.getConfString(WELCOME_MSG_1_KEY);
		messages[1] = this.getConfString(WELCOME_MSG_2_KEY);
		messages[2] = this.getConfString(WELCOME_MSG_3_KEY);
		messages[3] = this.getConfString(WELCOME_MSG_4_KEY);
		messages[4] = this.getConfString(WELCOME_MSG_5_KEY);
		messages[5] = this.getConfString(WELCOME_MSG_6_KEY);
		
		return messages;
	}
	
	public Integer getConfInteger(String confKey) {
		try {
			return Integer.parseInt(this.p.getProperty(confKey));
		} catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
		return -1;
	}
	
	public Integer getConfInteger(String confKey, Integer defaultValue) {
		try {
			return Integer.parseInt(this.p.getProperty(confKey));
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public Collection<String> getPredefinedChannels() {
		return Helpers.stringToCollection(this.getConfString(PREDEFINED_CHANNELS_KEY, PREDEFINED_CHANNELS_DEFAULT));
	}
	
	public boolean isAllowedSender(String jid) {
				
		if(this.allowedJIDs.isEmpty()) {
			this.allowedJIDs = Helpers.stringToCollection(this.getConfString(ALLOWED_SENDERS_KEY, ALLOWED_SENDERS_DEFAULT));
		}
		
		//LogMe.debug("Allowed senders: '" + this.allowedJIDs.toString() + "' (" + this.getConfString(ALLOWED_SENDERS_KEY, ALLOWED_SENDERS_DEFAULT) + ")");
		
		if(this.allowedJIDs.contains(jid)) {
			LogMe.debug("'" + jid + "' is allowed sender.");
			return true;
		} 
		LogMe.debug("'" + jid + "' is NOT a allowed sender.");
		return false;
	}
}
