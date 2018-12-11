/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kronoze.lasertagwaitlist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author Ryan
 */
public class Waitlist extends TreeMap<LocalTime, Group> implements GroupListener {
    
    private static final LocalTime OPEN = LaserTagWaitlist.parseTime(LaserTagWaitlist.getDBA().loadSetting("open_time"));
    private static final LocalTime CLOSE = LaserTagWaitlist.parseTime(LaserTagWaitlist.getDBA().loadSetting("close_time"));
    private static final int INCREMENT = Integer.parseInt(LaserTagWaitlist.getDBA().loadSetting("time_slot_increment"));
    
    private Connection db;
    
    public Waitlist() {
	super();
	int closeHour = (CLOSE.getHour() == 0) ? 24 : CLOSE.getHour();
	LocalTime nextSlot = OPEN;
	int nextHour = (nextSlot.getHour() == 0) ? 24 : nextSlot.getHour();
	while (nextHour < closeHour) {
	    Group group = new Group(nextSlot);
	    group.addListener(this);
	    this.put(nextSlot, new Group(nextSlot));
	    nextSlot = nextSlot.plusMinutes(INCREMENT);
	    nextHour = (nextSlot.getHour() == 0) ? 24 : nextSlot.getHour();
	}
    }
    
    public Waitlist(Connection db) {
	this.db = db;
	
	int closeHour = (CLOSE.getHour() == 0) ? 24 : CLOSE.getHour();
	LocalTime nextSlot = OPEN;
	int nextHour = (nextSlot.getHour() == 0) ? 24 : nextSlot.getHour();
	while (nextHour < closeHour) {
	    Group group = new Group(nextSlot);
	    group.addListener(this);
	    this.put(nextSlot, new Group(nextSlot));
	    nextSlot = nextSlot.plusMinutes(INCREMENT);
	    nextHour = (nextSlot.getHour() == 0) ? 24 : nextSlot.getHour();
	}
    }
    
    // Only adds party if connected to server now
    public void addParty(LocalTime time, Party pty) {
	DBAssistant dba = LaserTagWaitlist.getDBA();
	if (dba.isConnected()) {
	    if (this.containsKey(time)) {
		getGroup(time).add(pty);
	    } else {
		System.err.println("Time " + time + " doesn't exist in the waitlist. Skipping " + pty.getName() + ".");
	    }
	}
    }
    
    public Group getGroup(LocalTime time) {
	Group g = this.get(time);
	if (g == null) {
	    System.err.println("Time " + time + " doesn't exist in this waitlist.");
	    return null;
	} else {
	    return g;
	}
    }

    @Override
    public void groupChanged(Group group) {
	System.out.println("Group at " + group.getTime() + " changed");
    }
    
    public boolean isConnected() {
	return db != null;
    }
    
    public void disconnect() {
	db = null;
    }
    
}
