<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.math.BigInteger" %>
<%@ page import="com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl" %>
<%@ page import="javax.xml.bind.JAXBElement" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="javax.xml.bind.Unmarshaller" %>
<%@ page import="org.yawlfoundation.sb.timesheetinfo.*"%>
<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Assistant Director Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript">
var artist_count = 1;
var child_count = 0;
var crew_count = 1;

//function for adding artist information
function addArtistRow()
{
var tbody = document.getElementById("artist").getElementsByTagName("tbody")[0];
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
var inp8 =  document.createElement("INPUT");

artist_count ++;
var current_artist = "artist_" + artist_count;
var current_pu = "artist_pu_" + artist_count;
var current_muwdcall_scheduled = "artist_muwdcall_scheduled_" + artist_count;
var current_muwdcall_actual = "artist_muwdcall_actual_" + artist_count;
var current_meal = "artist_meal_" + artist_count;
var current_wrap = "artist_wrap_" + artist_count;
var current_travel = "artist_travel_" + artist_count;
var current_signature = "artist_signature_" + artist_count;

row.setAttribute("valign", "top");
row.setAttribute("align", "center");
row.setAttribute("height", "30");

cell1.setAttribute("class", "leftbox");
cell10.setAttribute("class", "rightbox");

inp1.setAttribute("name", current_artist);
inp1.setAttribute("id", current_artist);

inp2.setAttribute("size","15");
inp2.setAttribute("name", current_pu);
inp2.setAttribute("id", current_pu);

inp3.setAttribute("size","8");
inp3.setAttribute("name", current_muwdcall_scheduled);
inp3.setAttribute("id", current_muwdcall_scheduled);

inp4.setAttribute("size","8");
inp4.setAttribute("name", current_muwdcall_actual);
inp4.setAttribute("id", current_muwdcall_actual);

inp5.setAttribute("size","8");
inp5.setAttribute("name", current_meal);
inp5.setAttribute("id", current_meal);

inp6.setAttribute("size","8");
inp6.setAttribute("name", current_wrap);
inp6.setAttribute("id", current_wrap);

inp7.setAttribute("size","8");
inp7.setAttribute("name", current_travel);
inp7.setAttribute("id", current_travel);

inp8.setAttribute("name", current_signature);
inp8.setAttribute("id", current_signature);

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

//function for adding child information
function addChildRow(){
var tbody = document.getElementById("child").getElementsByTagName("tbody")[0];
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

child_count ++;
var current_child = "children_" + child_count;
var current_pu = "children_pu_" + child_count;
var current_muwdcall_scheduled = "children_muwdcall_scheduled_" + child_count;
var current_muwdcall_actual = "children_muwdcall_actual_" + child_count;
var current_meal = "children_meal_" + child_count;
var current_wrap = "children_wrap_" + child_count;
var current_travel = "children_travel_" + child_count;
var current_remarks = "children_remarks_" + child_count;

row.setAttribute("valign", "top");
row.setAttribute("align", "center");

cell1.setAttribute("class", "leftbox");
cell10.setAttribute("class", "rightbox");

inp1.setAttribute("name", current_child);
inp1.setAttribute("id", current_child);

inp2.setAttribute("size","15");
inp2.setAttribute("name", current_pu);
inp2.setAttribute("id", current_pu);

inp3.setAttribute("size","8");
inp3.setAttribute("name", current_muwdcall_scheduled);
inp3.setAttribute("id", current_muwdcall_scheduled);

inp4.setAttribute("size","8");
inp4.setAttribute("name", current_muwdcall_actual);
inp4.setAttribute("id", current_muwdcall_actual);

inp5.setAttribute("size","8");
inp5.setAttribute("name", current_meal);
inp5.setAttribute("id", current_meal);

inp6.setAttribute("size","8");
inp6.setAttribute("name", current_wrap);
inp6.setAttribute("id", current_wrap);

inp7.setAttribute("size","8");
inp7.setAttribute("name", current_travel);
inp7.setAttribute("id", current_travel);

inp8.setAttribute("name", current_remarks);
inp8.setAttribute("id", current_remarks);

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

function addCrewRow(){
var tbody = document.getElementById("crew").getElementsByTagName("tbody")[0];
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
var inp1 =  document.createElement("SELECT");
var inp2 =  document.createElement("INPUT");
var inp3 =  document.createElement("INPUT");
var inp4 =  document.createElement("INPUT");
var inp5 =  document.createElement("INPUT");
var inp6 =  document.createElement("INPUT");
var inp7 =  document.createElement("TEXTAREA");

crew_count ++;
var current_crew = "crew_" + crew_count;
var current_call_scheduled = "crew_call_scheduled_" + crew_count;
var current_call_actual = "crew_call_actual_" + crew_count;
var current_meal = "crew_meal_" + crew_count;
var current_wrap = "crew_wrap_" + crew_count;
var current_departloc = "crew_departloc_" + crew_count;
var current_remarks = "crew_remarks_" + crew_count;

row.setAttribute("valign", "top");
row.setAttribute("align", "center");

cell1.setAttribute("class", "leftbox");
cell9.setAttribute("class", "rightbox");

inp1.setAttribute("name", current_crew);
inp1.setAttribute("id", current_crew);
var opt1 =  document.createElement("OPTION");
opt1.setAttribute("value", "Unit");
opt1.appendChild(document.createTextNode("Unit"));
var opt2 =  document.createElement("OPTION");
opt2.setAttribute("value", "2nd AD");
opt2.appendChild(document.createTextNode("2nd AD"));
var opt3 =  document.createElement("OPTION");
opt3.setAttribute("value", "3rd AD");
opt3.appendChild(document.createTextNode("3rd AD"));
var opt4 =  document.createElement("OPTION");
opt4.setAttribute("value", "Continuity");
opt4.appendChild(document.createTextNode("Continuity"));
var opt5 =  document.createElement("OPTION");
opt5.setAttribute("value", "Camera");
opt5.appendChild(document.createTextNode("Camera"));
var opt6 =  document.createElement("OPTION");
opt6.setAttribute("value", "Sound");
opt6.appendChild(document.createTextNode("Sound"));
var opt7 =  document.createElement("OPTION");
opt7.setAttribute("value", "Grips");
opt7.appendChild(document.createTextNode("Grips"));
var opt8 =  document.createElement("OPTION");
opt8.setAttribute("value", "Electrics");
opt8.appendChild(document.createTextNode("Electrics"));
var opt9 =  document.createElement("OPTION");
opt9.setAttribute("value", "Make-up/Hair");
opt9.appendChild(document.createTextNode("Make-up/Hair"));
var opt10 =  document.createElement("OPTION");
opt10.setAttribute("value", "Wardrobe");
opt10.appendChild(document.createTextNode("Wardrobe"));
var opt11 =  document.createElement("OPTION");
opt11.setAttribute("value", "Livestock");
opt11.appendChild(document.createTextNode("Livestock"));
var opt12 =  document.createElement("OPTION");
opt12.setAttribute("value", "Stills");
opt12.appendChild(document.createTextNode("Stills"));
var opt13 =  document.createElement("OPTION");
opt13.setAttribute("value", "First Setup");
opt13.appendChild(document.createTextNode("First Setup"));

inp1.appendChild(opt1);
inp1.appendChild(opt2);
inp1.appendChild(opt3);
inp1.appendChild(opt4);
inp1.appendChild(opt5);
inp1.appendChild(opt6);
inp1.appendChild(opt7);
inp1.appendChild(opt8);
inp1.appendChild(opt9);
inp1.appendChild(opt10);
inp1.appendChild(opt11);
inp1.appendChild(opt12);
inp1.appendChild(opt13);

inp2.setAttribute("size","8");
inp2.setAttribute("name", current_call_scheduled);
inp2.setAttribute("id", current_call_scheduled);

inp3.setAttribute("size","8");
inp3.setAttribute("name", current_call_actual);
inp3.setAttribute("id", current_call_actual);

inp4.setAttribute("size","8");
inp4.setAttribute("name", current_meal);
inp4.setAttribute("id", current_meal);

inp5.setAttribute("size","8");
inp5.setAttribute("name", current_wrap);
inp5.setAttribute("id", current_wrap);

inp6.setAttribute("size","8");
inp6.setAttribute("name", current_departloc);
inp6.setAttribute("id", current_departloc);

inp7.setAttribute("name", current_remarks);
inp7.setAttribute("id", current_remarks);

cell2.appendChild(inp1);
cell3.appendChild(inp2);
cell4.appendChild(inp3);
cell5.appendChild(inp4);
cell6.appendChild(inp5);
cell7.appendChild(inp6);
cell8.appendChild(inp7);

row.appendChild(cell1);
row.appendChild(cell2);
row.appendChild(cell3);
row.appendChild(cell4);
row.appendChild(cell5);
row.appendChild(cell6);
row.appendChild(cell7);
row.appendChild(cell8);
row.appendChild(cell9);
tbody.appendChild(row);
//alert(row.innerHTML);
}

function getCounts (form) {
document.getElementById("artist_count").value = artist_count;
document.getElementById("child_count").value = child_count;
document.getElementById("crew_count").value = crew_count;
return true;
}


function getParam(name){
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

</script>
<link href="style.css" rel="stylesheet" type="text/css">
<style type="text/css">
<!--
body {
	margin-left: 15px;
	margin-top: 15px;
	margin-right: 15px;
	margin-bottom: 15px;
}
-->
</style>
</head>

<body onLoad="getParameters()">
				<% 
				//String xml = request.getParameter("outputData");
				String xml = (String)session.getAttribute("outputData");
				xml = xml.replaceAll("<Fill_Out_AD_Report", "<ns2:Fill_Out_AD_Report xmlns:ns2='http://www.yawlfoundation.org/sb/timeSheetInfo'");
				xml = xml.replaceAll("</Fill_Out_AD_Report","</ns2:Fill_Out_AD_Report");
				
				ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
				JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.timesheetinfo");
				Unmarshaller u = jc.createUnmarshaller();
				JAXBElement foadrElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
				FillOutADReportType foadr = (FillOutADReportType)foadrElement.getValue();
				
				GeneralInfoType gi = foadr.getGeneralInfo();
		%>
<table width="1100" height="100%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td height="14" colspan="3" class="background_top">&nbsp;</td></tr>
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td>
		<h1 align="center"><img src="graphics/logo.jpg" width="58" height="57"></h1>      
		<h1 align="center">Assistant Director's Report</h1>
		<form name="form1" method="post" onSubmit="return getCounts(this)">			
			
		<table border="0" align="center">
			<tr><td>&nbsp;</td></tr>

			<tr><td>
				<table width='900' border='0' cellpadding='0' cellspacing='0'>
					<tr><td colspan='8' align='right' valign='top'><img src='graphics/testing/box_top.jpg' width='902' height='10'></td></tr>
					<tr height='30'>
						<td class='leftbox' width='14'>&nbsp;</td>
						<td><strong>Production</strong></td><td><input name='production' type='text' id='production' value='<%=gi.getProduction()%>' readonly></td>
						<td><strong>Date</strong></td><td><input name='date' type='text' id='date' value='<%=gi.getDate().getDay()+"-"+gi.getDate().getMonth()+"-"+gi.getDate().getYear()%>' readonly></td>
						<td><strong>Day</strong></td><td><input name='weekday' type='text' id='weekday' value='<%=gi.getWeekday()%>' readonly></td>
						<td class='rightbox' width='14'>&nbsp;</td>
					</tr>
					<tr height='30'>
						<td class='leftbox' width='14'>&nbsp;</td>
						<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='<%=foadr.getProducer()%>' readonly></td>
						<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='<%=foadr.getDirector()%>' readonly></td>
						<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='<%=gi.getShootDayNo()%>' readonly></td>
						<td class='rightbox' width='14'>&nbsp;</td>
					</tr>
					<tr height='30'>
						<td class='leftbox' width='14'>&nbsp;</td>
						<td><strong>Assistant Director</strong></td>
						<td><input name='assistant_director' type='text' id='assistant_director' value='<%=foadr.getAssistantDirector()%>' readonly></td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td class='rightbox' width='14'>&nbsp;</td>
					</tr>
					<tr height='30'><td colspan='8' class='bottombox'>&nbsp;</td></tr>
				</table>
			</td></tr>
			<tr><td>&nbsp;</td></tr>
			<tr><td>
				<table width="900" border="0" cellpadding="0" cellspacing="0" id="artist">
					<tbody>
						<tr align="center" valign="top"><td colspan="10" align="right" valign="top"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td></tr>
						<tr align="center" valign="top">
							<td width="14" height="27" rowspan="2" align="center" class="leftbox">&nbsp;</td>
							<td rowspan="2" align="center"><strong>Artist</strong></td>
							<td rowspan="2" align="center"><strong>P/U</strong></td>
							<td colspan="2" align="center"><strong>MU/WD/Call</strong></td>
							<td rowspan="2" align="center"><strong>Meal Break</strong></td>
							<td rowspan="2" align="center"><strong>Time Wrap</strong></td>
							<td rowspan="2" align="center"><strong>Travel</strong></td>
							<td rowspan="2" align="center"><strong>Signature</strong></td>
							<td width="15" rowspan="2" class="rightbox"><div align="right"></div></td>
						</tr>
						<tr valign="top" height="30">
						  	<td align="center"><strong>scheduled</strong></td>
						  	<td align="center"><strong>actual</strong></td>
				  		</tr>
						<tr align="center" height="30">
							<td width="14" align="center" class="leftbox">&nbsp;</td>
							<td align="center"><input name="artist_1" type="text" id="artist_12"></td>
							<td align="center"><input name="artist_pu_1" type="text" id="artist_pu_12" size="15"></td>
							<td align="center"><input name="artist_muwdcall_scheduled_1" type="text" id="artist_muwdcall_scheduled_12" size="8"></td>
							<td align="center"><input name="artist_muwdcall_actual_1" type="text" id="artist_muwdcall_actual_12" size="8"></td>
							<td align="center"><input name="artist_meal_1" type="text" id="artist_meal_12" size="8"></td>
							<td align="center"><input name="artist_wrap_1" type="text" id="artist_wrap_12" size="8"></td>
							<td align="center"><input name="artist_travel_1" type="text" id="artist_travel_12" size="8"></td>
							<td align="center"><input name="artist_signature" type="text" id="artist_signature"></td>
							<td width="15" class="rightbox"><div align="right"></div></td>
						</tr> 
					</tbody>
					<tr align="center" valign="top"><td colspan="10" align="right" class="bottombox">&nbsp;</td></tr>
				</table>
	  		</td></tr>
			<tr><td><input type="button" value="Insert Row" onClick="addArtistRow();"></td></tr>
			<tr><td>&nbsp;</td></tr>
			<tr><td>
				<table width="900" cellpadding="0" cellspacing="0" id="child">
					<tbody>
						<tr align="center" valign="top"><td colspan="10"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td></tr>
						<tr align="center" valign="top">
							<td width="15" rowspan="2" class="leftbox">&nbsp;</td>
							<td rowspan="2"><strong>Children</strong></td>
							<td rowspan="2"><strong>P/U</strong></td>
							<td colspan="2"><strong>MU/WD/Call</strong></td>
							<td rowspan="2"><strong>Meal</strong></td>
							<td rowspan="2"><strong>Wrap</strong></td>
							<td rowspan="2"><strong>Travel</strong></td>
							<td rowspan="2"><strong>Remarks<br>Rest breaks, meals)</strong></td>
							<td width="15" rowspan="2" class="rightbox">&nbsp;</td>
						</tr>
						<tr valign="top">
					  		<td align="center"><strong>scheduled</strong></td>
					  		<td><strong>actual</strong></td>
				  		</tr> 
					</tbody>
					<tr align="center" valign="top"><td colspan="10" class="bottombox">&nbsp;</td></tr>
				</table>
			</td></tr>
			<tr><td><input type="button" value="Insert Row" onClick="addChildRow();"></td></tr>
			<tr><td>&nbsp;</td></tr>
			<tr><td>
				<table width="900" border="0" cellpadding="0" cellspacing="0" id="crew">
					<tbody>
						<tr align="center" valign="top"><td colspan="9"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td></tr>
						<tr align="center" valign="top">
							<td width="15" rowspan="2" class="leftbox">&nbsp;</td>
				  			<td rowspan="2"><strong>Crew</strong></td>
							<td colspan="2"><strong>Call</strong></td>
							<td rowspan="2"><strong>Meal Break</strong></td>
							<td rowspan="2"><strong>Wrap</strong></td>
							<td rowspan="2"><strong>Depart Loc</strong></td>
							<td rowspan="2"><strong>Remarks</strong></td>
							<td width="15" rowspan="2" class="rightbox">&nbsp;</td>
						</tr>
						<tr valign="top">
				  			<td align="center"><strong>scheduled</strong></td>
				  			<td align="center"><strong>actual</strong></td>
			  			</tr>
						<tr>
							<td width="15" align="center" class="leftbox">&nbsp;</td>
						  <td align="center" valign="top">							  	<select name="crew_1" id="crew_1">
									<option value="Unit">Unit</option>
									<option value="2nd AD">2nd AD</option>
									<option value="3rd AD">3rd AD</option>
									<option value="Continuity">Continuity</option>
									<option value="Camera">Camera</option>
									<option value="Sound">Sound</option>
									<option value="Grips">Grips</option>
									<option value="Electrics">Electrics</option>
									<option value="Make-up/Hair">Make-up/Hair</option>
									<option value="Wardrobe">Wardrobe</option>
									<option value="Livestock">Livestock</option>
									<option value="Stills">Stills</option>
									<option value="First Setup">First Setup</option>
						    	</select>							</td>
							<td align="center" valign="top"><input name="crew_call_scheduled_1" type="text" id="crew_call_scheduled_1" size="8"></td>
							<td align="center" valign="top"><input name="crew_call_actual_1" type="text" id="crew_call_actual_1" size="8"></td>
							<td align="center" valign="top"><input name="crew_meal_1" type="text" id="crew_meal_1" size="8"></td>
							<td align="center" valign="top"><input name="crew_wrap_1" type="text" id="crew_wrap_1" size="8"></td>
							<td align="center" valign="top"><input name="crew_departloc_1" type="text" id="crew_departloc_1" size="8"></td>
							<td align="center" valign="top"><textarea name="crew_remarks_1" id="crew_remarks_1"></textarea></td>
							<td width="15" align="center" class="rightbox">&nbsp;</td>
						</tr>
			  		</tbody>
			  		<tr align="center" valign="top"><td colspan="9" class="bottombox">&nbsp;</td></tr>
				</table>
			</td></tr>
			<tr><td><input name="button" type="button" onClick="addCrewRow();" value="Insert Row"></td></tr>
			<tr><td>&nbsp;</td></tr>
	
			<tr><td>
				<table width="900" border="0" cellpadding="0" cellspacing="0">
					<tr align="center" valign="top"><td colspan="7"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td></tr>
					<tr align="center" valign="top">
					  	<td width="15" class="leftbox">&nbsp;</td>
					  	<td><strong>Meal</strong></td>
					  	<td><strong>Time</strong></td>
					  	<td><strong>NOs</strong></td>
					  	<td><strong>Location</strong></td>
					  	<td><strong>Remarks</strong></td>
					  	<td width="15" class="rightbox">&nbsp;</td>
					</tr>
					<tr>
						<td width="15" align="center" class="leftbox">&nbsp;</td>
					  	<td align="center" valign="top"><input name="meal_1" type="text" id="meal_1" value="Breakfast" size="20"></td>
					  	<td align="center" valign="top"><input name="meal_time_1" type="text" id="meal_time_1" size="15"></td>
					  	<td align="center" valign="top"><input name="meal_numbers_1" type="text" id="meal_numbers_1" size="10"></td>
					  	<td align="center" valign="top"><input name="meal_location_1" type="text" id="meal_location_1"></td>
					  	<td align="center" valign="top"><textarea name="meal_remarks_1" id="meal_remarks_1"></textarea></td>
					  	<td width="15" align="center" class="rightbox">&nbsp;</td>
					</tr>
					<tr>
						<td width="15" align="center" class="leftbox">&nbsp;</td>
						<td align="center" valign="top"><input name="meal_2" type="text" id="meal_2" value="Morning Tea" size="20"></td>
						<td align="center" valign="top"><input name="meal_time_2" type="text" id="meal_time_2" size="15"></td>
						<td align="center" valign="top"><input name="meal_numbers_2" type="text" id="meal_numbers_2" size="10"></td>
						<td align="center" valign="top"><input name="meal_location_2" type="text" id="meal_location_2"></td>
						<td align="center" valign="top"><textarea name="meal_remarks_2" id="meal_remarks_2"></textarea></td>
						<td width="15" align="center" class="rightbox">&nbsp;</td>
					</tr>
					<tr>
					  	<td width="15" align="center" class="leftbox">&nbsp;</td>
					  	<td align="center" valign="top"><input name="meal_3" type="text" id="meal_3" value="Lunch" size="20"></td>
					  	<td align="center" valign="top"><input name="meal_time_3" type="text" id="meal_time_3" size="15"></td>
					  	<td align="center" valign="top"><input name="meal_numbers_3" type="text" id="meal_numbers_3" size="10"></td>
					  	<td align="center" valign="top"><input name="meal_location_3" type="text" id="meal_location_3"></td>
					  	<td align="center" valign="top"><textarea name="meal_remarks_3" id="meal_remarks_3"></textarea></td>
					  	<td width="15" align="center" class="rightbox">&nbsp;</td>
					</tr>
					<tr>
					  	<td width="15" align="center" class="leftbox">&nbsp;</td>
					 	<td align="center" valign="top"><input name="meal_4" type="text" id="meal_4" value="Afternoon Tea" size="20"></td>
					  	<td align="center" valign="top"><input name="meal_time_4" type="text" id="meal_time_4" size="15"></td>
					  	<td align="center" valign="top"><input name="meal_numbers_4" type="text" id="meal_numbers_4" size="10"></td>
					  	<td align="center" valign="top"><input name="meal_location_4" type="text" id="meal_location_4"></td>
					  	<td align="center" valign="top"><textarea name="meal_remarks_4" id="meal_remarks_4"></textarea></td>
					  	<td width="15" align="center" class="rightbox">&nbsp;</td>
					</tr>
					<tr>
					  	<td width="15" align="center" class="leftbox">&nbsp;</td>
					  	<td align="center" valign="top"><input name="meal_5" type="text" id="meal_5" value="Supper/Dinner" size="20"></td>
					  	<td align="center" valign="top"><input name="meal_time_5" type="text" id="meal_time_5" size="15"></td>
					  	<td align="center" valign="top"><input name="meal_numbers_5" type="text" id="meal_numbers_5" size="10"></td>
					  	<td align="center" valign="top"><input name="meal_location_5" type="text" id="meal_location_5"></td>
					  	<td align="center" valign="top"><textarea name="meal_remarks_5" id="meal_remarks_5"></textarea></td>
					  	<td width="15" align="center" class="rightbox">&nbsp;</td>
					</tr>
					<tr><td colspan="7" align="center" class="bottombox">&nbsp;</td></tr>
	  			</table>
			</td></tr>
			<tr><td>&nbsp;</td></tr>
			<tr><td>
				<table width="900" border="0" cellpadding="0" cellspacing="0">
					<tr><td colspan="3"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td></tr>
					<tr><td height="30" class="leftbox">&nbsp;</td><td height="30" align="center"><strong>Additional Comments</strong></td><td height="30" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center"><textarea name="additional_comments" cols="100" id="textarea7"></textarea></td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center">&nbsp;</td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center"><strong>Delays</strong></td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center"><textarea name="delays" cols="100" id="textarea8"></textarea></td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center">&nbsp;</td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center"><strong>Accidents</strong></td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center"><textarea name="accidents" cols="100" id="textarea9"></textarea></td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center">&nbsp;</td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center"><strong>Major Props/Action Vehicles/Extra Equipment</strong></td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center"><textarea name="major_props" cols="100" id="textarea11"></textarea></td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center">&nbsp;</td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center"><strong>Additional Personnel</strong></td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center"><textarea name="additional_personnel" cols="100" id="additional_personnel"></textarea></td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center">&nbsp;</td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" class="leftbox">&nbsp;</td><td align="center"><strong>General Comments (Cast, Delays, Injuries, Gear Failure, Late Arrivals, etc):</strong></td><td width="15" class="rightbox">&nbsp;</td></tr>
					<tr><td width="15" height="90" class="leftbox">&nbsp;</td><td height="90" align="center"><textarea name="general_comments" cols="100" id="general_comments"></textarea></td><td width="15" height="90" class="rightbox">&nbsp;</td></tr>
					<tr><td colspan="3" class="bottombox">&nbsp;</td></tr>
	  			</table>
			</td></tr>
			<tr><td>&nbsp;</td>
		</tr>
		<tr><td align="center">	
		<input type="hidden" name="artist_count" id="artist_count" value="1"/>
		<input type="hidden" name="child_count" id="child_count" value="0"/>
		<input type="hidden" name="crew_count" id="crew_count" value="1"/>
		<input type="hidden" name="meal_count" id="meal_count" value="5"/>
		<input type="hidden" name="workItemID" id="workItemID"/>
		<input type="hidden" name="userID" id="userID"/>
		<input type="hidden" name="sessionHandle" id="sessionHandle"/>
		<input type="hidden" name="specID" id="specID"/>
		<input type="hidden" name="submit" id="submit"/>
		<input type="button" value="Print"  onclick="window.print()"/>
		<input type="submit" name="Save" value="Save"/>
		<input type="submit" name="Submission" value="Submission"/>
        </td></tr>	
	</table>
	</form>	
	</td>
	<td width="14" class="background_right">&nbsp;</td>
  </tr>
  <tr><td height="14" colspan="3" class="background_bottom">&nbsp;</td></tr>
</table>
	
	<%
				if(request.getParameter("Submission") != null){

					int artist_count = Integer.parseInt(request.getParameter("artist_count"));
					int child_count = Integer.parseInt(request.getParameter("child_count"));
					int crew_count = Integer.parseInt(request.getParameter("crew_count"));
					int meal_count = Integer.parseInt(request.getParameter("meal_count"));
					
					ArtistTimeSheetType ats = new ArtistTimeSheetType();
					for(int ck1=1; ck1<=artist_count; ck1++){//getting the artist information
						SingleArtistType sa = new SingleArtistType();
						sa.setArtist(request.getParameter("artist_"+ ck1));
						sa.setPU(request.getParameter("artist_pu_"+ ck1));
						sa.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_scheduled_"+ ck1)));
						sa.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_actual_"+ ck1)));
						sa.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("artist_meal_"+ ck1)));
						sa.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("artist_wrap_"+ ck1)));
						sa.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("artist_travel_"+ ck1)));
						ats.getSingleArtist().add(sa);
					}
					
					ChildrenTimeSheetType chts = new ChildrenTimeSheetType();
					for(int ck2=1; ck2<=child_count; ck2++){//getting the children information
						SingleChildrenType sch = new SingleChildrenType();
						sch.setChildren(request.getParameter("children_"+ ck2));
						sch.setPU(request.getParameter("children_pu_"+ ck2));
						sch.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("children_muwdcall_scheduled_"+ ck2)));
						sch.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("children_muwdcall_actual_"+ ck2)));
						sch.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("children_meal_"+ ck2)));
						sch.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("children_wrap_"+ ck2)));
						sch.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("children_travel_"+ ck2)));
						sch.setRemarks(request.getParameter("children_remarks_"+ ck2));
						chts.getSingleChildren().add(sch);
					}
					
					CrewTimeSheetType cwts = new CrewTimeSheetType();
					for(int ck3=1; ck3<=crew_count; ck3++){//getting the crew information
						SingleCrewType scw = new SingleCrewType();
						scw.setCrew(request.getParameter("crew_"+ ck3));
						scw.setCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("crew_call_scheduled_"+ ck3)));
						scw.setCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("crew_call_actual_"+ ck3)));
						scw.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("crew_meal_"+ ck3)));
						scw.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("crew_wrap_"+ ck3)));
						scw.setDepartLoc(XMLGregorianCalendarImpl.parse(request.getParameter("crew_departloc_"+ ck3)));
						scw.setRemarks(request.getParameter("crew_remarks_"+ ck3));
						cwts.getSingleCrew().add(scw);
					}
					
					MealInfoType mit = new MealInfoType();
					for(int ck4=1; ck4<=meal_count; ck4++){//getting the meal information
						SingleMealType sm = new SingleMealType();
						sm.setMeal(request.getParameter("meal_"+ ck4));
						sm.setTime(request.getParameter("meal_time_"+ ck4));
						sm.setNumbers(new BigInteger(request.getParameter("meal_numbers_"+ ck4)));
						sm.setLocation(request.getParameter("meal_location_"+ ck4));
						sm.setRemarks(request.getParameter("meal_remarks_"+ ck4));
						mit.getSingleMeal().add(sm);
					}
					
					TimeSheetInfoType tsi = new TimeSheetInfoType();
					tsi.setArtistTimeSheet(ats);
					tsi.setChildrenTimeSheet(chts);
					tsi.setCrewTimeSheet(cwts);
					tsi.setMealInfo(mit);
					tsi.setAdditionalComments(request.getParameter("additonal_comments"));
					tsi.setDelays(request.getParameter("delays"));
					tsi.setAccidents(request.getParameter("accidents"));
					tsi.setMajorPropsActionVehiclesExtraEquipment(request.getParameter("major_props"));
					tsi.setAdditionalPersonnel(request.getParameter("additional_personnel"));
					tsi.setGeneralComments(request.getParameter("general_comments"));
					
					foadr.setTimeSheetInfo(tsi);
					
					Marshaller m = jc.createMarshaller();
				    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
				    File f = new File("./backup/ADReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
				    m.marshal( foadrElement,  f);//output to file
				    
					ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
				    m.marshal(foadrElement, xmlOS);//out to ByteArray
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

					int artist_count = Integer.parseInt(request.getParameter("artist_count"));
					int child_count = Integer.parseInt(request.getParameter("child_count"));
					int crew_count = Integer.parseInt(request.getParameter("crew_count"));
					int meal_count = Integer.parseInt(request.getParameter("meal_count"));
					
					ArtistTimeSheetType ats = new ArtistTimeSheetType();
					for(int ck1=1; ck1<=artist_count; ck1++){//getting the artist information
						SingleArtistType sa = new SingleArtistType();
						sa.setArtist(request.getParameter("artist_"+ ck1));
						sa.setPU(request.getParameter("artist_pu_"+ ck1));
						sa.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_scheduled_"+ ck1)));
						sa.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_actual_"+ ck1)));
						sa.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("artist_meal_"+ ck1)));
						sa.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("artist_wrap_"+ ck1)));
						sa.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("artist_travel_"+ ck1)));
						ats.getSingleArtist().add(sa);
					}
					
					ChildrenTimeSheetType chts = new ChildrenTimeSheetType();
					for(int ck2=1; ck2<=child_count; ck2++){//getting the children information
						SingleChildrenType sch = new SingleChildrenType();
						sch.setChildren(request.getParameter("children_"+ ck2));
						sch.setPU(request.getParameter("children_pu_"+ ck2));
						sch.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("children_muwdcall_scheduled_"+ ck2)));
						sch.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("children_muwdcall_actual_"+ ck2)));
						sch.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("children_meal_"+ ck2)));
						sch.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("children_wrap_"+ ck2)));
						sch.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("children_travel_"+ ck2)));
						sch.setRemarks(request.getParameter("children_remarks_"+ ck2));
						chts.getSingleChildren().add(sch);
					}
					
					CrewTimeSheetType cwts = new CrewTimeSheetType();
					for(int ck3=1; ck3<=crew_count; ck3++){//getting the crew information
						SingleCrewType scw = new SingleCrewType();
						scw.setCrew(request.getParameter("crew_"+ ck3));
						scw.setCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("crew_call_scheduled_"+ ck3)));
						scw.setCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("crew_call_actual_"+ ck3)));
						scw.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("crew_meal_"+ ck3)));
						scw.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("crew_wrap_"+ ck3)));
						scw.setDepartLoc(XMLGregorianCalendarImpl.parse(request.getParameter("crew_departloc_"+ ck3)));
						scw.setRemarks(request.getParameter("crew_remarks_"+ ck3));
						cwts.getSingleCrew().add(scw);
					}
					
					MealInfoType mit = new MealInfoType();
					for(int ck4=1; ck4<=meal_count; ck4++){//getting the meal information
						SingleMealType sm = new SingleMealType();
						sm.setMeal(request.getParameter("meal_"+ ck4));
						sm.setTime(request.getParameter("meal_time_"+ ck4));
						sm.setNumbers(new BigInteger(request.getParameter("meal_numbers_"+ ck4)));
						sm.setLocation(request.getParameter("meal_location_"+ ck4));
						sm.setRemarks(request.getParameter("meal_remarks_"+ ck4));
						mit.getSingleMeal().add(sm);
					}
					
					TimeSheetInfoType tsi = new TimeSheetInfoType();
					tsi.setArtistTimeSheet(ats);
					tsi.setChildrenTimeSheet(chts);
					tsi.setCrewTimeSheet(cwts);
					tsi.setMealInfo(mit);
					tsi.setAdditionalComments(request.getParameter("additonal_comments"));
					tsi.setDelays(request.getParameter("delays"));
					tsi.setAccidents(request.getParameter("accidents"));
					tsi.setMajorPropsActionVehiclesExtraEquipment(request.getParameter("major_props"));
					tsi.setAdditionalPersonnel(request.getParameter("additional_personnel"));
					tsi.setGeneralComments(request.getParameter("general_comments"));
					
					foadr.setTimeSheetInfo(tsi);
					Marshaller m = jc.createMarshaller();
				    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
				    
					ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
				    m.marshal(foadrElement, xmlOS);//out to ByteArray

				    response.setHeader("Content-Disposition", "attachment;filename=\"ADReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
				    response.setHeader("Content-Type", "text/xml");

				    ServletOutputStream outs = response.getOutputStream();
				    xmlOS.writeTo(outs);
				    outs.close();
				}					
%>		

</body>
</html>