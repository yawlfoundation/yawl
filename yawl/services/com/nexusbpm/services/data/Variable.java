/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.services.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A single dynamic variable for use in {@link NexusServiceData}. This class is just
 * a key/value pair annotated for marshalling/unmarshalling by JAXB and XFire.
 * 
 * @author Nathan Rose
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="variable", namespace="http://www.nexusworkflow.com/", propOrder = {
		"value"})
public class Variable {
	@XmlAttribute(required=true)
	protected String name;
	@XmlElement(namespace="http://www.nexusworkflow.com/")
	protected String value;
	
	public Variable() {};
	
	public Variable( String name, String value ) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName( String name ) {
		this.name = name;
	}
	
	/**
	 * Do not call this directly, instead call {@link NexusServiceData#get(String)}
	 * so the value gets decoded.
	 */
	String getValue() {
		return value;
	}
	
	/**
	 * Do not call this directly, instead call {@link NexusServiceData#set(String, String)
	 * so the value gets encoded.
	 */
	void setValue( String value ) {
		this.value = value;
	}
}
