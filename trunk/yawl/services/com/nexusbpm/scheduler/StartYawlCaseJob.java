/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.scheduler;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.edu.qut.yawl.engine.interfce.InterfaceB_EnvironmentBasedClient;

import com.nexusbpm.editor.util.InterfaceB;
import com.nexusbpm.services.YawlClientConfigurationFactory;

public class StartYawlCaseJob implements Job {
	public static final String MAP_KEY_SPEC_ID = "specId";

	private static final boolean DEBUG = false;

	private static final Log LOG = LogFactory.getLog(StartYawlCaseJob.class);

	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		String result = "";
		String specID = context.getTrigger().getJobDataMap().getString(
				MAP_KEY_SPEC_ID);
		try {
			// get the client to the engine
    		String[] paths = { "YawlClientApplicationContext.xml" };
    		ApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
        	YawlClientConfigurationFactory configFactory = (YawlClientConfigurationFactory) ctx.getBean("yawlClientConfigurationFactory");  

			InterfaceB_EnvironmentBasedClient clientB = InterfaceB.getClient(configFactory.getConfiguration().getServerUri());

			if (DEBUG) {
				LOG.info(clientB.getSpecification(specID, InterfaceB
						.getConnectionHandle()));
			}
			// launch the case
			result = clientB.launchCase(specID, "", InterfaceB
					.getConnectionHandle());

			// try to convert the result into a number. If the case was launched
			// it will
			// be an ID and this will succeed, if it wasn't launched this will
			// fail
			Long.parseLong(result);
			context.setResult(result);
		} catch (IOException e) {
			e.printStackTrace();
			throw new JobExecutionException(
					"Error starting case for specification '" + specID + "'",
					e, false);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new JobExecutionException(
					"Error starting case for specification '" + specID + "'\n"
							+ result);
		}
	}
}
