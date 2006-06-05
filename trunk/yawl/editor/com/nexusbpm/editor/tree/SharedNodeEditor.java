package com.nexusbpm.editor.tree;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
		return super.getTreeCellEditorComponent( t, value, isSelected, expanded, leaf, row );
	}

	/**
	 * @see DefaultTreeCellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		String cellEditorValue = (String) super.getCellEditorValue();
		if( SharedNodeEditor.allowNextEdit == false ) return cellEditorValue;
		SharedNodeEditor.allowNextEdit = false;
		String newName = (String) cellEditorValue;
		String oldName = _node.getName();
		if( !oldName.equals( newName ) ) {
			LOG.debug( "Persisting new component name (" + oldName + " -> " + newName + ")." );
			throw new RuntimeException( "implement the rename tree node methods!" );
		}
		return _node;
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
			SharedNode node = (SharedNode) value;
			this.editingIcon = node.getIcon();
			this.offset = this.renderer.getIconTextGap() + this.editingIcon.getIconWidth();
		}
		else {
			this.editingIcon = null;
			this.offset = 0;
		}
	}

}
