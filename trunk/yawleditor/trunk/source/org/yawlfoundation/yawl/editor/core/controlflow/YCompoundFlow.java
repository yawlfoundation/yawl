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

package org.yawlfoundation.yawl.editor.core.controlflow;

import org.yawlfoundation.yawl.elements.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Michael Adams
 * @date 31/07/13
 */
public class YCompoundFlow implements Comparable<YCompoundFlow> {

    private YFlow _flowFromSource;
    private YCondition _implicitCondition;
    private YFlow _flowIntoTarget;

    private static final String DEFAULT_PREDICATE = "true()";

    public YCompoundFlow() { }

    public YCompoundFlow(YFlow only) {
        this(only, null, null);
    }

    public YCompoundFlow(YFlow fromSource, YCondition implicit, YFlow intoTarget) {
        _flowFromSource = fromSource;
        _implicitCondition = implicit;
        _flowIntoTarget = intoTarget;
    }

    public YCompoundFlow(YExternalNetElement source, YExternalNetElement target) {

        // if flow connects two tasks, insert an implicit condition between them
        if ((source instanceof YTask) && (target instanceof YTask)) {
            _implicitCondition = makeImplicitCondition(source, target);
            _flowFromSource = makeFlow(source, _implicitCondition);
            _flowIntoTarget = makeFlow(_implicitCondition, target);
        }
        else _flowFromSource = makeFlow(source, target);

        if (source instanceof YTask) setInitialProperties(this);
    }

    public YCompoundFlow moveSourceTo(YExternalNetElement newSource) {
        return moveTo(newSource, getTarget());
    }

    public YCompoundFlow moveTargetTo(YExternalNetElement newTarget) {
        return moveTo(getSource(), newTarget);
    }


    public YFlow getSourceFlow() {
        return _flowFromSource;
    }

    public YFlow getTargetFlow() {
        return isCompound() ? _flowIntoTarget : _flowFromSource;
    }

    public YCondition getImplicitCondition() {
        return _implicitCondition;
    }


    public YFlow getSimpleFlow() {
        return isCompound() ? null : _flowFromSource;
    }

    public YExternalNetElement getSource() {
        return _flowFromSource.getPriorElement();
    }

    public YExternalNetElement getTarget() {
        return isCompound() ? _flowIntoTarget.getNextElement() :
                _flowFromSource.getNextElement();
    }

    public YNet getNet() {
        YExternalNetElement element = getSource();
        if (element == null) element = getTarget();
        return element != null ? element.getNet() : null;
    }

    public void setOrdering(Integer order) {
        _flowFromSource.setEvalOrdering(order);
    }

    public Integer getOrdering() {
        return _flowFromSource.getEvalOrdering();
    }

    public void setPredicate(String predicate) {
        _flowFromSource.setXpathPredicate(predicate);
    }

    public String getPredicate() {
        return _flowFromSource.getXpathPredicate();
    }

    public void setIsDefaultFlow(boolean def) { _flowFromSource.setIsDefaultFlow(def); }

    public boolean isDefaultFlow() { return _flowFromSource.isDefaultFlow(); }

    public void setDocumentation(String doco) {
        _flowFromSource.setDocumentation(doco);
    }

    public String getDocumentation() { return _flowFromSource.getDocumentation(); }

    public boolean isCompound() {
        return _implicitCondition != null;
    }


    public boolean hasSourceSplitType(int splitType) {
        YExternalNetElement source = getSource();
        return (source instanceof YTask) && ((YTask) source).getSplitType() == splitType;
    }

    public boolean hasTargetJoinType(int joinType) {
        YExternalNetElement target = getTarget();
        return (target instanceof YTask) && ((YTask) target).getJoinType() == joinType;
    }

    public boolean isOnlySourceFlow() {
        return getSource().getPostsetFlows().size() == 1;
    }

    public boolean isOnlyTargetFlow() {
        return getTarget().getPresetFlows().size() == 1;
    }

    public YFlow getCompositeFlow() {
        return isCompound() ? new YFlow(getSource(), getTarget()) : getSimpleFlow();
    }

    public boolean isLoop() {
        YExternalNetElement source = getSource();
        YExternalNetElement target = getTarget();
        return source != null && target != null && source == target;
    }


