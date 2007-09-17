<%@page import="java.sql.*" %>
<%@page import="java.util.*" %>
<%@page import="au.edu.qut.yawl.admintool.*" %>
<%@page import="au.edu.qut.yawl.engine.interfce.*" %>

<html xmlns="http://www.w3.org/1999.xhtml">
<head>
<title>YAWL Administration and Monitoring Tool</title>
<meta name="Pragma" content="no-cache"/>
<meta name="Cache-Control" content="no-cache"/>
<meta name="Expires" content="0"/>
<link rel="stylesheet" href="./graphics/common.css">
</head>
<body>

<% 	
	if (request.getParameter("success") != null){
		String success = new String(request.getParameter("success"));
		System.out.println("Success:" + success);
		if (success.compareTo("true")==0) {
		%>
			<font color="green">The data was successfully entered into the database!</font>
			<p>
		<%	
		}
	}
%>	

<%
	if (request.getParameter("failure") != null){
		String failure = new String(request.getParameter("failure"));
		if(failure.compareTo("true") == 0) {
		%>
			<font color="red">The data was not entered into the database because an SQLException was thrown. 
			Check whether you entered valid values. More details about the Exception are available in the logfile of Tomcat.</font>
			<p>
		<%	
		}
	}
%>

<h2>Adding capabilities: </h2>
Enter the description of the capability you would like to add and click submit.
<p/>
<table bgcolor="lightGrey">
<form method="post" action="http://192.94.227.138:8080/admintool/addServlet" name="addCapbility">
	<input type="hidden" name="which_form" value="addCapability"/>
	<tr>
		<td>Capability:</td>
		<td>
			<input type="text" name="capabilitydesc"/>
		</td>
	</tr>
	<tr>
		<td>
			<input type="submit" value=" Submit " name="action"/>
		</td>
	</tr>
</form>
</table>
<p>
<%@include file="footer.jsp" %>
</body>
</html>
