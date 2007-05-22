<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>

<%@ page import="java.math.BigInteger" %>

<%@ page import="javax.xml.bind.JAXBElement" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="javax.xml.bind.Unmarshaller" %>

<%@ page import="org.yawlfoundation.sb.camerainfo.*"%>

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

inp1.setAttribute("size","10");
inp1.setAttribute("name", current_slate);
inp1.setAttribute("id", current_slate);

inp2.setAttribute("size","10");
inp2.setAttribute("name", current_take);
inp2.setAttribute("id", current_take);

inp3.setAttribute("size","10");
inp3.setAttribute("name", current_counter_reading);
inp3.setAttribute("id", current_counter_reading);

inp4.setAttribute("size","10");
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

cell1.appendChild(inp1);
cell2.appendChild(inp2);
cell3.appendChild(inp3);
cell4.appendChild(inp4);
cell5.appendChild(inp5);
cell6.appendChild(inp6);
cell6.appendChild(inp7);
cell7.appendChild(inp8);

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
	document.form1.specID.value = getParam('specID');
	document.form1.submit.value = "htmlForm";
}
</script>
</head>

<body onLoad="getParameters()">
<h1>Picture Negative Camera Sheet</h1>
<form name="form1" method="post" onSubmit="return getCount(this)">
  <table width="800"  border="0">
  				<% 
				String xml = request.getParameter("outputData");
				xml = xml.replaceAll("<Fill_Out_Camera_Sheets", "<ns2:Fill_Out_Camera_Sheets xmlns:ns2='http://www.yawlfoundation.org/sb/cameraInfo'");
				xml = xml.replaceAll("</Fill_Out_Camera_Sheets","</ns2:Fill_Out_Camera_Sheets");
				//System.out.println("JSP outputData: "+xml);
				
				ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
				JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.camerainfo");
				Unmarshaller u = jc.createUnmarshaller();
				JAXBElement focsElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
				FillOutCameraSheetsType focs = (FillOutCameraSheetsType)focsElement.getValue();
				
				GeneralInfoType gi = focs.getGeneralInfo();
				
				out.println("<tr><td><table width='800'><tr>");
                out.println("<td><strong>PRODUCTION</strong></td><td><input name='production' type='text' id='production' value='"+gi.getProduction()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>DATE</strong></td><td><input name='date' type='text' id='date' value='"+gi.getDate()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>DAY</strong></td><td><input name='weekday' type='text' id='weekday' value='"+gi.getWeekday()+"' readonly></td>");
				out.println("</tr></table></td></tr>");
					
				out.println("<tr><td>&nbsp;</td></tr>");
				out.println("<tr><td><table width='800'><tr>");
				out.println("<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='"+focs.getProducer()+"' readonly></td>");
				out.println("<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='"+focs.getDirector()+"' readonly></td>");
				out.println("<td><strong>Shoot Day</strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='"+gi.getShootDayNo()+"' readonly></td>");
				out.println("</tr><tr>");
				out.println("<td><strong>Director of Photography</strong></td><td><input name='director_photography' type='text' id='director_photography' value='"+focs.getDirectorOfPhotography()+"' readonly></td>");
				out.println("<td><strong>Camera Operator</strong></td><td><input name='camera_operator' type='text' id='camera_operator' value='"+focs.getCameraOperator()+"' readonly></td>");
				out.println("<td><strong>Camera Assistant</strong></td><td><input name='camera_assistant' type='text' id='camera_assistant' value='"+focs.getCameraAssistant()+"' readonly></td>");
				out.println("</tr></table></td></tr>");
					
				out.println("<tr><td>&nbsp;</td></tr>");
				%>
		
	<tr><td>&nbsp;</td></tr>
  
	<tr>
		<td>
			<table width="800">
				<tr>
					<td><strong>SHEET NUMBER </strong></td>
					<td><input name="sheet_number" type="text" id="sheet_number"></td>
				</tr>
			</table>
		</td>
	</tr>
	
	<tr><td>&nbsp;</td></tr>
	
	<tr>
		<td>
			<table width="800">
				<tr>
					<td><strong>PROJECT NUMBER </strong></td>
					<td><input name="project_number" type="text" id="project_number"></td>
					<td><strong>DATE</strong></td>
					<td><input name="date" type="text" id="date"></td>
				</tr>
				<tr>
					<td><strong>PRODUCTION TITLE </strong></td>
					<td><input name="production_title" type="text" id="production_title"></td>
					<td><strong>CAMERA TYPE AND NUMBER </strong></td>
					<td><input name="camera_type_number" type="text" id="camera_type_number"></td>
				</tr>
				<tr>
					<td><strong>STUDIOS/LOCATIONS</strong></td>
					<td><input name="studios_locations" type="text" id="studios_locations"></td>
					<td><strong>STOCK NUMBER</strong></td>
					<td><input name="stock_number" type="text" id="stock_number"></td>
				</tr>
				<tr>
					<td><strong>DIRECTOR</strong></td>
					<td><input name="director" type="text" id="director"></td>
					<td><strong>EMULSION</strong></td>
					<td><input name="emulsion" type="text" id="emulsion"></td>
				</tr>
				<tr>
					<td><strong>DIRECTOR OF PHOTOGRAPHY </strong></td>
					<td><input name="directory_photography" type="text" id="directory_photography"></td>
					<td><strong>ROLL NUMBER</strong></td>
					<td><input name="roll" type="text" id="roll"></td>
				</tr>
				<tr>
					<td><strong>CAMERA OPERATOR </strong></td>
					<td><input name="camera_operator" type="text" id="camera_operator"></td>
					<td><strong>TOTAL CANS NUMBER </strong></td>
					<td><input name="total_cans_number" type="text" id="total_cans_number"></td>
				</tr>
				<tr>
				  <td><strong>CAMERA ASSISTANT </strong></td>
				  <td><input name="camera_assistant" type="text" id="camera_assistant"></td>
				  <td><strong>MAG NUMBER(S)</strong></td>
				  <td><input name="mag_number" type="text" id="mag_number"></td>
			  </tr>
			</table>
		</td>
	</tr>
	
	<tr><td>&nbsp;</td></tr>
	
	<tr>
		<td>
			<table width="800" id="table1">
				<tbody>
					<tr valign="top">
						<td><strong>SLATE NUMBER </strong></td>
						<td><strong>TAKE NUMBER </strong></td>
						<td><strong>COUNTER READING</strong></td>
						<td><strong>TAKE LENGTH </strong></td>
						<td><strong>PRINT</strong></td>
						<td><strong>PRINT SETTING</strong></td>
						<td><strong>ESSENTIAL INFORMATION/ GENERAL NOTES<br>
						Colour description of scene, filter and/or diffusion used, Day, night or other effects. </strong></td>
					</tr>
					<tr valign="top">
						<td><input name="slate_1" type="text" id="slate_1" size="10"></td>
						<td><input name="take_1" type="text" id="take_1" size="10"></td>
						<td><input name="counter_reading_1" type="text" id="counter_reading_1" size="10"></td>
						<td><input name="take_length_1" type="text" id="take_length_1" size="10"></td>
						<td><input name="print_1" type="checkbox" id="print_1" value="True"></td>
						<td><input name="print_setting_1" id="print_setting_1" type="radio" value="B/W">B/W<br>
						  <input name="print_setting_1" id="print_setting_1" type="radio" value="Colour">Colour</td>
						<td><textarea name="notes_1" id="notes_1"></textarea></td>
					</tr>
				</tbody>
			</table>
		</td>
	</tr>
	
	<tr><td><input type="button" value="Insert Row" onClick="addRow();">
	  <input name="End" type="button" id="End" value="End"></td></tr>
	
	<tr><td>&nbsp;</td></tr>
	
	<tr>
		<td>
			<table width="800">
				<tr>
					<td><strong>TOTAL EXPOSED </strong></td>
					<td><input name="total_exposed" type="text" id="total_exposed" size="10"></td>
					<td><strong>TOTAL EXPOSED </strong></td>
					<td><input name="total_exposed_2" type="text" id="total_exposed_2" size="10"></td>
					<td><strong>TOTAL FTGE PREV DRAWN </strong></td>
					<td><input name="total_footage_prev_drawn" type="text" id="total_footage_prev_drawn" size="10"></td>
					<td><strong>INSTRUCTIONS TO LABORATORY</strong></td>
				</tr>
				<tr>
					<td><strong>SHORT ENDS </strong></td>
					<td><input name="short_ends" type="text" id="short_ends" size="10"></td>
					<td><strong>TOTAL DEVELOPED</strong></td>
					<td><input name="total_developed" type="text" id="total_developed" size="10"></td>
					<td><strong>FOOTAGE DRN TODAY</strong></td>
					<td><input name="footage_drawn_today" type="text" id="footage_drawn_today" size="10"></td>
					<td><textarea name="instructions" id="instructions"></textarea></td>
				</tr>
				<tr>
					<td><strong>WASTE</strong></td>
					<td><input name="waste" type="text" id="waste" size="10"></td>
					<td><strong>TOTAL PRINTED</strong></td>
					<td><input name="total_printed" type="text" id="total_printed" size="10"></td>
					<td><strong>PREVIOUSLY EXPOSED</strong></td>
					<td><input name="previously_exposed" type="text" id="previously_exposed" size="10"></td>
					<td><input name="assistant_signature" type="text" id="assistant_signature"></td>
				</tr>
				<tr>
					<td><strong>FOOTAGE (LOADED) </strong></td>
					<td><input name="footage_loaded" type="text" id="footage_loaded" size="10"></td>
					<td><strong>HELD OR NOT SENT </strong></td>
					<td><input name="held_notsent" type="text" id="held_notsent" size="10"></td>
					<td><strong>EXPOSED TODAY</strong></td>
					<td><input name="exposed_today" type="text" id="exposed_today" size="10"></td>
					<td>Signed by Camera Assistant</td>
				</tr>
			</table>
		</td>
	</tr>
	
   	<tr><td>
			<input type="hidden" name="count" id="count" value="1">
			<input type="hidden" name="workItemID" id="workItemID"/>
			<input type="hidden" name="userID" id="userID"/>
			<input type="hidden" name="sessionHandle" id="sessionHandle"/>
			<input type="hidden" name="specID" id="specID"/>
			<input type="hidden" name="submit" id="submit"/>
		</td></tr>
  </table>
  <p><input type="submit" name="Submission" value="Submission"></p>      
</form>
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
		if (request.getParameter("print_"+i)==null)
			ti.setPrint(false);
		else
			ti.setPrint(true);
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
    //m.marshal( focsElement, new File("./webapps/JSP/cameraSheets.xml") );//output to file
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(focsElement, xmlOS);//out to ByteArray
	String result = xmlOS.toString().replaceAll("ns2:", "");
    
    String workItemID = new String(request.getParameter("workItemID"));
    String sessionHandle = new String(request.getParameter("sessionHandle"));
    String userID = new String(request.getParameter("userID"));
    String submit = new String(request.getParameter("submit"));
    
	System.out.println(result);
	
    response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit+"&inputData="+result));
}
%>
</body>
</html>
