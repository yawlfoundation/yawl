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

import java.util.List;
import java.util.Random;

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
            Thread.sleep(randomWait);
        }
        catch (Exception e) {
            e.printStackTrace();
            // nothing to do
        }

        // set the output result. setParameterValue requires the result to be a String.
        setParameterValue("waitTime", String.valueOf(randomWait));

        // return the Element created in the bae class and containing the result.
        return getOutputData();
    }

}