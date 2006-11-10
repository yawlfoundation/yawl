package com.nexusbpm.scheduler;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.spi.SchedulerPlugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class YawlTriggerExecutionPlugin implements SchedulerPlugin,
		TriggerListener {

	private String name;
	private String appContextUrl;
	QuartzEventDao dao;
	public YawlTriggerExecutionPlugin() {}

	/**
	 * <p>
	 * Called during creation of the <code>Scheduler</code> in order to give
	 * the <code>SchedulerPlugin</code> a chance to initialize.
	 * </p>
	 * 
	 * @throws SchedulerConfigException
	 *           if there is an error initializing.
	 */
	public void initialize(String name, Scheduler scheduler)
			throws SchedulerException {
		this.name = name;
		String[] paths = {getAppContextUrl()};
		ApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		dao = (QuartzEventDao) ctx.getBean("quartzDao");
		scheduler.addGlobalTriggerListener(this);
	}

	protected void saveEvent(QuartzEvent qe) {
		dao.saveRecord(qe);
	}
	
	
	public void start() {
	}

	/**
	 * <p>
	 * Called in order to inform the <code>SchedulerPlugin</code> that it
	 * should free up all of it's resources because the scheduler is shutting
	 * down.
	 * </p>
	 */
	public void shutdown() {
	}

	public String getName() {
		return name;
	}

	public void triggerFired(Trigger trigger, JobExecutionContext context) {
		Object[] args = { trigger.getName(), trigger.getGroup(),
				trigger.getPreviousFireTime(), trigger.getNextFireTime(),
				new java.util.Date(), context.getJobDetail().getName(),
				context.getJobDetail().getGroup(),
				new Integer(context.getRefireCount()) };
		QuartzEvent qe = new QuartzEvent(trigger.getName(), trigger.getPreviousFireTime(), new Date(), null, "fired");
		saveEvent(qe);
	}

	public void triggerMisfired(Trigger trigger) {
		Object[] args = { trigger.getName(), trigger.getGroup(),
				trigger.getPreviousFireTime(), trigger.getNextFireTime(),
				new java.util.Date(), trigger.getJobGroup(),
				trigger.getJobGroup() };
		QuartzEvent qe = new QuartzEvent(trigger.getName(), trigger.getPreviousFireTime(), new Date(), null, "misfired");
		saveEvent(qe);
	}

	public void triggerComplete(Trigger trigger, JobExecutionContext context,
			int triggerInstructionCode) {
		String instrCode = "UNKNOWN";
		if (triggerInstructionCode == Trigger.INSTRUCTION_DELETE_TRIGGER) {
			instrCode = "DELETE TRIGGER";
		} else if (triggerInstructionCode == Trigger.INSTRUCTION_NOOP) {
			instrCode = "DO NOTHING";
		} else if (triggerInstructionCode == Trigger.INSTRUCTION_RE_EXECUTE_JOB) {
			instrCode = "RE-EXECUTE JOB";
		} else if (triggerInstructionCode == Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE) {
			instrCode = "SET ALL OF JOB'S TRIGGERS COMPLETE";
		} else if (triggerInstructionCode == Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE) {
			instrCode = "SET THIS TRIGGER COMPLETE";
		}

		Object[] args = { trigger.getName(), trigger.getGroup(),
				trigger.getPreviousFireTime(), trigger.getNextFireTime(),
				new java.util.Date(), context.getJobDetail().getName(),
				context.getJobDetail().getGroup(),
				new Integer(context.getRefireCount()),
				new Integer(triggerInstructionCode), instrCode };
		QuartzEvent qe = new QuartzEvent(trigger.getName(), trigger.getPreviousFireTime(), new Date(), context.getResult().toString(), "fired");
		saveEvent(qe);
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

}
