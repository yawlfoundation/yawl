/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.codelets;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

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
 * @author: Michael Adams
 * Date: 18/06/2008
 */
public class ShellExecution extends AbstractCodelet {

    // Constructor
    public ShellExecution() {
        super();
        setDescription("This codelet executes an external program. Required parameters:<br> " +
                       "Input: command (type String, required)<br>" +
                       "       env     (attrib=value pairs, optional)<br>" +
                       "       dir     (type String, optional)<br>" +
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
        setInputs(inData, inParams, outParams);
        String cmd = (String) getParameterValue("command");
        StringWriter out = new StringWriter(8192);
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            handleOptionalParameters(pb, inData);                // env and working dir

            Process proc = pb.start();

            // get the result of the process execution
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[8192];
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
            List addToEnvList = envElem.getChildren();
            if (addToEnvList != null) {
                for (Object envVar : addToEnvList) {
                    Element child = (Element) envVar;
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
    
}
