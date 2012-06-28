package org.yawlfoundation.yawl.editor.core.resourcing;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidParticipantReference;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidRoleReference;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 14/06/12
 */
public class TaskResources {

    private YAtomicTask _task;
    private YNet _net;
    private ResourceParameters _resources;

    public TaskResources(YAtomicTask task) {
        _task = task;
        _net = task.getNet();
        _resources = new ResourceParameters(_task);
    }


    public YAtomicTask getTask() { return _task; }


    protected void primeTask() {
        _task.setResourcingXML(_resources.toXML());
    }


    public boolean setOfferInitiator(int initiator) {
        return _resources.getOffer().setInitiator(initiator);
    }

    public void addParticipant(String id) {
        _resources.getOffer().addParticipant(id);
    }

    public Set<Participant> getParticipants() {
        return _resources.getOffer().getParticipants();
    }

    public void removeParticipant(String id) {
        _resources.getOffer().removeParticipant(id);
    }

    public void clearParticipants() {
        _resources.getOffer().clearParticipants();
    }


    public void addRole(String id) {
        _resources.getOffer().addRole(id);
    }

    public Set<Role> getRoles() {
        return _resources.getOffer().getRoles();
    }

    public void removeRole(String id) {
        _resources.getOffer().removeRole(id);
    }

    public void clearRoles() {
        _resources.getOffer().clearRoles();
    }


    public void addDynParam(String name, DynParam.Refers refers) {
        _resources.getOffer().addDynParam(name, refers);
    }

    public void addDynParam(DynParam param) {
        _resources.getOffer().addDynParam(param);
    }

    public Set<DynParam> getDynParams() {
        return _resources.getOffer().getDynParams();
    }

    public void removeDynParam(String name) {
        _resources.getOffer().removeDynParam(name);
    }

    public void clearDynParams() {
        _resources.getOffer().clearDynParams();
    }

    public void addFilter(AbstractFilter f) {
        _resources.getOffer().addFilter(f);
    }

    public Set<AbstractFilter> getFilters() {
        return _resources.getOffer().getFilters();
    }

    public void removeFilter(String name) {
        _resources.getOffer().removeFilter(name);
    }

    public void clearFilters() {
        _resources.getOffer().clearFilters();
    }

    public void addConstraint(AbstractConstraint c) {
        _resources.getOffer().addConstraint(c);
    }

    public Set<AbstractConstraint> getConstraints() {
        return _resources.getOffer().getConstraints();
    }

    public void removeConstraint(String name) {
        _resources.getOffer().removeConstraint(name);
    }

    public void clearConstraints() {
        _resources.getOffer().clearConstraints();
    }

    public void setFamiliarTask(String taskID) {
        _resources.getOffer().setFamiliarParticipantTask(taskID);
    }

    public String getFamiliarTask() {
        return _resources.getOffer().getFamiliarParticipantTask();
    }

    public void clearFamiliarTask() {
        _resources.getOffer().clearFamiliarParticipantTask();
    }


    public boolean setAllocateInitiator(int initiator) {
        return _resources.getAllocate().setInitiator(initiator);
    }

    public void setAllocator(AbstractAllocator allocator) {
        _resources.getAllocate().setAllocator(allocator);
    }

    public AbstractAllocator getAllocator() {
        return _resources.getAllocate().getAllocator();
    }

    public void clearAllocator() {
        _resources.getAllocate().clearAllocator();
    }


    public boolean setStartInitiator(int initiator) {
        return _resources.getStart().setInitiator(initiator);
    }


    public Set<InvalidReference> validate() {
        if (! YConnector.isResourceConnected()) return Collections.emptySet();

        Set<InvalidReference> invalidRefs = new HashSet<InvalidReference>();
        List<String> pids = YConnector.getParticipantIDs();
        for (Participant p : getParticipants()) {
            if (! pids.contains(p.getID())) {
                invalidRefs.add(new InvalidParticipantReference(_net, _task, p.getID()));
            }
        }

        List<String> rids = YConnector.getRoleIDs();
        for (Role r : getRoles()) {
            if (! rids.contains(r.getID())) {
                invalidRefs.add(new InvalidRoleReference(_net, _task, r.getID()));
            }
        }

        // todo: other stuff

        return invalidRefs;
    }




}
