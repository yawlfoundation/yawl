/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.miscellaneousPrograms;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @author Lachlan Aldred
 * Date: 18/02/2004
 * Time: 12:32:20
 * 
 */
public class TestURLs {
    public static void main(String[] args) {
        URL u1;
        try {
            u1 = new URL("http://localhost:8080/yawl/ia/B.xml!@#$%^&*():t1/http://dfsedf.dfs.dfsd");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
