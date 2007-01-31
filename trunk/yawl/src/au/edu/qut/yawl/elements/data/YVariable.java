/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.data;

import java.io.Serializable;
import java.io.StringReader;
import java.util.List;
import java.util.Vector;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.elements.Parented;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.YVerifiable;
import au.edu.qut.yawl.schema.XMLToolsForYAWL;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * 
 * @author Lachlan Aldred
 * Date: 24/09/2003
 * Time: 16:10:14
 * 
 * 
 * @hibernate.class table="VARIABLE" discriminator-value="0"
 * @hibernate.discriminator column="VARIABLE_TYPE_ID" type="integer"
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="variable_type",
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("variable")
public class YVariable implements Comparable, Cloneable, YVerifiable, Parented<YDecomposition>, Serializable {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    protected String _dataTypeName;
    protected String _name;
    protected String _elementName;
    protected String _initialValue;
    protected String _namespaceURI;
    protected boolean _isUntyped = false;
    private String _documentation;

    /**
     * This null constructor is necessary for hibernate.
     *
     */
    protected YVariable() {
    }

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
        this.parent = dec;
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
        this.parent = dec;
    }

    private Long _id;

	/**
	 * Method should be only used by hibernate
	 * 
	 * @hibernate.id column="VARIABLE_ID" generator-class="sequence"
	 */
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    protected Long getID() {
		return _id;
	}

	/**
	 * Method should be only used by hibernate
	 */
	protected void setID( Long id ) {
		_id = id;
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
    
    @Transient
    public void setParent(YDecomposition parent) {
    	this.parent = parent;
    }

    @Transient
    public YDecomposition getParent() {
    	return parent;
    }

    @ManyToOne(cascade={CascadeType.ALL})
    @JoinColumn(name="decomposition")
    @OnDelete(action=OnDeleteAction.CASCADE)
    public YDecomposition getDecomposition() {
    	return parent;
    }
    
    public void setDecomposition(YDecomposition parent) {
    	this.parent = parent;
    }
    
    private YDecomposition parent;


	/**
     * links the variable to a xs:any schema element type.
     * @param isUntyped
     */
    public void setUntyped(boolean isUntyped) {
        _isUntyped = isUntyped;
    }

    /**
     * Returns the name reference for the data type to be used in this variable.
     * @return the name of the data type.
     * @hibernate.property column="DATA_TYPE_NAME" length="1024"
     */
    @Basic
    public String getDataTypeName() {
        return _dataTypeName;
    }
    /**
     * Inserted for purposes of Hibernate TODO Set to protected later
     * @param s
     */
    public void setDataTypeName(String s) {
    	_dataTypeName = s;
    }

    /**
     * Returns the namespace of the data type.  Expect either null if the type is a custom type
     * or "http://www.w3.org/2001/XMLSchema" if the variable uses a "built in" Schema primitive type.
     * @return null or "http://www.w3.org/2001/XMLSchema"
     * @hibernate.property column="DATA_TYPE_NAMESPACE" length="1024"
     */
    @Basic
    public String getDataTypeNameSpace() {
        return _namespaceURI;
    }
    /**
     * Inserted for hibernate TODO Set to protected later
     * @param s
     */
    public void setDataTypeNameSpace(String s) {
    	_namespaceURI = s;
    }

	/**
	 * 
	 * @return
	 * @hibernate.property column="NAME" length="255"
	 */
    @Basic
    public String getName() {
        return _name;
    }

    /**
     * Sets the name on the variabel.
     * @param name
     */
    public void setName(String name) {
        _name = name;
    }


	/**
	 * 
	 * @return
     * @hibernate.property column="INITIAL_VALUE" length="4096"
	 */
    @Column(length=1024*1024)
    public String getInitialValue() {
        return _initialValue;
    }

    /**
     * sets the initial value of the variable
     * @param initialValue
     */
    public void setInitialValue(String initialValue) {
        _initialValue = initialValue;
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
            xml.append("<documentation>")
                    .append(_documentation)
                    .append("</documentation>");
        }
        if (_isUntyped || null != _name) {
            if (null != _name) {
                xml.append("<name>")
                        .append(_name)
                        .append("</name>");
                if (_isUntyped) {
                    xml.append("<isUntyped/>");
                } else {
                    xml.append("<type>")
                            .append(_dataTypeName)
                            .append("</type>");
                    if (null != _namespaceURI) {
                        xml.append("<namespace>")
                                .append(_namespaceURI)
                                .append("</namespace>");
                    }
                }
            }
        } else if (null != _elementName) {
            xml.append("<element>")
                    .append(_elementName)
                    .append("</element>");
        }
        if (_initialValue != null) {
            xml.append("<initialValue>")
                    .append(YTask.marshal(_initialValue))
                    .append("</initialValue>");
        }
        return xml.toString();
    }


    public String toString() {
        return getClass().getName() +
                ":" + _name != null ? _name : _elementName + toXML();
    }


    public Object clone() throws CloneNotSupportedException {
    	YVariable copy = (YVariable) super.clone();
    	copy.setID(null);
        return copy;
    }


    public List <YVerificationMessage> verify() {
        List <YVerificationMessage> messages =
                new Vector<YVerificationMessage>();
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
        XMLToolsForYAWL xty = getParent().getParent().getToolsForYAWL();
        //check schema contains type with typename.
        if (null != _name) {
            boolean isSchemForSchemType =
                    XMLToolsForYAWL.getSchema4SchemaNameSpace().equals(_namespaceURI);
            if (_isUntyped) {
                if (null != _dataTypeName) {
                //todo [in future - if we ever disallow untyped elements]
                //todo we may want to catch this and _report it.
                }
            } else if (!xty.isValidType(_dataTypeName, isSchemForSchemType)) {
                messages.add(new YVerificationMessage(
                        this,
                        "The type library (Schema) in specification contains no " +
                        "type definition with name [" + _dataTypeName + "].  " +
                        "Therefore the decomposition " + parent +
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
                        "\n    Therefore the decomposition " + parent +
                        " cannot create this variable.",
                        YVerificationMessage.ERROR_STATUS));
            }
        } else {
                messages.add(new YVerificationMessage(
                        this,
                        "name or element name must be set",
                        YVerificationMessage.ERROR_STATUS));
        }
        if (null != _name) {
            if (null != _elementName) {
                messages.add(new YVerificationMessage(
                        this,
                        "name xor element name must be set, not both",
                        YVerificationMessage.ERROR_STATUS));
            }
        }
        //todo check initial value is of data-type
        return messages;
    }


    /**
     * 
     * @return
     * @hibernate.property column="DOCUMENTATION" length="4096"
     */
    @Basic
    public String getDocumentation() {
        return _documentation;
    }


    public void setDocumentation(String documentation) {
        this._documentation = documentation;
    }

    /**
     * 
     * @return
     * @hibernate.property column="ELEMENT_NAME" length="255"
     */
    @Basic
    public String getElementName(){
    	return _elementName;
    }

    /**
     * links the variable to a schema element declaration
     * @param elementName
     */
    public void setElementName(String elementName) {
        _elementName = elementName;
    }
    
    @Basic
    public boolean isUntyped(){
    	return _isUntyped;
    }


    /**
     * Returns whether or not the parameter uses an element declaration in the
     * schema library of the specification.
     * @return true if it does use element declaration
     */
    @Transient
    public boolean usesElementDeclaration() {
        return _elementName != null;
    }

    /**
     * Returns whether or not the parameter uses a type declaration in the
     * schema library of the specification.
     * @return true if it does use a type declaration
     */
    @Transient
    public boolean usesTypeDeclaration() {
        return _dataTypeName != null;
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
    	YVariable other = (YVariable) o;
    	String myName = ( this.getName() == null ) ? this.getElementName() : this.getName();
    	String otherName = ( other.getName() == null ) ? other.getElementName() : other.getName();
        return myName.compareTo( otherName );
    }

}
