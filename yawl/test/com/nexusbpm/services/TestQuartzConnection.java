/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.nexusbpm.scheduler.AbstractJob;
import com.nexusbpm.scheduler.QuartzEvent;
import com.nexusbpm.scheduler.QuartzEventDao;
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
	
	public Properties getRemoteProperties() {
		Properties p = new Properties();
		p.setProperty("org.quartz.scheduler.instanceName", "YAWLQuartzScheduler");
		p.setProperty("org.quartz.scheduler.rmi.proxy","true");
		p.setProperty("org.quartz.scheduler.rmi.registryHost","localhost");
		p.setProperty("org.quartz.scheduler.rmi.registryPort","1098");
		p.setProperty("java.rmi.server.useCodebaseOnly", "true");
		return p;
	}
	
	public Properties getLocalProperties() {
		Properties p = new Properties();
		p.setProperty("org.quartz.scheduler.instanceName", "YAWLLocalQuartzScheduler");
		p.setProperty("org.quartz.plugin.yawlevent.class", "com.nexusbpm.scheduler.YawlTriggerExecutionPlugin");
		p.setProperty("org.quartz.plugin.yawlevent.appContextUrl", "testresources/applicationContext.xml");
		
		p.setProperty("org.quartz.scheduler.rmi.proxy","false");
		p.setProperty("java.rmi.server.useCodebaseOnly", "false");
		p.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
		p.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
		p.setProperty("org.quartz.jobStore.dataSource", "yawlDS");
		p.setProperty("org.quartz.dataSource.yawlDS.driver", "org.postgresql.Driver");
		p.setProperty("org.quartz.dataSource.yawlDS.URL", "jdbc:postgresql:yawl");
		p.setProperty("org.quartz.dataSource.yawlDS.user", "postgres");
		p.setProperty("org.quartz.dataSource.yawlDS.password", "admin");
		p.setProperty("org.quartz.dataSource.yawlDS.maxConnections", "10");
		p.setProperty("org.quartz.dataSource.yawlDS.runScript", "tables_postgres.sql");
		return p;
	}
	
	
	public void atestRemoteQuartzServer() {
		try {
			System.getProperties().putAll(getRemoteProperties());
			SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
			Scheduler sched = schedFact.getScheduler();
			JobDetail jobDetail = new JobDetail("name", "group", AbstractJob.class);
			CronTrigger trigger = new CronTrigger("name", "group");
			trigger.setCronExpression("0 0 12 ? * SUN");
			sched.deleteJob(trigger.getName(), trigger.getGroup());
			sched.scheduleJob(jobDetail, trigger);
			sched.deleteJob(trigger.getName(), trigger.getGroup());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	public void testLocalQuartzServer() {
		try {
			System.getProperties().putAll(getLocalProperties());
			Class.forName(System.getProperty("org.quartz.dataSource.yawlDS.driver"));
			Connection c = DriverManager.getConnection(System.getProperty("org.quartz.dataSource.yawlDS.URL"), 
					System.getProperty("org.quartz.dataSource.yawlDS.user"),
					System.getProperty("org.quartz.dataSource.yawlDS.password")
					);
			String t = readStreamAsString(SchedulerService.class.getResourceAsStream("scripts/" + System.getProperty("org.quartz.dataSource.yawlDS.runScript")));
			try {
				Statement s = c.createStatement();
				s.execute(t);
				c.commit();
				s.close();
				c.close();
			} catch(Exception e) {}
			
			SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
			Scheduler sched = schedFact.getScheduler();
			sched.start();
			sched.addGlobalJobListener(this);
			JobDetail jobDetail = new JobDetail("name","group", StartYawlCaseJob.class);

			SimpleTrigger trigger = new SimpleTrigger("name","group");
			JobDataMap data = new JobDataMap();
			data.put( StartYawlCaseJob.MAP_KEY_SPEC_ID, "MakeRecordings");
//			jobDetail.setJobDataMap( data );
			trigger.setJobDataMap(data);
			sched.deleteJob(trigger.getName(), trigger.getGroup());
			fired = false;
			sched.scheduleJob(jobDetail, trigger);
			synchronized(lock) {lock.wait(1000);}
			assertTrue("The job must have started if it is to succeed.", fired);
			sched.deleteJob(trigger.getName(), trigger.getGroup());
			sched.shutdown(true);
			
			String[] paths = {"testresources/applicationContext.xml"};
			ApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
			QuartzEventDao dao = (QuartzEventDao) ctx.getBean("quartzDao");
			Calendar start = new GregorianCalendar();
			start.roll(Calendar.DATE, false);
			Calendar end = new GregorianCalendar();
			end.roll(Calendar.DATE, true);
			List<QuartzEvent> qel = dao.getRecords(start.getTime(), end.getTime());
			assertNotNull(qel);
			assertTrue(qel.size() > 0);			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private static String readStreamAsString(InputStream stream)
			throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

}
