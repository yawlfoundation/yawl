/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
 * variables that can be marshalled/unmarshalled between POJO and XML form for use inside
 * YAWL specifications, XFire, and the Nexus Services themselves.<br>
 * Provides base 64 encoding for storage of binary data.
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
	 * Returns the value of the variable with the specified name.
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
	
	/**
	 * Returns the base64 decoded value for the variable with the specified name as a
	 * string. If no variable with the specified name exists, then <tt>null</tt>
	 * is returned.
	 */
	public String getBase64( String variableName ) {
		String str = get( variableName );
		if( str == null ) {
			return null;
		}
		else {
			return decodeBase64( str );
		}
	}
	
	/**
	 * Returns the base 64 decoded value for the variable with the specified name as a
	 * byte array. If no variable with the specified name exists, then <tt>null</tt>
	 * is returned.
	 */
	public byte[] getBinary( String variableName ) {
		String str = get( variableName );
		if( str == null ) {
			return null;
		}
		else {
			return decodeBinary( str );
		}
	}
	
	/**
	 * Returns the decoded object for the variable with the specified name.
	 */
	public Object getObject( String variableName ) throws IOException, ClassNotFoundException {
		byte[] bytes = getBinary( variableName );
		ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream( bytes ) );
		return in.readObject();
	}
	
	/**
	 * Sets the value of the variable with the specified name. If no variable with
	 * the specified name yet exists, one is created.
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
	
	/**
	 * Encodes the given value into base64 encoding and sets the variable with the
	 * given name to the encoded value. If no variable with the given name exists yet,
	 * one will be created.
	 */
	public void setBase64( String name, String value ) {
		if( value == null ) {
			set( name, null );
		}
		else {
			set( name, encodeBase64( value ) );
		}
	}
	
	/**
	 * Encodes the given value into base64 encoding and sets the variable with the
	 * given name to the encoded value. If no variable with the given name exists yet,
	 * one will be created.
	 */
	public void setBinary( String name, byte[] value ) {
		if( value == null ) {
			set( name, null );
		}
		else {
			set( name, encodeBinary( value ) );
		}
	}
	
	/**
	 * Encodes the given object into base64 encoding and sets the variable with the
	 * specified name to the encoded value. If no variable with the given name exists
	 * yet, one will be created.
	 */
	public void setObject( String name, Object value ) throws IOException {
		if( name == null ) {
			throw new NullPointerException( "Cannot set a variable without a name!" );
		}
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream( bout );
		out.writeObject( value );
		out.flush();
		setBinary( name, bout.toByteArray() ); 
	}
	
	private static String encode( String str ) {
		if( str == null ) {
			return null;
		}
		else {
			return str
				.replaceAll( "e", "eb" )
				.replaceAll( "<", "el")
				.replaceAll( ">", "er")
				.replaceAll( "&", "ea")
				.replaceAll( "%", "ep");
		}
	}
	
	private static String encodeBase64( String value ) {
		if( value == null ) {
			return null;
		}
		else {
			return Base64Coder.encode( value )
				// further modify the base64 value to remove characters that are unsafe
				// in certain circumstances (URLs, filenames, etc)
				.replaceAll( "\\+", "_")
				.replaceAll( "/", "-" )
				.replaceAll( "=", ".");
		}
	}
	
	private static String encodeBinary( byte[] value ) {
		if( value == null ) {
			return null;
		}
		else {
			return new String( Base64Coder.encode( value ) )
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
			return str
				.replaceAll( "ep", "%")
				.replaceAll( "ea", "&")
				.replaceAll( "er", ">")
				.replaceAll( "el", "<")
				.replaceAll( "eb", "e");
		}
	}
	
	private static String decodeBase64( String str ) {
		if( str == null ) {
			return null;
		}
		else {
			return Base64Coder.decode( str
					.replaceAll( "\\.", "=" )
					.replaceAll( "-", "/" )
					.replaceAll( "_", "+" ) );
		}
	}
	
	private static byte[] decodeBinary( String str ) {
		if( str == null ) {
			return null;
		}
		else {
			return Base64Coder.decode( str
					.replaceAll( "\\.", "=" )
					.replaceAll( "-", "/" )
					.replaceAll( "_", "+" )
					.toCharArray() );
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
