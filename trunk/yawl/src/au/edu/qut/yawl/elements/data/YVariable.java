/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.data;

import java.io.StringReader;
import java.util.List;
import java.util.Vector;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.YVerifiable;
import au.edu.qut.yawl.persistence.PolymorphicPersistableObject;
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
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "VariableFactsType", namespace="http://www.citi.qut.edu.au/yawl",
	propOrder = {
	    "initialValue",
	    "documentation",
	    "name",
	    "dataTypeName",
	    "namespaceURI",
	    "isUntyped",
	    "elementName"
})
public class YVariable implements Cloneable, YVerifiable, Comparable, PolymorphicPersistableObject {
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
        this.parentLocalVariables = dec;
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
        this.parentLocalVariables = dec;
    }

    private Long _id;

	/**
	 * Method should be only used by hibernate
	 * 
	 * @hibernate.id column="VARIABLE_ID" generator-class="sequence"
	 */
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @XmlTransient
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
    

    private YDecomposition justGetParent() {
    	if (parentInputParameters != null) return parentInputParameters;
    	if (parentOutputParameters != null) return parentOutputParameters;
    	if (parentLocalVariables != null) return parentLocalVariables;
    	if (this instanceof YParameter) {
    		return ((YParameter) this).getParentEnablementParameters();
    	}
    	return null;
    }
    
    private YDecomposition parentLocalVariables;

	/**
	 * Only used by hibernate
	 */
    @ManyToOne
    @XmlTransient
	private YDecomposition getParentLocalVariables() {
		return parentLocalVariables;
	}

	/**
	 * Only used by hibernate
	 * @param parentLocalVariables
	 */
	public void setParentLocalVariables( YDecomposition parentLocalVariables ) {
		this.parentLocalVariables = parentLocalVariables;
	}
	


    private YDecomposition parentInputParameters;
	/**
	 * Only used by hibernate
	 */
    @ManyToOne
    @XmlTransient
	private YDecomposition getParentInputParameters() {
		return parentInputParameters;
	}

	/**
	 * Only used by hibernate
	 * @param parentInputParameters
	 */
	public void setParentInputParameters( YDecomposition parentInputParameters ) {
		this.parentInputParameters = parentInputParameters;
	}
	

    
    private YDecomposition parentOutputParameters;
	/**
	 * Only used by hibernate
	 */
    @ManyToOne
    @XmlTransient
	private YDecomposition getParentOutputParameters() {
		return parentOutputParameters;
	}

	/**
	 * Only used by hibernate
	 * @param parentInputParameters
	 */
	public void setParentOutputParameters( YDecomposition parentInputParameters ) {
		this.parentOutputParameters = parentInputParameters;
	}


	/**
     * links the variable to a xs:any schema element type.
     * @param isUntyped
     */
    public void setUntyped(boolean isUntyped) {
        _isUntyped = isUntyped;
    }

    /**
     * Returns the name reference for the data type to be used in this variable.
     * @return
     * @hibernate.property column="DATA_TYPE_NAME" length="1024"
     */
    @Basic
    @XmlElement(name="type", namespace="http://www.citi.qut.edu.au/yawl")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
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
    @XmlTransient
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
    @XmlElement(name="name", namespace="http://www.citi.qut.edu.au/yawl")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
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
    @Basic
    @XmlElement(name="initialValue", namespace="http://www.citi.qut.edu.au/yawl")
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
                    YTask.marshal(_initialValue) +
                    "</initialValue>");
        }
        return xml.toString();
    }


    public String toString() {
        return getClass().getName() +
                ":" + _name != null ? _name : _elementName + toXML();
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
        XMLToolsForYAWL xty = justGetParent().getSpecification().getToolsForYAWL();
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
                        "Therefore the decomposition " + parentLocalVariables +
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
                        "\n    Therefore the decomposition " + parentLocalVariables +
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
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.<p>
     *
     * In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of <i>expression</i>
     * is negative, zero or positive.
     *
     * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)<p>
     *
     * The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.<p>
     *
     * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.<p>
     *
     * It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * @param   o the Object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this Object.
     */
/*    public int compareTo(Object o) {
        YVariable otherV = (YVariable) o;
        if(null != _name){
            if(null != otherV._name){
                return _name.compareTo(otherV._name);
            } else if(null != otherV._elementName){
                if(_name.equals(otherV._elementName)){
                    return -1;
                } else {
                    return _name.compareTo(otherV._elementName);
                }
            }
        } else if(null != _elementName){
            if(null != otherV._name){
                if(_elementName.equals(otherV._name)){
                    return 1;
                } else {
                    return _elementName.compareTo(otherV._name);
                }
            } else if(null != otherV._elementName){
                return _elementName.compareTo(otherV._elementName);
            }
        }
        throw new RuntimeException();
    }
*/

    public static void main(String[] args) {
        YVariable var = new YVariable(null, "xs:boolean", "run", "false", "");
    }


    /**
     * 
     * @return
     * @hibernate.property column="DOCUMENTATION" length="4096"
     */
    @Basic
    @XmlElement(name="documentation", namespace="http://www.citi.qut.edu.au/yawl")
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
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlElement(name="element", namespace="http://www.citi.qut.edu.au/yawl")
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
    @XmlTransient
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
     * TODO Uhhh...  why does this function always return 1?  Should you at least compare if it is the same class? -DM
     * 
     * @param o
     * @return
     */
    public int compareTo(Object o) {
        return 1;
    }
    public static class MyAdapter extends XmlAdapter<String, String> {
        public MyAdapter(){}
        
        // Convert a value type to a bound type.
        // read xml content and put into Java class.
        public String unmarshal(String v){
        	return null;//v.getName().toString();
        }
        
        // Convert a bound type to a value type.
        // write Java content into class that generates desired XML 
        public String marshal(String v){
            return null;
        }
    }

    @XmlElement(name="namespace", namespace="http://www.citi.qut.edu.au/yawl")
	private String getNamespaceURI() {
		return _namespaceURI;
	}
	private void setNamespaceURI(String _namespaceuri) {
		_namespaceURI = _namespaceuri;
	}

    @XmlElement(name="isUntyped", namespace="http://www.citi.qut.edu.au/yawl")
    private Boolean getIsUntyped() {
		return _isUntyped ? Boolean.TRUE : null;
	}
	private void setIsUntyped(Boolean untyped) {
		_isUntyped = (untyped == null) ? Boolean.FALSE : Boolean.TRUE;
	}

}
