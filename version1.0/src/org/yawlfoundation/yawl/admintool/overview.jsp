<%@page import="org.yawlfoundation.yawl.admintool.*" %>
<%@page import="org.yawlfoundation.yawl.admintool.model.*" %>
<%@page import="org.yawlfoundation.yawl.engine.interfce.*" %>

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
	ServletContext context = getServletContext();
	String persistOn = context.getInitParameter("EnablePersistance");
	boolean _persistanceConfiguredOn = "true".equalsIgnoreCase(persistOn);
	if (_persistanceConfiguredOn) {
	if (request.getParameter("success") != null) {
		String bla = new String(request.getParameter("success"));
		if (bla.compareTo("true")==0) {
		%>
			<font color="green">The chart was updated</font>
			<p/>
		<%	
		}
	}
%>	

<% 	
	if (request.getParameter("table") != null) {
		String bla = new String(request.getParameter("table"));
		if (bla.compareTo("true")==0) {
		%>
			<font color="green">The table was updated</font>
			<p/>

		<%	
		}
	}
%>	

<%
    DatabaseGatewayImpl _model = DatabaseGatewayImpl.getInstance(_persistanceConfiguredOn);


    String[] specIDs = _model.getSpecs();
%>

<h2>Overview</h2>
<p/>
<% if (request.getParameter("success") != null) {
%>
	<img src="http://localhost:8080/admintool/createChart" alt="Graph" width=500 height=300 border=0 />
<% 
	}
%>

<% if (request.getParameter("table") != null) {
%>

	<p><a href="createChart"> Click to Display Table </a></p>

<% 
	}
%>


<% if (request.getParameter("success") == null) {
%>
Please select which chart you would like to view below.
<% 
	}
%>
<p/>
<hr/>
<form method="post" action="http://localhost:8080/admintool/createChart" name="selectquery">
<table width="90%" border="0" bgcolor="#ffffff">
	<tr>
		<td>Select Query</td>
		<td><select name="query">
			<option>Display a summary of the status of the work items</option>
			<option>Display completion time for the different cases</option>
			<option>Display average specification completion time</option>
			<!--
			<option>Count the number of cases that where not finished on time in the time frame specified below</option>
			<option>Count the number of cases finished on time in the time frame specified below</option>
			<option>Display a summary of the status of the cases</option>
			<option>Show the work items completed today for every user</option>
			<option>Display a list of processing times for a specific task per resource</option>-->
		</select></td>
	</tr>
	<tr>
		<td>Select Graph Type</td>
		<td><select name="selectgraph">
			<option>Bar chart</option>
			<option>Pie chart</option>
			</select>
		</td>
	</tr>
	<tr>
		<td>Select Specification</td>
		<td><select name="selectspec">
			<option>All Specifications</option>
			<%
			for (int i = 0; i < specIDs.length;i++) {
				%><option><%
				out.print(specIDs[i]);
				%></option><%
			}%>
			</select>
		</td>
	</tr>
	<tr>
		<td>Select Case</td>
		<td><select name="caseid">
			<option>All Specifications</option>
			<%
			for (int i = 0; i < specIDs.length;i++) {
				%><option><%
				out.print(specIDs[i]);
				%></option><%
			}%>
			</select>
		</td>
	</tr>
	<tr>
		<td>Select Task Name</td>
		<td><select name="taskname">
			<option>All Specifications</option>
			<%
			for (int i = 0; i < specIDs.length;i++) {
				%><option><%
				out.print(specIDs[i]);
				%></option><%
			}%>
			</select>
		</td>
	</tr>
	<tr>
		<td>Select Time</td>
		<td><select name="time">
			<option>Seconds</option>
			<option>Minutes</option>
			<option>Hours</option>
			<option>Days</option>
			<option>Months</option>
			<option>Years</option>
			</select>
		</td>
		
	</tr>
	<td>
	</td>
	<tr>
	</tr>
	<tr>
		<td><input type="submit" value="Create chart" name="action"/>
		</td>
		<td><input type="submit" value="Create table" name="action"/>
		</td>
	</tr>
</table>
</form>
<p/>
<%
} else {
  out.println("<a> <font color=\"red\">This page has been disabled because persistence is switched off!</font></a>");
}
%>

<%@include file="footer.jsp" %>
</body>
</html>