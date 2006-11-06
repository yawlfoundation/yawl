/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors.net;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Map;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellHandle;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphCellEditor;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphContext;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.PortView;
import org.jgraph.plaf.basic.BasicGraphUI;

import au.edu.qut.yawl.elements.YExternalNetElement;

import com.nexusbpm.command.MoveTasksCommand;
import com.nexusbpm.command.RenameElementCommand;
import com.nexusbpm.editor.WorkflowEditor;
import com.nexusbpm.editor.editors.net.cells.NexusCell;

/**
 *  Description of the Class
 *
 * @author     Dean Mao
 * @created    September 17, 2003
 */
public class GraphUI extends BasicGraphUI {
    /**
     *  Description of the Method
     *
     * @param  context  Description of the Parameter
     * @return          Description of the Return Value
     */
    public CellHandle createHandle( GraphContext context ) {
        if( context != null && !context.isEmpty() && graph.isEnabled() ) { return new CapselaRootHandle( context ); }
        return null;
    }

    /**
     * The following function is mostly copied from JGraph, with one section
     * of the code changed. The purpose of the change is so that renaming of
     * components in the graph is done through commands, and not through
     * JGraph's internal mechanism.
     * 
     * @see org.jgraph.plaf.basic.BasicGraphUI#completeEditing(boolean, boolean, boolean)
     */
    @Override
    protected void completeEditing( boolean messageStop, boolean messageCancel, boolean messageGraph ) {
        if (stopEditingInCompleteEditing && editingComponent != null) {
            Component oldComponent = editingComponent;
            Object oldCell = editingCell;
            GraphCellEditor oldEditor = cellEditor;
            boolean requestFocus = (graph != null &&
                    (graph.hasFocus() || SwingUtilities.findFocusOwner(editingComponent) != null));
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
                //----- start of changes made for YAWL
                Object newValue = oldEditor.getCellEditorValue();
                if( oldCell instanceof NexusCell
                        && ((NexusCell) oldCell).getProxy().getData() instanceof YExternalNetElement
                        && newValue instanceof String ) {
                    String oldName = ((YExternalNetElement) ((NexusCell) oldCell).getProxy().getData()).getName();
                    WorkflowEditor.getExecutor().executeCommand(
                            new RenameElementCommand(
                                    ((NexusCell) oldCell).getProxy(),
                                    (String) newValue,
                                    oldName ) );
                }
                else {
                    // the following line is original to the JGraph code
                    graphLayoutCache.valueForCellChanged(oldCell, newValue);
                }
                //----- end of changes made for YAWL
            }
            updateSize();
            // Remove Editor Listener
            if (oldEditor != null && cellEditorListener != null)
                oldEditor.removeCellEditorListener(cellEditorListener);
            cellEditor = null;
        }
    }
    
    /**
     * This custom root handle handles a different mechanism for moving
     * objects than the default for JGraph. Most of the code is copied
     * from JGraph, with the following changes:
     * <ul>
     * <li>When the drag operation first starts, a copy of the attributes
     * map for the relevant views is made (to enable undoing).</li>
     * <li>When the drag operation completes, instead of modifying the
     * graph right away, it creates a command.</li>
     * </ul>
     * I've placed comments including the word "YAWL" (in all caps) at
     * every change made in order to make the changes easy to find, in
     * case they need to be modified (such as if we upgrade to a
     * different version of JGraph, etc).
     *
     * @author Nathan Rose
     */
    public class CapselaRootHandle implements CellHandle, Serializable {
        // x and y offset from the mouse press event to the left/top corner of a
        // view that is returned by a findViewForPoint().
        // These are used only when the isSnapSelectedView mode is enabled.
        protected transient double _mouseToViewDelta_x = 0;

        protected transient double _mouseToViewDelta_y = 0;

        // Double Buffered
        protected transient Image offscreen;

        protected transient Graphics offgraphics;

        protected transient boolean firstDrag = true;

        /* Temporary views for the cells. */
        protected transient CellView[] views;

        protected transient CellView[] contextViews;

        protected transient CellView[] portViews;

        /* Bounds of the cells. Non-null if too many cells. */
        protected transient Rectangle2D cachedBounds;

        /* Initial top left corner of the selection */
        protected transient Point2D initialLocation;

        /* Child handles. Null if too many handles. */
        protected transient CellHandle[] handles;

        /* The point where the mouse was pressed. */
        protected transient Point2D start = null, last, snapStart, snapLast;

        /**
         * Indicates whether this handle is currently moving cells. Start may be
         * non-null and isMoving false while the minimum movement has not been
         * reached.
         */
        protected boolean isMoving = false;

        /**
         * Indicates whether this handle has started drag and drop. Note:
         * isDragging => isMoving.
         */
        protected boolean isDragging = false;

        /** The handle that consumed the last mousePressedEvent. Initially null. */
        protected transient CellHandle activeHandle = null;

        /* The current selection context, responsible for cloning the cells. */
        protected transient GraphContext context;

        /*
         * True after the graph was repainted to block xor-ed painting of
         * background.
         */
        protected boolean isContextVisible = true;

        protected boolean blockPaint = false;

        /* Defines the Disconnection if DisconnectOnMove is True */
        protected transient ConnectionSet disconnect = null;

        /**
         * Creates a root handle which contains handles for the given cells. The
         * root handle and all its childs point to the specified JGraph
         * instance. The root handle is responsible for dragging the selection.
         */
        public CapselaRootHandle(GraphContext ctx) {
            this.context = ctx;
            if (!ctx.isEmpty()) {
                // Temporary cells
                views = ctx.createTemporaryCellViews();
                Rectangle2D tmpBounds = graph.toScreen(graph.getCellBounds(ctx.getCells()));
                if (ctx.getDescendantCount() < MAXCELLS) {
                    contextViews = ctx.createTemporaryContextViews();
                    initialLocation = graph.toScreen(getInitialLocation(ctx.getCells()));
                } else
                    cachedBounds = tmpBounds;
                if (initialLocation == null)
                    initialLocation = new Point2D.Double(tmpBounds.getX(), tmpBounds.getY());
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

        /**
         * Returns the initial location, which is the top left corner of the
         * selection, ignoring all connected endpoints of edges.
         */
        protected Point2D getInitialLocation(Object[] cells) {
            if (cells != null && cells.length > 0) {
                Rectangle2D ret = null;
                for (int i = 0; i < cells.length; i++) {
                    if (graphModel.isEdge(cells[i])) {
                        CellView cellView = graphLayoutCache.getMapping(cells[i], false);
                        if (cellView instanceof EdgeView) {
                            EdgeView edgeView = (EdgeView) cellView;
                            if (edgeView.getSource() == null) {
                                Point2D pt = edgeView.getPoint(0);
                                if (pt != null) {
                                    if (ret == null)
                                        ret = new Rectangle2D.Double(pt.getX(), pt.getY(), 0, 0);
                                    else
                                        Rectangle2D.union(
                                                ret,
                                                new Rectangle2D.Double(
                                                        pt.getX(),
                                                        pt.getY(),
                                                        0,
                                                        0),
                                                ret);
                                }
                            }
                            if (edgeView.getTarget() == null) {
                                Point2D pt = edgeView.getPoint(edgeView.getPointCount() - 1);
                                if (pt != null) {
                                    if (ret == null)
                                        ret = new Rectangle2D.Double(pt.getX(), pt.getY(), 0, 0);
                                    else
                                        Rectangle2D.union(
                                                ret,
                                                new Rectangle2D.Double(
                                                        pt.getX(),
                                                        pt.getY(),
                                                        0,
                                                        0),
                                                ret);
                                }
                            }
                        }
                    } else {
                        Rectangle2D r = graph.getCellBounds(cells[i]);
                        if (r != null) {
                            if (ret == null)
                                ret = (Rectangle2D) r.clone();
                            Rectangle2D.union(ret, r, ret);
                        }
                    }
                }
                if (ret != null)
                    return new Point2D.Double(ret.getX(), ret.getY());
            }
            return null;
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
            if (isDragging && !DNDPREVIEW) // BUG IN 1.4.0 (FREEZE)
                return;
            if (cachedBounds != null) { // Paint Cached Bounds
                g.setColor(Color.black);
                g.drawRect(
                        (int) cachedBounds.getX(),
                        (int) cachedBounds.getY(),
                        (int) cachedBounds.getWidth() - 2,
                        (int) cachedBounds.getHeight() - 2);

            } else {
                Graphics2D g2 = (Graphics2D) g;
                AffineTransform oldTransform = g2.getTransform();
                g2.scale(graph.getScale(), graph.getScale());
                if (views != null) { // Paint Temporary Views
                    for (int i = 0; i < views.length; i++)
                        paintCell(g, views[i], views[i].getBounds(), true);
                }
                // Paint temporary context
                if (contextViews != null && isContextVisible) {
                    for (int i = 0; i < contextViews.length; i++)
                        paintCell(g, contextViews[i], contextViews[i].getBounds(), true);
                }
                if (!graph.isPortsScaled())
                    g2.setTransform(oldTransform);
                if (portViews != null && graph.isPortsVisible())
                    paintPorts(g, portViews);
                g2.setTransform(oldTransform);
            }
        }

        /**
         * Invoked when the mouse pointer has been moved on a component (with no
         * buttons down).
         */
        public void mouseMoved(MouseEvent event) {
            if (!event.isConsumed() && handles != null) {
                for (int i = handles.length - 1; i >= 0 && !event.isConsumed(); i--)
                    if (handles[i] != null)
                        handles[i].mouseMoved(event);
            }
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
                    Point2D screenPoint = event.getPoint();
                    Point2D pt = graph.fromScreen((Point2D) screenPoint.clone());
                    CellView view = findViewForPoint(pt);
                    if (view != null) {
                        if (snapSelectedView) {
                            Rectangle2D bounds = view.getBounds();
                            start = graph.toScreen(new Point2D.Double(bounds.getX(), bounds.getY()));
                            snapStart = graph.snap((Point2D) start.clone());
                            _mouseToViewDelta_x = screenPoint.getX() - start.getX();
                            _mouseToViewDelta_y = screenPoint.getY() - start.getY();
                        } else { // this is the original RootHandle's mode.
                            snapStart = graph.snap((Point2D) screenPoint.clone());
                            _mouseToViewDelta_x = snapStart.getX() - screenPoint.getX();
                            _mouseToViewDelta_y = snapStart.getY() - screenPoint.getY();
                            start = (Point2D) snapStart.clone();
                        }
                        last = (Point2D) start.clone();
                        snapLast = (Point2D) snapStart.clone();
                        isContextVisible = contextViews != null
                            && contextViews.length < MAXCELLS
                            && (!event.isControlDown() || !graph.isCloneable());
                        event.consume();
                    }
                }
            }
        }

        /**
         * Hook for subclassers to return a different view for a mouse click at
         * <code>pt</code>. For example, this can be used to return a leaf
         * cell instead of a group.
         */
        protected CellView findViewForPoint(Point2D pt) {
            double snap = graph.getTolerance();
            Rectangle2D r = new Rectangle2D.Double(pt.getX() - snap, pt.getY()
                    - snap, 2 * snap, 2 * snap);
            for (int i = 0; i < views.length; i++)
                if (views[i].intersects(graph, r))
                    return views[i];
            return null;
        }

        protected void startDragging(MouseEvent event) {
            isDragging = true;
            if (graph.isDragEnabled()) {
                int action = (event.isControlDown() && graph.isCloneable()) ? TransferHandler.COPY
                        : TransferHandler.MOVE;
                TransferHandler th = graph.getTransferHandler();
                setInsertionLocation(event.getPoint());
                try {
                    th.exportAsDrag(graph, event, action);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }

        /**
         * @return Returns the parent graph scrollpane for the specified graph.
         */
        public Component getFirstOpaqueParent(Component component) {
            if (component != null) {
                Component parent = component;
                while (parent != null) {
                    if (parent.isOpaque() && !(parent instanceof JViewport))
                        return parent;
                    parent = parent.getParent();
                }
            }
            return component;
        }

        // Double Buffers by David Larsson
        protected void initOffscreen() {
            try {
                Rectangle rect = graph.getBounds();
                /*
                 * RepaintManager repMan = RepaintManager.currentManager(graph);
                 * offscreen = repMan.getVolatileOffscreenBuffer(graph, (int)
                 * rect.getWidth(), (int) rect.getHeight());
                 */
                offscreen = new BufferedImage(rect.width, rect.height,
                        BufferedImage.TYPE_INT_RGB);
                offgraphics = offscreen.getGraphics();
                offgraphics.setClip(0, 0, rect.width, rect.height);
                /*
                Component comp = getFirstOpaqueParent(graph);
                offgraphics.translate(-rect.x, -rect.y);
                comp.paint(offgraphics);
                offgraphics.translate(rect.x, rect.y);
                */
                offgraphics.setColor(graph.getBackground());
                offgraphics.fillRect(0, 0, rect.width, rect.height);
                graph.getUI().paint(offgraphics, graph);
            } catch (Exception e) {
                offscreen = null;
                offgraphics = null;
            } catch (Error e) {
                offscreen = null;
                offgraphics = null;
            }
        }

        /** Process mouse dragged event. */
        public void mouseDragged(MouseEvent event) {
            boolean constrained = isConstrainedMoveEvent(event);
            Rectangle2D dirty = null;
            if (firstDrag && graph.isDoubleBuffered() && cachedBounds == null) {
                initOffscreen();
                firstDrag = false;
            }
            if (event != null && !event.isConsumed()) {
                if (activeHandle != null) // Paint Active Handle
                    activeHandle.mouseDragged(event);
                // Invoke Mouse Dragged
                else if (start != null) { // Move Cells
                    Graphics g = (offgraphics != null) ? offgraphics : graph.getGraphics();
                    Point ep = event.getPoint();
                    Point2D point = new Point2D.Double(
                            ep.getX() - _mouseToViewDelta_x,
                            ep.getY() - _mouseToViewDelta_y);
                    Point2D snapCurrent = graph.snap(point);
                    Point2D current = snapCurrent;
                    int thresh = graph.getMinimumMove();
                    double dx = current.getX() - start.getX();
                    double dy = current.getY() - start.getY();
                    if (isMoving || Math.abs(dx) > thresh || Math.abs(dy) > thresh) {
                        boolean overlayed = false;
                        isMoving = true;
                        if (disconnect == null && graph.isDisconnectOnMove())
                            disconnect = context.disconnect(graphLayoutCache.getAllDescendants(views));
                        // Constrained movement
                        double totDx = current.getX() - start.getX();
                        double totDy = current.getY() - start.getY();
                        dx = current.getX() - last.getX();
                        dy = current.getY() - last.getY();
                        if (constrained && cachedBounds == null) {
                            if (Math.abs(totDx) < Math.abs(totDy)) {
                                dx = 0;
                                dy = totDy;
                            } else {
                                dx = totDx;
                                dy = 0;
                            }
                        } else if (!graph.isMoveBelowZero()
                                && last != null
                                && initialLocation != null
                                && start != null) {
                            // TODO: remove?
                            if (initialLocation.getX() + totDx < 0)
                                dx = start.getX() - last.getX() - initialLocation.getX();
                            if (initialLocation.getY() + totDy < 0)
                                dy = start.getY() - last.getY() - initialLocation.getY();
                        }
                        double scale = graph.getScale();
                        dx = (int) (dx / scale);
                        // we don't want to round. The best thing is to get just
                        // the integer part.
                        // That way, the view won't "run away" from the mouse.
                        // It may lag behind
                        // a mouse pointer occasionally, but will be catching
                        // up.
                        dy = (int) (dy / scale);

                        g.setColor(graph.getForeground());

                        // use 'darker' to force XOR to distinguish between
                        // existing background elements during drag
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
                            isContextVisible = (!event.isControlDown() || !graph
                                    .isCloneable())
                                    && contextViews != null
                                    && (contextViews.length < MAXCELLS);
                            blockPaint = false;
                            if (offscreen != null) {
                                dirty = graph.toScreen(AbstractCellView.getBounds(views));
                                Rectangle2D t = graph.toScreen(AbstractCellView.getBounds(contextViews));
                                if (t != null)
                                    dirty.add(t);
                            }
                            if (constrained && cachedBounds == null) {
                                // Reset Initial Positions
                                CellView[] all = graphLayoutCache.getAllDescendants(views);
                                for (int i = 0; i < all.length; i++) {
                                    CellView orig = graphLayoutCache.getMapping(all[i].getCell(), false);
                                    AttributeMap attr = orig.getAllAttributes();
                                    all[i].changeAttributes((AttributeMap) attr.clone());
                                    all[i].refresh(graph.getModel(), context, false);
                                }
                            }
                            if (cachedBounds != null)
                                cachedBounds.setFrame(
                                        cachedBounds.getX() + dx * scale,
                                        cachedBounds.getY() + dy * scale,
                                        cachedBounds.getWidth(),
                                        cachedBounds.getHeight());
                            else {
                                // ----- start of inserted code for YAWL
                                if( oldAttributes == null ) {
                                    CellView[] oldAll = graphLayoutCache.getAllDescendants(views);
                                    oldAttributes = GraphConstants.createAttributes(oldAll, null);
                                }
                                // ----- end of inserted code for YAWL
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
                                last = (Point2D) start.clone();
                            last.setLocation(last.getX() + dx * scale, last.getY() + dy * scale);
                            // It is better to translate <code>last<code> by a
                            // scaled dx/dy
                            // instead of making it to be the
                            // <code>current<code> (as in prev version),
                            // so that the view would be catching up with a
                            // mouse pointer
                            snapLast = snapCurrent;
                            if (overlayed && offscreen != null) {
                                dirty.add(graph.toScreen(AbstractCellView
                                        .getBounds(views)));
                                Rectangle2D t = graph.toScreen(AbstractCellView
                                        .getBounds(contextViews));
                                if (t != null)
                                    dirty.add(t);
                                // TODO: Should use real ports if portsVisible
                                // and check if ports are scaled
                                int border = PortView.SIZE + 4;
                                if (graph.isPortsScaled())
                                    border = (int) (graph.getScale() * border);
                                int border2 = border / 2;
                                dirty.setFrame(dirty.getX() - border2, dirty
                                        .getY()
                                        - border2, dirty.getWidth() + border,
                                        dirty.getHeight() + border);
                                double sx1 = Math.max(0, dirty.getX());
                                double sy1 = Math.max(0, dirty.getY());
                                double sx2 = sx1 + dirty.getWidth();
                                double sy2 = sy1 + dirty.getHeight();
                                if (isDragging && !DNDPREVIEW) // BUG IN 1.4.0
                                    // (FREEZE)
                                    return;
                                graph.getGraphics().drawImage(offscreen,
                                        (int) sx1, (int) sy1, (int) sx2,
                                        (int) sy2, (int) sx1, (int) sy1,
                                        (int) sx2, (int) sy2, graph);
                            }
                        }
                    } // end if (isMoving or ...)
                } // end if (start != null)
            } else if (event == null)
                graph.repaint();
        }
        
        /** New variable inserted for YAWL. */
        Map oldAttributes;

        public void mouseReleased(MouseEvent event) {
            try {
                if (event != null && !event.isConsumed()) {
                    if (activeHandle != null) {
                        activeHandle.mouseReleased(event);
                        activeHandle = null;
                    } else if (isMoving && !event.getPoint().equals(start)) {
                        if (cachedBounds != null) {
                            double dx = event.getX() - start.getX();
                            double dy = event.getY() - start.getY();
                            Point2D tmp = graph.fromScreen(new Point2D.Double(dx, dy));
                            GraphLayoutCache.translateViews(views, tmp.getX(), tmp.getY());
                        }
                        CellView[] all = graphLayoutCache.getAllDescendants(views);
                        Map attributes = GraphConstants.createAttributes(all, null);
//                        if (event.isControlDown() && graph.isCloneable()) { // Clone
//                            // Cells
//                            Object[] cells = graph.getDescendants(graph.order(context.getCells()));
//                            // Include properties from hidden cells
//                            Map hiddenMapping = graphLayoutCache.getHiddenMapping();
//                            for (int i = 0; i < cells.length; i++) {
//                                Object witness = attributes.get(cells[i]);
//                                if (witness == null) {
//                                    CellView view = (CellView) hiddenMapping.get(cells[i]);
//                                    if (view != null && !graphModel.isPort(view.getCell())) {
//                                        // TODO: Clone required? Same in
//                                        // GraphConstants.
//                                        AttributeMap attrs = (AttributeMap) view.getAllAttributes().clone();
//                                        // Maybe translate?
//                                        // attrs.translate(dx, dy);
//                                        attributes.put(cells[i], attrs.clone());
//                                    }
//                                }
//                            }
//                            ConnectionSet cs = ConnectionSet.create(graphModel, cells, false);
//                            ParentMap pm = ParentMap.create(graphModel, cells, false, true);
//                            cells = graphLayoutCache.insertClones(
//                                    cells, graph.cloneCells(cells), attributes, cs, pm, 0, 0);
//                        } else
                        if (graph.isMoveable()) { // Move Cells
                            // Execute on view
                            //----- start of changes made for YAWL
                            for( int index = 0; index < all.length; index++ ) {
                                assert oldAttributes.containsKey( all[ index ].getCell() ) :
                                    "old attributes is missing a key";
                            }
                            
                            WorkflowEditor.getExecutor().executeCommand(
                                    new MoveTasksCommand( oldAttributes, attributes ) );
                            
                            oldAttributes = null;
                            
//                            graph.getGraphLayoutCache().edit(attributes,
//                                    disconnect, null, null);
                            //----- end of changes made for YAWL
                        }
                        event.consume();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                initialLocation = null;
                isDragging = false;
                disconnect = null;
                offscreen = null;
                firstDrag = true;
                start = null;
            }
        }
    }
}