    public void detach() {
        detach(getSourceFlow(), getTargetFlow());
        if (isCompound()) removeImplicitCondition();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        YCompoundFlow other = (YCompoundFlow) o;
        if (! _flowFromSource.equals(other._flowFromSource)) return false;
        if (isCompound()) {
            if (!_implicitCondition.equals(other._implicitCondition)) return false;
            if (!_flowIntoTarget.equals(other._flowIntoTarget)) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * _flowFromSource.hashCode();
        if (isCompound()) {
            result +=  _implicitCondition.hashCode() + _flowIntoTarget.hashCode();
        }
        return result;
    }

    public int compareTo(YCompoundFlow other) {
        return getOrdering() - other.getOrdering();
    }


    public String rationaliseImplicitConditionID() {
        String newID = null;
        if (isCompound()) {
            newID = makeImplicitConditionID(getSource().getID(), getTarget().getID());
            if (! newID.equals(_implicitCondition.getID())) {
                _implicitCondition.setID(newID);
            }
        }
        return newID;
    }

    private void detach(YFlow sourceFlow, YFlow targetFlow) {
        if (sourceFlow == null || targetFlow == null) return;
        YExternalNetElement source = sourceFlow.getPriorElement();
        if (source != null) {
            source.removePostsetFlow(sourceFlow);
            if ((source instanceof YTask) && sourceFlow.isDefaultFlow()) {
                nominateDefaultFlow((YTask) source);
            }
        }

        YExternalNetElement target = targetFlow.getNextElement();
        if (target != null) target.removePresetFlow(targetFlow);
    }


    private void removeImplicitCondition() {
        if (_implicitCondition != null) {
            _implicitCondition.getNet().removeNetElement(_implicitCondition);
            _implicitCondition = null;
        }
    }


    private YFlow makeFlow(YExternalNetElement source, YExternalNetElement target) {
        YFlow flow = new YFlow(source, target);
        if (source != null) source.addPostset(flow);
        if (target != null) target.addPreset(flow);
        return flow;
    }


    private YCondition makeImplicitCondition(YExternalNetElement source,
                                             YExternalNetElement target) {
        String id = makeImplicitConditionID(source.getID(), target.getID());
        YNet net = source.getNet();
        YCondition implicit = new YCondition(id, net);
        implicit.setImplicit(true);
        net.addNetElement(implicit);
        return implicit;
    }


    private String makeImplicitConditionID(String sourceID, String targetID) {
        return "c{" + sourceID + "_" + targetID + "}";
    }


    private YCompoundFlow moveTo(YExternalNetElement source, YExternalNetElement target) {
        if (isCompound()) {
            removeImplicitCondition();
        }
        else {

            // current source or target is explicit condition
            getSource().removePostsetFlow(_flowFromSource);
            getTarget().removePresetFlow(_flowFromSource);
        }
        YCompoundFlow flow = new YCompoundFlow(source, target);
        setPropertiesOnMove(flow);
        return flow;
    }


    private void setInitialProperties(YCompoundFlow flow) {
        YExternalNetElement source = flow.getSource();
        if (source instanceof YTask) {
            int splitType = ((YTask) source).getSplitType();

            // NOTE: a condition or an AND-split task already have the correct defaults
            if (splitType == YTask._XOR) {

                // a new flow at an XOR-split becomes the new default
                unsetDefaultFlow((YTask) source);
                flow.setIsDefaultFlow(true);
                rationaliseOrdering((YTask) source);
            }
            else if (splitType == YTask._OR) {
                flow.setIsDefaultFlow(! hasDefaultFlow((YTask) source));
                flow.setPredicate(DEFAULT_PREDICATE);
            }
        }
    }


    private void setPropertiesOnMove(YCompoundFlow flow) {
        flow.setDocumentation(this.getDocumentation());
        YExternalNetElement oldSource = this.getSource();
        YExternalNetElement newSource = flow.getSource();

        // if the flow has a new source, move its predicate to the new source
        if ((newSource instanceof YTask) && ! newSource.equals(oldSource)) {
            setInitialProperties(flow);
            int splitType = ((YTask) newSource).getSplitType();
            if ((splitType == YTask._XOR && ! isDefaultFlow()) ||
                    splitType == YTask._OR) {
                movePredicate(flow, this.getPredicate());
           }
        }
    }


    private boolean hasDefaultFlow(YTask task) {
        for (YFlow flow : task.getPostsetFlows()) {
            if (flow.isDefaultFlow()) {
                return true;
            }
        }
        return false;
    }


    // only called for XOR splits
    private void unsetDefaultFlow(YTask task) {
        if (task.getSplitType() == YTask._XOR) {
            for (YFlow flow : task.getPostsetFlows()) {
                if (flow.isDefaultFlow()) {
                    flow.setIsDefaultFlow(false);
                    flow.setXpathPredicate(DEFAULT_PREDICATE);
                }
            }
        }
    }


    private void rationaliseOrdering(YTask task) {
        if (task.getSplitType() == YTask._XOR) {
            List<YFlow> flowList = new ArrayList<YFlow>(task.getPostsetFlows());
            Collections.sort(flowList);
            for (int i = 0; i < flowList.size() - 1; i++) {   // last one is default
                flowList.get(i).setEvalOrdering(i);
            }
        }
    }


    private void movePredicate(YCompoundFlow flow, String predicate) {
        flow.setPredicate(predicate != null ? predicate : DEFAULT_PREDICATE);
    }


    private void nominateDefaultFlow(YTask task) {
        if (task.getSplitType() != YTask._AND) {
            YFlow nominatedFlow = null;
            int lowestPriority = -1;
            for (YFlow flow : task.getPostsetFlows()) {
                Integer ordering = flow.getEvalOrdering();
                if (ordering != null && lowestPriority < ordering) {
                    lowestPriority = flow.getEvalOrdering();
                    nominatedFlow = flow;
                }
            }
            if (nominatedFlow != null) {
                nominatedFlow.setIsDefaultFlow(true);

                // default XOR flow can't have a predicate
                if (task.getSplitType() == YTask._XOR) {
                    nominatedFlow.setXpathPredicate(null);
                }
            }
        }
    }

}
