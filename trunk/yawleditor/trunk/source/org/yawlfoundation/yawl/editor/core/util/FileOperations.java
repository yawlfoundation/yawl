package org.yawlfoundation.yawl.editor.core.util;

import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @author Michael Adams
 * @date 4/06/12
 */
public class FileOperations {

    private String _layoutXML;
    private String _fileName;
    private FileSaveOptions _saveOptions;
    private YSpecVersion _prevVersion;
    private YSpecification _specification;


    public FileOperations() {
        setFileSaveOptions(new FileSaveOptions());
    }

    public void setFileSaveOptions(FileSaveOptions options) {
        _saveOptions = options;
    }

    public FileSaveOptions getFileSaveOptions() { return _saveOptions; }

    public String getFileName() { return _fileName; }


    public YSpecification load(String file) throws IOException {
        String specXML = FileUtil.load(file);

        try {
            List<YSpecification> specifications =
                    YMarshal.unmarshalSpecifications(specXML, false);
            _fileName = file;
            _specification = specifications.get(0);
            _layoutXML = unmarshalLayout(specXML);
        }
        catch (YSyntaxException yse) {
            throw new IOException(yse.getMessage());
        }
        return _specification;
    }


    public void save() throws IOException {
        save(_saveOptions);          // save with default options
    }


    public void save(FileSaveOptions saveOptions) throws IOException {
        if (saveOptions.autoIncVersion()) incVersion();
        _specification.setVersion(YSchemaVersion.defaultVersion());
        String specXML = getSpecificationXML();
        if (saveOptions.backupOnSave()) backup();
        if (saveOptions.versioningOnSave()) savePrevVersion();
        appendLayout(specXML);
        FileUtil.write(_fileName, specXML);
    }


    public void saveAs(String file, YMetaData metaData) throws IOException {
        saveAs(file, metaData, _saveOptions);
    }

    public void saveAs(String file, YMetaData metaData, FileSaveOptions saveOptions)
            throws IOException {
        _fileName = file;
        metaData.setUniqueID(generateSpecificationIdentifier());
        save(saveOptions);
    }

    public void close() {

    }


    private String unmarshalLayout(String xml) {
        XNode specNode = new XNodeParser().parse(xml);
        if (specNode != null) {
            XNode layoutNode = specNode.getChild("layout");
            return layoutNode != null ? layoutNode.toString() : "";
        }
        return "";
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

}

