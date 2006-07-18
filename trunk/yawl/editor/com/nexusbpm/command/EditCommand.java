/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.beans.PropertyVetoException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * The EditCommand is the common ancestor for all editor commands that
 * modify YAWL objects
 * 
 * @author Matthew Sandoz
 *
 */
public class EditCommand implements Command{

	private static final Log LOG = LogFactory.getLog(EditCommand.class);
	private EditorDataProxy proxy; 
	private String attribute; 
	private Object value;
	private Object oldValue;
	
	public EditCommand(EditorDataProxy proxy, String attribute, Object value) {
		this.proxy = proxy;
		this.attribute = attribute;
		this.value = value;
		this.oldValue = proxy.getAttribute(attribute);
	}

	public void execute() {
		try {
			proxy.setAttribute(attribute, value);
		} catch (PropertyVetoException e) {
			LOG.error("Attempt to set " + attribute + " failed", e);
		}
	}
	
	public void undo() {
		try {
			proxy.setAttribute(attribute, oldValue);
		} catch (PropertyVetoException e) {
			LOG.error("Attempt to undo set " + attribute + " failed", e);
		}
	}
	
    public void redo() {
        throw new UnsupportedOperationException(
                "nexus insert undo not yet implemented");
    }
    
    public boolean supportsUndo() {
        return true;
    }
}
