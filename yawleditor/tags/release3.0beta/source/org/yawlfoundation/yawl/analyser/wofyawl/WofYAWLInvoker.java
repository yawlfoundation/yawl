/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.analyser.wofyawl;

import org.yawlfoundation.yawl.analyser.YAnalyserOptions;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WofYAWLInvoker {

    /**
     * Returns the raw XML output from wofyawl that is generated when wofyawl is
     * run against the input engine XML file.
     * @param specXML
     * @return the raw XML wofyawl output
     */
    public String getAnalysisResults(String specXML, YAnalyserOptions options) {
        String wofYawlExecutableLocation = options.getWofYawlExecutableLocation();
        if (wofYawlExecutableLocation == null || !fileExists(wofYawlExecutableLocation)) {
            return wrapResult("The WofYAWL analysis tool is not installed or not " +
                    "available for your OS.");
        }

        File specFile = StringUtil.stringToTempFile(specXML);
        if (specFile == null) {
            return wrapResult("WofYAWL Unexpected Error: Problem parsing specification" +
                    " file for analysis.");
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(buildCommandList(options,
                    specFile.getAbsolutePath()));
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            // get the result of the process execution
            return wrapResult(StringUtil.streamToString(proc.getInputStream()));
        }
        catch (Exception e) {
            return wrapResult("WofYAWL Unexpected Error: The wofYAWL analysis tool " +
                    "did not return any analysis information.");
        }
        finally {
            specFile.delete();
        }
    }

    private String getParameterForBehaviouralAnalysis(YAnalyserOptions options) {
        return getOptionAsSwitch(options.isWofBehavioural(), 'b');
    }


    private String getParameterForStructuralAnalysis(YAnalyserOptions options) {
        return getOptionAsSwitch(options.isWofStructural(), 's');
    }


    private String getParameterForExtendedCoverability(YAnalyserOptions options) {
        return getOptionAsSwitch(options.isWofExtendedCoverabiity(), '1');
    }


    private String getOptionAsSwitch(boolean on, char switchChar) {
        char[] optionSwitch = { (on ? '+' : '-'), switchChar };
        return new String(optionSwitch);
    }


    private List<String> buildCommandList(YAnalyserOptions options, String specFilePath) {
        List<String> cmdList = new ArrayList<String>();
        cmdList.add(options.getWofYawlExecutableLocation());
        cmdList.add(getParameterForBehaviouralAnalysis(options));
        cmdList.add(getParameterForStructuralAnalysis(options));
        cmdList.add(getParameterForExtendedCoverability(options));
        cmdList.add(specFilePath);
        return cmdList;
    }


    private boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    private String wrapResult(String result) {
        return StringUtil.wrap(result, "wofYawlAnalysisResults");
    }

}
