<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.util.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>PDF Demonstration</title>
<style type="text/css">
<!--
.style1 {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 12px;
}
.style2 {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 14px;
	font-weight: bold;
}
-->
</style>
</head>

<body>
<p class="style2">This page simulates the data that is received from the PDF Component sends the form fields data back.</p>
<p class="style1"><strong>Output form fields: </strong><br>
  <%
	ArrayList list = (ArrayList) session.getAttribute("result");
	ArrayList outputList = (ArrayList) session.getAttribute("test");
	for(int i=0; i<list.size(); i++)
		out.println((String)list.get(i)+"<BR>");
	%>
</p>
</body>
</html>
