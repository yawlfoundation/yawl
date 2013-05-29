package org.yawlfoundation.yawl.editor.core;

import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandler;
import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.core.resourcing.TaskResources;
import org.yawlfoundation.yawl.editor.core.resourcing.YResourceHandler;
import org.yawlfoundation.yawl.editor.core.util.FileOperations;
import org.yawlfoundation.yawl.editor.core.util.FileSaveOptions;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Michael Adams
 * @date 5/09/11
 */
public class YSpecificationHandler {

    private YSpecification _specification;
    private boolean _modified;
    private FileOperations _fileOps;

    private YControlFlowHandler _controlFlowHandler;
    private YDataHandler _dataHandler;
    private YResourceHandler _resourceHandler;


    public YSpecificationHandler() {
        _fileOps = new FileOperations();
        _dataHandler = new YDataHandler();
        _controlFlowHandler = new YControlFlowHandler();
        _resourceHandler = new YResourceHandler();
    }


    public YSpecification newSpecification() {
        _specification = _fileOps.newSpecification();
        _dataHandler.setSpecification(_specification);
        _controlFlowHandler.setSpecification(_specification);
        _resourceHandler.setSpecification(_specification);
        addAuthor(System.getProperty("user.name"));
        setDescription("No description provided");
        return _specification;
    }


    public YSpecification getSpecification() { return _specification; }

    public String getSpecificationXML() throws IOException {
        return _fileOps.getSpecificationXML();
    }


    public boolean isLoaded() { return _specification != null; }


    public void setSchema(String schema) throws YSyntaxException {
        _specification.setSchema(schema);
        _dataHandler.setSchema(schema);
    }

    public String getSchema() { return _specification.getDataSchema(); }


    public YDataHandler getDataHandler() { return _dataHandler; }

    public YControlFlowHandler getControlFlowHandler() { return _controlFlowHandler; }

    public YResourceHandler getResourceHandler() { return _resourceHandler; }


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
            _dataHandler.setSpecification(loaded);
            _controlFlowHandler.setSpecification(loaded);
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


    public void save(YLayout layout) throws IOException {
        setLayout(layout);
        save();
    }

    public void save(YLayout layout, FileSaveOptions saveOptions) throws IOException {
        setLayout(layout);
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

    public void saveAs(String file, YLayout layout) throws IOException {
        setLayout(layout);
        _fileOps.saveAs(file, getMetaData());
        resetModified();
    }

    public void saveAs(String file, YLayout layout, FileSaveOptions saveOptions)
            throws IOException {
        setLayout(layout);
        _fileOps.saveAs(file, getMetaData(), saveOptions);
        resetModified();
    }

    public void close() { _fileOps.close(); }

    public String getFileName() { return _fileOps.getFileName(); }

    public void setFileName(String name) { _fileOps.setFileName(name); }  // temp for migration


    public void setLayout(YLayout layout) { _fileOps.setLayout(layout); }

    public YLayout getLayout() { return _fileOps.getLayout(); }


    private String generateSpecificationIdentifier() {
         return "UID_" + UUID.randomUUID().toString();
    }


    public void addTaskResources(TaskResources resources) {
        _fileOps.addTaskResources(resources);
    }


    public TaskResources getTaskResources(String netID, String taskID) {
        TaskResources resources = _fileOps.getTaskResources(netID, taskID);
        if (resources == null) {
            YAtomicTask task = _controlFlowHandler.getAtomicTask(netID, taskID);
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

}
