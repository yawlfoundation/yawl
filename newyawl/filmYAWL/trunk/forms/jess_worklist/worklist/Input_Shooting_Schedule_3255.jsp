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
<%@ page import="org.yawlfoundation.sb.shootingschedule.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Shooting Schedule</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<!-- Stylesheet imports -->
<link href="graphics/style.css" rel="stylesheet" type="text/css">
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js"></script>
<script type="text/javascript" src="scripts/filloutShootingSchedule.js"></script>
</head>

<body>

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
            int endOfFile = result.indexOf("</ns2:Input_Shooting_Schedule>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(
                    beginOfFile,
                    endOfFile + 30);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		//xml = "<?xml version='1.0' encoding='UTF-8'?><ns2:Input_Shooting_Schedule xmlns:ns2='http://www.yawlfoundation.org/sb/shootingSchedule' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.yawlfoundation.org/sb/shootingSchedule shootingScheduleType.xsd '><production>new</production><shootingSchedule><lastUpdatedDate>2007-07-07</lastUpdatedDate><director>me</director><producer>you</producer><startDate>2006-06-06</startDate><scheduledFinish>2005-05-05</scheduledFinish><revisedFinish>2004-04-04</revisedFinish><scheduledShootingDays>20</scheduledShootingDays></shootingSchedule><totalScenes>0</totalScenes><totalPageTime><number>0</number><numerator>0</numerator></totalPageTime><originalTiming>12:00:00</originalTiming></ns2:Input_Shooting_Schedule>";
		xml = (String)session.getAttribute("outputData");
		xml = xml.replaceAll("<Input_Shooting_Schedule", "<ns2:Input_Shooting_Schedule xmlns:ns2='http://www.yawlfoundation.org/sb/shootingSchedule'");
		xml = xml.replaceAll("</Input_Shooting_Schedule","</ns2:Input_Shooting_Schedule");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.shootingschedule");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement issElement = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file	            
	InputShootingScheduleType iss = (InputShootingScheduleType) issElement.getValue();
	ShootingScheduleType ss = iss.getShootingSchedule(); 
%>

<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
<tr><td colspan="3" class="background_top">&nbsp;</td></tr>
<tr>
<td width="14" class="background_left">&nbsp;</td>
<td><h1 align="center">Shooting Schedule </h1>      
<form name="form1" method="post">
<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
<tr>
<td>
	<table width='700' border='0' cellspacing='0' cellpadding='0'>
	<tr>
		<td width="15" align="right" class="header-left">&nbsp;</td>
		<td height="20" colspan='4' class="header-middle">General Info </td>
		<td width="15" class="header-right">&nbsp;</td>
	</tr>
	<tr height="30">
		<td width="15" class="left">&nbsp;</td>
		<td><strong>Production</strong></td>
		<td><input name='production' type='text' id='production' value="<%= iss.getProduction() %>" pattern="any_text" title="Enter Production. [String Value]" readonly></td>
		<td><strong>Director</strong></td>
		<td><input name='director' type='text' id='director' value="<%=ss.getDirector() %>" pattern="any_text" title="Enter Director. [String Value]" readonly></td>
		<td width="15" class="right">&nbsp;</td>
	</tr>
	<tr height="30">
		<td class="left">&nbsp;</td>
		<td><strong>Producer</strong></td>
		<td><input name='producer' type='text' id='producer' value="<%=ss.getProducer() %>" pattern="any_text" title="Enter Producer. [String Value]" readonly></td>
		<td><strong>Last Updated Date</strong></td>
		<td><input name='last_updated' type='text' id='last_updated' value="<%=ss.getLastUpdatedDate().getDay()+"-"+ss.getLastUpdatedDate().getMonth()+"-"+ss.getLastUpdatedDate().getYear() %>" pattern="real_date" title="Enter Last Updated Date. [Date Value DD-MM-YYYY]" readonly></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr height="30"><td colspan="10" class='bottom'>&nbsp;</td></tr>
	</table>
</td>
</tr>
<tr>
<td>
	<table width='700' border='0' cellspacing='0' cellpadding='0'>
		<tr>
			<td width="15" align="right" class="header-left">&nbsp;</td>
			<td height="20" colspan='4' class="header-middle">Scheduled Date Brief </td>
			<td width="15" class="header-right">&nbsp;</td>
		</tr>
		<tr height="30">
			<td class="left">&nbsp;</td>
			<td><strong>Start Date </strong></td>
			<td><input name='start_date' type='text' id='start_date' value="<%=ss.getStartDate().getDay()+"-"+ss.getStartDate().getMonth()+"-"+ss.getStartDate().getYear() %>"  pattern="real_date" title="Enter Start Date. [Date Value DD-MM-YYYY]"></td>
			<td><strong>Scheduled Finish</strong></td>
			<td><input name='scheduled_finish' type='text' id='scheduled_finish' value="<%=ss.getScheduledFinish().getDay()+"-"+ss.getScheduledFinish().getMonth()+"-"+ss.getScheduledFinish().getYear() %>" pattern="real_date" title="Enter Scheduled Finish Date. [Date Value DD-MM-YYYY]"></td>
			<td class="right">&nbsp;</td>
		</tr>
		<tr height="30">
			<td class="left">&nbsp;</td>
			<td><strong>Revised Finish </strong></td>
			<td><input name='revised_finish' type='text' id='revised_finish' value="<% if(ss.getRevisedFinish() != null) {out.print(ss.getRevisedFinish().getDay()+"-"+ss.getRevisedFinish().getMonth()+"-"+ss.getRevisedFinish().getYear());} %>" title="Enter Revised Finish - If Necessary. [Date Value DD-MM-YYYY]"></td>
			<td><strong>Scheduled Shooting Days </strong></td>
			<td><input name='scheduled_shooting_days' type='text' id='scheduled_shooting_days' value="<% if (ss.getScheduledShootingDays().intValue() > 0){ out.print(ss.getScheduledShootingDays()); }%>" pattern="number" title="Enter Schedule Shooting Days. [Number Value]"></td>
			<td class="right">&nbsp;</td>
		</tr>
		<tr height="30"><td colspan="10" class='bottom'>&nbsp;</td></tr>
	</table>					
