/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Data container for Nexus Services. Instances of this class contain a list of dynamic
 * variables, encoded in base64 encoding, that can be marshalled/unmarshalled between
 * POJO and XML form for use inside YAWL specifications, XFire, and the Nexus Services
 * themselves.
 * 
 * @author Nathan Rose
 */
@XmlRootElement(name="NexusServiceData", namespace="http://www.nexusworkflow.com/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="NexusServiceData", namespace="http://www.nexusworkflow.com/", propOrder = {
		"variable"})
public class NexusServiceData {
	@XmlElement(namespace="http://www.nexusworkflow.com/", required=true)
	private List<Variable> variable;
	
	/**
	 * Returns the un-encoded value for the variable with the specified name. If no
	 * variable with the specified name exists, then <tt>null</tt> is returned.
	 */
	public String get( String variableName ) {
		if( variable == null ) {
			variable = new ArrayList<Variable>();
		}
		for( Variable v : variable ) {
			if( v.getName().equals( variableName ) )
				return decode( v.getValue() );
		}
		return null;
	}
	
	public List<Variable> getVariables() {
		return variable;
	}
	
	/**
	 * Encodes the given value into base64 encoding and sets the variable with the
	 * given name to the encoded value. If no variable with the given name exists yet,
	 * one will be created.
	 */
	public void set( String name, String value ) {
		if( name == null ) {
			throw new NullPointerException( "Cannot set a variable without a name!" );
		}
		if( variable == null ) {
			variable = new ArrayList<Variable>();
		}
		for( Variable v : variable ) {
			if( v.getName().equals( name ) ) {
				v.setValue( encode( value ) );
				return;
			}
		}
		variable.add( new Variable( name, encode( value ) ) );
	}
	
	private static String encode( String str ) {
		if( str == null ) {
			return null;
		}
		else {
			return Base64Coder.encode( str )
				// further modify the base64 value to remove characters that are unsafe
				// in certain circumstances (URLs, filenames, etc)
				.replaceAll( "\\+", "_")
				.replaceAll( "/", "-" )
				.replaceAll( "=", ".");
		}
	}
	
	private static String decode( String str ) {
		if( str == null ) {
			return null;
		}
		else {
			return Base64Coder.decode(
					str
					.replaceAll( "\\.", "=" )
					.replaceAll( "-", "/" )
					.replaceAll( "_", "+" ) );
		}
	}
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append( this.getClass().toString() )
			.append( "\n" );
		if( variable == null ) {
			variable = new ArrayList<Variable>();
		}
		b.append( variable.size() )
			.append( "\n" );
		for( Iterator<Variable> iter = variable.iterator(); iter.hasNext(); ) {
			Variable v = iter.next();
			b.append( v.getName() )
				.append( ":" )
				.append( v.getValue() )
				.append( "\n" );
		}
		return b.toString();
	}
	
	public static void main(String[] args) {
		String str = Base64Coder.encode( "print foo + ' ' + bar + ' ' + baz\nprint '5'" );
		System.out.println( str );
		str = Base64Coder.encode( "itsy" );
		System.out.println( str );
		str = Base64Coder.encode( "bitsy" );
		System.out.println( str );
		str = Base64Coder.encode( "spider" );
		System.out.println( str );
		System.out.println("-");
		str = Base64Coder.decode( "aXRzeSBiaXRzeSBzcGlkZXINCjUNCg==" );
		System.out.println( str );
		System.out.println("-");
	}
}
