package org.yawlfoundation.yawl.editor.thirdparty.engine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Author: Michael Adams
 * Creation Date: 18/02/2009
 */
public class ServerLookup {

    public static boolean isReachable(String serviceURI)
            throws IOException {
        URL url = new URL(serviceURI);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("HEAD");
        httpConnection.setConnectTimeout(2000);     // max wait 2 secs

        // if the URI is down, the next line throws an exception
        httpConnection.getResponseCode();

        // if it gets here, the connection is ok, the URI is up
        return true;
     }

}
