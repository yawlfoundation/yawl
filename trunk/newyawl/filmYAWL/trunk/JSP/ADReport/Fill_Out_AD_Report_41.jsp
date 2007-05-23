<%@ page import="java.io.*" %>

<%@ page import="java.math.BigInteger" %>
<%@ page import="com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl" %>


<%@ page import="javax.xml.bind.JAXBElement" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="javax.xml.bind.Unmarshaller" %>

<%@ page import="org.yawlfoundation.sb.timesheetinfo.*"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Assistant Director Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript">
var artist_count = 1;
var child_count = 1;
var crew_count = 12;
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

inp1.setAttribute("name", current_artist);
inp1.setAttribute("id", current_artist);

inp2.setAttribute("size","15");
inp2.setAttribute("name", current_pu);
inp2.setAttribute("id", current_pu);

inp3.setAttribute("size","15");
inp3.setAttribute("name", current_muwdcall_scheduled);
inp3.setAttribute("id", current_muwdcall_scheduled);

inp4.setAttribute("size","15");
inp4.setAttribute("name", current_muwdcall_actual);
inp4.setAttribute("id", current_muwdcall_actual);

inp5.setAttribute("size","15");
inp5.setAttribute("name", current_meal);
inp5.setAttribute("id", current_meal);

inp6.setAttribute("size","15");
inp6.setAttribute("name", current_wrap);
inp6.setAttribute("id", current_wrap);

inp7.setAttribute("size","15");
inp7.setAttribute("name", current_travel);
inp7.setAttribute("id", current_travel);

inp8.setAttribute("name", current_signature);
inp8.setAttribute("id", current_signature);


cell1.appendChild(inp1);
cell2.appendChild(inp2);
cell3.appendChild(inp3);
cell4.appendChild(inp4);
cell5.appendChild(inp5);
cell6.appendChild(inp6);
cell7.appendChild(inp7);
cell8.appendChild(inp8);

row.appendChild(cell1);
row.appendChild(cell2);
row.appendChild(cell3);
row.appendChild(cell4);
row.appendChild(cell5);
row.appendChild(cell6);
row.appendChild(cell7);
row.appendChild(cell8);
tbody.appendChild(row);
//alert(row.innerHTML);

}
//function for adding child information
function addChildRow()
{
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
var inp1 =  document.createElement("INPUT");
var inp2 =  document.createElement("INPUT");
var inp3 =  document.createElement("INPUT");
var inp4 =  document.createElement("INPUT");
var inp5 =  document.createElement("INPUT");
var inp6 =  document.createElement("INPUT");
var inp7 =  document.createElement("INPUT");
var inp8 =  document.createElement("INPUT");

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

inp1.setAttribute("name", current_child);
inp1.setAttribute("id", current_child);

inp2.setAttribute("size","15");
inp2.setAttribute("name", current_pu);
inp2.setAttribute("id", current_pu);

inp3.setAttribute("size","15");
inp3.setAttribute("name", current_muwdcall_scheduled);
inp3.setAttribute("id", current_muwdcall_scheduled);

inp4.setAttribute("size","15");
inp4.setAttribute("name", current_muwdcall_actual);
inp4.setAttribute("id", current_muwdcall_actual);

inp5.setAttribute("size","15");
inp5.setAttribute("name", current_meal);
inp5.setAttribute("id", current_meal);

inp6.setAttribute("size","15");
inp6.setAttribute("name", current_wrap);
inp6.setAttribute("id", current_wrap);

inp7.setAttribute("size","15");
inp7.setAttribute("name", current_travel);
inp7.setAttribute("id", current_travel);

inp8.setAttribute("name", current_remarks);
inp8.setAttribute("id", current_remarks);


cell1.appendChild(inp1);
cell2.appendChild(inp2);
cell3.appendChild(inp3);
cell4.appendChild(inp4);
cell5.appendChild(inp5);
cell6.appendChild(inp6);
cell7.appendChild(inp7);
cell8.appendChild(inp8);

row.appendChild(cell1);
row.appendChild(cell2);
row.appendChild(cell3);
row.appendChild(cell4);
row.appendChild(cell5);
row.appendChild(cell6);
row.appendChild(cell7);
row.appendChild(cell8);
tbody.appendChild(row);
//alert(row.innerHTML);

}
function addCrewRow()
{
var tbody = document.getElementById("crew").getElementsByTagName("tbody")[0];
var row = document.createElement("TR");
var cell1 = document.createElement("TD");
var cell2 = document.createElement("TD");
var cell3 = document.createElement("TD");
var cell4 = document.createElement("TD");
var cell5 = document.createElement("TD");
var cell6 = document.createElement("TD");
var cell7 = document.createElement("TD");
var inp1 =  document.createElement("INPUT");
var inp2 =  document.createElement("INPUT");
var inp3 =  document.createElement("INPUT");
var inp4 =  document.createElement("INPUT");
var inp5 =  document.createElement("INPUT");
var inp6 =  document.createElement("INPUT");
var inp7 =  document.createElement("INPUT");

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

inp1.setAttribute("size","15");
inp1.setAttribute("name", current_crew);
inp1.setAttribute("id", current_crew);

inp2.setAttribute("size","15");
inp2.setAttribute("name", current_call_scheduled);
inp2.setAttribute("id", current_call_scheduled);

inp3.setAttribute("size","15");
inp3.setAttribute("name", current_call_actual);
inp3.setAttribute("id", current_call_actual);

inp4.setAttribute("size","15");
inp4.setAttribute("name", current_meal);
inp4.setAttribute("id", current_meal);

inp5.setAttribute("size","15");
inp5.setAttribute("name", current_wrap);
inp5.setAttribute("id", current_wrap);

inp6.setAttribute("size","15");
inp6.setAttribute("name", current_departloc);
inp6.setAttribute("id", current_departloc);

inp7.setAttribute("name", current_remarks);
inp7.setAttribute("id", current_remarks);



cell1.appendChild(inp1);
cell2.appendChild(inp2);
cell3.appendChild(inp3);
cell4.appendChild(inp4);
cell5.appendChild(inp5);
cell6.appendChild(inp6);
cell7.appendChild(inp7);

row.appendChild(cell1);
row.appendChild(cell2);
row.appendChild(cell3);
row.appendChild(cell4);
row.appendChild(cell5);
row.appendChild(cell6);
row.appendChild(cell7);
tbody.appendChild(row);
//alert(row.innerHTML);

}

