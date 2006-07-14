package com.nexusbpm.command;

import java.beans.PropertyVetoException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.persistence.EditorDataProxy;

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
	
	
}
