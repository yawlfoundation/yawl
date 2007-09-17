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

<p>
<% 	
	if (request.getParameter("success") != null){
		String success = new String(request.getParameter("success"));
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
	if (request.getParameter("same") != null){
		String same = new String(request.getParameter("same"));
		if(same.compareTo("true") == 0) {
		%>
			<font color="red">A position or resource cannot substitute itself. Select 2 different roles.</font>
			<p>
		<%	
		}
	}
%>

<% 	
	if (request.getParameter("wrongdate") != null){
		String wrongdate = new String(request.getParameter("wrongdate"));
		if (wrongdate.compareTo("true")==0) {
		%>
			<font color="orange">The dates should be in the format: DD-MM-YYYY without spaces in between.</font>
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
		
		LinkedList resourceList = new LinkedList();
		LinkedList resourceTypeList = new LinkedList();
		LinkedList positionList = new LinkedList();
		LinkedList positionNameList = new LinkedList();
		
		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT ID, IsOfResourceType FROM ResSerPosID WHERE IsOfResSerPosType = 'Resource';";
			ResultSet rs = DBconnection.getResultSet(statement, sql);
			while (rs.next()) {
				resourceList.add(rs.getString(1));
				resourceTypeList.add(rs.getString(2));
			}
			statement.close();
			Statement statement2 = DBconnection.createStatement();
			String sql2 = "SELECT * FROM Position;";
			ResultSet rs2 = DBconnection.getResultSet(statement2, sql2);
			while (rs2.next()) {
				positionList.add(rs2.getString(1));
				positionNameList.add(rs2.getString(2));
			}
			statement2.close();
		}	
		catch (Exception e) {
			//nothing needs doing
		}
		if (connection != null) {
			DBconnection.killConnection();
		}
%>
<p>
<h2>Add substitute for a resource</h2>
<table bgcolor="lightgrey">
<form method="post" action="http://192.94.227.138:8080/admintool/addServlet" name="addSubRes">
	<input type="hidden" name="which_form" value="addSubRes"/>
<tr>
	<td>Resource:</td>
		<td>
			<select name="resource1"/>
			<%
				String resource = "";
				String resourceType = "";
				for (int i=0; i<resourceList.size(); i++) {
					resource = (String) resourceList.get(i);
					resourceType = (String) resourceTypeList.get(i);
			%>	
			<option><%= resource%>-<%= resourceType%></option>
			<%
				}
			%>
			</select>	
		</td>
		<td>Has substitute resource:</td>
		<td>
			<select name="resource2"/>
			<%
				String resource2 = "";
				String resourceType2 = "";
				for (int i=0; i<resourceList.size(); i++) {
					resource2 = (String) resourceList.get(i);
					resourceType2 = (String) resourceTypeList.get(i);
			%>	
			<option><%= resource2%>-<%= resourceType2%></option>
			<%
				}
			%>
			</select>	
		</td>
</tr>
<tr>
		<td>Redirect to substitute from:</td>
		<td><input type="text" name="fromres"/></td>
		<td><i>(eg. DD-MM-YYYY)</i></td>
		<td>To:</td>
		<td><input type="text" name="untilres"/></td>
		<td><i>(eg. DD-MM-YYYY)</i></td>
</tr>
<tr>
	<td><input type="submit" value="Add Substitution" name="action"/></td>
</tr>
</form>
</table>
<p>
<h2>Add substitute for a position</h2>
<table bgcolor="lightgrey">
<form method="post" action="http://192.94.227.138:8080/admintool/addServlet" name="addSubPos">
	<input type="hidden" name="which_form" value="addSubPos"/>
<tr>
	<td>Position:</td>
		<td>
			<select name="position1"/>
			<%
				String position = "";
				String positionName = "";
				for (int i=0; i<positionList.size(); i++) {
				position = (String) positionList.get(i);
				positionName = (String) positionNameList.get(i);
			%>	
			<option><%= position%>-<%= positionName%></option>
			<%
				}
			%>
			</select>	
		</td>
		<td>Has substitute position:</td>
		<td>
			<select name="positionsub"/>
			<%
				String position2 = "";
				String positionName2 = "";
				for (int i=0; i<positionList.size(); i++) {
				position2 = (String) positionList.get(i);
				positionName2 = (String) positionNameList.get(i);
			%>	
			<option><%= position2%>-<%= positionName2%></option>
			<%
				}
			%>
			</select>	
		</td>
</tr>
<tr>
		<td>Redirect to substitute from:</td>
		<td><input type="text" name="frompos"/></td>
		<td><i>(eg. DD-MM-YYYY)</i></td>
		<td>To:</td>
		<td><input type="text" name="untilpos"/></td>
		<td><i>(eg. DD-MM-YYYY)</i></td>
</tr>
<tr>
	<td><input type="submit" value="Add Substitution" name="action"/></td>
</tr>
</form>
</table>

<%@include file="footer.jsp" %>
</body>
</html>
