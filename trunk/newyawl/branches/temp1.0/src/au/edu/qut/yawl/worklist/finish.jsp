<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.io.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Digital ID Generated</title>
<style type="text/css">
<!--
.style1 {font-family: Arial, Helvetica, sans-serif}
-->
</style>

<%@include file="head.jsp"%>


</head>

<%@include file="banner.jsp"%>

<body>
<%
	if(request.getParameter("status")!=null)
	{
		ServletContext sc = getServletConfig().getServletContext();
		String path = sc.getRealPath("\\") + "secure";
		String alias = (String) session.getAttribute("alias");
		
		File f = new File(path+"\\genDir\\certificate.pfx");
		if(f.exists())
			f.delete();
		
		File privCert = new File("C:\\genDir\\certificate.pfx");
		File pubCert = new File("C:\\genDir\\pubcertfile.cer");

		privCert.renameTo(new File(path+"\\genDir\\certificate.pfx"));
		pubCert.renameTo(new File(path+"\\certificates\\"+alias+".cer"));
%>
<p align="center" class="style1">Your Digital ID has been generated:</p>
<p align="center"><span class="style1"><a href="secure/genDir/certificate.pfx">certificate.pfx</a></span></p>
<%
}
else
{
%>
<p align="center" class="style1">Your Digital ID has been generated:</p>
<p align="center"><span class="style1"><a href="?status=get">Get certificate</a></span></p>
<%}%>
</body>
<%@include file="footer.jsp"%>
</html>
