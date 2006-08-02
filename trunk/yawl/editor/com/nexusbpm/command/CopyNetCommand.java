/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.util.VisitSpecificationOperation;
import au.edu.qut.yawl.util.VisitSpecificationOperation.Visitor;

import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

/**
 * The CopyNetCommand provides a way of copying a net from one 
 * YSpecification to another.
 * 
 * @author Nathan Rose
 */
public class CopyNetCommand extends AbstractCommand {
    private EditorDataProxy<YNet> netProxy;
    private EditorDataProxy<YSpecification> specProxy;
    private SharedNode specNode;
    
    private DataContext targetContext;
    
    private Map<Object,EditorDataProxy> proxies;
    
    private List<YDecomposition> decomps;
	
    /**
     * @param netProxy the source net to copy.
     * @param specProxy the target specification to copy the net into.
     */
	public CopyNetCommand( SharedNode netNode, SharedNode specNode ) {
		this.netProxy = netNode.getProxy();
        this.specNode = specNode;
        this.specProxy = specNode.getProxy();
        targetContext = specProxy.getContext();
	}
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() {
        for( YDecomposition decomp : decomps ) {
            WorkflowOperation.attachDecompositionToSpec( decomp, specProxy.getData() );
            VisitSpecificationOperation.visitDecomposition( decomp, new Visitor() {
                /**
                 * @see VisitSpecificationOperation.Visitor#visit(Object, String)
                 */
                public void visit( Object child, Object parent, String childLabel ) {
                    if( child instanceof YDecomposition ||
                            child instanceof YExternalNetElement ||
                            child instanceof YFlow ) {
                        targetContext.attachProxy( proxies.get( child ), child,
                                targetContext.getDataProxy( parent, null ) );
                    }
                }
            });
        }
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() {
        for( YDecomposition decomp : decomps ) {
            WorkflowOperation.detachDecompositionFromSpec( decomp );
            VisitSpecificationOperation.visitDecomposition( decomp, new Visitor() {
                /**
                 * @see VisitSpecificationOperation.Visitor#visit(Object, String)
                 */
                public void visit( Object child, Object parent, String childLabel ) {
                    if( child instanceof YDecomposition ||
                            child instanceof YExternalNetElement ||
                            child instanceof YFlow ) {
                        targetContext.detachProxy( proxies.get( child ), child,
                                targetContext.getDataProxy( parent, null ) );
                    }
                }
            });
        }
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws CloneNotSupportedException {
        decomps = WorkflowOperation.copyDecomposition( netProxy.getData(), specProxy.getData() );
        
        proxies = new HashMap<Object, EditorDataProxy>();
        
        for( YDecomposition decomp : decomps ) {
            VisitSpecificationOperation.visitDecomposition( decomp, new Visitor() {
                /**
                 * @see VisitSpecificationOperation.Visitor#visit(Object, String)
                 */
                public void visit( Object child, Object parent, String childLabel ) {
                    if( child instanceof YDecomposition ||
                            child instanceof YExternalNetElement ||
                            child instanceof YFlow ) {
                        proxies.put( child, (EditorDataProxy) targetContext.createProxy(
                                child, (SharedNodeTreeModel) specNode.getTreeModel() ) );
                    }
                }
            });
        }
    }
}
