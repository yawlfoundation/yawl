/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements.data;

import org.jdom.input.SAXBuilder;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YVerifiable;
import org.yawlfoundation.yawl.schema.XMLToolsForYAWL;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.io.StringReader;
import java.util.List;
import java.util.Vector;

/**
 * 
 * @author Lachlan Aldred
 * Date: 24/09/2003
 * Time: 16:10:14
 * 
 */
public class YVariable implements Cloneable, YVerifiable, Comparable{
    protected YDecomposition _parentDecomposition;
    protected String _dataTypeName;
    protected String _name;
    protected String _elementName;
    protected String _initialValue;
    protected String _defaultValue;
    protected String _namespaceURI;
    protected boolean _isUntyped = false;
    private String _documentation;
    private boolean _mandatory;

    public boolean isMandatory()
    {
        return _mandatory;
    }

    public void setMandatory(boolean _mandatory)
    {
        this._mandatory = _mandatory;
    }

    public YVariable() {}

    /**
     * old method
     * @param dec
     * @param dataType
     * @param name
     * @param initialValue
     * @param namespaceURI
     * @deprecated see new constructor and setter methods
     */
    public YVariable(YDecomposition dec, String dataType, String name, String initialValue, String namespaceURI) {
        this._parentDecomposition = dec;
        this._dataTypeName = dataType;
        this._name = name;
        this._initialValue = initialValue;
        this._namespaceURI = namespaceURI;
    }


    /**
     * Beta 3 constructer for variables
     * @param dec parent decomposition
     */
    public YVariable(YDecomposition dec) {
        this._parentDecomposition = dec;
    }


    /**
     * sets three related attributes.
     * @param dataType (mandatory) the datatype
     * @param name (mandatory) name of the var
     * @param namespace (null optional) the URI of the namespace for the type
     */
    public void setDataTypeAndName(String dataType, String name, String namespace) {
        _dataTypeName = dataType;
        _name = name;
        _namespaceURI = namespace;
    }


    /**
     * links the variable to a xs:any schema element type.
     * @param isUntyped
     */
    public void setUntyped(boolean isUntyped) {
        _isUntyped = isUntyped;
    }

    /**
     * Sets the name on the variabel.
     * @param name
     */
    public void setName(String name) {
        _name = name;
    }


    /**
     * links the variable to a schema element declaration
     * @param elementName
     */
    public void setElementName(String elementName) {
        _elementName = elementName;
    }


    /**
     * @return the name reference for the data type to be used in this variable.
     */
    public String getDataTypeName() {
        return _dataTypeName;
    }

    /**
     * Returns the namespace of the data type.  Expect either null if the type is a custom type
     * or "http://www.w3.org/2001/XMLSchema" if the variable uses a "built in" Schema primitive type.
     * @return null or "http://www.w3.org/2001/XMLSchema"
     */
    public String getDataTypeNameSpace() {
        return _namespaceURI;
    }

    public boolean isUserDefinedType() {
        return (_namespaceURI == null);
    }


    public String getName() {
        return _name;
    }

    public YDecomposition getParentDecomposition() {
        return _parentDecomposition;
    }

    public void setParentDecomposition(YDecomposition parentDecomposition) {
        _parentDecomposition = parentDecomposition;
    }

    public String getInitialValue() {
        return _initialValue;
    }

    public String getDefaultValue() {
        return _defaultValue;
    }

