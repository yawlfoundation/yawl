package com.nexusbpm.services;

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

import com.nexusbpm.scheduler.AbstractJob;
import com.nexusbpm.scheduler.StartYawlCaseJob;
import com.nexusbpm.scheduler.SimpleTriggerEx;

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
		p.setProperty("org.quartz.plugin.nexus.class", "com.nexusbpm.scheduler.NexusWorkflowTriggerExecutionPlugin");
		p.setProperty("org.quartz.scheduler.rmi.proxy","false");
		p.setProperty("java.rmi.server.useCodebaseOnly", "false");
		return p;
	}
	
	
	public void atestRemoteQuartzServer() {
		try {
			System.getProperties().putAll(getRemoteProperties());
			SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
			Scheduler sched = schedFact.getScheduler();
			JobDetail jobDetail = new JobDetail("Income Report",
					"Report Generation", AbstractJob.class);

			CronTrigger trigger = new CronTrigger("Income Report",
					"Report Generation");
			trigger.setCronExpression("0 0 12 ? * SUN");
			sched.deleteJob(trigger.getName(), trigger.getGroup());
			sched.scheduleJob(jobDetail, trigger);
			sched.deleteJob(trigger.getName(), trigger.getGroup());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void testLocalQuartzServer() {
		try {
			System.getProperties().putAll(getLocalProperties());
			SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
			Scheduler sched = schedFact.getScheduler();
			sched.start();
			sched.addGlobalJobListener(this);
			JobDetail jobDetail = new JobDetail("start a case now","case starts", StartYawlCaseJob.class);

			SimpleTrigger trigger = new SimpleTrigger("start a case now","case starts");
			JobDataMap data = new JobDataMap();
			data.put( StartYawlCaseJob.MAP_KEY_SPEC_ID, "MakeRecordings");
			jobDetail.setJobDataMap( data );
			fired = false;
			sched.scheduleJob(jobDetail, trigger);
			synchronized(lock) {lock.wait(1000);}
			assertTrue("The job must have actually started to succeed.", fired);
			sched.deleteJob(trigger.getName(), trigger.getGroup());
			sched.shutdown(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
