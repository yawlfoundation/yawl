/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation. The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.elements.data;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * 
 * @author Lachlan Aldred
 * Date: 24/09/2003
 * Time: 15:49:36
 * 
 */
public class YParameter extends YVariable implements Comparable {

    public static final int _INPUT_PARAM_TYPE = 0;
    public static final int _OUTPUT_PARAM_TYPE = 1;
    public static final int _ENABLEMENT_PARAM_TYPE = 2;

    private int _ordering;
    private boolean _cutsThroughDecompositionStateSpace;
    private int _paramType;
    private YAttributeMap _attributes = new YAttributeMap();
//    private boolean _mandatory = false;


    public YParameter() { }

    /**
     * creates a parameter
     * @param decomposition the parent decomposition
     * @param type use one of the public static type attributes
     */
    public YParameter(YDecomposition decomposition, int type) {
        super(decomposition);
        if (isValidType(type)) {
           _paramType = type;
        }
        else throw new IllegalArgumentException("<type> param is not valid.");
    }

    /**
     * Constructs a parameter using a type string.
     * @param decomposition the decomposition
     * @param type the parameter type (inputParam /outputParam /enablementParam).
     */
    public YParameter(YDecomposition decomposition, String type) {
        super(decomposition);
        _paramType = getParamType(type);
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
        if (_paramType == _OUTPUT_PARAM_TYPE) {
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

    public int getOrdering() { return _ordering; }


    public String getDirection() {
        return getParamTypeStr(_paramType);
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<");
        String type = getParamTypeStr(_paramType);
        xml.append(type);

        if ((getAttributes() != null) && (_paramType == _INPUT_PARAM_TYPE)) {
            xml.append(getAttributes().toXML());
        }

        xml.append(toXMLGuts());

        if (super.isMandatory()) {
            xml.append("<mandatory/>");
        }
        if (_cutsThroughDecompositionStateSpace) {
            xml.append("<bypassesStatespaceForDecomposition/>");
        }

        xml.append("</").append(type).append(">");
        return xml.toString();
    }


    public String toSummaryXML() {
        String result = "";
        Element eParam = JDOMUtil.stringToElement(toXML());
        if (eParam != null) {
            eParam.removeChild("initialValue");
            Element typeElement = eParam.getChild("type");
            Element orderingElem = new Element("ordering");
            orderingElem.setText("" + _ordering);
            int insPos = (null == typeElement) ? 0 : 1 ;
            eParam.addContent(insPos, orderingElem);
            result = JDOMUtil.elementToString(eParam);
        }
        return result ;
    }    

    public String toString() {
        return toXML();
    }


    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();
        messages.addAll(super.verify());
        if (super.isMandatory() && _initialValue != null) {
            messages.add(new YVerificationMessage(this,
                    this + "cannot be mandatory and have initial value.",
                    YVerificationMessage.ERROR_STATUS));
        }
        return messages;
    }


    public int compareTo(Object o) {
        YParameter other = (YParameter) o;
        return this._ordering - other._ordering;
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
       return _paramType == paramType;
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
        return getParamTypeStr(_INPUT_PARAM_TYPE);
    }

    public static String getTypeForOutput() {
        return getParamTypeStr(_OUTPUT_PARAM_TYPE);
    }

    public static String getTypeForEnablement() {
        return getParamTypeStr(_ENABLEMENT_PARAM_TYPE);
    }

    public String getParamType() { return getParamTypeStr(_paramType); }

    
    private static int getParamType(String typeStr) {
        int type;
        if (typeStr.equals("inputParam")) type = _INPUT_PARAM_TYPE;
        else if (typeStr.equals("outputParam")) type = _OUTPUT_PARAM_TYPE;
        else if (typeStr.equals("enablementParam")) type = _ENABLEMENT_PARAM_TYPE;
        else throw new IllegalArgumentException("Invalid parameter type: " + typeStr);
        return type;
    }


    private static String getParamTypeStr(int type) {
        String typeStr;
        switch (type) {
            case _INPUT_PARAM_TYPE : typeStr = "inputParam"; break;
            case _OUTPUT_PARAM_TYPE : typeStr = "outputParam"; break;
            case _ENABLEMENT_PARAM_TYPE : typeStr = "enablementParam"; break;
            default : throw new IllegalArgumentException("Invalid parameter type");
        }
        return typeStr;
    }

    private static boolean isValidType(int type) {
        return (type >= _INPUT_PARAM_TYPE) && (type <= _ENABLEMENT_PARAM_TYPE);
    }


    /**
     * Return table of attributes associated with this variable.<P>
     * Table is keyed by attribute 'name' and contains the string represenation of the XML elements attribute.<P>
     * @return
     */
    public YAttributeMap getAttributes() {
        return _attributes;
    }

    public void addAttribute(String key, String value) {
        _attributes.put(key, value);
    }

    public void setAttributes(Hashtable<String, String> attributes) {
        _attributes.set(attributes);
    }
}
