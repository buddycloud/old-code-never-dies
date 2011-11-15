package com.buddycloud.pubsub.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.ofllineStorage.StorageItem;

public class DBOfflineStorage {
	
	private static DBOfflineStorage instance;
	
	private Connection conn = null;
	
	private String db_uri = "";
	private String db_user = Conf.getInstance().getConfString(Conf.DB_ROSTER_USER_KEY);
	private String db_pw = Conf.getInstance().getConfString(Conf.DB_ROSTER_PW_KEY);
	
	private static final String QUERY_INSERT_ITEM = "INSERT INTO broadcaster.offline_storage " +
													"(jid, payload) VALUES (?, ?)";
	private static final String QUERY_SELECT_ITEMS_OF_JID = "SELECT id, jid, payload FROM offline_storage WHERE jid = ?";
	private static final String QUERY_DELETE_ITEM_WITH_ID = "DELETE FROM offline_storage WHERE id = ?";
	
	public static DBOfflineStorage getInstance() {
		if(instance == null) {
			instance = new DBOfflineStorage();
		}
		return instance;
	}
	
	public DBOfflineStorage() {
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
	
	public boolean insertItem(StorageItem item) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to insert jid to roster in db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_INSERT_ITEM);

			pstmt.setString(1, item.getJid());
			pstmt.setString(2, item.getPayloadAsString());
			
			pstmt.execute();
			LogMe.info("Query 'insertItem' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while inserting item to offline storage db. Error: '" + se.getMessage() + "'");
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
            	LogMe.warning("Database error occurred while closing pstm of insertItem.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public Collection<StorageItem> getOfflineItemsOfJid(String bareJID) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		Collection<StorageItem> items = new LinkedList<StorageItem>();
		
		LogMe.debug("Starting to select offline events of JID '" + bareJID + "' db:");
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_SELECT_ITEMS_OF_JID);
			pstmt.setString(1, bareJID);
			ResultSet rs = pstmt.executeQuery();
			LogMe.info("Query 'getOfflineItemsOfJid' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			while(rs.next()) {
				try {
					StorageItem si = new StorageItem(rs.getInt(1), rs.getString(2), rs.getString(3));
					items.add(si);
				} catch (Exception e) {
					LogMe.warning(e.getMessage() + " SHIT, SOMETHING is VERY VERY WRONG!");
				}
			}
			rs.close();	
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while reading leafnode from db. Error: '" + se.getMessage() + "'");
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
            	LogMe.warning("Database error occurred while closing pstm of getLeafNode.");
            	e.printStackTrace(); 
            }
		}
		return items;
	}
	
	public boolean deleteItem(int id) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to delete item with id '" + Integer.toString(id) + "' from DB:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_DELETE_ITEM_WITH_ID);
			pstmt.setInt(1, id);
			pstmt.execute();
			LogMe.info("Query 'deleteItem' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to delete offline storage item at db.");
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
            	LogMe.warning("Database error occurred while closing pstm of deleteItem.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
}
