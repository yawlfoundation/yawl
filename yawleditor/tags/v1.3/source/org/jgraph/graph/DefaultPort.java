/*
 * @(#)DefaultPort.java	1.0 1/1/02
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A simple implementation for a port.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class DefaultPort extends DefaultGraphCell implements Port {

	/** Edges that are connected to the port */
	protected HashSet edges = new HashSet();

	/** Reference to the anchor of this port */
	protected Port anchor;

	/**
	 * Constructs an empty port.
	 */
	public DefaultPort() {
		this(null, null);
	}

	/**
	 * Constructs a vertex that holds a reference to the specified user object.
	 *
	 * @param userObject reference to the user object
	 */
	public DefaultPort(Object userObject) {
		this(userObject, null);
	}

	/**
	 * Constructs a vertex that holds a reference to the specified user object
	 * and a reference to the specified anchor.
	 *
	 * @param userObject reference to the user object
	 * @param reference to a a graphcell that constitutes the anchor
	 */
	public DefaultPort(Object userObject, Port anchor) {
		super(userObject, false);
		this.anchor = anchor;
	}

	/**
	 * Returns an iterator of the edges connected
	 * to the port.
	 */
	public Iterator edges() {
		return edges.iterator();
	}

	/**
	 * Adds <code>edge</code> to the list of ports.
	 */
	public boolean addEdge(Object edge) {
		return edges.add(edge);
	}

	/**
	 * Removes <code>edge</code> from the list of ports.
	 */
	public boolean removeEdge(Object edge) {
		return edges.remove(edge);
	}

	/**
	 * Returns the anchor of this port.
	 */
	public Set getEdges() {
		return new HashSet(edges);
	}

	/**
	 * Sets the anchor of this port.
	 */
	public void setEdges(Set edges) {
		this.edges = new HashSet(edges);
	}

	/**
	 * Returns the anchor of this port.
	 */
	public Port getAnchor() {
		return anchor;
	}

	/**
	 * Sets the anchor of this port.
	 */
	public void setAnchor(Port port) {
		anchor = port;
	}

	/**
	 * Create a clone of the cell. The cloning of the
	 * user object is deferred to the cloneUserObject()
	 * method.
	 *
	 * @return Object  a clone of this object.
	 */
	public Object clone() {
		DefaultPort c = (DefaultPort) super.clone();
		c.edges = new HashSet();
		return c;
	}

}