    public void setDefaultValue(String value) {
        _defaultValue = value;
    }

    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<localVariable");
        xml.append(toXMLGuts());
        xml.append("</localVariable>");
        return xml.toString();
    }

    protected String toXMLGuts() {
        StringBuffer xml = new StringBuffer();
        xml.append(">");

        if (null != _documentation) {
            xml.append("<documentation>"
                    + _documentation +
                    "</documentation>");
        }
        if (_isUntyped || null != _name) {
            if (null != _name) {
                xml.append("<name>" + _name + "</name>");
                if (_isUntyped) {
                    xml.append("<isUntyped/>");
                } else {
                    xml.append("<type>" + _dataTypeName + "</type>");
                    if (null != _namespaceURI) {
                        xml.append("<namespace>" +
                                _namespaceURI +
                                "</namespace>");
                    }
                }
            }
        } else if (null != _elementName) {
            xml.append("<element>" + _elementName + "</element>");
        }
        if (_initialValue != null) {
            xml.append("<initialValue>" +
                    JDOMUtil.encodeEscapes(_initialValue) +
                    "</initialValue>");
        }
        if (_defaultValue != null) {
            xml.append("<defaultValue>" +
                    JDOMUtil.encodeEscapes(_defaultValue) +
                    "</defaultValue>");
        }
        return xml.toString();
    }


    public String toString() {
        return getClass().getName() +
                ":" + ((_name != null) ? _name : _elementName + toXML());
    }


    public Object clone() throws CloneNotSupportedException {
        YVariable copy = (YVariable) super.clone();
        return copy;
    }


    public List verify() {
        List messages = new Vector();
        //check that the intital value if well formed
        if (_initialValue != null && _initialValue.indexOf("<") != -1) {
            try {
                SAXBuilder builder = new SAXBuilder();
                builder.build(new StringReader("<bla>" + _initialValue + "</bla>"));
            } catch (Exception e) {
                messages.add(new YVerificationMessage(
                        this,
                        "Problem with InitialValue [" + _initialValue +
                        "] of " + this +
                        " " + e.getMessage(),
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        XMLToolsForYAWL xty = new XMLToolsForYAWL(); //todo MLF: convert to new schema handling
        //check schema contains type with typename.
        if (null != _name) {
            boolean isSchemForSchemType =
                    xty.getSchema4SchemaNameSpace().equals(_namespaceURI);
            if (true == _isUntyped) {
                if (null != _dataTypeName) {
                //todo [in future - if we ever disallow untyped elements]
                //todo we may want to catch this and _report it.
                }
            } else if (!xty.isValidType(_dataTypeName, isSchemForSchemType)) {
                messages.add(new YVerificationMessage(
                        this,
                        "The type library (Schema) in specification contains no " +
                        "type definition with name [" + _dataTypeName + "].  " +
                        "Therefore the decomposition " + _parentDecomposition +
                        " cannot create this variable.",
                        YVerificationMessage.ERROR_STATUS));
            }
        } else if (null != _elementName) {
            boolean schemaContainsElement =
                    xty.getPrimarySchemaElementNames().contains(_elementName);
            if (!schemaContainsElement) {
                messages.add(new YVerificationMessage(
                        this,
                        "The type library (Schema) in specification contains no " +
                        "element definition with name [" + _elementName + "].  " +
                        "\n    Therefore the decomposition " + _parentDecomposition +
                        " cannot create this variable.",
                        YVerificationMessage.ERROR_STATUS));
            }
        } else if (null != _name) {
            if (null != _elementName) {
                messages.add(new YVerificationMessage(
                        this,
                        "name xor element name must be set, not both",
                        YVerificationMessage.ERROR_STATUS));
            }
        } else {
            messages.add(new YVerificationMessage(
                    this,
                    "name or element name must be set",
                    YVerificationMessage.ERROR_STATUS));
        }
        //todo check initial value is of data-type
        return messages;
    }


    /**
     * sets the initial value of the variable
     * @param initialValue
     */
    public void setInitialValue(String initialValue) {
        _initialValue = initialValue;
    }


    public String getDocumentation() {
        return _documentation;
    }


    public void setDocumentation(String documentation) {
        this._documentation = documentation;
    }
    
    public String getElementName(){
    	return _elementName;
    }
    
    public boolean isUntyped(){
    	return _isUntyped;
    }


    /**
     * Returns whether or not the parameter uses an element declaration in the
     * schema library of the specification.
     * @return true if it does use element declaration
     */
    public boolean usesElementDeclaration() {
        return _elementName != null;
    }

    /**
     * Returns whether or not the parameter uses a type declaration in the
     * schema library of the specification.
     * @return true if it does use a type declaration
     */
    public boolean usesTypeDeclaration() {
        return _dataTypeName != null;
    }

    public int compareTo(Object o) {
        return 1;
    }


}
