/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors.net.cells;

import org.jgraph.graph.DefaultEdge;

import com.nexusbpm.editor.persistence.EditorDataProxy;


/**
 * Graph edge that has a special routing mechanism and awareness of domain object proxys
 * 
 * @author catch23
 * @created October 28, 2002 
 */
public class GraphEdge extends DefaultEdge {
  public GraphEdge(EditorDataProxy proxy) {
    super();
    _proxy = proxy;
    _proxy.setGraphEdge(this);
  }

  public EditorDataProxy _proxy;

  public EditorDataProxy getProxy() {
    return _proxy;
  }

  public void setProxy(EditorDataProxy proxy) {
    _proxy = proxy;
  }
}