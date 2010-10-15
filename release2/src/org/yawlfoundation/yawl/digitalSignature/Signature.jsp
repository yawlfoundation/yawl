<%@ page import="org.yawlfoundation.yawl.engine.interfce.Marshaller" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.WorkItemRecord" %>
<%@ page import="org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGatewayClient" %>
    <%--
  ~ Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

<html xmlns="http://www.w3.org/1999/xhtml">

<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <meta content="no-cache" http-equiv="Pragma"/>
    <meta content="no-cache" http-equiv="Cache-Control"/>
    <meta content="no-store" http-equiv="Cache-Control"/>
    <meta content="max-age=0" http-equiv="Cache-Control"/>
    <meta content="1" http-equiv="Expires"/>


    <title>Welcome to the Digital Signature Service</title>
    <script type="text/javascript"
            src="/resourceService/theme/com/sun/rave/web/ui/defaulttheme-gray/javascript/formElements.js"></script>
    <link rel="stylesheet" type="text/css"
          href="/resourceService/theme/com/sun/rave/web/ui/defaulttheme-gray/css/css_master.css"/>
    <script type="text/javascript">
        var sjwuic_ScrollCookie = new sjwuic_ScrollCookie('/dynForm.jsp', '/resourceService/faces/dynForm.jsp');
    </script>

    <link id="link1" rel="stylesheet" type="text/css"
          href="/resourceService/resources/stylesheet.css"/>
    <link id="lnkFavIcon" rel="shortcut icon" type="image/x-icon"
          href="/resourceService/resources/favicon.ico"/>


</head>
<body id="body1" style="-rave-layout: grid"
      onload="return body1_jsObject.setInitialFocus();"
      onunload="return body1_jsObject.setScrollPosition();">

<div>
    <div>
        <table cellpadding="0" cellspacing="0" width="100%" style="height: 69px"
               id="form1:headtable1">
            <tbody>
            <tr>
                <td background="./resources/headbgleft.jpg" height="69px" width="40%"
                    valign="bottom" align="left"><p>
                    <a style="font-family:verdana; color: #3277ba" target="_blank"
                       href="http://www.yawlfoundation.org/">
                        www.yawlfoundation.org
                    </a></p></td>
                <td width="409px" height="69px" align="center"><img
                        src="./resources/newYAWL.jpg" border="0" alt="YAWL 2.0"
                        width="409px" height="69px" id="form1:headImage1"/></td>
                <td background="./resources/headbgright.jpg" height="69px" valign="bottom"
                    align="right" width="40%"><p
                        style="font-family:verdana; color: #97cbfd"><i>Leading the World
                    in Process Innovation</i>

                </p></td>
            </tr>
            </tbody>
        </table>
    </div>
</div>


<%
    String itemXML = (String) session.getAttribute("itemXML");
    if (itemXML == null) {

        // The workitem id and user's session handle are passed as parameters. Use them
        // to get an xml record of the workitem.
        String itemid = request.getParameter("workitem");
        String handle = request.getParameter("handle");
        String redirectURL = request.getParameter("source");

        String wqURL = "http://localhost:8080/resourceService/workqueuegateway";
        WorkQueueGatewayClient wqClient = new WorkQueueGatewayClient(wqURL);
        itemXML = wqClient.getWorkItem(itemid, handle);
        
        session.setAttribute("itemXML", itemXML);
        session.setAttribute("workitem", itemid);
        session.setAttribute("handle", handle);
        session.setAttribute("redirectURL", redirectURL);
    }

%>
		
			
      <center>
		<div id="form1:pnlContainer" style="position: relative; height: 10px; top: 0; width: 290px" style="position: relative;-rave-layout: grid; position: relative; height: 10px; top: 0; width: 290px">
		<span id="form1:txtHeader" class="pageHeading">Digital Signature</span>
		<div id="form1:compPanel" class="dynformOuterPanel" style="position: absolute; height: 150px; width: 350px" style="position: relative;-rave-layout: grid; position: absolute; height: 170px; width: 390px">


		<form method="POST" enctype="multipart/form-data" onsubmit="return checkForm(this)" action="upload.jsp">
          <label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top: 15px">P12 Certificate : </label>
          <input type="file" name="P12" class="dynformInput TxtFld" size="20" title=" Please load a valid certificate"  style="top: 10px; left:125px; width:115px" value="" maxlength="20"/>
					
          <label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top: 45px">Password : </label>
          <input type="password" name="password" class="dynformInput TxtFld" size="20" title=" Please enter your password"  style="top: 40px; left:125px; width:115px" value="" maxlength="20"/>
					
          <label id="form1:lblDocument1" for="form1:txtDocument1" class="dynformLabel LblLev2Txt" style="top: 70px">x509 Certificate : </label>
          <input type="file" name="x509" class="dynformInput TxtFld" size="20" title=" Please load a valid certificate"  style="top: 65px; left:125px; width:115px" value="" maxlength="20"/>

					<input style="left: 105px ;top: 110px" class="dynformButton Btn2" type="reset" value="Clear" />
					<INPUT style="left: 205px; top: 110px" class="dynformButton Btn2" TYPE="submit" Value="SIGN" />


			</form>

			</div>
		</div>
		</center>


<script type="text/javascript">
function checkForm(form){
msg = "";
   if(!form.password.value){
      msg += "- password is required\n";
   }
   if(!form.P12.value){
      msg += "- Certificate is required\n";
   }
   if(!form.x509.value){
      msg += "- Certificate is required\n";
   }

   if(msg != ""){
      alert(msg);
      return false;
   }
}
</script>

	</body>
</html>
