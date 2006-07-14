package com.nexusbpm.command;

import java.util.Collection;
import java.util.List;

import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class RemoveNetCommand implements Command{

	EditorDataProxy netToRemove;
	
	public RemoveNetCommand(EditorDataProxy proxy) {
		netToRemove = proxy;
	}
	
	public void execute() {
		YNet net = (YNet) netToRemove.getData();
		YSpecification spec = net.getParent();
		List<YDecomposition> decomps = spec.getDecompositions();
		if (spec.getRootNet() == net) {
			spec.setRootNet(null);
		}
		decomps.remove(net);
		netToRemove.getContext().remove(netToRemove);
		for (YDecomposition decomp: decomps) {
			if (decomp instanceof YNet) {
				net = (YNet) decomp;
				List<YExternalNetElement> elements = net.getNetElements();
				for (YExternalNetElement element: elements) {
					if (element instanceof YCompositeTask) {
						YCompositeTask task = (YCompositeTask) element;
						if (task.getDecompositionPrototype() == net) {
							task.setDecompositionPrototype(null);
							//so we dont have to remove the composite task...
						}
					}
				}
			}
		}
	}
	
	public void undo() {
		YNet net = (YNet) netToRemove.getData();
	}

}