</td>
</tr>
<tr>
<td>
<table width="700" border="0" cellpadding="0" cellspacing="0" id="shooting_days">
<% int a=0;
int tables = 0;
if(ss.getSingleDaySchedule().size() != 0) { 
	for(SingleDayScheduleType sds : ss.getSingleDaySchedule()){
		a++;
%>
<tr>
<td>
<table width="700" border="0" cellpadding="0" cellspacing="0" id="<% out.print("day" + a); %>">
	<tr>
		<td width="15" class="header-left">&nbsp;</td>
		<td colspan="4" class="header-middle">Shoot Day # 
		<input name="<% out.print("sd" + a + "_number");%>" type="text" id="<% out.print("sd" + a + "_number");%>" size="5" value="<%=sds.getShootDayNo() %>" pattern="number" title="Enter Shoot Day Number."></td>
		<td width="15" class="header-right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td colspan="4" align="left">&nbsp;</td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Shoot Day Date</strong></td>
		<td width="185" align="left"><input name="<% out.print("sd" + a + "_date");%>" type="text" id="<% out.print("sd" + a + "_date");%>" value="<%=sds.getShootDayDate().getDay()+"-"+sds.getShootDayDate().getMonth()+"-"+sds.getShootDayDate().getYear() %>" pattern="real_date" title="Enter Shoot Date."></td>
		<td width="150" align="left"><strong>Shoot Day Weekday</strong></td>
		<td width="185" align="left"><input name="<% out.print("sd" + a + "_weekday");%>" type="text" id="<% out.print("sd" + a + "_weekday");%>" value="<%=sds.getShootDayWeekday() %>" pattern="any_text" title="Enter Weekday."></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Crew Call </strong></td>
		<td width="185" align="left"><input name="<% out.print("sd" + a + "_crew");%>" type="text" id="<% out.print("sd" + a + "_crew");%>" value="<%=sds.getCrewCall() %>" pattern="time" title="Enter Crew Call. [Time Format HH:MM:SS]"></td>
		<td width="150" align="left"><strong>Travel To Loc </strong></td>
		<td width="185" align="left"><input name="<% out.print("sd" + a + "_traveltoloc");%>" type="text" id="<% out.print("sd" + a + "_traveltoloc");%>" value="<%=sds.getTravelToLoc() %>" pattern="time" title="Enter Travel to Location. [Time Format HH:MM:SS]"></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td align="left"><strong>Bump In</strong></td>
		<td colspan="3" align="left"><input name="<% out.print("sd" + a + "_bumpin");%>" type="text" id="<% out.print("sd" + a + "_bumpin");%>" size="8" value="<% if(sds.getBumpIn()!= null){out.print(sds.getBumpIn()); }%>"></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Start of Day Notes </strong></td>
		<td colspan="3" align="left"><input name="<% out.print("sd" + a + "_start");%>" type="text" id="<% out.print("sd" + a + "_start");%>" size="73" value="<% if(sds.getStartDayNotes()!= null){ out.print(sds.getStartDayNotes());} %>"></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td colspan="4" align="left">
			<table width="670" border="0" cellpadding="0" cellspacing="0" id="<% out.print("scenes" + a); %>">
			<% int b=0;
				for(SceneScheduleType scs : sds.getSceneSchedule()) {
					b++;
			%>
				<tr>
					<td>
						<table border="0" cellpadding="0" cellspacing="0">
							<tr><td colspan="6" class="top">&nbsp;</td></tr>
							<tr>
								<td width="15" height="30" class="left">&nbsp;</td>
								<td width="160" height="30" valign="top"><strong>Scene Number</strong></td>
								<td height="30" colspan="3" valign="top"><input name="<% out.print("sd" + a + "_scene" + b);%>" type="text" id="<% out.print("sd" + a + "_scene" + b);%>" value="<%= scs.getScene() %>" pattern="any_text" title="Enter Scene Number."></td>
								<td width="15" height="30" class="right">&nbsp;</td>
							</tr>
							<tr>
								<td height="30" class="left">&nbsp;</td>
								<td height="30" valign="top"><strong>Set Name</strong></td>
								<td height="30" valign="top"><input name="<% out.print("sd" + a + "_set" + b);%>" type="text" id="<% out.print("sd" + a + "_set" + b);%>" value="<%= scs.getSet()%>" pattern="any_text" title="Enter Set Name."></td>
								<td height="30" valign="top"><strong>Synopsis</strong></td>
								<td height="30" valign="top"><input name="<% out.print("sd" + a + "_synopsis" + b);%>" type="text" id="<% out.print("sd" + a + "_synopsis" + b);%>" value="<%= scs.getSynopsis() %>" pattern="any_text" title="Enter Synopsis."></td>
								<td height="30" class="right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" height="30" class="left">&nbsp;</td>
								<td width="160" height="30" valign="top"><strong>Location ID </strong></td>
								<td width="160" height="30" valign="top"><input name="<% out.print("sd" + a + "_locationID" + b);%>" type="text" id="<% out.print("sd" + a + "_locationID" + b);%>" value="<%= scs.getLocationID()%>" pattern="any_text" title="Enter Location ID."></td>
								<td width="160" height="30" valign="top"><strong>Address</strong></td>
								<td width="160" height="30" valign="top"><input name="<% out.print("sd" + a + "_address" + b);%>" type="text" id="<% out.print("sd" + a + "_address" + b);%>" value="<%= scs.getAddress()%>" pattern="any_text" title="Enter Address."></td>
								<td height="30" class="right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" height="30" class="left">&nbsp;</td>
								<td width="160" height="30" valign="top"><strong>Interior</strong><input name="<% out.print("sd" + a + "_intext" + b);%>" type="radio" value="INT" <% if(scs.getINEX().equals("INT")){ out.print("checked"); } %>></td>
								<td width="160" height="30" valign="top"><strong>Exterior</strong><input name="<% out.print("sd" + a + "_intext" + b);%>" type="radio" value="EXT" <% if(scs.getINEX().equals("EXT")){ out.print("checked"); } %>></td>
								<td width="160" height="30" valign="top"><strong>Day</strong><input name="<% out.print("sd" + a + "_daynight" + b);%>" type="radio" value="Day" <% if(scs.getDN().equals("Day")){ out.print("checked"); } %>></td>
								<td width="160" height="30" valign="top"><strong>Night</strong><input name="<% out.print("sd" + a + "_daynight" + b);%>" type="radio" value="Night" <% if(scs.getDN().equals("Night")){ out.print("checked"); } %>></td>
								<td height="30" class="right">&nbsp;</td>
							</tr>
							<% PageTimeType pt = scs.getPageTime(); %>
							<tr>
								<td height="30" class="left">&nbsp;</td>
								<td height="30" valign="top"><strong>No. of Script Pages</strong></td>
								<td height="30" valign="top">
								<input name="<% out.print("sd" + a + "_pages" + b);%>" type="text" id="<% out.print("sd" + a + "_pages" + b);%>" size="4" value="<%=pt.getNumber() %>" pattern="number" title="Enter Script Pages.">
								<input name="<% out.print("sd" + a + "_pagesnum" + b);%>" type="text" id="<% out.print("sd" + a + "_pagesnum" + b);%>" size="2" value="<%=pt.getNumerator() %>" pattern="number" title="Enter Script Pages.">
								<strong>/8 pgs </strong></td>
								<td height="30" valign="top"><strong>Est. Shoot Times </strong></td>
								<td height="30" valign="top"><input name="<% out.print("sd" + a + "_shoottimes" + b);%>" type="text" id="<% out.print("sd" + a + "_shoottimes" + b);%>" value="<%= scs.getEstShootTimes()%>" pattern="any_text" title="Enter Est. Shoot Times."></td>
								<td height="30" class="right">&nbsp;</td>
							</tr>
							<tr>
								<td height="30" class="left">&nbsp;</td>
								<td height="30" valign="top"><strong>Est. Script Timing </strong></td>
								<td height="30" colspan="3" valign="top"><input name="<% out.print("sd" + a + "_scripttime" + b);%>" type="text" id="<% out.print("sd" + a + "_scripttime" + b);%>" value="<%=scs.getEstScriptTiming() %>" pattern="time" title="Enter Est. Script Timing. [Time Format HH:MM:SS]"></td>
								<td height="30" class="right">&nbsp;</td>
							</tr>
							<tr>
								<td height="30" rowspan="2" class="left">&nbsp;</td>
								<td height="30" rowspan="2" valign="top"><strong>Characters</strong></td>
								<td height="15" colspan="3" valign="top">
									<table width="510" border="0" cellpadding="0" cellspacing="0" id="<% out.print("sd" + a + "_characters" + b);%>">
										<% int c=0;
										CharactersType ct = scs.getCharacters();
										List<String> characters_list = ct.getCharacter();
										for(String characters : characters_list) {
										c ++;%>
										<tr><td><input name="<% out.print("sd" + a + "_charactername" + b + "_" + c);%>" type="text" id="<% out.print("sd" + a + "_charactername" + b + "_" + c);%>" value="<%=characters%>" pattern="any_text" title="Enter Character Name."></td></tr>
										<% } //end of character loop%>
									</table>													  
								</td>
								<td height="30" rowspan="2" class="right">&nbsp;</td>
							</tr>
							<tr><td height="15" colspan="3" valign="top"><input name="button3" type="button" onClick="addCharacterRow(<% out.print(a +","+ b); %>);" value="Add Character"/>
							<input name="button3" type="button" onClick="deleteCharacterRow(<% out.print(a +","+ b); %>);" value="Delete Character"/>
							<input name="<% out.print("sd" + a + "_charactercount" + b);%>" type="hidden" id="<% out.print("sd" + a + "_charactercount" + b);%>" value="<% out.print(c);%>"></td></tr>
							<tr>
								<td width="15" height="30" rowspan="2" class="left">&nbsp;</td>
								<td width="160" height="30" rowspan="2" valign="top"><strong>Set Requirements </strong></td>
								<td height="15" colspan="3" valign="top">
									<table width="510" border="0" cellpadding="0" cellspacing="0" id="<% out.print("sd" + a + "_requirements" + b );%>">
										<% int d=0;
										SetRequirementsType sr = scs.getSetRequirements();
										for(SingleEntryType se : sr.getSingleEntry()) {
										d++;
										%>  
										<tr valign="top">
										<td><select name="<% out.print("sd" + a + "_items" + b + "_" + d);%>" id="<% out.print("sd" + a + "_items" + b + "_" + d);%>">
										<option value="Background Actors" <% if(se.getItem().equals("Background Actors")) { out.print("selected"); }%>>Background Actors</option>
										<option value="Stunts" <% if(se.getItem().equals("Stunts")) { out.print("selected"); }%>>Stunts</option>
										<option value="Vehicles" <% if(se.getItem().equals("Vehicles")) { out.print("selected"); }%>>Vehicles</option>
										<option value="Props" <% if(se.getItem().equals("Props")) { out.print("selected"); }%>>Props</option>
										<option value="Camera" <% if(se.getItem().equals("Camera")) { out.print("selected"); }%>>Camera</option>
										<option value="Special Effects" <% if(se.getItem().equals("Special Effects")) { out.print("selected"); }%>>Special Effects</option>
										<option value="Wardrobe" <% if(se.getItem().equals("Wardrobe")) { out.print("selected"); }%>>Wardrobe</option>
										<option value="Makeup/Hair" <% if(se.getItem().equals("Makeup/Hair")) { out.print("selected"); }%>>Makeup/Hair</option>
										<option value="Animals" <% if(se.getItem().equals("Animals")) { out.print("selected"); }%>>Animals</option>
										<option value="Animal Wrangler" <% if(se.getItem().equals("Animal Wrangler")) { out.print("selected"); }%>>Animal Wrangler</option>
										<option value="Music" <% if(se.getItem().equals("Music")) { out.print("selected"); }%>>Music</option>
										<option value="Sound" <% if(se.getItem().equals("Sound")) { out.print("selected"); }%>>Sound</option>
										<option value="Art Department" <% if(se.getItem().equals("Art Department")) { out.print("selected"); }%>>Art Department</option>
										<option value="Set Dressing" <% if(se.getItem().equals("Set Dressing")) { out.print("selected"); }%>>Set Dressing</option>
										<option value="Greenery" <% if(se.getItem().equals("Greenary")) { out.print("selected"); }%>>Greenery</option>
										<option value="Special Equipment" <% if(se.getItem().equals("Special Equipment")) { out.print("selected"); }%>>Special Equipment</option>
										<option value="Security" <% if(se.getItem().equals("Security")) { out.print("selected"); }%>>Security</option>
										<option value="Additional Labour" <% if(se.getItem().equals("Additional Labour")) { out.print("selected"); }%>>Additional Labour</option>
										<option value="Visual Effects" <% if(se.getItem().equals("Visual Effects")) { out.print("selected"); }%>>Visual Effects</option>
										<option value="Mechanical Effects" <% if(se.getItem().equals("Mechanical Effects")) { out.print("selected"); }%>>Mechanical Effects</option>
										<option value="Miscellaneous" <% if(se.getItem().equals("Miscellaneous")) { out.print("selected"); }%>>Miscellaneous</option>
										<option value="Notes" <% if(se.getItem().equals("Notes")) { out.print("selected"); }%>>Notes</option>
										</select></td>
										<td><textarea name="<% out.print("sd" + a + "_requirements" + b + "_" + d);%>" id="<% out.print("sd" + a + "_requirements" + b + "_" + d);%>" cols="20" pattern="any_text" title="Enter Set Requirements."><%=se.getRequirements() %></textarea></td>
										</tr>
										<%}//end of requirements loop%> 
									</table>														
								</td>
								<td height="30" rowspan="2" class="right">&nbsp;</td>
							</tr>
							<tr>
							<td height="15" colspan="3" valign="top"><input name="requirements1" type="button" id="requirements1" onClick="addRequirementsRow(<% out.print(a +","+ b); %>);" value="Add Set Requirements">
							<input name="requirements1" type="button" id="requirements1" onClick="deleteRequirementsRow(<% out.print(a +","+ b); %>);" value="Delete Set Requirements">
							<input name="<% out.print("sd" + a + "_requirementscount" + b);%>" type="hidden" id="<% out.print("sd" + a + "_requirementscount" + b);%>" value="<%if (d==0) {out.print("1");}else{out.print(d);}%>"></td>
							</tr>
							<% if(scs.getMealBreak() != null) {
							MealBreakType mb = scs.getMealBreak();%>
							<tr>
								<td class="left">&nbsp;</td>
								<td><input name="<% out.print("sd" + a + "_mealbreakbutton" + b);%>" type="button" id="<% out.print("sd" + a + "_mealbreakbutton" + b);%>" onClick="addMealBreak(<% out.print(a +","+ b); %>);" value="Add Meal Break" disabled></td>
								<td colspan="3">
									<table width="510" border="0" cellpadding="0" cellspacing="0" id="<% out.print("sd" + a + "_mealbreak" + b);%>">
										<tr><td><select name="<% out.print("sd" + a + "_meal" + b);%>" id="<% out.print("sd" + a + "_meal" + b);%>">
										<option value="Breakfast" <% if(mb.getMeal().equals("Breakfast")) { out.print("selected"); }%>>Breakfast</option>
										<option value="Morning Tea" <% if(mb.getMeal().equals("Morning Tea")) { out.print("selected"); }%>>Morning Tea</option>
										<option value="Lunch" <% if(mb.getMeal().equals("Lunch")) { out.print("selected"); }%>>Lunch</option>
										<option value="Afternoon Tea" <% if(mb.getMeal().equals("Afternoon Tea")) { out.print("selected"); }%>>Afternoon Tea</option>
										<option value="Dinner" <% if(mb.getMeal().equals("Dinner")) { out.print("selected"); }%>>Dinner</option>
										<option value="Supper" <% if(mb.getMeal().equals("Supper")) { out.print("selected"); }%>>Supper</option>
										</select></td><td><strong>Times</strong></td><td><input name="<% out.print("sd" + a + "_break" + b);%>" type="text" id="<% out.print("sd" + a + "_break" + b);%>" value="<%=mb.getBreak()%>" pattern="any_text" title="Enter Meal Break."></td></tr>
									</table>
								</td>
								<td class="right">&nbsp;</td>
							</tr>
							<%}else{%>
							<tr>
								<td class="left">&nbsp;</td>
								<td><input name="<% out.print("sd" + a + "_mealbreakbutton" + b);%>" type="button" id="<% out.print("sd" + a + "_mealbreakbutton" + b);%>" onClick="addMealBreak(<% out.print(a +","+ b); %>);" value="Add Meal Break"></td>
								<td colspan="3"><table width="510" border="0" cellpadding="0" cellspacing="0" id="<% out.print("sd" + a + "_mealbreak" + b);%>"></table></td>
								<td class="right">&nbsp;</td>
							</tr>
							<%} //end of meal choice%>
							<tr><td colspan="6" class="bottom">&nbsp;</td></tr>
						</table>
					</td>
				</tr>
				<%}//end of scenescheduletype loop - b%>
			</table>											
		</td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td colspan="4" align="left"><input name="button2" type="button" onClick="addScene(<% out.print("'scenes" +a+ "'," +a);%>);" value="Add Scene"/>
		<input name="button2" type="button" onClick="deleteScene(<% out.print("'scenes" +a+ "'," +a);%>);" value="Delete Scene"/>
		<input name="<% out.print("day_number_" + a);%>" type="hidden" id="<% out.print("day_number_" + a);%>" value="<%=a%>">
		<input name="<% out.print("table_count_" + a);%>" type="hidden" id="<% out.print("table_count_" + a);%>" value="<%=b %>"></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150"><strong>End of Day Notes</strong></td>
		<td colspan="3"><textarea name="<% out.print("sd" + a + "_end");%>" cols="50" id="<% out.print("sd" + a + "_end");%>" pattern="any_text" title="Enter End of Day Notes."><%= sds.getEndDayNotes() %></textarea></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<td class="right">&nbsp;</td>
	</tr>
	<% PageTimeType pt2 = sds.getTotalScriptPages();%>
	<tr>
		<td class="left">&nbsp;</td>
		<td><strong>Total Script Pages </strong></td>
		<td><input name="<% out.print("sd" + a + "_totalpages");%>" type="text" id="<% out.print("sd" + a + "_totalpages");%>" size="4" value="<%= pt2.getNumber() %>" pattern="number" title="Calculate Script Pages.">
		<input name="<% out.print("sd" + a + "_totalpagesnum");%>" type="text" id="<% out.print("sd" + a + "_totalpagesnum");%>" size="2" value="<%= pt2.getNumerator() %>" pattern="number" title="Calculate Script Pages.">
		<strong>/8 pgs</strong> </td>
		<td><input name="button22" type="button" onClick="calculateMod(<% out.print(a);%>);" value="Calculate"/></td>
		<td>&nbsp;</td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr><td colspan="6" class="bottom">&nbsp;</td></tr>
	</table>
</td>
</tr>
	
<% }  //end of loop - a%>
<%} else {%>
<tr><td>
<table width="700" border="0" cellpadding="0" cellspacing="0" id="day1">
	<tr>
		<td width="15" class="header-left">&nbsp;</td>
		<td colspan="4" class="header-middle">Shoot Day # 
		<input name="sd1_number" type="text" id="sd1_number" size="5" value="" pattern="number" title="Enter Shoot Day Number."></td>
		<td width="15" class="header-right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td colspan="4" align="left">&nbsp;</td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Shoot Day Date</strong></td>
		<td width="185" align="left"><input name="sd1_date" type="text" id="sd1_date" value="" title="Enter Shoot Date."></td>
		<td width="150" align="left"><strong>Shoot Day Weekday</strong></td>
		<td width="185" align="left"><input name="sd1_weekday" type="text" id="sd1_weekday" value="" pattern="any_text" title="Enter Weekday."></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Crew Call </strong></td>
		<td width="185" align="left"><input name="sd1_crew" type="text" id="sd1_crew" pattern="time" title="Enter Crew Call. [Time Format HH:MM:SS]"></td>
		<td width="150" align="left"><strong>Travel To Loc </strong></td>
		<td width="185" align="left"><input name="sd1_traveltoloc" type="text" id="sd1_traveltoloc" pattern="time" title="Enter Travel to Location. [Time Format HH:MM:SS]"></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td align="left"><strong>Bump In</strong></td>
		<td colspan="3" align="left"><input name="sd1_bumpin" type="text" id="sd1_bumpin" size="8" value=""></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
		<td class="left">&nbsp;</td>
		<td width="150" align="left"><strong>Start of Day Notes </strong></td>
		<td colspan="3" align="left"><input name="sd1_start" type="text" id="sd1_start" size="73" value=""></td>
		<td class="right">&nbsp;</td>
	</tr>
	<tr>
	<td class="left">&nbsp;</td>
	<td colspan="4" align="left">
		<table width="670" border="0" cellpadding="0" cellspacing="0" id="scenes1">
		<tr><td><table border="0" cellpadding="0" cellspacing="0">
		
		
		<tr><td colspan="6" class="top">&nbsp;</td></tr>
		<tr>
			<td width="15" height="30" class="left">&nbsp;</td>
			<td width="160" height="30" valign="top"><strong>Scene Number</strong></td>
			<td height="30" colspan="3" valign="top"><input name="sd1_scene1" type="text" id="sd1_scene" value="" pattern="any_text" title="Enter Scene Number."></td>
			<td width="15" height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="30" class="left">&nbsp;</td>
			<td height="30" valign="top"><strong>Set Name</strong></td>
			<td height="30" valign="top"><input name="sd1_set1" type="text" id="sd1_set1" value="" pattern="any_text" title="Enter Set Name."></td>
			<td height="30" valign="top"><strong>Synopsis</strong></td>
			<td height="30" valign="top"><input name="sd1_synopsis1" type="text" id="sd1_synopsis1" value="" pattern="any_text" title="Enter Synopsis."></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td width="15" height="30" class="left">&nbsp;</td>
			<td width="160" height="30" valign="top"><strong>Location ID </strong></td>
			<td width="160" height="30" valign="top"><input name="sd1_locationID1" type="text" id="sd1_locationID1" value="" pattern="any_text" title="Enter Location ID."></td>
			<td width="160" height="30" valign="top"><strong>Address</strong></td>
			<td width="160" height="30" valign="top"><input name="sd1_address1" type="text" id="sd1_address" value="" pattern="any_text" title="Enter Address."></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td width="15" height="30" class="left">&nbsp;</td>
			<td width="160" height="30" valign="top"><strong>Interior</strong>
			  <input name="sd1_intext1" type="radio" value="INT" checked></td>
			<td width="160" height="30" valign="top"><strong>Exterior</strong><input name="sd1_intext1" type="radio" value="EXT"></td>
			<td width="160" height="30" valign="top"><strong>Day</strong><input name="sd1_daynight1" type="radio" value="Day" checked></td>
			<td width="160" height="30" valign="top"><strong>Night</strong><input name="sd1_daynight1" type="radio" value="Night"></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="30" class="left">&nbsp;</td>
			<td height="30" valign="top"><strong>No. of Script Pages</strong></td>
			<td height="30" valign="top">
			<input name="sd1_pages1" type="text" id="sd1_pages1" size="4" value="" pattern="number" title="Enter Script Pages.">
			<input name="sd1_pagesnum1" type="text" id="sd1_pagesnum1" size="2" value="" pattern="number" title="Enter Script Pages.">
			<strong>/8 pgs </strong></td>
			<td height="30" valign="top"><strong>Est. Shoot Times </strong></td>
			<td height="30" valign="top"><input name="sd1_shoottimes1" type="text" id="sd1_shoottimes1" value="" pattern="any_text" title="Enter Est. Shoot Times."></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="30" class="left">&nbsp;</td>
			<td height="30" valign="top"><strong>Est. Script Timing </strong></td>
			<td height="30" colspan="3" valign="top"><input name="sd1_scripttime1" type="text" id="sd1_scripttime1" value="" pattern="time" title="Enter Est. Script Timing. [Time Format HH:MM:SS]"></td>
			<td height="30" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="30" rowspan="2" class="left">&nbsp;</td>
			<td height="30" rowspan="2" valign="top"><strong>Characters</strong></td>
			<td height="15" colspan="3" valign="top">
			<table width="510" border="0" cellpadding="0" cellspacing="0" id="sd1_characters1">
				<tr><td><input name="sd1_charactername1_1" type="text" id="sd1_charactername1_1" value="" pattern="any_text" title="Enter Character Name."></td></tr>
			</table>													  
			</td>
			<td height="30" rowspan="2" class="right">&nbsp;</td>
		</tr>
		<tr><td height="15" colspan="3" valign="top"><input name="button3" type="button" onClick="addCharacterRow(1,1);" value="Add Character"/>
		<input name="button3" type="button" onClick="deleteCharacterRow(1,1);" value="Delete Character"/>
		<input name="sd1_charactercount1" type="hidden" id="sd1_charactercount1" value="1"></td></tr>
		<tr>
			<td width="15" height="30" rowspan="2" class="left">&nbsp;</td>
			<td width="160" height="30" rowspan="2" valign="top"><strong>Set Requirements </strong></td>
			<td height="15" colspan="3" valign="top">
				<table width="510" border="0" cellpadding="0" cellspacing="0" id="sd1_requirements1"> 
					<tr valign="top">
					<td><select name="sd1_items1_1" id="sd1_items1_1">
					  <option value="Background Actors">Background Actors</option>
					  <option value="Stunts">Stunts</option>
					  <option value="Vehicles">Vehicles</option>
					  <option value="Props">Props</option>
					  <option value="Camera">Camera</option>
					  <option value="Special Effects">Special Effects</option>
					  <option value="Wardrobe">Wardrobe</option>
					  <option value="Makeup/Hair">Makeup/Hair</option>
					  <option value="Animals">Animals</option>
					  <option value="Animal Wrangler">Animal Wrangler</option>
					  <option value="Music">Music</option>
					  <option value="Sound">Sound</option>
					  <option value="Art Department">Art Department</option>
					  <option value="Set Dressing">Set Dressing</option>
					  <option value="Greenery">Greenery</option>
					  <option value="Special Equipment">Special Equipment</option>
					  <option value="Security">Security</option>
					  <option value="Additional Labour">Additional Labour</option>
					  <option value="Visual Effects">Visual Effects</option>
					  <option value="Mechanical Effects">Mechanical Effects</option>
					  <option value="Miscellaneous">Miscellaneous</option>
					  <option value="Notes">Notes</option>
					</select></td>
					<td><textarea name="sd1_requirements1_1" id="sd1_requirements1_1" cols="20" pattern="any_text" title="Enter Set Requirements."></textarea></td>
					</tr>
				</table>														
			</td>
			<td height="30" rowspan="2" class="right">&nbsp;</td>
		</tr>
		<tr>
			<td height="15" colspan="3" valign="top"><input name="requirements1" type="button" id="requirements1" onClick="addRequirementsRow(1,1);" value="Add Set Requirements">
			<input name="requirements1" type="button" id="requirements1" onClick="deleteRequirementsRow(1,1);" value="Delete Set Requirements">
			<input name="sd1_requirementscount1" type="hidden" id="sd1_requirementscount1" value="1"></td>
		</tr>
		<tr>
			<td class="left">&nbsp;</td>
			<td><input name="sd1_mealbreakbutton1" type="button" id="sd1_mealbreakbutton1" onClick="addMealBreak(1,1);" value="Add Meal Break"></td>
			<td colspan="3">
				<table width="510" border="0" cellpadding="0" cellspacing="0" id="sd1_mealbreak1">
				
				</table></td>
			<td class="right">&nbsp;</td>
		</tr>
		<tr><td colspan="6" class="bottom">&nbsp;</td></tr>
		
		
		</table>
		</td></tr>
		</table>											
	</td>
	<td class="right">&nbsp;</td>
	</tr>
	<tr>
	<td class="left">&nbsp;</td>
	<td colspan="4" align="left"><input name="button2" type="button" onClick="addScene('scenes1',1);" value="Add Scene"/>
	  <input name="button23" type="button" onClick="deleteScene('scenes1',1);" value="Delete Scene"/>
	<input name="day_number_1" type="hidden" id="day_number_1" value="1">
	<input name="table_count_1" type="hidden" id="table_count_1" value="1"></td>
	<td class="right">&nbsp;</td>
	</tr>
	<tr>
	<td class="left">&nbsp;</td>
	<td width="150"><strong>End of Day Notes</strong></td>
	<td colspan="3">
	<p>
	<textarea name="sd1_end" cols="50" id="sd1_end" pattern="any_text" title="Enter End of Day Notes."></textarea>
	</p></td>
	<td class="right">&nbsp;</td>
	</tr>
	<tr>
	<td class="left">&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td>&nbsp;</td>
	<td class="right">&nbsp;</td>
	</tr>
	<tr>
	<td class="left">&nbsp;</td>
	<td><strong>Total Script Pages </strong></td>
	<td><input name="sd1_totalpages" type="text" id="sd1_totalpages" size="4" value="" pattern="number" title="Calculate Script Pages.">
	<input name="sd1_totalpagesnum" type="text" id="sd1_totalpagesnum" size="2" value="" pattern="number" title="Calculate Script Pages.">
	<strong>/8 pgs</strong> </td>
	<td><input name="button22" type="button" onClick="calculateMod(1);" value="Calculate"/></td>
	<td>&nbsp;</td>
	<td class="right">&nbsp;</td>
	</tr>
	<tr><td colspan="6" class="bottom">&nbsp;</td></tr>
	</table>	
	</td>
</tr>
<%}%>		
</table>					
</td>
</tr>
</table>
<p align="center">
<input name="button" type="button" onClick="addShootingDay();" value="Add Shooting Day"/>
<input name="button4" type="button" onClick="deleteShootingDay();" value="Delete Shooting Day"/>
<br>
<input type="button" value="Print"  onclick="window.print()">
<input type="submit" name="Save" value="Save" onclick="return validateFields('form1');">
<input type="submit" name="Submission" value="Submission" onclick="return validateFields('form1');">

