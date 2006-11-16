/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.data;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * 
 * @author Lachlan Aldred
 * Date: 24/09/2003
 * Time: 15:49:36
 * 
 * todo | LJA: why did you remove the compareTo/comparable?  have you found another
 * todo | way to guarantee the order params are read in is the order in which they
 * todo | are stored and retrieived?  you are deferring sorting to the superclass,
 * todo | which I think is unwise.  It is now sorting a different way too.  This
 * todo | will break the toXML() produced and the schema for YAWL will reject it.
 */
@Entity
@DiscriminatorValue("parameter")
public class YParameter extends YVariable implements Serializable  {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private int _ordering;
	
	private static final long serialVersionUID = 2006030080l;
	
    private boolean _mandatory = false;

    private boolean _cutsThroughDecompositionStateSpace;

    private static final String[] _paramTypes = new String[]{"inputParam", "outputParam", "enablementParam"};
    public static final int _INPUT_PARAM_TYPE = 0;
    public static final int _OUTPUT_PARAM_TYPE = 1;
    public static final int _ENABLEMENT_PARAM_TYPE = 2;
    private String _paramType;
    private Map<String, String> attributes;

    /**
     * Null constructor inserted for hibernate
     */
    protected YParameter() {
    }

   

    /**
     * creates a parameter
     * @param decomposition the parent decomposition
     * @param type use one of the public static type attributes
     */
    public YParameter(YDecomposition decomposition, int type){
        super(decomposition);
        try{
            _paramType = _paramTypes[type];
//            if (decomposition != null) {
//            	setParent(decomposition);
//            }
        }catch (ArrayIndexOutOfBoundsException e){
            throw new IllegalArgumentException("<type> param is not valid.");
        }
    }

    /**
     * Constructs a parameter using a type string.
     * @param decomposition the decomposition
     * @param type the parameter type (inputParam /outputParam /enablementParam).
     */
    public YParameter(YDecomposition decomposition, String type) {
        super(decomposition);
        List types = Arrays.asList(_paramTypes);
        if(! types.contains(type)){
            throw new IllegalArgumentException("Type (" + type + ") is not valid.");
        }
        _paramType = type;
    }



    
    /**
     * Establishes whether or not the parameter is meant to be mandatory
     * @param isMandatory
     */
    public void setManadatory(boolean isMandatory){
        //todo make this mean something to the engine because at the moment it means nothing
        _mandatory = isMandatory;
    }


    /**
     * Set whether the param bypasses the decomposition's state space.  Can only be set
     * on an input param.
     * @param isCutThroughParam is yes then true.
     */
    public void setIsCutThroughParam(boolean isCutThroughParam) {
        if(_paramType.equals(_paramTypes[_OUTPUT_PARAM_TYPE])){
            _cutsThroughDecompositionStateSpace = isCutThroughParam;
        }
        else {
            throw new IllegalArgumentException("Cannot be set on input param.");
        }
    }

    public void setOrdering(int ordering) {
           _ordering = ordering;
    }

    /**
     * 
     * @return
     * @hibernate.property column="IS_MANDATORY"
     */
    @Column(name="is_mandatory")
    public boolean isMandatory() {
        return _mandatory;
    }
    /**
     * Inserted for hibernate
     * @param b
     */
    protected void setMandatory(boolean b) {
    	_mandatory = b;
    }


    /**
     * 
     * @return
     * @hibernate.property column="DIRECTION" length="255"
     */
    @Column(name="direction")
    public String getDirection() {
        return _paramType;
    }
    /**
     * Inserted for hibernate
     * @param s
     */
    protected void setDirection(String s) {
    	_paramType = s;
    }


    public String toXML() {
        StringBuffer xml = new StringBuffer();

        xml.append("<" + _paramType);

        /**
         * AJH: Output any attributes for this parameter
         */
        if ((getAttributes() != null) && (_paramType.equals(_paramTypes[_INPUT_PARAM_TYPE])))
        {
            Iterator iter = getAttributes().keySet().iterator();
            while(iter.hasNext())
            {
                String attrName = (String)iter.next();
                xml.append(" " + attrName + "=\"" + (String)getAttributes().get(attrName) + "\"");
            }
        }

        xml.append(toXMLGuts());
        if (_mandatory) {
            xml.append("<mandatory/>");
        }
        if (_cutsThroughDecompositionStateSpace) {
            xml.append("<bypassesStatespaceForDecomposition/>");
        }

        xml.append("</" + _paramType + ">");

        return xml.toString();
    }

    public String toSummaryXML() {
        SAXBuilder builder = new SAXBuilder();
        String xml = toXML();

        Document doc = null;
        try {
            doc = builder.build(new StringReader(xml));
        } catch (JDOMException e) {
            /**
             * AJH: Silent failure here.
             */
            e.printStackTrace();
        } catch (IOException e) {
            /**
             * AJH: Silent failure here.
             */
            System.out.println(xml);
            e.printStackTrace();
        }
        Element paramElem = doc.getRootElement();
        paramElem.removeChild("initialValue");
        XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        return outputter.outputString(paramElem);
    }

    public String toString() {
        return toXML();
    }


    public List <YVerificationMessage> verify() {
        List <YVerificationMessage> messages =
                new Vector<YVerificationMessage>();
        messages.addAll(super.verify());
        if (_mandatory && _initialValue != null) {
            messages.add(new YVerificationMessage(this,
                    this + "cannot be mandatory and have initial value.",
                    YVerificationMessage.ERROR_STATUS));
        }
        return messages;
    }

    @Transient
    public boolean isInput() {
        return isParamType(_INPUT_PARAM_TYPE);
    }

    @Transient
    public boolean isOutput() {
        return isParamType(_OUTPUT_PARAM_TYPE);
    }

    @Transient
    public boolean isEnablement() {
        return isParamType(_ENABLEMENT_PARAM_TYPE);
    }

    @Transient
    private boolean isParamType(int paramType) {
       return _paramTypes[paramType].equals(_paramType);
    }
    
    /**
     * Returns whether or not the param is used for pure control flow, i.e.,
     * the value by passes the decompositions state space.
     */
    @Transient
    public boolean bypassesDecompositionStateSpace() {
        return _cutsThroughDecompositionStateSpace;
    }

    @Transient
    public static String getTypeForInput() {
        return _paramTypes[_INPUT_PARAM_TYPE];
    }

    @Transient
    public static String getTypeForOutput() {
        return _paramTypes[_OUTPUT_PARAM_TYPE];
    }

    @Transient
    public static String getTypeForEnablement() {
        return _paramTypes[_ENABLEMENT_PARAM_TYPE];
    }
    /**
     * Return table of attributes associated with this variable.<P>
     *
     * Table is keyed by attribute 'name' and contains the string represenation of the XML elements attribute.<P>
     * @hibernate.property
     */
//    @CollectionOfElements
    @Transient
    public Map<String, String> getAttributes()
    {
        return this.attributes;
    }
    
    public void setAttributes(Map<String, String> map) {
    	this.attributes = map;
    }

    public void addAttribute(String id, String value)
    {
        if (attributes == null)
        {
            attributes = new Hashtable();
        }
        attributes.put(id, value);
    }}
