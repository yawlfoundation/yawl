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

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * A codelet that executes an external program in the client environment. It expects
 * the following workitem input parameters:
 *  - command: the command line (including any arguments)
 *  - env: an optional set of attrib-value pairs representing temporary additions to
 *         the client environment (requires a user-defined data type that will
 *         deliver data like this:
 *             <env>
 *                <name1>value1</name>
 *                <name2>value2</name>
 *                ...
 *             <env>
 *  - dir: an optional working directory
 *
 * @author Michael Adams
 * Date: 18/06/2008
 */
public class ShellExecution extends AbstractCodelet {

    Process _proc = null;

    // Constructor
    public ShellExecution() {
        super();
        setDescription("This codelet executes an external program. Required parameters:<br> " +
                       "Inputs: command (type String, required)<br>" +
                       "&nbsp;&nbsp;&nbsp; env (attrib=value pairs, optional)<br>" +
                       "&nbsp;&nbsp;&nbsp; dir (type String, optional)<br>" +
                       "Output: result (type String)");
    }


    /**
     * Base override. Executes the codelet
     * @param inData the input data
     * @param inParams a list of input parameters
     * @param outParams a list of output parameters
     * @return the completed output data for the workitem
     * @throws CodeletExecutionException
     */
    public Element execute(Element inData, List<YParameter> inParams,
                           List<YParameter> outParams) throws CodeletExecutionException {
        final int BUF_SIZE = 8192;
        setInputs(inData, inParams, outParams);
        List<String> cmd = createCommandList((String) getParameterValue("command"));
        StringWriter out = new StringWriter(BUF_SIZE);
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            handleOptionalParameters(pb, inData);                // env and working dir

            _proc = pb.start();

            // get the result of the process execution
            InputStream is = _proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[BUF_SIZE];
            int count;

            while ((count = isr.read(buffer)) > 0)
               out.write(buffer, 0, count);

            isr.close();

            // set and return the output
            setParameterValue("result", out.toString());
            return getOutputData();
        }
        catch (Exception e) {
            throw new CodeletExecutionException("Exception executing shell process '" +
                                   cmd + "': " + e.getMessage());
        }
    }


    private List<String> createCommandList(String cmd) {
        if (cmd == null) return null;
        return Arrays.asList(cmd.split("\\s+"));
    }



    /**
     *
     * @param pb an instantiated ProcessBuilder object
     * @param inData the input data
     */
    private void handleOptionalParameters(ProcessBuilder pb, Element inData) {

        // adjust environment vars if required
        Element envElem = inData.getChild("env");
        if (envElem != null) {
            Map<String, String> env = pb.environment();
            List<Element> addToEnvList = envElem.getChildren();
            if (addToEnvList != null) {
                for (Element child : addToEnvList) {
                    env.put(child.getName(), child.getText());
                }
            }
        }

        // change working dir if specified
        Element dir = inData.getChild("dir");
        if (dir != null) {
            pb.directory(new File(dir.getText())) ;
        }
    }


    public void cancel() {
        if (_proc != null) _proc.destroy();
    }


    public List<YParameter> getRequiredParams() {
        List<YParameter> params = new ArrayList<YParameter>();

        YParameter param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName("string", "command", XSD_NAMESPACE);
        param.setDocumentation("The command line");
        params.add(param);

        param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        param.setDataTypeAndName("string", "result", XSD_NAMESPACE);
        param.setDocumentation("The result of the command's execution");
        params.add(param);
        return params;
    }
    
}
