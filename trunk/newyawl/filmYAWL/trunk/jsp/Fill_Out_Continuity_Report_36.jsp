<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
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
<%@ page import="org.yawlfoundation.sb.continuityinfo.*"%>
<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Continuity Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript">
var count = 1;
function addRow()
{
count = document.getElementById("count").value;
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

var previous_cam = document.getElementById("cam_" + count);
var previous_sound = document.getElementById("sound_" + count);
var previous_scene = document.getElementById("scene_" + count);
var previous_slate = document.getElementById("slate_" + count);

count ++;
document.getElementById("count").value = count;

var current_cam = "cam_" + count;
var current_sound = "sound_" + count;
var current_scene = "scene_" + count;
var current_slate = "slate_" + count;
var current_take = "take_" + count;
var current_print = "print_" + count;
var current_duration = "duration_" + count;
var current_comments = "comments_" + count;

row.setAttribute("valign", "top");
row.setAttribute("align", "center");

cell1.setAttribute("class", "leftbox");
cell10.setAttribute("class", "rightbox");

inp1.setAttribute("size",  "8");
inp1.setAttribute("value", previous_cam.value);
inp1.setAttribute("name", current_cam);
inp1.setAttribute("id", current_cam);

inp2.setAttribute("size", "8");
inp2.setAttribute("value", previous_sound.value);
inp2.setAttribute("name", current_sound);
inp2.setAttribute("id", current_sound);

inp3.setAttribute("size", "8");
inp3.setAttribute("value", previous_scene.value);
inp3.setAttribute("name", current_scene);
inp3.setAttribute("id", current_scene);

inp4.setAttribute("size", "8");
inp4.setAttribute("value", previous_slate.value);
inp4.setAttribute("name", current_slate);
inp4.setAttribute("id", current_slate);

inp5.setAttribute("size", "8");
inp5.setAttribute("name", current_take);
inp5.setAttribute("id", current_take);

inp6.setAttribute("type", "checkbox");
inp6.setAttribute("value", "True");
inp6.setAttribute("name", current_print);
inp6.setAttribute("id", current_print);

inp7.setAttribute("size", "8");
inp7.setAttribute("name", current_duration);
inp7.setAttribute("id", current_duration);

inp8.setAttribute("cols", "40");
inp8.setAttribute("name", current_comments);
inp8.setAttribute("id", current_comments);

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
</script>
<link href="porchlight.css" rel="stylesheet" type="text/css">
</head>

<body onLoad="getParameters()">
<% 
				//String xml = request.getParameter("outputData");
				String xml = (String)session.getAttribute("outputData");
				xml = xml.replaceAll("<Fill_Out_Continuity_Report", "<ns2:Fill_Out_Continuity_Report xmlns:ns2='http://www.yawlfoundation.org/sb/continuityInfo'");
				xml = xml.replaceAll("</Fill_Out_Continuity_Report","</ns2:Fill_Out_Continuity_Report");
				
				ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
				JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.continuityinfo");
				Unmarshaller u = jc.createUnmarshaller();
				JAXBElement focrElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
				FillOutContinuityReportType focr = (FillOutContinuityReportType)focrElement.getValue();
				
				GeneralInfoType gi = focr.getGeneralInfo();
%>
				
<table width="1100" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td colspan="3" class="background_top">&nbsp;</td></tr>
  
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td>
		<h1 align="center"><img src="graphics/logo.jpg" width="58" height="57"></h1>
		<h1 align="center">Continuity Report</h1>
		<form name="form1" method="post" onSubmit="return getCount(this)">
    	<table width="800" border="0" align="center">
		
			<tr><td>
			
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
						<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='<%=focr.getProducer()%>' readonly></td>
						<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='<%=focr.getDirector()%>' readonly></td>
						<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='<%=gi.getShootDayNo()%>' readonly></td>
						<td class='rightbox' width='15'></td>
					</tr>
					<tr height='30'>
						<td class='leftbox' width='15'></td>
						<td><strong>Director of Photography</strong></td><td><input name='director_photography' type='text' id='director_photography' value='<%=focr.getDirectorOfPhotography()%>' readonly></td>
						<td><strong>Editor</strong></td><td><input name='editor' type='text' id='editor' value='<%=focr.getEditor()%>' readonly></td>
						<td><strong>Continuity</strong></td><td><input name='continuity' type='text' id='continuity' value='<%=focr.getContinuity()%>' readonly></td>
						<td class='rightbox' width='15'></td>
					<tr height='30'><td colspan='8' class='bottombox'>&nbsp;</td></tr>
				</table>
				
			</td></tr>
			<tr><td>
			
				<table width="900" border="0" cellpadding="0" cellspacing="0" id="table1">
					<tbody>
						<tr valign="top"><th colspan="10"><img src="graphics/testing/box_top.jpg" width="902" height="10"></th></tr>
						<tr valign="top">
							<th width="15" class="leftbox">&nbsp;</th>
							<th><strong>Cam</strong></th>
							<th><strong>Sound</strong></th>
							<th><strong>Scene</strong></th>
							<th><strong>Slate</strong></th>
							<th><strong>Take</strong></th>
							<th><strong>Print</strong></th>
							<th><strong>Duration</strong></th>
							<th><strong>Shot Description - Comments<br>(lens, focus, stop, filter)</strong></th>
							<th width="15" class="rightbox">&nbsp;</th>
						</tr>
						<tr valign="top">
							<td width="15" align="center" class="leftbox">&nbsp;</td>
							<td align="center"><input name="cam_1" type="text" id="cam_1" size="8"></td>
							<td align="center"><input name="sound_1" type="text" id="sound_1" size="8"></td>
							<td align="center"><input name="scene_1" type="text" id="scene_1" size="8"></td>
							<td align="center"><input name="slate_1" type="text" id="slate_1" size="8"></td>
							<td align="center"><input name="take_1" type="text" id="take_1" size="8"></td>
							<td align="center"><input name="print_1" type="checkbox" id="print_1" value="True"></td>
							<td align="center"><input name="duration_1" type="text" id="duration_1" size="8"></td>
							<td align="center"><textarea name="comments_1" cols="40" id="comments_1"></textarea></td>
							<td width="15" class="rightbox">&nbsp;</td>
						</tr>
					</tbody>
					<tr valign="top"><th colspan="10" class="bottombox">&nbsp;</th></tr>
				</table>
				
			</td></tr>
		
			<tr><td>
				<input type="button" value="Insert Row" onClick="addRow();"/>
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
	</form>
	</td>
    <td width="14" class="background_right">&nbsp;</td>
  </tr>
  <tr>
    <td colspan="3" class="background_bottom">&nbsp;</td>
  </tr>
</table>
<%
if(request.getParameter("Submission") != null){

	int count = Integer.parseInt(request.getParameter("count"));
	
	Map<String,SceneInfoType> scenes = new TreeMap<String,SceneInfoType>();
	Map<String,SlateInfoType> slates = new TreeMap<String,SlateInfoType>();
	
	SlateInfoType tempSlate = null;
	String tempSlateNO = null;
	String tempSceneNOSlateNO = null;
	
	SceneInfoType tempScene = null;
	String tempSceneNO = null;
	
	for (int i=1;i<=count;i++){//takes are ordered within each slate. Slates are backwards ordered. Scenes are backwards ordered.
		TakeInfoType ti = new TakeInfoType();
		ti.setTake(new BigInteger(request.getParameter("take_"+i)));
		ti.setDuration(XMLGregorianCalendarImpl.parse(request.getParameter("duration_"+i)));
		if (request.getParameter("print_"+i)==null)
			ti.setPrint(false);
		else
			ti.setPrint(true);
		ti.setComments(request.getParameter("comments_"+i));
		ti.setCamRoll(request.getParameter("cam_"+i));
		ti.setSoundRoll(new BigInteger(request.getParameter("sound_"+i)));
		
				
		tempSceneNO=request.getParameter("scene_"+i);
		tempSlateNO=request.getParameter("slate_"+i);
		tempSceneNOSlateNO=tempSceneNO+"\t"+tempSlateNO;//concatenation of tempSceneNO and tempSlateNO. The token separator is "\t"
		
		tempSlate=slates.get(tempSceneNOSlateNO);
		if (tempSlate==null){
			SlateInfoType si = new SlateInfoType();
			si.setSlate(new BigInteger(tempSlateNO));
			si.getTakeInfo().add(ti);
			slates.put(tempSceneNOSlateNO, si);//add the newly created slate into the "slates" map
		}
		else{//the slateNO already exists
			tempSlate.getTakeInfo().add(ti);
		}
	}
	for (String key : slates.keySet()){//adds slates to relative scenes
		StringTokenizer st = new StringTokenizer(key);
		tempSceneNO=st.nextToken();
		
		tempScene=scenes.get(tempSceneNO);
		if (tempScene==null){
			SceneInfoType sci = new SceneInfoType();
			sci.setScene(tempSceneNO);
			sci.getSlateInfo().add(slates.get(key));//retrieves the slate associated to the current scene
			scenes.put(tempSceneNO, sci);
		}
		else{//the sceneNO already exists
			tempScene.getSlateInfo().add(slates.get(key));
		}
	}
	List<SceneInfoType> scl = new ArrayList<SceneInfoType>(scenes.values());//creates a list of the scenes and adds it to the continuityInfo facade
	ContinuityInfoType ci = new ContinuityInfoType();
	ci.getSceneInfo().addAll(scl);
	
	focr.setContinuityInfo(ci);
	
	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    File f = new File("./backup/ContinuityReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
    m.marshal( focrElement,  f);//output to file
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(focrElement, xmlOS);//out to ByteArray
	String result = xmlOS.toString().replaceAll("ns2:", "");
    
    String workItemID = new String(request.getParameter("workItemID"));
    String sessionHandle = new String(request.getParameter("sessionHandle"));
    String userID = new String(request.getParameter("userID"));
    String submit = new String(request.getParameter("submit"));
  
    session.setAttribute("inputData", result);//to be possibly replaced
    response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit));
    return;
}

