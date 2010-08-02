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

package org.yawlfoundation.yawl.engine.interfce.interfaceD;

import org.jdom.JDOMException;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Sends workitem related messages to a
 *
 * 
 * @author Lachlan Aldred
 * Date: 16/09/2005
 * Time: 15:34:59
 */
public class InterfaceD_Client extends Interface_Client{

    private String _interfaceDServerURI;

    public InterfaceD_Client(String interfaceDServerURI) {
        _interfaceDServerURI = interfaceDServerURI;
    }


    /**
     * Permits the sending of WorkItemRecords.
     * @param urlStr
     * @param paramsMap
     * @param attribute
     * @return A success or failure message
     * @throws IOException
     */
    public String executePost(String urlStr, Map paramsMap, WorkItemRecord attribute) throws IOException {
        StringBuilder result = new StringBuilder();
        HttpURLConnection connection = null;

        URL url = new URL(urlStr);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        //send query
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        Iterator paramKeys = paramsMap.keySet().iterator();
        while (paramKeys.hasNext()) {
            String paramName = (String) paramKeys.next();
            out.print(paramName + "=" + paramsMap.get(paramName));
            if (paramKeys.hasNext()) {
                out.print('&');
            }
        }
        
        if (attribute != null){
        	out.print("&workitem="+attribute.toXML());
        }
        
        out.flush();
        //retrieve reply
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine);
        }
        //clean up
        in.close();
        out.close();
        connection.disconnect();

        String msg = result.toString();
        return stripOuterElement(msg);
    }
    
    
    public String sendWorkItem(WorkItemRecord workitem) throws IOException, JDOMException {
        Map queryMap = new HashMap();
        queryMap.put("workitem", workitem.toXML());
        return  executePost(_interfaceDServerURI + "", queryMap);
    }
}
