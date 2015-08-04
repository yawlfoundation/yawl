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
<%@include file="checkLogin.jsp" %>
<%@include file="YAWLnavigation.jsp" %>

<% 	
	if (request.getParameter("upload") != null){
		String upload = new String(request.getParameter("upload"));
		if (upload.compareTo("true")==0) {
		%>
			<font color="green">The specification was successfully uploaded!</font>
			<p>
		<%	
		}
	}
%>	

<% 	
	if (request.getParameter("unload") != null){
		String unload = new String(request.getParameter("unload"));
		if (unload.compareTo("true")==0) {
		%>
			<font color="green">The specification was successfully deleted.</font>
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
		
		LinkedList IDList = new LinkedList();
		LinkedList createdOnList = new LinkedList();
		LinkedList createdByList = new LinkedList();
		LinkedList validFromList = new LinkedList();
		LinkedList validUntilList = new LinkedList();
		LinkedList descriptionList = new LinkedList();
		LinkedList versionList = new LinkedList();
		String ID = "";
		String createdOn = "";
		String createdBy = "";
		String validFrom = "";
		String validUntil = "";
		String description = "";
		String version = "";
				
		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT * FROM Specifications;";
			ResultSet rs = DBconnection.getResultSet(statement, sql);
			while (rs.next()) {
				IDList.add(rs.getString(1));
				descriptionList.add(rs.getString(2));
				createdOnList.add(rs.getString(3));
				createdByList.add(rs.getString(4));
				versionList.add(rs.getString(5));
				validFromList.add(rs.getString(6));
				validUntilList.add(rs.getString(7));
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

<h2>Specifications Overview</h2>

<table width="90%" border="1" bgcolor="#ffffff">
			<tr align="left">
			<td width="110"><b>ID</b></td>
			<td width="110"><b>Description</b></td>
			<td width="110"><b>Created On</b></td>
			<td width="110"><b>Created By</b></td>
			<td width="110"><b>VersionNr</b></td>
			<td width="110"><b>Valid From</b></td>
			<td width="110"><b>Valid Until</b></td>
			<!--<td width="110"><b>Status</b></td> -->
			</tr>
			<%
				for (int i=0; i<IDList.size(); i++) {
				ID = (String) IDList.get(i);
				description = (String) descriptionList.get(i);
				createdOn = (String) createdOnList.get(i);
				createdBy = (String) createdByList.get(i);
				version = (String) versionList.get(i);
				validFrom = (String) validFromList.get(i);
				validUntil = (String) validUntilList.get(i);
			%>
			<tr>
				<td><%=ID %></td>
				<td><%=description %></td>
				<td><%=createdOn %></td>
				<td><%=createdBy %></td>
				<td><%=version %></td>
				<td><%=validFrom %></td>
				<td><%=validUntil %></td>
			</tr>
			<%
				}
			%>
</table>
<p>
<b>Delete Specification</b>
<form method="post" action="http://131.181.70.9:8080/admintool/spec" name="deleteService">
<table>	
	<tr>
	<td>
			<select name="selectedSpecification"/>
			<%
				for (int i=0; i<IDList.size(); i++) {
	 				String specID = (String) IDList.get(i);	
			%>	
			<option><%=specID %></option>
			<%
				}
			%>
			</select>
			</td>
			<td><input type="submit" value="Delete Selected Specification" name="action"/></td>
			</tr>
</table>
</form>
<p>
<hr/>
<h2>Add Specifications</h2>
<table bgcolor="lightgrey">
<tr>
        <td align="left">
        <form action="http://131.181.70.9:8080/admintool/spec" enctype="MULTIPART/FORM-DATA" method=post>
        Load YAWL Specification : 
        <input type="file" name="filename" />
        <br />
        <input type="submit" value="Upload" name="action" />
        </form>
        </td>
        </tr>
</table>

<p>
<%@include file="footer.jsp" %>
</body>
</html>
