<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
/*------------------------------------------------------------------------
 @author tbe
 @version $Id: Error.jsp 24755 2010-12-07 15:35:35Z tbe $
--------------------------------------------------------------------------*/
%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<title>
	<%
	out.print(session.getAttribute("ErrorPageTitle"));
	%>
</title>

</head>
<body>
	<% 
	
	out.print(session.getAttribute("ExceptionMsg") + "<br>");
	Throwable e = (Throwable)session.getAttribute("Exception");
	while (e != null) {
		out.print("Caused by " + e.getClass().getName() + (e.getMessage()==null ? "" : ": " + e.getMessage()));
		
		StackTraceElement[] ste = e.getStackTrace();
		out.print("<blockquote>");
		for (int i=0; i<ste.length; i++ ) {
			out.print("at " + ste[i].toString() + "<br>");
		}
		
		out.print("</blockquote>"); // better than &nbsp;
		
		e = e.getCause();
	}
	
	out.print("<br><br><br>");
	out.print("<a href=\""+session.getAttribute("ErrorPageLoginLink")+"\">");
	out.print(session.getAttribute("ErrorPageLoginText"));
	out.print("</a>&nbsp;");
	
	out.print("<a href=\""+session.getAttribute("ErrorPageWorkqueueLink")+"\">");
	out.print(session.getAttribute("ErrorPageWorkqueueText"));
	out.print("</a>");

	%>
	
</body>
</html>