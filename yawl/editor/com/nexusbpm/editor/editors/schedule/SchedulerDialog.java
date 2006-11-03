/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.editor.editors.schedule;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import com.toedter.calendar.JSpinnerDateEditor;

public class SchedulerDialog extends JDialog {

	private static final String SAMPLE_CRON_STRING = "0 0,15,30,45 3,7,22 26,27,28,29 1,2,3,4,5,9,10,11,12 2,3,5,6";  //  @jve:decl-index=0:
	private static final String DEFAULT_CRON_STRING = "0 0 0 ? 1,2,3,4,5,6,7,8,9,10,11,12 2,3,4,5,6";  //  @jve:decl-index=0:

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

	private JList hoursList = null;

	private JPanel minutePanel = null;

	private JPanel hourPanel = null;

	private JPanel jPanel = null;

	private JPanel jPanel1 = null;

	private JPanel jPanel2 = null;

	private JPanel buttonPanel = null;

	private JButton okButton = null;

	private JButton cancelButton = null;

	private JTextField uriTextField = null;

	private JLabel jLabel = null;

	private JScrollPane hourScrollPane = null;

	private JScrollPane minuteScrollPane = null;

	private DefaultListModel hoursModel = null; 

	private DefaultListModel minutesModel = null; 

	private DefaultListModel daysOfMonthModel = null;  //  @jve:decl-index=0:visual-constraint="489,64"

	private String cronExpression;  //  @jve:decl-index=0:
	private JPanel startAndEndDates = null;
	private JPanel startAndEndPanel = null;
	private JCheckBox useStartDateCheckBox = null;
	private JCheckBox useEndDateCheckBox = null;
	private JSpinnerDateEditor startSpinnerDateEditor = null;
	private JSpinnerDateEditor endSpinnerDateEditor = null;
	private boolean cancelled = false;
	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	/**
	 * @param owner
	 */
	public SchedulerDialog( Frame owner, ScheduleInformation info) {
		super( owner );
		this.cronExpression = info.getCronExpression() == null ? DEFAULT_CRON_STRING : info.getCronExpression();
		initialize();
		this.uriTextField.setText(info.getUri() == null ? "" : info.getUri());
		if (info.getStartDate() != null) {
			this.startSpinnerDateEditor.setDate(info.getStartDate());
			this.useStartDateCheckBox.setSelected(true);
		}
		else {
			this.startSpinnerDateEditor.setEnabled(false);
			this.startSpinnerDateEditor.setDate(new Date());
		}
		if (info.getEndDate() != null) {
			this.endSpinnerDateEditor.setDate(info.getEndDate());
			this.useEndDateCheckBox.setSelected(true);
		}
		else {
			this.endSpinnerDateEditor.setEnabled(false);
			this.endSpinnerDateEditor.setDate(new Date());
		}
		ScheduleMarshaller.getInstance().unmarshal(this.cronExpression, this);
	}

