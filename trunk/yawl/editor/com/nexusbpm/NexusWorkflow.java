/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm;

import au.edu.qut.yawl.elements.YSpecification;

public class NexusWorkflow {
	
	public static final String NAME_SEPARATOR = "__";

	public static final String VARTYPE_STRING = "string";

	public static final String STATUS_VAR = "Status";

	public static final String SERVICENAME_VAR = "ServiceName";

	public static final String LOCAL_INVOKER_URI = "http://localhost:8080/NexusServiceInvoker/";

	public static final String XML_SCHEMA_URL = "http://www.w3.org/2001/XMLSchema";
    
    public static final String CURRENT_VERSION = YSpecification._Beta7_1;
}
