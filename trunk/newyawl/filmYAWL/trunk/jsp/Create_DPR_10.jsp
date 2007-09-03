<%@ page import="java.util.List" %>
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
<%@ page import="org.yawlfoundation.sb.dprinfo.*"%>
<%@ page buffer="2048kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Daily Progress Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="javascript">
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
<h1>Daily Progress Report</h1>
<%	
			//String xml = "<?xml version='1.0' encoding='UTF-8'?><ns2:Create_DPR xmlns:ns2='http://www.yawlfoundation.org/sb/DPRinfo' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.yawlfoundation.org/sb/DPRinfo Create_DPR_Type.xsd'><generalInfo><production>production</production><date>2001-01-01</date><weekday>weekday</weekday><shootDayNo>0</shootDayNo></generalInfo><producer>producer</producer><director>director</director><productionManager>productionManager</productionManager><directorOfPhotography>directorOfPhotography</directorOfPhotography><DPRinfo><startDate>2001-01-01</startDate><scheduledFinish>2001-01-01</scheduledFinish><revisedFinish>2001-01-01</revisedFinish><callWrapHRs><MU_Call>12:00:00</MU_Call><WD_Call>12:00:00</WD_Call><crewCall>12:00:00</crewCall><firstSetUp>12:00:00</firstSetUp><lunch>12:00:00</lunch><wrapLocation>12:00:00</wrapLocation><MU_Wrap>12:00:00</MU_Wrap><WD_Wrap>12:00:00</WD_Wrap><MU_Hours>12:00:00</MU_Hours><WD_Hours>12:00:00</WD_Hours><totalCrewHours>12:00:00</totalCrewHours></callWrapHRs><locationSet><singleLocationSet><number>0</number><setName>setName</setName><location>location</location><address>address</address></singleLocationSet><singleLocationSet><number>0</number><setName>setName</setName><location>location</location><address>address</address></singleLocationSet></locationSet><slateNOs><slate>0</slate></slateNOs><scenesShotInfo><scenesShotToday><scene>scene</scene></scenesShotToday><scenesScheduledNotShot><scene>scene</scene></scenesScheduledNotShot><scenesAdded><scene>scene</scene></scenesAdded><scenesDeleted><scene>scene</scene></scenesDeleted></scenesShotInfo><shootingDaysSchedule><scheduledDays>0</scheduledDays><daysToDate>0</daysToDate><estdToComplete>0</estdToComplete><estdTotal>0</estdTotal></shootingDaysSchedule><sceneShotSchedule><prevShot><scenes>0</scenes><estd>12:00:00</estd><actual>12:00:00</actual></prevShot><shotToday><scenes>0</scenes><estd>12:00:00</estd><actual>12:00:00</actual></shotToday><toDate><scenes>0</scenes><estd>12:00:00</estd><actual>12:00:00</actual></toDate><toBeShot><scenes>0</scenes><estd>12:00:00</estd><actual>12:00:00</actual></toBeShot><total><scenes>0</scenes><estd>12:00:00</estd><actual>12:00:00</actual></total></sceneShotSchedule><ratioTimingSpec><scheduledRatio>scheduledRatio</scheduledRatio><dailyRatio>dailyRatio</dailyRatio><averageRatio>averageRatio</averageRatio><averageTiming>averageTiming</averageTiming><cumulative>cumulative</cumulative><originalTiming>12:00:00</originalTiming></ratioTimingSpec><stockInfo><previously><loaded>0</loaded><gross>0</gross><exposed>0</exposed><print>0</print><N_G>0</N_G><waste>0</waste><shotEnds>0</shotEnds><sound>0</sound></previously><today><loaded>0</loaded><gross>0</gross><exposed>0</exposed><print>0</print><N_G>0</N_G><waste>0</waste><shotEnds>0</shotEnds><sound>0</sound></today><totalToDate><loaded>0</loaded><gross>0</gross> <exposed>0</exposed><print>0</print><N_G>0</N_G><waste>0</waste><shotEnds>0</shotEnds><sound>0</sound></totalToDate></stockInfo><castTimeSheetInfo><singleCastTimeSheetInfo><cast>cast</cast><character>character</character><pickUp>pickUp</pickUp><MU_WD_Call>12:00:00</MU_WD_Call><travelTime>travelTime</travelTime><wrap>12:00:00</wrap><lunchBreak>12:00:00</lunchBreak><totalHours>12:00:00</totalHours><extrasNOs>0</extrasNOs><HRs>12:00:00</HRs></singleCastTimeSheetInfo><singleCastTimeSheetInfo><cast>cast</cast><character>character</character><pickUp>pickUp</pickUp><MU_WD_Call>12:00:00</MU_WD_Call><travelTime>travelTime</travelTime><wrap>12:00:00</wrap><lunchBreak>12:00:00</lunchBreak><totalHours>12:00:00</totalHours><extrasNOs>0</extrasNOs><HRs>12:00:00</HRs></singleCastTimeSheetInfo></castTimeSheetInfo><majorProps_actionVehicles_additionalEquipment>majorProps_actionVehicles_additionalEquipment</majorProps_actionVehicles_additionalEquipment><additionalCrew>additionalCrew</additionalCrew><livestocks_other>livestocks_other</livestocks_other><accidents_delays>accidents_delays</accidents_delays><catering><singleMeal><meal>meal</meal><time>12:00:00</time><numbers>0</numbers><location>location</location></singleMeal> <singleMeal><meal>meal</meal><time>12:00:00</time><numbers>0</numbers><location>location</location></singleMeal></catering><generalRemarks_Notes>generalRemarks_Notes</generalRemarks_Notes></DPRinfo></ns2:Create_DPR>";
			String xml = (String)session.getAttribute("outputData");
			xml = xml.replaceAll("<Create_DPR", "<ns2:Create_DPR xmlns:ns2='http://www.yawlfoundation.org/sb/DPRinfo'");
			xml = xml.replaceAll("</Create_DPR","</ns2:Create_DPR");
			
			ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
			JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.dprinfo");
			Unmarshaller u = jc.createUnmarshaller();
			JAXBElement cdprtElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
			CreateDPRType cdprt = (CreateDPRType)cdprtElement.getValue();

			GeneralInfoType git = cdprt.getGeneralInfo();
			DPRinfoType dprit = cdprt.getDPRinfo();
			CallWrapHRsType cwht = dprit.getCallWrapHRs();
			ShootingDaysScheduleType sdst = dprit.getShootingDaysSchedule();
			RatioTimingSpecType rtst = dprit.getRatioTimingSpec();
