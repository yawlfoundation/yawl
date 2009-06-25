package org.yawlfoundation.yawl.editor.swing.data;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.net.NetGraph;

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

        String value = variable.getAttribute("readOnly");

        // add attributes
        _variableList.add(new ExtendedAttribute(variable, decomposition,
                                                "readOnly", "boolean", value));
    }

    private Vector<ExtendedAttribute> getVariableAttributes() {
        return _variableList;
    }

    private void buildDecompositionTable(NetGraph graph, Decomposition decomposition) {
        _decompositionList = new Vector<ExtendedAttribute>();

       // String value = decomposition.getAttribute(name);
        // decomp attibutes go here
    }

    private Vector<ExtendedAttribute> getDecompositionAttributes() {
        return _decompositionList;
    }

}
