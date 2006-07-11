package com.nexusbpm.editor.editors.net;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.GraphConstants;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.persistence.managed.DataContext;

import com.nexusbpm.editor.editors.net.cells.NexusCell;
import com.nexusbpm.editor.persistence.YTaskEditorExtension;

/**
 * @author Dean Mao
 * @created Sep 8, 2004
 */
public class GraphChangeSummary implements GraphModelListener {
	private final static Log LOG = LogFactory.getLog( GraphChangeSummary.class );

  private NexusGraph _graph;
  private GraphEditor _editor;

  /**
   *  
   */
  public GraphChangeSummary(NexusGraph graph, GraphEditor editor) {
    _graph = graph;
    _editor = editor;
  }

  /**
   * This is necessary such that the animated icon will move with the icon if the cell is moved
   * 
   * @see org.jgraph.event.GraphModelListener#graphChanged(org.jgraph.event.GraphModelEvent)
   */
  public void graphChanged(GraphModelEvent e) {
    if (e.getChange().getPreviousAttributes() != null) {
    for (Iterator iter = e.getChange().getPreviousAttributes().keySet().iterator(); iter.hasNext();) {
      Object obj = iter.next();
      if (obj != null && obj instanceof NexusCell) {
        NexusCell cell = (NexusCell) obj;
        Map attributeMap = (Map) e.getChange().getPreviousAttributes().get(cell);
        if (attributeMap.get(GraphConstants.BOUNDS) != null) {
        	YExternalNetElement ene = (YExternalNetElement) cell.getProxy().getData();
        	YTaskEditorExtension extension = new YTaskEditorExtension(ene);
        	Rectangle2D r = (Rectangle2D) attributeMap.get(GraphConstants.BOUNDS);
        	extension.setCenterPoint(r.getBounds().getLocation());
//          CellView view = _graph.getGraphLayoutCache().getMapping(cell, false);
	      _editor.getFlowEditor().setDirty(true);
        	LOG.info("Element position changed.");
		  // TODO The commented block of code in the "if (view instanceof DefaultView)" is to 
		  // reposition the animated icon if the position of the component moves.  This is not
		  // necessary at the moment since animated icons represent running components and
		  // running components are in instances that do not allow their position to be moved.
		  
//          if (view instanceof DefaultView) {
//            AnimatedIcon animatedIcon = ((DefaultView) view).getAnimatedIcon();
//            DefaultRenderer renderer = (DefaultRenderer) view.getRendererComponent(_graph, false, false, false);
//            Rectangle rendererBounds = renderer.getNameRenderer().getBounds();
//
//            int xOffset = (int) ((rendererBounds.getWidth() / 2) - (animatedIcon.getBounds().getWidth() / 2));
//            int yOffset = 2;
//
//            //Rectangle2D oldBounds = (Rectangle2D) oldAttributeMap.get(GraphConstants.BOUNDS);
//            Rectangle2D bounds = (Rectangle2D) attributeMap.get(GraphConstants.BOUNDS);
//            Point location = bounds.getBounds().getLocation();
//            animatedIcon.setLocation((int) location.getX() + xOffset, (int) location.getY() + yOffset);
//           
//            _graph.repaint();
//          }
        }
      }
    }
    }
  }

  //		// Get Old Attributes From GraphModelChange (Undo) -- used to remap
  //		// removed cells
  //		CellView[] views = change.getViews(this);
  //		if (views != null) {
  //			// Only ex-visible views are piggybacked
  //			for (int i = 0; i < views.length; i++)
  //				if (views[i] != null) {
  //					// Do not use putMapping because cells are invisible
  //					mapping.put(views[i].getCell(), views[i]);
  //				}
  //			// Ensure visible state
  //			setVisibleImpl(getCells(views), true);
  //		}
  //		// Fetch View Order Of Changed Cells (Before any changes)
  //		Object[] changed = order(change.getChanged()); // change.getChanged(); ?
  //		// Fetch Views to Insert before Removal (Special case: two step process,
  //		// see setModel)
  //		CellView[] insertViews = getMapping(change.getInserted(), true);
  //		// Remove and Hide Roots
  //		views = removeRoots(change.getRemoved());
  //		// Store Removed Attributes In GraphModelChange (Undo)
  //		change.putViews(this, views);
  //		// Insert New Roots
  //		insertRoots(insertViews);
  //		// Hide edges with invisible source or target
  //		if (isPartial()) {
  //			// Then show
  //			showCellsForChange(change);
  //			// First hide
  //			hideCellsForChange(change);
  //		}
  //		// Refresh Changed Cells
  //		if (changed != null && changed.length > 0) {
  //			// Restore All Cells in Model Order (Replace Roots)
  //			if (!isOrdered()) {
  //				roots.clear();
  //				Object[] rootCells = DefaultGraphModel.getRoots(graphModel);
  //				CellView[] rootViews = getMapping(rootCells, false);
  //				for (int i = 0; i < rootViews.length; i++) {
  //					if (rootViews[i] != null) {
  //						// && isVisible(rootViews[i].getCell())) {
  //						roots.add(rootViews[i]);
  //						rootViews[i].refresh(true);
  //						factory.updateAutoSize(getGraph(), rootViews[i]);
  //					}
  //				}
  //			}
  //			for (int i = 0; i < changed.length; i++) {
  //				CellView view = getMapping(changed[i], false);
  //				if (view != null) { // && isVisible(view.getCell())) {
  //					view.refresh(true);
  //					// Update child edges in groups (routing)
  //					update(view);
  //					factory.updateAutoSize(getGraph(), view);
  //					if (isOrdered()) {
  //						CellView parentView = view.getParentView();
  //						Object par = (parentView != null) ? parentView
  //								.getCell() : null;
  //						boolean isRoot = roots.contains(view);
  //						// Adopt Orphans
  //						if (par == null && !isRoot)
  //							roots.add(view);
  //						// Root Lost
  //						else if (par != null && isRoot)
  //							roots.remove(view);
  //					}
  //				}
  //			}
  //		}
  //		// Refresh inserted cells
  //		Object[] inserted = change.getInserted();
  //		if (inserted != null && inserted.length > 0) {
  //			for (int i = 0; i < inserted.length; i++)
  //				factory.updateAutoSize(getGraph(), getMapping(inserted[i],
  //						false));
  //		}
  //		// Refresh Context of Changed Cells (=Connected Edges)
  //		refresh(getMapping(change.getContext(), false), false);
  //		// Update Cached Ports If Necessary
  //		Object[] removed = change.getRemoved();
  //		if ((removed != null && removed.length > 0)
  //				|| (inserted != null && inserted.length > 0) || !isOrdered())
  //			updatePorts();

	public void finalize() throws Throwable {
		LOG.debug("GraphChangeSummary.finalize");

		_graph = null;
		_editor = null;

		super.finalize();

	}

}