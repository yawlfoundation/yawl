/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.editor.editors.schedule;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListModel;

import org.quartz.Scheduler;

import com.nexusbpm.scheduler.QuartzEvent;
import com.nexusbpm.scheduler.QuartzEventDataSource;
import com.nexusbpm.scheduler.QuartzEventDataSourceFactory;

public class HistoricContentProvider extends ScheduledContentProvider {
	private QuartzEventDataSource history;
	public HistoricContentProvider( Scheduler scheduler ) throws RemoteException, NotBoundException {
		super( scheduler );
		this.history = QuartzEventDataSourceFactory.getDataSource( false, true );
	}
	
	@Override
	public void setContent( Date date, DefaultListModel model ) {
		super.setContent( date, model );
		
		if( model.getSize() == 1 && model.get( 0 ) instanceof Exception ) {
			// if the higher level failed, we can't continue
			return;
		}
		
		Date min = getMinDate( date );
		Date max = getMaxDate( date );
		Date now = new Date();
		
		if( min.after( now ) ) {
			return;
		}
		
		try {
			// ask the cache for events between min and max times
			List<QuartzEvent> events = history.getEventsBetween( min, max );
			
			// merge events
			for( QuartzEvent event : events ) {
				String name = event.getTriggerName();
				Date scheduledAt = event.getScheduledFireTime();
				boolean added = false;
				for( int index = 0; !added && index < model.size(); index++ ) {
					ScheduledContent content = (ScheduledContent) model.get( index );
					if( content.getTrigger() != null && name.equals( content.getTrigger().getName() ) &&
							scheduledAt.equals( content.getScheduledFireTime() ) ) {
						content.setEvent( event );
						added = true;
					}
					else if( content.getScheduledFireTime() != null &&
							content.getScheduledFireTime().after( scheduledAt ) ) {
						model.add( index, new ScheduledContent( event ) );
						added = true;
					}
				}
				if( ! added ) {
					model.addElement( new ScheduledContent( event ) );
				}
			}
		}
		catch( RemoteException e ) {
			
		}
		
		// remove scheduled events from the past that never ran
		for( int index = 0; index < model.size(); index++ ) {
			ScheduledContent content = (ScheduledContent) model.get( index );
			
			if( content.getScheduledFireTime().before( now ) && content.getEvent() == null ) {
				model.remove( index );
				index -= 1;
			}
		}
	}
}
