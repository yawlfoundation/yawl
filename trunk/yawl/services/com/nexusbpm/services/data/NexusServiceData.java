/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jdom.Content;
import org.jdom.Element;

import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YVariable;

import com.nexusbpm.services.NexusServiceConstants;

/**
 * Data container for Nexus Services. Instances of this class contain a list of dynamic
 * variables that can be marshalled/unmarshalled between POJO and XML form for use inside
 * YAWL specifications, XFire, and the Nexus Services themselves.<br>
 * Provides base 64 encoding for storage of binary data and java objects.
 * 
 * @author Nathan Rose
 */
@XmlRootElement(name = "NexusServiceData", namespace = "http://www.nexusworkflow.com/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NexusServiceData", namespace = "http://www.nexusworkflow.com/", propOrder = { "variable" })
public class NexusServiceData implements Cloneable {
    @XmlElement(namespace = "http://www.nexusworkflow.com/", required = true)
    private List<Variable> variable;
    
    private void initList() {
        if( variable == null ) {
            variable = new ArrayList<Variable>();
        }
    }
    
    /**
     * @return a newly created (ie: safe to modify) list of the names of all the variables.
     */
    public List<String> getVariableNames() {
        int size = 1;
        if( variable != null ) {
            size += variable.size();
        }
        List<String> variableNames = new ArrayList<String>( size );
        for( Variable v : variable ) {
            variableNames.add( v.getName() );
        }
        return variableNames;
    }
    
    /**
     * @see Variable#get()
     */
    public Object get( String variableName ) throws IOException, ClassNotFoundException {
        return getVariable( variableName ).get();
    }
    
    /**
     * @see Variable#getPlain()
     */
    public String getPlain( String variableName ) {
        return getVariable( variableName ).getPlain();
    }
    
    /**
     * @see Variable#getBase64()
     */
    public String getBase64( String variableName ) {
        return getVariable( variableName ).getBase64();
    }
    
    /**
     * @see Variable#getBinary()
     */
    public byte[] getBinary( String variableName ) {
        return getVariable( variableName ).getBinary();
    }
    
    /**
     * @see Variable#getObject()
     */
    public Object getObject( String variableName ) throws IOException, ClassNotFoundException {
        return getVariable( variableName ).getObject();
    }
    
    /**
     * @see Variable#getType()
     */
    public String getType( String variableName ) {
        return getVariable( variableName ).getType();
    }
    
    /**
     * @see Variable#getValue()
     */
    public String getEncodedValue( String variableName ) {
    	return getVariable( variableName ).getEncodedValue();
    }
    
    /**
     * @see Variable#set(Object)
     */
    public void set( String variableName, Object value ) throws IOException {
        getOrCreateVariable( variableName ).set( value );
    }
    
    /**
     * @see Variable#setPlain(String)
     */
    public void setPlain( String variableName, String value ) {
        getOrCreateVariable( variableName ).setPlain( value );
    }
    
    /**
     * @see Variable#setBase64(String)
     */
    public void setBase64( String variableName, String value ) {
        getOrCreateVariable( variableName ).setBase64( value );
    }
    
    /**
     * @see Variable#setBinary(byte[])
     */
    public void setBinary( String variableName, byte[] value ) {
        getOrCreateVariable( variableName ).setBinary( value );
    }
    
    /**
     * @see Variable#setObject(Object)
     */
    public void setObject( String variableName, Object value ) throws IOException {
        getOrCreateVariable( variableName ).setObject( value );
    }
    
    /**
     * @see Variable#setType(String)
     */
    public void setType( String variableName, String type ) {
        getOrCreateVariable( variableName ).setType( type );
    }
    
    private Variable getVariable( String variableName ) {
        initList();
        for( Iterator<Variable> iter = variable.iterator(); iter.hasNext(); ) {
            Variable v = iter.next();
            if( v.getName().equals( variableName ) ) {
                return v;
            }
        }
        return Variable.getNullVariable();
    }
    
    private Variable getOrCreateVariable( String variableName ) {
        initList();
        for( Iterator<Variable> iter = variable.iterator(); iter.hasNext(); ) {
            Variable v = iter.next();
            if( v.getName().equals( variableName ) ) {
                return v;
            }
        }
        Variable v = new Variable();
        v.setName( variableName );
        variable.add( v );
        return v;
    }
    
