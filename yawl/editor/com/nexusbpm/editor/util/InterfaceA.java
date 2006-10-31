/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.editor.util;

import java.io.IOException;

import com.nexusbpm.editor.EditorConfiguration;

import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.Interface_Client;

public final class InterfaceA {
	private static InterfaceA_EnvironmentBasedClient iaClient;
	private static String aConnectionHandle;
	
	private InterfaceA() {}
	
	private static void initializeClient() {
		if( iaClient == null ) {
			iaClient = new InterfaceA_EnvironmentBasedClient(EditorConfiguration.getInstance().getServerUri() + "/ia");
		}
	}
	
	private static void initializeConnection() throws IOException {
		initializeClient();
		if( aConnectionHandle == null ) {
			aConnectionHandle = iaClient.connect( "admin", "YAWL" );
		}
		else if( ! "<response>Permission Granted</response>".equalsIgnoreCase(
				iaClient.checkConnection( aConnectionHandle ) ) ) {
			aConnectionHandle = null;
			initializeConnection();
		}
	}
	
	public static InterfaceA_EnvironmentBasedClient getClient() throws IOException {
		initializeConnection();
		return iaClient;
	}
	
	public static String getConnectionHandle() throws IOException {
		initializeConnection();
		return aConnectionHandle;
	}
	
	public static boolean successful( String message ) {
		return Interface_Client.successful( message );
	}
}
