package com.nexusbpm.editor.editors.schedule;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.quartz.CronTrigger;
import org.quartz.Trigger;

public class CronTriggerEx extends CronTrigger implements TriggerEx {
	public CronTriggerEx() {
		super();
	}

	public CronTriggerEx( String name, String group, String jobName, String jobGroup, Date startTime, Date endTime, String cronExpression, TimeZone timeZone ) throws ParseException {
		super( name, group, jobName, jobGroup, startTime, endTime, cronExpression, timeZone );
	}

	public CronTriggerEx( String name, String group, String jobName, String jobGroup, Date startTime, Date endTime, String cronExpression ) throws ParseException {
		super( name, group, jobName, jobGroup, startTime, endTime, cronExpression );
	}

	public CronTriggerEx( String name, String group, String jobName, String jobGroup, String cronExpression, TimeZone timeZone ) throws ParseException {
		super( name, group, jobName, jobGroup, cronExpression, timeZone );
	}

	public CronTriggerEx( String name, String group, String jobName, String jobGroup, String cronExpression ) throws ParseException {
		super( name, group, jobName, jobGroup, cronExpression );
	}

	public CronTriggerEx( String name, String group, String jobName, String jobGroup ) {
		super( name, group, jobName, jobGroup );
	}

	public CronTriggerEx( String name, String group, String cronExpression ) throws ParseException {
		super( name, group, cronExpression );
	}

	public CronTriggerEx( String name, String group ) {
		super( name, group );
	}
	
	public CronTriggerEx( Trigger t ) {
		CronTrigger trigger = (CronTrigger) t;
		this.setCalendarName( trigger.getCalendarName() );
		try {
			this.setCronExpression( trigger.getCronExpression() );
		}
		catch( ParseException e ) {
			throw new RuntimeException( e );
		}
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
		if( trigger.getStartTime() != null )
			this.setStartTime( trigger.getStartTime() );
		this.setTimeZone( trigger.getTimeZone() );
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
		return getFireTimeAfter( afterTime );
	}
}
