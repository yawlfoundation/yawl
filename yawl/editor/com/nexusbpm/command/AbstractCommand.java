/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

public abstract class AbstractCommand implements Command {
    private boolean done = false;
    /**
     * @see com.nexusbpm.command.Command#execute()
     */
    public final void execute() throws Exception {
        if( done == true ) {
            throw new IllegalStateException(
                    "Error executing command: this command has already been executed!\n" +
                    this.getClass().getName() );
        }
        done = true;
        System.err.println( "Executing abstract command:" + this.getClass().getName() );
        perform();
        attach();
    }
    
    /**
     * @see com.nexusbpm.command.Command#redo()
     */
    public final void redo() throws Exception {
        attach();
    }
    
    /**
     * @see com.nexusbpm.command.Command#undo()
     */
    public final void undo() throws Exception {
        if( supportsUndo() ) {
            detach();
        }
        else {
            throw new UnsupportedOperationException( this.getClass().toString()
                    + " does not support the undo operation!" );
        }
    }
    
    /**
     * Performs this command's operation and creates proxies as needed, but does
     * not attach the results to existing data objects within the data context.
     */
    protected abstract void perform() throws Exception;
    
    /**
     * Attaches the results of the command to the data context and has the
     * proxy notify its listeners that changes were made. 
     */
    protected abstract void attach() throws Exception;
    
    /**
     * Removes the results of the command from the data context and has the
     * proxy notify its listeners that changes were made.
     */
    protected abstract void detach() throws Exception;
    
    /**
     * Default behavior is to support undo, but if a particular command does not
     * support undo then it can override this function and return false, and
     * that command will not perform undo operations.
     */
    public boolean supportsUndo() {
        return true;
    }
}
