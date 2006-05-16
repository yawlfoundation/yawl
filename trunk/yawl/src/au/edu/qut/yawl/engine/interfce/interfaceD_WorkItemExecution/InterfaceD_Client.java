/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce.interfaceD_WorkItemExecution;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.engine.interfce.Interface_Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import org.jdom.JDOMException;

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
    public static String executePost(String urlStr, Map paramsMap, WorkItemRecord attribute) throws IOException {
        StringBuffer result = new StringBuffer();
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
