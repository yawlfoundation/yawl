/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */
package org.yawlfoundation.yawl.worklet.support;

import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.util.*;

import org.jdom.Element;


/**
 *  A class that will allow developers to define functions that can be called via
 *  conditional expressions in rules.
 *
 * To successfully add a function:
 *  1. Add the function to the 'function definitions' section
 *  2. Ensure the function is declared 'private static'
 *  3. Add the function's name added to the list of '_functionNames'.
 *  4. Add a mapping for the function to the 'execute' method, following the examples
 *  5. Ensure the function returns a String value.
 *
 * Once the function is added, it can be used in any rule's conditional expression
 *
 * Currently only a STUB with a couple of examples
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@yawlfoundation.org
 *  v0.8 04-097/2006
 */
public class RdrConditionFunctions {

    // HEADER //

//    private static Logger _log =
//               Logger.getLogger("org.yawlfoundation.yawl.worklet.support.RdrConditionFunctions");


    // add the name of each defined function here
    public static final String[] _functionNames = { "max",
                                                    "min",
                                                    "isNotCompleted",
                                                    "today"} ;

    public static boolean isRegisteredFunction(String s) {
        for (int i=0; i < _functionNames.length; i++)
           if (s.equalsIgnoreCase(_functionNames[i])) return true ;

        return false;                               // not a function name
    }

    /*****************************************************************************/


    // EXECUTE METHOD //

    // Note: all args are passed as Strings
    public static String execute(String name, HashMap args) {
        if (name.equalsIgnoreCase("isNotCompleted")) {
            String taskInfo = (String) args.get("this");
            return isNotCompleted(taskInfo);
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
    private static int getArgAsInt(HashMap args, String var) {
        String valStr = (String) args.get(var);
        return Integer.parseInt(valStr);
    }

}
