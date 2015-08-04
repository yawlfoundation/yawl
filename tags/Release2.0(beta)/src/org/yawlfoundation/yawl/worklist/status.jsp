<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Verify Signature</title>
<style type="text/css">
<!--
.style1 {font-family: Arial, Helvetica, sans-serif}
.style2 {color: #FF0000}
-->
</style>

<%@include file="head.jsp"%>


</head>

<%@include file="banner.jsp"%>

<body class="style1">
<p>
  <%
	String status = "";
	
	if (session.getAttribute("status")!=null)
	{
		status = (String)session.getAttribute("status");
		session.removeAttribute("status");
	}
	%>
</p>
<%if(status!="true"){%>
<p><%=status%></p>
<p>
  <%}else{
	String sigName = (String)session.getAttribute("sigName");
	String cover = (String)session.getAttribute("cover");
	String revision = (String)session.getAttribute("revision");
	
	String subject = (String)session.getAttribute("subject");
	String modified = (String)session.getAttribute("modified");
	String verified = (String)session.getAttribute("verified");
%>
</p>
<p align="center"><strong>PDF Signature Verification Report </strong></p>
<table width="554" border="0" align="center">
  <tr>
    <td width="225">Signature Field Name:</td>
    <td width="293"><%=sigName%></td>
  </tr>
  <tr>
    <td>Signature covers whole document:</td>
    <td><span class="style2"><%=cover%></span></td>
  </tr>
  <tr>
    <td>Document revision:</td>
    <td><%=revision%></td>
  </tr>
  <tr>
    <td>Signature Certificate:</td>
    <td><%=subject%></td>
  </tr>
  <tr>
    <td>Document modified:</td>
    <td><span class="style2"><%=modified%></span></td>
  </tr>
    <tr>
    <td>Result:</td>
    <td><span class="style2"><b><%=verified%></b></span></td>
  </tr>
</table>
<%}%>
</body>
<%@include file="footer.jsp"%>
</html>
