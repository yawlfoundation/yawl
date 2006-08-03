/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

/**
 * The RenameElementCommand renames any single object that is an instance of
 * YSpecification, YDecomposition, YExternalNetElement, or that has a method
 * setName(String).
 * 
 * TODO this needs major work so that it functions properly for folders that aren't empty, for
 * files/directories on the file system, etc.
 * 
 * @author Nathan Rose
 */
public class RenameElementCommand extends AbstractCommand {
    private DataProxy proxy;
    
    private String oldName;
    private String newName;
	
	public RenameElementCommand( DataProxy proxy, String newName, String oldName ) {
        this.proxy = proxy;
        this.oldName = oldName;
        this.newName = newName;
	}
    
	/**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        rename( newName, oldName );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        rename( oldName, newName );
    }
    
    private void rename( String newName, String oldName )
    throws SecurityException, NoSuchMethodException, IllegalArgumentException,
    IllegalAccessException, InvocationTargetException {
        Object data = proxy.getData();
        
        if( data instanceof YSpecification ) {
            ((YSpecification) data).setName( newName );
        }
        else if( data instanceof YDecomposition ) {
            ((YDecomposition) data).setName( newName );
        }
        else if( data instanceof YExternalNetElement ) {
            ((YExternalNetElement) data).setName( newName );
        }
        else {
            Method setter = data.getClass().getMethod( "setName", new Class[] { String.class } );
            setter.invoke( data, new Object[] { newName } );
        }
        
        proxy.setLabel( newName );
        proxy.fireUpdated( DataProxyStateChangeListener.PROPERTY_NAME, oldName, newName );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        // empty (we already have old and new names because they were passed to the constructor)
    }
}
