package org.yawlfoundation.yawl.engine.instance;

import org.yawlfoundation.yawl.util.StringUtil;

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


    public String getName() { return name; }

    public void setName(String s) { name = s; }


    public String getDataType() { return dataType; }

    public void setDataType(String s) { dataType = s; }


    public String getDataSchema() { return dataSchema; }

    public void setDataSchema(String s) { dataSchema = s; }


    public Usage getUsage() { return usage; }

    public void setUsage(Usage u) { usage = u; }


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
        

}
