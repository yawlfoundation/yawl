package org.yawlfoundation.yawl.editor.core;

import org.yawlfoundation.yawl.editor.core.resourcing.ResourceParameters;
import org.yawlfoundation.yawl.editor.core.util.FileOperations;
import org.yawlfoundation.yawl.editor.core.util.FileSaveOptions;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Michael Adams
 * @date 5/09/11
 */
public class YEditorSpecification {

    private YSpecification _specification;
    private boolean _modified;
    private List<String> _identifiers;
    private FileOperations _fileOps;

    private Map<String, ResourceParameters> _taskResources;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    public YEditorSpecification() {
        _specification = new YSpecification();
        _fileOps = new FileOperations();
        _identifiers = new ArrayList<String>();
        _taskResources = new Hashtable<String, ResourceParameters>();
    }

    public YEditorSpecification(String name) {
        _specification = new YSpecification(name);
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

    public YNet createSubNet(String netName) {
        YNet net = createNet(netName);
        _specification.addDecomposition(net);
        return net;
    }

    public YDecomposition getSubNet(String netName) {
        return _specification.getDecomposition(netName);
    }

    private YNet createNet(String netName) {
        YNet net = new YNet(netName, _specification);
        net.setInputCondition(new YInputCondition(checkID("InputCondition"), net));
        net.setOutputCondition(new YOutputCondition(checkID("OutputCondition"), net));
        return net;
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
            buildResources();
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


    public void setLayoutXML(String xml) { _fileOps.setLayoutXML(xml); }

    public String getLayoutXML() { return _fileOps.getLayoutXML(); }

    private String checkID(String id) {
        if (! _identifiers.contains(id)) return id;

        int i = 0;
        String newID = id;
        while (_identifiers.contains(id)) {
            newID = id + i++;
        }
        _identifiers.add(newID);
        return newID;
    }

    private String generateSpecificationIdentifier() {
         return "UID_" + UUID.randomUUID().toString();
    }


    private void buildResources() {
        for (YDecomposition decomposition : _specification.getDecompositions()) {
            if (decomposition instanceof YNet) {
                for (YTask task : ((YNet) decomposition).getNetTasks()) {
                    if (task instanceof YAtomicTask) {
                        _taskResources.put(task.getID(),
                                new ResourceParameters((YAtomicTask) task));
                    }
                }
            }
        }
    }


    private void setModified() { _modified = true; }

    private void resetModified() { _modified = false; }

    public boolean isModified() { return _modified; }


    public YSpecificationID getID() {
        return _specification.getSpecificationID();
    }

}
