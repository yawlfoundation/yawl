/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YNet;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * The CopyTaskCommand copies a task to a net. All existing YFlows are 
 * disconnected from the task. Also the mapping names must change to the
 * new parent net's name. Variables must be added to the net, and a gateway
 * may be added to the specification if it does not already exist in the 
 * necessary form.
 * 
 * @author Matthew Sandoz
 */
public class CopyTaskCommand implements Command{

	EditorDataProxy source;
	EditorDataProxy target;
	
	public CopyTaskCommand(EditorDataProxy source, EditorDataProxy targetNet) {
		this.source = source;
		this.target = targetNet;
	}

	public void execute() {
		YExternalNetElement sourceElement = (YExternalNetElement) source.getData();
		YNet targetNet = (YNet) target.getData();
	}
	
	public void undo() {
	}
	
    public void redo() {
        throw new UnsupportedOperationException(
                "nexus insert undo not yet implemented");
    }
}
