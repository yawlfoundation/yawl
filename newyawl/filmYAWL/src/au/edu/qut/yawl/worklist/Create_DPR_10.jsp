<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.io.File" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.math.*" %>
<%@ page import="com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl" %>
<%@ page import="javax.xml.bind.JAXBElement" %>
<%@ page import="javax.xml.bind.JAXBContext" %>
<%@ page import="javax.xml.bind.Marshaller" %>
<%@ page import="javax.xml.bind.Unmarshaller" %>
<%@ page import="org.yawlfoundation.sb.dprinfo.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="2048kb" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Daily Progress Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<!-- style sheet imports -->
<link href="graphics/style.css" rel="stylesheet" type="text/css" />
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js"></script>
</head>

<body onLoad="getParameters()">
<h1 align="center">Daily Progress Report</h1>

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
            int endOfFile = result.indexOf("</ns2:Create_DPR>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(
                    beginOfFile,
                    endOfFile + 17);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		//xml = "<?xml version='1.0' encoding='UTF-8'?><ns2:Create_DPR xmlns:ns2='http://www.yawlfoundation.org/sb/DPRinfo' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.yawlfoundation.org/sb/DPRinfo DPRinfoType.xsd '><generalInfo><production>production</production><date>2001-01-01</date><weekday>weekday</weekday><shootDayNo>0</shootDayNo></generalInfo><producer>producer</producer><director>director</director><productionManager>productionManager</productionManager><directorOfPhotography>directorOfPhotography</directorOfPhotography><DPRinfo><startDate>2001-01-01</startDate><scheduledFinish>2001-01-01</scheduledFinish><revisedFinish>2001-01-01</revisedFinish><shootingDaysSchedule><scheduledDays>0</scheduledDays><daysToDate>0</daysToDate><estdToComplete>0</estdToComplete><estdTotal>0</estdTotal></shootingDaysSchedule><locationSets><singleLocation><locationName>locationName</locationName><address>address</address><set>set</set></singleLocation></locationSets><slateNOs><slate>0</slate></slateNOs><scheduledScenesShot><scene>scene</scene></scheduledScenesShot><scheduledScenesNotShot><scene>scene</scene></scheduledScenesNotShot><ScenesNotYetCompleted><scene>scene</scene></ScenesNotYetCompleted><ScenesDeleted><scene>scene</scene></ScenesDeleted><ScenesAdded><scene>scene</scene></ScenesAdded><unscheduledScenesShot><scene>scene</scene></unscheduledScenesShot><scriptTiming><prevShot><scenes>0</scenes><pageTime><number>0</number><numerator>0</numerator></pageTime><estTiming>12:00:00</estTiming><actualTiming>12:00:00</actualTiming></prevShot><shotToday><scenes>0</scenes><pageTime><number>0</number><numerator>0</numerator></pageTime><estTiming>12:00:00</estTiming><actualTiming>12:00:00</actualTiming></shotToday><shotTodate><scenes>0</scenes><pageTime><number>0</number><numerator>0</numerator></pageTime><estTiming>12:00:00</estTiming><actualTiming>12:00:00</actualTiming></shotTodate><toBeShot><scenes>0</scenes><pageTime><number>0</number><numerator>0</numerator></pageTime><estTiming>12:00:00</estTiming><actualTiming>12:00:00</actualTiming></toBeShot><total><scenes>0</scenes><pageTime><number>0</number><numerator>0</numerator></pageTime><estTiming>12:00:00</estTiming><actualTiming>12:00:00</actualTiming></total></scriptTiming><ratioTimingSpec><scheduledRatio>0.0</scheduledRatio><dailyRatio>0.0</dailyRatio><averageRatio>0.0</averageRatio><averageTiming>12:00:00</averageTiming><cumulative><sign>true</sign><varTime>12:00:00</varTime></cumulative><originalTiming>12:00:00</originalTiming></ratioTimingSpec><stockInfo><previously><loaded>0</loaded><gross>0</gross><exposed>0</exposed><print>0</print><N_G>0</N_G><waste>0</waste><shortEnds>0</shortEnds><soundRolls>soundRoll</soundRolls><camRolls>camRoll</camRolls></previously><today><loaded>0</loaded><gross>0</gross><exposed>0</exposed><print>0</print><N_G>0</N_G><waste>0</waste><shortEnds>0</shortEnds><soundRolls>soundRoll</soundRolls><camRolls>camRoll</camRolls></today><totalToDate><loaded>0</loaded><gross>0</gross><exposed>0</exposed><print>0</print><N_G>0</N_G><waste>0</waste><shortEnds>0</shortEnds><soundRolls>soundRoll</soundRolls><camRolls>camRoll</camRolls></totalToDate></stockInfo><artistTimeSheet><singleArtist><artist>artist</artist><character>character</character><P_U>P_U</P_U><MU_WD_Call_scheduled>12:00:00</MU_WD_Call_scheduled><MU_WD_Call_actualArrival>12:00:00</MU_WD_Call_actualArrival><mealBreak>12:00:00</mealBreak><timeWrap>12:00:00</timeWrap><travel>12:00:00</travel><totalHRs>12:00:00</totalHRs></singleArtist></artistTimeSheet><extrasTimeSheet><singleArtist><artist>artist</artist><character>character</character><P_U>P_U</P_U><MU_WD_Call_scheduled>12:00:00</MU_WD_Call_scheduled><MU_WD_Call_actualArrival>12:00:00</MU_WD_Call_actualArrival><mealBreak>12:00:00</mealBreak><timeWrap>12:00:00</timeWrap><travel>12:00:00</travel><totalHRs>12:00:00</totalHRs></singleArtist></extrasTimeSheet><crewTimeSheet><singleCrew><crewName>crewName</crewName><crewRole>crewRole</crewRole><crewCall>12:00:00</crewCall><travelIn>12:00:00</travelIn><locationCall>12:00:00</locationCall><mealBreak>12:00:00</mealBreak><wrap>12:00:00</wrap><wrapLoc>12:00:00</wrapLoc><departLoc>12:00:00</departLoc><travelOut>12:00:00</travelOut><totalHRs>12:00:00</totalHRs></singleCrew></crewTimeSheet><majorProps_actionVehicles_additionalEquipment>majorProps_actionVehicles_additionalEquipment</majorProps_actionVehicles_additionalEquipment><additionalCrew>additionalCrew</additionalCrew><livestocks_other>livestocks_other</livestocks_other><accidents_delays>accidents_delays</accidents_delays><catering><singleMeal><meal>meal</meal><time>12:00:00</time><numbers>0</numbers><location>location</location></singleMeal></catering><generalRemarks>generalRemarks</generalRemarks></DPRinfo></ns2:Create_DPR>";
		xml = (String)session.getAttribute("outputData");
		xml = xml.replaceAll("<Create_DPR", "<ns2:Create_DPR xmlns:ns2='http://www.yawlfoundation.org/sb/DPRinfo'");
			xml = xml.replaceAll("</Create_DPR","</ns2:Create_DPR");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.dprinfo");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement cdprtElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
	CreateDPRType cdprt = (CreateDPRType)cdprtElement.getValue();

	GeneralInfoType git = cdprt.getGeneralInfo();
	DPRinfoType dprit = cdprt.getDPRinfo();
	ShootingDaysScheduleType sdst = dprit.getShootingDaysSchedule();
	RatioTimingSpecType rtst = dprit.getRatioTimingSpec();
