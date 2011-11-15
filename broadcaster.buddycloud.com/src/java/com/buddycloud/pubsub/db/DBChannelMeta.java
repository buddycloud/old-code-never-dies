package com.buddycloud.pubsub.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.log.LogMe;

public class DBChannelMeta {

		private static DBChannelMeta instance;
		
		private Connection conn = null;
		
		private String db_uri = "";
		private String db_user = Conf.getInstance().getConfString(Conf.DB_METADATA_USER_KEY);
		private String db_pw = Conf.getInstance().getConfString(Conf.DB_METADATA_PW_KEY);
		
		private static final String QUERY_SELECT_METADATA = "SELECT rank, popularity, latitude, longitude, " +
				                                            "location, followers, is_hidden FROM maitred.channel_metadata WHERE " +
				                                            "leafnode_id = ? LIMIT 1;";
		private static final String QUERY_INSERT_METADATA = "INSERT INTO maitred.channel_metadata (leafnode_id, rank, is_hidden) " +
															"VALUES (?, ?, ?)";
		private static final String QUERY_DELETE_METADATA = "DELETE FROM maitred.channel_metadata WHERE leafnode_id = ?";
		
		public static DBChannelMeta getInstance() {
			if(instance == null) {
				instance = new DBChannelMeta();
			}
			return instance;
		}
		
		public DBChannelMeta() {
			this.createDBUri();
			try {
				this.initializeConnection();
			} catch (SQLException e) {
				LogMe.warning("PROBLEM CREATING CONNECTION TO DATABASE: '" + e.getMessage() + "'.");
			}
		}
		
		private void createDBUri() {
			this.db_uri = "jdbc:" + Conf.getInstance().getConfString(Conf.DB_METADATA_DRIVER_KEY) + 
			"://" + Conf.getInstance().getConfString(Conf.DB_METADATA_HOST_KEY) + "/" + Conf.getInstance().getConfString(Conf.DB_METADATA_NAME_KEY);
		}
		
		private void initializeConnection() throws SQLException {
			if(this.conn != null) {
				return;
			}
			this.conn = DriverManager.getConnection(this.db_uri, 
													this.db_user, 
													this.db_pw);
		}
		
		public HashMap<String, String> getNodesMetadata(int leafnode_id) {
			PreparedStatement pstmt = null;
			HashMap<String, String> values = new HashMap<String, String>();
			Long start = System.currentTimeMillis();
			LogMe.debug("Starting to select leafnode's metadata with id '" + leafnode_id + "' from db:");
			try {
				this.initializeConnection(); // This will immediately return if connection is up already.
				pstmt = this.conn.prepareStatement(QUERY_SELECT_METADATA);
				pstmt.setInt(1, leafnode_id);
				ResultSet rs = pstmt.executeQuery();
				LogMe.info("Query 'getNodesMetadata' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
				while(rs.next()) {
					try {
						//rank, 
						values.put("rank", Integer.toString(rs.getInt(1)));
						//popularity, 
						values.put("popularity", Integer.toString(rs.getInt(2)));
						//latitude,
						values.put("latitude", Double.toString(rs.getDouble(3)));
						//longitude
						values.put("longitude", Double.toString(rs.getDouble(4)));
						//location,
						values.put("location", rs.getString(5));
						//followers
						values.put("followers", Integer.toString(rs.getInt(6)));
						//is_hidden
						values.put("is_hidden", Boolean.toString(rs.getBoolean(7)));
						
						break;
					} catch (Exception e) {
						LogMe.warning(e.getMessage() + " SHIT, SOMETHING is VERY VERY WRONG with metadata!");
					}
				}
				rs.close();	
			} catch (SQLException se) {
				LogMe.warning("Database error occurred while reading metadata of a node from db. Error: '" + se.getMessage() + "'");
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
	            	LogMe.warning("Database error occurred while closing pstm of getNodesMetadata.");
	            	e.printStackTrace(); 
	            }
			}
			return values;
		}
		
		public boolean insertMetaData(int leafnode_id, boolean is_hidden) {
			PreparedStatement pstmt = null;
			Long start = System.currentTimeMillis();
			LogMe.debug("Starting to insert metadata in db:");
			Boolean ret = false;
			try {
				this.initializeConnection(); // This will immediately return if connection is up already.
				pstmt = this.conn.prepareStatement(QUERY_INSERT_METADATA);

				pstmt.setInt(1, leafnode_id);
				pstmt.setInt(2, leafnode_id);
				pstmt.setBoolean(3, is_hidden);
				
				pstmt.execute();
				
				ResultSet rs = pstmt.getGeneratedKeys();
				
				ret = true;
				LogMe.info("Query 'insertMetaData' executed (" + Boolean.toString(ret) + "'). Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
				
			} catch (SQLException se) {
				LogMe.warning("Database error occurred while inserting metadata to db. Error: '" + se.getMessage() + "'");
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
	            	LogMe.warning("Database error occurred while closing pstm of insertMetaData.");
	            	e.printStackTrace(); 
	            }
			}	
			return ret;
		}
		
		public boolean deleteMetaData(int leafnode_id) {
			PreparedStatement pstmt = null;
			Long start = System.currentTimeMillis();
			LogMe.debug("Starting to delete metadata with leafnode id " + Integer.toString(leafnode_id) + " from DB:");
			Boolean ret = false;
			try {
				this.initializeConnection(); // This will immediately return if connection is up already.
				pstmt = this.conn.prepareStatement(QUERY_DELETE_METADATA);
				pstmt.setInt(1, leafnode_id);
				pstmt.execute();
				LogMe.info("Query 'deleteMetaData' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
				ret = true;
			} catch (SQLException se) {
				LogMe.warning("Database error occurred while trying to delete metadata from db.");
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
	            	LogMe.warning("Database error occurred while closing pstm of deleteMetaData.");
	            	e.printStackTrace(); 
	            }
			}	
			return ret;
		}
}
