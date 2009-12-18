/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.engine.instance;

import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.jdom.Element;

/**
 * Author: Michael Adams
 * Creation Date: 11/11/2008
 */
public class ParameterInstance {

    public enum Usage { inputOnly, outputOnly, inputOutput }

    private String name;
    private String dataType;
    private String dataSchema;
    private Usage usage;
    private String inputPredicate;
    private String outputPredicate;
    private String originalValue;
    private String defaultValue;
    private String value;

    
    public ParameterInstance() {}

    public ParameterInstance(YParameter param, String predicate, Element data) {
        name =  param.getPreferredName();
        dataType = param.getDataTypeName();
        setUsage(param.getParamType());
        inputPredicate = predicate;
        defaultValue = param.getDefaultValue();
        if (data != null) {
            originalValue = data.getText();
            value = originalValue;
        }
    }

    public ParameterInstance(String xml) {
        this();
        fromXML(xml);
    }

    public ParameterInstance(Element instance) {
        this();
        fromXML(instance);
    }




    public String getName() { return name; }

    public void setName(String s) { name = s; }


    public String getDataType() { return dataType; }

    public void setDataType(String s) { dataType = s; }


    public String getDataSchema() { return dataSchema; }

    public void setDataSchema(String s) { dataSchema = s; }


    public Usage getUsage() { return usage; }

    public void setUsage(Usage u) { usage = u; }

    public void setUsage(String s) {
        if (s.equals("inputParam"))
            usage = Usage.inputOnly;
        else
            usage = Usage.outputOnly ;
    }


    public String getInputPredicate() { return inputPredicate; }

    public void setInputPredicate(String s) { inputPredicate = s; }


    public String getOutputPredicate() { return outputPredicate; }

    public void setOutputPredicate(String s) { outputPredicate = s; }


    public String getOriginalValue() { return originalValue; }

    public void setOriginalValue(String s) { originalValue = s; }


    public String getDefaultValue() { return defaultValue; }

    public void setDefaultValue(String s) { defaultValue = s; }


    public String getValue() { return value; }

    public void setValue(String s) { value = s; }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<parameterInstance>");
        xml.append(StringUtil.wrap(name, "name"));
        xml.append(StringUtil.wrap(dataType, "dataType"));
        xml.append(StringUtil.wrap(usage.name(), "usage"));
        xml.append(StringUtil.wrap(inputPredicate, "inputPredicate"));
        xml.append(StringUtil.wrap(outputPredicate, "outputPredicate"));
        xml.append(StringUtil.wrap(originalValue, "originalValue"));
        xml.append(StringUtil.wrap(defaultValue, "defaultValue"));
        xml.append(StringUtil.wrap(value, "value"));
        xml.append("</parameterInstance>");
        return xml.toString();
    }
        
    public void fromXML(String xml) {
        fromXML(JDOMUtil.stringToElement(xml));
    }

    public void fromXML(Element instance) {
        if (instance != null) {
            name = instance.getChildText("name");
            dataType = instance.getChildText("dataType");
            setUsage(instance.getChildText("usage"));
            inputPredicate = instance.getChildText("inputPredicate");
            outputPredicate = instance.getChildText("outputPredicate");
            originalValue = instance.getChildText("originalValue");
            defaultValue = instance.getChildText("defaultValue");
            value = instance.getChildText("value");
        }
    }

}
