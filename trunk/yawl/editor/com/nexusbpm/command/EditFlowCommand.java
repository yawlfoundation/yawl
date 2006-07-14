package com.nexusbpm.command;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class EditFlowCommand extends EditCommand {

	public EditFlowCommand(EditorDataProxy proxy, String attribute, Object value) {
		super(proxy, attribute, value);
	}
	
}