%>


  <table width="700"  border="0" align="center" cellpadding="0" cellspacing="0">
    <form name="form1" method="post">
	<tr>
      <td>
        <table width="700" border="0" cellpadding="0" cellspacing="0">
          <tr><td class="header-left">&nbsp;</td>
            <td colspan="4" class="header-middle">General Info </td>
            <td class="header-right">&nbsp;</td>
          </tr>
          <tr>
		  	<td width="15" class="left">&nbsp;</td>
            <td><strong>Producer</strong></td><td><input name="producer" type="text" id="producer" value="<%= cdprt.getProducer() %>" size="15" readonly></td>
            <td><strong>Production</strong></td><td><input name="production" type="text" id="production" value="<%= git.getProduction() %>" size="15" readonly></td>
            <td width="15" class="right">&nbsp;</td>
          </tr>
          <tr>
            <td width="15" class="left">&nbsp;</td>
			<td><strong>Director</strong></td><td><input name="director" type="text" id="director" value="<%= cdprt.getDirector()%>" size="15" readonly></td>
            <td><strong>Day</strong></td><td><input name="day" type="text" id="day" value="<%= git.getWeekday()%>" size="15" readonly></td>
            <td width="15" class="right">&nbsp;</td>
          </tr>
          <tr>
            <td width="15" class="left">&nbsp;</td>
			<td><strong>Prod. Manager</strong></td><td><input name="prod_manager" type="text" id="prod_manager" value="<%= cdprt.getProductionManager()%>" size="15" readonly></td>
            <td><strong>Date</strong></td><td><input name="date" type="text" id="date" value="<%= git.getDate()%>" size="15" readonly></td>
            <td width="15" class="right">&nbsp;</td>
          </tr>
          <tr>
            <td width="15" class="left">&nbsp;</td>
			<td><strong>D.O.P</strong></td><td><input name="dop" type="text" id="dop" value="<%= cdprt.getDirectorOfPhotography()%>" size="15" readonly></td>
            <td><strong>Shoot Day No. </strong></td><td><input name="shoot_day_no" type="text" id="shoot_day_no" value="<%= git.getShootDayNo() %>" size="15" readonly></td>
            <td width="15" class="right">&nbsp;</td>
          </tr>
          <tr><td colspan="6" class="bottom">&nbsp;</td>
          </tr>
      </table>
	</td></tr>
    <tr><td>
        <table width="700" border="0" cellpadding="0" cellspacing="0">
          <tr><td class="header-left">&nbsp;</td>
            <td colspan="6" class="header-middle">Scheduled Date Brief </td>
            <td class="header-right">&nbsp;</td>
          </tr>
          <tr>
            <td width="15" class="left">&nbsp;</td>
            <td><strong>Start Date</strong></td>
            <td><input name="start_date" type="text" id="start_date" value="<%= dprit.getStartDate() %>" size="15" readonly></td>
            <td><strong>Scheduled Finish</strong></td>
            <td><input name="scheduled_finish" type="text" id="scheduled_finish" value="<%= dprit.getScheduledFinish()%>" size="15" readonly></td>
            <td><strong>Revised Finish</strong></td>
            <td><input name="revised_finish" type="text" id="revised_finish" value="<% if(dprit.getRevisedFinish() != null){out.print(dprit.getRevisedFinish());}%>" size="15" readonly></td>
            <td width="15" class="right">&nbsp;</td>
          </tr>
          <tr><td colspan="8" class="bottom">&nbsp;</td></tr>
      </table>
	</td></tr>
    <tr><td><table width="700" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <th width="15" class="header-left">&nbsp;</th>
        <td colspan="2" class="header-middle">Shooting Days Schedule </td>
        <th width="15" class="header-right">&nbsp;</th>
      </tr>
      <tr>
        <td width="15" align="center" class="left">&nbsp;</td>
        <td><strong>Scheduled</strong></td>
        <td><input name="scheduled" type="text" id="scheduled" value="<%= sdst.getScheduledDays() %>" size="10" readonly></td>
        <td width="15" align="center" class="right">&nbsp;</td>
      </tr>
      <tr>
        <td width="15" align="center" class="left">&nbsp;</td>
        <td><strong>Days to Date</strong></td>
        <td><input name="days_to_date" type="text" id="days_to_date" value="<%= sdst.getDaysToDate() %>" size="10" readonly></td>
        <td width="15" align="center" class="right">&nbsp;</td>
      </tr>
      <tr>
        <td width="15" align="center" class="left">&nbsp;</td>
        <td><strong>Est. to Complete </strong></td>
        <td><input name="est_complete" type="text" id="est_complete" value="<%= sdst.getEstdToComplete() %>" size="10" readonly></td>
        <td width="15" align="center" class="right">&nbsp;</td>
      </tr>
      <tr>
        <td width="15" align="center" class="left">&nbsp;</td>
        <td><strong>Est. Total </strong></td>
        <td><input name="est_total" type="text" id="est_total" value="<%= sdst.getEstdTotal() %>" size="10" readonly></td>
        <td width="15" align="center" class="right">&nbsp;</td>
      </tr>
      <tr>
        <td colspan="4" class="bottom">&nbsp;</td>
      </tr>
    </table></td></tr>
    <tr valign="top">
      <td><table width="700" border="0" cellpadding="0" cellspacing="0" id="locations">
	  <tbody>
        <tr valign="top">
          <td width="15" class="header-left">&nbsp;</td>
          <td width="530" colspan="4" class="header-middle">Locations/Sets</td>
          <td width="15" class="header-right">&nbsp;</td>
        </tr>
        <tr valign="top">
          <td class="left">&nbsp;</td>
          <td><strong>Location Name </strong></td>
          <td><strong>Address</strong></td>
          <td><strong>Set</strong></td>
          <td>&nbsp;</td>
          <td class="right">&nbsp;</td>
        </tr>
		<%	int g=0;
			LocationSetsType lst = dprit.getLocationSets();
			for(SingleLocationType slt: lst.getSingleLocation()){
				g++;%>
        <tr valign="top">
          <td align="center" class="left">&nbsp;</td>
          <td><input name='location_name_<%=g%>' type='text' id='location_name_<%=g%>' size="25" value="<%=slt.getLocationName() %>" readonly></td>
          <td><input name='location_address_<%=g%>' type='text' id='location_address_<%=g%>' size="25" value="<%=slt.getAddress() %>" readonly></td>
          <td id="location_set_<%=g%>">
            <% int h=0;
			List<String> set_list = slt.getSet();
				for(String set : set_list) {
					h ++;%>
            <input name='location_set_<%=g%>_<%=h%>' type='text' id='location_set_<%=g%>_<%=h%>' size="25" value="<%=set%>" readonly>
            <%}%>
          </td>
          <td>                <input type="hidden" name="location_set_<%=g%>_count" id="location_set_<%=g%>_count" value="<%out.print(h);%>" readonly></td><td class="right">&nbsp;</td>
        </tr>
		<%}%>
		</tbody>
        <tr>
          <td colspan="6" class="bottom">&nbsp;</td>
        </tr>
      </table></td>
    </tr>
    <tr valign="top">
    
    <td>      
    
      <table width="700" border="0" cellpadding="0" cellspacing="0">
        <tr valign="top">
          <td width="15" valign="top" class="header-left">&nbsp;</td>
          <td colspan="2" valign="top" class="header-middle">Scene Schedule</td>
          <td width="15" valign="top" class="header-right">&nbsp;</td>
        </tr>
        <tr valign="top">
          <td width="15" valign="top" class="left">&nbsp;</td>
          <td width="335" valign="top"><strong>Slates</strong></td>
          <td width="335" id="slate">
			<% int e=0;
			SlateNOs sln = dprit.getSlateNOs();
			List<String> slate_list = sln.getSlate();
				for(String slate : slate_list) {
					e ++;%>
            <input name='slate_<%=e%>' type='text' id='slate_<%=e%>' size="2" value="<%=slate%>" readonly>
            <%}%>          </td>
          <td width="15" valign="top" class="right">&nbsp;</td>
        </tr>
        <tr valign="top">
          <td width="15" valign="top" class="left">&nbsp;</td>
          <td width="335" valign="top"><strong>Scheduled Scenes Shot </strong></td>
          <td width="335" id="scheduled_scenes_shot">
			<% SceneNOsType snt1 = dprit.getScheduledScenesShot();
			int s_1=0;
			if(snt1.getScene() != null) {
				List<String> scenes_list1 = snt1.getScene();
					for(String scene1 : scenes_list1) {
						s_1 ++;
			%>
            <input name='scheduled_scenes_shot_<%=s_1%>' type='text' id='scheduled_scenes_shot_<%=s_1%>'  size="2" value="<%=scene1 %>" readonly>
            <%}
			}%>
		</td>
          <td width="15" valign="top" class="right">&nbsp;</td>
        </tr>
        <tr valign="top">
          <td width="15" align="center" valign="top" class="left">&nbsp;</td>
          <td width="335" valign="top"><strong>Scenes Scheduled &amp; Not Shot</strong> </td>
          <td width="335" id="scenes_scheduled_not_shot">
            <% SceneNOsType snt2 = dprit.getScheduledScenesNotShot();
			int s_2=0;
			if(snt2.getScene() != null) {
				List<String> scenes_list2 = snt2.getScene();
					for(String scene2 : scenes_list2) {
						s_2 ++;
			%>
            <input name='scenes_scheduled_not_shot_<%=s_2%>' type='text' id='scenes_scheduled_not_shot_<%=s_2%>'  size="2" value="<%=scene2 %>" readonly>
            <%}}%>          </td>
          <td width="15" valign="top" class="right">&nbsp;</td>
        </tr>
        <tr valign="top">
          <td width="15" align="center" valign="top" class="left">&nbsp;</td>
          <td width="335" valign="top"><strong>Scenes Not Yet Completed </strong></td>
          <td width="335" id="scenes_not_yet_completed">
			<% SceneNOsType snt3 = dprit.getScenesNotYetCompleted();
			int s_3=0;
			if(snt3.getScene() != null) {
				List<String> scenes_list3 = snt3.getScene();
					for(String scene3 : scenes_list3) {
						s_3 ++;
			%>            
			<input name='scenes_not_yet_completed_<%=s_3%>' type='text' id='scenes_not_yet_completed_<%=s_3%>'  size="2" value="<%=scene3 %>" readonly>
            <%}}%>          </td>
          <td width="15" valign="top" class="right">&nbsp;</td>
        </tr>
        <tr valign="top">
          <td width="15" align="center" valign="top" class="left">&nbsp;</td>
          <td width="335" valign="top"><strong>Scenes Deleted </strong></td>
          <td width="335" id="scenes_deleted">
            <% SceneNOsType snt4 = dprit.getScenesDeleted();
			int s_4=0;
			if(snt4.getScene() != null) {
				List<String> scenes_list4 = snt4.getScene();
					for(String scene4 : scenes_list4) {
						s_4 ++;
			%>
            <input name='scenes_deleted_<%=s_4%>' type='text' id='scenes_deleted_<%=s_4%>'  size="2" value="<%=scene4 %>" readonly>
            <%}}%>          </td>
          <td width="15" valign="top" class="right">&nbsp;</td>
        </tr>
        <tr valign="top">
          <td width="15" align="center" valign="top" class="left">&nbsp;</td>
          <td width="335" valign="top"><strong>Scenes Added </strong></td>
          <td width="335" id="scenes_added">
            <% SceneNOsType snt5 = dprit.getScenesAdded();
			int s_5=0;
			if(snt5.getScene() != null) {
				List<String> scenes_list5 = snt5.getScene();
					for(String scene5 : scenes_list5) {
						s_5 ++;
			%>
            <input name='scenes_added_<%=s_5%>' type='text' id='scenes_added_<%=s_5%>'  size="2" value="<%=scene5 %>" readonly>
            <%}}%>          </td>
          <td width="15" valign="top" class="right">&nbsp;</td>
        </tr>
        <tr valign="top">
          <td width="15" align="center" valign="top" class="left">&nbsp;</td>
          <td width="335" valign="top"><strong>Unscheduled Scenes Shot </strong></td>
          <td width="335" id="unscheduled_scenes_shot">
             <% SceneNOsType snt6 = dprit.getUnscheduledScenesShot();
			int s_6=0;
			if(snt6.getScene() != null) {
				List<String> scenes_list6 = snt6.getScene();
					for(String scene6 : scenes_list6) {
						s_6 ++;
			%>
            <input name='unscheduled_scenes_shot_<%=s_6%>' type='text' id='unscheduled_scenes_shot_<%=s_6%>'  size="2" value="<%=scene6 %>" readonly>
            <%}}%>          </td>
          <td width="15" valign="top" class="right">&nbsp;</td>
        </tr>
    <tr>
      <td colspan="4" class="bottom">&nbsp;</td>
      </tr>
      </table>      </td>
    </tr>
    <tr valign="top">
      <td><table width="700" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <th class="header-left">&nbsp;</th>
          <td colspan="5" class="header-middle">Script Timing </td>
          <th class="header-right">&nbsp;</th>
        </tr>
        <tr>
          <th class="left">&nbsp;</th>
          <td>&nbsp;</td>
          <th>Scenes</th>
          <th>Page Count </th>
          <th>Est.</th>
          <th>Actual</th>
          <th class="right">&nbsp;</th>
        </tr>
        <%
		  ScriptTimingType stt = dprit.getScriptTiming();
		  SingleSriptTimingType sstt_pr = stt.getPrevShot();
		  SingleSriptTimingType sstt_today = stt.getShotToday();
		  SingleSriptTimingType sstt_todate = stt.getShotToDate();
		  SingleSriptTimingType sstt_tobe = stt.getToBeShot();
		  SingleSriptTimingType sstt_total = stt.getTotal();
		  %>
        <tr>
          <td align="center" class="left">&nbsp;</td>
          <td><strong>Prev. Shot </strong></td>
          <td align="center"><input name="prev_scenes" type="text" id="prev_scenes" value="<%= sstt_pr.getScenes() %>" size="5" readonly></td>
          <td align="center"><%PageTimeType ptt1  = sstt_pr.getPageTime();%>
            <input name='prev_pages' type='text' id='prev_pages' value="<%= ptt1.getNumber() %>" size="4" readonly>
            &nbsp;
            <input name='prev_pagesnum' type='text' id='prev_pagesnum' value="<%= ptt1.getNumber() %>" size="2" readonly>/8</td>
          <td align="center"><input name="prev_est" type="text" id="prev_est" value="<%= sstt_pr.getEstTiming() %>" size="5" readonly></td>
          <td align="center"><input name="prev_actual" type="text" id="prev_actual" value="<%= sstt_pr.getActualTiming() %>" size="5" readonly></td>
          <td align="center" class="right">&nbsp;</td>
        </tr>
        <tr>
          <td align="center" class="left">&nbsp;</td>
          <td><strong>Shot Today </strong></td>
          <td align="center"><input name="shot_scenes" type="text" id="shot_scenes" value="<%= sstt_today.getScenes() %>" size="5" readonly></td>
          <td align="center"><%PageTimeType ptt2  = sstt_today.getPageTime();%>
            <input name='shot_pages' type='text' id='shot_pages' value="<%= ptt2.getNumber() %>" size="4" readonly>
            &nbsp;
            <input name='shot_pagesnum' type='text' id='shot_pagesnum' value="<%= ptt2.getNumber() %>" size="2" readonly>/8</td>
          <td align="center"><input name="shot_est" type="text" id="shot_est" value="<%= sstt_today.getEstTiming() %>" size="5" readonly></td>
          <td align="center"><input name="shot_actual" type="text" id="shot_actual" value="<%= sstt_today.getActualTiming() %>" size="5" readonly></td>
          <td align="center" class="right">&nbsp;</td>
        </tr>
        <tr>
          <td align="center" class="left">&nbsp;</td>
          <td><strong>To Date </strong></td>
          <td align="center"><input name="todate_scenes" type="text" id="todate_scenes" value="<%= sstt_todate.getScenes() %>" size="5" readonly></td>
          <td align="center"><%PageTimeType ptt3  = sstt_todate.getPageTime();%>
            <input name='todate_pages' type='text' id='todate_pages' value="<%= ptt3.getNumber() %>" size="4" readonly>
            &nbsp;
            <input name='todate_pagesnum' type='text' id='todate_pagesnum' value="<%= ptt3.getNumerator() %>" size="2" readonly>/8</td>
          <td align="center"><input name="todate_est" type="text" id="todate_est" value="<%= sstt_todate.getEstTiming() %>" size="5" readonly></td>
          <td align="center"><input name="todate_actual" type="text" id="todate_actual" value="<%= sstt_todate.getActualTiming() %>" size="5" readonly></td>
          <td align="center" class="right">&nbsp;</td>
        </tr>
        <tr>
          <td align="center" class="left">&nbsp;</td>
          <td><strong>To Be Shot </strong></td>
          <td align="center"><input name="tobe_scenes" type="text" id="tobe_scenes" value="<%= sstt_tobe.getScenes() %>" size="5" readonly></td>
          <td align="center"><%PageTimeType ptt4  = sstt_tobe.getPageTime();%>
            <input name='tobe_pages' type='text' id='tobe_pages' value="<%= ptt4.getNumber() %>" size="4" readonly>
            &nbsp;
            <input name='tobe_pagesnum' type='text' id='tobe_pagesnum' value="<%= ptt4.getNumerator() %>" size="2" readonly>/8</td>
          <td align="center"><input name="tobe_est" type="text" id="tobe_est" value="<%= sstt_tobe.getEstTiming() %>" size="5" readonly></td>
          <td align="center"><input name="tobe_actual" type="text" id="tobe_actual" value="<%= sstt_tobe.getActualTiming() %>" size="5" readonly></td>
          <td align="center" class="right">&nbsp;</td>
        </tr>
        <tr>
          <td class="left">&nbsp;</td>
          <td><strong>Total</strong></td>
          <td align="center"><input name="total_scenes" type="text" id="total_scenes" value="<%= sstt_total.getScenes() %>" size="5" readonly></td>
          <td align="center"><%PageTimeType ptt5  = sstt_total.getPageTime();%>
            <input name='total_pages' type='text' id='total_pages' value="<%= ptt5.getNumber() %>" size="4" readonly>
            &nbsp;
            <input name='total_pagesnum' type='text' id='total_pagesnum' value="<%= ptt5.getNumerator() %>" size="2" readonly>/8</td>
          <td align="center"><input name="total_est" type="text" id="total_est" value="<%= sstt_total.getEstTiming() %>" size="5" readonly></td>
          <td align="center"><input name="total_actual" type="text" id="total_actual" value="<%= sstt_total.getActualTiming() %>" size="5" readonly></td>
          <td align="center" class="right">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="7" class="bottom">&nbsp;</td>
        </tr>
      </table></td>
