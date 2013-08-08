package org.yawlfoundation.yawl.editor.core.controlflow;

import org.yawlfoundation.yawl.elements.*;

/**
 * @author Michael Adams
 * @date 31/07/13
 */
public class YCompoundFlow {

    YFlow _flowFromSource;
    YCondition _implicitCondition;
    YFlow _flowIntoTarget;

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
    }

    public YCompoundFlow moveTo(YExternalNetElement source, YExternalNetElement target) {
        removeImplicitCondition();
        YCompoundFlow flow = new YCompoundFlow(source, target);
        flow.setOrdering(this.getOrdering());
        flow.setPredicate(this.getPredicate());
        flow.setIsDefaultFlow(this.isDefaultFlow());
        flow.setDocumentation(this.getDocumentation());
        return flow;
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

    public void setOrdering(Integer order) {
        _flowFromSource.setEvalOrdering(order);
    }

    public Integer getOrdering() {
        return _flowFromSource.getEvalOrdering();
    }

    public void setPredicate(String predicate) {
        _flowFromSource.setXpathPredicate(predicate);
        if (isCompound()) _flowIntoTarget.setXpathPredicate(predicate);
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

    private void detach(YFlow sourceFlow, YFlow targetFlow) {
        if (sourceFlow == null || targetFlow == null) return;
        YExternalNetElement source = sourceFlow.getPriorElement();
        if (source != null) source.removePostsetFlow(sourceFlow);
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
        String id = "c{" + source.getID() + "_" + target.getID() + "}";
        YNet net = source.getNet();
        YCondition implicit = new YCondition(id, net);
        implicit.setImplicit(true);
        net.addNetElement(implicit);
        return implicit;
    }

}
