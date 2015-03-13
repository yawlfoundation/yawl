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
import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
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
 * A YSpecificationHandler object manages a YAWL specification file, including
 * creation, population, update, loading and saving.
 * @author Michael Adams
 * @date 5/09/11
 */
public class YSpecificationHandler {

    private YSpecification _specification;
    private final FileOperations _fileOps;

    // the three sub-handlers, one for each perspective
    private final YControlFlowHandler _controlFlowHandler;
    private final YDataHandler _dataHandler;
    private final YResourceHandler _resourceHandler;

    public static final String DEFAULT_TYPE_DEFINITION =
            "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n\n</xs:schema>";


    /**
     * The constructor. Each YSpecificationHandler object manages exactly
     * one specification.
     */
    public YSpecificationHandler() {
        _fileOps = new FileOperations();
        _dataHandler = new YDataHandler();
        _controlFlowHandler = new YControlFlowHandler();
        _resourceHandler = new YResourceHandler();
    }


    /**
     * Creates a new base YAWL specification
     * @return the created specification
     */
    public YSpecification newSpecification() {
        _specification = _fileOps.newSpecification();
        setUniqueID();
        addAuthor(System.getProperty("user.name"));
        setDescription("No description provided");

        _dataHandler.setSpecification(_specification);
        try {
            setSchema(DEFAULT_TYPE_DEFINITION);
        }
        catch (Exception yse) {
            // only thrown when schema is invalid, so never here
        }

        _controlFlowHandler.setSpecification(_specification);
        try {
            _controlFlowHandler.createRootNet("Net");
        }
        catch (YControlFlowHandlerException ycfhe) {
            // only thrown when spec is null, so never here
        }

        _resourceHandler.setSpecification(_specification);
        return _specification;
    }


    /** @return the current specification object */
    public YSpecification getSpecification() { return _specification; }


    /**
     * @return An XML representation of the specification
     * @throws IOException if there's a problem converting the specification to XML
     */
    public String getSpecificationXML() throws IOException {
        return _fileOps.getSpecificationXML();
    }


    /**
     * Sets the data schema for this specification
     * @param schema the data schema
     * @throws YSyntaxException if the schema is invalid
     */
    public void setSchema(String schema) throws YSyntaxException, YDataHandlerException {
        _specification.setSchema(schema);
        _dataHandler.setSchema(schema);                        // for later validation
    }


    /** @return the current data schema of the specification */
    public String getSchema() { return _specification.getDataSchema(); }


    /**
     * The data handler manages all aspects of the data perspective
     * @return the handler for the current specification
     */
    public YDataHandler getDataHandler() { return _dataHandler; }


    /**
     * The control-flow handler manages all aspects of the control-flow perspective
     * @return the handler for the current specification
     */
    public YControlFlowHandler getControlFlowHandler() { return _controlFlowHandler; }


    /**
     * The resource handler manages all aspects of the resource perspective
     * @return the handler for the current specification
     */
    public YResourceHandler getResourceHandler() { return _resourceHandler; }


    /** @return true if a specification is currently loaded */
    public boolean isLoaded() { return _specification != null; }


    /**
     * Checks an identifier for uniqueness, and appends a numeric suffix if necessary
     * @param id the identifier to check
     * @return the identifier, appended with a suffix to ensure uniqueness if necessary
     * @throws IllegalIdentifierException if the identifier contains XML reserved chars
     */
    public String checkID(String id) throws IllegalIdentifierException {
        return _controlFlowHandler.checkID(id);
    }


    /**
     * Checks whether an identifier is free of XML reserved characters
     * @param id an identifier to check
     * @return true if there are no reserved characters in the identifier
     */
    public boolean isValidXMLIdentifier(String id) {
        return _controlFlowHandler.isValidXMLIdentifier(id);
    }


    /******************************************************************************/
    // MetaData Settings //

    /**
     * Sets the URI (unique name identifier) of the specification
     * @param uri the name to set
     * @throws IllegalIdentifierException if the identifier contains XML reserved chars
     */
    public void setURI(String uri) throws IllegalIdentifierException {
        _specification.setURI(uri);
    }

