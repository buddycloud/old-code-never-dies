package com.buddycloud.pubsub.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;

import com.buddycloud.pubsub.Item.PayloadType;
import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.node.Leaf;
import com.buddycloud.pubsub.node.LeafType;
import com.buddycloud.pubsub.node.LeafnodeChange;
import com.buddycloud.pubsub.node.Nodes;
import com.buddycloud.pubsub.utils.FastDateFormat;

public class DBNode {

	private static DBNode instance;
	
	private Connection conn = null;
	
	private String db_uri = "";
	private String db_user = Conf.getInstance().getConfString(Conf.DB_ROSTER_USER_KEY);
	private String db_pw = Conf.getInstance().getConfString(Conf.DB_ROSTER_PW_KEY);
	
	private static final String QUERY_INSERT_LEAFNODE = "INSERT INTO broadcaster.leafnode " +
								"(nodename, title, description, access_model, publish_model, max_items, " +
								"payload_type, default_affiliation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String QUERY_SELECT_ALL_LEAFNODES = "SELECT nodename FROM broadcaster.leafnode";
	private static final String QUERY_SELECT_LEAFNODE_WITH_NODENAME = "SELECT nodename, title, description, access_model, " +
			                    "publish_model, max_items, payload_type, default_affiliation, datecreated, leafnode_id, avatar_hash " +
			                    "FROM broadcaster.leafnode WHERE nodename = ? LIMIT 1";
	private static final String QUERY_UPDATE_NODE = "UPDATE broadcaster.leafnode SET " +
								"title = ?, description = ?, access_model = ?, default_affiliation = ?, avatar_hash = ? WHERE nodename = ?";
	private static final String QUERY_DELETE_NODE = "DELETE FROM broadcaster.leafnode WHERE nodename = ?";
	
	
	public static DBNode getInstance() {
		if(instance == null) {
			instance = new DBNode();
		}
		return instance;
	}
	
	public DBNode() {
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
	
	public boolean insertLeafnode(Leaf node) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to insert leafnode to db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_INSERT_LEAFNODE, Statement.RETURN_GENERATED_KEYS);

			pstmt.setString(1, node.getNodeName());
			pstmt.setString(2, node.getTitle());
			pstmt.setString(3, node.getDescription());
			pstmt.setString(4, node.getAccessModelAsString());
			pstmt.setString(5, node.getPublishModesAsString());
			pstmt.setInt(6, node.getMaxItems());
			pstmt.setString(7, node.getPayloadTypeAsString());
			pstmt.setString(8, node.getDefaultAffiliation().toString());
			
			pstmt.execute();
			
			ResultSet rs = pstmt.getGeneratedKeys();
			
			while(rs.next()) {
				try {
					LeafType t = Leaf.getLeafType(node.getNodeName());
					if( t == LeafType.topicchannel) {
						DBChannelMeta.getInstance().insertMetaData(rs.getInt(1), false);
					} else if ( t == LeafType.userchannel) {
						DBChannelMeta.getInstance().insertMetaData(rs.getInt(1), true);
					}
					Nodes.getInstance().getLeafnode(node.getNodeName()).setDbId(rs.getInt(1));
					break;
				} catch (Exception e) {
					LogMe.warning(e.getMessage() + " could not handle correclty the Channel's meta data when insterting the leaf.");
				}
			}
			ret = true;
			LogMe.info("Query 'insertLeafnode' executed (" + Boolean.toString(ret) + "'). Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while inserting leafnode to db. Error: '" + se.getMessage() + "'");
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
            	LogMe.warning("Database error occurred while closing pstm of insertLeafnode.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public boolean updateLeafnodeToDB(LeafnodeChange change) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to update node '" + change.getNodename() + "' to DB.:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_UPDATE_NODE);
			pstmt.setString(1, change.getNode().getTitle());
			pstmt.setString(2, change.getNode().getDescription());
			pstmt.setString(3, change.getNode().getAccessModelAsString());
			pstmt.setString(4, change.getNode().getDefaultAffiliation().toString());
			pstmt.setString(6, change.getNode().getNodeName());
			pstmt.setString(5, change.getNode().getAvatarHash());
			
			pstmt.execute();
			LogMe.info("Query 'updateLeafnodeToDB' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to update leafnode at db.");
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
            	LogMe.warning("Database error occurred while closing pstm of updateLeafnodeToDB.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public boolean deleteLeafNode(String nodename, int leafnode_id) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to delete node '" + nodename + "' from DB:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_DELETE_NODE);
			pstmt.setString(1, nodename);
			pstmt.execute();
			DBChannelMeta.getInstance().deleteMetaData(leafnode_id);
			LogMe.info("Query 'deleteLeafNode' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to delete leafnode at db.");
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
            	LogMe.warning("Database error occurred while closing pstm of deleteLeafNode.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public Leaf getLeafNode(String nodename) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		Leaf node = new Leaf(nodename);
		LogMe.debug("Starting to select leafnode with name '" + nodename + "' db:");
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			pstmt = this.conn.prepareStatement(QUERY_SELECT_LEAFNODE_WITH_NODENAME);
			pstmt.setString(1, nodename);
			ResultSet rs = pstmt.executeQuery();
			LogMe.info("Query 'getLeafNode' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			while(rs.next()) {
				try {
					node.setTitle(rs.getString(2));
					node.setDescription(rs.getString(3));
					node.setAccessModel(rs.getString(4));
					node.setPublishModel(rs.getString(5));
					node.setMaxItems(rs.getString(6));
					// TODO lastItemId to memcache?
					node.setLastItemId(DBItem.getInstance().getIdOfLastPost(node.getNodeName()));
					node.setPayloadType( PayloadType.parseFromString(rs.getString(7)) );
					node.setDefaultAffiliation(rs.getString(8));
					node.setCreated(FastDateFormat.getInstance(Conf.TIME_TEMPLATE, TimeZone.getTimeZone(Conf.TIME_ZONE)).format(rs.getDate(9)));
					
					node.setMetadata(DBChannelMeta.getInstance().getNodesMetadata(rs.getInt(10)));
					node.setOwner(DBSubscription.getInstance().getOwnerOfNode(nodename));
					node.setDbId(rs.getInt(10));
					node.setAvatarHash(rs.getString(11));
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
		return node;
	}
	
	public void initKnownNodes() {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to select all leafnodes from db:");
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			this.conn.setAutoCommit(false);
			Statement stmt = this.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(25);
			ResultSet rs = stmt.executeQuery(QUERY_SELECT_ALL_LEAFNODES);
			
			LogMe.info("Query 'initKnownNodes' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			LogMe.debug("Starting to init leaf nodes, this might take a while...");
			
			int i = 0;
			start = System.currentTimeMillis();
			while(rs.next()) {
				Nodes.getInstance().addAsKnownNode(rs.getString(1));
				LogMe.debug(" - added node '" + rs.getString(1) + "'.");
				i++;
			}
			rs.close();
			rs = null;
			stmt.close();
			
			stmt = null;
			this.conn.close();
        	this.conn = null;
        	
			LogMe.debug("All leafNodes added, total nodes of '" + Integer.toString(i) + "' added. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while reading leafnodes from db. Error: '" + se.getMessage() + "'");
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
            	LogMe.warning("Database error occurred while closing pstm of initKnownNodes.");
            	e.printStackTrace(); 
            }
            
            try {
                if (this.conn != null) {
                	this.conn.close();
                	this.conn = null;
                } 
            } catch (Exception e) { 
            	System.out.println("Database error occurred while closing the connection too! Error: '" + e.getMessage() + "'.");
            	this.conn = null;
            }
		}	
	}
	
}
