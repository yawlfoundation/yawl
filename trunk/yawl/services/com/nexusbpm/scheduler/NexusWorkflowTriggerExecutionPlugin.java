package com.nexusbpm.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.spi.SchedulerPlugin;

public class NexusWorkflowTriggerExecutionPlugin implements SchedulerPlugin,
		TriggerListener {

	private String name;

	public NexusWorkflowTriggerExecutionPlugin() {}

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

		scheduler.addGlobalTriggerListener(this);
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
	}

	public void triggerMisfired(Trigger trigger) {
		Object[] args = { trigger.getName(), trigger.getGroup(),
				trigger.getPreviousFireTime(), trigger.getNextFireTime(),
				new java.util.Date(), trigger.getJobGroup(),
				trigger.getJobGroup() };
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
	}

	public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
		return false;
	}

}
