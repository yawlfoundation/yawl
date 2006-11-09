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
		List<ScheduledContent> events = new LinkedList<ScheduledContent>();
		
		// TODO merge events
	}
}
