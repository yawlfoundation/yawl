/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.core.util;

import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.core.layout.YNetLayout;
import org.yawlfoundation.yawl.editor.core.layout.YTaskLayout;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.unmarshal.YMetaData;
import org.yawlfoundation.yawl.util.StringUtil;

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
    private String _prevVersion;
    private YSpecification _specification;
    private LayoutHandler _layoutHandler;


    public FileOperations() {
        setFileSaveOptions(new FileSaveOptions());
    }


    public void setFileSaveOptions(FileSaveOptions options) { _saveOptions = options; }

    public FileSaveOptions getFileSaveOptions() { return _saveOptions; }


    public String getFileName() { return _fileName; }



    public YSpecification load(String file) throws IOException {
        String specXML = FileUtil.load(file);

        try {
            List<YSpecification> specifications =
                    YMarshal.unmarshalSpecifications(specXML, false);
            _fileName = file;
            _specification = specifications.get(0);
            _layoutHandler = new LayoutHandler(_specification, specXML);
            nullifyUnlabelledTaskNames();
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
        if (StringUtil.isNullOrEmpty(_fileName) || ! _fileName.equals(file)) {
            _fileName = file;
            metaData.setUniqueID(generateSpecificationIdentifier());
            metaData.setVersion(new YSpecVersion(0,1));
            saveOptions.setAutoIncVersion(false);    // don't auto inc on save as
        }
        save(saveOptions);
    }

    public void close() {
        reset();
    }

    public YSpecification newSpecification() {
        reset();
        _specification = new YSpecification();
        _specification.setMetaData(new YMetaData());
        _specification.setURI("New_Specification");
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
        YSpecVersion version = getSpecVersion();
        _prevVersion = version.toString();
        version.minorIncrement();
    }


    private boolean isVersionChanged() {
        return (_prevVersion != null) &&
               (! getSpecVersion().toString().equals(_prevVersion));
    }


    private void backup() throws IOException {
        FileUtil.backup(_fileName, _fileName + ".bak");
    }


    private YSpecVersion getSpecVersion() {
        return _specification.getMetaData().getVersion();
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


    /**
     * When a specification is parsed, if a task has no name it is given a name
     * derived from its ID for runtime purposes. For design time purposes, no name
     * means a label has not been set for the task. Thus, any derived name given to a
     * task has to be removed for an editor. We can test whether there is layout info
     * for a task's label and, if not, unset the task's name.
     */
    private void nullifyUnlabelledTaskNames() {
        for (YNetLayout netLayout : getLayout().getNetLayouts().values()) {
            for (YTaskLayout taskLayout : netLayout.getTaskLayouts()) {
                if (! taskLayout.hasLabel()) {
                    nullifyTaskName(netLayout.getID(), taskLayout.getID());
                }
            }
        }
    }


    private void nullifyTaskName(String netID, String taskID) {
        YNet net = (YNet) _specification.getDecomposition(netID);
        if (net != null) {
            YTask task = (YTask) net.getNetElement(taskID);
            if (task != null) {
                task.setName(null);
            }
        }
    }

}

