package com.nexusbpm.editor.tree;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.persistence.dao.DatasourceFolder;

import com.nexusbpm.command.RenameElementCommand;
import com.nexusbpm.command.RenameFolderCommand;
import com.nexusbpm.editor.WorkflowEditor;


/**
 * Editor for component nodes that allows edits only at certain times, tells
 * the server when to persist the renaming of a component, and keeps the edit
 * icon for the component nodes the same as the regular icon.
 * 
 * @author     Dean Mao
 * @author     Daniel Gredler
 * @version    $Revision: 1.5 $
 * @created    October 30, 2002
 */
public class SharedNodeEditor extends DefaultTreeCellEditor {

	private final static Log LOG = LogFactory.getLog( SharedNodeEditor.class );

	private static boolean allowNextEdit = false;

	private SharedNode _node;

	/**
	 * Constructor.
	 * @param tree the ComponentTree that this editor is for.
	 * @param renderer the tree cell renderer.
	 */
	public SharedNodeEditor( JTree tree, DefaultTreeCellRenderer renderer ) {
		super( tree, renderer );
	}

	/**
	 * Allow the next edit attempt.
	 */
	public static void allowNextEdit() {
		SharedNodeEditor.allowNextEdit = true;
	}

	/**
	 * @see DefaultTreeCellEditor#getTreeCellEditorComponent(JTree, Object, boolean, boolean, boolean, int)
	 */
	public Component getTreeCellEditorComponent( JTree t, Object value, boolean isSelected, boolean expanded, boolean leaf, int row ) {
		_node = (SharedNode) value;
		return super.getTreeCellEditorComponent( t, _node.getProxy().getLabel(), isSelected, expanded, leaf, row );
	}

	/**
	 * @see DefaultTreeCellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		String cellEditorValue = (String) super.getCellEditorValue();
		if( SharedNodeEditor.allowNextEdit == false ) return cellEditorValue;
		SharedNodeEditor.allowNextEdit = false;
		String newName = cellEditorValue;
		String oldName = _node.getProxy().getLabel();
		if( !oldName.equals( newName ) ) {
            if( _node.getProxy().getData() instanceof DatasourceFolder ) {
                WorkflowEditor.getExecutor().executeCommand(
                        new RenameFolderCommand( _node.getProxy(), newName ) );
            }
            else {
                WorkflowEditor.getExecutor().executeCommand(
                        new RenameElementCommand( _node.getProxy(), newName, oldName ) );
            }
//            if( _node.getProxy().getData() instanceof String ) {
//                // TODO renaming folders will be complicated....
//                _node.getProxy().setLabel( newName );
//                _node.getProxy().fireUpdated( DataProxyStateChangeListener.PROPERTY_NAME, oldName, newName );
//                String newVal = _node.getProxy().getData().toString();
//                newVal = newVal.substring(0, newVal.length() - oldName.length() ) + newName;
//                _node.getProxy().getContext().renameFolder( _node.getProxy(), newVal );
//                LOG.warn( "Folder renaming will NOT work as expected if the folder was not empty!" );
//            }
//            else {
//                LOG.debug( "Persisting new component name (" + oldName + " -> " + newName + ")." );
//                WorkflowEditor.getExecutor().executeCommand(
//                        new RenameElementCommand( _node, newName, oldName ) );
//            }
		}
		return _node.getProxy().getData();
	}
    
	/**
	 * @see DefaultTreeCellEditor#isCellEditable(EventObject)
	 */
	public boolean isCellEditable( EventObject event ) {
		return SharedNodeEditor.allowNextEdit;
	}

	/**
	 * Overriden so that we can dynamically change the edit icon and
	 * keep it the same as when we are not editing the tree node.
	 * 
	 * @see DefaultTreeCellEditor#determineOffset(JTree, Object, boolean, boolean, boolean, int)
	 */
	protected void determineOffset( JTree t, Object value, boolean selected, boolean expanded, boolean leaf, int row ) {
		if( this.renderer != null ) {
//			SharedNode node = (SharedNode) value;
			this.editingIcon = _node.getIcon();
			this.offset = this.renderer.getIconTextGap() + this.editingIcon.getIconWidth();
		}
		else {
			this.editingIcon = null;
			this.offset = 0;
		}
	}

}