</tr>
    <tr>
      <td><table width="700" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td class="header-left">&nbsp;</td>
          <td colspan="2" class="header-middle">Ratio Timing Spec </td>
          <td class="header-right">&nbsp;</td>
        </tr>
        <tr>
          <td class="left">&nbsp;</td>
          <td><strong>Scheduled Ratio</strong></td>
          <td><input name="scheduled_ratio" type="text" id="scheduled_ratio" value="<%= rtst.getScheduledRatio() %>" size="10" readonly></td>
          <td class="right">&nbsp;</td>
        </tr>
        <tr>
          <td class="left">&nbsp;</td>
          <td><strong>Daily Ratio</strong></td>
          <td><input name="daily_ratio" type="text" id="daily_ratio" value="<%= rtst.getDailyRatio() %>" size="10" readonly></td>
          <td class="right">&nbsp;</td>
        </tr>
        <tr>
          <td class="left">&nbsp;</td>
          <td><strong>Average Ratio</strong></td>
          <td><input name="average_ratio" type="text" id="average_ratio" value="<%= rtst.getAverageRatio() %>" size="10" readonly></td>
          <td class="right">&nbsp; </td>
        </tr>
        <tr>
          <td class="left">&nbsp;</td>
          <td><strong>Average Timing</strong></td>
          <td><input name="average_timing" type="text" id="average_timing" value="<%= rtst.getAverageTiming() %>" size="10" readonly></td>
          <td class="right">&nbsp;</td>
        </tr>
        <tr>
          <%CumulativeType ct = rtst.getCumulative();%>
          <td class="left">&nbsp;</td>
          <td><strong>Cumulative +/-</strong></td>
		  
          <td> <input name="cumulative_sign" type="text" id="cumulative_sign" value="<%if(ct.isSign() == true){out.print("+");}else{out.print("-");} %>" size="1" readonly>
              <input name="cumulative" type="text" id="cumulative" value="<%= ct.getVarTime() %>" size="10" readonly></td>
          <td class="right">&nbsp;</td>
        </tr>
        <tr>
          <td class="left">&nbsp;</td>
          <td><strong>Original Timing</strong></td>
          <td><input name="original_timing" type="text" id="original_timing" value="<%= rtst.getOriginalTiming() %>" size="10" readonly></td>
          <td class="right">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="4" class="bottom">&nbsp;</td>
        </tr>
      </table></td>
    </tr>
    <tr>
      <td>
        <table width="700" border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td class="header-left">&nbsp;</td>
            <td colspan="10" class="header-middle">Stock Info </td>
            <td class="header-right">&nbsp;</td>
          </tr>
          <tr>
            <th width="15" class="left">&nbsp;</th>
			<td>&nbsp;</td>
            <th>Loaded</th>
            <th>Gross</th>
            <th>Exposed</th>
            <th>Print</th>
            <th>N.G.</th>
            <th>Waste</th>
            <th>S/Ends</th>
            <th>Sound Rolls </th>
            <th>Camera Rolls </th>
            <th width="15" class="right">&nbsp;</th>
          </tr>
		  <%
		  StockInfoType sit = dprit.getStockInfo();
		  SingleStockInfoType ssit_pr = sit.getPreviously();
		  SingleStockInfoType ssit_today = sit.getToday();
		  SingleStockInfoType ssit_total = sit.getTotalToDate();
		  %>
          <tr>
            <td width="15" align="center" class="left">&nbsp;</td>
			<td><strong>Previously</strong></td>
            <td align="center"><input name="previously_loaded" type="text" id="previously_loaded" value="<%=ssit_pr.getLoaded() %>" size="5" readonly></td>
            <td align="center"><input name="previously_gross" type="text" id="previously_gross" value="<%=ssit_pr.getGross() %>" size="5" readonly></td>
            <td align="center"><input name="previously_exposed" type="text" id="previously_exposed" value="<%=ssit_pr.getExposed() %>" size="5" readonly></td>
            <td align="center"><input name="previously_print" type="text" id="previously_print" value="<%=ssit_pr.getPrint() %>" size="5" readonly></td>
            <td align="center"><input name="previously_ng" type="text" id="previously_ng" value="<%=ssit_pr.getNG() %>" size="5" readonly></td>
            <td align="center"><input name="previously_waste" type="text" id="previously_waste" value="<%=ssit_pr.getWaste() %>" size="5" readonly></td>
            <td align="center"><input name="previously_shortend" type="text" id="previously_shortend" value="<%=ssit_pr.getShortEnds() %>" size="5" readonly></td>
            <td align="center"><input name="previously_soundrolls" type="text" id="previously_soundrolls" value="<%=ssit_pr.getSoundRolls() %>" size="5" readonly></td>
            <td align="center"><input name="previously_camrolls" type="text" id="previously_camrolls" value="<%=ssit_pr.getCamRolls() %>" size="5" readonly></td>
            <td width="15" align="center" class="right">&nbsp;</td>
          </tr>
          <tr>
            <td width="15" align="center" class="left">&nbsp;</td>
			<td><strong>Today</strong></td>
            <td align="center"><input name="today_loaded" type="text" id="today_loaded" value="<%=ssit_today.getLoaded() %>" size="5" readonly></td>
            <td align="center"><input name="today_gross" type="text" id="today_gross" value="<%=ssit_today.getGross() %>" size="5" readonly></td>
            <td align="center"><input name="today_exposed" type="text" id="today_exposed" value="<%=ssit_today.getExposed() %>" size="5" readonly></td>
            <td align="center"><input name="today_print" type="text" id="today_print" value="<%=ssit_today.getPrint() %>" size="5" readonly></td>
            <td align="center"><input name="today_ng" type="text" id="today_ng" value="<%=ssit_today.getNG() %>" size="5" readonly></td>
            <td align="center"><input name="today_waste" type="text" id="today_waste" value="<%=ssit_today.getWaste() %>" size="5" readonly></td>
            <td align="center"><input name="today_shortend" type="text" id="today_shortend" value="<%=ssit_today.getShortEnds() %>" size="5" readonly></td>
            <td align="center"><input name="today_soundrolls" type="text" id="today_soundrolls" value="<%=ssit_today.getSoundRolls() %>" size="5" readonly></td>
            <td align="center"><input name="today_camrolls" type="text" id="today_camrolls" value="<%=ssit_today.getCamRolls() %>" size="5" readonly></td>
            <td width="15" align="center" class="right">&nbsp;</td>
          </tr>
          <tr>
            <td width="15" align="center" class="left">&nbsp;</td>
			<td><strong>Total to Date </strong></td>
            <td align="center"><input name="total_loaded" type="text" id="total_loaded" value="<%=ssit_total.getLoaded() %>" size="5" readonly></td>
            <td align="center"><input name="total_gross" type="text" id="total_gross" value="<%=ssit_total.getGross() %>" size="5" readonly></td>
            <td align="center"><input name="total_exposed" type="text" id="total_exposed" value="<%=ssit_total.getExposed() %>" size="5" readonly></td>
            <td align="center"><input name="total_print" type="text" id="total_print" value="<%=ssit_total.getPrint() %>" size="5" readonly></td>
            <td align="center"><input name="total_ng" type="text" id="total_ng" value="<%=ssit_total.getNG() %>" size="5" readonly></td>
            <td align="center"><input name="total_waste" type="text" id="total_waste" value="<%=ssit_total.getWaste() %>" size="5" readonly></td>
            <td align="center"><input name="total_shortend" type="text" id="total_shortend" value="<%=ssit_total.getShortEnds() %>" size="5" readonly></td>
            <td align="center"><input name="total_soundrolls" type="text" id="total_soundrolls" value="<%=ssit_total.getSoundRolls() %>" size="5" readonly></td>
            <td align="center"><input name="total_camrolls" type="text" id="total_camrolls" value="<%=ssit_total.getCamRolls() %>" size="5" readonly></td>
            <td width="15" align="center" class="right">&nbsp;</td>
          </tr>
          <tr>
            <td colspan="12" class="bottom">&nbsp;</td>
          </tr>
      </table></td>
    </tr>
    <tr>
      <td><table width="700" border="0" cellpadding="0" cellspacing="0" id="artist">
        <tbody>
          <tr>
            <td class="header-left">&nbsp;</td>
            <td colspan="9" class="header-middle">Artist Details </td>
            <td class="header-right">&nbsp;</td>
          </tr>
          <tr>
            <th width="15" class="left">&nbsp;</th>
            <td valign="top"><strong>Artist</strong></td>
            <th valign="top">Character</th>
            <th valign="top">Pick Up</th>
            <th valign="top">MU/WD Call Scheduled</th>
            <th valign="top">MU/WD Call Actual </th>
            <th valign="top">Meal Break </th>
            <th valign="top">TimeWrap</th>
            <th valign="top">Travel</th>
            <th valign="top">Total Hrs</th>
            <th width="15" class="right">&nbsp;</th>
          </tr>
           <% 
			ArtistTimeSheetType atst1 = dprit.getArtistTimeSheet();
			int d=0;
			if (atst1.getSingleArtist() != null) {
			for(SingleArtistType sat1 : atst1.getSingleArtist()) {
			d++;
			%>
          <tr align="center">
		   <%	String a_wrap = sat1.getTimeWrap().toString();
				String a_actual = sat1.getMUWDCallActualArrival().toString();
				String a_meal = sat1.getMealBreak().toString();
				SimpleDateFormat a_sdf = new SimpleDateFormat("HH:mm:ss");
				Date a_wrap_time = a_sdf.parse(a_wrap);
				Date a_actual_time = a_sdf.parse(a_actual);
				Date a_meal_time = a_sdf.parse(a_meal);
				//get times in seconds
				long a_wrap_hours = a_wrap_time.getHours() *3600;
				long a_wrap_minutes = a_wrap_time.getMinutes() *60;
				long a_wrap_seconds = a_wrap_time.getSeconds();
				long a_wrap_total = a_wrap_hours + a_wrap_minutes + a_wrap_seconds;
				//get times in seconds
				long a_actual_hours = a_actual_time.getHours() *3600;
				long a_actual_minutes = a_actual_time.getMinutes() *60;
				long a_actual_seconds = a_actual_time.getSeconds();
				long a_actual_total = a_actual_hours + a_actual_minutes + a_actual_seconds;
				//get times in seconds
				long a_meal_hours = a_meal_time.getHours() *3600;
				long a_meal_minutes = a_meal_time.getMinutes() *60;
				long a_meal_seconds = a_meal_time.getSeconds();
				long a_meal_total = a_meal_hours + a_meal_minutes + a_meal_seconds;
				
				//do the calculation
				long a_dif= a_wrap_total - a_actual_total - a_meal_total ;
				if (a_dif <0) {
					a_dif = a_wrap_total - a_actual_total;
					a_meal = "00:00:00";
				}
				//convert to HH:MM:SS format
				long a_hours, a_minutes, a_seconds,a_timeInSeconds;
				a_timeInSeconds=a_dif;
				a_hours = a_timeInSeconds / 3600;
				a_timeInSeconds = a_timeInSeconds - (a_hours * 3600);
				a_minutes = a_timeInSeconds / 60;
				a_timeInSeconds = a_timeInSeconds - (a_minutes * 60);
				a_seconds = a_timeInSeconds;
				String a_hours_string = String.valueOf(a_hours);
				String a_minutes_string = String.valueOf(a_minutes);
				String a_seconds_string = String.valueOf(a_seconds);
				if (a_hours_string.length() == 1) {
					a_hours_string = "0" +  a_hours_string;
				}
				if (a_minutes_string.length() == 1) {
					a_minutes_string = "0" +  a_minutes_string;
				}
				if (a_seconds_string.length() == 1) {
					a_seconds_string = "0" +  a_seconds_string;
				}
				String a_total_hrs = a_hours_string + ":" + a_minutes_string + ":"+ a_seconds_string;
				%>		
            <td class="left">&nbsp;</td>
            <td><input name="artist_artist_<%=d%>" type="text" id="artist_artist_<%=d%>" value="<%= sat1.getArtist() %>" size="10" readonly></td>
            <td><input name="artist_character_<%=d%>" type="text" id="artist_character_<%=d%>" value="<%= sat1.getCharacter() %>" size="10" readonly></td>
            <td><input name="artist_pu_<%=d%>" type="text" id="artist_pu_<%=d%>" value="<%= sat1.getPU() %>" size="6" readonly></td>
            <td><input name="artist_MUWDCall_scheduled_<%=d%>" type="text" id="artist_MUWDCall_scheduled_<%=d%>" value="<%= sat1.getMUWDCallScheduled() %>" size="6" readonly></td>
            <td><input name="artist_MUWDCall_actual_<%=d%>" type="text" id="artist_MUWDCall_actual_<%=d%>" value="<%= sat1.getMUWDCallActualArrival() %>" size="6" readonly></td>
            <td><input name="artist_mealbreak_<%=d%>" type="text" id="artist_mealbreak_<%=d%>" value="<%= a_meal %>" size="6" readonly></td>
            <td><input name="artist_timewrap_<%=d%>" type="text" id="artist_timewrap_<%=d%>" value="<%= sat1.getTimeWrap() %>" size="6" readonly></td>
            <td><input name="artist_travel_<%=d%>" type="text" id="artist_travel_<%=d%>" value="<%= sat1.getTravel() %>" size="6" readonly></td>
		    <td><input name="artist_totalhrs_<%=d%>" type="text" id="artist_totalhrs_<%=d%>" value="<%= a_total_hrs %>" size="6" readonly></td>
            <td class="right">&nbsp;</td>
          </tr>
          <%}
		  }%>
          <tr align="center">
            <td colspan="11" class="bottom">&nbsp;</td>
          </tr>
        </tbody>
      </table></td>
    </tr>
    <tr>
      <td><table width="700" border="0" cellpadding="0" cellspacing="0" id="extras">
        <tbody>
          <tr>
            <td class="header-left">&nbsp;</td>
            <td colspan="9" class="header-middle">Extras Details </td>
            <td class="header-right">&nbsp;</td>
          </tr>
          <tr>
            <th width="15" class="left">&nbsp;</th>
            <td valign="top"><strong>Artist</strong></td>
            <th valign="top">Character</th>
            <th valign="top">Pick Up</th>
            <th valign="top">MU/WD Call Scheduled</th>
            <th valign="top">MU/WD Call Actual </th>
            <th valign="top">Meal Break </th>
            <th valign="top">TimeWrap</th>
            <th valign="top">Travel</th>
            <th valign="top">Total Hrs</th>
            <th width="15" class="right">&nbsp;</th>
          </tr>
          <% 
			ArtistTimeSheetType atst2 = dprit.getExtrasTimeSheet();
			int c=0;
			if (atst2.getSingleArtist() != null) {
			for(SingleArtistType sat2 : atst2.getSingleArtist()) {
			c++;
			%>
          <tr align="center">
		  <%	String e_wrap = sat2.getTimeWrap().toString();
				String e_actual = sat2.getMUWDCallActualArrival().toString();
				String e_meal = sat2.getMealBreak().toString();
				SimpleDateFormat e_sdf = new SimpleDateFormat("HH:mm:ss");
				Date e_wrap_time = e_sdf.parse(e_wrap);
				Date e_actual_time = e_sdf.parse(e_actual);
				Date e_meal_time = e_sdf.parse(e_meal);
				//get times in seconds
				long e_wrap_hours = e_wrap_time.getHours() *3600;
				long e_wrap_minutes = e_wrap_time.getMinutes() *60;
				long e_wrap_seconds = e_wrap_time.getSeconds();
				long e_wrap_total = e_wrap_hours + e_wrap_minutes + e_wrap_seconds;
				//get times in seconds
				long e_actual_hours = e_actual_time.getHours() *3600;
				long e_actual_minutes = e_actual_time.getMinutes() *60;
				long e_actual_seconds = e_actual_time.getSeconds();
				long e_actual_total = e_actual_hours + e_actual_minutes + e_actual_seconds;
				//get times in seconds
				long e_meal_hours = e_meal_time.getHours() *3600;
				long e_meal_minutes = e_meal_time.getMinutes() *60;
				long e_meal_seconds = e_meal_time.getSeconds();
				long e_meal_total = e_meal_hours + e_meal_minutes + e_meal_seconds;
				
				//do the calculation
				long e_dif= e_wrap_total - e_actual_total - e_meal_total ;
				if (e_dif <0) {
					e_dif = e_wrap_total - e_actual_total;
					e_meal = "00:00:00";
				}
				
				//convert to HH:MM:SS format
				long e_hours, e_minutes, e_seconds,e_timeInSeconds;
				e_timeInSeconds=e_dif;
				e_hours = e_timeInSeconds / 3600;
				e_timeInSeconds = e_timeInSeconds - (e_hours * 3600);
				e_minutes = e_timeInSeconds / 60;
				e_timeInSeconds = e_timeInSeconds - (e_minutes * 60);
				e_seconds = e_timeInSeconds;
				String e_hours_string = String.valueOf(e_hours);
				String e_minutes_string = String.valueOf(e_minutes);
				String e_seconds_string = String.valueOf(e_seconds);
				if (e_hours_string.length() == 1) {
					e_hours_string = "0" +  e_hours_string;
				}
				if (e_minutes_string.length() == 1) {
					e_minutes_string = "0" +  e_minutes_string;
				}
				if (e_seconds_string.length() == 1) {
					e_seconds_string = "0" +  e_seconds_string;
				}
				String e_total_hrs = e_hours_string + ":" + e_minutes_string + ":"+ e_seconds_string;
				%>
            <td width="15" class="left">&nbsp;</td>
            <td><input name="extras_artist_<%=c%>" type="text" id="extras_artist_<%=c%>" value="<%= sat2.getArtist() %>" size="10" readonly></td>
            <td><input name="extras_character_<%=c%>" type="text" id="extras_character_<%=c%>" value="<%= sat2.getCharacter() %>" size="10" readonly></td>
            <td><input name="extras_pu_<%=c%>" type="text" id="extras_pu_<%=c%>" value="<%= sat2.getPU() %>" size="6" readonly></td>
            <td><input name="extras_MUWDCall_scheduled_<%=c%>" type="text" id="extras_MUWDCall_scheduled_<%=c%>" value="<%= sat2.getMUWDCallScheduled() %>" size="6" readonly></td>
            <td><input name="extras_MUWDCall_actual_<%=c%>" type="text" id="extras_MUWDCall_actual_<%=c%>" value="<%= sat2.getMUWDCallActualArrival() %>" size="6" readonly></td>
            <td><input name="extras_mealbreak_<%=c%>" type="text" id="extras_mealbreak_<%=c%>" value="<%= e_meal %>" size="6" readonly></td>
            <td><input name="extras_timewrap_<%=c%>" type="text" id="extras_timewrap_<%=c%>" value="<%= sat2.getTimeWrap() %>" size="6" readonly></td>
            <td><input name="extras_travel_<%=c%>" type="text" id="extras_travel_<%=c%>" value="<%= sat2.getTravel() %>" size="6" readonly></td>
			<td><input name="extras_totalhrs_<%=c%>" type="text" id="extras_totalhrs_<%=c%>" value="<%= e_total_hrs %>" size="6" readonly></td>
            <td width="15" class="right">&nbsp;</td>
          </tr>
          <%}
		  }%></tbody>
          <tr align="center">
            <td colspan="11" class="bottom">&nbsp;</td>
          </tr>
        
      </table></td>
    </tr>
    <tr>
      <td>
        <table width="700" border="0" cellpadding="0" cellspacing="0" id="crew">
          <tbody>
            <tr>
              <td class="header-left">&nbsp;</td>
              <td colspan="11" class="header-middle">Crew Details </td>
              <td class="header-right">&nbsp;</td>
            </tr>
            <tr>
              <th width="15" class="left">&nbsp;</th>
			  <td align="center" valign="top"><strong>Name</strong></td>
              <td align="center" valign="top"><strong>Role</strong></td>
              <td align="center" valign="top"><strong>Call</strong></td>
              <td align="center" valign="top"><strong>Travel In </strong></td>
              <td align="center" valign="top"><strong>Location Call </strong></td>
              <td align="center" valign="top"><strong>Meal Break </strong></td>
              <td align="center" valign="top"><strong>Wrap</strong></td>
              <td align="center" valign="top"><strong>Wrap Loc </strong></td>
              <td align="center" valign="top"><strong>Depart Loc </strong></td>
              <td align="center" valign="top"><strong>Travel out</strong></td>
              <td align="center" valign="top"><strong>Total Hrs </strong></td>
              <th width="15" class="right">&nbsp;</th>
            </tr>
			<% 
			CrewTimeSheetType ctst = dprit.getCrewTimeSheet();
			int b=0;
			if (ctst.getSingleCrew() != null) {
			for(SingleCrewType sct : ctst.getSingleCrew()) {
				b++;
			%>
            <tr align="center">
			<%	String c_departloc = sct.getDepartLoc().toString();
				String c_crewcall = sct.getCrewCall().toString();
				String c_meal = sct.getMealBreak().toString();
				SimpleDateFormat c_sdf = new SimpleDateFormat("HH:mm:ss");
				Date c_departloc_time = c_sdf.parse(c_departloc);
				Date c_crewcall_time = c_sdf.parse(c_crewcall);
				Date c_meal_time = c_sdf.parse(c_meal);
				//get times in seconds
				long c_departloc_hours = c_departloc_time.getHours() *3600;
				long c_departloc_minutes = c_departloc_time.getMinutes() *60;
				long c_departloc_seconds = c_departloc_time.getSeconds();
				long c_departloc_total = c_departloc_hours + c_departloc_minutes + c_departloc_seconds;
				//get times in seconds
				long c_crewcall_hours = c_crewcall_time.getHours() *3600;
				long c_crewcall_minutes = c_crewcall_time.getMinutes() *60;
				long c_crewcall_seconds = c_crewcall_time.getSeconds();
				long c_crewcall_total = c_crewcall_hours + c_crewcall_minutes + c_crewcall_seconds;
				//get times in seconds
				long c_meal_hours = c_meal_time.getHours() *3600;
				long c_meal_minutes = c_meal_time.getMinutes() *60;
				long c_meal_seconds = c_meal_time.getSeconds();
				long c_meal_total = c_meal_hours + c_meal_minutes + c_meal_seconds;
				
				//do the calculation
				long c_dif= c_departloc_total - c_crewcall_total - c_meal_total ;
				if (c_dif <0) {
					c_dif = 0;
					c_meal = "00:00:00";
				}
				//convert to HH:MM:SS format
				long c_hours, c_minutes, c_seconds,c_timeInSeconds;
				c_timeInSeconds=c_dif;
				c_hours = c_timeInSeconds / 3600;
				c_timeInSeconds = c_timeInSeconds - (c_hours * 3600);
				c_minutes = c_timeInSeconds / 60;
				c_timeInSeconds = c_timeInSeconds - (c_minutes * 60);
				c_seconds = c_timeInSeconds;
				String c_hours_string = String.valueOf(c_hours);
				String c_minutes_string = String.valueOf(c_minutes);
				String c_seconds_string = String.valueOf(c_seconds);
				if (c_hours_string.length() == 1) {
					c_hours_string = "0" +  c_hours_string;
				}
				if (c_minutes_string.length() == 1) {
					c_minutes_string = "0" +  c_minutes_string;
				}
				if (c_seconds_string.length() == 1) {
					c_seconds_string = "0" +  c_seconds_string;
				}
				String c_total_hrs = c_hours_string + ":" + c_minutes_string + ":"+ c_seconds_string;
				
				%>	
              <td width="15" class="left">&nbsp;</td>
			  <td><input name="crew_name_<%=b%>" type="text" id="crew_name_<%=b%>" value="<%= sct.getCrewName() %>" size="10" readonly></td>
              <td><input name="crew_role_<%=b%>" type="text" id="crew_role_<%=b%>" value="<%= sct.getCrewRole() %>" size="10" readonly></td>
              <td><input name="crew_call_<%=b%>" type="text" id="crew_call_<%=b%>" value="<%= sct.getCrewCall() %>" size="5" readonly></td>
              <td><input name="crew_travelin_<%=b%>" type="text" id="crew_travelin_<%=b%>" value="<%= sct.getTravelIn() %>" size="5" readonly></td>
              <td><input name="crew_loccall_<%=b%>" type="text" id="crew_loccall_<%=b%>" value="<%= sct.getLocationCall() %>" size="5" readonly></td>
              <td><input name="crew_mealbreak_<%=b%>" type="text" id="crew_mealbreak_<%=b%>" value="<%=c_meal %>" size="5" readonly></td>
              <td><input name="crew_wrap_<%=b%>" type="text" id="crew_wrap_<%=b%>" value="<%= sct.getWrap() %>" size="5" readonly></td>
              <td><input name="crew_wraploc_<%=b%>" type="text" id="crew_wraploc_<%=b%>" value="<%= sct.getWrapLoc() %>" size="5" readonly></td>
              <td><input name="crew_departloc_<%=b%>" type="text" id="crew_departloc_<%=b%>" value="<%= sct.getDepartLoc() %>" size="5" readonly></td>
              <td><input name="crew_travelout_<%=b%>" type="text" id="crew_travelout_<%=b%>" value="<%= sct.getTravelOut() %>" size="5" readonly></td>
			  <td><input name="crew_totalhrs_<%=b%>" type="text" id="crew_totalhrs_<%=b%>" value="<%= c_total_hrs %>" size="5" readonly></td>
              <td width="15" class="right">&nbsp;</td>
            </tr>
			<%}
			}%>
			</tbody>
            <tr align="center">
              <td colspan="13" class="bottom">&nbsp;</td>
            </tr>
      </table></td>
    </tr>
    <tr>
      <td>
	  	<table width="700" border="0" cellpadding="0" cellspacing="0">
       	  <tr>
       	    <td class="header-left">&nbsp;</td>
   	        <td colspan="4" class="header-middle">Catering</td>
   	        <td class="header-right">&nbsp;</td>
       	  </tr>
			<tr>
			  <td width="15" class="left">&nbsp;</td>
			  <td><strong>Meal</strong></td>
			  <td><strong>Time</strong></td>
			  <td><strong>Nos</strong></td>
			  <td><strong>Loc.</strong></td>
			  <td width="15" class="right">&nbsp;</td>
			</tr>
			<% 
			CateringType cat = dprit.getCatering();
			int a=0;
			for(SingleMealType smt : cat.getSingleMeal()) {
				a++;
			%>
				<tr>
				  <td width="15" class="left">&nbsp;</td>
				  <td><input name="catering_meal_<%=a%>" type="text" id="catering_meal_<%=a%>" value="<%= smt.getMeal() %>" readonly></td>
				  <td><input name="catering_time_<%=a%>" type="text" id="catering_time_<%=a%>" value="<%= smt.getTime() %>" size="10" readonly></td>
				  <td><input name="catering_nos_<%=a%>" type="text" id="catering_nos_<%=a%>" value="<%= smt.getNumbers() %>" size="10" readonly></td>
				  <td><input name="catering_loc_<%=a%>" type="text" id="catering_loc_<%=a%>" value="<%= smt.getLocation() %>" size="10" readonly></td>
				  <td width="15" class="right">&nbsp;</td>
				</tr>
			<%
			}
			%>
			<tr><td colspan="6" class="bottom">&nbsp;</td></tr>
   	  </table>	</td></tr>
	<tr>
	  <td><table width="700" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td class="header-left">&nbsp;</td>
          <td class="header-middle">Major Props / Action Vehicles / Additional Equipment </td>
          <td class="header-right">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="left">&nbsp;</td>
          <td align="center"><textarea name="major_props" cols="80" id="major_props" readonly><%= dprit.getMajorPropsActionVehiclesAdditionalEquipment()%></textarea></td>
          <td width="15" class="right">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="3" class="bottom">&nbsp;</td>
        </tr>
      </table></td>
    </tr>
	<tr>
	  <td><table width="700" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td class="header-left">&nbsp;</td>
          <td class="header-middle">Additional Crew </td>
          <td class="header-right">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="left">&nbsp;</td>
          <td align="center"><textarea name="additional_crew" cols="80" id="additional_crew" readonly><%= dprit.getAdditionalCrew()%></textarea></td>
          <td width="15" class="right">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="3" class="bottom">&nbsp;</td>
        </tr>
      </table></td>
    </tr>
	<tr>
	  <td><table width="700" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td class="header-left">&nbsp;</td>
          <td class="header-middle">Livestock / Other</td>
          <td class="header-right">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="left">&nbsp;</td>
          <td align="center"><textarea name="livestock_others" cols="80" id="livestock_others" readonly><%= dprit.getLivestocksOther()%></textarea></td>
          <td width="15" class="right">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="3" class="bottom">&nbsp;</td>
        </tr>
      </table></td>
    </tr>
	<tr>
	  <td><table width="700" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td class="header-left">&nbsp;</td>
          <td class="header-middle">Accidents / Delays</td>
          <td class="header-right">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="left">&nbsp;</td>
          <td align="center"><textarea name="accidents_delays" cols="80" id="accidents_delays" readonly><%= dprit.getAccidentsDelays()%></textarea></td>
          <td width="15" class="right">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="3" class="bottom">&nbsp;</td>
        </tr>
      </table></td>
    </tr>
	<tr>
	  <td><table width="700" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td class="header-left">&nbsp;</td>
          <td class="header-middle">General Remarks </td>
          <td class="header-right">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="left">&nbsp;</td>
          <td align="center"><textarea name="general_remarks" cols="80" id="general_remarks"><%= dprit.getGeneralRemarks()%></textarea></td>
          <td width="15" class="right">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="3" class="bottom">&nbsp;</td>
        </tr>
      </table></td>
    </tr>
	<tr><td>&nbsp;</td></tr>
		  <tr><td align="center" colspan="2">	
				<input type="hidden" name="location_count" id="location_count" value="<%=g%>">
				<input type="hidden" name="slate_count" id="slate_count" value="<%=e%>">
				<input type="hidden" name="scheduled_scenes_shot_count"  id="scheduled_scenes_shot_count" value="<%if(s_1 == 0){out.print("0");}else{out.print(s_1);}%>">
                <input type="hidden" name="scenes_scheduled_not_shot_count" id="scenes_scheduled_not_shot_count" value="<%if (s_2==0) {out.print("0");}else{out.print(s_2);}%>">
                <input type="hidden" name="scenes_not_yet_completed_count" id="scenes_not_yet_completed_count" value="<%if (s_3==0) {out.print("0");}else{out.print(s_3);}%>">
                <input type="hidden" name="scenes_deleted_count" id="scenes_deleted_count" value="<%if (s_4==0) {out.print("0");}else{out.print(s_4);}%>">
                <input type="hidden" name="scenes_added_count" id="scenes_added_count" value="<%if (s_5==0) {out.print("0");}else{out.print(s_5);}%>">
                <input type="hidden" name="unscheduled_scenes_shot_count" id="unscheduled_scenes_shot_count" value="<%if (s_6==0) {out.print("0");}else{out.print(s_6);}%>">
				<input type="hidden" name="artist_count" id="artist_count" value="<%if (d==0) {out.print("1");}else{out.print(d);}%>">
				<input type="hidden" name="extras_count" id="extras_count" value="<%if (c==0) {out.print("0");}else{out.print(c);}%>">
				<input type="hidden" name="crew_count" id="crew_count" value="<%if (b==0) {out.print("1");}else{out.print(b);}%>">
				<input name="catering_count" type="hidden" id="catering_count" value="<%=a%>">
				<input type="hidden" name="workItemID" id="workItemID"/>
				<input type="hidden" name="userID" id="userID"/>
				<input type="hidden" name="sessionHandle" id="sessionHandle"/>
				<input type="hidden" name="submit" id="submit"/>
			    <input type="button" value="Print"  onclick="window.print()"/>
	            <input type="submit" name="Save" value="Save">
            	<input type="submit" name="Submission" value="Submission">
	  </form>
			</td></tr>
		  <tr>
		    <td align="center" colspan="2">		
			<!-- LOAD -->
    <form method="post" action="Create_DPR_10.jsp?formType=load&workItemID=<%= request.getParameter("workItemID") %>&userID=<%= request.getParameter("userID") %>&sessionHandle=<%= request.getParameter("sessionHandle") %>&JSESSIONID=<%= request.getParameter("JSESSIONID") %>&submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
      <table width="60%" border="0" cellspacing="1" cellpadding="1" align="center" class="style1">
        <tr>
          <td align="left"><strong>Select a file to upload :</strong></td>
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
    </tr>
  </table>

