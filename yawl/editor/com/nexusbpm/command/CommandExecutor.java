/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The command executor provides a thread safe mechanism for adding commands
 * to the command stack, undoing, and redoing commands.
 * 
 * @author Nathan Rose
 */
public class CommandExecutor {
    private ExecutorService executor;
    private CommandStack stack;
    private List<CommandCompletionListener> listeners = new LinkedList<CommandCompletionListener>();
    
    /**
     * Creates a command executor backed by a command stack that will have a
     * maximum undo memory of the given size.
     */
    public CommandExecutor( int stackSize ) {
        stack = new CommandStack( stackSize );
        executor = Executors.newSingleThreadExecutor();
        listeners.add( new CommandCompletionListener() {
            public void commandCompleted( ExecutionResult result ) {
                if( result.getError() == null ) {
                    System.out.println( "Command completed successfully" );
                }
                else {
                    System.err.println( "Error occurred executing command:" );
                    result.getError().printStackTrace( System.err );
                }
            }
        });
    }
    
    /**
     * Puts the given command on the stack to be executed. By returning the
     * future we can easily execute the command synchronously if needed by 
     * appending a call to the get method of the returned future to the 
     * executeCommand call.
     */
    public Future executeCommand( Command command ) {
        return executor.submit( new ExecuteCommand( command ) );
    }
    
    /**
     * Undoes the command most recently added to the stack.
     */
    public void undo() {
        executor.submit( new ExecuteUndo() );
    }
    
    /**
     * Redoes the command most recently undone.
     */
    public void redo() {
        executor.submit( new ExecuteRedo() );
    }
    
    /**
     * Adds a listener that will be alerted whenever a command has finished executing,
     * being undone, or being redone.
     */
    public void addCommandCompletionListener( CommandCompletionListener listener ) {
        synchronized( listeners ) {
            listeners.add( listener );
        }
    }
    
    /**
     * Removes the given CommandCompletionListener.
     */
    public boolean removeCommandCompletionListener( CommandCompletionListener listener ) {
        synchronized( listeners ) {
            return listeners.remove( listener );
        }
    }
    
    /**
     * @return the maximum number of commands that will be stored in the stack.
     */
    public int getMaximumSize() {
        return stack.getMaximumSize();
    }
    
    /**
     * @param maximumSize the maximum number of commands to store in the stack.
     */
    public void setMaximumSize( int maximumSize ) {
        stack.setMaximumSize( maximumSize );
    }
    
    public interface CommandCompletionListener {
        public void commandCompleted( ExecutionResult result );
    }
    
    public static class ExecutionResult {
        private Throwable error;
        private boolean canUndo;
        private boolean canRedo;
        private ExecutionResult( Throwable error, boolean canUndo, boolean canRedo ) {
            this.error = error;
            this.canUndo = canUndo;
            this.canRedo = canRedo;
        }
        
        public boolean canRedo() {
            return canRedo;
        }
        
        public boolean canUndo() {
            return canUndo;
        }
        
        public Throwable getError() {
            return error;
        }
    }
    
    private abstract class AbstractExecute implements Runnable {
        abstract void execute() throws Exception;
        public final void run() {
            Throwable t = null;
            try {
            	execute();
            }
            catch( Throwable tr ) {
                t = tr;
            }
            ExecutionResult result = new ExecutionResult( t,
                    CommandExecutor.this.stack.canUndo(),
                    CommandExecutor.this.stack.canRedo() );
            synchronized( listeners ) {
                for( CommandCompletionListener listener : listeners ) {
                    listener.commandCompleted( result );
                }
            }
        }
    }
    
    private class ExecuteCommand extends AbstractExecute {
        private Command c;
        private ExecuteCommand( Command command ) {
            c = command;
        }
        public void execute() throws Exception {
            CommandExecutor.this.stack.executeCommand( c );
        }
    }
    
    private class ExecuteUndo extends AbstractExecute {
        public void execute() throws Exception {
            CommandExecutor.this.stack.undoLastCommand();
        }
    }
    
    private class ExecuteRedo extends AbstractExecute {
        public void execute() throws Exception {
            CommandExecutor.this.stack.redoNextCommand();
        }
    }
}
