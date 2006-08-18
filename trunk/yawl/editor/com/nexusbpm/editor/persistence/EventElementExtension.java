/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.persistence;

import org.jdom.Element;
import org.jdom.Namespace;

import au.edu.qut.yawl.elements.ElementExtension;
import au.edu.qut.yawl.elements.ExtensionListContainer;

public class EventElementExtension extends ElementExtension {
	public static Namespace EVENT_NAMESPACE = Namespace.getNamespace("http://www.nexusbpm.com");
	public static String EVENT_ROOT_ELEMENT = "eventExtension";
	protected static String domainString = "YAWL Event";
	public String getDomain() {return domainString;}
	public void ensureRootElementExists() {
		super.ensureRootElementExists();
		Element root = getRootElement();
		Element child = root.getChild(EVENT_ROOT_ELEMENT, EVENT_NAMESPACE);
		if (child == null) {
			child = new Element(EVENT_ROOT_ELEMENT, EVENT_NAMESPACE);
			root.getChildren().add(child);
		}
	}
	public EventElementExtension(ExtensionListContainer t) {
		super(t);
	}
	
	public static String CONSOLE_LOGGING_ELEMENT = "console";
	public static String SPRING_LOGGING_ELEMENT = "spring";
	public static String JMS_LOGGING_ELEMENT = "jms";
	
	public boolean isConsoleLogging() {
		return getAttribute(CONSOLE_LOGGING_ELEMENT) != null;
	}
	
	public void setConsoleLogging( boolean logging ) {
		setLogging( logging, CONSOLE_LOGGING_ELEMENT );
	}
	
	public boolean isSpringLogging() {
		return getAttribute(SPRING_LOGGING_ELEMENT) != null;
	}
	
	public void setSpringLogging( boolean logging ) {
		setLogging( logging, SPRING_LOGGING_ELEMENT );
	}
	
	public boolean isJMSLogging() {
		return getAttribute(JMS_LOGGING_ELEMENT) != null;
	}
	
	public void setJMSLogging( boolean logging ) {
		setLogging( logging, JMS_LOGGING_ELEMENT );
	}
	
	private void setLogging( boolean logging, String element ) {
		this.ensureRootElementExists();
		if( logging && getAttribute( element ) == null ) {
			getRootElement().getChild( EVENT_ROOT_ELEMENT, EVENT_NAMESPACE )
				.addContent( new Element( element ) );
		}
		else if( !logging && getAttribute( element ) != null ) {
			getAttribute( element ).detach();
		}
	}
	
    private Element getAttribute(String element) {
        if( getRootElement().getChild(EVENT_ROOT_ELEMENT, EVENT_NAMESPACE) != null ) {
        	return getRootElement().getChild( EVENT_ROOT_ELEMENT, EVENT_NAMESPACE )
        		.getChild( element, EVENT_NAMESPACE );
        }
        else
            return null;
    }
}