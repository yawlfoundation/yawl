<%@page import="au.edu.qut.yawl.engine.interfce.*" %>
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
<%@include file="checkLogin.jsp" %>
<%@include file="YAWLnavigation.jsp" %>
<h2>Entries Audit Trail</h2>
<table>
<form method="post" action="http://192.94.227.138:8080/admintool/auditPage" name="getAuditTrail">
	<input type="hidden" name="which_form" value="GetAuditTrail"/>
	<tr>
		<td><select name="selecttable" value="select logging table to display">
			<option>Work_Items</option>
			<option>Logidentifiers</option>
			<option>Workitemevent</option>
			</select>
		</td>
		<td><input type="submit" value="Show Entries"/></td>
	</tr>
</form>
</table>
<p>
<% 
	if (request.getAttribute("table") != null) {
		String table = (String) request.getAttribute("table");

		Connection connection = null;
		DBconnection.loadDriver("org.postgresql.Driver");
		
		boolean isClosed = DBconnection.getConnection();
		if(isClosed == true){
			DBconnection.getConnection();
		}
		
		DBconnection.printMetaData();

		LinkedList idList = new LinkedList();
		LinkedList specsList = new LinkedList();
		LinkedList enablementTimeList = new LinkedList();
		LinkedList firingTimeList = new LinkedList();
		LinkedList startTimeList = new LinkedList();
		LinkedList statusList = new LinkedList();
		LinkedList parentList = new LinkedList();
		LinkedList whoStartedMeList = new LinkedList(); 
		LinkedList allowsDynamicCreationList = new LinkedList();
		LinkedList dataStringList = new LinkedList();
		LinkedList parentIDList = new LinkedList();
			
		LinkedList rowkeyList = new LinkedList();
		LinkedList WIidentifierList = new LinkedList();
		LinkedList taskidList = new LinkedList();
		LinkedList resourceList = new LinkedList();
		LinkedList timeList = new LinkedList();
		LinkedList eventList = new LinkedList();
		LinkedList descriptionList = new LinkedList();
			
		LinkedList caseidList = new LinkedList();
		LinkedList cancelledList = new LinkedList();
		LinkedList createdList = new LinkedList();
		LinkedList createdByList = new LinkedList();
		LinkedList specificationList = new LinkedList();
		LinkedList completedList = new LinkedList();
		LinkedList parentCaseList = new LinkedList();
			
		try {
			Statement statement = DBconnection.createStatement();
			String sql = "SELECT * FROM Work_Items;";
			ResultSet rs = DBconnection.getResultSet(statement, sql);
			while (rs.next()) {
				idList.add(rs.getString(1));
				specsList.add(rs.getString(2));
				enablementTimeList.add(rs.getString(3));
				firingTimeList.add(rs.getString(4));
				startTimeList.add(rs.getString(5));
				statusList.add(rs.getString(6));
				parentList.add(rs.getString(7));
				whoStartedMeList.add(rs.getString(8));
				allowsDynamicCreationList.add(rs.getString(9));
				dataStringList.add(rs.getString(10));
				parentIDList.add(rs.getString(11));	
			}
			statement.close();
			Statement statement2 = DBconnection.createStatement();
			String sql2 = "SELECT * FROM Workitemevent;";
			ResultSet rs2 = DBconnection.getResultSet(statement2, sql2);
			while (rs2.next()) {
				rowkeyList.add(rs2.getString(1));
				WIidentifierList.add(rs2.getString(2));
				taskidList.add(rs2.getString(3));
				resourceList.add(rs2.getString(4));
				timeList.add(rs2.getString(5));
				eventList.add(rs2.getString(6));
				descriptionList.add(rs2.getString(7));
			}
			statement2.close();
			Statement statement3 = DBconnection.createStatement();
			String sql3 = "SELECT * FROM Logidentifiers;";
			ResultSet rs3 = DBconnection.getResultSet(statement3, sql3);
			while (rs3.next()) {
				caseidList.add(rs3.getString(1));
				cancelledList.add(rs3.getString(2));
				createdList.add(rs3.getString(3));
				createdByList.add(rs3.getString(4));
				completedList.add(rs3.getString(5));
				specificationList.add(rs3.getString(6));
				parentCaseList.add(rs3.getString(7));
			}
			statement3.close();
		}	
		catch (Exception e) {
			//nothing needs doing
		}
		
%>

<% 
	if(table.compareTo("Work_Items") ==0){
%>

<table width="95%" border="1" bgcolor="#ffffff">
			<tr align="left">
			<td width="100"><b>ID</b></td>
			<td width="100"><b>Specs</b></td>
			<td width="100"><b>Enablement Time</b></td>
			<td width="100"><b>Firing Time</b></td>
			<td width="100"><b>Start Time</b></td>
			<td width="100"><b>Status</b></td>
			<td width="100"><b>Parent</b></td>
			<td width="100"><b>Who Started Me</b></td>
			<td width="100"><b>Allows Dynamic Creation</b></td>
			<td width="100"><b>Data String</b></td>
			<td width="100"><b>parentID</b></td>
			</tr>
<%
		for (int i=0; i<idList.size(); i++) {
			String id =  (String) idList.get(i);
			String specs = (String) specsList.get(i);
			String enablementTime = (String) enablementTimeList.get(i);
			String firingTime = (String) firingTimeList.get(i);
			String startTime = (String) startTimeList.get(i);
			String parent = (String) parentList.get(i);
			String whoStartedMe = (String) whoStartedMeList.get(i);
			String allowsDynamicCreation = (String) allowsDynamicCreationList.get(i);
			String dataString = (String) dataStringList.get(i);
			String parentID = (String) parentIDList.get(i);	
%>
			<tr>
				<td><%=id %></td>
				<td><%=specs %></td>
				<td><%=enablementTime %></td>
				<td><%=firingTime %></td>
				<td><%=startTime %></td>
				<td><%=parent %></td>
				<td><%=whoStartedMe %></td>
				<td><%=allowsDynamicCreation %></td>
				<td><%=dataString %></td>
				<td><%=parentID %></td>
			</tr>
	<% 
		}
	%>
</table>
<p>
<table>
	<tr>
	<td>Filter on:</td>
	<td><select name="select-filter" value="select on which to filter">
			<option>Involved resource</option>
			<option>Status</option>
		</select>
	</td>
	<td><input type="text" name="filter"/></td>
	</tr>
	<tr>
	<td>From:</td>
	<td><input type="text" name="DD/MM/YYYY"/></td>
	<td>To:</td>
	<td><input type="text" name="DD/MM/YYYY"/></td>
	</tr>
	<tr>
	<td><input type="submit" value="Filter"/></td>
	</tr>
</table>
<% 

	}

%>

<% 		if(table.compareTo("Workitemevent")==0) {
		
%>
<table width="95%" border="1" bgcolor="#ffffff">
		<tr align="left">
			<td width="100"><b>Identifier</b></td>
			<td width="100"><b>TaskID</b></td>
			<td width="100"><b>Resource</b></td>
			<td width="100"><b>Time</b></td>
			<td width="100"><b>Event</b></td>
			<td width="100"><b>Description</b></td>
		</tr>
<%
		for (int i=0; i<rowkeyList.size(); i++) {
			String WIidentifier = (String) WIidentifierList.get(i);		
			String taskid = (String) taskidList.get(i);
			String resource = (String) resourceList.get(i);
			String time = (String) timeList.get(i);
			String event = (String) eventList.get(i);
			String description = (String) descriptionList.get(i);
%>
			<tr>
				<td><%=WIidentifier %></td>
				<td><%=taskid %></td>
				<td><%=resource %></td>
				<td><%=time %></td>
				<td><%=event %></td>
				<td><%=description %></td>
			</tr>
	<% 
		}
	%>
</table>
<%
	}
%>

<% 
		if(table.compareTo("Logidentifiers")==0) {
		
		
%>

<table width="95%" border="1" bgcolor="#ffffff">
		<tr align="left">
			<td width="100"><b>CaseID</b></td>
			<td width="100"><b>Cancelled</b></td>
			<td width="100"><b>Created</b></td>
			<td width="100"><b>Created By</b></td>
			<td width="100"><b>Completed</b></td>
			<td width="100"><b>Specification</b></td>
			<td width="100"><b>Parent</b></td>
		</tr>
<%
		for (int i=0; i<caseidList.size(); i++) {
			String caseid =  (String) caseidList.get(i);
			String cancelled = (String) cancelledList.get(i);
			String created = (String) createdList.get(i);
			String createdBy = (String) createdByList.get(i);
			String specification = (String) specificationList.get(i);
			String completed = (String) completedList.get(i);
			String parentCase = (String) parentCaseList.get(i);
%>
			<tr>
				<td><%=caseid %></td>
				<td><%=cancelled %></td>
				<td><%=created %></td>
				<td><%=createdBy %></td>
				<td><%=completed %></td>
				<td><%=specification %></td>
				<td><%=parentCase %></td>
			</tr>
	<% 
		}
	%>
</table>
<%
	}
%>


<%
	}
%>
		
<hr/>
<h2>Delete audit trail entries</h2>
<table>
	<tr>
		<td>Delete entries before: <i>(e.g.DD-MM-YYYY)</i></td>
		<td><input type="text" name="DD/MM/YYYY"/></td>
		<td><input type="submit" value="Delete"/></td>
	</tr>
</table> 
<p>
<%@include file="footer.jsp" %>
</body>
</html>