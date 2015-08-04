<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Log Out</title>
        <!-- Include file to load init method -->
        <%@ include file="head.jsp"  %>

	</head>
	<body>
        <%
            session.setAttribute("sessionHandle", null);
        %>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp"%>

		<h3>Logout Page</h3>

        <p>You have logged out.</p>
        <p>Click <a href="<%= contextPath %>/login">here</a> to log back in.</p>
        <%@include file="footer.jsp"%>
    </body>
</html>