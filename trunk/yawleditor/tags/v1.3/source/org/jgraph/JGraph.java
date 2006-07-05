/*
 * @(#)JGraph.java	1.0 1/1/02
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

package org.jgraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.accessibility.Accessible;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultGraphSelectionModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.Edge;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphSelectionModel;
import org.jgraph.graph.Port;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;
import org.jgraph.plaf.GraphUI;

/**
 * A control that displays a network of related objects using the
 * well-known paradigm of a graph.
 * <p>
 * A JGraph object doesn't actually contain your data; it simply provides
 * a view of the data. Like any non-trivial Swing component, the graph gets
 * data by querying its data model.
 * <p>
 * JGraph displays its data by drawing individual elements. Each element
 * displayed by the graph contains exactly one item of data, which is
 * called a cell. A cell may either be a vertex or an edge. Vertices may
 * have neighbours or not, and edges may have source and target vertices
 * or not, depending on whether they are connected.
 * <p>
 * <strong>Creating a Graph</strong>
 * <p>
 * The following code creates a JGraph object:<p>
 * JGraph graph = new JGraph();<br>
 * ...<br>
 * JScrollPane graphLayoutCache = new JScrollPane(graph)
 * <p>
 * The code creates an instance of JGraph and puts it in a scroll pane.
 * JGraphs constructor is called with no arguments in this example, which
 * causes the constructor to create a sample model.
 * <p>
 * <strong>Editing</strong>
 * <p>
 * JGraph supports in-place editing of text and shapes. These features can
 * be disabled using the setEnabled() method, which blocks all features, or
 * individually, using the following methods:
 * <p>
 * setEditable() controls in-place editing of cells. Moving, cloning, sizing,
 * and bending, connection and disconnection of edges may also be disabled
 * using the respective methods, namemly setMoveable(), setCloneable(),
 * setSizeable(), setBendable(), setConnectable() and setDisconnectable().
 * <p>
 * The model offers fainer control of connection establishment based
 * on the passed-in edge and port. The individual cells offer yet
 * another level of control in that they may allow/disallow being edited,
 * moved, cloned, resized, and shaped, or connected/disconnected to or
 * from other cells.
 * <p>
 * <strong>Keyboard Bindings</strong>
 * <p>
 * JGraph defines the following set of keyboard bindings:
 * <p><ul>
 * <li>Alt-Click forces marquee selection if over a cell.
 * <li>Shift- or Ctrl-Select extends or toggles the selection.
 * <li>Shift-Drag constrains the offset to one direction.
 * <li>Ctrl-Drag clones the selection.
 * <li>Doubleclick/F2 starts editing a cell.
 * </ul>
 * You can change the number of clicks that triggers editing using
 * setEditClickCount().
 * <p>
 * <strong>Customization</strong>
 * <p>
 * There are a number of additional methods that customize JGraph.
 * For example, setMinimumMove() defines the minimum amount of
 * pixels before a move operation is initiated. setSnapSize() defines
 * the maximum distance for a cell to be selected. setFloatEnabled()
 * enables/disables port floating.
 * <p>
 * With setDisconnectOnMove() you can indicate if the selected subgraph
 * should be disconnected from the unselected rest when a move operation
 * is initiated. setDragEnabled() enables/disables the use of Drag And
 * Drop, and setDropEnabled() sets if the graph accepts Drops from
 * external sources.
 * <p>
 * <strong>Customizing a graphs display</strong>
 * <p>
 * JGraph performs some look-and-feel specific painting. You can
 * customize this painting in a limited way. For example, you can modify the
 * grid using setGridColor() and setGridSize(), and you can change the handle
 * colors using setHandleColor() and setLockedHandleColor().
 * <p>
 * If you want finer control over the rendering, you can subclass one of the
 * default renderers, and extend its paint()-method. A renderer is a
 * Component-extension that paints a cell based on its attributes. Thus,
 * neither the JGraph nor its look-and-feel-specific implementation actually
 * contain the code that paints the cell. Instead, the graph uses the cell
 * renderers painting code.
 * <p>
 * <strong>Selection</strong>
 * <p>
 * Apart from the single-cell and marquee-selection, JGraphs selection
 * model also allows to "step-into" groups, and select children. This
 * feature can be disabled using the setAllowsChildSelection() method
 * of the selection model instance.
 * <p>
 * If you are interested in knowing when the selection changes implement
 * the <code>GraphSelectionListener</code> interface and add the instance
 * using the method <code>addGraphSelectionListener</code>.
 * <code>valueChanged</code> will be invoked when the
 * selection changes, that is if the user clicks twice on the same
 * vertex <code>valueChanged</code> will only be invoked once.
 * <p>
 * <strong>Change Notification</strong>
 * <p>
 * If you are interested in handling modifications, implement
 * the <code>GraphEventHandler</code> interface and add the instance
 * using the method <code>addGraphEventHandler</code>.
 * <p>
 * For detection of double-clicks or when a user clicks on a cell,
 * regardless of whether or not it was selected, I recommend you
 * implement a MouseListener and use <code>getFirstCellForLocation</code>.
 * <p>
 * <strong>Undo Support</strong>
 * <p>
 * To enable Undo-Support, a <code>GraphUndoManager</code> must be added
 * using <code>addGraphSelectionListener</code>. The GraphUndoManager
 * is an extension of Swing's <code>GraphUndoManager</code> that maintains
 * a command history in the context of multiple views. In this setup, a
 * cell may have a set of attributes in each view attached to the model.
 * <p>
 * For example, consider a position that is stored separately in each view.
 * If a node is inserted, the change will be visible in all attached views,
 * resulting in a new node that pops-up at the initial position.
 * If the node is subsequently moved, say, in view1, this does not constitute
 * a change in view2. If view2 does an "undo", the move <i>and</i> the
 * insertion must be undone, whereas an "undo" in view1 will only undo
 * the previous move operation.
 * <p>
 * Like all <code>JComponent</code> classes, you can use {@link javax.swing.InputMap} and
 * {@link javax.swing.ActionMap} to associate an {@link javax.swing.Action} object with a
 * {@link javax.swing.KeyStroke} and execute the action under specified conditions.
 *
 * @author Gaudenz Alder
 * @version 2.1 16/03/03
 *
 */

