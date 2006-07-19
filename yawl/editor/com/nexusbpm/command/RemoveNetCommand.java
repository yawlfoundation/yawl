/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.List;

import au.edu.qut.yawl.elements.YCompositeTask;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * The RemoveNetCommand removes a YNet from a YSpecification
 * 
 * @author Matthew Sandoz
 *
 */
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
		netToRemove.getContext().delete(netToRemove);
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
    
    public void redo() {
        throw new UnsupportedOperationException(
                "nexus insert undo not yet implemented");
    }
    
    public boolean supportsUndo() {
        return true;
    }
}
