/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.jgraph.graph.GraphConstants;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

import com.nexusbpm.editor.editors.net.cells.NexusCell;
import com.nexusbpm.editor.persistence.YTaskEditorExtension;

/**
 * The MoveTasksCommand moves a set of graph objects and their corresponding
 * tasks within a net.
 * 
 * @author Nathan Rose
 */
public class MoveTasksCommand extends AbstractCommand {
	private Map<Object, Map> oldAttributes;
    private Map<Object, Map> newAttributes;
    
    private DataProxy<YNet> netProxy;
    
    /**
     * @param parent
     * @param netName
     */
	public MoveTasksCommand( Map<Object, Map> oldAttributes, Map<Object, Map> newAttributes ) {
        this.oldAttributes = oldAttributes;
        this.newAttributes = newAttributes;
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        performMove( oldAttributes, newAttributes );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        performMove( newAttributes, oldAttributes );
    }
    
    private void performMove( Map<Object, Map> fromAttributes, Map<Object, Map> toAttributes ) {
        netProxy.fireUpdated(
                DataProxyStateChangeListener.PROPERTY_TASK_BOUNDS, fromAttributes, toAttributes );
        
        for( Object key : fromAttributes.keySet() ) {
            if( key instanceof NexusCell ) {
                NexusCell cell = (NexusCell) key;
                Map attributeMap = newAttributes.get( key );
                
                assert attributeMap.get( GraphConstants.BOUNDS ) != null :
                    "attribute map doesn't contain new bounds to move a task";
                
                YExternalNetElement element = (YExternalNetElement) cell.getProxy().getData();
                YTaskEditorExtension extension = new YTaskEditorExtension( element );
                Rectangle2D r = (Rectangle2D) attributeMap.get( GraphConstants.BOUNDS );
                extension.setBounds(r.getBounds());
            }
        }
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        DataContext context = null;
        YNet net = null;
        for( Object key : oldAttributes.keySet() ) {
            assert newAttributes.containsKey( key ) : "new attributes lacks a key old attributes has";
            if( key instanceof NexusCell ) {
                NexusCell cell = (NexusCell) key;
                context = cell.getProxy().getContext();
                assert cell.getProxy().getData() instanceof YExternalNetElement : "invalid cell being moved";
                YNet nextNet = ((YExternalNetElement) cell.getProxy().getData()).getParent();
                assert nextNet != null : "containing net is null";
                assert nextNet != null && ( net == null || nextNet == net ) :
                    "moving tasks that are in different nets";
                net = nextNet;
            }
        }
        for( Object key : newAttributes.keySet() ) {
            assert oldAttributes.containsKey( key ) : "old attributes lacks a key new attributes has";
        }
        assert context != null : "data context is null";
        
        netProxy = context.getDataProxy( net );
    }
}
