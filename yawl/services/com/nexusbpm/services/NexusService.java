/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.nexusbpm.services.data.NexusServiceData;

/**
 * Base interface that all Nexus Services extend.
 * 
 * @author Nathan Rose
 */
@WebService(name="NexusServiceIntf", targetNamespace="http://www.citi.qut.edu.au/yawl")
public interface NexusService {
	@WebMethod
	@WebResult(name="results")
	public NexusServiceData execute(
			@WebParam(name="data") NexusServiceData data );
}
