/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.codelets;

import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 7/03/2011
 */
public class TaskCompleterInfo extends AbstractCodelet {

    public TaskCompleterInfo() {
        super();
        setDescription("This codelet gets the name and userid of the participant <br> " +
                       "who completed a specified <i>atomic</i> task in the current case.<br> " +
                       "Input: taskName (string type).<br>" +
                       "Outputs: name (string type)<br>" +
                       "         userid (string type)");
    }


    public Element execute(Element inData, List<YParameter> inParams,
                           List<YParameter> outParams) throws CodeletExecutionException {
        setInputs(inData, inParams, outParams);
        String taskName = getValue("taskName");               // throws excp. if missing
        Set<Participant> completers =
                ResourceManager.getInstance().getWhoCompletedTask(taskName, getWorkItem());
        if (completers.isEmpty()) {
            throw new CodeletExecutionException("Unknown task completer. Either the " +
              "taskName '" + taskName +
              "'  is invalid or a task of that name has not yet completed for this case.");
        }
        Participant p = completers.iterator().next();      // only one for atomic tasks
        setParameterValue("name", p.getFullName());
        setParameterValue("userid", p.getUserID());
        
        return getOutputData();
    }


    public List<YParameter> getRequiredParams() {
        List<YParameter> params = new ArrayList<YParameter>();

        YParameter param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName("string", "taskName", XSD_NAMESPACE);
        param.setDocumentation("The name of the completed task");
        params.add(param);

        param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        param.setDataTypeAndName("string", "name", XSD_NAMESPACE);
        param.setDocumentation("The name of the completing participant");
        params.add(param);

        param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        param.setDataTypeAndName("string", "userid", XSD_NAMESPACE);
        param.setDocumentation("The userid of the completing participant");
        params.add(param);
        return params;
    }

}
