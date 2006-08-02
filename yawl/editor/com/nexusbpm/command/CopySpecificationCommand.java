/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.HashMap;
import java.util.Map;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.util.VisitSpecificationOperation;
import au.edu.qut.yawl.util.VisitSpecificationOperation.Visitor;

import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

/**
 * The CopySpecificationCommand copies an entire specification
 * from one location to another (within a data context or
 * between contexts).
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class CopySpecificationCommand extends AbstractCommand{
    private EditorDataProxy<YSpecification> sourceSpecProxy;
    private SharedNode targetNode;
    private EditorDataProxy<YSpecification> copySpecProxy;
    private YSpecification copySpec;
    
    private Map<Object, EditorDataProxy> proxies;
    
    private DataContext targetContext;
	
	public CopySpecificationCommand( SharedNode sourceSpecNode, SharedNode targetNode ) {
        this.sourceSpecProxy = sourceSpecNode.getProxy();
		this.targetNode = targetNode;
        this.targetContext = targetNode.getProxy().getContext();
	}
    
	/**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        targetContext.attachProxy( copySpecProxy, copySpec, targetNode.getProxy() );
        for( YDecomposition decomp : copySpec._decompositions ) {
            VisitSpecificationOperation.visitDecomposition( decomp, new Visitor() {
                /** @see VisitSpecificationOperation.Visitor#visit(Object, String) */
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
        targetContext.save( copySpecProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        for( YDecomposition decomp : copySpec._decompositions ) {
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
        targetContext.delete( copySpecProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        copySpec = WorkflowOperation.copySpecification(
                sourceSpecProxy.getData(), targetNode.getProxy().getData().toString() );
        
        copySpecProxy = (EditorDataProxy) targetContext.createProxy( copySpec,
                (SharedNodeTreeModel) targetNode.getTreeModel() );
        
        proxies = new HashMap<Object, EditorDataProxy>();
        
        for( YDecomposition decomp : copySpec._decompositions ) {
            VisitSpecificationOperation.visitDecomposition( decomp, new Visitor() {
                /**
                 * @see VisitSpecificationOperation.Visitor#visit(Object, String)
                 */
                public void visit( Object child, Object parent, String childLabel ) {
                    if( child instanceof YDecomposition ||
                            child instanceof YExternalNetElement ||
                            child instanceof YFlow ) {
                        proxies.put( child, (EditorDataProxy) targetContext.createProxy(
                                child, (SharedNodeTreeModel) targetNode.getTreeModel() ) );
                    }
                }
            });
        }
    }
}
