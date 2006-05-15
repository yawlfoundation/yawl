<%@ page import="java.io.BufferedReader,
                 java.util.Enumeration,
                 java.text.DateFormat,
                 java.util.Date"%>
<%@ page session="true" %>
<%@ page errorPage="error.jsp" %>
<html>
    <head>
        <title>Instance Data submitted</title>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/styles.css"/>
     </head>
    <body>
        <a href="jsp/forms.jsp"><img src="<%=request.getContextPath()%>/images/chiba50t.gif" border="0" id="chiba-logo"></a>

        <table class="full-group valid readwrite optional enabled" border="0" width="100%">
            <tr><td colspan="2" class="full-group-label label"><b>Instance Data</b></td></tr>
            <%
                char[] readerBuffer = new char[request.getContentLength()];
                BufferedReader bufferedReader = request.getReader();

                int portion = bufferedReader.read(readerBuffer);
                int amount = portion;
                while (amount < readerBuffer.length) {
                    portion = bufferedReader.read(readerBuffer, amount, readerBuffer.length - amount);
                    amount = amount + portion;
                }

                StringBuffer stringBuffer = new StringBuffer((int) (readerBuffer.length * 1.5));
                for (int index = 0; index < readerBuffer.length; index++) {
                    char c = readerBuffer[index];
                    switch (c) {
                        case '<':
                            stringBuffer.append("&lt;");
                            break;
                        default:
                            stringBuffer.append(c);
                    }
                }

                String xml = stringBuffer.toString();
            %>
            <tr><td colspan="2" style="color:darkred;"><pre><%= xml %></pre></td></tr>
        </table>

        <table class="full-group valid readwrite optional enabled" border="0" width="100%">
            <tr><td colspan="2" class="full-group-label label"><b>Request</b></td></tr>
            <tr><td class="label enabled" width="25%">Request Method</td><td class="output valid readwrite optional enabled" width="75%"><span class="value"><%= request.getMethod() %></span></td></tr>
            <tr><td class="label enabled">Request URI</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getRequestURI() %></span></td></tr>
            <tr><td class="label enabled">Request Protocol</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getProtocol() %></span></td></tr>
            <tr><td class="label enabled">Servlet Path</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getServletPath() %></span></td></tr>
            <tr><td class="label enabled">Path Info</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getPathInfo() %></span></td></tr>
            <tr><td class="label enabled">Path Translated</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getPathTranslated() %></span></td></tr>
            <tr><td class="label enabled">Query String</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getQueryString() %></span></td></tr>
            <tr><td class="label enabled">Content Length</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getContentLength() %></span></td></tr>
            <tr><td class="label enabled">Content Type</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getContentType() %></span></td></tr>
            <tr><td class="label enabled">Server Name</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getServerName() %></span></td></tr>
            <tr><td class="label enabled">Server Port</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getServerPort() %></span></td></tr>
            <tr><td class="label enabled">Remote User</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getRemoteUser() %></span></td></tr>
            <tr><td class="label enabled">Remote Address</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getRemoteAddr() %></span></td></tr>
            <tr><td class="label enabled">Remote Host</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getRemoteHost() %></span></td></tr>
            <tr><td class="label enabled">Authorization Scheme</td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getAuthType() %></span></td></tr>

            <tr><td colspan="2" class="full-group-label label"><b>Request Headers</b></td></tr>
            <%
                Enumeration headers = request.getHeaderNames();
                while (headers.hasMoreElements()) {
                   String name = headers.nextElement().toString(); %>
            <tr><td class="label enabled"><%= name %></td><td class="output valid readwrite optional enabled"><span class="value"><%= request.getHeader(name) %></span></td></tr>
            <%
                }
            %>

            <tr><td colspan="2" class="full-group-label label"><b>Session</b></td></tr>
            <tr><td class="label enabled">Session ID</td><td class="output valid readwrite optional enabled"><span class="value"><%= session.getId() %></span></td></tr>
            <tr><td class="label enabled">Creation time</td><td class="output valid readwrite optional enabled"><span class="value"><%= DateFormat.getDateTimeInstance().format(new Date(session.getCreationTime())) %></span></td></tr>
            <tr><td class="label enabled">Last accessed</td><td class="output valid readwrite optional enabled"><span class="value"><%= DateFormat.getDateTimeInstance().format(new Date(session.getLastAccessedTime())) %></span></td></tr>

            <tr><td colspan="2" class="full-group-label label"><b>Session Variables</b></td></tr>
            <%
                Enumeration variables = session.getAttributeNames();
                while (variables.hasMoreElements()) {
                    String name = variables.nextElement().toString(); %>
            <tr><td class="label enabled"><%= name %></td><td class="output valid readwrite optional enabled"><span class="value"><%= session.getAttribute(name).toString() %></span></td></tr>
            <%
                }
            %>
        </table>

        <center>
            <font face="sans-serif"><a href="<%=request.getContextPath()%>/jsp/forms.jsp">Back to Samples</a></font>
        </center>
    </body>
</html>
