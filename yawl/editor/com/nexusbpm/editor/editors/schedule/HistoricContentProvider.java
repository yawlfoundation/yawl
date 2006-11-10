/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.editor.editors.schedule;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;

import org.quartz.Scheduler;

import com.nexusbpm.scheduler.QuartzEvent;

public class HistoricContentProvider extends ScheduledContentProvider {
	public HistoricContentProvider( Scheduler scheduler ) {
		super( scheduler );
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
		
		// TODO ask the cache for events between min and max times
		List<QuartzEvent> events = new LinkedList<QuartzEvent>();
		// TODO remove the following testing event
		// add a quartz event for testing
		if( model.size() > 0 ) {
			ScheduledContent content = (ScheduledContent) model.get( 0 );
			if( content.getScheduledFireTime() != null && content.getTrigger() != null ) {
				events.add( new QuartzEvent(
						content.getTrigger().getName(),
						content.getScheduledFireTime(),
						new Date( (long)( content.getScheduledFireTime().getTime() + 1000 * 60 * 60 * 2.25 ) ),
						"12345",
						"TESTING" ) );
			}
		}
		
		// merge events
		for( QuartzEvent event : events ) {
			String name = event.getTriggerName();
			Date scheduledAt = event.getScheduledFireTime();
			Date firedAt = event.getActualFireTime();
			boolean added = false;
			for( int index = 0; !added && index < model.size(); index++ ) {
				ScheduledContent content = (ScheduledContent) model.get( index );
				if( content.getTrigger() != null && name.equals( content.getTrigger().getName() ) &&
						scheduledAt.equals( content.getScheduledFireTime() ) ) {
					content.setEvent( event );
					added = true;
				}
				else if( content.getScheduledFireTime() != null &&
						content.getScheduledFireTime().after( firedAt ) ||
						content.getActualFireTime() != null &&
						content.getActualFireTime().after( firedAt ) ) {
					model.add( index, new ScheduledContent( event ) );
					added = true;
				}
			}
			if( ! added ) {
				model.addElement( new ScheduledContent( event ) );
			}
		}
	}
}
