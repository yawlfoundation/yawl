<%@page import="java.sql.*" %>
<%@page import="java.util.*" %>
<%@page import="au.edu.qut.yawl.admintool.*" %>

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
		if (success.compareTo("true")==0) {
		%>
			<font color="green">The data was successfully deleted from the database!</font>
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
			<font color="red">The data was not deleted from the database because an SQLException was thrown. 
			Check whether the item you want to delete still has dependencies (foreign keys pointing to it). 
			If so delete the items pointing to it first. 
			More details about the Exception are available in the logfile of Tomcat.</font>
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
		
		LinkedList roleList = new LinkedList();
		String role = "";
		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT * FROM Role;";
			ResultSet rs = DBconnection.getResultSet(statement, sql);
			while (rs.next()) {
				roleList.add(rs.getString(1));
			}
			statement.close();
		}	
		catch (Exception e) {
			//nothing needs doing
		}
		LinkedList role1List = new LinkedList();
		LinkedList role2List = new LinkedList();
		String mainrole = "";
		String incorporatesrole = "";
		try {
			Statement statement2 = DBconnection.createStatement();
			String sql2 = "SELECT * FROM RoleIncorporatesRole;";
			ResultSet rs2 = DBconnection.getResultSet(statement2, sql2);
			while (rs2.next()) {
				role1List.add(rs2.getString(1)); //get values in column 1
				role2List.add(rs2.getString(2)); //get values in column 2
			}
			statement2.close();
		}	
		catch (Exception e) {
			//nothing needs doing
		}
		LinkedList posList = new LinkedList();
		LinkedList role3List = new LinkedList();
		String pos = "";
		String rolepos = "";
		try {
			Statement statement3 = DBconnection.createStatement();
			String sql3 = "SELECT * FROM RolePosition;";
			ResultSet rs3 = DBconnection.getResultSet(statement3, sql3);
			while (rs3.next()) {
				role3List.add(rs3.getString(1));
				posList.add(rs3.getString(2));
			}
			statement3.close();
		}	
		catch (Exception e) {
			//nothing needs doing
		}
				if (connection != null) {
			DBconnection.killConnection();
		}
%>

<p>
<h2>Roles entered into the database:</h2>
Select a role and click the delete button to delete it.
<table border="0" bgcolor="lightgrey">
<form method="post" action="http://localhost:8080/admintool/editServlet" name="deleteroles">
	<input type="hidden" name="which_form" value="deleteroles"/>
<tr>
	<td>
			<select name="role"/>
			<%
				for (int i=0; i<roleList.size(); i++) {
					if (roleList.get(i) != null){
					role = (String) roleList.get(i);
					}
			%>	
			<option><%=role %></option>
			<%
				}
			%>
			</select>
		</td>	
		<td>
			<input type="Submit" value=" Delete " name="action"/>
		</td>
	</tr>
</form>
</table>
<p>
<h2>Already Connected Roles:</h2>
Select a role-role combination and click the delete button to delete it.
<table border="0" bgcolor="lightgrey"> 
<form method="post" action="http://localhost:8080/admintool/editServlet" name="deleteconnectedroles">
	<input type="hidden" name="which_form" value="deleteconnectedroles"/>
	<tr>
		<td>
			<select name="role2role"/>
	<%
		for (int i=0; i<role1List.size(); i++) {
			if (role1List.get(i) != null && role2List.get(i) != null){
				mainrole = (String) role1List.get(i);
				incorporatesrole = (String) role2List.get(i);
			}
	%>
		<option><%=mainrole%>-<%=incorporatesrole %></option>
	<%
		}
	%>
		</select>
		</td>
		<td>
			<input type="Submit" value=" Delete " name="action"/>
		</td>
	</tr>
</table>
<p>
<h2>Already Connected Roles & Positions:</h2>
Select a role-position combination and click the delete button to delete it.
<table border="0" bgcolor="lightgrey"> 
<form method="post" action="http://localhost:8080/admintool/editServlet" name="deleteroleposition">
	<input type="hidden" name="which_form" value="deleteroleposition"/>
	<tr>
		<td>
		<select name="role2pos"/>
	<%
		for (int i=0; i<role3List.size(); i++) {
			if (role3List.get(i) != null && posList.get(i) != null){
				rolepos = (String) role3List.get(i);
				pos = (String) posList.get(i);
			}
	%>
		<option><%=rolepos %>-<%=pos %></option>
	<%
		}
	 %>	
	 	</select>
	</td>
	<td>
			<input type="Submit" value=" Delete " name="action"/>
	</td>
	</tr>
</table>


<%@include file="footer.jsp" %>
</body>
</html>
