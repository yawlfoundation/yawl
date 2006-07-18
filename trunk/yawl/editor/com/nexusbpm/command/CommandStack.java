package com.nexusbpm.command;

import java.util.ArrayList;
import java.util.List;

import com.nexusbpm.editor.exception.EditorException;

/**
 * This is the list of commands that have been executed via the Capsela client,
 * allowing for undoing of specific actions, as well as redoing them once they
 * have been undone.
 * 
 * TODO ensure threading issues are taken care of.
 * 
 * @author Daniel Gredler
 * @author Nathan Rose
 * @see Command
 */
public class CommandStack {
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
     * @throws EditorException if the undo operation fails.
     */
    public void undoLastCommand() throws EditorException {
        if( lastExecuted >= 0 ) {
            try {
                Command cmd = commands.get( lastExecuted );
                cmd.undo();
                lastExecuted--;
            }
            catch( Exception e ) {
                throw new EditorException( "Exception encountered while undoing the last command!", e );
            }
        }
        else {
            throw new EditorException( "Cannot undo because there are no more commands to undo!" );
        }
    }

    /**
     * Redoes the next command.
     * 
     * @throws EditorException if the command fails to execute.
     */
    public void redoNextCommand() throws EditorException {
        if( lastExecuted < commands.size() - 1 ) {
            int oldLastExecuted = lastExecuted;
            try {
                lastExecuted++;
                Command cmd = (Command) commands.get( lastExecuted );
                cmd.execute();
            }
            catch( Exception e ) {
                lastExecuted = oldLastExecuted;
                throw new EditorException( "Exception encountered while redoing the next command!", e );
            }
        }
        else {
            throw new EditorException( "Cannot redo because there are no more commands to redo!" );
        }
    }

    /**
     * Executes the specified command and adds it to the command stack.
     * 
     * @param cmd the command to execute
     * @throws EditorException if the command fails to execute.
     */
    public void executeCommand( Command cmd ) throws EditorException {
        try {
            cmd.execute();
        }
        catch( Exception e ) {
            throw new EditorException( "Exception encountered while executing the command!", e );
        }
        
        lastExecuted++;
        while( commands.size() > lastExecuted ) {
            commands.remove( commands.size() );
        }
        
        commands.add( cmd );
        
        resize();
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
