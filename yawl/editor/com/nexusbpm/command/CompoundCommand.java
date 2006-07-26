/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.List;

/**
 * The CompoundCommand executes, undoes and redoes other commands in a batch.
 * If any of the commands in the compound command cannot be undone, then the
 * compound command itself cannot be undone.
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class CompoundCommand implements Command  {

	private List<Command> commands;
	
	public CompoundCommand(List<Command> commands) {
		this.commands = commands;
	}

	public void execute() throws Exception {
		for (Command command: commands) {
			command.execute();
		}
	}

	public void redo() throws Exception {
		for (Command command: commands) {
			command.redo();
		}
	}

	public boolean supportsUndo() {
		boolean retval = true;
		for( Command command: commands ) {
			if( ! command.supportsUndo() ) {
				retval = false;
			}
		}
		return retval;
	}

	public void undo() throws Exception {
        if( supportsUndo() ) {
            for (int i = commands.size() - 1; i >= 0; i--) {
				commands.get(i).undo();
            }
        }
        else {
            throw new UnsupportedOperationException( "This compound command cannot be undone!" );
        }
	}
	
}
