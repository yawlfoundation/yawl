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

<p>
Select the group you want to edit or delete and click submit <i>(GroupName-Type-Starts-Ends)</i>.

<%
		Connection connection = null;
		DBconnection.loadDriver("org.postgresql.Driver");

		boolean isClosed = DBconnection.getConnection();
		if(isClosed == true){
			DBconnection.getConnection();
		}

		DBconnection.printMetaData();

		LinkedList nameList = new LinkedList();
		LinkedList typeList = new LinkedList();
        LinkedList startsList = new LinkedList();
        LinkedList endsList = new LinkedList();
        
		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT * FROM OrgGroup;";
			ResultSet rs = DBconnection.getResultSet(statement, sql);
			while (rs.next()) {
				nameList.add(rs.getString(1));
				typeList.add(rs.getString(2));
				startsList.add(rs.getString(3));
				endsList.add(rs.getString(4));
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
<form method="post" action="http://131.181.70.9:8080/admintool/editServlet" name="selectgroup">
	<input type="hidden" name="which_form" value="selectgroup"/>
	<tr>
		<td>
			<select name="selectgroup"/>
	<%
		String name = "";
		String type = "";
        String starts = "";
        String ends = "";
		for (int i=0; i<nameList.size(); i++) {
			name = (String) nameList.get(i);
			type = (String) typeList.get(i);
            starts = (String) startsList.get(i);
            ends = (String) endsList.get(i);
	%>
		<option><%=name%>-<%=type %>-<%=starts %>-<%=ends %></option>
	<%
		}
	%>
		</select>
		</td>
		<td>
			<input type="Submit" value=" Edit " name="editgroup"/>
		</td>
		<td>
			<input type="Submit" value=" Delete " name="deletegroup"/>
	</tr>
</table>

<%
	if (request.getAttribute("groupname") != null) {
		String groupname = (String) request.getAttribute("groupname");
		String gtype = (String) request.getAttribute("grouptype");
		String groupstarts = (String) request.getAttribute("groupstarts");
        String groupends = (String) request.getAttribute("groupends");
%>
<table bgcolor="lightgrey">
<form method="post" action="http://131.181.70.9:8080/admintool/editServlet" name="editGroup">
	<input type="hidden" name="which_form" value="editgroup"/>
	<tr>
		<td>Organisational Group Name:</td>
		<td>
			<input type="text" name="group" value="<%=groupname %>" readonly="readonly"/>
		</td>
	</tr>
	<tr>
		<td>Type of the Organisational Group</td>
		<td><select name="grouptype">
			<option selected value><%=gtype %></option>
			<option>Group</option>
			<option>Unit</option>
			<option>Team</option>
			<option>Branch</option>
			<option>Division</option>
			<option>Organisation</option>
		</select>
		</td>
	</tr>
	<tr>
		<td>Working day for this group starts at:</td>
		<td><select name="starttime">
		<option selected value><%=groupstarts %></option>
			<option>00</option>
			<option>01</option>
			<option>02</option>
			<option>03</option>
			<option>04</option>
			<option>05</option>
			<option>06</option>
			<option>07</option>
			<option>08</option>
			<option>09</option>
			<option>10</option>
			<option>11</option>
			<option>12</option>
			<option>13</option>
			<option>14</option>
			<option>15</option>
			<option>16</option>
			<option>17</option>
			<option>18</option>
			<option>19</option>
			<option>20</option>
			<option>21</option>
			<option>22</option>
			<option>23</option>
		</select></td>
		<td>Hour</td>
	</tr>
	<tr>
		<td>Working day for this group ends at:</td>
		<td><select name="endtime">
		<option selected value><%=groupends %></option>
			<option>00</option>
			<option>01</option>
			<option>02</option>
			<option>03</option>
			<option>04</option>
			<option>05</option>
			<option>06</option>
			<option>07</option>
			<option>08</option>
			<option>09</option>
			<option>10</option>
			<option>11</option>
			<option>12</option>
			<option>13</option>
			<option>14</option>
			<option>15</option>
			<option>16</option>
			<option>17</option>
			<option>18</option>
			<option>19</option>
			<option>20</option>
			<option>21</option>
			<option>22</option>
			<option>23</option>
		</select></td>
		<td>Hour</td>
	</tr>
	<tr>
		<td>
			<input type="submit" value=" Submit " name="action"/>
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
