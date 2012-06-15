package org.yawlfoundation.yawl.editor.core.resourcing;

import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import java.util.Set;

/**
 * @author Michael Adams
 * @date 14/06/12
 */
public class TaskResources {

    private YAtomicTask _task;
    private ResourceParameters _resources;

    public TaskResources(YAtomicTask task) {
        _task = task;
        _resources = new ResourceParameters(_task);
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


//    public Set<String> validate() {
//        Map<String, Participant> connectedParticipants = YConnector.getParticipantMap();
//
//    }


}
