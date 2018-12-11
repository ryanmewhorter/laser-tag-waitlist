/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kronoze.lasertagwaitlist;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author Ryan
 */
public class Group extends ArrayList<Party> {
    
    private LocalTime time;
    
    // add party
    // remove party
   
    
    private final ArrayList<GroupListener> listeners = new ArrayList<>();
    
    public Group() {
	super();
    }
    
    public Group(LocalTime time) {
	this.time = time;
    }
    
    public Group(ArrayList<Party> group) {
	super(group);
    }
    
    public void addListener(GroupListener listener) {
	listeners.add(listener);
	for (Party pty : this) {
	    pty.addListener(new PartyListener() {
		@Override
		public void partyChanged(Party party) {
		    listener.groupChanged(Group.this);
		}

		@Override
		public void partyNameChanged(Party party) {
		    listener.groupChanged(Group.this);
		}

		@Override
		public void partySizeChanged(Party party) {
		    listener.groupChanged(Group.this);
		}
		
	    });
	}
    }
    
    public ArrayList<GroupListener> getListeners() {
	return listeners;
    }
    
    public void removeListener(GroupListener listener) {
	listeners.remove(listener);
    }
    
    @Override
    public boolean add(Party party) {
	boolean s = super.add(party);
	Iterator<GroupListener> iterator = listeners.iterator();
	while (iterator.hasNext()) {
	    GroupListener l = iterator.next();
	    l.groupChanged(this);
	}
	return s;
    }
    
    public boolean isAdmitted() {
	if (isEmpty()) {
	    return false;
	} else {
	    for (Party pty : this) {
		if (!pty.isAdmitted()) {
		    return false;
		}
	    }
	    return true;
	}
    }
    
    @Override
    public int size() {
	int size = 0;
	size = this.stream().map((pty) -> pty.size()).reduce(size, Integer::sum);
	return size;
    }
    
    public int numberOfParties() {
	return super.size();
    }
    
    @Override
    public void clear() {
	System.out.println("[Group]: " + getTime() + " group clearing itself...");
	LaserTagWaitlist.getDBA().clearGroup(this);
        Iterator<Party> pIter = this.iterator();
	while (pIter.hasNext()) {
	    Party pty = pIter.next();
	    pIter.remove();
//	    super.remove(pty); // Not sure if this is needed? I think pIter.remove() does the job
	}
	Iterator<GroupListener> iterator = listeners.iterator();
	while (iterator.hasNext()) {
	    GroupListener l = iterator.next();
	    l.groupChanged(this);
	}
    }
    
    public boolean remove(Party pty) {
	System.out.println("[Group]: " + getTime() + " group removing " + pty + "...");
	LaserTagWaitlist.getDBA().removeParty(pty, new LocalTime[]{time});
	boolean b = super.remove(pty);
	Iterator<GroupListener> glIter = listeners.iterator();
	while (glIter.hasNext()) {
	    GroupListener gl = glIter.next();
	    gl.groupChanged(this);
	}
	return b;
    }

    public LocalTime getTime() {
	return time;
    }

    public void setTime(LocalTime time) {
	this.time = time;
    }
    
    public String toHtmlString() {
	String result = "<html>";
	for (Party pty : this) {
	    result += pty.getName() + "<br>";
	}
	result += "</html>";
	return result;
    }
    
}
