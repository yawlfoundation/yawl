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

package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class VertexContainer extends DefaultGraphCell implements YAWLCell {


    public VertexContainer() {
        initialize();
    }

    private void initialize() {
        HashMap map = new HashMap();
        GraphConstants.setOpaque(map, false);
        GraphConstants.setSizeable(map, true);
        GraphConstants.setChildrenSelectable(map, true);
        getAttributes().applyMap(map);
    }

    public boolean isRemovable() {
        return getVertex() == null || getVertex().isRemovable();
    }

    public boolean isCopyable() {
        return getVertex() == null || getVertex().isCopyable();
    }

    public boolean acceptsIncomingFlows() {
        return true;
    }

    public boolean generatesOutgoingFlows() {
        return true;
    }

    public String getToolTipText() {
        StringBuilder tooltipText = null;
        boolean joinTextAdded = false;

        if (getVertex() != null) {
            tooltipText = new StringBuilder();
            tooltipText.append("<html><body>");
            tooltipText.append(getVertex().getInnerToolTipText());

            if (getVertex() instanceof YAWLTask) {
                YAWLTask task = (YAWLTask) getVertex();
                if (task.getDecomposition() != null) {
                    tooltipText.append("&nbsp;<b>Decomposition: </b>");
                    tooltipText.append(task.getDecomposition().getID());
                    tooltipText.append("&nbsp;<p>");
                }
            }

            if (getJoinDecorator() != null || getSplitDecorator() != null) {
                tooltipText.append("&nbsp;<b>Decorator(s):</b> ");

                if (getJoinDecorator() != null) {
                    if (getJoinDecorator().toString() != null) {
                        tooltipText.replace(tooltipText.length() - 1, tooltipText.length()-1, ": ");
                        tooltipText.append(getJoinDecorator().toString());
                    }
                    joinTextAdded = true;
                }

                if (getSplitDecorator() != null) {
                    if (getSplitDecorator().toString() != null) {
                        if (!joinTextAdded) {
                            tooltipText.replace(tooltipText.length() - 1, tooltipText.length()-1, ": ");
                            tooltipText.append(getSplitDecorator().toString());
                        } else {
                            tooltipText.append(", ").append(getSplitDecorator().toString());
                        }
                    }
                }
                tooltipText.append("&nbsp;<p>");
            }
            tooltipText.append("</body></html>");
        }
        return tooltipText != null ? tooltipText.toString() : null;
    }


    public YAWLVertex getVertex() {
        for (Object o : getChildren()) {
            if (o instanceof YAWLVertex) {
                return (YAWLVertex) o;
            }
        }
        return null;
    }

    public VertexLabel getLabel() {
        for (Object o : getChildren()) {
            if (o instanceof VertexLabel) {
                return (VertexLabel) o;
            }
        }
        return null;
    }

    public JoinDecorator getJoinDecorator() {
        for (Object o : getChildren()) {
            if (o instanceof JoinDecorator) {
                return (JoinDecorator) o;
            }
        }
        return null;
    }

    public Set<YAWLFlowRelation> getOutgoingFlows() {
        if (getSplitDecorator() != null) {
            return getSplitDecorator().getFlows();
        }
        return getVertex().getOutgoingFlows();
    }

    public HashSet getIncomingFlows() {
        if (getJoinDecorator() != null) {
            return getJoinDecorator().getFlows();
        }
        return getVertex().getIncomingFlows();
    }


    public SplitDecorator getSplitDecorator() {
        for (Object o : getChildren()) {
            if (o instanceof SplitDecorator) {
                return (SplitDecorator) o;
            }
        }
        return null;
    }


    public void setBounds(Rectangle2D bounds) {
        HashMap map = new HashMap();
        GraphConstants.setBounds(map, bounds);
         getAttributes().applyMap(map);
    }


    public Rectangle2D getBounds() {
        return GraphConstants.getBounds(getAttributes());
    }
}
