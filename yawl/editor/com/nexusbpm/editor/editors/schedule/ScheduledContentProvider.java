package com.nexusbpm.editor.editors.schedule;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import javax.swing.DefaultListModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.toedter.calendar.CalendarContentProvider;


public class ScheduledContentProvider implements CalendarContentProvider {
	private static final Log LOG = LogFactory.getLog( ScheduledContentProvider.class );
	
	private Scheduler scheduler;
	
	public ScheduledContentProvider( Scheduler scheduler ) {
		this.scheduler = scheduler;
	}

	public void setContent( Date date, DefaultListModel model ) {
		Calendar c = Calendar.getInstance();
		c.setTime( date );
		
		c.set( Calendar.HOUR_OF_DAY , c.getActualMinimum( Calendar.HOUR_OF_DAY ) );
		c.set( Calendar.MINUTE, c.getActualMinimum( Calendar.MINUTE ) );
		c.set( Calendar.SECOND, c.getActualMinimum( Calendar.SECOND ) );
		c.set( Calendar.MILLISECOND, c.getActualMinimum( Calendar.MILLISECOND ) );
		
		Date min = c.getTime();
		
		c.set( Calendar.HOUR_OF_DAY , c.getActualMaximum( Calendar.HOUR_OF_DAY ) );
		c.set( Calendar.MINUTE, c.getActualMaximum( Calendar.MINUTE ) );
		c.set( Calendar.SECOND, c.getActualMaximum( Calendar.SECOND ) );
		c.set( Calendar.MILLISECOND, c.getActualMaximum( Calendar.MILLISECOND ) );
		
		Date max = c.getTime();
		
		try {
			String[] triggerGroups = scheduler.getTriggerGroupNames();
			
			model.clear();
			
			TreeSet<ScheduledContent> ts = new TreeSet<ScheduledContent>();
			
			for( int index = 0; index < triggerGroups.length; index++ ) {
				String[] triggers = scheduler.getTriggerNames( triggerGroups[ index ] );
				
				for( int trigger = 0; trigger < triggers.length; trigger++ ) {
					Trigger t = scheduler.getTrigger( triggers[ trigger ], triggerGroups[ index ] );
					
					if( t instanceof TriggerEx ) {
						TriggerEx ex = (TriggerEx) t;
						List<Date> firings = ex.getFireTimesBetween( min, max );
						while( firings.size() > 0 ) {
							Date time = firings.remove( 0 );
							
							ts.add( new ScheduledContent( time, t ) );
						}
					}
				}
			}
			
			for( ScheduledContent content : ts ) {
				model.addElement( content );
			}
		}
		catch( SchedulerException e ) {
			LOG.error( e );
			model.clear();
			model.addElement( "ERROR" );
		}
	}
	
	public static class ScheduledContent implements Comparable {
		private Date date;
		private Trigger trigger;
		private Calendar calendar;
		
		public ScheduledContent( Date date, Trigger trigger ) {
			this.date = date;
			this.trigger = trigger;
			calendar = Calendar.getInstance();
			calendar.setTime( date );
		}
		
		public Date getDate() {
			return date;
		}
		
		public void setDate( Date date ) {
			this.date = date;
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
				if( this.date.before( c.date ) ) {
					return -1;
				}
				else if( this.date.after( c.date ) ) {
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
	}
}
