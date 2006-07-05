/*
 * @(#)GraphSelectionEvent.java	1.0 1/1/02
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

/**
 * An event that characterizes a change in the current
 * selection.  The change is based on any number of cells.
 * GraphSelectionListeners will generally query the source of
 * the event for the new selected status of each potentially
 * changed cell.
 *
 * @see GraphSelectionListener
 * @see org.jgraph.graph.GraphSelectionModel
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */
public class GraphSelectionEvent extends EventObject {
	/** Cells this event represents. */
	protected Object[] cells;
	/** For each cell identifies if that cell is in fact new. */
	protected boolean[] areNew;

	/**
	  * Represents a change in the selection of a GraphSelectionModel.
	  * cells identifies the cells that have been either added or
	  * removed from the selection.
	  *
	  * @param source source of event
	  * @param cells the paths that have changed in the selection
	  */
	public GraphSelectionEvent(
		Object source,
		Object[] cells,
		boolean[] areNew) {
		super(source);
		this.cells = cells;
		this.areNew = areNew;
	}

	/**
	  * Returns the cells that have been added or removed from the
	  * selection.
	  */
	public Object[] getCells() {
		int numCells;
		Object[] retCells;

		numCells = cells.length;
		retCells = new Object[numCells];
		System.arraycopy(cells, 0, retCells, 0, numCells);
		return retCells;
	}

	/**
	  * Returns the first cell.
	  */
	public Object getCell() {
		return cells[0];
	}

	/**
	 * Returns true if the first cell has been added to the selection,
	 * a return value of false means the first cell has been
	 * removed from the selection.
	 */
	public boolean isAddedCell() {
		return areNew[0];
	}

	/**
	 * Returns true if the cell identified by cell was added to the
	 * selection. A return value of false means the cell was in the
	 * selection but is no longer in the selection. This will raise if
	 * cell is not one of the cells identified by this event.
	 */
	public boolean isAddedCell(Object cell) {
		for (int counter = cells.length - 1; counter >= 0; counter--)
			if (cells[counter].equals(cell))
				return areNew[counter];
		throw new IllegalArgumentException("cell is not a cell identified by the GraphSelectionEvent");
	}

	/**
	 * Returns true if the cell identified by <code>index</code> was added to
	 * the selection. A return value of false means the cell was in the
	 * selection but is no longer in the selection. This will raise if
	 * index < 0 || >= <code>getPaths</code>.length.
	 *
	 * @since 1.3
	 */
	public boolean isAddedCell(int index) {
		if (cells == null || index < 0 || index >= cells.length) {
			throw new IllegalArgumentException("index is beyond range of added cells identified by GraphSelectionEvent");
		}
		return areNew[index];
	}

	/**
	 * Returns a copy of the receiver, but with the source being newSource.
	 */
	public Object cloneWithSource(Object newSource) {
		// Fix for IE bug - crashing
		return new GraphSelectionEvent(newSource, cells, areNew);
	}
}
