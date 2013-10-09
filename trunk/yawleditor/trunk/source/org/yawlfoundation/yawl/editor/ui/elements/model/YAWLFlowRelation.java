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

import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.GraphConstants;
import org.yawlfoundation.yawl.editor.core.controlflow.YCompoundFlow;
import org.yawlfoundation.yawl.elements.YExternalNetElement;

import java.util.HashMap;

public class YAWLFlowRelation extends DefaultEdge
        implements YAWLCell, Comparable<YAWLFlowRelation>, Cloneable {

    private boolean available = true;
    private YCompoundFlow yCompoundFlow;

    public YAWLFlowRelation(YCompoundFlow flow) {
        super();
        setYFlow(flow);
        buildContent();
    }

    public void setYFlow(YCompoundFlow flow) {
        yCompoundFlow = flow;
    }

    public YCompoundFlow getYFlow() {
        return yCompoundFlow;
    }

    private void buildContent() {
        HashMap map = new HashMap();
        GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
        GraphConstants.setEndFill(map, true);
        GraphConstants.setLineStyle(map, GraphConstants.STYLE_ORTHOGONAL);
        GraphConstants.setBendable(map, false);
        GraphConstants.setEditable(map, true);
        GraphConstants.setDisconnectable(map, true);
        GraphConstants.setConnectable(map, true);
        getAttributes().applyMap(map);
    }

    public boolean connectsTwoTasks() {
        return yCompoundFlow.isCompound();
    }

    public boolean isRemovable() {
        return true;
    }

    public boolean isCopyable() {
        YAWLPort sourcePort = (YAWLPort) this.getSource();
        YAWLPort targetPort = (YAWLPort) this.getTarget();

        return sourcePort != null && targetPort != null &&
                ((YAWLCell) sourcePort.getParent()).isCopyable() &&
                ((YAWLCell) targetPort.getParent()).isCopyable();
    }

    public boolean isBroken() {
        return getSource() == null || getTarget() == null;
    }

    public boolean generatesOutgoingFlows() {
        return false;
    }

    public boolean acceptsIncomingFlows() {
        return false;
    }


    public void setPriority(Integer priority) {
        yCompoundFlow.setOrdering(priority);
    }

    public Integer getPriority() {
        return yCompoundFlow.getOrdering();
    }

    public String getPredicate() {
        return yCompoundFlow.getPredicate();
    }

    public void setPredicate(String predicate) {
        yCompoundFlow.setPredicate(predicate);
    }


    public boolean isDefaultFlow() {
        return yCompoundFlow.isDefaultFlow();
    }

    public void setIsDefaultFlow(boolean isDefault) {
        yCompoundFlow.setIsDefaultFlow(isDefault);
    }


    public int compareTo(YAWLFlowRelation other) {
        Integer myOrdering = getPriority();
        Integer otherOrdering = other.getPriority();
        if (myOrdering == null && otherOrdering == null) return 0;
        if (myOrdering == null) return -1;
        if (otherOrdering == null) return 1;
        return myOrdering - otherOrdering;
    }


    public String getSourceID() {
        return getElementID(yCompoundFlow.getSource());
    }

     public String getTargetID() {
         return getElementID(yCompoundFlow.getTarget());
    }


    public boolean hasOrSplitAsSource() {
        return hasSplitAsSource(Decorator.OR_TYPE);
    }

    public boolean hasXorSplitAsSource() {
        return hasSplitAsSource(Decorator.XOR_TYPE);
    }

    public boolean requiresPredicate() {
        return (hasOrSplitAsSource() || hasXorSplitAsSource());
    }

    public boolean hasSplitAsSource(int type) {
        if (getSource() == null) {
            return false;
        }

        Object source = ((YAWLPort)getSource()).getParent();
        if (source instanceof SplitDecorator) {
            SplitDecorator decorator = (SplitDecorator) source;
            if (decorator.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public boolean connectsTaskToItself() {
        return yCompoundFlow.isLoop();
    }

    public YAWLTask getSourceTask() {
        if (getSourceVertex() instanceof YAWLTask) {
            return (YAWLTask) getSourceVertex();
        }
        return null;
    }

    public YAWLTask getTargetTask() {
        if (getTargetVertex() instanceof YAWLTask) {
            return (YAWLTask) getTargetVertex();
        }
        return null;
    }


    public YAWLVertex getSourceVertex() {
        if (getSource() == null) {
            return null;
        }
        return getVertexFrom(((YAWLPort) getSource()).getParent());
    }

    public YAWLVertex getTargetVertex() {
        if (getTarget() == null) {
            return null;
        }
        return getVertexFrom(((YAWLPort) getTarget()).getParent());
    }

    private YAWLVertex getVertexFrom(Object cell) {
        if (cell instanceof Decorator) {
            Decorator cellAsDecorator = (Decorator) cell;
            return cellAsDecorator.getTask();
        }
        if (cell instanceof YAWLVertex) {
            return (YAWLVertex) cell;
        }
        return null;
    }

    public YAWLFlowRelation clone() {
        return (YAWLFlowRelation) super.clone();
    }


    public void detach() {
        YAWLTask source = getSourceTask();
        if (source != null) source.detachFlow(this);
        YAWLTask target = getTargetTask();
        if (target != null) target.detachFlow(this);
    }


    private String getElementID(YExternalNetElement element) {
         return element != null ? element.getID() : null;
     }


    /**
     * Created By Jingxin XU
     * @return
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Created By Jingxin XU
     * @param available
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }


}
