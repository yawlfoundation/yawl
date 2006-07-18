/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.ArrayList;
import java.util.List;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.persistence.managed.DataContext;

import com.nexusbpm.NexusWorkflow;
import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * The RemoveTaskCommand removes a task from a network. 
 * 
 * @todo Add in parameters to remove the variables and gateway as well
 * @author Matthew Sandoz
 *
 */
public class RemoveTaskCommand implements Command{

	public EditorDataProxy taskProxy;
	
	public RemoveTaskCommand(EditorDataProxy taskProxy) {
		this.taskProxy = taskProxy;
	}
	
	public void execute() {
		DataContext context = taskProxy.getContext();
		YTask task = (YTask) taskProxy.getData();
		YNet net = task.getParent();
		YSpecification spec = net.getParent();
		YDecomposition prototype = task.getDecompositionPrototype();
		net.getNetElements().remove(task);
		if (task instanceof YAtomicTask && prototype != null) {
			spec.getDecompositions().remove(prototype);
			context.remove(context.getDataProxy(prototype, null));
		}
		if (task instanceof YCompositeTask) {
			task.setDecompositionPrototype(null);
		}
		List<YVariable> vars = new ArrayList<YVariable>(net.getLocalVariables());
		for (YVariable var: vars) {
			if (var.getName().startsWith(task.getID() + NexusWorkflow.NAME_SEPARATOR)) {
				net.getLocalVariables().remove(var);
			}
		}
		for (YFlow flow: task.getPresetFlows()) {
			context.remove(context.getDataProxy(flow, null));
		}
		for (YFlow flow: task.getPostsetFlows()) {
			context.remove(context.getDataProxy(flow, null));
		}
		task.removeAllFlows();		
		context.remove(taskProxy);
	}
	
	public void undo() {
		throw new UnsupportedOperationException("undo remove task is not implemented yet");
	}
	
}
