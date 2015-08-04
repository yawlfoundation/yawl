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
<%@ page import="org.yawlfoundation.sb.reviselocationinfo.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="1024kb" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Location Notes</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<!-- Stylesheet imports -->
<link href="graphics/style.css" rel="stylesheet" type="text/css" />
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js"></script>
<script type="text/javascript" src="scripts/filloutLocationNotes.js"></script>

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
            int endOfFile = result.indexOf("</ns2:Revise_Location_Notes>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(
                    beginOfFile,
                    endOfFile + 28);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		//xml = "<?xml version='1.0' encoding='UTF-8'?><ns2:Revise_Location_Notes xmlns:ns2='http://www.yawlfoundation.org/sb/reviseLocationInfo' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.yawlfoundation.org/sb/reviseLocationInfo reviseLocationInfoType.xsd '><production>production</production><locationInfo><singleLocationInfo><locationID>locationID</locationID><locationName>locationName</locationName><address>address</address><UBDMapRef>UBDMapRef</UBDMapRef><parking>parking</parking><unit>unit</unit><police>police</police><hospital>hospital</hospital><contact>contact</contact><contactNo>contactNo</contactNo><locationNotes>locationNotes</locationNotes></singleLocationInfo></locationInfo></ns2:Revise_Location_Notes>";
		xml = (String)session.getAttribute("outputData");
		xml = xml.replaceAll("<Revise_Location_Notes", "<ns2:Revise_Location_Notes xmlns:ns2='http://www.yawlfoundation.org/sb/reviseLocationInfo'");
		xml = xml.replaceAll("</Revise_Location_Notes","</ns2:Revise_Location_Notes");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.reviselocationinfo");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement rlnElement = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file	            
	ReviseLocationNotesType rln = (ReviseLocationNotesType) rlnElement.getValue();
%>

