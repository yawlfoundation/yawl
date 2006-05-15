<%@ page import="au.edu.qut.yawl.worklist.model.WorklistController,
                 au.edu.qut.yawl.smsModule.SMSSender"%><html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Welcome to the SMS Module for YAWL</title>
        <meta name="Pragma" content="no-cache"/>
        <meta name="Cache-Control" content="no-cache"/>
        <meta name="Expires" content="0"/>
        <link rel="stylesheet" href="./graphics/common.css"/>
        <style type="TEXT/CSS"><!--
        .leftArea	{
            color:DarkGrey;
            background:#E8E8E8;
        }
        body{
            scrollbar-arrow-color:WHITE;
            scrollbar-track-color:#D6D6D6;
            scrollbar-shadow-color:#D6D6D6;
            scrollbar-face-color:#135184;
            scrollbar-highlight-color:#D6D6D6;
            scrollbar-darkshadow-color:#135184;
            scrollbar-3dlight-color:#135184;
        }
        //--></style>

	</head>
	<body>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp" %>

        <%
        if(request.getMethod().equals("GET")){
        %>
        <img src="smsomp.jpg"/>
		<h3>Please enter your SMS Direct username and password</h3>

        <p>This module can send and receive SMS messages and pass the data
        into and out of the workflow engine.</p>

        <p>In order to pass the messages
        onto the mobile network this module relies on a proprietary Web
        service.
        <br/>i.e. You need to set up an account with
        <a href="http://www.directsms.com.au">this company</a> and enter
        your account info below.</p>

        <p>NB: You do not need to log in to the company Web site, this Web
        application will log in automatically when the message needs to
        be sent.</p>

		<center>
			<form method="post" action="<%= request.getContextPath() %>/smsWelcome">
				<table border="0" bgcolor="#ddddff" cellpadding="10">
					<tr>
						<td>
							<strong>SMS account username</strong>
						</td>
						<td>
							<input type="text" name="username" value="" size="20" maxlength="20" />
						</td>
					</tr>
					<tr>
						<td>
							<strong>SMS account password</strong>
						</td>
						<td>
							<input type="password" name="password" value="" size="10" maxlength="10" />
						</td>
					</tr>
				</table>
				<table border="0" cellspacing="20">
					<tr>
						<td>
							<input type="submit" value="    Set SMS Credentials    " />
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

            String username = request.getParameter("username");
            String password = request.getParameter("password");
System.out.println("Setting username = " + username);
System.out.println("Setting password = " + password);

            SMSSender _smsController = (SMSSender) application.getAttribute("controller");

            _smsController.setSMSUsernameAndPassword(username, password);

        %>
        <h3>Thank you</h3>
        <p>Your SMS username and password have been stored for future
        usage.</p>

        <%
        }
        %>
        <%@include file="footer.jsp"%>
	</body>
</html>
