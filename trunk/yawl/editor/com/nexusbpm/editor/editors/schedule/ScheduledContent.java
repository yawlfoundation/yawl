/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.editor.editors.schedule;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.Trigger;

import com.nexusbpm.scheduler.QuartzEvent;

public class ScheduledContent implements Comparable {
	public static final Color COMPLETED_COLOR = deriveColor( Color.BLUE, .65, Color.BLACK, .35 );
	public static final Color ERRORED_COLOR = deriveColor( Color.RED, .6, Color.DARK_GRAY, .4 );
	public static final Color FIRED_COLOR = deriveColor( Color.BLUE, .5, Color.CYAN, .5 );
	
	private static final DateFormat format = new SimpleDateFormat( "hh:mm a" );
	
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
			return format.format( scheduledFireTime ) + " - " + trigger.getName();
		}
		else if( event != null ) {
			return format.format( scheduledFireTime ) + " - " + event.getTriggerName();
		}
		else {
			return "NULL SCHEDULED EVENT";
		}
	}
	
	public Color getColor() {
		if( event != null ) {
			if( QuartzEvent.State.COMPLETED.toString().equals( event.getFireStatus() ) ) {
				return COMPLETED_COLOR;
			}
			else if( QuartzEvent.State.ERRORED.toString().equals( event.getFireStatus() ) ) {
				return ERRORED_COLOR;
			}
			else if( QuartzEvent.State.FIRED.toString().equals( event.getFireStatus() ) ) {
				return FIRED_COLOR;
			}
			else if( QuartzEvent.State.MISFIRED.toString().equals( event.getFireStatus() ) ) {
				return Color.GRAY;
			}
			else {
				return Color.RED;
			}
		}
		else if( trigger != null ) {
			return Color.BLACK;
		}
		else {
			return Color.RED;
		}
	}
	
	static Color deriveColor( Color color1, double weight1, Color color2, double weight2 ) {
		return new Color(
				(int) ( ( weight1 * color1.getRed() + weight2 * color2.getRed() ) / (weight1 + weight2) ),
				(int) ( ( weight1 * color1.getGreen() + weight2 * color2.getGreen() ) / (weight1 + weight2) ),
				(int) ( ( weight1 * color1.getBlue() + weight2 * color2.getBlue() ) / (weight1 + weight2) ) );
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