/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;
import au.edu.qut.yawl.util.VisitSpecificationOperation;
import au.edu.qut.yawl.util.VisitSpecificationOperation.Visitor;

/**
 * The CopySpecificationCommand copies an entire specification
 * from one location to another (within a data context or
 * between contexts).
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class CopySpecificationCommand extends AbstractCommand{
    private DataProxy<YSpecification> sourceSpecProxy;
    private DataProxy<YSpecification> copySpecProxy;
    private DataProxy targetProxy;
    private YSpecification copySpec;
    private DataProxyStateChangeListener listener;
    
    private Map<Object, DataProxy> proxies;
    
    private DataContext targetContext;
	
	public CopySpecificationCommand( DataProxy sourceSpecProxy, DataProxy targetProxy,
            DataProxyStateChangeListener listener ) {
        this.sourceSpecProxy = sourceSpecProxy;
		this.targetProxy = targetProxy;
        this.targetContext = targetProxy.getContext();
        this.listener = listener;
	}
    
	/**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        targetContext.attachProxy( copySpecProxy, copySpec, targetProxy );
        for( YDecomposition decomp : copySpec._decompositions ) {
            VisitSpecificationOperation.visitDecomposition( decomp, new Visitor() {
                /** @see VisitSpecificationOperation.Visitor#visit(Object, String) */
                public void visit( Object child, Object parent, String childLabel ) {
                    if( child instanceof YDecomposition ||
                            child instanceof YExternalNetElement ||
                            child instanceof YFlow ) {
                        targetContext.attachProxy( proxies.get( child ), child,
                                targetContext.getDataProxy( parent ) );
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
                                targetContext.getDataProxy( parent ) );
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
        Object targetParent = targetProxy.getData();
        String parent;
        if( targetParent instanceof String ) {
            parent = (String) targetParent;
        }
        else if( targetParent instanceof File ) {
            parent = ((File) targetParent).toURI().toASCIIString();
        }
        else if( targetParent instanceof DatasourceRoot ) {
            parent = targetParent.toString();
        }
        else {
            throw new IllegalArgumentException( "Attempting to copy a specification to an illegal destination!" );
        }
        copySpec = WorkflowOperation.copySpecification( sourceSpecProxy.getData(), parent );
        
        copySpecProxy = targetContext.createProxy( copySpec, listener );
        
        proxies = new HashMap<Object, DataProxy>();
        
        for( YDecomposition decomp : copySpec._decompositions ) {
            VisitSpecificationOperation.visitDecomposition( decomp, new Visitor() {
                /**
                 * @see VisitSpecificationOperation.Visitor#visit(Object, String)
                 */
                public void visit( Object child, Object parent, String childLabel ) {
                    if( child instanceof YDecomposition ||
                            child instanceof YExternalNetElement ||
                            child instanceof YFlow ) {
                        proxies.put( child, targetContext.createProxy( child, listener ) );
                    }
                }
            });
        }
    }
}
