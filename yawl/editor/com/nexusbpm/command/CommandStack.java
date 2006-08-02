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

/**
 * This is the list of commands that have been executed via the Capsela client,
 * allowing for undoing of specific actions, as well as redoing them once they
 * have been undone.
 * 
 * @author Daniel Gredler
 * @author Nathan Rose
 * @see Command
 */
class CommandStack {
	/**
     * The commands which have been executed thus far.
     */
    private List<Command> commands;

    /**
     * A pointer to the last executed command in the command list.
     */
    private int lastExecuted;
    
    /**
     * The maximum number of commands to store in the command stack before removing
     * the oldest commands.
     */
    private int maximumSize;
    
    /**
     * Creates a new command stack with the default maximum size of 25.
     */
    public CommandStack() {
        this( 25 );
    }
    
    /**
     * Creates a new command stack.
     */
    public CommandStack( int maximumSize ) {
        commands = new ArrayList<Command>();
        lastExecuted = -1;
        this.maximumSize = maximumSize;
    }

    /**
     * Undoes the previous command.
     * 
     * @throws Exception if the undo operation fails.
     */
    public Command undoLastCommand() throws Exception {
        if( lastExecuted >= 0 ) {
            Command cmd = commands.get( lastExecuted );
            cmd.undo();
            lastExecuted--;
            return cmd;
        }
        else {
            throw new Exception( "Cannot undo because there are no more commands to undo!" );
        }
    }

    /**
     * Redoes the next command.
     * 
     * @throws Exception if the command fails to execute.
     */
    public Command redoNextCommand() throws Exception {
        if( lastExecuted < commands.size() - 1 ) {
            Command cmd = (Command) commands.get( lastExecuted + 1 );
            cmd.redo();
            lastExecuted++;
            return cmd;
        }
        else {
            throw new Exception( "Cannot redo because there are no more commands to redo!" );
        }
    }

    /**
     * Executes the specified command and adds it to the command stack.
     * 
     * @param cmd the command to execute
     * @throws Exception if the command fails to execute.
     */
    public Command executeCommand( Command cmd ) throws Exception {
        cmd.execute();
        
        lastExecuted++;
        while( commands.size() > lastExecuted ) {
            commands.remove( commands.size() );
        }
        
        commands.add( cmd );
        
        resize();
        
        return cmd;
    }
    
    /**
     * @return the maximum number of commands that will be stored in the stack.
     */
    public int getMaximumSize() {
        return maximumSize;
    }
    
    /**
     * @param maximumSize the maximum number of commands to store in the stack.
     */
    public void setMaximumSize( int maximumSize ) {
        this.maximumSize = maximumSize;
        resize();
    }
    
    private void resize() {
        while( commands.size() > maximumSize ) {
            commands.remove( 0 );
            lastExecuted--;
        }
    }
    
    /**
     * @return whether the command stack can redo the last undone command (if any).
     */
    public boolean canRedo() {
        return ( lastExecuted < commands.size() - 1 );
    }

    /**
     * @return whether the command stack can undo the last executed command (if any).
     */
    public boolean canUndo() {
        return ( lastExecuted >= 0 ) && ( commands.get( lastExecuted ).supportsUndo() );
    }
}
