/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.editor.editors.schedule;

import java.util.HashMap;
import java.util.Map;

import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.nexusbpm.scheduler.CronTriggerEx;
import com.nexusbpm.scheduler.TriggerEx;

public class TriggerCache {
	private Scheduler scheduler;
	
	private long refreshDate = -1;
	
	private String[] triggerGroups;
	private Map<String, String[]> triggerNames;
	private Map<String, Map<String, Trigger>> triggers;
	
	TriggerCache( Scheduler scheduler ) {
		this.scheduler = scheduler;
		triggerNames = new HashMap<String, String[]>();
		triggers = new HashMap<String, Map<String, Trigger>>();
	}
	
	String[] getTriggerGroupNames() throws SchedulerException {
		refresh();
		return triggerGroups;
	}
	
	String[] getTriggerNames( String triggerGroup ) throws SchedulerException {
		refresh();
		return triggerNames.get( triggerGroup );
	}
	
	Trigger getTrigger( String triggerName, String triggerGroup ) throws SchedulerException {
		refresh();
		return triggers.get( triggerGroup ).get( triggerName );
	}
	
	private void refresh() throws SchedulerException {
		long now = System.currentTimeMillis();
		
		if( refreshDate + 1000 < now ) {
			refreshDate = now;
			
			triggerGroups = scheduler.getTriggerGroupNames();
			triggerNames.clear();
			triggers.clear();
			
			for( int index = 0; index < triggerGroups.length; index++ ) {
				String[] triggerNames = scheduler.getTriggerNames( triggerGroups[ index ] );
				
				this.triggerNames.put( triggerGroups[ index ], triggerNames );
				this.triggers.put( triggerGroups[ index ], new HashMap<String, Trigger>() );
				
				for( int trigger = 0; trigger < triggerNames.length; trigger++ ) {
					Trigger t = scheduler.getTrigger( triggerNames[ trigger ], triggerGroups[ index ] );
					
					if( t instanceof CronTrigger && ! ( t instanceof TriggerEx ) ) {
						t = new CronTriggerEx( t );
					}
					
					if( t instanceof TriggerEx ) {
						triggers.get( triggerGroups[ index ] ).put( triggerNames[ trigger ], t );
					}
				}
			}
		}
	}
}
