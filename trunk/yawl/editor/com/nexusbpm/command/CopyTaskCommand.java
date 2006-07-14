package com.nexusbpm.command;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YNet;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class CopyTaskCommand implements Command{

	EditorDataProxy source;
	EditorDataProxy target;
	
	public CopyTaskCommand(EditorDataProxy source, EditorDataProxy targetNet) {
		this.source = source;
		this.target = target;
	}

	public void execute() {
		YExternalNetElement sourceElement = (YExternalNetElement) source.getData();
		YNet targetNet = (YNet) target.getData();
	}
	
	public void undo() {
	}
	
}
