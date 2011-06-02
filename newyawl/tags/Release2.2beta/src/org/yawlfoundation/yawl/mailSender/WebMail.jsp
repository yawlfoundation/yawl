<%@ page import="org.jdom.Element" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.Marshaller" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.WorkItemRecord" %>

<%--
  ~ Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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
	
	<title>Mail Sender</title>
    <script type="text/javascript" src="/resourceService/theme/com/sun/rave/web/ui/defaulttheme-gray/javascript/formElements.js"></script>
    <link rel="stylesheet" type="text/css" href="/resourceService/theme/com/sun/rave/web/ui/defaulttheme-gray/css/css_master.css" />
    <script type="text/javascript">
       var sjwuic_ScrollCookie = new sjwuic_ScrollCookie('/dynForm.jsp', '/resourceService/faces/dynForm.jsp'); 
    </script>

    <link id="link1" rel="stylesheet" type="text/css" href="/resourceService/resources/stylesheet.css" />
    <link id="lnkFavIcon" rel="shortcut icon" type="image/x-icon" href="/resourceService/resources/favicon.ico" />
</head>


<body id="body1" style="-rave-layout: grid" onload="return body1_jsObject.setInitialFocus();" onunload="return body1_jsObject.setScrollPosition();">

	<div>
		<div>
			<table cellpadding="0" cellspacing="0" width="100%" style="height: 69px" id="form1:headtable1"><tbody><tr><td background="./resources/headbgleft.jpg" height="69px" width="40%" valign="bottom" align="left"><p> 
                           <a style="font-family:verdana; color: #3277ba" target="_blank" href="http://www.yawlfoundation.org/">
                                  www.yawlfoundation.org
            </a></p></td><td width="409px" height="69px" align="center"><img src="./resources/newYAWL.jpg" border="0" alt="YAWL 2.0" width="409px" height="69px" id="form1:headImage1"/></td><td background="./resources/headbgright.jpg" height="69px" valign="bottom" align="right" width="40%"><p style="font-family:verdana; color: #97cbfd"><i>Leading the World in Process Innovation</i>

 </p></td></tr></tbody></table></div></div>
 
          <%
            String redirectURL = "Send.jsp" ;

 			try{

        		
        		%>
        	
   
    <center>
	<div id="form1:pnlContainer" style="position: relative; height: 10px; top: 0; width: 290px" style="position: relative;-rave-layout: grid; position: relative; height: 10px; top: 0; width: 290px">
		<span id="form1:txtHeader" class="pageHeading">Mail Sender</span>
		<div id="form1:compPanel" class="dynformOuterPanel" style="position: absolute; height: 470px; width: 390px" style="position: relative;-rave-layout: grid; position: absolute; height: 170px; width: 290px">
		
		<form action= "<%=  redirectURL %>" method="post" enctype="multipart/form-data">
	    
			<div class="dynformSubPanelAlt" style="top: 10px; left: 10px; height: 210px; width: 370px"> 
				<strong>Set SMTP parameters :</strong>
			</div>       
			
			<div class="dynformSubPanelAlt" style="top: 30px; left: 10px; height: 110px; width: 370px" style="position: relative;-rave-layout: grid; top: 30px; left: 10px; height: 110px; width: 270px">

		  
				<label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top: 40px">Login :</label>
				<input type="text" name="Login" class="dynformInput TxtFld" style="top: 40px; left: 125px" value="" size="30" maxlength="40" />
				
				<label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top: 65px">Password :</label>
				<input type="password" name="password" class="dynformInput TxtFld" style="top: 65px; left: 125px" value="" size="20" maxlength="20" /></td>
		    </div>      
		   
			<div class="dynformSubPanelAlt" style="top: 160px; left: 10px; height: 210px; width: 370px"> 
				<strong>Email: </strong>
			</div>
		    
			<div class="dynformSubPanelAlt" style="top: 180px; left: 10px; height: 230px; width: 370px" style="position: relative;-rave-layout: grid; top: 30px; left: 10px; height: 110px; width: 270px">

					<label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top:  10px">Send To :</label>
					<input type="text" name="To" class="dynformInput TxtFld" style="top: 10px; left: 125px" value="" size="30" maxlength="60" />
		           
				    <label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top: 40px">Alias :</label>
					<input type="text" name="Alias" class="dynformInput TxtFld" style="top: 40px; left: 125px" value="" size="20" maxlength="40" />

					<label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top: 65px">Subject :</label>
					<input type="text" name="Object" class="dynformInput TxtFld" style="top: 65px; left: 125px" value="" size="20" maxlength="50" />
					
					<label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top: 95px">Attach File :</label>
					<input type="file" name="fileLocation" class="dynformInput TxtFld" style="top: 95px; left: 125px" value="" size="20" maxlength="20" />
		        
					<label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top: 125px">Message :</label>
					<textarea cols="25" rows="5" name="content" class="dynformInput TxtFld" style="top: 125px; left: 125px" ></textarea>
			</div>
			<input style="left: 105px ;top: 430px" class="dynformButton Btn2" type="reset" value="Clear" />
			<input style="left: 205px; top: 430px" class="dynformButton Btn2" type="submit" Value="Send" />
		</form>
</div>
</center>
        <%
        
		}catch(Exception e){}
        %>
</body>
</html>
