<%@ page import="org.yawlfoundation.yawl.mailSender.MailSender"%>
<%@ page import="java.io.*"%>
<%@ page import="javax.xml.transform.*"%>
<%@ page import="javax.xml.transform.dom.DOMSource"%>
<%@ page import="javax.xml.transform.stream.StreamResult"%>
<%@ page import="org.jdom.Document"%>
<%@ page import="org.jdom.Element"%>
<%@ page import="org.jdom.output.DOMOutputter"%>

<%--
  ~ Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
  ~ The YAWL Foundation is a collaboration of individuals and
  ~ organisations who are committed to improving workflow technology.
  ~
  ~ This file is part of YAWL. YAWL is free software: you can
  ~ redistribute it and/or modify it under the terms of the GNU Lesser
  ~ General Public License as published by the Free Software Foundation.
  ~
  ~ YAWL is distributed in the hope that it will be useful, but WITHOUT
  ~ ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  ~ or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
  ~ Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public
  ~ License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
  --%>

<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta content="no-cache" http-equiv="Pragma" />
    <meta content="no-cache" http-equiv="Cache-Control" />
    <meta content="no-store" http-equiv="Cache-Control" />
    <meta content="max-age=0" http-equiv="Cache-Control" />
    <meta content="1" http-equiv="Expires" />
	
	<title>SMTP Setting :</title>
    <script type="text/javascript" src="/resourceService/theme/com/sun/rave/web/ui/defaulttheme-gray/javascript/formElements.js"></script>
    <link rel="stylesheet" type="text/css" href="/resourceService/theme/com/sun/rave/web/ui/defaulttheme-gray/css/css_master.css" />
    <script type="text/javascript">
       var sjwuic_ScrollCookie = new sjwuic_ScrollCookie('/dynForm.jsp', '/resourceService/faces/dynForm.jsp'); 
    </script>

    <link id="link1" rel="stylesheet" type="text/css" href="/resourceService/resources/stylesheet.css" />
    <link id="lnkFavIcon" rel="shortcut icon" type="image/x-icon" href="/resourceService/resources/favicon.ico" />
</head>
<body id="body1" style="-rave-layout: grid" onload="return body1_jsObject.setInitialFocus();" onunload="return body1_jsObject.setScrollPosition();">
<center>
	<div>
		<div>
			<table cellpadding="0" cellspacing="0" width="100%" style="height: 69px" id="form1:headtable1"><tbody><tr><td background="./resources/headbgleft.jpg" height="69px" width="40%" valign="bottom" align="left"><p> 
						   <a style="font-family:verdana; color: #3277ba" target="_blank" href="http://www.yawlfoundation.org/">
								  www.yawlfoundation.org
			</a></p></td><td width="409px" height="69px" align="center"><img src="./resources/newYAWL.jpg" border="0" alt="YAWL 2.0" width="409px" height="69px" id="form1:headImage1"/></td><td background="./resources/headbgright.jpg" height="69px" valign="bottom" align="right" width="40%"><p style="font-family:verdana; color: #97cbfd"><i>Leading the World in Process Innovation</i>

			</p></td></tr></tbody></table>
		</div>
	</div>
	
	<div id="form1:pnlContainer" style="position: relative; height: 10px; top: 0; width: 290px" style="position: relative;-rave-layout: grid; position: relative; height: 10px; top: 0; width: 90px">
		<span id="form1:txtHeader" class="pageHeading">Set SMTP</span>
		<div id="form1:compPanel" class="dynformOuterPanel" style="position: absolute; height: 130px; width: 390px" style="position: relative;-rave-layout: grid; position: absolute; height: 170px; width: 290px">
		<%
			if(request.getMethod().equals("GET")){
        %>
		<form method="post" action="<%= request.getContextPath() %>/setSMTP">
	    
			<label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top:  10px">SMTP :</label>
			<input type="text" name="SMTP" class="dynformInput TxtFld" style="top: 10px; left: 125px" value="" size="40" maxlength="60" />
			
			<label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top:  30px">Port :</label>
			<input type="text" name="Port" class="dynformInput TxtFld" style="top: 40px; left: 125px" value="" size="40" maxlength="60" />
			
			
			<input style="left: 105px ;top: 73px" class="dynformButton Btn2" type="reset" value="Clear" />
			<input style="left: 205px; top: 73px" class="dynformButton Btn2" type="submit" value="Submit" />
		</form>
		</div>
	</div>	            
	        <%

        } else { 

	        String SMTP = request.getParameter("SMTP");
	        String Port = request.getParameter("Port");
	        Element XMLSMTP = new Element("SMTP");
	    	Element Child = new Element("SMTP_Address");
	    	Child.setText(SMTP);
	    	XMLSMTP.addContent(Child);
	         
	    	Element Child2 = new Element("Port");
	    	Child2.setText(Port);
	    	XMLSMTP.addContent(Child2);
		    File file = new File(getServletContext().getRealPath("/files/"), "SMTP.xml");
	    	
	    	try{ 
			     Document doc = new Document(XMLSMTP);
			     DOMOutputter outputter = new DOMOutputter();
			 	 org.w3c.dom.Document DocToXML = outputter.output(doc);
		    	
			 	 // Prepare the DOM document for writing
		         Source source = new DOMSource(DocToXML);
		         // Prepare the output file
		         Result result = new StreamResult(file);
		         // Write the DOM document to the file
		         Transformer xformer = TransformerFactory.newInstance().newTransformer();
		         xformer.transform(source, result);
		       
	        } catch (TransformerConfigurationException e) {
	        } catch (TransformerException e) {
	    	} catch (Exception e) {    
				e.printStackTrace(); 
			} 
		
   

        %>
        <h3>Thank you</h3>
        <p>Your SMTP is configured.</p>

        <%
        }
        %>
	</center>
</body>
</html>