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
import java.util.List;
import java.util.TreeSet;

import javax.swing.DefaultListModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.nexusbpm.scheduler.CronTriggerEx;
import com.nexusbpm.scheduler.TriggerEx;
import com.toedter.calendar.CalendarContentProvider;


public class ScheduledContentProvider implements CalendarContentProvider {
	private static final Log LOG = LogFactory.getLog( ScheduledContentProvider.class );
	
	private TriggerCache cache;
	
	public ScheduledContentProvider( Scheduler scheduler ) {
		this.cache = new TriggerCache( scheduler );
	}

	public void setContent( Date date, DefaultListModel model ) {
		Date min = getMinDate( date );
		Date max = getMaxDate( date );
		
		try {
			String[] triggerGroups = cache.getTriggerGroupNames();
			
			model.clear();
			
			TreeSet<ScheduledContent> ts = new TreeSet<ScheduledContent>();
			
			for( int index = 0; index < triggerGroups.length; index++ ) {
				String[] triggers = cache.getTriggerNames( triggerGroups[ index ] );
				
				for( int trigger = 0; trigger < triggers.length; trigger++ ) {
					Trigger t = cache.getTrigger( triggers[ trigger ], triggerGroups[ index ] );
					
					if( t instanceof CronTrigger && ! ( t instanceof TriggerEx ) ) {
						t = new CronTriggerEx( t );
					}
					
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
			model.addElement( e );
		}
	}
	
	protected final Date getMinDate( Date date ) {
		Calendar c = Calendar.getInstance();
		c.setTime( date );
		
		c.set( Calendar.HOUR_OF_DAY , c.getActualMinimum( Calendar.HOUR_OF_DAY ) );
		c.set( Calendar.MINUTE, c.getActualMinimum( Calendar.MINUTE ) );
		c.set( Calendar.SECOND, c.getActualMinimum( Calendar.SECOND ) );
		c.set( Calendar.MILLISECOND, c.getActualMinimum( Calendar.MILLISECOND ) );
		
		return c.getTime();
	}
	
	protected final Date getMaxDate( Date date ) {
		Calendar c = Calendar.getInstance();
		c.setTime( date );
		
		c.set( Calendar.HOUR_OF_DAY , c.getActualMaximum( Calendar.HOUR_OF_DAY ) );
		c.set( Calendar.MINUTE, c.getActualMaximum( Calendar.MINUTE ) );
		c.set( Calendar.SECOND, c.getActualMaximum( Calendar.SECOND ) );
		c.set( Calendar.MILLISECOND, c.getActualMaximum( Calendar.MILLISECOND ) );
		
		return c.getTime();
	}
}
