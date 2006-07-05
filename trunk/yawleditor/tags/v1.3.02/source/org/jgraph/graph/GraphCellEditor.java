/*
 * @(#)GraphCellEditor.java	1.0 1/1/02
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

import javax.swing.CellEditor;

import org.jgraph.JGraph;

/**
  * Adds to CellEditor the extensions necessary to configure an editor
  * in a graph.
  *
  * @version 1.0 1/1/02
  * @author Gaudenz Alder
  */

public interface GraphCellEditor extends CellEditor {
	/**
	 * Sets an initial <I>value</I> for the editor.  This will cause
	 * the editor to stopEditing and lose any partially edited value
	 * if the editor is editing when this method is called. <p>
	 *
	 * Returns the component that should be added to the client's
	 * Component hierarchy.  Once installed in the client's hierarchy
	 * this component will then be able to draw and receive user input.
	 *
	 * @param	graph		the JGraph that is asking the editor to edit
	 *				This parameter can be null.
	 * @param	value		the value of the cell to be edited.
	 * @param	isSelected	true if the cell is to be rendered with
	 *				selection highlighting
	 * @return	the component for editing
	 */
	Component getGraphCellEditorComponent(
		JGraph graph,
		Object value,
		boolean isSelected);
}
