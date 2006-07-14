/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services;

public class NexusServiceInfo {
    /** The name of the service. */
    private final String serviceName;
    /** The name of the class implementing the service. */
    private final String serviceClassName; // use the class name so that this class will still work
                                           // even if used without the services being on the classpath
    /** The URI where the service can be accessed. */
    private final String serviceURI; // TODO get URI from properties file
    /** The variables that the service will use. */
    private final String[] variables;
    
    public static final NexusServiceInfo[] SERVICES = new NexusServiceInfo[] {
        new NexusServiceInfo( "Jython",
                "com.nexusbpm.services.jython.JythonService",
                "http://localhost:8080/JythonService/services/JythonService",
                new String[] { "code", "output", "error" } ),
        new NexusServiceInfo( "EmailSender",
                "com.nexusbpm.services.email.EmailSenderService",
                "http://localhost:8080/EmailSenderService/services/EmailSenderService",
                new String[] { "toAddress", "ccAddress", "bccAddress", "subject", "body" } )
    };
    
    private NexusServiceInfo(String serviceName, String serviceClassName,
            String serviceURI, String[] variables) {
        this.serviceName = serviceName;
        this.serviceClassName = serviceClassName;
        this.serviceURI = serviceURI;
        this.variables = variables;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getServiceClassName() {
        return serviceClassName;
    }

    public String getServiceURI() {
        return serviceURI;
    }

    public String[] getVariables() {
        return variables;
    }
}
