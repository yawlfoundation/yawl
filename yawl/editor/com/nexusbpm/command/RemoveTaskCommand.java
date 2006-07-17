package com.nexusbpm.command;

import java.util.ArrayList;
import java.util.List;

import operation.NexusWorkflow;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.persistence.managed.DataContext;

import com.nexusbpm.editor.persistence.EditorDataProxy;

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