    public void removeVariable(String name) {
    	Variable varToRemove = null;
    	for (Variable v: variable) {
    		if (v.getName().equals(name)) varToRemove = v;
    	}
    	if (varToRemove != null) variable.remove(varToRemove); 
    }
    
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append( this.getClass().toString() ).append( ".toString():\n" );
        initList();
        b.append( "Nuber of variables:" ).append( variable.size() ).append( "\n" );
        for( Iterator<Variable> iter = variable.iterator(); iter.hasNext(); ) {
            Variable v = iter.next();
            b.append( v.getName() ).append( ":" );
            if( v.getValue() != null ) {
                b.append( ":" ).append( v.getValue() ).append( "\n" );
            }
            else {
                b.append( "null\n" );
            }
        }
        return b.toString();
    }
    
    public void addStatusMessage( String message ) {
        String status = "";
        if( getType( "Status" ) != null
                && getType( "Status" ).equals( Variable.TYPE_TEXT )
                && getPlain( "Status" ) != null ) {
            status = getPlain( "Status" );
        }
        if( status.length() > 0 ) {
            status += "\n";
        }
        status = status + message;
        setPlain( "Status", status );
    }
    
    /**
     * Given runtime data from the service invoker, converts the data into an
     * instance of NexusServiceData.
     */
    public static NexusServiceData unmarshal( List<Content> variables ) {
        NexusServiceData data = new NexusServiceData();
        data.initList();
        
        for( Content c : variables ) {
            if( c instanceof Element ) {
                Element e = (Element) c;
                
                data.unmarshalVariable( e.getName(), e.getText() );
            }
        }
        
        return data;
    }
    
    public static NexusServiceData unmarshal( YTask task, boolean includeStatusVariable ) {
        NexusServiceData data = new NexusServiceData();
        data.initList();
        
        String taskID = task.getID();
        
        for( String varName : task.getDataMappingsForTaskStarting().keySet() ) {
            if( !( varName.equals( NexusServiceConstants.SERVICENAME_VAR )
                    || ( varName.equals( NexusServiceConstants.STATUS_VAR ) && !includeStatusVariable )
                    || varName.equals( "YawlWSInvokerWSDLLocation" )
                    || varName.equals( "YawlWSInvokerOperationName" )
                    || varName.equals( "YawlWSInvokerPortName" ) ) ) {
                String val = null;
                if( task.getParent().getLocalVariable(
                        taskID + NexusServiceConstants.NAME_SEPARATOR + varName ) != null ) {
                    val = task.getParent().getLocalVariable(
                            taskID + NexusServiceConstants.NAME_SEPARATOR + varName ).getInitialValue();
                }
                
                data.unmarshalVariable( varName, val );
            }
        }
        
        return data;
    }
    
    private void unmarshalVariable( String name, String value ) {
        String type = "text";
        String finalValue = value;
        
        if( finalValue != null && finalValue.indexOf( ":" ) > 0 ) {
            String candidateType = finalValue.substring( 0, finalValue.indexOf( ":" ) );
            candidateType = candidateType.trim().toLowerCase();
            // actual value is only the part after the colon
            finalValue = finalValue.substring( finalValue.indexOf( ":" ) + 1 );
            
            if( finalValue.equals( "null" ) ) {
                finalValue = null;
            }
            else {
                finalValue = finalValue.substring( 1 );
            }
            
            if( candidateType.equals( "text" ) ) {
                // do nothing, type "text" is default
            }
            else if( candidateType.equals( "base64" ) ) {
                type = "base64";
            }
            else if( candidateType.equals( "binary" ) ) {
                type = "binary";
            }
            else if( candidateType.equals( "object" ) ) {
                type = "object";
            }
            else {
                // the colon in the value must be incidental, so restore the value
                finalValue = value;
            }
        }
        
        Variable variable = new Variable( name, type, finalValue );
        this.variable.add( variable );
    }
    
    public static List<Content> marshal( NexusServiceData data ) {
        List<Content> variables = new ArrayList<Content>( data.getVariableNames().size() + 1 );
        
        data.initList();
        for( Variable v : data.variable ) {
            Element e = new Element( v.getName() );
            if( v.getValue() != null ) {
                e.setText( v.getType() + "::" + v.getValue() );
            }
            else {
                e.setText( v.getType() + ":null" );
            }
            variables.add( e );
        }
        
        return variables;
    }
    
    /**
     * @param net the net containing the task.
     * @param taskID the ID of the task to save the variables for.
     */
    public void marshal( YNet net, String taskID ) {
        
        for( Variable v : variable ) {
            String name = taskID + NexusServiceConstants.NAME_SEPARATOR + v.name;
            String value = v.getEncodedValue();
            
            if( net.getLocalVariable( name ) != null ) {
                net.getLocalVariable( name ).setInitialValue( value );
            }
            else {
                YVariable var = new YVariable( net );
                var.setDataTypeAndName(NexusServiceConstants.VARTYPE_STRING, name, NexusServiceConstants.XML_SCHEMA_URL);
                var.setInitialValue( value );
                net.setLocalVariable( var );
            }
        }
    }
    
    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        NexusServiceData clone = (NexusServiceData) super.clone();
        
        if( clone.variable != null ) {
            clone.variable = new ArrayList<Variable>();
            
            for( Variable var : this.variable ) {
                clone.variable.add( (Variable) var.clone() );
            }
        }
        
        return clone;
    }
}