<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td colspan="3" class="background_top">&nbsp;</td></tr>
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td>
	<h1 align="center">Location Notes </h1>      
	<form name="form1" method="post">
		<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
			<tr>
				<td><table width="700" border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td width="15" align="right" class="header-left">&nbsp;</td>
						<td height="20" colspan="2" class="header-middle">General</td>
						<td width="15" class="header-right">&nbsp;</td>
					</tr>
					<tr>
						<td width="15" class="left">&nbsp;</td>
						<td><strong>Production</strong></td>
						<td><input name="production" type="text" id="production" value="<%= rln.getProduction() %>" readonly></td>
						<td width="15" class="right">&nbsp;</td>
					</tr>
					<tr>
						<td colspan="4" class="bottom">&nbsp;</td>
					</tr>
				</table></td>
            </tr>
            <tr>
              <td><table width="700" border="0" cellpadding="0" cellspacing="0" id="locations">
					<tbody>
				  		<%
							int a=0;
							if (rln.getLocationInfo() != null) {
							LocationInfoType lit = rln.getLocationInfo();
							if (lit.getSingleLocationInfo().isEmpty() == false){
								for(SingleLocationInfoType slit : lit.getSingleLocationInfo()){
									a++;
						%>
						  <tr>
							<td width="15" class="header-left">&nbsp;</td>
							<td colspan="4" class="header-middle"> <% out.print("Location " + a); %> </td>
							<td width="15" class="header-right">&nbsp;</td>
						  </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td width="100" align="left"><strong>Location ID </strong></td>
                            <td align="left"><input name="location_ID_<%=a%>" type="text" id="location_ID_<%=a%>" value="<%=slit.getLocationID() %>" pattern="any_text" title="Enter Location ID. [String Value]"></td>
                            <td align="left"><strong>Location Name </strong></td>
                            <td align="left"><input name="location_name_<%=a%>" type="text" id="location_name_<%=a%>" value="<%=slit.getLocationName() %>" pattern="any_text" title="Enter Location Name. [String Value]"></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Contact</strong></td>
                            <td align="left"><input name="contact_<%=a%>" type="text" id="contact_<%=a%>" value="<%=slit.getContact() %>" pattern="any_text" title="Enter Contact Person. [String Value]"></td>
                            <td align="left"><strong>Contact No. </strong></td>
                            <td align="left"><input name="phone_<%=a%>" type="text" id="phone_<%=a%>" value="<%=slit.getContactNo() %>" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>UBD Map Ref</strong></td>
                            <td align="left"><input name="ubd_<%=a%>" type="text" id="ubd_<%=a%>" value="<%=slit.getUBDMapRef() %>" pattern="any_text" title="Enter UBD Reference. [String Value]"></td>
                            <td align="left">&nbsp;</td>
                            <td align="left">&nbsp;</td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Address</strong></td>
                            <td colspan="3" align="left"><input name="address_<%=a%>" type="text" id="address_<%=a%>" title="Enter Location Address. [String Value]" value="<%=slit.getAddress() %>" size="80" pattern="any_text"></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Police</strong></td>
                            <td colspan="3" align="left"><input name="police_<%=a%>" type="text" id="police_<%=a%>" title="Enter Police Details. [String Value]" value="<%=slit.getPolice() %>" size="80" pattern="any_text"></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Hospital</strong></td>
                            <td colspan="3" align="left"><input name="hospital_<%=a%>" type="text" id="hospital_<%=a%>" title="Enter Hospital Details. [String Value]" value="<%=slit.getHospital() %>" size="80" pattern="any_text"></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Parking</strong></td>
                            <td colspan="3" align="left"><textarea name="parking_<%=a%>" cols="70" id="parking_<%=a%>" title="Enter Parking Instructions. [String Value]"><%=slit.getParking() %></textarea></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Unit</strong></td>
                            <td colspan="3" align="left"><textarea name="unit_<%=a%>" cols="70" id="unit_<%=a%>" title="Enter Unit Instructions. [String Value]"><%=slit.getUnit() %></textarea></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Location Notes</strong></td>
                            <td colspan="3" align="left"><textarea name="notes_<%=a%>" cols="70" rows="5" id="notes_<%=a%>" title="Enter Location Notes. [String Value]"><%=slit.getLocationNotes() %></textarea></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr><td colspan="6" class="bottom">&nbsp;</td></tr>
						  
						  <% }
						  }else { %>
						  
						  <tr>
							<td width="15" class="header-left">&nbsp;</td>
							<td colspan="4" class="header-middle"> Location 1 </td>
							<td width="15" class="header-right">&nbsp;</td>
						  </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Location ID </strong></td>
                            <td align="left"><input name="location_ID_1" type="text" id="location_ID_1" pattern="any_text" title="Enter Location ID. [String Value]"></td>
                            <td align="left"><strong>Location Name </strong></td>
                            <td align="left"><input name="location_name_1" type="text" id="location_name_1" pattern="any_text" title="Enter Location Name. [String Value]"></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Contact</strong></td>
                            <td align="left"><input name="contact_1" type="text" id="contact_1" pattern="any_text" title="Enter Contact Person. [String Value]"></td>
                            <td align="left"><strong>Contact No. </strong></td>
                            <td align="left"><input name="phone_1" type="text" id="phone_1" pattern="any_text" title="Enter Contact Number. [String Value]"></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>UBD Map Ref</strong></td>
                            <td align="left"><input name="ubd_1" type="text" id="ubd_1" pattern="any_text" title="Enter UBD Reference. [String Value]"></td>
                            <td align="left">&nbsp;</td>
                            <td align="left">&nbsp;</td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Address</strong></td>
                            <td colspan="3" align="left"><input name="address_1" type="text" id="address_1" title="Enter Location Address. [String Value]" size="80" pattern="any_text"></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Police</strong></td>
                            <td colspan="3" align="left"><input name="police_1" type="text" id="police_1" title="Enter Police Details. [String Value]" size="80" pattern="any_text"></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Hospital</strong></td>
                            <td colspan="3" align="left"><input name="hospital_1" type="text" id="hospital_1" title="Enter Hospital Details. [String Value]" size="80" pattern="any_text"></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Parking</strong></td>
                            <td colspan="3" align="left"><textarea name="parking_1" cols="70" id="parking_1" title="Enter Parking Instructions. [String Value]"></textarea></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Unit</strong></td>
                            <td colspan="3" align="left"><textarea name="unit_1" cols="70" id="unit_1" title="Enter Unit Instructions. [String Value]"></textarea></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr>
                            <td class="left">&nbsp;</td>
                            <td align="left"><strong>Location Notes</strong></td>
                            <td colspan="3" align="left"><textarea name="notes_1" cols="70" rows="5" id="notes_1" title="Enter Location Notes. [String Value]"></textarea></td>
                            <td class="right">&nbsp;</td>
				      </tr>
						  <tr><td colspan="6" class="bottom">&nbsp;</td></tr>
						  <%} }%>
	           
					</tbody>
				</table></td>
            </tr>
        </table>
		
		<table width='700' border='0' cellpadding='10' cellspacing='0'>
			<tr>
				<td width="1%"/>
				<td>
					<input name="button" type="button" onClick="createEntry();" value="Insert Row" />
					<input name="button_delete" type="button" onClick="deleteEntry();" value="Delete Row" />
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
			<input type="hidden" name="submit" id="submit"></p></form>
		<!-- LOAD -->
    <form method="post" action="Revise_Location_Notes_7866.jsp?formType=load&workItemID=<%= request.getParameter("workItemID") %>&userID=<%= request.getParameter("userID") %>&sessionHandle=<%= request.getParameter("sessionHandle") %>&JSESSIONID=<%= request.getParameter("JSESSIONID") %>&submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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
	</td>
