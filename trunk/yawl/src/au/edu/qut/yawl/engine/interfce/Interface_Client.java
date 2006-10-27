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
	
	public static void main(String[] args) throws Throwable {
		System.out.println( URLDecoder.decode("org.hibernate.SessionException%3A+Session+is+closed%0D%0A%09at+org.hibernate.jdbc.JDBCContext.connection%28JDBCContext.java%3A115%29%0D%0A%09at+org.hibernate.transaction.JDBCTransaction.rollbackAndResetAutoCommit%28JDBCTransaction.java%3A183%29%0D%0A%09at+org.hibernate.transaction.JDBCTransaction.rollback%28JDBCTransaction.java%3A162%29%0D%0A%09at+au.edu.qut.yawl.persistence.YAWLTransactionAdvice.afterThrowing%28YAWLTransactionAdvice.java%3A68%29%0D%0A%09at+sun.reflect.NativeMethodAccessorImpl.invoke0%28Native+Method%29%0D%0A%09at+sun.reflect.NativeMethodAccessorImpl.invoke%28Unknown+Source%29%0D%0A%09at+sun.reflect.DelegatingMethodAccessorImpl.invoke%28Unknown+Source%29%0D%0A%09at+java.lang.reflect.Method.invoke%28Unknown+Source%29%0D%0A%09at+org.springframework.aop.framework.adapter.ThrowsAdviceInterceptor.invokeHandlerMethod%28ThrowsAdviceInterceptor.java%3A138%29%0D%0A%09at+org.springframework.aop.framework.adapter.ThrowsAdviceInterceptor.invoke%28ThrowsAdviceInterceptor.java%3A123%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor.invoke%28AfterReturningAdviceInterceptor.java%3A51%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke%28MethodBeforeAdviceInterceptor.java%3A53%29%0D%0A%09at+org.springframework.aop.framework.ReflectiveMethodInvocation.proceed%28ReflectiveMethodInvocation.java%3A170%29%0D%0A%09at+org.springframework.aop.framework.JdkDynamicAopProxy.invoke%28JdkDynamicAopProxy.java%3A209%29%0D%0A%09at+%24Proxy42.unloadSpecification%28Unknown+Source%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.EngineGatewayImpl.unloadSpecification%28EngineGatewayImpl.java%3A738%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.InterfaceA_EngineBasedServer.processPostQuery%28InterfaceA_EngineBasedServer.java%3A226%29%0D%0A%09at+au.edu.qut.yawl.engine.interfce.InterfaceA_EngineBasedServer.doPost%28InterfaceA_EngineBasedServer.java%3A101%29%0D%0A%09at+javax.servlet.http.HttpServlet.service%28HttpServlet.java%3A709%29%0D%0A%09at+javax.servlet.http.HttpServlet.service%28HttpServlet.java%3A802%29%0D%0A%09at+org.apache.catalina.core.ApplicationFilterChain.internalDoFilter%28ApplicationFilterChain.java%3A252%29%0D%0A%09at+org.apache.catalina.core.ApplicationFilterChain.doFilter%28ApplicationFilterChain.java%3A173%29%0D%0A%09at+org.apache.catalina.core.StandardWrapperValve.invoke%28StandardWrapperValve.java%3A213%29%0D%0A%09at+org.apache.catalina.core.StandardContextValve.invoke%28StandardContextValve.java%3A178%29%0D%0A%09at+org.apache.catalina.core.StandardHostValve.invoke%28StandardHostValve.java%3A126%29%0D%0A%09at+org.apache.catalina.valves.ErrorReportValve.invoke%28ErrorReportValve.java%3A105%29%0D%0A%09at+org.apache.catalina.core.StandardEngineValve.invoke%28StandardEngineValve.java%3A107%29%0D%0A%09at+org.apache.catalina.connector.CoyoteAdapter.service%28CoyoteAdapter.java%3A148%29%0D%0A%09at+org.apache.coyote.http11.Http11AprProcessor.process%28Http11AprProcessor.java%3A831%29%0D%0A%09at+org.apache.coyote.http11.Http11AprProtocol%24Http11ConnectionHandler.process%28Http11AprProtocol.java%3A639%29%0D%0A%09at+org.apache.tomcat.util.net.AprEndpoint%24Worker.run%28AprEndpoint.java%3A1196%29%0D%0A%09at+java.lang.Thread.run%28Unknown+Source%29%0D%0A", "UTF-8"));
	}

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
