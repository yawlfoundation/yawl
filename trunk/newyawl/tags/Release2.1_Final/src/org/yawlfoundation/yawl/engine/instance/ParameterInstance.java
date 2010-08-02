/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.instance;

import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.logging.YEventLogger;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.jdom.Element;

/**
 * Author: Michael Adams
 * Creation Date: 11/11/2008
 */
public class ParameterInstance implements YInstance {

    public enum Usage { inputOnly, outputOnly, inputOutput, undefined }

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

    public ParameterInstance(YParameter param, YTask task, Usage usage, Element data) {
        name =  param.getPreferredName();
        dataType = param.getDataTypeName();
        dataSchema = extractDataSchema(task, dataType);
        setUsage(usage);
        if (hasInputUsage()) setInputPredicate(task.getDataBindingForInputParam(name));
        if (hasOutputUsage()) setOutputPredicate(task.getDataBindingForOutputParam(name));
        defaultValue = param.getDefaultValue();
        if (data != null) {
            originalValue = (XSDType.getInstance().isBuiltInType(dataType)) ?
                             data.getText() : JDOMUtil.elementToString(data) ;
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

    public String getUsageString() { return usage.name(); }

    public void setUsage(Usage u) { usage = u; }

    public void setUsage(String s) {
        try {
            usage = Usage.valueOf(s);
        }
        catch (Exception e) {
            usage = Usage.undefined;
        }
    }


    public String getInputPredicate() { return inputPredicate; }

    public void setInputPredicate(String s) {
        inputPredicate = StringUtil.unwrap(s);
    }


    public String getOutputPredicate() { return outputPredicate; }

    public void setOutputPredicate(String s) {
        outputPredicate = StringUtil.unwrap(s);
    }


    public String getOriginalValue() { return originalValue; }

    public void setOriginalValue(String s) { originalValue = s; }


    public String getDefaultValue() { return defaultValue; }

    public void setDefaultValue(String s) { defaultValue = s; }


    public String getValue() { return value; }

    public void setValue(String s) { value = s; }

    public void setValue(Element e) {
        if (e != null) {
            value = (e.getChildren().isEmpty()) ? e.getText() : JDOMUtil.elementToString(e) ;
        }    
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<parameterInstance>");
        xml.append(StringUtil.wrap(name, "name"));
        xml.append(StringUtil.wrap(dataType, "dataType"));
        xml.append(StringUtil.wrapEscaped(dataSchema, "dataSchema"));
        xml.append(StringUtil.wrap(usage.name(), "usage"));
        xml.append(StringUtil.wrapEscaped(inputPredicate, "inputPredicate"));
        xml.append(StringUtil.wrapEscaped(outputPredicate, "outputPredicate"));
        xml.append(StringUtil.wrapEscaped(originalValue, "originalValue"));
        xml.append(StringUtil.wrapEscaped(defaultValue, "defaultValue"));
        xml.append(StringUtil.wrapEscaped(value, "value"));
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
            dataSchema = instance.getChildText("dataSchema");
            setUsage(instance.getChildText("usage"));
            inputPredicate = instance.getChildText("inputPredicate");
            outputPredicate = instance.getChildText("outputPredicate");
            originalValue = instance.getChildText("originalValue");
            defaultValue = instance.getChildText("defaultValue");
            value = instance.getChildText("value");
        }
    }

    private boolean hasInputUsage() {
        return usage != Usage.outputOnly;
    }

    private boolean hasOutputUsage() {
        return usage != Usage.inputOnly;
    }

    private String extractDataSchema(YTask task, String dataTypeName) {
        YSpecificationID specID =
                task.getDecompositionPrototype().getSpecification().getSpecificationID();
        return YEventLogger.getInstance().getDataSchema(specID, dataTypeName);         
    }


}
