package com.nexusbpm.command;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class EditSpecificationCommand extends EditCommand{

	public EditSpecificationCommand(EditorDataProxy proxy, String attribute, Object value) {
		super(proxy, attribute, value);
	}

}
