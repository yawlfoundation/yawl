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
<%@ page import="org.yawlfoundation.sb.soundinfo.*"%>
<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Sound Report</title>
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
var inp7 =  document.createElement("TEXTAREA");

count ++;
var current_cam_roll = "cam_roll_" + count;
var current_scene = "scene_" + count;
var current_slate = "slate_" + count;
var current_take = "take_" + count;
var current_print = "print_" + count;
var current_timecode = "timecode_" + count;
var current_remarks = "remarks_" + count;

row.setAttribute("align","center");

cell1.setAttribute("class", "leftbox");
cell10.setAttribute("class", "rightbox");

inp1.setAttribute("size","5");
inp1.setAttribute("name", current_cam_roll);
inp1.setAttribute("id", current_cam_roll);

inp2.setAttribute("size","5");
inp2.setAttribute("name", current_scene);
inp2.setAttribute("id", current_scene);

inp3.setAttribute("size","5");
inp3.setAttribute("name", current_slate);
inp3.setAttribute("id", current_slate);

inp4.setAttribute("size","5");
inp4.setAttribute("name", current_take);
inp4.setAttribute("id", current_take);

inp5.setAttribute("type","checkbox");
inp5.setAttribute("value","True");
inp5.setAttribute("name", current_print);
inp5.setAttribute("id", current_print);

inp6.setAttribute("size", "8")
inp6.setAttribute("name", current_timecode);
inp6.setAttribute("id", current_timecode);

