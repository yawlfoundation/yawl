<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>YAWL PDF Component</title>


<%@include file="head.jsp"%>


</head>

<%@include file="banner.jsp"%>

<body>

<%


	String filename = new String(request.getParameter("filename"));
	System.out.println(filename);

	if(filename.equals("error")!=true)
	{
%>
<p align="center" class="style1">Download the PDF form and submit when form is filled. </p>
<p align="center"><a href="../worklist/repository/working/<%out.print(filename);%>" class="style1">Download PDF form here</a> 
</p>
<p>  
</p>
<FORM ENCTYPE='multipart/form-data'
 method='POST' action='http://localhost:8080/worklist/handler'>
  <INPUT name="file" TYPE='file' id="file">
  <input name="filename" type="hidden" value="<%out.print(filename);%>">
  <INPUT TYPE='submit' VALUE='upload'>
</FORM>

  <%
	}
	else
	{
%> <div align="center">
  <span class="style3">Error: No PDF file exist for this workitem.</span>  </div>
  <%}%>

<!--<%@include file="footer.jsp"%>-->
</body>
</html>
