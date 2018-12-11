/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kronoze.lasertagwaitlist;

import java.time.LocalTime;
import java.util.ArrayList;

/**
 *
 * @author Ryan
 */
public class Party {
    
    private int id = -1;
    private String name;
    private int size;
    private String hint = "";
    private PaymentType paymentType;
    private boolean admitted;
    private float rate;
    
    private ArrayList<LocalTime> scheduledTimes;
    private final ArrayList<PartyListener> listeners = new ArrayList<>();
    
    
    public Party(String name, int size, boolean admitted, PaymentType admissionType, String hint, float rate) {
	this.name = name;
	this.size = size;
	this.admitted = admitted;
	this.paymentType = admissionType;
	this.hint = hint;
	this.rate = rate;

    }
    
    public Party(String name, int size, ArrayList<LocalTime> scheduledTimes) {
	this.name = name;
	this.size = size;
	this.scheduledTimes = scheduledTimes;
    }
    
    public void register() {
	DBAssistant dba = LaserTagWaitlist.getDBA();
	if (dba.isConnected()) {
	    this.id = dba.registerParty(this);
	    if (this.id == -1) {
		System.err.println("[Party]: Failed to retrieve valid ID for " + this + ".");
	    }
	}
    }
    
    public void addListener(PartyListener listener) {
	listeners.add(listener);
    }
    
    public void removeListener(PartyListener listener) {
	listeners.remove(listener);
    }
    
    public boolean isAdmitted() {
	return admitted;
    }
    
    public boolean isRegistered() {
	return id != -1;
    }

    public void setAdmitted(boolean admitted) {
	this.admitted = admitted;
	listeners.stream().forEach((l) -> {
	    l.partyChanged(this);
	});
    }

    public PaymentType getPaymentType() {
	return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
	this.paymentType = paymentType;
	listeners.stream().forEach((l) -> {
	    l.partyChanged(this);
	});
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	if (!this.name.equals(name)) {
	    listeners.stream().forEach((l) -> {
		l.partyChanged(this);
	    });
	}
	this.name = name;
    }

    public int getSize() {
	return size;
    }
    
    public int size() { 
	return getSize();
    }

    public void setSize(int size) {
	if (!(this.size == size)) {
	    listeners.stream().forEach((l) -> {
		l.partyChanged(this);
	    });
	}
	this.size = size;
    }
    
    public String getHint() {
	return hint;
    }
    
    public void setHint(String hint) {
	this.hint = hint;
	listeners.stream().forEach((l) -> {
	    l.partyChanged(this);
	});
    }

    public ArrayList<LocalTime> getScheduledTimes() {
	return scheduledTimes;
    }

    public void setScheduledTimes(ArrayList<LocalTime> scheduledTimes) {
	this.scheduledTimes = scheduledTimes;
    }
    
    @Override
    public String toString() {
	return getName();
//	return "Party \"" + this.name + "\" [" + this.size + ", " + this.admitted + ", " + this.admissionType + ", " + this.rate + "]";
    }

    public float getRate() {
	return rate;
    }

    public void setRate(float rate) {
	this.rate = rate;
	listeners.stream().forEach((l) -> {
	    l.partyChanged(this);
	});
    }

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }
    
}
