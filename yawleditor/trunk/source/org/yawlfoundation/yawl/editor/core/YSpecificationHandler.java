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

package org.yawlfoundation.yawl.editor.core;

import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandler;
import org.yawlfoundation.yawl.editor.core.controlflow.YControlFlowHandlerException;
import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.editor.core.exception.IllegalIdentifierException;
import org.yawlfoundation.yawl.editor.core.layout.YLayout;
import org.yawlfoundation.yawl.editor.core.layout.YLayoutParseException;
import org.yawlfoundation.yawl.editor.core.resourcing.YResourceHandler;
import org.yawlfoundation.yawl.editor.core.util.FileOperations;
import org.yawlfoundation.yawl.editor.core.util.FileSaveOptions;
import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMetaData;

import java.io.IOException;
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
    private final FileOperations _fileOps;

    private final YControlFlowHandler _controlFlowHandler;
    private final YDataHandler _dataHandler;
    private final YResourceHandler _resourceHandler;

    public static final String DEFAULT_TYPE_DEFINITION =
            "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n\n</xs:schema>";


    public YSpecificationHandler() {
        _fileOps = new FileOperations();
        _dataHandler = new YDataHandler();
        _controlFlowHandler = new YControlFlowHandler();
        _resourceHandler = new YResourceHandler();
    }


    public YSpecification newSpecification()
            throws YControlFlowHandlerException, YSyntaxException {
        _specification = _fileOps.newSpecification();
        setUniqueID();
        addAuthor(System.getProperty("user.name"));
        setDescription("No description provided");
        _dataHandler.setSpecification(_specification);
        setSchema(DEFAULT_TYPE_DEFINITION);
        _controlFlowHandler.setSpecification(_specification);
        _controlFlowHandler.createRootNet("Net");
        _resourceHandler.setSpecification(_specification);
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

    public String checkID(String id) throws IllegalIdentifierException {
        return _controlFlowHandler.checkID(id);
    }

    public boolean isValidXMLIdentifier(String id) {
        return _controlFlowHandler.isValidXMLIdentifier(id);
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


    public void setURI(String uri) throws IllegalIdentifierException {
        if (isValidXMLIdentifier(uri)) {
            _specification.setURI(uri);
        }
        else throw new IllegalIdentifierException("Invalid or Empty Specification URI");
    }

    public String getURI() { return _specification.getURI(); }


    public void setTitle(String title) {
        getMetaData().setTitle(title);
    }

    public String getTitle() {
        return getMetaData().getTitle();
    }

    public void setDescription(String desc) {
        getMetaData().setDescription(desc);
    }

    public String getDescription() {
        return getMetaData().getDescription();
    }

    public void setAuthors(List<String> authors) {
        getMetaData().setCreators(authors);
    }

    public List<String> getAuthors() {
        return getMetaData().getCreators();
    }

    public void addAuthor(String author) {
        if (author != null) {
            List<String> authors = getAuthors();
            authors.add(author);
            getMetaData().setCreators(authors);
        }
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


    public void load(String specXML, String layoutXML)
            throws IOException, YLayoutParseException {
        initHandlers(_fileOps.load(specXML, layoutXML));
    }


    public void load(String fileName) throws IOException {
        initHandlers(_fileOps.load(fileName));
    }


    public void save() throws IOException {
        save((FileSaveOptions) null);          // save with default options
    }


    public void save(FileSaveOptions saveOptions) throws IOException {
        _fileOps.save(saveOptions);
        resetModified();
    }


    public void save(YLayout layout) throws IOException {
        save(layout, null);
    }

    public void save(YLayout layout, FileSaveOptions saveOptions) throws IOException {
        setLayout(layout);
        save(saveOptions);
    }


    public void saveAs(String file) throws IOException {
        saveAs(file, (FileSaveOptions) null);
    }

    public void saveAs(String file, FileSaveOptions saveOptions) throws IOException {
        _fileOps.saveAs(file, getMetaData(), saveOptions);
        resetModified();
    }

    public void saveAs(String file, YLayout layout) throws IOException {
        saveAs(file, layout, null);
    }

    public void saveAs(String file, YLayout layout, FileSaveOptions saveOptions)
            throws IOException {
        setLayout(layout);
        saveAs(file, saveOptions);
    }

    public void close() { _fileOps.close(); }

    public String getFileName() { return _fileOps.getFileName(); }


    public void setLayout(YLayout layout) { _fileOps.setLayout(layout); }

    public YLayout getLayout() { return _fileOps.getLayout(); }


    private String generateSpecificationIdentifier() {
         return "UID_" + UUID.randomUUID().toString();
    }


    private void setModified() { _modified = true; }

    private void resetModified() { _modified = false; }

    public boolean isModified() { return _modified; }


    public YSpecificationID getID() {
        return _specification.getSpecificationID();
    }

    private void initHandlers(YSpecification loaded) {
        if (loaded != null) {
            _specification = loaded;
            _dataHandler.setSpecification(loaded);
            _controlFlowHandler.setSpecification(loaded);
            _resourceHandler.setSpecification(loaded);
        }
    }

}
