<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.io.File" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.math.BigInteger" %>
<%@ page import="com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl" %>
<%@ page import="javax.xml.bind.JAXBElement" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="javax.xml.bind.Unmarshaller" %>
<%@ page import="org.yawlfoundation.sb.start.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Welcome</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<!-- Stylesheet imports -->
<link href="graphics/style.css" rel="stylesheet" type="text/css">
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js"></script>
</head>

<body onLoad="getParameters()">

<% 
	String xml = "<?xml version='1.0' encoding='UTF-8'?><ns2:Welcome_to_Start_Process xmlns:ns2='http://www.yawlfoundation.org/sb/start' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.yawlfoundation.org/sb/start welcomeToStartProcessType.xsd '><production></production></ns2:Welcome_to_Start_Process>";
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.start");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement wspElement = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file	            
	WelcomeToStartProcessType wsp = (WelcomeToStartProcessType) wspElement.getValue();
	
%>

<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td colspan="3" class="background_top">&nbsp;</td></tr>
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td>
	<h1 align="center">Welcome to Start Process </h1>      
	<form name="form1" method="post">
		  <table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
            <tr>
              <td><table width='700' border='0' cellpadding='0' cellspacing='0'>
                <tr>
                  <td width="15" align="right" class="header-left">&nbsp;</td>
                  <td height="20" align="center" class="header-middle">Production</td>
                  <td width="15" class="header-right">&nbsp;</td>
                </tr>
                <tr>
                  <td width="15" class="left">&nbsp;</td>
                  <td align="center"><input name='production' type='text' id='production' size="50" pattern="any_text" title="Enter Production Name. [String Value - Compulsory]"></td>
                  <td width="15" class="right">&nbsp;</td>
                </tr>
                <tr>
                  <td colspan='3' class='bottom'>&nbsp;</td>
                </tr>
              </table></td>
            </tr>
        </table>
		  <p align="center">
            <input name="button2" type="button"  onclick="window.print()" value="Print">
            <input type="submit" name="Save" value="Save" onclick="return validateFields('form1');">
            <input type="submit" name="Submission" value="Submission" onclick="return validateFields('form1');">
				<input type="hidden" name="workItemID" id="workItemID">
				<input type="hidden" name="userID" id="userID">
				<input type="hidden" name="sessionHandle" id="sessionHandle">
				<input type="hidden" name="JSESSIONID" id="JSESSIONID">
				<input type="hidden" name="submit" id="submit">
		  </p>
	</form>
		
	</td>
    <td width="14" class="background_right">&nbsp;</td></tr>
  <tr><td colspan="3" class="background_bottom">&nbsp;</td></tr>
</table>
<%
if(request.getParameter("Submission") != null){
	
	wsp.setProduction(request.getParameter("production"));
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	File f = new File("./backup/Start_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
	m.marshal( wspElement,  f);//output to file
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(wspElement, xmlOS);//out to ByteArray
	String result = xmlOS.toString().replaceAll("ns2:", "");

	String workItemID = new String(request.getParameter("workItemID"));
	String sessionHandle = new String(request.getParameter("sessionHandle"));
	String userID = new String(request.getParameter("userID"));
	String submit = new String(request.getParameter("submit"));
	
	session.setAttribute("inputData", result);
	response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit));
	return;
}
else if(request.getParameter("Save") != null){				
	
	wsp.setProduction(request.getParameter("production"));
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(wspElement, xmlOS);//out to ByteArray
	
	response.setHeader("Content-Disposition", "attachment;filename=\"Start_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
	response.setHeader("Content-Type", "text/xml");
	
	ServletOutputStream outs = response.getOutputStream();
	xmlOS.writeTo(outs);
	outs.close();
}
%>
</body>
</html>