    /** @return the specification's URI (i.e. its name) */
    public String getURI() { return _specification.getURI(); }


    /**
     * Sets the specification's title (a non-unique descriptive term)
     * @param title the term to assign to the title
     */
    public void setTitle(String title) { getMetaData().setTitle(title); }

    /** @return the specification's title */
    public String getTitle() { return getMetaData().getTitle(); }


    /**
     * Sets a description of the specification
     * @param desc the description
     */
    public void setDescription(String desc) {
        getMetaData().setDescription(desc);
        _specification.setDocumentation(desc);          // for SpecificationData
    }

    /** @return the specification's description */
    public String getDescription() { return getMetaData().getDescription(); }


    /**
     * Sets a list of author names
     * @param authors the list of names
     */
    public void setAuthors(List<String> authors) { getMetaData().setCreators(authors); }

    /** @return the list of author names */
    public List<String> getAuthors() { return getMetaData().getCreators(); }

    /**
     * Adds an author name to the current list of authors
     * @param author the author name to add
     */
    public void addAuthor(String author) {
        if (author != null) {
            List<String> authors = getAuthors();
            authors.add(author);
            getMetaData().setCreators(authors);
        }
    }


    /**
     * Sets the specification's version
     * @param version the version
     */
    public void setVersion(YSpecVersion version) {
        if (version.toString().equals("0.0")) version.minorIncrement();
        getMetaData().setVersion(version);
    }

    /** @return the specification's version */
    public YSpecVersion getVersion() { return getMetaData().getVersion(); }


    /** Generates a unique identifier (UUID) for this specification */
    public void setUniqueID() {
        getMetaData().setUniqueID(generateSpecificationIdentifier());
    }

    /** @return the unique identifier (UUID) for this specification */
    public String getUniqueID() {
        String unique = getMetaData().getUniqueID();
        if (unique == null) {
            unique = generateSpecificationIdentifier();
            getMetaData().setUniqueID(unique);
        }
        return unique;
    }


    /**
     * Sets the date from which this specification is considered to be 'in production'
     * @param validFrom the date
     */
    public void setValidFrom(Date validFrom) { getMetaData().setValidFrom(validFrom); }

    /**
     * @return the date from which this specification is considered to be
     * 'in production'. Can be null if no particular start date has been set.
     */
    public Date getValidFrom() { return getMetaData().getValidFrom(); }


    /**
     * Sets the date until which this specification is considered to be 'in production'
     * @param validUntil the date
     */
    public void setValidUntil(Date validUntil) { getMetaData().setValidUntil(validUntil); }

    /**
     * @return the date until which this specification is considered to be
     * 'in production'. Can be null if no particular start date has been set.
     */
    public Date getValidUntil() { return getMetaData().getValidUntil(); }


    private YMetaData getMetaData() {
        YMetaData metaData = _specification.getMetaData();
        if (metaData == null) {
            metaData = new YMetaData();
            _specification.setMetaData(metaData);
        }
        return metaData;
    }


    /******************************************************************************/

    /**
     * Sets the global options for future file saves.
     * @param options the file options to set.
     * @see org.yawlfoundation.yawl.editor.core.util.FileSaveOptions
     */
    public void setFileSaveOptions(FileSaveOptions options) {
        _fileOps.setFileSaveOptions(options);
    }

    /**
     * @return the current file save options
     * @see org.yawlfoundation.yawl.editor.core.util.FileSaveOptions
     */
    public FileSaveOptions getFileSaveOptions() {
        return _fileOps.getFileSaveOptions();
    }


    /**
     * Loads an XML representation of a specification into this handler object
     * @param specXML the specification XML
     * @param layoutXML XML of the graphical layout information for the specification
     *                  (optional: may be null)
     * @throws YSyntaxException if the specification XML fails validation
     * @throws YLayoutParseException if the layout XML fails validation
     */
    public void load(String specXML, String layoutXML)
            throws IOException, YSyntaxException, YLayoutParseException {
        initHandlers(_fileOps.load(specXML, layoutXML));
    }


