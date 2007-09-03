<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.math.BigInteger" %>
<%@ page import="javax.xml.bind.JAXBElement" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="javax.xml.bind.Unmarshaller" %>
<%@ page import="org.yawlfoundation.sb.camerainfo.*"%>
<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Camera Sheet</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript">
var count = 1;
function addRow()
{
var tbody = document.getElementById("table1").getElementsByTagName("tbody")[0];
var row = document.createElement("TR");
var cell1 = document.createElement("TD");
var cell2 = document.createElement("TD");
var cell3 = document.createElement("TD");
var cell4 = document.createElement("TD");
var cell5 = document.createElement("TD");
var cell6 = document.createElement("TD");
var cell7 = document.createElement("TD");
var cell8 = document.createElement("TD");
var cell9 = document.createElement("TD");
var cell10 = document.createElement("TD");
var inp1 =  document.createElement("INPUT");
var inp2 =  document.createElement("INPUT");
var inp3 =  document.createElement("INPUT");
var inp4 =  document.createElement("INPUT");
var inp5 =  document.createElement("INPUT");
var inp6 =  document.createElement("INPUT");
var inp7 =  document.createElement("INPUT");
var inp8 =  document.createElement("TEXTAREA");

count ++;
var current_slate = "slate_" + count;
var current_take = "take_" + count;
var current_counter_reading = "counter_reading_" + count;
var current_take_length = "take_length_" + count;
var current_print = "print_" + count;
var current_print_setting = "print_setting_" + count;
var current_notes = "notes_" + count;

row.setAttribute("valign", "top");
row.setAttribute("align", "center");

cell1.setAttribute("class", "leftbox");
cell10.setAttribute("class", "rightbox");

inp1.setAttribute("size","5");
inp1.setAttribute("name", current_slate);
inp1.setAttribute("id", current_slate);

inp2.setAttribute("size","5");
inp2.setAttribute("name", current_take);
inp2.setAttribute("id", current_take);

inp3.setAttribute("size","8");
inp3.setAttribute("name", current_counter_reading);
inp3.setAttribute("id", current_counter_reading);

inp4.setAttribute("size","8");
inp4.setAttribute("name", current_take_length);
inp4.setAttribute("id", current_take_length);

inp5.setAttribute("type", "checkbox");
inp5.setAttribute("value", "True");
inp5.setAttribute("name", current_print);
inp5.setAttribute("id", current_print);

inp6.setAttribute("type", "radio");
inp6.setAttribute("value", "B/W");
inp6.setAttribute("name", current_print_setting);
inp6.setAttribute("id", current_print_setting);

inp7.setAttribute("type", "radio");
inp7.setAttribute("value", "Colour");
inp7.setAttribute("name", current_print_setting);
inp7.setAttribute("id", current_print_setting);

inp8.setAttribute("cols", "50");
inp8.setAttribute("name", current_notes);
inp8.setAttribute("id", current_notes);


cell2.appendChild(inp1);
cell3.appendChild(inp2);
cell4.appendChild(inp3);
cell5.appendChild(inp4);
cell6.appendChild(inp5);
cell7.appendChild(inp6);
cell8.appendChild(inp7);
cell9.appendChild(inp8);


row.appendChild(cell1);
row.appendChild(cell2);
row.appendChild(cell3);
row.appendChild(cell4);
row.appendChild(cell5);
row.appendChild(cell6);
row.appendChild(cell7);
row.appendChild(cell8);
row.appendChild(cell9);
row.appendChild(cell10);
tbody.appendChild(row);
//alert(row.innerHTML);
}

function getCount (form) {
	document.getElementById("count").value = count;
return true;
}

