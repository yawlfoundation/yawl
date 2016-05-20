package org.yawlfoundation.yawl.worklet.rdr.xsd;

import org.yawlfoundation.yawl.schema.SchemaHandler;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 13/05/2016
 */
public class RuleSetValidator {

    public boolean validate(String xml) {
        URL xsdPath = getClass().getResource("ruleset.xsd");
        SchemaHandler validator = new SchemaHandler(xsdPath);
        boolean valid = validator.compileAndValidate(addAttributes(xml));
        if (!valid) System.out.println(validator.getConcatenatedMessage());
        return valid;
    }


    private String addAttributes(String xml) {
        XNode node = new XNodeParser().parse(xml);
        if (node != null) {
            node.addAttributes(getAttributes());
            return node.toString();
        }
        return xml;
    }

    private Map<String, String> getAttributes() {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("xmlns", "ruleset.xsd");
        attributes.put("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        attributes.put("xsi:noNamespaceSchemaLocation", "ruleset.xsd");
        return attributes;
    }


    public static void main(String[] args) {
        String xml = StringUtil.fileToString(args[0]);
        RuleSetValidator v = new RuleSetValidator();
        System.out.println(v.validate(xml));
    }
}
