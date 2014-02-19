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

package org.yawlfoundation.yawl.editor.ui.elements.view;

import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexRenderer;
import org.yawlfoundation.yawl.editor.ui.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.plugin.YPluginHandler;
import org.yawlfoundation.yawl.editor.ui.util.IconList;
import org.yawlfoundation.yawl.elements.YDecomposition;

import javax.swing.*;
import java.awt.*;

abstract class YAWLVertexRenderer extends VertexRenderer {

    protected static final int CONFIGURED_TASK_STOKE_WIDTH = 4;

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        boolean tmp = selected;
        if (isOpaque()) {
            g2.setColor(super.getBackground());
            fillVertex(g2, getSize());
            g2.setColor(super.getForeground());
        }
        try {
            setBorder(null);
            setOpaque(false);
            selected = false;
            YPluginHandler.getInstance().preCellRender(g2, view);
            drawIcon(g2, getSize());
//            setStroke(g2);
            drawVertex(g2, getSize());
        }
        finally {
            selected = tmp;
        }
        if (bordercolor != null) {
            g2.setStroke(new BasicStroke(1));
            g2.setColor(bordercolor);
            drawIcon(g2, getSize());
            drawVertex(g2, getSize());
        }
        if (selected) {
            g2.setStroke(GraphConstants.SELECTION_STROKE);
            g2.setColor(highlightColor);
            drawIcon(g2, getSize());
            drawVertex(g2, getSize());
        }

        g2.setStroke(new BasicStroke(1));
        if (view.getCell() instanceof YAWLTask) {
            YAWLTask task = (YAWLTask) view.getCell();
            if (task.hasCancellationSetMembers()) {
                drawCancelSetMarker(g2, getSize());
            }
            if (isAutomatedTask(task)) {
                drawAutomatedMarker(g2, getSize());
                if (hasCodelet(task)) {
                    drawCodeletMarker(g2, getSize());
                }
            }
            if (task instanceof AtomicTask) {
                if (((AtomicTask) task).hasTimerEnabled()) {
                    drawTimerMarker(g2, getSize());
                }
            }
        }
    }

    protected void drawIcon(Graphics graphics, Dimension size) {
        if (view.getCell() instanceof YAWLTask) {
            YAWLTask task = (YAWLTask) view.getCell();
            String iconPath = task.getIconPath();
            if (iconPath != null) {
                Icon icon = IconList.getInstance().getIcon(
                        ((YAWLTask) view.getCell()).getIconPath());
                if (icon != null) {
                    icon.paintIcon(null, graphics,
                            getIconHorizontalOffset(size, icon),
                            getIconVerticalOffset(size,icon)
                    );
                }
            }
        }
    }

    protected int getIconHorizontalOffset(Dimension size, Icon icon) {
        return (size.width - icon.getIconWidth())/2;
    }

    protected int getIconVerticalOffset(Dimension size, Icon icon) {
        return (size.height - icon.getIconHeight())/2;
    }

//    protected void setStroke(Graphics2D g2) {
//        if ((view.getCell() instanceof YAWLTask) && ((YAWLTask) view.getCell()).isConfigurable()) {
//            g2.setStroke(new BasicStroke(CONFIGURED_TASK_STOKE_WIDTH));
//        }
//    }

    // these indicator marker graphics are designed to occupy 25% of the width of
    // a task, and 25% of the height, across the top of the task

    protected void drawCancelSetMarker(Graphics2D graphics, Dimension size) {
        int height = getMarkerHeight(size);
        graphics.setColor(Color.red);
        graphics.fillOval(Math.round(3 * size.width/4) - 2, 1, height, height);
    }

    protected void drawTimerMarker(Graphics2D graphics, Dimension size) {
        int height = getMarkerHeight(size);
        int centre = height/2 + 1;
        graphics.setColor(Color.white);
        graphics.fillOval(1, 1, height, height);
        graphics.setColor(Color.black);
        graphics.drawOval(1, 1, height, height);
        graphics.drawLine(centre, 1, centre, centre);
        graphics.drawLine(centre, centre, height + 1, centre);
    }

    protected void drawAutomatedMarker(Graphics2D graphics, Dimension size) {
        int height = getMarkerHeight(size);
        int midWidth = Math.round(size.width/2);
        int eighthwidth = Math.round(size.width/8);
        graphics.setColor(Color.black);
        int[] x = { midWidth - eighthwidth, midWidth - eighthwidth, midWidth + eighthwidth };
        int[] y = { 2, height, height/2 + 1 };
        graphics.drawPolygon(x, y, 3);
    }

    protected void drawCodeletMarker(Graphics2D graphics, Dimension size) {
        int height = getMarkerHeight(size);
        int midWidth = Math.round(size.width/2);
        int eighthwidth = Math.round(size.width/8);
        graphics.setColor(Color.green.darker());
        int[] x = { midWidth - eighthwidth, midWidth - eighthwidth, midWidth + eighthwidth };
        int[] y = { 2, height, height/2 + 1 };
        graphics.fillPolygon(x, y, 3);
    }

    private int getMarkerHeight(Dimension size) {
        return Math.round(size.height/4);
    }

    private boolean isAutomatedTask(YAWLTask task) {
        YDecomposition decomp = task.getDecomposition();
        return (decomp != null) && (! decomp.requiresResourcingDecisions());
    }

    private boolean hasCodelet(YAWLTask task) {
        YDecomposition decomp = task.getDecomposition();
        return decomp != null && decomp.getCodelet() != null;
    }


    abstract protected void fillVertex(Graphics graphics, Dimension size);

    abstract protected void drawVertex(Graphics graphics, Dimension size);
}
