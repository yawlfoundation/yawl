package com.nexusbpm.editor.editors.net.cells;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultGraphCell;

import com.nexusbpm.editor.persistence.EditorDataProxy;


public class NexusCell extends DefaultGraphCell {

	private static final transient Log LOG = LogFactory.getLog( NexusCell.class );

	public static final int DEFAULT_HEIGHT = 70;
	public static final int DEFAULT_WIDTH = 77;

	private EditorDataProxy _proxy;

	public NexusCell( EditorDataProxy proxy ) {
		super();
		_proxy = proxy;
		_proxy.setGraphCell( this );
	}

	public EditorDataProxy getProxy() {
		return _proxy;
	}

	public void setProxy( EditorDataProxy controller ) {
		_proxy = controller;
	}

	/**
	 * @see javax.swing.tree.DefaultMutableTreeNode#setUserObject(java.lang.Object)
	 */
	public void setUserObject( Object obj ) {
        // Empty.
	}
    
    public boolean contains( Object o ) {
        return children.contains( o );
    }

	/**
	 * Return the description of the cell so that it will be the initial value of the in-graph editor.
	 */
	public String toString() {
		return _proxy.getLabel();
	}

	public void finalize() throws Throwable {
		_proxy = null; // mjf 2005-05-17 16:31
		super.finalize();
	}

}
