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
<%@ page import="org.yawlfoundation.sb.camerainfo.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="1024kb" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Camera Sheet</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<link href="graphics/style.css" rel="stylesheet" type="text/css" />
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js"></script>
<script type="text/javascript" src="scripts/fillOutCameraSheets40.js"></script>

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
            int endOfFile = result.indexOf("</ns2:Fill_Out_Camera_Sheets>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(beginOfFile, endOfFile + 29);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		
		xml = (String)session.getAttribute("outputData");
		xml = xml.replaceAll("<Fill_Out_Camera_Sheets", "<ns2:Fill_Out_Camera_Sheets xmlns:ns2='http://www.yawlfoundation.org/sb/cameraInfo'");
		xml = xml.replaceAll("</Fill_Out_Camera_Sheets","</ns2:Fill_Out_Camera_Sheets");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.camerainfo");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement focsElement = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file	            
	FillOutCameraSheetsType focs = (FillOutCameraSheetsType) focsElement.getValue();
	GeneralInfoType gi = focs.getGeneralInfo();
	CameraInfoType ci = focs.getCameraInfo();
	
%>
<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td colspan="3" class="background_top">&nbsp;</td></tr>
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td><h1 align="center">Camera Sheet </h1>
		<form name="form1" method="post">
  		<table width="700"  border="0" align="center">
			<tr>
			  <td width="998" colspan="5"><table width="700" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td class="header-left">&nbsp;</td>
                  <td colspan="4" class="header-middle">Sheet Number </td>
                  <td class="header-right">&nbsp;</td>
                </tr>
                <tr height="30">
                  <td class="left" width="15">&nbsp;</td>
                  <td align="center"><strong>Sheet Number 
                  </strong></td>
                  <td align="center"><input name="sheet_number" type="text" id="sheet_number" value="<%=ci.getSheetNumber() %>" pattern="number" title="Enter Sheet Number. [Number Value]"></td>
                  <td align="center"><strong>Camera Roll </strong></td>
                  <td align="center"><input name="roll" type="text" id="roll" size="20" value="<%=ci.getCamRoll()%>" pattern="any_text" title="Enter Camera Roll. [String Value]"></td>
                  <td class="right" width="15">&nbsp;</td>
                </tr>
                <tr height="30">
                  <td colspan="6" class="bottom">&nbsp;</td>
                </tr>
              </table></td>
		  </tr>
			<tr><td colspan="5">
				<table width="700" border="0" cellspacing="0" cellpadding="0">
					<tr><td width="15" class="header-left">&nbsp;</td>
					  <td colspan="6" class="header-middle">General Info </td>
					  <td width="15" class="header-right">&nbsp;</td>
					</tr>
					<tr height="30">
						<td class="left" width="15">&nbsp;</td>
						<td><strong>Production</strong></td><td width="150"><input name="production" type="text" id="production" value="<%= gi.getProduction()%>" size="20" readonly></td>
						<td><strong>Date</strong></td><td width="150"><input name="date" type="text" id="date" value="<%= gi.getDate()%>" size="20" readonly></td>
						<td><strong>Day</strong></td><td width="150"><input name="weekday" type="text" id="weekday" value="<%=gi.getWeekday()%>" size="20" readonly></td>
						<td class="right" width="15">&nbsp;</td>
					</tr>
					<tr height="30">
						<td class="left" width="15">&nbsp;</td>
						<td><strong>Producer</strong></td><td width="150"><input name="producer" type="text" id="producer" value="<%=focs.getProducer()%>" size="20" readonly></td>
						<td><strong>Director</strong></td><td width="150"><input name="director" type="text" id="director" value="<%=focs.getDirector()%>" size="20" readonly></td>
						<td><strong>Shoot Day </strong></td><td width="150"><input name="shoot_day" type="text" id="shoot_day" value="<%=gi.getShootDayNo()%>" size="20" readonly></td>
						<td class="right" width="15">&nbsp;</td>
					</tr>
					<tr height="30">
					  <td width="15" class="left">&nbsp;</td>
					  <td><strong>Director of Photography</strong></td>
					  <td width="150"><input name="director_photography" type="text" id="director_photography" value="<%= focs.getDirectorOfPhotography() %>" size="20" readonly></td>
					  <td><strong>Camera Operator</strong></td>
					  <td width="150"><input name="camera_operator" type="text" id="camera_operator" value="<%=focs.getCameraOperator()%>" size="20" readonly></td>
					  <td><strong>Camera Assistant</strong></td>
					  <td width="150"><input name="camera_assistant" type="text" id="camera_assistant" value="<%=focs.getCameraAssistant()%>" size="20" readonly></td>
					  <td width="15" class="right">&nbsp;</td>
				  </tr> 
					<tr height="30">
						<td class="left" width="15">&nbsp;</td>
						<td><strong>Studios/Locations</strong></td><td colspan="5"><textarea name="studios_locations" cols="65" id="studios_locations" pattern="any_text"><%=ci.getStudiosLocation() %></textarea></td>
						<td class="right" width="15">&nbsp;</td>
					</tr>
					<tr height="30"><td colspan="8" class="bottom">&nbsp;</td>
					</tr>
			  </table>
			</td></tr>
		
		<tr><td>
			<table width="700" border="0" cellpadding="0" cellspacing="0">
				<tr><td class="header-left">&nbsp;</td>
				  <td colspan="4" class="header-middle">Tech Info </td>
				  <td class="header-right">&nbsp;</td>
				</tr>
				<%
				if(ci.getTechInfo() != null) {
					TechInfoType ti = ci.getTechInfo();
					%>
				<tr>
					<td width="15" align="right" class="left">&nbsp;</td>
					<td height="30"><strong>Camera Type and Number</strong></td>
					<td height="30"><input name="camera_type_number" type="text" id="camera_type_number" size="20" value="<%= ti.getCameraTypeAndNumber()%>" pattern="any_text" title="Enter Camera Type and Number. [String Value]"></td>
					<td height="30"><strong>Emulsion</strong></td>
					<td height="30"><input name="emulsion" type="text" id="emulsion" size="20" value="<%= ti.getEmulsion()%>" pattern="any_text" title="Enter Emulsion. [String Value]"></td>
					<td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" align="right" class="left">&nbsp;</td>
				    <td height="30"><strong>Stock Number</strong></td>
				  	<td height="30"><input name="stock_number" type="text" id="stock_number" size="20" value="<%= ti.getStockNumber()%>" pattern="any_text" title="Enter Stock Number. [String Value]"></td>
					<td height="30"><strong>Total Cans Number</strong></td>
					<td height="30"><input name="total_cans_number" type="text" id="total_cans_number" size="20" value="<%= ti.getTotalCansNumber()%>" pattern="number" title="Enter Total Cans Number. [Number Value]"></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<%}else{%>
				<tr>
					<td width="15" align="right" class="left">&nbsp;</td>
					<td height="30"><strong>Camera Type and Number</strong></td>
					<td height="30"><input name="camera_type_number" type="text" id="camera_type_number" size="20" pattern="any_text" title="Enter Camera Type and Number. [String Value]"></td>
					<td height="30"><strong>Emulsion</strong></td>
					<td height="30"><input name="emulsion" type="text" id="emulsion" size="20" pattern="any_text" title="Enter Emulsion. [String Value]"></td>
					<td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" align="right" class="left">&nbsp;</td>
				    <td height="30"><strong>Stock Number</strong></td>
				  	<td height="30"><input name="stock_number" type="text" id="stock_number" size="20" pattern="any_text" title="Enter Stock Number. [String Value]"></td>
					<td height="30"><strong>Total Cans Number</strong></td>
					<td height="30"><input name="total_cans_number" type="text" id="total_cans_number" size="20" pattern="number" title="Enter Total Cans Number. [Number Value]"></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<%}%>
				<tr>
				  <td colspan="6" class="bottom">&nbsp;</td>
			    </tr>
		  </table>
		</td>
	</tr>
	
	<tr>
		<td>
			<table width="700" border="0" cellpadding="0" cellspacing="0" bordercolor="#000000" id="cam_roll_info">
				<tbody>
					<tr align="center" valign="top">
					    <td width="15" class="header-left">&nbsp;</td>
			            <td colspan="9" align="left" class="header-middle">Camera Roll Info </td>
			            <td width="15" class="header-right">&nbsp;</td>
					</tr>
					<tr align="center" valign="top">
					  <td width="15" rowspan="2" class="left">&nbsp;</td>
						<td rowspan="2"><strong>Mag No.</strong></td>
						<td rowspan="2"><strong>Slate No.</strong></td>
						<td rowspan="2"><strong>Take No.</strong></td>
						<td rowspan="2"><strong>Counter Reading</strong></td>
						<td rowspan="2"><strong>Take<br>Length</strong></td>
						<td rowspan="2"><strong>Print</strong></td>
						<td colspan="2"><strong>Print Setting</strong></td>
						<td><strong>Essential Information/ General Notes</strong></td>
					    <td width="15" rowspan="2" class="right">&nbsp;</td>
					</tr>
					<tr valign="top">
						<th><strong>B/W</strong></th>
						<td align="center"><strong>Colour</strong></td>
						<td align="center"><em>Colour description of scene, filter and/or diffusion used, Day, night or other effects.</em></td>
					</tr>
				  
				   <%
				   	int a=0;
						if(ci.getSlateInfo().isEmpty() == false) {
						for(SlateInfoType sl : ci.getSlateInfo()){
							for(TakeInfoType ti : sl.getTakeInfo()){
								a++;
					%>
					<tr align="center" valign="top">
						<td width="15" class="left">&nbsp;</td>
						<td><input name="mag_number_<%=a%>" type="text" id="mag_number_<%=a%>" size="5" value="<%= sl.getMagNumber() %>" pattern="any_text" title="Enter Mag Number. [String Value]"></td>
						<td><input name="slate_<%=a%>" type="text" id="slate_<%=a%>" size="5" value="<%= sl.getSlate() %>" pattern="any_text" title="Enter Slate. [String Value]"></td>
						<td><input name="take_<%=a%>" type="text" id="take_<%=a%>" size="5" value="<%= ti.getTake() %>" pattern="number" title="Enter Take Number. [Number Value]"></td>
						<td><input name="counter_reading_<%=a%>" type="text" id="counter_reading_<%=a%>" size="6" value="<%= ti.getCounter() %>" pattern="number" title="Enter Counter Reading. [Number Value]"></td>
						<td><input name="take_length_<%=a%>" type="text" id="take_length_<%=a%>" size="6" value="<%= ti.getLength() %>" pattern="number" title="Enter Take Length. [Number Value]"></td>
						<td><input name="print_<%=a%>" type="checkbox" id="print_<%=a%>" value="True" <% if(ti.isPrint() == true) {out.print("checked");}%>></td>
						<td><input name="print_setting_<%=a%>" id="radio" type="radio" value="B/W" <% if(!(ti.getPrintSetting().equals("Colour"))){out.print("checked");}%>></td>
						<td><input name="print_setting_<%=a%>" id="radio" type="radio" value="Colour" <% if(ti.getPrintSetting().equals("Colour")){out.print("checked");}%>></td>
					    <td><textarea name="notes_<%=a%>" cols="30" id="notes_<%=a%>" title="Enter Notes. [String Value]"><%= ti.getEssentialInfo() %></textarea></td>
					    <td width="15" align="right" class="right">&nbsp;</td>
					</tr>
					<% 
							}
						}
					}else {
					%>
					<tr align="center" valign="top">
						<td width="15" class="left">&nbsp;</td>
						<td><input name="mag_number_1" type="text" id="mag_number_1" size="5" pattern="any_text" title="Enter Mag Number. [String Value]"></td>
						<td><input name="slate_1" type="text" id="slate_1" size="5" pattern="any_text" title="Enter Slate. [String Value]"></td>
						<td><input name="take_1" type="text" id="take_1" size="5" pattern="number" title="Enter Take Number. [Number Value]"></td>
						<td><input name="counter_reading_1" type="text" id="counter_reading_1" value="0" size="6" pattern="number" title="Enter Counter Reading. [Number Value]"></td>
						<td><input name="take_length_1" type="text" id="take_length_1" size="6" value="0" pattern="number" title="Enter Take Length. [Number Value]"></td>
						<td><input name="print_1" type="checkbox" id="print_1" value="True"></td>
						<td><input name="print_setting_1" id="radio" type="radio" value="B/W" checked></td>
						<td><input name="print_setting_1" id="radio" type="radio" value="Colour"></td>
					    <td><textarea name="notes_1" cols="30" id="textarea" title="Enter Notes. [String Value]"></textarea></td>
					    <td width="15" align="right" class="right">&nbsp;</td>
					</tr>
					<%}%>
				</tbody>
	            <tbody>
	                <tr valign="top">
	                    <th class="bottom" colspan="11">&nbsp; </th>
					</tr>
				</tbody>
	        </table>
			<table width="700" border="0" cellpadding="10" cellspacing="0">
				</tbody>
					<tr>
						<td width="1%"/>
						<td>
							<input name="button" type="button" onClick="addRow();" value="Insert Row"/>
							<input name="button_delete" type="button" onClick="deleteRow();" value="Delete Row"/></td>
						<td>&nbsp;</td>
					</tr>
				</tbody>
			</table>
		</td>
	</tr>
	
	<tr>
	  <td><table width="700" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td class="header-left">&nbsp;</td>
          <td class="header-middle">Short End Minimum</td>
          <td class="header-right">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="left">&nbsp;</td>
          <td height="30"><input name="short_end_minimum" type="text" id="short_end_minimum" value="<%=ci.getShortEndMin() %>" pattern="number" title="Enter Short End Minimum. [Number Value]"></td>
          <td width="15" class="right">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="3" class="bottom">&nbsp;</td>
        </tr>
      </table></td>
	  </tr>
	<tr>
		<td>
			<table width="700" border="0" cellpadding="0" cellspacing="0">
				<tr>
				  <td width="15" class="header-left">&nbsp;</td>
		          <td colspan="7" class="header-middle">Camera Info Summary </td>
		          <td width="15" class="header-right">&nbsp;</td>
				</tr>
				<% if(ci.getCamInfoSum() != null) { 
				CamInfoSumType cis = ci.getCamInfoSum();%>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="80" height="30"><strong>Footage (Loaded)</strong></td>
			  	  <td width="50" height="30"><input name="footage_loaded" type="text" id="footage_loaded" size="5" value="<%=cis.getFootageLoaded() %>" pattern="number" title="Enter Footage Loaded. [Number Value]"></td>
					<td width="80" height="30"><strong>Total Exposed</strong></td>
				  <td width="50" height="30"><input name="total_exposed_2" type="text" id="total_exposed_2" size="5" readonly></td>
					<td width="80" height="30"><strong>Total Ftge Prev Drawn</strong></td>
				  <td width="50" height="30"><input name="total_footage_prev_drawn" type="text" id="total_footage_prev_drawn" size="5" readonly></td>
					<td width="100" height="30"><strong>Instructions to Laboratory</strong></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="80" height="30"><strong>Total Exposed </strong></td>
				  <td width="50" height="30"><input name="total_exposed" type="text" id="total_exposed" size="5" value="<%=cis.getTotalExposed() %>" pattern="number" title="Calculate Total Exposed. [Number Value]"></td>
					<td width="80" height="30"><strong>Total Developed</strong></td>
				  <td width="50" height="30"><input name="total_developed" type="text" id="total_developed" size="5" readonly></td>
					<td width="80" height="30"><strong>Footage Drn Today</strong></td>
				  <td width="50" height="30"><input name="footage_drawn_today" type="text" id="footage_drawn_today" size="5" readonly></td>
					<td width="100" height="30"><textarea name="instructions" cols="20" id="instructions"><%=ci.getInstructionsToLab() %></textarea></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="80" height="30"><strong>Short Ends</strong></td>
			  	  <td width="50" height="30"><input name="short_ends" type="text" id="short_ends" size="5" value="<%=cis.getShortEnds() %>" pattern="number" title="Calculate Short Ends. [Number Value]"></td>
					<td width="80" height="30"><strong>Total Printed</strong></td>
				  <td width="50" height="30"><input name="total_printed" type="text" id="total_printed" size="5" readonly></td>
					<td width="80" height="30"><strong>Previously Exposed</strong></td>
				  <td width="50" height="30"><input name="previously_exposed" type="text" id="previously_exposed" size="5" readonly></td>
					<td width="100" height="30"><em>Signed by Camera Assistant </em></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="80" height="30"><strong>Waste</strong></td>
				  <td width="50" height="30"><input name="waste" type="text" id="waste" size="5" value="<%=cis.getWaste() %>" pattern="number" title="Calculate Waste. [Number Value]"></td>
					<td width="80" height="30"><strong>Held or Not Sent</strong></td>
				  <td width="50" height="30"><input name="held_notsent" type="text" id="held_notsent" size="5" readonly></td>
					<td width="80" height="30"><strong>Exposed Today</strong></td>
				  <td width="50" height="30"><input name="exposed_today" type="text" id="exposed_today" size="5" readonly></td>
					<td width="100" height="30"><input name="assistant_signature" type="text" id="assistant_signature" value="<%= ci.getSignatureOfCameraAssistant() %>" size="20"></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<%}else{%>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="80" height="30"><strong>Footage (Loaded)</strong></td>
			  	  <td width="50" height="30"><input name="footage_loaded" type="text" id="footage_loaded" size="5" pattern="number" title="Enter Footage Loaded. [Number Value]"></td>
					<td width="80" height="30"><strong>Total Exposed</strong></td>
				  <td width="50" height="30"><input name="total_exposed_2" type="text" id="total_exposed_2" size="5" readonly></td>
					<td width="80" height="30"><strong>Total Ftge Prev Drawn</strong></td>
				  <td width="50" height="30"><input name="total_footage_prev_drawn" type="text" id="total_footage_prev_drawn" size="5" readonly></td>
					<td width="100" height="30"><strong>Instructions to Laboratory</strong></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="80" height="30"><strong>Total Exposed </strong></td>
				  <td width="50" height="30"><input name="total_exposed" type="text" id="total_exposed" size="5" pattern="number" title="Calculate Total Exposed. [Number Value]"></td>
					<td width="80" height="30"><strong>Total Developed</strong></td>
				  <td width="50" height="30"><input name="total_developed" type="text" id="total_developed" size="5" readonly></td>
					<td width="80" height="30"><strong>Footage Drn Today</strong></td>
				  <td width="50" height="30"><input name="footage_drawn_today" type="text" id="footage_drawn_today" size="5" readonly></td>
					<td width="100" height="30"><textarea name="instructions" cols="20" id="instructions"><%=ci.getInstructionsToLab() %></textarea></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="80" height="30"><strong>Short Ends</strong></td>
			  	  <td width="50" height="30"><input name="short_ends" type="text" id="short_ends" size="5" pattern="number" title="Calculate Short Ends. [Number Value]"></td>
					<td width="80" height="30"><strong>Total Printed</strong></td>
				  <td width="50" height="30"><input name="total_printed" type="text" id="total_printed" size="5" readonly></td>
					<td width="80" height="30"><strong>Previously Exposed</strong></td>
				  <td width="50" height="30"><input name="previously_exposed" type="text" id="previously_exposed" size="5" readonly></td>
					<td width="100" height="30"><em>Signed by Camera Assistant </em></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="80" height="30"><strong>Waste</strong></td>
				  <td width="50" height="30"><input name="waste" type="text" id="waste" size="5" pattern="number" title="Calculate Waste. [Number Value]"></td>
					<td width="80" height="30"><strong>Held or Not Sent</strong></td>
				  <td width="50" height="30"><input name="held_notsent" type="text" id="held_notsent" size="5" readonly></td>
					<td width="80" height="30"><strong>Exposed Today</strong></td>
				  <td width="50" height="30"><input name="exposed_today" type="text" id="exposed_today" size="5" readonly></td>
					<td width="100" height="30"><input name="assistant_signature" type="text" id="assistant_signature" value="<%= ci.getSignatureOfCameraAssistant() %>" size="20"></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<%}%>
				<tr>
				  <td width="15" class="left">&nbsp;</td>
		          <td colspan="7"><input name="button2" type="button" onClick="calculate();" value="Calculate"/></td>
		          <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
				  <td colspan="9" class="bottom">&nbsp;</td>
			    </tr>
			</table>
		</td>
	</tr>
	
   	<tr>
   	  <td align="center">Another Roll
   	    <input name="another_roll" type="checkbox" id="another_roll" value="True" <% if(focs.isAnotherRoll() == true) {out.print("checked");}%>></td>
 	  </tr>
   	<tr>
	<%StockInfoType sti = focs.getStockInfo();%>
   	  <td><input type="hidden" name="loaded_hidden" id="loaded_hidden" value="<%=sti.getLoaded()%>"/>
   	    <input type="hidden" name="gross_hidden" id="gross_hidden" value="<%=sti.getGross()%>"/>
   	    <input type="hidden" name="exposed_hidden" id="exposed_hidden" value="<%=sti.getExposed()%>"/>
   	    <input type="hidden" name="print_hidden" id="print_hidden" value="<%=sti.getPrint()%>"/>
   	    <input type="hidden" name="ng_hidden" id="ng_hidden" value="<%=sti.getNG()%>"/>
   	    <input type="hidden" name="waste_hidden" id="waste_hidden" value="<%=sti.getWaste()%>"/>
   	    <input type="hidden" name="shortends_hidden" id="shortends_hidden" value="<%=sti.getShortEnds()%>"/>
		<input type="hidden" name="camrolls_hidden" id="camrolls_hidden" value="<%=focs.getCamRolls()%>"/></td>
 	  </tr>
   	<tr><td>
		<input type="hidden" name="count" id="count" value="<%if (a==0) {out.print("1");}else{out.print(a);}%>"/>
		<input type="hidden" name="workItemID" id="workItemID"/>
		<input type="hidden" name="userID" id="userID"/>
		<input type="hidden" name="sessionHandle" id="sessionHandle"/>
		<input type="hidden" name="JSESSIONID" id="JSESSIONID"/>
		<input type="hidden" name="submit" id="submit"/>
	</td></tr>
  </table>
  <p align="center">
  <input type="button" value="Print"  onclick="window.print()"/>
  <input type="submit" name="Save" value="Save" onclick="return validateFields('form1');"/>
  <input type="submit" name="Submission" value="Submission" onclick="return validateFields('form1');"/></p>
</form>
<!-- LOAD -->
    <form method="post" action="Fill_Out_Camera_Sheets_40.jsp?formType=load&workItemID=<%= request.getParameter("workItemID") %>&userID=<%= request.getParameter("userID") %>&sessionHandle=<%= request.getParameter("sessionHandle") %>&JSESSIONID=<%= request.getParameter("JSESSIONID") %>&submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
      <table width="60%" border="0" cellspacing="1" cellpadding="1" align="center" class="style1">
        <tr>
          <td align="left"><b>Select a file to upload :</b></td>
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
<!-- END LOAD --></td>
    <td width="14" class="background_right">&nbsp;</td>
  </tr>
  <tr>
    <td colspan="3" class="background_bottom">&nbsp;</td>
  </tr>
</table>
<%
if(request.getParameter("Submission") != null){
	
	int count = Integer.parseInt(request.getParameter("count"));
	
	StockInfoType sit = new StockInfoType();
	sit.setLoaded(new BigInteger(request.getParameter("loaded_hidden")));
	sit.setGross(new BigInteger(request.getParameter("gross_hidden")));
	sit.setExposed(new BigInteger(request.getParameter("exposed_hidden")));
	sit.setPrint(new BigInteger(request.getParameter("print_hidden")));
	sit.setNG(new BigInteger(request.getParameter("ng_hidden")));
	sit.setWaste(new BigInteger(request.getParameter("waste_hidden")));
	sit.setShortEnds(new BigInteger(request.getParameter("shortends_hidden")));

	TechInfoType thi =  new TechInfoType();
	thi.setCameraTypeAndNumber(request.getParameter("camera_type_number"));
	thi.setStockNumber(request.getParameter("stock_number"));
	thi.setEmulsion(request.getParameter("emulsion"));
	thi.setTotalCansNumber(new BigInteger(request.getParameter("total_cans_number")));

	CamInfoSumType cam1 = new CamInfoSumType();
	cam1.setFootageLoaded(new BigInteger(request.getParameter("footage_loaded")));
	cam1.setTotalExposed(new BigInteger(request.getParameter("total_exposed")));
	cam1.setShortEnds(new BigInteger(request.getParameter("short_ends")));
	cam1.setWaste(new BigInteger(request.getParameter("waste")));
	
	SlateInfoType tempSlate = null;
	String tempSlateNO = null;
	Map<String,SlateInfoType> slates = new TreeMap<String,SlateInfoType>();
	
	for (int i=1;i<=count;i++){//takes are ordered within each slate. Slates are backwards ordered. Scenes are backwards ordered.
		TakeInfoType ti = new TakeInfoType();
		ti.setTake(new BigInteger(request.getParameter("take_"+i)));
		ti.setCounter(new BigInteger(request.getParameter("counter_reading_"+i)));
		ti.setLength(new BigInteger(request.getParameter("take_length_"+i)));
		
		if (request.getParameter("print_"+i)==null){
			ti.setPrint(false);
		}
		else{
			ti.setPrint(true);
		}
		
		ti.setPrintSetting(request.getParameter("print_setting_"+i));
		ti.setEssentialInfo(request.getParameter("notes_"+i));
				
		tempSlateNO=request.getParameter("slate_"+i);
		
		tempSlate = slates.get(tempSlateNO);
		if (tempSlate==null){
			SlateInfoType si = new SlateInfoType();
			si.setMagNumber(request.getParameter("mag_number_" + i));
			si.setSlate(tempSlateNO);
			si.getTakeInfo().add(ti);
			slates.put(tempSlateNO, si);//add the newly created slate into the "slates" map
		}
		else{//the slateNO already exists
			tempSlate.getTakeInfo().add(ti);
		}
	}
	
	List<SlateInfoType> sl = new ArrayList<SlateInfoType>(slates.values());//creates a list of the scenes and adds it to the cameraInfo facade
	
	CameraInfoType cit = new CameraInfoType();
	cit.setSheetNumber(new BigInteger(request.getParameter("sheet_number")));
	cit.setCamRoll(request.getParameter("roll"));
	cit.setStudiosLocation(request.getParameter("studios_locations"));
	cit.setTechInfo(thi);
	cit.getSlateInfo().addAll(sl);
	cit.setCamInfoSum(cam1);
	cit.setShortEndMin(new BigInteger(request.getParameter("short_end_minimum")));
	cit.setInstructionsToLab(request.getParameter("instructions"));
	cit.setSignatureOfCameraAssistant(request.getParameter("assistant_signature"));
	
	gi.setProduction(request.getParameter("production"));
	gi.setDate(XMLGregorianCalendarImpl.parse(request.getParameter("date")));
	gi.setWeekday(request.getParameter("weekday"));
	gi.setShootDayNo(new BigInteger(request.getParameter("shoot_day")));
	
	focs.setProducer(request.getParameter("producer"));
	focs.setDirector(request.getParameter("director"));
	focs.setDirectorOfPhotography(request.getParameter("director_photography"));
	focs.setCameraOperator(request.getParameter("camera_operator"));
	focs.setCameraAssistant(request.getParameter("camera_assistant"));
	focs.setCameraInfo(cit);
	if (request.getParameter("another_roll")==null){
		focs.setAnotherRoll(false);
	}else{
		focs.setAnotherRoll(true);
	}
	focs.setStockInfo(sit);
	focs.setCamRolls(request.getParameter("camrolls_hidden"));
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	File f = new File("./backup/CameraSheets_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
	m.marshal( focsElement,  f);//output to file
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(focsElement, xmlOS);//out to ByteArray
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
	
	StockInfoType sit = new StockInfoType();
	sit.setLoaded(new BigInteger(request.getParameter("loaded_hidden")));
	sit.setGross(new BigInteger(request.getParameter("gross_hidden")));
	sit.setExposed(new BigInteger(request.getParameter("exposed_hidden")));
	sit.setPrint(new BigInteger(request.getParameter("print_hidden")));
	sit.setNG(new BigInteger(request.getParameter("ng_hidden")));
	sit.setWaste(new BigInteger(request.getParameter("waste_hidden")));
	sit.setShortEnds(new BigInteger(request.getParameter("shortends_hidden")));

	TechInfoType thi =  new TechInfoType();
	thi.setCameraTypeAndNumber(request.getParameter("camera_type_number"));
	thi.setStockNumber(request.getParameter("stock_number"));
	thi.setEmulsion(request.getParameter("emulsion"));
	thi.setTotalCansNumber(new BigInteger(request.getParameter("total_cans_number")));

	CamInfoSumType cam1 = new CamInfoSumType();
	cam1.setFootageLoaded(new BigInteger(request.getParameter("footage_loaded")));
	cam1.setTotalExposed(new BigInteger(request.getParameter("total_exposed")));
	cam1.setShortEnds(new BigInteger(request.getParameter("short_ends")));
	cam1.setWaste(new BigInteger(request.getParameter("waste")));
	
	SlateInfoType tempSlate = null;
	String tempSlateNO = null;
	Map<String,SlateInfoType> slates = new TreeMap<String,SlateInfoType>();
	
	for (int i=1;i<=count;i++){//takes are ordered within each slate. Slates are backwards ordered. Scenes are backwards ordered.
		TakeInfoType ti = new TakeInfoType();
		ti.setTake(new BigInteger(request.getParameter("take_"+i)));
		ti.setCounter(new BigInteger(request.getParameter("counter_reading_"+i)));
		ti.setLength(new BigInteger(request.getParameter("take_length_"+i)));
		
		if (request.getParameter("print_"+i)==null){
			ti.setPrint(false);
		}
		else{
			ti.setPrint(true);
		}
		
		ti.setPrintSetting(request.getParameter("print_setting_"+i));
		ti.setEssentialInfo(request.getParameter("notes_"+i));
				
		tempSlateNO=request.getParameter("slate_"+i);
		
		tempSlate = slates.get(tempSlateNO);
		if (tempSlate==null){
			SlateInfoType si = new SlateInfoType();
			si.setMagNumber(request.getParameter("mag_number_" + i));
			si.setSlate(tempSlateNO);
			si.getTakeInfo().add(ti);
			slates.put(tempSlateNO, si);//add the newly created slate into the "slates" map
		}
		else{//the slateNO already exists
			tempSlate.getTakeInfo().add(ti);
		}
	}
	
	List<SlateInfoType> sl = new ArrayList<SlateInfoType>(slates.values());//creates a list of the scenes and adds it to the cameraInfo facade
	
	CameraInfoType cit = new CameraInfoType();
	cit.setSheetNumber(new BigInteger(request.getParameter("sheet_number")));
	cit.setCamRoll(request.getParameter("roll"));
	cit.setStudiosLocation(request.getParameter("studios_locations"));
	cit.setTechInfo(thi);
	cit.getSlateInfo().addAll(sl);
	cit.setCamInfoSum(cam1);
	cit.setShortEndMin(new BigInteger(request.getParameter("short_end_minimum")));
	cit.setInstructionsToLab(request.getParameter("instructions"));
	cit.setSignatureOfCameraAssistant(request.getParameter("assistant_signature"));
	
	gi.setProduction(request.getParameter("production"));
	gi.setDate(XMLGregorianCalendarImpl.parse(request.getParameter("date")));
	gi.setWeekday(request.getParameter("weekday"));
	gi.setShootDayNo(new BigInteger(request.getParameter("shoot_day")));
	
	
	focs.setProducer(request.getParameter("producer"));
	focs.setDirector(request.getParameter("director"));
	focs.setDirectorOfPhotography(request.getParameter("director_photography"));
	focs.setCameraOperator(request.getParameter("camera_operator"));
	focs.setCameraAssistant(request.getParameter("camera_assistant"));
	focs.setCameraInfo(cit);
	if (request.getParameter("another_roll")==null){
		focs.setAnotherRoll(false);
	}else{
		focs.setAnotherRoll(true);
	}
	focs.setStockInfo(sit);
	focs.setCamRolls(request.getParameter("camrolls_hidden"));
	
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(focsElement, xmlOS);//out to ByteArray

	response.setHeader("Content-Disposition", "attachment;filename=\"CameraSheets_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
	response.setHeader("Content-Type", "text/xml");

	ServletOutputStream outs = response.getOutputStream();
	xmlOS.writeTo(outs);
	outs.close();
}
%>

</body>
</html>