package com.nexusbpm.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.persistence.EditorDataProxy;

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
