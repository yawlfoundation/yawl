<!--editres.jsp-->
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
	if (request.getParameter("forgotpassword") != null){
		String forgotpassword = new String(request.getParameter("forgotpassword"));
		if(forgotpassword.compareTo("true") == 0){
		%>
			<font color="red">If a human resource is entered a password. You either forgot the password or the two passwords entered are different. Please try again.</font>
			<p>
		<%	
		}
	}
%>

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
Select the resource you want to edit or delete and click submit.
<%
		Connection connection = null;
		DBconnection.loadDriver("org.postgresql.Driver");
		
		boolean isClosed = DBconnection.getConnection();
		if(isClosed == true){
			DBconnection.getConnection();
		}
		
		DBconnection.printMetaData();
		
		LinkedList resIDList = new LinkedList();
		LinkedList resTypeList = new LinkedList();
		
		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT * FROM ResSerPosID WHERE isofresourcetype = 'Human' OR isofresourcetype = 'Non-Human';";
			ResultSet rs = DBconnection.getResultSet(statement, sql);
			while (rs.next()) {
				resIDList.add(rs.getString(1));
				resTypeList.add(rs.getString(2));
			}
			statement.close();
		}	
		catch (Exception e) {
			//nothing needs doing
		}
			
%>

<table border="0"> 
<form method="post" action="http://192.94.227.138:8080/admintool/editServlet" name="selectresource">
	<input type="hidden" name="which_form" value="selectresource"/>
	<tr>
		<td>
			<select name="selectres"/>
	<%
		String res = "";
		String restype = "";
		for (int i=0; i<resIDList.size(); i++) {
			res = (String) resIDList.get(i);
			restype = (String) resTypeList.get(i);
	%>
		<option><%=res%>-<%=restype %></option>
	<%
		}
	%>
		</select>
		</td>
		<td>
			<input type="Submit" value=" Edit " name="editres"/>
		</td>
		<td>
			<input type="Submit" value=" Delete " name="deleteres"/>
	</tr>
</table>

