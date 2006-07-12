/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.invoker;

import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.jaxb2.JaxbServiceFactory;
import org.codehaus.xfire.service.Service;

import au.edu.qut.yawl.exceptions.YAWLException;

import com.nexusbpm.services.NexusService;
import com.nexusbpm.services.data.NexusServiceData;

/**
 * Uses XFire to invoke Nexus services.
 * @author Nathan Rose
 */
public class NexusServiceInvoker {
    public static NexusServiceData invokeService(String serviceName,
                                                 NexusServiceData data) throws YAWLException {
    	NexusService service = getServiceWithName( serviceName );
    	
    	System.out.println( "Calling nexus service with:" + data );
    	
    	return service.execute( data );
    }
    
    public static NexusService getServiceWithName( String serviceName ) throws YAWLException {
    	
    	if( serviceName == null || serviceName.length() == 0 ) {
    		throw new YAWLException( "To invoke a Nexus Service the name of the service must be supplied!" );
    	}
    	
    	// TODO get appropriate class/URI for service dynamically
    	Class serviceClass = null;
    	String serviceURI = null;
    	try {
	    	if( serviceName.equalsIgnoreCase( "Jython" ) ) {
	    		serviceClass = Class.forName( "com.nexusbpm.services.jython.JythonService" );
	    		serviceURI = "http://localhost:8080/JythonService/services/JythonService";
	    	}
	    	else {
	    		throw new YAWLException( "There is no Nexus Service named '" + serviceName + "'!" );
	    	}
    	}
    	catch( ClassNotFoundException e ) {
    		throw new YAWLException( "There is no Nexus Service named '" + serviceName + "'!", e );
    	}
    	
    	try {
	    	JaxbServiceFactory serviceFactory = new JaxbServiceFactory();
//    		ObjectServiceFactory serviceFactory = new ObjectServiceFactory();
			Service serviceModel = serviceFactory.create( serviceClass );
//    		Service serviceModel = serviceFactory.create( NexusService.class );
	
			// Create a client proxy
			XFireProxyFactory proxyFactory = new XFireProxyFactory();
			NexusService service = (NexusService) proxyFactory.create( serviceModel, serviceURI );
			
			assert service != null : "Proxy for Nexus Service '" + serviceName + "' was null";
			
	    	return service;
    	}
		catch( Exception e ) {
			throw new YAWLException(
					"Error creating proxy for Nexus Service named '" +
					serviceName + "'!\n(serviceClass=" + serviceClass +
							"\nserviceURI=" + serviceURI + ")", e );
		}
    }
}
