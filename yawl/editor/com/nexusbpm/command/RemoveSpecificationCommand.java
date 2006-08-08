/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.util.VisitSpecificationOperation;
import au.edu.qut.yawl.util.VisitSpecificationOperation.Visitor;

/**
 * The RemoveSpecificationCommand removes a specification.
 * 
 * @author Nathan Rose
 */
public class RemoveSpecificationCommand extends AbstractCommand {
	private DataProxy<YSpecification> specProxy;
    private YSpecification spec;
    
    private DataContext context;
    
    private List<DataProxy> proxies;
    private Map<DataProxy, Object> data;
    private Map<DataProxy, DataProxy> parents;
    
	public RemoveSpecificationCommand( DataProxy specProxy ) {
		this.specProxy = specProxy;
        context = specProxy.getContext();
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        for( int index = proxies.size() - 1; index >= 0; index-- ) {
            DataProxy proxy = proxies.get( index );
            Object object = data.get( proxy );
            DataProxy parent = parents.get( proxy );
            
            if( object instanceof YSpecification ) {
                context.delete( proxy );
            }
            else {
                context.detachProxy( proxy, object, parent );
            }
        }
    }

    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        for( int index = 0; index < proxies.size(); index++ ) {
            DataProxy proxy = proxies.get( index );
            Object object = data.get( proxy );
            DataProxy parent = parents.get( proxy );
            
            context.attachProxy( proxy, object, parent );
            if( object instanceof YSpecification ) {
                context.save( proxy );
            }
        }
    }

    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        spec = specProxy.getData();
        proxies = new ArrayList<DataProxy>();
        data = new HashMap<DataProxy, Object>();
        parents = new HashMap<DataProxy, DataProxy>();
        
        DataProxy parentProxy = context.getParentProxy( specProxy );
        
        VisitSpecificationOperation.visitSpecification( spec, parentProxy.getData(), new SpecVisitor() );
    }
    
    private class SpecVisitor implements Visitor {
        /**
         * @see au.edu.qut.yawl.util.VisitSpecificationOperation.Visitor#visit(Object, Object, String)
         */
        public void visit( Object child, Object parent, String childLabel ) {
            DataProxy childProxy = context.getDataProxy( child, null );
            if( childProxy != null ) {
                DataProxy parentProxy = context.getParentProxy( childProxy );
                proxies.add( childProxy );
                data.put( childProxy, child );
                parents.put( childProxy, parentProxy );
            }
        }
    }
}
