<%@page import="au.edu.qut.yawl.admintool.*" %>
<%@page import="au.edu.qut.yawl.engine.interfce.*" %>

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<title>YAWL Administration and Monitoring Tool</title>
	<meta name="Pragma" content="no-cache"/>
	<meta name="Cache-Control" content="no-cache"/>
	<meta name="Expires" content="0"/>
	<link rel="stylesheet" href="./graphics/common.css">
	</head>
<%@include file="YAWLnavigation.jsp" %>
<body>
        <%
        if( session.getAttribute("userid") != null ){
            session.invalidate();
        }
        String loginFailure = (String) request.getAttribute("login");
        if(request.getMethod().equals("GET") || loginFailure != null){
            String yawlAdmin = application.getInitParameter("YAWLAdmin");
        %>
		<h3>Please Log In</h3>
        <%
            if(loginFailure != null){
        %>
        <p><font color='red'><em>Your attempt at login failed try again ;-).</em></font></p>
        <%
            }
        %>
        <p>The information you enter here must exactly match our records and you must have administrator rights.
            Enter your userID and password.   If you are not a registered user or not authorized to access the administration tool please contact
            <a href="mailto:<%= yawlAdmin  %>">YAWL Administrator</a>.
        </p>
		<center>
			<form method="post" action="<%=  request.getRequestURL().toString()  %>">
				<table border="0" bgcolor="#ddddff" cellpadding="10">
					<tr>
						<td>
							<strong>UserID</strong>
						</td>
						<td>
							<input type="text" name="userid" value="" size="20" maxlength="20" />
						</td>
					</tr>
					<tr>
						<td>
							<strong>Password</strong>
						</td>
						<td>
							<input type="password" name="password" value="" size="10" maxlength="10" />
						</td>
					</tr>
				</table>
				<table border="0" cellspacing="20">
					<tr>
						<td>
							<input type="submit" value="    Log In    " />
						</td>
						<td>
							<input type="reset" value="    Clear    " />
						</td>
					</tr>
				</table>
			</form>
		</center>
        <%
} else { // request.getMethod().equals(POST)
	InterfaceA_EnvironmentBasedClient iaClient = new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");
    String userid = request.getParameter("userid");
    String password = request.getParameter("password");
    String sessionHandle = iaClient.connect(userid, password);
    
    String checkConnection = iaClient.checkConnection(sessionHandle);
    String success = "<response>Permission";
    System.out.println(checkConnection);
    
    if (iaClient.checkConnection(sessionHandle).startsWith(success)){
   		session.setAttribute("sessionHandle", sessionHandle);
        session.setAttribute("userid", userid);
        application.getRequestDispatcher("/organizational.jsp").forward(request, response);
    }
    else{
        request.setAttribute("login", "failure");
        application.getRequestDispatcher("/login.jsp").forward(request, response);
    }
 }
%>
        <%@include file="footer.jsp"%>
	</body>
</html>
