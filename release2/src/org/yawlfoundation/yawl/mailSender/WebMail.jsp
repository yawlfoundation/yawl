<%@ page import="org.jdom.Element" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.Marshaller" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.WorkItemRecord" %>

                 
<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Mail Sender</title>
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
			if (workItemXML != null) {
        	wir = Marshaller.unmarshalWorkItem(workItemXML) ;
       	 	session.setAttribute("workitem", wir);                  // save it for the post
   	 		}
			else {
			wir = (WorkItemRecord) session.getAttribute("workitem");
			}
			String redirectURL = "Send.jsp?workitem=" + wir.toXML();
        
 			try{

        		
        		%>
        	
   
 			<center>
      <h3>Mail Sender</h3>
 		
 		 <div class="dynformOuterPanel" style="height: 470px; width: 390px" style="position: relative;-rave-layout: grid; height: 170px; width: 290px">  
       
 <form action= "<%=  redirectURL %>" method="post" enctype="multipart/form-data">
         <div align="left"><strong>Set SMTP parameters : </strong></div>
         <div class="dynformSubPanelAlt" style="top: 30px; left: 10px; height: 110px; width: 370px" style="position: relative;-rave-layout: grid; top: 30px; left: 10px; height: 110px; width: 270px">
         <tr>
            <p align="left">
				 <td><strong>SMTP</strong></td>
				 <td><select name="SMTP" class="dynformInput TxtFld" style="top: 10px" onChange="messageValue()">
                 <option value="smtp.qut.edu.au">QUT - WebMail</option>
                 <option value="smtp.gmail.com">Gmail</option>
                 <option value="smtp.mail.yahoo.com">Yahoo</option>
                 </select>
            </td>
            </p>
		 </tr>
					
					<tr>
            <p align="left">
						<td><strong>Login</strong></td>
						<td><input type="text" name="Login" class="dynformInput TxtFld" style="top: 40px" value="" size="40" maxlength="40" /></td>
						</p>
					</tr>
					
					<tr>
					<p align="left">
						<td><strong>Password</strong></td>
						<td><input type="password" name="password" class="dynformInput TxtFld" style="top: 65px" value="" size="20" maxlength="20" /></td>
					</p>
					</tr>
                    
             </div>      
   

<div class="dynformSubPanelAlt" style="top: 160px; left: 10px; height: 210px; width: 370px"> 
  <strong>Email: </strong>
</div>
     <div class="dynformSubPanelAlt" style="top: 180px; left: 10px; height: 230px; width: 370px" style="position: relative;-rave-layout: grid; top: 30px; left: 10px; height: 110px; width: 270px">
         
          
					<tr>
           <p align="left">
						<td><strong>Send To</strong></td>
						<td><input type="text" name="To" class="dynformInput TxtFld" style="top: 10px" value="" size="40" maxlength="60" /></td>
           </p>
					</tr>
					
					<tr>
					 <p align="left">
            <td><strong>Alias</strong></td>
            <td><input type="text" name="Alias" class="dynformInput TxtFld" style="top: 40px" value="" size="20" maxlength="40" /></td>
           </p>
					</tr>
					
					<tr>
					 <p align="left">
						<td><strong>Subject</strong></td>
						<td><input type="text" name="Object" class="dynformInput TxtFld" style="top: 65px" value="" size="20" maxlength="50" /></td>
					 </p>
					</tr>
					<tr>
					<p align="left">
						<td><strong align=left>Attach File</strong></td>
            			<td><input type="file" name="fileLocation" class="dynformInput TxtFld" style="top: 95px" value="" size="20" maxlength="20" /></td>
          			</p>
					</tr>
					
					<tr>
					 <p align="left">
						<td><strong>Message</strong></td>
						<td><textarea cols="35" rows="5" name="content" class="dynformInput TxtFld" style="top: 125px" ></textarea></td>
						</p>
					</tr>
				</div>
				
          <table border="0" cellspacing="20" class="dynformInput TxtFld" style="top: 400px">
					<tr>
						<td><input type="reset" value="Clear" /></td>
						<td><INPUT TYPE="submit" Value="Send" /></td>
					</tr>
          </table>
       
	</div>
</FORM>

		</center>

        <%
        
		}catch(Exception e){}
        %>
</body>
</html>
