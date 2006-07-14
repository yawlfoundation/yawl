package com.nexusbpm.command;

import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.SharedNode;

public class CopySpecificationCommand implements Command{

	public EditorDataProxy source;
	public EditorDataProxy target;
	
	public CopySpecificationCommand(EditorDataProxy source, EditorDataProxy target) {
		this.source = source;
		this.target = target;
	}
	
	public void execute() {
		//steal from: 
		//EditorCommand.executeCopyCommand(source, target);
	}
	
	public void undo() {
		
	}
	
}
