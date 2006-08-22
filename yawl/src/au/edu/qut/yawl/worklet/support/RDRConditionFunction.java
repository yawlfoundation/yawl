/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */
package au.edu.qut.yawl.worklet.support;

import au.edu.qut.yawl.worklet.exception.ExceptionService;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.util.JDOMConversionTools;

import java.util.*;

import org.jdom.Element;
import org.apache.log4j.*;


/**
 *  A class that will allow developers to define functions that can be called via
 *  conditional expressions in rules.
 *
 * To successfully add a function:
 *  1. Add the function to the 'function definitions' section
 *  2. Ensure the function is declared 'public static'
 *  3. Add the function's name added to the list of '_functionNames'.
 *  4. Add a mapping for the function to the 'execute' method, following the examples
 *  5. Ensure the function returns a String value.
 *
 * Once the function is added, it can be used in any rule's conditional expression
 *
 * Currently only a STUB witch a couple of examples
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.8 04/07/2006
 */

public class RdrConditionFunction {

    private static final ExceptionService _es = ExceptionService.getInst();
    private static Logger _log =
                        Logger.getLogger("au.edu.qut.yawl.worklet.support.RdrConditionFunction");


    // add the name of each defined function here
    public static final String[] _functionNames = { "max",
                                                    "min",
                                                    "isNotCompleted",
                                                    "today"} ;

    public static boolean isRegisteredFunction(String s) {
        for (String _functionName : _functionNames)
            if (s.equalsIgnoreCase(_functionName)) return true;

        return false;                               // not a function name
    }

    /*****************************************************************************/


    // EXECUTE METHOD //

    // Note: all args are passed as Strings
    public static String execute(String name, HashMap args) {
        _log.setLevel(Level.ERROR);
//        _log.info("execute, name = " + name + ", args = " + args.toString()) ;
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
            return max(x, y);
        }
        else if (name.equalsIgnoreCase("today")) {
            return today();
        }
        return null ;
    }

    /*****************************************************************************/


    // FUNCTION DEFINITIONS //

    public static String max(int x, int y) {
        if (x >= y) return String.valueOf(x) ;
        else return String.valueOf(y) ;
    }

    public static String min(int x, int y) {
        if (x <= y) return String.valueOf(x) ;
        else return String.valueOf(y) ;
    }


    public static String isNotCompleted(String itemInfo) {
        _log.info("in isNotCompleted, info = " + itemInfo);
        Element eItem = JDOMConversionTools.stringToElement(itemInfo);
        String status = eItem.getChildText("status");
        _log.info("in isNotCompleted, status = " + status);
        return String.valueOf(! isFinishedStatus(status) );
    }

    public static String today() {
        Date now = new Date() ;
        return now.toString();
    }

    /*****************************************************************************/

    // IMPLEMENTATION //

    /** returns true if the status passed is one of the completed statuses */
    private static boolean isFinishedStatus(String status) {
        return status.equals(YWorkItem.Status.Complete) ||
               status.equals(YWorkItem.Status.ForcedComplete) ||
               status.equals(YWorkItem.Status.Failed) ;
    }

    /** extract the specified argument and reutrns its integer value */
    private static  int getArgAsInt(HashMap args, String var) {
        String valStr = (String) args.get(var);
        return Integer.parseInt(valStr);
    }

}
