package org.yawlfoundation.yawl.editor.thirdparty.engine;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Author: Michael Adams
 * Creation Date: 18/02/2009
 */
public class ServerLookup {

    public static boolean isReachable(String serviceURI)
            throws IOException {
        URL url = new URL(serviceURI);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);

        // if the URI is down, the next line throws an exception
        connection.getOutputStream();

        // if it gets here, the connection is ok, the URI is up
        return true;
     }

}
