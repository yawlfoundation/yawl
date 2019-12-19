/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.interfce;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.util.*;


/**
 *  Just some summary data about a workflow specification.
 *
 * @author Lachlan Aldred
 * Date: 8/03/2004
 * Time: 12:16:02
 *
 * @author Michael Adams - updated for v2.1
 *
 */
public class SpecificationData {

    private YSpecificationID _specificationID;
    private String _specificationName;
    private String _documentation;
    private String _status;
    private String _specAsXML;
    private Map<String, YParameter> _inputParams = new HashMap<String, YParameter>();
    private Map<String, String> _dataTypes = new HashMap<String, String>();
    private YSchemaVersion _schemaVersion;
    private String _rootNetID;
    private String _schema ;
    private String _title;
    private String _authors;
    private String _externalDataGateway;

    public SpecificationData(YSpecificationID specID, String specificationName,
                             String documentation, String status, YSchemaVersion version) {
        _specificationID = specID ;
        _documentation = documentation;
        _specificationName = specificationName;
        _status = status;
        _schemaVersion = version;
    }


    public String getStatus() {
        return _status;
    }


    public YSpecificationID getID() {
        return _specificationID;
    }


    public String getName() {
        return _specificationName;
    }


    public String getDocumentation() {
        return _documentation;
    }


    public String getAsXML() {
        return _specAsXML;
    }


    public void setSpecAsXML(String specAsXML) {
        this._specAsXML = specAsXML;
    }


    public String getSchema() {
        return _schema;
    }

    public void setSchema(String schema) {
        _schema = schema;
    }

    public void addInputParam(YParameter parameter) {
        _inputParams.put(parameter.getName(), parameter);
    }


    public List<YParameter> getInputParams() {
        List<YParameter> params = new ArrayList<YParameter>(_inputParams.values());
        Collections.sort(params);
        return params;
    }

    public Map getInputParamMap() {
        return _inputParams ;
    }


    public void setDataType(String nameSpaceURI, String typeName, String typeSpecification) {
        _dataTypes.put(nameSpaceURI + "#" + typeName, typeSpecification);
    }


    public String getDataType(String typeName) {
        return _dataTypes.get(typeName);
    }

    /**
     * If the specification contains a schema library this method returns the
     * schema library as a string (in XML Schema format).
     * @return  schema library as a string.
     */
    public String getSchemaLibrary() throws IOException, JDOMException {
        Document document = JDOMUtil.stringToDocument(_specAsXML);
        Element yawlSpecSetElement = document.getRootElement();

        String ns = isSecondGenSchemaVersion() ?
                "http://www.yawlfoundation.org/yawlschema" :
                "http://www.citi.qut.edu.au/yawl" ;

        Namespace yawlNameSpace = Namespace.getNamespace(ns);
        Element yawlSpecElement = yawlSpecSetElement.getChild("specification", yawlNameSpace);

        if (yawlSpecElement != null) {
            Namespace schema2SchNS = Namespace.getNamespace("http://www.w3.org/2001/XMLSchema");
            Element schemaLibraryElement = yawlSpecElement.getChild("schema", schema2SchNS);

            if (schemaLibraryElement != null) {
                return JDOMUtil.elementToString(schemaLibraryElement);
            }
        }
        return null;
    }


    public YSchemaVersion getSchemaVersion() {
        return _schemaVersion;
    }


    public boolean isSecondGenSchemaVersion() {
        return ! _schemaVersion.isBetaVersion();
    }


    public String getSpecURI() {
        return _specificationID.getUri();
    }


    public String getSpecIdentifier() {
        return _specificationID.getIdentifier();
    }


    public void setSchemaVersion(YSchemaVersion version) {
        _schemaVersion = version;
    }


    public String getSpecVersion() {
         return _specificationID.getVersionAsString();
     }


     public void setSpecVersion(String version) {
         _specificationID.setVersion(version);
     }


    public String getRootNetID() {
        return this._rootNetID;
    }


    public void setRootNetID(String rootNetID) {
        this._rootNetID = rootNetID;
    }

    public boolean usesSimpleRootData() {
        return _schemaVersion.usesSimpleRootData();
    }

    public String getMetaTitle() {
        return _title;
    }

    public void setMetaTitle(String title) {
        _title = title;
    }

    public String getAuthors() {
        return _authors;
    }

    public void setAuthors(String... authors) {
        _authors = null;
        if (authors != null) {
            for (String author : authors) {
                addAuthor(author);
            }
        }    
    }

    public void addAuthor(String author) {
        if (_authors == null)
            _authors = author;
        else
            _authors += ", " + author;
    }

    public String getExternalDataGateway() {
        return _externalDataGateway;
    }

    public void setExternalDataGateway(String gateway) {
        _externalDataGateway = gateway;
    }

    public boolean hasExternalCaseDataGateway() {
        return _externalDataGateway != null;
    }
}