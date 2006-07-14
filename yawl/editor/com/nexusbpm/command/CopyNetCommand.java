package com.nexusbpm.command;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class CopyNetCommand implements Command{

	EditorDataProxy netProxy;
	
	public CopyNetCommand(EditorDataProxy netProxy) {
		this.netProxy = netProxy;
	}
	
	public void execute() {
		
	}
	
	public void undo() {
		
	}
	
}
