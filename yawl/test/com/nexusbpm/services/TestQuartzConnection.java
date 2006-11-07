package com.nexusbpm.services;

import java.util.Properties;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;

import com.nexusbpm.scheduler.AbstractJob;

public class TestQuartzConnection {

	public static void main(String[] args) {
		try {
			Properties p = new Properties();
			p.setProperty("org.quartz.scheduler.instanceName", "RMIScheduler");
			p.setProperty("org.quartz.scheduler.rmi.proxy","true");
			p.setProperty("org.quartz.scheduler.rmi.registryHost","localhost");
			p.setProperty("org.quartz.scheduler.rmi.registryPort","1099");
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
/*
 * 
 * 
 *
 * 
 * 
 *
 */