/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services;

import com.nexusbpm.services.data.Variable;

public class NexusServiceInfo {
    /** The name of the service. */
    private final String serviceName;
    /** The name of the class implementing the service. */
    private final String serviceClassName; // use the class name so that this class will still work
                                           // even if used without the services being on the classpath
    /** The name of the editor's class for tasks that use this nexus service. */
    private final String editorClassName; // use the class name instead of the class itself for the same
                                          // reason as with serviceClassName
    /** The URI where the service can be accessed. */
    private final String serviceURI; // TODO get URI from properties file
    /** The variables that the service will use. */
    private final String[] variableNames;
    private final String[] variableTypes;
    private final Object[] initialValues;
    
    public static final NexusServiceInfo getServiceWithName( String serviceName ) {
        for( int index = 0; index < SERVICES.length; index++ ) {
            if( SERVICES[ index ].getServiceName().equalsIgnoreCase( serviceName ) ) {
                return SERVICES[ index ];
            }
        }
        return null;
    }
    
    public static final NexusServiceInfo[] SERVICES = new NexusServiceInfo[] {
        new NexusServiceInfo( "Jython",
                "com.nexusbpm.services.jython.JythonService",
                "com.nexusbpm.editor.editors.JythonEditor",
                "http://localhost:8080/JythonService/services/JythonService",
                new String[] { "code", "output", "error" },
                new String[] { Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT },
                new Object[] { "print 'foo'", "", "" }),
        new NexusServiceInfo( "EmailSender",
                "com.nexusbpm.services.email.EmailSenderService",
                "com.nexusbpm.editor.editors.EmailSenderEditor",
                "http://localhost:8080/EmailSenderService/services/EmailSenderService",
                new String[] { "toAddress", /*"ccAddress", "bccAddress",*/ "subject", "body" },
                new String[] { Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT },
                new Object[] { "nathan.rose@ichotelsgroup.com", "", "", "", ""} )
    };
    
    private NexusServiceInfo(String serviceName, String serviceClassName, String editorClassName,
            String serviceURI, String[] variableNames,
            String[] variableTypes, Object[] initialValues) {
        this.serviceName = serviceName;
        this.serviceClassName = serviceClassName;
        this.editorClassName = editorClassName;
        this.serviceURI = serviceURI;
        this.variableNames = variableNames;
        this.variableTypes = variableTypes;
        this.initialValues = initialValues;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getServiceClassName() {
        return serviceClassName;
    }
    
    public String getEditorClassName() {
        return editorClassName;
    }

    public String getServiceURI() {
        return serviceURI;
    }

    public String[] getVariableNames() {
        return variableNames;
    }

	public Object[] getInitialValues() {
		return initialValues;
	}

	public String[] getVariableTypes() {
		return variableTypes;
	}
}
