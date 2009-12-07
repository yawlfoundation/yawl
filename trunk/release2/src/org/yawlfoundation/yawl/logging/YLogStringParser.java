package org.yawlfoundation.yawl.logging;

/**
 * Author: Michael Adams
 * Creation Date: 17/04/2009
 *
 * other services can extend this to do their own pre parsing
 *
 * $now$ - current date/time
 * $date$ - current date
 * $time$ - current time
 * \$ - $
 * $taskvar:[name]$ - the value of a task variable [name]
 * $netvar:[name]$ - the value of a net variable [name]
 * $http://[webservice]$ - call the service, insert the result (short timeout)
 *
 * things for the res service:
 * $resource:participant$
 * $resource:role$
 * $resource:position$
 * $resource:capability$
 * $resource:orggroup$
 * $codelet:[name]$
 *
 */
public class YLogStringParser {

    public YLogStringParser() {}

    public static String parse(String s) {
        String result = null;
        if (s != null) {
            result = s.replaceAll("$now$", "the current time");
        }
        return result;
    }
}
