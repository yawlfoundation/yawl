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

<script language="javascript">
var count = 1;
var headerSize = 3;
var footerSize = 1;
var headerAndFooterSize = headerSize + footerSize;

function deleteRow() {
    var table = document.getElementById("table1");
    var rows = table.rows.length;

    if (rows > headerAndFooterSize) {
        //delete from the bottom, removing 1 for the 0-based index.
        table.deleteRow(rows-(footerSize+1));
        if (count > 0) {
            document.getElementById("count").value = --count;
        }

        if (count == 0) {
            //add an empty row after the last row has been deleted.
            //this allows all data-containing rows to be deleted and for there to be a single empty row
            //at any point in time.
            addRow();
        }
    }
}

function addRow(){
	var tbody = document.getElementById("table1").getElementsByTagName("tbody")[0];
	var row = document.createElement("TR");
	var leftCELL = document.createElement("TD");
	var magCELL = document.createElement("TD");
	var slateCELL = document.createElement("TD");
	var takeCELL = document.createElement("TD");
	var counterreadingCELL = document.createElement("TD");
	var takelengthCELL = document.createElement("TD");
	var printCELL = document.createElement("TD");
	var bwCELL = document.createElement("TD");
	var colourCELL = document.createElement("TD");
	var notesCELL = document.createElement("TD");
	var rightCELL = document.createElement("TD");
	var printINPUT =  document.createElement("INPUT");

	//var previous_counter_reading = parseInt(document.getElementById("counter_reading_" + count).value);
	var previous_counter_reading = parseInt(count).value;
	//var previous_take_length = parseInt(document.getElementById("take_length_" + count).value);
	var previous_take_length = parseInt(count).value;
	var current_counter_reading = previous_counter_reading + previous_take_length;

	//count = document.getElementById("count").value;
	//document.getElementById("count").value = count;

	row.setAttribute("valign", "top");
	row.setAttribute("align", "center");

	leftCELL.className = "left";
	leftCELL.appendChild(document.createTextNode("\u00a0"));
	rightCELL.className = "right";
	rightCELL.appendChild(document.createTextNode("\u00a0"));

	printINPUT.setAttribute("type", "checkbox");
	printINPUT.setAttribute("value", "True");
	printINPUT.setAttribute("name", "print_" + count);
	printINPUT.setAttribute("id", "print_" + count);

	magCELL.appendChild(createTextBox("mag_number_" + count, 5, ""));
	slateCELL.appendChild(createTextBox("slate_" + count, 5, ""));
	takeCELL.appendChild(createTextBox("take_" + count, 5, ""));
	counterreadingCELL.appendChild(createTextBox("counter_reading_" + count, 6, count));
	takelengthCELL.appendChild(createTextBox("take_length_" + count, 6, ""));
	printCELL.appendChild(printINPUT);
	bwCELL.appendChild(createRadioButton("print_setting_" + count, "B/W"));
	colourCELL.appendChild(createRadioButton("print_setting_" + count, "Colour"));
	notesCELL.appendChild(createTextArea("notes_" + count, 30));

	count ++;
	
	row.appendChild(leftCELL);
	row.appendChild(magCELL);
	row.appendChild(slateCELL);
	row.appendChild(takeCELL);
	row.appendChild(counterreadingCELL);
	row.appendChild(takelengthCELL);
	row.appendChild(printCELL);
	row.appendChild(bwCELL);
	row.appendChild(colourCELL);
	row.appendChild(notesCELL);
	row.appendChild(rightCELL);
	tbody.appendChild(row);
	//alert(row.innerHTML);
}

//function for textbox details
function createTextBox(id, size, value) {
	var input =  document.createElement("INPUT");
	input.setAttribute("size", size);
	input.setAttribute("name", id);
	input.setAttribute("id", id);
	input.setAttribute("value", value);
	return input;
}

