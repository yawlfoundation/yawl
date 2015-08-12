<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
/*------------------------------------------------------------------------
 @author tbe
 @version $Id: SchedulingServiceJSP.jsp 27769 2011-03-16 12:43:11Z tbe $
--------------------------------------------------------------------------*/
%>

<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.yawlfoundation.yawl.scheduling.FormGenerator" %>
<%@ page import="org.yawlfoundation.yawl.scheduling.ConfigManager" %>
<!-- ISO-8859-1 UTF-8 US-ASCII -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<html>
<head>

	<%
  	Logger logger = Logger.getLogger(this.getClass());
		FormGenerator fg = new FormGenerator(request, response);
	%>
	
  <script type="text/javascript">
		<%out.print("var languageCode = \""+fg.getConfig().getLanguage()+"\";\r\n"); // set calendar language %>
  </script>

<script type="text/javascript" src="jquery/jquery-1.4.2.js"></script>
<script type="text/javascript" src="jquery/jquery-ui-1.8.2.js"></script>
<script type="text/javascript" src="JS_Calendar2.1/js_calendar.js" charset="ISO-8859-1"></script>
<script type="text/javascript" src="json/json_sans_eval.js"></script>
<script type="text/javascript" src="perikles.js"></script>

<!-- script type="text/javascript" src="jquery.simpleCombo.js"></script-->
<script type="text/javascript" src="jquery.sexy-combo-2.1.3/jquery.sexy-combo.js"></script>
<script type="text/javascript" src="jquery.sexy-combo-2.1.3/examples/jquery.bgiframe.min.js"></script>


<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<title>
	<%
		out.print(fg.getConfig().getLocalizedString("titleSchedulingPage"));
	%>
</title> 

<link rel="stylesheet" type="text/css" href="jquery/jquery-ui.css">
<link rel="stylesheet" type="text/css" href="JS_Calendar2.1/js_calendar.css">
<link rel="stylesheet" type="text/css" href="perikles.css">

<link rel="stylesheet" type="text/css" href="jquery.sexy-combo-2.1.3/css/sexy-combo.css">
<link rel="stylesheet" type="text/css" href="jquery.sexy-combo-2.1.3/css/sexy/sexy.css">
<link rel="stylesheet" type="text/css" href="jquery.sexy-combo-2.1.3/css/custom/custom.css">


  <script>
  $(document).ready(function() {
    $("#tabs").tabs();
  });
  </script>
  
</head>
<body>
	<!-- script type="text/javascript" 
         src="http://localhost:8080/resourceService/gateway?action=getNonHumanResourceIdentifiers&format=JSON&sessionHandle=54a4ee87-face-4193-83bb-0a3a938dbf4b&callback=function bla">
	</script -->
	
<%
	out.print(fg.outForm());
%>

</body>
</html>