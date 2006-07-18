/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * 
 * The CopySpecificationCommand copies an entire specification
 * from one DataContext to another.
 * 
 * @author Matthew Sandoz
 *
 */
public class CopySpecificationCommand implements Command{

	public EditorDataProxy source;
	public EditorDataProxy target;
	
	public CopySpecificationCommand(EditorDataProxy source, EditorDataProxy target) {
		this.source = source;
		this.target = target;
	}
	
	public void execute() {
		//steal from: 
		//EditorCommand.executeCopyCommand(source, target);
	}
	
	public void undo() {
		
	}
    
    public void redo() {
        throw new UnsupportedOperationException(
                "nexus insert undo not yet implemented");
    }
    
    public boolean supportsUndo() {
        return true;
    }
}
