/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements.data;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.util.YVerificationMessage;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 24/09/2003
 * Time: 15:49:36
 * 
 */
public class YParameter extends YVariable implements Comparable {
//    private boolean _mandatory = false;

    private int _ordering;
    private boolean _cutsThroughDecompositionStateSpace;

    private static final String[] _paramTypes = new String[]{"inputParam", "outputParam", "enablementParam"};
    public static final int _INPUT_PARAM_TYPE = 0;
    public static final int _OUTPUT_PARAM_TYPE = 1;
    public static final int _ENABLEMENT_PARAM_TYPE = 2;
    private String _paramType;
    private Hashtable attributes;



    /**
     * creates a parameter
     * @param dec the parent decomposition
     * @param dataType the datatype
     * @param name the name
     * @param nameSpaceURI
     * @param initialValue
     * @param inputTrueOutputFalse
     * @deprecated use YParam(dec, type) instead
     */
    public YParameter(YDecomposition dec, String dataType, String name, String nameSpaceURI, String initialValue, boolean inputTrueOutputFalse) {
        super(dec, dataType, name, initialValue, nameSpaceURI);
        _paramType = inputTrueOutputFalse ?
                _paramTypes[_INPUT_PARAM_TYPE]
                :
                _paramTypes[_OUTPUT_PARAM_TYPE];
    }


    /**
     * @param dec
     * @param dataType
     * @param name
     * @param namespaceURI
     * @param mandatory
     * @param inputTrueOutputFalse
     * @deprecated use YParam(dec, type) instead
     */
    public YParameter(YDecomposition dec, String dataType, String name, String namespaceURI, boolean mandatory, boolean inputTrueOutputFalse) {
        super(dec, dataType, name, null, namespaceURI);
//        _mandatory = mandatory;
        super.setMandatory(mandatory);

        _paramType = inputTrueOutputFalse ?
                _paramTypes[_INPUT_PARAM_TYPE]
                :
                _paramTypes[_OUTPUT_PARAM_TYPE];
    }


    /**
     * creates a parameter
     * @param dec the parent decomposition
     * @param isInputParam if false means is output param
     * @deprecated use YParam(dec, type) instead
     */
    public YParameter(YDecomposition dec, boolean isInputParam){
        super(dec);

        _paramType = isInputParam ?
                _paramTypes[_INPUT_PARAM_TYPE]
                :
                _paramTypes[_OUTPUT_PARAM_TYPE];
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


//    /**
//     * Establishes whether or not the parameter is meant to be mandatory
//     * @param isMandatory
//     */
//    public void setManadatory(boolean isMandatory){
//        //todo make this mean something to the engine because at the moment it means nothing
////        _mandatory = isMandatory;
//        super.setMandatory(isMandatory);
//    }


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


//    public boolean isMandatory() {
//        return super.isMandatory();
//    }


    public void setOrdering(int ordering) {
        _ordering = ordering;
    }


    public String getDirection() {
        return _paramType;
    }


    public String toXML() {
        StringBuffer xml = new StringBuffer();

        xml.append("<" + _paramType);

        /**
         * AJH: Output any attributes for this parameter
         */
        if ((getAttributes() != null) && (_paramType.equals(_paramTypes[_INPUT_PARAM_TYPE])))
        {
            Enumeration enumeration = getAttributes().keys();
            while(enumeration.hasMoreElements())
            {
                String attrName = (String)enumeration.nextElement();
                xml.append(" " + attrName + "=\"" + (String)getAttributes().get(attrName) + "\"");
            }
        }

        xml.append(toXMLGuts());
        if (super.isMandatory()) {
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
            Logger.getLogger(YParameter.class).error(xml);
            e.printStackTrace();
        } catch (IOException e) {
            /**
             * AJH: Silent failure here.
             */
            Logger.getLogger(YParameter.class).error(xml);
            e.printStackTrace();
        }
        Element paramElem = doc.getRootElement();
        paramElem.removeChild("initialValue");
        Element typeElement = paramElem.getChild("type");
        Element orderingElem = new Element("ordering");
        orderingElem.setText("" + _ordering);
        if(null == typeElement){
            paramElem.addContent(0, orderingElem);
        } else {
            paramElem.addContent(1, orderingElem);
        }
        XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        return outputter.outputString(paramElem);
    }

    public String toString() {
        return toXML();
    }


    public List verify() {
        List messages = new Vector();
        messages.addAll(super.verify());
        if (super.isMandatory() && _initialValue != null) {
            messages.add(new YVerificationMessage(this,
                    this + "cannot be mandatory and have initial value.",
                    YVerificationMessage.ERROR_STATUS));
        }
        return messages;
    }


    public int compareTo(Object o) {
        YParameter s = (YParameter) o;
        int dif = this._ordering - s._ordering;
        if (dif < 0) {
            return -1;
        }
        if (dif > 0) {
            return 1;
        }
        return 0;
    }

    public boolean isInput() {
        return isParamType(_INPUT_PARAM_TYPE);
    }

    public boolean isOutput() {
        return isParamType(_OUTPUT_PARAM_TYPE);
    }

    public boolean isEnablement() {
        return isParamType(_ENABLEMENT_PARAM_TYPE);
    }

    private boolean isParamType(int paramType) {
       return _paramTypes[paramType].equals(_paramType);
    }
    
    /**
     * Returns whether or not the param is used for pure control flow, i.e.,
     * the value by passes the decompositions state space.
     * @return
     */
    public boolean bypassesDecompositionStateSpace() {
        return _cutsThroughDecompositionStateSpace;
    }

    public static String getTypeForInput() {
        return _paramTypes[_INPUT_PARAM_TYPE];
    }

    public static String getTypeForOutput() {
        return _paramTypes[_OUTPUT_PARAM_TYPE];
    }

    public static String getTypeForEnablement() {
        return _paramTypes[_ENABLEMENT_PARAM_TYPE];
    }
    /**
     * Return table of attributes associated with this variable.<P>
     *
     * Table is keyed by attribute 'name' and contains the string represenation of the XML elements attribute.<P>
     *
     * @return
     */
    public Hashtable getAttributes()
    {
        return this.attributes;
    }

    public void addAttribute(String id, String value)
    {
        if (attributes == null)
        {
            attributes = new Hashtable();
        }
        attributes.put(id, value);
    }}
