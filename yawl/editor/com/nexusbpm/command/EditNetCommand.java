package com.nexusbpm.command;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class EditNetCommand extends EditCommand{

	public EditNetCommand(EditorDataProxy proxy, String attribute, Object value) {
		super(proxy, attribute, value);
	}

}
