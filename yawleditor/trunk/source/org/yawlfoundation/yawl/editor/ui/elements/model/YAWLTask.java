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

import org.jgraph.graph.GraphConstants;
import org.yawlfoundation.yawl.editor.ui.net.CancellationSet;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YTask;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public abstract class YAWLTask extends YAWLVertex {

    private CancellationSet _cancellationSet;

    private String _iconPath;

    /**
     * This constructor is ONLY to be invoked when we are reconstructing a task
     * from saved state. Ports will not be created with this constructor, as they
     * are already part of the JGraph state-space.
     */
    public YAWLTask() {
        this(null, null);
    }

    /**
     * This constructor is to be invoked whenever we are creating a new task
     * from scratch. It also creates the correct ports needed for the vertex
     * as an intended side-effect.
     */
    public YAWLTask(Point2D startPoint) {
        this(startPoint, null);
    }

    public YAWLTask(Point2D startPoint, String iconPath) {
        super(startPoint);
        initialize(iconPath);
    }


    public void setTask(YTask task) { setYAWLElement(task); }

    public YTask getTask() { return (YTask) getYAWLElement(); }


    private void initialize(String iconPath) {
        setCancellationSet(new CancellationSet(this));
        setIconPath(iconPath);
    }

    public void setIconPath(String path) { _iconPath = path; }

    public String getIconPath() { return _iconPath; }


    public String getID() { return _yawlElement.getID(); }

    public void setID(String id) { _yawlElement.setID(id); }


    public String getName() { return _yawlElement.getName(); }

    public void setName(String name) { _yawlElement.setName(name); }


    public String getDocumentation() { return _yawlElement.getDocumentation(); }

    public void setDocumentation(String doco) { _yawlElement.setDocumentation(doco); }




    public int getSplitDecoratorPos() {
        if (getSplitDecorator() != null) {
            return getSplitDecorator().getCardinalPosition();
        }
        return getPositionOfOutgoingFlow();
    }

    public int getJoinDecoratorPos() {
        if (getJoinDecorator() != null) {
            return getJoinDecorator().getCardinalPosition();
        }
        return this.getPositionOfIncomingFlow();
    }

    public int hasJoinDecoratorAt() {
        return getJoinDecorator() != null ?
                getJoinDecorator().getCardinalPosition() : NOWHERE;
    }

    public int hasSplitDecoratorAt() {
        return getSplitDecorator() != null ?
                getSplitDecorator().getCardinalPosition() : NOWHERE;
    }

    public boolean hasSplitDecorator() {
        return getSplitDecorator() != null;
    }

    public boolean hasJoinDecorator() {
        return getJoinDecorator() != null;
    }

    public boolean hasDecoratorAtPosition(int position) {
        return decoratorTypeAtPosition(position) != Decorator.NO_TYPE;
    }

    public int decoratorTypeAtPosition(int position) {
        if (getJoinDecorator() != null &&
                getJoinDecorator().getCardinalPosition() == position) {
            return getJoinDecorator().getType();
        }
        if (getSplitDecorator() != null &&
                getSplitDecorator().getCardinalPosition() == position) {
            return getSplitDecorator().getType();
        }
        return Decorator.NO_TYPE;
    }

    public YAWLPort getDefaultSourcePort() {
        if (hasSplitDecorator()) {
            return getSplitDecorator().getDefaultPort();
        }
        return super.getDefaultSourcePort();
    }

    public YAWLPort getDefaultTargetPort() {
        if (hasJoinDecorator()) {
            return getJoinDecorator().getDefaultPort();
        }
        return super.getDefaultTargetPort();
    }

    public YAWLFlowRelation getOnlyOutgoingFlow() {
        if (getPositionOfOutgoingFlow() != NOWHERE) {
            return (YAWLFlowRelation)
                    (getPortAt(getPositionOfOutgoingFlow()).getEdges().toArray())[0];
        }
        return null;
    }

    public YAWLFlowRelation getOnlyIncomingFlow() {
        if (getPositionOfIncomingFlow() != NOWHERE) {

            return (YAWLFlowRelation)
                    (getPortAt(getPositionOfIncomingFlow()).getEdges().toArray())[0];
        }
        return null;
    }

    public JoinDecorator getJoinDecorator() {
        VertexContainer container = (VertexContainer) this.getParent();
        if (container == null) {
            return null;
        }
        return container.getJoinDecorator();
    }

    public SplitDecorator getSplitDecorator() {
        VertexContainer container = (VertexContainer) this.getParent();
        if (container == null) {
            return null;
        }
        return container.getSplitDecorator();
    }

    public Rectangle2D getBounds() {
        return GraphConstants.getBounds(getAttributes());
    }

    public Point2D getLocation() {
        return new Point2D.Double(getBounds().getX(), getBounds().getY());
    }

    public HashSet<YAWLFlowRelation> getIncomingFlows() {
        if (hasJoinDecorator()) {
            return getJoinDecorator().getFlows();
        }
        return super.getIncomingFlows();
    }


    public int getIncomingFlowCount() {
        return getJoinDecorator() != null ?
                getJoinDecorator().getFlowCount() : super.getIncomingFlows().size();
    }

    public Set<YAWLFlowRelation> getOutgoingFlows() {
        if (hasSplitDecorator()) {
            return getSplitDecorator().getFlows();
        }
        return super.getOutgoingFlows();
    }


    public int getOutgoingFlowCount() {
        return getSplitDecorator() != null ?
                getSplitDecorator().getFlowCount() : super.getOutgoingFlows().size();
    }

    public CancellationSet getCancellationSet() {
        return _cancellationSet;
    }

    public void setCancellationSet(CancellationSet set) {
        _cancellationSet = set;
    }

    public boolean hasCancellationSetMembers() {
        return getCancellationSet().hasMembers();
    }


    public void setDecomposition(YDecomposition decomposition) {
        ((YTask) _yawlElement).setDecompositionPrototype(decomposition);
    }

    public YDecomposition getDecomposition() {
        return ((YTask) _yawlElement).getDecompositionPrototype();
    }


    public void setCustomFormURL(String urlStr) throws MalformedURLException {
        setCustomFormURL(urlStr != null ? new URL(urlStr) : null);
    }

    public void setCustomFormURL(URL url) {
        ((YTask) _yawlElement).setCustomFormURI(url);
    }

    public URL getCustomFormURL() {
        return ((YTask) _yawlElement).getCustomFormURL();
    }

    public boolean hasBothDecorators() {
        return (hasJoinDecorator() && hasSplitDecorator());
    }

    public boolean hasTopLeftAdjacentDecorators() {
        return hasDecoratorPairAt(Decorator.LEFT, Decorator.TOP);
    }

    public boolean hasTopRightAdjacentDecorators() {
        return hasDecoratorPairAt(Decorator.TOP, Decorator.RIGHT);
    }

    public boolean hasBottomRightAdjacentDecorators() {
        return hasDecoratorPairAt(Decorator.RIGHT, Decorator.BOTTOM);
    }

    public boolean hasBottomLeftAdjacentDecorators() {
        return hasDecoratorPairAt(Decorator.LEFT, Decorator.BOTTOM);
    }

    public boolean hasVerticallyAlignedDecorators() {
        return hasDecoratorPairAt(Decorator.TOP, Decorator.BOTTOM);
    }

    public boolean hasHorizontallyAlignedDecorators() {
        return hasDecoratorPairAt(Decorator.LEFT, Decorator.RIGHT);
    }

    private boolean hasDecoratorPairAt(int first, int second) {
        return hasDecoratorAtPosition(first) &&
               hasDecoratorAtPosition(second);
    }

    public boolean hasNoSelfReferencingFlows() {
        HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
        if (! hasBothDecorators()) return true;

        flows.addAll(getSplitDecorator().getFlows());
        flows.addAll(getJoinDecorator().getFlows());
        for (YAWLFlowRelation flow : flows) {
            if (flow.connectsTaskToItself()) {
                return false;
            }
        }
        return true;
    }

    public void detachFlow(YAWLFlowRelation flow) {
        if (!(hasSplitDecorator() || hasJoinDecorator())) {
            super.detachFlow(flow);
        }
        if (hasSplitDecorator()) {
            getSplitDecorator().detachFlow(flow);
        }
        if (hasJoinDecorator()) {
            getJoinDecorator().detachFlow(flow);
        }
    }


    public Object clone() {
        YAWLTask clone = (YAWLTask) super.clone();

        CancellationSet clonedCancellationSet = (CancellationSet) getCancellationSet().clone();
        clone.setCancellationSet(clonedCancellationSet);
        clonedCancellationSet.setOwnerTask(clone);

        clone.setTask(((YTask) _yawlElement));
        clone.setDecomposition(getDecomposition());
        clone.setCustomFormURL(getCustomFormURL());
        clone.setIconPath(_iconPath);
        return clone;
    }

}