public class JGraph
// DO NOT REMOVE OR MODIFY THIS LINE!
extends JComponent // JAVA13: org.jgraph.plaf.basic.TransferHandler.JAdapterComponent
implements CellViewFactory, Scrollable, Accessible, Serializable {

	public static final String VERSION = "JGraph (v3.1)";

	public static final int DOT_GRID_MODE = 0;
	public static final int CROSS_GRID_MODE = 1;
	public static final int LINE_GRID_MODE = 2;

	/**
	 * @see #getUIClassID
	 * @see #readObject
	 */
	private static final String uiClassID = "GraphUI";

	/** Creates a new event and passes it off the <code>selectionListeners</code>. */
	protected transient GraphSelectionRedirector selectionRedirector;

	//
	// Bound Properties
	//

	/** The model that defines the graph displayed by this object. Bound property. */
	transient protected GraphModel graphModel;

	/** The view that defines the display properties of the model. Bound property. */
	transient protected GraphLayoutCache graphLayoutCache;

	/** Handler for marquee selection. */
	transient protected BasicMarqueeHandler marquee;

	/** Models the set of selected objects in this graph. Bound property. */
	transient protected GraphSelectionModel selectionModel;

	/** Scale of the graph. Default is 1. Bound property. */
	protected double scale = 1.0;

	/** True if the graph is anti-aliased. Default is false. Bound property. */
	protected boolean antiAliased = false;

	/** True if the graph allows editing the value of a cell. Bound property. */
	protected boolean editable = true;

	/** True if the grid is visible. Bound property. */
	protected boolean gridVisible = false;

	/** The size of the grid in points.  Default is 10. Bound property.*/
	protected int gridSize = 10;

	/** The style of the grid. Use one of the _GRID_MODE constants. */
	protected int gridMode = DOT_GRID_MODE;

	/** True if the ports are visible. Bound property. */
	protected boolean portsVisible = false;

	//
	// Look-And-Feel dependent
	//

	/** Highlight Color. Changes when the Look-and-Feel changes. */
	protected Color highlightColor = Color.green;

	/** Color of the handles and locked handles.  Changes when the Look-and-Feel changes. */
	protected Color handleColor, lockedHandleColor;

	/** Color of the marquee. Changes when the Look-and-Feel changes. */
	protected Color marqueeColor;

	/** The color of the grid. Changes when the Look-and-Feel changes. */
	protected Color gridColor;

	//
	// Datatransfer
	//

	/**
	 * True if Drag-and-Drop should be used for move operations. Default is
	 * false due to a JDK bug.
	 */
	protected boolean dragEnabled = false;

	/**
	 * True if the graph accepts transfers from other components (graphs).
	 * This also affects the clipboard. Default is true.
	 */
	protected boolean dropEnabled = true;

	//
	// Unbound Properties
	//

	/** Number of clicks for editing to start. Default is 2 clicks.*/
	protected int editClickCount = 2;

	/** True if the graph allows interactions. Default is true. */
	protected boolean enabled = true;

	/** True if the snap method should be active (snap to grid). */
	protected boolean gridEnabled = false;

	/** Size of a handle. Default is 3 pixels.*/
	protected int handleSize = 3;

	/** Maximum distance between a cell and the mousepointer. Default is 4.*/
	protected int tolerance = 4;

	/** Minimum amount of pixels to start a move transaction. Default is 5. */
	protected int minimumMove = 5;

	/** True if inserted cells should be selected. Default is false. */
	protected boolean selectNewCells = false;

	/**
	 * True if selected edges are disconnected from unselected vertices on move.
	 * Default is true.
	 */
	protected boolean disconnectOnMove = false;

	/** True if the graph allows move operations. Default is true. */
	protected boolean moveable = true;

	/** True if the graph allows "ctrl-drag" operations. Default is true. */
	protected boolean cloneable = true;

	/** True if the graph allows cells to be resized. Default is true. */
	protected boolean sizeable = true;

	/** True if the graph allows points to be midified/added/removed. Default is true. */
	protected boolean bendable = true;

	/** True if the graph allows new connections to be established. Default is true. */
	protected boolean connectable = true;

	/** True if the graph allows existing connections to be removed. Default is true.  */
	protected boolean disconnectable = true;

	/**
	 * If true, when editing is to be stopped by way of selection changing,
	 * data in graph changing or other means <code>stopCellEditing</code>
	 * is invoked, and changes are saved. If false,
	 * <code>cancelCellEditing</code> is invoked, and changes
	 * are discarded. Default is false.
	 */
	protected boolean invokesStopCellEditing;

	/**
	 * This is set to true for the life of the setUI call.
	 */
	private boolean settingUI;

	//
	// Bound propery names
	//
	/**
	 * Bound property name for <code>graphModel</code>.
	 */
	public final static String GRAPH_MODEL_PROPERTY = "model";
	/**
	 * Bound property name for <code>graphModel</code>.
	 */
	public final static String GRAPH_LAYOUT_CACHE_PROPERTY = "view";
	/**
	 * Bound property name for <code>graphModel</code>.
	 */
	public final static String MARQUEE_HANDLER_PROPERTY = "marquee";
	/**
	 * Bound property name for <code>editable</code>.
	 */
	public final static String EDITABLE_PROPERTY = "editable";
	/**
	 * Bound property name for <code>scale</code>.
	 */
	public final static String SCALE_PROPERTY = "scale";
	/**
	 * Bound property name for <code>antiAliased</code>.
	 */
	public final static String ANTIALIASED_PROPERTY = "antiAliased";
	/**
	 * Bound property name for <code>gridSize</code>.
	 */
	public final static String GRID_SIZE_PROPERTY = "gridSize";
	/**
	 * Bound property name for <code>gridVisible</code>.
	 */
	public final static String GRID_VISIBLE_PROPERTY = "gridVisible";
	/**
	 * Bound property name for <code>gridVisible</code>.
	 */
	public final static String PORTS_VISIBLE_PROPERTY = "portsVisible";
	/**
	 * Bound property name for <code>selectionModel</code>.
	 */
	public final static String SELECTION_MODEL_PROPERTY = "selectionModel";
	/**
	 * Bound property name for <code>messagesStopCellEditing</code>.
	 */
	public final static String INVOKES_STOP_CELL_EDITING_PROPERTY =
		"invokesStopCellEditing";

	/**
	 * Creates and returns a default <code>GraphLayoutCache</code>.
	 *
	 * @return the default <code>GraphLayoutCache</code>
	 */
	protected static GraphLayoutCache createDefaultGraphView(JGraph graph) {
		return new GraphLayoutCache(graph.getModel(), graph);
	}

	/**
	 * Creates and returns a sample <code>GraphModel</code>.
	 * Used primarily for beanbuilders to show something interesting.
	 *
	 * @return the default <code>GraphModel</code>
	 */
	public static void addSampleData(GraphModel model) {
		ConnectionSet cs = new ConnectionSet();
		Map attributes = new Hashtable();

		// Styles For Implement/Extend/Aggregation
		Map implementStyle = GraphConstants.createMap();
		GraphConstants.setLineBegin(
			implementStyle,
			GraphConstants.ARROW_TECHNICAL);
		GraphConstants.setBeginSize(implementStyle, 10);
		GraphConstants.setDashPattern(implementStyle, new float[] { 3, 3 });
		GraphConstants.setFont(implementStyle, GraphConstants.defaultFont.deriveFont(10));

		Map extendStyle = GraphConstants.createMap();
		GraphConstants.setLineBegin(
			extendStyle,
			GraphConstants.ARROW_TECHNICAL);
		GraphConstants.setBeginFill(extendStyle, true);
		GraphConstants.setBeginSize(extendStyle, 10);
		GraphConstants.setFont(extendStyle, GraphConstants.defaultFont.deriveFont(10));

		Map aggregateStyle = GraphConstants.createMap();
		GraphConstants.setLineBegin(
			aggregateStyle,
			GraphConstants.ARROW_DIAMOND);
		GraphConstants.setBeginFill(aggregateStyle, true);
		GraphConstants.setBeginSize(aggregateStyle, 6);
		GraphConstants.setLineEnd(aggregateStyle, GraphConstants.ARROW_SIMPLE);
		GraphConstants.setEndSize(aggregateStyle, 8);
		GraphConstants.setLabelPosition(aggregateStyle, new Point(500, 1200));
		GraphConstants.setFont(aggregateStyle, GraphConstants.defaultFont.deriveFont(10));

		//
		// The Swing MVC Pattern
		//

		// Model Column
		DefaultGraphCell gm = new DefaultGraphCell("GraphModel");
		attributes.put(gm, createBounds(20, 100, Color.blue));
		gm.add(new DefaultPort("GraphModel/Center"));

		DefaultGraphCell dgm = new DefaultGraphCell("DefaultGraphModel");
		attributes.put(dgm, createBounds(20, 180, Color.blue));
		dgm.add(new DefaultPort("DefaultGraphModel/Center"));

		DefaultEdge dgmImplementsGm = new DefaultEdge("implements");
		cs.connect(dgmImplementsGm, gm.getChildAt(0), dgm.getChildAt(0));
		attributes.put(dgmImplementsGm, implementStyle);

		DefaultGraphCell modelGroup = new DefaultGraphCell("ModelGroup");
		modelGroup.add(gm);
		modelGroup.add(dgm);
		modelGroup.add(dgmImplementsGm);

		// JComponent Column
		DefaultGraphCell jc = new DefaultGraphCell("JComponent");
		attributes.put(jc, createBounds(150, 20, Color.green));
		jc.add(new DefaultPort("JComponent/Center"));

		DefaultGraphCell jg = new DefaultGraphCell("JGraph");
		attributes.put(jg, createBounds(150, 100, Color.green));
		jg.add(new DefaultPort("JGraph/Center"));

		DefaultEdge jgExtendsJc = new DefaultEdge("extends");
		cs.connect(jgExtendsJc, jc.getChildAt(0), jg.getChildAt(0));
		attributes.put(jgExtendsJc, extendStyle);

		// UI Column
		DefaultGraphCell cu = new DefaultGraphCell("ComponentUI");
		attributes.put(cu, createBounds(280, 20, Color.red));
		cu.add(new DefaultPort("ComponentUI/Center"));

		DefaultGraphCell gu = new DefaultGraphCell("GraphUI");
		attributes.put(gu, createBounds(280, 100, Color.red));
		gu.add(new DefaultPort("GraphUI/Center"));

		DefaultGraphCell dgu = new DefaultGraphCell("BasicGraphUI");
		attributes.put(dgu, createBounds(280, 180, Color.red));
		dgu.add(new DefaultPort("BasicGraphUI/Center"));

		DefaultEdge guExtendsCu = new DefaultEdge("extends");
		cs.connect(guExtendsCu, cu.getChildAt(0), gu.getChildAt(0));
		attributes.put(guExtendsCu, extendStyle);

		DefaultEdge dguImplementsDu = new DefaultEdge("implements");
		cs.connect(dguImplementsDu, gu.getChildAt(0), dgu.getChildAt(0));
		attributes.put(dguImplementsDu, implementStyle);

		DefaultGraphCell uiGroup = new DefaultGraphCell("UIGroup");
		uiGroup.add(cu);
		uiGroup.add(gu);
		uiGroup.add(dgu);
		uiGroup.add(dguImplementsDu);
		uiGroup.add(guExtendsCu);

		// Aggregations
		DefaultEdge jgAggregatesGm = new DefaultEdge("model");
		cs.connect(jgAggregatesGm, jg.getChildAt(0), gm.getChildAt(0));
		attributes.put(jgAggregatesGm, aggregateStyle);

		DefaultEdge jcAggregatesCu = new DefaultEdge("ui");
		cs.connect(jcAggregatesCu, jc.getChildAt(0), cu.getChildAt(0));
		attributes.put(jcAggregatesCu, aggregateStyle);

		// Insert Cells into model

		Object[] cells =
			new Object[] {
				jgAggregatesGm,
				jcAggregatesCu,
				modelGroup,
				jc,
				jg,
				jgExtendsJc,
				uiGroup };
		model.insert(cells, attributes, cs, null, null);
	}

	/**
	 * Returns an attributeMap for the specified position and color.
	 */
	public static Map createBounds(int x, int y, Color c) {
		Map map = GraphConstants.createMap();
		GraphConstants.setBounds(map, new Rectangle(x, y, 90, 30));
		GraphConstants.setBorder(map, BorderFactory.createRaisedBevelBorder());
		GraphConstants.setBackground(map, c.darker());
		GraphConstants.setForeground(map, Color.white);
		GraphConstants.setFont(map, GraphConstants.defaultFont.deriveFont(Font.BOLD, 12));
		GraphConstants.setOpaque(map, true);
		return map;
	}

	/**
	 * Returns a <code>JGraph</code> with a sample model.
	 */
	public JGraph() {
		this(null);
	}

	/**
	  * Returns an instance of <code>JGraph</code> which displays the
	  * the specified data model.
	  *
	  * @param model  the <code>GraphModel</code> to use as the data model
	  */
	public JGraph(GraphModel model) {
		this(model, (GraphLayoutCache) null);
	}

	/**
	  * Returns an instance of <code>JGraph</code> which displays
	  * the specified data model using the specified view.
	  *
	  * @param model  the <code>GraphModel</code> to use as the data model
	  * @param view  the <code>GraphLayoutCache</code> to use as the view
	  */
	public JGraph(GraphModel model, GraphLayoutCache view) {
		this(model, view, new BasicMarqueeHandler());
	}

	/**
	  * Returns an instance of <code>JGraph</code> which displays
	  * the specified data model using the specified view.
	  *
	  * @param model  the <code>GraphModel</code> to use as the data model
	  * @param view  the <code>GraphLayoutCache</code> to use as the view
	  */
	public JGraph(GraphModel model, BasicMarqueeHandler mh) {
		this(model, null, mh);
	}

	/**
	  * Returns an instance of <code>JGraph</code> which displays
	  * the specified data model using the specified view.
	  *
	  * @param model  the <code>GraphModel</code> to use as the data model
	  * @param view  the <code>GraphLayoutCache</code> to use as the view
	  */
	public JGraph(
		GraphModel model,
		GraphLayoutCache view,
		BasicMarqueeHandler mh) {
		selectionModel = new DefaultGraphSelectionModel(this);
		setLayout(null);
		marquee = mh;
		if (view == null)
			view = createDefaultGraphView(this);
		setGraphLayoutCache(view);
		updateUI();
		if (model == null) {
			model = new DefaultGraphModel();
			setModel(model);
			addSampleData(model);
		} else
			setModel(model);
		setDoubleBuffered(true);
	}

	//
	// UI-delegate (GraphUI)
	//

	/**
	 * Returns the L&F object that renders this component.
	 * @return the GraphUI object that renders this component
	 */
	public GraphUI getUI() {
		return (GraphUI) ui;
	}

	/**
	 * Sets the L&F object that renders this component.
	 * @param ui the GraphUI L&F object
	 * @see javax.swing.UIDefaults#getUI(JComponent)
	 *
	 */
	public void setUI(GraphUI ui) {
		if ((GraphUI) this.ui != ui) {
			settingUI = true;
			try {
				super.setUI(ui);
			} finally {
				settingUI = false;
			}
		}
	}

	/**
	 * Notification from the <code>UIManager</code> that the L&F has changed.
	 * Replaces the current UI object with the latest version from the
	 * <code>UIManager</code>. Subclassers can override this to support
	 * different GraphUIs.
	 * @see JComponent#updateUI
	 *
	 */
	public void updateUI() {
		setUI(new org.jgraph.plaf.basic.BasicGraphUI());
		invalidate();
	}

	/**
	 * Returns the name of the L&F class that renders this component.
	 * @return the string "GraphUI"
	 * @see JComponent#getUIClassID
	 *
	 */
	public String getUIClassID() {
		return uiClassID;
	}

	//
	// Content
	//

	/**
	 * Returns all cells that the model contains.
	 */
	public Object[] getRoots() {
		return DefaultGraphModel.getRoots(graphModel);
	}

	/**
	 * Returns all cells that intersect the given rectangle.
	 */
	public Object[] getRoots(Rectangle clip) {
		CellView[] views = graphLayoutCache.getRoots(clip);
		Object[] cells = new Object[views.length];
		for (int i = 0; i < views.length; i++)
			cells[i] = views[i].getCell();
		return cells;
	}

	/**
	 * Returns all <code>cells</code> including all descendants.
	 * DEPRECATED: Use getDescendantList instead.
	 */
	public Object[] getDescendants(Object[] cells) {
		Set set = DefaultGraphModel.getDescendants(getModel(), cells);
		return set.toArray();
	}

	/**
	 * Returns all <code>cells</code> including all descendants.
	 */
	public Object[] getDescendantList(Object[] cells) {
		return DefaultGraphModel.getDescendantList(getModel(), cells).toArray();
	}

	/**
	 * Returns a map of (cell, clone)-pairs for all <code>cells</code>
	 * and their children. Special care is taken to replace the anchor
	 * references between ports. (Iterative implementation.)
	 */
	public Map cloneCells(Object[] cells) {
		return graphModel.cloneCells(cells);
	}

	/**
	 * Returns the topmost cell at the specified location.
	 * @param x an integer giving the number of pixels horizontally from
	 * the left edge of the display area, minus any left margin
	 * @param y an integer giving the number of pixels vertically from
	 * the top of the display area, minus any top margin
	 * @return the topmost cell at the specified location
	 */
	public Object getFirstCellForLocation(int x, int y) {
		return getNextCellForLocation(null, x, y);
	}

	/**
	 * Returns the cell at the specified location that is "behind" the
	 * <code>current</code> cell. Returns the topmost cell if there are
	 * no more cells behind <code>current</code>.
	 */
	public Object getNextCellForLocation(Object current, int x, int y) {
		x /= scale;
		y /= scale; // FIX: Consistency with other methods?
		CellView cur = graphLayoutCache.getMapping(current, false);
		CellView cell = getNextViewAt(cur, x, y);
		if (cell != null)
			return cell.getCell();
		return null;
	}

	/**
	 * Returns the bounding rectangle of the specified cell.
	 */
	public Rectangle getCellBounds(Object cell) {
		CellView view = graphLayoutCache.getMapping(cell, false);
		if (view != null)
			return view.getBounds();
		return null;
	}

	/**
	 * Returns the bounding rectangle of the specified cells.
	 */
	public Rectangle getCellBounds(Object[] cells) {
		if (cells != null && cells.length > 0) {
			Rectangle ret = getCellBounds(cells[0]);
			if (ret != null) {
				ret = new Rectangle(ret);
				for (int i = 1; i < cells.length; i++) {
					Rectangle r = getCellBounds(cells[i]);
					if (r != null)
					    SwingUtilities.computeUnion(
						r.x,
						r.y,
						r.width,
						r.height,
						ret);
				}
				return ret;
			}
		}
		return null;
	}

	/**
	 * Returns the next view at the specified location wrt. <code>current</code>.
	 * This is used to iterate overlapping cells, and cells that are grouped.
	 * The current selection affects this method.
	 */
	public CellView getNextViewAt(CellView current, int x, int y) {
		Object[] sel =
			graphLayoutCache.order(getSelectionModel().getSelectables());
		CellView[] cells = graphLayoutCache.getMapping(sel);
		CellView cell = getNextViewAt(cells, current, x, y);
		return cell;
	}

	/**
	 * Returns the next view at the specified location wrt. <code>c</code>
	 * in the specified array of views. The views must be in order, as
	 * returned, for example, by GraphLayoutCache.order(Object[]).
	 */
	public CellView getNextViewAt(CellView[] cells, CellView c, int x, int y) {
		if (cells != null) {
			Rectangle r =
				new Rectangle(
					x - tolerance,
					y - tolerance,
					2 * tolerance,
					2 * tolerance);
			// Iterate through cells and switch to active
			// if current is traversed. Cache first cell.
			CellView first = null;
			boolean active = (c == null);
			Graphics g = getGraphics();
			for (int i = cells.length - 1; i >= 0; i--) {
				if (cells[i] != null && cells[i].intersects(g, r)) {
					if (active
						&& !selectionModel.isChildrenSelected(cells[i].getCell()))
						return cells[i];
					else if (first == null)
						first = cells[i];
					active = active | (cells[i] == c);
				}
			}
			return first;
		}
		return null;
	}

	/**
	 * Convenience method to return the port at the specified location.
	 */
	public Object getPortForLocation(int x, int y) {
		PortView view = getPortViewAt(x, y);
		if (view != null)
			return view.getCell();
		return null;
	}

	/**
	 * Returns the portview at the specified location.
	 */
	public PortView getPortViewAt(int x, int y) {
		Rectangle r =
			new Rectangle(
				x - tolerance,
				y - tolerance,
				2 * tolerance,
				2 * tolerance);
		PortView[] ports = graphLayoutCache.getPorts();
		for (int i = ports.length - 1; i >= 0; i--)
			if (ports[i] != null && ports[i].intersects(getGraphics(), r))
				return ports[i];
		return null;
	}

	/**
	 * Converts the specified value to string. If the value is an instance of
	 * CellView or the current GraphLayoutCache returns a mapping for value, then
	 * then value attribute of that CellView is used. (The value is retrieved using
	 * getAllAttributes.) If the value is an instance
	 * of DefaultMutableTreeNode (e.g. DefaultGraphCell), then the userobject
	 * is returned as a String.
	 */
	public String convertValueToString(Object value) {
		CellView view =
			(value instanceof CellView)
				? (CellView) value
				: getGraphLayoutCache().getMapping(value, false);
		if (view != null) {
			Object newValue = GraphConstants.getValue(view.getAllAttributes());
			if (newValue != null)
				value = newValue;
			else
				value = view.getCell();
		}
		if (value instanceof DefaultMutableTreeNode
			&& ((DefaultMutableTreeNode) value).getUserObject() != null)
			return ((DefaultMutableTreeNode) value).getUserObject().toString();
		else if (value != null)
			return value.toString();
		return null;
	}

	//
	// Grid and Scale
	//

	/**
	 * Returns the given point applied to the grid.
	 * @param p a point in screen coordinates.
	 * @return the same point applied to the grid.
	 */
	public Point snap(Point p) {

		if (gridEnabled && p != null) {

			double sgs = (double) gridSize * getScale();

			p.x = (int) Math.round(Math.round(p.x / sgs) * sgs);
			p.y = (int) Math.round(Math.round(p.y / sgs) * sgs);

		}

		return p;

	}

	/**
	 * Returns the given point applied to the grid.
	 * @param p a point in screen coordinates.
	 * @return the same point applied to the grid.
	 */
	public Dimension snap(Dimension d) {

		if (gridEnabled && d != null) {

			double sgs = (double) gridSize * getScale();

			d.width = 1 + (int) Math.round(Math.round(d.width / sgs) * sgs);
			d.height = 1 + (int) Math.round(Math.round(d.height / sgs) * sgs);

		}

		return d;

	}

	/**
	 * Upscale the given point in place, ie.
	 * using the given instance.
	 * @param p the point to be upscaled
	 * @return the upscaled point instance
	 */
	public Point toScreen(Point p) {
		if (p == null)
			return null;

		p.x = (int) Math.round(p.x * scale);
		p.y = (int) Math.round(p.y * scale);
		return p;
	}

	/**
	 * Downscale the given point in place, ie.
	 * using the given instance.
	 * @param p the point to be downscaled
	 * @return the downscaled point instance
	 */
	public Point fromScreen(Point p) {
		if (p == null)
			return null;

		p.x = (int) Math.round(p.x / scale);
		p.y = (int) Math.round(p.y / scale);
		return p;
	}

	/**
	 * Upscale the given rectangle in place, ie.
	 * using the given instance.
	 * @param rect the rectangle to be upscaled
	 * @return the upscaled rectangle instance
	 */
	public Rectangle toScreen(Rectangle rect) {
		if (rect == null)
			return null;

		rect.x *= scale;
		rect.y *= scale;
		rect.width *= scale;
		rect.height *= scale;
		return rect;
	}

	/**
	 * Downscale the given rectangle in place, ie.
	 * using the given instance.
	 * @param rect the rectangle to be downscaled
	 * @return the down-scaled rectangle instance
	 */
	public Rectangle fromScreen(Rectangle rect) {
		if (rect == null)
			return null;

		rect.x /= scale;
		rect.y /= scale;
		rect.width /= scale;
		rect.height /= scale;
		return rect;
	}

	//
	// Cell View Factory
	//

	/**
	 * Constructs a view for the specified cell and associates it
	 * with the specified object using the specified CellMapper.
	 * This calls refresh on the created CellView to create all
	 * dependent views.<p>
	 * Note: The mapping needs to be available before the views
	 * of child cells and ports are created.
	 *
	 * @param cell reference to the object in the model
	 */
	public CellView createView(Object cell, CellMapper map) {
		CellView view = null;
		if (graphModel.isPort(cell))
			view = createPortView(cell, map);
		else if (graphModel.isEdge(cell))
			view = createEdgeView(cell, map);
		else
			view = createVertexView(cell, map);
		map.putMapping(cell, view);
		view.refresh(true); // Create Dependent Views
		view.update();
		return view;
	}

	/**
	 * Computes and updates the size for <code>view</code>.
	 */
	public void updateAutoSize(CellView view) {
		if (view != null && !isEditing()
			&& GraphConstants.isAutoSize(view.getAllAttributes())) {
			Rectangle bounds = view.getBounds();
			if (bounds != null) {
				Dimension d = getUI().getPreferredSize(this, view);
				bounds.setSize(d);
			}
		}
	}

	/**
	 * Constructs an EdgeView view for the specified object.
	 */
	protected EdgeView createEdgeView(Object e, CellMapper cm) {
		if (e instanceof Edge)
			return createEdgeView((Edge) e, cm);
		else
			return new EdgeView(e, this, cm);
	}

	/**
	 * Constructs a PortView view for the specified object.
	 */
	protected PortView createPortView(Object p, CellMapper cm) {
		if (p instanceof Port)
			return createPortView((Port) p, cm);
		else
			return new PortView(p, this, cm);
	}

	/**
	 * Constructs an EdgeView view for the specified object.
	 *
	 * @deprecated	replaced by {@link #createEdgeView(Object,CellMapper)}
	 *		since JGraph no longer exposes dependecies on
	 * 		GraphCell subclasses (Port, Edge)
	 */
	protected EdgeView createEdgeView(Edge e, CellMapper cm) {
		return new EdgeView(e, this, cm);
	}

	/**
	 * Constructs a PortView view for the specified object.
	*
	* @deprecated	replaced by {@link #createPortView(Object,CellMapper)}
	*		since JGraph no longer exposes dependecies on
	* 		GraphCell subclasses (Port, Edge)
	 */
	protected PortView createPortView(Port p, CellMapper cm) {
		return new PortView(p, this, cm);
	}

	/**
	 * Constructs a VertexView view for the specified object.
	 */
	protected VertexView createVertexView(Object v, CellMapper cm) {
		return new VertexView(v, this, cm);
	}

	//
	// Unbound Properties
	//

	/**
	 * Returns the number of clicks for editing to start.
	 */
	public int getEditClickCount() {
		return editClickCount;
	}

	/**
	 * Sets the number of clicks for editing to start.
	 */
	public void setEditClickCount(int count) {
		editClickCount = count;
	}

	/**
	 * Returns true if the graph accepts drops/pastes from external sources.
	 */
	public boolean isDropEnabled() {
		return dropEnabled;
	}

	/**
	 * Sets if the graph accepts drops/pastes from external sources.
	 */
	public void setDropEnabled(boolean flag) {
		dropEnabled = flag;
	}

	/**
	 * Returns true if the graph uses Drag-and-Drop to move cells.
	 */
	public boolean isDragEnabled() {
		return dragEnabled;
	}

	/**
	 * Sets if the graph uses Drag-and-Drop to move cells.
	 */
	public void setDragEnabled(boolean flag) {
		dragEnabled = flag;
	}

	/*
	 * Returns true if the graph allows movement of cells.
	 */
	public boolean isMoveable() {
		return moveable;
	}

	/**
	 * Sets if the graph allows movement of cells.
	 */
	public void setMoveable(boolean flag) {
		moveable = flag;
	}

	/**
	 * Returns true if the graph allows adding/removing/modifying points.
	 */
	public boolean isBendable() {
		return bendable;
	}

	/**
	 * Sets if the graph allows adding/removing/modifying points.
	 */
	public void setBendable(boolean flag) {
		bendable = flag;
	}

	/**
	 * Returns true if the graph allows new connections to be established.
	 */
	public boolean isConnectable() {
		return connectable;
	}

	/**
	 * Setse if the graph allows new connections to be established.
	 */
	public void setConnectable(boolean flag) {
		connectable = flag;
	}

	/**
	 * Returns true if the graph allows existing connections to be removed.
	 */
	public boolean isDisconnectable() {
		return disconnectable;
	}

	/**
	 * Sets if the graph allows existing connections to be removed.
	 */
	public void setDisconnectable(boolean flag) {
		disconnectable = flag;
	}

	/**
	 * Returns true if cells are cloned on CTRL-Drag operations.
	 */
	public boolean isCloneable() {
		return cloneable;
	}

	/**
	 * Sets if cells are cloned on CTRL-Drag operations.
	 */
	public void setCloneable(boolean flag) {
		cloneable = flag;
	}

	/**
	 * Returns true if the graph allows cells to be resized.
	 */
	public boolean isSizeable() {
		return sizeable;
	}

	/**
	 * Sets if the graph allows cells to be resized.
	 */
	public void setSizeable(boolean flag) {
		sizeable = flag;
	}

	/**
	 * Returns true if selected edges should be disconnected from
	 * unselected vertices when they are moved.
	 */
	public boolean isDisconnectOnMove() {
		return disconnectOnMove && disconnectable;
	}

	/**
	 * Sets if selected edges should be disconnected from
	 * unselected vertices when they are moved.
	 */
	public void setSelectNewCells(boolean flag) {
		selectNewCells = flag;
	}

	/**
	 * Returns true if selected edges should be disconnected from
	 * unselected vertices when they are moved.
	 */
	public boolean isSelectNewCells() {
		return selectNewCells;
	}

	/**
	 * Sets if selected edges should be disconnected from
	 * unselected vertices when they are moved.
	 */
	public void setDisconnectOnMove(boolean flag) {
		disconnectOnMove = flag;
	}

	/**
	 * Returns true if the grid is active.
	 * @see #snap
	 *
	 */
	public boolean isGridEnabled() {
		return gridEnabled;
	}

	/**
	 * If set to true, the grid will be active.
	 * @see #snap
	 *
	 */
	public void setGridEnabled(boolean flag) {
		gridEnabled = flag;
	}

	/**
	 * Returns the maximum distance between the mousepointer and a cell to
	 * be selected.
	 */
	public int getTolerance() {
		return tolerance;
	}

	/**
	 * Sets the maximum distance between the mousepointer and a cell to
	 * be selected.
	 */
	public void setTolerance(int size) {
		tolerance = size;
	}

	/**
	 * Returns the size of the handles.
	 */
	public int getHandleSize() {
		return handleSize;
	}

	/**
	 * Sets the size of the handles.
	 */
	public void setHandleSize(int size) {
		handleSize = size;
	}

	/**
	 * Returns the miminum amount of pixels for a move operation.
	 */
	public int getMinimumMove() {
		return minimumMove;
	}

	/**
	 * Sets the miminum amount of pixels for a move operation.
	 */
	public void setMinimumMove(int pixels) {
		minimumMove = pixels;
	}

	//
	// Laf-Specific color scheme. These colors are changed
	// by BasicGraphUI when the laf changes.
	//

	/**
	 * Returns the current grid color.
	 */
	public Color getGridColor() {
		return gridColor;
	}

	/**
	 * Sets the current grid color.
	 */
	public void setGridColor(Color newColor) {
		gridColor = newColor;
	}

	/**
	 * Returns the current handle color.
	 */
	public Color getHandleColor() {
		return handleColor;
	}

	/**
	 * Sets the current handle color.
	 */
	public void setHandleColor(Color newColor) {
		handleColor = newColor;
	}

	/**
	 * Returns the current second handle color.
	 */
	public Color getLockedHandleColor() {
		return lockedHandleColor;
	}

	/**
	 * Sets the current second handle color.
	 */
	public void setLockedHandleColor(Color newColor) {
		lockedHandleColor = newColor;
	}

	/**
	 * Returns the current marquee color.
	 */
	public Color getMarqueeColor() {
		return marqueeColor;
	}

	/**
	 * Sets the current marquee color.
	 */
	public void setMarqueeColor(Color newColor) {
		marqueeColor = newColor;
	}

	/**
	 * Returns the current highlight color.
	 */
	public Color getHighlightColor() {
		return highlightColor;
	}

	/**
	 * Sets the current selection highlight color.
	 */
	public void setHighlightColor(Color newColor) {
		highlightColor = newColor;
	}

	//
	// Bound properties
	//

	/**
	 * Returns the current scale.
	 * @return the current scale as a double
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Sets the current scale.
	 * <p>
	 * Fires a property change for the SCALE_PROPERTY.
	 * @param newValue the new scale
	 */
	public void setScale(double newValue) {
		if (newValue > 0) {
			double oldValue = this.scale;
			scale = newValue;
			firePropertyChange(SCALE_PROPERTY, oldValue, newValue);
		}
	}

	/**
	 * Returns the size of the grid in pixels.
	 * @return the size of the grid as an int
	 */
	public int getGridSize() {
		return gridSize;
	}

	/**
	 * Returns the current grid view mode.
	 */
	public int getGridMode() {
		return gridMode;
	}

	/**
	 * Sets the size of the grid.
	 * <p>
	 * Fires a property change for the GRID_SIZE_PROPERTY.
	 * @param newSize the new size of the grid in pixels
	 */
	public void setGridSize(int newSize) {
		int oldValue = this.gridSize;

		this.gridSize = newSize;
		firePropertyChange(GRID_SIZE_PROPERTY, oldValue, newSize);
	}

	/**
	 * Sets the current grid view mode.
	 *
	 * @param mode The current grid view mode. Valid values are
	 *    <CODE>DOT_GRID_MODE</CODE>,
	 *    <CODE>CROSS_GRID_MODE</CODE>, and
	 *    <CODE>LINE_GRID_MODE</CODE>.
	 */
	public void setGridMode(int mode) {
		if (mode == DOT_GRID_MODE
			|| mode == CROSS_GRID_MODE
			|| mode == LINE_GRID_MODE) {
			gridMode = mode;
			repaint();
		}
	}

	/**
	 * Returns true if the grid will be visible.
	 * @return true if the grid is visible
	 */
	public boolean isGridVisible() {
		return gridVisible;
	}

	/**
	 * If set to true, the grid will be visible. <p>
	 * Fires a property change for the GRID_VISIBLE_PROPERTY.
	 */
	public void setGridVisible(boolean flag) {
		boolean oldValue = gridVisible;

		gridVisible = flag;
		firePropertyChange(GRID_VISIBLE_PROPERTY, oldValue, flag);
	}

	/**
	 * Returns true if the ports will be visible.
	 * @return true if the ports are visible
	 */
	public boolean isPortsVisible() {
		return portsVisible;
	}

	/**
	 * If set to true, the ports will be visible. <p>
	 * Fires a property change for the PORTS_VISIBLE_PROPERTY.
	 */
	public void setPortsVisible(boolean flag) {
		boolean oldValue = portsVisible;

		portsVisible = flag;
		firePropertyChange(PORTS_VISIBLE_PROPERTY, oldValue, flag);
	}

	/**
	 * Returns true if the graph will be anti aliased.
	 * @return true if the graph is anti aliased
	 */
	public boolean isAntiAliased() {
		return antiAliased;
	}

	/**
	 * Sets antialiasing on or off based on the boolean value.
	 * <p>
	 * Fires a property change for the ANTIALIASED_PROPERTY.
	 * @param newValue whether to turn antialiasing on or off
	 */
	public void setAntiAliased(boolean newValue) {
		boolean oldValue = this.antiAliased;

		this.antiAliased = newValue;
		firePropertyChange(ANTIALIASED_PROPERTY, oldValue, newValue);
	}

	/**
	 * Returns true if the graph is editable, ie. if it allows
	 * cells to be edited.
	 * @return true if the graph is editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * Determines whether the graph is editable. Fires a property
	 * change event if the new setting is different from the existing
	 * setting.
	 * <p>
	 * Note: Editable determines whether the graph allows editing. This
	 * is not to be confused with enabled, which allows the graph to
	 * handle mouse events (including editing).
	 * @param flag a boolean value, true if the graph is editable
	 */
	public void setEditable(boolean flag) {
		boolean oldValue = this.editable;

		this.editable = flag;
		firePropertyChange(EDITABLE_PROPERTY, oldValue, flag);
	}

	/**
	 * Returns the <code>GraphModel</code> that is providing the data.
	 * @return the model that is providing the data
	 */
	public GraphModel getModel() {
		return graphModel;
	}

	/**
	 * Sets the <code>GraphModel</code> that will provide the data.
	 * Note: Updates the current GraphLayoutCache's model using setModel if the
	 * GraphLayoutCache points to a different model. <p>
	 * Fires a property change for the GRAPH_MODEL_PROPERTY.
	 * @param newModel the <code>GraphModel</code> that is to provide the data
	 */
	public void setModel(GraphModel newModel) {
		GraphModel oldModel = graphModel;

		graphModel = newModel;
		firePropertyChange(GRAPH_MODEL_PROPERTY, oldModel, graphModel);
		// FIX: Use Listener
		if (graphLayoutCache != null
			&& graphLayoutCache.getModel() != graphModel)
			graphLayoutCache.setModel(graphModel);
		invalidate();
	}

	/**
	 * Returns the <code>GraphLayoutCache</code> that is providing the view-data.
	 * @return the view that is providing the view-data
	 */
	public GraphLayoutCache getGraphLayoutCache() {
		return graphLayoutCache;
	}

	/**
	 * Sets the <code>GraphLayoutCache</code> that will provide the view-data. <p>
	 * Note: Updates the GraphLayoutCache's model using setModel if the
	 * GraphLayoutCache points to an other model than this graph. <p>
	 * Fires a property change for the GRAPH_LAYOUT_CACHE_PROPERTY.
	 * @param newView the <code>GraphLayoutCache</code> that is to provide the view-data
	 */
	public void setGraphLayoutCache(GraphLayoutCache newLayoutCache) {
		GraphLayoutCache oldLayoutCache = graphLayoutCache;

		graphLayoutCache = newLayoutCache;
		firePropertyChange(
			GRAPH_LAYOUT_CACHE_PROPERTY,
			oldLayoutCache,
			graphLayoutCache);
		// FIX: Use Listener
		if (graphLayoutCache != null
			&& graphLayoutCache.getModel() != getModel())
			graphLayoutCache.setModel(getModel());
		invalidate();
	}

	/**
	 * Returns the <code>MarqueeHandler</code> that will handle
	 * marquee selection.
	 */
	public BasicMarqueeHandler getMarqueeHandler() {
		return marquee;
	}

	/**
	 * Sets the <code>MarqueeHandler</code> that will handle
	 * marquee selection.
	 */
	public void setMarqueeHandler(BasicMarqueeHandler newMarquee) {
		BasicMarqueeHandler oldMarquee = marquee;

		marquee = newMarquee;
		firePropertyChange(MARQUEE_HANDLER_PROPERTY, oldMarquee, newMarquee);
		invalidate();
	}

	/**
	 * Determines what happens when editing is interrupted by selecting
	 * another cell in the graph, a change in the graph's data, or by some
	 * other means. Setting this property to <code>true</code> causes the
	 * changes to be automatically saved when editing is interrupted.
	 * <p>
	 * Fires a property change for the INVOKES_STOP_CELL_EDITING_PROPERTY.
	 * @param newValue true means that <code>stopCellEditing</code> is invoked
	 * when editing is interruped, and data is saved; false means that
	 * <code>cancelCellEditing</code> is invoked, and changes are lost
	 */
	public void setInvokesStopCellEditing(boolean newValue) {
		boolean oldValue = invokesStopCellEditing;

		invokesStopCellEditing = newValue;
		firePropertyChange(
			INVOKES_STOP_CELL_EDITING_PROPERTY,
			oldValue,
			newValue);
	}

	/**
	 * Returns the indicator that tells what happens when editing is
	 * interrupted.
	 * @return the indicator that tells what happens when editing is
	 * interrupted
	 * @see #setInvokesStopCellEditing
	 *
	 */
	public boolean getInvokesStopCellEditing() {
		return invokesStopCellEditing;
	}

	/**
	 * Returns <code>isEditable</code>. This is invoked from the UI before
	 * editing begins to ensure that the given cell can be edited. This
	 * is provided as an entry point for subclassers to add filtered
	 * editing without having to resort to creating a new editor.
	 * @return true if the specified cell is editable
	 * @see #isEditable
	 *
	 */
	public boolean isCellEditable(Object cell) {
		if (cell != null) {
			CellView view = graphLayoutCache.getMapping(cell, false);
			if (view != null) {
				return isEditable()
				&& GraphConstants.isEditable(view.getAllAttributes());
			}
		}
		return false;
	}

	/**
	 * Overrides <code>JComponent</code>'s <code>getToolTipText</code>
	 * method in order to allow the graph to create a tooltip
	 * for the topmost cell under the mousepointer. This differs from JTree
	 * where the renderers tooltip is used.
	 * <p>
	 * NOTE: For <code>JGraph</code> to properly display tooltips of its
	 * renderers, <code>JGraph</code> must be a registered component with the
	 * <code>ToolTipManager</code>.  This can be done by invoking
	 * <code>ToolTipManager.sharedInstance().registerComponent(graph)</code>.
	 * This is not done automatically!
	 * @param event the <code>MouseEvent</code> that initiated the
	 * <code>ToolTip</code> display
	 * @return a string containing the  tooltip or <code>null</code>
	 * if <code>event</code> is null
	 */
	public String getToolTipText(MouseEvent event) {
		if (event != null) {
			Object cell = getFirstCellForLocation(event.getX(), event.getY());
			String s = convertValueToString(cell);
			return (s != null && s.length() > 0) ? s : null;
		}
		return null;
	}

	//
	// The following are convenience methods that get forwarded to the
	// current GraphSelectionModel.
	//

	/**
	 * Sets the graph's selection model. When a <code>null</code> value is
	 * specified an emtpy
	 * <code>selectionModel</code> is used, which does not allow selections.
	 * @param selectionModel the <code>GraphSelectionModel</code> to use,
	 * or <code>null</code> to disable selections
	 * @see GraphSelectionModel
	 *
	 */
	public void setSelectionModel(GraphSelectionModel selectionModel) {
		if (selectionModel == null)
			selectionModel = EmptySelectionModel.sharedInstance();

		GraphSelectionModel oldValue = this.selectionModel;

		// Remove Redirector From Old Selection Model
		if (this.selectionModel != null && selectionRedirector != null)
			this.selectionModel.removeGraphSelectionListener(
				selectionRedirector);

		this.selectionModel = selectionModel;

		// Add Redirector To New Selection Model
		if (selectionRedirector != null)
			this.selectionModel.addGraphSelectionListener(selectionRedirector);

		firePropertyChange(
			SELECTION_MODEL_PROPERTY,
			oldValue,
			this.selectionModel);
	}

	/**
	 * Returns the model for selections. This should always return a
	 * non-<code>null</code> value. If you don't want to allow anything
	 * to be selected
	 * set the selection model to <code>null</code>, which forces an empty
	 * selection model to be used.
	 * @return the current selection model
	 * @see #setSelectionModel
	 *
	 */
	public GraphSelectionModel getSelectionModel() {
		return selectionModel;
	}

	/**
	 * Clears the selection.
	 */
	public void clearSelection() {
		getSelectionModel().clearSelection();
	}

	/**
	 * Returns true if the selection is currently empty.
	 * @return true if the selection is currently empty
	 */
	public boolean isSelectionEmpty() {
		return getSelectionModel().isSelectionEmpty();
	}

	/**
	 * Adds a listener for <code>GraphSelection</code> events.
	 * @param tsl the <code>GraphSelectionListener</code> that will be notified
	 * when a cell is selected or deselected (a "negative
	 * selection")
	 */
	public void addGraphSelectionListener(GraphSelectionListener tsl) {
		listenerList.add(GraphSelectionListener.class, tsl);
		if (listenerList.getListenerCount(GraphSelectionListener.class) != 0
			&& selectionRedirector == null) {
			selectionRedirector = new GraphSelectionRedirector();
			selectionModel.addGraphSelectionListener(selectionRedirector);
		}
	}

	/**
	 * Removes a <code>GraphSelection</code> listener.
	 * @param tsl the <code>GraphSelectionListener</code> to remove
	 */
	public void removeGraphSelectionListener(GraphSelectionListener tsl) {
		listenerList.remove(GraphSelectionListener.class, tsl);
		if (listenerList.getListenerCount(GraphSelectionListener.class) == 0
			&& selectionRedirector != null) {
			selectionModel.removeGraphSelectionListener(selectionRedirector);
			selectionRedirector = null;
		}
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * @param e the <code>GraphSelectionEvent</code> generated by the
	 * <code>GraphSelectionModel</code>
	 * when a cell is selected or deselected
	 * @see javax.swing.event.EventListenerList
	 *
	 */
	protected void fireValueChanged(GraphSelectionEvent e) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GraphSelectionListener.class) {
				((GraphSelectionListener) listeners[i + 1]).valueChanged(e);
			}
		}
	}

	/**
	 * Selects the specified cell.
	 * @param cell the <code>Object</code> specifying the cell to select
	 */
	public void setSelectionCell(Object cell) {
		getSelectionModel().setSelectionCell(cell);
	}

	/**
	 * Selects the specified cells.
	 * @param cells an array of objects that specifies
	 * the cells to select
	 */
	public void setSelectionCells(Object[] cells) {
		getSelectionModel().setSelectionCells(cells);
	}

	/**
	 * Adds the cell identified by the specified <code>Object</code>
	 * to the current selection.
	 * @param cell the cell to be added to the selection
	 */
	public void addSelectionCell(Object cell) {
		getSelectionModel().addSelectionCell(cell);
	}

	/**
	 * Adds each cell in the array of cells to the current selection.
	 * @param cells an array of objects that specifies the cells to add
	 */
	public void addSelectionCells(Object[] cells) {
		getSelectionModel().addSelectionCells(cells);
	}

	/**
	 * Removes the cell identified by the specified Object from the current
	 * selection.
	 * @param cell the cell to be removed from the selection
	 */
	public void removeSelectionCell(Object cell) {
		getSelectionModel().removeSelectionCell(cell);
	}

	/**
	 * Returns the first selected cell.
	 * @return the <code>Object</code> for the first selected cell,
	 * or <code>null</code> if nothing is currently selected
	 */
	public Object getSelectionCell() {
		return getSelectionModel().getSelectionCell();
	}

	/**
	 * Returns all selected cells.
	 * @return an array of objects representing the selected cells,
	 * or <code>null</code> if nothing is currently selected
	 */
	public Object[] getSelectionCells() {
		return getSelectionModel().getSelectionCells();
	}

	/**
	 * Returns the number of cells selected.
	 * @return the number of cells selected
	 */
	public int getSelectionCount() {
		return getSelectionModel().getSelectionCount();
	}

	/**
	 * Returns true if the cell is currently selected.
	 * @param cell an object identifying a cell
	 * @return true if the cell is selected
	 */
	public boolean isCellSelected(Object cell) {
		return getSelectionModel().isCellSelected(cell);
	}

	/**
	 * Scrolls to the specified cell. Only works when this
	 * <code>JGraph</code> is contained in a <code>JScrollPane</code>.
	 * @param cell the object identifying the cell to bring into view
	 */
	public void scrollCellToVisible(Object cell) {
		Rectangle bounds = getCellBounds(cell);
		if (bounds != null) {
			bounds = new Rectangle(bounds);
			scrollRectToVisible(toScreen(bounds));
		}
	}

	/**
	 * Makes sure the specified point is visible.
	 * @param p the point that should be visible
	 */
	public void scrollPointToVisible(Point p) {
		if (p != null) {
			Rectangle bounds = new Rectangle(p);
			if (bounds != null)
				scrollRectToVisible(bounds);
		}
	}

	/**
	 * Returns true if the graph is being edited. The item that is being
	 * edited can be obtained using <code>getEditingCell</code>.
	 * @return true if the user is currently editing a cell
	 * @see #getSelectionCell
	 *
	 */
	public boolean isEditing() {
		GraphUI graph = getUI();

		if (graph != null)
			return graph.isEditing(this);
		return false;
	}

	/**
	 * Ends the current editing session.
	 * (The <code>DefaultGraphCellEditor</code>
	 * object saves any edits that are currently in progress on a cell.
	 * Other implementations may operate differently.)
	 * Has no effect if the tree isn't being edited.
	 * <blockquote>
	 * <b>Note:</b><br>
	 * To make edit-saves automatic whenever the user changes
	 * their position in the graph, use {@link #setInvokesStopCellEditing}.
	 * </blockquote>
	 * @return true if editing was in progress and is now stopped,
	 * false if editing was not in progress
	 */
	public boolean stopEditing() {
		GraphUI graph = getUI();

		if (graph != null)
			return graph.stopEditing(this);
		return false;
	}

	/**
	 * Cancels the current editing session. Has no effect if the
	 * graph isn't being edited.
	 */
	public void cancelEditing() {
		GraphUI graph = getUI();

		if (graph != null)
			graph.cancelEditing(this);
	}

	/**
	 * Selects the specified cell and initiates editing.
	 * The edit-attempt fails if the <code>CellEditor</code>
	 * does not allow
	 * editing for the specified item.
	 */
	public void startEditingAtCell(Object cell) {
		GraphUI graph = getUI();

		if (graph != null)
			graph.startEditingAtCell(this, cell);
	}

	/**
	 * Returns the cell that is currently being edited.
	 * @return the cell being edited
	 */
	public Object getEditingCell() {
		GraphUI graph = getUI();

		if (graph != null)
			return graph.getEditingCell(this);
		return null;
	}

	/**
	 * Messaged when the graph has changed enough that we need to resize
	 * the bounds, but not enough that we need to remove the cells
	 * (e.g cells were inserted into the graph). You should never have to
	 * invoke this, the UI will invoke this as it needs to. (Note: This
	 * is invoked by GraphUI, eg. after moving.)
	 */
	public void graphDidChange() {
		revalidate();
		repaint();
	}

	/**
	 * Serialization support.
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		Vector values = new Vector();
		s.defaultWriteObject();

		// Save the cellEditor, if its Serializable.
		if (graphModel instanceof Serializable) {
			values.addElement("graphModel");
			values.addElement(graphModel);
		}
		// Save the graphModel, if its Serializable.
		if (graphLayoutCache instanceof Serializable) {
			values.addElement("graphLayoutCache");
			values.addElement(graphLayoutCache);
		}
		// Save the selectionModel, if its Serializable.
		if (selectionModel instanceof Serializable) {
			values.addElement("selectionModel");
			values.addElement(selectionModel);
		}
		s.writeObject(values);

		if (getUIClassID().equals(uiClassID)) {
			/*byte count = JComponent.getWriteObjCounter(this);
			JComponent.setWriteObjCounter(this, --count);*/
			if (/*count == 0 && */
				ui != null) {
				ui.installUI(this);
			}
		}

	}

	/**
	 * Serialization support.
	 */
	private void readObject(ObjectInputStream s)
		throws IOException, ClassNotFoundException {

		s.defaultReadObject();

		Vector values = (Vector) s.readObject();
		int indexCounter = 0;
		int maxCounter = values.size();

		if (indexCounter < maxCounter
			&& values.elementAt(indexCounter).equals("graphModel")) {
			graphModel = (GraphModel) values.elementAt(++indexCounter);
			indexCounter++;
		}
		if (indexCounter < maxCounter
			&& values.elementAt(indexCounter).equals("graphLayoutCache")) {
			graphLayoutCache =
				(GraphLayoutCache) values.elementAt(++indexCounter);
			indexCounter++;
		}
		if (indexCounter < maxCounter
			&& values.elementAt(indexCounter).equals("selectionModel")) {
			selectionModel =
				(GraphSelectionModel) values.elementAt(++indexCounter);
			indexCounter++;
		}

		// Reinstall the redirector.
		if (listenerList.getListenerCount(GraphSelectionListener.class) != 0) {
			selectionRedirector = new GraphSelectionRedirector();
			selectionModel.addGraphSelectionListener(selectionRedirector);
		}
	}

	/**
	 * <code>EmptySelectionModel</code> is a <code>GraphSelectionModel</code>
	 * that does not allow anything to be selected.
	 * <p>
	 * <strong>Warning:</strong>
	 * Serialized objects of this class will not be compatible with
	 * future Swing releases.  The current serialization support is appropriate
	 * for short term storage or RMI between applications running the same
	 * version of Swing.  A future release of Swing will provide support for
	 * long term persistence.
	 */
	public static class EmptySelectionModel
		extends DefaultGraphSelectionModel {
		/** Unique shared instance. */
		protected static final EmptySelectionModel sharedInstance =
			new EmptySelectionModel();

		/** A <code>null</code> implementation that constructs an
				 *  EmptySelectionModel. */
		public EmptySelectionModel() {
			super(null);
		}

		/** Returns a shared instance of an empty selection model. */
		static public EmptySelectionModel sharedInstance() {
			return sharedInstance;
		}

		/** A <code>null</code> implementation that selects nothing. */
		public void setSelectionCells(Object[] cells) {
		}
		/** A <code>null</code> implementation that adds nothing. */
		public void addSelectionCells(Object[] cells) {
		}
		/** A <code>null</code> implementation that removes nothing. */
		public void removeSelectionCells(Object[] cells) {
		}
	}

	/**
	 * Handles creating a new <code>GraphSelectionEvent</code> with the
	 * <code>JGraph</code> as the
	 * source and passing it off to all the listeners.
	 * <p>
	 * <strong>Warning:</strong>
	 * Serialized objects of this class will not be compatible with
	 * future Swing releases.  The current serialization support is appropriate
	 * for short term storage or RMI between applications running the same
	 * version of Swing.  A future release of Swing will provide support for
	 * long term persistence.
	 */
	protected class GraphSelectionRedirector
		implements Serializable, GraphSelectionListener {
		/**
		 * Invoked by the <code>GraphSelectionModel</code> when the
		 * selection changes.
		 *
		 * @param e the <code>GraphSelectionEvent</code> generated by the
		 *		<code>GraphSelectionModel</code>
		 */
		public void valueChanged(GraphSelectionEvent e) {
			GraphSelectionEvent newE;

			newE = (GraphSelectionEvent) e.cloneWithSource(JGraph.this);
			fireValueChanged(newE);
		}
	} // End of class JGraph.GraphSelectionRedirector

	//
	// Scrollable interface
	//

	/**
	* Returns the preferred display size of a <code>JGraph</code>. The height is
	* determined from <code>getPreferredWidth</code>.
	* @return the graph's preferred size
	*/
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/**
	* Returns the amount to increment when scrolling. The amount is 4.
	* @param visibleRect the view area visible within the viewport
	* @param orientation either <code>SwingConstants.VERTICAL</code>
	* or <code>SwingConstants.HORIZONTAL</code>
	* @param direction less than zero to scroll up/left,
	* greater than zero for down/right
	* @return the "unit" increment for scrolling in the specified direction
	* @see javax.swing.JScrollBar#setUnitIncrement(int)
	*
	*/
	public int getScrollableUnitIncrement(
		Rectangle visibleRect,
		int orientation,
		int direction) {
		if (orientation == SwingConstants.VERTICAL) {
			return 2;
		}
		return 4;
	}

	/**
	* Returns the amount for a block increment, which is the height or
	* width of <code>visibleRect</code>, based on <code>orientation</code>.
	* @param visibleRect the view area visible within the viewport
	* @param orientation either <code>SwingConstants.VERTICAL</code>
	* or <code>SwingConstants.HORIZONTAL</code>
	* @param direction less than zero to scroll up/left,
	* greater than zero for down/right.
	* @return the "block" increment for scrolling in the specified direction
	* @see javax.swing.JScrollBar#setBlockIncrement(int)
	*
	*/
	public int getScrollableBlockIncrement(
		Rectangle visibleRect,
		int orientation,
		int direction) {
		return (orientation == SwingConstants.VERTICAL)
			? visibleRect.height
			: visibleRect.width;
	}

	/**
	* Returns false to indicate that the width of the viewport does not
	* determine the width of the graph, unless the preferred width of
	* the graph is smaller than the viewports width.  In other words:
	* ensure that the graph is never smaller than its viewport.
	* @return false
	* @see Scrollable#getScrollableTracksViewportWidth
	*
	*/
	public boolean getScrollableTracksViewportWidth() {
		if (getParent() instanceof JViewport) {
			return (
				((JViewport) getParent()).getWidth()
					> getPreferredSize().width);
		}
		return false;
	}

	/**
	* Returns false to indicate that the height of the viewport does not
	* determine the height of the graph, unless the preferred height
	* of the graph is smaller than the viewports height.  In other words:
	* ensure that the graph is never smaller than its viewport.
	* @return false
	* @see Scrollable#getScrollableTracksViewportHeight
	*
	*/
	public boolean getScrollableTracksViewportHeight() {
		if (getParent() instanceof JViewport) {
			return (
				((JViewport) getParent()).getHeight()
					> getPreferredSize().height);
		}
		return false;
	}

	/**
	* Returns a string representation of this <code>JGraph</code>.
	* This method
	* is intended to be used only for debugging purposes, and the
	* content and format of the returned string may vary between
	* implementations. The returned string may be empty but may not
	* be <code>null</code>.
	* @return a string representation of this <code>JGraph</code>.
	*/
	protected String paramString() {
		String editableString = (editable ? "true" : "false");
		String invokesStopCellEditingString =
			(invokesStopCellEditing ? "true" : "false");
		return super.paramString()
			+ ",editable="
			+ editableString
			+ ",invokesStopCellEditing="
			+ invokesStopCellEditingString;
	}

	public static void main(String[] args) {
		System.out.println(VERSION);
	}

}
