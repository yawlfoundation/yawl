/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.scheduler;

import java.util.Calendar;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;


public class SchedulerService extends HttpServlet{
	private Scheduler scheduler;
	
	
	
	@Override
	public void init(ServletConfig arg0) throws ServletException {
		super.init(arg0);
		try {
			start();
		} catch (SchedulerException e) {
			log("Unable to start quartz scheduler", e);
		}
	}

	public void start() throws SchedulerException {

		Properties p = new Properties();
		p.setProperty("org.quartz.scheduler.instanceName","RMIScheduler");
		p.setProperty("org.quartz.scheduler.rmi.export","true");
		p.setProperty("org.quartz.scheduler.rmi.registryHost","localhost");
		p.setProperty("org.quartz.scheduler.rmi.registryPort","1099");
		p.setProperty("org.quartz.scheduler.rmi.createRegistry","true");
		p.setProperty("org.quartz.scheduler.rmi.serverPort","0");
		p.setProperty("org.quartz.threadPool.class","org.quartz.simpl.SimpleThreadPool");
		p.setProperty("org.quartz.threadPool.threadCount","10");
		p.setProperty("org.quartz.threadPool.threadPriority","5");

		p.setProperty("org.quartz.jobStore.misfireThreshold","60000");
		p.setProperty("org.quartz.jobStore.class","org.quartz.simpl.RAMJobStore");
		System.getProperties().putAll(p);
		System.out.println( "starting scheduler..." );
		
		scheduler = StdSchedulerFactory.getDefaultScheduler();
		
		scheduler.start();
		
		
		System.out.println( "scheduler startup completed for " + scheduler.getClass().getName() + ":" + scheduler.getSchedulerInstanceId());
		
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
