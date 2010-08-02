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

package org.yawlfoundation.yawl.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A simple static checker that (1) checks that the url string passed in is a valid
 * URL, and then (2) that the server at the URL is responsive.
 *
 * Author: Michael Adams
 * Creation Date: 7/05/2009
 */
public class HttpURLValidator {

    /**
     * validaets a url passed as a String
     * @param urlStr the url to validate
     * @return a message describing the success of failure of the validation
     */
    public static String validate(String urlStr) {
        URL url ;

        // escape early if urlStr is obviously bad
        if (urlStr == null)
            return getErrorMessage("URL is null");
        else if (! urlStr.startsWith("http://"))
            return getErrorMessage("URL does not begin with 'http://'") ;

        try {

            // this will throw an exception if the URL is invalid
            url = new URL(urlStr);

            // ok - let's see if and how it responds
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("HEAD");
            httpConnection.connect();
            int response = httpConnection.getResponseCode();
            if (response >= 300)                           // indicates some error
                return getErrorMessage(response + " " + httpConnection.getResponseMessage());
        }
        catch (MalformedURLException mue) {
            return getErrorMessage("Malformed URL") ;
        }
        catch (IOException e) {
            return getErrorMessage("IO Exception when validating URL") ;
        }

        return "<success/>";                             // no errors and responded 'OK'
    }


    private static String getErrorMessage(String msg) {
        return StringUtil.wrap(msg, "failure");
    }


    public synchronized static String pingUntilAvailable(String urlStr, int timeoutSeconds) {
        String result = null;
        int timeoutMsecs = timeoutSeconds * 1000;
        int expiredMsecs = 0;
        long period = 100;
        while (expiredMsecs <= timeoutMsecs) {
            result = validate(urlStr);
            if (result.equals("<success/>")) break;
            try {
                Thread.sleep(period);
                expiredMsecs += period;
            }
            catch (InterruptedException ie) {
                result = getErrorMessage("Exception while waiting for URL to be available.");
                break;
            }
        }
        return result;
    }

}
