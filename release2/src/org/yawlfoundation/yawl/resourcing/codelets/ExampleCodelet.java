/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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
public class ExampleCodelet extends AbstractCodelet {

    public ExampleCodelet() {
        super();
        setDescription("This codelet is a simple example of codelet construction and<br> " +
                       "usage. It adds two integers. Required parameters:<br> " +
                       "Inputs: a, b (both long types)<br>" +
                       "Output: c (also a long)");
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
     * @throws CodeletExecutionException
     */
    public Element execute(Element inData, List<YParameter> inParams,
                           List<YParameter> outParams) throws CodeletExecutionException {
        // set the inputs passed in the base class
        setInputs(inData, inParams, outParams);

        // get the data values required - note that getParameterValue returns the value
        // as a plain Object, which needs to be cast as required.
        long a, b, c ;
        try {
            a = (Long) getParameterValue("a");
            b = (Long) getParameterValue("b");
        }
        catch (ClassCastException cce) {
            throw new CodeletExecutionException("Exception casting input values to " +
                                                "long types.") ;
        }

        // do the work of the codelet
        c = a + b ;

        // set the output result. setParameterValue requires the result to be a String.
        setParameterValue("c", String.valueOf(c));

        // return the Element created in the base class and containing the result.
        return getOutputData();
    }


    /**
     * All codelets must provide a mechanism to handle workitem (or case) cancellation.
     * In this example nothing need to be done, but in long running codelets some
     * handling of processes etc. may be required.
     */
    public void cancel() {}
}
