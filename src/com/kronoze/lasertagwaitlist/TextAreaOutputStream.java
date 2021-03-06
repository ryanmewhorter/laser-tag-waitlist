/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kronoze.lasertagwaitlist;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;

/**
 *
 * @author Ryan
 */
public class TextAreaOutputStream extends OutputStream {

    private final JTextArea textArea;
    private final int maxLines;
    
    public TextAreaOutputStream(JTextArea textArea, int maxLines) {
	this.textArea = textArea;
	this.maxLines = maxLines;
    }
    
    public TextAreaOutputStream(JTextArea textArea) {
	this(textArea, 60);
    }
    
    @Override
    public void write(int b) throws IOException {
	java.awt.EventQueue.invokeLater(() -> {
//	    if (textArea.getLineCount() > maxLines) {
//		textArea.replaceRange("", 0, 50);
//	    }
	    // redirects data to the text area
	    textArea.setText(textArea.getText() +  String.valueOf((char) b));
	    // scrolls the text area to the end of data
	    textArea.setCaretPosition(textArea.getDocument().getLength());
	    // keeps the textArea up to date
	    textArea.update(textArea.getGraphics());
	});
    }
}
