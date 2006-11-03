package com.nexusbpm.scheduler;

import java.util.Calendar;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

import com.nexusbpm.editor.editors.schedule.CronTriggerEx;
import com.nexusbpm.editor.editors.schedule.SimpleTriggerEx;

public class SchedulerService {
	private Scheduler scheduler;
	public void start() throws SchedulerException {
		System.setProperty( "org.quartz.properties", "quartz.server.properties" );
		
		System.out.println( "starting scheduler..." );
		
		scheduler = StdSchedulerFactory.getDefaultScheduler();
		
		scheduler.start();
		
		System.out.println( "scheduler startup completed." );
		
		JobDetail jobDetail = new JobDetail("job", "group", new Job() {
			public void execute( JobExecutionContext context ) throws JobExecutionException {
			}
		}.getClass(), false, true, false);
		
		
		Trigger trigger = new SimpleTriggerEx( TriggerUtils.makeHourlyTrigger( 3, 260 ) );
		trigger.setName( "testTriggerName" );
		
		Calendar c = Calendar.getInstance();
		c.add( Calendar.MONTH, 1 );
		c.set( Calendar.DAY_OF_MONTH, 30 );
		
		trigger.setStartTime( c.getTime() );
		
		scheduleJob( jobDetail, trigger );
		
		
		Trigger t2 = new CronTriggerEx( TriggerUtils.makeWeeklyTrigger( TriggerUtils.SUNDAY, 15, 30 ) );
		t2.setName( "Weekly_Trigger" );
		
		scheduleJob( jobDetail, t2 );
		
		System.out.println( "default jobs are scheduled." );
	}
	
	public void scheduleJob( JobDetail job, Trigger trigger ) {
		trigger.setJobGroup( job.getGroup() );
		trigger.setJobName( job.getName() );
		
		try {
			scheduler.addJob( job, true );
			scheduler.scheduleJob( trigger );
		}
		catch( SchedulerException e ) {
			throw new RuntimeException( e );
		}
	}
	
	public static void main(String[] args) throws SchedulerException {
		SchedulerService scheduler = new SchedulerService();
		scheduler.start();
	}
}
