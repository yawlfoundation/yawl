package org.yawlfoundation.yawl.editor.core.resourcing;

import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;

import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 31/08/12
 */
public class YResourceHandler {

    private YSpecification _specification;
    private ResourcesCache _resourcesCache;


    public YResourceHandler() {
        _resourcesCache = new ResourcesCache();
    }


    public YSpecification getSpecification() {
        return _specification;
    }

    public void setSpecification(YSpecification specification) {
        _specification = specification;
        parseResources();
    }

    public void resetCache() {
        ResourceDataSet.reset();
    }

    public Set<Participant> getParticipants() {
        return ResourceDataSet.getParticipants();
    }

    public Set<Role> getRoles() {
        return ResourceDataSet.getRoles();
    }

    public Set<NonHumanResource> getNonHumanResources() {
        return ResourceDataSet.getNonHumanResources();
    }

    public List<NonHumanCategory> getNonHumanResourceCategories() {
        return ResourceDataSet.getNonHumanResourceCategories();
    }

    public List<Capability> getCapabilities() {
        return ResourceDataSet.getCapabilities();
    }

    public List<Position> getPositions() {
        return ResourceDataSet.getPositions();
    }

    public List<OrgGroup> getOrgGroups() {
        return ResourceDataSet.getOrgGroups();
    }

    public TaskResourceSet getOrCreateTaskResources(String netID, String taskID) {
        TaskResourceSet resources = getTaskResources(netID, taskID);
        if (resources == null) {
            YAtomicTask task = getAtomicTask(netID, taskID);
            if (task != null) {
                resources = new TaskResourceSet(task);
                addTaskResources(resources);
            }
        }
        return resources;
    }


    public TaskResourceSet getTaskResources(String netID, String taskID) {
        return _resourcesCache.get(netID, taskID);
    }


    public void addTaskResources(TaskResourceSet resources) {
        _resourcesCache.add(resources);
    }

    public TaskResourceSet removeTaskResources(String netID, String taskID) {
        return _resourcesCache.remove(netID, taskID);
    }

    public Set<YAtomicTask> getAllPrecedingAtomicTasks(YAtomicTask task) {
        return new PresetWalker().getAtomicTasks(task);
    }

    public void generateXML() {
        _resourcesCache.generateXML();
    }

    public Set<InvalidReference> getInvalidReferences() {
        return _resourcesCache.getAllInvalidReferences();
    }


    private void parseResources() {
        _resourcesCache.clear();
        for (YDecomposition decomposition : _specification.getDecompositions()) {
            if (decomposition instanceof YNet) {
                for (YTask task : ((YNet) decomposition).getNetTasks()) {
                    if (task instanceof YAtomicTask) {
                        _resourcesCache.add(decomposition.getID(), task.getID(),
                                new TaskResourceSet((YAtomicTask) task));
                    }
                }
            }
        }
    }


    private YAtomicTask getAtomicTask(String netID, String taskID) {
        for (YDecomposition decomposition : _specification.getDecompositions()) {
            if ((decomposition instanceof YNet) && decomposition.getID().equals(netID)) {
                for (YTask task : ((YNet) decomposition).getNetTasks()) {
                    if ((task instanceof YAtomicTask) && task.getID().equals(taskID)) {
                        return (YAtomicTask) task;
                    }
                }
            }
        }
        return null;
    }

}