	public static ScheduleInformation showSchedulerDialog(Frame parent, ScheduleInformation info) {
		SchedulerDialog dialog = new SchedulerDialog(parent, info == null ? new ScheduleInformation(null, null, null, null) : info);
		dialog.setVisible(true);
		dialog.dispose();
		Date startDate = dialog.useStartDateCheckBox.isSelected() ? dialog.startSpinnerDateEditor.getDate() : null; 
		Date endDate = dialog.useEndDateCheckBox.isSelected() ? dialog.endSpinnerDateEditor.getDate() : null;
		if( dialog.cancelled ) {
			return null;
		}
		else {
			return new ScheduleInformation(dialog.getUriTextField().getText(), dialog.getCronExpression(), startDate, endDate);
		}
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(465, 318);
		this.setTitle("Specification Scheduler");
		this.setModal(true);
		this.setBackground(SystemColor.control);
		this.setContentPane( getJContentPane() );
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				SchedulerDialog.this.setVisible(false);
			}
		});
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if( jContentPane == null ) {
			GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
			gridBagConstraints33.gridx = 0;
			gridBagConstraints33.ipadx = 0;
			gridBagConstraints33.weightx = 1.0;
			gridBagConstraints33.weighty = 1.0;
			gridBagConstraints33.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints33.fill = GridBagConstraints.BOTH;
			gridBagConstraints33.anchor = GridBagConstraints.EAST;
			gridBagConstraints33.gridy = 1;
			GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
			gridBagConstraints32.fill = GridBagConstraints.BOTH;
			gridBagConstraints32.gridy = 0;
			gridBagConstraints32.ipadx = 62;
			gridBagConstraints32.ipady = 2;
			gridBagConstraints32.weightx = 1.0;
			gridBagConstraints32.weighty = 1.0;
			gridBagConstraints32.gridx = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.setBackground(SystemColor.control);
			jContentPane.add(getJTabbedPane(), gridBagConstraints32);
			jContentPane.add(getButtonPanel(), gridBagConstraints33);
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
			jTabbedPane.setPreferredSize(new Dimension(395, 250));
			jTabbedPane.setBackground(SystemColor.control);
			jTabbedPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			jTabbedPane.addTab("Start and End ", null, getStartAndEndDates(), null);
			jTabbedPane.addTab("Time", null, getTime(), null);
			jTabbedPane.addTab("Days", null, getDays(), null);
			jTabbedPane.addTab("Months", null, getMonth(), null);
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
			time.setBackground(SystemColor.control);
			time.add(getMinutePanel(), gridBagConstraints111);
			time.add(getHourPanel(), gridBagConstraints121);
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
			days.setBackground(SystemColor.control);
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
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.fill = GridBagConstraints.BOTH;
			gridBagConstraints31.ipadx = 1;
			gridBagConstraints31.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints31.weighty = 1.0;
			month = new JPanel();
			month.setLayout(new GridBagLayout());
			month.setBackground(SystemColor.control);
			month.add(getJPanel2(), gridBagConstraints31);
		}
		return month;
	}

	/**
	 * This method initializes minutesList	
	 * 	
	 * @return javax.swing.JList	
	 */
	public JList getMinutesList() {
		if( minutesList == null ) {
			minutesList = new JList();
			minutesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			minutesList.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
			minutesModel = new DefaultListModel();
			minutesList.setModel(minutesModel);
			minutesList.setCellRenderer(new AlternatingListCellRenderer(TextRenderer.getMinutelyInstance()));
			Calendar c = new GregorianCalendar();
			for (int i = 0; i < 60; i = i + 5) {
				c.set(0, 0, 0, 0, i);
				minutesModel.addElement(c.getTime());
			}
		}
		return minutesList;
	}

	/**
	 * This method initializes January	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getJanuary() {
		if( January == null ) {
			January = new JCheckBox();
			January.setText("January");
			January.setBackground(SystemColor.control);
			January.setSelected(true);
		}
		return January;
	}

	/**
	 * This method initializes February	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getFebruary() {
		if( February == null ) {
			February = new JCheckBox();
			February.setText("February");
			February.setBackground(SystemColor.control);
			February.setSelected(true);
		}
		return February;
	}

	/**
	 * This method initializes March	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getMarch() {
		if( March == null ) {
			March = new JCheckBox();
			March.setText("March");
			March.setBackground(SystemColor.control);
			March.setSelected(true);
		}
		return March;
	}

	/**
	 * This method initializes April	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getApril() {
		if( April == null ) {
			April = new JCheckBox();
			April.setText("April");
			April.setBackground(SystemColor.control);
			April.setSelected(true);
		}
		return April;
	}

	/**
	 * This method initializes May	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getMay() {
		if( May == null ) {
			May = new JCheckBox();
			May.setText("May");
			May.setBackground(SystemColor.control);
			May.setSelected(true);
		}
		return May;
	}

	/**
	 * This method initializes June	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getJune() {
		if( June == null ) {
			June = new JCheckBox();
			June.setText("June");
			June.setBackground(SystemColor.control);
			June.setSelected(true);
		}
		return June;
	}

	/**
	 * This method initializes July	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getJuly() {
		if( July == null ) {
			July = new JCheckBox();
			July.setText("July");
			July.setBackground(SystemColor.control);
			July.setSelected(true);
		}
		return July;
	}

	/**
	 * This method initializes August	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getAugust() {
		if( August == null ) {
			August = new JCheckBox();
			August.setText("August");
			August.setBackground(SystemColor.control);
			August.setSelected(true);
		}
		return August;
	}

	/**
	 * This method initializes September	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getSeptember() {
		if( September == null ) {
			September = new JCheckBox();
			September.setText("September");
			September.setBackground(SystemColor.control);
			September.setSelected(true);
		}
		return September;
	}

	/**
	 * This method initializes October	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getOctober() {
		if( October == null ) {
			October = new JCheckBox();
			October.setText("October");
			October.setBackground(SystemColor.control);
			October.setSelected(true);
		}
		return October;
	}

	/**
	 * This method initializes November	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getNovember() {
		if( November == null ) {
			November = new JCheckBox();
			November.setText("November");
			November.setBackground(SystemColor.control);
			November.setSelected(true);
		}
		return November;
	}

	/**
	 * This method initializes December	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getDecember() {
		if( December == null ) {
			December = new JCheckBox();
			December.setText("December");
			December.setBackground(SystemColor.control);
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
			daysOfMonth.setBackground(SystemColor.control);
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
			daysOfWeek.setBackground(SystemColor.control);
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
	public JCheckBox getSunday() {
		if( Sunday == null ) {
			Sunday = new JCheckBox();
			Sunday.setText("Sunday");
			Sunday.setBackground(SystemColor.control);
		}
		return Sunday;
	}

	/**
	 * This method initializes Monday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getMonday() {
		if( Monday == null ) {
			Monday = new JCheckBox();
			Monday.setText("Monday");
			Monday.setBackground(SystemColor.control);
			Monday.setSelected(true);
		}
		return Monday;
	}

	/**
	 * This method initializes Tuesday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getTuesday() {
		if( Tuesday == null ) {
			Tuesday = new JCheckBox();
			Tuesday.setText("Tuesday");
			Tuesday.setBackground(SystemColor.control);
			Tuesday.setSelected(true);
		}
		return Tuesday;
	}

	/**
	 * This method initializes Wednesday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getWednesday() {
		if( Wednesday == null ) {
			Wednesday = new JCheckBox();
			Wednesday.setText("Wednesday");
			Wednesday.setBackground(SystemColor.control);
			Wednesday.setSelected(true);
		}
		return Wednesday;
	}

	/**
	 * This method initializes Thursday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getThursday() {
		if( Thursday == null ) {
			Thursday = new JCheckBox();
			Thursday.setText("Thursday");
			Thursday.setBackground(SystemColor.control);
			Thursday.setSelected(true);
		}
		return Thursday;
	}

	/**
	 * This method initializes Friday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getFriday() {
		if( Friday == null ) {
			Friday = new JCheckBox();
			Friday.setText("Friday");
			Friday.setBackground(SystemColor.control);
			Friday.setSelected(true);
		}
		return Friday;
	}

	/**
	 * This method initializes Saturday	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getSaturday() {
		if( Saturday == null ) {
			Saturday = new JCheckBox();
			Saturday.setText("Saturday");
			Saturday.setBackground(SystemColor.control);
		}
		return Saturday;
	}

	/**
	 * This method initializes daysOfMonthList	
	 * 	
	 * @return javax.swing.JList	
	 */
	public JList getDaysOfMonthList() {
		if( daysOfMonthList == null ) {
		    daysOfMonthModel = new DefaultListModel();
		    daysOfMonthList = new JList();
		    daysOfMonthList.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		    daysOfMonthList.setToolTipText("Choose as many days as needed or Last or Last Weekday");
		    daysOfMonthList.setModel(daysOfMonthModel);
		    daysOfMonthList.setCellRenderer(new AlternatingListCellRenderer(TextRenderer.getDailyInstance()));
			Object[] values = new Object[]
			{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,
			16,17,18,19,20,21,22,23,24,25,26,27,
			28,29,30,31,"L","LW"};
			for (Object o: values) {
				daysOfMonthModel.addElement(o);
			}
			daysOfMonthList.setSelectionModel(new DefaultListSelectionModel() {
				@Override
				public void addSelectionInterval(int index0, int index1) {
					boolean found = false;
					for (int i = Math.min(index0, index1); i < 1 + Math.max(index0, index1); i++) {
						Object o = SchedulerDialog.this.getDaysOfMonthModel().getElementAt(i);
						if (o instanceof String) {
							super.clearSelection();
							super.addSelectionInterval(i, i);
							found = true;
							break;							
						} 
					}
					if (!found) super.addSelectionInterval(index0, index1);
				}
				@Override
				public void setSelectionInterval(int index0, int index1) {
					boolean found = false;
					for (int i = Math.min(index0, index1); i < 1 + Math.max(index0, index1); i++) {
						Object o = SchedulerDialog.this.getDaysOfMonthModel().getElementAt(i);
						if (o instanceof String) {
							super.clearSelection();
							super.setSelectionInterval(i, i);
							found = true;
							break;							
						} 
					}
					if (!found) super.setSelectionInterval(index0, index1);
				}
			});
			daysOfMonthList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
			spacer.setBackground(SystemColor.control);
		}
		return spacer;
	}

	/**
	 * This method initializes hoursList	
	 * 	
	 * @return javax.swing.JList	
	 */
	public JList getHoursList() {
		if( hoursList == null ) {
			hoursList = new JList();
			hoursList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			hoursList.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
			hoursModel = new DefaultListModel();
			hoursList.setModel(hoursModel);
			hoursList.setCellRenderer(new AlternatingListCellRenderer(TextRenderer.getHourlyInstance()));
			Calendar c = new GregorianCalendar();
			for (int i = 0; i < 24; i++) {
				c.set(0, 0, 0, i, 0);
				hoursModel.addElement(c.getTime());
			}
		}
		return hoursList;
	}

	/**
	 * This method initializes minutePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getMinutePanel() {
		if( minutePanel == null ) {
			GridBagConstraints gridBagConstraints110 = new GridBagConstraints();
			gridBagConstraints110.fill = GridBagConstraints.BOTH;
			gridBagConstraints110.weighty = 1.0;
			gridBagConstraints110.weightx = 1.0;
			minutePanel = new JPanel();
			minutePanel.setLayout(new GridBagLayout());
			minutePanel.setBorder(BorderFactory.createTitledBorder(null, "Minutes", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			minutePanel.setBackground(SystemColor.control);
			minutePanel.add(getMinuteScrollPane(), gridBagConstraints110);
		}
		return minutePanel;
	}

	/**
	 * This method initializes hourPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getHourPanel() {
		if( hourPanel == null ) {
			GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
			gridBagConstraints27.fill = GridBagConstraints.BOTH;
			gridBagConstraints27.weighty = 1.0;
			gridBagConstraints27.weightx = 1.0;
			hourPanel = new JPanel();
			hourPanel.setLayout(new GridBagLayout());
			hourPanel.setBorder(BorderFactory.createTitledBorder(null, "Hours", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			hourPanel.setBackground(SystemColor.control);
			hourPanel.add(getHourScrollPane(), gridBagConstraints27);
		}
		return hourPanel;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 5;
			gridBagConstraints5.ipadx = 0;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.weighty = 1.0;
			gridBagConstraints5.insets = new Insets(0, 20, 0, 10);
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridy = 4;
			gridBagConstraints12.ipadx = 0;
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.fill = GridBagConstraints.BOTH;
			gridBagConstraints12.weighty = 1.0;
			gridBagConstraints12.insets = new Insets(0, 20, 0, 10);
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 3;
			gridBagConstraints4.ipadx = 0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.fill = GridBagConstraints.BOTH;
			gridBagConstraints4.weighty = 1.0;
			gridBagConstraints4.insets = new Insets(0, 20, 0, 10);
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 2;
			gridBagConstraints3.ipadx = 0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.fill = GridBagConstraints.BOTH;
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.insets = new Insets(0, 20, 0, 10);
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.insets = new Insets(0, 20, 0, 10);
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.ipadx = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.insets = new Insets(0, 20, 0, 10);
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setBackground(SystemColor.control);
			jPanel.add(getJanuary(), gridBagConstraints1);
			jPanel.add(getFebruary(), gridBagConstraints2);
			jPanel.add(getMarch(), gridBagConstraints3);
			jPanel.add(getApril(), gridBagConstraints4);
			jPanel.add(getMay(), gridBagConstraints12);
			jPanel.add(getJune(), gridBagConstraints5);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 5;
			gridBagConstraints11.ipadx = 0;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.weighty = 1.0;
			gridBagConstraints11.fill = GridBagConstraints.BOTH;
			gridBagConstraints11.insets = new Insets(0, 20, 0, 10);
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.gridy = 4;
			gridBagConstraints10.ipadx = 0;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.weighty = 1.0;
			gridBagConstraints10.fill = GridBagConstraints.BOTH;
			gridBagConstraints10.insets = new Insets(0, 20, 0, 10);
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.gridy = 3;
			gridBagConstraints9.ipadx = 0;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.weighty = 1.0;
			gridBagConstraints9.fill = GridBagConstraints.BOTH;
			gridBagConstraints9.insets = new Insets(0, 20, 0, 10);
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridy = 2;
			gridBagConstraints8.ipadx = 0;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.weighty = 1.0;
			gridBagConstraints8.fill = GridBagConstraints.BOTH;
			gridBagConstraints8.insets = new Insets(0, 20, 0, 10);
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridy = 1;
			gridBagConstraints7.ipadx = 0;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.weighty = 1.0;
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.insets = new Insets(0, 20, 0, 10);
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.ipadx = 0;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.insets = new Insets(0, 20, 0, 10);
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.setBackground(SystemColor.control);
			jPanel1.add(getJuly(), gridBagConstraints6);
			jPanel1.add(getAugust(), gridBagConstraints7);
			jPanel1.add(getSeptember(), gridBagConstraints8);
			jPanel1.add(getOctober(), gridBagConstraints9);
			jPanel1.add(getNovember(), gridBagConstraints10);
			jPanel1.add(getDecember(), gridBagConstraints11);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
			gridBagConstraints30.gridx = -1;
			gridBagConstraints30.weightx = 1.0;
			gridBagConstraints30.weighty = 1.0;
			gridBagConstraints30.fill = GridBagConstraints.BOTH;
			gridBagConstraints30.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints30.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints30.gridy = -1;
			GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
			gridBagConstraints29.gridx = -1;
			gridBagConstraints29.weightx = 1.0;
			gridBagConstraints29.weighty = 1.0;
			gridBagConstraints29.fill = GridBagConstraints.BOTH;
			gridBagConstraints29.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints29.gridy = -1;
			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
			jPanel2.setBorder(BorderFactory.createTitledBorder(null, "Months of the Year", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanel2.setBackground(SystemColor.control);
			jPanel2.add(getJPanel(), gridBagConstraints29);
			jPanel2.add(getJPanel1(), gridBagConstraints30);
		}
		return jPanel2;
	}

	/**
	 * This method initializes buttonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.insets = new Insets(1, 0, 0, 10);
			jLabel = new JLabel();
			jLabel.setText("Specification");
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.fill = GridBagConstraints.BOTH;
			gridBagConstraints24.weighty = 1.0;
			gridBagConstraints24.gridx = 1;
			gridBagConstraints24.gridy = 0;
			gridBagConstraints24.weightx = 10.0;
			GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
			gridBagConstraints35.weightx = 1.0;
			gridBagConstraints35.gridx = 3;
			gridBagConstraints35.gridy = 0;
			gridBagConstraints35.ipadx = 0;
			gridBagConstraints35.ipady = 0;
			gridBagConstraints35.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints35.anchor = GridBagConstraints.EAST;
			gridBagConstraints35.weighty = 1.0;
			GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
			gridBagConstraints34.weightx = 1.0;
			gridBagConstraints34.gridx = 2;
			gridBagConstraints34.gridy = 0;
			gridBagConstraints34.ipadx = 0;
			gridBagConstraints34.ipady = 0;
			gridBagConstraints34.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints34.fill = GridBagConstraints.BOTH;
			gridBagConstraints34.anchor = GridBagConstraints.EAST;
			gridBagConstraints34.weighty = 1.0;
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridBagLayout());
			buttonPanel.setBackground(SystemColor.control);
			buttonPanel.add(jLabel, gridBagConstraints25);
			buttonPanel.add(getUriTextField(), gridBagConstraints24);
			buttonPanel.add(getOkButton(), gridBagConstraints34);
			buttonPanel.add(getCancelButton(), gridBagConstraints35);
		}
		return buttonPanel;
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText("OK");
			okButton.setPreferredSize(new Dimension(73, 26));
			okButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cronExpression = ScheduleMarshaller.getInstance().marshal(SchedulerDialog.this);
					SchedulerDialog.this.setVisible(false);
				}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText("Cancel");
			cancelButton.setMnemonic(KeyEvent.VK_UNDEFINED);
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					cancelled = true;
					SchedulerDialog.this.setVisible(false);
				}
			});
		}
		return cancelButton;
	}

	/**
	 * This method initializes uriTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	public JTextField getUriTextField() {
		if (uriTextField == null) {
			uriTextField = new JTextField();
			uriTextField.setText("");
			uriTextField.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		}
		return uriTextField;
	}

	/**
	 * This method initializes hourScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getHourScrollPane() {
		if (hourScrollPane == null) {
			hourScrollPane = new JScrollPane();
			hourScrollPane.setViewportView(getHoursList());
		}
		return hourScrollPane;
	}

	/**
	 * This method initializes minuteScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getMinuteScrollPane() {
		if (minuteScrollPane == null) {
			minuteScrollPane = new JScrollPane();
			minuteScrollPane.setViewportView(getMinutesList());
		}
		return minuteScrollPane;
	}

	/**
	 * This method initializes startAndEndDates	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getStartAndEndDates() {
		if (startAndEndDates == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints.insets = new Insets(10, 10, 10, 10);
			gridBagConstraints.gridy = 0;
			startAndEndDates = new JPanel();
			startAndEndDates.setLayout(new GridBagLayout());
			startAndEndDates.setBackground(SystemColor.control);
			startAndEndDates.add(getStartAndEndPanel(), gridBagConstraints);
		}
		return startAndEndDates;
	}

	/**
	 * This method initializes startAndEndPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getStartAndEndPanel() {
		if (startAndEndPanel == null) {
			GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
			gridBagConstraints28.gridx = 1;
			gridBagConstraints28.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints28.anchor = GridBagConstraints.WEST;
			gridBagConstraints28.weightx = 1.0;
			gridBagConstraints28.weighty = 1.0;
			gridBagConstraints28.gridy = 1;
			GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
			gridBagConstraints26.gridx = 1;
			gridBagConstraints26.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints26.anchor = GridBagConstraints.WEST;
			gridBagConstraints26.weightx = 1.0;
			gridBagConstraints26.weighty = 1.0;
			gridBagConstraints26.gridy = 0;
			GridBagConstraints gridBagConstraints37 = new GridBagConstraints();
			gridBagConstraints37.gridx = 0;
			gridBagConstraints37.anchor = GridBagConstraints.WEST;
			gridBagConstraints37.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints37.weightx = 0.0;
			gridBagConstraints37.weighty = 1.0;
			gridBagConstraints37.gridy = 1;
			GridBagConstraints gridBagConstraints36 = new GridBagConstraints();
			gridBagConstraints36.gridx = 0;
			gridBagConstraints36.anchor = GridBagConstraints.WEST;
			gridBagConstraints36.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints36.weightx = 0.0;
			gridBagConstraints36.weighty = 1.0;
			gridBagConstraints36.gridy = 0;
			startAndEndPanel = new JPanel();
			startAndEndPanel.setLayout(new GridBagLayout());
			startAndEndPanel.setBackground(SystemColor.control);
			startAndEndPanel.setBorder(BorderFactory.createTitledBorder(null, "Select Start and End Dates", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			startAndEndPanel.add(getStartSpinnerDateEditor(), gridBagConstraints26);
			startAndEndPanel.add(getEndSpinnerDateEditor(), gridBagConstraints28);
			startAndEndPanel.add(getUseStartDateCheckBox(), gridBagConstraints36);
			startAndEndPanel.add(getUseEndDateCheckBox(), gridBagConstraints37);
		}
		return startAndEndPanel;
	}

	/**
	 * This method initializes useStartDateCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getUseStartDateCheckBox() {
		if (useStartDateCheckBox == null) {
			useStartDateCheckBox = new JCheckBox();
			useStartDateCheckBox.setText("Use start date");
			useStartDateCheckBox.setBackground(SystemColor.control);
			useStartDateCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SchedulerDialog.this.startSpinnerDateEditor.setEnabled(SchedulerDialog.this.useStartDateCheckBox.isSelected());
				}
			});
		}
		return useStartDateCheckBox;
	}

	/**
	 * This method initializes useEndDateCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getUseEndDateCheckBox() {
		if (useEndDateCheckBox == null) {
			useEndDateCheckBox = new JCheckBox();
			useEndDateCheckBox.setText("Use end date");
			useEndDateCheckBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SchedulerDialog.this.endSpinnerDateEditor.setEnabled(SchedulerDialog.this.useEndDateCheckBox.isSelected());
				}
			});
			useEndDateCheckBox.setBackground(SystemColor.control);
		}
		return useEndDateCheckBox;
	}

	/**
	 * This method initializes startSpinnerDateEditor	
	 * 	
	 * @return com.toedter.calendar.JSpinnerDateEditor	
	 */
	private JSpinnerDateEditor getStartSpinnerDateEditor() {
		if (startSpinnerDateEditor == null) {
			startSpinnerDateEditor = new JSpinnerDateEditor();
		}
		return startSpinnerDateEditor;
	}

	/**
	 * This method initializes endSpinnerDateEditor	
	 * 	
	 * @return com.toedter.calendar.JSpinnerDateEditor	
	 */
	private JSpinnerDateEditor getEndSpinnerDateEditor() {
		if (endSpinnerDateEditor == null) {
			endSpinnerDateEditor = new JSpinnerDateEditor();
		}
		return endSpinnerDateEditor;
	}

	public static void main( String[] args ) {
		ScheduleInformation s = SchedulerDialog.showSchedulerDialog(null, new ScheduleInformation("MakeRecordings", DEFAULT_CRON_STRING, new Date(), new Date()));
		System.out.println(s);
	}

	public DefaultListModel getDaysOfMonthModel() {
		return daysOfMonthModel;
	}

	public void setDaysOfMonthModel(DefaultListModel daysOfMonthModel) {
		this.daysOfMonthModel = daysOfMonthModel;
	}

	public DefaultListModel getHoursModel() {
		return hoursModel;
	}

	public void setHoursModel(DefaultListModel hoursModel) {
		this.hoursModel = hoursModel;
	}

	public DefaultListModel getMinutesModel() {
		return minutesModel;
	}

	public void setMinutesModel(DefaultListModel minutesModel) {
		this.minutesModel = minutesModel;
	}


}  //  @jve:decl-index=0:visual-constraint="10,10"
