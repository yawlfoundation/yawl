<%@ page import="java.io.Reader,
                 java.io.InputStreamReader,
                 java.util.Enumeration,
                 java.text.DateFormat,
                 java.util.Date"%>
<%@ page session="true" %>
<%@ page errorPage="error.jsp" %>
<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Instance Data submitted</title>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/forms/styles/xforms.css"/>
     </head>
    <body>
        <%
            // create a buffer longer than binary content to have enough space for character conversion
            StringBuffer buffer = new StringBuffer((int) (request.getContentLength() * 1.5));

            // convert binary content to character data in specified encoding
            Reader reader = new InputStreamReader(request.getInputStream(), request.getCharacterEncoding());

            int i = reader.read();
            while (i > -1) {
                switch (i) {
                    case '<':
                        buffer.append("&lt;");
                        break;
                    default:
                        buffer.append((char) i);
                }
                i = reader.read();
            }
        %>
        <fieldset class="group full-group">
            <legend class="label">Instance Data</legend>
            <div style="color: darkred;"><pre><%=buffer%></pre></div>
        </fieldset>

        <fieldset class="group full-group">
            <legend class="label">Request</legend>
            <div class="output enabled readwrite optional valid"><label class="label">Request Method</label><span class="value"><%= request.getMethod() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Request URI</label><span class="value"><%= request.getRequestURI() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Request Protocol</label><span class="value"><%= request.getProtocol() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Servlet Path</label><span class="value"><%= request.getServletPath() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Path Info</label><span class="value"><%= request.getPathInfo() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Path Translated</label><span class="value"><%= request.getPathTranslated() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Query String</label><span class="value"><%= request.getQueryString() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Content Length</label><span class="value"><%= request.getContentLength() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Content Type</label><span class="value"><%= request.getContentType() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Server Name</label><span class="value"><%= request.getServerName() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Server Port</label><span class="value"><%= request.getServerPort() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Remote User</label><span class="value"><%= request.getRemoteUser() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Remote Address</label><span class="value"><%= request.getRemoteAddr() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Remote Host</label><span class="value"><%= request.getRemoteHost() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Authorization Scheme</label><span class="value"><%= request.getAuthType() %></span></div>
        </fieldset>

        <fieldset class="group full-group">
            <legend class="label">Request Headers</legend>
            <%
                Enumeration headers = request.getHeaderNames();
                while (headers.hasMoreElements()) {
                   String name = headers.nextElement().toString(); %>
            <div class="output enabled readwrite optional valid"><label class="label"><%= name %></label><span class="value"><%= request.getHeader(name) %></span></div>
            <%
                }
            %>
        </fieldset>

        <fieldset class="group full-group">
            <legend class="label">Session</legend>
            <div class="output enabled readwrite optional valid"><label class="label">Session ID</label><span class="value"><%= session.getId() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Creation time</label><span class="value"><%= DateFormat.getDateTimeInstance().format(new Date(session.getCreationTime())) %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Last accessed</label><span class="value"><%= DateFormat.getDateTimeInstance().format(new Date(session.getLastAccessedTime())) %></span></div>
        </fieldset>

        <fieldset class="group full-group">
            <legend class="label">Session Attributes during submit</legend>
            <%
                Enumeration variables = session.getAttributeNames();
                while (variables.hasMoreElements()) {
                    String name = variables.nextElement().toString(); %>
            <div class="output enabled readwrite optional valid"><label class="label"><%= name %></label><span class="value"><%= session.getAttribute(name) %></span></div>
            <%
                }
            %>
        </fieldset>

        <fieldset class="group full-group">
            <legend class="label">Response</legend>
            <div class="output enabled readwrite optional valid"><label class="label">Content Type</label><span class="value"><%= response.getContentType() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Character Encoding</label><span class="value"><%= response.getCharacterEncoding() %></span></div>
            <div class="output enabled readwrite optional valid"><label class="label">Locale</label><span class="value"><%= response.getLocale() %></span></div>

        </fieldset>

        <center>
            <font face="sans-serif"><a href="<%=request.getContextPath()%>/jsp/forms.jsp">Back to Samples</a></font>
        </center>
    </body>
</html>
