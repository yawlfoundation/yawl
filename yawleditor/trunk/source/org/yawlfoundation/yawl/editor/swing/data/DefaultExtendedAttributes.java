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
        addAttribute(decomposition, variable, "font-size", "string", fontGroup);
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
        addAttribute(decomposition, variable, "minExclusive", "string");
        addAttribute(decomposition, variable, "maxExclusive", "string");
        addAttribute(decomposition, variable, "minInclusive", "string");
        addAttribute(decomposition, variable, "maxInclusive", "string");
        addAttribute(decomposition, variable, "minLength", "string");
        addAttribute(decomposition, variable, "maxLength", "string");
        addAttribute(decomposition, variable, "length", "string");
        addAttribute(decomposition, variable, "totalDigits", "string");
        addAttribute(decomposition, variable, "fractionDigits", "string");
        addAttribute(decomposition, variable, "pattern", "text");
        addAttribute(decomposition, variable, "whiteSpace",
                "enumeration{, preserve, replace, collapse}");

        Collections.sort(_variableList);

    }

    private Vector<ExtendedAttribute> getVariableAttributes() {
        return _variableList;
    }

    private void buildDecompositionTable(NetGraph graph, Decomposition decomposition) {
        if (decomposition == null) return;
        _decompositionList = new Vector<ExtendedAttribute>();


        addAttribute(graph, decomposition, "background-color", "color");
        addAttribute(graph, decomposition, "background-alt-color", "color");
        addAttribute(graph, decomposition, "font-color", "color");
        addAttribute(graph, decomposition, "font-name", "font");
        addAttribute(graph, decomposition, "font-size", "string");
        addAttribute(graph, decomposition, "font-style",
                "enumeration{None, Bold, Italic, Bold+Italic}");
        addAttribute(graph, decomposition, "header-font-color", "color");
        addAttribute(graph, decomposition, "header-font-name", "font");
        addAttribute(graph, decomposition, "header-font-size", "string");
        addAttribute(graph, decomposition, "header-font-style",
                "enumeration{None, Bold, Italic, Bold+Italic}");
        addAttribute(graph, decomposition, "justify", "enumeration{left, center, right}");
        addAttribute(graph, decomposition, "label", "text");
        addAttribute(graph, decomposition, "readOnly", "boolean");
    }

    private Vector<ExtendedAttribute> getDecompositionAttributes() {
        return _decompositionList;
    }


    private void addAttribute(Decomposition decomposition, DataVariable variable,
                              String name, String type, ExtendedAttributeGroup group) {
        ExtendedAttribute attribute = new ExtendedAttribute(variable, decomposition, name, type,
                                                variable.getAttribute(name), group);
        group.add(attribute);
        _variableList.add(attribute);
    }

    private void addAttribute(Decomposition decomposition, DataVariable variable,
                              String name, String type) {
        _variableList.add(new ExtendedAttribute(variable, decomposition, name, type,
                                                variable.getAttribute(name)));
    }

    private void addAttribute(NetGraph graph, Decomposition decomposition,
                              String name, String type) {
        _decompositionList.add(new ExtendedAttribute(graph, decomposition, name, type,
                                                decomposition.getAttribute(name)));
    }

}