%>


<form name="form1" method="post">
  <table width="900"  border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td colspan="2">
        <table width="900" border="0" cellpadding="0" cellspacing="0">
          <tr><td colspan="6"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td></tr>
          <tr>
		  	<td width="15" class="leftbox">&nbsp;</td>
            <td><strong>Producer</strong></td><td><input name="producer" type="text" id="producer" value="<%= cdprt.getProducer() %>"></td>
            <td><strong>Production</strong></td><td><input name="production" type="text" id="production" value="<%= git.getProduction() %>"></td>
            <td width="15" class="rightbox">&nbsp;</td>
          </tr>
          <tr>
            <td width="15" class="leftbox">&nbsp;</td>
			<td><strong>Director</strong></td><td><input name="director" type="text" id="director" value="<%= cdprt.getDirector()%>"></td>
            <td><strong>Day</strong></td><td><input name="day" type="text" id="day" value="<%= git.getWeekday()%>"></td>
            <td width="15" class="rightbox">&nbsp;</td>
          </tr>
          <tr>
            <td width="15" class="leftbox">&nbsp;</td>
			<td><strong>Prod. Manager</strong></td><td><input name="prod_manager" type="text" id="prod_manager" value="<%= cdprt.getProductionManager()%>"></td>
            <td><strong>Date</strong></td><td><input name="date" type="text" id="date" value="<%= git.getDate()%>"></td>
            <td width="15" class="rightbox">&nbsp;</td>
          </tr>
          <tr>
            <td width="15" class="leftbox">&nbsp;</td>
			<td><strong>D.O.P</strong></td><td><input name="dop" type="text" id="dop" value="<%= cdprt.getDirectorOfPhotography()%>"></td>
            <td><strong>Shoot Day No. </strong></td><td><input name="shoot_day_no" type="text" id="shoot_day_no" value="<%= git.getShootDayNo() %>"></td>
            <td width="15" class="rightbox">&nbsp;</td>
          </tr>
          <tr><td colspan="6" class="bottombox">&nbsp;</td></tr>
      </table>
	</td></tr>
    <tr><td colspan="2">&nbsp;</td></tr>
    <tr><td colspan="2">
        <table width="900" border="0" cellpadding="0" cellspacing="0">
          <tr><td colspan="8"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td></tr>
          <tr>
            <td width="15" class="leftbox">&nbsp;</td>
            <td><strong>Start Date</strong></td>
            <td><input name="start_date" type="text" id="start_date" value="<%= dprit.getStartDate() %>"></td>
            <td><strong>Scheduled Finish</strong></td>
            <td><input name="scheduled_finish" type="text" id="scheduled_finish" value="<%= dprit.getScheduledFinish()%>"></td>
            <td><strong>Revised Finish</strong></td>
            <td><input name="revised_finish" type="text" id="revised_finish" value="<%=dprit.getRevisedFinish()%>"></td>
            <td width="15" class="rightbox">&nbsp;</td>
          </tr>
          <tr><td colspan="8" class="bottombox">&nbsp;</td></tr>
      </table>
	</td></tr>
    <tr><td colspan="2">&nbsp;</td></tr>
    <tr valign="top">
    
    <td width="300">
      <table width="300" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td colspan="4">&nbsp;</td>
        </tr>
        <tr>
          <td class="leftbox">&nbsp;</td>
          <td colspan="2"><strong>CALLS</strong></td>
          <td class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>MU</td><td><input name="calls_mu" type="text" id="calls_mu" value="<%= cwht.getMUCall() %>" size="10"></td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>WD</td><td><input name="calls_wd" type="text" id="calls_wd" value="<%= cwht.getWDCall() %>" size="10"></td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>CREW</td><td><input name="calls_crew" type="text" id="calls_crew" value="<%= cwht.getCrewCall() %>" size="10"></td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>1st SET UP </td><td><input name="calls_first_setup" type="text" id="calls_first_setup" value="<%= cwht.getFirstSetUp() %>" size="10"></td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>LUNCH</td><td><input name="calls_lunch" type="text" id="calls_lunch" value="<%= cwht.getLunch() %>" size="10"></td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>WRAP LOCATION </td><td><input name="calls_wrap_location" type="text" id="calls_wrap_location" value="<%= cwht.getWrapLocation() %>" size="10"></td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>MU WRAP </td><td><input name="calls_mu_wrap" type="text" id="calls_mu_wrap" value="<%= cwht.getMUWrap() %>" size="10"></td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>WD WRAP </td><td><input name="calls_wd_wrap" type="text" id="calls_wd_wrap" value="<%= cwht.getWDWrap() %>" size="10"></td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>MU HRS </td><td><input name="calls_mu_hrs" type="text" id="calls_mu_hrs" value="<%= cwht.getMUHours() %>" size="10"></td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>WD HRS </td><td><input name="calls_wd_hrs" type="text" id="calls_wd_hrs" value="<%= cwht.getWDHours() %>" size="10"></td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>TOTAL CREW HRS </td><td><input name="calls_total_crew_hours" type="text" id="calls_total_crew_hours" value="<%= cwht.getTotalCrewHours() %>" size="10"></td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td width="15" class="leftbox">&nbsp;</td>
          <td>&nbsp;</td>
          <td>inc. lunch</td>
          <td width="15" class="rightbox">&nbsp;</td>
        </tr>
        <tr>
          <td colspan="4" class="bottombox">&nbsp;</td>
        </tr>
    </table></td>
    <td width="600">
    
    <table width="600" border="0" cellpadding="0" cellspacing="0">
      <tr valign="top">
        <td colspan="4">&nbsp;</td>
        </tr>
      <tr>
	  	<td width="15" class="leftbox">&nbsp;</td>
        <td colspan="2"><strong>Locations/Sets</strong></td>
		<td width="15" class="rightbox">&nbsp;</td>
      </tr>
      <tr>
        <td width="15" class="leftbox">&nbsp;</td>
		<td colspan="2">
		<textarea name="locations_sets" cols="60" id="locations_sets"><%
		List<SingleLocationSetType> slst_list = dprit.getLocationSet().getSingleLocationSet();
		//int n_slst = slst_list.size();
		for(SingleLocationSetType slst : slst_list){
		out.println(slst.getNumber() + " " + slst.getSetName() + " " + slst.getLocation() + " " + slst.getAddress());
		//if ((--n_slst)!=0)
		//	out.print("\n");
		} %> 
		</textarea></td>
        <td width="15" class="rightbox">&nbsp;</td>
      </tr>
      <tr>
        <td width="15" class="leftbox">&nbsp;</td>
		<td width="265"><strong>Slate NO.s</strong></td>
        <td width="265"><strong>Scenes Shot Today</strong></td>
        <td width="15" class="rightbox">&nbsp;</td>
      </tr>
      <tr>
        <td width="15" class="leftbox">&nbsp;</td>
		<td><textarea name="slate_numbers" cols="30" id="slate_numbers"><%
		List<BigInteger> slateNO_list = dprit.getSlateNOs().getSlate();
		int n_slates = slateNO_list.size();
		for (BigInteger slateNO : slateNO_list){
			out.print(slateNO);
			if ((--n_slates)!=0)
				out.print(", ");
		}%> 
		</textarea></td>
        <td><textarea name="scenes_today" cols="30" id="textarea"><%
        ScenesShotInfoType ssit = dprit.getScenesShotInfo();
        List<String> sst_list=ssit.getScenesShotToday().getScene();
		int n_scnt = sst_list.size();
        for (String scnt_today : sst_list){
        	out.print(scnt_today);
			if ((--n_scnt)!=0)
				out.print(", ");
        }
        %> 
        </textarea></td>
        <td width="15" class="rightbox">&nbsp;</td>
      </tr>
      <tr>
        <td width="15" class="leftbox">&nbsp;</td>
		<td><strong>Scenes Deleted </strong></td>
        <td><strong>Scenes Added</strong></td>
        <td width="15" class="rightbox">&nbsp;</td>
      </tr>
  	  <tr>
		<td width="15" class="leftbox">&nbsp;</td>
		<td><textarea name="scenes_delete" cols="30" id="textarea2"><%
        List<String> sd_list=ssit.getScenesDeleted().getScene();
		int n_sd = sd_list.size();
        for (String sd : sd_list){
        	out.print(sd);
			if ((--n_sd)!=0)
				out.print(", ");
        }
        %>
		</textarea></td>
		<td><textarea name="scenes_add" cols="30" id="textarea4"><%
        List<String> sa_list=ssit.getScenesAdded().getScene();
		int n_sa = sa_list.size();
        for (String sa : sa_list){
        	out.print(sa);
			if ((--n_sa)!=0)
				out.print(", ");
        }
		%> 
		</textarea></td>
		<td width="15" class="rightbox">&nbsp;</td>
  </tr>
  <tr>
    <td width="15" class="leftbox">&nbsp;</td>
    <td><strong>Scheduled Scenes Not Shot</strong></td>
    <td>&nbsp;</td>
    <td width="15" class="rightbox">&nbsp;</td>
  </tr>
  <tr>
    <td width="15" class="leftbox">&nbsp;</td>
    <td><textarea name="scheduled_not_shot" cols="30" id="textarea5"><%
    List<String> scns_list=ssit.getScenesScheduledNotShot().getScene();
	int n_scns = scns_list.size();
    for (String scns : scns_list){
    	out.print(scns);
		if ((--n_scns)!=0)
			out.print(", ");
    }
    %> 
    </textarea></td>
    <td>&nbsp;</td>
    <td width="15" class="rightbox">&nbsp;</td>
  </tr>
  <tr>
    <td colspan="4" class="bottombox">&nbsp;</td>
    </tr>
      </table></td>
    </tr>
    <tr>
      <td colspan="2">&nbsp;</td>
    </tr>
    <tr valign="top">
      <td width="300">	  	<table width="300" border="0" cellpadding="0" cellspacing="0">
        <tr><td colspan="4">&nbsp;</td></tr>
		<tr>
		<td class="rightbox">&nbsp;</td>
		<td>Scheduled Ratio</td><td><input name="scheduled_ratio" type="text" id="scheduled_ratio" value="<%= rtst.getScheduledRatio() %>" size="10"></td>
		<td class="rightbox">&nbsp;</td>
		</tr>
		<tr>
		<td class="rightbox">&nbsp;</td>
		<td>Daily Ratio</td><td><input name="daily_ratio" type="text" id="daily_ratio" value="<%= rtst.getDailyRatio() %>" size="10"></td>
		<td class="rightbox">&nbsp;</td>
		</tr>
		<tr>
		<td class="rightbox">&nbsp;</td>
		<td>Average Ratio</td><td><input name="average_ratio" type="text" id="average_ratio" value="<%= rtst.getAverageRatio() %>" size="10"></td>
		<td class="rightbox">&nbsp; </td>
		</tr>
		<tr>
		<td class="rightbox">&nbsp;</td>
		<td>Average Timing</td><td><input name="average_timing" type="text" id="average_timing" value="<%= rtst.getAverageTiming() %>" size="10"></td>
		<td class="rightbox">&nbsp;</td>
		</tr>
		<tr>
		<td class="rightbox">&nbsp;</td>
		<td>Cumulative +/-</td><td><input name="cumulative" type="text" id="cumulative" value="<%= rtst.getCumulative() %>" size="10"></td>
		<td class="rightbox">&nbsp;</td>
		</tr>
		<tr>
		<td class="rightbox">&nbsp;</td>
		<td>Original Timing</td><td><input name="original_timing" type="text" id="original_timing" value="<%= rtst.getOriginalTiming() %>" size="10"></td>
		<td class="rightbox">&nbsp;</td>
		</tr>
  
  <tr><td colspan="4" class="bottombox">&nbsp;</td>
  
  </tr>
  
    </table>    </td>
    
