package com.nexusbpm.editor.editors.schedule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import com.toedter.calendar.JSpinnerDateEditor;
import javax.swing.JSpinner;
import com.toedter.components.JSpinField;
import javax.swing.JButton;

public class SchedulerDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JTabbedPane jTabbedPane = null;

	private JPanel time = null;

	private JPanel days = null;

	private JPanel month = null;

	private JList minutesList = null;

	private JCheckBox January = null;

	private JCheckBox February = null;

	private JCheckBox March = null;

	private JCheckBox April = null;

	private JCheckBox May = null;

	private JCheckBox June = null;

	private JCheckBox July = null;

	private JCheckBox August = null;

	private JCheckBox September = null;

	private JCheckBox October = null;

	private JCheckBox November = null;

	private JCheckBox December = null;

	private JPanel daysOfMonth = null;

	private JPanel daysOfWeek = null;

	private JCheckBox Sunday = null;

	private JCheckBox Monday = null;

	private JCheckBox Tuesday = null;

	private JCheckBox Wednesday = null;

	private JCheckBox Thursday = null;

	private JCheckBox Friday = null;

	private JCheckBox Saturday = null;

	private JList daysOfMonthList = null;

	private JScrollPane daysOfMonthScroll = null;

	private JPanel spacer = null;

	private JSpinField jSpinField = null;

	private JButton jButton = null;

	private JList hoursList = null;

	private JSpinField jSpinField1 = null;

	private JButton jButton1 = null;

	private JPanel jPanel = null;

	private JPanel jPanel1 = null;

	/**
	 * @param owner
	 */
	public SchedulerDialog( Frame owner ) {
		super( owner );
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(465, 389);
		this.setContentPane( getJContentPane() );
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if( jContentPane == null ) {
			jContentPane = new JPanel();
			jContentPane.setLayout( new BorderLayout() );
			jContentPane.add(getJTabbedPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if( jTabbedPane == null ) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("time", null, getTime(), null);
			jTabbedPane.addTab("days", null, getDays(), null);
			jTabbedPane.addTab("months", null, getMonth(), null);
		}
		return jTabbedPane;
	}

	/**
	 * This method initializes time	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getTime() {
		if( time == null ) {
			GridBagConstraints gridBagConstraints121 = new GridBagConstraints();
			gridBagConstraints121.gridy = 0;
			gridBagConstraints121.weightx = 2.0;
			gridBagConstraints121.weighty = 2.0;
			gridBagConstraints121.fill = GridBagConstraints.BOTH;
			gridBagConstraints121.insets = new Insets(10, 10, 10, 5);
			gridBagConstraints121.gridx = 0;
			GridBagConstraints gridBagConstraints111 = new GridBagConstraints();
			gridBagConstraints111.gridx = 1;
			gridBagConstraints111.weightx = 2.0;
			gridBagConstraints111.weighty = 2.0;
			gridBagConstraints111.fill = GridBagConstraints.BOTH;
			gridBagConstraints111.insets = new Insets(10, 5, 10, 10);
			gridBagConstraints111.gridy = 0;
			time = new JPanel();
			time.setLayout(new GridBagLayout());
			time.add(getJPanel(), gridBagConstraints111);
			time.add(getJPanel1(), gridBagConstraints121);
		}
		return time;
	}

	/**
	 * This method initializes days	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDays() {
		if( days == null ) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.fill = GridBagConstraints.BOTH;
			gridBagConstraints21.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints21.weighty = 1.0;
			gridBagConstraints21.anchor = GridBagConstraints.WEST;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.weightx = 1.0;
			gridBagConstraints13.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints13.fill = GridBagConstraints.BOTH;
			gridBagConstraints13.weighty = 1.0;
			gridBagConstraints13.gridy = 0;
			days = new JPanel();
			days.setLayout(new GridBagLayout());
			days.add(getDaysOfWeek(), gridBagConstraints21);
			days.add(getDaysOfMonth(), gridBagConstraints13);
		}
		return days;
	}

	/**
	 * This method initializes month	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMonth() {
		if( month == null ) {
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.ipadx = 0;
			gridBagConstraints12.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints12.gridy = 4;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 1;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.ipadx = 0;
			gridBagConstraints11.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints11.gridy = 11;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.anchor = GridBagConstraints.WEST;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.ipadx = 0;
			gridBagConstraints10.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints10.gridy = 10;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.anchor = GridBagConstraints.WEST;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.ipadx = 0;
			gridBagConstraints9.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints9.gridy = 9;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.anchor = GridBagConstraints.WEST;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.ipadx = 0;
			gridBagConstraints8.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints8.gridy = 8;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.ipadx = 0;
			gridBagConstraints7.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints7.gridy = 7;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 1;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.ipadx = 0;
			gridBagConstraints6.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints6.gridy = 6;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.anchor = GridBagConstraints.WEST;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.ipadx = 0;
			gridBagConstraints5.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints5.gridy = 5;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.ipadx = 0;
			gridBagConstraints4.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints4.gridy = 3;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.ipadx = 0;
			gridBagConstraints3.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints3.gridy = 2;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints2.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.ipadx = 0;
			gridBagConstraints1.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints1.gridy = 0;
			month = new JPanel();
			month.setLayout(new GridBagLayout());
			month.add(getJanuary(), gridBagConstraints1);
			month.add(getFebruary(), gridBagConstraints2);
			month.add(getMarch(), gridBagConstraints3);
			month.add(getApril(), gridBagConstraints4);
			month.add(getMay(), gridBagConstraints12);
			month.add(getJune(), gridBagConstraints5);
			month.add(getJuly(), gridBagConstraints6);
			month.add(getAugust(), gridBagConstraints7);
			month.add(getSeptember(), gridBagConstraints8);
			month.add(getOctober(), gridBagConstraints9);
			month.add(getNovember(), gridBagConstraints10);
			month.add(getDecember(), gridBagConstraints11);
		}
		return month;
	}

	/**
	 * This method initializes minutesList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getMinutesList() {
		if( minutesList == null ) {
			minutesList = new JList();
			minutesList.setPreferredSize(new Dimension(150, 200));
		}
		return minutesList;
	}

	/**
	 * This method initializes January	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJanuary() {
		if( January == null ) {
			January = new JCheckBox();
			January.setText("January");
			January.setSelected(true);
		}
		return January;
	}

	/**
	 * This method initializes February	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getFebruary() {
		if( February == null ) {
			February = new JCheckBox();
			February.setText("February");
			February.setSelected(true);
		}
		return February;
	}

	/**
	 * This method initializes March	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getMarch() {
		if( March == null ) {
			March = new JCheckBox();
			March.setText("March");
			March.setSelected(true);
		}
		return March;
	}

	/**
	 * This method initializes April	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getApril() {
		if( April == null ) {
			April = new JCheckBox();
			April.setText("April");
			April.setSelected(true);
		}
		return April;
	}

	/**
	 * This method initializes May	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getMay() {
		if( May == null ) {
			May = new JCheckBox();
			May.setText("May");
			May.setSelected(true);
		}
		return May;
	}

	/**
	 * This method initializes June	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJune() {
		if( June == null ) {
			June = new JCheckBox();
			June.setText("June");
			June.setSelected(true);
		}
		return June;
	}

	/**
	 * This method initializes July	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJuly() {
		if( July == null ) {
			July = new JCheckBox();
			July.setText("July");
			July.setSelected(true);
		}
		return July;
	}

	/**
	 * This method initializes August	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getAugust() {
		if( August == null ) {
			August = new JCheckBox();
			August.setText("August");
			August.setSelected(true);
		}
		return August;
	}

	/**
	 * This method initializes September	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getSeptember() {
		if( September == null ) {
			September = new JCheckBox();
			September.setText("September");
			September.setSelected(true);
		}
		return September;
	}

	/**
	 * This method initializes October	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getOctober() {
		if( October == null ) {
			October = new JCheckBox();
			October.setText("October");
			October.setSelected(true);
		}
		return October;
	}

	/**
	 * This method initializes November	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getNovember() {
		if( November == null ) {
			November = new JCheckBox();
			November.setText("November");
			November.setSelected(true);
		}
		return November;
	}

	/**
	 * This method initializes December	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getDecember() {
		if( December == null ) {
			December = new JCheckBox();
			December.setText("December");
			December.setSelected(true);
		}
		return December;
	}

	/**
	 * This method initializes daysOfMonth	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDaysOfMonth() {
		if( daysOfMonth == null ) {
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.fill = GridBagConstraints.BOTH;
			gridBagConstraints23.weighty = 1.0;
			gridBagConstraints23.weightx = 1.0;
			daysOfMonth = new JPanel();
			daysOfMonth.setLayout(new GridBagLayout());
			daysOfMonth.setPreferredSize(new Dimension(100, 100));
			daysOfMonth.setBorder(BorderFactory.createTitledBorder(null, "Days of the Month", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			daysOfMonth.add(getDaysOfMonthScroll(), gridBagConstraints23);
		}
		return daysOfMonth;
	}

	/**
	 * This method initializes daysOfWeek	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDaysOfWeek() {
		if( daysOfWeek == null ) {
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 0;
			gridBagConstraints22.weighty = 1.0;
			gridBagConstraints22.gridy = 7;
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 0;
			gridBagConstraints20.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints20.weightx = 1.0;
			gridBagConstraints20.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints20.weighty = 0.0;
			gridBagConstraints20.gridy = 6;
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 0;
			gridBagConstraints19.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints19.weightx = 1.0;
			gridBagConstraints19.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints19.weighty = 0.0;
			gridBagConstraints19.gridy = 5;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 0;
			gridBagConstraints18.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints18.weightx = 1.0;
			gridBagConstraints18.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints18.weighty = 0.0;
			gridBagConstraints18.gridy = 4;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 0;
			gridBagConstraints17.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints17.weightx = 1.0;
			gridBagConstraints17.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints17.weighty = 0.0;
			gridBagConstraints17.gridy = 3;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 0;
			gridBagConstraints16.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints16.weightx = 1.0;
			gridBagConstraints16.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints16.weighty = 0.0;
			gridBagConstraints16.gridy = 2;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints15.weighty = 0.0;
			gridBagConstraints15.gridy = 1;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints14.weighty = 0.0;
			gridBagConstraints14.gridy = 0;
			daysOfWeek = new JPanel();
			daysOfWeek.setLayout(new GridBagLayout());
			daysOfWeek.setPreferredSize(new Dimension(100, 200));
			daysOfWeek.setBorder(BorderFactory.createTitledBorder(null, "Days of the Week", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			daysOfWeek.add(getSunday(), gridBagConstraints14);
			daysOfWeek.add(getMonday(), gridBagConstraints15);
			daysOfWeek.add(getTuesday(), gridBagConstraints16);
			daysOfWeek.add(getWednesday(), gridBagConstraints17);
			daysOfWeek.add(getThursday(), gridBagConstraints18);
			daysOfWeek.add(getFriday(), gridBagConstraints19);
			daysOfWeek.add(getSaturday(), gridBagConstraints20);
			daysOfWeek.add(getSpacer(), gridBagConstraints22);
		}
		return daysOfWeek;
	}

	/**
	 * This method initializes Sunday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getSunday() {
		if( Sunday == null ) {
			Sunday = new JCheckBox();
			Sunday.setText("Sunday");
		}
		return Sunday;
	}

	/**
	 * This method initializes Monday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getMonday() {
		if( Monday == null ) {
			Monday = new JCheckBox();
			Monday.setText("Monday");
			Monday.setSelected(true);
		}
		return Monday;
	}

	/**
	 * This method initializes Tuesday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getTuesday() {
		if( Tuesday == null ) {
			Tuesday = new JCheckBox();
			Tuesday.setText("Tuesday");
			Tuesday.setSelected(true);
		}
		return Tuesday;
	}

	/**
	 * This method initializes Wednesday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getWednesday() {
		if( Wednesday == null ) {
			Wednesday = new JCheckBox();
			Wednesday.setText("Wednesday");
			Wednesday.setSelected(true);
		}
		return Wednesday;
	}

	/**
	 * This method initializes Thursday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getThursday() {
		if( Thursday == null ) {
			Thursday = new JCheckBox();
			Thursday.setText("Thursday");
			Thursday.setSelected(true);
		}
		return Thursday;
	}

	/**
	 * This method initializes Friday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getFriday() {
		if( Friday == null ) {
			Friday = new JCheckBox();
			Friday.setText("Friday");
			Friday.setSelected(true);
		}
		return Friday;
	}

	/**
	 * This method initializes Saturday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getSaturday() {
		if( Saturday == null ) {
			Saturday = new JCheckBox();
			Saturday.setText("Saturday");
		}
		return Saturday;
	}

	/**
	 * This method initializes daysOfMonthList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getDaysOfMonthList() {
		if( daysOfMonthList == null ) {
			daysOfMonthList = new JList(new Object[]
			{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,
			16,17,18,19,20,21,22,23,24,25,26,27,
			28,29,30,31,"last","last weekday"});
			daysOfMonthList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			
		}
		return daysOfMonthList;
	}

	/**
	 * This method initializes daysOfMonthScroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getDaysOfMonthScroll() {
		if( daysOfMonthScroll == null ) {
			daysOfMonthScroll = new JScrollPane();
			daysOfMonthScroll.setFont(new Font("Dialog", Font.PLAIN, 12));
			daysOfMonthScroll.setViewportView(getDaysOfMonthList());
		}
		return daysOfMonthScroll;
	}

	/**
	 * This method initializes spacer	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSpacer() {
		if( spacer == null ) {
			spacer = new JPanel();
			spacer.setLayout(new GridBagLayout());
		}
		return spacer;
	}

	/**
	 * This method initializes jSpinField	
	 * 	
	 * @return com.toedter.components.JSpinField	
	 */
	private JSpinField getJSpinField() {
		if( jSpinField == null ) {
			jSpinField = new JSpinField();
			jSpinField.setMaximum(59);
			jSpinField.setMinimum(0);
		}
		return jSpinField;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if( jButton == null ) {
			jButton = new JButton();
			jButton.setText("add");
		}
		return jButton;
	}

	/**
	 * This method initializes hoursList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getHoursList() {
		if( hoursList == null ) {
			hoursList = new JList();
			hoursList.setPreferredSize(new Dimension(150, 200));
		}
		return hoursList;
	}

	/**
	 * This method initializes jSpinField1	
	 * 	
	 * @return com.toedter.components.JSpinField	
	 */
	private JSpinField getJSpinField1() {
		if( jSpinField1 == null ) {
			jSpinField1 = new JSpinField();
			jSpinField1.setMaximum(59);
			jSpinField1.setMinimum(0);
		}
		return jSpinField1;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if( jButton1 == null ) {
			jButton1 = new JButton();
			jButton1.setText("add");
		}
		return jButton1;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if( jPanel == null ) {
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.gridx = 1;
			gridBagConstraints25.anchor = GridBagConstraints.EAST;
			gridBagConstraints25.insets = new Insets(0, 0, 0, 10);
			gridBagConstraints25.gridy = 1;
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints24.gridy = 1;
			gridBagConstraints24.weightx = 1.0;
			gridBagConstraints24.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints24.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.CENTER;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 0.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setBorder(BorderFactory.createTitledBorder(null, "Minutes", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel.add(getMinutesList(), gridBagConstraints);
			jPanel.add(getJSpinField(), gridBagConstraints24);
			jPanel.add(getJButton(), gridBagConstraints25);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if( jPanel1 == null ) {
			GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
			gridBagConstraints28.gridx = 1;
			gridBagConstraints28.anchor = GridBagConstraints.EAST;
			gridBagConstraints28.insets = new Insets(0, 0, 0, 10);
			gridBagConstraints28.gridy = 1;
			GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
			gridBagConstraints27.gridx = 0;
			gridBagConstraints27.weightx = 1.0;
			gridBagConstraints27.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints27.anchor = GridBagConstraints.EAST;
			gridBagConstraints27.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints27.gridy = 1;
			GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
			gridBagConstraints26.fill = GridBagConstraints.BOTH;
			gridBagConstraints26.gridx = 0;
			gridBagConstraints26.gridy = 0;
			gridBagConstraints26.weightx = 1.0;
			gridBagConstraints26.weighty = 1.0;
			gridBagConstraints26.anchor = GridBagConstraints.WEST;
			gridBagConstraints26.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints26.gridwidth = 2;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.setBorder(BorderFactory.createTitledBorder(null, "Hours", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel1.add(getHoursList(), gridBagConstraints26);
			jPanel1.add(getJSpinField1(), gridBagConstraints27);
			jPanel1.add(getJButton1(), gridBagConstraints28);
		}
		return jPanel1;
	}

	public static void main( String[] args ) {
		SchedulerDialog d = new SchedulerDialog( null );
		d.setVisible( true );
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
