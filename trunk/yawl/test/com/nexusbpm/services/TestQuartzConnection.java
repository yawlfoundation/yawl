package com.nexusbpm.services;

import java.util.Properties;

import junit.framework.TestCase;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;

import com.nexusbpm.scheduler.AbstractJob;

public class TestQuartzConnection extends TestCase{

	public void testQuartzServer() {
		try {
			Properties p = new Properties();
			p.setProperty("org.quartz.scheduler.instanceName", "YAWLQuartzScheduler");
			p.setProperty("org.quartz.scheduler.rmi.proxy","true");
			p.setProperty("org.quartz.scheduler.rmi.registryHost","localhost");
			p.setProperty("org.quartz.scheduler.rmi.registryPort","1098");
			p.setProperty("java.rmi.server.useCodebaseOnly", "true");
			System.getProperties().putAll(p);
			SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
			Scheduler sched = schedFact.getScheduler();
			JobDetail jobDetail = new JobDetail("Income Report",
					"Report Generation", AbstractJob.class);

			CronTrigger trigger = new CronTrigger("Income Report",
					"Report Generation");
			trigger.setCronExpression("0 0 12 ? * SUN");
			sched.scheduleJob(jobDetail, trigger);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
