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


<p>
Select the substitution you want to edit or delete and click submit <i>(ResPosID-From-To-HasSubstitute)</i>.

<%
		Connection connection = null;
		DBconnection.loadDriver("org.postgresql.Driver");

		boolean isClosed = DBconnection.getConnection();
		if(isClosed == true){
			DBconnection.getConnection();
		}

		DBconnection.printMetaData();

		LinkedList idList = new LinkedList();
		LinkedList fromList = new LinkedList();
        LinkedList toList = new LinkedList();
        LinkedList subList = new LinkedList();
        LinkedList resPosList = new LinkedList();

		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT * FROM ResPosSubstitution;";
			ResultSet rs = DBconnection.getResultSet(statement, sql);
			while (rs.next()) {
				idList.add(rs.getString(1));
				fromList.add(rs.getString(2));
				toList.add(rs.getString(3));
				subList.add(rs.getString(4));
			}
			statement.close();
            Statement statement2 = DBconnection.createStatement();
            String sql2 = "SELECT ID FROM ResSerPosID WHERE IsOfResSerPosType = 'Resource';";
            ResultSet rs2 = DBconnection.getResultSet(statement2, sql2);
            while (rs2.next()) {
                resPosList.add(rs2.getString(1));
            }
            statement2.close();
            Statement statement3 = DBconnection.createStatement();
            String sql3 = "SELECT PositionID FROM Position;";
            ResultSet rs3 = DBconnection.getResultSet(statement3, sql3);
            while (rs3.next()) {
                resPosList.add(rs3.getString(1));
            }
		}
		catch (Exception e) {
			//nothing needs doing
		}
				if (connection != null) {
			DBconnection.killConnection();
		}
%>

<table border="0">
<form method="post" action="http://192.94.227.138:8080/admintool/editServlet" name="selectsub">
	<input type="hidden" name="which_form" value="selectsubstitution"/>
	<tr>
		<td>
			<select name="selectsub"/>
	<%
		String id = "";
		String from = "";
        String to = "";
        String sub = "";
		for (int i=0; i<idList.size(); i++) {
			id = (String) idList.get(i);
			from = (String) fromList.get(i);
            to = (String) toList.get(i);
            sub = (String) subList.get(i);
	%>
		<option><%=id%>-<%=from %>-<%=to%>-<%=sub%></option>
	<%
		}
	%>
		</select>
		</td>
		<td>
			<input type="Submit" value=" Edit " name="editsub"/>
		</td>
		<td>
			<input type="Submit" value=" Delete " name="deletesub"/>
	</tr>
</table>

<%
	if (request.getAttribute("resposID") != null) {
		String resposID = (String) request.getAttribute("resposID");
		String subfrom = (String) request.getAttribute("subfrom");
		String subto = (String) request.getAttribute("subto");
        String substi = (String) request.getAttribute("substi");
%>
<table bgcolor="lightgrey">
<form method="post" action="http://192.94.227.138:8080/admintool/editServlet" name="editPosition">
	<input type="hidden" name="which_form" value="editposition"/>
	<tr>
		<td>Resource or Position:</td>
		<td>
			<input type="text" name="subid" value="<%=resposID %>" readonly="readonly"/>
		</td>
	</tr>
	<tr>
		<td>Is away from:</td>
		<td>
			<input type="text" name="subfrom" value="<%=subfrom %>"/>
		</td>
	</tr>
    <tr>
		<td>To:</td>
		<td>
			<input type="text" name="subto" value="<%=subto %>"/>
		</td>
	</tr>
	<tr>
		<td>Has substitution:</td>
		<td>
			<select name="subid2"/>
			<%
				String respos = "";
				for (int i=0; i<resPosList.size(); i++) {
					if (resPosList.get(i) != null) {
						respos = (String) resPosList.get(i);
						if (respos.compareTo(substi)==0) {
							// set as selected value in the drop down list
			%>
			<option selected value><%=substi %></option>
			<%
						} else {
						// just add the group to the drop down list
			%>
			<option><%=respos %></option>
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
			<input type="submit" value=" Submit " name="updatesub"/>
		</td>
	</tr>
</form>
</table>
<%

	}

%>
<p>
<%@include file="footer.jsp" %>
</body>
</html>
