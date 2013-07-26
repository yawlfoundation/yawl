/*
 * Created on 28/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.GraphConstants;

import java.util.HashMap;

public class YAWLFlowRelation extends DefaultEdge
        implements YAWLCell, Comparable<YAWLFlowRelation>, Cloneable {

    private boolean available = true;

    private int priority;
    private String predicate;

    public YAWLFlowRelation() {
        super();
        buildContent();
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

        setPredicate("true()");
        setPriority(0);
    }

    public boolean connectsTwoTasks() {
        return isTaskPort(this.getSource()) &&
                isTaskPort(this.getTarget());
    }

    private boolean isTaskPort(Object port) {
        YAWLPort yawlPort = (YAWLPort) port;
        return (port != null) &&
                (yawlPort.getParent() instanceof YAWLTask ||
                        yawlPort.getParent() instanceof Decorator);
    }

    public boolean isRemovable() {
        return true;
    }

    public boolean isCopyable() {
        YAWLPort sourcePort = (YAWLPort) this.getSource();
        YAWLPort targetPort = (YAWLPort) this.getTarget();

        return (! isBroken()) &&
                ((YAWLCell) sourcePort.getParent()).isCopyable() &&
                ((YAWLCell) targetPort.getParent()).isCopyable();
    }

    public boolean isBroken() {
        YAWLPort sourcePort = (YAWLPort) this.getSource();
        YAWLPort targetPort = (YAWLPort) this.getTarget();
        return sourcePort == null || targetPort == null;
    }

    public boolean generatesOutgoingFlows() {
        return false;
    }

    public boolean acceptsIncomingFlows() {
        return false;
    }


    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public void incrementPriority() {
        setPriority(getPriority()+1);
    }

    public void decrementPriority() {
        setPriority(getPriority()-1);
    }

    public int compareTo(YAWLFlowRelation other) throws ClassCastException {
        return getPriority() - other.getPriority();
    }


    public String getSourceLabel() {
        if (getSource() == null) {
            return "";
        }
        Object source = ((YAWLPort)getSource()).getParent();
        if (source instanceof YAWLVertex) {
            return ((YAWLVertex)source).getLabel();
        }
        if (source instanceof SplitDecorator) {
            return ((SplitDecorator)source).getTask().getLabel();
        }
        return "";
    }


    public String getSourceID() {
        YAWLVertex source = getSourceVertex();
        if (source != null) {
            return source.getID();
        }
        return null;
    }


    public String getTargetID() {
        YAWLVertex target = getTargetVertex();
        if (target != null) {
            return target.getID();
        }
        return null;
    }

    public String getTargetLabel() {
        if (getTarget() == null) {
            return "";
        }
        Object target = ((YAWLPort)getTarget()).getParent();
        if (target instanceof YAWLVertex) {
            return ((YAWLVertex)target).getLabel();
        }
        if (target instanceof JoinDecorator) {
            return ((JoinDecorator)target).getTask().getLabel();
        }
        return "";
    }

    public boolean isDefaultFlow() {
        if (hasOrSplitAsSource() || hasXorSplitAsSource()) {
            SplitDecorator decorator = (SplitDecorator) ((YAWLPort)getSource()).getParent();
            if (decorator.getFlowsInPriorityOrder().last().equals(this)) {
                return true;
            }
        }
        return false;
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
        return !(getSourceTask() == null || getTargetTask() == null) &&
                getSourceTask().equals(getTargetTask());
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

    public boolean connectsElements() {
        return getSource() != null && getTarget() != null;
    }

    public YAWLVertex getSourceVertex() {
        if (getSource() == null) {
            return null;
        }
        return getVertexFrom(
                ((YAWLPort)getSource()).getParent()
        );
    }

    public YAWLVertex getTargetVertex() {
        if (getTarget() == null) {
            return null;
        }
        return getVertexFrom(
                ((YAWLPort)getTarget()).getParent()
        );
    }

    private YAWLVertex getVertexFrom(Object cell) {
        assert cell != null : "null YAWLCell passed to getVertexFrom()";

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
