/*
 * @(#)GraphUI.java	1.0 1/1/02
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

package org.jgraph.plaf;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.plaf.ComponentUI;

import org.jgraph.JGraph;
import org.jgraph.graph.CellHandle;
import org.jgraph.graph.CellView;

/**
 * Pluggable look and feel interface for JGraph.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */
public abstract class GraphUI extends ComponentUI {

	/**
	 * Paints the renderer of <code>view</code> to <code>g</code>
	 * at <code>bounds</code>.
	 */
	public abstract void paintCell(
		Graphics g,
		CellView view,
		Rectangle bounds,
		boolean preview);

	/**
	 * Paints the renderers of <code>portViews</code> to <code>g</code>.
	 */
	public abstract void paintPorts(Graphics g, CellView[] portViews);

	/**
	 * Messaged to update the selection based on a MouseEvent for a group of
	 * cells. If the event is a toggle selection event, the cells are either
	 * selected, or deselected. Otherwise the cells are selected.
	 */
	public abstract void selectCellsForEvent(
		JGraph graph,
		Object[] cells,
		MouseEvent event);

	/**
	  * Returns the preferred size for <code>view</code>.
	  */
	public abstract Dimension getPreferredSize(JGraph graph, CellView view);

	/**
	  * Returns the <code>CellHandle</code> that is currently active,
	  * or <code>null</code> if no handle is active.
	  */
	public abstract CellHandle getHandle(JGraph graph);

	/**
	  * Returns true if the graph is being edited.  The item that is being
	  * edited can be returned by getEditingCell().
	  */
	public abstract boolean isEditing(JGraph graph);

	/**
	  * Stops the current editing session.  This has no effect if the
	  * graph isn't being edited.  Returns true if the editor allows the
	  * editing session to stop.
	  */
	public abstract boolean stopEditing(JGraph graph);

	/**
	  * Cancels the current editing session. This has no effect if the
	  * graph isn't being edited.  Returns true if the editor allows the
	  * editing session to stop.
	  */
	public abstract void cancelEditing(JGraph graph);

	/**
	  * Selects the cell and tries to edit it.  Editing will
	  * fail if the CellEditor won't allow it for the selected item.
	  */
	public abstract void startEditingAtCell(JGraph graph, Object cell);

	/**
	 * Returns the cell that is being edited.
	 */
	public abstract Object getEditingCell(JGraph graph);

}
