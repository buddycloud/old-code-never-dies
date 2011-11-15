package com.buddycloud.pubsub.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.node.Nodes;
import com.buddycloud.pubsub.subscriber.PubsubhubbubSubscriber;
import com.buddycloud.pubsub.subscriber.PubsubhubbubSubscriptionChange;

public class DBPubsubhubbubSubscriptions {

	private static DBPubsubhubbubSubscriptions instance;
	
	private Connection conn = null;
	
	private String db_uri = "";
	private String db_user = Conf.getInstance().getConfString(Conf.DB_ROSTER_USER_KEY);
	private String db_pw = Conf.getInstance().getConfString(Conf.DB_ROSTER_PW_KEY);
	
	private static final String QUERY_SELECT_SUBSCRIPTIONS_OF_LEAFNODE = "SELECT nodename, callback, secret FROM " +
								"broadcaster.pubsubhubbubsubscription WHERE nodename = ?";
	private static final String QUERY_INSERT_SUBSCRIPTION = "INSERT INTO broadcaster.pubsubhubbubsubscription (nodename, callback, " +
							 	"topic, verify_token, lease_seconds, secret, goes_old) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String QUERY_DELETE_SUBSCRIPTION = "DELETE FROM broadcaster.pubsubhubbubsubscription WHERE topic = ? AND " +
								"secret = ?";
	
	public static DBPubsubhubbubSubscriptions getInstance() {
		if(instance == null) {
			instance = new DBPubsubhubbubSubscriptions();
		}
		return instance;
	}
	
	public DBPubsubhubbubSubscriptions() {
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
	
	public boolean insertSubscription(PubsubhubbubSubscriptionChange insert) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to insert pubsubhubbubsubscription to db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_INSERT_SUBSCRIPTION);
			pstmt.setString(1, insert.getNodename());
			pstmt.setString(2, insert.getCallback());
			pstmt.setString(3, insert.getTopic());
			pstmt.setString(4, insert.getVerify_token());
			pstmt.setString(5, insert.getLease_seconds());
			pstmt.setString(6, insert.getSecret());
			pstmt.setLong(7, insert.getGoesold());
			pstmt.execute();
			
			LogMe.info("Query 'insertPubsubhubbubsubscription' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while inserting pubsubhubbubsubscription to db. Error: '" + se.getMessage() + "'");
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
            	LogMe.warning("Database error occurred while closing pstm of insertPubsubhubbubsubscription.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public boolean deleteSubscription(PubsubhubbubSubscriptionChange deletion) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to delete pubsubhubbubsubscription from db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_DELETE_SUBSCRIPTION);
			pstmt.setString(1, deletion.getTopic());
			//pstmt.setString(2, deletion.getVerify_token());
			pstmt.setString(2, deletion.getSecret());
			pstmt.execute();
			
			LogMe.info("Query 'deletePubsubhubbubsubscription' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while deleting pubsubhubbubsubscription to db. Error: '" + se.getMessage() + "'");
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
            	LogMe.warning("Database error occurred while closing pstm of deletionPubsubhubbubsubscription.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public void initNodesPubsubhubbubSubscriptions(String nodename) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to select nodesPubsubhubbubSubscriptions for node '" + nodename + "' from db:");
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_SELECT_SUBSCRIPTIONS_OF_LEAFNODE);
			pstmt.setString(1, nodename);
			ResultSet rs = pstmt.executeQuery();
			
			LogMe.info("Query 'initNodesPubsubhubbubSubscriptions' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			LogMe.debug("Starting to init subscriptions, this might take a while...");
			
			int i = 0;
			start = System.currentTimeMillis();
			while(rs.next()) {
				Nodes.getInstance().putAsPubsubhubbubSubscriber(rs.getString(1), new PubsubhubbubSubscriber(rs.getString(2), rs.getString(3)));
				i++;
				if((i % 100) == 0)
					LogMe.debug("Added '" + i + "' subscriptions.");
			}
			rs.close();
			
			LogMe.debug("All added, total '" + Integer.toString(i) + "' subscriptions found. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while reading all Pubsubhubbubsubscriptions for node '" + nodename + "' from db. Error: '" + se.getMessage() + "'");
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
            	LogMe.warning("Database error occurred while closing pstm of initNodesPubsubhubbubSubscriptions.");
            	e.printStackTrace(); 
            }
		}	
	}
}
