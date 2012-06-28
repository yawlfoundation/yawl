package org.yawlfoundation.yawl.editor.core;

import org.yawlfoundation.yawl.editor.core.identity.ElementIdentifiers;
import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.core.resourcing.TaskResources;
import org.yawlfoundation.yawl.editor.core.util.FileOperations;
import org.yawlfoundation.yawl.editor.core.util.FileSaveOptions;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Michael Adams
 * @date 5/09/11
 */
public class YEditorSpecification {

    private YSpecification _specification;
    private boolean _modified;
    private ElementIdentifiers _identifiers;
    private FileOperations _fileOps;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    public YEditorSpecification() {
        _fileOps = new FileOperations();
        initialise();
    }


    public void initialise() {
        _specification = new YSpecification();
        _identifiers = new ElementIdentifiers();
        addAuthor(System.getProperty("user.name"));
        setDescription("No description provided");
        createRootNet("NewNet");
    }


    public YSpecification getSpecification() { return _specification; }

    public int getNetCount() {
        return _specification.getDecompositions().size();
    }


    public YNet createRootNet(String netName) {
        YNet root = createNet(netName);
        setRootNet(root);
        return root;
    }

    public YNet getRootNet() { return _specification.getRootNet(); }

    public void setRootNet(YNet net) { _specification.setRootNet(net); }

    public YNet addSubNet(String netName) {
        YNet net = createNet(netName);
        _specification.addDecomposition(net);
        return net;
    }

    public YNet getNet(String netName) {
        YDecomposition decomposition = _specification.getDecomposition(netName);
        return (decomposition instanceof YNet) ? (YNet) decomposition : null;
    }

    public YNet removeNet(String netID) {
        YNet net = getNet(netID);
        if (! (net == null || net.equals(_specification.getRootNet()))) {
            if (_specification.getDecompositions().remove(net)) {
                return net;
            }
        }
        return null;
    }

    private YNet createNet(String netName) {
        YNet net = new YNet(checkID(netName), _specification);
        net.setInputCondition(new YInputCondition(checkID("InputCondition"), net));
        net.setOutputCondition(new YOutputCondition(checkID("OutputCondition"), net));
        return net;
    }


    public YAWLServiceGateway addTaskDecomposition(String name) {
        YAWLServiceGateway gateway = new YAWLServiceGateway(checkID(name), _specification);
        _specification.addDecomposition(gateway);
        return gateway;
    }


    public YAWLServiceGateway getTaskDecomposition(String name) {
        YDecomposition decomposition = _specification.getDecomposition(name);
        return (decomposition instanceof YAWLServiceGateway) ?
                (YAWLServiceGateway) decomposition : null;
    }


    public YAWLServiceGateway removeTaskDecomposition(String name) {
        YAWLServiceGateway gateway = getTaskDecomposition(name);
        if (gateway != null) _specification.getDecompositions().remove(gateway);
        return gateway;
    }


    /******************************************************************************/
    // MetaData Settings //

    private YMetaData getMetaData() {
        YMetaData metaData = _specification.getMetaData();
        if (metaData == null) {
            metaData = new YMetaData();
            _specification.setMetaData(metaData);
        }
        return metaData;
    }


    public void setTitle(String title) {
        getMetaData().setTitle(JDOMUtil.encodeEscapes(title));
    }

    public String getTitle() {
        return JDOMUtil.decodeEscapes(getMetaData().getTitle());
    }

    public void setDescription(String desc) {
        getMetaData().setDescription(JDOMUtil.encodeEscapes(desc));
    }

    public String getDescription() {
        return JDOMUtil.decodeEscapes(getMetaData().getDescription());
    }

    public void setAuthors(List<String> authors) {
        if (authors != null) {
            List<String> encoded = new ArrayList<String>();
            for (String author : authors) {
                encoded.add(JDOMUtil.encodeEscapes(author));
            }
            authors = encoded;
        }
        getMetaData().setCreators(authors);
    }

    public List<String> getAuthors() {
        List<String> authors = getMetaData().getCreators();
        if (authors != null) {
            List<String> decoded = new ArrayList<String>();
            for (String author : authors) {
                decoded.add(JDOMUtil.decodeEscapes(author));
            }
            authors = decoded;
        }
        return authors;
    }

    public void addAuthor(String author) {
        if (author != null) getAuthors().add(author);
    }

    public void setVersion(YSpecVersion version) {
        if (version.toString().equals("0.0")) version.minorIncrement();
        getMetaData().setVersion(version);
    }

    public YSpecVersion getVersion() { return getMetaData().getVersion(); }

    public void setValidFrom(Date validFrom) { getMetaData().setValidFrom(validFrom); }

    public Date getValidFrom() { return getMetaData().getValidFrom(); }

    public void setValidUntil(Date validUntil) { getMetaData().setValidUntil(validUntil); }

    public Date getValidUntil() { return getMetaData().getValidUntil(); }

    public String getUniqueID() {
        String unique = getMetaData().getUniqueID();
        if (unique == null) {
            unique = generateSpecificationIdentifier();
            getMetaData().setUniqueID(unique);
        }
        return unique;
    }