inp7.setAttribute("cols","50");
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
<table width="1100" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td colspan="3" class="background_top">&nbsp;</td></tr>
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td>
	<h1 align="center"><img src="graphics/logo.jpg" width="58" height="57"></h1>      
	<h1 align="center">Sound Report</h1>
		<form name="form1" method="post" onSubmit="return getCount(this)">
		<table width="900"  border="0" align="center" cellpadding="0" cellspacing="0">
			<% 
			//String xml = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><ns2:Fill_Out_Sound_Sheets xmlns:ns2='http://www.yawlfoundation.org/sb/soundInfo'><generalInfo><production>miracle</production><date>2007-05-18</date><weekday>fri</weekday><shootDayNo>4</shootDayNo></generalInfo><producer>me</producer><director>you</director><editor>her</editor><soundRecordist>him</soundRecordist><soundInfo/></ns2:Fill_Out_Sound_Sheets>";      
			//String xml = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><ns2:soundInfo xmlns:ns2='http://www.yawlfoundation.org/sb/soundInfo'></ns2:soundInfo>";      
				 
				//String xml = request.getParameter("outputData");
				String xml = (String)session.getAttribute("outputData");
				xml = xml.replaceAll("<Fill_Out_Sound_Sheets", "<ns2:Fill_Out_Sound_Sheets xmlns:ns2='http://www.yawlfoundation.org/sb/soundInfo'");
				xml = xml.replaceAll("</Fill_Out_Sound_Sheets","</ns2:Fill_Out_Sound_Sheets");
				
				ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
				JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.soundinfo");
				Unmarshaller u = jc.createUnmarshaller();
				JAXBElement fossElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
				FillOutSoundSheetsType foss = (FillOutSoundSheetsType)fossElement.getValue();
	
				GeneralInfoType gi = foss.getGeneralInfo();
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
						<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='<%=foss.getProducer()%>' readonly></td>
						<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='<%=foss.getDirector()%>' readonly></td>
						<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='<%=gi.getShootDayNo()%>' readonly></td>
						<td class='rightbox' width='15'></td>
					</tr>
					<tr height='30'>
						<td class='leftbox' width='15'></td>
						<td><strong>Editor</strong></td><td><input name='editor' type='text' id='editor' value='<%= foss.getEditor() %>' readonly></td>
						<td><strong>Sound Recordist</strong></td><td><input name='sound_recordist' type='text' id='sound_recordist' value='<%=foss.getSoundRecordist()%>' readonly></td>
						<td><strong>Sound Roll #</strong></td><td><input name='sound_roll' type='text' id='sound_roll'></td>
						<td class='rightbox' width='15'></td>
					</tr>
					<tr height='30'><td colspan='8' class='bottombox'>&nbsp;</td></tr>
				</table>
			</td></tr>
			<%
				if(request.getParameter("Submission") != null){
					
					int count = Integer.parseInt(request.getParameter("count"));
					
					TechInfoType thi = new TechInfoType();
					thi.setSampleRate(request.getParameter("sample_rate"));
					thi.setBitRate(request.getParameter("bit_rate"));
					thi.setTimeCodeDF(request.getParameter("timecode"));
					thi.setTimeCodeSource(request.getParameter("timecode_source"));
					thi.setRefTone(request.getParameter("ref_tone"));
					
					if (request.getParameter("user_bits")== "true"){
						thi.setUserBits(true);
					}
					else{
						thi.setUserBits(false);
					}
					
					thi.setSoundMixer(request.getParameter("sound_mixer"));
					thi.setCameraFrameRate(request.getParameter("camera_frame_rate"));
					thi.setMediaFormat(request.getParameter("media_format"));
					thi.setRecorder(request.getParameter("recorder"));
					thi.setTransferTo(request.getParameter("transfer_to"));

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
						ti.setTimecode(XMLGregorianCalendarImpl.parse(request.getParameter("timecode_"+i)));
						ti.setRemarks(request.getParameter("remarks_"+i));
						ti.setCamRoll(request.getParameter("cam_roll_"+i));
								
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
					
					List<SceneInfoType> scl = new ArrayList<SceneInfoType>(scenes.values());//creates a list of the scenes and add it to the continuityInfo facade
					SoundInfoType sit = new SoundInfoType();
					
					sit.setSoundRoll(new BigInteger(request.getParameter("sound_roll")));
					sit.setTechInfo(thi);
					sit.getSceneInfo().addAll(scl);
					
					foss.setSoundInfo(sit);
					
					Marshaller m = jc.createMarshaller();
				    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
				    File f = new File("./backup/SoundSheets_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
				    m.marshal( fossElement,  f);//output to file
				    
					ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
				    m.marshal(fossElement, xmlOS);//out to ByteArray
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
					
					TechInfoType thi = new TechInfoType();
					thi.setSampleRate(request.getParameter("sample_rate"));
					thi.setBitRate(request.getParameter("bit_rate"));
					thi.setTimeCodeDF(request.getParameter("timecode"));
					thi.setTimeCodeSource(request.getParameter("timecode_source"));
					thi.setRefTone(request.getParameter("ref_tone"));
					
					if (request.getParameter("user_bits")== "true"){
						thi.setUserBits(true);
					}
					else{
						thi.setUserBits(false);
					}
					
					thi.setSoundMixer(request.getParameter("sound_mixer"));
					thi.setCameraFrameRate(request.getParameter("camera_frame_rate"));
					thi.setMediaFormat(request.getParameter("media_format"));
					thi.setRecorder(request.getParameter("recorder"));
					thi.setTransferTo(request.getParameter("transfer_to"));

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
						ti.setTimecode(XMLGregorianCalendarImpl.parse(request.getParameter("timecode_"+i)));
						ti.setRemarks(request.getParameter("remarks_"+i));
						ti.setCamRoll(request.getParameter("cam_roll_"+i));
								
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
					
					List<SceneInfoType> scl = new ArrayList<SceneInfoType>(scenes.values());//creates a list of the scenes and add it to the continuityInfo facade
					SoundInfoType sit = new SoundInfoType();
					
					sit.setSoundRoll(new BigInteger(request.getParameter("sound_roll")));
					sit.setTechInfo(thi);
					sit.getSceneInfo().addAll(scl);
					
					foss.setSoundInfo(sit);					
					
				Marshaller m = jc.createMarshaller();
			    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			    
				ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
			    m.marshal(fossElement, xmlOS);//out to ByteArray

			    response.setHeader("Content-Disposition", "attachment;filename=\"SoundSheets_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
			    response.setHeader("Content-Type", "text/xml");

			    ServletOutputStream outs = response.getOutputStream();
			    xmlOS.writeTo(outs);
			    outs.close();
				}
%>
	        <tr><td width="168">&nbsp;</td></tr>
    		<tr><td>
				<table width="902" border="0" cellpadding="0" cellspacing="0">
      				<tr align="left" valign="top"><td colspan="7"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td></tr>
      				<tr align="left" valign="top">
        				<td width="15" class="leftbox">&nbsp;</td>
        				<td height="30"><strong>Sample Rate </strong></td>
        				<td height="30"><strong>Bit Rate </strong></td>
        				<td height="30"><strong>Timecode (DF) </strong></td>
        				<td height="30"><strong>Reference Tone</strong></td>
						<td height="30"><strong>Sound Mixer</strong></td>
						<td width="15" class="rightbox">&nbsp;</td>
					</tr>
					<tr>
						<td width="15" rowspan="3" valign="top" class="leftbox">&nbsp; </td>
						<td height="30" rowspan="3" valign="top">
							<input name="sample_rate" type="radio" value="44.1 KHz">44.1 KHz<br>
							<input name="sample_rate" type="radio" value="48 KHz">48 KHz<br>
							<input name="sample_rate" type="radio" value="96 KHz">96 KHz </td>
						<td height="30" rowspan="3" valign="top">
							<input name="bit_rate" type="radio" value="16">16<br>
							<input name="bit_rate" type="radio" value="20">20<br>
							<input name="bit_rate" type="radio" value="24">24 </td>
						<td height="30" rowspan="3" align="left">
							<input name="timecode" type="radio" value="24 fps">24fps<br>
							<input name="timecode" type="radio" value="25 fps">25fps<br>
							<input name="timecode" type="radio" value="29.97 fps">29.97fps<br>
							<input name="timecode" type="radio" value="30 fps">30fps</td>
						<td height="30" align="left"><input name="ref_tone" type="text" id="ref_tone" size="5">dBfs</td>
						<td height="30" rowspan="3" valign="top">
							<input name="sound_mixer" type="radio" value="Mono">Mono<br>
							<input name="sound_mixer" type="radio" value="Stereo">Stereo<br>
							<input name="sound_mixer" type="radio" value="Multi Track">Multi Track</td>
						<td width="15" rowspan="3" valign="top" class="rightbox">&nbsp;</td>
					</tr>
					<tr><td height="30" align="left"><strong>User Bits</strong></td></tr>
					<tr>
						<td height="30" align="left">
							<input name="user_bits" type="radio" value="True">Yes
							<input name="user_bits" type="radio" value="False">No</td>
					</tr>
					<tr>
						<th width="15" align="left" class="leftbox">&nbsp;</th>
						<th height="30" align="left"><strong>Timecode Source</strong></th>
						<th height="30" align="left"><strong><strong>Format Media</strong></strong></th>
						<th height="30" align="left"><strong>Recorder</strong></th>
						<th height="30" align="left"><strong>Transfer To</strong></th>
						<th height="30" align="left"><strong>Camera Frame Rate </strong></th>
						<th width="15" align="left" class="rightbox">&nbsp;</th>
					</tr>
					<tr>
						<td width="15" class="leftbox">&nbsp;</td>
						<td height="30"><input name="timecode_source" type="text" id="timecode_source"></td>
						<td height="30"><strong><input name="media_format" type="text" id="media_format"></strong></td>
						<td height="30"><input name="recorder" type="text" id="recorder"></td>
						<td height="30"><strong><input name="transfer_to" type="text" id="transfer_to"></strong></td>
						<td height="30"><strong><input name="camera_frame_rate" type="text" id="camera_frame_rate"></strong></td>
						<td width="15" class="rightbox">&nbsp;</td>
					</tr>
					<tr><td colspan="7" class="bottombox">&nbsp;</td></tr>
				</table>
			</td></tr>
			<tr><td>&nbsp;</td></tr>
			<tr><td>
    			<table width="902" border="0" align="center" cellpadding="0" cellspacing="0" id="table1">
					<tbody>
						<tr align="center" valign="top"><td colspan="9"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td></tr>
						<tr align="center" valign="top">
							<td width="15" class="leftbox">&nbsp;</td>
							<td><strong>Cam Roll</strong></td>
							<td><strong>Scene</strong></td>
							<td><strong>Slate</strong></td>
							<td><strong>Take</strong></td>
							<td><strong>Print</strong></td>
							<td><strong>Timecode</strong></td>
							<td><strong>Remarks</strong></td>
							<td width="15" class="rightbox">&nbsp;</td>
						</tr>
						<tr>
							<td width="15" align="center" class="leftbox">&nbsp;</td>
							<td align="center"><input name="cam_roll_1" type="text" id="cam_roll_1" size="5"></td>
							<td align="center"><input name="scene_1" type="text" id="scene_1" size="5"></td>
							<td align="center"><input name="slate_1" type="text" id="slate_1" size="5"></td>
							<td align="center"><input name="take_1" type="text" id="take_1" size="5"></td>
							<td align="center"><input name="print_1" type="checkbox" id="print_1" value="True"></td>
							<td align="center"><input name="timecode_1" type="text" id="timecode_1" size="8"></td>
							<td align="center"><textarea name="remarks_1" cols="50" id="remarks_1"></textarea></td>
							<td width="15" align="center" class="rightbox">&nbsp;</td>
						</tr>
					</tbody>
					<tr align="center" valign="top"><td colspan="9" class="bottombox">&nbsp;</td></tr>
				</table>		    
			</td></tr>
			<tr><td>
				<input type="button" value="Insert Row" onClick="addRow();">
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
  		<input type="submit" name="Submission" value="Submission"/></p></form>
	</td>
    <td width="14" class="background_right">&nbsp;</td></tr>
  <tr><td colspan="3" class="background_bottom">&nbsp;</td></tr>
</table>

</body>
</html>