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
 * The CopyNetCommand provides a way of copying a net from one 
 * YSpecification to another. Some different things to think about
 * as we do this are: <p>
 *
 * How deep should the copy go?<p>
 * What to do about existing elements that may cause naming collisions<p> 
 * 
 * @author Matthew Sandoz
 *
 */
public class CopyNetCommand implements Command{

	EditorDataProxy netProxy;
	
	public CopyNetCommand(EditorDataProxy netProxy) {
		this.netProxy = netProxy;
	}
	
	public void execute() {
		
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
