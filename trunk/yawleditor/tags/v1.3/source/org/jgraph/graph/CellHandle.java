/*
 * @(#)CellHandle.java	1.0 1/1/02
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

import java.awt.Graphics;
import java.awt.event.MouseEvent;

/**
 * Defines the requirements for objects that may be used as handles.
 * Handles are used to interactively manipulate a cell's appearance.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public interface CellHandle {

	/**
	 * Paint the handle on the given graphics object once.
	 *
	 * @param g       the graphics object to paint the handle on
	 */
	void paint(Graphics g);

	/**
	 * Paint the handle on the given graphics object during mouse
	 * operations.
	 *
	 * @param g       the graphics object to paint the handle on
	 */
	void overlay(Graphics g);

	/**
	 * Return a cursor for the given point.
	 *
	 * @param p   the point for which the cursor is returned
	 */
	void mouseMoved(MouseEvent e);

	/**
	 * Messaged when a drag gesture is recogniced.
	 *
	 * @param e   the drag gesture event to be processed
	 */
	void mousePressed(MouseEvent event);

	/**
	 * Messagedwhen the user drags the selection.
	 * The Controller is responsible to determine whether the mouse is
	 * inside the parent graph or not.
	 *
	 * @param e   the drag event to be processed
	 */
	void mouseDragged(MouseEvent e);

	/**
	 * Messaged when the drag operation has
	 * terminated with a drop.
	 *
	 * @param e   the drop event to be processed
	 */
	void mouseReleased(MouseEvent event);

}
