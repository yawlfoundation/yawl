package com.nexusbpm.command;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class CreateFlowCommand implements Command{

	EditorDataProxy source;
	EditorDataProxy target;
	DataProxy<YFlow> createdFlow;
	
	public CreateFlowCommand(EditorDataProxy source, EditorDataProxy target) {
		this.source = source;
		this.target = target;
	}

	public void execute() {
		YExternalNetElement sourceElement = (YExternalNetElement) source.getData();
		YExternalNetElement targetElement = (YExternalNetElement) target.getData();
		YFlow flow = new YFlow(sourceElement, targetElement);
		sourceElement.getPostsetFlows().add(flow);
		targetElement.getPresetFlows().add(flow);
		createdFlow = source.getContext().getDataProxy(flow, null);
	}
	
	public void undo() {
		YExternalNetElement sourceElement = (YExternalNetElement) source.getData();
		YExternalNetElement targetElement = (YExternalNetElement) target.getData();
		sourceElement.getPostsetFlows().remove(createdFlow.getData());
		targetElement.getPresetFlows().remove(createdFlow.getData());
		source.getContext().remove(createdFlow);
	}
}
