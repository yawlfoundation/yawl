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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A single dynamic variable for use in {@link NexusServiceData}.
 * 
 * @see NexusServiceData
 * @author Nathan Rose
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="variable", namespace="http://www.nexusworkflow.com/", propOrder = {
		"value"})
public class Variable implements Cloneable {
    /** Type constant for plain text variables. */
    public static final String TYPE_TEXT = "text";
    /** Type constant for base 64 encoded string variables. */
    public static final String TYPE_BASE64 = "base64";
    /** Type constant for base 64 encoded byte array variables. */
    public static final String TYPE_BINARY = "binary";
    /** Type constant for base 64 encoded java object variables. */
    public static final String TYPE_OBJECT = "object";
    
	@XmlAttribute(required=true)
	protected String name;
    @XmlAttribute(required=true)
    protected String type;
	@XmlElement(namespace="http://www.nexusworkflow.com/")
	protected String value;
	
	public Variable() {};
	
	public Variable( String name, String type, String value ) {
		setName( name );
        setType( type );
		setValue( value );
	}
	
	public String getName() {
		return name;
	}
	
	public void setName( String name ) {
		this.name = name;
	}
    
    public String getType() {
        return ( type != null ) ? type : TYPE_TEXT;
    }
    
    public void setType( String type ) {
        if( TYPE_BASE64.equals( type ) ) {
            this.type = TYPE_BASE64;   
        }
        else if( TYPE_BINARY.equals( type ) ) {
            this.type = TYPE_BINARY;
        }
        else if( TYPE_OBJECT.equals( type ) ) {
            this.type = TYPE_OBJECT;
        }
        else {
            this.type = TYPE_TEXT;
        }
    }
	
	/**
	 * Do not call this directly, instead call {@link Variable#get()}
	 * so the value gets decoded.
	 */
	String getValue() {
		return value;
	}
	
	/**
	 * Do not call this directly, instead call {@link Variable#set(Object)},
     * {@link Variable#setBase64(String)}, {@link Variable#setBinary(byte[])},
     * {@link Variable#setObject(Object), or {@link Variable#setPlain(String)}
	 * so the value gets encoded properly.
	 */
	void setValue( String value ) {
		this.value = value;
	}
    
    /**
     * Gets the appropriately decoded value of the variable for the variable's type.
     */
    public Object get() throws IOException, ClassNotFoundException {
        if( getType().equals( TYPE_BASE64 ) ) {
            return getBase64();
        }
        else if( getType().equals( TYPE_BINARY ) ) {
            return decodeBinaryObject( getValue() );
        }
        else if( getType().equals( TYPE_OBJECT ) ) {
            return getObject();
        }
        else {
            return getPlain();
        }
    }
    
    /**
     * Returns the plain text value for the variable.
     */
    public String getPlain() {
        return decodePlain( getValue() );
    }
    
    /**
     * Returns the base64 decoded value for the variable.
     */
    public String getBase64() {
        return decodeBase64( getValue() );
    }
    
    /**
     * Returns the base 64 decoded value for the variable as a byte array.
     */
    public byte[] getBinary() {
        return decodeBinary( getValue() );
    }
    
    /**
     * Returns the decoded object for the variable.
     */
    public Object getObject() throws IOException, ClassNotFoundException {
        return decodeObject( getValue() );
    }
    
    String getEncodedValue() {
    	if( getValue() == null ) {
    		return getType() + ":null";
    	}
    	else {
    		return getType() + "::" + getValue();
    	}
    }
    
    /**
     * Encodes the given value as appropriate for the variable's type and sets
     * the encoded value for the variable. Note that for binary data this
     * function expects arrays of {@link Byte}s (upper case B) and not arrays
     * of bytes (lower case b).
     */
    public void set( Object value ) throws IOException {
        if( value == null ) {
            setValue( null );
        }
        else {
            if( getType().equals( TYPE_BASE64 ) ) {
                setBase64( value.toString() );
            }
            else if( getType().equals( TYPE_BINARY ) ) {
                setValue( encodeBinaryObject( (Byte[]) value ) );
            }
            else if( getType().equals( TYPE_OBJECT ) ) {
                setObject( value );
            }
            else {
                setPlain( value.toString() );
            }
        }
    }
    
    /**
     * Sets the plain-text value for the variable.
     */
    public void setPlain( String value ) {
        setType( TYPE_TEXT );
        setValue( encodePlain( value ) );
    }
    