<br>
<input type="hidden" name="workItemID" id="workItemID">
<input type="hidden" name="userID" id="userID">
<input type="hidden" name="sessionHandle" id="sessionHandle">
<input type="hidden" name="JSESSIONID" id="JSESSIONID">
<input type="hidden" name="submit" id="submit">

<input name="days" type="hidden" id="days" value="<%if (a==0) {out.print("1");}else{out.print(a);}%>">
</p>
</form>		</td>
<td width="14" class="background_right">&nbsp;</td></tr>
<tr><td colspan="3">
<!-- LOAD -->
<form method="post" action="Input_Shooting_Schedule_3255.jsp?formType=load&workItemID=<%= request.getParameter("workItemID") %>&userID=<%= request.getParameter("userID") %>&sessionHandle=<%= request.getParameter("sessionHandle") %>&JSESSIONID=<%= request.getParameter("JSESSIONID") %>&submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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
<!-- END LOAD -->
</td></tr>
<tr>
<td colspan="3" class="background_bottom">&nbsp;</td>
</tr>
</table>

<%
if(request.getParameter("Submission") != null){
	java.text.SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
	java.text.SimpleDateFormat df2 = new SimpleDateFormat("dd-MM-yyyy");
	int total_scenes = 0;
	int total_pages = 0;
	int total_pagesnum = 0;
	String array[] = {"","",""}; 
	int original_timing_array[] = {0,0,0}; 
	String original_timing;
	int days_count = Integer.parseInt(request.getParameter("days"));
	ShootingScheduleType sst = new ShootingScheduleType();
	//start of singledayschedule loop
	for(int current_day=1; current_day<=days_count; current_day ++) {
		SingleDayScheduleType sdst = new SingleDayScheduleType();
		sdst.setShootDayNo(new BigInteger(request.getParameter("sd"+current_day+"_number")));
		sdst.setShootDayDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("sd"+current_day+"_date")))));
		sdst.setShootDayWeekday(request.getParameter("sd"+current_day+"_weekday"));
		sdst.setCrewCall(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_crew")));
		sdst.setTravelToLoc(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_traveltoloc")));
		if(!(request.getParameter("sd"+current_day+"_bumpin").equals(""))) {
			sdst.setBumpIn(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_bumpin")));
		}
		if(!(request.getParameter("sd"+current_day+"_start").equals(""))) {
			sdst.setStartDayNotes(request.getParameter("sd"+current_day+"_start"));
		}
		
		int table_count = Integer.parseInt(request.getParameter("table_count_"+current_day));
		total_scenes += table_count;
		//start of sceneschedule loop
		for(int current_table=1; current_table<=table_count; current_table ++) {
			SceneScheduleType scst = new SceneScheduleType();
			scst.setScene(request.getParameter("sd"+current_day+"_scene"+current_table));
			PageTimeType ptt = new PageTimeType();
			ptt.setNumber(new BigInteger(request.getParameter("sd"+current_day+"_pages"+current_table)));
			ptt.setNumerator(new BigInteger(request.getParameter("sd"+current_day+"_pagesnum"+current_table)));
			scst.setPageTime(ptt);
			scst.setDN(request.getParameter("sd"+current_day+"_daynight"+current_table));
			scst.setINEX(request.getParameter("sd"+current_day+"_intext"+current_table));
			scst.setLocationID(request.getParameter("sd"+current_day+"_locationID"+current_table));
			scst.setSet(request.getParameter("sd"+current_day+"_set"+current_table));
			scst.setAddress(request.getParameter("sd"+current_day+"_address"+current_table));
			scst.setSynopsis(request.getParameter("sd"+current_day+"_synopsis"+current_table));
			
			int character_count = Integer.parseInt(request.getParameter("sd"+current_day+"_charactercount"+current_table));
			CharactersType ct = new CharactersType();
			//start of character loop
			for(int current_character=1; current_character<=character_count; current_character ++) {
				ct.getCharacter().add(request.getParameter("sd"+current_day+"_charactername"+current_table+"_"+current_character));
			}//end of character loop
			scst.setCharacters(ct);
			
			scst.setEstShootTimes(request.getParameter("sd"+current_day+"_shoottimes"+current_table));
			scst.setEstScriptTiming(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_scripttime"+current_table)));
			original_timing = request.getParameter("sd"+current_day+"_scripttime"+current_table);
			array = original_timing.split(":");
			original_timing_array[0] += Integer.parseInt(array[0]);
			original_timing_array[1] += Integer.parseInt(array[1]);
			original_timing_array[2] += Integer.parseInt(array[2]);
			
			int requirements_count = Integer.parseInt(request.getParameter("sd"+current_day+"_requirementscount"+current_table));
			SetRequirementsType srt = new SetRequirementsType();
			//start of setrequirements loop
			for(int current_requirements=1; current_requirements<=requirements_count; current_requirements ++) {
				SingleEntryType set = new SingleEntryType();
				set.setItem(request.getParameter("sd"+current_day+"_items"+current_table+"_"+current_requirements));
				set.setRequirements(request.getParameter("sd"+current_day+"_requirements"+current_table+"_"+current_requirements));
				srt.getSingleEntry().add(set);
			}//end of setrequirements loop
			
			scst.setSetRequirements(srt);
			if (request.getParameter("sd"+current_day+"_meal"+current_table) != null) {
				MealBreakType mbt = new MealBreakType();
				mbt.setMeal(request.getParameter("sd"+current_day+"_meal"+current_table));				
				mbt.setBreak(request.getParameter("sd"+current_day+"_break"+current_table));
				scst.setMealBreak(mbt);
			}
			sdst.getSceneSchedule().add(scst);
		}//end of sceneschedule loop
		sdst.setEndDayNotes(request.getParameter("sd"+current_day+"_end"));
		total_pages += Integer.parseInt(request.getParameter("sd"+current_day+"_totalpages"));
		total_pagesnum += Integer.parseInt(request.getParameter("sd"+current_day+"_totalpagesnum"));
		PageTimeType ptt2 = new PageTimeType();
		ptt2.setNumber(new BigInteger(request.getParameter("sd"+current_day+"_totalpages")));
		ptt2.setNumerator(new BigInteger(request.getParameter("sd"+current_day+"_totalpagesnum")));
		sdst.setTotalScriptPages(ptt2);
		sst.getSingleDaySchedule().add(sdst);
	}// end of singledayschedule loop
		
		
	sst.setLastUpdatedDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("last_updated")))));
	sst.setDirector(request.getParameter("director"));
	sst.setProducer(request.getParameter("producer"));
	sst.setStartDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("start_date")))));
	sst.setScheduledFinish(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("scheduled_finish")))));
	if(!(request.getParameter("revised_finish").equals(""))){
		sst.setRevisedFinish(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("revised_finish")))));
	}
	sst.setScheduledShootingDays(new BigInteger(request.getParameter("scheduled_shooting_days")));
	
	
	iss.setProduction(request.getParameter("production"));
	iss.setShootingSchedule(sst); 
	String total_scenes_string = Integer.toString(total_scenes);
	iss.setTotalScenes(new BigInteger(total_scenes_string));
	
	int final_pages = total_pages + (int)Math.floor(total_pagesnum / 8);
	int final_pagesnum = total_pagesnum % 8;
	String final_pages_string = Integer.toString(final_pages);
	String final_pagesnum_string = Integer.toString(final_pagesnum);
		PageTimeType ptt3 = new PageTimeType();
		ptt3.setNumber(new BigInteger(final_pages_string));
		ptt3.setNumerator(new BigInteger(final_pagesnum_string));
	iss.setTotalPageTime(ptt3);
	
	original_timing_array[1] = original_timing_array[1] + (int)Math.floor(original_timing_array[2] / 60);
	original_timing_array[2] = original_timing_array[2] % 60;
	
	original_timing_array[0] = original_timing_array[0] + (int)Math.floor(original_timing_array[1] / 60);
	original_timing_array[1] = original_timing_array[1] % 60;
	
	array[0] = Integer.toString(original_timing_array[0]);
	if (array[0].length() <= 1) {
		array[0] = "0" + array[0];
	}
	
	array[1] = Integer.toString(original_timing_array[1]);
	if (array[1].length() <= 1) {
		array[1] = "0" + array[1];
	}
	
	array[2] = Integer.toString(original_timing_array[2]);
	if (array[2].length() <= 1) {
		array[2] = "0" + array[2];
	}
	
	
	original_timing = array[0] + ":" + array[1] + ":" + array[2];
	iss.setOriginalTiming(XMLGregorianCalendarImpl.parse(original_timing));
	
	
	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    File f = new File("./backup/ShootingSchedule_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
    m.marshal( issElement,  f);//output to file
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(issElement, xmlOS);//out to ByteArray
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
	java.text.SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
	java.text.SimpleDateFormat df2 = new SimpleDateFormat("dd-MM-yyyy");
	int total_scenes = 0;
	int total_pages = 0;
	int total_pagesnum = 0;
	String array[] = {"","",""}; 
	int original_timing_array[] = {0,0,0}; 
	String original_timing;
	int days_count = Integer.parseInt(request.getParameter("days"));
	ShootingScheduleType sst = new ShootingScheduleType();
	//start of singledayschedule loop
	for(int current_day=1; current_day<=days_count; current_day ++) {
		SingleDayScheduleType sdst = new SingleDayScheduleType();
		sdst.setShootDayNo(new BigInteger(request.getParameter("sd"+current_day+"_number")));
		sdst.setShootDayDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("sd"+current_day+"_date")))));
		sdst.setShootDayWeekday(request.getParameter("sd"+current_day+"_weekday"));
		sdst.setCrewCall(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_crew")));
		sdst.setTravelToLoc(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_traveltoloc")));
		if(!(request.getParameter("sd"+current_day+"_bumpin").equals(""))) {
			sdst.setBumpIn(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_bumpin")));
		}
		if(!(request.getParameter("sd"+current_day+"_start").equals(""))) {
			sdst.setStartDayNotes(request.getParameter("sd"+current_day+"_start"));
		}
		
		int table_count = Integer.parseInt(request.getParameter("table_count_"+current_day));
		total_scenes += table_count;
		//start of sceneschedule loop
		for(int current_table=1; current_table<=table_count; current_table ++) {
			SceneScheduleType scst = new SceneScheduleType();
			scst.setScene(request.getParameter("sd"+current_day+"_scene"+current_table));
			PageTimeType ptt = new PageTimeType();
			ptt.setNumber(new BigInteger(request.getParameter("sd"+current_day+"_pages"+current_table)));
			ptt.setNumerator(new BigInteger(request.getParameter("sd"+current_day+"_pagesnum"+current_table)));
			scst.setPageTime(ptt);
			scst.setDN(request.getParameter("sd"+current_day+"_daynight"+current_table));
			scst.setINEX(request.getParameter("sd"+current_day+"_intext"+current_table));
			scst.setLocationID(request.getParameter("sd"+current_day+"_locationID"+current_table));
			scst.setSet(request.getParameter("sd"+current_day+"_set"+current_table));
			scst.setAddress(request.getParameter("sd"+current_day+"_address"+current_table));
			scst.setSynopsis(request.getParameter("sd"+current_day+"_synopsis"+current_table));
			
			int character_count = Integer.parseInt(request.getParameter("sd"+current_day+"_charactercount"+current_table));
			CharactersType ct = new CharactersType();
			//start of character loop
			for(int current_character=1; current_character<=character_count; current_character ++) {
				ct.getCharacter().add(request.getParameter("sd"+current_day+"_charactername"+current_table+"_"+current_character));
			}//end of character loop
			scst.setCharacters(ct);
			
			scst.setEstShootTimes(request.getParameter("sd"+current_day+"_shoottimes"+current_table));
			scst.setEstScriptTiming(XMLGregorianCalendarImpl.parse(request.getParameter("sd"+current_day+"_scripttime"+current_table)));
			original_timing = request.getParameter("sd"+current_day+"_scripttime"+current_table);
			array = original_timing.split(":");
			original_timing_array[0] += Integer.parseInt(array[0]);
			original_timing_array[1] += Integer.parseInt(array[1]);
			original_timing_array[2] += Integer.parseInt(array[2]);
			
			int requirements_count = Integer.parseInt(request.getParameter("sd"+current_day+"_requirementscount"+current_table));
			SetRequirementsType srt = new SetRequirementsType();
			//start of setrequirements loop
			for(int current_requirements=1; current_requirements<=requirements_count; current_requirements ++) {
				SingleEntryType set = new SingleEntryType();
				set.setItem(request.getParameter("sd"+current_day+"_items"+current_table+"_"+current_requirements));
				set.setRequirements(request.getParameter("sd"+current_day+"_requirements"+current_table+"_"+current_requirements));
				srt.getSingleEntry().add(set);
			}//end of setrequirements loop
			
			scst.setSetRequirements(srt);
			if (request.getParameter("sd"+current_day+"_meal"+current_table) != null) {
				MealBreakType mbt = new MealBreakType();
				mbt.setMeal(request.getParameter("sd"+current_day+"_meal"+current_table));				
				mbt.setBreak(request.getParameter("sd"+current_day+"_break"+current_table));
				scst.setMealBreak(mbt);
			}
			sdst.getSceneSchedule().add(scst);
		}//end of sceneschedule loop
		sdst.setEndDayNotes(request.getParameter("sd"+current_day+"_end"));
		total_pages += Integer.parseInt(request.getParameter("sd"+current_day+"_totalpages"));
		total_pagesnum += Integer.parseInt(request.getParameter("sd"+current_day+"_totalpagesnum"));
		PageTimeType ptt2 = new PageTimeType();
		ptt2.setNumber(new BigInteger(request.getParameter("sd"+current_day+"_totalpages")));
		ptt2.setNumerator(new BigInteger(request.getParameter("sd"+current_day+"_totalpagesnum")));
		sdst.setTotalScriptPages(ptt2);
		sst.getSingleDaySchedule().add(sdst);
	}// end of singledayschedule loop
		
	
	sst.setLastUpdatedDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("last_updated")))));
	sst.setDirector(request.getParameter("director"));
	sst.setProducer(request.getParameter("producer"));
	sst.setStartDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("start_date")))));
	sst.setScheduledFinish(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("scheduled_finish")))));
	if(!(request.getParameter("revised_finish").equals(""))){
		sst.setRevisedFinish(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("revised_finish")))));
	}
	sst.setScheduledShootingDays(new BigInteger(request.getParameter("scheduled_shooting_days")));
	
	
	iss.setProduction(request.getParameter("production"));
	iss.setShootingSchedule(sst); 
	String total_scenes_string = Integer.toString(total_scenes);
	iss.setTotalScenes(new BigInteger(total_scenes_string));
	
	int final_pages = total_pages + (int)Math.floor(total_pagesnum / 8);
	int final_pagesnum = total_pagesnum % 8;
	String final_pages_string = Integer.toString(final_pages);
	String final_pagesnum_string = Integer.toString(final_pagesnum);
		PageTimeType ptt3 = new PageTimeType();
		ptt3.setNumber(new BigInteger(final_pages_string));
		ptt3.setNumerator(new BigInteger(final_pagesnum_string));
	iss.setTotalPageTime(ptt3);
	
	original_timing_array[1] = original_timing_array[1] + (int)Math.floor(original_timing_array[2] / 60);
	original_timing_array[2] = original_timing_array[2] % 60;
	
	original_timing_array[0] = original_timing_array[0] + (int)Math.floor(original_timing_array[1] / 60);
	original_timing_array[1] = original_timing_array[1] % 60;
	
	
	array[0] = Integer.toString(original_timing_array[0]);
	if (array[0].length() <= 1) {
		array[0] = "0" + array[0];
	}
	
	array[1] = Integer.toString(original_timing_array[1]);
	if (array[1].length() <= 1) {
		array[1] = "0" + array[1];
	}
	
	array[2] = Integer.toString(original_timing_array[2]);
	if (array[2].length() <= 1) {
		array[2] = "0" + array[2];
	}
	
	
	original_timing = array[0] + ":" + array[1] + ":" + array[2];
	iss.setOriginalTiming(XMLGregorianCalendarImpl.parse(original_timing));
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(issElement, xmlOS);//out to ByteArray

	response.setHeader("Content-Disposition", "attachment;filename=\"ShootingSchedule_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
	response.setHeader("Content-Type", "text/xml");

	ServletOutputStream outs = response.getOutputStream();
	xmlOS.writeTo(outs);
	outs.close();
}
%>

</body>
</html>