/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.interfce;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Lachlan Aldred
 * Date: 22/01/2004
 * Time: 14:14:02
 * 
 */
public class ServletUtils {

    public static OutputStreamWriter prepareResponse(HttpServletResponse response) throws IOException {
        response.setContentType("text/xml; charset=UTF-8");
        return new OutputStreamWriter(response.getOutputStream(), "UTF-8");
    }

    
    public static void finalizeResponse(OutputStreamWriter outputWriter, String output) throws IOException {
        outputWriter.write(output);
        outputWriter.flush();
        outputWriter.close();
    }


    public static void finalizeResponse(OutputStreamWriter outputWriter, StringBuilder output) throws IOException {
        finalizeResponse(outputWriter, output.toString());
    }


    public static boolean validURL(HttpServletRequest request) {
        String url = request.getRequestURI();
        return "/yawl/".equals(url);
    }


    public static void doNotFound(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html; charset=UTF-8");
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

    /**
     * Url-encodes data to make it safe for HTTP transport
     *
     * @param   s    data to be encoded for HTTP transport
     * @return  encoded data
     */
    public static String urlEncode(String s) {
        if (s == null) return s;
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            return s;
        }
    }

    /**
     * Url-decodes data after possible encoding for transport
     *
     * @param   s    data to be decoded from HTTP request
     * @return  decoded data
     */
    public static String urlDecode(String s) {
        if (s == null) return s;
        try {
            return URLDecoder.decode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            return s;
        }
    }


    public static Map<String, String> getRequestParams(HttpServletRequest request) throws IOException {
        ServletInputStream in = request.getInputStream();

                // read spec into a byte array wrapped in a ByteBuffer - can't do it in
                // one read because of a buffer size limit in the InputStream
                byte[] contents = new byte[request.getContentLength()];
                ByteBuffer bytes = ByteBuffer.wrap(contents);
                byte[] buffer = new byte[8192];

                // read chunks from the stream and append them to the ByteBuffer
                int bytesRead;
                while ((bytesRead = in.read(buffer, 0, buffer.length)) > 0) {
                    bytes.put(buffer, 0, bytesRead);
                }

                // convert the bytes to a string with the right charset
                String paramStr = new String(contents, "UTF-8");

        // split into params & rebuild map
        Map<String, String> result = new HashMap<String, String>();
        String[] params = paramStr.split("&");
        for (String param : params) {
            String[] parts = param.split("=");
            if (parts.length == 2) {
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }
}
