package org.yawlfoundation.yawl.editor.core.controlflow;

import org.apache.xerces.util.XMLChar;
import org.yawlfoundation.yawl.editor.core.exception.IllegalIdentifierException;
import org.yawlfoundation.yawl.editor.core.identity.ElementIdentifiers;
import org.yawlfoundation.yawl.elements.*;

import java.util.*;

/**
 * @author Michael Adams
 * @date 31/08/12
 */
public class YControlFlowHandler {

    private YSpecification _specification;
    private ElementIdentifiers _identifiers;


    public YControlFlowHandler() { _identifiers = new ElementIdentifiers(); }

    public YControlFlowHandler(YSpecification specification) {
        this();
        setSpecification(specification);
    }


    public void setSpecification(YSpecification specification) {
        _specification = specification;
        _identifiers.load(specification);
    }

    public YSpecification getSpecification() { return _specification; }


    /*** Net CRUD ***/

    public int getNetCount() {
        return getNets().size();
    }

    public Set<String> getDecompositionIds() {
        Set<String> ids = new HashSet<String>();
        if (_specification != null) {
            for (YDecomposition decomposition : _specification.getDecompositions()) {
                ids.add(decomposition.getID());
            }
        }
        return ids;
    }


    public Set<YNet> getNets() {
        Set<YNet> nets = new HashSet<YNet>();
        if (_specification != null) {
            for (YDecomposition decomposition : _specification.getDecompositions()) {
                if (decomposition instanceof YNet) nets.add((YNet) decomposition);
            }
        }
        return nets;
    }


    public Set<YNet> getSubNets() {
        if (_specification != null) {
            Set<YNet> nets = getNets();
            nets.remove(getRootNet());
            return nets;
        }
        return Collections.emptySet();
    }


    public YNet createRootNet(String netName)
            throws YControlFlowHandlerException, IllegalIdentifierException {
        if (_specification == null) raise("No specification is loaded");
        YNet root = addNet(netName);
        setRootNet(root);
        return root;
    }

    public YNet getRootNet() { return _specification.getRootNet(); }

    public void setRootNet(YNet net) { _specification.setRootNet(net); }

    public YNet addNet(String netName)
            throws YControlFlowHandlerException, IllegalIdentifierException {
        if (_specification == null) raise("No specification is loaded");
        YNet net = createNet(netName);
        _specification.addDecomposition(net);
        return net;
    }

    public String addNet(YNet net) {
        String uniqueID = checkID(net.getID());
        if (! uniqueID.equals(net.getID())) net.setID(uniqueID);
        _specification.addDecomposition(net);
        return uniqueID;
    }

    public YNet getNet(String netName) {
        YDecomposition decomposition = _specification.getDecomposition(netName);
        return (decomposition instanceof YNet) ? (YNet) decomposition : null;
    }

    public YNet removeNet(String netID) throws YControlFlowHandlerException {
        YNet net = getNet(netID);
        if (net != null) {
            if (net.equals(_specification.getRootNet())) {
                raise("Removing the root net is not allowed");
            }
            return (YNet) _specification.removeDecomposition(netID);
        }
        return null;
    }

    private YNet createNet(String netName) throws IllegalIdentifierException {
        if (netName == null) netName = "Net";
        YNet net = new YNet(checkID(netName), _specification);
        net.setInputCondition(new YInputCondition(checkID("InputCondition"), net));
        net.setOutputCondition(new YOutputCondition(checkID("OutputCondition"), net));
        return net;
    }


    /*** task decomposition CRUD ***/

    public YAWLServiceGateway addTaskDecomposition(String name)
            throws IllegalIdentifierException {
        YAWLServiceGateway gateway = new YAWLServiceGateway(checkID(name), _specification);
        _specification.addDecomposition(gateway);
        return gateway;
    }


    public void addTaskDecomposition(YAWLServiceGateway decomposition) {
        _specification.addDecomposition(decomposition);
    }


    public YAWLServiceGateway getTaskDecomposition(String name) {
        YDecomposition decomposition = _specification.getDecomposition(name);
        return (decomposition instanceof YAWLServiceGateway) ?
                (YAWLServiceGateway) decomposition : null;
    }


