/*
 * @(#)CellView.java	1.0 1/1/02
 * 
 * Copyright (c) 2001-2004, Gaudenz Alder 
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * - Neither the name of JGraph nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific
 *   prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jgraph.graph;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Map;

import org.jgraph.JGraph;

/**
 * Defines the requirements for an object that
 * represents a view for a model cell.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public interface CellView {

	//
	// Data Source
	//

	/**
	 * Returns the model object that this view represents.
	 */
	Object getCell();

	/**
	 * Refresh this view based on the model cell. This is
	 * messaged when the model cell has changed.
	 */
	void refresh(boolean createDependentViews);

	/**
	 * Update this view's attributes. This is messaged whenever refresh is
	 * messaged, and additionally when the context of the cell has changed,
	 * and during live-preview changes to the view.
	 */
	void update();

	void childUpdated();

	//
	// Group Structure
	//

	/**
	 * Returns the parent of view of this view.
	 */
	CellView getParentView();

	/**
	 * Returns the child views of this view.
	 */
	CellView[] getChildViews();

	/**
	 * Removes this view from the list of childs of the parent.
	 */
	void removeFromParent();

	/**
	 * Returns true if the view is a leaf.
	 */
	boolean isLeaf();

	//
	// View Methods
	//

	/**
	 * Returns the bounds for the view.
	 */
	Rectangle getBounds();

	/**
	 * Returns true if the view intersects the given rectangle.
	 */
	boolean intersects(Graphics g, Rectangle rect);

	/**
	 * Apply the specified map of attributes on the view.
	 */
	Map setAttributes(Map map);

	/**
	 * Returns all attributes of the view as a map.
	 */
	Map getAttributes();

	Map getAllAttributes();

	//
	// Renderer, Editor and Handle
	//

	/**
	 * Returns a renderer component, configured for the view.
	 */
	Component getRendererComponent(
		JGraph graph,
		boolean selected,
		boolean focus,
		boolean preview);

	/**
	 * Returns a cell handle for the view.
	 */
	CellHandle getHandle(GraphContext context);

	/**
	 * Returns a cell editor for the view.
	 */
	GraphCellEditor getEditor();

}
