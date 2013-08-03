package org.yawlfoundation.yawl.editor.core.controlflow;

import org.yawlfoundation.yawl.elements.*;

/**
 * @author Michael Adams
 * @date 31/07/13
 */
public class YCompoundFlow {

    YFlow _incoming;
    YCondition _implicit;
    YFlow _outgoing;

    public YCompoundFlow() { }

    public YCompoundFlow(YFlow only) {
        this(only, null, null);
    }

    public YCompoundFlow(YFlow incoming, YCondition implicit, YFlow outgoing) {
        _incoming = incoming;
        _implicit = implicit;
        _outgoing = outgoing;
    }

    public YCompoundFlow(YExternalNetElement source, YExternalNetElement target) {

        // if flow connects two tasks, insert an implicit condition between them
        if ((source instanceof YTask) && (target instanceof YTask)) {
            _implicit = makeImplicitCondition(source, target);
            _incoming = makeFlow(source, _implicit);
            _outgoing = makeFlow(_implicit, target);
        }
        else _incoming = makeFlow(source, target);
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

    public YFlow getIncomingFlow() {
        return getSourceFlow();
    }

    public YFlow getSourceFlow() {
        return _incoming;
    }

    public YFlow getTargetFlow() {
        return isCompound() ? _outgoing : _incoming;
    }

    public YCondition getImplicitCondition() {
        return _implicit;
    }

    public YFlow getOutgoingFlow() {
        return _outgoing;
    }

    public YFlow getSimpleFlow() {
        return isCompound() ? null : _incoming;
    }

    public YExternalNetElement getSource() {
        return _incoming.getPriorElement();
    }

    public YExternalNetElement getTarget() {
        return isCompound() ? _outgoing.getNextElement() : _incoming.getNextElement();
    }

    public void setOrdering(Integer order) {
        _incoming.setEvalOrdering(order);
    }

    public Integer getOrdering() {
        return _incoming.getEvalOrdering();
    }

    public void setPredicate(String predicate) {
        _incoming.setXpathPredicate(predicate);
        if (isCompound()) _outgoing.setXpathPredicate(predicate);
    }

    public String getPredicate() {
        return _incoming.getXpathPredicate();
    }

    public void setIsDefaultFlow(boolean def) { _incoming.setIsDefaultFlow(def); }

    public boolean isDefaultFlow() { return _incoming.isDefaultFlow(); }

    public void setDocumentation(String doco) {
        _incoming.setDocumentation(doco);
    }

    public String getDocumentation() { return _incoming.getDocumentation(); }

    public boolean isCompound() {
        return _implicit != null;
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


    public void detachSource() {
        YExternalNetElement source = getSource();
        if (source != null) source.removePostsetFlow(getSourceFlow());
        if (isCompound() && getTarget() == null) {
            removeImplicitCondition();
        }
    }

    public void detachTarget() {
        YExternalNetElement target = getTarget();
        if (target != null) target.removePresetFlow(getTargetFlow());
        if (isCompound() && getSource() == null) {
            removeImplicitCondition();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        YCompoundFlow other = (YCompoundFlow) o;
        if (! _incoming.equals(other._incoming)) return false;
        if (isCompound()) {
            if (!_implicit.equals(other._implicit)) return false;
            if (!_outgoing.equals(other._outgoing)) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * _incoming.hashCode();
        if (isCompound()) {
            result +=  _implicit.hashCode() + _outgoing.hashCode();
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
        if (_implicit != null) {
            _implicit.getNet().removeNetElement(_implicit);
            _implicit = null;
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
