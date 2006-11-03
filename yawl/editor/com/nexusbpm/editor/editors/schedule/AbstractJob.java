package com.nexusbpm.editor.editors.schedule;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class AbstractJob implements Job {
	private static final Log LOG = LogFactory.getLog( AbstractJob.class );
	
	public abstract void execute( JobDetail detail ) throws JobExecutionException;
	
	public void execute( JobExecutionContext context ) throws JobExecutionException {
		// every job has its own job detail
		JobDetail jobDetail = context.getJobDetail();
		
		String jobName = jobDetail.getName();
		String fullName = jobDetail.getFullName();
		
		// log the time the job started
		LOG.debug( jobName + "(" + fullName + ") fired at " + new Date() );
		
		execute( jobDetail );
	}
}
