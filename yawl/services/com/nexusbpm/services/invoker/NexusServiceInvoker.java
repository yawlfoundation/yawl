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
    	
    	// TODO get appropriate class for service dynamically
    	Class serviceClass = null;
    	String serviceURI = null;
    	try {
	    	if( serviceName.equalsIgnoreCase( "Jython" ) ) {
	    		serviceClass = Class.forName( "com.nexusbpm.services.jython.JythonServiceImpl" );
//	    		serviceClass = Class.forName( "com.nexusbpm.services.invoker.JythonServiceImpl" );
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
			Service serviceModel = serviceFactory.create( serviceClass );
	
			// Create a client proxy
//			XFire xf = XFireFactory.newInstance().getXFire();
//			XFireProxyFactory proxyFactory = new XFireProxyFactory( xf );
			XFireProxyFactory proxyFactory = new XFireProxyFactory();
			NexusService service = (NexusService) proxyFactory.create( serviceModel, serviceURI );
//			NexusService service = create( proxyFactory, xf, serviceModel, serviceURI );
			
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
    
//    /**
//     * <p>This function is a modified version of an XFire function in the XFireProxyFactory that
//     * handles a bug in XFire 1.1 that should be fixed in 1.2.
//     * The bug has to do with having the service definition (the interface: {@link NexusService})
//     * declare a function that takes as a parameter and returns instances of
//     * {@link NexusServiceData} but the service implementations ({@link JythonService}, etc)
//     * actually use subclasses of {@link NexusServiceData}.</p>
//     * TODO remove this method and use XFire 1.2 once it is released.
//     * @see "http://jira.codehaus.org/browse/XFIRE-406"
//     * @see org.codehaus.xfire.client.XFireProxyFactory#create(org.codehaus.xfire.service.Service, java.lang.String)
//     */
//    private static NexusService create(
//    		XFireProxyFactory proxyFactory,
//    		XFire xfire,
//    		Service serviceModel,
//    		String serviceURI ) {
//    	
//    	Collection transports = xfire.getTransportManager().getTransportsForUri( serviceURI );
//
//        if (transports.size() == 0)
//            throw new XFireRuntimeException("No Transport is available for url " + serviceURI);
//        
//        Binding binding = null;
//        Transport transport = null;
//        for (Iterator itr = transports.iterator(); itr.hasNext() && binding == null;)
//        {
//            transport = (Transport) itr.next();
//            
//            for (int i = 0; i < transport.getSupportedBindings().length; i++)
//            {
//                binding = serviceModel.getBinding(transport.getSupportedBindings()[i]);
//                
//                if (binding != null)
//                    break;
//            }
//        }
//
//        Client client = new Client(transport, binding, serviceURI);
//        
//        // begin modified code for Nexus Service
//        
//        List<String> packages = new LinkedList<String>();
//        
//        packages.add( "com.nexusbpm.services.data" );
//        
//        client.setProperty( "jaxb.search.pacakges", packages );
//        
//        // end of modified code for nexus service
//        
//    	return (NexusService) proxyFactory.create( client );
//    }
}


