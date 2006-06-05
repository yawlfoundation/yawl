package com.nexusbpm.editor.editors.net;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;

import javax.swing.JComponent;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author Dean Mao
 * @created Sep 10, 2004
 */
public class CapselaGraphLayoutCache extends GraphLayoutCache {

  private static final Log LOG = LogFactory.getLog(CapselaGraphLayoutCache.class);

  private JGraph _graph;
  
  public CapselaGraphLayoutCache(GraphModel arg0, CellViewFactory arg1, JGraph graph, boolean arg2) {
    super(arg0, arg1, arg2);
    _graph = graph;
  }
  /**
   * This method is only called after we edit the description for CapselaCell object.  It will resize the object to
   * the cell's renderer's preferred size.  Normally the description would be hidden if this were not called.
   * Calling this method after editing a description will resize the object such that the description is 
   * visible.
   * 
   * @see org.jgraph.graph.GraphLayoutCache#valueForCellChanged(java.lang.Object, java.lang.Object)
   */
  public void valueForCellChanged(Object cell, Object newValue) {
    super.valueForCellChanged(cell, newValue);
    AttributeMap map = new AttributeMap();
		CellView view = getMapping(cell, false);
		JComponent component = (JComponent)
			view.getRendererComponent(_graph, false, false, false);
		if (component != null) {
			_graph.add(component);
			component.validate();
			Rectangle2D bounds = GraphConstants.getBounds(view.getAllAttributes());
			if (bounds != null) {
			  bounds = (Rectangle2D) bounds.clone();
				Dimension d = component.getPreferredSize();
				bounds.setFrame(bounds.getX(), bounds.getY(), d.getWidth(), d
						.getHeight());
		    GraphConstants.setBounds(map, bounds);
			}
		}
    
    Hashtable table = new Hashtable();
    table.put(cell, map);
    
		edit(table, null, null, null);
  }

	public void finalize() throws Throwable{
		LOG.debug("CapselaGraphLayoutCache.finalize");
		_graph = null;
		super.finalize();
	}
}
