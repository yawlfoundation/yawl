<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>

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

inp6.setAttribute("name", current_timecode);
inp6.setAttribute("id", current_timecode);

inp7.setAttribute("cols","80");
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
</head>

<body onLoad="getParameters()">
<h1>Sound Report</h1>
<form name="form1" method="post" onSubmit="return getCount(this)">
<table width="900"  border="0">
				<% 
				//String xml = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><ns2:Fill_Out_Sound_Sheets xmlns:ns2='http://www.yawlfoundation.org/sb/soundInfo'><generalInfo><production>miracle</production><date>2007-05-18</date><weekday>fri</weekday><shootDayNo>4</shootDayNo></generalInfo><producer>me</producer><director>you</director><editor>her</editor><soundRecordist>him</soundRecordist><soundInfo/></ns2:Fill_Out_Sound_Sheets>";      
				//String xml = "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><ns2:soundInfo xmlns:ns2='http://www.yawlfoundation.org/sb/soundInfo'></ns2:soundInfo>";      
				
				String xml = request.getParameter("outputData");
				xml = xml.replaceAll("<Fill_Out_Sound_Sheets", "<ns2:Fill_Out_Sound_Sheets xmlns:ns2='http://www.yawlfoundation.org/sb/soundInfo'");
				xml = xml.replaceAll("</Fill_Out_Sound_Sheets","</ns2:Fill_Out_Sound_Sheets");
				
				ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
				JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.soundinfo");
				Unmarshaller u = jc.createUnmarshaller();
				JAXBElement fossElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
				FillOutSoundSheetsType foss = (FillOutSoundSheetsType)fossElement.getValue();
	
				GeneralInfoType gi = foss.getGeneralInfo();
				
				out.println("<tr><td><table width='800'><tr>");
                out.println("<td><strong>PRODUCTION</strong></td><td><input name='production' type='text' id='production' value='"+gi.getProduction()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>DATE</strong></td><td><input name='date' type='text' id='date' value='"+gi.getDate().getDay()+"-"+gi.getDate().getMonth()+"-"+gi.getDate().getYear()+"' readonly></td><td>&nbsp;</td>");
                out.println("<td><strong>DAY</strong></td><td><input name='weekday' type='text' id='weekday' value='"+gi.getWeekday()+"' readonly></td>");
				out.println("</tr></table></td></tr>");
					
				out.println("<tr><td>&nbsp;</td></tr>");
				out.println("<tr><td><table width='800'><tr>");
				out.println("<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='"+foss.getProducer()+"' readonly></td>");
				out.println("<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='"+foss.getDirector()+"' readonly></td>");
				out.println("<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='"+gi.getShootDayNo()+"' readonly></td>");
				out.println("</tr><tr>");
				//out.println("<td><strong>Director of Photography</strong></td><td><input name='director_photography' type='text' id='director_photography' value='"+foss.getDirectorOfPhotography()+"' readonly></td>");
				out.println("<td><strong>Editor</strong></td><td><input name='editor' type='text' id='editor' value='"+foss.getEditor()+"' readonly></td>");
				out.println("<td><strong>Sound Recordist</strong></td><td><input name='sound_recordist' type='text' id='sound_recordist' value='"+foss.getSoundRecordist()+"' readonly></td>");
				out.println("<td><strong>Sound Roll #</td><td><input name='sound_roll' type='text' id='sound_roll'></strong></td>");
				out.println("</tr></table></td></tr>");
					
				out.println("<tr><td>&nbsp;</td></tr>");
				
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
				    //m.marshal( fossElement, new File("./webapps/JSP/soundSheets.xml") );//output to file
				    
					ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
				    m.marshal(fossElement, xmlOS);//out to ByteArray
					String result = xmlOS.toString().replaceAll("ns2:", "");
				    
					//System.out.println(result);
				    
				    String workItemID = new String(request.getParameter("workItemID"));
				    String sessionHandle = new String(request.getParameter("sessionHandle"));
				    String userID = new String(request.getParameter("userID"));
				    String submit = new String(request.getParameter("submit"));
				    
		    	    session.setAttribute("inputData", result);
				    
				    response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit));
				    return;
				    //InterfaceD_XForm idx = new InterfaceD_XForm(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit+"&inputData="+result);
				}
