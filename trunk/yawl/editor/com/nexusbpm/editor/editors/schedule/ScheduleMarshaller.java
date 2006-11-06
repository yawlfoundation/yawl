/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors.schedule;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;

public class ScheduleMarshaller {

	private static ScheduleMarshaller INSTANCE;
	
	public static synchronized ScheduleMarshaller getInstance() {
		if (INSTANCE == null) INSTANCE = new ScheduleMarshaller();
		return INSTANCE;		
	}
	
	private ScheduleMarshaller() {}
	
	private String marshalMinutes(SchedulerDialog dialog) {
		String retval = "";
		Calendar c = new GregorianCalendar();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < dialog.getMinutesModel().getSize(); i++) {
			Date d = (Date) dialog.getMinutesModel().getElementAt(i);
			c.setTime(d);
			if (dialog.getMinutesList().getSelectionModel().isSelectedIndex(i)) {
				sb.append(c.get(Calendar.MINUTE));
				sb.append(",");
			}
		}
		if (sb.length() != 0) sb.setLength(sb.length() - 1);
		retval = sb.toString();
		if (retval.length() == 0) retval = "0";
		return retval;
	}
	
	private String marshalHours(SchedulerDialog dialog) {
		String retval = "";
		Calendar c = new GregorianCalendar();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < dialog.getHoursModel().getSize(); i++) {
			Date d = (Date) dialog.getHoursModel().getElementAt(i);
			c.setTime(d);
			if (dialog.getHoursList().getSelectionModel().isSelectedIndex(i)) {
				sb.append(c.get(Calendar.HOUR_OF_DAY));
				sb.append(",");
			}
		}
		if (sb.length() != 0) sb.setLength(sb.length() - 1);
		retval = sb.toString();
		if (retval.length() == 0) retval = "0";
		return retval;
	}

	private String marshalDayOfMonth(SchedulerDialog dialog) {
		String retval = "";
		Calendar c = new GregorianCalendar();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < dialog.getDaysOfMonthModel().getSize(); i++) {
			Object o = dialog.getDaysOfMonthModel().getElementAt(i);
			if (dialog.getDaysOfMonthList().getSelectionModel().isSelectedIndex(i)) {
				sb.append(o.toString());
				sb.append(",");
			}
		}
		if (sb.length() != 0) sb.setLength(sb.length() - 1);
		retval = sb.toString();
		if (retval.length() == 0) retval = "?";
		return retval;
	}

	private String marshalMonth(SchedulerDialog dialog) {
		String retval = "";
		StringBuilder sb = new StringBuilder();
		if (dialog.getJanuary().isSelected()) sb.append("1,");
		if (dialog.getFebruary().isSelected()) sb.append("2,");
		if (dialog.getMarch().isSelected()) sb.append("3,");
		if (dialog.getApril().isSelected()) sb.append("4,");
		if (dialog.getMay().isSelected()) sb.append("5,");
		if (dialog.getJune().isSelected()) sb.append("6,");
		if (dialog.getJuly().isSelected()) sb.append("7,");
		if (dialog.getAugust().isSelected()) sb.append("8,");
		if (dialog.getSeptember().isSelected()) sb.append("9,");
		if (dialog.getOctober().isSelected()) sb.append("10,");
		if (dialog.getNovember().isSelected()) sb.append("11,");
		if (dialog.getDecember().isSelected()) sb.append("12,");
		if (sb.length() > 0) sb.setLength(sb.length() - 1);
		retval = sb.toString();
		if (retval.length() == 0) retval = "*";
		return retval;
	}

	private String marshalDayOfWeek(SchedulerDialog dialog) {
		String retval = "";
		StringBuilder sb = new StringBuilder();
		if (dialog.getSunday().isSelected()) sb.append("1,");
		if (dialog.getMonday().isSelected()) sb.append("2,");
		if (dialog.getTuesday().isSelected()) sb.append("3,");
		if (dialog.getWednesday().isSelected()) sb.append("4,");
		if (dialog.getThursday().isSelected()) sb.append("5,");
		if (dialog.getFriday().isSelected()) sb.append("6,");
		if (dialog.getSaturday().isSelected()) sb.append("7,");
		if (sb.length() > 0) sb.setLength(sb.length() - 1);
		retval = sb.toString();
		if (retval.length() == 0) retval = "?";
		return retval;
	}
	
	public String marshal(SchedulerDialog dialog) {
		String retval = "";
		StringBuilder sb = new StringBuilder();
		sb.append("0 ");
		sb.append(marshalMinutes(dialog));
		sb.append(" ");
		sb.append(marshalHours(dialog));
		sb.append(" ");
		sb.append(marshalDayOfMonth(dialog));
		sb.append(" ");
		sb.append(marshalMonth(dialog));
		sb.append(" ");
		sb.append(marshalDayOfWeek(dialog));
		retval = sb.toString();
		return retval;
	}
	
	public void unmarshalMinutes(String minutes, SchedulerDialog dialog) {
		StringTokenizer st = new StringTokenizer(minutes, ",");
		dialog.getMinutesList().clearSelection();
		Calendar c = new GregorianCalendar();
		while (st.hasMoreTokens()) {
			String o = st.nextToken();
			for (int i = 0; i < dialog.getMinutesModel().size(); i++) {
				c.setTime((Date) dialog.getMinutesModel().elementAt(i));
				if (c.get(Calendar.MINUTE) == Integer.parseInt(o)) {
					dialog.getMinutesList().addSelectionInterval(i, i);
				}
			}
		}
	}

	public void unmarshalHours(String hours, SchedulerDialog dialog) {
		StringTokenizer st = new StringTokenizer(hours, ",");
		dialog.getHoursList().clearSelection();
		Calendar c = new GregorianCalendar();
		while (st.hasMoreTokens()) {
			String o = st.nextToken();
			for (int i = 0; i < dialog.getHoursModel().size(); i++) {
				c.setTime((Date) dialog.getHoursModel().elementAt(i));
				if (c.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(o)) {
					dialog.getHoursList().addSelectionInterval(i, i);
				}
			}
		}
	}

	public void unmarshalDaysOfMonth(String daysOfMonth, SchedulerDialog dialog) {
		StringTokenizer st = new StringTokenizer(daysOfMonth, ",");
		dialog.getDaysOfMonthList().clearSelection();
		while (st.hasMoreTokens()) {
			Object o = st.nextToken();
			for (int i = 0; i < dialog.getDaysOfMonthModel().size(); i++) {
				Object o2 = dialog.getDaysOfMonthModel().elementAt(i);
//				System.out.println(o + " d " + dialog.getDaysOfMonthModel().elementAt(i));
				if (o.equals(o2.toString())) {
//					System.out.println(o + " d " + dialog.getDaysOfMonthModel().elementAt(i));
					dialog.getDaysOfMonthList().addSelectionInterval(i, i);
				}
			}
		}
	}

	public void unmarshalMonths(String months, SchedulerDialog dialog) {
		StringTokenizer st = new StringTokenizer(months, ",");
		dialog.getJanuary().setSelected(false);
		dialog.getFebruary().setSelected(false);
		dialog.getMarch().setSelected(false);
		dialog.getApril().setSelected(false);
		dialog.getMay().setSelected(false);
		dialog.getJune().setSelected(false);
		dialog.getJuly().setSelected(false);
		dialog.getAugust().setSelected(false);
		dialog.getSeptember().setSelected(false);
		dialog.getOctober().setSelected(false);
		dialog.getNovember().setSelected(false);
		dialog.getDecember().setSelected(false);
		while (st.hasMoreTokens()) {
			Object o = st.nextToken();
			if (o.equals("1")) { dialog.getJanuary().setSelected(true);}
			if (o.equals("2")) { dialog.getFebruary().setSelected(true);}
			if (o.equals("3")) { dialog.getMarch().setSelected(true);}
			if (o.equals("4")) { dialog.getApril().setSelected(true);}
			if (o.equals("5")) { dialog.getMay().setSelected(true);}
			if (o.equals("6")) { dialog.getJune().setSelected(true);}
			if (o.equals("7")) { dialog.getJuly().setSelected(true);}
			if (o.equals("8")) { dialog.getAugust().setSelected(true);}
			if (o.equals("9")) { dialog.getSeptember().setSelected(true);}
			if (o.equals("10")) { dialog.getOctober().setSelected(true);}
			if (o.equals("11")) { dialog.getNovember().setSelected(true);}
			if (o.equals("12")) { dialog.getDecember().setSelected(true);}
		};
	}

	public void unmarshalDaysOfWeek(String daysOfWeek, SchedulerDialog dialog) {
		StringTokenizer st = new StringTokenizer(daysOfWeek, ",");
		dialog.getSunday().setSelected(false);
		dialog.getMonday().setSelected(false);
		dialog.getTuesday().setSelected(false);
		dialog.getWednesday().setSelected(false);
		dialog.getThursday().setSelected(false);
		dialog.getFriday().setSelected(false);
		dialog.getSaturday().setSelected(false);
		while (st.hasMoreTokens()) {
			Object o = st.nextToken();
			if (o.equals("1")) { dialog.getSunday().setSelected(true);}
			if (o.equals("2")) { dialog.getMonday().setSelected(true);}
			if (o.equals("3")) { dialog.getTuesday().setSelected(true);}
			if (o.equals("4")) { dialog.getWednesday().setSelected(true);}
			if (o.equals("5")) { dialog.getThursday().setSelected(true);}
			if (o.equals("6")) { dialog.getFriday().setSelected(true);}
			if (o.equals("7")) { dialog.getSaturday().setSelected(true);}
		};
	}

	public void unmarshal(String cron, SchedulerDialog dialog) {
		StringTokenizer st = new StringTokenizer(cron);
		st.nextToken(); // we dont do seconds...
		String minutes = st.nextToken();
		String hours = st.nextToken();
		String daysOfMonth = st.nextToken();
		String months = st.nextToken();
		String daysOfWeek = st.nextToken();
		unmarshalMinutes(minutes, dialog);
		unmarshalHours(hours, dialog);
		unmarshalDaysOfMonth(daysOfMonth, dialog);
		unmarshalMonths(months, dialog);
		unmarshalDaysOfWeek(daysOfWeek, dialog);
		
	}
	
}
