<%@ page import="org.jdom.Element" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.Marshaller" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.WorkItemRecord" %>
    <html xmlns="http://www.w3.org/1999/xhtml">
    

     
	<head>
		<title>Welcome to the Digital Signature Service</title>
        <meta name="Pragma" content="no-cache"/>
        <meta name="Cache-Control" content="no-cache"/>
        <script type="text/javascript" src="/resourceService/theme/com/sun/rave/web/ui/defaulttheme-gray/javascript/formElements.js"></script>
        <meta name="Expires" content="0"/>
        <link rel="stylesheet" type="text/css" href="/resourceService/theme/com/sun/rave/web/ui/defaulttheme-gray/css/css_master.css" />
        <style type="TEXT/CSS"><!--
        .leftArea	{
            color:DarkGrey;
            background:#E8E8E8;
        }
        body{
            scrollbar-arrow-color:WHITE;
            scrollbar-track-color:#D6D6D6;
            scrollbar-shadow-color:#D6D6D6;
            scrollbar-face-color:#135184;
            scrollbar-highlight-color:#D6D6D6;
            scrollbar-darkshadow-color:#135184;
            scrollbar-3dlight-color:#135184;
        }
        //--></style>
<link id="link1" rel="stylesheet" type="text/css" href="/resourceService/resources/stylesheet.css" />
<link id="lnkFavIcon" rel="shortcut icon" type="image/x-icon" href="/resourceService/resources/favicon.ico" />


	
	</head>
	<body>
	<div><div><table cellpadding="0" cellspacing="0" width="100%" style="height: 69px" id="form1:headtable1"><tbody><tr><td background="./resources/headbgleft.jpg" height="69px" width="40%" valign="bottom" align="left"><p> 
                                <a style="font-family:verdana; color: #3277ba" target="_blank" href="http://www.yawlfoundation.org/">
                                    www.yawlfoundation.org
                                </a></p></td><td width="409px" height="69px" align="center"><img src="./resources/newYAWL.jpg" border="0" alt="YAWL 2.0" width="409px" height="69px" id="form1:headImage1"/></td><td background="./resources/headbgright.jpg" height="69px" valign="bottom" align="right" width="40%"><p style="font-family:verdana; color: #97cbfd"><i>Leading the World in Process Innovation</i>

 </p></td></tr></tbody></table></div></div>

        <%
 	    
 	    String workItemXML = request.getParameter("workitem");
    	WorkItemRecord wir;

    	// workItemXML won't be null on the first call from the worklist handler
    	if (workItemXML != null) {
       	wir = Marshaller.unmarshalWorkItem(workItemXML) ;
       	 	session.setAttribute("workitem", wir);                  // save it for the post
   	 		}

		// if it is null, it's after a 'submit' and the request param is lost,
		// so retreive the wir from the session attribute saved earlier
		else {
			wir = (WorkItemRecord) session.getAttribute("workitem");
		}
		String redirectURL = "upload.jsp?workitem=" + wir.toXML();


		%>
		
			
    <div class="dynformOuterPanel" style="height: 170px; width: 390px" style="position: relative;-rave-layout: grid; height: 170px; width: 290px">  
      
      <tr align="center">
				<td><h3>Digital Signature</h3></tr>
			</tr>
      <center>
      
      <form action= "upload.jsp" method="POST" enctype="multipart/form-data">
      <div class="dynformSubPanelAlt" style="top: 30px; left: 10px; height: 110px; width: 370px" style="position: relative;-rave-layout: grid; top: 30px; left: 10px; height: 110px; width: 270px">
					<tr>
					<p align="left">
						<td><strong align=left>P12 Certificate</strong></td>
            <td><input type="file" name="P12" class="dynformInput TxtFld" style="top: 10px" value="" size="20" maxlength="20" /></td>
          </p>
					</tr>
					
					<tr>
					<p align="left">
						<td><strong>Password</strong></td>
            <td><input type="password" name="password" class="dynformInput TxtFld" style="top: 40px" value="" size="20" maxlength="20" /></td>
           </p>
					</tr>					
					
					<tr>
					<p align="left">
						<td><strong>x509 Certificate</strong></td>
            <td><input type="file" name="x509" class="dynformInput TxtFld" style="top: 65px" value="" size="20" maxlength="20" /></td>
          </p>
					</tr>		
					
					<table border="0" cellspacing="20">
					<tr>
				    <td><input type="reset" value="Clear" /></td>
            <td><INPUT TYPE="submit" Value="SIGN" /></td>
					</tr>		
					</table>
			</div>
		</div>
		</center>
			</form>
          
	</body>
</html>