    public void setUniqueID() {
        getMetaData().setUniqueID(generateSpecificationIdentifier());
    }


    /******************************************************************************/

    public void setFileSaveOptions(FileSaveOptions options) {
        _fileOps.setFileSaveOptions(options);
    }

    public FileSaveOptions getFileSaveOptions() {
        return _fileOps.getFileSaveOptions();
    }


    public void load(String file) throws IOException {
        YSpecification loaded = _fileOps.load(file);
        if (loaded != null) {
            _specification = loaded;
        }
    }


    public void save() throws IOException {
        _fileOps.save();          // save with default options
        resetModified();
    }


    public void save(FileSaveOptions saveOptions) throws IOException {
        _fileOps.save(saveOptions);
        resetModified();
    }


    public void saveAs(String file) throws IOException {
        _fileOps.saveAs(file, getMetaData());
        resetModified();
    }

    public void saveAs(String file, FileSaveOptions saveOptions) throws IOException {
        _fileOps.saveAs(file, getMetaData(), saveOptions);
        resetModified();
    }

    public void close() { }

    public String getFileName() { return _fileOps.getFileName(); }

    public void setFileName(String name) { _fileOps.setFileName(name); }  // temp for migration


    public void setLayout(YLayout layout) { _fileOps.setLayout(layout); }

    public YLayout getLayout() { return _fileOps.getLayout(); }

    private String checkID(String id) {
        return _identifiers.getIdentifier(id).toString();
    }

    private String generateSpecificationIdentifier() {
         return "UID_" + UUID.randomUUID().toString();
    }


    public void addTaskResources(TaskResources resources) {
        _fileOps.addTaskResources(resources);
    }


    public TaskResources getTaskResources(String netID, String taskID) {
        TaskResources resources = _fileOps.getTaskResources(netID, taskID);
        if (resources == null) {
            YAtomicTask task = getAtomicTask(netID, taskID);
            if (task != null) {
                resources = new TaskResources(task);
                addTaskResources(resources);
            }
        }
        return resources;
    }

    public TaskResources removeTaskResources(String netID, String taskID) {
        return _fileOps.removeTaskResources(netID, taskID);
    }


    private void setModified() { _modified = true; }

    private void resetModified() { _modified = false; }

    public boolean isModified() { return _modified; }


    public YSpecificationID getID() {
        return _specification.getSpecificationID();
    }


    public YCondition addCondition(String netID, String id) {
        YNet net = getNet(netID);
        if (net != null) {
            YCondition condition = new YCondition(checkID(id), net);
            net.addNetElement(condition);
            return condition;
        }
        return null;
    }

    public YAtomicTask addAtomicTask(String netID, String id) {
        YNet net = getNet(netID);
        if (net != null) {
            YAtomicTask task = new YAtomicTask(checkID(id), YTask._AND, YTask._XOR, net);
            net.addNetElement(task);
            return task;
        }
        return null;
    }


    public YCompositeTask addCompositeTask(String netID, String id) {
        YNet net = getNet(netID);
        if (net != null) {
            YCompositeTask task =
                    new YCompositeTask(checkID(id), YTask._AND, YTask._XOR, net);
            net.addNetElement(task);
            return task;
        }
        return null;
    }


    public YFlow addFlow(String netID, String sourceID, String targetID) {
        YNet net = getNet(netID);
        if (net != null) {
            YExternalNetElement source = getNetElement(netID, sourceID);
            YExternalNetElement target = getNetElement(netID, targetID);
            YFlow flow = new YFlow(source, target);
            if (source != null) source.addPostset(flow);
            if (target != null) target.addPreset(flow);
            return flow;
        }
        return null;
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


    public YFlow getFlow(String netID, String sourceID, String targetID) {
        YNet net = getNet(netID);
        if (net != null) {
            YExternalNetElement source = getNetElement(netID, sourceID);
            YExternalNetElement target = getNetElement(netID, targetID);
            if (! (source == null || target == null)) {
                return source.getPostsetFlow(target);
            }
        }
        return null;
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


    public YFlow removeFlow(String netID, String sourceID, String targetID) {
        YFlow flow = getFlow(netID, sourceID, targetID);
        if (flow != null) {
            YExternalNetElement source = getNetElement(netID, sourceID);
            YExternalNetElement target = getNetElement(netID, targetID);
            if (source != null) source.removePostsetFlow(flow);
            if (target != null) target.removePresetFlow(flow);
        }
        return flow;
    }


    public YParameter addInputParameter(String decompositionID) {
        return addParameter(decompositionID, YParameter._INPUT_PARAM_TYPE);
    }


    public YParameter addOutputParameter(String decompositionID) {
        return addParameter(decompositionID, YParameter._OUTPUT_PARAM_TYPE);
    }


    private YParameter addParameter(String decompositionID, int type) {
        YAWLServiceGateway gateway = getTaskDecomposition(decompositionID);
        return (gateway != null) ? new YParameter(gateway, type) : null;
    }

}
