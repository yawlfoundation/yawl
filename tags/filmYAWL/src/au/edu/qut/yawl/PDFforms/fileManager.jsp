<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.io.*" errorPage="" %>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page import="java.util.List" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>YAWL File Repository Manager</title>
<style type="text/css">
<!--
.style1 {
	font-family: Arial, Helvetica, sans-serif;
	font-weight: bold;
}
.style3 {font-family: Arial, Helvetica, sans-serif}
.style5 {font-family: Arial, Helvetica, sans-serif; font-size: 12; }
.style6 {font-size: 12}
-->
</style>
</head>

<%
	ServletContext sc = getServletConfig().getServletContext();
	String path = sc.getRealPath("/") + "repository/";
	String status = "";
	if (request.getParameter("status") != null)
	{
		if(((String)request.getParameter("status")).equals("del"))
		{
			File f = new File(path + (String)request.getParameter("filename"));
			if(f.delete())
				status = "File "+(String)request.getParameter("filename")+" has been deleted.";
			else
				status = "Unable to delete "+(String)request.getParameter("filename");
		}
	}
	
	if (session.getAttribute("status")!=null)
	{
		status = (String)session.getAttribute("status");
		session.removeAttribute("status");
	}
%>

<body>
<p align="center" class="style1">YAWL File Repository Manager</p>
<p align="center" class="style1"><%=status%></p>
<table width="683" border="1" align="center" cellpadding="0">
  <tr>
    <td height="33" colspan="2"><div align="center" class="style1">Repository</div></td>
  </tr>
<p>
  <%
	File file = new File(path);
	File[] files = file.listFiles();
	boolean gotFiles = false;
	
	for(int i=0; i<files.length; i++)
	{
		if(files[i].isFile())
		{
%>
  <tr class="style3">
				<td width="524" height="45"><span class="style5"><a href="repository/<%=files[i].getName()%>"><%=files[i].getName()%></a></span></td>
				<td width="84"><span class="style6"><a href="fileManager.jsp?status=del&filename=<%=files[i].getName()%>">Delete</a></span></td>
  </tr>
  <%
		gotFiles = true;
		}
	}
	
	if(gotFiles==false)
	{
%>
    <td height="33" colspan="2"><div align="center" class="style5">No files found.</div></td>
<%
	}
%>
</table>
</p>
<p>&nbsp;</p>
<table width="683" border="1" align="center" cellpadding="0">
  <tr>
    <td height="33" colspan="2"><div align="center" class="style3"><strong>Workitems</strong></div></td>
  </tr>
  <p>
    <%
	
	String path2 = sc.getRealPath("/") + "repository/working/";
	File file2 = new File(path2);
	File[] files2 = file2.listFiles();
	gotFiles = false;
	
	for(int i=0; i<files2.length; i++)
	{
		if(files2[i].isFile())
		{
%>
  <tr class="style3">
    <td width="524" height="45"><span class="style5"><a href="repository/working/<%=files2[i].getName()%>"><%=files2[i].getName()%></a></span></td>
    <td width="84"><span class="style6"><a href="fileManager.jsp?status=del&filename=working/<%=files2[i].getName()%>">Delete</a></span></td>
  </tr>
  <%
		gotFiles = true;
		}
	}
	
	if(gotFiles==false)
	{
%>
    <td height="33" colspan="2"><div align="center" class="style5">No files found.</div></td>
<%
	}
%>
</table>
<p>
<p>
<FORM ENCTYPE='multipart/form-data'
 method='POST' action='servlet/Uploader'>
  <div align="center">
  <INPUT name="file" TYPE='file' id="file">
  <INPUT TYPE='submit' VALUE='upload'>
  </div>
</FORM>
</p>
</body>
</html>
