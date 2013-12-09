/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */
package org.yawlfoundation.yawl.editor.ui.net.utilities;

import org.jgraph.graph.*;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetCellUtilities {

    private static final int FLOW_SPACER = 20;


    public static void propagateFontChangeAcrossNet(NetGraph net, Font font) {
        if (net.getNetModel() == null) {
            return;
        }
        net.getNetModel().beginUpdate();
        propagateFontChangeAcrossNetNoUndo(net, font);
        net.getNetModel().endUpdate();
    }


    private static void propagateFontChangeAcrossNetNoUndo(NetGraph net, Font font) {
        net.setFont(font);

        List cells = NetGraphModel.getDescendants(net.getNetModel(),
                NetGraphModel.getRoots(net.getNetModel()));
        for (Object cell : cells) {
            if (cell instanceof VertexContainer) {
                VertexContainer container = (VertexContainer) cell;
                if (container.getLabel() != null) {
                    String label = container.getLabel().getText();

                    // cheap and nasty way of getting the label to position nicely
                    // with the vertex is to delete, and set it again, letting
                    // the method do the math for positioning them in relation
                    // to each other.

                    net.setElementLabelInsideUpdate(container.getVertex(), "");
                    net.setElementLabelInsideUpdate(container.getVertex(), label);
                }
            }
        }
        net.revalidate();
    }

    public static void translateView(NetGraph net, CellView view, double x, double y) {
        translateViews(net, new CellView[] { view }, x, y);
    }


    public static void translateViews(NetGraph net, CellView[] views, double x, double y) {
        for (CellView view: views) {
            Rectangle2D oldBounds = view.getBounds();
            Rectangle2D.Double newBounds = new Rectangle2D.Double(
                            oldBounds.getX() + x,
                            oldBounds.getY() + y,
                            oldBounds.getWidth(),
                            oldBounds.getHeight());

            if (newBounds.getX() < 0 || newBounds.getY() < 0) {
                return;
            }
            if (view instanceof VertexView) {
                ((VertexView) view).setBounds(newBounds);
            }
            if (view instanceof EdgeView) {
                EdgeView edgeView = (EdgeView) view;

                for (int j = 0; j < edgeView.getPointCount(); j++) {
                    Point2D oldPoint = edgeView.getPoint(j);
                    Point2D.Double newPoint = new Point2D.Double(
                            oldPoint.getX() + x,
                            oldPoint.getY() + y);
                    edgeView.setPoint(j, newPoint);
                }
            }
        }

        net.setGridEnabled(false);
        applyViewChange(net, views);
        net.setGridEnabled(true);
    }


    public static void moveViewToLocation(NetGraph net, CellView view, double x, double y) {
        moveViewsToLocation(net, new CellView[] { view }, x, y);
    }


    private static void moveViewsToLocation(NetGraph net, CellView[] views, double x, double y) {
        for (CellView view: views) {
            Rectangle2D oldBounds = view.getBounds();
            Rectangle2D.Double newBounds = new Rectangle2D.Double(x, y,
                            oldBounds.getWidth(), oldBounds.getHeight());

            if (view instanceof VertexView) {
                ((VertexView) view).setBounds(newBounds);
            }
        }

        net.setGridEnabled(false);
        applyViewChange(net, views);
        net.setGridEnabled(true);
    }


    public static void resizeView(NetGraph net, CellView view, double width, double height) {
        Rectangle2D oldBounds = view.getBounds();

        Rectangle2D.Double newBounds = new Rectangle2D.Double(
                        oldBounds.getX(),
                        oldBounds.getY(),
                        oldBounds.getWidth() + width,
                        oldBounds.getHeight() + height);

        net.setGridEnabled(false);

        HashMap map = new HashMap();
        GraphConstants.setBounds(map,newBounds);
        net.getGraphLayoutCache().editCell(view.getCell(),map);
        net.setGridEnabled(true);
    }


    public static void applyViewChange(NetGraph net, CellView view) {
        applyViewChange(net, new CellView[] { view } );
    }


    private static void applyViewChange(NetGraph net, CellView[] views) {
        CellView[] allViews = VertexView.getDescendantViews(views);
        Map attributes = GraphConstants.createAttributes(allViews, null);
        net.getNetModel().edit(attributes, null, null, null);
        scrollNetToShowCells(net, getCellsOfViews(views));
    }


    public static void alignCellsAlongTop(NetGraph net, Object[] cells) {
        if (cells != null) {
            Rectangle2D r = net.getCellBounds(cells);
            net.getNetModel().beginUpdate();

            for (Object cell: cells) {
                Rectangle2D bounds = net.getCellBounds(cell);
                net.moveElementBy((GraphCell) cell, 0, (-1 * bounds.getY()) + r.getY());
            }

            net.getNetModel().endUpdate();
            net.requestFocus();
        }
    }


    public static void alignCellsAlongHorizontalCentre(NetGraph net, Object[] cells) {
        if (cells != null) {
            Rectangle2D r = net.getCellBounds(cells);
            double cy = r.getHeight() / 2;
            net.getNetModel().beginUpdate();

            for (Object cell: cells) {
                Rectangle2D bounds = net.getCellBounds(cell);
                net.moveElementBy((GraphCell) cell, 0,
                        (-1 * bounds.getY()) + r.getY() + cy - bounds.getHeight() / 2);
            }

            net.getNetModel().endUpdate();
            net.requestFocus();
        }
    }


    public static void alignCellsAlongBottom(NetGraph net, Object[] cells) {
        if (cells != null) {
            Rectangle2D r = net.getCellBounds(cells);
            net.getNetModel().beginUpdate();

            for (Object cell: cells) {
                Rectangle2D bounds = net.getCellBounds(cell);
                net.moveElementBy((GraphCell) cell, 0,
                        (-1 * bounds.getY()) + r.getY() +
                                r.getHeight() - bounds.getHeight());
            }

            net.getNetModel().endUpdate();
            net.requestFocus();
        }
    }

    public static void alignCellsAlongLeft(NetGraph net, Object[] cells) {
        if (cells != null) {
            Rectangle2D r = net.getCellBounds(cells);
            net.getNetModel().beginUpdate();

            for (Object cell: cells) {
                Rectangle2D bounds = net.getCellBounds(cell);
                net.moveElementBy((GraphCell) cell, (-1 * bounds.getX()) + r.getX(), 0);
            }

            net.getNetModel().endUpdate();
            net.requestFocus();
        }
    }

    public static void alignCellsAlongVerticalCentre(NetGraph net, Object[] cells) {
        if (cells != null) {
            Rectangle2D r = net.getCellBounds(cells);
            double cx = r.getWidth() / 2;
            net.getNetModel().beginUpdate();

            for (Object cell: cells) {
                Rectangle2D bounds = net.getCellBounds(cell);
                net.moveElementBy((GraphCell) cell,
                        (-1 * bounds.getX()) + r.getX() + cx - bounds.getWidth() / 2, 0);
            }

            net.getNetModel().endUpdate();
            net.requestFocus();
        }
    }


    public static void alignCellsAlongRight(NetGraph net, Object[] cells) {
        if (cells != null) {
            Rectangle2D r = net.getCellBounds(cells);
            net.getNetModel().beginUpdate();

            for (Object cell: cells) {
                Rectangle2D bounds = net.getCellBounds(cell);
                net.moveElementBy((GraphCell) cell,
                        (-1 * bounds.getX())
                                + r.getX() + r.getWidth() - bounds.getWidth(), 0);
            }

            net.getNetModel().endUpdate();
            net.requestFocus();
        }
    }


    public static void setFlowStyle(NetGraph net, YAWLFlowRelation flow, int flowStyle) {
        Map flowMap = GraphConstants.createAttributes(flow, GraphConstants.LINESTYLE,
                flowStyle);
        net.getNetModel().edit(flowMap, null, null, null);
    }


    public static int getFlowLineStyle(NetGraph net, YAWLFlowRelation flow) {
        CellView view = net.getViewFor(flow);
        if (view != null) {
            return GraphConstants.getLineStyle(net.getViewFor(flow).getAllAttributes());
        }
        return GraphConstants.STYLE_ORTHOGONAL;
    }


    public static void togglePointOnFlow(NetGraph net, YAWLFlowRelation flow, Point point) {
        Point2D pointFoundForDeletion = null;
        List flowPoints = GraphConstants.getPoints(net.getViewFor(flow).getAllAttributes());

        for (Object flowPoint : flowPoints) {
            // Because we have ports in the list too, focus just on the extra points.
            if (flowPoint instanceof Point2D) {
                Point2D thisPoint = (Point2D) flowPoint;

                // if it's pretty close (within 8 pixels), we're trying to remove a point.
                if ((Math.abs(thisPoint.getX() - point.getX()) <= 8) &&
                        (Math.abs(thisPoint.getY() - point.getY()) <= 8)) {
                    pointFoundForDeletion = thisPoint;
                }
            }
        }

        if (pointFoundForDeletion != null) {
            flowPoints.remove(pointFoundForDeletion);
            updateFlowPoints(net, flow, flowPoints);
            return;
        }

        // Dealing with addition of a point below.
        if (flowPoints.size() == 2) {
            flowPoints.add(1,point);
        } else {

            Line2D thisSegment = null;
            int containedSegmentIndex = 0;  // default to the first segment.

            for(int i = 0; i < flowPoints.size() - 1; i++) {
                Point2D currentPoint = getPointFor(net, flowPoints.get(i));
                Point2D nextPoint = getPointFor(net, flowPoints.get(i + 1));
                thisSegment = new Line2D.Double(currentPoint, nextPoint);

                // if the point is within 4 pixels of the nearest point in thisSegment, we
                // want this segment  to insert the new point within.

                if (thisSegment.ptSegDist(point) <= 4) {
                    containedSegmentIndex = i;
                }
            }
            flowPoints.add(containedSegmentIndex + 1,point);
        }

        updateFlowPoints(net, flow, flowPoints);
    }

    private static Point2D getPointFor(NetGraph net, Object object) {
        if (object instanceof PortView) {
            return getCenterOfPort(net, (PortView)object);
        }
        if (object instanceof Point2D) {
            return (Point2D) object;
        }
        return null;
    }

    private static Point2D getCenterOfPort(NetGraph net, PortView portView) {
        return new Point2D.Double(
                portView.getBounds().getCenterX(),
                portView.getBounds().getCenterY());
    }

    private static void updateFlowPoints(NetGraph net, YAWLFlowRelation flow, List flowPoints) {
        Map flowMap = GraphConstants.createAttributes(flow, GraphConstants.POINTS,
                flowPoints);
        net.getNetModel().edit(flowMap, null, null, null);
    }


    public static void scrollNetToShowCells(NetGraph net, Object[] cells) {
        final int ELEMENT_BUFFER = 10;

        if (net.getFrame() == null) {
            return;
        }

        Rectangle bufferedCellBounds = (Rectangle) net.getCellBounds(cells).getBounds().clone();
        bufferedCellBounds.grow(ELEMENT_BUFFER, ELEMENT_BUFFER);
        ((JViewport) net.getParent()).scrollRectToVisible(bufferedCellBounds);
    }

    /**
     * A convenience method that takes all the supplied views and
     * returns their underlying cells in an object array.
     * @param views
     * @return
     */
    private static Object[] getCellsOfViews(CellView[] views) {
        Object[] cells = new Object[views.length];

        int i = 0;
        for(CellView view: views) {
            cells[i++] = view.getCell();
        }
        return cells;
    }


    public static YAWLVertex getVertexFromCell(Object cell) {
        if (cell instanceof VertexContainer) {
            cell = ((VertexContainer) cell).getVertex();
        }
        if (cell instanceof YAWLVertex) {
            return (YAWLVertex) cell;
        }
        if (cell instanceof Decorator) {
            return ((Decorator) cell).getTask();
        }
        return null;
    }

    private static Condition getConditionFromCell(Object cell) {
        YAWLVertex vertex = getVertexFromCell(cell);
        return vertex instanceof Condition ? (Condition) vertex : null;
    }

    public static InputCondition getInputConditionFromCell(Object cell) {
        Condition condition = getConditionFromCell(cell);
        return condition instanceof InputCondition ? (InputCondition) condition : null;
    }

    public static OutputCondition getOutputConditionFromCell(Object cell) {
        Condition condition = getConditionFromCell(cell);
        return condition instanceof OutputCondition ? (OutputCondition) condition : null;
    }

    public static YAWLTask getTaskFromCell(Object cell) {
        YAWLVertex vertex = getVertexFromCell(cell);
        return vertex instanceof YAWLTask ? (YAWLTask) vertex : null;
    }

    public static YAWLAtomicTask getAtomicTaskFromCell(Object cell) {
        YAWLTask task = getTaskFromCell(cell);
        return task instanceof YAWLAtomicTask ? (YAWLAtomicTask) task : null;
    }

    public static YAWLCompositeTask getCompositeTaskFromCell(Object cell) {
        YAWLTask task = getTaskFromCell(cell);
        return task instanceof YAWLCompositeTask ? (YAWLCompositeTask) task : null;
    }

    public static YAWLFlowRelation getFlowRelationFromCell(Object cell) {
        return cell instanceof YAWLFlowRelation ? (YAWLFlowRelation) cell : null;
    }



    public static void prettifyLoopingFlow(NetGraph graph, YAWLFlowRelation flow,
                                           EdgeView flowView, CellView sourceView) {
        if (! flow.connectsTaskToItself()) return;

        YAWLTask sourceTask = flow.getSourceTask();
        Point2D.Double sourcePoint = new Point2D.Double(flowView.getPoint(0).getX(),
                flowView.getPoint(0).getY());
        Point2D.Double targetPoint = new Point2D.Double(flowView.getPoint(1).getX(),
                flowView.getPoint(1).getY());

        adjustPortLocation(sourcePoint,
                sourceTask.getSplitDecorator().getCardinalPosition());
        adjustPortLocation(targetPoint,
                sourceTask.getJoinDecorator().getCardinalPosition());

        flowView.addPoint(1, sourcePoint);
        flowView.addPoint(2, targetPoint);

        Point2D.Double cornerPoint = adjustForAdjacentDecorators(
                sourceTask, sourcePoint, targetPoint);
        if (cornerPoint != null) {
            flowView.addPoint(2, cornerPoint);
        }

        if (sourceTask.hasHorizontallyAlignedDecorators()) {
            double adjustedY = sourceView.getBounds().getY() - FLOW_SPACER;
            Point2D.Double sourceCornerPoint = new Point2D.Double(
                    sourcePoint.getX(), adjustedY);
            Point2D.Double targetCornerPoint = new Point2D.Double(
                    targetPoint.getX(), adjustedY);
            flowView.addPoint(2, sourceCornerPoint);
            flowView.addPoint(3, targetCornerPoint);
        }

        if (sourceTask.hasVerticallyAlignedDecorators()) {
            double adjustedX = sourceView.getBounds().getX() - FLOW_SPACER;
            Point2D.Double sourceCornerPoint = new Point2D.Double(
                    adjustedX, sourcePoint.getY());
            Point2D.Double targetCornerPoint = new Point2D.Double(
                    adjustedX, targetPoint.getY());
            flowView.addPoint(2, sourceCornerPoint);
            flowView.addPoint(3, targetCornerPoint);
        }
        NetCellUtilities.applyViewChange(graph, flowView);
    }



    private static void adjustPortLocation(Point2D.Double portPoint, int splitPosition) {
        double x = portPoint.getX();
        double y = portPoint.getY();
        switch(splitPosition) {
            case Decorator.TOP    : y -= FLOW_SPACER; break;
            case Decorator.BOTTOM : y += FLOW_SPACER; break;
            case Decorator.LEFT   : x -= FLOW_SPACER; break;
            case Decorator.RIGHT  : x += FLOW_SPACER; break;
        }
        portPoint.setLocation(x, y);
    }


    private static Point2D.Double adjustForAdjacentDecorators(YAWLTask sourceTask,
                                                              Point2D.Double sourcePoint,
                                                              Point2D.Double targetPoint) {
        double minX = Math.min(sourcePoint.x, targetPoint.x);
        double minY = Math.min(sourcePoint.y, targetPoint.y);
        double maxX = Math.max(sourcePoint.x, targetPoint.x);
        double maxY = Math.max(sourcePoint.y, targetPoint.y);

        if (sourceTask.hasTopLeftAdjacentDecorators()) {
            return new Point2D.Double(minX, minY);
        }
        if (sourceTask.hasTopRightAdjacentDecorators()) {
            return new Point2D.Double(maxX, minY);
        }
        if (sourceTask.hasBottomRightAdjacentDecorators()) {
            return new Point2D.Double(maxX, maxY);
        }
        if (sourceTask.hasBottomLeftAdjacentDecorators()) {
            return new Point2D.Double(minX, maxY);
        }
        return null;
    }

}
