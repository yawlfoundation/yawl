<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>YAWL Administration and Monitoring Tool</title>
<meta name="Pragma" content="no-cache"/>
<meta name="Cache-Control" content="no-cache"/>
<meta name="Expires" content="0"/>
<link rel="stylesheet" href="./graphics/common.css">
</head>
	<body>
	<%@include file="YAWLnavigation.jsp"%>
        <%
            session.setAttribute("sessionHandle", null);
        %>
       	<h3>Logout Page</h3>

        <p>You have logged out of the administration and monitoring tool.</p>
        <p>Click <a href="http://localhost:8080/worklist" target="_top">here</a> to go back to YAWL.</p>
        <p>
   		<%@include file="footer.jsp" %>
</body>
</html>