package com.nexusbpm.editor.editors.net.cells;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * JGraph edge class for control edges.
 * 
 * @author catch23
 * @author Daniel Gredler
 * @created October 28, 2002
 */
public class FlowControlEdge extends GraphEdge {

	/**
	 * Creates a new JGraph edge for the specified controller's component edge.
	 * @param controller The controller for the component edge corresponding to this JGraph edge.
	 */
	public FlowControlEdge( EditorDataProxy proxy ) {
		super( proxy );
	}

	/**
	 * Gets the source attribute of the GraphControlEdge object
	 * @return The source value
	 */
	public Object getSource() {
		return super.getSource();
	}

	/**
	 * Gets the target attribute of the GraphControlEdge object
	 * @return The target value
	 */
	public Object getTarget() {
		return super.getTarget();
	}

	/**
	 * Sets the source attribute of the GraphControlEdge object
	 * @param port
	 *          The new source value
	 */
	public void setSource( Object port ) {
		super.setSource( port );
	}

	/**
	 * Sets the target attribute of the GraphControlEdge object
	 * @param port The new target value
	 */
	public void setTarget( Object port ) {
		super.setTarget( port );
	}

	/**
	 * Sets the name on an edge. The in-graph editor would normally call
	 * setUserObject to set the edge label, but instead, we will set the label on
	 * our domain object.
	 */
	public void setUserObject( Object obj ) {
		super.setUserObject(obj);
	}

	/**
	 * Return the label of the edge so that it will be the initial value of the
	 * in-graph editor.
	 */
	public String toString() {
		return "";
	}

}