//function for radio button details
function createRadioButton(id, value) {
	var input =  document.createElement("INPUT");
	input.setAttribute("type", "radio");
	input.setAttribute("name", id);
	input.setAttribute("id", id);
	input.setAttribute("value", value);
	return input;
}

//function for textarea details
function createTextArea(id, size) {
	var input =  document.createElement("TEXTAREA");
	input.setAttribute("cols", size);
	input.setAttribute("name", id);
	input.setAttribute("id", id);
	return input;
}


function calculate(){
	var s_ends = null;
	var t_exposed = parseInt(document.getElementById("counter_reading_"+ count).value) + parseInt(document.getElementById("take_length_"+ count).value);
	document.getElementById("total_exposed").value = t_exposed;
	s_ends = parseInt(document.getElementById("footage_loaded").value ) - t_exposed;
	s_ends_minimum = parseInt(document.getElementById("short_end_minimum").value);
	if (s_ends < s_ends_minimum){
		document.getElementById("waste").value = s_ends;
		document.getElementById("short_ends").value = 0;
	}else{
		document.getElementById("waste").value = 0;
		document.getElementById("short_ends").value = s_ends;
	}
}

</script>
<link href="graphics/style.css" rel="stylesheet" type="text/css">
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
		xml = "<?xml version='1.0' encoding='UTF-8'?><ns2:Fill_Out_Camera_Sheets xmlns:ns2='http://www.yawlfoundation.org/sb/cameraInfo' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.yawlfoundation.org/sb/cameraInfo cameraInfoType.xsd '><generalInfo><production>production</production><date>2001-01-01</date><weekday>weekday</weekday><shootDayNo>0</shootDayNo></generalInfo><producer>producer</producer><director>director</director><directorOfPhotography>directorOfPhotography</directorOfPhotography><cameraOperator>cameraOperator</cameraOperator><cameraAssistant>cameraAssistant</cameraAssistant><cameraInfo><sheetNumber>0</sheetNumber><camRoll>camRoll</camRoll><studios_location>studios_location</studios_location><techInfo><cameraType_and_Number>cameraType_and_Number</cameraType_and_Number><stockNumber>stockNumber</stockNumber><emulsion>emulsion</emulsion><totalCansNumber>0</totalCansNumber></techInfo><slateInfo><magNumber>magNumber</magNumber><slate>slate</slate><takeInfo><take>0</take><counter>0</counter><length>0</length><print>true</print><printSetting>printSetting</printSetting><essentialInfo>essentialInfo</essentialInfo></takeInfo></slateInfo><shortEndMin>0</shortEndMin><camInfoSum><footageLoaded>0</footageLoaded><totalExposed>0</totalExposed><shortEnds>0</shortEnds><waste>0</waste></camInfoSum><instructionsToLab>instructionsToLab</instructionsToLab><signatureOfCameraAssistant>signatureOfCameraAssistant</signatureOfCameraAssistant></cameraInfo><anotherRoll>true</anotherRoll><stockInfo><loaded>0</loaded><gross>0</gross><exposed>0</exposed><print>0</print><N_G>0</N_G><waste>0</waste><shortEnds>0</shortEnds></stockInfo><camRolls>camRolls</camRolls></ns2:Fill_Out_Camera_Sheets>";
		//xml = (String)session.getAttribute("outputData");
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
			  <td width="998" colspan='5'><table width='700' border='0' cellspacing='0' cellpadding='0'>
                <tr>
                  <td class="header-left">&nbsp;</td>
                  <td colspan="4" class="header-middle">Sheet Number </td>
                  <td class="header-right">&nbsp;</td>
                </tr>
                <tr height='30'>
                  <td class='left' width='15'>&nbsp;</td>
                  <td align="center"><strong>Sheet Number 
                  </strong></td>
                  <td align="center"><input name="sheet_number" type="text" id="sheet_number" value="<%=ci.getSheetNumber() %>" pattern="number"></td>
                  <td align="center"><strong>Camera Roll </strong></td>
                  <td align="center"><input name="roll" type="text" id="roll" size="20" value="<%=ci.getCamRoll()%>" pattern="text"></td>
                  <td class='right' width='15'>&nbsp;</td>
                </tr>
                <tr height='30'>
                  <td colspan='6' class='bottom'>&nbsp;</td>
                </tr>
              </table></td>
		  </tr>
			<tr><td colspan='5'>
				<table width='700' border='0' cellspacing='0' cellpadding='0'>
					<tr><td class="header-left">&nbsp;</td>
					  <td colspan='6' class="header-middle">General Info </td>
					  <td class="header-right">&nbsp;</td>
					</tr>
					<tr height='30'>
						<td class='left' width='15'>&nbsp;</td>
						<td><strong>Production</strong></td><td><input name='production' type='text' id='production' value='<%= gi.getProduction()%>' size="15" readonly></td>
						<td><strong>Date</strong></td><td><input name='date' type='text' id='date' value='<%= gi.getDate()%>' size="15" readonly></td>
						<td><strong>Day</strong></td><td><input name='weekday' type='text' id='weekday' value='<%=gi.getWeekday()%>' size="15" readonly></td>
						<td class='right' width='15'>&nbsp;</td>
					</tr>
					<tr height='30'>
						<td class='left' width='15'>&nbsp;</td>
						<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='<%=focs.getProducer()%>' size="15" readonly></td>
						<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='<%=focs.getDirector()%>' size="15" readonly></td>
						<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='<%=gi.getShootDayNo()%>' size="15" readonly></td>
						<td class='right' width='15'>&nbsp;</td>
					</tr>
					<tr height='30'>
					  <td class='left'>&nbsp;</td>
					  <td><strong>Director of Photography</strong></td>
					  <td><input name='director_photography' type='text' id='director_photography' value='<%= focs.getDirectorOfPhotography() %>' size="15" readonly></td>
					  <td><strong>Camera Operator</strong></td>
					  <td><input name='camera_operator' type='text' id='camera_operator' value='<%=focs.getCameraOperator()%>' size="15" readonly></td>
					  <td><strong>Camera Assistant</strong></td>
					  <td><input name='camera_assistant' type='text' id='camera_assistant' value='<%=focs.getCameraAssistant()%>' size="15" readonly></td>
					  <td class='right'>&nbsp;</td>
				  </tr>
					<tr height='30'>
						<td class='left' width='15'>&nbsp;</td>
						<td><strong>Studios/Locations</strong></td><td colspan="5"><input name="studios_locations" type="text" id="studios_locations" size="50" value="<%=ci.getStudiosLocation() %>" pattern="text"></td>
						<td class='right' width='15'>&nbsp;</td>
					</tr>
					<tr height='30'><td colspan='8' class='bottom'>&nbsp;</td>
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
					<td height="30"><input name="camera_type_number" type="text" id="camera_type_number" size="20" value="<%= ti.getCameraTypeAndNumber()%>" pattern="text"></td>
					<td height="30"><strong>Emulsion</strong></td>
					<td height="30"><input name="emulsion" type="text" id="emulsion" size="20" value="<%= ti.getEmulsion()%>"></td>
					<td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" align="right" class="left">&nbsp;</td>
				    <td height="30"><strong>Stock Number</strong></td>
				  	<td height="30"><input name="stock_number" type="text" id="stock_number" size="20" value="<%= ti.getStockNumber()%>" pattern="text"></td>
					<td height="30"><strong>Total Cans Number</strong></td>
					<td height="30"><input name="total_cans_number" type="text" id="total_cans_number" size="20" value="<%= ti.getTotalCansNumber()%>" pattern="number"></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<%}else{%>
				<tr>
					<td width="15" align="right" class="left">&nbsp;</td>
					<td height="30"><strong>Camera Type and Number</strong></td>
					<td height="30"><input name="camera_type_number" type="text" id="camera_type_number" size="20" pattern="text"></td>
					<td height="30"><strong>Emulsion</strong></td>
					<td height="30"><input name="emulsion" type="text" id="emulsion" size="20" pattern="text"></td>
					<td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" align="right" class="left">&nbsp;</td>
				    <td height="30"><strong>Stock Number</strong></td>
				  	<td height="30"><input name="stock_number" type="text" id="stock_number" size="20" pattern="text"></td>
					<td height="30"><strong>Total Cans Number</strong></td>
					<td height="30"><input name="total_cans_number" type="text" id="total_cans_number" size="20" pattern="number"></td>
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
			<table width="700" border="0" cellpadding="0" cellspacing="0" bordercolor="#000000" id="table1">
				<tbody>
					<tr align="center" valign="top">
					    <td class="header-left">&nbsp;</td>
			            <td colspan="9" align="left" class="header-middle">Camera Roll Info </td>
			            <td class="header-right">&nbsp;</td>
					</tr>
					<tr align="center" valign="top">
					  <td rowspan="2" class="left">&nbsp;</td>
						<td rowspan="2"><strong>Mag No.</strong></td>
						<td rowspan="2"><strong>Slate No.</strong></td>
						<td rowspan="2"><strong>Take No.</strong></td>
						<td rowspan="2"><strong>Counter Reading</strong></td>
						<td rowspan="2"><strong>Take<br> 
					    Length</strong></td>
						<td rowspan="2"><strong>Print</strong></td>
						<td colspan="2"><strong>Print Setting</strong></td>
						<td><strong>Essential Information/ General Notes</strong></td>
					    <td rowspan="2" class="right">&nbsp;</td>
					</tr>
					<tr valign="top">
						<th><strong>B/W</strong></th>
						<td align="center"><strong>Colour</strong></td>
						<td align="center"><em>Colour description of scene, filter and/or diffusion used, Day, night or other effects.</em></td>
					</tr>
				  
				   <%
				   	int a=0;
						if(ci.getSlateInfo() != null) {
						for(SlateInfoType sl : ci.getSlateInfo()){
							for(TakeInfoType ti : sl.getTakeInfo()){
								a++;
					%>
					<tr align="center" valign="top">
						<td class="left">&nbsp;</td>
						<td><input name="mag_number_<%=a%>" type="text" id="mag_number_<%=a%>" size="5" value="<%= sl.getMagNumber() %>" pattern="text"></td>
						<td><input name="slate_<%=a%>" type="text" id="slate_<%=a%>" size="5" value="<%= sl.getSlate() %>" pattern="text"></td>
						<td><input name="take_<%=a%>" type="text" id="take_<%=a%>" size="5" value="<%= ti.getTake() %>" pattern="number"></td>
						<td><input name="counter_reading_<%=a%>" type="text" id="counter_reading_<%=a%>" size="6" value="<%= ti.getCounter() %>" pattern="number"></td>
						<td><input name="take_length_<%=a%>" type="text" id="take_length_<%=a%>" size="6" value="<%= ti.getLength() %>" pattern="number"></td>
						<td><input name="print_<%=a%>" type="checkbox" id="print_<%=a%>" value="True" <% if(ti.isPrint() == true) {out.print("checked");}%>></td>
						<td><input name="print_setting_<%=a%>" id="radio" type="radio" value="B/W" <% if(ti.getPrintSetting().equals("B/W")){out.print("checked");}%>></td>
						<td><input name="print_setting_<%=a%>" id="radio" type="radio" value="Colour" <% if(ti.getPrintSetting().equals("Colour")){out.print("checked");}%>></td>
					    <td><textarea name="notes_<%=a%>" cols="30" id="notes_<%=a%>" pattern="any_text"><%= ti.getEssentialInfo() %></textarea></td>
					    <td align="right" class="right">&nbsp;</td>
					</tr>
					<% 
							}
						}
					}else {
					%>
					<tr align="center" valign="top">
						<td class="left">&nbsp;</td>
						<td><input name="mag_number_1" type="text" id="mag_number_1" size="5" pattern="text"></td>
						<td><input name="slate_1" type="text" id="slate_1" size="5" pattern="text"></td>
						<td><input name="take_1" type="text" id="take_1" size="5" pattern="number"></td>
						<td><input name="counter_reading_1" type="text" id="counter_reading_1" size="6" pattern="number"></td>
						<td><input name="take_length_1" type="text" id="take_length_1" size="6" pattern="number"></td>
						<td><input name="print_1" type="checkbox" id="print_1" value="True"></td>
						<td><input name="print_setting_1" id="radio" type="radio" value="B/W"></td>
						<td><input name="print_setting_1" id="radio" type="radio" value="Colour"></td>
					    <td><textarea name="notes_1" cols="30" id="textarea" pattern="any_text"></textarea></td>
					    <td align="right" class="right">&nbsp;</td>
					</tr>
					<%}%>
				</tbody>
	            <tbody>
	                <tr valign="top">
	                    <th class="bottom" colspan="10"> </th>
					</tr>
				</tbody>
	        </table>
			<table width='700' border='0' cellpadding='10' cellspacing='0'>
				</tbody>
					<tr>
						<td width="1%"/>
						<td>
							<input name="button" type="button" onClick="addRow();" value="Insert Row"/>
							<input name="button_delete" type="button" onClick="deleteRow();" value="Delete Row"/></td>
						<td class="right">&nbsp;</td>
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
          <td height="30"><input name="short_end_minimum" type="text" id="short_end_minimum" value="<%=ci.getShortEndMin() %>" pattern="number"></td>
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
				  <td class="header-left">&nbsp;</td>
		          <td colspan="7" class="header-middle">Camera Info Summary </td>
		          <td class="header-right">&nbsp;</td>
				</tr>
				<% if(ci.getCamInfoSum() != null) { 
				CamInfoSumType cis = ci.getCamInfoSum();%>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="100" height="30"><strong>Footage(Loaded)</strong></td>
				  	<td height="30"><input name="footage_loaded" type="text" id="footage_loaded" size="5" value="<%=cis.getFootageLoaded() %>" pattern="number"></td>
					<td width="100" height="30"><strong>Total Exposed</strong></td>
					<td height="30"><input name="total_exposed_2" type="text" id="total_exposed_2" size="5" readonly></td>
					<td width="100" height="30"><strong>Total Ftge Prev Drawn</strong></td>
					<td height="30"><input name="total_footage_prev_drawn" type="text" id="total_footage_prev_drawn" size="5" readonly></td>
					<td height="30"><strong>Instructions to Laboratory</strong></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="100" height="30"><strong>Total Exposed </strong></td>
					<td height="30"><input name="total_exposed" type="text" id="total_exposed" size="5" value="<%=cis.getTotalExposed() %>" pattern="number"></td>
					<td width="100" height="30"><strong>Total Developed</strong></td>
					<td height="30"><input name="total_developed" type="text" id="total_developed" size="5" readonly></td>
					<td width="100" height="30"><strong>Footage Drn Today</strong></td>
					<td height="30"><input name="footage_drawn_today" type="text" id="footage_drawn_today" size="5" readonly></td>
					<td height="30"><textarea name="instructions" cols="20" id="instructions" pattern="any_text"><%=ci.getInstructionsToLab() %></textarea></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="100" height="30"><strong>Short Ends</strong></td>
				  	<td height="30"><input name="short_ends" type="text" id="short_ends" size="5" value="<%=cis.getShortEnds() %>" pattern="number"></td>
					<td width="100" height="30"><strong>Total Printed</strong></td>
					<td height="30"><input name="total_printed" type="text" id="total_printed" size="5" readonly></td>
					<td width="100" height="30"><strong>Previously Exposed</strong></td>
					<td height="30"><input name="previously_exposed" type="text" id="previously_exposed" size="5" readonly></td>
					<td height="30"><em>Signed by Camera Assistant </em></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="100" height="30"><strong>Waste</strong></td>
					<td height="30"><input name="waste" type="text" id="waste" size="5" value="<%=cis.getWaste() %>" pattern="number"></td>
					<td width="100" height="30"><strong>Held or Not Sent</strong></td>
					<td height="30"><input name="held_notsent" type="text" id="held_notsent" size="5" readonly></td>
					<td width="100" height="30"><strong>Exposed Today</strong></td>
					<td height="30"><input name="exposed_today" type="text" id="exposed_today" size="5" readonly></td>
					<td height="30"><input name="assistant_signature" type="text" id="assistant_signature" value="<%= ci.getSignatureOfCameraAssistant() %>" size="20"></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<%}else{%>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="100" height="30"><strong>Footage(Loaded)</strong></td>
				  	<td height="30"><input name="footage_loaded" type="text" id="footage_loaded" size="5" pattern="number"></td>
					<td width="100" height="30"><strong>Total Exposed</strong></td>
					<td height="30"><input name="total_exposed_2" type="text" id="total_exposed_2" size="5" readonly></td>
					<td width="100" height="30"><strong>Total Ftge Prev Drawn</strong></td>
					<td height="30"><input name="total_footage_prev_drawn" type="text" id="total_footage_prev_drawn" size="5" readonly></td>
					<td height="30"><strong>Instructions to Laboratory</strong></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="100" height="30"><strong>Total Exposed </strong></td>
					<td height="30"><input name="total_exposed" type="text" id="total_exposed" size="5" pattern="number"></td>
					<td width="100" height="30"><strong>Total Developed</strong></td>
					<td height="30"><input name="total_developed" type="text" id="total_developed" size="5" readonly></td>
					<td width="100" height="30"><strong>Footage Drn Today</strong></td>
					<td height="30"><input name="footage_drawn_today" type="text" id="footage_drawn_today" size="5" readonly></td>
					<td height="30"><textarea name="instructions" cols="20" id="instructions"><%=ci.getInstructionsToLab() %></textarea></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="100" height="30"><strong>Short Ends</strong></td>
				  	<td height="30"><input name="short_ends" type="text" id="short_ends" size="5" pattern="number"></td>
					<td width="100" height="30"><strong>Total Printed</strong></td>
					<td height="30"><input name="total_printed" type="text" id="total_printed" size="5" readonly></td>
					<td width="100" height="30"><strong>Previously Exposed</strong></td>
					<td height="30"><input name="previously_exposed" type="text" id="previously_exposed" size="5" readonly></td>
					<td height="30"><em>Signed by Camera Assistant </em></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="left">&nbsp;</td>
					<td width="100" height="30"><strong>Waste</strong></td>
					<td height="30"><input name="waste" type="text" id="waste" size="5" pattern="number"></td>
					<td width="100" height="30"><strong>Held or Not Sent</strong></td>
					<td height="30"><input name="held_notsent" type="text" id="held_notsent" size="5" readonly></td>
					<td width="100" height="30"><strong>Exposed Today</strong></td>
					<td height="30"><input name="exposed_today" type="text" id="exposed_today" size="5" readonly></td>
					<td height="30"><input name="assistant_signature" type="text" id="assistant_signature" value="<%= ci.getSignatureOfCameraAssistant() %>" size="20"></td>
				    <td width="15" class="right">&nbsp;</td>
				</tr>
				<%}%>
				<tr>
				  <td class="left">&nbsp;</td>
		          <td colspan="7"><input name="button2" type="button" onClick="calculate();" value="Calculate"/></td>
		          <td class="right">&nbsp;</td>
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