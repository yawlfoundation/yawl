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
import operation.WorkflowOperation.NameAndCounter;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DatasourceFolder;
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
public class CopySpecificationCommand extends AbstractFileSystemCommand {
    private DataProxy<YSpecification> sourceSpecProxy;
    private DataProxy<YSpecification> copySpecProxy;
    private DataProxy<DatasourceFolder> targetProxy;
    private YSpecification copySpec;
    private DataProxyStateChangeListener listener;
    
    private Map<Object, DataProxy> proxies;
    
    private DataContext targetContext;
	
	public CopySpecificationCommand(
            DataProxy<YSpecification> sourceSpecProxy,
            DataProxy<DatasourceFolder> targetProxy,
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
        DatasourceFolder targetParent = targetProxy.getData();
        String parentPath = targetParent.getPath();
        
        // first convert the ID to fit the target path
        String destID = WorkflowOperation.joinURIs( parentPath, sourceSpecProxy.getData().getID() );
        
        // then drop or add the ".xml" depending on which context we're going to or coming from
        NameAndCounter n = new NameAndCounter( destID );
        if( targetParent.isSchemaFile() ) {
            n = new NameAndCounter( n.getStrippedName(), n.getCounter(), ".xml" );
        }
        else {
            n = new NameAndCounter( n.getStrippedName(), n.getCounter(), null );
        }
        
        String prefix;
        String suffix;
        String tmp = n.toString();
        if( tmp.indexOf( "/" ) >= 0 ) {
            prefix = tmp.substring( 0, tmp.lastIndexOf( "/" ) + 1 );
            suffix = tmp.substring( tmp.lastIndexOf( "/" ) + 1 );
        }
        else {
            prefix = "";
            suffix = tmp;
        }
        
        // then make sure the ID is unique within the folder we're copying to
        List<String> usedIDs = getChildNames( targetProxy );
        suffix = WorkflowOperation.getAvailableID( usedIDs, suffix );
        
        destID = prefix + suffix;
        
        // then perform the copy
        copySpec = WorkflowOperation.copySpecification( sourceSpecProxy.getData(), destID );
        
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
