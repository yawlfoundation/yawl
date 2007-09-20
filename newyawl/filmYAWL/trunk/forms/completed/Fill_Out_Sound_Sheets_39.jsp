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
<%@ page import="org.yawlfoundation.sb.soundinfo.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="1024kb" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Sound Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

    <!-- style sheet imports -->
<link href="graphics/style.css" rel="stylesheet" type="text/css" />
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js" ></script>
<script type="text/javascript" src="scripts/fillOutSoundSheets39.js" ></script>
    
</head>

<body onLoad="getParameters()">
<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td colspan="3" class="background_top">&nbsp;</td></tr>
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td>
	<h1 align="center">Sound Report </h1>      
	<form name="form1" method="post">
		<table width="700"  border="0" align="center" cellpadding="0" cellspacing="0">
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
            int endOfFile = result.indexOf("</ns2:Fill_Out_Sound_Sheets>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(beginOfFile,endOfFile + 28);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		//String xml = request.getParameter("outputData");
//		xml = (String)session.getAttribute("outputData");
        xml ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<ns2:Fill_Out_Sound_Sheets xmlns:ns2=\"http://www.yawlfoundation.org/sb/soundInfo\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.yawlfoundation.org/sb/soundInfo ../soundInfoType.xsd \">\n" +
"  <generalInfo>\n" +
"    <production>production</production>\n" +
"    <date>2001-01-01</date>\n" +
"    <weekday>weekday</weekday>\n" +
"    <shootDayNo>0</shootDayNo>\n" +
"  </generalInfo>\n" +
"  <producer>producer</producer>\n" +
"  <director>director</director>\n" +
"  <editor>editor</editor>\n" +
"  <soundRecordist>soundRecordist</soundRecordist>\n" +
"  <soundInfo>\n" +
"    <soundRoll>soundRoll</soundRoll>\n" +
"    <techInfo>\n" +
"      <sampleRate>sampleRate</sampleRate>\n" +
"      <bitRate>bitRate</bitRate>\n" +
"      <timeCode_DF>timeCode_DF</timeCode_DF>\n" +
"      <timeCodeSource>timeCodeSource</timeCodeSource>\n" +
"      <refTone>refTone</refTone>\n" +
"      <userBits>true</userBits>\n" +
"      <soundMixer>soundMixer</soundMixer>\n" +
"      <cameraFrameRate>cameraFrameRate</cameraFrameRate>\n" +
"      <mediaFormat>mediaFormat</mediaFormat>\n" +
"      <recorder>recorder</recorder>\n" +
"      <transferTo>transferTo</transferTo>\n" +
"    </techInfo>\n" +
"    <sceneInfo>\n" +
"      <scene>scene</scene>\n" +
"      <slateInfo>\n" +
"        <slate>slate</slate>\n" +
"        <takeInfo>\n" +
"          <take>0</take>\n" +
"          <timecode>12:00:00</timecode>\n" +
"          <print>true</print>\n" +
"          <remarks>remarks</remarks>\n" +
"          <camRoll>camRoll</camRoll>\n" +
"        </takeInfo>\n" +
"      </slateInfo>\n" +
"    </sceneInfo>\n" +
"  </soundInfo>\n" +
"  <anotherRoll>true</anotherRoll>\n" +
"  <soundRolls>soundRolls</soundRolls>\n" +
"</ns2:Fill_Out_Sound_Sheets>";
        xml = xml.replaceAll("<Fill_Out_Sound_Sheets", "<ns2:Fill_Out_Sound_Sheets xmlns:ns2='http://www.yawlfoundation.org/sb/soundInfo'");
		xml = xml.replaceAll("</Fill_Out_Sound_Sheets","</ns2:Fill_Out_Sound_Sheets");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.soundinfo");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement fossElement = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file	            
	FillOutSoundSheetsType foss = (FillOutSoundSheetsType) fossElement.getValue();
	GeneralInfoType gi = foss.getGeneralInfo();
	SoundInfoType sity_l = foss.getSoundInfo();
%>
					
			<tr>
			  <td align="center"><strong>Sound Roll # <input name='sound_roll' type='text' id='sound_roll' size="15" value="<% if(sity_l != null) {out.print(sity_l.getSoundRoll()); } %>"></strong></td>
		  </tr>
			<tr><td>&nbsp;</td></tr>
			<tr><td colspan='5'>
				<table width='700' border='0' cellspacing='0' cellpadding='0'>
					<tr><td class="header-left">&nbsp;</td>
					  <td colspan='6' class="header-middle">General Info </td>
					  <td class="header-right">&nbsp;</td>
					</tr>
					<tr>
						<td class='left' width='15'>&nbsp; </td>
						<td><strong>Production</strong></td><td><input name='production' type='text' id='production' value="<%=gi.getProduction()%>" size="15" readonly></td>
						<td><strong>Date</strong></td>
						<td><input name='date' type='text' id='date' value='<%=gi.getDate().getDay()+"-"+gi.getDate().getMonth()+"-"+gi.getDate().getYear()%>' size="15" readonly></td>
						<td><strong>Day</strong></td><td><input name='weekday' type='text' id='weekday' value='<%=gi.getWeekday()%>' size="15" readonly></td>
						<td class='right' width='15'>&nbsp;</td>
					</tr>
					<tr>
						<td class='left' width='15'>&nbsp; </td>
						<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='<%=foss.getProducer()%>' size="15" readonly></td>
						<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='<%=foss.getDirector()%>' size="15" readonly></td>
						<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='<%=gi.getShootDayNo()%>' size="15" readonly></td>
						<td class='right' width='15'>&nbsp;</td>
					</tr>
					<tr>
						<td class='left' width='15'>&nbsp; </td>
						<td><strong>Editor</strong></td><td><input name='editor' type='text' id='editor' value='<%= foss.getEditor() %>' size="15" readonly></td>
						<td><strong>Sound Recordist</strong></td><td><input name='sound_recordist' type='text' id='sound_recordist' value='<%=foss.getSoundRecordist()%>' size="15" readonly></td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td class='right' width='15'>&nbsp;</td>
					</tr>
					<tr><td colspan='8' class='bottom'>&nbsp;</td>
					</tr>
			  </table>
			</td></tr>
			
    		<tr><td width="168">
				<table width="700" border="0" cellpadding="0" cellspacing="0">
      				<tr align="left" valign="top"><td class="header-left">&nbsp;</td>
      				  <td colspan="5" class="header-middle">Tech Spec </td>
      				  <td class="header-right">&nbsp;</td>
      				</tr>
      				<tr align="left" valign="top">
        				<td class="left">&nbsp;</td>
        				<td height="30"><strong>Sample Rate </strong></td>
        				<td height="30"><strong>Bit Rate </strong></td>
        				<td height="30"><strong>Timecode (DF) </strong></td>
        				<td height="30"><strong>Reference Tone</strong></td>
						<td height="30"><strong>Sound Mixer</strong></td>
						<td class="right">&nbsp;</td>
					</tr>
					<% if (sity_l != null) {
						TechInfoType tity_l = sity_l.getTechInfo();
					%>
					<tr>
						<td rowspan="3" valign="top" class="left">&nbsp; </td>
						<td height="30" rowspan="3" valign="top" id="sample_rate_grouping">
							<input name="sample_rate" type="radio" value="44.1 KHz" <% if( tity_l.getSampleRate().equals("44.1 KHz")) { out.print("checked"); }%>>44.1 KHz<br>
							<input name="sample_rate" type="radio" value="48 KHz" <% if( tity_l.getSampleRate().equals("48 KHz")) { out.print("checked"); }%>>48 KHz<br>
							<input name="sample_rate" type="radio" value="96 KHz" <% if( tity_l.getSampleRate().equals("96 KHz")) { out.print("checked"); }%>>96 KHz </td>
						<td height="30" rowspan="3" valign="top" id="bit_rate_grouping">
							<input name="bit_rate" type="radio" value="16" <% if( tity_l.getBitRate().equals("16")) { out.print("checked"); }%>>16<br>
							<input name="bit_rate" type="radio" value="20" <% if( tity_l.getBitRate().equals("20")) { out.print("checked"); }%>>20<br>
							<input name="bit_rate" type="radio" value="24" <% if( tity_l.getBitRate().equals("24")) { out.print("checked"); }%>>24 </td>
						<td height="30" rowspan="3" align="left" id="timecode_grouping">
							<input name="timecode" type="radio" value="24 fps" <% if( tity_l.getTimeCodeDF().equals("24 fps")) { out.print("checked"); }%>>24fps<br>
							<input name="timecode" type="radio" value="25 fps" <% if( tity_l.getTimeCodeDF().equals("25 fps")) { out.print("checked"); }%>>25fps<br>
							<input name="timecode" type="radio" value="29.97 fps" <% if( tity_l.getTimeCodeDF().equals("29.97 fps")) { out.print("checked"); }%>>29.97fps<br>
							<input name="timecode" type="radio" value="30 fps" <% if( tity_l.getTimeCodeDF().equals("30 fps")) { out.print("checked"); }%>>30fps</td>
						<td height="30" align="left"><input name="ref_tone" type="text" id="ref_tone" size="5" value="<%=tity_l.getRefTone() %>" pattern="any_text">dBfs</td>
						<td height="30" rowspan="3" valign="top" id="sound_mixer_grouping">
							<input name="sound_mixer" type="radio" value="Mono" <% if( tity_l.getSoundMixer().equals("Mono")) { out.print("checked"); }%>>Mono<br>
							<input name="sound_mixer" type="radio" value="Stereo" <% if( tity_l.getSoundMixer().equals("Stereo")) { out.print("checked"); }%>>Stereo<br>
							<input name="sound_mixer" type="radio" value="Multi Track" <% if( tity_l.getSoundMixer().equals("Multi Track")) { out.print("checked"); }%>>Multi Track</td>
						<td rowspan="3" valign="top" class="right">&nbsp;</td>
					</tr>
					<tr><td height="30" align="left"><strong>User Bits</strong></td></tr>
					<tr>
						<td height="30" align="left" id="user_bits_grouping">
							<input name="user_bits" type="radio" value="true" <% if(tity_l.isUserBits() == true) {out.print("checked");}%>>Yes
							<input name="user_bits" type="radio" value="false" <% if(tity_l.isUserBits() == false) {out.print("checked");}%>>No</td>
					</tr>
					<tr>
						<th align="left" class="left">&nbsp;</th>
						<th height="30" align="center"><strong>Timecode Source</strong></th>
						<th height="30" align="center"><strong><strong>Format Media</strong></strong></th>
						<th height="30" align="center"><strong>Recorder</strong></th>
						<th height="30" align="center"><strong>Transfer To</strong></th>
						<th height="30" align="center"><strong>Camera Frame Rate </strong></th>
						<th align="left" class="right">&nbsp;</th>
					</tr>
					<tr>
						<td class="left">&nbsp;</td>
					  <td height="30" align="center"><input name="timecode_source" type="text" id="timecode_source" size="15" value="<%= tity_l.getTimeCodeSource()%>" pattern="any_text"></td>
						<td height="30" align="center"><strong><input name="media_format" type="text" id="media_format" size="15" value="<%= tity_l.getMediaFormat()%>" pattern="any_text">
					  </strong></td>
						<td height="30" align="center"><input name="recorder" type="text" id="recorder" size="15" value="<%= tity_l.getRecorder()%>" pattern="any_text"></td>
						<td height="30" align="center"><strong><input name="transfer_to" type="text" id="transfer_to" size="15" value="<%= tity_l.getTransferTo()%>" pattern="any_text">
					  </strong></td>
						<td height="30" align="center"><strong><input name="camera_frame_rate" type="text" id="camera_frame_rate" size="15" value="<%= tity_l.getCameraFrameRate()%>" pattern="any_text">
					  </strong></td>
						<td class="right">&nbsp;</td>
					</tr>
					<% } else {%>
					<tr>
						<td rowspan="3" valign="top" class="left">&nbsp; </td>
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
						<td rowspan="3" valign="top" class="right">&nbsp;</td>
					</tr>
					<tr><td height="30" align="left"><strong>User Bits</strong></td></tr>
					<tr>
						<td height="30" align="left">
							<input name="user_bits" type="radio" value="true">Yes
							<input name="user_bits" type="radio" value="false">No</td>
					</tr>
					<tr>
						<th align="left" class="left">&nbsp;</th>
						<th height="30" align="center"><strong>Timecode Source</strong></th>
						<th height="30" align="center"><strong><strong>Format Media</strong></strong></th>
						<th height="30" align="center"><strong>Recorder</strong></th>
						<th height="30" align="center"><strong>Transfer To</strong></th>
						<th height="30" align="center"><strong>Camera Frame Rate </strong></th>
						<th align="left" class="right">&nbsp;</th>
					</tr>
					<tr>
						<td class="left">&nbsp;</td>
					  <td height="30" align="center"><input name="timecode_source" type="text" id="timecode_source" size="15"></td>
						<td height="30" align="center"><strong><input name="media_format" type="text" id="media_format" size="15">
					  </strong></td>
						<td height="30" align="center"><input name="recorder" type="text" id="recorder" size="15" value=""></td>
						<td height="30" align="center"><strong><input name="transfer_to" type="text" id="transfer_to" size="15">
					  </strong></td>
						<td height="30" align="center"><strong><input name="camera_frame_rate" type="text" id="camera_frame_rate" size="15">
					  </strong></td>
						<td class="right">&nbsp;</td>
					</tr>
					<%}%>
					<tr><td colspan="7" class="bottom">&nbsp;</td>
					</tr>
			  </table>
			</td></tr>
			<tr><td><table width="700" border="0" align="center" cellpadding="0" cellspacing="0" id="table1">
              <tbody>
                <tr align="center" valign="top">
                  <td align="center" class="header-left">&nbsp;</td>
                  <td colspan="7" align="left" class="header-middle">Sound Roll Info </td>
                  <td align="center" class="header-right">&nbsp;</td>
                </tr>
                <tr align="center" valign="top">
                  <td width="15" class="left">&nbsp;</td>
                  <td><strong>Cam Roll</strong></td>
                  <td><strong>Scene</strong></td>
                  <td><strong>Slate</strong></td>
                  <td><strong>Take</strong></td>
                  <td><strong>Print</strong></td>
                  <td><strong>Timecode</strong></td>
                  <td><strong>Remarks</strong></td>
                  <td width="15" class="right">&nbsp;</td>
                </tr>
                <%
						int a=0;
						if (sity_l != null) {
							for(SceneInfoType sit_l : sity_l.getSceneInfo()){
								for(SlateInfoType slt_l : sit_l.getSlateInfo()){
									for(TakeInfoType tit_l : slt_l.getTakeInfo()){
									a++;
					%>
                <tr>
                  <td width="15" align="center" class="left">&nbsp;</td>
                  <td align="center"><input name="<% out.print("cam_roll_"+a);%>" type="text" id="<% out.print("cam_roll_"+a);%>" size="5" value="<%= tit_l.getCamRoll()%>" pattern="any_text"></td>
                  <td align="center"><input name="<% out.print("scene_"+a);%>" type="text" id="<% out.print("scene_"+a);%>" size="5" value="<%= sit_l.getScene()%>" pattern="any_text"></td>
                  <td align="center"><input name="<% out.print("slate_"+a);%>" type="text" id="<% out.print("slate_"+a);%>" size="5" value="<%= slt_l.getSlate()%>" pattern="any_text"></td>
                  <td align="center"><input name="<% out.print("take_"+a);%>" type="text" id="<% out.print("take_"+a);%>" size="5" value="<%= tit_l.getTake()%>" pattern="number"></td>
                  <td align="center"><input name="<% out.print("print_"+a);%>" type="checkbox" id="<% out.print("print_"+a);%>" value="True" <% if(tit_l.isPrint() == true) {out.print("checked");}%>></td>
                  <td align="center"><input name="<% out.print("timecode_"+a);%>" type="text" id="<% out.print("timecode_"+a);%>" size="8" value="<%= tit_l.getTimecode()%>" pattern="date"></td>
                  <td align="center"><textarea name="<% out.print("remarks_"+a);%>" cols="30" id="<% out.print("remarks_"+a);%>"><%= tit_l.getRemarks()%></textarea></td>
                  <td width="15" align="center" class="right">&nbsp;</td>
                </tr>
                <% 
								}
							}
						}
					} else {
					%>
                <tr>
                  <td width="15" align="center" class="left">&nbsp;</td>
                  <td align="center"><input name="cam_roll_1" type="text" id="cam_roll_1" size="5" pattern="any_text"></td>
                  <td align="center"><input name="scene_1" type="text" id="scene_1" size="5" pattern="any_text"></td>
                  <td align="center"><input name="slate_1" type="text" id="slate_1" size="5" pattern="any_text"></td>
                  <td align="center"><input name="take_1" type="text" id="take_1" size="5" pattern="number"></td>
                  <td align="center"><input name="print_1" type="checkbox" id="print_1" value="True"></td>
                  <td align="center"><input name="timecode_1" type="text" id="timecode_1" size="8" pattern="date"></td>
                  <td align="center"><textarea name="remarks_1" cols="30" id="remarks_1"></textarea></td>
                  <td width="15" align="center" class="right">&nbsp;</td>
                </tr>
                <% } %>
              </tbody>
              <tr align="center" valign="top">
                <td align="center" class="left">&nbsp;</td>
                <td colspan="1" align="left"><input name="button100" type="button" onClick="addRow();" value="Insert Row"></td>
                <td colspan="6" align="left"><input name="button101" type="button" onClick="deleteRow();" value="delete Row"></td>
                <td align="center" class="right">&nbsp;</td>
              </tr>
              <tr align="left" valign="top">
                <td colspan="9" class="bottom">
                  <input type="hidden" name="count" id="count" value="<%if (a==0) {out.print("1");}else{out.print(a);}%>"/>
                  <input type="hidden" name="workItemID" id="workItemID">
                  <input type="hidden" name="userID" id="userID">
                  <input type="hidden" name="sessionHandle" id="sessionHandle">
                  <input type="hidden" name="JSESSIONID" id="JSESSIONID">
                  <input type="hidden" name="submit" id="submit"></td>
              </tr>
            </table></td></tr>
			<tr>
			  <td align="center">Another Roll
              <input name="another_roll" type="checkbox" id="another_roll" value="True" <% if(foss.isAnotherRoll() == true) {out.print("checked");}%>>
              <input type="hidden" name="sound_rolls" id="sound_rolls" value="<%=foss.getSoundRolls()%>"/>
			  </td>
		  </tr>
			<tr><td>&nbsp;
   			</td></tr>
  		</table>
  		<p align="center">
  		<input type="button" value="Print"  onclick="window.print()"/>
  		<input type="submit" name="Save" value="Save" onclick="return validateWithTechSpec('form1');"/>
  		<input type="submit" name="Submission" value="Submission"onclick="return validateWithTechSpec('form1');"/></p></form>
		
			<!-- LOAD -->
    <form method="post" action="Fill_Out_Sound_Sheets_39.jsp?formType=load&workItemID=<%= request.getParameter("workItemID") %>&userID=<%= request.getParameter("userID") %>&sessionHandle=<%= request.getParameter("sessionHandle") %>&JSESSIONID=<%= request.getParameter("JSESSIONID") %>&submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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
<!-- END LOAD -->
	</td>
    <td width="14" class="background_right">&nbsp;</td></tr>
  <tr><td colspan="3" class="background_bottom">&nbsp;</td></tr>
</table>
<%
if(request.getParameter("Submission") != null){
	
	int count = Integer.parseInt(request.getParameter("count"));
	
	TechInfoType thi = new TechInfoType();
	thi.setSampleRate(request.getParameter("sample_rate"));
	thi.setBitRate(request.getParameter("bit_rate"));
	thi.setTimeCodeDF(request.getParameter("timecode"));
	thi.setTimeCodeSource(request.getParameter("timecode_source"));
	thi.setRefTone(request.getParameter("ref_tone"));
	
	if (request.getParameter("user_bits").equals("true")){
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
		if (request.getParameter("print_"+i)==null)
			ti.setPrint(false);
		else
			ti.setPrint(true);
		ti.setRemarks(request.getParameter("remarks_"+i));
		ti.setCamRoll(request.getParameter("cam_roll_"+i));
				
		tempSceneNO=request.getParameter("scene_"+i);
		tempSlateNO=request.getParameter("slate_"+i);
		tempSceneNOSlateNO=tempSceneNO+"\t"+tempSlateNO;//concatenation of tempSceneNO and tempSlateNO. The token separator is "\t"
		
		tempSlate=slates.get(tempSceneNOSlateNO);
		
		if (tempSlate==null){
			SlateInfoType si = new SlateInfoType();
			si.setSlate(tempSlateNO);
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
	
	sit.setSoundRoll(request.getParameter("sound_roll"));
	sit.setTechInfo(thi);
	sit.getSceneInfo().addAll(scl);
	
	gi.setProduction(request.getParameter("production"));
	
	String temp_date = request.getParameter("date");
	String date_array[] = temp_date.split("-");
	if (date_array[0].length() == 1) {
		date_array[0] = "0" + date_array[0];
	}
	if (date_array[1].length() == 1) {
		date_array[1] = "0" + date_array[1];
	}
	temp_date = date_array[2] + "-" + date_array[1] + "-" + date_array[0];
	gi.setDate(XMLGregorianCalendarImpl.parse(temp_date));
	
	gi.setWeekday(request.getParameter("weekday"));
	gi.setShootDayNo(new BigInteger(request.getParameter("shoot_day")));
	
	foss.setProducer(request.getParameter("producer"));
	foss.setDirector(request.getParameter("director"));
	foss.setEditor(request.getParameter("editor"));
	foss.setSoundRecordist(request.getParameter("sound_recordist"));
	foss.setSoundInfo(sit);
	if (request.getParameter("another_roll")==null){
		foss.setAnotherRoll(false);
	}else{
		foss.setAnotherRoll(true);
	}
	foss.setSoundRolls(request.getParameter("sound_rolls"));
	
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
}else if(request.getParameter("Save") != null){				

	int count = Integer.parseInt(request.getParameter("count"));
	
	TechInfoType thi = new TechInfoType();
	thi.setSampleRate(request.getParameter("sample_rate"));
	thi.setBitRate(request.getParameter("bit_rate"));
	thi.setTimeCodeDF(request.getParameter("timecode"));
	thi.setTimeCodeSource(request.getParameter("timecode_source"));
	thi.setRefTone(request.getParameter("ref_tone"));
	
	if (request.getParameter("user_bits").equals("true")){
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
		if (request.getParameter("print_"+i)==null)
			ti.setPrint(false);
		else
			ti.setPrint(true);
		ti.setRemarks(request.getParameter("remarks_"+i));
		ti.setCamRoll(request.getParameter("cam_roll_"+i));
				
		tempSceneNO=request.getParameter("scene_"+i);
		tempSlateNO=request.getParameter("slate_"+i);
		tempSceneNOSlateNO=tempSceneNO+"\t"+tempSlateNO;//concatenation of tempSceneNO and tempSlateNO. The token separator is "\t"
		
		tempSlate=slates.get(tempSceneNOSlateNO);
		
		if (tempSlate==null){
			SlateInfoType si = new SlateInfoType();
			si.setSlate(tempSlateNO);
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
	
	sit.setSoundRoll(request.getParameter("sound_roll"));
	sit.setTechInfo(thi);
	sit.getSceneInfo().addAll(scl);
	
	gi.setProduction(request.getParameter("production"));
	
	String temp_date = request.getParameter("date");
	String date_array[] = temp_date.split("-");
	if (date_array[0].length() == 1) {
		date_array[0] = "0" + date_array[0];
	}
	if (date_array[1].length() == 1) {
		date_array[1] = "0" + date_array[1];
	}
	temp_date = date_array[2] + "-" + date_array[1] + "-" + date_array[0];
	gi.setDate(XMLGregorianCalendarImpl.parse(temp_date));
	
	gi.setWeekday(request.getParameter("weekday"));
	gi.setShootDayNo(new BigInteger(request.getParameter("shoot_day")));
	
	foss.setProducer(request.getParameter("producer"));
	foss.setDirector(request.getParameter("director"));
	foss.setEditor(request.getParameter("editor"));
	foss.setSoundRecordist(request.getParameter("sound_recordist"));
	foss.setSoundInfo(sit);		
	if (request.getParameter("another_roll")==null){
		foss.setAnotherRoll(false);
	}else{
		foss.setAnotherRoll(true);
	}
	foss.setSoundRolls(request.getParameter("sound_rolls"));			
	
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
</body>
</html>