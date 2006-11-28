/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.services;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import util.MockServletConfig;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

import com.nexusbpm.scheduler.QuartzEvent;
import com.nexusbpm.scheduler.QuartzEventDataSource;
import com.nexusbpm.scheduler.QuartzEventDataSourceFactory;
import com.nexusbpm.scheduler.SchedulerService;
import com.nexusbpm.scheduler.StartYawlCaseJob;

public class TestQuartzConnection extends TestCase implements JobListener{

	public void jobExecutionVetoed(JobExecutionContext context) {
	}

	public void jobToBeExecuted(JobExecutionContext context) {
		synchronized(lock) {
			fired = true;
			lock.notifyAll();
		}
	}

	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
	}

	public Object lock = new Object();
	public boolean fired = false;
	
//	public Properties getRemoteProperties() {
//		Properties p = new Properties();
//		p.setProperty("org.quartz.scheduler.instanceName", "YAWLQuartzScheduler");
//		p.setProperty("org.quartz.scheduler.rmi.proxy","true");
//		p.setProperty("org.quartz.scheduler.rmi.registryHost","localhost");
//		p.setProperty("org.quartz.scheduler.rmi.registryPort","1098");
//		p.setProperty("java.rmi.server.useCodebaseOnly", "true");
//		p.setProperty("org.quartz.plugin.yawlevent.appContextUrl", "testresources/applicationContext.xml");
//		return p;
//	}
	
//	public Properties getLocalProperties() {
//		Properties p = new Properties();
//		p.setProperty("org.quartz.scheduler.instanceName", "YAWLLocalTestQuartzScheduler");
//		p.setProperty("org.quartz.plugin.yawlevent.class", "com.nexusbpm.scheduler.YawlTriggerExecutionPlugin");
//		p.setProperty("org.quartz.plugin.yawlevent.appContextUrl", "testresources/applicationContext.xml");
//
//		p.setProperty("org.quartz.scheduler.rmi.proxy","false");
//		p.setProperty("java.rmi.server.useCodebaseOnly", "false");
//		p.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
//		p.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
//		p.setProperty("org.quartz.jobStore.dataSource", "yawlDS");
//		p.setProperty("org.quartz.dataSource.yawlDS.driver", "org.postgresql.Driver");
//		p.setProperty("org.quartz.dataSource.yawlDS.URL", "jdbc:postgresql:yawl");
//		p.setProperty("org.quartz.dataSource.yawlDS.user", "postgres");
//		p.setProperty("org.quartz.dataSource.yawlDS.password", "admin");
//		p.setProperty("org.quartz.dataSource.yawlDS.maxConnections", "10");
//		p.setProperty("org.quartz.dataSource.yawlDS.runScript", "tables_postgres.sql");
//		return p;
//	}
	
	public void testQuartzService() throws Exception{
		LocalClientConfiguration lc = new LocalClientConfiguration(
				"/testresources/quartzServerApplicationContext.xml",
				"/testresources/quartz.server.properties"
		);
		BootstrapConfiguration.setInstance(lc);
		BootstrapConfiguration bc = BootstrapConfiguration.getInstance();
		SchedulerService sc = new SchedulerService() {

			@Override
			public void init(ServletConfig arg0) throws ServletException {
				try {
					start(); 
				} catch (Exception e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
			
		};
		sc.init(new MockServletConfig());
		Scheduler sched = sc.getScheduler();
		sched.addGlobalJobListener(this);

		SimpleTrigger trigger = getTrigger();
		JobDetail jobDetail = new JobDetail("name","group", StartYawlCaseJob.class);
		sched.deleteJob(trigger.getName(), trigger.getGroup());
		fired = false;
		sched.scheduleJob(jobDetail, trigger);
		synchronized(lock) {lock.wait(1000);}
		assertTrue("The job must have started if it is to succeed.", fired);
		sched.deleteJob(trigger.getName(), trigger.getGroup());
		sched.shutdown(true);
	}

	public void testHistory() throws Exception{
		LocalClientConfiguration lc = new LocalClientConfiguration(
				"/testresources/quartzServerApplicationContext.xml",
				"/testresources/quartz.server.properties"
		);
		BootstrapConfiguration.setInstance(lc);
		BootstrapConfiguration bc = BootstrapConfiguration.getInstance();

		Calendar c = new GregorianCalendar();
		c.add(Calendar.YEAR, -1);
		Date startDate = c.getTime();
		c.add(Calendar.YEAR, +2);
		Date endDate = c.getTime();

		QuartzEventDataSourceFactory f = (QuartzEventDataSourceFactory) bc.getApplicationContext().getBean("quartzEventDataSourceFactory");
		QuartzEventDataSource source = f.getDataSource(true);
		List<QuartzEvent> events = source.getEventsBetween(startDate, endDate);
		assertNotSame("No events found", 0, events.size());
	}
	
	private SimpleTrigger getTrigger() {
		SimpleTrigger trigger = new SimpleTrigger("name","group");
		JobDataMap data = new JobDataMap();
		data.put( StartYawlCaseJob.MAP_KEY_SPEC_ID, "MakeRecordings");
		trigger.setJobDataMap(data);
		return trigger;
	}
	
}
