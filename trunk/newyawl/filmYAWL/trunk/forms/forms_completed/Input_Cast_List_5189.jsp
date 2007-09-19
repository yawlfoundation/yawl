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
<%@ page import="org.yawlfoundation.sb.castinfo.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Cast List</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<!-- style sheet imports -->
<link href="graphics/style.css" rel="stylesheet" type="text/css" />
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js"></script>
<script type="text/javascript" src="scripts/inputCast.js"></script>
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
            int endOfFile = result.indexOf("</ns2:Input_Cast_List>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(
                    beginOfFile,
                    endOfFile + 23);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		xml = "<?xml version='1.0' encoding='UTF-8'?><ns2:Input_Cast_List xmlns:ns2='http://www.yawlfoundation.org/sb/castInfo' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.yawlfoundation.org/sb/castInfo castInfoType.xsd '><production>production</production></ns2:Input_Cast_List>";
		//xml = (String)session.getAttribute("outputData");
		xml = xml.replaceAll("<Input_Cast_List", "<ns2:Input_Cast_List xmlns:ns2='http://www.yawlfoundation.org/sb/castInfo'");
		xml = xml.replaceAll("</Input_Cast_List","</ns2:Input_Cast_List");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.castinfo");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement iclElement = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file	            
	InputCastListType icl = (InputCastListType) iclElement.getValue();
	
%>

<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td colspan="3" class="background_top">&nbsp;</td></tr>
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td>
	<h1 align="center">Cast List </h1>      
	<form name="form1" method="post">
		  <table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
            <tr>
              <td><table width='700' border='0' cellpadding='0' cellspacing='0'>
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
                    <td colspan="4" class="header-middle">Cast Details </td>
                    <td class="header-right">&nbsp;</td>
                  </tr>
                  <tr>
                    <td width="15" class="left">&nbsp;</td>
                    <th align="center"><strong>Character</strong></th>
                    <th align="center"><strong>Artist</strong></th>
                    <th align="center">Agent</th>
                    <th align="center"><strong>Agent Contact No. </strong></th>
                    <td width="15" class="right">&nbsp;</td>
                  </tr>
					<%int a=0;
					if (icl.getCastInfo() != null) {
					CastInfoType cit = icl.getCastInfo();
						for(SingleCastInfoType scit : cit.getSingleCastInfo()) {
							a++;%>
						   <tr valign="top">
							<td width="15" height="30" align="center" class="left">&nbsp;</td>
							<td height="30" align="center"><input name='<% out.print("character_" + a); %>' type="text" id='<% out.print("character_" + a); %>' size="20" value="<%= scit.getCharacter() %>" pattern="text"></td>
							<td height="30" align="center"><input name='<% out.print("artist_" + a); %>' type='text' id='<% out.print("artist_" + a); %>' size="20" value="<%= scit.getArtist() %>" pattern="text"></td>
							<td height="30" align="center"><input name='<% out.print("agent_" + a); %>' type='text' id='<% out.print("agent_" + a); %>' size="20" value="<%= scit.getAgent() %>" pattern="text"></td>
							<td height="30" align="center"><input name='<% out.print("contactno_" + a); %>' type='text' id='<% out.print("contactno_" + a); %>' size="10" value="<%= scit.getAgentContactNo() %>" pattern="tel"></td>
							<td width="15" height="30" class="right">&nbsp;</td>
						  </tr>
					  <% }
					  } 
					  %>
					  <% if (icl.getCastInfo() == null) {%>
					   <tr valign="top">
							<td width="15" height="30" align="center" class="left">&nbsp;</td>
							<td height="30" align="center"><input name='character_1' type="text" id='character_1' size="20" value="" pattern="text"" title="enter character name"></td>
							<td height="30" align="center"><input name='artist_1' type='text' id='artist_1' size="20" value="" pattern="text" title="enter artist name"></td>
							<td height="30" align="center"><input name='agent_1' type='text' id='agent_1' size="20" value="" pattern="text" title="enter agent name"></td>
							<td height="30" align="center"><input name='contactno_1' type='text' id='contactno_1' size="10" value="" pattern="tel" title="enter contact number"></td>
							<td width="15" height="30" class="right">&nbsp;</td>
						  </tr>
					 <% }%>
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
                <td>
                    <input name="button" type="button" onClick="addCastRow();" value="Insert Row" />
                    <input name="button" type="button" onClick="deleteCastRow();" value="Delete Row" />
                </td>
            </tr>
        </table>

        <p align="center">
            <input name="button2" type="button"  onclick="window.print()" value="Print">
            <input type="submit" name="Save" value="Save" onclick="return validateFields('form1');">
            <input type="submit" name="Submission" value="Submission" onclick="return validateFields('form1');">
			
			<input type="hidden" name="count" id="count" value="<%if (a==0) {out.print("1");}else{out.print(a);}%>">
			<input type="hidden" name="workItemID" id="workItemID">
			<input type="hidden" name="userID" id="userID">
			<input type="hidden" name="sessionHandle" id="sessionHandle">
			<input type="hidden" name="JSESSIONID" id="JSESSIONID">
			<input type="hidden" name="submit" id="submit">
		</p>
	</form>

	<!-- LOAD -->
    <form method="post" action="Input_Cast_List_5189.jsp?formType=load&workItemID=<%= request.getParameter("workItemID") %>&userID=<%= request.getParameter("userID") %>&sessionHandle=<%= request.getParameter("sessionHandle") %>&JSESSIONID=<%= request.getParameter("JSESSIONID") %>&submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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
<!-- END LOAD -->
	</td>
    <td width="14" class="background_right">&nbsp;</td></tr>
  <tr><td colspan="3" class="background_bottom">&nbsp;</td></tr>
</table>
<%
if(request.getParameter("Submission") != null){
	
	int count = Integer.parseInt(request.getParameter("count"));
	CastInfoType ci = new CastInfoType();
	for (int i=1;i<=count;i++){
		SingleCastInfoType scit = new SingleCastInfoType();
		scit.setCharacter(request.getParameter("character_" + i));
		scit.setArtist(request.getParameter("artist_" + i));
		scit.setAgent(request.getParameter("agent_" + i));
		scit.setAgentContactNo(request.getParameter("contactno_" + i));
		ci.getSingleCastInfo().add(scit);
	}
	icl.setCastInfo(ci);
	icl.setProduction(request.getParameter("production"));
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	File f = new File("./backup/CastList_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
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
	CastInfoType ci = new CastInfoType();
	for (int i=1;i<=count;i++){
		SingleCastInfoType scit = new SingleCastInfoType();
		scit.setCharacter(request.getParameter("character_" + i));
		scit.setArtist(request.getParameter("artist_" + i));
		scit.setAgent(request.getParameter("agent_" + i));
		scit.setAgentContactNo(request.getParameter("contactno_" + i));
		ci.getSingleCastInfo().add(scit);
	}
	icl.setCastInfo(ci);
	icl.setProduction(request.getParameter("production"));
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(iclElement, xmlOS);//out to ByteArray
	
	response.setHeader("Content-Disposition", "attachment;filename=\"CastList_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
	response.setHeader("Content-Type", "text/xml");
	
	ServletOutputStream outs = response.getOutputStream();
	xmlOS.writeTo(outs);
	outs.close();
}
%>
</body>
</html>