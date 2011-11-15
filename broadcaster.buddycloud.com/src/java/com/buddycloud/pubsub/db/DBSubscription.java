package com.buddycloud.pubsub.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.node.Nodes;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;
import com.buddycloud.pubsub.subscriber.SubscriberEntity;
import com.buddycloud.pubsub.subscriber.Subscription;
import com.buddycloud.pubsub.subscriber.SubscriptionChange;

public class DBSubscription {

	private static DBSubscription instance;
	
	private Connection conn = null;
	
	private String db_uri = "";
	private String db_user = Conf.getInstance().getConfString(Conf.DB_ROSTER_USER_KEY);
	private String db_pw = Conf.getInstance().getConfString(Conf.DB_ROSTER_PW_KEY);
	
	private static final String QUERY_INSERT_SUBSCRIPTION = "INSERT INTO broadcaster.subscription " +
								"(nodename, jid, subscription, affiliation) VALUES (?, ?, ?, ?)";
	private static final String QUERY_SELECT_ALL_SUBSCRIPTIONS = "SELECT nodename, jid, subscription, affiliation FROM broadcaster.subscription " +
								"WHERE subscription = 'subscribed' OR subscription = 'pending'";
	private static final String QUERY_SELECT_SUBSCRIPTIONS_OF_LEAFNODE = "SELECT nodename, jid, subscription, affiliation FROM " +
								"broadcaster.subscription WHERE nodename = ?";
	private static final String QUERY_DELETE_SUBSCRIPTION = "DELETE FROM broadcaster.subscription WHERE jid = ? AND nodename = ?";
	private static final String QUERY_UPDATE_SUBSCRIPTION = "UPDATE broadcaster.subscription SET subscription = ?, affiliation = ? " +
								"WHERE jid = ? AND nodename = ?";
	private static final String QUERY_DELETE_ALL_SUBSCRIPTION_OF_NODE = "DELETE FROM broadcaster.subscription WHERE nodename = ?";
	private static final String QUERY_SELECT_UNCONFIGURED_SUBSCRIPTIONS_OF_USER = "SELECT nodename, affiliation FROM broadcaster.subscription " +
			"WHERE jid = ? AND subscription = 'unconfigured'";
	private static final String QUERY_SELECT_CONFIGURED_SUBSCRIPTIONS_OF_USER = "SELECT nodename, affiliation FROM broadcaster.subscription " +
			"WHERE jid = ? AND subscription = 'subscribed'";
	private static final String QUERY_SELECT_OWNER = "SELECT jid FROM broadcaster.subscription WHERE nodename = ? AND affiliation = 'owner'";
	
	public static DBSubscription getInstance() {
		if(instance == null) {
			instance = new DBSubscription();
		}
		return instance;
	}
	
	public DBSubscription() {
		this.createDBUri();
		try {
			this.initializeConnection();
		} catch (SQLException e) {
			LogMe.warning("PROBLEM CREATING CONNECTION TO DATABASE: '" + e.getMessage() + "'.");
		}
	}
	
	private void createDBUri() {
		this.db_uri = "jdbc:" + Conf.getInstance().getConfString(Conf.DB_ROSTER_DRIVER_KEY) + 
		"://" + Conf.getInstance().getConfString(Conf.DB_ROSTER_HOST_KEY) + "/" + Conf.getInstance().getConfString(Conf.DB_ROSTER_NAME_KEY);
	}
	
	private void initializeConnection() throws SQLException {
		if(this.conn != null) {
			return;
		}
		this.conn = DriverManager.getConnection(this.db_uri, 
												this.db_user, 
												this.db_pw);
	}
	
