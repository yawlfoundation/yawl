/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.scheduler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

import au.edu.qut.yawl.engine.interfce.InterfaceB_EnvironmentBasedClient;

import com.nexusbpm.editor.util.InterfaceB;

public class StartYawlCaseJob implements Job {
	public static final String MAP_KEY_SPEC_ID = "specId";
	public static final String MAP_KEY_CASE_ID = "caseId";

	private static final boolean DEBUG = false;

	private static final Log LOG = LogFactory.getLog( StartYawlCaseJob.class );
	
	public StartYawlCaseJob() {
	}
	
	public void execute( JobExecutionContext context ) throws JobExecutionException {

		JobDetail jobDetail = context.getJobDetail();
		JobDataMap dataMap = jobDetail.getJobDataMap();
		String specID = dataMap.getString( MAP_KEY_SPEC_ID );
		
		String result = "";
		
		try {
			// get the client to the engine
			InterfaceB_EnvironmentBasedClient clientB = InterfaceB.getClient();
	    	
			if( DEBUG ) {
				LOG.info( clientB.getSpecification( specID, InterfaceB.getConnectionHandle() ) );
			}
			// launch the case
			result = clientB.launchCase( specID, "", InterfaceB.getConnectionHandle() );
			context.setResult(result);
			LOG.debug( result );
			
			// try to convert the result into a number. If the case was launched it will
			// be an ID and this will succeed, if it wasn't launched this will fail
			long id = Long.parseLong( result );
		}
		catch( IOException e ) {
			LOG.error( "Error starting case for specification '" + specID + "'", e );
			
			StringWriter sw = new StringWriter();
    		e.printStackTrace( new PrintWriter( sw ) );
		}	
		catch( NumberFormatException e ) {
			LOG.error( "Error starting case for specification '" + specID + "'", e );
		}
	}
}
