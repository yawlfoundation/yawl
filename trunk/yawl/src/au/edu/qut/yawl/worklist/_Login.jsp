<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Log In</title>
        <!-- Include file to load init method -->
        <%@ include file="head.jsp"  %>
	</head>
	<body>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp" %>

        <%
//        if( session.getAttribute("userid") != null ){
//            session.invalidate();
//        }
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
        <p>The information you enter here must exactly match our records.
            Enter your userID and password.   If you are not a registered user please contact
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
    String userid = request.getParameter("userid");
    String password = request.getParameter("password");
    String sessionHandle = _worklistController.connect(userid, password);
    if (_worklistController.successful(sessionHandle)){
        session.setAttribute("sessionHandle", sessionHandle);
        session.setAttribute("userid", userid);

        String pagetoget = (String) session.getAttribute("pagetoget");
        session.removeAttribute("pagetoget");
        if(null == pagetoget ||
                !(pagetoget.equals("/admin") ||
                pagetoget.equals("/viewSpecifications") ||
                pagetoget.equals("/availableWork") ||
                pagetoget.equals("/checkedOut"))){
            pagetoget = "/availableWork";
        }
        application.getRequestDispatcher(pagetoget).forward(request, response);
    }
    else{
        request.setAttribute("login", "failure");
        application.getRequestDispatcher("/login").forward(request, response);
    }
 }
%>
        <%@include file="footer.jsp"%>
	</body>
</html>
