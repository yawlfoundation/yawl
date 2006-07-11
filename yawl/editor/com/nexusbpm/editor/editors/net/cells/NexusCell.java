package com.nexusbpm.editor.editors.net.cells;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.graph.DefaultGraphCell;

import au.edu.qut.yawl.elements.YExternalNetElement;

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
	 * Sets the description on a cell. The in-graph editor would normally call setUserObject to set the cell
	 * label, but instead, we will set the label on our domain object.
	 */
	public void setUserObject( Object obj ) {
//		super.setUserObject(obj);
		if( obj != null && obj instanceof String ) {
			// Set the description and save it immediately. If we don't save immediately, the user might
			// connect this component to another component, which will wipe out the description change.
			Object o = getProxy().getData();
			_proxy.setLabel((String) obj);
			LOG.error("message only", new RuntimeException("implement for yawl"));
		}
	}

	/**
	 * Return the description of the cell so that it will be the initial value of the in-graph editor.
	 */
	public String toString() {
		return _proxy.getLabel();//"this is the description, nothing too interesting, yet often times intriguing.  I often wonder where our creativity comes from.";
	}

	public void finalize() throws Throwable {
		_proxy = null; // mjf 2005-05-17 16:31
		super.finalize();
	}

}