else if(request.getParameter("Save") != null){

	int count = Integer.parseInt(request.getParameter("count"));
	
	Map<String,SceneInfoType> scenes = new TreeMap<String,SceneInfoType>();
	Map<String,SlateInfoType> slates = new TreeMap<String,SlateInfoType>();
	
	SlateInfoType tempSlate = null;
	String tempSlateNO = null;
	String tempSceneNOSlateNO = null;
	
	SceneInfoType tempScene = null;
	String tempSceneNO = null;
	
	for (int i=1;i<=count;i++){//takes are ordered within each slate. Slates are backwards ordered. Scenes are backwards ordered.
		TakeInfoType ti = new TakeInfoType();
		ti.setTake(new BigInteger(request.getParameter("take_"+i)));
		ti.setDuration(XMLGregorianCalendarImpl.parse(request.getParameter("duration_"+i)));
		if (request.getParameter("print_"+i)==null)
			ti.setPrint(false);
		else
			ti.setPrint(true);
		ti.setComments(request.getParameter("comments_"+i));
		ti.setCamRoll(request.getParameter("cam_"+i));
		ti.setSoundRoll(new BigInteger(request.getParameter("sound_"+i)));
		
				
		tempSceneNO=request.getParameter("scene_"+i);
		tempSlateNO=request.getParameter("slate_"+i);
		tempSceneNOSlateNO=tempSceneNO+"\t"+tempSlateNO;//concatenation of tempSceneNO and tempSlateNO. The token separator is "\t"
		
		tempSlate=slates.get(tempSceneNOSlateNO);
		if (tempSlate==null){
			SlateInfoType si = new SlateInfoType();
			si.setSlate(new BigInteger(tempSlateNO));
			si.getTakeInfo().add(ti);
			slates.put(tempSceneNOSlateNO, si);//add the newly created slate into the "slates" map
		}
		else{//the slateNO already exists
			tempSlate.getTakeInfo().add(ti);
		}
	}
	for (String key : slates.keySet()){//adds slates to relative scenes
		StringTokenizer st = new StringTokenizer(key);
		tempSceneNO=st.nextToken();
		
		tempScene=scenes.get(tempSceneNO);
		if (tempScene==null){
			SceneInfoType sci = new SceneInfoType();
			sci.setScene(tempSceneNO);
			sci.getSlateInfo().add(slates.get(key));//retrieves the slate associated to the current scene
			scenes.put(tempSceneNO, sci);
		}
		else{//the sceneNO already exists
			tempScene.getSlateInfo().add(slates.get(key));
		}
	}
	List<SceneInfoType> scl = new ArrayList<SceneInfoType>(scenes.values());//creates a list of the scenes and adds it to the continuityInfo facade
	ContinuityInfoType ci = new ContinuityInfoType();
	ci.getSceneInfo().addAll(scl);
	
	focr.setContinuityInfo(ci);
	
	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(focrElement, xmlOS);//out to ByteArray

    response.setHeader("Content-Disposition", "attachment;filename=\"ContinuityReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
    response.setHeader("Content-Type", "text/xml");

    ServletOutputStream outs = response.getOutputStream();
    xmlOS.writeTo(outs);
    outs.close();
}
%>
</body>
</html>