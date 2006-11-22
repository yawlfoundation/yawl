/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.editor.util;

import java.io.IOException;

import au.edu.qut.yawl.engine.interfce.InterfaceB_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.Interface_Client;

public final class InterfaceB {
	private static InterfaceB_EnvironmentBasedClient ibClient;
	private static String aConnectionHandle;
	private static String serverUri;
	private InterfaceB() {}
	
	private static void initializeClient() throws IOException {
		if( ibClient == null ) {
			ibClient = new InterfaceB_EnvironmentBasedClient(serverUri + "/ib");
			System.out.println("Interface b client pointing to " + serverUri + "/ib");
		}
	}
	
	private static void initializeConnection() throws IOException {
		initializeClient();
		if( aConnectionHandle == null ) {
			aConnectionHandle = ibClient.connect( "admin", "YAWL" );
		}
		else if( ! "<response>Permission Granted</response>".equalsIgnoreCase(
				ibClient.checkConnection( aConnectionHandle ) ) ) {
			aConnectionHandle = null;
			initializeConnection();
		}
	}
	
	public static InterfaceB_EnvironmentBasedClient getClient(String serverUri) throws IOException {
		InterfaceB.serverUri = serverUri;
		initializeConnection();
		return ibClient;
	}
	
	public static String getConnectionHandle() throws IOException {
		initializeConnection();
		return aConnectionHandle;
	}
	
	public static boolean successful( String message ) {
		return Interface_Client.successful( message );
	}
}
