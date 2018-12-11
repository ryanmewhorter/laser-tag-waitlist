/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kronoze.lasertagwaitlist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ryan
 */
public class DBAssistant {
    
    private String host;
    private String port;
    private String dbName;
    private String username;
    private String password;
    
    private Connection connection;
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DecimalFormat RATE_FORMAT = new DecimalFormat("#0.00");
    
    private static final String PARTY_TABLE_NAME	= "parties";
    private static final String TIME_SLOT_TABLE_NAME	= "time_slots";
    private static final String SCHEDULE_TABLE_NAME	= "schedule";
    private static final String SETTINGS_TABLE_NAME	= "settings";

    public DBAssistant() {
	System.out.println("[DBAssistant]: Initializing DBAssistant using configuration in settings...");
	LocalSettings settings = LaserTagWaitlist.getLocalSettings();
	if (settings != null) {
	    this.host = settings.load("Host", "localhost");
	    this.port = settings.load("Port", "3306");
	    this.dbName = settings.load("DBName", "laser_tag_waitlist");
	    this.username = settings.load("DBUsername", "root");
	    this.password = settings.load("DBPassword", "secret");
	    this.connect();
	}
    }
    
    public DBAssistant(String host, String port, String dbName, String username, String password) {
	System.out.println("[DBAssistant]: Initializing DBAssistant using specified parameters...");
	this.host = host;
	this.port = port;
	this.dbName = dbName;
	this.username = username;
	this.password = password;
	this.connect();
    }
    
    public DBAssistant(Connection db) {
	this.connection = db;
	
    }
    
