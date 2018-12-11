/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kronoze.lasertagwaitlist;

import java.awt.event.ItemEvent;
import java.text.ParseException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Ryan
 */
public class AddParty extends javax.swing.JFrame {
    
    private final LaserTagWaitlist sourceWindow;
    private final Waitlist waitlist;
    private final ArrayList<LocalTime> times;

    /**
     * Creates new form AddParty
     * @param sourceWindow
     * @param waitlist
     * @param times
     */
    public AddParty(LaserTagWaitlist sourceWindow, Waitlist waitlist, ArrayList<LocalTime> times) {
	
	initComponents(); // This must be called first in the constructor
	
	this.getRootPane().setDefaultButton(addPartyButton);
	
	this.sourceWindow = sourceWindow;
	this.waitlist = waitlist;
	this.times = times;
	
	System.out.println(times.size());
	
	int maxGroupSize = Integer.parseInt(LaserTagWaitlist.getDBA().loadSetting("game_capacity"));
	
	if (times.size() > 1) {
	    timeLabel.setText("TEST");
	    timeLabel.setText(LaserTagWaitlist.formatTime(times.get(0)) + " - " + LaserTagWaitlist.formatTime(times.get(times.size() - 1)));
	} else if (times.size() == 1) {
	    System.out.println(timeLabel.getName() + ", " + times + ", " + times.get(0));
	    timeLabel.setText(LaserTagWaitlist.formatTime(times.get(0)));
	}
	
	
	Integer[] allowedSizes = new Integer[maxGroupSize];
	for (int i = 0; i < maxGroupSize; i++) {
	    allowedSizes[i] = i + 1;
	}
	partySizeInput.setModel(new javax.swing.DefaultComboBoxModel(allowedSizes));
	
	partyRateInput.setSelectedItem(LaserTagWaitlist.CURRENCY_FORMAT.format(LaserTagWaitlist.getCurrentRate()));
	
	
	
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        partyNameInput = new javax.swing.JTextField();
        partySizeInput = new javax.swing.JComboBox<>();
        partyAdmissionTypeInput = new javax.swing.JComboBox<>();
        partyAdmittedInput = new javax.swing.JToggleButton();
        partyRateInput = new javax.swing.JComboBox<>();
        timeLabel = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        addPartyButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Add Party");
        setResizable(false);

        partySizeInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                partySizeInputActionPerformed(evt);
            }
        });

        partyAdmissionTypeInput.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tender", "Packages", "Mixed", "Tab", "Free" }));

        partyAdmittedInput.setText("No");
        partyAdmittedInput.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                partyAdmittedInputItemStateChanged(evt);
            }
        });

        partyRateInput.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "$4.00", "$4.99", "$6.99" }));

        timeLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        timeLabel.setText("09:00 AM");

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        addPartyButton.setText("Add Party");
        addPartyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPartyButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Name");

        jLabel2.setText("Size");

        jLabel3.setText("Payment Type");

        jLabel4.setText("Admitted");

        jLabel5.setText("Rate");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGap(6, 6, 6)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addPartyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(timeLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(partyNameInput, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(partySizeInput, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(partyAdmissionTypeInput, javax.swing.GroupLayout.Alignment.TRAILING, 0, 290, Short.MAX_VALUE)
                    .addComponent(partyAdmittedInput, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(partyRateInput, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(timeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(partyNameInput, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(partySizeInput, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(partyAdmissionTypeInput, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(partyAdmittedInput, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(partyRateInput, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addPartyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void partySizeInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_partySizeInputActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_partySizeInputActionPerformed

    private void partyAdmittedInputItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_partyAdmittedInputItemStateChanged
        // TODO add your handling code here:
        switch (evt.getStateChange()) {
            case ItemEvent.SELECTED:
            partyAdmittedInput.setText("Yes");
            break;
            case ItemEvent.DESELECTED:
            partyAdmittedInput.setText("No");
            break;
        }
    }//GEN-LAST:event_partyAdmittedInputItemStateChanged

    private void addPartyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPartyButtonActionPerformed
        String partyName = partyNameInput.getText().trim();
	if (!partyName.equals("")) {
	    int partySize = Integer.parseInt(partySizeInput.getSelectedItem().toString());
	    Party party = new Party(partyName, partySize, times);
	    System.out.println("[AddParty]: String retrieved from combobox for AdmissionType: " + partyAdmissionTypeInput.getSelectedItem().toString());
	    party.setPaymentType(PaymentType.valueOf(partyAdmissionTypeInput.getSelectedItem().toString()));
	    party.setAdmitted(partyAdmittedInput.isSelected());
	    try {
		party.setRate(LaserTagWaitlist.CURRENCY_FORMAT.parse(partyRateInput.getSelectedItem().toString()).floatValue());
	    } catch (ParseException e) {
		System.err.println("[AddParty]: Failed to parse rate into float (" + partyRateInput.getSelectedItem().toString() + "): " + e.getMessage());
	    }
	    party.register();
	    for (LocalTime time : times) {
		try {
		    if (waitlist.getGroup(time).size() + party.size() > Integer.parseInt(LaserTagWaitlist.getDBA().loadSetting("game_capacity"))) {
			JOptionPane.showMessageDialog(this, "Failed to schedule " + party.getName() + " for " + LaserTagWaitlist.formatTime(time) + ". Group size exceeds game capacity.", "Error", JOptionPane.ERROR_MESSAGE);
		    } else {
			waitlist.addParty(time, party);
			LaserTagWaitlist.getDBA().scheduleParty(party, time);
		    }
		    
		} catch (Exception ex) {
		    Logger.getLogger(AddParty.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }
	    this.dispose();
	    sourceWindow.renderWaitlist();
	    sourceWindow.renderPartyTable(sourceWindow.getWaitlist().getGroup(times.get(0)));
	} else {
	    JOptionPane.showMessageDialog(this, "Please enter a valid party name.", "Error", JOptionPane.ERROR_MESSAGE);
	}
    }//GEN-LAST:event_addPartyButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addPartyButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JComboBox<String> partyAdmissionTypeInput;
    private javax.swing.JToggleButton partyAdmittedInput;
    private javax.swing.JTextField partyNameInput;
    private javax.swing.JComboBox<String> partyRateInput;
    private javax.swing.JComboBox<String> partySizeInput;
    private javax.swing.JLabel timeLabel;
    // End of variables declaration//GEN-END:variables
}