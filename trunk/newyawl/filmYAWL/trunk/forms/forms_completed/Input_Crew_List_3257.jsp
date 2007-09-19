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
		xml = "<?xml version='1.0' encoding='UTF-8'?><ns2:Input_Crew_List xmlns:ns2='http://www.yawlfoundation.org/sb/crewInfo' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.yawlfoundation.org/sb/crewInfo crewInfoType.xsd '><production>production</production></ns2:Input_Crew_List>";
		//xml = (String)session.getAttribute("outputData");
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
					for(SingleCrewInfoType scit : cit.getSingleCrewInfo()) {
						a++;%> 
                  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center" id="<% out.print("rolecell_" + a); %>">				      

					    <select name="<% out.print("role_" + a); %>" id="<% out.print("role_" + a); %>" onChange="<% out.print("checkOtherRole("+a+");"); %>">
			              <option value="1st AD" <% if(scit.getRole().equals("1st AD")) {out.print("selected");} %> >1st AD</option>
			              <option value="2nd AD" <% if(scit.getRole().equals("2nd AD")) {out.print("selected");} %>>2nd AD</option>
			              <option value="Camera Assistant" <% if(scit.getRole().equals("Camera Assistant")) {out.print("selected");} %>>Camera Assistant</option>
			              <option value="Camera Operator" <% if(scit.getRole().equals("Camera Operator")) {out.print("selected");} %>>Camera Operator</option>
			              <option value="Continuity" <% if(scit.getRole().equals("Continuity")) {out.print("selected");} %>>Continuity</option>
			              <option value="Director" <% if(scit.getRole().equals("Director")) {out.print("selected");} %>>Director</option>
			              <option value="D.O.P." <% if(scit.getRole().equals("D.O.P.")) {out.print("selected");} %>>D.O.P.</option>
			              <option value="Editor" <% if(scit.getRole().equals("Editor")) {out.print("selected");} %>>Editor</option>
			              <option value="Producer" <% if(scit.getRole().equals("Producer")) {out.print("selected");} %>>Producer</option>
			              <option value="Production Manager" <% if(scit.getRole().equals("Production Manager")) {out.print("selected");} %>>Production Manager</option>
			              <option value="Sound Recordist" <% if(scit.getRole().equals("Sound Recordist")) {out.print("selected");} %>>Sound Recordist</option>
			              <option value="Supervising Production Manager" <% if(scit.getRole().equals("Supervising Production Mananger")) {out.print("selected");} %>>Supervising Production Manager</option>
			              
						  <option value="Other ..." <% if( !(scit.getRole().equals("1st AD")) && !(scit.getRole().equals("2nd AD")) && !(scit.getRole().equals("Camera Assistant")) && !(scit.getRole().equals("Camera Operator")) && !(scit.getRole().equals("Continuity")) && !(scit.getRole().equals("Director")) && !(scit.getRole().equals("D.O.P.")) && !(scit.getRole().equals("Editor")) && !(scit.getRole().equals("Producer")) && !(scit.getRole().equals("Production Manager"))  && !(scit.getRole().equals("Sound Recordist")) && !(scit.getRole().equals("Supervising Production Mananger"))  ) { out.print("selected");}%>>Other ...</option>
		                </select>
						<br>
				      <input name='<% out.print("role_other_" + a); %>' type="text" id='<% out.print("role_other_" + a); %>' value="<% if( !(scit.getRole().equals("1st AD")) && !(scit.getRole().equals("2nd AD")) && !(scit.getRole().equals("Camera Assistant")) && !(scit.getRole().equals("Camera Operator")) && !(scit.getRole().equals("Continuity")) && !(scit.getRole().equals("Director")) && !(scit.getRole().equals("D.O.P.")) && !(scit.getRole().equals("Editor")) && !(scit.getRole().equals("Producer")) && !(scit.getRole().equals("Production Manager"))  && !(scit.getRole().equals("Sound Recordist")) && !(scit.getRole().equals("Supervising Production Mananger"))  ) { out.print(scit.getRole());}else { out.print("[If Other, please specify]");}%>" size="32"> 
					                   
			        </td>
					<td height="30" align="center"><input name='<% out.print("firstname_" + a); %>' type='text' id='<% out.print("firstname_" + a); %>' size="10" value="<%= scit.getFirstName() %>" pattern="text"></td>
                    <td height="30" align="center"><input name='<% out.print("lastname_" + a); %>' type='text' id='<% out.print("lastname_" + a); %>' size="15" value="<%= scit.getLastName() %>" pattern="text"></td>
                    <td height="30" align="center"><input name='<% out.print("contactno_" + a); %>' type='text' id='<% out.print("contactno_" + a); %>' size="8" value="<%= scit.getContactNo() %>" pattern="tel"></td>
                    <td align="center"><input name='<% out.print("email_" + a); %>' type='text' id='<% out.print("email_" + a); %>' size="20" value="<%= scit.getEmail() %>" pattern="email"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <% }
				  } else {%>
				  <tr valign="top">
                    <td width="15" height="30" align="center" class="left">&nbsp;</td>
					<td height="30" align="center" id="rolecell_1">				      

					    <select name="role_1" id="role_1" onChange="checkOtherRole(1);">
			              <option value="1st AD">1st AD</option>
			              <option value="2nd AD">2nd AD</option>
			              <option value="Camera Assistant">Camera Assistant</option>
			              <option value="Camera Operator">Camera Operator</option>
			              <option value="Continuity">Continuity</option>
			              <option value="Director">Director</option>
			              <option value="D.O.P.">D.O.P.</option>
			              <option value="Editor">Editor</option>
			              <option value="Producer">Producer</option>
			              <option value="Production Manager">Production Manager</option>
			              <option value="Sound Recordist">Sound Recordist</option>
			              <option value="Supervising Production Manager">Supervising Production Manager</option>
			              <option value="Other ..." >Other ...</option>
		                </select>
						<br>
				      <input name='role_other_1' type="text" id='role_other_1' value="[If Other, please specify]" size="32"> 
					                   
			        </td>
					<td height="30" align="center"><input name='firstname_1' type='text' id='firstname_1' size="10" value="" pattern="text" title="enter your first name"></td>
                    <td height="30" align="center"><input name='lastname_1' type='text' id='lastname_1' size="15" value="" pattern="text" title="enter your last name"></td>
                    <td height="30" align="center"><input name='contactno_1' type='text' id='contactno_1' size="8" value="" pattern="tel" title="enter your contact number"></td>
                    <td align="center"><input name='email_1' type='text' id='email_1' size="20" value="" pattern="email" title="enter your email address"></td>
                    <td width="15" height="30" class="right">&nbsp;</td>
                  </tr>
				  <%} %>
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
			<input type="hidden" name="count" id="count" value="<%if (a==0) {out.print("1");}else{out.print(a);}%>">
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
		
		if(request.getParameter("role_" + i).equals("Other ...")) {
			scit.setRole(request.getParameter("role_other_" + i));
		} else {
			scit.setRole(request.getParameter("role_" + i));
		}
		
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
		
		if(request.getParameter("role_" + i).equals("Other ...")) {
			scit.setRole(request.getParameter("role_other_" + i));
		} else {
			scit.setRole(request.getParameter("role_" + i));
		}
		
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