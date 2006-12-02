/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YAWLException;
import au.edu.qut.yawl.persistence.dao.YawlEngineDAO;
import au.edu.qut.yawl.util.configuration.BootstrapConfiguration;

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

			BootstrapConfiguration bc = BootstrapConfiguration.getInstance();
        	YawlEngineDAO dao = (YawlEngineDAO) bc.getApplicationContext().getBean("yawlEngineDao"); 
//			InterfaceB_EnvironmentBasedClient clientB = InterfaceB.getClient(configFactory.getConfiguration().getServerUri());

			if (DEBUG) {
				LOG.info(dao.retrieve(YSpecification.class, specID));
			}
			// launch the case
			Object resultObject = dao.startCase(specID, "");
			if (resultObject != null) {
				result = resultObject.toString();
			}

			// try to convert the result into a number. If the case was launched
			// it will
			// be an ID and this will succeed, if it wasn't launched this will
			// fail
			Long.parseLong(result);
			context.setResult(result);
			if (result == null) {
				JobExecutionException e = new JobExecutionException(
					"Error starting case for specification '" + specID + "'");
				e.printStackTrace();
				throw e; 
			}			
		} catch (YAWLException e) {
			e.printStackTrace();
			throw new JobExecutionException(
					"Error starting case for specification '" + specID + "'\n"
							+ result, e, false);
	} catch (NumberFormatException e) {
		e.printStackTrace();
		throw new JobExecutionException(
				"Error starting case for specification '" + specID + "'\n"
						+ result);
	}
	}
}
