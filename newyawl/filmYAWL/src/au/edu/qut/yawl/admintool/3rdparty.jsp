<%@page import="java.sql.*" %>
<%@page import="java.util.*" %>
<%@page import="au.edu.qut.yawl.admintool.*" %>
<%@page import="au.edu.qut.yawl.engine.interfce.*" %>
<%@page import="au.edu.qut.yawl.elements.YAWLServiceReference"%>

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
	if (request.getParameter("wrongdate") != null){
		String wrongdate = new String(request.getParameter("wrongdate"));
		System.out.println("Wrongdate:" + wrongdate);
		if (wrongdate.compareTo("true")==0) {
		%>
			<font color="orange">The dates should be in the format: DD-MM-YYYY without spaces in between.</font>
			<p>
		<%	
		}
	}
%>	

<% 	
	if (request.getParameter("created") != null){
		String created = new String(request.getParameter("created"));
		if (created.compareTo("true")==0) {
		%>
			<font color="green">The requested data has been retrieved from the database and put into a xml file that opened in the new browser window. Save the file as a .xml file and start the process mining tool.</font>
			<p>
		<%	
		}
	}
%>	

<% 	
	if (request.getParameter("upload") != null){
		String upload = new String(request.getParameter("upload"));
		if (upload.compareTo("true")==0) {
		%>
			<font color="green">The service was uploaded.</font>
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
			<font color="green">The service was deleted.</font>
			<p>
		<%	
		}
	}
%>	

<% 	
	if (request.getParameter("uploaderror") != null){
		String uploaderror = new String(request.getParameter("uploaderror"));
		if (uploaderror.compareTo("true")==0) {
		%>
			<font color="red">There was an error uploading the service.</font>
			<p>
		<%	
		}
	}
%>	

<% 	
	if (request.getParameter("unloaderror") != null){
		String unloaderror = new String(request.getParameter("unloaderror"));
		if (unloaderror.compareTo("true")==0) {
		%>
			<font color="red">There was an error deleting the service.</font>
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
		
		Vector specList = new Vector();
		String specification = "";
				
		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT description FROM workitemevent;";
			ResultSet rs = DBconnection.getResultSet(statement, sql);
			while (rs.next()) {
				if (!specList.contains(rs.getString(1))) {
				specList.add(rs.getString(1));
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

<h2>Process Mining</h2>
<p>
Select which data should be included in the XML-file:
<table width="90%" border="0" bgcolor="#ffffff">
<form method="post" action="http://131.181.70.9:8080/admintool/makeXML" name="makeXML">
	<tr>
		<td>Select specification</td>
		<td><select name="selectspec">
			<%
				for (int i=0; i<specList.size(); i++) {
				specification =  (String) specList.get(i);
			%>	
			<option><%=specification %></option>
			<%
				}
			%>
		</select></td>
		<td>Use audit trail data from:</td>
		<td><input type="text" name="from"/></td>
		<td><i>(eg. DD-MM-YYYY)</i></td>
		<td>To:</td>
		<td><input type="text" name="until"/></td>
		<td><i>(eg. DD-MM-YYYY)</i></td>
		<td><input type="submit" value="Create XML-file" name="action"/></td>
	<tr>
</form>
</table>

<!--
<form method="post" action="http://131.181.70.9:8080/admintool/launchProM" name="launchProM">
<table>
	<tr>
		<td><input type="submit" value="Start ProM" name="action"/></td>
		<td>An XML file in the correct format is needed to use the ProM tool. Use the create XML-file to create this file.</td> 
	</tr>
</table>
</form>
-->



<p>
<%@include file="footer.jsp" %>
</body>
</html>