<td width="600"><table width="600" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <th colspan="9">&nbsp;</th>
      </tr>
      <tr>
        <th class="leftbox">&nbsp;</th>
        <td><strong>SHOOTING DAYS: </strong></td>
        <th>&nbsp;</th>
        <th colspan="2">SCRIPT</th>
        <th>SCENES</th>
        <th>EST.</th>
        <th>ACTUAL</th>
        <th class="rightbox">&nbsp;</th>
      </tr>
	  <%
		  SceneShotScheduleType ssst = dprit.getSceneShotSchedule();
		  SingleSceneShotScheduleType sssst_pr = ssst.getPrevShot();
		  SingleSceneShotScheduleType sssst_today = ssst.getShotToday();
		  SingleSceneShotScheduleType sssst_todate = ssst.getToDate();
		  SingleSceneShotScheduleType sssst_tobe = ssst.getToBeShot();
		  SingleSceneShotScheduleType sssst_total = ssst.getTotal();
		  %>
      <tr>
        <td align="center" class="leftbox">&nbsp;</td>
        <td>SCHEDULED</td>
        <td><input name="scheduled" type="text" id="scheduled" value="<%= sdst.getScheduledDays() %>" size="10"></td>
        <td colspan="2">PREV. SHOT </td>
        <td align="center"><input name="prev_scenes" type="text" id="prev_scenes" value="<%= sssst_pr.getScenes() %>" size="5"></td>
        <td align="center"><input name="prev_est" type="text" id="prev_est" value="<%= sssst_pr.getEstd() %>" size="5"></td>
        <td align="center"><input name="prev_actual" type="text" id="prev_actual" value="<%= sssst_pr.getActual() %>" size="5"></td>
        <td align="center" class="rightbox">&nbsp;</td>
      </tr>
      <tr>
        <td align="center" class="leftbox">&nbsp;</td>
        <td>DAYS TO DATE </td>
        <td><input name="days_to_date" type="text" id="days_to_date" value="<%= sdst.getDaysToDate() %>" size="10"></td>
        <td colspan="2">SHOT TODAY </td>
        <td align="center"><input name="shot_scenes" type="text" id="shot_scenes" value="<%= sssst_today.getScenes() %>" size="5"></td>
        <td align="center"><input name="shot_est" type="text" id="shot_est" value="<%= sssst_today.getEstd() %>" size="5"></td>
        <td align="center"><input name="shot_actual" type="text" id="shot_actual" value="<%= sssst_today.getActual() %>" size="5"></td>
        <td align="center" class="rightbox">&nbsp;</td>
      </tr>
      <tr>
        <td align="center" class="leftbox">&nbsp;</td>
        <td>EST. TO COMPLETE </td>
        <td><input name="est_complete" type="text" id="est_complete" value="<%= sdst.getEstdToComplete() %>" size="10"></td>
        <td colspan="2">TO DATE </td>
        <td align="center"><input name="todate_scenes" type="text" id="todate_scenes" value="<%= sssst_todate.getScenes() %>" size="5"></td>
        <td align="center"><input name="todate_est" type="text" id="todate_est" value="<%= sssst_todate.getEstd() %>" size="5"></td>
        <td align="center"><input name="todate_actual" type="text" id="todate_actual" value="<%= sssst_todate.getActual() %>" size="5"></td>
        <td align="center" class="rightbox">&nbsp;</td>
      </tr>
      <tr>
        <td align="center" class="leftbox">&nbsp;</td>
        <td>EST'D TOTAL </td>
        <td><input name="estd_total" type="text" id="estd_total" value="<%= sdst.getEstdTotal() %>" size="10"></td>
        <td colspan="2">TO BE SHOT </td>
        <td align="center"><input name="tobe_scenes" type="text" id="tobe_scenes" value="<%= sssst_tobe.getScenes() %>" size="5"></td>
        <td align="center"><input name="tobe_est" type="text" id="tobe_est" value="<%= sssst_tobe.getEstd() %>" size="5"></td>
        <td align="center"><input name="tobe_actual" type="text" id="tobe_actual" value="<%= sssst_tobe.getActual() %>" size="5"></td>
        <td align="center" class="rightbox">&nbsp;</td>
      </tr>
      <tr>
        <td class="leftbox">&nbsp;</td>
        <td>&nbsp;</td>
        <td align="center">&nbsp;</td>
        <td colspan="2">TOTAL</td>
        <td align="center"><input name="total_scenes" type="text" id="total_scenes" value="<%= sssst_total.getScenes() %>" size="5"></td>
        <td align="center"><input name="total_est" type="text" id="total_est" value="<%= sssst_total.getEstd() %>" size="5"></td>
        <td align="center"><input name="total_actual" type="text" id="total_actual" value="<%= sssst_total.getActual() %>" size="5"></td>
        <td align="center" class="rightbox">&nbsp;</td>
      </tr>
      <tr>
        <td colspan="9" class="bottombox">&nbsp;</td>
      </tr>
  </table></td>