    public final boolean connect() {
	if (connection == null) {
	    try {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName, username, password);
		return true;
	    } catch (ClassNotFoundException | SQLException e) {
		System.err.println(this + " Connection Error: " + e.getMessage());
		return false;
	    }
	} else {
	    return true;
	}
    }
    
    public void updateTimeSlots(Waitlist waitlist) {
	Statement st;
	try {
	    st = connection.createStatement();
	    connection.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
	    st.executeUpdate("TRUNCATE TABLE `" + TIME_SLOT_TABLE_NAME + "`;");
	    connection.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
	String query = "INSERT INTO `" + TIME_SLOT_TABLE_NAME + "` (time) VALUES ";
	Iterator<LocalTime> iterator = waitlist.keySet().iterator();
	while (iterator.hasNext()) {
	    LocalTime time = iterator.next();
	    if (iterator.hasNext()) {
		query += "('" + time.format(TIME_FORMAT) + "'), ";
	    } else {
		query += "('" + time.format(TIME_FORMAT) + "')";
	    }
	}
	st.executeUpdate(query);
	} catch (SQLException ex) {
	    Logger.getLogger(DBAssistant.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    public Waitlist getWaitlist() {
	Waitlist waitlist = new Waitlist();
	System.out.println("[DBAssistant] Loading waitlist object with database records...");
	try {
	    ResultSet parties = connection.createStatement().executeQuery("SELECT p.id, p.name, p.size, p.admitted, pt.description AS payment_type, r.rate, p.description\n" +
						"FROM " + PARTY_TABLE_NAME + " p\n" +
						"INNER JOIN payment_types pt ON p.payment_type_id = pt.id\n" +
						"INNER JOIN rates r ON p.rate_id = r.id;");
	    while (parties.next()) {
		int id = parties.getInt(1);
		String name = parties.getString(2);
		int size = parties.getInt(3);
		boolean admitted = (parties.getInt(4) == 1);
		PaymentType paymentType = PaymentType.valueOf(parties.getString(5));
		float rate = parties.getFloat(6);
		String description = parties.getString(7);
		Party party = new Party(name, size, admitted, paymentType, description, rate);
		party.setId(id);
		ResultSet scheduledTimes =	connection.createStatement().executeQuery("SELECT ts.time\n" +
							    "FROM " + SCHEDULE_TABLE_NAME + " s\n" +
							    "INNER JOIN time_slots ts ON ts.id = s.time_id\n" +
							    "WHERE s.party_id = " + id + ";");
		while (scheduledTimes.next()) {
		    java.sql.Time t = scheduledTimes.getTime(1);
		    LocalTime time = LocalTime.of(t.getHours(), t.getMinutes()); // Not deprecated in java.sql.Time, only in java.util.Date
		    waitlist.addParty(time, party);
		}

	    }
	} catch (SQLException e) {
	    System.err.println(e.getMessage());
	}
	return waitlist;
    }
    
    public String loadSetting(String key) {
	System.out.println("[DBAssistant]: loadSetting(\"" + key + "\")...");
	try {
	    PreparedStatement pstmt = connection.prepareStatement("SELECT settings.value FROM settings WHERE settings.key = ? LIMIT 1");
	    pstmt.setString(1, key);
	    ResultSet result = pstmt.executeQuery();
	    String value = null;
	    if (result.next())
		value = result.getString(1);
	    System.out.println("[DBAssistant]: loadSetting(\"" + key + "\") = \"" + value + "\"");
	    return value;
	    
	} catch (SQLException ex) {
	    System.err.println("[DBAssistant]: loadSetting > SQLException > " + ex.getMessage());
	    return null;
	}
    }
    
    public boolean changeSetting(String key, String value) {
	System.out.println("[DBAssistant]: changeSetting(\"" + key + "\", \"" + value + "\")...");
	try {
	    PreparedStatement pstmt = connection.prepareStatement("UPDATE `settings` SET `settings`.`value` = ? WHERE `settings`.`key` = ?");
	    pstmt.setString(1, value);
	    pstmt.setString(2, key);
	    boolean success = (pstmt.executeUpdate() > 0);
	    if (success)
		System.out.println("[DBAssistant]: Successfully updated setting " + key + " to " + value + ".");
	    return success;
	} catch (SQLException ex) {
	    System.err.println("[DBAssistant]: changeSetting > SQLException > " + ex.getMessage());
	    return false;
	}
    }
    
    public ResultSet execute(String sql) {
	try {
	    if (connection != null) {
		Statement st = connection.createStatement();
		return st.executeQuery(sql);
	    }
	} catch (SQLException e) {
	    System.out.println("[DBAssistant]: Error executing SQL " + sql + ". " + e.getMessage());
	}
	return null;
    }
    
    /**
     * returnParty
     * @param pty Party to register.
     * @return Party's auto-generated ID.
     */
    public int registerParty(Party pty) {
	
	System.out.println("[DBAssistant]: Registering " + pty + "...");
	try {
	    if (connection != null) {
		PreparedStatement pstmt = connection.prepareStatement("INSERT INTO `laser_tag_waitlist`.`parties` (`name`, `size`, `admitted`, `payment_type_id`, `rate_id`, `description`) VALUES (?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
		pstmt.setString(1, pty.getName());
		pstmt.setInt(2, pty.getSize());
		pstmt.setInt(3, (pty.isAdmitted() ? 1 : 0));
		
		ResultSet paymentTypeIdResult = connection.createStatement().executeQuery( "SELECT id FROM payment_types WHERE description = '" + pty.getPaymentType().toString() + "' LIMIT 1;");
		paymentTypeIdResult.next();
		pstmt.setInt(4, paymentTypeIdResult.getInt(1));
		
		ResultSet rateIdResult = connection.createStatement().executeQuery(  "SELECT id FROM rates WHERE rate = '" + RATE_FORMAT.format(pty.getRate()) + "' LIMIT 1;");
		rateIdResult.next();
		pstmt.setInt(5, rateIdResult.getInt(1));
		
		pstmt.setString(6, pty.getHint());
		System.out.println("[DBAssistant]: Register party query: \"" + pstmt.toString() + "\"");
		pstmt.executeUpdate();
		ResultSet res = pstmt.getGeneratedKeys();
		if (res.next()) {
		    int ptyId = res.getInt(1);
		    System.out.println("[DBAssistant]: Registered party id: " + ptyId);
		    return ptyId;
		} else {
		    System.err.println("[DBAssistant]: Failed to register party " + pty + ".");
		    return -1;
		}
	    }
	} catch (SQLException ex) {
	    Logger.getLogger(DBAssistant.class.getName()).log(Level.SEVERE, null, ex);
	}
	return -1;
    }
    
    public void scheduleParty(Party pty, LocalTime time) {
	System.out.println("[DBAssistant]: Scheduling " + pty + " for " + time + "...");
	try {
	    PreparedStatement pstmt = connection.prepareStatement("INSERT INTO `laser_tag_waitlist`.`" + SCHEDULE_TABLE_NAME + "` (`party_id`, `time_id`) VALUES (?, ?)");
	    pstmt.setInt(1, pty.getId());
	    
	    PreparedStatement timeIdQuery = connection.prepareStatement("SELECT `" + TIME_SLOT_TABLE_NAME + "`.`id` FROM `" + TIME_SLOT_TABLE_NAME + "` WHERE `time` = ? LIMIT 1;");
	    timeIdQuery.setString(1, time.format(TIME_FORMAT));
	    System.out.println("[DBAssistant]: Get time id query: " + timeIdQuery.toString());
	    ResultSet timeIdResult = timeIdQuery.executeQuery();
	    if (!timeIdResult.next()) {
		System.err.println(TIME_FORMAT);
	    }
	    
	    pstmt.setInt(2, timeIdResult.getInt(1));
	    
	    System.out.println("[DBAssistant]: Schedule party query: " + pstmt);
	    pstmt.executeUpdate();
	} catch (SQLException ex) {
	    Logger.getLogger(DBAssistant.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    public void updateParty(Party pty) {
	if (!pty.isRegistered()) {
	    registerParty(pty);
	} else {
	    try {
		if (connection != null) {
		    PreparedStatement pstmt = connection.prepareStatement("UPDATE `parties` SET `name`=?, `size`=?, `admitted`=?, \n"
									+ "`payment_type_id` = (SELECT `id` FROM `payment_types` WHERE `description`=? LIMIT 1), \n"
									+ "`rate_id` = (SELECT `id` FROM `rates` WHERE `rate`=? LIMIT 1), \n"
									+ "description = ? \n"
									+ "WHERE `parties`.`id`=?");
		    
		    pstmt.setString(1, pty.getName());
		    pstmt.setInt(2, pty.getSize());
		    pstmt.setInt(3, (pty.isAdmitted() ? 1 : 0));
		    pstmt.setString(4, pty.getPaymentType().toString());
		    pstmt.setString(5, RATE_FORMAT.format(pty.getRate()));
		    pstmt.setString(6, pty.getHint());
		    pstmt.setInt(7, pty.getId());
		    System.out.println("[DBAssistant]: Update party query: \"" + pstmt.toString() + "\"");
		    int rows = pstmt.executeUpdate();
		    System.out.println("[DBAssistant]: Updated party query updated rows: " + rows);
		}
	    } catch (SQLException ex) {
		Logger.getLogger(DBAssistant.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }
    
    public void clearSchedule() {
	try {
	    Statement st = connection.createStatement();
	    connection.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
	    st.executeUpdate("TRUNCATE TABLE `" + PARTY_TABLE_NAME + "`;");
	    st.executeUpdate("TRUNCATE TABLE `" + SCHEDULE_TABLE_NAME + "`;");
	    connection.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
	} catch (SQLException ex) {
	    System.err.println("[DBAssistant]: Error clearing party registry: " + ex.getMessage());
	}
    }
    
    public void unregisterParty(Party pty) {
	
    }
    
    public void removeParty(Party pty, LocalTime[] times) {
	System.out.println("[DBAssistant]: Removing " + pty + " from times " + Arrays.toString(times) + "...");
	if (!pty.isRegistered()) {
	    System.err.println("[DBAssistant]: Error removing party " + pty.getName() + ". Party is not registered.");
	    return;
	}
	try {
	    if (isConnected()) {
		String timeSet = "(";
		for (int i = 0; i < times.length - 1; i++) {
		    timeSet += "'" + times[i].format(TIME_FORMAT) + "', ";
		}
		timeSet += "'" + times[times.length - 1].format(TIME_FORMAT) + "')";
		
		System.out.println("[DBAssistant]: removeParty() > String timeSet = \"" + timeSet + "\"");
		
		PreparedStatement pstmt = connection.prepareStatement("DELETE FROM `" + SCHEDULE_TABLE_NAME + "` WHERE `party_id` = ? AND `time_id` IN (SELECT `id` FROM `" + TIME_SLOT_TABLE_NAME + "` WHERE `time` IN " + timeSet + ");");
		pstmt.setInt(1, pty.getId());
		System.out.println("[DBAssistant]: removeParty() > Query: " + pstmt);
		int rows = pstmt.executeUpdate();
		System.out.println("[DBAssistant]: removeParty() > Affected rows: " + rows);
		
	    }
	} catch (SQLException ex) {
	    System.err.println("[DBAssistant]: Remove party error: " + ex.getMessage());
	}
    }
    
    public void clearGroup(Group group) {
	LocalTime time = group.getTime();
	System.out.println("[DBAssistant]: Clearing group for time " + time + "...");
	if (isConnected()) {
	    try {
		PreparedStatement pstmt = connection.prepareStatement("DELETE FROM `" + SCHEDULE_TABLE_NAME + "` WHERE `time_id` IN (SELECT `id` FROM `" + TIME_SLOT_TABLE_NAME + "` WHERE `time` = ?)");
		pstmt.setString(1, TIME_FORMAT.format(time));
		System.out.println("[DBAssistant]: Clear group query: " + pstmt);
		int rows = pstmt.executeUpdate();
		System.out.println("[DBAssistant]: Clear group affected rows: " + rows);
	    } catch (SQLException ex) {
		System.err.println("[DBAssistant]: Error clearing group: " + ex.getMessage());
	    }
	}
    }
    
    public Connection getConnection() {
	return connection;
    }
    
    public void closeConnection() {
	try {
	    if (connection != null) {
		connection.close();
		connection = null;
	    }
	} catch (SQLException e) {
	    System.out.println("[DBAssistant]: Error closing connection. " + e.getMessage());
	}
    }
    
    @Override
    public String toString() {
	return "DBAssistant[" + dbName + "@" + host + ":" + port + ", " + username + ", " + password + "]";
    }
    
    public boolean isConnected() {
	return connection != null;
    }

    public String getHost() {
	return host;
    }

    public void setHost(String host) {
	this.host = host;
    }

    public String getPort() {
	return port;
    }

    public void setPort(String port) {
	this.port = port;
    }

    public String getDbName() {
	return dbName;
    }

    public void setDbName(String dbName) {
	this.dbName = dbName;
    }

    public String getUsername() {
	return username;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }
    
}