	public boolean insertSubscription(SubscriptionChange insert) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to insert subscription to db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_INSERT_SUBSCRIPTION);
			pstmt.setString(1, insert.getNodename());
			pstmt.setString(2, insert.getJid());
			pstmt.setString(3, insert.getSubscriber().getSubscriptionAsString());
			pstmt.setString(4, insert.getSubscriber().getAffiliationAsString());
			pstmt.execute();
			
			LogMe.info("Query 'insertSubscription' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while inserting subscription to db. Error: '" + se.getMessage() + "'");
			try {
                if (this.conn != null) {
                	this.conn.close();
                	this.conn = null;
                } 
            } catch (Exception e) { 
            	System.out.println("Database error occurred while closing the connection too! Error: '" + e.getMessage() + "'.");
            	this.conn = null;
            }
            
		} finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                } 
            } catch (Exception e) { 
            	LogMe.warning("Database error occurred while closing pstm of insertSubscription.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public boolean updateSubscription(SubscriptionChange update) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to update subscription to db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_UPDATE_SUBSCRIPTION);
			pstmt.setString(1, update.getSubscriber().getSubscriptionAsString());
			pstmt.setString(2, update.getSubscriber().getAffiliationAsString());
			pstmt.setString(3, update.getJid());
			pstmt.setString(4, update.getNodename());
			pstmt.execute();
			
			LogMe.info("Query 'updateSubscription' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while updating subscription to db. Error: '" + se.getMessage() + "'");
			try {
                if (this.conn != null) {
                	this.conn.close();
                	this.conn = null;
                } 
            } catch (Exception e) { 
            	System.out.println("Database error occurred while closing the connection too! Error: '" + e.getMessage() + "'.");
            	this.conn = null;
            }
            
		} finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                } 
            } catch (Exception e) { 
            	LogMe.warning("Database error occurred while closing pstm of updatingSubscription.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public boolean deleteSubscription(SubscriptionChange delete) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to delete subscriptions from db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_DELETE_SUBSCRIPTION);
			pstmt.setString(1, delete.getJid());
			pstmt.setString(2, delete.getNodename());
			pstmt.execute();
			
			LogMe.info("Query 'deleteSubscriptions' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to delete subscription from db.");
			se.printStackTrace();
			
			try {
                if (this.conn != null) {
                	this.conn.close();
                	this.conn = null;
                } 
            } catch (Exception e) { 
            	System.out.println("Database error occurred while closing the connection too!");
            	e.printStackTrace(); 
            	this.conn = null;
            }
            
		} finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                } 
            } catch (Exception e) { 
            	LogMe.warning("Database error occurred while closing pstm of deleteSubscriptions.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public HashMap<String, String> getUnconfiguredSubscriptions(String bareJID) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to select unconfigured subscriptions of JID '" + bareJID + "':");
		HashMap<String, String> subs = new HashMap<String, String>();
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_SELECT_UNCONFIGURED_SUBSCRIPTIONS_OF_USER);
			pstmt.setString(1, bareJID);
			ResultSet rs = pstmt.executeQuery();
			
			LogMe.info("Query 'getUnconfiguredSubscriptions' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			LogMe.debug("Starting to fill up unconfigured subscriptions, this might take a while...");
			
			int i = 0;
			start = System.currentTimeMillis();
			while(rs.next()) {
				subs.put(rs.getString(1), rs.getString(2));
				i++;
				if((i % 100) == 0)
					LogMe.debug("Added '" + i + "' subscriptions.");
			}
			rs.close();
			
			LogMe.debug("All added, total '" + Integer.toString(i) + "' unconfigured subscriptions found. Population took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to read unconfigured subscriptions of user '" + bareJID +"' from db.");
			se.printStackTrace();
			
			try {
                if (this.conn != null) {
                	this.conn.close();
                	this.conn = null;
                } 
            } catch (Exception e) { 
            	System.out.println("Database error occurred while closing the connection too!");
            	e.printStackTrace(); 
            	this.conn = null;
            }
            
		} finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                } 
            } catch (Exception e) { 
            	LogMe.warning("Database error occurred while closing pstm of getUnconfiguredSubscriptions.");
            	e.printStackTrace(); 
            }
		}	
		return subs;
	}
	
	public HashMap<String, String> getConfiguredSubscriptions(String bareJID) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to select configured subscriptions of JID '" + bareJID + "':");
		HashMap<String, String> subs = new HashMap<String, String>();
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_SELECT_CONFIGURED_SUBSCRIPTIONS_OF_USER);
			pstmt.setString(1, bareJID);
			ResultSet rs = pstmt.executeQuery();
			
			LogMe.info("Query 'getConfiguredSubscriptions' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			LogMe.debug("Starting to fill up configured subscriptions, this might take a while...");
			
			int i = 0;
			start = System.currentTimeMillis();
			while(rs.next()) {
				subs.put(rs.getString(1), rs.getString(2));
				i++;
				if((i % 100) == 0)
					LogMe.debug("Added '" + i + "' subscriptions.");
			}
			rs.close();
			
			LogMe.debug("All added, total '" + Integer.toString(i) + "' configured subscriptions found. Population took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to read configured subscriptions of user '" + bareJID +"' from db.");
			se.printStackTrace();
			
			try {
                if (this.conn != null) {
                	this.conn.close();
                	this.conn = null;
                } 
            } catch (Exception e) { 
            	System.out.println("Database error occurred while closing the connection too!");
            	e.printStackTrace(); 
            	this.conn = null;
            }
            
		} finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                } 
            } catch (Exception e) { 
            	LogMe.warning("Database error occurred while closing pstm of getConfiguredSubscriptions.");
            	e.printStackTrace(); 
            }
		}	
		return subs;
	}
	
	public boolean deleteSubscriptionOfNode(String nodename) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to delete subscriptions from db of a node '" + nodename + "':");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_DELETE_ALL_SUBSCRIPTION_OF_NODE);
			pstmt.setString(1, nodename);
			pstmt.execute();
			
			LogMe.info("Query 'deleteSubscriptionOfNode' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to delete subscriptions of a node from db.");
			se.printStackTrace();
			
			try {
                if (this.conn != null) {
                	this.conn.close();
                	this.conn = null;
                } 
            } catch (Exception e) { 
            	System.out.println("Database error occurred while closing the connection too!");
            	e.printStackTrace(); 
            	this.conn = null;
            }
            
		} finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                } 
            } catch (Exception e) { 
            	LogMe.warning("Database error occurred while closing pstm of deleteSubscriptionOfNode.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public void initNodesSubscriptions(String nodename) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to select nodesSubscriptions for node '" + nodename + "' from db:");
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_SELECT_SUBSCRIPTIONS_OF_LEAFNODE);
			pstmt.setString(1, nodename);
			ResultSet rs = pstmt.executeQuery();
			
			LogMe.info("Query 'initNodesSubscriptions' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			LogMe.debug("Starting to init subscriptions, this might take a while...");
			
			int i = 0;
			start = System.currentTimeMillis();
			while(rs.next()) {
				Nodes.getInstance().putAsSubscriber(rs.getString(1), 
						                            rs.getString(2), 
						                            rs.getString(4), 
						                            Subscription.createFromString(rs.getString(3)) );
				i++;
				if((i % 100) == 0)
					LogMe.debug("Added '" + i + "' subscriptions.");
			}
			Nodes.getInstance().getLeafnode(nodename).sortSubscribersList();
			rs.close();
			
			LogMe.debug("All added, total '" + Integer.toString(i) + "' subscriptions found. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while reading all subscriptions for node '" + nodename + "' from db. Error: '" + se.getMessage() + "'");
			try {
                if (this.conn != null) {
                	this.conn.close();
                	this.conn = null;
                } 
            } catch (Exception e) { 
            	System.out.println("Database error occurred while closing the connection too! Error: '" + e.getMessage() + "'.");
            	this.conn = null;
            }
            
		} finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                } 
            } catch (Exception e) { 
            	LogMe.warning("Database error occurred while closing pstm of initNodesSubscriptions.");
            	e.printStackTrace(); 
            }
		}	
	}
	
	public String getOwnerOfNode(String nodename) {
		PreparedStatement pstmt = null;
		String bareJID = "";
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to select owner for node '" + nodename + "' from db:");
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_SELECT_OWNER);
			pstmt.setString(1, nodename);
			ResultSet rs = pstmt.executeQuery();
			
			LogMe.info("Query 'getOwnerOfNode' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			start = System.currentTimeMillis();
			while(rs.next()) {
				bareJID = rs.getString(1);
				break;
			}
			rs.close();
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while reading owner for node '" + nodename + "' from db. Error: '" + se.getMessage() + "'");
			try {
                if (this.conn != null) {
                	this.conn.close();
                	this.conn = null;
                } 
            } catch (Exception e) { 
            	System.out.println("Database error occurred while closing the connection too! Error: '" + e.getMessage() + "'.");
            	this.conn = null;
            }
            
		} finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                } 
            } catch (Exception e) { 
            	LogMe.warning("Database error occurred while closing pstm of getOwnerOfNode.");
            	e.printStackTrace(); 
            }
		}	
		return bareJID;
	}
	
	public void initSubscriptions() {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to select all subscriptions from db:");
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			this.conn.setAutoCommit(false);
			
			Statement stmt = this.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(25);
			ResultSet rs = stmt.executeQuery(QUERY_SELECT_ALL_SUBSCRIPTIONS);
			
			LogMe.info("Query 'initsubscriptions' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			LogMe.debug("Starting to init subscriptions, this might take a while...");
			
			int i = 0;
			start = System.currentTimeMillis();
			while(rs.next()) {
				SubscriberEntity e = SubscriberEntities.getInstance().getEntity(rs.getString(2));
				if(e == null) {
					e = new SubscriberEntity(new JID(rs.getString(2)), Presence.Type.unavailable);
					SubscriberEntities.getInstance().addEntity(e);
				} 
				e.addSubscription(rs.getString(1));
				i++;
				if((i % 100) == 0)
					LogMe.debug("Added '" + i + "' subscriptions.");
			}
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
			this.conn.close();
        	this.conn = null;
			LogMe.debug("All added, total '" + Integer.toString(i) + "' subscribers found. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while reading all subscriptions from db. Error: '" + se.getMessage() + "'");
			try {
                if (this.conn != null) {
                	this.conn.close();
                	this.conn = null;
                } 
            } catch (Exception e) { 
            	System.out.println("Database error occurred while closing the connection too! Error: '" + e.getMessage() + "'.");
            	this.conn = null;
            }
            
		} finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                } 
            } catch (Exception e) { 
            	LogMe.warning("Database error occurred while closing pstm of initSubscriptions.");
            	e.printStackTrace(); 
            }
		}	
	}
}
