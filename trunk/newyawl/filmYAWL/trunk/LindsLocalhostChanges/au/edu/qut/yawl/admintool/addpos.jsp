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

<h2>Adding Positions</h2>
<p>
Enter the name of the positions you would like to add and click submit.
<p>
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

<%
		Connection connection = null;
		DBconnection.loadDriver("org.postgresql.Driver");
		
		boolean isClosed = DBconnection.getConnection();
		if(isClosed == true){
			DBconnection.getConnection();
		}
		
		DBconnection.printMetaData();
		
		LinkedList orggroupList = new LinkedList();

		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT OrgGroupName FROM OrgGroup;";
			ResultSet groups = DBconnection.getResultSet(statement, sql);
			while (groups.next()) {
				orggroupList.add(groups.getString(1));
			}
			statement.close();
		}	
		catch (Exception e) {
			e.printStackTrace();
			//nothing needs doing
		}
		
		if (connection != null) {
			DBconnection.killConnection();
		}
%>

<table bgcolor="lightGrey">
<form method="post" action="http://192.94.227.138:8080/admintool/addServlet" name="addPosition">
	<input type="hidden" name="which_form" value="addPosition"/>
	<tr>
		<td>PositionID:</td>
		<td>
			<input type="text" name="position"/>
		</td>
	</tr>
	<tr>
		<td>Position Name:</td>
		<td>
			<input type="text" name="positionname"/>
		</td>
	</tr>
	<tr>
		<td>Belongs To Organisational Group:</td>
		<td>
			<select name="orggroup"/>
			<%
				String orggroup = "";
				for (int i=0; i<orggroupList.size(); i++) {
					if (orggroupList.get(i) != null) {
						orggroup = orggroupList.get(i).toString();
					}
			%>	
			<option><%= orggroup %></option>
			<%
				}
			%>
			</select>	
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
