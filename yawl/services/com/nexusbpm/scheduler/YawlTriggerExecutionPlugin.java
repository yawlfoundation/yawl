/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.scheduler;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.spi.SchedulerPlugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class YawlTriggerExecutionPlugin implements SchedulerPlugin,
		TriggerListener, JobListener {

	private String name;

	private String appContextUrl;

	QuartzEventDao dao;

	public YawlTriggerExecutionPlugin() {
	}

	/**
	 * <p>
	 * Called during creation of the <code>Scheduler</code> in order to give
	 * the <code>SchedulerPlugin</code> a chance to initialize.
	 * </p>
	 * 
	 * @throws SchedulerConfigException
	 *             if there is an error initializing.
	 */
	public void initialize(String name, Scheduler scheduler)
			throws SchedulerException {
		this.name = name;
		String[] paths = { getAppContextUrl() };
		ApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		dao = (QuartzEventDao) ctx.getBean("quartzDao");
		scheduler.addGlobalTriggerListener(this);
		scheduler.addGlobalJobListener(this);
	}

	protected void saveEvent(QuartzEvent qe) {
		dao.saveRecord(qe);
	}

	public void start() {
	}

	public void shutdown() {
	}

	public String getName() {
		return name;
	}

	public void triggerFired(Trigger trigger, JobExecutionContext context) {
		QuartzEvent qe = new QuartzEvent(trigger.getName(), trigger
				.getPreviousFireTime(), new Date(), null,
				QuartzEvent.State.FIRED.toString());
		saveEvent(qe);
	}

	public void triggerMisfired(Trigger trigger) {
		QuartzEvent qe = new QuartzEvent(trigger.getName(), trigger
				.getPreviousFireTime(), new Date(), null,
				QuartzEvent.State.MISFIRED.toString());
		saveEvent(qe);
	}

	public void triggerComplete(Trigger trigger, JobExecutionContext context,
			int triggerInstructionCode) {
		if (context.getResult() != null) {
			Date reportedFireTime = trigger.getPreviousFireTime();
			if (reportedFireTime == null) reportedFireTime = new Date();
			QuartzEvent qe = new QuartzEvent(trigger.getName(),
					reportedFireTime, new Date(), context.getResult()
							.toString(), QuartzEvent.State.COMPLETED.toString());
			saveEvent(qe);
		}
	}

	public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
		return false;
	}

	public String getAppContextUrl() {
		return appContextUrl;
	}

	public void setAppContextUrl(String appContextUrl) {
		this.appContextUrl = appContextUrl;
	}

	public void jobExecutionVetoed(JobExecutionContext context) {
	}

	public void jobToBeExecuted(JobExecutionContext context) {
	}

	public void jobWasExecuted(JobExecutionContext context,
			JobExecutionException jobException) {
		if (jobException != null) {
			QuartzEvent qe = new QuartzEvent(context.getTrigger().getName(),
					context.getTrigger().getPreviousFireTime(), new Date(),
					null, QuartzEvent.State.ERRORED.toString(), jobException
							.getMessage());
			saveEvent(qe);
		}
	}

}
