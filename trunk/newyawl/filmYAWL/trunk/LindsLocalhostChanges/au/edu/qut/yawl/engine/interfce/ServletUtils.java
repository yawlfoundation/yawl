/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 
 * @author Lachlan Aldred
 * Date: 22/01/2004
 * Time: 14:14:02
 * 
 */
public class ServletUtils {

/*    public static EngineGateway init(ServletContext application) {
        String rmiSvrNm = application.getInitParameter("RMIServerName");
        EngineGateway engine = null;
        try {
            engine = (EngineGatewayImpl) Naming.lookup(rmiSvrNm);
        } catch (Exception e) {
            System.out.println(
                "\n\n" +
                "#########################################################################\n" +
                "###################      Warning From YAWL Engine     ###################\n" +
                "#########################################################################\n" +
                "####                                                                     \n" +
                "####                                                                     \n" +
                "####            Interface B of the YAWL Engine was trying to look        \n" +
                "####            up an RMI Registry for a YAWL RMI server ("+rmiSvrNm+")  \n" +
                "####            and failed to find it.  This is either because:          \n" +
                "####               a) RMI Registry has not been launched.                \n" +
                "####               b) The value ["+rmiSvrNm+"] of 'RMIServerName' inside \n" +
                "####                      web.xml was incorrect.                         \n" +
                "####               c) The engine is not yet successfully launched in     \n" +
                "####                      'networked mode'.                              \n" +
                "####                                                                     \n" +
                "####                                                                     \n" +
                "#########################################################################\n" +
                "#########################################################################\n" +
                "#########################################################################\n" +
                "\n\n");
        }
        return engine;
    }*/


    protected static PrintWriter prepareResponse(HttpServletResponse response) throws IOException {
        response.setContentType("text/xml");
        PrintWriter outputWriter = response.getWriter();
        return outputWriter;
    }


    protected static void finalizeResponse(PrintWriter outputWriter, StringBuffer output) {
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
    }


    protected static boolean validURL(HttpServletRequest request) {
        String url = request.getRequestURI();
        return "/yawl/".equals(url);
    }


    public static void doNotFound(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html");
        try {
            PrintWriter outputWriter = response.getWriter();

            String outputStr = "<html><head><title>404 Not Found</title>" +
                    "<style>" +
                    "<!--body {font-family: arial,sans-serif}div.nav {margin-top: 1ex}" +
                    "div.nav A {font-size: 10pt; font-family: arial,sans-serif}" +
                    "span.nav {font-size: 10pt; font-family: arial,sans-serif; font-weight: bold}" +
                    "div.nav A,span.big {font-size: 12pt; color: #0000cc}" +
                    "div.nav A {font-size: 10pt; color: black}" +
                    "A.l:link {color: #6f6f6f}A.u:link {color: green}//-->" +
                    "</style>" +
                    "</head><body text=#000000 bgcolor=#ffffff>" +
                    "<table border=0 cellpadding=2 cellspacing=0 width=100%>" +
                    "<tr>" +
                    "   <td rowspan=3 width=1% nowrap>" +
                    "   <b><font face=times color=Green size=10>Y</font>" +
                    "   <font face=times color=Yellow size=10>A</font>" +
                    "   <font face=times color=Blue size=10>W</font>" +
                    "   <font face=times color=Orange size=10>L</font>" +
                    "   &nbsp;&nbsp;</b>" +
                    "   <td>&nbsp;</td>" +
                    "</tr><tr>" +
                    "   <td bgcolor=#3366cc>" +
                    "   <font face=arial,sans-serif color=#ffffff><b>Error</b></td>" +
                    "</tr><tr>" +
                    "   <td>&nbsp;</td>" +
                    "</tr>" +
                    "</table>" +
                    "<blockquote>" +
                    "<H1>Not Found</H1>The requested URL <code>" + request.getRequestURI() +
                    "<code> was not found on this server.<p>" +
                    "</blockquote>" +
                    "<table width=100% cellpadding=0 cellspacing=0>" +
                    "<tr><td bgcolor=#3366cc><img alt=\"\" width=1 height=4></td></tr>" +
                    "</table></body></html>";
            outputWriter.print(outputStr);
            outputWriter.flush();
            outputWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
