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

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

/**
 * A simple codelet example demonstrating usage. Much of the work is done in the
 * abstract ancestor, but of course those base classes do not need to be relied upon -
 * all the work my be done in this class if desired.
 *
 * The Element passed back to the caller of the execute method must contain the results
 * of the execution. It should be constructed as:
 *      <root>
 *         <paramName>paramValue</paramName>
 *         <paramName>paramValue</paramName>
 *         ......
 *      </root>
 *
 * "root" can be called anything; each paramName must be the same as one of the
 * triggering workitem's output parameters.
 *
 * @author Michael Adams
 * Creation Date: 18/06/2008
 */
public class RandomWait extends AbstractCodelet {

    private boolean _cancelled = false;

    public RandomWait() {
        super();
        setDescription("This codelet is a simple example of codelet construction and<br> " +
                       "usage. It waits for a random amount of time before completing.<br>" +
                       "Required parameters:<br> " +
                       "Input: interval (type String) - H, M or S (for hour, minute or second)<br>" +
                       "Input: max (type long) - maximum wait time<br>" +
                       "Output: waitTime (type long) - the time waited.");
    }

    /**
     * The implentation of the abstact class that does the work of this codelet. Note
     * that calls to most of the base class methods may throw a CodeletExecutionException
     * which should be passed back to the caller,
     *
     * @param inData The calling workitem's data
     * @param inParams the workitem's input parameters
     * @param outParams the workitem's output parameters
     * @return the result of the codelet's work as a JDOM Element
     * @throws org.yawlfoundation.yawl.resourcing.codelets.CodeletExecutionException
     */
    public Element execute(Element inData, List<YParameter> inParams,
                           List<YParameter> outParams) throws CodeletExecutionException {

        // set the inputs passed in the base class
        setInputs(inData, inParams, outParams);

        // get the data values required - note that getParameterValue returns the value
        // as a plain Object, which needs to be cast as required.
        String interval;
        long max;
        try {
            interval = (String) getParameterValue("interval");
            max = (Long) getParameterValue("max");
        }
        catch (ClassCastException cce) {
            throw new CodeletExecutionException("Exception casting input values to " +
                                                "required types.") ;
        }

        // do the work of the codelet
        long randomWait = Math.round(new Random().nextDouble() * max) * 1000;   // msecs
        if (interval.equalsIgnoreCase("M"))
            randomWait *= 60;                                // seconds -> minutes
        else if (interval.equalsIgnoreCase("H"))
            randomWait *= 3600;                              // seconds -> hours

        try {
            long expired = 0;
            while ((! _cancelled) && (expired <= randomWait)) {
                Thread.sleep(1000);
                expired += 1000;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            // nothing to do
        }

        // set the output result. setParameterValue requires the result to be a String.
        setParameterValue("waitTime", String.valueOf(randomWait));

        // return the Element created in the base class and containing the result.
        return getOutputData();
    }


    public void cancel() {
        _cancelled = true;
    }


    public List<YParameter> getRequiredParams() {
        List<YParameter> params = new ArrayList<YParameter>();

        YParameter param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName("string", "interval", XSD_NAMESPACE);
        param.setDocumentation("H, M or S (for hour, minute or second)");
        params.add(param);

        param = new YParameter(null, YParameter._INPUT_PARAM_TYPE);
        param.setDataTypeAndName("long", "max", XSD_NAMESPACE);
        param.setDocumentation("Maximum wait time");
        params.add(param);

        param = new YParameter(null, YParameter._OUTPUT_PARAM_TYPE);
        param.setDataTypeAndName("long", "waitTime", XSD_NAMESPACE);
        param.setDocumentation("The time actually waited");
        params.add(param);
        return params;
    }

}