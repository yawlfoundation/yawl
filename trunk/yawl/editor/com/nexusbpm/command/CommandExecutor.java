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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The command executor provides a thread safe mechanism for adding commands
 * to the command stack, undoing, and redoing commands.
 * 
 * @author Nathan Rose
 */
public class CommandExecutor {
    private static final Log LOG = LogFactory.getLog( CommandExecutor.class );
    
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
                    String name = "Command";
                    if( result.getCommand() != null ) {
                        name = result.getCommand().getClass().getName();
                        if( name.indexOf( "." ) >= 0 ) {
                            name = name.substring( name.lastIndexOf( "." ) + 1 );
                        }
                        
                    }
                    String verb = " was completed ";
                    if( result.getOperation().equals( ExecutionResult.OPERATION_UNDO ) )
                        verb = " was undone ";
                    else if( result.getOperation().equals( ExecutionResult.OPERATION_REDO ) )
                        verb = " was redone ";
                    LOG.info( name + verb + "successfully" );
                }
                else {
                    LOG.error( result.getError().getMessage(), result.getError() );
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
        public static final String OPERATION_EXECUTE = "execute";
        public static final String OPERATION_UNDO = "undo";
        public static final String OPERATION_REDO = "redo";
        
        private Command command;
        private Throwable error;
        private boolean canUndo;
        private boolean canRedo;
        private String operation;
        
        public boolean canRedo() {
            return canRedo;
        }
        
        public boolean canUndo() {
            return canUndo;
        }
        
        public Throwable getError() {
            return error;
        }
        
        public Command getCommand() {
            return command;
        }
        
        public String getOperation() {
            return operation;
        }
    }
    
    private abstract class AbstractExecute implements Runnable {
        abstract Command execute() throws Exception;
        ExecutionResult result;
        public final void run() {
            result = new ExecutionResult();
            Throwable t = null;
            Command cmd = null;
            
            try {
            	cmd = execute();
            }
            catch( Throwable tr ) {
                t = tr;
            }
            
            result.command = cmd;
            result.error = t;
            result.canUndo = CommandExecutor.this.stack.canUndo();
            result.canRedo = CommandExecutor.this.stack.canRedo();

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
        public Command execute() throws Exception {
            result.operation = ExecutionResult.OPERATION_EXECUTE;
            return CommandExecutor.this.stack.executeCommand( c );
        }
    }
    
    private class ExecuteUndo extends AbstractExecute {
        public Command execute() throws Exception {
            result.operation = ExecutionResult.OPERATION_UNDO;
            return CommandExecutor.this.stack.undoLastCommand();
        }
    }
    
    private class ExecuteRedo extends AbstractExecute {
        public Command execute() throws Exception {
            result.operation = ExecutionResult.OPERATION_REDO;
            return CommandExecutor.this.stack.redoNextCommand();
        }
    }
}