    public List<YAWLServiceGateway> getTaskDecompositions() {
        List<YAWLServiceGateway> decompositionList = new ArrayList<YAWLServiceGateway>();
        for (YDecomposition decomposition : _specification.getDecompositions()) {
             if (decomposition instanceof YAWLServiceGateway) {
                 decompositionList.add((YAWLServiceGateway) decomposition);
             }
        }
        return decompositionList;
    }


    public YAWLServiceGateway removeTaskDecomposition(String name) {
        return (YAWLServiceGateway) _specification.removeDecomposition(name);
    }


    public void removeOrphanTaskDecompositions() {
        Set<YDecomposition> orphans = new HashSet<YDecomposition>();
        Set<YAtomicTask> allTasks = getAllAtomicTasks();
        for (YDecomposition decomposition : getTaskDecompositions()) {
            if (isOrphan(decomposition, allTasks)) {
                orphans.add(decomposition);
            }
        }
        for (YDecomposition orphan : orphans) {
            _specification.removeDecomposition(orphan.getID());
        }
    }

     /*** net elements CRUD ***/

    public YCondition addCondition(String netID, String id)
            throws IllegalIdentifierException {
        YNet net = getNet(netID);
        if (net != null) {
            YCondition condition = new YCondition(checkID(id), net);
            net.addNetElement(condition);
            return condition;
        }
        return null;
    }

    public YAtomicTask addAtomicTask(String netID, String id)
            throws IllegalIdentifierException {
        YNet net = getNet(netID);
        if (net != null) {
            YAtomicTask task = new YAtomicTask(checkID(id), YTask._AND, YTask._XOR, net);
            net.addNetElement(task);
            return task;
        }
        return null;
    }


    public YAtomicTask addMultipleInstanceAtomicTask(String netID, String id)
            throws IllegalIdentifierException {
        YAtomicTask task = addAtomicTask(netID, id);
        setMultiInstance(task);
        return task;
    }


    public YCompositeTask addCompositeTask(String netID, String id)
            throws IllegalIdentifierException {
        YNet net = getNet(netID);
        if (net != null) {
            YCompositeTask task = new YCompositeTask(
                    checkID(id), YTask._AND, YTask._XOR, net);
            net.addNetElement(task);
            return task;
        }
        return null;
    }


    public YCompositeTask addMultipleInstanceCompositeTask(String netID, String id)
            throws IllegalIdentifierException {
        YCompositeTask task = addCompositeTask(netID, id);
        setMultiInstance(task);
        return task;
    }


    public YCompoundFlow addFlow(String netID, String sourceID, String targetID) {
        YNet net = getNet(netID);
        return net == null ? null : new YCompoundFlow(getNetElement(netID, sourceID),
                       getNetElement(netID, targetID));
     }


    public YExternalNetElement getNetElement(String netID, String id) {
        if (id == null) return null;
        YNet net = getNet(netID);
        return (net != null) ? net.getNetElement(id) : null;
    }


    public YCondition getCondition(String netID, String id) {
        YNet net = getNet(netID);
        if (net != null) {
            YExternalNetElement element = getNetElement(netID, id);
            return (element instanceof YCondition) ? (YCondition) element : null;
        }
        return null;
    }


    public YInputCondition getInputCondition(String netID) {
        YNet net = getNet(netID);
        return (net != null) ? net.getInputCondition() : null;
    }


    public YOutputCondition getOutputCondition(String netID) {
        YNet net = getNet(netID);
        return (net != null) ? net.getOutputCondition() : null;
    }


    public YTask getTask(String netID, String id) {
        YNet net = getNet(netID);
        if (net != null) {
            YExternalNetElement element = getNetElement(netID, id);
            return (element instanceof YTask) ? (YTask) element : null;
        }
        return null;
    }


    public YAtomicTask getAtomicTask(String netID, String id) {
        YTask task = getTask(netID, id);
        return (task instanceof YAtomicTask) ? (YAtomicTask) task : null;
    }


