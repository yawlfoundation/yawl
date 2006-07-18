/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.Collection;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * The RemoveFlowCommand removes a flow from its prior's postsets and 
 * next's presets.
 * 
 * @author Matthew Sandoz
 *
 */
public class RemoveFlowCommand implements Command{

	EditorDataProxy flowToRemove;
	
	public RemoveFlowCommand(EditorDataProxy proxy) {
		flowToRemove = proxy;
	}
	
	public void execute() {
		YFlow flow = (YFlow) flowToRemove.getData();
		YExternalNetElement.removeFlow(flow);
		flowToRemove.getContext().remove(flowToRemove);
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
    
    public void redo() {
        throw new UnsupportedOperationException(
                "nexus insert undo not yet implemented");
    }
    
    public boolean supportsUndo() {
        return true;
    }
}