    /**
     * Loads a specification from file
     * @param fileName the absolute name of the file
     * @throws IOException if there are problems loading or reading the file
     * @throws YSyntaxException if the specification XML fails validation
     */
    public void load(String fileName) throws IOException, YSyntaxException {
        initHandlers(_fileOps.load(fileName));
    }


    /**
     * Saves the current specification to file, using the previously set file save
     * options, or a default set if none saved
     * @throws IOException if there's a problem writing the specification to disk file
     */
    public void save() throws IOException {
        save((FileSaveOptions) null);          // save with default options
    }


    /**
     * Saves the current specification to file, using the file save options passed
     * @param saveOptions the file save options to use for this save
     * @throws IOException if there's a problem writing the specification to disk file
     */
    public void save(FileSaveOptions saveOptions) throws IOException {
        _fileOps.save(saveOptions);
    }


    /**
     * Saves the current specification to file, using the previously set file save
     * options, or a default set if none saved, and the layout information passed
     * @param layout a YLayout object describing the layout information to use for
     *               this save
     * @throws IOException if there's a problem writing the specification to disk file
     */
    public void save(YLayout layout) throws IOException {
        save(layout, null);
    }


    /**
     * Saves the current specification to file, using the file save options and layout
     * information passed
     * @param saveOptions the file save options to use for this save
     * @param layout a YLayout object describing the layout information to use for
     *               this save
     * @throws IOException if there's a problem writing the specification to disk file
     */
    public void save(YLayout layout, FileSaveOptions saveOptions) throws IOException {
        setLayout(layout);
        save(saveOptions);
    }


    /**
     * Saves the current specification to a new file name, using the previously set
     * file save options, or a default set if none saved
     * @param fileName the absolute name of the new file to save to
     * @throws IOException if there's a problem writing the specification to disk file
     */
    public void saveAs(String fileName) throws IOException {
        saveAs(fileName, (FileSaveOptions) null);
    }


    /**
     * Saves the current specification to a new file name, using the file save options
     * passed
     * @param fileName the absolute name of the new file to save to
     * @param saveOptions the file save options to use for this save
     * @throws IOException if there's a problem writing the specification to disk file
     */
    public void saveAs(String fileName, FileSaveOptions saveOptions) throws IOException {
        _fileOps.saveAs(fileName, getMetaData(), saveOptions);
    }


    /**
     * Saves the current specification to a new file name, using the layout information
     * passed
     * @param fileName the absolute name of the new file to save to
     * @param layout the file save options to use for this save
     * @throws IOException if there's a problem writing the specification to disk file
     */
    public void saveAs(String fileName, YLayout layout) throws IOException {
        saveAs(fileName, layout, null);
    }


    /**
     * Saves the current specification to a new file name, using the layout information
     * and file save options passed
     * @param fileName the absolute name of the new file to save to
     * @param layout the file save options to use for this save
     * @throws IOException if there's a problem writing the specification to disk file
     */
    public void saveAs(String fileName, YLayout layout, FileSaveOptions saveOptions)
            throws IOException {
        setLayout(layout);
        saveAs(fileName, saveOptions);
    }


    /**
     * Removes the current specification from all handlers
     */
    public void close() {
        _fileOps.close();
        _specification = null;
        _dataHandler.close();
        _controlFlowHandler.close();
        _resourceHandler.close();
    }


    /** @return the current file name for the loaded specification */
    public String getFileName() { return _fileOps.getFileName(); }


    /**
     * Sets the layout information for the current specification
     * @param layout the layout to set
     */
    public void setLayout(YLayout layout) { _fileOps.setLayout(layout); }

    /** @return the current layout information for the loaded specification */
    public YLayout getLayout() { return _fileOps.getLayout(); }


    /** @return the identifier object for the loaded specification */
    public YSpecificationID getID() {
        return _specification.getSpecificationID();
    }


    /****************************************************************************/

    private String generateSpecificationIdentifier() {
         return "UID_" + UUID.randomUUID().toString();
    }


    private void initHandlers(YSpecification specification) {
        if (specification != null) {
            _specification = specification;
            _dataHandler.setSpecification(specification);
            _controlFlowHandler.setSpecification(specification);
            _resourceHandler.setSpecification(specification);
        }
    }

}
