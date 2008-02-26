/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce;

import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.URL;
import java.util.*;


/**
 *  Just some summary data about a workflow specification.
 *
 * 
 * @author Lachlan Aldred
 * Date: 8/03/2004
 * Time: 12:16:02
 * 
 */
public class SpecificationData {
    private String _specificationID;
    private String _specificationName;
    private String _documentation;
    private String _status;
    private String _specAsXML;
    private Map _inputParams = new HashMap();
    private Map _dataTypes = new HashMap();
    private String _betaFormat;
    private String _rootNetID;
    private String _schema ;


    public SpecificationData(String specificationID, String specificationName,
                             String documentation, String status,
                             String version) {
        this._specificationID = specificationID;
        this._specificationName = specificationName;
        this._documentation = documentation;
        this._status = status;
        this._betaFormat = version;
    }


    public String getStatus() {
        return _status;
    }


    public String getID() {
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
        List<YParameter> params = new ArrayList(_inputParams.values());
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
        return (String) _dataTypes.get(typeName);
    }

    /**
     * If the specification contains a schema library this method returns the
     * schema library as a string (in XML Schema format).
     * @return  schema library as a string.
     */
    public String getSchemaLibrary() throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();

        InputSource yawlSpecificationInputSource = new InputSource(new StringReader(_specAsXML));

        Document document = builder.build(yawlSpecificationInputSource);
        Element yawlSpecSetElement = document.getRootElement();

        String ns ;
        if (_betaFormat.equals("2.0"))
            ns = "http://www.yawlfoundation.org/yawlschema" ;
        else
            ns = "http://www.citi.qut.edu.au/yawl" ;

        Namespace yawlNameSpace = Namespace.getNamespace(ns);
        Element yawlSpecElement = yawlSpecSetElement.getChild("specification", yawlNameSpace);

        if (yawlSpecElement != null) {
            Namespace schema2SchNS = Namespace.getNamespace("http://www.w3.org/2001/XMLSchema");
            Element schemaLibraryElement = yawlSpecElement.getChild("schema", schema2SchNS);

            if (schemaLibraryElement != null) {
                XMLOutputter output = new XMLOutputter();
                return output.outputString(schemaLibraryElement);
            }    
        }
        return null;
    }

    public static void main(String args[]) throws Exception {
        SpecificationData sd = new SpecificationData("specid", "specname", "doco", "ok", "");
        StringBuffer sb = new StringBuffer();
        URL url = SpecificationData.class.getResource("MakeRecordings.xml");
        File f = new File(url.getFile());
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = null;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        sd.setSpecAsXML(sb.toString());
    }


    public String getSchemaVersion() {
        return _betaFormat;
    }


    public void setVersion(String version) {
        this._betaFormat = version;
    }

    public String getRootNetID() {
        return this._rootNetID;
    }


    public void setRootNetID(String rootNetID) {
        this._rootNetID = rootNetID;
    }

    public boolean usesSimpleRootData() {
        return YSpecification._Beta2.equals(_betaFormat) ||
                YSpecification._Beta3.equals(_betaFormat);
    }

}