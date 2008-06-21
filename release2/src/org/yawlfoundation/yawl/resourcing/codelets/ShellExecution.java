package org.yawlfoundation.yawl.resourcing.codelets;

import org.jdom.Element;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 18/06/2008
 */
public class ShellExecution extends AbstractCodelet {

    public ShellExecution() {
        super();
        setDescription("This codelet executes an external program. Required parameters:\n " +
                       "\tInputs: command (type String)\n" +
                       "\tOutput: result (type String)");
    }

    public Element execute(Element inData, List inParams, List outParams)
                                                     throws CodeletExecutionException {

        setInputs(inData, inParams, outParams);
        String cmd = (String) getParameterValue("command");
        String result = "";
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process proc = pb.start();

            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int data = 0 ;
            int chrsRead = 0;
            char[] buf = new char[512];
            while (data != -1) {
                data = isr.read(buf);
                result += new String(buf) ;
                chrsRead += data;
            } 
            isr.close();

            setParameterValue("result", result.substring(0, chrsRead + 1));
            return getOutputData();
        }
        catch (Exception e) {
            throw new CodeletExecutionException("Exception executing shell process '" +
                                   cmd +"': " + e.getMessage());
        }
    }
    
}
