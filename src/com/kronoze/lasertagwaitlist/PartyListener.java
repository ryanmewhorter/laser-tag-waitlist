/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kronoze.lasertagwaitlist;

/**
 *
 * @author Ryan
 */
public interface PartyListener {
    
    void partyChanged(Party party);
    
    void partyNameChanged(Party party);
    
    void partySizeChanged(Party party);
    
}
