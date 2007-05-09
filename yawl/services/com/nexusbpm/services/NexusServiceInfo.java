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
    private String serviceName;
    /** The name of the class implementing the service. */
    private String serviceClassName; // use the class name so that this class will still work
                                           // even if used without the services being on the classpath
    /** The name of the editor's class for tasks that use this nexus service. */
    /** The URI where the service can be accessed. */
    private String serviceURI; // TODO get URI from properties file
    /** The variables that the service will use. */
    private String[] variableNames;
    private String[] variableTypes;
    private Object[] initialValues;
    
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
                "internal://Jython",
                new String[] { "code", "output", "error" },
                new String[] { Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT },
                new Object[] { "print 'foo'", "", "" }),
        new NexusServiceInfo( "EmailSender",
                "com.nexusbpm.services.email.EmailSenderService",
                "internal://EmailSenderService",
                new String[] { "toAddress", "ccAddress", "bccAddress", "fromAddress", "host", "subject", "body" },
                new String[] { Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT },
                new Object[] { "nathan.rose@ichotelsgroup.com", "", "", "", "", "", ""} ),
        new NexusServiceInfo( "Ftp",
                "com.nexusbpm.services.Ftp.FtpService",
                "internal://FtpService",
                new String[] { "remoteHost", "username", "password", "remoteDir", "remoteFile", "operation" },
                new String[] { Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT },
                new Object[] { "", "", "", "", "", "" }),
        new NexusServiceInfo( "Shell",
                "com.nexusbpm.services.Shell.ShellService",
                "internal://ShellService",
                new String[] { "code", "output", "error" },
                new String[] { Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT },
                new Object[] { "cd ~;ls -la", "", "" }),
        new NexusServiceInfo( "R",
                "com.nexusbpm.services.R.RService",
                "internal://RService",
                new String[] { "code", "output", "error" },
                new String[] { Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT },
                new Object[] { "//R Code goes here", "", "" }),
        new NexusServiceInfo( "Excel",
                "com.nexusbpm.services.excel.ExcelService",
                "internal://ExcelService",
                new String[] { "output", "template", "rowLimit", "columnLimit", "sheetName", "excelAnchor", "outputType" },
                new String[] { Variable.TYPE_BINARY, Variable.TYPE_BINARY, 
        				Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT },
                new Object[] { null, null, 0, 0, "", "", "" }),
        new NexusServiceInfo( "Sql",
                "com.nexusbpm.services.sql.SqlService",
                "internal://SqlService",
                new String[] { "sql", "userName", "password", "databaseAddress", "databaseName", "databaseType", "isQuery", "output" },
                new String[] {  Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_BINARY},
                new Object[] { "", "", "", "", "", "", "", null }), 
        new NexusServiceInfo( "Sas",
                "com.nexusbpm.services.sas.SasService",
                "internal://SasService",
                new String[] { "code", "output", "log", "userName", "password"  },
                new String[] { Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT, Variable.TYPE_TEXT },
                new Object[] { "//SAS Code from NexusBPM", "", "" , "" , "" })
    };

    public NexusServiceInfo() {
		super();
	}
    
    protected NexusServiceInfo(String serviceName, String serviceClassName, String serviceURI, String[] variableNames,
            String[] variableTypes, Object[] initialValues) {
        this.serviceName = serviceName;
        this.serviceClassName = serviceClassName;
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

	protected void setInitialValues(Object[] initialValues) {
		this.initialValues = initialValues;
	}

	protected void setServiceClassName(String serviceClassName) {
		this.serviceClassName = serviceClassName;
	}

	protected void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	protected void setServiceURI(String serviceURI) {
		this.serviceURI = serviceURI;
	}

	protected void setVariableNames(String[] variableNames) {
		this.variableNames = variableNames;
	}

	protected void setVariableTypes(String[] variableTypes) {
		this.variableTypes = variableTypes;
	}
}
