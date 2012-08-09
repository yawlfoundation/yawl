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

import org.yawlfoundation.yawl.editor.core.YEditorSpecification;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.logging.YLogPredicate;

import java.util.Map;


public class Decomposition {

    protected YDecomposition _decomposition;
    private DataVariableSet _dataSet;

    // called from WebServiceDecomposition
    public Decomposition(String id) {
        if (SpecificationModel.getInstance().isLoadInProgress()) {
            _decomposition = SpecificationModel.getSpec().getTaskDecomposition(id);
        }
        else {
            _decomposition = SpecificationModel.getSpec().addTaskDecomposition(id);
        }
        setVariables(new DataVariableSet());
    }

    public Decomposition(boolean isRootNet) {
        YEditorSpecification spec = SpecificationModel.getSpec();

        if (SpecificationModel.getInstance().isLoadInProgress()) {
            if (isRootNet) {
                _decomposition = spec.getRootNet();
            }
            else {
                _decomposition = spec.addSubNet("what");
            }
        }
        else {
            if (SpecificationModel.getInstance().getNets().isEmpty()) {
                spec.newSpecification();
                _decomposition = spec.createRootNet("NewNet");
            }
            else {
                _decomposition = spec.addSubNet("NewNet");
            }
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
        if (_decomposition instanceof YNet) {
            ((YNet) _decomposition).setExternalDataGateway(gateway);
        }
    }

    public String getExternalDataGateway() {
        return (_decomposition instanceof YNet) ?
                ((YNet) _decomposition).getExternalDataGateway() : null;
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
        for (Object o : _dataSet.getVariableSet()) {
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
