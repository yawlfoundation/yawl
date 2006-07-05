/*
 * @(#)GraphUndoManager.java	1.0 1/1/02
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

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.jgraph.event.GraphModelEvent;

/**
 * An UndoManager that may be shared among multiple GraphLayoutCache's.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class GraphUndoManager extends UndoManager {

	/**
	 * Overridden to preserve usual semantics: returns true if an undo
	 * operation would be successful now for the given view, false otherwise
	 */
	public synchronized boolean canUndo(Object source) {
		if (isInProgress()) {
			UndoableEdit edit = editToBeUndone(source);
			return edit != null && edit.canUndo();
		} else {
			return super.canUndo();
		}
	}

	/**
	 * Overridden to preserve usual semantics: returns true if a redo
	 * operation would be successful now for the given view, false otherwise
	 */
	public synchronized boolean canRedo(Object source) {
		if (isInProgress()) {
			UndoableEdit edit = editToBeRedone(source);
			return edit != null && edit.canRedo();
		} else {
			return super.canRedo();
		}
	}

	/**
	 * If this UndoManager is inProgress, undo the last significant
	 * UndoableEdit wrt. to source, and all insignificant edits back to
	 * it. Updates indexOfNextAdd accordingly.
	 *
	 * <p>If not inProgress, indexOfNextAdd is ignored and super's routine is
	 * called.</p>
	 *
	 * @see UndoManager#undo
	 */
	public void undo(Object source) {
		if (source == null || !isInProgress())
			super.undo();
		else {
			UndoableEdit edit = editToBeUndone(source);
			//System.out.println("undoTo edit="+edit);
			if (edit == null)
				throw new CannotUndoException();
			undoTo(edit);
		}
	}

	protected UndoableEdit editToBeUndone(Object source) {
		UndoableEdit edit = null;
		Object src = null;
		do {
			edit = nextEditToBeUndone(edit);
			if (edit instanceof GraphModelEvent.GraphViewChange)
				src = ((GraphModelEvent.GraphViewChange) edit).getSource();
			if (!(src instanceof GraphLayoutCache))
				src = null;
		} while (edit != null && src != null && src != source);
		return edit;
	}

	/**
	 * Returns the the next significant edit wrt. to current
	 * to be undone if undo is called. May return null.
	 */
	protected UndoableEdit nextEditToBeUndone(UndoableEdit current) {
		if (current == null)
			return editToBeUndone();
		else {
			int index = edits.indexOf(current) - 1;
			if (index >= 0)
				return (UndoableEdit) edits.get(index);
		}
		return null;
	}

	/**
	 * If this <code>UndoManager</code> is <code>inProgress</code>,
	 * redoes the last significant <code>UndoableEdit</code> with
	 * respect to source or after, and all insignificant
	 * edits up to it. Updates <code>indexOfNextAdd</code> accordingly.
	 *
	 * <p>If not <code>inProgress</code>, <code>indexOfNextAdd</code>
	 * is ignored and super's routine is called.</p>
	 */
	public void redo(Object source) {
		if (source == null || !isInProgress())
			super.redo();
		else {
			UndoableEdit edit = editToBeRedone(source);
			//System.out.println("redoTo edit="+edit);
			if (edit == null)
				throw new CannotRedoException();
			redoTo(edit);
		}
	}

	protected UndoableEdit editToBeRedone(Object source) {
		UndoableEdit edit = nextEditToBeRedone(null);
		UndoableEdit last = null;
		Object src = null;
		do {
			last = edit;
			edit = nextEditToBeRedone(edit);
			if (edit instanceof GraphModelEvent.GraphViewChange)
				src = ((GraphModelEvent.GraphViewChange) edit).getSource();
			if (!(src instanceof GraphLayoutCache))
				src = null;
		} while (edit != null && src != null && src != source);
		return last;
	}

	/**
	 * Returns the the next significant edit wrt. to current
	 * to be redone if redo is called. May return null.
	 */
	protected UndoableEdit nextEditToBeRedone(UndoableEdit current) {
		if (current == null)
			return editToBeRedone();
		else {
			int index = edits.indexOf(current) + 1;
			if (index < edits.size())
				return (UndoableEdit) edits.get(index);
		}
		return null;
	}

}
