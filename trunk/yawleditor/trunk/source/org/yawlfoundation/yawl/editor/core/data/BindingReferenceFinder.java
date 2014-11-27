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

import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 20/11/14
 */
public class BindingReferenceFinder {

    public BindingReferenceFinder() { }

    public List<BindingReference> getBindingReferences(YSpecification specification,
                                                       String netVarName) {
        return getBindingReferences(specification.getRootNet(), netVarName);
    }


    public List<BindingReference> getBindingReferences(YNet net, String netVarName) {
        List<BindingReference> referenceList = new ArrayList<BindingReference>();
        if (net.equals(net.getSpecification().getRootNet())) {
            addRootNetInputParameters(referenceList, net, netVarName);
        }
        for (YTask task : net.getNetTasks()) {
            getInputReferences(referenceList, net, task, netVarName);
            getOutputReferences(referenceList, net, task, netVarName);
            if (task instanceof YCompositeTask) {
                YNet containingNet = (YNet) task.getDecompositionPrototype();
                referenceList.addAll(getBindingReferences(containingNet, netVarName));
            }
        }
        return referenceList;
    }


    private void getInputReferences(List<BindingReference> referenceList,
                                     YNet net, YTask task, String netVarName) {
        String netID = net.getID();
        YDecomposition decomposition = task.getDecompositionPrototype();
        for (String taskVarName : task.getDataMappingsForTaskStarting().keySet()) {
            String binding = task.getDataBindingForInputParam(taskVarName);
            if (isBound(binding, netID, netVarName)) {
                YParameter param = decomposition.getInputParameters().get(taskVarName);
                referenceList.add(new BindingReference(net, task, param, binding));
            }
        }
    }


    private void getOutputReferences(List<BindingReference> referenceList,
                                     YNet net, YTask task, String netVarName) {
        String binding = task.getDataBindingForOutputParam(netVarName);
        if (binding != null) {
            YDecomposition decomposition = task.getDecompositionPrototype();
            String id = decomposition.getID();
            for (String taskVarName : decomposition.getOutputParameterNames()) {
                if (isBound(binding, id, taskVarName)) {
                    YParameter param = decomposition.getOutputParameters().get(taskVarName);
                    referenceList.add(new BindingReference(net, task, param, binding));
                }
            }
        }
    }


    private boolean isBound(String binding, String rootID, String varName) {
        StringBuilder key = new StringBuilder();
        key.append('/').append(rootID).append('/').append(varName).append('/');
        return binding.contains(key.toString());
    }


    private void addRootNetInputParameters(List<BindingReference> referenceList,
                                           YNet net, String netVarName) {
        if (net.equals(net.getSpecification().getRootNet())) {
            for (String varName : net.getInputParameters().keySet()) {
                if (varName.equals(netVarName)) {
                    referenceList.add(new BindingReference(net, null,
                            net.getInputParameters().get(varName),
                            "Value supplied on case start"));
                    break;
                }
            }
        }
    }

}
