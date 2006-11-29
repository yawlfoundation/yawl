/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.nexusbpm.operation.WorkflowOperation;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

/**
 * The RemoveNetCommand removes a YNet from a YSpecification. All of the
 * net elements inside the net and the decompositions that those net
 * elements decompose to (except the decompositions that net elements of
 * other nets decompose to) and the flows connected to the net's net
 * elements are all disconnected from the data context.
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class RemoveNetCommand extends RemoveDecompositionCommand {
	private DataProxy<YNet> netProxy;
    private YNet net;
    
    private DataContext context;
    
    private YSpecification parentSpec;
    private DataProxy<YSpecification> parentSpecProxy;
    
    private Map<YFlow, DataProxy> flows;
    private Map<YFlow, DataProxy> flowParents;
    private Map<YExternalNetElement, DataProxy> netElements;
    private Map<YDecomposition, DataProxy> decompositions;
	
	public RemoveNetCommand( DataProxy netProxy ) {
        super( netProxy );
		this.netProxy = netProxy;
        context = netProxy.getContext();
	}
	
	/**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        for( YFlow flow : flows.keySet() ) {
            DataProxy proxy = flows.get( flow );
            DataProxy parentProxy = flowParents.get( flow );
            context.detachProxy( proxy, flow, parentProxy );
        }
        for( YExternalNetElement element : netElements.keySet() ) {
            DataProxy proxy = netElements.get( element );
            context.detachProxy( proxy, element, netProxy );
        }
        for( YDecomposition decomp : decompositions.keySet() ) {
            DataProxy proxy = decompositions.get( decomp );
            WorkflowOperation.detachDecompositionFromSpec( decomp );
            context.detachProxy( proxy, decomp, parentSpecProxy );
        }
        super.attach();
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        super.detach();
        for( YDecomposition decomp : decompositions.keySet() ) {
            DataProxy proxy = decompositions.get( decomp );
            WorkflowOperation.attachDecompositionToSpec( decomp, parentSpec );
            context.attachProxy( proxy, decomp, parentSpecProxy );
        }
        for( YExternalNetElement element : netElements.keySet() ) {
            DataProxy proxy = netElements.get( element );
            context.attachProxy( proxy, element, netProxy );
        }
        for( YFlow flow : flows.keySet() ) {
            DataProxy proxy = flows.get( flow );
            DataProxy parentProxy = flowParents.get( flow );
            context.attachProxy( proxy, flow, parentProxy );
        }
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        net = netProxy.getData();
        parentSpec = net.getParent();
        assert parentSpec != null : "parent specification was null";
        parentSpecProxy = context.getDataProxy( parentSpec );
        assert parentSpecProxy != null : "parent specification's proxy was null";
        
        // set of decompositions used by the tasks in other nets
        Set<YDecomposition> usedDecomps = new HashSet<YDecomposition>();
        
        for( YDecomposition decomp : parentSpec.getDecompositions() ) {
            if( decomp instanceof YNet && net != decomp ) {
                for( YExternalNetElement element : ((YNet) decomp).getNetElements() ) {
                    if( element instanceof YTask ) {
                        YDecomposition d = ((YTask) element).getDecompositionPrototype();
                        if( d != null ) {
                            usedDecomps.add( d );
                        }
                    }
                }
            }
        }
        
        DataProxy proxy;
        
        flows = new HashMap<YFlow, DataProxy>();
        flowParents = new HashMap<YFlow, DataProxy>();
        netElements = new HashMap<YExternalNetElement, DataProxy>();
        decompositions = new HashMap<YDecomposition, DataProxy>();
        
        for( YExternalNetElement element : net.getNetElements() ) {
            proxy = context.getDataProxy( element );
            assert proxy != null : "net element's proxy was null";
            netElements.put( element, proxy );
            if( element instanceof YTask ) {
                YTask task = (YTask) element;
                YDecomposition decomp = task.getDecompositionPrototype();
                if( decomp != null && !usedDecomps.contains( decomp ) ) {
                    proxy = context.getDataProxy( decomp );
                    assert proxy != null : "decomposition's proxy was null";
                    decompositions.put( decomp, proxy );
                }
            }
            for( YFlow flow : element.getPostsetFlows() ) {
                proxy = context.getDataProxy( flow );
                assert proxy != null : "flow's proxy was null";
                flows.put( flow, proxy );
                proxy = context.getDataProxy( flow.getParent() );
                assert proxy != null : "flow's parent's proxy was null";
                flowParents.put( flow, proxy );
            }
        }
        super.perform();
    }
}
