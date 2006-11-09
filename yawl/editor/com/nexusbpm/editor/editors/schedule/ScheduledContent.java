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

import org.quartz.Trigger;

public class ScheduledContent implements Comparable {
	private String caseID;
	private Date actualFireTime;
	private Date scheduledFireTime;
	private Trigger trigger;
	private Calendar calendar;
	
	public ScheduledContent( Date scheduledFireTime, Trigger trigger ) {
		this( null, null, scheduledFireTime, trigger );
	}
	
	public ScheduledContent(
			String caseID,
			Date actualFireTime,
			Date scheduledFireTime,
			Trigger trigger ) {
		this.caseID = caseID;
		this.actualFireTime = actualFireTime;
		this.scheduledFireTime = scheduledFireTime;
		this.trigger = trigger;
		calendar = Calendar.getInstance();
		calendar.setTime( scheduledFireTime );
	}
	
	public Trigger getTrigger() {
		return trigger;
	}
	
	public void setTrigger( Trigger trigger ) {
		this.trigger = trigger;
	}
	
	public int compareTo( Object o ) {
		if( o instanceof ScheduledContent ) {
			ScheduledContent c = (ScheduledContent) o;
			if( this.scheduledFireTime.before( c.scheduledFireTime ) ) {
				return -1;
			}
			else if( this.scheduledFireTime.after( c.scheduledFireTime ) ) {
				return 1;
			}
			else {
				return this.trigger.getName().compareTo( c.trigger.getName() );
			}
		}
		else {
			return -1;
		}
	}

	@Override
	public String toString() {
		
		return getTimeString() + trigger.getName();
	}
	
	private String getTimeString() {
		StringBuffer b = new StringBuffer();
		
		if( calendar.get( Calendar.HOUR ) > 0 ) {
			b.append( digit( calendar.get( Calendar.HOUR ) ) );
		}
		else {
			b.append( "12" );
		}
		
		b.append( ":" );
		
		b.append( digit( calendar.get( Calendar.MINUTE ) ) );
		
		if( calendar.get( Calendar.AM_PM ) == Calendar.AM ) {
			b.append( " AM - " );
		}
		else {
			b.append( " PM - " );
		}
		
		return b.toString();
	}
	
	private String digit( int val ) {
		if( val < 10 ) {
			return "0" + val;
		}
		else {
			return String.valueOf( val );
		}
	}

	
	public Date getActualFireTime() {
		return actualFireTime;
	}

	
	public void setActualFireTime( Date actualFireTime ) {
		this.actualFireTime = actualFireTime;
	}

	
	public String getCaseID() {
		return caseID;
	}

	
	public void setCaseID( String caseID ) {
		this.caseID = caseID;
	}

	
	public Date getScheduledFireTime() {
		return scheduledFireTime;
	}

	
	public void setScheduledFireTime( Date scheduledFireTime ) {
		this.scheduledFireTime = scheduledFireTime;
	}

	
	public Calendar getCalendar() {
		return calendar;
	}

	
	public void setCalendar( Calendar calendar ) {
		this.calendar = calendar;
	}
}