    public YCompositeTask getCompositeTask(String netID, String id) {
        YTask task = getTask(netID, id);
        return (task instanceof YCompositeTask) ? (YCompositeTask) task : null;
    }


    public YCompoundFlow getFlow(String netID, String sourceID, String targetID) {
        YCompoundFlow flow = null;
        YNet net = getNet(netID);
        if (net != null) {
            YExternalNetElement source = getNetElement(netID, sourceID);
            YExternalNetElement target = getNetElement(netID, targetID);
            if (source == null || target == null) {
                return null;
            }

            // if flow connects two tasks, get the implicit condition between them too
            if ((source instanceof YTask) && (target instanceof YTask)) {
                String implicitID = "c{" + source.getID() + "_" + target.getID() + "}";
                YCondition implicit = getCondition(netID, implicitID);
                YFlow incoming = getFlow(source, implicit);
                YFlow outgoing = getFlow(implicit, target);
                flow = new YCompoundFlow(incoming, implicit, outgoing);
            }
            else flow = new YCompoundFlow(getFlow(source, target));
        }
        return flow;
    }


    public void removeNetElement(String netID, YExternalNetElement element) {
        YNet net = getNet(netID);
        if (net != null) net.removeNetElement(element);
    }


    public YCondition removeCondition(String netID, String id) {
        YCondition condition = getCondition(netID, id);
        if (condition != null) removeNetElement(netID, condition);
        return condition;
    }


    public YTask removeTask(String netID, String id) {
        YTask task = getTask(netID, id);
        if (task != null) removeNetElement(netID, task);
        return task;
    }


    public YAtomicTask removeAtomicTask(String netID, String id) {
        YAtomicTask task = getAtomicTask(netID, id);
        if (task != null) removeNetElement(netID, task);
        return task;
    }


    public YCompositeTask removeCompositeTask(String netID, String id) {
        YCompositeTask task = getCompositeTask(netID, id);
        if (task != null) removeNetElement(netID, task);
        return task;
    }


    public YCompoundFlow removeFlow(String netID, String sourceID, String targetID) {
        YCompoundFlow flow = getFlow(netID, sourceID, targetID);
        if (flow != null) {
            flow.detach();
        }
        return flow;
    }


    public ElementIdentifiers getIdentifiers() { return _identifiers; }

    public void rationaliseIdentifiers() {
        _identifiers.rationaliseIfRequired(_specification);
    }

    public String checkID(String id) throws IllegalIdentifierException {
        if (! isValidXMLIdentifier(id)) {
            throw new IllegalIdentifierException("Illegal XML identifier: '" + id + "'");
        }
        return _identifiers.getIdentifier(id).toString();
    }


    public boolean isValidXMLIdentifier(String id) {
        return ! (id == null || id.toLowerCase().startsWith("xml")
                || ! XMLChar.isValidName(id));
    }


    private void setMultiInstance(YTask task) {
        if (task != null) {
            task.setUpMultipleInstanceAttributes("1", "2", "1",
                    YMultiInstanceAttributes.CREATION_MODE_STATIC);
        }
    }


    private YFlow getFlow(YExternalNetElement source, YExternalNetElement target) {
        if (! (source == null || target == null)) {
            return source.getPostsetFlow(target);
        }
        return null;
    }


    private Set<YAtomicTask> getAllAtomicTasks() {
         Set<YAtomicTask> taskSet = new HashSet<YAtomicTask>();
         for (YNet net : getNets()) {
             for (YTask task : net.getNetTasks()) {
                 if (task instanceof YAtomicTask) {
                     taskSet.add((YAtomicTask) task);
                 }
             }
         }
         return taskSet;
     }

     private boolean isOrphan(YDecomposition decomposition, Set<YAtomicTask> allTasks) {
         for (YAtomicTask task : allTasks) {
             YDecomposition decompOfTask = task.getDecompositionPrototype();
             if ((decompOfTask != null) && (decompOfTask == decomposition)) {
                 return false;
             }
         }
         return true;    // no task references the decomposition
     }


    private void raise(String msg) throws YControlFlowHandlerException {
        throw new YControlFlowHandlerException(msg);
    }

}
