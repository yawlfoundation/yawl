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

import com.nexusbpm.scheduler.QuartzEvent;

public class ScheduledContent implements Comparable {
	private Date actualFireTime;
	private Date scheduledFireTime;
	private Trigger trigger;
	private QuartzEvent event;
	
	public ScheduledContent( Date scheduledFireTime, Trigger trigger ) {
		this( null, scheduledFireTime, null, trigger );
	}
	
	/**
	 * Constructor for content that was scheduled and run (and so it appears
	 * in the history), but the original trigger is gone or modified (so the
	 * occurence is no longer tied to a specific firing from an existing
	 * trigger).
	 */
	public ScheduledContent( QuartzEvent event ) {
		this( event.getActualFireTime(), event.getScheduledFireTime(), event, null );
	}
	
	public ScheduledContent(
			Date actualFireTime,
			Date scheduledFireTime,
			QuartzEvent event,
			Trigger trigger ) {
		this.actualFireTime = actualFireTime;
		this.scheduledFireTime = scheduledFireTime;
		this.event = event;
		this.trigger = trigger;
	}
	
	public Trigger getTrigger() {
		return trigger;
	}
	
	public void setTrigger( Trigger trigger ) {
		this.trigger = trigger;
	}
	
	public QuartzEvent getEvent() {
		return event;
	}
	
	public void setEvent( QuartzEvent event ) {
		this.event = event;
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
		if( trigger != null ) {
			if( event != null ) {
				return getTimeString( scheduledFireTime ) +
					"(" + getTimeString( event.getActualFireTime() ) + ") " + trigger.getName();
			}
			else {
				return getTimeString( scheduledFireTime ) + " - " + trigger.getName();
			}
		}
		else if( event != null ) {
			return getTimeString( actualFireTime ) + ">" + event.getTriggerName();
		}
		else {
			return "NULL SCHEDULED EVENT";
		}
	}
	
	private String getTimeString( Date date ) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime( date );
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
			b.append( " AM" );
		}
		else {
			b.append( " PM" );
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
	
	public Date getScheduledFireTime() {
		return scheduledFireTime;
	}
	
	public void setScheduledFireTime( Date scheduledFireTime ) {
		this.scheduledFireTime = scheduledFireTime;
	}
}