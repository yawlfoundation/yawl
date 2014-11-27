/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.data;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;

/**
 * @author Michael Adams
 * @date 20/11/14
 */
public class BindingReference {

    private YNet _net;
    private YTask _task;
    private YVariable _variable;
    private String _binding;

    public BindingReference(YNet net, YTask task, YVariable variable, String binding) {
        _net = net;
        _task = task;
        _variable = variable;
        _binding = binding;
    }


    public YNet getNet() { return _net; }

    public String getNetID() { return _net != null ? _net.getID() : null; }

    public YTask getTask() { return _task; }

    public String getTaskID() { return _task != null ? _task.getID() : null; }

    public YDecomposition getDecomposition() {
        return _task != null ? _task.getDecompositionPrototype() : null;
    }

    public String getDecompositionID() {
        YDecomposition decomposition = getDecomposition();
        return decomposition != null ? getDecomposition().getID() : null;
    }

    public YVariable getVariable() { return _variable; }

    public String getVariableName() {
        return _variable != null ? _variable.getName() : null;
    }

    public String getBinding() { return _binding; }

    public String getScope() {
        if (_variable instanceof YParameter) {
            return ((YParameter) _variable).getParamType() == 1 ? "Output" : "Input";
        }
        else return "Local";
    }

    public String getBindingKey() {
        StringBuilder key = new StringBuilder();
        String rootID = getScope().equals("Output") ? getDecompositionID() : getNetID();
        key.append('/').append(rootID).append('/').append(getVariableName()).append('/');
        return key.toString();
    }

}