<%
if(request.getParameter("Submission") != null){
	DPRinfoType dpri = new DPRinfoType();
	//scheduled date brief
	dpri.setStartDate(XMLGregorianCalendarImpl.parse(request.getParameter("start_date")));
	dpri.setScheduledFinish(XMLGregorianCalendarImpl.parse(request.getParameter("scheduled_finish")));
	
	if(!(request.getParameter("revised_finish").equals(""))){
		dpri.setRevisedFinish(XMLGregorianCalendarImpl.parse(request.getParameter("revised_finish")));
	}
	
	//shooting days schedule
	ShootingDaysScheduleType sds = new ShootingDaysScheduleType();
	sds.setScheduledDays(new BigInteger(request.getParameter("scheduled")));
	sds.setDaysToDate(new BigInteger(request.getParameter("days_to_date")));
	sds.setEstdToComplete(new BigInteger(request.getParameter("est_complete")));
	sds.setEstdTotal(new BigInteger(request.getParameter("est_total")));
	dpri.setShootingDaysSchedule(sds);
	
	//locations sets
	LocationSetsType ls = new LocationSetsType();
	int location_count = Integer.parseInt(request.getParameter("location_count"));
	for(int current_location=1; current_location<=location_count; current_location ++) {
		SingleLocationType sl = new SingleLocationType();
		sl.setLocationName(request.getParameter("location_name_" + current_location));
		sl.setAddress(request.getParameter("location_address_" + current_location));
		int set_count = Integer.parseInt(request.getParameter("location_set_"+ current_location +"_count"));
		for(int current_set=1; current_set<=set_count; current_set ++) {
			sl.getSet().add(request.getParameter("location_set_"+current_location + "_" + current_set));
			//sl.getSet().add("location_set_"+current_location+ "_"+current_set);
		}
		ls.getSingleLocation().add(sl);
	}
	dpri.setLocationSets(ls);
	
	//Scene Schedule
	//slates
	int slates_count = Integer.parseInt(request.getParameter("slate_count"));
	SlateNOs sn = new SlateNOs();
	for(int current_slate=1; current_slate<=slates_count; current_slate ++) {
		sn.getSlate().add(request.getParameter("slate_" + current_slate));
	}
	dpri.setSlateNOs(sn);
	//scheduled scenes shot
	int scheduled_scenes_shot_count = Integer.parseInt(request.getParameter("scheduled_scenes_shot_count"));
	SceneNOsType s1 = new SceneNOsType();
	if(scheduled_scenes_shot_count>0){
		for(int current_scheduled_scenes_shot=1; current_scheduled_scenes_shot<=scheduled_scenes_shot_count; current_scheduled_scenes_shot ++) {
			s1.getScene().add(request.getParameter("scheduled_scenes_shot_" + current_scheduled_scenes_shot));
		}
	}
	dpri.setScheduledScenesShot(s1);
	//scheduled scenes not shot
	int scheduled_scenes_not_shot_count = Integer.parseInt(request.getParameter("scenes_scheduled_not_shot_count"));
	SceneNOsType s2 = new SceneNOsType();
	if(scheduled_scenes_not_shot_count>0){
		for(int current_scheduled_scenes_not_shot=1; current_scheduled_scenes_not_shot<=scheduled_scenes_not_shot_count; current_scheduled_scenes_not_shot ++) {
			s2.getScene().add(request.getParameter("scenes_scheduled_not_shot_" + current_scheduled_scenes_not_shot));
		}
	}
	dpri.setScheduledScenesNotShot(s2);
	//scenes not yet completed
	int scenes_not_yet_completed_count = Integer.parseInt(request.getParameter("scenes_not_yet_completed_count"));
	SceneNOsType s3 = new SceneNOsType();
	if(scenes_not_yet_completed_count>0){
		for(int current_scenes_not_yet_completed=1; current_scenes_not_yet_completed<=scenes_not_yet_completed_count; current_scenes_not_yet_completed ++) {
			s3.getScene().add(request.getParameter("scenes_not_yet_completed_" + current_scenes_not_yet_completed));
		}
	}
	dpri.setScenesNotYetCompleted(s3);
	//scenes deleted
	int scenes_deleted_count = Integer.parseInt(request.getParameter("scenes_deleted_count"));
	SceneNOsType s4 = new SceneNOsType();
	if(scenes_deleted_count > 0){
		for(int current_scenes_deleted=1; current_scenes_deleted<=scenes_deleted_count; current_scenes_deleted ++) {
			s4.getScene().add(request.getParameter("scenes_deleted_" + current_scenes_deleted));
		}
	}
	dpri.setScenesDeleted(s4);
	//scenes added
	int scenes_added_count = Integer.parseInt(request.getParameter("scenes_added_count"));
	SceneNOsType s5 = new SceneNOsType();
	if(scenes_added_count >0){
		for(int current_scenes_added=1; current_scenes_added<=scenes_added_count; current_scenes_added ++) {
			s5.getScene().add(request.getParameter("scenes_added_" + current_scenes_added));
		}
	}
	dpri.setScenesAdded(s5);
	//unscheduled scenes shot
	int unscheduled_scenes_shot_count = Integer.parseInt(request.getParameter("unscheduled_scenes_shot_count"));
	SceneNOsType s6 = new SceneNOsType();
	if(unscheduled_scenes_shot_count > 0){
		for(int current_unscheduled_scenes_shot=1; current_unscheduled_scenes_shot<=unscheduled_scenes_shot_count; current_unscheduled_scenes_shot ++) {
			s6.getScene().add(request.getParameter("unscheduled_scenes_shot_" + current_unscheduled_scenes_shot));
		}
	}
	dpri.setUnscheduledScenesShot(s6);
	
	//Script Timing
	ScriptTimingType st = new ScriptTimingType();
	//Script Timing - Previous Shot
	SingleSriptTimingType sst1 = new SingleSriptTimingType();
	sst1.setScenes(new BigInteger(request.getParameter("prev_scenes")));
	PageTimeType pt1 = new PageTimeType();
	pt1.setNumber(new BigInteger(request.getParameter("prev_pages")));
	pt1.setNumerator(new BigInteger(request.getParameter("prev_pagesnum")));
	sst1.setPageTime(pt1);
	sst1.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("prev_est")));
	sst1.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("prev_actual")));
	st.setPrevShot(sst1);
	
	//Script Timing - Shot Today
	SingleSriptTimingType sst2 = new SingleSriptTimingType();
	sst2.setScenes(new BigInteger(request.getParameter("shot_scenes")));
	PageTimeType pt2 = new PageTimeType();
	pt2.setNumber(new BigInteger(request.getParameter("shot_pages")));
	pt2.setNumerator(new BigInteger(request.getParameter("shot_pagesnum")));
	sst2.setPageTime(pt2);
	sst2.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_est")));
	sst2.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_actual")));
	st.setShotToday(sst2);
	//Script Timing - Shot To Date
	SingleSriptTimingType sst3 = new SingleSriptTimingType();
	sst3.setScenes(new BigInteger(request.getParameter("todate_scenes")));
	PageTimeType pt3 = new PageTimeType();
	pt3.setNumber(new BigInteger(request.getParameter("todate_pages")));
	pt3.setNumerator(new BigInteger(request.getParameter("todate_pagesnum")));
	sst3.setPageTime(pt3);
	sst3.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("todate_est")));
	sst3.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("todate_actual")));
	st.setShotToDate(sst3);
	//Script Timing - To Be Shot
	SingleSriptTimingType sst4 = new SingleSriptTimingType();
	sst4.setScenes(new BigInteger(request.getParameter("tobe_scenes")));
	PageTimeType pt4 = new PageTimeType();
	pt4.setNumber(new BigInteger(request.getParameter("tobe_pages")));
	pt4.setNumerator(new BigInteger(request.getParameter("tobe_pagesnum")));
	sst4.setPageTime(pt4);
	sst4.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("tobe_est")));
	sst4.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("tobe_actual")));
	st.setToBeShot(sst4);
	//Script Timing - Total
	SingleSriptTimingType sst5 = new SingleSriptTimingType();
	sst5.setScenes(new BigInteger(request.getParameter("total_scenes")));
	PageTimeType pt5 = new PageTimeType();
	pt5.setNumber(new BigInteger(request.getParameter("total_pages")));
	pt5.setNumerator(new BigInteger(request.getParameter("total_pagesnum")));
	sst5.setPageTime(pt5);
	sst5.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_est")));
	sst5.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_actual")));
	st.setTotal(sst5);
	//set everything
	dpri.setScriptTiming(st);
	
	//Ratio Timing Spec
	RatioTimingSpecType sts = new RatioTimingSpecType();
	sts.setScheduledRatio(new Double(request.getParameter("scheduled_ratio")));
	sts.setDailyRatio(new Double(request.getParameter("daily_ratio")));
	sts.setAverageRatio(new Double(request.getParameter("average_ratio")));
	sts.setAverageTiming(XMLGregorianCalendarImpl.parse(request.getParameter("average_timing")));
	CumulativeType cu = new CumulativeType();
	if (request.getParameter("cumulative_sign").equals("-")){
		cu.setSign(false);
	}else{
		cu.setSign(true);
	}
	cu.setVarTime(XMLGregorianCalendarImpl.parse(request.getParameter("cumulative")));
	sts.setCumulative(cu);
	sts.setOriginalTiming(XMLGregorianCalendarImpl.parse(request.getParameter("original_timing")));
	dpri.setRatioTimingSpec(sts);
	
	//Stock Info
	StockInfoType si = new StockInfoType();
	//previously
	SingleStockInfoType ssi1 = new SingleStockInfoType();
	ssi1.setLoaded(new BigInteger(request.getParameter("previously_loaded")));
	ssi1.setGross(new BigInteger(request.getParameter("previously_gross")));
	ssi1.setExposed(new BigInteger(request.getParameter("previously_exposed")));
	ssi1.setPrint(new BigInteger(request.getParameter("previously_print")));
	ssi1.setNG(new BigInteger(request.getParameter("previously_ng")));
	ssi1.setWaste(new BigInteger(request.getParameter("previously_waste")));
	ssi1.setShortEnds(new BigInteger(request.getParameter("previously_shortend")));
	ssi1.setSoundRolls(request.getParameter("previously_soundrolls"));
	ssi1.setCamRolls(request.getParameter("previously_camrolls"));
	si.setPreviously(ssi1);
	//today
	SingleStockInfoType ssi2 = new SingleStockInfoType();
	ssi2.setLoaded(new BigInteger(request.getParameter("today_loaded")));
	ssi2.setGross(new BigInteger(request.getParameter("today_gross")));
	ssi2.setExposed(new BigInteger(request.getParameter("today_exposed")));
	ssi2.setPrint(new BigInteger(request.getParameter("today_print")));
	ssi2.setNG(new BigInteger(request.getParameter("today_ng")));
	ssi2.setWaste(new BigInteger(request.getParameter("today_waste")));
	ssi2.setShortEnds(new BigInteger(request.getParameter("today_shortend")));
	ssi2.setSoundRolls(request.getParameter("today_soundrolls"));
	ssi2.setCamRolls(request.getParameter("today_camrolls"));
	si.setToday(ssi2);
	//total
	SingleStockInfoType ssi3 = new SingleStockInfoType();
	ssi3.setLoaded(new BigInteger(request.getParameter("total_loaded")));
	ssi3.setGross(new BigInteger(request.getParameter("total_gross")));
	ssi3.setExposed(new BigInteger(request.getParameter("total_exposed")));
	ssi3.setPrint(new BigInteger(request.getParameter("total_print")));
	ssi3.setNG(new BigInteger(request.getParameter("total_ng")));
	ssi3.setWaste(new BigInteger(request.getParameter("total_waste")));
	ssi3.setShortEnds(new BigInteger(request.getParameter("total_shortend")));
	ssi3.setSoundRolls(request.getParameter("total_soundrolls"));
	ssi3.setCamRolls(request.getParameter("total_camrolls"));
	si.setTotalToDate(ssi3);
	dpri.setStockInfo(si);
	
	//Artist Details
	ArtistTimeSheetType ats1 = new ArtistTimeSheetType();
	int artist_count = Integer.parseInt(request.getParameter("artist_count"));
	for(int current_artist=1; current_artist<=artist_count; current_artist++){//getting the crew information
		if(!(request.getParameter("artist_artist_" + current_artist).equals(""))){
			SingleArtistType sa1 = new SingleArtistType();
			sa1.setArtist(request.getParameter("artist_artist_" + current_artist));
			sa1.setCharacter(request.getParameter("artist_character_" + current_artist));
			sa1.setPU(request.getParameter("artist_pu_"+ current_artist));
			sa1.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("artist_MUWDCall_scheduled_"+ current_artist)));
			sa1.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("artist_MUWDCall_actual_"+ current_artist)));
			sa1.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("artist_mealbreak_"+ current_artist)));
			sa1.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("artist_timewrap_"+ current_artist)));
			sa1.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("artist_travel_"+ current_artist)));
			sa1.setTotalHRs(XMLGregorianCalendarImpl.parse(request.getParameter("artist_totalhrs_"+ current_artist)));
			ats1.getSingleArtist().add(sa1);
		}
	}
	dpri.setArtistTimeSheet(ats1);
	
	//Extras Details
	ArtistTimeSheetType ats2 = new ArtistTimeSheetType();
	int extras_count = Integer.parseInt(request.getParameter("extras_count"));
	if(extras_count > 0){
		for(int current_extras=1; current_extras<=extras_count; current_extras++){//getting the crew information
			SingleArtistType sa2 = new SingleArtistType();
			sa2.setArtist(request.getParameter("extras_artist_" + current_extras));
			sa2.setCharacter(request.getParameter("extras_character_" + current_extras));
			sa2.setPU(request.getParameter("extras_pu_"+ current_extras));
			sa2.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("extras_MUWDCall_scheduled_"+ current_extras)));
			sa2.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("extras_MUWDCall_actual_"+ current_extras)));
			sa2.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("extras_mealbreak_"+ current_extras)));
			sa2.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("extras_timewrap_"+ current_extras)));
			sa2.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("extras_travel_"+ current_extras)));
			sa2.setTotalHRs(XMLGregorianCalendarImpl.parse(request.getParameter("extras_totalhrs_"+ current_extras)));
			ats2.getSingleArtist().add(sa2);
		}
		dpri.setExtrasTimeSheet(ats2);
	}else{
	dpri.setExtrasTimeSheet(ats2);
	}
	
	//Crew Details
	CrewTimeSheetType cwts = new CrewTimeSheetType();
	int crew_count = Integer.parseInt(request.getParameter("crew_count"));
	if(!(request.getParameter("crew_name_1").equals(""))){
		for(int current_crew=1; current_crew<=crew_count; current_crew++){//getting the crew information
			SingleCrewType scw = new SingleCrewType();
			scw.setCrewName(request.getParameter("crew_name_" + current_crew));
			scw.setCrewRole(request.getParameter("crew_role_" + current_crew));
			scw.setCrewCall(XMLGregorianCalendarImpl.parse(request.getParameter("crew_call_"+ current_crew)));
			scw.setTravelIn(XMLGregorianCalendarImpl.parse(request.getParameter("crew_travelin_"+ current_crew)));
			scw.setLocationCall(XMLGregorianCalendarImpl.parse(request.getParameter("crew_loccall_"+ current_crew)));
			scw.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("crew_mealbreak_"+ current_crew)));
			scw.setWrap(XMLGregorianCalendarImpl.parse(request.getParameter("crew_wrap_"+ current_crew)));
			scw.setWrapLoc(XMLGregorianCalendarImpl.parse(request.getParameter("crew_wraploc_"+ current_crew)));
			scw.setDepartLoc(XMLGregorianCalendarImpl.parse(request.getParameter("crew_departloc_"+ current_crew)));
			scw.setTravelOut(XMLGregorianCalendarImpl.parse(request.getParameter("crew_travelout_"+ current_crew)));
			scw.setTotalHRs(XMLGregorianCalendarImpl.parse(request.getParameter("crew_totalhrs_"+ current_crew)));
			cwts.getSingleCrew().add(scw);
		}
	}
	dpri.setCrewTimeSheet(cwts);
	
	//catering details
	CateringType ca = new CateringType();
	int catering_count = Integer.parseInt(request.getParameter("catering_count"));
	for(int current_meal=1; current_meal<=catering_count; current_meal ++) {
		SingleMealType sm = new SingleMealType();
		sm.setMeal(request.getParameter("catering_meal_" + current_meal));
		sm.setTime(XMLGregorianCalendarImpl.parse(request.getParameter("catering_time_" + current_meal)));
		sm.setNumbers(new BigInteger(request.getParameter("catering_nos_" + current_meal)));
		sm.setLocation(request.getParameter("catering_loc_" + current_meal));
		ca.getSingleMeal().add(sm);
	}
	dpri.setCatering(ca);
	
	//general comments
	dpri.setMajorPropsActionVehiclesAdditionalEquipment(request.getParameter("major_props"));
	dpri.setAdditionalCrew(request.getParameter("additional_crew"));
	dpri.setLivestocksOther(request.getParameter("livestock_others"));
	dpri.setAccidentsDelays(request.getParameter("accidents_delays"));
	dpri.setGeneralRemarks(request.getParameter("general_remarks"));

	//general info
	GeneralInfoType gi = new GeneralInfoType();
	gi.setProduction(request.getParameter("production"));
	gi.setDate(XMLGregorianCalendarImpl.parse(request.getParameter("date")));
	gi.setWeekday(request.getParameter("day"));
	gi.setShootDayNo(new BigInteger(request.getParameter("shoot_day_no")));
	
	//compile everything
	cdprt.setGeneralInfo(gi);
	cdprt.setProducer(request.getParameter("producer"));
	cdprt.setDirector(request.getParameter("director"));
	cdprt.setProductionManager(request.getParameter("prod_manager"));
	cdprt.setDirectorOfPhotography(request.getParameter("dop"));
	cdprt.setDPRinfo(dpri);
	
	
	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    File f = new File("./backup/DPR_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
    m.marshal( cdprtElement,  f);//output to file
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(cdprtElement, xmlOS);//out to ByteArray
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
	
DPRinfoType dpri = new DPRinfoType();
	//scheduled date brief
	dpri.setStartDate(XMLGregorianCalendarImpl.parse(request.getParameter("start_date")));
	dpri.setScheduledFinish(XMLGregorianCalendarImpl.parse(request.getParameter("scheduled_finish")));
	
	if(!(request.getParameter("revised_finish").equals(""))){
		dpri.setRevisedFinish(XMLGregorianCalendarImpl.parse(request.getParameter("revised_finish")));
	}
	
	//shooting days schedule
	ShootingDaysScheduleType sds = new ShootingDaysScheduleType();
	sds.setScheduledDays(new BigInteger(request.getParameter("scheduled")));
	sds.setDaysToDate(new BigInteger(request.getParameter("days_to_date")));
	sds.setEstdToComplete(new BigInteger(request.getParameter("est_complete")));
	sds.setEstdTotal(new BigInteger(request.getParameter("est_total")));
	dpri.setShootingDaysSchedule(sds);
	
	//locations sets
	LocationSetsType ls = new LocationSetsType();
	int location_count = Integer.parseInt(request.getParameter("location_count"));
	for(int current_location=1; current_location<=location_count; current_location ++) {
		SingleLocationType sl = new SingleLocationType();
		sl.setLocationName(request.getParameter("location_name_" + current_location));
		sl.setAddress(request.getParameter("location_address_" + current_location));
		int set_count = Integer.parseInt(request.getParameter("location_set_"+ current_location +"_count"));
		for(int current_set=1; current_set<=set_count; current_set ++) {
			sl.getSet().add(request.getParameter("location_set_"+current_location + "_" + current_set));
			//sl.getSet().add("location_set_"+current_location+ "_"+current_set);
		}
		ls.getSingleLocation().add(sl);
	}
	dpri.setLocationSets(ls);
	
	//Scene Schedule
	//slates
	int slates_count = Integer.parseInt(request.getParameter("slate_count"));
	SlateNOs sn = new SlateNOs();
	for(int current_slate=1; current_slate<=slates_count; current_slate ++) {
		sn.getSlate().add(request.getParameter("slate_" + current_slate));
	}
	dpri.setSlateNOs(sn);
	//scheduled scenes shot
	int scheduled_scenes_shot_count = Integer.parseInt(request.getParameter("scheduled_scenes_shot_count"));
	SceneNOsType s1 = new SceneNOsType();
	if(scheduled_scenes_shot_count>0){
		for(int current_scheduled_scenes_shot=1; current_scheduled_scenes_shot<=scheduled_scenes_shot_count; current_scheduled_scenes_shot ++) {
			s1.getScene().add(request.getParameter("scheduled_scenes_shot_" + current_scheduled_scenes_shot));
		}
	}
	dpri.setScheduledScenesShot(s1);
	//scheduled scenes not shot
	int scheduled_scenes_not_shot_count = Integer.parseInt(request.getParameter("scenes_scheduled_not_shot_count"));
	SceneNOsType s2 = new SceneNOsType();
	if(scheduled_scenes_not_shot_count>0){
		for(int current_scheduled_scenes_not_shot=1; current_scheduled_scenes_not_shot<=scheduled_scenes_not_shot_count; current_scheduled_scenes_not_shot ++) {
			s2.getScene().add(request.getParameter("scenes_scheduled_not_shot_" + current_scheduled_scenes_not_shot));
		}
	}
	dpri.setScheduledScenesNotShot(s2);
	//scenes not yet completed
	int scenes_not_yet_completed_count = Integer.parseInt(request.getParameter("scenes_not_yet_completed_count"));
	SceneNOsType s3 = new SceneNOsType();
	if(scenes_not_yet_completed_count>0){
		for(int current_scenes_not_yet_completed=1; current_scenes_not_yet_completed<=scenes_not_yet_completed_count; current_scenes_not_yet_completed ++) {
			s3.getScene().add(request.getParameter("scenes_not_yet_completed_" + current_scenes_not_yet_completed));
		}
	}
	dpri.setScenesNotYetCompleted(s3);
	//scenes deleted
	int scenes_deleted_count = Integer.parseInt(request.getParameter("scenes_deleted_count"));
	SceneNOsType s4 = new SceneNOsType();
	if(scenes_deleted_count > 0){
		for(int current_scenes_deleted=1; current_scenes_deleted<=scenes_deleted_count; current_scenes_deleted ++) {
			s4.getScene().add(request.getParameter("scenes_deleted_" + current_scenes_deleted));
		}
	}
	dpri.setScenesDeleted(s4);
	//scenes added
	int scenes_added_count = Integer.parseInt(request.getParameter("scenes_added_count"));
	SceneNOsType s5 = new SceneNOsType();
	if(scenes_added_count >0){
		for(int current_scenes_added=1; current_scenes_added<=scenes_added_count; current_scenes_added ++) {
			s5.getScene().add(request.getParameter("scenes_added_" + current_scenes_added));
		}
	}
	dpri.setScenesAdded(s5);
	//unscheduled scenes shot
	int unscheduled_scenes_shot_count = Integer.parseInt(request.getParameter("unscheduled_scenes_shot_count"));
	SceneNOsType s6 = new SceneNOsType();
	if(unscheduled_scenes_shot_count > 0){
		for(int current_unscheduled_scenes_shot=1; current_unscheduled_scenes_shot<=unscheduled_scenes_shot_count; current_unscheduled_scenes_shot ++) {
			s6.getScene().add(request.getParameter("unscheduled_scenes_shot_" + current_unscheduled_scenes_shot));
		}
	}
	dpri.setUnscheduledScenesShot(s6);
	
	//Script Timing
	ScriptTimingType st = new ScriptTimingType();
	//Script Timing - Previous Shot
	SingleSriptTimingType sst1 = new SingleSriptTimingType();
	sst1.setScenes(new BigInteger(request.getParameter("prev_scenes")));
	PageTimeType pt1 = new PageTimeType();
	pt1.setNumber(new BigInteger(request.getParameter("prev_pages")));
	pt1.setNumerator(new BigInteger(request.getParameter("prev_pagesnum")));
	sst1.setPageTime(pt1);
	sst1.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("prev_est")));
	sst1.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("prev_actual")));
	st.setPrevShot(sst1);
	
	//Script Timing - Shot Today
	SingleSriptTimingType sst2 = new SingleSriptTimingType();
	sst2.setScenes(new BigInteger(request.getParameter("shot_scenes")));
	PageTimeType pt2 = new PageTimeType();
	pt2.setNumber(new BigInteger(request.getParameter("shot_pages")));
	pt2.setNumerator(new BigInteger(request.getParameter("shot_pagesnum")));
	sst2.setPageTime(pt2);
	sst2.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_est")));
	sst2.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("shot_actual")));
	st.setShotToday(sst2);
	//Script Timing - Shot To Date
	SingleSriptTimingType sst3 = new SingleSriptTimingType();
	sst3.setScenes(new BigInteger(request.getParameter("todate_scenes")));
	PageTimeType pt3 = new PageTimeType();
	pt3.setNumber(new BigInteger(request.getParameter("todate_pages")));
	pt3.setNumerator(new BigInteger(request.getParameter("todate_pagesnum")));
	sst3.setPageTime(pt3);
	sst3.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("todate_est")));
	sst3.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("todate_actual")));
	st.setShotToDate(sst3);
	//Script Timing - To Be Shot
	SingleSriptTimingType sst4 = new SingleSriptTimingType();
	sst4.setScenes(new BigInteger(request.getParameter("tobe_scenes")));
	PageTimeType pt4 = new PageTimeType();
	pt4.setNumber(new BigInteger(request.getParameter("tobe_pages")));
	pt4.setNumerator(new BigInteger(request.getParameter("tobe_pagesnum")));
	sst4.setPageTime(pt4);
	sst4.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("tobe_est")));
	sst4.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("tobe_actual")));
	st.setToBeShot(sst4);
	//Script Timing - Total
	SingleSriptTimingType sst5 = new SingleSriptTimingType();
	sst5.setScenes(new BigInteger(request.getParameter("total_scenes")));
	PageTimeType pt5 = new PageTimeType();
	pt5.setNumber(new BigInteger(request.getParameter("total_pages")));
	pt5.setNumerator(new BigInteger(request.getParameter("total_pagesnum")));
	sst5.setPageTime(pt5);
	sst5.setEstTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_est")));
	sst5.setActualTiming(XMLGregorianCalendarImpl.parse(request.getParameter("total_actual")));
	st.setTotal(sst5);
	//set everything
	dpri.setScriptTiming(st);
	
	//Ratio Timing Spec
	RatioTimingSpecType sts = new RatioTimingSpecType();
	sts.setScheduledRatio(new Double(request.getParameter("scheduled_ratio")));
	sts.setDailyRatio(new Double(request.getParameter("daily_ratio")));
	sts.setAverageRatio(new Double(request.getParameter("average_ratio")));
	sts.setAverageTiming(XMLGregorianCalendarImpl.parse(request.getParameter("average_timing")));
	CumulativeType cu = new CumulativeType();
	if (request.getParameter("cumulative_sign").equals("-")){
		cu.setSign(false);
	}else{
		cu.setSign(true);
	}
	cu.setVarTime(XMLGregorianCalendarImpl.parse(request.getParameter("cumulative")));
	sts.setCumulative(cu);
	sts.setOriginalTiming(XMLGregorianCalendarImpl.parse(request.getParameter("original_timing")));
	dpri.setRatioTimingSpec(sts);
	
	//Stock Info
	StockInfoType si = new StockInfoType();
	//previously
	SingleStockInfoType ssi1 = new SingleStockInfoType();
	ssi1.setLoaded(new BigInteger(request.getParameter("previously_loaded")));
	ssi1.setGross(new BigInteger(request.getParameter("previously_gross")));
	ssi1.setExposed(new BigInteger(request.getParameter("previously_exposed")));
	ssi1.setPrint(new BigInteger(request.getParameter("previously_print")));
	ssi1.setNG(new BigInteger(request.getParameter("previously_ng")));
	ssi1.setWaste(new BigInteger(request.getParameter("previously_waste")));
	ssi1.setShortEnds(new BigInteger(request.getParameter("previously_shortend")));
	ssi1.setSoundRolls(request.getParameter("previously_soundrolls"));
	ssi1.setCamRolls(request.getParameter("previously_camrolls"));
	si.setPreviously(ssi1);
	//today
	SingleStockInfoType ssi2 = new SingleStockInfoType();
	ssi2.setLoaded(new BigInteger(request.getParameter("today_loaded")));
	ssi2.setGross(new BigInteger(request.getParameter("today_gross")));
	ssi2.setExposed(new BigInteger(request.getParameter("today_exposed")));
	ssi2.setPrint(new BigInteger(request.getParameter("today_print")));
	ssi2.setNG(new BigInteger(request.getParameter("today_ng")));
	ssi2.setWaste(new BigInteger(request.getParameter("today_waste")));
	ssi2.setShortEnds(new BigInteger(request.getParameter("today_shortend")));
	ssi2.setSoundRolls(request.getParameter("today_soundrolls"));
	ssi2.setCamRolls(request.getParameter("today_camrolls"));
	si.setToday(ssi2);
	//total
	SingleStockInfoType ssi3 = new SingleStockInfoType();
	ssi3.setLoaded(new BigInteger(request.getParameter("total_loaded")));
	ssi3.setGross(new BigInteger(request.getParameter("total_gross")));
	ssi3.setExposed(new BigInteger(request.getParameter("total_exposed")));
	ssi3.setPrint(new BigInteger(request.getParameter("total_print")));
	ssi3.setNG(new BigInteger(request.getParameter("total_ng")));
	ssi3.setWaste(new BigInteger(request.getParameter("total_waste")));
	ssi3.setShortEnds(new BigInteger(request.getParameter("total_shortend")));
	ssi3.setSoundRolls(request.getParameter("total_soundrolls"));
	ssi3.setCamRolls(request.getParameter("total_camrolls"));
	si.setTotalToDate(ssi3);
	dpri.setStockInfo(si);
	
	//Artist Details
	ArtistTimeSheetType ats1 = new ArtistTimeSheetType();
	int artist_count = Integer.parseInt(request.getParameter("artist_count"));
	for(int current_artist=1; current_artist<=artist_count; current_artist++){//getting the crew information
		if(!(request.getParameter("artist_artist_" + current_artist).equals(""))){
			SingleArtistType sa1 = new SingleArtistType();
			sa1.setArtist(request.getParameter("artist_artist_" + current_artist));
			sa1.setCharacter(request.getParameter("artist_character_" + current_artist));
			sa1.setPU(request.getParameter("artist_pu_"+ current_artist));
			sa1.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("artist_MUWDCall_scheduled_"+ current_artist)));
			sa1.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("artist_MUWDCall_actual_"+ current_artist)));
			sa1.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("artist_mealbreak_"+ current_artist)));
			sa1.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("artist_timewrap_"+ current_artist)));
			sa1.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("artist_travel_"+ current_artist)));
			sa1.setTotalHRs(XMLGregorianCalendarImpl.parse(request.getParameter("artist_totalhrs_"+ current_artist)));
			ats1.getSingleArtist().add(sa1);
		}
	}
	dpri.setArtistTimeSheet(ats1);
	
	//Extras Details
	ArtistTimeSheetType ats2 = new ArtistTimeSheetType();
	int extras_count = Integer.parseInt(request.getParameter("extras_count"));
	if(extras_count > 0){
		for(int current_extras=1; current_extras<=extras_count; current_extras++){//getting the crew information
			SingleArtistType sa2 = new SingleArtistType();
			sa2.setArtist(request.getParameter("extras_artist_" + current_extras));
			sa2.setCharacter(request.getParameter("extras_character_" + current_extras));
			sa2.setPU(request.getParameter("extras_pu_"+ current_extras));
			sa2.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("extras_MUWDCall_scheduled_"+ current_extras)));
			sa2.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("extras_MUWDCall_actual_"+ current_extras)));
			sa2.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("extras_mealbreak_"+ current_extras)));
			sa2.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("extras_timewrap_"+ current_extras)));
			sa2.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("extras_travel_"+ current_extras)));
			sa2.setTotalHRs(XMLGregorianCalendarImpl.parse(request.getParameter("extras_totalhrs_"+ current_extras)));
			ats2.getSingleArtist().add(sa2);
		}
		dpri.setExtrasTimeSheet(ats2);
	}else{
	dpri.setExtrasTimeSheet(ats2);
	}
	
	
	//Crew Details
	CrewTimeSheetType cwts = new CrewTimeSheetType();
	int crew_count = Integer.parseInt(request.getParameter("crew_count"));
	if(!(request.getParameter("crew_name_1").equals(""))){
		for(int current_crew=1; current_crew<=crew_count; current_crew++){//getting the crew information
			SingleCrewType scw = new SingleCrewType();
			scw.setCrewName(request.getParameter("crew_name_" + current_crew));
			scw.setCrewRole(request.getParameter("crew_role_" + current_crew));
			scw.setCrewCall(XMLGregorianCalendarImpl.parse(request.getParameter("crew_call_"+ current_crew)));
			scw.setTravelIn(XMLGregorianCalendarImpl.parse(request.getParameter("crew_travelin_"+ current_crew)));
			scw.setLocationCall(XMLGregorianCalendarImpl.parse(request.getParameter("crew_loccall_"+ current_crew)));
			scw.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("crew_mealbreak_"+ current_crew)));
			scw.setWrap(XMLGregorianCalendarImpl.parse(request.getParameter("crew_wrap_"+ current_crew)));
			scw.setWrapLoc(XMLGregorianCalendarImpl.parse(request.getParameter("crew_wraploc_"+ current_crew)));
			scw.setDepartLoc(XMLGregorianCalendarImpl.parse(request.getParameter("crew_departloc_"+ current_crew)));
			scw.setTravelOut(XMLGregorianCalendarImpl.parse(request.getParameter("crew_travelout_"+ current_crew)));
			scw.setTotalHRs(XMLGregorianCalendarImpl.parse(request.getParameter("crew_totalhrs_"+ current_crew)));
			cwts.getSingleCrew().add(scw);
		}
	}
	dpri.setCrewTimeSheet(cwts);
	
	//catering details
	CateringType ca = new CateringType();
	int catering_count = Integer.parseInt(request.getParameter("catering_count"));
	for(int current_meal=1; current_meal<=catering_count; current_meal ++) {
		SingleMealType sm = new SingleMealType();
		sm.setMeal(request.getParameter("catering_meal_" + current_meal));
		sm.setTime(XMLGregorianCalendarImpl.parse(request.getParameter("catering_time_" + current_meal)));
		sm.setNumbers(new BigInteger(request.getParameter("catering_nos_" + current_meal)));
		sm.setLocation(request.getParameter("catering_loc_" + current_meal));
		ca.getSingleMeal().add(sm);
	}
	dpri.setCatering(ca);
	
	//general comments
	dpri.setMajorPropsActionVehiclesAdditionalEquipment(request.getParameter("major_props"));
	dpri.setAdditionalCrew(request.getParameter("additional_crew"));
	dpri.setLivestocksOther(request.getParameter("livestock_others"));
	dpri.setAccidentsDelays(request.getParameter("accidents_delays"));
	dpri.setGeneralRemarks(request.getParameter("general_remarks"));

	//general info
	GeneralInfoType gi = new GeneralInfoType();
	gi.setProduction(request.getParameter("production"));
	gi.setDate(XMLGregorianCalendarImpl.parse(request.getParameter("date")));
	gi.setWeekday(request.getParameter("day"));
	gi.setShootDayNo(new BigInteger(request.getParameter("shoot_day_no")));
	
	//compile everything
	cdprt.setGeneralInfo(gi);
	cdprt.setProducer(request.getParameter("producer"));
	cdprt.setDirector(request.getParameter("director"));
	cdprt.setProductionManager(request.getParameter("prod_manager"));
	cdprt.setDirectorOfPhotography(request.getParameter("dop"));
	cdprt.setDPRinfo(dpri);
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(cdprtElement, xmlOS);//out to ByteArray

	response.setHeader("Content-Disposition", "attachment;filename=\"DPR_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
	response.setHeader("Content-Type", "text/xml");

	ServletOutputStream outs = response.getOutputStream();
	xmlOS.writeTo(outs);
	outs.close();
}
%>
</body>
</html>
