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
<%@ page import="org.yawlfoundation.sb.continuitydailyreport.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="1024kb" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Continuity Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<!-- style sheet imports -->
<link href="graphics/style.css" rel="stylesheet" type="text/css">
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js"></script>
<script type="text/javascript" src="scripts/fillOutContinuityDailyReport.js"></script>

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
            int endOfFile = result.indexOf("</ns2:Fill_Out_Continuity_Daily_Report>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(
                    beginOfFile,
                    endOfFile + 39);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		xml = (String)session.getAttribute("outputData");
		xml = xml.replaceAll("<Fill_Out_Continuity_Daily_Report", "<ns2:Fill_Out_Continuity_Daily_Report xmlns:ns2='http://www.yawlfoundation.org/sb/continuityDailyReport'");
		xml = xml.replaceAll("</Fill_Out_Continuity_Daily_Report","</ns2:Fill_Out_Continuity_Daily_Report");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.continuitydailyreport");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement focdrElement = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file	         
	FillOutContinuityDailyReportType focdr = (FillOutContinuityDailyReportType) focdrElement.getValue();
	GeneralInfoType gi = focdr.getGeneralInfo();
	ContinuityDailyReportType cdrt = focdr.getContinuityDailyReport();
%>
				
<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td colspan="3" class="background_top">&nbsp;</td></tr>
  
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td><h1 align="center">Continuity Daily Report </h1>
		<form name="form1" method="post">
    	<table width="700" border="0" align="center">
			<%-- general info --%>
			<tr>
				<td>
					<table width='700' border='0' cellspacing='0' cellpadding='0'>
						<tr><td class="header-left">&nbsp;</td>
						  <td colspan='4' class="header-middle">General Info </td>
						  <td class="header-right">&nbsp;</td>
						</tr>
						<tr height='30'>
							<td class='left' width='15'>&nbsp;</td>
							<td><strong>Production</strong></td><td><input name='production' type='text' id='production'  size="15" value="<%= gi.getProduction() %>" readonly></td>
							<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' size="15" value="<%= gi.getShootDayNo() %>" readonly></td>
							<td class='right' width='15'>&nbsp;</td>
						</tr>
						<tr height='30'>
							<td class='left' width='15'>&nbsp;</td>
							<td><strong>Day</strong></td>
							<td><input name='weekday' type='text' id='weekday' size="15" value="<%= gi.getWeekday() %>" readonly></td>
							<td><strong>Date</strong></td>
							<td><input name='date' type='text' id='date' size="15" value="<%= gi.getDate() %>" readonly></td>
							<td class='right' width='15'>&nbsp;</td>
						</tr><tr height='30'><td colspan='6' class='bottom'>&nbsp;</td>
						</tr>
				  </table>
				</td>
			</tr>
			<%-- locations --%>
			<tr>
				<td>
					<table width="700" border="0" cellpadding="0" cellspacing="0" id="locations">
						<tbody>
							<tr valign="top"><th class="header-left">&nbsp;</th>
							  <td colspan="4" class="header-middle">Locations</td>
							  <th class="header-right">&nbsp;</th>
							</tr>
							<tr valign="top">
								<td width="15" class="left">&nbsp;</td>
								<td width="185"><strong>Location Name </strong></td>
								<td width="185"><strong>Address</strong></td>
								<td width="185"><strong>Set</strong></td>
								<td width="115">&nbsp;</td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<%int a=0;
							LocationSetsType ls = cdrt.getLocationSets();
							for(SingleLocationType sl: ls.getSingleLocation()){
								a++;
							%>
							<tr valign="top">
								<td width="15" align="center" class="left">&nbsp;</td>
								<td><input name='location_name_<%=a%>' type='text' id='location_name_<%=a%>' size="25" value="<%=sl.getLocationName() %>" readonly></td>
								<td><input name='location_address_<%=a%>' type='text' id='location_address_<%=a%>' size="25" value="<%=sl.getAddress() %>" readonly></td>
								<td width="185" id="location_set_<%=a%>">
								<% int b=0;
									List<String> set_list = sl.getSet();
										for(String set : set_list) {
											b ++;%>
								<input name='location_set_<%=a%>_<%=b%>' type='text' id='location_set_<%=a%>_<%=b%>' size="25" value="<%=set%>"  readonly>
								<%}%>
								</td><td/>
								<!-- <td><input name="button83" type="button" onClick="addSet('location_set_<%=a%>');" value="Insert Set"/> -->
								<!-- <input type="hidden" name="location_set_<%=a%>_count" id="location_set_<%=a%>_count" value="<%=b%>"/></td> -->
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<%}%>
						</tbody>
						<tr valign="top">
						  <th class="left">&nbsp;</th>
						  <td colspan="4" align="left"></td>
						  <!-- <td colspan="4" align="left"><input name="button" type="button" id="button" onClick="addLocation();" value="Insert Row"/></td> -->
						  <td class="right">&nbsp;</td>
					  </tr>
						<tr valign="top"><th colspan="6" class="bottom">&nbsp;</th>
						</tr>
				  </table>
				</td>
			</tr>
			<tr>
              <td>
                <table width="700" border="0" cellpadding="0" cellspacing="0" id="tape_info">
                  <tbody>
                    <tr valign="top">
                      <th class="header-left">&nbsp;</th>
                      <td colspan="2" class="header-middle">Tape Info </td>
                      <th class="header-right">&nbsp;</th>
                    </tr>
                    <tr valign="top">
                      <td align="center" class="left">&nbsp;</td>
                      <td width="121"><strong>Stills</strong></td>
                      <td><input name='stills' type='text' id='stills'  size="15" value="<%if(cdrt.getStills() != null) {out.print(cdrt.getStills());}%>" readonly></td>
                      <td class="right">&nbsp;</td>
                    </tr>
                    <tr valign="top">
                      <td align="center" class="left">&nbsp;</td>
                      <td><strong>B/W</strong></td>
                      <td><input name='b_w' type='text' id='b_w'  size="15" value="<%if(cdrt.getBw() != null) {out.print(cdrt.getBw());}%>" readonly></td>
                      <td class="right">&nbsp;</td>
                    </tr>
                    <tr valign="top">
                      <td align="center" class="left">&nbsp;</td>
                      <td><strong>Colour</strong></td>
                      <td><input name='colour' type='text' id='colour'  size="15" value="<%if(cdrt.getColour() != null) {out.print(cdrt.getColour());}%>" readonly></td>
                      <td class="right">&nbsp;</td>
                    </tr>
                    <tr valign="top">
                      <td width="17" align="center" class="left">&nbsp;</td>
                      <td><strong>Sound Rolls</strong> </td>
                      <td><input name='sound_rolls' type='text' id='sound_rolls'  size="15" value="<%= cdrt.getSoundRollsDatTapes()%>" readonly></td>
                      <td width="17" class="right">&nbsp;</td>
                    </tr>
					<tr><td width="17" align="center" class="left">&nbsp;</td><td/><td/><td width="17" class="right">&nbsp;</td></tr>
                  </tbody>
                  <tr valign="top">
                    <th colspan="4" class="bottom">&nbsp;</th>
                  </tr>
              </table></td>
		  </tr>
			<%-- slate no --%>
			<tr>
				<td>
					<table width="700" border="0" cellpadding="0" cellspacing="0" id="slate_no">
						<tbody>
							<tr valign="top"><th width="17" class="header-left">&nbsp;</th>
							  <td colspan="3" class="header-middle">Slate No. </td>
							  <th width="17" class="header-right">&nbsp;</th>
							</tr>
						<tr valign="top">
						  <th class="left">&nbsp;</th>
						  <td><strong>Unit</strong></td>
						  <td><strong>Slates</strong></td>
						  <th>&nbsp;</th>
						  <th class="right">&nbsp;</th>
					  </tr>
					  <%int c=0;
							SlateNosType sn = cdrt.getSlateNos();
							for(SingleUnitType su: sn.getSingleUnit()){
								c++;
							%>
						<tr valign="top">
						  <th class="left">&nbsp;</th>
						  <td width="121"><strong>
						    <input name='unit<%=c%>_name' type='text' id='unit<%=c%>_name' value="<%=su.getUnit()%>"  size="15" readonly>
						  </strong></td>
						  <td width="450" id="unit<%=c%>">
						  <% int d=0;
							List<String> slate_list = su.getSlate();
								for(String slate : slate_list) {
									d ++;%>
						  <input name='unit<%=c%>_<%=d%>' type='text' id='unit<%=c%>_<%=d%>' size="2" value="<%=slate%>" readonly>
						  <%}%>
						  </td>
						  <td width="95">
						  <input type="hidden" name="unit<%=c%>_count" id="unit<%=c%>_count" value="<%=d%>"/></td>
						  <th class="right">&nbsp;</th>
					  </tr>
					  <%}%>
					  </tbody>
						<tr valign="top">
						  <th class="left">&nbsp;</th>
					      <td colspan="3"></td>
					      <th class="right">&nbsp;</th>
					  </tr>
						<tr valign="top"><th colspan="5" class="bottom">&nbsp;</th></tr>
				  </table>
				</td>
			</tr>
			<%-- scene schedule --%>
			<tr>
				<td>
				<table width="700" border="0" cellpadding="0" cellspacing="0" id="scene_schedule">
				  <tbody>
					<tr valign="top">
					  <th class="header-left">&nbsp;</th>
					  <td colspan="3" class="header-middle">Scene Schedule</td>
					  <th class="header-right">&nbsp;</th>
					</tr>
					<tr valign="top">
					  <td class="left">&nbsp;</td>
					  <td><strong>Scheduled Scenes</strong></td>
					  <td id="scheduled_scenes">
					  <% ScenesType s1 = cdrt.getScheduledScenes();
					  int s_1=0;
					  if(s1.getScene() != null) {
							List<String> scenes_list1 = s1.getScene();
								for(String scene1 : scenes_list1) {
									s_1 ++;
					  %>
					  <input name='scheduled_scenes_<%=s_1%>' type='text' id='scheduled_scenes_<%=s_1%>'  size="2" value="<%=scene1 %>" readonly>
					  <%}
					  }else{%>
					  <input name='scheduled_scenes_1' type='text' id='scheduled_scenes_1'  size="2" readonly>
					  <%}%>
					  </td>
					  <td></td>
					  <td class="right">&nbsp;</td>
				    </tr>
					<tr valign="top">
					  <td class="left">&nbsp;</td>
					  <td><strong>Scenes Shot </strong></td>
					  <td id="scenes_shot">
					  <% ScenesType s2 = cdrt.getScenesShot();
					  int s_2=0;
					  if(s2.getScene() != null) {
							List<String> scenes_list2 = s2.getScene();
								for(String scene2 : scenes_list2) {
									s_2 ++;
					  %>
					  <input name='scenes_shot_<%=s_2%>' type='text' id='scenes_shot_<%=s_2%>' size="2" value="<%=scene2 %>" readonly>
					   <%}
					  }else{%>
					  <input name='scenes_shot_1' type='text' id='scenes_shot_1'  size="2" readonly>
					  <%}%>
					  </td>
					  <td></td>
					  <td class="right">&nbsp;</td>
				    </tr>
					<tr valign="top">
					  <td height="10" class="left">&nbsp;</td>
					  <td height="10" colspan="3"><hr></td>
					  <td height="10" class="right">&nbsp;</td>
				    </tr>
					<tr valign="top">
					  <td width="15" class="left">&nbsp;</td>
					  <td width="204"><strong>Scheduled Scenes Shot </strong></td>
					  <td width="371" id="scheduled_scenes_shot">
					  <% ScenesType s3 = cdrt.getScheduledScenesShot();
					  int s_3=0;
					  if(s3.getScene() != null) {
							List<String> scenes_list3 = s3.getScene();
								for(String scene3 : scenes_list3) {
									s_3 ++;
					  %>
					  <input name='scheduled_scenes_shot_<%=s_3%>' type='text' id='scheduled_scenes_shot_<%=s_3%>'  size="2" value="<%=scene3 %>" readonly>
					   <%}
					  }else{%>
					  <input name='scheduled_scenes_shot_1' type='text' id='scheduled_scenes_shot_1'  size="2" readonly>
					  <%}%>
					  </td>
					  <td width="95">
					  </td>
					  <td width="15" class="right">&nbsp;</td>
					</tr>
					<tr valign="top">
					  <td align="center" class="left">&nbsp;</td>
					  <td><strong>Scenes Scheduled &amp; Not Shot</strong> </td>
					  <td width="371" id="scenes_scheduled_not_shot">
					  <% ScenesType s4 = cdrt.getScheduledScenesNotShot();
					  int s_4=0;
					  if(s4.getScene() != null) {
							List<String> scenes_list4 = s4.getScene();
								for(String scene4 : scenes_list4) {
									s_4 ++;
					  %>
					  <input name='scenes_scheduled_not_shot_<%=s_4%>' type='text' id='scenes_scheduled_not_shot_<%=s_4%>'  size="2" value="<%=scene4 %>">
					   <%}
					  }else{%>
					  <input name='scenes_scheduled_not_shot_1' type='text' id='scenes_scheduled_not_shot_1'  size="2">
					  <%}%>
					  </td>
					  <td><input name="button2" type="button" onClick="addScenes('scenes_scheduled_not_shot');" value="Insert Scene"/></td>
					  <td class="right">&nbsp;</td>
					</tr>
					<tr valign="top">
					  <td align="center" class="left">&nbsp;</td>
					  <td><strong>Scenes Not Yet Completed </strong></td>
					  <td width="371" id="scenes_not_yet_completed">
					  <% ScenesType s5 = cdrt.getScenesNotYetCompleted();
					  int s_5 =0;
					  if(s5.getScene() != null) {
							List<String> scenes_list5 = s5.getScene();
								for(String scene5 : scenes_list5) {
									s_5 ++;
					  %>
					  <input name='scenes_not_yet_completed_<%=s_5%>' type='text' id='scenes_not_yet_completed_<%=s_5%>'  size="2" value="<%=scene5 %>">
					   <%}
					  }else{%>
					  <input name='scenes_not_yet_completed_1' type='text' id='scenes_not_yet_completed_1'  size="2">
					  <%}%>
					  </td>
					  <td><input name="button3" type="button" onClick="addScenes('scenes_not_yet_completed');" value="Insert Scene"/></td>
					  <td class="right">&nbsp;</td>
					</tr>
					<tr valign="top">
					  <td align="center" class="left">&nbsp;</td>
					  <td><strong>Scenes Deleted </strong></td>
					  <td width="371" id="scenes_deleted">
					  <% ScenesType s6 = cdrt.getScenesDeleted();
					  int s_6=0;
					  if(s6.getScene() != null) {
							List<String> scenes_list6 = s6.getScene();
								for(String scene6 : scenes_list6) {
									s_6 ++;
					  %>
					  <input name='scenes_deleted_<%=s_6%>' type='text' id='scenes_deleted_<%=s_6%>'  size="2" value="<%=scene6 %>">
					   <%}
					  }else{%>
					  <input name='scenes_deleted_1' type='text' id='scenes_deleted_1'  size="2">
					  <%}%>
					  </td>
					  <td><input name="button4" type="button" onClick="addScenes('scenes_deleted');" value="Insert Scene"/></td>
					  <td class="right">&nbsp;</td>
					</tr>
					<tr valign="top">
					  <td align="center" class="left">&nbsp;</td>
					  <td><strong>Scenes Added </strong></td>
					  <td width="371" id="scenes_added">
					  <% ScenesType s7 = cdrt.getScenesAdded();
					  int s_7=0;
					  if(s7.getScene() != null) {
							List<String> scenes_list7 = s7.getScene();
								for(String scene7 : scenes_list7) {
									s_7 ++;
					  %>
					  <input name='scenes_added_<%=s_7%>' type='text' id='scenes_added_<%=s_7%>'  size="2" value="<%=scene7 %>">
					   <%}
					  }else{%>
					  <input name='scenes_added_1' type='text' id='scenes_added_1'  size="2">
					  <%}%>
					  </td>
					  <td><input name="button5" type="button" onClick="addScenes('scenes_added');" value="Insert Scene"/></td>
					  <td class="right">&nbsp;</td>
					</tr>
					<tr valign="top">
					  <td width="15" align="center" class="left">&nbsp;</td>
					  <td><strong>Unscheduled Scenes Shot </strong></td>
					  <td width="371" id="unscheduled_scenes_shot">
					  <% ScenesType s8 = cdrt.getUnscheduledScenesShot();
					  int s_8=0;
					  if(s8.getScene() != null) {
							List<String> scenes_list8 = s8.getScene();
								for(String scene8 : scenes_list8) {
									s_8 ++;
					  %>
					  <input name='unscheduled_scenes_shot_<%=s_8%>' type='text' id='unscheduled_scenes_shot_<%=s_8%>'  size="2" value="<%=scene8 %>">
					   <%}
					  }else{%>
					  <input name='unscheduled_scenes_shot_1' type='text' id='unscheduled_scenes_shot_1'  size="2">
					  <%}%>
					  </td>
					  <td><input name="button6" type="button" onClick="addScenes('unscheduled_scenes_shot');" value="Insert Scene"/></td>
					  <td width="15" class="right">&nbsp;</td>
					</tr>
				  </tbody>
				  <tr valign="top">
					<th colspan="6" class="bottom">&nbsp;</th>
				  </tr>
				</table>
			</td>
		</tr>
			<%-- scene timing --%>
			<tr>
				<td>
				<table width="700" border="0" cellpadding="0" cellspacing="0" id="scene_timing">
				  <tbody>
					<tr valign="top">
					  <th class="header-left">&nbsp;</th>
					  <td colspan="6" class="header-middle">Scene Timing </td>
					  <th width="15" class="header-right">&nbsp;</th>
					</tr>
					<tr valign="top">
					  <td width="15" class="left">&nbsp;</td>
					  <td width="90" align="center"><strong>Sc No.</strong></td>
					  <td width="124" align="center"><strong>Page Count </strong></td>
					  <td width="109" align="center"><strong>Estimated Timing</strong></td>
					  <td width="109" align="center"><strong>Actual Timing</strong></td>
					  <td width="124" align="center"><strong>Variance</strong></td>
					  <td width="114" align="center"><strong>Total Cumulative Running </strong></td>
					  <td width="15" class="right">&nbsp;</td>
					</tr>
					<%ScenesTimingType st = cdrt.getScenesTiming();
					int e = 0;
					for(SingleSceneTimingType sst : st.getSingleSceneTiming()) {
						e++;%>
					<tr valign="top">
					  <td align="center" class="left">&nbsp;</td>
					  <td width="90" align="center"><input name='scene_no_<%=e%>' type='text' id='scene_no_<%=e%>'  size="8" value="<%=sst.getScene()%>"></td>
					  <td width="124" align="center">
					  <% PageTimeType  pt1 = sst.getPageTime(); %>
						<input name='page_count_<%=e%>' type='text' id='page_count_<%=e%>'  size="4" value="<%=pt1.getNumber()%>">&nbsp;
						<input name='page_count_num_<%=e%>' type='text' id='page_count_num_<%=e%>'  size="2" value="<%=pt1.getNumerator()%>"> 
						/8
					  </td>
					  <td width="109" align="center"><input name='est_timing_<%=e%>' type='text' id='est_timing_<%=e%>'  size="8" value="<%=sst.getEstTiming()%>"></td>
					  <td width="109" align="center"><input name='actual_timing_<%=e%>' type='text' id='actual_timing_<%=e%>'  size="8" value="<%=sst.getActualTiming()%>"></td>
					  <td width="124" align="center">
					  <% if(sst.getVariance() != null) {
					  VarianceType  v = sst.getVariance(); %>
					  <select name="variance_sign_<%=e%>" id="variance_sign_<%=e%>" disabled>
						<option value="+" <%if(v.isSign() == true) {out.print("selected");}%>>+</option>
						<option value="-" <%if(v.isSign() == false) {out.print("selected");}%>>-</option>
					  </select>&nbsp;				  
					  <input name='variance_<%=e%>' type='text' id='variance_<%=e%>'  size="8" value="<%=v.getVarTime()%>" readonly>
					  <% }else{%>
					  <select name="variance_sign_<%=e%>" id="variance_sign_<%=e%>" disabled>
						<option value="+" >+</option>
						<option value="-" >-</option>
					  </select>&nbsp;				  
					  <input name='variance_<%=e%>' type='text' id='variance_<%=e%>'  size="8" value="" readonly>
					  <% }%>
					  </td>
					  <td width="114" align="center">
					  <input name='cumulative_running_<%=e%>' type='text' id='cumulative_running_<%=e%>'  size="8" value="<% if(sst.getCumulative()!= null) {out.print(sst.getCumulative());}%>" readonly>
					  </td>
					  <td width="15" class="right">&nbsp;</td>
					</tr>
					<%}%>
				  </tbody>
				  <%
					TotalScenesTimingType tst = st.getTotalScenesTiming();
					if(st.getTotalScenesTiming()!= null){%>
					<tr valign="top">
					  <td width="15" align="center" class="left">&nbsp;</td>
					  <td width="90" align="center" class="bottom"><input name="button7" type="button" onClick="calculateTotal();" value="Total"/></td>
					  <td width="124" align="center" class="bottom">
					  <% PageTimeType  pt2 = tst.getPageTime(); %>
						<input name='total_page_count' type='text' id='total_page_count'  size="4"  value="<%= pt2.getNumber()%>" readonly>&nbsp;
						<input name='total_page_count_num' type='text' id='total_page_count_num'  size="2" value="<%= pt2.getNumerator()%>" readonly>
	/8</td>
					  <td width="109" align="center" class="bottom"><input name='total_est_timing' type='text' id='total_est_timing'  size="8" value="<%= tst.getEstTiming()%>" readonly></td>
					  <td width="109" align="center" class="bottom"><input name='total_actual_timing' type='text' id='total_actual_timing'  size="8" value="<%= tst.getActualTiming()%>" readonly></td>
					  <% VarianceType  v2 = tst.getVariance(); %>
					  <td width="124" align="center" class="bottom"><select name="total_variance_sign" id="total_variance_sign" disabled>
                        <option value="+" <%if(v2.isSign() == true) {out.print("selected");}%>>+</option>
                        <option value="-" <%if(v2.isSign() == true) {out.print("selected");}%>>-</option>
                      </select>&nbsp; <input name='total_variance' type='text' id='total_variance'  size="8" value="<%=v2.getVarTime()%>" readonly> </td>
					  <td width="114" align="center" class="bottom"><input name='total_cumulative_running' type='text' id='total_cumulative_running'  size="8" value="<%= tst.getCumulative()%>" readonly></td>
					  <td width="15" class="right">&nbsp;</td>
					</tr>
				  <%}else{%>
				  <tr valign="top">
					  <td width="15" align="center" class="left">&nbsp;</td>
					  <td width="90" align="center" class="bottom"><input name="button7" type="button" onClick="calculateTotal();" value="Total"/></td>
					  <td width="124" align="center" class="bottom">
						<input name='total_page_count' type='text' id='total_page_count'  size="4" readonly>&nbsp;
						<input name='total_page_count_num' type='text' id='total_page_count_num'  size="2" readonly>
	/8</td>
					  <td width="109" align="center" class="bottom"><input name='total_est_timing' type='text' id='total_est_timing'  size="8" readonly></td>
					  <td width="109" align="center" class="bottom"><input name='total_actual_timing' type='text' id='total_actual_timing'  size="8" readonly></td>
					  <td width="124" align="center" class="bottom"><select name="total_variance_sign" id="total_variance_sign" disabled>
                        <option value="+">+</option>
                        <option value="-">-</option>
                      </select>&nbsp; <input name='total_variance' type='text' id='total_variance'  size="8" readonly> </td>
					  <td width="114" align="center" class="bottom"><input name='total_cumulative_running' type='text' id='total_cumulative_running'  size="8" readonly></td>
					  <td width="15" class="right">&nbsp;</td>
					</tr>
				  <%}%>
				  <tr valign="top">
					<th class="left">&nbsp;</th>
					<td colspan="6"></td>
					<th width="15" class="right">&nbsp;</th>
				  </tr>
				  <tr valign="top">
					<th colspan="8" class="bottom">&nbsp;</th>
				  </tr>
				</table>
			</td>
		</tr>
			<%-- script timing --%>
			<tr><td>
			<table width="700" border="0" cellpadding="0" cellspacing="0" id="script_timing">
              <tbody>
                <tr valign="top">
                  <th class="header-left">&nbsp;</th>
                  <td colspan="5" class="header-middle">Script Timing </td>
                  <th class="header-right">&nbsp;</th>
                </tr>
                <tr valign="top">
                  <td class="left">&nbsp;</td>
                  <td>&nbsp;</td>
                  <td><strong>Scenes</strong></td>
                  <td><strong>Page Count </strong></td>
                  <td><strong>Est. Timing </strong></td>
                  <td><strong>Act. Timing </strong></td>
                  <td class="right">&nbsp;</td>
                </tr>
				<% ScriptTimingType sct = cdrt.getScriptTiming();
				SingleScriptTimingType ssct1 = sct.getPrevShot();
				SingleScriptTimingType ssct2 = sct.getShotToday();
				SingleScriptTimingType ssct3 = sct.getShotToDate();
				SingleScriptTimingType ssct4 = sct.getToBeShot();
				SingleScriptTimingType ssct5 = sct.getTotal();
				
				%>
                <tr valign="top">
                  <td width="15" class="left">&nbsp;</td>
                  <td><strong>Prev. Shot </strong></td>
                  <td><input name='prev_shot_scenes' type='text' id='prev_shot_scenes' size="15" value="<%if(ssct1.getScenes() != null) {out.print(ssct1.getScenes());}%>" readonly></td>
                  <td>
				  <% if (ssct1.getPageTime() != null) {
				  	PageTimeType  pt2 = ssct1.getPageTime(); 
				  	%>
				  	<input name='prev_shot_pages' type='text' id='prev_shot_pages' size="4" value="<%=pt2.getNumber()%>" readonly>
					&nbsp;
					<input name='prev_shot_pagesnum' type='text' id='prev_shot_pagesnum' size="2" value="<%=pt2.getNumerator()%>" readonly>/8
				  	<%}else{%>
					<input name='prev_shot_pages' type='text' id='prev_shot_pages' size="4" value="" readonly>
					&nbsp;
					<input name='prev_shot_pagesnum' type='text' id='prev_shot_pagesnum' size="2" value="" readonly> /8
					<%}%>
				  </td>
                  <td><input name='prev_shot_esttiming' type='text' id='prev_shot_esttiming'  size="15" value="<%if(ssct1.getEstTiming() != null) {out.print(ssct1.getEstTiming());}%>" readonly></td>
                  <td><input name='prev_shot_acttiming' type='text' id='prev_shot_acttiming'  size="15" value="<%if(ssct1.getActualTiming() != null) {out.print(ssct1.getActualTiming());}%>" readonly></td>
                  <td width="15" class="right">&nbsp;</td>
                </tr>
                <tr valign="top">
                  <td align="center" class="left">&nbsp;</td>
                  <td><strong>Shot Today </strong></td>
                  <td><input name='shot_today_scenes' type='text' id='shot_today_scenes' value="<%if(ssct2.getScenes() != null) {out.print(ssct2.getScenes());}%>"  size="15" readonly></td>
                  <td>
				  <% if (ssct2.getPageTime() != null) {
				  	PageTimeType  pt3 = ssct2.getPageTime(); 
				  	%>
				  	<input name='shot_today_pages' type='text' id='shot_today_pages' size="4" value="<%=pt3.getNumber()%>" readonly>
					&nbsp;
					<input name='shot_today_pagesnum' type='text' id='shot_today_pagesnum' size="2" value="<%=pt3.getNumerator()%>" readonly>/8
				  	<%}else{%>
					<input name='shot_today_pages' type='text' id='shot_today_pages' size="4" value="" readonly>
					&nbsp;
					<input name='shot_today_pagesnum' type='text' id='shot_today_pagesnum' size="2" value="" readonly> /8
					<%}%>
				  </td>
                  <td><input name='shot_today_esttiming' type='text' id='shot_today_esttiming'  size="15" value="<%if(ssct2.getEstTiming() != null) {out.print(ssct2.getEstTiming());}%>" readonly></td>
                  <td><input name='shot_today_acttiming' type='text' id='shot_today_acttiming'  size="15" value="<%if(ssct2.getActualTiming() != null) {out.print(ssct2.getActualTiming());}%>" readonly></td>
                  <td class="right">&nbsp;</td>
                </tr>
                <tr valign="top">
                  <td align="center" class="left">&nbsp;</td>
                  <td><strong>Shot to Date </strong></td>
                  <td><input name='shot_to_date_scenes' type='text' id='shot_to_date_scenes' value="<%if(ssct3.getScenes() != null) {out.print(ssct3.getScenes());}%>" size="15" readonly></td>
                  <td>
				  <% if (ssct3.getPageTime() != null) {
				  	PageTimeType  pt4 = ssct3.getPageTime(); 
				  	%>
				  	<input name='shot_to_date_pages' type='text' id='shot_to_date_pages' size="4" value="<%=pt4.getNumber()%>" readonly>
					&nbsp;
					<input name='shot_to_date_pagesnum' type='text' id='shot_to_date_pagesnum' size="2" value="<%=pt4.getNumerator()%>" readonly>/8
				  	<%}else{%>
					<input name='shot_to_date_pages' type='text' id='shot_to_date_pages' size="4" value="" readonly>
					&nbsp;
					<input name='shot_to_date_pagesnum' type='text' id='shot_to_date_pagesnum' size="2" value="" readonly> /8
					<%}%>
				  </td>
                  <td><input name='shot_to_date_esttiming' type='text' id='shot_to_date_esttiming'  size="15" value="<%if(ssct3.getEstTiming() != null) {out.print(ssct3.getEstTiming());}%>" readonly></td>
                  <td><input name='shot_to_date_acttiming' type='text' id='shot_to_date_acttiming'  size="15" value="<%if(ssct3.getActualTiming() != null) {out.print(ssct3.getActualTiming());}%>" readonly></td>
                  <td class="right">&nbsp;</td>
                </tr>
                <tr valign="top">
                  <td align="center" class="left">&nbsp;</td>
                  <td><strong>Remaining</strong></td>
                  <td><input name='remaining_scenes' type='text' id='remaining_scenes' value="<%if(ssct4.getScenes() != null) {out.print(ssct4.getScenes());}%>"  size="15" readonly></td>
                  <td>
				  <% if (ssct4.getPageTime() != null) {
				  	PageTimeType  pt5 = ssct4.getPageTime(); 
				  	%>
				  	<input name='remaining_pages' type='text' id='remaining_pages' size="4" value="<%=pt5.getNumber()%>" readonly>
					&nbsp;
					<input name='remaining_pagesnum' type='text' id='remaining_pagesnum' size="2" value="<%=pt5.getNumerator()%>" readonly>/8
				  	<%}else{%>
					<input name='remaining_pages' type='text' id='remaining_pages' size="4" value="" readonly>
					&nbsp;
					<input name='remaining_pagesnum' type='text' id='remaining_pagesnum' size="2" value="" readonly> /8
					<%}%>
				  </td>
                  <td><input name='remaining_esttiming' type='text' id='remaining_esttiming'  size="15" value="<%if(ssct4.getEstTiming() != null) {out.print(ssct4.getEstTiming());}%>" readonly></td>
                  <td><input name='remaining_acttiming' type='text' id='remaining_acttiming'  size="15" value="<%if(ssct4.getActualTiming() != null) {out.print(ssct4.getActualTiming());}%>" readonly></td>
                  <td class="right">&nbsp;</td>
                </tr>
                <tr valign="top">
                  <td width="15" align="center" class="left">&nbsp;</td>
                  <td><strong>Total</strong></td>
                  <td><input name='total_scenes' type='text' id='total_scenes' value="<%if(ssct5.getScenes() != null) {out.print(ssct5.getScenes());}%>"  size="15" readonly></td>
                  <td>
				  <% if (ssct5.getPageTime() != null) {
				  	PageTimeType  pt6 = ssct5.getPageTime(); 
				  	%>
				  	<input name='total_pages' type='text' id='total_pages' size="4" value="<%=pt6.getNumber()%>" readonly>
					&nbsp;
					<input name='total_pagesnum' type='text' id='total_pagesnum' size="2" value="<%=pt6.getNumerator()%>" readonly>/8
				  	<%}else{%>
					<input name='total_pages' type='text' id='total_pages' size="4" value="" readonly>
					&nbsp;
					<input name='total_pagesnum' type='text' id='total_pagesnum' size="2" value="" readonly> /8
					<%}%>
				  </td>
                  <td><input name='total_esttiming' type='text' id='total_esttiming'  size="15" value="<%if(ssct5.getEstTiming() != null) {out.print(ssct5.getEstTiming());}%>" readonly></td>
                  <td><input name='total_acttiming' type='text' id='total_acttiming'  size="15" value="<%if(ssct5.getActualTiming() != null) {out.print(ssct5.getActualTiming());}%>" readonly></td>
                  <td width="15" class="right">&nbsp;</td>
                </tr>
              </tbody>
              <tr valign="top">
                <th class="left">&nbsp;</th>
                <td colspan="5">                  <input name="button72" type="button" onClick="calculateScriptTiming();" value="Calculate"/>                </td>
                <th class="right">&nbsp;</th>
              </tr>
              <tr valign="top">
                <th colspan="7" class="bottom">&nbsp;</th>
              </tr>
            </table></td></tr>
			<%-- remarks --%>
			<tr><td>
			<table width="700" border="0" cellpadding="0" cellspacing="0" id="table1">
              <tbody>
                <tr valign="top">
                  <th width="15" class="header-left">&nbsp;</th>
                  <td class="header-middle">Remarks</td>
                  <th width="15" class="header-right">&nbsp;</th>
                </tr>
                <tr valign="top">
                  <td class="left">&nbsp;</td>
                  <td align="center"><textarea name="remarks" id="remarks" cols="80"><%if(cdrt.getRemarks() != null) {out.print(cdrt.getRemarks());}%></textarea></td>
                  <td class="right">&nbsp;</td>
                </tr>
              </tbody>
              <tr valign="top">
                <th colspan="3" class="bottom">&nbsp;</th>
              </tr>
            </table></td></tr>
			
			<tr><td>
				<input type="hidden" name="count" id="count" value="1"/>
				<input type="hidden" name="location_count" id="location_count" value="<%if (a==0) {out.print("1");}else{out.print(a);}%>"/>
				<input type="hidden" name="unit_count" id="unit_count" value="<%if (c==0) {out.print("1");}else{out.print(c);}%>"/>
				<input type="hidden" name="scene_timing_count" id="scene_timing_count" value="<%if (e==0) {out.print("1");}else{out.print(e);}%>"/>
				<input type="hidden" name="scheduled_scenes_count" id="scheduled_scenes_count" value="<%if (s_1==0) {out.print("1");}else{out.print(s_1);}%>"/>
				<input type="hidden" name="scenes_shot_count" id="scenes_shot_count" value="<%if (s_2==0) {out.print("1");}else{out.print(s_2);}%>"/>
				<input type="hidden" name="scheduled_scenes_shot_count" id="scheduled_scenes_shot_count" value="<%if (s_3==0) {out.print("1");}else{out.print(s_3);}%>"/>
				<input type="hidden" name="scenes_scheduled_not_shot_count" id="scenes_scheduled_not_shot_count" value="<%if (s_4==0) {out.print("1");}else{out.print(s_4);}%>"/>
				<input type="hidden" name="scenes_not_yet_completed_count" id="scenes_not_yet_completed_count" value="<%if (s_5==0) {out.print("1");}else{out.print(s_5);}%>"/>
				<input type="hidden" name="scenes_deleted_count" id="scenes_deleted_count" value="<%if (s_6==0) {out.print("1");}else{out.print(s_6);}%>"/>
				<input type="hidden" name="scenes_added_count" id="scenes_added_count" value="<%if (s_7==0) {out.print("1");}else{out.print(s_7);}%>"/>
				<input type="hidden" name="unscheduled_scenes_shot_count" id="unscheduled_scenes_shot_count" value="<%if (s_8==0) {out.print("1");}else{out.print(s_8);}%>"/>
				<input type="hidden" name="workItemID" id="workItemID"/>
				<input type="hidden" name="userID" id="userID"/>
				<input type="hidden" name="sessionHandle" id="sessionHandle"/>
				<input type="hidden" name="JSESSIONID" id="JSESSIONID"/>
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
	ContinuityDailyReportType cdrt1 = new ContinuityDailyReportType();
	//generalinfo
	gi.setProduction(request.getParameter("production"));
	gi.setDate(XMLGregorianCalendarImpl.parse(request.getParameter("date")));
	gi.setWeekday(request.getParameter("weekday"));
	gi.setShootDayNo(new BigInteger(request.getParameter("shoot_day")));
	
	//Locations
	int location_count = Integer.parseInt(request.getParameter("location_count"));
	LocationSetsType lst1 = new LocationSetsType();
	for(int current_location=1; current_location<=location_count; current_location ++) {
		SingleLocationType slt1 = new SingleLocationType();
		slt1.setLocationName(request.getParameter("location_name_" + current_location));
		slt1.setAddress(request.getParameter("location_address_" + current_location));
		int set_count = Integer.parseInt(request.getParameter("location_set_"+ current_location +"_count"));
		for(int current_set=1; current_set<=set_count; current_set ++) {
			slt1.getSet().add(request.getParameter("location_set_" + current_location + "_" + current_set));
		}
		lst1.getSingleLocation().add(slt1);
	}
	cdrt1.setLocationSets(lst1);
	
	//Tape Info
	if(!(request.getParameter("stills").equals(""))){
		cdrt1.setStills(request.getParameter("stills"));
	}
	if(!(request.getParameter("b_w").equals(""))){
		cdrt1.setBw(request.getParameter("b_w"));
	}
	if(!(request.getParameter("colour").equals(""))){
		cdrt1.setColour(request.getParameter("colour"));
	}
	cdrt1.setSoundRollsDatTapes(request.getParameter("sound_rolls"));
	
	//Slate No
	int unit_count = Integer.parseInt(request.getParameter("unit_count"));
	SlateNosType snt1 = new SlateNosType();
	for(int current_unit=1; current_unit<=unit_count; current_unit ++) {	
		SingleUnitType sut1 = new SingleUnitType();
		sut1.setUnit(request.getParameter("unit" + current_unit + "_name"));
		int slate_count = Integer.parseInt(request.getParameter("unit"+ current_unit +"_count"));
		for(int current_slate=1; current_slate<=slate_count; current_slate ++) {
			sut1.getSlate().add(request.getParameter("unit" + current_slate+ "_" + current_slate));
		}
		snt1.getSingleUnit().add(sut1);
	}
	cdrt1.setSlateNos(snt1);

	//Scene Schedule - Scheduled Scenes
	int scheduled_scenes_count = Integer.parseInt(request.getParameter("scheduled_scenes_count"));
	ScenesType st1 = new ScenesType();
	for(int current_scheduled_scenes=1; current_scheduled_scenes<=scheduled_scenes_count; current_scheduled_scenes ++) {
		if(!(request.getParameter("scheduled_scenes_" + current_scheduled_scenes).equals(""))){
			st1.getScene().add(request.getParameter("scheduled_scenes_" + current_scheduled_scenes));
		}
	}
	cdrt1.setScheduledScenes(st1);
	
	//Scene Schedule - Scenes Shot
	int scenes_shot_count = Integer.parseInt(request.getParameter("scenes_shot_count"));
	ScenesType st2 = new ScenesType();
	for(int current_scenes_shot=1; current_scenes_shot<=scenes_shot_count; current_scenes_shot ++) {
		if(!(request.getParameter("scenes_shot_" + current_scenes_shot).equals(""))){
			st2.getScene().add(request.getParameter("scenes_shot_" + current_scenes_shot));
		}
	}
	cdrt1.setScenesShot(st2);
	
	//Scene Schedule - Scheduled Scenes Shot
	int scheduled_scenes_shot_count = Integer.parseInt(request.getParameter("scheduled_scenes_shot_count"));
	ScenesType st3 = new ScenesType();
	for(int current_scheduled_scenes_shot=1; current_scheduled_scenes_shot<=scheduled_scenes_shot_count; current_scheduled_scenes_shot ++) {
		if(!(request.getParameter("scheduled_scenes_shot_" + current_scheduled_scenes_shot).equals(""))){
			st3.getScene().add(request.getParameter("scheduled_scenes_shot_" + current_scheduled_scenes_shot));
		}
	}
	cdrt1.setScheduledScenesShot(st3);
	
	//Scene Schedule - Scheduled Scenes Not Shot
	int scheduled_scenes_not_shot_count = Integer.parseInt(request.getParameter("scenes_scheduled_not_shot_count"));
	ScenesType st4 = new ScenesType();
	for(int current_scheduled_scenes_not_shot=1; current_scheduled_scenes_not_shot<=scheduled_scenes_not_shot_count; current_scheduled_scenes_not_shot ++) {
		if(!(request.getParameter("scenes_scheduled_not_shot_" + current_scheduled_scenes_not_shot).equals(""))){
			st4.getScene().add(request.getParameter("scenes_scheduled_not_shot_" + current_scheduled_scenes_not_shot));
		}
	}
	cdrt1.setScheduledScenesNotShot(st4);
	
	//Scene Schedule - Scenes Not Yet Completed
	int scenes_not_yet_completed_count = Integer.parseInt(request.getParameter("scenes_not_yet_completed_count"));
	ScenesType st5 = new ScenesType();
	for(int current_scenes_not_yet_completed=1; current_scenes_not_yet_completed<=scenes_not_yet_completed_count; current_scenes_not_yet_completed ++) {
		if(!(request.getParameter("scenes_not_yet_completed_" + current_scenes_not_yet_completed).equals(""))){
			st5.getScene().add(request.getParameter("scenes_not_yet_completed_" + current_scenes_not_yet_completed));
		}
	}
	cdrt1.setScenesNotYetCompleted(st5);
	
	//Scene Schedule - Scenes Deleted
	int scenes_deleted_count = Integer.parseInt(request.getParameter("scenes_deleted_count"));
	ScenesType st6 = new ScenesType();
	for(int current_scenes_deleted=1; current_scenes_deleted<=scenes_deleted_count; current_scenes_deleted ++) {
		if(!(request.getParameter("scenes_deleted_" + current_scenes_deleted).equals(""))){
			st6.getScene().add(request.getParameter("scenes_deleted_" + current_scenes_deleted));
		}
	}
	cdrt1.setScenesDeleted(st6);
	
	//Scene Schedule - Scenes Added
	int scenes_added_count = Integer.parseInt(request.getParameter("scenes_added_count"));
	ScenesType st7 = new ScenesType();
	for(int current_scenes_added=1; current_scenes_added<=scenes_added_count; current_scenes_added ++) {
		if(!(request.getParameter("scenes_added_" + current_scenes_added).equals(""))){
			st7.getScene().add(request.getParameter("scenes_added_" + current_scenes_added));
		}
	}
	cdrt1.setScenesAdded(st7);
	
	//Scene Schedule - Unscheduled Scenes Shot
	int unscheduled_scenes_shot_count = Integer.parseInt(request.getParameter("unscheduled_scenes_shot_count"));
	ScenesType st8 = new ScenesType();
	for(int current_unscheduled_scenes_shot=1; current_unscheduled_scenes_shot<=unscheduled_scenes_shot_count; current_unscheduled_scenes_shot ++) {
		if(!(request.getParameter("unscheduled_scenes_shot_" + current_unscheduled_scenes_shot).equals(""))){
			st8.getScene().add(request.getParameter("unscheduled_scenes_shot_" + current_unscheduled_scenes_shot));
		}
	}
	cdrt1.setUnscheduledScenesShot(st8);
	
	//Scene Timing
	ScenesTimingType stt1 = new ScenesTimingType();
	int scene_timing_count = Integer.parseInt(request.getParameter("scene_timing_count"));
	for(int current_scene_timing=1; current_scene_timing<=scene_timing_count; current_scene_timing ++) {
		SingleSceneTimingType sstt1 = new SingleSceneTimingType();
		sstt1.setScene(request.getParameter("scene_no_" + current_scene_timing));
		PageTimeType ptt1 = new PageTimeType();
		ptt1.setNumber(new BigInteger(request.getParameter("page_count_" + current_scene_timing)));
		ptt1.setNumerator(new BigInteger(request.getParameter("page_count_num_" + current_scene_timing)));
		sstt1.setPageTime(ptt1);
		sstt1.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("est_timing_" + current_scene_timing)));
		sstt1.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("actual_timing_" + current_scene_timing)));
		VarianceType vt1 = new VarianceType();
		if (request.getParameter("variance_sign_"+ current_scene_timing)== "+"){
			vt1.setSign(true);
		}else{
			vt1.setSign(false);
		}
		vt1.setVarTime(XMLGregorianCalendarImpl.parse(request.getParameter("variance_" + current_scene_timing)));
		sstt1.setVariance(vt1);
		sstt1.setCumulative(XMLGregorianCalendarImpl.parse(request.getParameter("cumulative_running_" + current_scene_timing)));
		stt1.getSingleSceneTiming().add(sstt1);
	}
	TotalScenesTimingType tstt1 = new TotalScenesTimingType();
	PageTimeType ptt2 = new PageTimeType();
	ptt2.setNumber(new BigInteger(request.getParameter("total_page_count")));
	ptt2.setNumerator(new BigInteger(request.getParameter("total_page_count_num")));
	tstt1.setPageTime(ptt2);
	tstt1.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_est_timing")));
	tstt1.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_actual_timing")));
	VarianceType vt2 = new VarianceType();
	if (request.getParameter("total_variance_sign")== "-"){
		vt2.setSign(false);
	}else{
		vt2.setSign(true);
	}
	vt2.setVarTime(XMLGregorianCalendarImpl.parse(request.getParameter("total_variance")));
	tstt1.setVariance(vt2);
	tstt1.setCumulative(XMLGregorianCalendarImpl.parse(request.getParameter("total_cumulative_running")));
	stt1.setTotalScenesTiming(tstt1);
	
	cdrt1.setScenesTiming(stt1);
	
	//Script Timing
	ScriptTimingType srtt1 = new ScriptTimingType();
	//Script Timing - Previous Shot
	SingleScriptTimingType ssrtt1 = new SingleScriptTimingType();
	ssrtt1.setScenes(new BigInteger(request.getParameter("prev_shot_scenes")));
	PageTimeType ptt3 = new PageTimeType();
	ptt3.setNumber(new BigInteger(request.getParameter("prev_shot_pages")));
	ptt3.setNumerator(new BigInteger(request.getParameter("prev_shot_pagesnum")));
	ssrtt1.setPageTime(ptt3);
	ssrtt1.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("prev_shot_esttiming")));
	ssrtt1.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("prev_shot_acttiming")));
	srtt1.setPrevShot(ssrtt1);
	//Script Timing - Shot Today
	SingleScriptTimingType ssrtt2 = new SingleScriptTimingType();
	ssrtt2.setScenes(new BigInteger(request.getParameter("shot_today_scenes")));
	PageTimeType ptt4 = new PageTimeType();
	ptt4.setNumber(new BigInteger(request.getParameter("shot_today_pages")));
	ptt4.setNumerator(new BigInteger(request.getParameter("shot_today_pagesnum")));
	ssrtt2.setPageTime(ptt4);
	ssrtt2.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_today_esttiming")));
	ssrtt2.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_today_acttiming")));
	srtt1.setShotToday(ssrtt2);
	//Script Timing - Shot To Date
	SingleScriptTimingType ssrtt3 = new SingleScriptTimingType();
	ssrtt3.setScenes(new BigInteger(request.getParameter("shot_to_date_scenes")));
	PageTimeType ptt5 = new PageTimeType();
	ptt5.setNumber(new BigInteger(request.getParameter("shot_to_date_pages")));
	ptt5.setNumerator(new BigInteger(request.getParameter("shot_to_date_pagesnum")));
	ssrtt3.setPageTime(ptt5);
	ssrtt3.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_to_date_esttiming")));
	ssrtt3.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_to_date_acttiming")));
	srtt1.setShotToDate(ssrtt3);
	//Script Timing - To Be Shot
	SingleScriptTimingType ssrtt4 = new SingleScriptTimingType();
	ssrtt4.setScenes(new BigInteger(request.getParameter("remaining_scenes")));
	PageTimeType ptt6 = new PageTimeType();
	ptt6.setNumber(new BigInteger(request.getParameter("remaining_pages")));
	ptt6.setNumerator(new BigInteger(request.getParameter("remaining_pagesnum")));
	ssrtt4.setPageTime(ptt6);
	ssrtt4.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("remaining_esttiming")));
	ssrtt4.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("remaining_acttiming")));
	srtt1.setPrevShot(ssrtt4);
	//Script Timing - Total
	SingleScriptTimingType ssrtt5 = new SingleScriptTimingType();
	ssrtt5.setScenes(new BigInteger(request.getParameter("total_scenes")));
	PageTimeType ptt7 = new PageTimeType();
	ptt7.setNumber(new BigInteger(request.getParameter("total_pages")));
	ptt7.setNumerator(new BigInteger(request.getParameter("total_pagesnum")));
	ssrtt5.setPageTime(ptt7);
	ssrtt5.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_esttiming")));
	ssrtt5.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_acttiming")));
	srtt1.setTotal(ssrtt5);
	cdrt1.setScriptTiming(srtt1);
	
	//Remarks
	if(!(request.getParameter("remarks").equals(""))){
		cdrt1.setRemarks(request.getParameter("remarks"));
	}
	
	focdr.setContinuityDailyReport(cdrt1);
	
	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    File f = new File("./backup/ContinuityDailyReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
    m.marshal( focdrElement,  f);//output to file
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(focdrElement, xmlOS);//out to ByteArray
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
	ContinuityDailyReportType cdrt1 = new ContinuityDailyReportType();
	//generalinfo
	gi.setProduction(request.getParameter("production"));
	gi.setDate(XMLGregorianCalendarImpl.parse(request.getParameter("date")));
	gi.setWeekday(request.getParameter("weekday"));
	gi.setShootDayNo(new BigInteger(request.getParameter("shoot_day")));
	
	//Locations
	int location_count = Integer.parseInt(request.getParameter("location_count"));
	LocationSetsType lst1 = new LocationSetsType();
	for(int current_location=1; current_location<=location_count; current_location ++) {
		SingleLocationType slt1 = new SingleLocationType();
		slt1.setLocationName(request.getParameter("location_name_" + current_location));
		slt1.setAddress(request.getParameter("location_address_" + current_location));
		int set_count = Integer.parseInt(request.getParameter("location_set_"+ current_location +"_count"));
		for(int current_set=1; current_set<=set_count; current_set ++) {
			slt1.getSet().add(request.getParameter("location_set_" + current_location + "_" + current_set));
		}
		lst1.getSingleLocation().add(slt1);
	}
	cdrt1.setLocationSets(lst1);
	
	//Tape Info
	if(!(request.getParameter("stills").equals(""))){
		cdrt1.setStills(request.getParameter("stills"));
	}
	if(!(request.getParameter("b_w").equals(""))){
		cdrt1.setBw(request.getParameter("b_w"));
	}
	if(!(request.getParameter("colour").equals(""))){
		cdrt1.setColour(request.getParameter("colour"));
	}
	cdrt1.setSoundRollsDatTapes(request.getParameter("sound_rolls"));
	
	//Slate No
	int unit_count = Integer.parseInt(request.getParameter("unit_count"));
	SlateNosType snt1 = new SlateNosType();
	for(int current_unit=1; current_unit<=unit_count; current_unit ++) {	
		SingleUnitType sut1 = new SingleUnitType();
		sut1.setUnit(request.getParameter("unit" + current_unit + "_name"));
		int slate_count = Integer.parseInt(request.getParameter("unit"+ current_unit +"_count"));
		for(int current_slate=1; current_slate<=slate_count; current_slate ++) {
			sut1.getSlate().add(request.getParameter("unit" + current_slate+ "_" + current_slate));
		}
		snt1.getSingleUnit().add(sut1);
	}
	cdrt1.setSlateNos(snt1);

	//Scene Schedule - Scheduled Scenes
	int scheduled_scenes_count = Integer.parseInt(request.getParameter("scheduled_scenes_count"));
	ScenesType st1 = new ScenesType();
	for(int current_scheduled_scenes=1; current_scheduled_scenes<=scheduled_scenes_count; current_scheduled_scenes ++) {
		if(!(request.getParameter("scheduled_scenes_" + current_scheduled_scenes).equals(""))){
			st1.getScene().add(request.getParameter("scheduled_scenes_" + current_scheduled_scenes));
		}
	}
	cdrt1.setScheduledScenes(st1);
	
	//Scene Schedule - Scenes Shot
	int scenes_shot_count = Integer.parseInt(request.getParameter("scenes_shot_count"));
	ScenesType st2 = new ScenesType();
	for(int current_scenes_shot=1; current_scenes_shot<=scenes_shot_count; current_scenes_shot ++) {
		if(!(request.getParameter("scenes_shot_" + current_scenes_shot).equals(""))){
			st2.getScene().add(request.getParameter("scenes_shot_" + current_scenes_shot));
		}
	}
	cdrt1.setScenesShot(st2);
	
	//Scene Schedule - Scheduled Scenes Shot
	int scheduled_scenes_shot_count = Integer.parseInt(request.getParameter("scheduled_scenes_shot_count"));
	ScenesType st3 = new ScenesType();
	for(int current_scheduled_scenes_shot=1; current_scheduled_scenes_shot<=scheduled_scenes_shot_count; current_scheduled_scenes_shot ++) {
		if(!(request.getParameter("scheduled_scenes_shot_" + current_scheduled_scenes_shot).equals(""))){
			st3.getScene().add(request.getParameter("scheduled_scenes_shot_" + current_scheduled_scenes_shot));
		}
	}
	cdrt1.setScheduledScenesShot(st3);
	
	//Scene Schedule - Scheduled Scenes Not Shot
	int scheduled_scenes_not_shot_count = Integer.parseInt(request.getParameter("scenes_scheduled_not_shot_count"));
	ScenesType st4 = new ScenesType();
	for(int current_scheduled_scenes_not_shot=1; current_scheduled_scenes_not_shot<=scheduled_scenes_not_shot_count; current_scheduled_scenes_not_shot ++) {
		if(!(request.getParameter("scenes_scheduled_not_shot_" + current_scheduled_scenes_not_shot).equals(""))){
			st4.getScene().add(request.getParameter("scenes_scheduled_not_shot_" + current_scheduled_scenes_not_shot));
		}
	}
	cdrt1.setScheduledScenesNotShot(st4);
	
	//Scene Schedule - Scenes Not Yet Completed
	int scenes_not_yet_completed_count = Integer.parseInt(request.getParameter("scenes_not_yet_completed_count"));
	ScenesType st5 = new ScenesType();
	for(int current_scenes_not_yet_completed=1; current_scenes_not_yet_completed<=scenes_not_yet_completed_count; current_scenes_not_yet_completed ++) {
		if(!(request.getParameter("scenes_not_yet_completed_" + current_scenes_not_yet_completed).equals(""))){
			st5.getScene().add(request.getParameter("scenes_not_yet_completed_" + current_scenes_not_yet_completed));
		}
	}
	cdrt1.setScenesNotYetCompleted(st5);
	
	//Scene Schedule - Scenes Deleted
	int scenes_deleted_count = Integer.parseInt(request.getParameter("scenes_deleted_count"));
	ScenesType st6 = new ScenesType();
	for(int current_scenes_deleted=1; current_scenes_deleted<=scenes_deleted_count; current_scenes_deleted ++) {
		if(!(request.getParameter("scenes_deleted_" + current_scenes_deleted).equals(""))){
			st6.getScene().add(request.getParameter("scenes_deleted_" + current_scenes_deleted));
		}
	}
	cdrt1.setScenesDeleted(st6);
	
	//Scene Schedule - Scenes Added
	int scenes_added_count = Integer.parseInt(request.getParameter("scenes_added_count"));
	ScenesType st7 = new ScenesType();
	for(int current_scenes_added=1; current_scenes_added<=scenes_added_count; current_scenes_added ++) {
		if(!(request.getParameter("scenes_added_" + current_scenes_added).equals(""))){
			st7.getScene().add(request.getParameter("scenes_added_" + current_scenes_added));
		}
	}
	cdrt1.setScenesAdded(st7);
	
	//Scene Schedule - Unscheduled Scenes Shot
	int unscheduled_scenes_shot_count = Integer.parseInt(request.getParameter("unscheduled_scenes_shot_count"));
	ScenesType st8 = new ScenesType();
	for(int current_unscheduled_scenes_shot=1; current_unscheduled_scenes_shot<=unscheduled_scenes_shot_count; current_unscheduled_scenes_shot ++) {
		if(!(request.getParameter("unscheduled_scenes_shot_" + current_unscheduled_scenes_shot).equals(""))){
			st8.getScene().add(request.getParameter("unscheduled_scenes_shot_" + current_unscheduled_scenes_shot));
		}
	}
	cdrt1.setUnscheduledScenesShot(st8);
	
	//Scene Timing
	ScenesTimingType stt1 = new ScenesTimingType();
	int scene_timing_count = Integer.parseInt(request.getParameter("scene_timing_count"));
	for(int current_scene_timing=1; current_scene_timing<=scene_timing_count; current_scene_timing ++) {
		SingleSceneTimingType sstt1 = new SingleSceneTimingType();
		sstt1.setScene(request.getParameter("scene_no_" + current_scene_timing));
		PageTimeType ptt1 = new PageTimeType();
		ptt1.setNumber(new BigInteger(request.getParameter("page_count_" + current_scene_timing)));
		ptt1.setNumerator(new BigInteger(request.getParameter("page_count_num_" + current_scene_timing)));
		sstt1.setPageTime(ptt1);
		sstt1.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("est_timing_" + current_scene_timing)));
		sstt1.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("actual_timing_" + current_scene_timing)));
		VarianceType vt1 = new VarianceType();
		if (request.getParameter("variance_sign_"+ current_scene_timing)=="+"){
			vt1.setSign(true);
		}else{
			vt1.setSign(false);
		}
		vt1.setVarTime(XMLGregorianCalendarImpl.parse(request.getParameter("variance_" + current_scene_timing)));
		sstt1.setVariance(vt1);
		sstt1.setCumulative(XMLGregorianCalendarImpl.parse(request.getParameter("cumulative_running_" + current_scene_timing)));
		stt1.getSingleSceneTiming().add(sstt1);
	}
	TotalScenesTimingType tstt1 = new TotalScenesTimingType();
	PageTimeType ptt2 = new PageTimeType();
	ptt2.setNumber(new BigInteger(request.getParameter("total_page_count")));
	ptt2.setNumerator(new BigInteger(request.getParameter("total_page_count_num")));
	tstt1.setPageTime(ptt2);
	tstt1.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_est_timing")));
	tstt1.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_actual_timing")));
	VarianceType vt2 = new VarianceType();
	if (request.getParameter("total_variance_sign")== "-"){
		vt2.setSign(false);
	}else{
		vt2.setSign(true);
	}
	vt2.setVarTime(XMLGregorianCalendarImpl.parse(request.getParameter("total_variance")));
	tstt1.setVariance(vt2);
	tstt1.setCumulative(XMLGregorianCalendarImpl.parse(request.getParameter("total_cumulative_running")));
	stt1.setTotalScenesTiming(tstt1);
	
	cdrt1.setScenesTiming(stt1);
	
	//Script Timing
	ScriptTimingType srtt1 = new ScriptTimingType();
	//Script Timing - Previous Shot
	SingleScriptTimingType ssrtt1 = new SingleScriptTimingType();
	ssrtt1.setScenes(new BigInteger(request.getParameter("prev_shot_scenes")));
	PageTimeType ptt3 = new PageTimeType();
	ptt3.setNumber(new BigInteger(request.getParameter("prev_shot_pages")));
	ptt3.setNumerator(new BigInteger(request.getParameter("prev_shot_pagesnum")));
	ssrtt1.setPageTime(ptt3);
	ssrtt1.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("prev_shot_esttiming")));
	ssrtt1.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("prev_shot_acttiming")));
	srtt1.setPrevShot(ssrtt1);
	//Script Timing - Shot Today
	SingleScriptTimingType ssrtt2 = new SingleScriptTimingType();
	ssrtt2.setScenes(new BigInteger(request.getParameter("shot_today_scenes")));
	PageTimeType ptt4 = new PageTimeType();
	ptt4.setNumber(new BigInteger(request.getParameter("shot_today_pages")));
	ptt4.setNumerator(new BigInteger(request.getParameter("shot_today_pagesnum")));
	ssrtt2.setPageTime(ptt4);
	ssrtt2.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_today_esttiming")));
	ssrtt2.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_today_acttiming")));
	srtt1.setShotToday(ssrtt2);
	//Script Timing - Shot To Date
	SingleScriptTimingType ssrtt3 = new SingleScriptTimingType();
	ssrtt3.setScenes(new BigInteger(request.getParameter("shot_to_date_scenes")));
	PageTimeType ptt5 = new PageTimeType();
	ptt5.setNumber(new BigInteger(request.getParameter("shot_to_date_pages")));
	ptt5.setNumerator(new BigInteger(request.getParameter("shot_to_date_pagesnum")));
	ssrtt3.setPageTime(ptt5);
	ssrtt3.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_to_date_esttiming")));
	ssrtt3.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_to_date_acttiming")));
	srtt1.setShotToDate(ssrtt3);
	//Script Timing - To Be Shot
	SingleScriptTimingType ssrtt4 = new SingleScriptTimingType();
	ssrtt4.setScenes(new BigInteger(request.getParameter("remaining_scenes")));
	PageTimeType ptt6 = new PageTimeType();
	ptt6.setNumber(new BigInteger(request.getParameter("remaining_pages")));
	ptt6.setNumerator(new BigInteger(request.getParameter("remaining_pagesnum")));
	ssrtt4.setPageTime(ptt6);
	ssrtt4.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("remaining_esttiming")));
	ssrtt4.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("remaining_acttiming")));
	srtt1.setPrevShot(ssrtt4);
	//Script Timing - Total
	SingleScriptTimingType ssrtt5 = new SingleScriptTimingType();
	ssrtt5.setScenes(new BigInteger(request.getParameter("total_scenes")));
	PageTimeType ptt7 = new PageTimeType();
	ptt7.setNumber(new BigInteger(request.getParameter("total_pages")));
	ptt7.setNumerator(new BigInteger(request.getParameter("total_pagesnum")));
	ssrtt5.setPageTime(ptt7);
	ssrtt5.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_esttiming")));
	ssrtt5.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_acttiming")));
	srtt1.setTotal(ssrtt5);
	cdrt1.setScriptTiming(srtt1);
	
	//Remarks
	if(!(request.getParameter("remarks").equals(""))){
		cdrt1.setRemarks(request.getParameter("remarks"));
	}
	
	focdr.setContinuityDailyReport(cdrt1);

	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(focdrElement, xmlOS);//out to ByteArray

	response.setHeader("Content-Disposition", "attachment;filename=\"ContinuityDailyReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
	response.setHeader("Content-Type", "text/xml");

	ServletOutputStream outs = response.getOutputStream();
	xmlOS.writeTo(outs);
	outs.close();
}
%>
</body>
</html>