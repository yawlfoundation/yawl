package com.nexusbpm.editor.editors.net;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.util.JGraphUtilities;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YExternalNetElement;

import com.nexusbpm.command.CopyTaskCommand;
import com.nexusbpm.editor.WorkflowEditor;
import com.nexusbpm.editor.editors.net.cells.NexusCell;
import com.nexusbpm.editor.editors.net.cells.ViewFactory;
import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.DragAndDrop;
import com.nexusbpm.editor.tree.SharedNode;

/**
 * JGraph object that handles drag'n drop, cell bounds auto-sizing, printing
 * abilities, and property change listener for all the domain object proxys that
 * are inside the graph.
 * 
 * @author catch23
 * @created October 28, 2002
 */
public class NexusGraph extends JGraph implements Printable,
		DropTargetListener, DragGestureListener, DragSourceListener,
		PropertyChangeListener {

	private static final Log LOG = LogFactory.getLog(NexusGraph.class);

	private GraphEditor _graphEditor;

	private SharedNode _SharedNode;

	private EditorDataProxy _proxy;

	private DragSource _dragSource;

	/**
	 * Constructor used to create a new graph for a flow that is an instance.
	 * 
	 * @param model
	 *            the graph model to use as the data model.
	 * @param graphEditor
	 *            the graph editor that this graph is contained in.
	 * @param proxy
	 *            the proxy for the flow instance.
	 */
	public NexusGraph(NexusGraphModel model, GraphEditor graphEditor,
			EditorDataProxy proxy) {
		this(model, graphEditor, (SharedNode) null);
		_proxy = proxy;
	}

	/**
	 * Constructor used to create a new graph for a flow that is a template.
	 * 
	 * @param model
	 *            the graph model to use as the data model.
	 * @param graphEditor
	 *            the graph editor that this graph is contained in.
	 * @param sharedNode
	 *            the node representing the flow template.
	 */
	public NexusGraph(NexusGraphModel model, GraphEditor graphEditor,
			SharedNode sharedNode) {
		super(model);
		_dragSource = new DragSource();
		_dragSource.createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_COPY_OR_MOVE, this);

		_SharedNode = sharedNode;
		if (_SharedNode != null) {
			_proxy = sharedNode.getProxy();
		}

		_graphEditor = graphEditor;
		setMarqueeHandler(new GraphMarqueeHandler(this, graphEditor));
		setPortsVisible(true);
		setGridEnabled(true);
		setGridSize(6);
		setTolerance(1);
		setAntiAliased(false);
		setHighlightColor(Color.white);
		setBendable(false);

		// Pressing the mouse button 2 times in a row will allow users to edit
		// the
		// description
		setEditClickCount(2);

		new DropTarget(this, this);

		// Register the graph with the tool-tip manager for DataEdge hovers
		ToolTipManager.sharedInstance().registerComponent(this);

		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 3) {
					int x = e.getX(), y = e.getY();
					Object cell = NexusGraph.this.getFirstCellForLocation(x,
							y);
					startEditingAtCell(cell);
				}
			}

			public void mouseReleased(MouseEvent e) {
				NexusGraph.this.repaint();
			}
		});

		GraphLayoutCache cache = new NexusGraphLayoutCache(getModel(),
				new ViewFactory(), this, true);
		cache.setFactory(new ViewFactory());
		cache.setSelectsAllInsertedCells(false);
		cache.setSelectsLocalInsertedCells(false);
		setGraphLayoutCache(cache);
		setAntiAliased(true);
	}
    
    /**
     * Notification from the <code>UIManager</code> that the L&F has changed.
     * Replaces the current UI object with the latest version from the
     * <code>UIManager</code>. Subclassers can override this to support
     * different GraphUIs.
     * 
     * @see JComponent#updateUI
     */
    public void updateUI() {
        setUI(new GraphUI());
        invalidate();
    }

	/**
	 * CapselaGraph will be the property listener for all domain object proxys
	 * it contains. So if a proxy's domain object has changed state and the
	 * state affects cell renderer output, it will repaint the graph. Since the
	 * renderers do not maintain state, they must fetch the state information
	 * from the domain object via the proxy, via the cell.
	 * 
	 * The this is added as a property change listener in
	 * GraphEditor.initializeCellAndPort()
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event ) {
        throw new RuntimeException( "needs a yawl specific impl" );
//        DomainObjectproxy source = (DomainObjectproxy) event.getSource();
//        String property = event.getPropertyName();
//        long id = source.identifier().getId();
//        boolean repaint = false;
//        if( property.equals( Component.ATTR_EXECUTION_STATUS ) ) {
//            ExecutionStatus status = (ExecutionStatus) event.getNewValue();
//            LOG.debug( "Component " + id + " changed execution status to " + status.getName() + "." );
//            repaint = true;
//        }
//        if( property.equals( Component.ATTR_NAME ) ) {
//            String oldName = (String) event.getOldValue();
//            String name = (String) event.getNewValue();
//            LOG.debug( "Component " + id + " changed name from " + oldName + " to '" + name + "'." );
//            repaint = true;
//        }
//        if( property.equals( MarkupComponent.ATTR_MARKUP ) ) {
//            String markup = (String) event.getNewValue();
//            LOG.debug( "Component " + id + " changed markup to '" + markup + "'." );
//            repaint = true;
//        }
//        if( repaint && source instanceof DataProxy ) {
//            Rectangle2D bounds = this.getGraphLayoutCache().getMapping(
//                    ((DataProxy) source).getGraphCell(), false ).getBounds();
//            this.repaint(
//                    (int) bounds.getX(),
//                    (int) bounds.getY(),
//                    (int) bounds.getWidth(),
//                    (int) bounds.getHeight() );
//        }
    }

	/**
	 * Cause all cells to change their size to the cell's renderer's perferred
	 * size. This will reveal any descriptions under the cell.
	 */
	public void autoSizeAll() {
		CellView[] views = this.getGraphLayoutCache().getMapping(
				JGraphUtilities.getVertices(this.getModel(), DefaultGraphModel
						.getAll(this.getModel())));
		try {
			Hashtable table = new Hashtable();
			if (views != null) {
				for (int i = 0; i < views.length; i++) {
					CellView view = views[i];
					JComponent component = (JComponent) view
							.getRendererComponent(this, false, false, false);
					if (component != null) {
						this.add(component);
						component.validate();
						Rectangle2D bounds = GraphConstants.getBounds(view
								.getAllAttributes());
						if (bounds != null) {
							bounds = (Rectangle2D) bounds.clone();
							Dimension d = component.getPreferredSize();
							bounds.setFrame(bounds.getX(), bounds.getY(), d
									.getWidth(), d.getHeight());
							AttributeMap map = new AttributeMap();
							GraphConstants.setBounds(map, bounds);
							table.put(view.getCell(), map);
						}
					}
				}
			}
			this.getGraphLayoutCache().edit(table, null, null, null);
		} catch (Exception ee) {
			LOG.info("Exception resizing graph.", ee);
		}
		super.repaint();
	}

	/**
	 * Causes only a single cell to autosize to the cell's renderer's preferred
	 * size.
	 * 
	 * @param view
	 */
	public void autoSize(CellView view) {
		Hashtable table = new Hashtable();

		JComponent component = (JComponent) view.getRendererComponent(this,
				false, false, false);
		if (component != null) {
			add(component);
			component.validate();
			Rectangle2D bounds = GraphConstants.getBounds(view
					.getAllAttributes());
			if (bounds != null) {
				bounds = (Rectangle2D) bounds.clone();
				Dimension d = component.getPreferredSize();
				bounds.setFrame(bounds.getX(), bounds.getY(), d.getWidth(), d
						.getHeight());
				AttributeMap map = new AttributeMap();
				GraphConstants.setBounds(map, bounds);
				table.put(view.getCell(), map);
			}
		}
		getGraphLayoutCache().edit(table, null, null, null);
		super.repaint();

	}

	/**
	 * Print method for Printable interface
	 */
	public int print(Graphics g, PageFormat pageFormat, int page) {
		double oldScale = getScale();
		try {
			// turn off double buffering of graph so we don't get bitmap printed
			setDoubleBuffered(false);

			double pageWidth = pageFormat.getImageableWidth();
			double pageHeight = pageFormat.getImageableHeight();
			int cols = (int) (this.getWidth() / pageWidth) + 1;
			int rows = (int) (this.getHeight() / pageHeight) + 1;
			int pageCount = cols * rows;
			double xScale = pageWidth / this.getWidth();
			double yScale = pageHeight / this.getHeight();
			double scale = Math.min(xScale, yScale);

			double tx = 0.0;
			double ty = 0.0;
			if (xScale > scale) {
				tx = 0.5 * (xScale - scale) * this.getWidth();
			} else {
				ty = 0.5 * (yScale - scale) * this.getHeight();
			}
			((Graphics2D) g).translate((int) pageFormat.getImageableX(),
					(int) pageFormat.getImageableY());
			((Graphics2D) g).translate(tx, ty);
			((Graphics2D) g).scale(scale, scale);

			if (page >= pageCount) {
				return NO_SUCH_PAGE;
			}

			this.paint(g);
		} finally {
			// turn double buffering back on
			setDoubleBuffered(true);
			setScale(oldScale);
		}
		return PAGE_EXISTS;
	}

	/**
	 * This is for displaying the tooltips over any given cell. Right now we'll
	 * use it to display what is being transferred over a data connection edge.
	 */
	public String getToolTipText(MouseEvent event) {
		Object cell = getFirstCellForLocation(event.getX(), event.getY());

		return null;
	}

	/**
	 * @return the proxy for the flow which this graph displays.
	 */
	public EditorDataProxy getProxy() {
		return _proxy;
	}

	/**
	 * @return the graph editor in which this graph is displayed.
	 */
	public GraphEditor getGraphEditor() {
		return _graphEditor;
	}

	/**
	 * Empty implementation.
	 * 
	 * @see DragGestureListener#dragGestureRecognized(DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent e) {
	}

	/**
	 * Empty implementation.
	 * 
	 * @see DragSourceListener#dragDropEnd(DragSourceDropEvent)
	 */
	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	/**
	 * Empty implementation.
	 * 
	 * @see DragSourceListener#dragEnter(DragSourceDragEvent)
	 */
	public void dragEnter(DragSourceDragEvent dsde) {
	}

	/**
	 * Empty implementation.
	 * 
	 * @see DragSourceListener#dragOver(DragSourceDragEvent)
	 */
	public void dragOver(DragSourceDragEvent dsde) {
	}

	/**
	 * Empty implementation.
	 * 
	 * @see DragSourceListener#dropActionChanged(DragSourceDragEvent)
	 */
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	/**
	 * Empty implementation.
	 * 
	 * @see DragSourceListener#dragExit(DragSourceEvent)
	 */
	public void dragExit(DragSourceEvent dsde) {
	}

	/**
	 * Empty implementation.
	 * 
	 * @see DropTargetListener#dropActionChanged(DropTargetDragEvent)
	 */
	public void dropActionChanged(DropTargetDragEvent event) {
	}

	/**
	 * Empty implementation.
	 * 
	 * @see DropTargetListener#dragEnter(DropTargetDragEvent)
	 */
	public void dragEnter(DropTargetDragEvent event) {
	}

	/**
	 * Sets mouse cursor to the "reject" cursor after dragging away from the
	 * graph.
	 * 
	 * @see DropTargetListener#dragExit(DropTargetEvent)
	 */
	public void dragExit(DropTargetEvent event) {
		DragAndDrop.setMouseCursorToRejectDrop();
	}

	/**
	 * Sets the mouse cursor to the appropriate cursor depending on whether the
	 * object being dragged is rejected or accepted.
	 * 
	 * @see DropTargetListener#dragOver(DropTargetDragEvent)
	 */
	public void dragOver(DropTargetDragEvent event) {
		SharedNode draggingNode = DragAndDrop.getDraggingNode();
		if (isDropAcceptable(draggingNode)) {
			boolean isCopy = isDropCopy(draggingNode);
			if (isCopy) {
				event.acceptDrag(DnDConstants.ACTION_COPY);
				DragAndDrop.setMouseCursorToAcceptDropForCopy();
			} else {
				event.acceptDrag(DnDConstants.ACTION_MOVE);
				DragAndDrop.setMouseCursorToAcceptDropForMove();
			}
		} else {
			event.rejectDrag();
			DragAndDrop.setMouseCursorToRejectDrop();
		}
	}

	/**
	 * Accepts the drop if the object being dropped is acceptable.
	 * 
	 * @see DropTargetListener#drop(DropTargetDropEvent)
	 */
	public void drop(DropTargetDropEvent event) {

		final SharedNode draggingNode = DragAndDrop.getDraggingNode();
		if (!isDropAcceptable(draggingNode)) {
			return;
		}

//		SharedNode oldParent = (SharedNode) draggingNode.getParent();
		SharedNode newParent = _SharedNode;

        Point origLocation = event.getLocation();
		final Point location = new Point(
                (int)( origLocation.getX() / getScale() ),
                (int)( origLocation.getY() / getScale() ) );
//		if (isDropCopy(draggingNode)) {
			// if copy:
			LOG.debug("IS COPY ACTION");
            WorkflowEditor.getExecutor().executeCommand(
                    new CopyTaskCommand( draggingNode, _SharedNode, location ) );
//			throw new RuntimeException("implement the copy operation");
			// try {
			// ClientOperation.executeCopyCommand((DataProxy)
			// draggingNode.getproxy(), (DataProxy) newParent.getproxy(),
			// location,
			// _graphEditor);
			// } catch (CapselaException e) {
			// LOG.warn("Error executing copy command.", e);
			// }
//		} else {
//			// if move:
//			LOG.debug("IS MOVE ACTION");
//			if (newParent != oldParent) {
//				throw new RuntimeException("implement the copy operation");
//				// try {
//				// ClientOperation.executeMoveCommand((DataProxy)
//				// draggingNode.getproxy(),(DataProxy)
//				// oldParent.getproxy(),(DataProxy) newParent
//				// .getproxy(), location, _graphEditor);
//				// } catch (CapselaException e) {
//				// LOG.warn("Error executing move command.", e);
//				// }
//			}
//		}
	}

	/**
	 * Returns <tt>true</tt> if the specified node may be dropped into this
	 * graph.
	 * 
	 * @param draggingNode
	 *            the node to check
	 * @return whether the given node is acceptable for a drop operation.
	 */
	private boolean isDropAcceptable(SharedNode draggingNode) {
        return draggingNode.getProxy().getData() instanceof YExternalNetElement;
//		throw new RuntimeException("implement this for yawl!");
		// // Can't drop a folder in a flow.
		// DataProxy proxy = draggingNode.getProxy();
		// boolean notDroppingFolder = (proxy.isFlow() || (!proxy.isFolder()));
		// // Can only drop into flows that are not instances.
		// boolean notDroppingIntoInstance = (!this.isInstance());
		// // We can drop if everything is ok.
		// return (notDroppingFolder && notDroppingIntoInstance);
	}

	/**
	 * Returns <tt>true</tt> if dropping the specified node into this graph
	 * should be a copy operation, rather than a move operation.
	 */
	private boolean isDropCopy(SharedNode draggingNode) {
		// TODO: as per Zubin (May 10 2005), everything is a copy operation
		// until further notice.
		return true;
		// // It's a copy operation (rather than a move operation) if you're
		// dragging:
		// // - From the network to anywhere.
		// // - From anywhere to the network.
		// // - From the components panel to anywhere.
		// // - From inside a flow that's anywhere to inside another flow that's
		// // anywhere.
		// SharedNode parent1 = (SharedNode) draggingNode.getParent();
		// SharedNode parent2 = (SharedNode) ((DataProxy)
		// this.getproxy()).getSharedNode().getParent();
		// boolean fromNetworkToAnywhere = draggingNode.isInNetworkFolder();
		// boolean fromAnywhereToNetwork = ((DataProxy)
		// this.getproxy()).getSharedNode().isInNetworkFolder();
		// boolean fromComponentsToAnywhere =
		// draggingNode.isInComponentsFolder();
		// boolean fromFlowToFlow = parent1 != null &&
		// parent1.getproxy().isFlow() && parent2 != null &&
		// parent2.getproxy().isFlow();
		// return fromNetworkToAnywhere || fromAnywhereToNetwork ||
		// fromComponentsToAnywhere || fromFlowToFlow;
	}

	/**
	 * Locks the graph so that cells can't be moved.
	 */
	public void lockGraph() {
		this.setMoveable(false);
	}

	/**
	 * Returns <code>true</code> if this graph pertains to an instance of a
	 * flow.
	 * 
	 * @return whether this graph pertains to a flow instance.
	 */
	public boolean isInstance() {
		return (_SharedNode == null);
	}

	/**
	 * Open the specific editor for a given graph cell object.
	 */
	public void startEditingAtCell(Object cell) {
	  new RuntimeException("OUTPUT ONLY This should open up an editor for the task/net/decomposition type").printStackTrace();
// if (cell instanceof GraphEdge && ((GraphEdge)
// cell).getproxy().getPersistentDomainObject() instanceof DataEdge) {
// LOG.debug("Trying to edit a data edge");
// GraphEdge dataEdge = (GraphEdge) cell;
// GraphPort sourcePort = (GraphPort) dataEdge.getSource();
// GraphPort sinkPort = (GraphPort) dataEdge.getTarget();
//      
// DataProxy sourceproxy = sourcePort.getproxy();
// DataProxy sinkproxy = sinkPort.getproxy();
// DataConnectionEditor editor = new DataConnectionEditor((DataProxy) _proxy,
// _graphEditor.getFlowEditor());
// editor.setVisible(true);
// editor.expandPaths(new TreePath(sourceproxy.getSharedNode().getPath()), new
// TreePath(sinkproxy.getSharedNode().getPath()));
// } else {
// assert cell instanceof CapselaCell : "cell instanceof CapselaCell";
// LOG.debug("Trying to edit a component");
	  Object node = ((NexusCell) cell).getProxy().getData();
      
      if (node instanceof YAtomicTask) {
          try {
              WorkflowEditor.getInstance().openEditor( ((NexusCell) cell).getProxy(), null );
          }
          catch( Exception e ) {
              LOG.error( "Error opening editor!", e );
          }
      }
// ((CapselaCell) cell).getproxy().getPersistentDomainObject(1);
// if (!node.isInComponentsFolder()) {
// try {
// CapselaInternalFrame internalFrame = node.getPrgetFrame();
// Client.openInternalFrame(internalFrame);
// } catch (Exception e) {
// LOG.error(e);
// }
// }
// }
  }

	/**
	 * Deselects all selected items.
	 */
	public void deselectAll() {
		Object o;
		while ((o = this.getSelectionCell()) != null) {
			this.removeSelectionCell(o);
		}
	}

	/**
	 * Start a property editor for the graph cell object. This will work for any
	 * object.
	 * 
	 * @param cell
	 */
	public void startBasicEditingAtCell(final NexusCell cell) {
		throw new RuntimeException(
				"This should open up an editor for the task/net/decomposition type");
	}

	/**
	 * Nulls out some of the references in the graph.
	 */
	public void clear() {
		LOG.debug("CapselaGraph.clear");

		// may not be strictly necessary but makes it a little easier to see in
		// profiler what's left
		_graphEditor = null;
		_SharedNode = null;
		_proxy = null;
		_dragSource = null;
		setMarqueeHandler(null);

		setGraphLayoutCache(null); // mjf 2005-05-17 16:15
	}

	/**
	 * @see Object#finalize()
	 */
	public void finalize() throws Throwable {
		LOG.debug("CapselaGraph.finalize");
		clear();

		super.finalize();
	}
}