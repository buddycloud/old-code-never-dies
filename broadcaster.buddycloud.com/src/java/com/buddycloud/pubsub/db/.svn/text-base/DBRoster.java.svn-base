package com.buddycloud.pubsub.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

import com.buddycloud.pubsub.BuddycloudPubsubComponent;
import com.buddycloud.pubsub.config.Conf;
import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.managers.IncidentManager;
import com.buddycloud.pubsub.managers.OutQueueManager;
import com.buddycloud.pubsub.subscriber.SubscriberEntities;
import com.buddycloud.pubsub.subscriber.SubscriberEntity;
import com.buddycloud.pubsub.subscriber.SubscriberEntityType;

public class DBRoster {

	private static DBRoster instance;
	
	private Connection conn = null;
	
	private String db_uri = "";
	private String db_user = Conf.getInstance().getConfString(Conf.DB_ROSTER_USER_KEY);
	private String db_pw = Conf.getInstance().getConfString(Conf.DB_ROSTER_PW_KEY);
	
	private static final String QUERY_INSERT_JID_TO_ROSTER = "INSERT INTO broadcaster.roster (jid, entrytype) VALUES (?, ?)";
	private static final String QUERY_DELETE_JID_FROM_ROSTER = "DELETE FROM broadcaster.roster WHERE jid = ?";
	private static final String QUERY_SELECT_ALL_JIDS_FROM_ROSTER = "SELECT jid, entrytype FROM broadcaster.roster";
	private static final String QUERY_UPDATE_ROSTER_AS_NORMAL = "UPDATE broadcaster.roster SET entrytype = 'normal' WHERE jid = ?";
	private static final String QUERY_UPDATE_LASTSEEN = "UPDATE broadcaster.roster SET lastseen = ? WHERE jid = ?";
	
	public static DBRoster getInstance() {
		if(instance == null) {
			instance = new DBRoster();
		}
		return instance;
	}
	
	public DBRoster() {
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
	
	public boolean insertJidToRoster(String jid, SubscriberEntityType type) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to insert jid to roster in db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_INSERT_JID_TO_ROSTER);
			pstmt.setString(1, jid);
			pstmt.setString(2, type.toString());
			pstmt.execute();
			
			LogMe.info("Query 'insertJidToRoster' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while inserting Jid to roster to db. Error: '" + se.getMessage() + "'");
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
            	LogMe.warning("Database error occurred while closing pstm of insertJidToRoster.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public boolean deleteJidFromRoster(String jid) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to delete jid from roster in db:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_DELETE_JID_FROM_ROSTER);
			pstmt.setString(1, jid);
			pstmt.execute();
			
			LogMe.info("Query 'deleteJidFromRoster' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to delete JID from roster at db.");
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
            	LogMe.warning("Database error occurred while closing pstm of deleteJidFromROster.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public boolean updateJidToNormal(String jid) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to update jid '" + jid + "' in roster in db to 'normal':");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_UPDATE_ROSTER_AS_NORMAL);
			pstmt.setString(1, jid);
			pstmt.execute();
			
			LogMe.info("Query 'updateJidToNormal' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to update JID in roster at db.");
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
            	LogMe.warning("Database error occurred while closing pstm of updateJidToNormal.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public boolean updateLastSeen(String jid) {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to update lastseen of '" + jid + "' in roster:");
		Boolean ret = false;
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
			
			pstmt = this.conn.prepareStatement(QUERY_UPDATE_LASTSEEN);
			
			pstmt.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
			pstmt.setString(2, jid);
			
			pstmt.execute();
			
			LogMe.info("Query 'updateLastSeen' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
			ret = true;
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while trying to update lastseen of JID in roster at db.");
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
            	LogMe.warning("Database error occurred while closing pstm of updateLastSeen.");
            	e.printStackTrace(); 
            }
		}	
		return ret;
	}
	
	public void probeEveryJidInRoster() {
		PreparedStatement pstmt = null;
		Long start = System.currentTimeMillis();
		LogMe.debug("Starting to get all JIDs in roster to probe for presences:");
		try {
			this.initializeConnection(); // This will immediately return if connection is up already.
	
			this.conn.setAutoCommit(false);
			Statement stmt = this.conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			stmt.setFetchSize(25);
			ResultSet rs = stmt.executeQuery(QUERY_SELECT_ALL_JIDS_FROM_ROSTER);
			
			LogMe.info("Query 'ProbeEveryJidInRoster' executed. Execution took '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
			
			Presence probe = new Presence();
			probe.setType(org.xmpp.packet.Presence.Type.probe);
			probe.setFrom(BuddycloudPubsubComponent.PUBSUB_ENGINE.getJID());
			
			while(rs.next()) {
				if(rs.getString(2).equals("temporary")) {
					SubscriberEntities.getInstance().addEntity(new SubscriberEntity(new JID(rs.getString(1)), null, SubscriberEntityType.temporary));
					continue;
				} else if(rs.getString(2).equals("blocked")) {
					IncidentManager.getInstance().putBlockedJID(rs.getString(1));
					continue;
				}
	
				probe.setTo(rs.getString(1));
				SubscriberEntities.getInstance().addEntity(new SubscriberEntity(new JID(rs.getString(1)), Presence.Type.unavailable));
				
				//try {
					//ComponentManagerFactory.getComponentManager().sendPacket(BuddycloudPubsubComponent.PUBSUB_ENGINE, probe);
					//LogMe.debug("Probe message send to: '" + rs.getString(1) + "'.");
					OutQueueManager.getInstance().put(probe.createCopy());
//				} catch (ComponentException e) {
//					LogMe.warning("Error while probing user with jid '" + rs.getString(1) + "': '" + e.getMessage() + "'!");
//				}
				
			}
			
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
			this.conn.close();
        	this.conn = null;
			Runtime.getRuntime().gc();
			
		} catch (SQLException se) {
			LogMe.warning("Database error occurred while getting channelID.");
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
            	LogMe.warning("Database error occurred while closing pstm of getChannelID.");
            	e.printStackTrace(); 
            }
		}
	}
}
