/*
 * @(#)GraphModelEvent.java	1.0 1/1/02
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

package org.jgraph.event;

import java.util.EventObject;
import java.util.Map;

import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.ParentMap;

/**
 * Encapsulates information describing changes to a graph model, and
 * is used to notify graph model listeners of the change.
 *
 * @author Gaudenz Alder
 * @version 1.0 1/1/2
 *
 */

public class GraphModelEvent extends EventObject {

	/**
	 * The object that consistutes the change.
	 */
	protected GraphModelChange change;

	/**
	 * Used to create an event when cells have been changed, inserted, or
	 * removed, identifying the change as a ModelChange object.
	 * @param source the Object responsible for generating the event (typically
	 * the creator of the event object passes <code>this</code>
	 * for its value)
	 * @param change the object that describes the change
	 * @see org.jgraph.graph.GraphCell
	 *
	 */
	public GraphModelEvent(Object source, GraphModelChange change) {
		super(source);
		this.change = change;
	}

	/**
	 * Returns the object that constitues the change.
	 */
	public GraphModelChange getChange() {
		return change;
	}

	/**
	 * Defines the interface for objects that may be executed by the
	 * model when used as arguments to insert or edit.
	 */
	public static interface ExecutableGraphChange {

		/**
		 * Executes the change.
		 */
		public void execute();

	}

	/**
	 * Defines the interface for objects that may be used to
	 * represent a change to the view.
	 */
	public static interface GraphViewChange {

		/**
		 * Returns the source of this change. This can either be a
		 * view or a model, if this change is a GraphModelChange.
		 */
		public Object getSource();

		/**
		 * Returns the objects that have changed.
		 */
		public Object[] getChanged();

		/**
		 * Returns a map that contains (object, map) pairs which
		 * holds the new attributes for each changed cell.
		 * Note: This returns a map of (cell, map) pairs for
		 * an insert on a model that is not an attribute store.
		 * Use getStoredAttributeMap to access the attributes
		 * that have been stored in the model.
		 */
		public Map getAttributes();

		/**
		 * Returns the objects that have not changed explicitly, but
		 * implicitly because one of their dependent cells has changed.
		 * This is typically used to return the edges that are attached
		 * to vertices, which in turn have been resized or moved.
		 */
		public Object[] getContext();

	}

	/**
	 * Defines the interface for objects that may be included
	 * into a GraphModelEvent to describe a model change.
	 */
	public static interface GraphModelChange extends GraphViewChange {

		/**
		 * Returns the cells that have been inserted into the model.
		 */
		public Object[] getInserted();

		/**
		 * Returns the cells that have been removed from the model.
		 */
		public Object[] getRemoved();

		/**
		 * Returns a map that contains (object, map) pairs
		 * of the attributes that have been stored in the model.
		 */
		public Map getPreviousAttributes();

		public ConnectionSet getPreviousConnectionSet();
		
		public ParentMap getPreviousParentMap();

		/**
		 * Allows a <code>GraphLayoutCache</code> to store cell views
		 * for cells that have been removed. Such cell views are used
		 * for re-insertion and restoring the visual attributes.
		 */
		public void putViews(GraphLayoutCache view, CellView[] cellViews);

		/**
		 * Allows a <code>GraphLayoutCache</code> to retrieve an array of
		 * <code>CellViews</code> that was previously stored with
		 * <code>putViews(GraphLayoutCache, CellView[])</code>.
		 */
		public CellView[] getViews(GraphLayoutCache view);

	}

}
