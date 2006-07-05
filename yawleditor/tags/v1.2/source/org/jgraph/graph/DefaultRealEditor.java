/*
 * @(#)DefaultCellEditor.java	1.0 1/1/02
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

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.jgraph.JGraph;

/**
 * The default editor for graph cells.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class DefaultRealEditor
	extends DefaultCellEditor
	implements GraphCellEditor {

	//
	//  Constructors
	//

	/**
	 * Constructs a DefaultCellEditor that uses a text field.
	 *
	 * @param x  a JTextField object ...
	 */
	public DefaultRealEditor(final JTextField textField) {
		super(textField);
		setClickCountToStart(1);
	}

	/**
	 * Constructs a DefaultCellEditor object that uses a check box.
	 *
	 * @param x  a JCheckBox object ...
	 */
	public DefaultRealEditor(final JCheckBox checkBox) {
		super(checkBox);
	}

	/**
	 * Constructs a DefaultCellEditor object that uses a combo box.
	 *
	 * @param x  a JComboBox object ...
	 */
	public DefaultRealEditor(final JComboBox comboBox) {
		super(comboBox);
	}

	//
	//  GraphCellEditor Interface
	//

	public Component getGraphCellEditorComponent(
		JGraph graph,
		Object value,
		boolean isSelected) {
		String stringValue = graph.convertValueToString(value);

		delegate.setValue(stringValue);
		if (editorComponent instanceof JTextField)
			 ((JTextField) editorComponent).selectAll();
		return editorComponent;
	}

} // End of class JCellEditor
