<%
/*
@author mark dimon mailto:mark@markdimon.com

this code simply returns the supplied xml instance data back to the client
*/
        Reader reader = request.getReader();
        char[] buf = new char[request.getContentLength()];
        reader.read( buf );
        String s = new String(buf);
        s = s.toLowerCase(); //Sanity Check , change to lowercase
        response.setContentType("text/xml");
        out.write( s );
%>
<%@ page import="java.io.*" %>