package com.nexusbpm.command;

import java.util.Collection;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class RemoveFlowCommand implements Command{

	EditorDataProxy flowToRemove;
	
	public RemoveFlowCommand(EditorDataProxy proxy) {
		flowToRemove = proxy;
	}
	
	public void execute() {
		YFlow flow = (YFlow) flowToRemove.getData();
		YExternalNetElement priorElement = flow.getPriorElement();
		YExternalNetElement nextElement = flow.getNextElement();
		Collection<YFlow> flowsToCandidates = priorElement.getPostsetFlows();
		Collection<YFlow> flowsFromCandidates = nextElement.getPresetFlows();
		flowsToCandidates.remove(flow);
		flowsFromCandidates.remove(flow);
	}
	
	public void undo() {
		YFlow flow = (YFlow) flowToRemove.getData();
		YExternalNetElement priorElement = flow.getPriorElement();
		YExternalNetElement nextElement = flow.getNextElement();
		Collection<YFlow> flowsToCandidates = priorElement.getPostsetFlows();
		Collection<YFlow> flowsFromCandidates = nextElement.getPresetFlows();
		flowsToCandidates.add(flow);
		flowsFromCandidates.add(flow);
	}
}
