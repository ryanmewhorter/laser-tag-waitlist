/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kronoze.lasertagwaitlist;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Ryan Mewhorter
 */
public class LaserTagWaitlist extends JFrame implements GroupListener, PartyListener {
    
    private Waitlist waitlist;
    private static LocalSettings settings;
    private static DBAssistant dba;
    
    private Map<LocalTime, Integer> timeRowMap = new TreeMap<>();
    private LocalTime currentGroupTime = null;
    
    public static final int ROW_HEIGHT = 19;
    public static final String START_MYSQL_LOC = "H:/xampp/mysql_start.bat";
    
    private static final int PORT = 5142;
    private static ServerSocket socket;
    
    public static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm a");
    
    public static float TAX_RATE;
    
    /**
     * Creates new form LaserTagWaitlist
     */
    public LaserTagWaitlist() {
	initComponents();
	
	PrintStream output = new PrintStream(new TextAreaOutputStream(outputField));
	
//	System.setOut(output);
//	System.setErr(output);
	
	try {
	    settings = new LocalSettings(); 
//	    System.out.println("Would normally initialize settings here...");
	    try {
		socket = new ServerSocket(PORT, 0, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
	    } catch (BindException e) {
		JOptionPane.showMessageDialog(null, "Application is already running.", "Error", JOptionPane.INFORMATION_MESSAGE);
		this.dispose();
		System.exit(0);
	    } catch (IOException e) {
		System.err.println("Error: " + e.getMessage());
		System.exit(0);
	    }
	    
	} catch (IOException e) {
	    System.err.println("Cannot find settings file. " + e.getMessage());
	    System.out.println("Cannot find settings file. " + e.getMessage());
	    JOptionPane.showMessageDialog(null, "Failed to load settings file.\nPath: " + LocalSettings.PROPERTY_FILE_NAME, "Fatal Error", JOptionPane.ERROR_MESSAGE);
	}
	
	System.out.println("Attempting to connect to server...");
	dba = new DBAssistant();
	
	if (dba.isConnected()) {
	    System.out.println("Connected to server.");
	    try {
		System.out.println("Loading waitlist...");
		this.waitlist = dba.getWaitlist();
		System.out.println("Waitlist loaded successfully.");
	    } catch (Exception ex) {
		Logger.getLogger(LaserTagWaitlist.class.getName()).log(Level.SEVERE, null, ex);
	    }
	} else {
	    JOptionPane.showMessageDialog(null, "Failed to connect to MySQL server.\n" + dba.getHost() + ":" + dba.getPort(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
	    System.exit(0);
	}
	
	TAX_RATE = Float.parseFloat(dba.loadSetting("tax_rate"));
	
	groupTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
	    int[] selectedRows = groupTable.getSelectedRows();
	    if (selectedRows.length == 1) {
		try {
		    LocalTime firstSelectedTime = parseTime(groupTable.getValueAt(selectedRows[0], 0).toString());
		    renderPartyTable(waitlist.getGroup(firstSelectedTime));
		    long time = LocalTime.now().until(firstSelectedTime, java.time.temporal.ChronoUnit.SECONDS);
		    int hours = (int) Math.floor(time / 3600);
		    int minutes = (int) Math.floor((time - (hours * 3600)) / 60);
		    String text = (Math.abs(hours) > 0) ? Math.abs(hours) + "h " + Math.abs(minutes) + "m " : Math.abs(minutes) + "m ";
		    timeUntilSelectedGameLabel.setText(((time >= 0) ? "+ " : "- ") + text);
		} catch (Exception ex) {
		    Logger.getLogger(LaserTagWaitlist.class.getName()).log(Level.SEVERE, null, ex);
		    System.out.println(ex.getMessage());
		}
//		this.waitlist = dba.getWaitlist();
//		renderWaitlist();
	    }
	});	
	
	ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
	ses.scheduleAtFixedRate(() -> {
	    LocalTime nextAvailableTimeSlot = this.getNextAvailableTimeSlot().getValue();
	    long time = LocalTime.now().until(nextAvailableTimeSlot, java.time.temporal.ChronoUnit.SECONDS);
	    int hours = (int) Math.floor(time / 3600);
	    int minutes = (int) Math.floor((time - (hours * 3600)) / 60);
	    int seconds = (int) Math.floor((time - (hours * 3600 + minutes * 60)));
	    String text = (hours > 0) ? hours + "h " + minutes + "m " + seconds + "s " : minutes + "m " + seconds + "s ";
	    text += "until next game @ " + formatTime(nextAvailableTimeSlot);
	    timeUntilNextGameLabel.setText(text);
	}, 0, 1, java.util.concurrent.TimeUnit.SECONDS);
	
	System.out.println("Rendering waitlist...");
	renderWaitlist();
	
    }
    
    public static LocalTime parseTime(String time) {
	return LocalTime.parse(time, DateTimeFormatter.ofPattern(TIME_FORMAT.toPattern()));
    }
    
    public static String formatTime(LocalTime time) {
	try {
	    SimpleDateFormat f1 = new SimpleDateFormat("HH:mm"); //HH for hour of the day (0 - 23)
	    return TIME_FORMAT.format(f1.parse(time.toString()));
	} catch (ParseException ex) {
	    System.out.println("Failed to format time " + time.toString() + ". " + ex.getMessage());
	    Logger.getLogger(LaserTagWaitlist.class.getName()).log(Level.SEVERE, null, ex);
	    return null;
	}
    }
    
    public Entry<Integer, LocalTime> getNextAvailableTimeSlot() {
	LocalTime now = LocalTime.now();
//	System.out.println("now = " + formatTime(now));
	LocalTime nextTime = null;
	Integer selectionIndex = 0;
	int i = 0;
	if (waitlist.keySet().size() > 0) {
	    for (LocalTime time : waitlist.keySet()) {
		if (nextTime == null) {
		    nextTime = time;
		    selectionIndex = i;
		    
		} 
		if (time.equals(now) || time.isAfter(now)) {
		    nextTime = time;
		    selectionIndex = i;
		    break;
		}
//		System.out.println("i = " + i + ", time = " + formatTime(time) + ", nextTime = " + formatTime(nextTime) + ", selectionIndex = " + selectionIndex);
		i++;
	    }
	    return new SimpleEntry<>(selectionIndex, nextTime);
	} else {
	    System.err.println("getNextAvailableTimeSlot error: Waitlist key set not populated.");
	    return new SimpleEntry<>(-1, LocalTime.of(0, 0));
	}
    }
    
    public static double getCurrentRate() {
	Calendar calendar = Calendar.getInstance();
	int day = calendar.get(Calendar.DAY_OF_WEEK);
	int hour = LocalTime.now().getHour();
	if (day > 1 && day < 7 && hour >= 9 && hour < 17) {
	    return 4.99;
	} else {
	    return 6.99;
	}
    }
    
    public final void renderWaitlistRow(int row, LocalTime time, Group group) {
//	System.out.println("Rendering waitlist row (row=" + row + ", size=" + group.size() + ", time=" + time +", group=" + group + ")");
	groupTable.setValueAt(formatTime(time), row, 0); // TIME_FORMAT.format(time.toString()), row, 0); // display time
	groupTable.setValueAt(group.isAdmitted(), row, 3); // display group admitted
	groupTable.setValueAt(group.size(), row, 1);
	String htmlOut = group.toHtmlString();
	groupTable.setValueAt(htmlOut, row, 2);
	groupTable.setRowHeight(row, Math.max(group.numberOfParties() * ROW_HEIGHT, ROW_HEIGHT));
    }
    
    public final void renderWaitlist() {
	DefaultTableModel groupTableModel = (DefaultTableModel) groupTable.getModel();
	groupTableModel.setRowCount(waitlist.size());
	int i = 0;
	for (Entry<LocalTime, Group> entry : waitlist.entrySet()) {
	    LocalTime time = entry.getKey();
	    Group group = entry.getValue();
	    if (!group.getListeners().contains(this))
		group.addListener(this); // connect main class as listener
	    timeRowMap.put(time, i);
	    renderWaitlistRow(i, time, group);
	    i++;
	}
	groupTable.setModel(groupTableModel);
	System.out.println("Rendered waitlist.");
    }
    
    public final void renderPartyTable(Group group) {
	this.currentGroupTime = group.getTime();
	
	selectedTimeLabel.setText(formatTime(currentGroupTime));
	DefaultTableModel partyTableModel = (DefaultTableModel) partyTable.getModel();
	partyTableModel.setRowCount(group.numberOfParties());
	int i = 0;
	for (Party party : group) {
	    partyTableModel.setValueAt(party.size(), i, 0);
	    partyTableModel.setValueAt(party, i, 1);
	    partyTableModel.setValueAt(party.getPaymentType().toString(), i, 2);
	    partyTableModel.setValueAt(party.isAdmitted(), i, 3);
	    String due;
	    
	    if (!party.isAdmitted()) {
		switch (party.getPaymentType()) {
		    case Tender: {
			Number amt = party.getSize() * round(party.getRate() * (1 + TAX_RATE), 2);
			System.out.println("Amt due for " + party + ", " + party.getSize() + ", " + party.getRate() + " is " + amt);
			due = CURRENCY_FORMAT.format(amt);
			break;
		    }
		    case Packages:
			due = party.size() + " swipes";
			break;
		    case Free:
			due = CURRENCY_FORMAT.format(0);
			break;
		    default:
			due = "--";
			break;
		}
	    } else {
		switch (party.getPaymentType()) {
		    case Tender:
		    case Free:
			due = CURRENCY_FORMAT.format(0);
			break;
		    default:
			due = "--";
			break;
		}
	    }
	    	    
	    partyTableModel.setValueAt(due, i, 4);
	    i++;
	}
	System.out.println("Rendered party table for " + group.getTime() + ".");
    }
    
    public final void renderPartyTable() {
	this.currentGroupTime = null;
	selectedTimeLabel.setText("- - : - -");
	DefaultTableModel partyTableModel = (DefaultTableModel) partyTable.getModel();
	partyTableModel.setRowCount(0);
    }
    
    private double round(double n, double d) {
	return Math.floor((n * Math.pow(10, d)) + .5) / Math.pow(10, d);
    }
    
    private double round(float n, float d) {
	return Math.floor((n * Math.pow(10, d)) + .5) / Math.pow(10, d);
    }
    
    @Override
    public void groupChanged(Group group) {
//	LocalTime time = group.getTime();
//	System.out.println("Group at " + time + " changed");
//	int r = timeRowMap.get(time);
//	renderWaitlistRow(r, time, group);
	renderWaitlist();
	System.out.println("Group at " + group.getTime() + " changed.");
    }
    
    @Override
    public void partyChanged(Party party) {
	System.out.println("Party '" + party + "' changed. Registered times: " + party.getScheduledTimes());
    }
    
    public Waitlist getWaitlist() {
	return waitlist;
    }
    
    public void setWaitlist(Waitlist waitlist) {
	this.waitlist = waitlist;
    }
    
    public static LocalSettings getLocalSettings() {
	return settings;
    }
    
    public static DBAssistant getDBA() {
	return dba;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        groupTable = new javax.swing.JTable();
        clearSelection = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        partyTable = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        outputField = new javax.swing.JTextArea();
        selectedTimeLabel = new javax.swing.JLabel();
        addParty = new javax.swing.JButton();
        clearWaitlist = new javax.swing.JButton();
        editParty = new javax.swing.JButton();
        clearGroup = new javax.swing.JButton();
        refreshWaitlist = new javax.swing.JButton();
        removeParty = new javax.swing.JButton();
        selectNextSlot = new javax.swing.JButton();
        timeUntilNextGameLabel = new javax.swing.JLabel();
        timeUntilSelectedGameLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        settingsMenu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Laser Tag Waitlist");
        setPreferredSize(new java.awt.Dimension(1180, 743));

        groupTable.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        groupTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Slot", "Size", "Group", "Admtd"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        groupTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        groupTable.setRowHeight(19);
        groupTable.getTableHeader().setResizingAllowed(false);
        groupTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(groupTable);
        if (groupTable.getColumnModel().getColumnCount() > 0) {
            groupTable.getColumnModel().getColumn(0).setPreferredWidth(70);
            groupTable.getColumnModel().getColumn(1).setPreferredWidth(45);
            groupTable.getColumnModel().getColumn(2).setPreferredWidth(288);
            groupTable.getColumnModel().getColumn(3).setPreferredWidth(45);
        }

        clearSelection.setText("Clear Selection");
        clearSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSelectionActionPerformed(evt);
            }
        });

        partyTable.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        partyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Size", "Name", "Payment Type", "Admitted", "Due"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        partyTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        partyTable.setRowHeight(20);
        partyTable.getTableHeader().setResizingAllowed(false);
        partyTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane2.setViewportView(partyTable);
        if (partyTable.getColumnModel().getColumnCount() > 0) {
            partyTable.getColumnModel().getColumn(0).setPreferredWidth(45);
            partyTable.getColumnModel().getColumn(1).setPreferredWidth(200);
            partyTable.getColumnModel().getColumn(2).setPreferredWidth(86);
            partyTable.getColumnModel().getColumn(3).setPreferredWidth(45);
        }

        outputField.setEditable(false);
        outputField.setColumns(20);
        outputField.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        outputField.setLineWrap(true);
        outputField.setRows(5);
        jScrollPane3.setViewportView(outputField);

        selectedTimeLabel.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        selectedTimeLabel.setText("- - : - -");
        selectedTimeLabel.setToolTipText("");

        addParty.setText("Add Party");
        addParty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPartyActionPerformed(evt);
            }
        });

        clearWaitlist.setText("Clear Waitlist");
        clearWaitlist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearWaitlistActionPerformed(evt);
            }
        });

        editParty.setText("Edit Party");
        editParty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editPartyActionPerformed(evt);
            }
        });

        clearGroup.setText("Clear Group");
        clearGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearGroupActionPerformed(evt);
            }
        });

        refreshWaitlist.setText("Refresh Waitlist");
        refreshWaitlist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshWaitlistActionPerformed(evt);
            }
        });

        removeParty.setText("Remove Party");
        removeParty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePartyActionPerformed(evt);
            }
        });

        selectNextSlot.setText("Select Next Time Slot");
        selectNextSlot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNextSlotActionPerformed(evt);
            }
        });

        timeUntilNextGameLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        timeUntilNextGameLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        timeUntilNextGameLabel.setText("0m 0s until next game @ 9:00 AM");
        timeUntilNextGameLabel.setToolTipText("");

        timeUntilSelectedGameLabel.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        timeUntilSelectedGameLabel.setText("+ 0m 0s");
        timeUntilSelectedGameLabel.setToolTipText("");

        jMenuBar1.setEnabled(false);

        jMenu1.setText("File");

        settingsMenu.setText("Settings");
        settingsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsMenuActionPerformed(evt);
            }
        });
        jMenu1.add(settingsMenu);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 469, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addParty, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(clearGroup, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(selectNextSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(editParty, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(removeParty, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(clearWaitlist, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(clearSelection, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(refreshWaitlist, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timeUntilNextGameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(selectedTimeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(timeUntilSelectedGameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE))
                .addGap(10, 10, 10))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addParty, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(11, 11, 11)
                                .addComponent(clearGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(selectNextSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(editParty, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(removeParty, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(clearWaitlist, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(selectedTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(timeUntilSelectedGameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 416, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(7, 7, 7)
                                .addComponent(timeUntilNextGameLabel)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(clearSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(refreshWaitlist, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE))
                            .addComponent(jScrollPane3)))
                    .addComponent(jScrollPane1))
                .addGap(20, 20, 20))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clearSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSelectionActionPerformed
        partyTable.clearSelection();
	groupTable.clearSelection();
	currentGroupTime = null;
	selectedTimeLabel.setText("- - : - -");
	timeUntilSelectedGameLabel.setText("");
    }//GEN-LAST:event_clearSelectionActionPerformed

    private void addPartyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPartyActionPerformed
	int[] selectedRows = groupTable.getSelectedRows();
	if (selectedRows.length == 0) {
	    JOptionPane.showMessageDialog(this, "Please select a time.", "Error", JOptionPane.INFORMATION_MESSAGE);
	}
	ArrayList<LocalTime> times = new ArrayList<>();
	for (int r : selectedRows) {
	    times.add(parseTime(groupTable.getValueAt(r, 0).toString()));
	}
	AddParty addPartyWindow = new AddParty(this, waitlist, times);
//	addPartyWindow.setFocusCycleRoot(true);
//	addPartyWindow.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
	addPartyWindow.setLocationRelativeTo(this);
	addPartyWindow.setVisible(true);
	addPartyWindow.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		try {
		    renderPartyTable(waitlist.getGroup(times.get(0)));
		} catch (Exception ex) {
		    Logger.getLogger(LaserTagWaitlist.class.getName()).log(Level.SEVERE, null, ex);
		    System.out.println(ex.getMessage());
		}
	    }
	});
	System.out.println("AddParty is Focus Cycle Root: " + addPartyWindow.isFocusCycleRoot());
	System.out.println("AddParty Focus Traversal Policy set: " + addPartyWindow.isFocusTraversalPolicySet());
	System.out.println("AddParty Focus Traversal Policy: " + addPartyWindow.getFocusTraversalPolicy());
	System.out.println("Add party is Focus Traversal Policy Provider: " + addPartyWindow.isFocusTraversalPolicyProvider());
	System.out.println("Opened AddParty window");
    }//GEN-LAST:event_addPartyActionPerformed

    private void settingsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsMenuActionPerformed
        // open settings window
	EditSettings editSettingsWindow = new EditSettings();
	editSettingsWindow.setLocationRelativeTo(this);
	editSettingsWindow.setVisible(true);
	System.out.println("Opened EditSettings window");
    }//GEN-LAST:event_settingsMenuActionPerformed

    private void clearWaitlistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearWaitlistActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the waitlist?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
	    this.waitlist = new Waitlist();
	    dba.clearSchedule();
	    renderWaitlist();
	    renderPartyTable();
	}
    }//GEN-LAST:event_clearWaitlistActionPerformed

    private void editPartyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editPartyActionPerformed
	int r = partyTable.getSelectedRow();
	if (r != -1) {
	    Party party = (Party) partyTable.getValueAt(partyTable.getSelectedRow(), 1);
	    EditParty2 editPartyWindow = new EditParty2(this, currentGroupTime, party);
	    editPartyWindow.setLocationRelativeTo(this);
	    editPartyWindow.setVisible(true);
	    System.out.println("Opened EditParty window");
	    editPartyWindow.addWindowListener(new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
		    
		}
	    });
	} else {
	    JOptionPane.showMessageDialog(this, "Please select a party to edit.", "Error", JOptionPane.ERROR_MESSAGE);
	}
    }//GEN-LAST:event_editPartyActionPerformed

    private void clearGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearGroupActionPerformed
        int[] rows = groupTable.getSelectedRows();
	for (int r : rows) {
	    LocalTime time = parseTime(groupTable.getValueAt(r, 0).toString());
	    Group group;
	    try {
		group = waitlist.getGroup(time);
		group.clear();
		renderWaitlistRow(r, time, group);
		renderPartyTable();
	    } catch (Exception ex) {
		Logger.getLogger(LaserTagWaitlist.class.getName()).log(Level.SEVERE, null, ex);
		System.out.println(ex.getMessage());
	    }
	}
    }//GEN-LAST:event_clearGroupActionPerformed

    private void refreshWaitlistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshWaitlistActionPerformed
        renderWaitlist();
    }//GEN-LAST:event_refreshWaitlistActionPerformed

    private void removePartyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePartyActionPerformed
        int r = partyTable.getSelectedRow();
	if (currentGroupTime != null && r != -1) {
	    Group group;
	    try {
		group = waitlist.getGroup(currentGroupTime);
		Party party = (Party) partyTable.getValueAt(partyTable.getSelectedRow(), 1);
		int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the party for '" + party.getName() + "?'", "Remove Party", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		switch (response) {
		case JOptionPane.YES_OPTION:
		    group.remove(party);
		    renderPartyTable(group);
		    break;
		case JOptionPane.NO_OPTION:
		    break;
		}
	    } catch (Exception ex) {
		Logger.getLogger(LaserTagWaitlist.class.getName()).log(Level.SEVERE, null, ex);
		System.out.println(ex.getMessage());
	    }
	    
	    
	} else {
	    JOptionPane.showMessageDialog(this, "Please select a party to remove.", "Error", JOptionPane.ERROR_MESSAGE);
	}
    }//GEN-LAST:event_removePartyActionPerformed

    private void selectNextSlotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectNextSlotActionPerformed
	Entry<Integer, LocalTime> nextSlot = this.getNextAvailableTimeSlot();
	System.out.println("Select next time slot > Time: " + nextSlot.getValue() + " (row " + nextSlot.getKey() + ")");
	groupTable.setRowSelectionInterval(nextSlot.getKey(), nextSlot.getKey());
    }//GEN-LAST:event_selectNextSlotActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	/* Set the Nimbus look and feel */
	//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
	/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
	 */
	try {
	    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
		if ("Nimbus".equals(info.getName())) {
		    javax.swing.UIManager.setLookAndFeel(info.getClassName());
		    break;
		}
	    }
	} catch (ClassNotFoundException ex) {
	    java.util.logging.Logger.getLogger(LaserTagWaitlist.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	} catch (InstantiationException ex) {
	    java.util.logging.Logger.getLogger(LaserTagWaitlist.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	} catch (IllegalAccessException ex) {
	    java.util.logging.Logger.getLogger(LaserTagWaitlist.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	} catch (javax.swing.UnsupportedLookAndFeelException ex) {
	    java.util.logging.Logger.getLogger(LaserTagWaitlist.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	//</editor-fold>

	/* Create and display the form */
	java.awt.EventQueue.invokeLater(() -> {
	    new LaserTagWaitlist().setVisible(true);
	});
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addParty;
    private javax.swing.JButton clearGroup;
    private javax.swing.JButton clearSelection;
    private javax.swing.JButton clearWaitlist;
    private javax.swing.JButton editParty;
    private javax.swing.JTable groupTable;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea outputField;
    private javax.swing.JTable partyTable;
    private javax.swing.JButton refreshWaitlist;
    private javax.swing.JButton removeParty;
    private javax.swing.JButton selectNextSlot;
    private javax.swing.JLabel selectedTimeLabel;
    private javax.swing.JMenuItem settingsMenu;
    private javax.swing.JLabel timeUntilNextGameLabel;
    private javax.swing.JLabel timeUntilSelectedGameLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void partyNameChanged(Party party) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void partySizeChanged(Party party) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
