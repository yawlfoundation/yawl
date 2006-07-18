/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

/**
 * Command is the root interface which all editor commands must implement.
 * By convention if the undo command is not supported, throw a 
 * java.lang.UnsupportedOperationException with a description.
 *  
 * @author Matthew Sandoz
 *
 */
public interface Command {

	/**
	 * As this takes no arguments, pass all needed information into the
	 * constructor and hold as state. Be certain that enough information 
	 * is held to be able to undo the operation later if desired.
	 */
	public void execute();
	
	
	/**
	 * Undoes the previous command. Relies on internal state of the command.
	 */
	public void undo();
	
    public void redo();
}
