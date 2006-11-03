package com.nexusbpm.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.SimpleTrigger;
import org.quartz.Trigger;


public class SimpleTriggerEx extends SimpleTrigger implements TriggerEx {
	public SimpleTriggerEx() {
		super();
	}

	public SimpleTriggerEx( String name, String group, Date startTime, Date endTime, int repeatCount, long repeatInterval ) {
		super( name, group, startTime, endTime, repeatCount, repeatInterval );
	}

	public SimpleTriggerEx( String name, String group, Date startTime ) {
		super( name, group, startTime );
	}

	public SimpleTriggerEx( String name, String group, int repeatCount, long repeatInterval ) {
		super( name, group, repeatCount, repeatInterval );
	}

	public SimpleTriggerEx( String name, String group, String jobName, String jobGroup, Date startTime, Date endTime, int repeatCount, long repeatInterval ) {
		super( name, group, jobName, jobGroup, startTime, endTime, repeatCount, repeatInterval );
	}

	public SimpleTriggerEx( String name, String group ) {
		super( name, group );
	}
	
	public SimpleTriggerEx( Trigger t ) {
		SimpleTrigger trigger = (SimpleTrigger) t;
		this.setCalendarName( trigger.getCalendarName() );
		this.setDescription( trigger.getDescription() );
		this.setEndTime( trigger.getEndTime() );
		this.setGroup( trigger.getGroup() );
		this.setJobDataMap( trigger.getJobDataMap() );
		this.setJobGroup( trigger.getJobGroup() );
		if( trigger.getJobName() != null )
			this.setJobName( trigger.getJobName() );
		this.setMisfireInstruction( trigger.getMisfireInstruction() );
		if( trigger.getName() != null )
			this.setName( trigger.getName() );
		this.setRepeatCount( trigger.getRepeatCount() );
		this.setRepeatInterval( trigger.getRepeatInterval() );
		if( trigger.getStartTime() != null )
			this.setStartTime( trigger.getStartTime() );
		this.setVolatility( trigger.isVolatile() );
	}

	public List<Date> getFireTimesBetween( Date start, Date end ) {
		Date time = new Date( start.getTime() - 1 );
		List<Date> firings = new ArrayList<Date>();
		
		while( time != null && time.before( end ) ) {
			time = getFireTimeAfterEx( time );
			if( time != null && time.before( end ) ) {
				firings.add( time );
			}
		}
		
		return firings;
	}
	
	public Date getFireTimeAfterEx( Date afterTime ) {
		long startMillis = getStartTime().getTime();
        long afterMillis = afterTime.getTime();
        long endMillis = (getEndTime() == null) ? Long.MAX_VALUE : getEndTime().getTime();
        
        int repeat = getRepeatCount();
        
        int count = computeNumTimesFiredBetween( getStartTime(), afterTime ) - 1;
        if( count < 0 ) count = 0;
        
        while( repeat == REPEAT_INDEFINITELY || count < repeat ) {
        	long instanceMillis = startMillis + count * getRepeatInterval();
        	
        	if( instanceMillis >= endMillis ) {
        		break;
        	}
        	else if( instanceMillis > afterMillis ) {
        		return new Date( instanceMillis );
        	}
        	count++;
        }
        
        return null;
	}
}