<!-- END LOAD -->
	<td width="700">&nbsp;</td>
    <td width="700" class="background_right">&nbsp;</td>
  </tr>
  <tr><td colspan="3">&nbsp;</td></tr>
</table>
<%
if(request.getParameter("Submission") != null){
	
	int count = Integer.parseInt(request.getParameter("count"));
	
	LocationInfoType li = new LocationInfoType();
	for (int i=1;i<=count;i++){
		SingleLocationInfoType slit = new SingleLocationInfoType();
		
		slit.setLocationID(request.getParameter("location_ID_" + i));
		slit.setLocationName(request.getParameter("location_name_" + i));
		slit.setAddress(request.getParameter("address_" + i));
		slit.setUBDMapRef(request.getParameter("ubd_" + i));
		slit.setParking(request.getParameter("parking_" + i));
		slit.setUnit(request.getParameter("unit_" + i));
		slit.setPolice(request.getParameter("police_" + i));
		slit.setHospital(request.getParameter("hospital_" + i));
		slit.setContact(request.getParameter("contact_" + i));
		slit.setContactNo(request.getParameter("phone_" + i));
		slit.setLocationNotes(request.getParameter("notes_" + i));
		
		li.getSingleLocationInfo().add(slit);
	}
	rln.setProduction(request.getParameter("production"));
	rln.setLocationInfo(li);
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	File f = new File("./backup/ReviseLocationNotes_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
	m.marshal( rlnElement,  f);//output to file
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(rlnElement, xmlOS);//out to ByteArray
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
	
	LocationInfoType li = new LocationInfoType();
	for (int i=1;i<=count;i++){
		SingleLocationInfoType slit = new SingleLocationInfoType();
		
		slit.setLocationID(request.getParameter("location_ID_" + i));
		slit.setLocationName(request.getParameter("location_name_" + i));
		slit.setAddress(request.getParameter("address_" + i));
		slit.setUBDMapRef(request.getParameter("ubd_" + i));
		slit.setParking(request.getParameter("parking_" + i));
		slit.setUnit(request.getParameter("unit_" + i));
		slit.setPolice(request.getParameter("police_" + i));
		slit.setHospital(request.getParameter("hospital_" + i));
		slit.setContact(request.getParameter("contact_" + i));
		slit.setContactNo(request.getParameter("phone_" + i));
		slit.setLocationNotes(request.getParameter("notes_" + i));
		
		li.getSingleLocationInfo().add(slit);
	}
	rln.setProduction(request.getParameter("production"));
	rln.setLocationInfo(li);
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(rlnElement, xmlOS);//out to ByteArray
	
	response.setHeader("Content-Disposition", "attachment;filename=\"ReviseLocationNotes_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
	response.setHeader("Content-Type", "text/xml");
	
	ServletOutputStream outs = response.getOutputStream();
	xmlOS.writeTo(outs);
	outs.close();
}
%>
</body>
</html>