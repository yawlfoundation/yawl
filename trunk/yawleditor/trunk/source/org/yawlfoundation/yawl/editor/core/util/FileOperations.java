package org.yawlfoundation.yawl.editor.core.util;

import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.unmarshal.YMetaData;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @author Michael Adams
 * @date 4/06/12
 */
public class FileOperations {

    private String _fileName;
    private FileSaveOptions _saveOptions;
    private YSpecVersion _prevVersion;
    private YSpecification _specification;
    private LayoutHandler _layoutHandler;


    public FileOperations() {
        setFileSaveOptions(new FileSaveOptions());
    }


    public void setFileSaveOptions(FileSaveOptions options) { _saveOptions = options; }

    public FileSaveOptions getFileSaveOptions() { return _saveOptions; }


    public String getFileName() { return _fileName; }

    public void setFileName(String name) { _fileName = name; }  // temp for migration


    public YSpecification load(String file) throws IOException {
        String specXML = FileUtil.load(file);

        try {
            List<YSpecification> specifications =
                    YMarshal.unmarshalSpecifications(specXML, false);
            _fileName = file;
            _specification = specifications.get(0);
            _layoutHandler = new LayoutHandler(_specification, specXML);
        }
        catch (YSyntaxException yse) {
            throw new IOException(yse.getMessage());
        }
        return _specification;
    }


    public void save(FileSaveOptions saveOptions) throws IOException {
        if (saveOptions == null) saveOptions = _saveOptions;    // use defaults
        if (saveOptions.autoIncVersion()) incVersion();
        _specification.setVersion(YSchemaVersion.defaultVersion());
        String specXML = getSpecificationXML();
        if (saveOptions.backupOnSave()) backup();
        if (saveOptions.versioningOnSave()) savePrevVersion();
        specXML = _layoutHandler.appendLayoutXML(specXML);
        FileUtil.write(_fileName, specXML);
    }


    public void saveAs(String file, YMetaData metaData, FileSaveOptions saveOptions)
            throws IOException {
        _fileName = file;
        metaData.setUniqueID(generateSpecificationIdentifier());
        save(saveOptions);
    }

    public void close() {
        reset();
    }

    public YSpecification newSpecification() {
        reset();
        _specification = new YSpecification();
        _specification.setMetaData(new YMetaData());
        _layoutHandler = new LayoutHandler(_specification, null);
        return _specification;
    }


    public String getSpecificationXML() throws IOException {
        String specXML = YMarshal.marshal(_specification);
        if (! successful(specXML)) {
            throw new IOException(getMarshalErrorMsg(specXML));
        }
        return specXML;
    }


    public YLayout getLayout() { return _layoutHandler.getLayout(); }

    public void setLayout(YLayout layout) { _layoutHandler.setLayout(layout); }


    private void reset() {
        _fileName = null;
        _prevVersion = null;
        _specification = null;
        _layoutHandler = null;
    }


    private boolean successful(String xml) {
        return ! (xml == null || xml.equals("null") || xml.startsWith("<fail"));
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


    private String generateSpecificationIdentifier() {
        return "UID_" + UUID.randomUUID().toString();
    }

}