%>

	<tr><td>&nbsp;</td></tr>
	
	<tr align="center" valign="top">
		<td><strong>SAMPLE RATE</strong></td>
		<td><strong>BIT RATE </strong></td>
		<td><strong>TIMECODE (DF) </strong></td>
		<td><strong>REFERENCE TONE</strong></td>
		<td><strong>SOUND MIXER </strong></td>
		<td><strong>MEDIA FORMAT</strong></td>
	</tr>
	<tr>
		<td rowspan="4" valign="top">
		<input name="sample_rate" type="radio" value="44.1 KHz">44.1 KHz<br>
		<input name="sample_rate" type="radio" value="48 KHz">48 KHz<br>     
		<input name="sample_rate" type="radio" value="96 KHz">96 KHz</td>
		<td rowspan="4" valign="top">
		<input name="bit_rate" type="radio" value="16">16<br>        
		<input name="bit_rate" type="radio" value="20">20<br>        
		<input name="bit_rate" type="radio" value="24">24</td>
		<td rowspan="4" valign="top">
		<input name="timecode" type="radio" value="24 fps">24fps<br>        
		<input name="timecode" type="radio" value="25 fps">25fps<br>      
		<input name="timecode" type="radio" value="29.97 fps">29.97fps<br>      
		<input name="timecode" type="radio" value="30 fps">30fps</td>
		<td align="center"><input name="ref_tone" type="text" id="ref_tone" size="5">dBfs</td>
		<td rowspan="3" valign="top">
		<input name="sound_mixer" type="radio" value="Mono">Mono<br>        
		<input name="sound_mixer" type="radio" value="Stereo">Stereo<br>      
		<input name="sound_mixer" type="radio" value="Multi Track">Multi Track</td>
		<th><input name="media_format" type="text" id="media_format"></th>
	</tr>
	<tr>
		<td align="center"><strong>USER BITS</strong></td>
		<td align="center"><strong>RECORDER</strong></td>
	</tr>
	<tr>
		<td rowspan="2" align="center">
		<input name="user_bits" type="radio" value="True">Yes<input name="user_bits" type="radio" value="False">No</td>
		<th><input name="recorder" type="text" id="recorder"></th>
	</tr>
	<tr>
		<td align="center"><strong>CAMERA FRAME RATE</strong></td>
		<td align="center"><strong>TRANSFER TO</strong></td>
	</tr>
	<tr>
	    <th colspan="2" align="left">TIMECODE SOURCE<input name="timecode_source" type="text" id="timecode_source"></th>
      <th><input name="camera_frame_rate" type="text" id="camera_frame_rate"></th>
		<th><input name="transfer_to" type="text" id="transfer_to"></th>
	</tr>
	<tr>
		<td colspan="7">&nbsp;</td>
	</tr>
	<tr><th colspan="7">&nbsp;</th></tr>
	<tr>
		<td colspan="7">
			<table width="800" align="center" id="table1">
				<tbody>
					<tr align="center" valign="top">
						<td><strong>CAM ROLL</strong></td>
						<td><strong>SCENE</strong></td>
						<td><strong>SLATE</strong></td>
						<td><strong>TAKE</strong></td>
						<td><strong>PRINT</strong></td>
						<td><strong>TIMECODE</strong></td>
						<td><strong>REMARKS</strong></td>
					</tr>
					<tr>
						<td align="center"><input name="cam_roll_1" type="text" id="cam_roll_1" size="5"></td>
						<td align="center"><input name="scene_1" type="text" id="scene_1" size="5"></td>
						<td align="center"><input name="slate_1" type="text" id="slate_1" size="5"></td>
						<td align="center"><input name="take_1" type="text" id="take_1" size="5"></td>
						<td align="center"><input name="print_1" type="checkbox" id="print_1" value="True"></td>
						<td align="center"><input name="timecode_1" type="text" id="timecode_1"></td>
						<td align="center"><textarea name="remarks_1" cols="80" id="remarks_1"></textarea></td>
					</tr>
				</tbody>
			</table>
		</td>
	</tr>
		<tr><td>
			<input type="button" value="Insert Row" onClick="addRow();">
			<input type="hidden" name="count" id="count" value="1"/>
			<input type="hidden" name="workItemID" id="workItemID"/>
			<input type="hidden" name="userID" id="userID"/>
			<input type="hidden" name="sessionHandle" id="sessionHandle"/>
			<input type="hidden" name="submit" id="submit"/>
		</td></tr>
  </table>
  <p><input type="submit" name="Submission" value="Submission"/></p>
</form>
</body>
</html>