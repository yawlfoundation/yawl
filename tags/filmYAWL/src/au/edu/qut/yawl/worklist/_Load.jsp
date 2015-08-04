<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.math.BigInteger" %>
<%@ page import="com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl" %>
<%@ page import="javax.xml.bind.JAXBElement" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="javax.xml.bind.Unmarshaller" %>

<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Continuity Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript">
function getParam(name)
{
  var start=location.search.indexOf("?"+name+"=");
  if (start<0) start=location.search.indexOf("&"+name+"=");
  if (start<0) return '';
  start += name.length+2;
  var end=location.search.indexOf("&",start)-1;
  if (end<0) end=location.search.length;
  var result='';
  for(var i=start;i<=end;i++) {
    var c=location.search.charAt(i);
    result=result+(c=='+'?' ':c);
  }
  //window.alert('Result = '+result);
  return unescape(result);
}

function getParameters(){
	document.form1.workItemID.value = getParam('workItemID');
	document.form1.userID.value = getParam('userID');
	document.form1.sessionHandle.value = getParam('sessionHandle');
	document.form1.cameFrom.value = getParam('cameFrom');
	document.form1.submit.value = "htmlForm";
}
</script>
</head>

<body onLoad="getParameters()">

<h1>Load File</h1>

<form action="<%= request.getContextPath() %>/<%= request.getParameter("cameFrom") %>" enctype="multipart/form-data" method="POST">
	<p><input type="file" name="LoadButton" value="Load Report" /></p>
	<input type="hidden" name="workItemID" id="workItemID"/>
	<input type="hidden" name="userID" id="userID"/>
	<input type="hidden" name="sessionHandle" id="sessionHandle"/>
	<input type="hidden" name="submit" id="submit"/>
	<input type="hidden" name="cameFrom" id="cameFrom"/>
	<input type="submit" name="LoadButton" value="Load File">
</form>

<%
if (request.getParameter("LoadButton") != null){
	System.out.println("Load 1 from : "+request.getRequestURL());

	String cameFrom = new String(request.getParameter("cameFrom"));
    String workItemID = new String(request.getParameter("workItemID"));
    String sessionHandle = new String(request.getParameter("sessionHandle"));
    String userID = new String(request.getParameter("userID"));
    String submit = new String(request.getParameter("submit"));
	
    response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/"+cameFrom+"?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit+"&load=true"));
    return;
}
%>
</body>
</html>