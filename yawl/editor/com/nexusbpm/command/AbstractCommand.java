/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

public abstract class AbstractCommand implements Command {
    /**
     * @see com.nexusbpm.command.Command#execute()
     */
    public final void execute() {
        perform();
        attach();
    }
    
    /**
     * @see com.nexusbpm.command.Command#redo()
     */
    public final void redo() {
        attach();
    }
    
    /**
     * @see com.nexusbpm.command.Command#undo()
     */
    public final void undo() {
        if( supportsUndo() ) {
            detach();
        }
        else {
            throw new UnsupportedOperationException( this.getClass().toString()
                    + " does not support the undo operation!" );
        }
    }
    
    /**
     * Performs this command's operation, but does not attach the results
     * to existing data objects within the data context.
     * TODO is this exactly what needs to be done?
     * TODO are proxy objects created here or in attach? (probly depends on refactorings of DataContext)
     */
    protected abstract void perform();
    
    /**
     * Attaches the results of the command to the data context and has the
     * proxy notify its listeners that changes were made.
     * TODO how would there be any listeners when the command is first executed? or will this only be needed for redo?
     * TODO is this exactly what needs to be done? 
     */
    protected abstract void attach();
    
    /**
     * Removes the results of the command from the data context and has the
     * proxy notify its listeners that changes were made.
     * TODO is this exactly what needs to be done?
     */
    protected abstract void detach();
    
    /**
     * Default behavior is to support undo, but if a particular command does not
     * support undo then it can override this function and return false, and
     * that command will not perform undo operations.
     */
    public boolean supportsUndo() {
        return true;
    }
}
