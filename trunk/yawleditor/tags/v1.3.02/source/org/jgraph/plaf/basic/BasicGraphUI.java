/*
 * @(#)BasicGraphUI.java	1.0 29/2/02
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

package org.jgraph.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.TooManyListenersException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.CellRendererPane;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellHandle;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.EdgeRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphCellEditor;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphContext;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphSelectionModel;
import org.jgraph.graph.GraphTransferable;
import org.jgraph.graph.ParentMap;
import org.jgraph.plaf.GraphUI;

// DO NOT REMOVE OR MODIFY THIS LINE!
import javax.swing.TransferHandler; // JAVA13: org.jgraph.plaf.basic.TransferHandler;

/**
 * The basic L&F for a graph data structure.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class BasicGraphUI extends GraphUI implements Serializable {

	/** Controls live-preview in dragEnabled mode. This is used to disable
	 * live-preview in dragEnabled mode on Java 1.4.0 to workaround a bug that
	 * cause the VM to hang during concurrent DnD and repaints. 
	 * Is this still required? 
	 */
	public static final boolean DNDPREVIEW =
		System.getProperty("java.version").compareTo("1.4.0") < 0
			|| System.getProperty("java.version").compareTo("1.4.0") > 0;

	/** Border in pixels to scroll if marquee or dragging are active. */
	public static int SCROLLBORDER = 18;

	/** Multiplicator for width and height when autoscrolling (=stepsize). */
	public static float SCROLLSTEP = 0.05f;

	/** The maximum number of cells to paint when dragging. */
	public static int MAXCELLS = 20;

	/** The maximum number of handles to paint individually. */
	public static int MAXHANDLES = 20;

	/** Maximum number of cells to compute clipping bounds for. */
	public static int MAXCLIPCELLS = 20;

	/** Minimum preferred size. */
	protected Dimension preferredMinSize;

	/** Component that we're going to be drawing into. */
	protected JGraph graph;

	/** Reference to the graph's view (geometric pattern). */
	protected GraphLayoutCache graphLayoutCache;

	/** Current editor for the graph. */
	protected GraphCellEditor cellEditor;

	/** Set to false when editing and shouldSelectCell() returns true meaning
	  * the node should be selected before editing, used in completeEditing. */
	protected boolean stopEditingInCompleteEditing;

	/** Used to paint the CellRenderer. */
	protected CellRendererPane rendererPane;

	/** Size needed to completely display all the cells. */
	protected Dimension preferredSize;

	/** Is the preferredSize valid? */
	protected boolean validCachedPreferredSize;

	/** Used to determine what to display. */
	protected GraphModel graphModel;

	/** Model maintaining the selection. */
	protected GraphSelectionModel graphSelectionModel;

	/** Handle that we are going to use. */
	protected CellHandle handle;

	/** Marquee that we are going to use. */
	protected BasicMarqueeHandler marquee;

	// Following 4 ivars are only valid when editing.
	/** When editing, this will be the Component that is doing the actual
	  * editing. */
	protected Component editingComponent;

	/** The focused cell under the mousepointer. */
	protected CellView focus;

	/** Path that is being edited. */
	protected Object editingCell;

	/** Set to true if the editor has a different size than the renderer. */
	protected boolean editorHasDifferentSize;

	/** Needed to exchange information between Transfer- and MouseListener. */
	protected Point insertionLocation;

	/** Needed to exchange information between DropTargetHandler and TransferHandler. */
	protected int dropAction = TransferHandler.NONE;

	/**
	 * If ture, a the view under mousepointer will be snapped to the grid lines during
	 * a drag operation. If snap-to-grid mode is disabled, views are moved by a snap
	 * increment.
	 */
	protected boolean snapSelectedView = false;

	// Cached listeners
	/** Listens for JGraph property changes and updates display. */
	protected PropertyChangeListener propertyChangeListener;
	/** Listens for Mouse events. */
	protected MouseListener mouseListener;
	/** Listens for KeyListener events. */
	protected KeyListener keyListener;
	/** Listens for Component events. */
	protected ComponentListener componentListener;
	/** Listens for CellEditor events. */
	protected CellEditorListener cellEditorListener =
		createCellEditorListener();
	/** Updates the display when the selection changes. */
	protected GraphSelectionListener graphSelectionListener;
	/** Is responsible for updating the view based on model events. */
	protected GraphModelListener graphModelListener;
	/** Updates the display when the view has changed. */
	protected Observer graphViewObserver;
	/** The default TransferHandler. */
	protected TransferHandler defaultTransferHandler;
	/** The default DropTargetListener. */
	protected GraphDropTargetListener defaultDropTargetListener;

	public static ComponentUI createUI(JComponent x) {
		return new BasicGraphUI();
	}

	public BasicGraphUI() {
		super();
	}

	//
	// Methods for configuring the behavior of the graph. None of them
	// push the value to the JGraph instance. You should really only
	// call these methods on the JGraph instance.
	//

	/**
	 * Sets the GraphModel. This invokes <code>updateSize</code>.
	 */
	protected void setModel(GraphModel model) {
		cancelEditing(graph);
		if (graphModel != null && graphModelListener != null)
			graphModel.removeGraphModelListener(graphModelListener);
		graphModel = model;
		if (graphModel != null && graphModelListener != null)
			graphModel.addGraphModelListener(graphModelListener);
		if (graphModel != null) // jmv : to avoid NullPointerException 
			updateSize();
	}

	/**
	 * Sets the GraphLayoutCache (geometric pattern).
	 * This invokes <code>updateSize</code>.
	 */
	protected void setGraphLayoutCache(GraphLayoutCache view) {
		cancelEditing(graph);
		if (graphLayoutCache != null && graphViewObserver != null)
			graphLayoutCache.deleteObserver(graphViewObserver);
		graphLayoutCache = view;
		if (graphLayoutCache != null && graphViewObserver != null)
			graphLayoutCache.addObserver(graphViewObserver);
		updateSize();
	}

	/**
	 * Sets the marquee handler.
	 */
	protected void setMarquee(BasicMarqueeHandler marqueeHandler) {
		marquee = marqueeHandler;
	}

	/**
	 * Resets the selection model. The appropriate listeners are installed
	 * on the model.
	 */
	protected void setSelectionModel(GraphSelectionModel newLSM) {
		cancelEditing(graph);
		if (graphSelectionListener != null && graphSelectionModel != null)
			graphSelectionModel.removeGraphSelectionListener(
				graphSelectionListener);
		graphSelectionModel = newLSM;
		if (graphSelectionModel != null && graphSelectionListener != null)
			graphSelectionModel.addGraphSelectionListener(
				graphSelectionListener);
		if (graph != null)
			graph.repaint();
	}

	//
	// GraphUI methods
	//

	/**
	 * Returns the handle that is currently active, or null, if no
	 * handle is currently active. Typically, the returned objects
	 * are instances of the RootHandle inner class.
	 */
	public CellHandle getHandle(JGraph graph) {
		return handle;
	}

	/**
	 * Returns the cell that has the focus.
	 */
	protected Object getFocusedCell() {
		if (focus != null)
			return focus.getCell();
		return null;
	}

	/** Get the preferred Size for a cell view.*/
	public Dimension getPreferredSize(JGraph graph, CellView view) {
		// Either label or icon
		if (view != null) {
			Object cell = view.getCell();
			String valueStr = graph.convertValueToString(cell);
			boolean label = ((valueStr != null) && (valueStr.length() > 0));
			boolean icon =
				GraphConstants.getIcon(view.getAllAttributes()) != null;
			if (label || icon) {
				boolean focus = (getFocusedCell() == cell) && graph.hasFocus();
				// Only ever removed when UI changes, this is OK!
				Component component =
					view.getRendererComponent(graph, focus, false, false);
				if (component != null) {
					graph.add(component);
					component.validate();
					return component.getPreferredSize();
				}
			}
			return view.getBounds().getSize();
		}
		return null;
	}

	//
	// Insertion Location
	//
	// Used to track the location of the mousepointer during Drag-and-Drop.
	//

	/**
	 * Returns the current location of the Drag-and-Drop activity.
	 */
	public Point getInsertionLocation() {
		return insertionLocation;
	}

	/**
	 * Sets the current location for Drag-and-Drop activity. Should be
	 * set to null after a drop. Used from within DropTargetListener.
	 */
	public void setInsertionLocation(Point p) {
		insertionLocation = p;
	}

	//
	// Selection
	//

	/**
	 * From GraphUI interface.
	 */
	public void selectCellsForEvent(
		JGraph graph,
		Object[] cells,
		MouseEvent event) {
		selectCellsForEvent(cells, event);
	}

	/**
	 * Messaged to update the selection based on a MouseEvent for a group of
	 * cells. If the event is a toggle selection event, the cells are either
	 * selected, or deselected. Otherwise the cells are selected.
	 */
	public void selectCellsForEvent(Object[] cells, MouseEvent event) {
		if (cells == null)
			return;

		// Toggle selection
		if (isToggleSelectionEvent(event)) {
			for (int i = 0; i < cells.length; i++)
				toggleSelectionCellForEvent(cells[i], event);

			// Select cells
		} else if (isAddToSelectionEvent(event))
			graph.addSelectionCells(cells);
		else
			graph.setSelectionCells(cells);
	}

	/**
	 * Messaged to update the selection based on a MouseEvent over a
	 * particular cell. If the event is a toggle selection event, the
	 * cell is either selected, or deselected. Otherwise the cell is
	 * selected.
	 */
	public void selectCellForEvent(Object cell, MouseEvent event) {
		// Toggle selection
		if (isToggleSelectionEvent(event))
			toggleSelectionCellForEvent(cell, event);

		// Select cell
		else if (isAddToSelectionEvent(event))
			graph.addSelectionCell(cell);
		else
			graph.setSelectionCell(cell);
	}

	/**
	 * Messaged to update the selection based on a toggle selection
	 * event, which means the cell's selection state is inverted.
	 */
	protected void toggleSelectionCellForEvent(Object cell, MouseEvent event) {
		if (graph.isCellSelected(cell))
			graph.removeSelectionCell(cell);
		else
			graph.addSelectionCell(cell);
	}

	/**
	 * Returning true signifies that cells are added to the selection.
	 */
	public boolean isAddToSelectionEvent(MouseEvent e) {
		return e.isShiftDown();
	}

	/**
	 * Returning true signifies a mouse event on the cell should toggle
	 * the selection of only the cell under mouse.
	 */
	public boolean isToggleSelectionEvent(MouseEvent event) {
		return (event.isControlDown());
	}

	/**
	 * Returning true signifies the marquee handler has precedence over
	 * other handlers, and is receiving subsequent mouse events.
	 */
	public boolean isForceMarqueeEvent(MouseEvent event) {
		if (marquee != null)
			return marquee.isForceMarqueeEvent(event);
		return false;
	}

	/**
	 * Returning true signifies a move should only be
	 * applied to one direction.
	 */
	public boolean isConstrainedMoveEvent(MouseEvent event) {
		if (event != null)
			return event.isShiftDown();
		return false;
	}

	//
	// Editing
	//

	/**
	  * Returns true if the graph is being edited.  The item that is being
	  * edited can be returned by getEditingPath().
	  */
	public boolean isEditing(JGraph graph) {
		return (editingComponent != null);
	}

	/**
	  * Stops the current editing session.  This has no effect if the
	  * graph isn't being edited.  Returns true if the editor allows the
	  * editing session to stop.
	  */
	public boolean stopEditing(JGraph graph) {
		if (editingComponent != null && cellEditor.stopCellEditing()) {
			completeEditing(false, false, true);
			return true;
		}
		return false;
	}

	/**
	  * Cancels all current editing sessions.
	  */
	public void cancelEditing(JGraph graph) {
		if (editingComponent != null)
			completeEditing(false, true, false);
		// Escape key is handled by the KeyHandler.keyPressed inner class method
	}

	/**
	  * Selects the cell and tries to edit it.  Editing will
	  * fail if the CellEditor won't allow it for the selected item.
	  */
	public void startEditingAtCell(JGraph graph, Object cell) {
		graph.scrollCellToVisible(cell);
		if (cell != null)
			startEditing(cell, null);
	}

	/**
	 * Returns the element that is being edited.
	 */
	public Object getEditingCell(JGraph graph) {
		return editingCell;
	}

	//
	// Install methods
	//

	public void installUI(JComponent c) {
		if (c == null)
			throw new NullPointerException("null component passed to BasicGraphUI.installUI()");

		graph = (JGraph) c;
		marquee = graph.getMarqueeHandler();
		prepareForUIInstall();

		// Boilerplate install block
		installDefaults();
		installListeners();
		installKeyboardActions();
		installComponents();

		completeUIInstall();
	}

	/**
	 * Invoked after the <code>graph</code> instance variable has been
	 * set, but before any defaults/listeners have been installed.
	 */
	protected void prepareForUIInstall() {
		// Data member initializations
		stopEditingInCompleteEditing = true;
		preferredSize = new Dimension();
		setGraphLayoutCache(graph.getGraphLayoutCache());
		setModel(graph.getModel());
	}

	/**
	 * Invoked from installUI after all the defaults/listeners have been
	 * installed.
	 */
	protected void completeUIInstall() {
		// Custom install code
		setSelectionModel(graph.getSelectionModel());
		updateSize();
	}

	/**
	 * Invoked as part from the boilerplate install block. This
	 * sets the look and feel specific variables in JGraph.
	 */
	protected void installDefaults() {
		if (graph.getBackground() == null
			|| graph.getBackground() instanceof UIResource) {
			graph.setBackground(UIManager.getColor("Tree.background"));
		}
		if (graph.getFont() == null || graph.getFont() instanceof UIResource)
			graph.setFont(UIManager.getFont("Tree.font"));
		// Set JGraph's laf-specific colors
		graph.setMarqueeColor(UIManager.getColor("Table.gridColor"));
		graph.setHandleColor(
			UIManager.getColor("MenuItem.selectionBackground"));
		graph.setLockedHandleColor(UIManager.getColor("MenuItem.background"));
		graph.setGridColor(UIManager.getColor("Tree.selectionBackground"));
		graph.setOpaque(true);
	}

	/**
	 * Invoked as part from the boilerplate install block. This
	 * installs the listeners from BasicGraphUI in the graph.
	 */
	protected void installListeners() {
		// Install Local Handlers
		TransferHandler th = graph.getTransferHandler();
		if (th == null || th instanceof UIResource) {
			defaultTransferHandler = createTransferHandler();
			graph.setTransferHandler(defaultTransferHandler);
		}
		if (graphLayoutCache != null) {
			graphViewObserver = createGraphViewObserver();
			graphLayoutCache.addObserver(graphViewObserver);
		}
		DropTarget dropTarget = graph.getDropTarget();
		try {
			if (dropTarget != null) {
				defaultDropTargetListener = new GraphDropTargetListener();
				dropTarget.addDropTargetListener(defaultDropTargetListener);
			}
		} catch (TooManyListenersException tmle) {
			// should not happen... swing drop target is multicast
		}

		// Install Listeners
		if ((propertyChangeListener = createPropertyChangeListener()) != null)
			graph.addPropertyChangeListener(propertyChangeListener);
		if ((mouseListener = createMouseListener()) != null) {
			graph.addMouseListener(mouseListener);
			if (mouseListener instanceof MouseMotionListener) {
				graph.addMouseMotionListener(
					(MouseMotionListener) mouseListener);
			}
		}
		if ((keyListener = createKeyListener()) != null) {
			graph.addKeyListener(keyListener);
		}
		if ((graphModelListener = createGraphModelListener()) != null
			&& graphModel != null)
			graphModel.addGraphModelListener(graphModelListener);
		if ((graphSelectionListener = createGraphSelectionListener()) != null
			&& graphSelectionModel != null)
			graphSelectionModel.addGraphSelectionListener(
				graphSelectionListener);
	}

	/**
	 * Invoked as part from the boilerplate install block.
	 */
	protected void installKeyboardActions() {
		InputMap km =
			getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		SwingUtilities.replaceUIInputMap(
			graph,
			JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
			km);
		km = getInputMap(JComponent.WHEN_FOCUSED);
		SwingUtilities.replaceUIInputMap(graph, JComponent.WHEN_FOCUSED, km);
		SwingUtilities.replaceUIActionMap(graph, createActionMap());
	}

	/**
	 * Return JTree's input map.
	 */
	InputMap getInputMap(int condition) {
		if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
			return (InputMap) UIManager.get("Tree.ancestorInputMap");
		else if (condition == JComponent.WHEN_FOCUSED)
			return (InputMap) UIManager.get("Tree.focusInputMap");
		return null;
	}

	/**
	 * Return the mapping between JTree's input map and JGraph's actions.
	 */
	ActionMap createActionMap() {
		// 1: Up, 2: Right, 3: Down, 4: Left
		ActionMap map = new ActionMapUIResource();

		map.put(
			"selectPrevious",
			new GraphIncrementAction(1, "selectPrevious"));
		map.put(
			"selectPreviousChangeLead",
			new GraphIncrementAction(1, "selectPreviousLead"));
		map.put(
			"selectPreviousExtendSelection",
			new GraphIncrementAction(1, "selectPreviousExtendSelection"));

		map.put("selectParent", new GraphIncrementAction(4, "selectParent"));
		map.put(
			"selectParentChangeLead",
			new GraphIncrementAction(4, "selectParentChangeLead"));

		map.put("selectNext", new GraphIncrementAction(3, "selectNext"));
		map.put(
			"selectNextChangeLead",
			new GraphIncrementAction(3, "selectNextLead"));
		map.put(
			"selectNextExtendSelection",
			new GraphIncrementAction(3, "selectNextExtendSelection"));

		map.put("selectChild", new GraphIncrementAction(2, "selectChild"));
		map.put(
			"selectChildChangeLead",
			new GraphIncrementAction(2, "selectChildChangeLead"));

		map.put("cancel", new GraphCancelEditingAction("cancel"));
		map.put("startEditing", new GraphEditAction("startEditing"));
		map.put("selectAll", new GraphSelectAllAction("selectAll", true));
		map.put(
			"clearSelection",
			new GraphSelectAllAction("clearSelection", false));

		map.put(
			TransferHandler.getCutAction().getValue(Action.NAME),
			TransferHandler.getCutAction());
		map.put(
			TransferHandler.getCopyAction().getValue(Action.NAME),
			TransferHandler.getCopyAction());
		map.put(
			TransferHandler.getPasteAction().getValue(Action.NAME),
			TransferHandler.getPasteAction());

		return map;
	}

	/**
	 * Intalls the subcomponents of the graph, which is the renderer pane.
	 */
	protected void installComponents() {
		if ((rendererPane = createCellRendererPane()) != null)
			graph.add(rendererPane);
	}

	//
	// Create methods.
	//

	/**
	 * Creates an instance of TransferHandler. Used for subclassers
	 * to provide different TransferHandler.
	 */
	protected TransferHandler createTransferHandler() {
		return new GraphTransferHandler();
	}

	/**
	 * Creates a listener that is responsible to update the UI based on
	 * how the graph's bounds properties change.
	 */
	protected PropertyChangeListener createPropertyChangeListener() {
		return new PropertyChangeHandler();
	}

	/**
	 * Creates the listener responsible for calling the correct handlers
	 * based on mouse events, and to select invidual cells.
	 */
	protected MouseListener createMouseListener() {
		return new MouseHandler();
	}

	/**
	 * Creates the listener reponsible for getting key events from
	 * the graph.
	 */
	protected KeyListener createKeyListener() {
		return new KeyHandler();
	}

	/**
	 * Creates the listener that updates the display based on selection change
	 * methods.
	 */
	protected GraphSelectionListener createGraphSelectionListener() {
		return new GraphSelectionHandler();
	}

	/**
	 * Creates a listener to handle events from the current editor.
	 */
	protected CellEditorListener createCellEditorListener() {
		return new CellEditorHandler();
	}

	/**
	 * Creates and returns a new ComponentHandler.
	 */
	protected ComponentListener createComponentListener() {
		return new ComponentHandler();
	}

	/**
	 * Returns the renderer pane that renderer components are placed in.
	 */
	protected CellRendererPane createCellRendererPane() {
		return new CellRendererPane();
	}

	/**
	 * Returns a listener that can update the graph when the view changes.
	 */
	protected Observer createGraphViewObserver() {
		return new GraphViewObserver();
	}

	/**
	 * Returns a listener that can update the graph when the model changes.
	 */
	protected GraphModelListener createGraphModelListener() {
		return new GraphModelHandler();
	}

	//
	// Uninstall methods
	//

	public void uninstallUI(JComponent c) {
		cancelEditing(graph);
		uninstallListeners();
		uninstallKeyboardActions();
		uninstallComponents();

		completeUIUninstall();
	}

	protected void completeUIUninstall() {
		graphLayoutCache = null;
		rendererPane = null;
		componentListener = null;
		propertyChangeListener = null;
		keyListener = null;
		setSelectionModel(null);
		graph = null;
		graphModel = null;
		graphSelectionModel = null;
		graphSelectionListener = null;
	}

	protected void uninstallListeners() {
		// Uninstall Handlers
		TransferHandler th = graph.getTransferHandler();
		if (th == defaultTransferHandler)
			graph.setTransferHandler(null);
		if (graphViewObserver != null)
			graphLayoutCache.deleteObserver(graphViewObserver);
		if (graph.getDropTarget() != null && defaultDropTargetListener != null)
			graph.getDropTarget().removeDropTargetListener(
				defaultDropTargetListener);
		if (componentListener != null)
			graph.removeComponentListener(componentListener);
		if (propertyChangeListener != null)
			graph.removePropertyChangeListener(propertyChangeListener);
		if (mouseListener != null) {
			graph.removeMouseListener(mouseListener);
			if (mouseListener instanceof MouseMotionListener)
				graph.removeMouseMotionListener(
					(MouseMotionListener) mouseListener);
		}
		if (keyListener != null)
			graph.removeKeyListener(keyListener);
		if (graphModel != null && graphModelListener != null)
			graphModel.removeGraphModelListener(graphModelListener);
		if (graphSelectionListener != null && graphSelectionModel != null)
			graphSelectionModel.removeGraphSelectionListener(
				graphSelectionListener);
	}

	protected void uninstallKeyboardActions() {
		SwingUtilities.replaceUIActionMap(graph, null);
		SwingUtilities.replaceUIInputMap(
			graph,
			JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
			null);
		SwingUtilities.replaceUIInputMap(graph, JComponent.WHEN_FOCUSED, null);
	}

	/**
	 * Uninstalls the renderer pane.
	 */
	protected void uninstallComponents() {
		if (rendererPane != null)
			graph.remove(rendererPane);
	}

	//
	// Painting routines.
	//

	/**
	 * Main painting routine.
	 */
	public void paint(Graphics g, JComponent c) {
		if (graph != c)
			throw new InternalError(
				"BasicGraphUI cannot paint "
					+ c.toString()
					+ "; "
					+ graph
					+ " was expected.");

		Graphics2D g2 = (Graphics2D) g;
		Rectangle paintBounds = new Rectangle(g.getClipBounds());
		Rectangle real = graph.fromScreen(new Rectangle(paintBounds));

		// Paint Background (Typically Grid)
		paintBackground(g);

		// Remember current affine transform
		AffineTransform at = g2.getTransform();

		// Use anti aliasing
		if (graph.isAntiAliased())
			g2.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// Use Swing's scaling
		double scale = graph.getScale();
		g2.scale(scale, scale);

		// Paint cells
		CellView[] views = graphLayoutCache.getRoots();
		for (int i = 0; i < views.length; i++) {
			Rectangle bounds = views[i].getBounds();
			if (bounds != null && real != null && bounds.intersects(real))
				paintCell(g, views[i], bounds, false);
		}

		// Reset affine transform and antialias
		g2.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.setTransform(at);

		// Paint Foreground (Typically Ports)
		paintForeground(g);

		// Paint Handle
		if (handle != null)
			handle.paint(g);

		// Paint Marquee
		g.setColor(graph.getMarqueeColor());
		g.setXORMode(graph.getBackground());
		if (marquee != null)
			marquee.paint(g);
		g.setPaintMode();

		// Empty out the renderer pane, allowing renderers to be gc'ed.
		if (rendererPane != null)
			rendererPane.removeAll();
	}

	/**
	 * Paints the renderer of <code>view</code> to <code>g</code>
	 * at <code>bounds</code>. Recursive implementation that
	 * paints the children first.<p>
	 * The reciever should NOT modify <code>clipBounds</code>, or
	 * <code>insets</code>. The <code>preview</code> flag is passed to
	 * the renderer, and is not used here.
	 */
	public void paintCell(
		Graphics g,
		CellView view,
		Rectangle bounds,
		boolean preview) {
		// First Paint View
		if (view != null && bounds != null) {
			boolean bfocus = (view == this.focus);
			boolean sel = graph.isCellSelected(view.getCell());
			Component component =
				view.getRendererComponent(graph, sel, bfocus, preview);
			rendererPane.paintComponent(
				g,
				component,
				graph,
				bounds.x,
				bounds.y,
				bounds.width,
				bounds.height,
				true);
		}
		// Then Paint Children
		if (!view.isLeaf()) {
			CellView[] children = view.getChildViews();
			for (int i = 0; i < children.length; i++)
				paintCell(g, children[i], children[i].getBounds(), preview);
		}
	}

	//
	// Background
	//

	/**
	 * Paint the background of this graph. Calls paintGrid.
	 */
	protected void paintBackground(Graphics g) {
		if (graph.isGridVisible())
			paintGrid(graph.getGridSize(), g, g.getClipBounds());
	}

	/**
	 * Paint the grid.
	 */
	protected void paintGrid(int gs, Graphics g, Rectangle r) {

		//  Parameter "r" is never used: remove it.

		Rectangle rr = (Rectangle) g.getClipBounds();
		double xl = rr.x;
		double yt = rr.y;
		double xr = xl + rr.width;
		double yb = yt + rr.height;
		double sgs = (double) gs * graph.getScale();
		while (sgs < 2)
			sgs *= 2;

		if (sgs >= 0.0) {

			int xs = (int) (Math.floor(xl / sgs) * sgs);
			int xe = (int) (Math.ceil(xr / sgs) * sgs);
			int ys = (int) (Math.floor(yt / sgs) * sgs);
			int ye = (int) (Math.ceil(yb / sgs) * sgs);

			g.setColor(graph.getGridColor());

			switch (graph.getGridMode()) {

				case JGraph.CROSS_GRID_MODE :
					{

						int cs = (sgs > 16.0) ? 2 : ((sgs < 8.0) ? 0 : 1);

						for (double x = xs; x <= xe; x += sgs) {
							for (double y = ys; y <= ye; y += sgs) {
								int ix = (int) Math.round(x);
								int iy = (int) Math.round(y);

								g.drawLine(ix - cs, iy, ix + cs, iy);
								g.drawLine(ix, iy - cs, ix, iy + cs);

							}
						}

					}
					break;

				case JGraph.LINE_GRID_MODE :
					{

						xe += (int) Math.ceil(sgs);
						ye += (int) Math.ceil(sgs);

						for (double x = xs; x <= xe; x += sgs) {

							int ix = (int) Math.round(x);

							g.drawLine(ix, ys, ix, ye);

						}

						for (double y = ys; y <= ye; y += sgs) {

							int iy = (int) Math.round(y);

							g.drawLine(xs, iy, xe, iy);

						}

					}
					break;

				case JGraph.DOT_GRID_MODE :
				default :
					for (double x = xs; x <= xe; x += sgs) {
						for (double y = ys; y <= ye; y += sgs) {

							int ix = (int) Math.round(x);
							int iy = (int) Math.round(y);

							g.drawLine(ix, iy, ix, iy);

						}
					}
					break;

			}

		}

	}

	//
	// Foreground
	//

	/**
	 * Paint the foreground of this graph. Calls paintPorts.
	 */
	protected void paintForeground(Graphics g) {
		if (graph.isPortsVisible())
			paintPorts(g, graphLayoutCache.getPorts());
	}

	/**
	 * Paint <code>ports</code>.
	 */
	public void paintPorts(Graphics g, CellView[] ports) {
		if (ports != null) {
			Rectangle r = g.getClipBounds();
			for (int i = 0; i < ports.length; i++) {
				if (ports[i] != null) {
					Rectangle bounds = new Rectangle(ports[i].getBounds());
					Point center =
						graph.toScreen(
							new Point(
								(int) bounds.getCenterX(),
								(int) bounds.getCenterY()));
					bounds.setLocation(
						center.x - bounds.width / 2,
						center.y - bounds.height / 2);
					if (r == null || bounds.intersects(r))
						paintCell(g, ports[i], bounds, false);
				}
			}
		}
	}

	//
	// Various local methods
	//

	/**
	 * Update the handle using createHandle.
	 */
	protected void updateHandle() {
		if (graphLayoutCache != null) {
			Object[] cells = graphLayoutCache.order(graph.getSelectionCells());
			if (cells != null && cells.length > 0)
				handle = createHandle(createContext(graph, cells));
			else
				handle = null;
		}
	}

	protected GraphContext createContext(JGraph graph, Object[] cells) {
		return new GraphContext(graph, cells);
	}

	/**
	 * Constructs the "root handle" for <code>context</code>.
	 *
	 * @param context reference to the context of the current selection.
	 */
	public CellHandle createHandle(GraphContext context) {
		if (context != null && !context.isEmpty() && graph.isEnabled())
			return new RootHandle(context);
		return null;
	}

	/**
	 * Messages the Graph with <code>graphDidChange</code>.
	 */
	public void updateSize() {
		validCachedPreferredSize = false;
		graph.graphDidChange();
		updateHandle();
	}

	/**
	 * Updates the <code>preferredSize</code> instance variable,
	 * which is returned from <code>getPreferredSize()</code>.
	 */
	protected void updateCachedPreferredSize() {
		Rectangle size =
			AbstractCellView.getBounds(graphLayoutCache.getRoots());
		if (size == null)
			size = new Rectangle();
		Point psize = new Point(size.x + size.width, size.y + size.height);
		Dimension d = graph.getMinimumSize();
		Point min =
			(d != null)
				? graph.toScreen(new Point(d.width, d.height))
				: new Point(0, 0);
		Point scaled = graph.toScreen(psize);
		preferredSize =
			new Dimension(Math.max(min.x, scaled.x), Math.max(min.y, scaled.y));
		Insets in = graph.getInsets();
		if (in != null) {
			preferredSize.width += in.left + in.right;
			preferredSize.height += in.top + in.bottom;
		}
		validCachedPreferredSize = true;
	}

	/** Sets the preferred minimum size.
	  */
	public void setPreferredMinSize(Dimension newSize) {
		preferredMinSize = newSize;
	}

	/** Returns the minimum preferred size.
	  */
	public Dimension getPreferredMinSize() {
		if (preferredMinSize == null)
			return null;
		return new Dimension(preferredMinSize);
	}

	/** Returns the preferred size to properly display the graph.
	  */
	public Dimension getPreferredSize(JComponent c) {
		Dimension pSize = this.getPreferredMinSize();

		if (!validCachedPreferredSize)
			updateCachedPreferredSize();
		if (graph != null) {
			if (pSize != null)
				return new Dimension(
					Math.max(pSize.width, preferredSize.width),
					Math.max(pSize.height, preferredSize.height));
			return new Dimension(preferredSize.width, preferredSize.height);
		} else if (pSize != null)
			return pSize;
		else
			return new Dimension(0, 0);
	}

	/**
	  * Returns the minimum size for this component.  Which will be
	  * the min preferred size or 0, 0.
	  */
	public Dimension getMinimumSize(JComponent c) {
		if (this.getPreferredMinSize() != null)
			return this.getPreferredMinSize();
		return new Dimension(0, 0);
	}

	/**
	  * Returns the maximum size for this component, which will be the
	  * preferred size if the instance is currently in a JGraph, or 0, 0.
	  */
	public Dimension getMaximumSize(JComponent c) {
		if (graph != null)
			return getPreferredSize(graph);
		if (this.getPreferredMinSize() != null)
			return this.getPreferredMinSize();
		return new Dimension(0, 0);
	}

	/**
	 * Messages to stop the editing session. If the UI the receiver
	 * is providing the look and feel for returns true from
	 * <code>getInvokesStopCellEditing</code>, stopCellEditing will
	 * invoked on the current editor. Then completeEditing will
	 * be messaged with false, true, false to cancel any lingering
	 * editing.
	 */
	protected void completeEditing() {
		/* If should invoke stopCellEditing, try that */
		if (graph.getInvokesStopCellEditing()
			&& stopEditingInCompleteEditing
			&& editingComponent != null) {
			cellEditor.stopCellEditing();
		}
		/* Invoke cancelCellEditing, this will do nothing if stopCellEditing
		   was succesful. */
		completeEditing(false, true, false);
	}

	/**
	  * Stops the editing session.  If messageStop is true the editor
	  * is messaged with stopEditing, if messageCancel is true the
	  * editor is messaged with cancelEditing. If messageGraph is true
	  * the graphModel is messaged with valueForCellChanged.
	  */
	protected void completeEditing(
		boolean messageStop,
		boolean messageCancel,
		boolean messageGraph) {
		if (stopEditingInCompleteEditing && editingComponent != null) {
			Component oldComponent = editingComponent;
			Object oldCell = editingCell;
			GraphCellEditor oldEditor = cellEditor;
			Object newValue = oldEditor.getCellEditorValue();
			boolean requestFocus =
				(graph != null
					&& (graph.hasFocus() || editingComponent.hasFocus()));
			editingCell = null;
			editingComponent = null;
			if (messageStop)
				oldEditor.stopCellEditing();
			else if (messageCancel)
				oldEditor.cancelCellEditing();
			graph.remove(oldComponent);
			if (requestFocus)
				graph.requestFocus();
			if (messageGraph) {
				Map nested =
					GraphConstants.createAttributes(
						oldCell,
						GraphConstants.VALUE,
						newValue);
				graphLayoutCache.edit(nested, null, null, null);
			}
			updateSize();
			// Remove Editor Listener
			if (oldEditor != null && cellEditorListener != null)
				oldEditor.removeCellEditorListener(cellEditorListener);
			cellEditor = null;
		}
	}

	/**
	  * Will start editing for cell if there is a cellEditor and
	  * shouldSelectCell returns true.<p>
	  * This assumes that cell is valid and visible.
	  */
	protected boolean startEditing(Object cell, MouseEvent event) {
		completeEditing();
		if (graph.isCellEditable(cell)) {
			CellView tmp = graphLayoutCache.getMapping(cell, false);
			cellEditor = tmp.getEditor();
			editingComponent =
				cellEditor.getGraphCellEditorComponent(
					graph,
					cell,
					graph.isCellSelected(cell));
			if (cellEditor.isCellEditable(event)) {
				Rectangle cellBounds = graph.getCellBounds(cell);

				editingCell = cell;

				Dimension editorSize = editingComponent.getPreferredSize();

				graph.add(editingComponent);
				Point p = graph.toScreen(new Point(cellBounds.x, cellBounds.y));

				// Edges have different editor position and size
				if (graphLayoutCache.getMapping(cell, false)
					instanceof EdgeView) {
					EdgeView edgeView =
						(EdgeView) graphLayoutCache.getMapping(cell, false);
					EdgeRenderer er = edgeView.getEdgeRenderer();
					p = er.getLabelPosition(edgeView);
					p.x = p.x - editorSize.width / 2;
					p.y = p.y - editorSize.height / 2;
					graph.toScreen(p);
				}
				editingComponent.setBounds(
					p.x,
					p.y,
					editorSize.width,
					editorSize.height);
				editingCell = cell;
				editingComponent.validate();

				// Add Editor Listener
				if (cellEditorListener == null)
					cellEditorListener = createCellEditorListener();
				if (cellEditor != null && cellEditorListener != null)
					cellEditor.addCellEditorListener(cellEditorListener);
				Rectangle visRect = graph.getVisibleRect();
				graph.paintImmediately(
					p.x,
					p.y,
					visRect.width + visRect.x - cellBounds.x,
					editorSize.height);
				if (cellEditor.shouldSelectCell(event)) {
					stopEditingInCompleteEditing = false;
					try {
						graph.setSelectionCell(cell);
					} catch (Exception e) {
						System.err.println("Editing exception: " + e);
					}
					stopEditingInCompleteEditing = true;
				}

				if (event instanceof MouseEvent) {
					/* Find the component that will get forwarded all the
					   mouse events until mouseReleased. */
					Point componentPoint =
						SwingUtilities.convertPoint(
							graph,
							new Point(event.getX(), event.getY()),
							editingComponent);

					/* Create an instance of BasicTreeMouseListener to handle
					   passing the mouse/motion events to the necessary
					   component. */
					// We really want similiar behavior to getMouseEventTarget,
					// but it is package private.
					Component activeComponent =
						SwingUtilities.getDeepestComponentAt(
							editingComponent,
							componentPoint.x,
							componentPoint.y);
					if (activeComponent != null) {
						new MouseInputHandler(graph, activeComponent, event);
					}
				}
				return true;
			} else
				editingComponent = null;
		}
		return false;
	}

	/**
	 * Scroll the graph for an event at <code>p</code>.
	 */
	public static void autoscroll(JGraph graph, Point p) {
		Rectangle view = graph.getBounds();
		if (graph.getParent() instanceof JViewport)
			view = ((JViewport) graph.getParent()).getViewRect();
		if (view.contains(p)) {
			Point target = new Point(p);
			int dx = (int) (graph.getWidth() * SCROLLSTEP);
			int dy = (int) (graph.getHeight() * SCROLLSTEP);
			if (target.x - view.x < SCROLLBORDER)
				target.x -= dx;
			if (target.y - view.y < SCROLLBORDER)
				target.y -= dy;
			if (view.x + view.width - target.x < SCROLLBORDER)
				target.x += dx;
			if (view.y + view.height - target.y < SCROLLBORDER)
				target.y += dy;
			graph.scrollPointToVisible(target);
		}
	}

	/**
	 * Updates the preferred size when scrolling (if necessary).
	 */
	public class ComponentHandler
		extends ComponentAdapter
		implements ActionListener {
		/** Timer used when inside a scrollpane and the scrollbar is
		 * adjusting. */
		protected Timer timer;
		/** ScrollBar that is being adjusted. */
		protected JScrollBar scrollBar;

		public void componentMoved(ComponentEvent e) {
			if (timer == null) {
				JScrollPane scrollPane = getScrollPane();

				if (scrollPane == null)
					updateSize();
				else {
					scrollBar = scrollPane.getVerticalScrollBar();
					if (scrollBar == null
						|| !scrollBar.getValueIsAdjusting()) {
						// Try the horizontal scrollbar.
						if ((scrollBar = scrollPane.getHorizontalScrollBar())
							!= null
							&& scrollBar.getValueIsAdjusting())
							startTimer();
						else
							updateSize();
					} else
						startTimer();
				}
			}
		}

		/**
		 * Creates, if necessary, and starts a Timer to check if need to
		 * resize the bounds.
		 */
		protected void startTimer() {
			if (timer == null) {
				timer = new Timer(200, this);
				timer.setRepeats(true);
			}
			timer.start();
		}

		/**
		 * Returns the JScrollPane housing the JGraph, or null if one isn't
		 * found.
		 */
		protected JScrollPane getScrollPane() {
			Component c = graph.getParent();

			while (c != null && !(c instanceof JScrollPane))
				c = c.getParent();
			if (c instanceof JScrollPane)
				return (JScrollPane) c;
			return null;
		}

		/**
		 * Public as a result of Timer. If the scrollBar is null, or
		 * not adjusting, this stops the timer and updates the sizing.
		 */
		public void actionPerformed(ActionEvent ae) {
			if (scrollBar == null || !scrollBar.getValueIsAdjusting()) {
				if (timer != null)
					timer.stop();
				updateSize();
				timer = null;
				scrollBar = null;
			}
		}
	} // End of BasicGraphUI.ComponentHandler

	/**
	 * Listens for changes in the graph model and updates the view
	 * accordingly.
	 */
	public class GraphModelHandler
		implements GraphModelListener, Serializable {

		public void graphChanged(GraphModelEvent e) {
			if (graphLayoutCache != null)
				graphLayoutCache.graphChanged(e.getChange());
			// Get arrays
			Object[] inserted = e.getChange().getInserted();
			Object[] removed = e.getChange().getRemoved();
			// Insert
			if (inserted != null && inserted.length > 0)
				// Update focus to first inserted cell
				focus = graphLayoutCache.getMapping(inserted[0], false);
			// Remove
			if (removed != null && removed.length > 0) {
				// Update Focus If Necessary
				for (int i = 0; i < removed.length && focus != null; i++)
					if (removed[i] == focus.getCell())
						focus = null;
				// Remove from selection
				graph.getSelectionModel().removeSelectionCells(removed);
			}
			updateSize();
			// Select new root cells
			if (inserted != null
				&& inserted.length > 0
				&& graph.isSelectNewCells()) {
				java.util.List toSelect = new ArrayList();
				for (int i = 0; i < inserted.length; i++) {
					if (graphModel.getParent(inserted[i]) == null
						&& graphLayoutCache.isVisible(inserted[i])) {
						toSelect.add(inserted[i]);
					}
				}
				graph.setSelectionCells(toSelect.toArray());
				graph.requestFocus();
			}
		}

	} // End of BasicGraphUI.GraphModelHandler

	/**
	 * Listens for changes in the graph view and updates the size
	 * accordingly.
	 */
	public class GraphViewObserver implements Observer, Serializable {

		public void update(Observable o, Object arg) {
			updateSize();
		}

	} // End of BasicGraphUI.GraphModelHandler

	/**
	 * Listens for changes in the selection model and updates the display
	 * accordingly.
	 */
	public class GraphSelectionHandler
		implements GraphSelectionListener, Serializable {
		/**
		 * Messaged when the selection changes in the graph we're displaying
		 * for. Stops editing, updates handles and displays the changed cells.
		 */
		public void valueChanged(GraphSelectionEvent event) {
			//cancelEditing(graph);
			updateHandle();
			Object[] cells = event.getCells();
			if (cells != null && cells.length <= MAXCLIPCELLS) {
				Rectangle r = graph.toScreen(graph.getCellBounds(cells));
				if (r != null) {
					int hsize = graph.getHandleSize();
					r.grow(hsize, hsize); //padding to paint handles
					updateHandle();
					graph.repaint(r);
				}
			} else
				graph.repaint();
		}
	}

	/**
	 * Listener responsible for getting cell editing events and updating
	 * the graph accordingly.
	 */
	public class CellEditorHandler
		implements CellEditorListener, Serializable {
		/** Messaged when editing has stopped in the graph. */
		public void editingStopped(ChangeEvent e) {
			completeEditing(false, false, true);
		}

		/** Messaged when editing has been canceled in the graph. */
		public void editingCanceled(ChangeEvent e) {
			completeEditing(false, false, false);
		}
	} // BasicGraphUI.CellEditorHandler

	/**
	 * This is used to get mutliple key down events to appropriately generate
	 * events.
	 */
	public class KeyHandler extends KeyAdapter implements Serializable {
		/** Key code that is being generated for. */
		protected Action repeatKeyAction;

		/** Set to true while keyPressed is active. */
		protected boolean isKeyDown;

		public void keyPressed(KeyEvent e) {
			if (graph != null && graph.hasFocus() && graph.isEnabled()) {
				KeyStroke keyStroke =
					KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers());

				if (graph.getConditionForKeyStroke(keyStroke)
					== JComponent.WHEN_FOCUSED) {
					ActionListener listener =
						graph.getActionForKeyStroke(keyStroke);

					if (listener instanceof Action)
						repeatKeyAction = (Action) listener;
					else
						repeatKeyAction = null;
				} else {
					repeatKeyAction = null;
					if (keyStroke.getKeyCode() == KeyEvent.VK_ESCAPE) {
						if (marquee != null)
							marquee.mouseReleased(null);
						if (mouseListener != null)
							mouseListener.mouseReleased(null);
						updateHandle();
						graph.repaint();
					}
				}
				if (isKeyDown && repeatKeyAction != null) {
					repeatKeyAction.actionPerformed(
						new ActionEvent(
							graph,
							ActionEvent.ACTION_PERFORMED,
							""));
					e.consume();
				} else
					isKeyDown = true;
			}
		}

		public void keyReleased(KeyEvent e) {
			isKeyDown = false;
		}

	} // End of BasicGraphUI.KeyHandler

	/**
	 * TreeMouseListener is responsible for updating the selection
	 * based on mouse events.
	 */
	public class MouseHandler
		extends MouseAdapter
		implements MouseMotionListener, Serializable {

		/* The cell under the mousepointer. */
		protected CellView cell;

		/* The object that handles mouse operations. */
		protected Object handler;

		protected transient Cursor previousCursor = null;

		/**
		 * Invoked when a mouse button has been pressed on a component.
		 */
		public void mousePressed(MouseEvent e) {
			handler = null;
			if (!e.isConsumed() && graph.isEnabled()) {
				graph.requestFocus();
				int s = graph.getTolerance();
				Rectangle r =
					graph.fromScreen(
						new Rectangle(
							e.getX() - s,
							e.getY() - s,
							2 * s,
							2 * s));
				Point point = graph.fromScreen(new Point(e.getPoint()));

				focus =
					(focus != null && focus.intersects(graph.getGraphics(), r))
						? focus
						: null;
				cell = graph.getNextViewAt(focus, point.x, point.y);
				if (focus == null)
					focus = cell;
				completeEditing();
				if (!isForceMarqueeEvent(e)) {
					if (e.getClickCount() == graph.getEditClickCount()
						&& focus != null
						&& focus.isLeaf()
						&& focus.getParentView() == null) {
						// Start Editing
						handleEditTrigger(focus.getCell(), e);
						e.consume();
						cell = null;
					} else if (!isToggleSelectionEvent(e)) {
						if (handle != null) {
							handle.mousePressed(e);
							handler = handle;
						}
						// Immediate Selection
						if (!e.isConsumed()
							&& cell != null
							&& !graph.isCellSelected(cell)) {
							selectCellForEvent(cell.getCell(), e);
							focus = cell;
							if (handle != null) {
								handle.mousePressed(e);
								handler = handle;
							}
							e.consume();
							cell = null;
						}
					}
				}
				//Marquee Selection
				if (!e.isConsumed()
					&& (!isToggleSelectionEvent(e) || focus == null)) {
					if (marquee != null) {
						marquee.mousePressed(e);
						handler = marquee;
					}
				}
			}
		}

		protected void handleEditTrigger(Object cell, MouseEvent e) {
			graph.scrollCellToVisible(cell);
			if (cell != null)
				startEditing(cell, e);
		}

		public void mouseDragged(MouseEvent e) {
			try {
				autoscroll(graph, e.getPoint());
				if (handler != null && handler == marquee)
					marquee.mouseDragged(e);
				else if (
					handler == null && !isEditing(graph) && focus != null) {
					if (!graph.isCellSelected(focus.getCell())) {
						selectCellForEvent(focus.getCell(), e);
						cell = null;
					}
					if (handle != null)
						handle.mousePressed(e);
					handler = handle;
				}
				if (handle != null && handler == handle)
					handle.mouseDragged(e);
			} finally {
				//cell = null;
			}
		}

		/**
		 * Invoked when the mouse pointer has been moved on a component
		 * (with no buttons down).
		 */
		public void mouseMoved(MouseEvent e) {
			if (previousCursor == null)
				previousCursor = graph.getCursor();
			if (graph != null && graph.isEnabled()) {
				if (marquee != null)
					marquee.mouseMoved(e);
				if (handle != null)
					handle.mouseMoved(e);
				if (!e.isConsumed() && previousCursor != null) {
					graph.setCursor(previousCursor);
					previousCursor = null;
				}
			}
		}

		// Event may be null when called to cancel the current operation.
		public void mouseReleased(MouseEvent e) {
			try {
				if (e != null && !e.isConsumed()) {
					if (handler == marquee && marquee != null)
						marquee.mouseReleased(e);
					else if (handler == handle && handle != null)
						handle.mouseReleased(e);
					if (isDescendant(cell, focus) && e.getModifiers() != 0) {
						// Do not switch to parent if Special Selection
						cell = focus;
					}
					if (!e.isConsumed() && cell != null) {
						Object tmp = cell.getCell();
						boolean wasSelected = graph.isCellSelected(tmp);
						selectCellForEvent(tmp, e);
						focus = cell;
						postProcessSelection(e, tmp, wasSelected);
					}
				}
			} finally {
				handler = null;
				cell = null;
			}
		}

		/**
		 * Invoked after a cell has been selected in the mouseReleased method.
		 * This can be used to do something interesting if the cell was already
		 * selected, in which case this implementation selects the parent. 
		 * Override if you want different behaviour, such as start editing.
		 */
		protected void postProcessSelection(
			MouseEvent e,
			Object cell,
			boolean wasSelected) {
			if (wasSelected && graph.isCellSelected(cell)) {
				Object parent = cell;
				Object nextParent = null;
				while (((nextParent = graphModel.getParent(parent)) != null)
					&& graphLayoutCache.isVisible(nextParent))
					parent = nextParent;
				selectCellForEvent(parent, e);
				focus = graphLayoutCache.getMapping(parent, false);
			}
		}

		protected boolean isDescendant(
			CellView parentView,
			CellView childView) {
			if (parentView == null || childView == null) {
				return false;
			}

			Object parent = parentView.getCell();
			Object child = childView.getCell();
			Object ancestor = child;

			do {
				if (ancestor == parent)
					return true;
			} while ((ancestor = graphModel.getParent(ancestor)) != null);

			return false;
		}

	} // End of BasicGraphUI.MouseHandler

	public class RootHandle implements CellHandle, Serializable {
		//x and y offset from the mouse press event to the left/top corner of a view that is returned by a findViewForPoint().
		//These are used only when the isSnapSelectedView mode is enabled.
		protected transient int _mouseToViewDelta_x = 0;
		protected transient int _mouseToViewDelta_y = 0;

		// Double Buffered
		protected transient Image offscreen;
		protected transient Graphics offgraphics;
		protected transient boolean firstDrag = true;

		/* Temporary views for the cells. */
		protected transient CellView[] views;

		protected transient CellView[] contextViews;

		protected transient CellView[] portViews;

		/* Bounds of the cells. Non-null if too many cells. */
		protected transient Rectangle cachedBounds;

		/* Child handles. Null if too many handles. */
		protected transient CellHandle[] handles;

		/* The point where the mouse was pressed. */
		protected transient Point start = null, last, snapStart, snapLast;

		/**
		 * Indicates whether this handle is currently moving cells. Start
		 * may be non-null and isMoving false while the minimum movement
		 * has not been reached.
		 */
		protected boolean isMoving = false;

		/**
		 * Indicates whether this handle has started drag and drop.
		 * Note: isDragging => isMoving.
		 */
		protected boolean isDragging = false;

		/** The handle that consumed the last mousePressedEvent. Initially null. */
		protected transient CellHandle activeHandle = null;

		/* The current selection context, responsible for cloning the cells. */
		protected transient GraphContext context;

		/* True after the graph was repainted to block xor-ed painting of background. */
		protected boolean isContextVisible = true;

		protected boolean blockPaint = false;

		/* Defines the Disconnection if DisconnectOnMove is True */
		protected transient ConnectionSet disconnect = null;

		/**
		 * Creates a root handle which contains handles for the given
		 * cells. The root handle and all its childs point to the
		 * specified JGraph instance. The root handle is responsible
		 * for dragging the selection.
		 */
		public RootHandle(GraphContext ctx) {
			this.context = ctx;
			if (!ctx.isEmpty()) {
				// Temporary cells
				views = ctx.createTemporaryCellViews();
				if (ctx.getDescendantCount() < MAXCELLS)
					contextViews = ctx.createTemporaryContextViews();
				else
					cachedBounds =
						graph.toScreen(graph.getCellBounds(ctx.getCells()));
				// Sub-Handles
				Object[] cells = ctx.getCells();
				if (cells.length < MAXHANDLES) {
					handles = new CellHandle[views.length];
					for (int i = 0; i < views.length; i++)
						handles[i] = views[i].getHandle(ctx);
					// PortView Preview
					portViews = ctx.createTemporaryPortViews();
				}
			}
		}

		/* Returns the context of this root handle. */
		public GraphContext getContext() {
			return context;
		}

		/* Paint the handles. Use overlay to paint the current state. */
		public void paint(Graphics g) {
			if (handles != null && handles.length < MAXHANDLES)
				for (int i = 0; i < handles.length; i++)
					if (handles[i] != null)
						handles[i].paint(g);
			blockPaint = true;
		}

		public void overlay(Graphics g) {
			if (isDragging && !DNDPREVIEW) // BUG IN 1.4
				return;
			if (cachedBounds != null) { // Paint Cached Bounds
				g.setColor(Color.black);
				g.drawRect(
					cachedBounds.x,
					cachedBounds.y,
					cachedBounds.width - 2,
					cachedBounds.height - 2);
			} else {
				Graphics2D g2 = (Graphics2D) g;
				AffineTransform oldTransform = g2.getTransform();
				g2.scale(graph.getScale(), graph.getScale());
				if (views != null) { // Paint Temporary Views
					for (int i = 0; i < views.length; i++)
						paintCell(g, views[i], views[i].getBounds(), true);
				}
				if (contextViews != null
					&& isContextVisible) { // Paint Temporary Context
					for (int i = 0; i < contextViews.length; i++)
						paintCell(
							g,
							contextViews[i],
							contextViews[i].getBounds(),
							true);
				}
				g2.setTransform(oldTransform);
				if (portViews != null && graph.isPortsVisible())
					paintPorts(g, portViews);
			}
		}

		/**
		 * Invoked when the mouse pointer has been moved on a component
		 * (with no buttons down).
		 */
		public void mouseMoved(MouseEvent event) {
			if (!event.isConsumed() && handles != null)
				for (int i = handles.length - 1;
					i >= 0 && !event.isConsumed();
					i--)
					if (handles[i] != null)
						handles[i].mouseMoved(event);
		}

		public void mousePressed(MouseEvent event) {
			if (!event.isConsumed() && graph.isMoveable()) {
				if (handles != null) { // Find Handle
					for (int i = handles.length - 1; i >= 0; i--) {
						if (handles[i] != null) {
							handles[i].mousePressed(event);
							if (event.isConsumed()) {
								activeHandle = handles[i];
								return;
							}
						}
					}
				}
				if (views != null) { // Start Move if over cell
					Point screenPoint = event.getPoint();
					Point pt = graph.fromScreen(new Point(screenPoint));
					CellView view = findViewForPoint(pt);
					if (view != null) {
						if (snapSelectedView) {
							Rectangle bounds = view.getBounds();
							start =
								graph.toScreen(new Point(bounds.x, bounds.y));
							snapStart = graph.snap(new Point(start));
							_mouseToViewDelta_x = screenPoint.x - start.x;
							_mouseToViewDelta_y = screenPoint.y - start.y;
						} else { //this is the original RootHandle's mode.
							snapStart = graph.snap(new Point(screenPoint));
							_mouseToViewDelta_x = snapStart.x - screenPoint.x;
							_mouseToViewDelta_y = snapStart.y - screenPoint.y;
							start = new Point(snapStart);
						}
						last = new Point(start);
						snapLast = new Point(snapStart);
						isContextVisible =
							contextViews != null
								&& contextViews.length < MAXCELLS
								&& (!event.isControlDown()
									|| !graph.isCloneable());
						event.consume();
					}
				}
			}
		}

		/**
		 * Hook for subclassers to return a different view for a mouse click
		 * at <code>pt</code>. For example, this can be used to return a leaf
		 * cell instead of a group.
		 */
		protected CellView findViewForPoint(Point pt) {
			int snap = graph.getTolerance();
			Rectangle r =
				new Rectangle(pt.x - snap, pt.y - snap, 2 * snap, 2 * snap);
			for (int i = 0; i < views.length; i++)
				if (views[i].intersects(graph.getGraphics(), r))
					return views[i];
			return null;
		}

		protected void startDragging(MouseEvent event) {
			isDragging = true;
			if (graph.isDragEnabled()) {
				int action =
					(event.isControlDown() && graph.isCloneable())
						? TransferHandler.COPY
						: TransferHandler.MOVE;
				TransferHandler th = graph.getTransferHandler();
				try {
					th.exportAsDrag(graph, event, action);
				} catch (Exception ex) {
					// Ignore
				}
			}
		}

		// Double Buffers by David Larsson
		protected void initOffscreen() {
			try {
				Rectangle rect = graph.getBounds();
				//RepaintManager repMan = RepaintManager.currentManager(getContext().getGraph());
				//offscreen = repMan.getVolatileOffscreenBuffer(getContext().getGraph(), (int) rect.getWidth(), (int) rect.getHeight());
				offscreen =
					new BufferedImage(
						rect.width,
						rect.height,
						BufferedImage.TYPE_INT_RGB);
				offgraphics = offscreen.getGraphics();
				offgraphics.setClip(
					0,
					0,
					(int) rect.getWidth(),
					(int) rect.getHeight());
				offgraphics.setColor(graph.getBackground());
				offgraphics.fillRect(
					0,
					0,
					(int) rect.getWidth(),
					(int) rect.getHeight());
				graph.getUI().paint(offgraphics, graph);
			} catch (Error e) {
				offscreen = null;
				offgraphics = null;
			}
		}

		/** Process mouse dragged event. */
		public void mouseDragged(MouseEvent event) {
			boolean constrained = isConstrainedMoveEvent(event);
			Rectangle dirty = null;
			if (firstDrag
				&& graph.isDoubleBuffered()
				&& cachedBounds == null) {
				initOffscreen();
				firstDrag = false;
			}
			if (event != null && !event.isConsumed()) {
				if (activeHandle != null) // Paint Active Handle
					activeHandle.mouseDragged(event);
				// Invoke Mouse Dragged
				else if (start != null) { // Move Cells
					Graphics g =
						(offgraphics != null)
							? offgraphics
							: graph.getGraphics();
					Point point = new Point(event.getPoint());
					point.translate(-_mouseToViewDelta_x, -_mouseToViewDelta_y);
					Point snapCurrent = graph.snap(point);
					Point current = snapCurrent;
					int thresh = graph.getMinimumMove();
					int dx = current.x - start.x;
					int dy = current.y - start.y;
					if (isMoving
						|| Math.abs(dx) > thresh
						|| Math.abs(dy) > thresh) {
						boolean overlayed = false;
						isMoving = true;
						if (disconnect == null && graph.isDisconnectOnMove())
							disconnect =
								context.disconnect(
									graphLayoutCache.getAllDescendants(views));
						// Constrained movement
						if (constrained && cachedBounds == null) {
							int totDx = current.x - start.x;
							int totDy = current.y - start.y;
							if (Math.abs(totDx) < Math.abs(totDy)) {
								dx = 0;
								dy = totDy;
							} else {
								dx = totDx;
								dy = 0;
							}
						} else {
							dx = current.x - last.x;
							dy = current.y - last.y;
						}
						double scale = graph.getScale();
						dx = (int) (dx / scale);
						//we don't want to round. The best thing is to get just the integer part.
						//That way, the view won't "run away" from the mouse. It may lag behind
						//a mouse pointer occasionally, but will be catching up.
						dy = (int) (dy / scale);

						g.setColor(graph.getForeground());

						// use 'darker' to force XOR to distinguish between existing background elements during drag
						// http://sourceforge.net/tracker/index.php?func=detail&aid=677743&group_id=43118&atid=435210
						g.setXORMode(graph.getBackground().darker());

						// Start Drag and Drop
						if (graph.isDragEnabled() && !isDragging)
							startDragging(event);
						if (dx != 0 || dy != 0) {
							if (!snapLast.equals(snapStart)
								&& (offscreen != null || !blockPaint)) {
								overlay(g);
								overlayed = true;
							}
							isContextVisible =
								(!event.isControlDown()
									|| !graph.isCloneable())
									&& contextViews != null
									&& (contextViews.length < MAXCELLS);
							blockPaint = false;
							if (offscreen != null) {
								dirty =
									graph.toScreen(
										AbstractCellView.getBounds(views));
								Rectangle t =
									graph.toScreen(
										AbstractCellView.getBounds(
											contextViews));
								if (t != null)
									dirty.add(t);
							}
							if (constrained && cachedBounds == null) {
								//Reset Initial Positions
								CellView[] all =
									graphLayoutCache.getAllDescendants(views);
								for (int i = 0; i < all.length; i++) {
									CellView orig =
										graphLayoutCache.getMapping(
											all[i].getCell(),
											false);
									Map attr = orig.getAllAttributes();
									all[i].setAttributes(
										GraphConstants.cloneMap(attr));
									all[i].refresh(false);
								}
							}
							if (cachedBounds != null)
								cachedBounds.translate(
									(int) (dx * scale),
									(int) (dy * scale));
							else {
								// Translate
								GraphLayoutCache.translateViews(views, dx, dy);
								if (views != null)
									graphLayoutCache.update(views);
								if (contextViews != null)
									graphLayoutCache.update(contextViews);
							}
							if (!snapCurrent.equals(snapStart)) {
								overlay(g);
								overlayed = true;
							}
							if (constrained)
								last = new Point(start);
							last.translate(
								(int) (dx * scale),
								(int) (dy * scale));
							// It is better to translate <code>last<code> by a scaled dx/dy
							// instead of making it to be the <code>current<code> (as in prev version),
							// so that the view would be catching up with a mouse pointer
							snapLast = snapCurrent;
							if (overlayed && offscreen != null) {
								dirty.add(
									graph.toScreen(
										AbstractCellView.getBounds(views)));
								Rectangle t =
									graph.toScreen(
										AbstractCellView.getBounds(
											contextViews));
								if (t != null)
									dirty.add(t);
								dirty.grow(2, 2);
								int sx1 =
									(GraphConstants.NEGATIVE_ALLOWED)
										? dirty.x
										: Math.max(0, dirty.x);
								int sy1 =
									(GraphConstants.NEGATIVE_ALLOWED)
										? dirty.y
										: Math.max(0, dirty.y);
								int sx2 = sx1 + dirty.width;
								int sy2 = sy1 + dirty.height;
								if (isDragging && !DNDPREVIEW) // JDK BUG!
									return;
								graph.getGraphics().drawImage(
									offscreen,
									sx1,
									sy1,
									sx2,
									sy2,
									sx1,
									sy1,
									sx2,
									sy2,
									graph);
							}
						}
					} // end if (isMoving or ...)
				} // end if (start != null)
			} else if (event == null)
				graph.repaint();
		}

		public void mouseReleased(MouseEvent event) {
			try {
				if (event != null && !event.isConsumed()) {
					if (activeHandle != null) {
						activeHandle.mouseReleased(event);
						activeHandle = null;
					} else if (isMoving && !event.getPoint().equals(start)) {
						if (cachedBounds != null) {
							int dx = event.getX() - start.x;
							int dy = event.getY() - start.y;
							Point tmp = graph.fromScreen(new Point(dx, dy));
							GraphLayoutCache.translateViews(
								views,
								tmp.x,
								tmp.y);
						}
						CellView[] all =
							graphLayoutCache.getAllDescendants(views);
						if (event.isControlDown()
							&& graph.isCloneable()) { // Clone Cells
							Object[] cells = context.getCells();
							cells =
								graphLayoutCache.order(
									graph.getDescendantList(cells));
							ConnectionSet cs =
								ConnectionSet.create(graphModel, cells, false);
							ParentMap pm =
								ParentMap.create(
									graphModel,
									cells,
									false,
									true);
							Map attributes =
								GraphConstants.createAttributes(all, null);
							insertCells(cells, attributes, cs, pm, true, 0, 0);
						} else if (graph.isMoveable()) { // Move Cells
							// Execute on view
							Map attributes =
								GraphConstants.createAttributes(all, null);
							graph.getGraphLayoutCache().edit(
								attributes,
								disconnect,
								null,
								null);
						}
						event.consume();
					}
				}
			} catch (Exception e) {
				// ignore
			} finally {
				isDragging = false;
				disconnect = null;
				offscreen = null;
				firstDrag = true;
				start = null;
			}
		}

	}

	/**
	 * PropertyChangeListener for the graph. Updates the appropriate
	 * variable and takes the appropriate actions, based on what changes.
	 */
	public class PropertyChangeHandler
		implements PropertyChangeListener, Serializable {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getSource() == graph) {
				String changeName = event.getPropertyName();
				if (changeName.equals("minimumSize"))
					updateCachedPreferredSize();
				else if (changeName.equals(JGraph.GRAPH_MODEL_PROPERTY))
					setModel((GraphModel) event.getNewValue());
				else if (
					changeName.equals(JGraph.GRAPH_LAYOUT_CACHE_PROPERTY)) {
					setGraphLayoutCache((GraphLayoutCache) event.getNewValue());
					graph.repaint();
				} else if (changeName.equals(JGraph.MARQUEE_HANDLER_PROPERTY))
					setMarquee((BasicMarqueeHandler) event.getNewValue());
				else if (changeName.equals(JGraph.EDITABLE_PROPERTY)) {
					boolean editable =
						((Boolean) event.getNewValue()).booleanValue();
					if (!editable && isEditing(graph))
						cancelEditing(graph);
				} else if (changeName.equals(JGraph.SELECTION_MODEL_PROPERTY))
					setSelectionModel(graph.getSelectionModel());
				else if (
					changeName.equals(JGraph.GRID_VISIBLE_PROPERTY)
						|| changeName.equals(JGraph.GRID_SIZE_PROPERTY)
						|| changeName.equals(JGraph.PORTS_VISIBLE_PROPERTY)
						|| changeName.equals(JGraph.ANTIALIASED_PROPERTY))
					graph.repaint();
				else if (changeName.equals(JGraph.SCALE_PROPERTY))
					updateSize();
				else if (changeName.equals("font")) {
					completeEditing();
					updateSize();
				} else if (changeName.equals("componentOrientation")) {
					if (graph != null)
						graph.graphDidChange();
				}
			}
		}
	} // End of BasicGraphUI.PropertyChangeHandler

	/** GraphIncrementAction is used to handle up/down actions.
	  */
	public class GraphIncrementAction extends AbstractAction {
		/** Specifies the direction to adjust the selection by. */
		protected int direction;

		private GraphIncrementAction(int direction, String name) {
			this.direction = direction;
		}

		public void actionPerformed(ActionEvent e) {
			if (graph != null) {
				int step = 70;
				Rectangle rect = graph.getVisibleRect();
				if (direction == 1)
					rect.translate(0, -step); // up
				else if (direction == 2)
					rect.translate(step, 0); // right
				else if (direction == 3)
					rect.translate(0, step); // down
				else if (direction == 4)
					rect.translate(-step, 0); // left
				graph.scrollRectToVisible(rect);
			}
		}

		public boolean isEnabled() {
			return (graph != null && graph.isEnabled());
		}

	} // End of class BasicGraphUI.GraphIncrementAction

	/**
	 * ActionListener that invokes cancelEditing when action performed.
	 */
	private class GraphCancelEditingAction extends AbstractAction {
		public GraphCancelEditingAction(String name) {
		}

		public void actionPerformed(ActionEvent e) {
			if (graph != null)
				cancelEditing(graph);
		}

		public boolean isEnabled() {
			return (graph != null && graph.isEnabled());
		}
	} // End of class BasicGraphUI.GraphCancelEditingAction

	/**
	 * ActionListener invoked to start editing on the focused cell.
	 */
	private class GraphEditAction extends AbstractAction {
		public GraphEditAction(String name) {
		}

		public void actionPerformed(ActionEvent ae) {
			if (isEnabled()) {
				if (getFocusedCell() instanceof GraphCell) {
					graph.startEditingAtCell(getFocusedCell());
				}
			}
		}

		public boolean isEnabled() {
			return (graph != null && graph.isEnabled());
		}
	} // End of BasicGraphUI.GraphEditAction

	/**
	 * Action to select everything in the graph.
	 */
	private class GraphSelectAllAction extends AbstractAction {
		private boolean selectAll;

		public GraphSelectAllAction(String name, boolean selectAll) {
			this.selectAll = selectAll;
		}

		public void actionPerformed(ActionEvent ae) {
			if (graph != null) {
				if (selectAll) {
					Object[] all = new Object[graphModel.getRootCount()];
					for (int i = 0; i < all.length; i++)
						all[i] = graphModel.getRootAt(i);
					graph.addSelectionCells(all);
				} else
					graph.clearSelection();
			}
		}

		public boolean isEnabled() {
			return (graph != null && graph.isEnabled());
		}
	} // End of BasicGraphUI.GraphSelectAllAction

	/**
	  * MouseInputHandler handles passing all mouse events,
	  * including mouse motion events, until the mouse is released to
	  * the destination it is constructed with. It is assumed all the
	  * events are currently target at source.
	  */
	public class MouseInputHandler
		extends Object
		implements MouseInputListener {
		/** Source that events are coming from. */
		protected Component source;
		/** Destination that receives all events. */
		protected Component destination;

		public MouseInputHandler(
			Component source,
			Component destination,
			MouseEvent event) {
			this.source = source;
			this.destination = destination;
			this.source.addMouseListener(this);
			this.source.addMouseMotionListener(this);
			/* Dispatch the editing event */
			destination.dispatchEvent(
				SwingUtilities.convertMouseEvent(source, event, destination));
		}

		public void mouseClicked(MouseEvent e) {
			if (destination != null)
				destination.dispatchEvent(
					SwingUtilities.convertMouseEvent(source, e, destination));
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
			if (destination != null)
				destination.dispatchEvent(
					SwingUtilities.convertMouseEvent(source, e, destination));
			removeFromSource();
		}

		public void mouseEntered(MouseEvent e) {
			if (!SwingUtilities.isLeftMouseButton(e)) {
				removeFromSource();
			}
		}

		public void mouseExited(MouseEvent e) {
			if (!SwingUtilities.isLeftMouseButton(e)) {
				removeFromSource();
			}
			//insertionLocation = null;
		}

		public void mouseDragged(MouseEvent e) {
			if (destination != null)
				destination.dispatchEvent(
					SwingUtilities.convertMouseEvent(source, e, destination));
		}

		public void mouseMoved(MouseEvent e) {
			removeFromSource();
		}

		protected void removeFromSource() {
			if (source != null) {
				source.removeMouseListener(this);
				source.removeMouseMotionListener(this);
			}
			source = destination = null;
		}

	} // End of class BasicGraphUI.MouseInputHandler

	/**
	 * Graph Drop Target Listener
	 */
	public class GraphDropTargetListener
		extends BasicGraphDropTargetListener
		implements Serializable {

		/**
		 * called to save the state of a component in case it needs to
		 * be restored because a drop is not performed.
		 */
		protected void saveComponentState(JComponent comp) {
		}

		/**
		 * called to restore the state of a component
		 * because a drop was not performed.
		 */
		protected void restoreComponentState(JComponent comp) {
			if (handle != null)
				handle.mouseDragged(null);
		}

		/**
		 * called to set the insertion location to match the current
		 * mouse pointer coordinates.
		 */
		protected void updateInsertionLocation(JComponent comp, Point p) {
			setInsertionLocation(p);
			if (handle != null) {
				// How to fetch the shift state?
				int mod =
					(dropAction == TransferHandler.COPY)
						? InputEvent.CTRL_MASK
						: 0;
				handle.mouseDragged(
					new MouseEvent(comp, 0, 0, mod, p.x, p.y, 1, false));
			}
		}

		public void dragEnter(DropTargetDragEvent e) {
			dropAction = e.getDropAction();
			super.dragEnter(e);
		}

		public void dropActionChanged(DropTargetDragEvent e) {
			dropAction = e.getDropAction();
			super.dropActionChanged(e);
		}

	} // End of BasicGraphUI.GraphDropTargetListener

	public class GraphTransferHandler extends TransferHandler {

		/* Pointer to the last inserted array of cells. */
		protected Object out, in;

		/* How many times the last transferable was inserted. */
		protected int inCount = 0;

		public boolean canImport(JComponent comp, DataFlavor[] flavors) {
			return true;
		}

		/* Public entry point to create a Transferable. */
		public Transferable createTransferable() {
			return createTransferable(graph);
		}

		protected final Transferable createTransferable(JComponent c) {
			if (c instanceof JGraph) {
				JGraph graph = (JGraph) c;
				Object[] cells =
					graphLayoutCache.order(graph.getSelectionCells());
				if (cells != null && cells.length > 0) {
					Object[] flat =
						graphLayoutCache.order(graph.getDescendantList(cells));
					ParentMap pm =
						ParentMap.create(graphModel, flat, false, true);
					ConnectionSet cs =
						ConnectionSet.create(graphModel, flat, false);
					Map viewAttributes =
						GraphConstants.createAttributes(flat, graphLayoutCache);
					Rectangle bounds = graph.getCellBounds(cells);
					out = flat;
					return create(graph, flat, viewAttributes, bounds, cs, pm);
				}
			}
			return null;
		}

		protected GraphTransferable create(
			JGraph graph,
			Object[] cells,
			Map viewAttributes,
			Rectangle bounds,
			ConnectionSet cs,
			ParentMap pm) {
			return new GraphTransferable(cells, viewAttributes, bounds, cs, pm);
		}

		protected void exportDone(
			JComponent comp,
			Transferable data,
			int action) {
			if (comp instanceof JGraph && data instanceof GraphTransferable) {
				if (action == TransferHandler.MOVE) {
					JGraph graph = (JGraph) comp;
					Object[] cells = ((GraphTransferable) data).getCells();
					graph.getGraphLayoutCache().remove(cells);
				}
			}
			updateHandle();
			setInsertionLocation(null);
		}

		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE;
		}

		// NOTE: 1. We abuse return value to signal removal to the sender.
		//       2. We always clone cells when transferred between two models
		//          This is because they contain parts of the model's data.
		//       3. Transfer is passed to importDataImpl for unsupported
		//          dataflavors (becaue method may return false, see 1.)
		public boolean importData(JComponent comp, Transferable t) {
			try {
				if (comp instanceof JGraph) {
					JGraph graph = (JGraph) comp;
					if (t
						.isDataFlavorSupported(GraphTransferable.dataFlavor)) {
						// May be null
						Point p = getInsertionLocation();

						// Get Local Machine Flavor
						Object obj =
							t.getTransferData(GraphTransferable.dataFlavor);
						GraphTransferable gt = (GraphTransferable) obj;

						// Get Transferred Cells
						Object[] cells = gt.getCells();

						// Check if all cells are in the model
						boolean allInModel = true;
						for (int i = 0; i < cells.length && allInModel; i++)
							allInModel =
								allInModel
									&& graph.getModel().contains(cells[i]);

						// Count repetitive inserts
						if (in == cells)
							inCount++;
						else
							inCount = (allInModel) ? 1 : 0;
						in = cells;

						// Delegate to handle
						if (p != null && in == out && handle != null) {
							int mod =
								(dropAction == TransferHandler.COPY)
									? InputEvent.CTRL_MASK
									: 0;
							handle.mouseReleased(
								new MouseEvent(
									comp,
									0,
									0,
									mod,
									p.x,
									p.y,
									1,
									false));
							return false;
						}

						// Get more Transfer Data
						Rectangle bounds = gt.getBounds();
						Map nested = gt.getAttributeMap();
						ConnectionSet cs = gt.getConnectionSet();
						ParentMap pm = gt.getParentMap();

						// Move across models or via clipboard always clones
						if (!allInModel
							|| p == null
							|| dropAction == TransferHandler.COPY) {

							// Create map of (cell, clone) pairs
							Map clones = graph.cloneCells(cells);

							// Replace keys in transfer data
							nested = GraphConstants.replaceKeys(clones, nested);
							cs = cs.clone(clones);
							pm = pm.clone(clones);

							// Translate cells
							int dx = 0;
							int dy = 0;

							// Cloned via Drag and Drop
							if (p != null) {
								Point insert =
									graph.fromScreen(graph.snap(new Point(p)));
								dx = insert.x - bounds.x;
								dy = insert.y - bounds.y;

								// Cloned via Clipboard
							} else {
								dx = inCount * graph.getGridSize();
								dy = inCount * graph.getGridSize();
							}
							GraphConstants.translate(nested.values(), dx, dy);

							// Replace cells in order
							Object[] newCells = new Object[cells.length];
							for (int i = 0; i < cells.length; i++)
								newCells[i] = clones.get(cells[i]);

							// Insert and exit
							graph.getGraphLayoutCache().insert(
								newCells,
								nested,
								cs,
								pm,
								null);

							// Signal sender to remove only if moved between different models
							return (
								dropAction == TransferHandler.MOVE
									&& !allInModel);

							// We are dealing with a move across multiple views of the same model
						} else {

							// Moved via Drag and Drop
							if (p != null) {
								// Scale insertion location
								Point insert =
									graph.fromScreen(graph.snap(new Point(p)));

								// Compute translation vector
								int dx = insert.x - bounds.x;
								int dy = insert.y - bounds.y;

								// Translate attributes
								GraphConstants.translate(
									nested.values(),
									dx,
									dy);

								// Edit cells
								graph.getGraphLayoutCache().edit(
									nested,
									null,
									null,
									null);
							}

							// Select cells
							graph.setSelectionCells(cells);

							// Don't remove at sender
							return false;
						}
					} else
						return importDataImpl(comp, t);
				}
			} catch (Exception exception) {
				//System.err.println("Cannot import: "+exception.getMessage());
			}
			return false;
		}

		// For subclassers if above does not handle the insertion
		protected boolean importDataImpl(JComponent comp, Transferable t) {
			return false;
		}

	}

	/**
	 * Insert the specified cells into the model. If clone is true,
	 * the cells are cloned an all datastructures are updated to
	 * reference these clones. Clones are created using the
	 * GraphModel.clone(Object)-method.
	 * If cells are cloned, they are translated using the given amount.
	 */
	public void insertCells(
		Object[] cells,
		Map viewAttributeMap,
		ConnectionSet cs,
		ParentMap pm,
		boolean clone,
		int dx,
		int dy) {
		if (clone) {
			Map cellMap = graph.cloneCells(cells);
			viewAttributeMap =
				GraphConstants.replaceKeys(cellMap, viewAttributeMap);
			GraphConstants.translate(viewAttributeMap.values(), dx, dy);
			cs = cs.clone(cellMap);
			pm = pm.clone(cellMap);
			for (int i = 0; i < cells.length; i++)
				cells[i] = cellMap.get(cells[i]);
		}
		graphLayoutCache.insert(cells, viewAttributeMap, cs, pm, null);
	}

	/**
	 * @return true if snapSelectedView mode is enabled during the drag operation. If it is enabled,  the view, that is returned by the findViewForPoint(Point pt),
	 * will be snapped to the grid lines. <br>
	 * By default, findViewForPoint() returns the first view from the GraphContext whose bounds intersect with snap proximity of a mouse pointer.
	 * If snap-to-grid mode is disabled, views are moved by a snap increment.
	 */
	public boolean isSnapSelectedView() {
		return snapSelectedView;
	}

	/**
	 * Sets the mode of the snapSelectedView drag operation.
	 * @param bSnapToGrid specifies if the snap-to-grid mode should be applied during a drag operation.
	 * If it is enabled,  the view, that is returned by the findViewForPoint(Point pt),
	 * will be snapped to the grid lines. <br>
	 * By default, findViewForPoint() returns the first view from the GraphContext whose bounds intersect with snap proximity of a mouse pointer.
	 * If snap-to-grid mode is disabled, views are moved by a snap increment.
	 */
	public void setSnapSelectedView(boolean snapSelectedView) {
		this.snapSelectedView = snapSelectedView;
	}

} // End of class BasicGraphUI