function getParam(name)
{
  var start=location.search.indexOf("?"+name+"=");
  if (start<0) start=location.search.indexOf("&"+name+"=");
  if (start<0) return '';
  start += name.length+2;
  var end=location.search.indexOf("&",start)-1;
  if (end<0) end=location.search.length;
  var result='';
  for(var i=start;i<=end;i++) {
    var c=location.search.charAt(i);
    result=result+(c=='+'?' ':c);
  }
  //window.alert('Result = '+result);
  return unescape(result);
}

function getParameters(){
	document.form1.workItemID.value = getParam('workItemID');
	document.form1.userID.value = getParam('userID');
	document.form1.sessionHandle.value = getParam('sessionHandle');
	document.form1.submit.value = "htmlForm";
}

function calculate(){
	var t_exposed = 0;
	var s_ends = null;
	for (i=1; i<=count; i++){
		t_exposed = t_exposed + parseInt(document.getElementById("take_length_"+i).value);
	}
	document.form1.total_exposed.value=t_exposed;
	document.form1.short_ends.value = parseInt(document.form1.footage_loaded.value) - t_exposed;
	s_ends = parseInt(document.form1.short_ends.value);
	if (s_ends < 80)
		document.form1.waste.value = s_ends;
	else
		document.form1.waste.value = 0;
}


</script>
<link href="porchlight.css" rel="stylesheet" type="text/css">
</head>

<body onLoad="getParameters()">
<table width="1100" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td colspan="3" class="background_top">&nbsp;</td></tr>
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td><h1 align="center"><img src="graphics/logo.jpg" width="58" height="57"></h1>
    <h1 align="center">Picture Negative Camera Sheet</h1>
		<form name="form1" method="post" onSubmit="return getCount(this)">
  		<table width="900"  border="0" align="center">
