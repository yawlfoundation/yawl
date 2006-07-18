/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * The EditTaskCommand updates task data such as name etc. If the ID changes
 * we have to also change the variable names and mappings and so forth to
 * maintain a consistent state.
 * 
 * @author Matthew Sandoz
 *
 */
public class EditTaskCommand extends EditCommand{

	private static final Log LOG = LogFactory.getLog(EditTaskCommand.class);
	private EditorDataProxy proxy; 
	private String attribute; 
	private Object value;
	private Object oldValue;
	
	public EditTaskCommand(EditorDataProxy proxy, String attribute, Object value) {
		super(proxy, attribute, value);
	}

}
