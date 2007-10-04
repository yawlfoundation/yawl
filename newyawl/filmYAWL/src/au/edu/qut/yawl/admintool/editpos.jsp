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
	if (request.getParameter("successdelete") != null){
		String successdelete = new String(request.getParameter("successdelete"));
		if (successdelete.compareTo("true")==0) {
		%>
			<font color="green">The data was successfully deleted from the database!</font>
			<p>
		<%	
		}
	}
%>	

<%
	if (request.getParameter("failuredelete") != null){
		String failuredelete = new String(request.getParameter("failuredelete"));
		if(failuredelete.compareTo("true") == 0) {
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
	if (request.getParameter("successedit") != null){
		String successedit = new String(request.getParameter("successedit"));
		if (successedit.compareTo("true")==0) {
		%>
			<font color="green">The data was successfully updated in the database!</font>
			<p>
		<%	
		}
	}
%>	

<%
	if (request.getParameter("failureedit") != null){
		String failureedit = new String(request.getParameter("failureedit"));
		if(failureedit.compareTo("true") == 0) {
		%>
			<font color="red">The data was not updated into the database because an SQLException was thrown. 
			More details about the Exception are available in the logfile of Tomcat.</font>
			<p>
		<%	
		}
	}
%>

<p>
Select the position you want to edit or delete and click submit <i>(positionID-positionName)</i>.

<%
		Connection connection = null;
		DBconnection.loadDriver("org.postgresql.Driver");
		
		boolean isClosed = DBconnection.getConnection();
		if(isClosed == true){
			DBconnection.getConnection();
		}
		
		DBconnection.printMetaData();
		
		LinkedList posList = new LinkedList();
		LinkedList posnameList = new LinkedList();
		Vector groupList = new Vector();
		
		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT * FROM Position;";
			ResultSet rs = DBconnection.getResultSet(statement, sql);
			while (rs.next()) {
				posList.add(rs.getString(1));
				posnameList.add(rs.getString(2));
				if (!groupList.contains(rs.getString(3))) {
					groupList.add(rs.getString(3));
				}
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

<table border="0"> 
<form method="post" action="http://131.181.70.9:8080/admintool/editServlet" name="selectposition">
	<input type="hidden" name="which_form" value="selectposition"/>
	<tr>
		<td>
			<select name="selectpos"/>
	<%
		String pos = "";
		String posname = "";
		for (int i=0; i<posList.size(); i++) {
			pos = (String) posList.get(i);
			posname = (String) posnameList.get(i);
	%>
		<option><%=pos%>-<%=posname %></option>
	<%
		}
	%>
		</select>
		</td>
		<td>
			<input type="Submit" value=" Edit " name="editpos"/>
		</td>
		<td>
			<input type="Submit" value=" Delete " name="deletepos"/>
	</tr>
</table>

<% 
	if (request.getAttribute("positionID") != null) {
		String pi = (String) request.getAttribute("positionID");
		String pn = (String) request.getAttribute("positionname");
		String og = (String) request.getAttribute("orggroup");
%>

<table bgcolor="lightgrey">
<form method="post" action="http://131.181.70.9:8080/admintool/editServlet" name="editPosition">
	<input type="hidden" name="which_form" value="editposition"/>
	<tr>
		<td>PositionID:</td>
		<td>
			<input type="text" name="position" value="<%=pi %>" readonly="readonly"/>
		</td>
	</tr>
	<tr>
		<td>Position Name:</td>
		<td>
			<input type="text" name="positionname" value="<%=pn %>"/>
		</td>
	</tr>
	<tr>
		<td>Belongs To Organisational Group:</td>
		<td>
			<select name="orggroup"/>
			<%
				String group = "";
				for (int i=0; i<groupList.size(); i++) {
					if (groupList.get(i) != null) {
						group = (String) groupList.get(i);
						if (group.compareTo(og)==0) {
							// set as selected value in the drop down list
			%>	
			<option selected value><%=og %></option>
			<%
						} else {
						// just add the group to the drop down list
			%>
			<option><%=group %></option>
			<% 
						}
					}
				}
			%>
			</select>	
		</td>
	</tr>
	<tr>
		<td>
			<input type="submit" value=" Submit " name="updatepos"/>
		</td>
	</tr>
</form>
</table>
<% 

	}

%>
<%@include file="footer.jsp" %>
</body>
</html>