/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * Example codelet for extracting workitem metadata.
 *
 * @author Michael Adams
 */
public class ItemMetaData extends AbstractCodelet {

    public ItemMetaData() {
        super();
        setDescription("This codelet extracts certain work item metadata.<br>" +
                       "Output: specificationName (string)<br>" +
                       "Output: specificationVersion (string)<br>" +
                       "Output: caseID (string)");
    }

    /**
     * The implementation of the abstract class that does the work of this codelet. Note
     * that calls to most of the base class methods may throw a CodeletExecutionException
     * which should be passed back to the caller,
     *
     * @param inData The calling workitem's data
     * @param inParams the workitem's input parameters
     * @param outParams the workitem's output parameters
     * @return the result of the codelet's work as a JDOM Element
     * @throws CodeletExecutionException
     */
    public Element execute(Element inData, List<YParameter> inParams,
                           List<YParameter> outParams) throws CodeletExecutionException {
        WorkItemRecord wir = getWorkItem();

        // in this example, there's no inputs, so only have to set the outputs.
        String name = wir != null ? wir.getSpecURI() : "N/A";
        String version = wir != null ? wir.getSpecVersion() : "N/A";
        String caseID = wir != null ? wir.getRootCaseID() : "N/A";
        setParameterValue("specificationName", name);
        setParameterValue("specificationVersion", version);
        setParameterValue("caseID", caseID);

        // return the Element created containing the output data
        return getOutputData();
    }


    /**
     * This method is called by the YAWL editor to get the list of
     * parameters required by the codelet.
     */
    public List<YParameter> getRequiredParams() {
        List<YParameter> params = new ArrayList<YParameter>();

        YParameter param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        param.setDataTypeAndName("string", "specificationName", XSD_NAMESPACE);
        params.add(param);

        param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        param.setDataTypeAndName("string", "specificationVersion", XSD_NAMESPACE);
        params.add(param);

        param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        param.setDataTypeAndName("string", "caseID", XSD_NAMESPACE);
        params.add(param);

        return params;
    }

}
