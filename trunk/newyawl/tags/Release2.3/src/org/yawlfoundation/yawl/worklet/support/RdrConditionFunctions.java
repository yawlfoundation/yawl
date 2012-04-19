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

package org.yawlfoundation.yawl.worklet.support;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.Date;
import java.util.Map;


/**
 *  A class that will allow developers to define functions that can be called via
 *  conditional expressions in rules.
 *
 * To successfully add a function:
 *  1. Add the function (method body) to the 'function definitions' section
 *  2. Ensure the function (method) is declared 'private static'
 *  3. Add the function's name added to the list of '_functionNames'.
 *  4. Add a mapping for the function to the 'execute' method, following the examples
 *  5. Ensure the function returns a String value.
 *
 * Once the function is added, it can be used in any rule's conditional expression
 *
 * Currently only a STUB with a couple of examples
 *
 *  @author Michael Adams
 *  v0.8 04-097/2006
 */
public class RdrConditionFunctions {

    // HEADER //

    // add the name of each defined function here
    public static final String[] _functionNames = { "max",
                                                    "min",
                                                    "isNotCompleted",
                                                    "hasTimerExpired",
                                                    "today"} ;

    public static boolean isRegisteredFunction(String name) {
        for (String functionName : _functionNames) {
            if (name.equalsIgnoreCase(functionName)) return true;
        }
        return false;                               // not a function name
    }

    /*****************************************************************************/

    /**
     * Executes the named function, using the supplied arguments
     * @param name the name of the function to execute
     * @param args a map of String key-value pairs. Note that every map will contain
     *             a key called 'this' that has as its value an xml String of the
     *             workitem being evaluated
     * @return the function's result
     */
    public static String execute(String name, Map<String, String> args) {
        if (name.equalsIgnoreCase("isNotCompleted")) {
            String taskInfo = args.get("this");
            return isNotCompleted(taskInfo);
        }
        else if (name.equalsIgnoreCase("hasTimerExpired")) {
            String taskInfo = args.get("this");
            return hasTimerExpired(taskInfo);
        }
        else if (name.equalsIgnoreCase("max")) {
            int x = getArgAsInt(args, "x");
            int y = getArgAsInt(args, "y");
            return max(x, y);
        }
        else if (name.equalsIgnoreCase("min")) {
            int x = getArgAsInt(args, "x");
            int y = getArgAsInt(args, "y");
            return min(x, y);
        }
        else if (name.equalsIgnoreCase("today")) {
            return today();
        }
        return null ;
    }

    /*****************************************************************************/


    // FUNCTION DEFINITIONS //

    private static String max(int x, int y) {
        if (x >= y) return String.valueOf(x) ;
        else return String.valueOf(y) ;
    }


    private static String min(int x, int y) {
        if (x <= y) return String.valueOf(x) ;
        else return String.valueOf(y) ;
    }


    private static String isNotCompleted(String itemInfo) {
        Element eItem = JDOMUtil.stringToElement(itemInfo);
        String status = eItem.getChildText("status");
        return String.valueOf(! isFinishedStatus(status) );
    }


    private static String hasTimerExpired(String itemInfo) {
        Element eItem = JDOMUtil.stringToElement(itemInfo);
        return String.valueOf((eItem.getChildText("timerexpiry") != null));
    }


    private static String today() {
        Date now = new Date() ;
        return now.toString();
    }

    /*****************************************************************************/

    // IMPLEMENTATION //

    /** returns true if the status passed is one of the completed statuses */
    private static boolean isFinishedStatus(String status) {
        return status.equals(WorkItemRecord.statusComplete) ||
               status.equals(WorkItemRecord.statusForcedComplete) ||
               status.equals(WorkItemRecord.statusFailed) ;
    }


    /** extract the specified argument and returns its integer value */
    private static int getArgAsInt(Map<String, String> args, String var) {
        String valStr = args.get(var);
        return valStr != null ? Integer.parseInt(valStr) : -1;
    }

}