</tr>
    <tr>
      <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
      <td colspan="2">
        <table width="900" border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td colspan="11"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td>
          </tr>
          <tr>
            <th width="15" class="leftbox">&nbsp;</th>
			<td><strong>STOCK:</strong></td>
            <th>LOADED</th>
            <th>GROSS</th>
            <th>EXPOSED</th>
            <th>PRINT</th>
            <th>N.G.</th>
            <th>WASTE</th>
            <th>S/ENDS</th>
            <th>SOUND</th>
            
            <th width="15" class="rightbox">&nbsp;</th>
          </tr>
          <tr>
            <td width="15" class="leftbox">&nbsp;</td>
            <td align="center"><em>TESTS</em></td>
            <td align="center">&nbsp;</td>
            <td align="center">&nbsp;</td>
            <td align="center">&nbsp;</td>
            <td align="center">&nbsp;</td>
            <td align="center">&nbsp;</td>
            <td align="center">&nbsp;</td>
            <td align="center">&nbsp;</td>
            <td align="center">&nbsp;</td>
            <td width="15" align="center" class="rightbox">&nbsp;</td>
          </tr>
		  <%
		  StockInfoType sit = dprit.getStockInfo();
		  SingleStockInfoType ssit_pr = sit.getPreviously();
		  SingleStockInfoType ssit_today = sit.getToday();
		  SingleStockInfoType ssit_total = sit.getTotalToDate();
		  %>
          <tr>
            <td width="15" align="center" class="leftbox">&nbsp;</td>
			<td>PREVIOUSLY</td>
            <td align="center"><input name="previously_loaded" type="text" id="previously_loaded" value="<%=ssit_pr.getLoaded() %>" size="5"></td>
            <td align="center"><input name="previously_gross" type="text" id="previously_gross" value="<%=ssit_pr.getGross() %>" size="5"></td>
            <td align="center"><input name="previously_exposed" type="text" id="previously_exposed" value="<%=ssit_pr.getExposed() %>" size="5"></td>
            <td align="center"><input name="previously_print" type="text" id="previously_print" value="<%=ssit_pr.getPrint() %>" size="5"></td>
            <td align="center"><input name="previously_ng" type="text" id="previously_ng" value="<%=ssit_pr.getNG() %>" size="5"></td>
            <td align="center"><input name="previously_waste" type="text" id="previously_waste" value="<%=ssit_pr.getWaste() %>" size="5"></td>
            <td align="center"><input name="previously_shortend" type="text" id="previously_shortend" value="<%=ssit_pr.getShotEnds() %>" size="5"></td>
            <td align="center"><input name="previously_sound" type="text" id="previously_sound" value="<%=ssit_pr.getSound() %>" size="5"></td>
            <td width="15" align="center" class="rightbox">&nbsp;</td>
          </tr>
          <tr>
            <td width="15" align="center" class="leftbox">&nbsp;</td>
			<td>TODAY</td>
            <td align="center"><input name="today_loaded" type="text" id="today_loaded" value="<%=ssit_today.getLoaded() %>" size="5"></td>
            <td align="center"><input name="today_gross" type="text" id="today_gross" value="<%=ssit_today.getGross() %>" size="5"></td>
            <td align="center"><input name="today_exposed" type="text" id="today_exposed" value="<%=ssit_today.getExposed() %>" size="5"></td>
            <td align="center"><input name="today_print" type="text" id="today_print" value="<%=ssit_today.getPrint() %>" size="5"></td>
            <td align="center"><input name="today_ng" type="text" id="today_ng" value="<%=ssit_today.getNG() %>" size="5"></td>
            <td align="center"><input name="today_waste" type="text" id="today_waste" value="<%=ssit_today.getWaste() %>" size="5"></td>
            <td align="center"><input name="today_shortend" type="text" id="today_shortend" value="<%=ssit_today.getShotEnds() %>" size="5"></td>
            <td align="center"><input name="today_sound" type="text" id="today_sound" value="<%=ssit_today.getSound() %>" size="5"></td>
            <td width="15" align="center" class="rightbox">&nbsp;</td>
          </tr>
          <tr>
            <td width="15" align="center" class="leftbox">&nbsp;</td>
			<td>TOTAL TO DATE </td>
            <td align="center"><input name="total_loaded" type="text" id="total_loaded" value="<%=ssit_total.getLoaded() %>" size="5"></td>
            <td align="center"><input name="total_gross" type="text" id="total_gross" value="<%=ssit_total.getGross() %>" size="5"></td>
            <td align="center"><input name="total_exposed" type="text" id="total_exposed" value="<%=ssit_total.getExposed() %>" size="5"></td>
            <td align="center"><input name="total_print" type="text" id="total_print" value="<%=ssit_total.getPrint() %>" size="5"></td>
            <td align="center"><input name="total_ng" type="text" id="total_ng" value="<%=ssit_total.getNG() %>" size="5"></td>
            <td align="center"><input name="total_waste" type="text" id="total_waste" value="<%=ssit_total.getWaste() %>" size="5"></td>
            <td align="center"><input name="total_shortend" type="text" id="total_shortend" value="<%=ssit_total.getShotEnds() %>" size="5"></td>
            <td align="center"><input name="total_sound" type="text" id="total_sound" value="<%=ssit_total.getSound() %>" size="5"></td>
            <td width="15" align="center" class="rightbox">&nbsp;</td>
          </tr>
          <tr>
            <td colspan="11" class="bottombox">&nbsp;</td>
          </tr>
      </table></td>
    </tr>
    <tr>
      <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
      <td colspan="2">
        <table width="900" border="0" cellpadding="0" cellspacing="0" id="table1">
          <tbody>
            <tr>
              <td colspan="12"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td>
            </tr>
            <tr>
              <th width="15" class="leftbox">&nbsp;</th>
			  <td><strong>CAST:</strong></td>
              <th>CHARACTER</th>
              <th>PICK UP </th>
              <th>MU/WD CALL</th>
              <th>TRAVEL TIME </th>
              <th>WRAP</th>
              <th>LUNCH BREAK </th>
              <th>TOTAL HOURS </th>
              <th>EXTRAS NO.s </th>
              <th>HRS</th>
              <th width="15" class="rightbox">&nbsp;</th>
            </tr>
			<% 
			CastTimeSheetInfoType ctsit = dprit.getCastTimeSheetInfo();
			for(SingleCastTimeSheetInfoType sctsit : ctsit.getSingleCastTimeSheetInfo()) {
			%>
            <tr align="center">
              <td width="15" class="leftbox">&nbsp;</td>
			  <td><input name="cast" type="text" id="cast" value="<%= sctsit.getCast() %>" size="15"></td>
              <td><input name="character" type="text" id="character" value="<%= sctsit.getCharacter() %>" size="15"></td>
              <td><input name="pick_up" type="text" id="pick_up" value="<%= sctsit.getPickUp() %>" size="8"></td>
              <td><input name="MU_WD_Call" type="text" id="MU_WD_Call" value="<%= sctsit.getMUWDCall() %>" size="8"></td>
              <td><input name="travel_time" type="text" id="travel_time" value="<%= sctsit.getTravelTime() %>" size="8"></td>
              <td><input name="wrap" type="text" id="wrap" value="<%= sctsit.getWrap() %>" size="8"></td>
              <td><input name="lunch_break" type="text" id="lunch_break" value="<%= sctsit.getLunchBreak() %>" size="8"></td>
              <td><input name="total_hours" type="text" id="total_hours" value="<%= sctsit.getTotalHours() %>" size="8"></td>
              <td><input name="extra_nos" type="text" id="extra_nos" value="<%= sctsit.getExtrasNOs() %>" size="4"></td>
              <td><input name="hrs" type="text" id="hrs" value="<%= sctsit.getHRs() %>" size="8"></td>
              <td width="15" class="rightbox">&nbsp;</td>
            </tr>
			<%}%>
            <tr align="center">
              <td colspan="12" class="bottombox">&nbsp;</td>
            </tr>
          </tbody>
      </table></td>
    </tr>
    <tr>
      <td colspan="2">
	  	<table width="900" border="0" cellpadding="0" cellspacing="0">
       	  <tr>
       	    <td colspan="6"><img src="graphics/testing/box_top.jpg" width="902" height="10"></td>
   	      </tr>
       	  <tr>
		    <td width="15" class="leftbox">&nbsp;</td>
			  <td colspan="4"><strong>Catering</strong></td>
		    <td width="15" class="rightbox">&nbsp;</td>
		  </tr>
			<tr>
			  <td width="15" class="leftbox">&nbsp;</td>
			  <td><strong>Meal</strong></td>
			  <td><strong>Time</strong></td>
			  <td><strong>Nos</strong></td>
			  <td><strong>Loc.</strong></td>
			  <td width="15" class="rightbox">&nbsp;</td>
			</tr>
			<% 
			CateringType ct = dprit.getCatering();
			for(SingleMealType smt : ct.getSingleMeal()) {
			%>
							<tr>
							  <td width="15" class="leftbox">&nbsp;</td>
							  <td><input name="catering_meal" type="text" id="catering_meal" value="<%= smt.getMeal() %>"></td>
							  <td><input name="catering_time" type="text" id="catering_time" value="<%= smt.getTime() %>" size="10"></td>
							  <td><input name="catering_nos" type="text" id="catering_nos" value="<%= smt.getNumbers() %>" size="10"></td>
							  <td><input name="catering_loc" type="text" id="catering_loc" value="<%= smt.getLocation() %>" size="10"></td>
							  <td width="15" class="rightbox">&nbsp;</td>
							</tr>
							<%
							}
							%>
			<tr>
			  <td colspan="6" class="bottombox">&nbsp;</td>
		  </tr>
   	  </table>	</td></tr>
	<tr><td colspan="2">
		<table width="900" border="0" align="center" cellpadding="0" cellspacing="0">
		  	<tr><td colspan="3"><img src="graphics/testing/box_top.jpg"></td></tr>
		  	<tr><td width="15" class="leftbox"></td>
	  	    <td align="center"><strong>Major Props/Action Vehicles/Additional Equipment</strong></td><td width="15" class="rightbox"></td></tr>
    		<tr><td width="15" class="leftbox"></td><td align="center"><textarea name="major_props" cols="100" id="major_props"><%= dprit.getMajorPropsActionVehiclesAdditionalEquipment()%>
    		</textarea></td><td width="15" class="rightbox"></td></tr>
			<tr><td width="15" class="leftbox"></td>
		    <td align="center"><strong>Additional Crew</strong></td><td width="15" class="rightbox"></td></tr>
			<tr><td width="15" class="leftbox"></td><td align="center"><textarea name="additional_crew" cols="100" id="additional_crew"><%= dprit.getAdditionalCrew()%>
			</textarea></td><td width="15" class="rightbox"></td></tr>
			<tr><td width="15" class="leftbox"></td>
		    <td align="center"><strong>Livestock/Other </strong></td><td width="15" class="rightbox"></td></tr>
			<tr><td width="15" class="leftbox"></td><td align="center"><textarea name="livestock_others" cols="100" id="livestock_others"><%= dprit.getLivestocksOther()%>
			</textarea></td><td width="15" class="rightbox"></td></tr>
			<tr><td width="15" class="leftbox"></td>
		    <td align="center"><strong>Accidents/Delays</strong></td><td width="15" class="rightbox"></td></tr>
			<tr><td width="15" class="leftbox"></td><td align="center"><textarea name="accidents_delays" cols="100" id="accidents_delays"><%= dprit.getAccidentsDelays()%>
			</textarea></td><td width="15" class="rightbox"></td></tr>
			<tr><td width="15" class="leftbox"></td>
		    <td align="center"><strong>General Remarks/Notes</strong></td><td width="15" class="rightbox"></td></tr>
			<tr><td width="15" class="leftbox"></td><td align="center"><textarea name="general_remarks" cols="100" id="general_remarks"><%= dprit.getGeneralRemarksNotes()%>
			</textarea></td><td width="15" class="rightbox"></td></tr>
			<tr>
			  <td colspan="3" class="bottombox">&nbsp;</td>
		  </tr>
  		</table>
  </td></tr>
		  <tr><td align="center" colspan="3">	
				<input type="hidden" name="workItemID" id="workItemID"/>
				<input type="hidden" name="userID" id="userID"/>
				<input type="hidden" name="sessionHandle" id="sessionHandle"/>
				<input type="hidden" name="submit" id="submit"/>
			    <input type="button" value="Print"  onclick="window.print()"/>
	            <input type="submit" name="Save" value="Save"/>
	            <input type="submit" name="Submission" value="Submission"/>
			</td></tr>
  </table>
</form><%
if(request.getParameter("Submission") != null){

	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    File f = new File("./backup/DPR_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
    m.marshal(cdprtElement,  f);//output to file
    
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

	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(cdprtElement, xmlOS);//out to ByteArray

    response.setHeader("Content-Disposition", "attachment;filename=\"DPR_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
    response.setHeader("Content-Type", "text/xml");

    ServletOutputStream outs = response.getOutputStream();
    xmlOS.writeTo(outs);
    outs.close();
}%>
</body>
</html>
