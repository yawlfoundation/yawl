/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author Lachlan Aldred
 * Date: 22/03/2004
 * Time: 17:49:42
 * 
 */
public class Interface_Client {
    // handles all of the Http Client code
	public static String executePost(String urlStr, Map paramsMap) throws IOException {
		return executeRequest(urlStr, "POST", paramsMap);
	}
	
    private static String executeRequest(String urlStr, String requestMethod, Map paramsMap) throws IOException {
        StringBuffer result = new StringBuffer();
        HttpURLConnection connection = null;

        URL url;
        if( "GET".equals( requestMethod ) ) {
        	url = new URL( urlStr + "?" + writeParams( paramsMap ) );
        }
        else {
        	url = new URL(urlStr);
        }
        

        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput("POST".equals( requestMethod ));
        connection.setDoInput(true);
        connection.setRequestMethod(requestMethod);

        /*
         * Inserted by Tore to ensure that we can
         * check timeouts on read operations occuring
         * for POSTS on location which are running
         * but not accepting the post
         * */
        connection.setReadTimeout(10000);
        //send query
        if( "POST".equals( requestMethod ) ) {
        	PrintWriter out = new PrintWriter(connection.getOutputStream());
        	out.print( writeParams( paramsMap ) );
        	out.flush();
        	out.close();
        }
        //retrieve reply
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        String newline = System.getProperty("line.separator");
        while ((inputLine = in.readLine()) != null) {
            result.append(inputLine).append(newline);
        }
        //clean up
        in.close();
        connection.disconnect();

        String msg = result.toString();
        if( "GET".equals( requestMethod ) ) {
        	return msg;
        }
        else {
        	return stripOuterElement( msg );
        }
    }

    // handles all of the Http Client code
    public static String executeGet(String urlStr, Map paramsMap) throws IOException {
    	return executeRequest(urlStr, "GET", paramsMap);
    }
    
    private static String writeParams( Map params ) {
    	StringBuilder builder = new StringBuilder();
    	Iterator<String> paramIter = params.keySet().iterator();
    	while( paramIter.hasNext() ) {
    		String key = paramIter.next();
            Object paramObj = params.get(key);
            String paramVal;
            if(paramObj == null) {
                new RuntimeException("parameter named '" + key + "' had a null value!")
                    .fillInStackTrace().printStackTrace( System.out );
                paramVal = "";
            }
            else {
                paramVal = paramObj.toString();
            }
            try {
				builder.append( key + "=" + URLEncoder.encode( paramVal, "UTF-8" ) );
			}
			catch( UnsupportedEncodingException e ) {
				// won't happen
				throw new Error( e );
			}
            if (paramIter.hasNext()) {
                builder.append('&');
            }
        }
        return builder.toString();
    }

    protected static String stripOuterElement(String inputXML) {
        if (inputXML != null) {
            int beginClipping = inputXML.indexOf(">") + 1;
            int endClipping = inputXML.lastIndexOf("<");
            if (beginClipping >= 0 && endClipping >= 0) {
            	System.out.println( "front-clip:" + inputXML.substring( 0, beginClipping ) );
            	System.out.println( "back-clip:" + inputXML.substring( endClipping ) );
                inputXML = inputXML.substring(beginClipping, endClipping);
                return inputXML;
            }
        }
        return inputXML;
    }

    public static boolean successful(String message) {
        return message != null
                &&
                message.indexOf("<failure>") == -1
                &&
                message.length() > 0;
    }
}
