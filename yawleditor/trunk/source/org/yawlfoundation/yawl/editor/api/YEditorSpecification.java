package org.yawlfoundation.yawl.editor.api;

import org.yawlfoundation.yawl.editor.api.util.FileSaveOptions;
import org.yawlfoundation.yawl.editor.api.util.FileUtil;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.File;
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
    private String _layoutXML;
    private String _fileName;
    private FileSaveOptions _saveOptions;
    private List<String> _identifiers;

    private YSpecVersion _prevVersion;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    public YEditorSpecification() {
        _specification = new YSpecification();
        _saveOptions = new FileSaveOptions();
        _identifiers = new ArrayList<String>();
    }

    public YEditorSpecification(String name) {
        _specification = new YSpecification(name);
    }


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
        _specification.setDecomposition(net);
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
        _saveOptions = options;
    }

    public FileSaveOptions getFileSaveOptions() { return _saveOptions; }


    public void load(String file) throws IOException {
        String specXML = FileUtil.load(file);

        try {
            List<YSpecification> specifications = YMarshal.unmarshalSpecifications(specXML, false);
            _fileName = file;
            _specification = specifications.get(0);
        }
        catch (YSyntaxException yse) {
            throw new IOException(yse.getMessage());
        }
    }


    public void save() throws IOException {
        if (_saveOptions.autoIncVersion()) incVersion();
        _specification.setVersion(YSchemaVersion.defaultVersion());
        String specXML = getSpecificationXML();
        if (_saveOptions.backupOnSave()) backup();
        if (_saveOptions.versioningOnSave()) savePrevVersion();
        appendLayout(specXML);
        FileUtil.write(_fileName, specXML);
        resetModified();
    }


    public void saveAs(String file) throws IOException {
        _fileName = file;
        getMetaData().setUniqueID(generateSpecificationIdentifier());
        save();
    }


    public void close() {

    }


    public void setLayoutXML(String xml) { _layoutXML = xml; }

    public String getLayoutXML() { return _layoutXML; }


    private String getSpecificationXML() throws IOException {
        String specXML = YMarshal.marshal(_specification);
        if (! successful(specXML)) {
            throw new IOException(getMarshalErrorMsg(specXML));
        }
        return specXML;
    }

    private boolean successful(String xml) {
        return (xml == null) || (xml.equals("null")) || xml.startsWith("<fail");
    }

    private String getMarshalErrorMsg(String xml) {
        String msg = "File save process resulted in a 'null' specification.\n File not created.\n";
        if ((xml != null) && (xml.startsWith("<fail"))) {
            int start = xml.indexOf('>') + 1;
            int len = xml.lastIndexOf('<') - start;
            msg += "\nDetail: " + xml.substring(start, len) + '\n';
        }
        return msg;
    }


    private void incVersion() {
        _prevVersion = _specification.getSpecificationID().getVersion();
        _specification.getSpecificationID().getVersion().minorIncrement();
    }


    private boolean isVersionChanged() {
        return (_prevVersion != null) &&
               (! _specification.getSpecificationID().getVersion().equals(_prevVersion));
    }


    private void backup() throws IOException {
        FileUtil.backup(_fileName, _fileName + ".bak");
    }


    private void savePrevVersion() throws IOException {
        if (isVersionChanged()) {
            String versionedFileName = String.format("%s.%s.yawl",
                        FileUtil.stripFileExtension(_fileName), _prevVersion);
            if (! new File(versionedFileName).exists()) {
                FileUtil.backup(_fileName, versionedFileName);
            }
        }
    }


    private String appendLayout(String specXML) {
        if (! ((specXML == null) || (_layoutXML == null))) {
            int closingTag = specXML.lastIndexOf("</");
            return specXML.substring(0, closingTag) +
                   _layoutXML +
                   specXML.substring(closingTag) ;    // insert layout info

        }
        return specXML;  // return unchanged
    }


    private String generateSpecificationIdentifier() {
        return "UID_" + UUID.randomUUID().toString();
    }


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

    /*****************************************************************************/

    public YSpecificationID getID() {
        return _specification.getSpecificationID();
    }





    private void setModified() { _modified = true; }

    private void resetModified() { _modified = false; }

    public boolean isModified() { return _modified; }

}