<% 
				//String xml = request.getParameter("outputData");
				String xml = (String)session.getAttribute("outputData");
				xml = xml.replaceAll("<Fill_Out_Camera_Sheets", "<ns2:Fill_Out_Camera_Sheets xmlns:ns2='http://www.yawlfoundation.org/sb/cameraInfo'");
				xml = xml.replaceAll("</Fill_Out_Camera_Sheets","</ns2:Fill_Out_Camera_Sheets");
				
				ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
				JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.camerainfo");
				Unmarshaller u = jc.createUnmarshaller();
				JAXBElement focsElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
				FillOutCameraSheetsType focs = (FillOutCameraSheetsType)focsElement.getValue();
				
				GeneralInfoType gi = focs.getGeneralInfo();
		%>
			<tr><td colspan='5'>
				<table width='900' border='0' cellspacing='0' cellpadding='0'>
					<tr><td colspan='8'><img src='graphics/testing/box_top.jpg' width='902' height='10'></td></tr>
					<tr height='30'>
						<td class='leftbox' width='15'></td>
						<td><strong>Production</strong></td><td><input name='production' type='text' id='production' value='<%=gi.getProduction()%>' readonly></td>
						<td><strong>Date</strong></td><td><input name='date' type='text' id='date' value='<%=gi.getDate().getDay()+"-"+gi.getDate().getMonth()+"-"+gi.getDate().getYear()%>' readonly></td>
						<td><strong>Day</strong></td><td><input name='weekday' type='text' id='weekday' value='<%=gi.getWeekday()%>' readonly></td>
						<td class='rightbox' width='15'></td>
					</tr>
					<tr height='30'>
						<td class='leftbox' width='15'></td>
						<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='<%=focs.getProducer()%>' readonly></td>
						<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='<%=focs.getDirector()%>' readonly></td>
						<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='<%=gi.getShootDayNo()%>' readonly></td>
						<td class='rightbox' width='15'></td>
					</tr>
					<tr height='30'>
						<td class='leftbox' width='15'></td>
						<td><strong>Director of Photography</strong></td><td><input name='director_photography' type='text' id='director_photography' value='<%= focs.getDirectorOfPhotography() %>' readonly></td>
						<td><strong>Camera Operator</strong></td><td><input name='camera_operator' type='text' id='camera_operator' value='<%=focs.getCameraOperator()%>' readonly></td>
						<td><strong>Camera Assistant</strong></td><td><input name='camera_assistant' type='text' id='camera_assistant' value='<%=focs.getCameraAssistant()%>' readonly></td>
						<td class='rightbox' width='15'></td>
					</tr>
					<tr height='30'><td colspan='8' class='bottombox'>&nbsp;</td></tr>
				</table>
			</td></tr>
		<%
				if(request.getParameter("Submission") != null){
					
					int count = Integer.parseInt(request.getParameter("count"));

					TechInfoType thi =  new TechInfoType();
					thi.setCameraTypeAndNumber(request.getParameter("camera_type_number"));
					thi.setStockNumber(request.getParameter("stock_number"));
					thi.setEmulsion(request.getParameter("emulsion"));
					thi.setCamRoll(request.getParameter("roll"));
					thi.setTotalCansNumber(new BigInteger(request.getParameter("total_cans_number")));
					thi.setMagNumber(request.getParameter("mag_number"));

					CamInfoSumType1 cam1 = new CamInfoSumType1();
					cam1.setFootageLoaded(new BigInteger(request.getParameter("footage_loaded")));
					cam1.setTotalExposed(new BigInteger(request.getParameter("total_exposed")));
					cam1.setShortEnds(new BigInteger(request.getParameter("short_ends")));
					cam1.setWaste(new BigInteger(request.getParameter("waste")));
					
					CamInfoSumType2 cam2 = new CamInfoSumType2();
					cam2.setTotalExposed(new BigInteger(request.getParameter("total_exposed_2")));
					cam2.setTotalDeveloped(new BigInteger(request.getParameter("total_developed")));
					cam2.setTotalPrinted(new BigInteger(request.getParameter("total_printed")));
					cam2.setHeldOrNotSent(new BigInteger(request.getParameter("held_notsent")));
					
					CamInfoSumType3 cam3 = new CamInfoSumType3();
					cam3.setTotalFTGEPrevDrawn(new BigInteger(request.getParameter("total_footage_prev_drawn")));
					cam3.setFootageDRNToday(new BigInteger(request.getParameter("footage_drawn_today")));
					cam3.setPreviouslyExposed(new BigInteger(request.getParameter("previously_exposed")));
					cam3.setExposedToday(new BigInteger(request.getParameter("exposed_today")));
					
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
							si.setSlate(new BigInteger(tempSlateNO));
							si.getTakeInfo().add(ti);
							slates.put(tempSlateNO, si);//add the newly created slate into the "slates" map
						}
						else{//the slateNO already exists
							tempSlate.getTakeInfo().add(ti);
						}
					}
					
					List<SlateInfoType> sl = new ArrayList<SlateInfoType>(slates.values());//creates a list of the scenes and adds it to the cameraInfo facade
					
					CameraInfoType ci = new CameraInfoType();
					ci.setSheetNumber(new BigInteger(request.getParameter("sheet_number")));
					ci.setProjectNumber(request.getParameter("project_number"));
					ci.setStudiosLocation(request.getParameter("studios_locations"));
					ci.setTechInfo(thi);
					ci.getSlateInfo().addAll(sl);
					ci.setCamInfoSum1(cam1);
					ci.setCamInfoSum2(cam2);
					ci.setCamInfoSum3(cam3);
					ci.setInstructionsToLab(request.getParameter("instructions"));
					ci.setSignatureOfCameraAssistant(request.getParameter("assistant_signature"));
					
					focs.setCameraInfo(ci);
					
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

					TechInfoType thi =  new TechInfoType();
					thi.setCameraTypeAndNumber(request.getParameter("camera_type_number"));
					thi.setStockNumber(request.getParameter("stock_number"));
					thi.setEmulsion(request.getParameter("emulsion"));
					thi.setCamRoll(request.getParameter("roll"));
					thi.setTotalCansNumber(new BigInteger(request.getParameter("total_cans_number")));
					thi.setMagNumber(request.getParameter("mag_number"));

					CamInfoSumType1 cam1 = new CamInfoSumType1();
					cam1.setFootageLoaded(new BigInteger(request.getParameter("footage_loaded")));
					cam1.setTotalExposed(new BigInteger(request.getParameter("total_exposed")));
					cam1.setShortEnds(new BigInteger(request.getParameter("short_ends")));
					cam1.setWaste(new BigInteger(request.getParameter("waste")));
					
					CamInfoSumType2 cam2 = new CamInfoSumType2();
					cam2.setTotalExposed(new BigInteger(request.getParameter("total_exposed_2")));
					cam2.setTotalDeveloped(new BigInteger(request.getParameter("total_developed")));
					cam2.setTotalPrinted(new BigInteger(request.getParameter("total_printed")));
					cam2.setHeldOrNotSent(new BigInteger(request.getParameter("held_notsent")));
					
					CamInfoSumType3 cam3 = new CamInfoSumType3();
					cam3.setTotalFTGEPrevDrawn(new BigInteger(request.getParameter("total_footage_prev_drawn")));
					cam3.setFootageDRNToday(new BigInteger(request.getParameter("footage_drawn_today")));
					cam3.setPreviouslyExposed(new BigInteger(request.getParameter("previously_exposed")));
					cam3.setExposedToday(new BigInteger(request.getParameter("exposed_today")));
					
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
							si.setSlate(new BigInteger(tempSlateNO));
							si.getTakeInfo().add(ti);
							slates.put(tempSlateNO, si);//add the newly created slate into the "slates" map
						}
						else{//the slateNO already exists
							tempSlate.getTakeInfo().add(ti);
						}
					}
					
					List<SlateInfoType> sl = new ArrayList<SlateInfoType>(slates.values());//creates a list of the scenes and adds it to the cameraInfo facade
					
					CameraInfoType ci = new CameraInfoType();
					ci.setSheetNumber(new BigInteger(request.getParameter("sheet_number")));
					ci.setProjectNumber(request.getParameter("project_number"));
					ci.setStudiosLocation(request.getParameter("studios_locations"));
					ci.setTechInfo(thi);
					ci.getSlateInfo().addAll(sl);
					ci.setCamInfoSum1(cam1);
					ci.setCamInfoSum2(cam2);
					ci.setCamInfoSum3(cam3);
					ci.setInstructionsToLab(request.getParameter("instructions"));
					ci.setSignatureOfCameraAssistant(request.getParameter("assistant_signature"));
					
					focs.setCameraInfo(ci);					
					
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
		<tr><td>
			<table width="900" border="0" align="center" cellpadding="0" cellspacing="0">
				<tr><td colspan="6"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td></tr>
				<tr>
				  	<td width="15" class="leftbox">&nbsp;</td>
					<td height="30"><strong>Sheet Number  </strong></td>
					<td height="30"><input name="sheet_number" type="text" id="sheet_number"></td>
					<td height="30">&nbsp;</td>
					<td height="30">&nbsp;</td>
					<td width="15" class="rightbox">&nbsp;</td>
				</tr>
				<tr>
				  	<td width="15" class="leftbox">&nbsp;</td>
				  	<td height="30">&nbsp;</td>
				  	<td height="30">&nbsp;</td>
			      	<td height="30">&nbsp;</td>
			      	<td height="30">&nbsp;</td>
			      	<td width="15" class="rightbox">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" align="right" class="leftbox">&nbsp;</td>
					<td height="30"><strong>Project Number</strong></td>
					<td height="30"><input name="project_number" type="text" id="project_number"></td>
					<td height="30"><strong>Emulsion</strong></td>
					<td height="30"><input name="emulsion" type="text" id="emulsion"></td>
					<td width="15" class="rightbox">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" align="right" class="leftbox">&nbsp;</td>
				    <td height="30"><strong>Studios/Locations</strong></td>
				  	<td height="30"><input name="studios_locations" type="text" id="studios_locations"></td>
					<td height="30"><strong>Roll Number</strong></td>
					<td height="30"><input name="roll" type="text" id="roll"></td>
				    <td width="15" class="rightbox">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" align="right" class="leftbox">&nbsp;</td>
			  	    <td height="30"><strong>Camera Type and Number</strong></td>
				  	<td height="30"><input name="camera_type_number" type="text" id="camera_type_number"></td>
					<td height="30"><strong>Total Cans Number</strong></td>
					<td height="30"><input name="total_cans_number" type="text" id="total_cans_number"></td>
				    <td width="15" class="rightbox">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" align="right" class="leftbox">&nbsp;</td>
			  	    <td height="30"><strong>Stock Number</strong></td>
				  	<td height="30"><input name="stock_number" type="text" id="stock_number"></td>
					<td height="30"><strong>Mag Number(s)</strong></td>
					<td height="30"><input name="mag_number" type="text" id="mag_number"></td>
				    <td width="15" class="rightbox">&nbsp;</td>
				</tr>
				<tr>
				  <td colspan="6" class="bottombox">&nbsp;</td>
			    </tr>
		  </table>
		</td>
	</tr>
	
	<tr><td>&nbsp;</td></tr>
	
	<tr>
		<td>
			<table border="0" cellpadding="0" cellspacing="0" bordercolor="#000000" id="table1">
				<tbody>
					<tr align="center" valign="top">
					    <td colspan="10"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td>
			      </tr>
					<tr align="center" valign="top">
					  <td width="15" rowspan="2" class="leftbox">&nbsp;</td>
						<td rowspan="2"><strong>Slate No.</strong></td>
						<td rowspan="2"><strong>Take No.</strong></td>
						<td rowspan="2"><strong>Counter Reading</strong></td>
						<td rowspan="2"><strong>Take<br> 
					    Length</strong></td>
						<td rowspan="2"><strong>Print</strong></td>
						<td colspan="2"><strong>Print Setting</strong></td>
						<td><strong>Essential Information/ General Notes</strong></td>
					    <td width="15" rowspan="2" class="rightbox">&nbsp;</td>
					</tr>
					<tr valign="top">
					  <th><strong>B/W</strong></th>
				      <td align="center"><strong>Colour</strong></td>
				      <td align="center"><em>Colour description of scene, filter and/or diffusion used, Day, night or other effects.</em></td>
			      </tr>
					<tr align="center" valign="top">
						<td width="15" class="leftbox">&nbsp;</td>
						<td><input name="slate_1" type="text" id="slate_1" size="5"></td>
						<td><input name="take_1" type="text" id="take_1" size="5"></td>
						<td><input name="counter_reading_1" type="text" id="counter_reading_1" size="8"></td>
						<td><input name="take_length_1" type="text" id="take_length_1" size="8"></td>
						<td><input name="print_1" type="checkbox" id="print_1" value="True"></td>
						<td><input name="print_setting_1" id="radio" type="radio" value="B/W"></td>
						<td><input name="print_setting_1" id="radio" type="radio" value="Colour"></td>
					    <td><textarea name="notes_1" cols="50" id="textarea"></textarea></td>
					    <td width="15" align="right" class="rightbox">&nbsp;</td>
					</tr>
				</tbody>
				<tr align="center" valign="top">
					<td colspan="10" class="bottombox">&nbsp;</td>
			  </tr>
			</table>
		</td>
	</tr>
	
	<tr><td>
		<input type="button" value="Insert Row" onClick="addRow();"/>
	  	<input type="button" value="Calculate" onClick="calculate();"/>
	</td></tr>
	
	<tr><td>&nbsp;</td></tr>
	
	<tr>
		<td>
			<table width="900" border="0" cellpadding="0" cellspacing="0">
				<tr>
				  <td colspan="9"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td>
		      </tr>
				<tr>
					<td width="15" class="leftbox">&nbsp;</td>
					<td height="30"><strong>Footage(Loaded)</strong></td>
				  	<td height="30"><input name="footage_loaded" type="text" id="footage_loaded" value="400" size="10"></td>
					<td height="30"><strong>Total Exposed</strong></td>
					<td height="30"><input name="total_exposed_2" type="text" id="total_exposed_2" value="0" size="10"></td>
					<td height="30"><strong>Total Ftge Prev Drawn</strong></td>
					<td height="30"><input name="total_footage_prev_drawn" type="text" id="total_footage_prev_drawn" value="0" size="10"></td>
					<td height="30"><strong>Instructions to Laboratory</strong></td>
				    <td width="15" class="rightbox">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="leftbox">&nbsp;</td>
					<td height="30"><strong>Total Exposed </strong></td>
					<td height="30"><input name="total_exposed" type="text" id="total_exposed" size="10"></td>
					<td height="30"><strong>Total Developed</strong></td>
					<td height="30"><input name="total_developed" type="text" id="total_developed" value="0" size="10"></td>
					<td height="30"><strong>Footage Drn Today</strong></td>
					<td height="30"><input name="footage_drawn_today" type="text" id="footage_drawn_today" value="0" size="10"></td>
					<td height="30"><textarea name="instructions" id="instructions"></textarea></td>
				    <td width="15" class="rightbox">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="leftbox">&nbsp;</td>
					<td height="30"><strong>Short Ends</strong></td>
				  	<td height="30"><input name="short_ends" type="text" id="short_ends" size="10"></td>
					<td height="30"><strong>Total Printed</strong></td>
					<td height="30"><input name="total_printed" type="text" id="total_printed" value="0" size="10"></td>
					<td height="30"><strong>Previously Exposed</strong></td>
					<td height="30"><input name="previously_exposed" type="text" id="previously_exposed" value="0" size="10"></td>
					<td height="30"><em>Signed by Camera Assistant </em></td>
				    <td width="15" class="rightbox">&nbsp;</td>
				</tr>
				<tr>
					<td width="15" class="leftbox">&nbsp;</td>
					<td height="30"><strong>Waste</strong></td>
					<td height="30"><input name="waste" type="text" id="waste" size="10"></td>
					<td height="30"><strong>Held or Not Sent</strong></td>
					<td height="30"><input name="held_notsent" type="text" id="held_notsent" value="0" size="10"></td>
					<td height="30"><strong>Exposed Today</strong></td>
					<td height="30"><input name="exposed_today" type="text" id="exposed_today" value="0" size="10"></td>
					<td height="30"><input name="assistant_signature" type="text" id="assistant_signature"></td>
				    <td width="15" class="rightbox">&nbsp;</td>
				</tr>
				<tr>
				  <td colspan="9" class="bottombox">&nbsp;</td>
		      </tr>
			</table>
		</td>
	</tr>
	
   	<tr><td>
		<input type="hidden" name="count" id="count" value="1"/>
		<input type="hidden" name="workItemID" id="workItemID"/>
		<input type="hidden" name="userID" id="userID"/>
		<input type="hidden" name="sessionHandle" id="sessionHandle"/>
		<input type="hidden" name="submit" id="submit"/>
	</td></tr>
  </table>
  <p align="center">
  <input type="button" value="Print"  onclick="window.print()"/>
  <input type="submit" name="Save" value="Save"/>
  <input type="submit" name="Submission" value="Submission"/></p>
</form></td>
    <td width="14" class="background_right">&nbsp;</td>
  </tr>
  <tr>
    <td colspan="3" class="background_bottom">&nbsp;</td>
  </tr>
</table>
<p align="center">&nbsp;</p>

</body>
</html>