<% 
	if (request.getAttribute("resourceID") != null) {
		String ri = (String) request.getAttribute("resourceID");
		String rt = (String) request.getAttribute("resourcetype");
		String rd = (String) request.getAttribute("resdescription");
		String wl = (String) request.getAttribute("worklist");
		String sn = (String) request.getAttribute("surname");
		String gn = (String) request.getAttribute("givenname");
		String pw = (String) request.getAttribute("password");
%>

<table width="90%" bgcolor="lightgrey">
<form method="post" action="http://192.94.227.138:8080/admintool/editServlet" name="editResource">
<input type="hidden" name="which_form" value="editResource"/>
<tr>
							<td width="200"/>
							<td width="120"/>
							<td width="120"/>
							<td width="120"/>
							<td width="120"/>
							</tr>
	<tr>
		<td>Resource ID</td><td><input type="text" name="resourceID" value="<%=ri %>" readonly="readonly"/></td>
	</tr>
	<tr>
		<td>Description</td><td><input type="text" name="resdescription" value="<%=rd %>"/></td>	
	</tr>
	<tr>
		<td>Is of Type</td>
		<td><input type="radio" name="type" value="Human" 
		<% 
			if(rt.compareTo("Human")==0){
				System.out.println("Human");
		%>
				checked="checked"
				<%
			}
				%>
				/>Human</td>
		<td><input type="radio" name="type" value="Non-Human" 
		<% 
			if(rt.compareTo("Non-Human")==0){
				System.out.println("Non-Human");
		%>
				checked="checked"
				<%
			}
				%>
				/>Non-Human</td>
	</tr>
		<tr><td width="8">If the resource is of type Human fill in the fields below:</td>
	</tr>
	</tr>
	<tr>
		<td>Uses worklist:</td>
		<td><input type="text" name="worklist" value="<%=wl %>" readonly="readonly"/></td>
	</tr>
	<tr>
		<td>Given Name:</td>
		<td>
			<input type="text" name="givenname" value="<%=gn %>"/>
		</td>
	</tr>
	<tr>
		<td>Surname:</td>
		<td>
			<input type="text" name="surname" value="<%=sn %>"/>
		</td>
	</tr>
	<tr>
		<td>Has access to:</td>
		<td><input type="checkbox" name="usertype" value="User" />Worklist</td>
		<td><input type="checkbox" name="usertype" value="Admin"/>Administration Tool</td>
	</tr>	
	<tr>
		<td>Initial Password:</td>
		<td>
			<input type="password" name="password" value="<%=pw %>"/>
		</tr>
		<tr>
		<td>Confirm Password:</td>
		<td>
			<input type="password" name="password2" value="<%=pw %>"/>
		</tr>
	<tr>
		<td>
			<input type="submit" value=" Submit " name="updateres"/>
		</td>
	</tr>
</form>
</table>
<p>
<%
	LinkedList capabilities = new LinkedList();
	try {
			Statement statement2 = DBconnection.createStatement();
			String sql2 = "SELECT Capabilitydesc FROM ResourceCapability WHERE ResourceID = '"+ri+"';";
			ResultSet rs2 = DBconnection.getResultSet(statement2, sql2);
			while (rs2.next()) {
				capabilities.add(rs2.getString(1));
			}
			statement2.close();
		}	
		catch (Exception e) {
			//nothing needs doing
		}
%>
<h2> The selected resource has the following capabilities:</h2>
<table>
<form method="post" action="http://192.94.227.138:8080/admintool/editServlet" name="deleteResourceCapability">
<input type="hidden" name="which_form" value="deleteResourceCapability"/>
	<tr>
		<td>Resource ID:</td><td><input type="text" name="resourceID" value="<%=ri %>" readonly="readonly"/></td>
	</tr>
	<tr>
		<td>Has capability:</td>
		<td><select name="selectcapability">
			<%
				String cap = "";
				for (int i=0; i<capabilities.size(); i++) {
					if (capabilities.get(i) != null){
						cap = (String) capabilities.get(i);
					}
			%>	
			<option><%=cap %></option>
			<%
				}
			%>
		</select></td>
	</tr>
	<tr>
		<td>
			<input type="submit" value="Delete" name="delete"/>
		</td>
	</tr>
</form>
</table>
<%
	if (rt.compareTo("Human") ==0) {
		LinkedList positions= new LinkedList();
		try {
			Statement statement3 = DBconnection.createStatement();
			String sql3 = "SELECT PositionID FROM HResOccupiesPos WHERE ResourceID = '"+ri+"';";
			ResultSet rs3 = DBconnection.getResultSet(statement3, sql3);
			while (rs3.next()) {
				positions.add(rs3.getString(1));
			}
			statement3.close();
		}	
		catch (Exception e) {
            e.printStackTrace();
			//nothing needs doing
		}
%>
<h2> The selected human resource has the following positions:</h2>
<table>
<form method="post" action="http://192.94.227.138:8080/admintool/editServlet" name="deleteResourcePosition">
<input type="hidden" name="which_form" value="deleteResourcePosition"/>
	<tr>
		<td>Resource ID:</td><td><input type="text" name="resourceID" value="<%=ri %>" readonly="readonly"/></td>
	</tr>
	<tr>
		<td>Occupies position:</td>
		<td><select name="selectposition">
			<%
				String pos = "";
				for (int i=0; i<positions.size(); i++) {
						pos = (String) positions.get(i);
			%>	
			<option><%=pos %></option>
			<%
				}
			%>
		</select></td>
	</tr>
	<tr>
		<td>
			<input type="submit" value="Delete" name="delete"/>
		</td>
	</tr>
</form>
</table>
<p>	
<%
		LinkedList roles = new LinkedList();
		try {
			Statement statement4 = DBconnection.createStatement();
			String sql4 = "SELECT RoleName FROM HResPerformsRole WHERE ResourceID = '"+ri+"';";
			ResultSet rs4 = DBconnection.getResultSet(statement4, sql4);
			while (rs4.next()) {
				roles.add(rs4.getString(1));
			}
			statement4.close();
		}	
		catch (Exception e) {

            //nothing needs doing
		}
%>
<p>
<h2> The selected human resource performs the following roles:</h2>
<table>
<form method="post" action="http://192.94.227.138:8080/admintool/editServlet" name="deleteResourceRole">
<input type="hidden" name="which_form" value="deleteResourceRole"/>
	<tr>
		<td>Resource ID:</td><td><input type="text" name="resourceID" value="<%=ri %>" readonly="readonly"/></td>
	</tr>
	<tr>
		<td>Performs roles:</td>
		<td><select name="selectrole">
			<%
				String role = "";
				for (int i=0; i<roles.size(); i++) {
					if (roles.get(i) != null){
						role = (String) roles.get(i);
					}
			%>	
			<option><%=role %></option>
			<%
				}
			%>
		</select></td>
	</tr>
	<tr>
		<td>
			<input type="submit" value="Delete" name="delete"/>
		</td>
	</tr>
</form>
</table>	
<%
		}
	}
	
%>

<br/>
                <br/>
                <br/>
                <hr/>
                <center>
<table>
     <tr>
           <td>
                        YAWL is distributed under the
                        <a href="http://www.apache.org/licenses/LICENSE-2.0.html">YAWL licence</a>.
                        </td>
                    </tr>
</table>
</center>
</body>
</html>