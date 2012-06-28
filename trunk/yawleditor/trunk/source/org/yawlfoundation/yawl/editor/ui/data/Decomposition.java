/*
 * Created on 22/09/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.yawlfoundation.yawl.editor.ui.data;

import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.logging.YLogPredicate;

import java.util.Map;


public class Decomposition {

    public static final String PROPERTY_LOCATION =
            FileUtilities.getDecompositionPropertiesExtendeAttributePath();

    protected YDecomposition _decomposition;
    private String _description;
    private String _externalGateway;
    private DataVariableSet _dataSet;

    // called from WebServiceDecomposition
    public Decomposition() {
        _decomposition = SpecificationModel.getSpec().addTaskDecomposition("__temp__");
        setVariables(new DataVariableSet());
    }

    public Decomposition(boolean isRootNet) {
        if (isRootNet) {
            _decomposition = SpecificationModel.getSpec().getRootNet();
        }
        else {
            _decomposition = SpecificationModel.getSpec().addSubNet("NewNet");
        }
        setDescription("The default (empty) decomposition");
        setVariables(new DataVariableSet());
    }


    public YDecomposition getNet() { return _decomposition; }

    public void setLabel(String label) {
        _decomposition.setName(label);
    }

    public String getLabel() {
        return _decomposition.getName();
    }

    public String getLabelAsElementName() {
        return XMLUtilities.toValidXMLName(getLabel());
    }

    public void setDescription(String description) {
        _decomposition.setDocumentation(description);
    }

    public String getDescription() {
        return _decomposition.getDocumentation();
    }

    public void setLogPredicateStarted(String predicate) {
        YLogPredicate ylp = _decomposition.getLogPredicate();
        if (ylp == null) {
            if (predicate == null) return;             // don't create if pred is null
            ylp = new YLogPredicate();
            _decomposition.setLogPredicate(ylp);
        }
        ylp.setStartPredicate(predicate);
    }

    public String getLogPredicateStarted() {
        YLogPredicate predicate = _decomposition.getLogPredicate();
        return predicate != null ? predicate.getStartPredicate() : null;
    }

    public void setLogPredicateCompletion(String predicate) {
        YLogPredicate ylp = _decomposition.getLogPredicate();
        if (ylp == null) {
            if (predicate == null) return;             // don't create if pred is null
            ylp = new YLogPredicate();
            _decomposition.setLogPredicate(ylp);
        }
        ylp.setCompletionPredicate(predicate);
    }

    public String getLogPredicateCompletion() {
        YLogPredicate predicate = _decomposition.getLogPredicate();
        return predicate != null ? predicate.getCompletionPredicate() : null;
    }


    public void setExternalDataGateway(String gateway) {
        _externalGateway = gateway;
    }

    public String getExternalDataGateway() {
        return _externalGateway;
    }

    public DataVariableSet getVariables() {
        return _dataSet;
    }

    public void setVariables(DataVariableSet variables) {
        if (variables != null) {
            _dataSet = variables;
            variables.setDecomposition(this);
        }
    }

    public void addVariable(DataVariable variable) {
        getVariables().add(variable);
    }

    public void removeVariable(DataVariable variable) {
        getVariables().remove(variable);
    }

    public DataVariable getVariableWithName(String name) {
        return getVariables().getVariableWithName(name);
    }

    public boolean hasVariableEqualTo(DataVariable variable) {
        for (Object o : _dataSet.getAllVariables()) {
            DataVariable myVariable = (DataVariable) o;
            if (myVariable.equalsIgnoreUsage(variable)) {
                return true;
            }
        }
        return false;
    }

    public DataVariable getVariableAt(int position) {
        return getVariables().getVariableAt(position);
    }

    public int getVariableCount() {
        return getVariables().size();
    }

    //MLF: BEGIN
    //LWB: Slight mods on MLF code to make extended attributes part of the typical
    // decomposition attribute set.
    public void setAttribute(String name, Object value) {
        _decomposition.setAttribute(name, (String) value);
    }

    public String getAttribute(String name) {
        String value = _decomposition.getAttribute(name);
        return value != null ?  value : "";
    }

    public YAttributeMap getAttributes() {
        return _decomposition.getAttributes();
    }

    public void setAttributes(Map<String, String> attributes) {
        _decomposition.setAttributes(attributes);
    }


    public boolean invokesWorklist() {
        return false;
    }

    public boolean isManualInteraction() {
        return false;
    }
}
