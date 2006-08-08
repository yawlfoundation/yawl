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

import operation.WorkflowOperation;


import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

/**
 * The RemoveDecompositionCommand removes a YDecomposition from a YSpecification.
 * All of the tasks in other nets that decompose to the decomposition are set to
 * have a null decomposition.
 * 
 * @author Nathan Rose
 */
public class RemoveDecompositionCommand extends AbstractCommand {
	private DataProxy<YDecomposition> decompProxy;
    private YDecomposition decomposition;
    
    private DataContext context;
    
    private YSpecification parentSpec;
    private DataProxy<YSpecification> parentSpecProxy;
    
    private List<YTask> tasks;
    
	public RemoveDecompositionCommand( DataProxy decompProxy ) {
		this.decompProxy = decompProxy;
        context = decompProxy.getContext();
	}
	
	/**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        for( YTask task : tasks ) {
            task.setDecompositionPrototype( null );
        }
        WorkflowOperation.detachDecompositionFromSpec( decomposition );
        context.detachProxy( decompProxy, decomposition, parentSpecProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        WorkflowOperation.attachDecompositionToSpec( decomposition, parentSpec );
        context.attachProxy( decompProxy, decomposition, parentSpecProxy );
        for( YTask task : tasks ) {
            task.setDecompositionPrototype( decomposition );
        }
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        decomposition = decompProxy.getData();
        parentSpec = decomposition.getParent();
        assert parentSpec != null : "parent specification was null";
        parentSpecProxy = context.getDataProxy( parentSpec, null );
        assert parentSpecProxy != null : "parent specification's proxy was null";
        
        tasks = new LinkedList<YTask>();
        
        for( YDecomposition d : parentSpec.getDecompositions() ) {
            if( d instanceof YNet ) {
                for( YExternalNetElement element : ((YNet) d).getNetElements() ) {
                    if( element instanceof YTask ) {
                        YTask task = (YTask) element;
                        if( task.getDecompositionPrototype() == decomposition ) {
                            tasks.add( task );
                        }
                    }
                }
            }
        }
    }
}
