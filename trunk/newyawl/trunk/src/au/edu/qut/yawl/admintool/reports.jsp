<html xmlns="http://www.w3.org/1999.xhtml">
<head>
<title>YAWL Administration and Monitoring Tool</title>
<meta name="Pragma" content="no-cache"/>
<meta name="Cache-Control" content="no-cache"/>
<meta name="Expires" content="0"/>
<link rel="stylesheet" href="./graphics/common.css">
</head>
<body>
<%@include file="YAWLnavigation.jsp" %>
<h2>Reports</h2>
<p>
<table>
	<tr>
		<td>Select report:</td>
		<td><select name="select which report to create"></select></td>
		<td>From:</td>
		<td><input type="text" name="DD/MM/YYYY"/></td>
		<td>To:</td>
		<td><input type="text" name="DD/MM/YYYY"/></td>
		<td><input type="submit" value="Create Report"/></td>
	<tr>
</table>
		
<table width="40%" border="1" bgcolor="#ffffff">
			<tr align="left">
			<td width="200">Report Name</td>
			<td width="200">Created On</td>
			</tr>
</table>
<p>
<table>
	<tr>
		<td><input type="submit" value="View Report" name="action"/></td>
		<td><input type="submit" value="Print Report" name="action"/></td>
		<td><input type="submit" value="Delete Report" name="action"/></td>
	</tr>
</table>
<p>
<%@include file="footer.jsp" %>
</body>
</html>
