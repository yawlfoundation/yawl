/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * The CreateFlowCommand creates a flow between two external net elements.
 * The created flow is stored in the command for later undoing. The created
 * flow is minimally configured and must be customized afterwards.
 * 
 * @author Matthew Sandoz
 *
 */
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