function getCounts (form) {
document.getElementById("artist_count").value = artist_count;
document.getElementById("child_count").value = child_count;
document.getElementById("crew_count").value = crew_count;
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
	document.form1.specID.value = getParam('specID');
	document.form1.submit.value = "htmlForm";
}

</script>



</head>

<body onLoad="getParameters()">
<h1>Assistant Director's Report</h1>
<form name="form1" method="post" onSubmit="return getCounts(this)">
  <table width="800"  border="0">
				<% 
				//String xml = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><ns2:Fill_Out_Continuity_Report xmlns:ns2='http://www.yawlfoundation.org/sb/continuityInfo'><generalInfo><production>miracle</production><date>2007-05-18</date><weekday>fri</weekday><shootDayNo>4</shootDayNo></generalInfo><producer>me</producer><director>you</director><directorOfPhotography>him</directorOfPhotography><editor>her</editor><continuity>what</continuity><continuityInfo/></ns2:Fill_Out_Continuity_Report>";      
				String xml = request.getParameter("outputData");
				xml = xml.replaceAll("<Fill_Out_AD_Report", "<ns2:Fill_Out_AD_Report xmlns:ns2='http://www.yawlfoundation.org/sb/timeSheetInfo'");
				xml = xml.replaceAll("</Fill_Out_AD_Report","</ns2:Fill_Out_AD_Report");
				//System.out.println("JSP outputData: "+xml);
				
				ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
				JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.timesheetinfo");
				Unmarshaller u = jc.createUnmarshaller();
				JAXBElement foadrElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
				FillOutADReportType foadr = (FillOutADReportType)foadrElement.getValue();
				
				GeneralInfoType gi = foadr.getGeneralInfo();
				
				out.println("<tr><td><table width='800'><tr>");
                out.println("<td><strong>PRODUCTION</strong></td><td><input name='production' type='text' id='production' value='"+gi.getProduction()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>DATE</strong></td><td><input name='date' type='text' id='date' value='"+gi.getDate().getDay()+"-"+gi.getDate().getMonth()+"-"+gi.getDate().getYear()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>DAY</strong></td><td><input name='weekday' type='text' id='weekday' value='"+gi.getWeekday()+"' readonly></td>");
				out.println("</tr></table></td></tr>");
					
				out.println("<tr><td>&nbsp;</td></tr>");
				out.println("<tr><td><table width='800'><tr>");
				out.println("<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='"+foadr.getProducer()+"' readonly></td>");
				out.println("<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='"+foadr.getDirector()+"' readonly></td>");
				out.println("<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='"+gi.getShootDayNo()+"' readonly></td>");
				out.println("</tr><tr>");
				out.println("<td><strong>Assistant Director</strong></td><td><input name='assistant_director' type='text' id='assistant_director' value='"+foadr.getAssistantDirector()+"' readonly></td>");
				out.println("</tr></table></td></tr>");
					
				out.println("<tr><td>&nbsp;</td></tr>");
				%>
		
	<tr><td>&nbsp;</td></tr>

			<tr>
				<td>
					<table width="900">
						<tr>
							<td colspan="2"><strong>PRODUCTION:</strong></td>
							<td colspan="2"><input name="production" type="text" id="production" size="45"></td>
							<td><strong>DAY NO. </strong></td>
							<td><input name="day_number" type="text" id="day_number"></td>
						</tr>
						<tr>
							<td colspan="2"><strong>PRODUCTION COMPANY </strong></td>
							<td colspan="2"><input name="production_company" type="text" id="production_company" size="45"></td>
							<td><strong>DAY</strong></td>
							<td><input name="day" type="text" id="day"></td>
						</tr>
						<tr>
							<td><strong>TEL</strong></td>
							<td><input name="telephone" type="text" id="telephone"></td>
							<td><strong>FAX</strong></td>
							<td><input name="fax" type="text" id="fax"></td>
							<td><strong>DATE</strong></td>
							<td><input name="date" type="text" id="date"></td>
						</tr>
					</table>
				</td>
			</tr>
		
		<tr><td>&nbsp;</td></tr>
		
		<tr>
			<td>
				<table id="artist" width="900">
					<tbody>
					<tr align="center" valign="top">
						<td rowspan="2"><strong>ARTIST</strong></td>
						<td rowspan="2"><strong>P/U</strong></td>
						<td colspan="2"><strong>MU/WD/CALL</strong></td>
						<td rowspan="2"><strong>MEAL BREAK </strong></td>
						<td rowspan="2"><strong>TIME WRAP </strong></td>
						<td rowspan="2"><strong>TRAVEL</strong></td>
						<td rowspan="2"><strong>SIGNATURE</strong></td>
					</tr>
					<tr valign="top">
					  <td align="center"><strong>scheduled</strong></td>
					  <td align="center"><strong>actual</strong></td>
					</tr>
					<tr align="center">
						<td><input name="artist_1" type="text" id="artist_1"></td>
						<td><input name="artist_pu_1" type="text" id="artist_pu_1" size="15"></td>
						<td><input name="artist_muwdcall_scheduled_1" type="text" id="artist_muwdcall_scheduled_1" size="15"></td>
						<td><input name="artist_muwdcall_actual_1" type="text" id="artist_muwdcall_actual_1" size="15"></td>
						<td><input name="artist_meal_1" type="text" id="artist_meal_1" size="15"></td>
						<td><input name="artist_wrap_1" type="text" id="artist_wrap_1" size="15"></td>
						<td><input name="artist_travel_1" type="text" id="artist_travel_1" size="15"></td>
						<td><input name="artist_signature" type="text" id="artist_signature"></td>
					</tr> 
					</tbody>
				</table>
			</td>
		</tr>
		
		<tr><td><input type="button" value="Insert Row" onClick="addArtistRow();"></td></tr>
		
		<tr><td>&nbsp;</td></tr>
		
		<tr>
			<td>
				<table id="child" width="900">
					<tbody>
						<tr align="center" valign="top">
							<td rowspan="2"><strong>CHILDREN</strong></td>
							<td rowspan="2"><strong>P/U</strong></td>
							<td colspan="2"><strong>MU/WD/CALL</strong></td>
							<td rowspan="2"><strong>MEAL</strong></td>
							<td rowspan="2"><strong>WRAP</strong></td>
							<td rowspan="2"><strong>TRAVEL</strong></td>
							<td rowspan="2"><strong>REMARKS <br>
						    (Rest breaks, meals)</strong></td>
						</tr>
						<tr valign="top">
						  <td align="center"><strong>scheduled</strong></td>
						  <td align="center"><strong>actual</strong></td>
					  </tr>
						<tr align="center">
							<td><input name="children_1" type="text" id="children_1"></td>
							<td><input name="children_pu_1" type="text" id="children_pu_1" size="15"></td>
							<td><input name="children_muwdcall_scheduled_1" type="text" id="children_muwdcall_scheduled_1" size="15"></td>
							<td><input name="children_muwdcall_actual_1" type="text" id="children_muwdcall_actual_1" size="15"></td>
							<td><input name="children_meal_1" type="text" id="children_meal_1" size="15"></td>
							<td><input name="children_wrap_1" type="text" id="children_wrap_1" size="15"></td>
							<td><input name="children_travel_1" type="text" id="children_travel_1" size="15"></td>
							<td><input name="children_remarks_1" type="text" id="children_remarks_1"></td>
						</tr> 
					</tbody>
				</table>
			</td>
		</tr>
		
		<tr><td><input type="button" value="Insert Row" onClick="addChildRow();"></td></tr>
		
		<tr><td>&nbsp;</td></tr>
		
		<tr>
			<td>
				<table id="crew" width="900">
				<tbody>
					<tr align="center" valign="top">
						<td rowspan="2"><strong>CREW</strong></td>
						<td colspan="2"><strong>CALL</strong></td>
						<td rowspan="2"><strong>MEAL BREAK </strong></td>
						<td rowspan="2"><strong>WRAP</strong></td>
						<td rowspan="2"><strong>DEPART LOC </strong></td>
						<td rowspan="2"><strong>REMARKS</strong></td>
					</tr>
					<tr valign="top">
					  <td align="center"><strong>scheduled</strong></td>
					  <td align="center"><strong>actual</strong></td>
				  </tr>
					<tr>
						<td align="center"><input name="crew_1" type="text" id="crew_1" value="Unit" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_1" type="text" id="crew_call_scheduled_1" size="15"></td>
						<td align="center"><input name="crew_call_actual_1" type="text" id="crew_call_actual_1" size="15"></td>
						<td align="center"><input name="crew_meal_1" type="text" id="crew_meal_1" size="15"></td>
						<td align="center"><input name="crew_wrap_1" type="text" id="crew_wrap_1" size="15"></td>
						<td align="center"><input name="crew_departloc_1" type="text" id="crew_departloc_1" size="15"></td>
						<td align="center"><input name="crew_remarks_1" type="text" id="crew_remarks_1"></td>
					</tr>
					<tr>
						<td align="center"><input name="crew_2" type="text" id="crew_2" value="2nd AD" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_2" type="text" id="crew_call_scheduled_2" size="15"></td>
						<td align="center"><input name="crew_call_actual_2" type="text" id="crew_call_actual_2" size="15"></td>
						<td align="center"><input name="crew_meal_2" type="text" id="crew_meal_2" size="15"></td>
						<td align="center"><input name="crew_wrap_2" type="text" id="crew_wrap_2" size="15"></td>
						<td align="center"><input name="crew_departloc_2" type="text" id="crew_departloc_2" size="15"></td>
						<td align="center"><input name="crew_remarks_2" type="text" id="crew_remarks_2"></td>
					</tr>
					<tr>
						<td align="center"><input name="crew_3" type="text" id="crew_3" value="3rd AD" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_3" type="text" id="crew_call_scheduled_3" size="15"></td>
						<td align="center"><input name="crew_call_actual_3" type="text" id="crew_call_actual_3" size="15"></td>
						<td align="center"><input name="crew_meal_3" type="text" id="crew_meal_3" size="15"></td>
						<td align="center"><input name="crew_wrap_3" type="text" id="crew_wrap_3" size="15"></td>
						<td align="center"><input name="crew_departloc_3" type="text" id="crew_departloc_3" size="15"></td>
						<td align="center"><input name="crew_remarks_3" type="text" id="crew_remarks_3"></td>
					</tr>
					<tr>
						<td align="center"><input name="crew_4" type="text" id="crew_4" value="Continuity" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_4" type="text" id="crew_call_scheduled_4" size="15"></td>
						<td align="center"><input name="crew_call_actual_4" type="text" id="crew_call_actual_4" size="15"></td>
						<td align="center"><input name="crew_meal_4" type="text" id="crew_meal_4" size="15"></td>
						<td align="center"><input name="crew_wrap_4" type="text" id="crew_wrap_4" size="15"></td>
						<td align="center"><input name="crew_departloc_4" type="text" id="crew_departloc_4" size="15"></td>
						<td align="center"><input name="crew_remarks_4" type="text" id="crew_remarks_4"></td>
					</tr>
					<tr>
						<td align="center"><input name="crew_5" type="text" id="crew_5" value="Camera" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_5" type="text" id="crew_call_scheduled_5" size="15"></td>
						<td align="center"><input name="crew_call_actual_5" type="text" id="crew_call_actual_5" size="15"></td>
						<td align="center"><input name="crew_meal_5" type="text" id="crew_meal_5" size="15"></td>
						<td align="center"><input name="crew_wrap_5" type="text" id="crew_wrap_5" size="15"></td>
						<td align="center"><input name="crew_departloc_5" type="text" id="crew_departloc_5" size="15"></td>
						<td align="center"><input name="crew_remarks_5" type="text" id="crew_remarks_5"></td>
					</tr>
					<tr>
						<td align="center"><input name="crew_6" type="text" id="crew_6" value="Sound" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_6" type="text" id="crew_call_scheduled_6" size="15"></td>
						<td align="center"><input name="crew_call_actual_6" type="text" id="crew_call_actual_6" size="15"></td>
						<td align="center"><input name="crew_meal_6" type="text" id="crew_meal_6" size="15"></td>
						<td align="center"><input name="crew_wrap_6" type="text" id="crew_wrap_6" size="15"></td>
						<td align="center"><input name="crew_departloc_6" type="text" id="crew_departloc_6" size="15"></td>
						<td align="center"><input name="crew_remarks_6" type="text" id="crew_remarks_6"></td>
					</tr>
					<tr>
						<td align="center"><input name="crew_7" type="text" id="crew_7" value="Grips" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_7" type="text" id="crew_call_scheduled_7" size="15"></td>
						<td align="center"><input name="crew_call_actual_7" type="text" id="crew_call_actual_7" size="15"></td>
						<td align="center"><input name="crew_meal_7" type="text" id="crew_meal_7" size="15"></td>
						<td align="center"><input name="crew_wrap_7" type="text" id="crew_wrap_7" size="15"></td>
						<td align="center"><input name="crew_departloc_7" type="text" id="crew_departloc_7" size="15"></td>
						<td align="center"><input name="crew_remarks_7" type="text" id="crew_remarks_7"></td>
					</tr>
					<tr>
						<td align="center"><input name="crew_8" type="text" id="crew_8" value="Electrics" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_8" type="text" id="crew_call_scheduled_8" size="15"></td>
						<td align="center"><input name="crew_call_actual_8" type="text" id="crew_call_actual_8" size="15"></td>
						<td align="center"><input name="crew_meal_8" type="text" id="crew_meal_8" size="15"></td>
						<td align="center"><input name="crew_wrap_8" type="text" id="crew_wrap_8" size="15"></td>
						<td align="center"><input name="crew_departloc_8" type="text" id="crew_departloc_8" size="15"></td>
						<td align="center"><input name="crew_remarks_8" type="text" id="crew_remarks_8"></td>
					</tr>
					<tr>
						<td align="center"><input name="crew_9" type="text" id="crew_9" value="Make-up/hair" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_9" type="text" id="crew_call_scheduled_9" size="15"></td>
						<td align="center"><input name="crew_call_actual_9" type="text" id="crew_call_actual_9" size="15"></td>
						<td align="center"><input name="crew_meal_9" type="text" id="crew_meal_9" size="15"></td>
						<td align="center"><input name="crew_wrap_9" type="text" id="crew_wrap_9" size="15"></td>
						<td align="center"><input name="crew_departloc_9" type="text" id="crew_departloc_9" size="15"></td>

						<td align="center"><input name="crew_remarks_9" type="text" id="crew_remarks_9"></td>
					</tr>
					<tr>
						<td align="center"><input name="crew_10" type="text" id="crew_10" value="Wardrobe" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_10" type="text" id="crew_call_scheduled_10" size="15"></td>
						<td align="center"><input name="crew_call_actual_10" type="text" id="crew_call_actual_10" size="15"></td>
						<td align="center"><input name="crew_meal_10" type="text" id="crew_meal_10" size="15"></td>
						<td align="center"><input name="crew_wrap_10" type="text" id="crew_wrap_10" size="15"></td>
						<td align="center"><input name="crew_departloc_10" type="text" id="crew_departloc_10" size="15"></td>
						<td align="center"><input name="crew_remarks_10" type="text" id="crew_remarks_10"></td>
					</tr>
					<tr>
						<td align="center"><input name="crew_11" type="text" id="crew_11" value="Livestock" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_11" type="text" id="crew_call_scheduled_11" size="15"></td>
						<td align="center"><input name="crew_call_actual_11" type="text" id="crew_call_actual_11" size="15"></td>
						<td align="center"><input name="crew_meal_11" type="text" id="crew_meal_11" size="15"></td>
						<td align="center"><input name="crew_wrap_11" type="text" id="crew_wrap_11" size="15"></td>
						<td align="center"><input name="crew_departloc_11" type="text" id="crew_departloc_11" size="15"></td>
						<td align="center"><input name="crew_remarks_11" type="text" id="crew_remarks_11"></td>
					</tr>
					<tr>
						<td align="center"><input name="crew_12" type="text" id="crew_12" value="Stills" size="15"></td>
						<td align="center"><input name="crew_call_scheduled_12" type="text" id="crew_call_scheduled_12" size="15"></td>
						<td align="center"><input name="crew_call_actual_12" type="text" id="crew_call_actual_12" size="15"></td>
						<td align="center"><input name="crew_meal_12" type="text" id="crew_meal_12" size="15"></td>
						<td align="center"><input name="crew_wrap_12" type="text" id="crew_wrap_12" size="15"></td>
						<td align="center"><input name="crew_departloc_12" type="text" id="crew_departloc_12" size="15"></td>
						<td align="center"><input name="crew_remarks_12" type="text" id="crew_remarks_12"></td>
					</tr>
				  </tbody>
				</table>
			</td>
		</tr>
		
		<tr>
		  <td><input name="button" type="button" onClick="addCrewRow();" value="Insert Row"></td>
		  </tr>
		<tr>
		  <td>&nbsp;</td>
		  </tr>
		<tr>
		  <td><table width="900">
            <tr align="center" valign="top">
              <td><strong>MEAL</strong></td>
              <td><strong>TIME</strong></td>
              <td><strong>NOs</strong></td>
              <td><strong>LOCATION</strong></td>
              <td><strong>REMARKS</strong></td>
            </tr>
            <tr>
              <td align="center"><input name="meal_1" type="text" id="meal_1" value="Breakfast" size="20"></td>
              <td align="center"><input name="meal_time_1" type="text" id="meal_time_1" size="15"></td>
              <td align="center"><input name="meal_numbers_1" type="text" id="meal_numbers_1" size="10"></td>
              <td align="center"><input name="meal_location_1" type="text" id="meal_location_1"></td>
              <td align="center"><input name="meal_remarks_1" type="text" id="meal_remarks_1"></td>
            </tr>
            <tr>
              <td align="center"><input name="meal_2" type="text" id="meal_2" value="Morning Tea" size="20"></td>
              <td align="center"><input name="meal_time_2" type="text" id="meal_time_2" size="15"></td>
              <td align="center"><input name="meal_numbers_2" type="text" id="meal_numbers_2" size="10"></td>
              <td align="center"><input name="meal_location_2" type="text" id="meal_location_2"></td>
              <td align="center"><input name="meal_remarks_2" type="text" id="meal_remarks_2"></td>
            </tr>
            <tr>

              <td align="center"><input name="meal_3" type="text" id="meal_3" value="Lunch" size="20"></td>
              <td align="center"><input name="meal_time_3" type="text" id="meal_time_3" size="15"></td>
              <td align="center"><input name="meal_numbers_3" type="text" id="meal_numbers_3" size="10"></td>
              <td align="center"><input name="meal_location_3" type="text" id="meal_location_3"></td>
              <td align="center"><input name="meal_remarks_3" type="text" id="meal_remarks_3"></td>
            </tr>
            <tr>
              <td align="center"><input name="meal_4" type="text" id="meal_4" value="Afternoon Tea" size="20"></td>
              <td align="center"><input name="meal_time_4" type="text" id="meal_time_4" size="15"></td>
              <td align="center"><input name="meal_numbers_4" type="text" id="meal_numbers_4" size="10"></td>
              <td align="center"><input name="meal_location_4" type="text" id="meal_location_4"></td>
              <td align="center"><input name="meal_remarks_4" type="text" id="meal_remarks_4"></td>
            </tr>
            <tr>
              <td align="center"><input name="meal_5" type="text" id="meal_5" value="Supper/Dinner" size="20"></td>
              <td align="center"><input name="meal_time_5" type="text" id="meal_time_5" size="15"></td>
              <td align="center"><input name="meal_numbers_5" type="text" id="meal_numbers_5" size="10"></td>
              <td align="center"><input name="meal_location_5" type="text" id="meal_location_5"></td>
              <td align="center"><input name="meal_remarks_5" type="text" id="meal_remarks_5"></td>
            </tr>
          </table></td>
		  </tr>
		<tr><td>&nbsp;</td></tr>
		
		<tr>
		  <td><strong>ADDITIONAL COMMENTS: </strong></td>
		</tr>
		<tr><td align="center"><textarea name="additional_comments" cols="140" id="additional_comments"></textarea></td></tr>
		
		<tr><td>&nbsp;</td></tr>
		
		<tr><td><strong>DELAYS:</strong></td></tr>
		<tr><td align="center"><textarea name="delays" cols="140" id="delays"></textarea></td></tr>
		
		<tr><td>&nbsp;</td></tr>
		
		<tr><td><strong>ACCIDENTS:</strong></td></tr>
		<tr><td align="center"><textarea name="accidents" cols="140" id="accidents"></textarea></td></tr>
		
		<tr><td>&nbsp;</td></tr>
		
		<tr><td><strong>MAJOR PROPS/ACTION VEHICLES/EXTRA EQUIPMENT </strong></td></tr>
		<tr><td align="center"><textarea name="major_props" cols="140" id="major_props"></textarea></td></tr>
		
		<tr><td>&nbsp;</td></tr>
		
		<tr><td><strong>ADDITIONAL PERSONNEL:</strong></td></tr>
		<tr><td align="center"><textarea name="additional_personnel" cols="140" id="additional_personnel"></textarea></td></tr>
		
		<tr><td>&nbsp;</td></tr>
		
		<tr><td><strong>GENERAL COMMENTS (CAST, DELAYS, INJURIES, GEAR FAILURE, LATE ARRIVALS, ETC):</strong></td></tr>
		<tr><td align="center"><textarea name="general_comments" cols="140" id="general_comments"></textarea></td></tr>
		</table>
	    <p>
		<input type="hidden" name="artist_count" id="artist_count" value="1">
		<input type="hidden" name="child_count" id="child_count" value="1">
		<input type="hidden" name="crew_count" id="crew_count" value="12">
		<input type="hidden" name="meal_count" id="meal_count" value="5">
		<input type="hidden" name="workItemID" id="workItemID"/>
		<input type="hidden" name="userID" id="userID"/>
		<input type="hidden" name="sessionHandle" id="sessionHandle"/>
		<input type="hidden" name="specID" id="specID"/>
		<input type="hidden" name="submit" id="submit"/>
	    <input type="submit" name="Submission" value="Submission">
      </p>
	</form>
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
		sa.setSignature(request.getParameter("artist_signature_"+ ck1));
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
    //m.marshal( foadrElement, new File("./webapps/JSP/ADReport.xml") );//output to file
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(foadrElement, xmlOS);//out to ByteArray
	String result = xmlOS.toString().replaceAll("ns2:", "");
    
    String workItemID = new String(request.getParameter("workItemID"));
    String sessionHandle = new String(request.getParameter("sessionHandle"));
    String userID = new String(request.getParameter("userID"));
    String submit = new String(request.getParameter("submit"));
    
	// required response parameters:
	// specID (if launching a case)
	// workitemID (if editing a work item)
	// sessionHandle
	// userid
	// submit (submitting/suspending/saving/cancelling a workitem)
    
	//System.out.println(result);
	
    response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit+"&inputData="+result));
}
%>
</body>
</html>
