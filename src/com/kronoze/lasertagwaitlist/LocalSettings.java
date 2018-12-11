/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kronoze.lasertagwaitlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ryan
 */
public class LocalSettings {
    
    protected static final String PROPERTY_FILE_NAME = "config.properties";
    
    private final Properties prop = new Properties();
    private OutputStream output = null;
    
    public LocalSettings() throws IOException {
	System.out.println("[LocalSettings]: Initializing...");
	if (!(new File(PROPERTY_FILE_NAME)).exists()) {
	    System.out.println("[LocalSettings]: File '" + PROPERTY_FILE_NAME + "' not found.");
	}
	prop.load(new FileInputStream(PROPERTY_FILE_NAME));
	System.out.println("[LocalSettings]: Loaded.");
    }
    
    public void save(String key, Object value) {
	System.out.println("[LocalSettings]: Save called on key '" + key  + "' and value '" + value.toString() + ".'");
	try {
	    output = new FileOutputStream(PROPERTY_FILE_NAME);
	    prop.setProperty(key, value.toString());
	    prop.store(output, null);
	    System.out.println("Saved " + key + "=" + value.toString());
	    update();
	} catch (IOException ex) {
	    System.err.println("Failed to save " + key + "='" + value.toString() + "': " + ex.getMessage());
	}
    }
    
    private void printContents() {
	try {
	    BufferedReader in = new BufferedReader(new FileReader(PROPERTY_FILE_NAME));
	    String line;
	    while ((line = in.readLine()) != null) {
		System.out.println(line);
	    }
	} catch (FileNotFoundException ex) {
	    Logger.getLogger(LocalSettings.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IOException ex) {
	    Logger.getLogger(LocalSettings.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    public String load(String key) {
//	System.out.println("[LocalSettings]: Load called on key '" + key + ".'");
	InputStream inStream = null;
	Properties p = new Properties();
	try {
	    inStream = new FileInputStream(PROPERTY_FILE_NAME);
//	    printContents();
	    p.load(inStream);
	    String value = p.getProperty(key);
	    System.out.println("[LocalSettings]: Loaded value for key '" + key + "': " + p.getProperty(key));
	    return value;
	} catch (IOException ex) {
	    System.err.println("Failed to load settings from property file: " + ex.getMessage());
	    return null;
	} finally {
	    if (inStream != null) {
		try {
		    inStream.close();
		} catch (IOException ex) {
		    System.err.println("Failed to close settings file input stream: " + ex.getMessage());
		}
	    }
	}
    }
    
    public String load(String key, Object def) {
	String value = load(key);
	if (value == null) {
//	    save(key, def.toString());
	    return def.toString();
	} else {
	    return value;
	}
    }
    
    private void update() {
	OutputStream outStream = null;
	try {
	    outStream = new FileOutputStream(PROPERTY_FILE_NAME);
	    prop.store(outStream, null);
	    System.out.println("[LocalSettings]: " + prop);
	    System.out.println("[LocalSettings]: Update successful.");
	} catch (IOException e) {
	    System.err.println("Failed to locate property file '" + PROPERTY_FILE_NAME + "'");
	} finally {
	    if (outStream != null) {
		try {
		    outStream.close();
		} catch (IOException ex) {
		    System.err.println("Failed to close settings file output stream: " + ex.getMessage());
		}
	    }
	}
    }
    
}
