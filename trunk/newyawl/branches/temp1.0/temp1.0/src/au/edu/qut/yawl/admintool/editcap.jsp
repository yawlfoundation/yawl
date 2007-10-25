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
		System.out.println("Success:" + success);
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
		
		LinkedList capList = new LinkedList();
		String cap = "";
		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT * FROM Capability;";
			ResultSet rs = DBconnection.getResultSet(statement, sql);
			while (rs.next()) {
				capList.add(rs.getString(1));
			}
			statement.close();
		}	
		catch (Exception e) {
			//nothing needs doing
		}
				if (connection != null) {
			DBconnection.killConnection();
		}
%>

<p>
<h2>Capabilities entered into the database:</h2>
Select a capability and click the delete button to delete it.
<table border="0" bgcolor="lightgrey">
<form method="post" action="http://localhost:8080/admintool/editServlet" name="deletecap">
	<input type="hidden" name="which_form" value="deletecap"/>
<tr>
	<td>
			<select name="cap"/>
			<%
				for (int i=0; i<capList.size(); i++) {
					if (capList.get(i) != null){
					cap = capList.get(i).toString();
					}
					
			%>	
			<option><%=cap %></option>
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
		
		
<%@include file="footer.jsp" %>
</body>
</html>
