package org.yawlfoundation.yawl.editor.swing.data;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.net.NetGraph;

import java.util.Collections;
import java.util.Vector;

/**
 * Author: Michael Adams
 * Creation Date: 24/02/2009
 */
public class DefaultExtendedAttributes {

    private enum Table { variable, decomposition }

    private Vector<ExtendedAttribute> _variableList;
    private Vector<ExtendedAttribute> _decompositionList;
    private Table _tableType;

    public DefaultExtendedAttributes() { }

    public DefaultExtendedAttributes(NetGraph graph, Decomposition decomposition) {
        buildDecompositionTable(graph, decomposition);
        _tableType = Table.decomposition;
    }

    public DefaultExtendedAttributes(Decomposition decomposition, DataVariable variable) {
        buildVariableTable(decomposition, variable);
        _tableType = Table.variable;
    }


    public Vector<ExtendedAttribute> getAttributes() {
        return (_tableType == Table.variable) ?
                getVariableAttributes() :
                getDecompositionAttributes();
    }


    private void buildVariableTable(Decomposition decomposition, DataVariable variable) {
        _variableList = new Vector<ExtendedAttribute>();

        ExtendedAttributeGroup fontGroup = new ExtendedAttributeGroup();

        addAttribute(decomposition, variable, "readOnly", "boolean");
        addAttribute(decomposition, variable, "background-color", "color");
        addAttribute(decomposition, variable, "font-color", "color");
        addAttribute(decomposition, variable, "font-name", "font", fontGroup);
        addAttribute(decomposition, variable, "font-size", "integer", fontGroup);
        addAttribute(decomposition, variable, "font-style",
                "enumeration{None, Bold, Italic, Bold+Italic}", fontGroup);
        addAttribute(decomposition, variable, "hide", "boolean");
        addAttribute(decomposition, variable, "hideIf", "xquery");
        addAttribute(decomposition, variable, "alert", "text");
        addAttribute(decomposition, variable, "tooltip", "text");
        addAttribute(decomposition, variable, "blackout", "boolean");
        addAttribute(decomposition, variable, "textarea", "boolean");
        addAttribute(decomposition, variable, "justify", "enumeration{left, center, right}");
        addAttribute(decomposition, variable, "image-align", "enumeration{left, center, right}");
        addAttribute(decomposition, variable, "label", "text");
        addAttribute(decomposition, variable, "skipValidation", "boolean");
        addAttribute(decomposition, variable, "mandatory", "boolean");
        addAttribute(decomposition, variable, "image-above", "text");
        addAttribute(decomposition, variable, "image-below", "text");
        addAttribute(decomposition, variable, "text-above", "text");
        addAttribute(decomposition, variable, "text-below", "text");
        addAttribute(decomposition, variable, "line-above", "boolean");
        addAttribute(decomposition, variable, "line-below", "boolean");
        addAttribute(decomposition, variable, "minExclusive", "integer");
        addAttribute(decomposition, variable, "maxExclusive", "integer");
        addAttribute(decomposition, variable, "minInclusive", "integer");
        addAttribute(decomposition, variable, "maxInclusive", "integer");
        addAttribute(decomposition, variable, "minLength", "integer");
        addAttribute(decomposition, variable, "maxLength", "integer");
        addAttribute(decomposition, variable, "length", "integer");
        addAttribute(decomposition, variable, "totalDigits", "integer");
        addAttribute(decomposition, variable, "fractionDigits", "integer");
        addAttribute(decomposition, variable, "pattern", "text");
        addAttribute(decomposition, variable, "whiteSpace",
                "enumeration{, preserve, replace, collapse}");

        Collections.sort(_variableList);
    }

    private Vector<ExtendedAttribute> getVariableAttributes() {
        return _variableList;
    }

    private void buildDecompositionTable(NetGraph graph, Decomposition decomposition) {
//        if (decomposition == null) return;
        _decompositionList = new Vector<ExtendedAttribute>();

        ExtendedAttributeGroup fontGroup = new ExtendedAttributeGroup();
        ExtendedAttributeGroup headerFontGroup = new ExtendedAttributeGroup();

        addAttribute(graph, decomposition, "page-background-color", "color");
        addAttribute(graph, decomposition, "page-background-image", "text");
        addAttribute(graph, decomposition, "background-color", "color");
        addAttribute(graph, decomposition, "background-alt-color", "color");
        addAttribute(graph, decomposition, "font-color", "color");
        addAttribute(graph, decomposition, "font-name", "font", fontGroup);
        addAttribute(graph, decomposition, "font-size", "integer", fontGroup);
        addAttribute(graph, decomposition, "font-style",
                "enumeration{None, Bold, Italic, Bold+Italic}", fontGroup);
        addAttribute(graph, decomposition, "header-font-color", "color");
        addAttribute(graph, decomposition, "header-font-name", "font", headerFontGroup);
        addAttribute(graph, decomposition, "header-font-size", "integer", headerFontGroup);
        addAttribute(graph, decomposition, "header-font-style",
                "enumeration{None, Bold, Italic, Bold+Italic}", headerFontGroup);
        addAttribute(graph, decomposition, "justify", "enumeration{left, center, right}");
        addAttribute(graph, decomposition, "label", "text");
        addAttribute(graph, decomposition, "readOnly", "boolean");
        addAttribute(graph, decomposition, "title", "text");
        addAttribute(graph, decomposition, "hideHeader", "boolean");

        Collections.sort(_decompositionList);
    }

    private Vector<ExtendedAttribute> getDecompositionAttributes() {
        return _decompositionList;
    }

    /************************************************************************/

    private void addAttribute(Decomposition decomposition, DataVariable variable,
                              String name, String type, ExtendedAttributeGroup group) {
        ExtendedAttribute attribute = new ExtendedAttribute(variable, decomposition, name, type,
                                                variable.getAttribute(name), group);
        if (group != null) group.add(attribute);
        attribute.setAttributeType(ExtendedAttribute.DEFAULT_ATTRIBUTE);
        _variableList.add(attribute);
    }

    private void addAttribute(Decomposition decomposition, DataVariable variable,
                              String name, String type) {
        addAttribute(decomposition, variable, name, type, null);
    }


    private void addAttribute(NetGraph graph, Decomposition decomposition,
                              String name, String type, ExtendedAttributeGroup group) {
        ExtendedAttribute attribute = new ExtendedAttribute(graph, decomposition, name, type,
                                                decomposition.getAttribute(name), group);
        if (group != null) group.add(attribute);
        attribute.setAttributeType(ExtendedAttribute.DEFAULT_ATTRIBUTE);
        _decompositionList.add(attribute);
    }

    private void addAttribute(NetGraph graph, Decomposition decomposition,
                              String name, String type) {
        addAttribute(graph, decomposition, name, type, null);
    }

}