    /**
     * Encodes the given value into base64 encoding and sets the variable to the
     * encoded value.
     */
    public void setBase64( String value ) {
        setType( TYPE_BASE64 );
        setValue( encodeBase64( value ) );
    }
    
    /**
     * Encodes the given value into base64 encoding and sets the variable to the
     * encoded value.
     */
    public void setBinary( byte[] value ) {
        setType( TYPE_BINARY );
        setValue( encodeBinary( value ) );
    }
    
    /**
     * Encodes the given object into base64 encoding and sets the variable to the
     * encoded value.
     */
    public void setObject( Object value ) throws IOException {
        setType( TYPE_OBJECT );
        setValue( encodeObject( value ) );
    }
    
    private static Variable nullVariable;
    
    static Variable getNullVariable() {
        if( nullVariable == null ) {
            nullVariable = new NullVariable();
        }
        return nullVariable;
    }
    
    private static class NullVariable extends Variable {
        public Object get() {
            return null;
        }
        public String getBase64() {
            return null;
        }
        public byte[] getBinary() {
            return null;
        }
        public Object getObject() {
            return null;
        }
        public String getPlain() {
            return null;
        }
        String getValue() {
            return null;
        }
        public String getType() {
            return null;
        }
        public void set( Object value ) throws IOException {
        }
        public void setBase64( String value ) {
        }
        public void setBinary( byte[] value ) {
        }
        public void setObject( Object value ) throws IOException {
        }
        public void setPlain( String value ) {
        }
        void setValue( String value ) {
        }
        public void setType( String type ) {
        }
    }
    
    private static String encodePlain( String str ) {
        if( str == null ) {
            return null;
        }
        else {
            return str
                .replaceAll( "%", "%p" )
                .replaceAll( "<", "%l" )
                .replaceAll( ">", "%r" )
                .replaceAll( "&", "%a" );
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
                .replaceAll( "\\+", "_" )
                .replaceAll( "/", "-" )
                .replaceAll( "=", "." );
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
                .replaceAll( "\\+", "_" )
                .replaceAll( "/", "-" )
                .replaceAll( "=", "." );
        }
    }
    
    private static String encodeBinaryObject( Byte[] value ) {
        if( value == null ) {
            return null;
        }
        else {
            byte[] temp = new byte[ value.length ];
            for( int index = 0; index < temp.length; index++ ) {
                temp[ index ] = value[ index ].byteValue();
            }
            return encodeBinary( temp );
        }
    }
    
    private static String encodeObject( Object value ) throws IOException {
        if( value == null ) {
            return null;
        }
        else {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream( bout );
            out.writeObject( value );
            out.flush();
            return encodeBinary( bout.toByteArray() );
        }
    }
    
    private static String decodePlain( String str ) {
        if( str == null ) {
            return null;
        }
        else {
            return str
                .replaceAll( "%a", "&" )
                .replaceAll( "%r", ">" )
                .replaceAll( "%l", "<" )
                .replaceAll( "%p", "%" );
        }
    }
    
    private static String decodeBase64( String str ) {
        if( str == null ) {
            return null;
        }
        else {
            return Base64Coder.decode(
                    str.replaceAll( "\\.", "=" )
                        .replaceAll( "-", "/" )
                        .replaceAll( "_", "+" ) );
        }
    }
    
    private static byte[] decodeBinary( String str ) {
        if( str == null ) {
            return null;
        }
        else {
            return Base64Coder.decode(
                    str.replaceAll( "\\.", "=" )
                        .replaceAll( "-", "/" )
                        .replaceAll( "_", "+" )
                        .toCharArray() );
        }
    }
    
    private static Byte[] decodeBinaryObject( String str ) {
        byte[] temp = decodeBinary( str );
        if( temp == null ) {
            return null;
        }
        else {
            Byte[] ret = new Byte[ temp.length ];
            for( int index = 0; index < temp.length; index++ ) {
                ret[ index ] = Byte.valueOf( temp[ index ] );
            }
            return ret;
        }
    }
    
    private static Object decodeObject( String str ) throws IOException, ClassNotFoundException {
        byte[] temp = decodeBinary( str );
        if( temp == null ) {
            return null;
        }
        else {
            ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream( temp ) );
            return in.readObject();
        }
    }
    
    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    @Override
    public String toString() {
    	return this.type + " " + this.name + "='" + this.value.substring(0, 20) + "'";
    }

}
