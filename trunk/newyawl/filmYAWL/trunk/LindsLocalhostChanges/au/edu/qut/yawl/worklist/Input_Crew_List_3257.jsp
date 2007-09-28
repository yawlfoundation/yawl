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
<%@ page import="org.yawlfoundation.sb.crewinfo.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Crew List</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<!-- Stylesheet imports -->
<link href="graphics/style.css" rel="stylesheet" type="text/css">
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js"></script>
<script type="text/javascript" src="scripts/inputCrew.js"></script>
</head>

<body onLoad="getParameters()">

<% 
	String xml = null;
	
	if (MultipartFormDataRequest.isMultipartFormData(request)) 
	{
		System.out.println("mrequest workitemid: "+request.getParameter("workItemID"));
		System.out.println("mrequest userid: "+request.getParameter("userID"));
		System.out.println("mrequest sessionHandle: "+request.getParameter("sessionHandle"));
		System.out.println("mrequest submit: "+request.getParameter("submit"));
		System.out.println("mrequest jsessionid: "+request.getParameter("JSESSIONID"));
         MultipartFormDataRequest mrequest = new MultipartFormDataRequest(request);
         String todo = null;
		 StringBuffer result = new StringBuffer();
		 
         if (mrequest != null)
		 {
			todo = mrequest.getParameter("todo");
		 }
		 
	     if ( (todo != null) && (todo.equalsIgnoreCase("upload")) )
	     {
            Hashtable files = mrequest.getFiles();
            if ( (files != null) && (!files.isEmpty()) )
            {
                UploadFile file = (UploadFile) files.get("uploadfile");
				InputStream in = file.getInpuStream();
				
				int i = in.read();
				while (i != -1) {
					result.append((char) i);
					i = in.read();
				}
			}
			
            int beginOfFile = result.indexOf("<?xml");
            int endOfFile = result.indexOf("</ns2:Input_Crew_List>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(
                    beginOfFile,
                    endOfFile + 23);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		xml = (String)session.getAttribute("outputData");
		xml = xml.replaceAll("<Input_Crew_List", "<ns2:Input_Crew_List xmlns:ns2='http://www.yawlfoundation.org/sb/crewInfo'");
		xml = xml.replaceAll("</Input_Crew_List","</ns2:Input_Crew_List");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.crewinfo");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement iclElement = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file	            
	InputCrewListType icl = (InputCrewListType) iclElement.getValue();
	
%>

<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td colspan="3" class="background_top">&nbsp;</td></tr>
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td align="center">
	<h1 align="center">Crew List </h1>      
	<form name="form1" method="post">
		  <table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
            <tr>
              <td>
                  <table width='700' border='0' cellpadding='0' cellspacing='0'>
                <tr>
                  <td width="15" align="right" class="header-left">&nbsp;</td>
                  <td height="20" colspan='2' class="header-middle">General</td>
                  <td width="15" class="header-right">&nbsp;</td>
                </tr>
                <tr>
                  <td width="15" class="left">&nbsp;</td>
                  <td><strong>Production</strong></td>
                  <td><input name='production' type='text' id='production' value="<%=icl.getProduction() %>" readonly></td>
                  <td width="15" class="right">&nbsp;</td>
                </tr>
                <tr>
                  <td colspan='4' class='bottom'>&nbsp;</td>
                </tr>
              </table></td>
            </tr>
            <tr>
              <td><table width="700" border="0" cellpadding="0" cellspacing="0" id="crew">
				<tbody>
                  <tr>
                    <td class="header-left">&nbsp;</td>
                    <td colspan="5" class="header-middle">Crew Details </td>
                    <td class="header-right">&nbsp;</td>
                  </tr>
                  <tr>
                    <td width="15" class="left">&nbsp;</td>
                    <th align="center"><strong>Role</strong></th>
                    <th align="center"><strong>First Name </strong></th>
                    <th align="center">Last Name </th>
                    <th align="center"><strong>Contact No. </strong></th>
                    <th align="center">Email</th>
                    <td width="15" class="right">&nbsp;</td>
                  </tr>
				 <%int a=0;
				 if (icl.getCrewInfo() != null) {
				 CrewInfoType cit = icl.getCrewInfo();
				 if (cit.getSingleCrewInfo().isEmpty() == false){
					for(SingleCrewInfoType scit : cit.getSingleCrewInfo()) {
						a++;%> 
                  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_<%=a%>' type="text" id='role_<%=a%>' value="<%=scit.getRole()%>" size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_<%=a%>' type='text' id='firstname_<%=a%>' size="10" value="<%= scit.getFirstName() %>" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_<%=a%>' type='text' id='lastname_<%=a%>' size="15" value="<%= scit.getLastName() %>" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_<%=a%>' type='text' id='contactno_<%=a%>' size="8" value="<%= scit.getContactNo() %>" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_<%=a%>' type='text' id='email_<%=a%>' size="20" value="<%= scit.getEmail() %>" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <% }
				  } else {%>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_1' type="text" id='role_1' value="Production Manager" size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_1' type='text' id='firstname_1' size="10" value="" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_1' type='text' id='lastname_1' size="15" value="" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_1' type='text' id='contactno_1' size="8" value="" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_1' type='text' id='email_1' size="20" value="" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_2' type="text" id='role_2' value="1st AD" size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_2' type='text' id='firstname_2' size="10" value="" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_2' type='text' id='lastname_2' size="15" value="" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_2' type='text' id='contactno_2' size="8" value="" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_2' type='text' id='email_2' size="20" value="" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_3' type="text" id='role_3' value="2nd AD" size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_3' type='text' id='firstname_3' size="10" value="" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_3' type='text' id='lastname_3' size="15" value="" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_3' type='text' id='contactno_3' size="8" value="" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_3' type='text' id='email_3' size="20" value="" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_4' type="text" id='role_4' value="Camera Assistant" size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_4' type='text' id='firstname_4' size="10" value="" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_4' type='text' id='lastname_4' size="15" value="" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_4' type='text' id='contactno_4' size="8" value="" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_4' type='text' id='email_4' size="20" value="" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_5' type="text" id='role_5' value="Camera Operator" size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_5' type='text' id='firstname_5' size="10" value="" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_5' type='text' id='lastname_5' size="15" value="" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_5' type='text' id='contactno_5' size="8" value="" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_5' type='text' id='email_5' size="20" value="" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_6' type="text" id='role_6' value="Continuity" size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_6' type='text' id='firstname_6' size="10" value="" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_6' type='text' id='lastname_6' size="15" value="" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_6' type='text' id='contactno_6' size="8" value="" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_6' type='text' id='email_6' size="20" value="" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_7' type="text" id='role_7' value="Director" size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_7' type='text' id='firstname_7' size="10" value="" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_7' type='text' id='lastname_7' size="15" value="" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_7' type='text' id='contactno_7' size="8" value="" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_7' type='text' id='email_7' size="20" value="" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_8' type="text" id='role_8' value="D.O.P." size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_8' type='text' id='firstname_8' size="10" value="" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_8' type='text' id='lastname_8' size="15" value="" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_8' type='text' id='contactno_8' size="8" value="" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_8' type='text' id='email_8' size="20" value="" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_9' type="text" id='role_9' value="Editor" size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_9' type='text' id='firstname_9' size="10" value="" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_9' type='text' id='lastname_9' size="15" value="" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_9' type='text' id='contactno_9' size="8" value="" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_9' type='text' id='email_9' size="20" value="" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_10' type="text" id='role_10' value="Producer" size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_10' type='text' id='firstname_10' size="10" value="" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_10' type='text' id='lastname_10' size="15" value="" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_10' type='text' id='contactno_10' size="8" value="" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_10' type='text' id='email_10' size="20" value="" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center"><input name='role_11' type="text" id='role_11' value="Sound Recordist" size="32" pattern="any_text" title="Enter Role. [String Value]"></td>
					<td height="30" align="center"><input name='firstname_11' type='text' id='firstname_11' size="10" value="" pattern="any_text" title="Enter First Name. [String Value]"></td>
                    <td height="30" align="center"><input name='lastname_11' type='text' id='lastname_11' size="15" value="" pattern="any_text" title="Enter Last Name. [String Value]"></td>
                    <td height="30" align="center"><input name='contactno_11' type='text' id='contactno_11' size="8" value="" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                    <td align="center"><input name='email_11' type='text' id='email_11' size="20" value="" pattern="email" title="Enter Email Address. [String Value]"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <%} } %>
			    </tbody>
                <tbody>
                    <tr valign="top">
                        <th class="bottom" colspan="10"> </th>
                    </tr>
                </tbody>
                </table>
			  </td>
            </tr>
        </table>
          <table width='700' border='0' cellpadding='10' cellspacing='0'>
              <tr>
                  <td width="1%"/>
                  <td align="left">
                      <input name="button" type="button" onClick="addCrewRow();" value="Insert Row" />
                      <input name="button" type="button" onClick="deleteCrewRow();" value="Delete Row" />
                  </td>
              </tr>
              <!--<tr>-->
                <!--<td class="bottom">&nbsp;</td>-->
              <!--</tr>-->
          </table>

            <input name="button2" type="button"  onclick="window.print()" value="Print">
			<input type="submit" name="Save" value="Save" onclick="return validateFields('form1');">
			<input type="submit" name="Submission" value="Submission" onclick="return validateFields('form1');">
			<input type="hidden" name="count" id="count" value="<%if (a==0) {out.print("11");}else{out.print(a);}%>">
			<input type="hidden" name="workItemID" id="workItemID">
			<input type="hidden" name="userID" id="userID">
			<input type="hidden" name="sessionHandle" id="sessionHandle">
			<input type="hidden" name="JSESSIONID" id="JSESSIONID">
			<input type="hidden" name="submit" id="submit">
	</form>
		<!-- LOAD -->
    <form method="post" action="Input_Crew_List_3257.jsp?formType=load&workItemID=<%= request.getParameter("workItemID") %>&userID=<%= request.getParameter("userID") %>&sessionHandle=<%= request.getParameter("sessionHandle") %>&JSESSIONID=<%= request.getParameter("JSESSIONID") %>&submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
      <table width="60%" border="0" cellspacing="1" cellpadding="1" align="center" class="style1">
        <tr>
          <td align="left"><strong>Select a file to upload :</strong></td>
        </tr>
        <tr>
          <td align="left">
            <input type="file" name="uploadfile" size="50">
            </td>
        </tr>
        <tr>
          <td align="left">
    		<input type="hidden" name="todo" value="upload">
            <input type="submit" name="Submit" value="Upload">
            <input type="reset" name="Reset" value="Cancel">
				
            </td>
        </tr>
      </table>
      <br>
      <br>
    </form>
<!-- END LOAD -->	</td>
    <td width="14" class="background_right">&nbsp;</td></tr>
  <tr><td colspan="3" class="background_bottom">&nbsp;</td></tr>
</table>
<%
if(request.getParameter("Submission") != null){
	
	int count = Integer.parseInt(request.getParameter("count"));
	CrewInfoType ci = new CrewInfoType();
	for (int i=1;i<=count;i++){
		SingleCrewInfoType scit = new SingleCrewInfoType();
		scit.setRole(request.getParameter("role_" + i));
		scit.setFirstName(request.getParameter("firstname_" + i));
		scit.setLastName(request.getParameter("lastname_" + i));
		scit.setContactNo(request.getParameter("contactno_" + i));
		scit.setEmail(request.getParameter("email_" + i));
		
		ci.getSingleCrewInfo().add(scit);
	}
	icl.setProduction(request.getParameter("production"));
	icl.setCrewInfo(ci);
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	File f = new File("./backup/CrewList_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
	m.marshal( iclElement,  f);//output to file
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(iclElement, xmlOS);//out to ByteArray
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

	int count = Integer.parseInt(request.getParameter("count"));
	CrewInfoType ci = new CrewInfoType();
	for (int i=1;i<=count;i++){
		SingleCrewInfoType scit = new SingleCrewInfoType();
		scit.setRole(request.getParameter("role_" + i));
		scit.setFirstName(request.getParameter("firstname_" + i));
		scit.setLastName(request.getParameter("lastname_" + i));
		scit.setContactNo(request.getParameter("contactno_" + i));
		scit.setEmail(request.getParameter("email_" + i));
		
		ci.getSingleCrewInfo().add(scit);
	}
	icl.setProduction(request.getParameter("production"));
	icl.setCrewInfo(ci);
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(iclElement, xmlOS);//out to ByteArray
	
	response.setHeader("Content-Disposition", "attachment;filename=\"CrewList_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
	response.setHeader("Content-Type", "text/xml");
	
	ServletOutputStream outs = response.getOutputStream();
	xmlOS.writeTo(outs);
	outs.close();
}
%>
</body>
</html>