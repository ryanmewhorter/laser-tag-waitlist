/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kronoze.lasertagwaitlist;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Ryan
 */
public class OrderFocusTraversalPolicy extends java.awt.FocusTraversalPolicy {

    private final ArrayList<Component> order;
    
    public OrderFocusTraversalPolicy(Component... components) {
	
	order = new ArrayList<>(components.length);
	order.addAll(Arrays.asList(components));
	System.out.println("[OrderFocusTraversalPolicy]: New OrderFocusTraversalPolicy of size " + order.size() + "...");
    }
    
    @Override
    public Component getComponentAfter(Container aContainer, Component aComponent) {
	System.out.println("[OrderFocusTraversalPolicy]: OFTP.getComponentAfter...");
	int i = order.indexOf(aComponent) + 1;
	if (i > order.size() - 1)
	    i = 0;
	System.out.println("getComponentAfter > " + i);
	return order.get(i);
    }

    @Override
    public Component getComponentBefore(Container aContainer, Component aComponent) {
	System.out.println("[OrderFocusTraversalPolicy]: OFTP.getComponentBefre...");
	int i = order.indexOf(aComponent) - 1;
	if (i < 0)
	    i = order.size() - 1;
	return order.get(i);
    }

    @Override
    public Component getFirstComponent(Container aContainer) {
	return order.get(0);
    }

    @Override
    public Component getLastComponent(Container aContainer) {
	return order.get(order.size() - 1);
    }

    @Override
    public Component getDefaultComponent(Container aContainer) {
	return this.getFirstComponent(aContainer);
    }
    
    @Override
    public String toString() {
	return this.getClass().getName() + " [" + order.size() + "]";
    }
    
}
