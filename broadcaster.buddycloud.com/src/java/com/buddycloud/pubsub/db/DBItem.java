package com.buddycloud.pubsub.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.buddycloud.pubsub.Item.Entry;
import com.buddycloud.pubsub.Item.ItemChange;
import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.node.Nodes;

public class DBItem {

	private static DBItem instance;
	
	private Connection conn = null;
	
	private String db_uri = "";
	private String db_user = Conf.getInstance().getConfString(Conf.DB_ROSTER_USER_KEY);
	private String db_pw = Conf.getInstance().getConfString(Conf.DB_ROSTER_PW_KEY);
	
	private static final String QUERY_INSERT_ITEM = "INSERT INTO broadcaster.item " +
	                                                    "(nodename, id, payload) VALUES (?, ?, ?)";
	private static final String QUERY_DELETE_ITEM = "DELETE FROM broadcaster.item WHERE nodename = ? AND id = ?";
	private static final String QUERY_DELETE_ALL_ITEMS_OF_NODE = "DELETE FROM broadcaster.item WHERE nodename = ?";
	private static final String QUERY_SELECT_ALL_ITEMS = "SELECT payload, id FROM broadcaster.item WHERE nodename = ? ORDER BY id ASC";
	private static final String QUERY_SELECT_ID_OF_LATEST_POST = "SELECT id FROM broadcaster.item " +
																 "WHERE nodename = ? ORDER BY datecreated DESC LIMIT 1";
	
	public static DBItem getInstance() {
		if(instance == null) {
			instance = new DBItem();
		}
		return instance;
	}
	
	public DBItem() {
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
	
	public boolean insertItem(ItemChange change) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to insert item to db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_INSERT_ITEM);
			pstmt.setString(1, change.getNodename());
			pstmt.setLong(2, change.getEntry().getId());
			pstmt.setString(3, change.getEntry().payloadAsString());
			pstmt.execute();
			LogMe.info("Query 'insertItem' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while inserting item to db. Error: '" + se.getMessage() + "'");
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
	
	public boolean deleteItem(ItemChange change) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to delete item from db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_DELETE_ITEM);
			pstmt.setString(1, change.getNodename());
			pstmt.setLong(2, change.getEntry().getId());
			pstmt.execute();
			LogMe.info("Query 'deleteItem' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to delete item from DB.");
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
	
	public boolean deleteAllItemsOfNode(String nodename) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to delete items of a node '" + nodename + "' from db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_DELETE_ALL_ITEMS_OF_NODE);
			pstmt.setString(1, nodename);
			pstmt.execute();
			LogMe.info("Query 'deleteAllItemsOfNode' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to delete item from DB.");
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
            	LogMe.warning("Database error occurred while closing pstm of deleteAllItemsOfNode.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public void fillLeafnodeWithItems(String nodename) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to select all items for a node from db:");
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_SELECT_ALL_ITEMS);
			pstmt.setString(1, nodename);
			pstmt.execute();
			ResultSet rs = pstmt.executeQuery();
			LogMe.info("Query 'initLeafnodeWithItems' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			LogMe.debug("Starting to add items of a node, this might take a while...");
			int i = 0;
			start = System.currentTimeMillis();
			while(rs.next()) {
				Nodes.getInstance().addInitItem(nodename, new Entry(rs.getString(1), rs.getLong(2)));
				i++;
			}
			rs.close();
			LogMe.debug("All items added, total '" + Integer.toString(i) + "' items. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while reading items from db. Error: '" + se.getMessage() + "'");
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
            	LogMe.warning("Database error occurred while closing pstm of initLeafNodeWithItems.");
            	e.printStackTrace(); 
            }
		}
	}
	
	public Long getIdOfLastPost(String nodename) {
		PreparedStatement pstmt = null;
		Long id = -1L;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to the ID of latest item from db:");
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_SELECT_ID_OF_LATEST_POST);
			pstmt.setString(1, nodename);
			pstmt.execute();
			ResultSet rs = pstmt.executeQuery();
			LogMe.info("Query 'getIdOfLastPost' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			while(rs.next()) {
				id = rs.getLong(1);
				break;
			}
			rs.close();
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while reading the id of last posted item from db. Error: '" + se.getMessage() + "'");
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
            	LogMe.warning("Database error occurred while closing pstm of getIdOfLastPost.");
            	e.printStackTrace(); 
            }
		}
		return id;
	}
}
