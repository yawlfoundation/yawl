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
<%@ page import="org.yawlfoundation.sb.callsheet.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="1024kb" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Call Sheet</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<!-- style sheet imports -->
<link href="graphics/style.css" rel="stylesheet" type="text/css" />
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js" ></script>
<script type="text/javascript" src="scripts/updateCallSheet.js" ></script>

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
            int endOfFile = result.indexOf("</ns2:Update_Call_Sheet>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(
                    beginOfFile,
                    endOfFile + 24);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		xml = "<?xml version='1.0' encoding='UTF-8'?><ns2:Update_Call_Sheet xmlns:ns2='http://www.yawlfoundation.org/sb/callSheet' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.yawlfoundation.org/sb/callSheet callSheetType.xsd '><generalInfo><production>production</production><date>2001-01-01</date><weekday>weekday</weekday><shootDayNo>0</shootDayNo></generalInfo><callSheet><emergencyInfo><fireAmbulance>fireAmbulance</fireAmbulance><hospital>hospital</hospital><police>police</police></emergencyInfo><director>director</director><producer>producer</producer><productionManager>productionManager</productionManager><firstAD>firstAD</firstAD><sunrise>sunrise</sunrise><sunset>sunset</sunset><weather>weather</weather><callTimes><crewCall><callTime>12:00:00</callTime><callLoc>callLoc</callLoc></crewCall><locationCall><callTime>12:00:00</callTime><callLoc>callLoc</callLoc></locationCall><makeupHair><callTime>12:00:00</callTime><callLoc>callLoc</callLoc></makeupHair><wardrobe><callTime>12:00:00</callTime><callLoc>callLoc</callLoc></wardrobe><unit><callTime>12:00:00</callTime><callLoc>callLoc</callLoc></unit><other><call>call</call><callTime>12:00:00</callTime><callLoc>callLoc</callLoc></other><breakfast><from>12:00:00</from><to>12:00:00</to><callLoc>callLoc</callLoc></breakfast></callTimes><wrapTimes><estWrap>estWrap</estWrap><other><wrap>wrap</wrap><wrapTime>wrapTime</wrapTime></other></wrapTimes><location><singleLocation><locationName>locationName</locationName><address>address</address><contact>contact</contact><contactNo>contactNo</contactNo><UBDMapRef>UBDMapRef</UBDMapRef><notes>notes</notes></singleLocation></location><dailySchedule><startDayNotes>startDayNotes</startDayNotes><sceneSchedule><scene>scene</scene><pageTime><number>0</number><numerator>0</numerator></pageTime><D_N>D_N</D_N><IN_EX>IN_EX</IN_EX><setLocation>setLocation</setLocation><synopsis>synopsis</synopsis><artistTimeInfo><character>character</character><artist>artist</artist><pickup>pickup</pickup><makeup>makeup</makeup><wardrobe>wardrobe</wardrobe><onSet>onSet</onSet></artistTimeInfo><estShootTimes>estShootTimes</estShootTimes><mealBreak><meal>meal</meal><break>break</break></mealBreak></sceneSchedule><endDayNotes>endDayNotes</endDayNotes><totalScriptPages><number>0</number><numerator>0</numerator></totalScriptPages></dailySchedule><dailySetRequirements><singleEntry><item>item</item><description><scene>scene</scene><requirements>requirements</requirements></description></singleEntry></dailySetRequirements><unit>unit</unit><additionalEquipment>additionalEquipment</additionalEquipment><additionalCrew>additionalCrew</additionalCrew><directions>directions</directions><parking>parking</parking><catering><singleMeal><meal>meal</meal><break>break</break><serveNo>serveNo</serveNo><location>location</location></singleMeal></catering><productionNotes>productionNotes</productionNotes><locationCrewNotes>locationCrewNotes</locationCrewNotes><lunchPickup>lunchPickup</lunchPickup><exposedRushes>exposedRushes</exposedRushes><rushesScreening>rushesScreening</rushesScreening><crewAgreements>crewAgreements</crewAgreements><advancedSchedule><shootDayNo>0</shootDayNo><shootDayDate>2001-01-01</shootDayDate><shootDayWeekday>shootDayWeekday</shootDayWeekday><adSceneSchedule><scene>scene</scene><D_N>day</D_N><IN_EX>int</IN_EX><pageTime><number>0</number><numerator>0</numerator></pageTime><setSynopsis>setSynopsis</setSynopsis><location>location</location><characters>characters</characters></adSceneSchedule></advancedSchedule><additionalNotes>additionalNotes</additionalNotes></callSheet><finalSubmission>true</finalSubmission></ns2:Update_Call_Sheet>";
		//String xml = (String)session.getAttribute("outputData");
		xml = xml.replaceAll("<Update_Call_Sheet", "<ns2:Update_Call_Sheet xmlns:ns2='http://www.yawlfoundation.org/sb/callSheet'");
		xml = xml.replaceAll("</Update_Call_Sheet","</ns2:Update_Call_Sheet");
		
	}
	            
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.callsheet");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement ucstElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
	UpdateCallSheetType ucst = (UpdateCallSheetType)ucstElement.getValue();
	
	GeneralInfoType git = ucst.getGeneralInfo();
	CallSheetType cst = ucst.getCallSheet();
	
	EmergencyInfoType eit = cst.getEmergencyInfo();
%>
<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
	<tr><td colspan="3" class="background_top">&nbsp;</td></tr>
	<tr>
		<td width="14" class="background_left">&nbsp;</td>
		<td>
			<h1 align="center">Call Sheet</h1>      
			<form name="form1" method="post">
			<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
				<%-- Emergency Information Table --%>
				<tr>
					<td>
						<table width='700' border='0' cellspacing='0' cellpadding='0' id="emergency_info">
							<tr>
								<td width="15" align="right" class="header-left">&nbsp;</td>
								<td height="20" colspan='2' class="header-middle">Emergency Information</td>
								<td width="15" class="header-right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td><strong>Fire/Ambulance</strong></td>
								<td><input name='fire_ambulance' type='text' id='fire_ambulance' value="<%= eit.getFireAmbulance() %>" size="80"></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td><strong>Hospital</strong></td>
								<td><input name='hospital' type='text' id='hospital' value="<%= eit.getHospital() %>" size="80"></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td><strong>Police</strong></td>
								<td><input name='police' type='text' id='police' value="<%= eit.getPolice() %>" size="80"></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan='4' class='bottom'>&nbsp;</td></tr>
						</table>					
					</td>
				</tr>
				<%-- General Table --%>
				<tr>
					<td>
						<table width='700' border='0' cellspacing='0' cellpadding='0' id="general_info">
							<tr>
								<td width="15" align="right" class="header-left">&nbsp;</td>
								<td height="20" colspan='4' class="header-middle">General</td>
								<td width="15" class="header-right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td><strong>Production</strong></td>
								<td><input name='production' type='text' id='production' value="<%= git.getProduction() %>" size="15" readonly></td>
								<td><strong>Date</strong></td>
								<td><input name='date' type='text' id='date' value="<%= git.getDate() %>" size="15" readonly></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<tr>
								<td class="left">&nbsp;</td>
								<td><strong>Day</strong></td>
								<td><input name='weekday' type='text' id='weekday' value="<%= git.getWeekday() %>" size="15" readonly></td>
								<td><strong>Shoot Day </strong></td>
								<td><input name='shoot_day' type='text' id='shoot_day' value="<%= git.getShootDayNo() %>" size="15" readonly></td>
								<td class="right">&nbsp;</td>
							</tr>
							<tr><td colspan='6' class='bottom'>&nbsp;</td></tr>
						</table>					
					</td>
				</tr>
				<%-- Essential Crew / Contact Table --%>
				<tr>
					<td>
						<table width='700' border='0' cellspacing='0' cellpadding='0' id="essential_crew_contact">
							<tr>
								<td width="15" align="right" class="header-left">&nbsp;</td>
								<td height="20" colspan='4' class="header-middle">Essential Crew / Contact </td>
								<td width="15" class="header-right">&nbsp;</td>
							</tr>
							<tr>
								<td class="left">&nbsp;</td>
								<td><strong>Director</strong></td>
								<td><input name='director' type='text' id='director' value="<%= cst.getDirector() %>" size="15"></td>
								<td><strong>Producer</strong></td>
								<td><input name='producer' type='text' id='producer' value="<%= cst.getProducer () %>" size="15"></td>
								<td class="right">&nbsp;</td>
							</tr>
							<tr>
								<td class="left">&nbsp;</td>
								<td><strong>Production Manager </strong></td>
								<td colspan="3">
									<table width="510" border="0" cellspacing="0" cellpadding="0">
										<tr>
											<td><input name='production_manager' type='text' id='production_manager' size="80" 
												value="<%List<String> pm_list=cst.getProductionManager();int n_pm = pm_list.size();for (String pm : pm_list){out.print(pm);if ((--n_pm)!=0)out.print(" / ");}%> " readonly>											</td>
										</tr>
									</table>								
								</td>
								<td class="right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td><strong>1st AD </strong></td>
								<td colspan="3"><input name='first_ad' type='text' id='first_ad' value="<%= cst.getFirstAD() %>" size="80"></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan='6' class='bottom'>&nbsp;</td></tr>
						</table>					
					</td>
				</tr>
				<%-- Weather Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellspacing="0" cellpadding="0" id="weather">
							<tr>
								<td width="15" class="header-left">&nbsp;</td>
								<td colspan="4" class="header-middle">Weather </td>
								<td width="15" class="header-right">&nbsp;</td>
							</tr>
							<tr>
								<td class="left">&nbsp;</td>
								<td align="center" valign="top"><strong>Sunrise</strong></td>
								<td valign="top"><input name="sunrise" type="text" id="sunrise" value="<%= cst.getSunrise() %>"></td>
								<td align="center" valign="top"><strong>Sunset </strong></td>
								<td valign="top"><input name="sunset" type="text" id="sunset" value="<%= cst.getSunset() %>"></td>
								<td class="right">&nbsp;</td>
							</tr>
							<tr>
								<td class="left">&nbsp;</td>
								<td align="center" valign="top"><strong>Forecast</strong></td>
								<td colspan="3" valign="top"><input name="forecast" type="text" id="forecast" value="<%= cst.getWeather() %>" size="85"></td>
								<td class="right">&nbsp;</td>
							</tr>
							<tr><td colspan="6" class="bottom">&nbsp;</td></tr>
						</table>					
					</td>
				</tr>
				<%-- Call Times Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellpadding="0" cellspacing="0" id="call_times">
							<tbody>
							<tr>
								<td class="header-left">&nbsp;</td>
								<td colspan="3" class="header-middle">Call Times </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td align="center"><strong>Calls</strong></td>
								<td align="center"><strong>Time</strong></td>
								<td align="center"><strong>Location</strong></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<% CallTimesType ctt = cst.getCallTimes();%>
							<% CallType ct1 = ctt.getCrewCall();%>
							<tr>
								<td width="15" height="30" class="left">&nbsp;</td>
								<td height="30" valign="top"><strong>Crew  </strong></td>
								<td height="30" align="center" valign="top"><input name="crew_call_time" type="text" id="crew_call_time" value="<%= ct1.getCallTime() %>" size="25"></td>
								<td align="center" valign="top"><input name="crew_call_location" type="text" id="crew_call_location" value="<% if(ct1.getCallLoc() != null) { out.print(ct1.getCallLoc());} %>" size="25"></td>
								<td width="15" height="30" class="right">&nbsp;</td>
							</tr>
							<% CallType ct2 = ctt.getLocationCall();%>
							<tr>
								<td width="15" height="30" class="left">&nbsp;</td>
								<td height="30" valign="top"><strong>Location  </strong></td>
								<td height="30" align="center" valign="top"><input name="location_call_time" type="text" id="location_call_time" value="<%= ct2.getCallTime() %>" size="25"></td>
								<td align="center" valign="top"><input name="location_call_location" type="text" id="location_call_location" value="<% if(ct2.getCallLoc() != null) { out.print(ct2.getCallLoc());} %>" size="25"></td>
								<td width="15" height="30" class="right">&nbsp;</td>
							</tr>
							<% CallType ct3 = ctt.getMakeupHair();%>
							<tr>
								<td width="15" height="30" class="left">&nbsp;</td>
								<td height="30" valign="top"><strong>Makeup/Hair  </strong></td>
								<td height="30" align="center" valign="top"><input name="makeup_call_time" type="text" id="makeup_call_time" value="<%= ct3.getCallTime() %>" size="25"></td>
								<td align="center" valign="top"><input name="makeup_call_location" type="text" id="makeup_call_location" value="<% if(ct3.getCallLoc() != null) { out.print(ct3.getCallLoc());} %>" size="25"></td>
								<td width="15" height="30" class="right">&nbsp;</td>
							</tr>
							<% CallType ct4 = ctt.getWardrobe();%>
							<tr>
								<td width="15" height="30" class="left">&nbsp;</td>
								<td height="30" valign="top"><strong>Wardrobe  </strong></td>
								<td height="30" align="center" valign="top"><input name="wardrobe_call_time" type="text" id="wardrobe_call_time" value="<%= ct4.getCallTime() %>" size="25"></td>
								<td align="center" valign="top"><input name="wardrobe_call_location" type="text" id="wardrobe_call_location" value="<% if(ct4.getCallLoc() != null) { out.print(ct4.getCallLoc());} %>" size="25"></td>
								<td width="15" height="30" class="right">&nbsp;</td>
							</tr>
							<% CallType ct5 = ctt.getUnit();%>
							<tr>
								<td width="15" height="30" class="left">&nbsp;</td>
								<td height="30" valign="top"><strong>Unit  </strong></td>
								<td height="30" align="center" valign="top"><input name="unit_call_time" type="text" id="unit_call_time" value="<%= ct5.getCallTime() %>" size="25"></td>
								<td align="center" valign="top"><input name="unit_call_location" type="text" id="unit_call_location" value="<% if(ct5.getCallLoc() != null) { out.print(ct5.getCallLoc());} %>" size="25"></td>
								<td width="15" height="30" class="right">&nbsp;</td>
							</tr>
                          <%int call_count = 0;
							for (OtherCallType oct : ctt.getOther()) {
								call_count ++;
							%>
						  <tr>
							  <td class="left">&nbsp;</td>
							  <td><input name="call_<%=call_count%>" type="text" id="call_<%=call_count%>" value="<%= oct.getCallTime() %>" size="25" title="call name" pattern="any_text"></td>
						    <td align="center"><input name="call_time_<%=call_count%>" type="text" id="call_time_<%=call_count%>" value="<%= oct.getCallTime() %>" size="25" title="call time"  pattern="date"> </td>
							  <td align="center"><input name="call_location_<%=call_count%>" type="text" id="call_location_<%=call_count%>" value="<% if(oct.getCallLoc() != null) { out.print(oct.getCallLoc());} %>" size="25" title="call location"  pattern="any_text"></td>
							  <td class="right">&nbsp;</td>
						  </tr>
						  <% }%>
							</tbody>
							<% BreakfastType ft = ctt.getBreakfast();%>
							<tr>
							  <td class="left">&nbsp;</td>
							  <td><strong>Breakfast</strong></td>
							  <td align="center"><input name="breakfast_from" type="text" id="breakfast_from" value="<%= ft.getFrom() %>" size="10"> 
							    - 
					          <input name="breakfast_to" type="text" id="breakfast_to" value="<%= ft.getTo() %>" size="10"></td>
							  <td align="center"><input name="breakfast_location" type="text" id="breakfast_location>" value="<% if(ft.getCallLoc() != null) { out.print(ft.getCallLoc());} %>" size="25"></td>
							  <td class="right">&nbsp;</td>
						  </tr>
							<tr>
								<td class="left">&nbsp;</td>
							    <td colspan="3">
									<input name="button100" type="button" onClick="addCallTimesRow();" value="Insert Row">
									<input name="button101" type="button" onClick="deleteCallTimesRow();" value="Delete Row">
                                 	<input name="call_count" type="hidden" id="call_count" size="15" value="<%=call_count%>">
								</td>
							    <td class="right">&nbsp;</td>
							</tr>
							<tr>
							  <td colspan="5" class="bottom">&nbsp;</td>
						  </tr>
					  </table>					
					</td>
				</tr>
				<%-- Wrap Times Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellspacing="0" cellpadding="0" id="other_times">
							<tbody>
							<tr>
								<td class="header-left">&nbsp;</td>
								<td colspan="2" class="header-middle">Wrap Times </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<% WrapTimesType wtt = cst.getWrapTimes();%>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td><strong>Estimated Wrap </strong></td>
								<td align="left"><input name="estimated_wrap" type="text" id="estimated_wrap" value="<%= wtt.getEstWrap() %>" size="15" pattern="date" title="enter estimated wrap time"></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<% int others_count = 0;
							if(wtt.getOther() != null ) {
							for(OtherWrapType owt : wtt.getOther()) {
								others_count ++;%>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td align="left"><input name="wrap_<%=others_count%>" type="text" id="wrap_<%=others_count%>" value="<%= owt.getWrap() %>" size="15" pattern="any_text" title="enter wrap name"></td>
								<td align="left"><input name="wrap_time_<%=others_count%>" type="text" id="wrap_time_<%=others_count%>" value="<%= owt.getWrapTime() %>" size="15" pattern="date" title="enter wrap time"></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>	
								
							<% }
							} %>
							</tbody>
							<tr>
								<td class="left">&nbsp;</td>
							    <td colspan="1"><input name="button400" type="button" onClick="addWrapTimesRow();" value="Insert Row">
							        <input name="button401" type="button" onClick="deleteWrapTimesRow();" value="Delete Row">
                                    <input name="others_count" type="hidden" id="others_count" size="15" value="<%=others_count%>"></td>
							    <td>&nbsp;</td>
							    <td class="right">&nbsp;</td>
							</tr>
							<tr>
							  <td colspan="4" class="bottom">&nbsp;</td>
						  </tr>
						</table>					
					</td>
				</tr>
				<%-- Locations Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellpadding="0" cellspacing="0" id="locations">
							<tbody>
							<tr>
								<td class="header-left">&nbsp;</td>
								<td colspan="6" class="header-middle">Locations </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td align="center"><strong>Location Name</strong></td>
								<td align="center"><strong>Address</strong></td>
								<td align="center"><strong>Contact </strong></td>
								<td align="center"><strong>Contact No.</strong></td>
								<td align="center"><strong>UBD Ref </strong></td>
								<td align="center"><strong>Notes</strong></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<% LocationType lt = cst.getLocation();
							int locations_count = 0;
							for(SingleLocationType slt : lt.getSingleLocation()) {
								locations_count ++;%>
							<tr valign="top">
								<td width="15" align="center" class="left">&nbsp;</td>
								<td align="center"><input name="locations_name_<%=locations_count %>" type="text" id="locations_name_<%=locations_count %>" value="<%= slt.getLocationName() %>" size="15" pattern="any_text" title="location name"></td>
								<td align="center"><input name="locations_address_<%=locations_count %>" type="text" id="locations_address_<%=locations_count %>" value="<%= slt.getAddress() %>" size="20" pattern="any_text" title="=location address"></td>
								<td align="center"><input name="locations_contact_<%=locations_count %>" type="text" id="locations_contact_<%=locations_count %>" value="<%= slt.getContact() %>" size="15" pattern="any_text" title="location contact"></td>
								<td align="center"><input name="locations_contact_no_<%=locations_count %>" type="text" id="locations_contact_no_<%=locations_count %>" value="<%= slt.getContactNo() %>" size="10" pattern="tel" title="location contact number"></td>
								<td align="center"><input name="locations_UBD_<%=locations_count %>" type="text" id="locations_UBD_<%=locations_count %>" value="<%= slt.getUBDMapRef() %>" size="8" pattern="any_text" title="UBD ref location"></td>
								<td align="center"><textarea name="locations_notes_<%=locations_count %>" title="location notes" cols="10" id="location_notes_<%=locations_count %>"><% if(slt.getLocationNotes() != null) { out.print(slt.getLocationNotes());} %></textarea></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<%}%>
							</tbody>
                            <tr>
								<td class="left">&nbsp;</td>
							    <td colspan="1"><input name="button300" type="button" onClick="addLocationsRow();" value="Insert Row">
							    <td colspan="5"><input name="button301" type="button" onClick="deleteLocationsRow();" value="Delete Row">
                                  <input name="locations_count" type="hidden" id="locations_count" size="15" value="<%=locations_count %>"></td>
							    <td class="right">&nbsp;</td>
							</tr>
							<tr>
							  <td colspan="8" class="bottom">&nbsp;</td>
						  </tr>
						</table>					
					</td>
				</tr>
				<%-- Shooting Schedule Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellpadding="0" cellspacing="0" id="shooting_schedule">
							<tr>
								<td width="15" class="header-left">&nbsp;</td>
								<td colspan="2" class="header-middle">Shooting Schedule </td>
								<td width="15" class="header-right">&nbsp;</td>
							</tr>
							<% DailyScheduleType dst = cst.getDailySchedule();%>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td width="80" align="left"><strong>Start of Day Notes </strong></td>
								<td align="left"><input name='start_day_notes' type='text' id='start_day_notes' value="<%= dst.getStartDayNotes() %>" size="40" ></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<% int scene_count = 0;
							for(SceneScheduleType sst : dst.getSceneSchedule()) {
								scene_count ++;%>
							<tr>
								<td width="15" align="center" class="left">&nbsp;</td>
								<td colspan="2" align="center" valign="top">
									<table width="670" border="0" cellpadding="0" cellspacing="0" id="scene">
									<tbody>
										<tr><td colspan="8" class="top">&nbsp;</td></tr>
										<tr>
											<td class="left">&nbsp;</td>
											<td><strong>Scene</strong></td>
											<td><strong>Page Time </strong></td>
											<td><strong>D/N</strong></td>
											<td><strong>I/E</strong></td>
											<td><strong>Set/Location</strong></td>
											<td><strong>Synopsis</strong></td>
											<td class="right">&nbsp;</td>
										</tr>
										<tr>
											<td class="left">&nbsp;</td>
											<td><input name="ss<%=scene_count%>_scene" type="text" id="ss<%=scene_count%>_scene" value="<%= sst.getScene() %>" size="6"></td>
											<% PageTimeType ptt1 = sst.getPageTime();%>
											<td>
												<input name="ss<%=scene_count%>_pages" type="text" id="ss<%=scene_count%>_pages" size="4" value="<%=ptt1.getNumber() %>">
                                              	<input name="ss<%=scene_count%>_pagesnum" type="text" id="ss<%=scene_count%>_pagesnum" size="2" value="<%=ptt1.getNumerator() %>">
                                              <strong>/8pgs </strong></td>
											<td><input name="ss<%=scene_count%>_dn" type="text" id="ss<%=scene_count%>_dn" value="<%= sst.getDN() %>" size="6"></td>
											<td><input name="ss<%=scene_count%>_inex" type="text" id="ss<%=scene_count%>_inex" value="<%= sst.getINEX() %>" size="6"></td>
											<td><input name="ss<%=scene_count%>_setlocation" type="text" id="ss<%=scene_count%>_setlocation" value="<%= sst.getSetLocation() %>" size="15"></td>
											<td><input name="ss<%=scene_count%>_synopsis" type="text" id="ss<%=scene_count%>_synopsis" value="<%= sst.getSynopsis() %>" size="15"></td>
											<td class="right">&nbsp;</td>
										</tr>
										<tr>
											<td class="left">&nbsp;</td>
											<td colspan="6">
												<table width="640" border="0" align="center" cellpadding="0" cellspacing="0" id="artist_<%=scene_count%>">
												<tbody>
													<tr>
														<td><strong>Character</strong></td>
														<td><strong>Artist</strong></td>
														<td><strong>Pickup</strong></td>
														<td><strong>Makeup</strong></td>
														<td><strong>Wardrobe</strong></td>
														<td><strong>On Set </strong></td>
													</tr>
													<% int artist_count = 0;
													for(ArtistTimeInfoType atit : sst.getArtistTimeInfo()) {
														artist_count ++;%>
													<tr>
														<td><input name="ss<%=scene_count%>_character_<%=artist_count%>" type="text" id="ss<%=scene_count%>_character_<%=artist_count%>" value="<%=atit.getCharacter()%>" size="15"></td>
														<td><input name="ss<%=scene_count%>_artist_<%=artist_count%>" type="text" id="ss<%=scene_count%>_artist_<%=artist_count%>" value="<%= atit.getArtist() %>" size="15"></td>
														<td><input name="ss<%=scene_count%>_pickup_<%=artist_count%>" type="text" id="ss<%=scene_count%>_pickup_<%=artist_count%>" value="<%if (atit.getPickup() != null) {out.print(atit.getPickup());} %>" size="6"></td>
														<td><input name="ss<%=scene_count%>_makeup_<%=artist_count%>" type="text" id="ss<%=scene_count%>_makeup_<%=artist_count%>" value="<%if (atit.getMakeup() != null) {out.print(atit.getMakeup());} %>" size="6"></td>
														<td><input name="ss<%=scene_count%>_wardrobe_<%=artist_count%>" type="text" id="ss<%=scene_count%>_wardrobe_<%=artist_count%>" value="<%if (atit.getWardrobe() != null) {out.print(atit.getWardrobe());} %>" size="6"></td>
														<td><input name="ss<%=scene_count%>_onset_<%=artist_count%>" type="text" id="ss<%=scene_count%>_onset_<%=artist_count%>" value="<%= atit.getOnSet() %>" size="6"></td>
													</tr>
													<% }%>
												</tbody>
												</table>										  	
											</td>
											<td class="right">&nbsp;</td>
										</tr>
										<tr>
										  <td class="left">&nbsp;</td>
										  <td colspan="6" align="left">
										    <input type="button" name="Submit152" onclick="addArtistDetailsRow(<%= scene_count %>);" value="Add Artist Details">
									      <input name="artist_count_<%= scene_count %>" type="hidden" id="artist_count_<%= scene_count %>" size="15" value="<%= artist_count %>">										  
										  </td>
										  <td class="right">&nbsp;</td>
									  	</tr>
										<tr>
											<td class="left">&nbsp;</td>
											<td colspan="2"><strong>Est Shoot Times </strong></td>
										  	<td colspan="4" align="left"><input name="ss<%=scene_count%>_estshootingtime" type="text" id="ss<%=scene_count%>_estshootingtime" value="<%= sst.getEstShootTimes() %>" size="15"></td>
											<td class="right">&nbsp;</td>
										</tr>
										<tr>
											<td class="left">&nbsp;</td>
											<td colspan="2">
											<input type="button" name="ss<%=scene_count%>_mealbutton" id="ss<%=scene_count%>_mealbutton" value="Add Meal Break" onClick="addMealBreakRow(<%= scene_count %>);" <% if (sst.getMealBreak() != null) { out.print("disabled"); }%> >											
											</td>
										  	<td colspan="4" align="left">
											<table width="400" border="0" cellpadding="0" cellspacing="0" id="mealbreak_<%=scene_count%>">
											<tbody>
                                            <% if (sst.getMealBreak() != null) {
											MealBreakType mbt = sst.getMealBreak(); %>
                                            <tr>
                                              <td><strong>Meal</strong></td>
                                              <td><input name="ss<%=scene_count%>_meal" type="text" id="ss<%=scene_count%>_meal" value="<%= mbt.getMeal()%>"></td>
                                              <td>&nbsp;</td>
                                              <td><strong>Times</strong></td>
                                              <td><input name="ss<%=scene_count%>_times" type="text" id="ss<%=scene_count%>_times" value="<%= mbt.getBreak()%>"></td>
                                            </tr>
											<%}%>
											</tbody>
                                          	</table>											
										</td>
											<td class="right">&nbsp;</td>
										</tr>
										<tr><td colspan="8" class="bottom">&nbsp;</td></tr>
								</tbody>
							    </table>								</td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<% }%>
							<tr>
								<td width="15" align="center" class="left">&nbsp;</td>
								<td width="80" align="left" valign="top"><strong>End of Day Notes </strong></td>
								<td align="left" valign="top"><input name='end_day_notes' type='text' id='end_day_notes' value="<%= dst.getEndDayNotes() %>" size="40"></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<%PageTimeType ptt2 = dst.getTotalScriptPages();%>
							<tr>
							  <td class="left">&nbsp;</td>
							  <td><strong>Total Script Pages </strong></td>
							  <td><input name="total_script_pages" type="text" id="total_script_pages" size="4" value="<%=ptt2.getNumber() %>">
                                <input name="total_script_pagesnum" type="text" id="total_script_pagesnum" size="2" value="<%=ptt2.getNumerator() %>">
                                <strong>/8pgs
                                <input name="button22" type="button" onClick="calculateMod();" value="Calculate"/>
                              </strong></td>
							  <td class="right">&nbsp;</td>
						  </tr>
							<tr><td class="left">&nbsp;</td>
							  <td colspan="2"><input type="button" name="Submit15" value="Insert Scene" onClick="addSceneRow(<%= scene_count %>)">
                                <input name="scene_count" type="hidden" id="scene_count" size="15" value="<%= scene_count %>"></td>
							  <td class="right">&nbsp;</td>
							</tr>
							<tr>
							  <td colspan="4" class="bottom">&nbsp;</td>
						  </tr>
					  </table>					
				  </td>
				</tr>
				<tr><td>&nbsp;</td></tr>
				<%-- Set Requirements Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellpadding="0" cellspacing="0" id="set_requirements">
							<tbody>
							<tr>
								<td width="15" class="header-left">&nbsp;</td>
								<td colspan="2" class="header-middle">Set Requirements </td>
								<td width="15" class="header-right">&nbsp;</td>
							</tr>
							<tr>
								<td class="left">&nbsp;</td>
								<td align="center">&nbsp;</td>
								<td align="center">&nbsp;</td>
								<td class="right">&nbsp;</td>
							</tr>
							<% DailySetRequirementsType dsrt = cst.getDailySetRequirements();
							int requirements_count = 0;
							for (SingleEntryType set : dsrt.getSingleEntry()) {
								requirements_count ++;%>
							<tr>
								<td rowspan="2" class="left">&nbsp;</td>
								<td align="left" valign="top"><strong>Item</strong><br><input name="sr<%= requirements_count %>_item" type="text" id="sr<%= requirements_count %>_item" value="<%= set.getItem() %>"></td>
								<td align="center">
									<table width="510" border="0" cellpadding="0" cellspacing="0" id="description_<%= requirements_count %>">
									<tbody>
										<tr>
											<td><strong>Scene </strong></td>
											<td><strong>Requirements</strong></td>
										</tr>
										<% int description_count = 0;
										for (DescriptionType dt : set.getDescription()){ 
											description_count ++;%>
										<tr valign="top">
											<td><input name="sr<%= requirements_count %>_scene_<%= description_count %>" type="text" id="sr<%= requirements_count %>_scene_<%= description_count %>" value="<%= dt.getScene() %>"></td>
											<td><textarea name="sr<%= requirements_count %>_requirements_<%= description_count %>" cols="40" id="sr<%= requirements_count %>_requirements_<%= description_count %>"><%= dt.getRequirements() %></textarea></td>
										</tr>
										<% }%>
									</tbody>
									</table>								
								</td>
								<td rowspan="2" class="right">&nbsp;</td>
							</tr>
							<tr>
								<td align="center">&nbsp;</td>
								<td align="left">
								<input name="button500" type="button" onClick="addDescriptionRow(<%= requirements_count %>);" value="Insert Description">
								<input name="button501" type="button" onClick="deleteDescriptionRow(<%= requirements_count %>);" value="Delete Description">
								<input name="description_count_<%= requirements_count %>" type="hidden" id="description_count_<%= requirements_count %>" size="15" value="<%= description_count %>"></td>
							</tr>
							<% }%>
							</tbody>
							<tr>
								<td class="left">&nbsp;								</td>
							    <td colspan="2"><input name="button4" type="button" onClick="addRequirementsRow();" value="Insert Row">
                                  <input name="requirements_count" type="hidden" id="requirements_count" size="15" value="<%= requirements_count %>"></td>
							    <td class="right">&nbsp;</td>
							</tr>
							<tr>
							  <td colspan="4" class="bottom">&nbsp;</td>
						  </tr>
					  </table>					
				  </td>
				</tr>
				<%-- Unit Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellspacing="0" cellpadding="0" id="unit">
							<tr>
								<td class="header-left">&nbsp;</td>
								<td class="header-middle">Unit </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr align="center">
								<td width="15" height="90" class="left">&nbsp;</td>
								<td height="90" align="center" valign="middle"><textarea name="unit" cols="80" id="unit"><% if(cst.getUnit() != null) { out.print(cst.getUnit());} %></textarea></td>
								<td width="15" height="90" valign="top" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
						</table>					
					</td>
				</tr>
				<%-- Additional Equipment Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellspacing="0" cellpadding="0" id="additional_equipment">
							<tr>
								<td class="header-left">&nbsp;</td>
								<td class="header-middle">Additional Equipment </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr valign="middle">
								<td width="15" height="90" class="left">&nbsp;</td>
								<td height="90" align="center"><textarea name="additional_equipment" cols="80" id="additional_equipment"><% if(cst.getAdditionalEquipment() != null) { out.print(cst.getAdditionalEquipment());} %></textarea></td>
								<td width="15" height="90" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
						</table>					
					</td>
				</tr>
				<%-- Additional Crew Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellspacing="0" cellpadding="0" id="additional_crew">
							<tr>
								<td class="header-left">&nbsp;</td>
								<td class="header-middle">Additional Crew </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr valign="middle">
								<td width="15" height="90" class="left">&nbsp;</td>
								<td height="90" align="center"><textarea name="additional_crew" cols="80" id="additional_crew"><% if(cst.getAdditionalCrew() != null) { out.print(cst.getAdditionalCrew());} %></textarea></td>
								<td width="15" height="90" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
						</table>					
					</td>
				</tr>
				<%-- Directions Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellspacing="0" cellpadding="0" id="directions">
							<tr>
								<td class="header-left">&nbsp;</td>
								<td class="header-middle">Directions </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr valign="middle">
								<td width="15" height="90" class="left">&nbsp;</td>
								<td height="90" align="center"><textarea name="directions" cols="80" id="directions"><% if(cst.getDirections() != null) { out.print(cst.getDirections());} %></textarea></td>
								<td width="15" height="90" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
						</table>					
					</td>
				</tr>
				<%-- Parking Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellspacing="0" cellpadding="0" id="parking">
							<tr>
								<td class="header-left">&nbsp;</td>
								<td class="header-middle">Parking</td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr valign="middle">
								<td width="15" height="90" class="left">&nbsp;</td>
								<td height="90" align="center"><textarea name="parking" cols="80" id="parking"><% if(cst.getParking() != null) { out.print(cst.getParking());} %></textarea></td>
								<td width="15" height="90" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
						</table>					
					</td>
				</tr>
				<%-- Catering Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellpadding="0" cellspacing="0" id="catering">
							<tbody>
							<tr>
								<td class="header-left">&nbsp;</td>
								<td colspan="4" class="header-middle">Catering </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td align="center"><strong>Meal</strong></td>
								<td align="center"><strong>Time</strong></td>
								<td align="center"><strong>Numbers</strong></td>
								<td align="center"><strong>Location</strong></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<% int catering_count = 0;
							if (cst.getCatering() != null) {
								CateringType cat = cst.getCatering();
								for (SingleMealType smt : cat.getSingleMeal()) {
									catering_count ++;
							%>
							<tr>
								<td width="15" height="30" class="left">&nbsp;</td>
								<td height="30" align="center" valign="top"><select name="catering_meal_<%=catering_count %>" id="catering_meal_<%=catering_count %>">
								  <option value="Breakfast" <%if(smt.getMeal().equals("Breakfast")){out.print("selected"); }%>>Breakfast</option>
								  <option value="Morning Tea" <%if(smt.getMeal().equals("Morning Tea")){out.print("selected"); }%>>Morning Tea</option>
								  <option value="Lunch" <%if(smt.getMeal().equals("Lunch")){out.print("selected"); }%>>Lunch</option>
								  <option value="Afternoon Tea" <%if(smt.getMeal().equals("Afternoon Tea")){out.print("selected"); }%>>Afternoon Tea</option>
								  <option value="Dinner" <%if(smt.getMeal().equals("Dinner")){out.print("selected"); }%>>Dinner</option>
								  <option value="Supper" <%if(smt.getMeal().equals("Supper")){out.print("selected"); }%>>Supper</option>
							      </select>
							    </td>
								<td height="30" align="center" valign="top"><input name="catering_time_<%=catering_count %>" type="text" id="catering_time_<%=catering_count %>" value="<%= smt.getBreak() %>" size="8"></td>
								<td height="30" align="center" valign="top"><input name="catering_numbers_<%=catering_count %>" type="text" id="catering_numbers_<%=catering_count %>" value="<%= smt.getServeNo() %>" size="8"></td>
								<td height="30" align="center" valign="top"><input name="catering_location_<%=catering_count %>" type="text" id="catering_location_<%=catering_count %>" value="<%= smt.getLocation() %>" size="50"></td>
								<td width="15" height="30" class="right">&nbsp;</td>
							</tr>
								<% } 
							}%>
							</tbody>
							<tr>
								<td class="left">&nbsp;								</td>
							    <td colspan="4"><input name="button17" type="button" onClick="addCateringRow();" value="Insert Row">
                                  <input name="catering_count" type="hidden" id="catering_count" size="15" value="<%= catering_count %>"></td>
							    <td class="right">&nbsp;</td>
							</tr>
							<tr>
							  <td colspan="6" class="bottom">&nbsp;</td>
						  </tr>
						</table>					</td>
				</tr>
				<%-- Production Notes Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellspacing="0" cellpadding="0" id="production_notes">
							<tr>
								<td class="header-left">&nbsp;</td>
								<td class="header-middle">Production Notes </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" height="100" class="left">&nbsp;</td>
								<td height="90" align="center" valign="middle"><textarea name="production_notes" cols="80" id="production_notes"><% if(cst.getProductionNotes() != null) { out.print(cst.getProductionNotes());} %></textarea></td>
								<td width="15" height="100" valign="top" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
						</table>					</td>
				</tr>
				<%-- Location Crew Notes Table --%>
				<tr>
				  <td><table width="700" border="0" cellspacing="0" cellpadding="0" id="location_crew_notes">
                    <tr>
                      <td class="header-left">&nbsp;</td>
                      <td class="header-middle">Location/Crew Notes </td>
                      <td class="header-right">&nbsp;</td>
                    </tr>
                    <tr>
                      <td width="15" height="100" class="left">&nbsp;</td>
                      <td height="90" align="center" valign="middle"><textarea name="location_crew_notes" cols="80" id="location_crew_notes"><% if(cst.getLocationCrewNotes() != null) { out.print(cst.getLocationCrewNotes());} %>
                  </textarea></td>
                      <td width="15" height="100" valign="top" class="right">&nbsp;</td>
                    </tr>
                    <tr>
                      <td colspan="3" class="bottom">&nbsp;</td>
                    </tr>
                  </table></td>
			  </tr>
			  	<%-- Lunch Pickup Table --%>
				<tr>
				  <td><table width="700" border="0" cellspacing="0" cellpadding="0" id="lunch_pickup">
                    <tr>
                      <td class="header-left">&nbsp;</td>
                      <td class="header-middle">Lunch Pickup  </td>
                      <td class="header-right">&nbsp;</td>
                    </tr>
                    <tr>
                      <td width="15" height="100" class="left">&nbsp;</td>
                      <td height="90" align="center" valign="middle"><textarea name="lunch_pickup" cols="80" id="lunch_pickup"><% if(cst.getLunchPickup() != null) { out.print(cst.getLunchPickup());} %>
                  </textarea></td>
                      <td width="15" height="100" valign="top" class="right">&nbsp;</td>
                    </tr>
                    <tr>
                      <td colspan="3" class="bottom">&nbsp;</td>
                    </tr>
                  </table></td>
			  </tr>
			  	<%-- Exposed Rushes Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellspacing="0" cellpadding="0" id="exposed_rushes">
							<tr>
								<td class="header-left">&nbsp;</td>
								<td class="header-middle">Exposed Rushes </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr valign="middle">
								<td width="15" height="90" class="left">&nbsp;</td>
								<td height="90" align="center"><textarea name="exposed_rushes" cols="80" id="exposed_rushes"><% if(cst.getExposedRushes() != null) { out.print(cst.getExposedRushes());} %></textarea></td>
								<td width="15" height="90" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
						</table>					</td>
				</tr>
				<%-- Rushes Screening Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellspacing="0" cellpadding="0" id="rushes_screening">
							<tr>
								<td class="header-left">&nbsp;</td>
								<td class="header-middle">Rushes Screening </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr valign="middle">
								<td width="15" height="90" class="left">&nbsp;</td>
								<td height="90" align="center"><textarea name="rushes_screening" cols="80" id="rushes_screening"><% if(cst.getRushesScreening() != null) { out.print(cst.getRushesScreening());} %></textarea></td>
								<td width="15" height="90" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
						</table>					</td>
				</tr>
				<%-- Crew Agreements Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellpadding="0" cellspacing="0" id="crew_agreements">
							<tr>
								<td class="header-left">&nbsp;</td>
								<td class="header-middle">Crew Agreements </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr valign="middle">
								<td width="15" height="90" class="left">&nbsp;</td>
								<td height="90" align="center"><textarea name="crew_agreements" cols="80" id="crew_agreements"><% if(cst.getCrewAgreements() != null) { out.print(cst.getCrewAgreements());} %></textarea></td>
								<td width="15" height="90" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
						</table>					</td>
				</tr>
				<%-- Advanced Schedule Table --%>
				<% AdvancedScheduleType ast = cst.getAdvancedSchedule(); %>
				<tr>
					<td>
						<table width="700" border="0" cellpadding="0" cellspacing="0" id="advanced_schedule">
							<tbody>
							<tr>
								<td class="header-left">&nbsp;</td>
								<td colspan="6" class="header-middle">Advanced Schedule</td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td width="65" align="right"><strong>Day No.</strong></td>
								<td width="61" align="center"><input name="ad_schedule_day_no" type="text" id="ad_schedule_day_no" value="<% if(cst.getAdvancedSchedule() != null) {out.print(ast.getShootDayNo());} %>" size="4"></td>
								<td width="104" align="right"><strong>Weekday</strong></td>
								<td width="194" align="center"><input name="ad_schedule_weekday" type="text" id="ad_schedule_weekday" value="<% if(cst.getAdvancedSchedule() != null) {out.print(ast.getShootDayWeekday());} %>"></td>
								<td width="66" align="right"><strong>Date</strong></td>
								<td width="180" align="center"><input name="ad_schedule_date" type="text" id="ad_schedule_date" value="<% if(cst.getAdvancedSchedule() != null) {out.print(ast.getShootDayDate());} %>"></td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							<tr>
								<td class="left">&nbsp;</td>
								<td align="center" valign="top">&nbsp;</td>
								<td align="center" valign="top">&nbsp;</td>
								<td align="center" valign="top">&nbsp;</td>
								<td align="center" valign="top">&nbsp;</td>
								<td align="center" valign="top">&nbsp;</td>
								<td align="center" valign="top">&nbsp;</td>
								<td class="right">&nbsp;</td>
							</tr>
							<tr>
								<td class="left">&nbsp;</td>
								<td colspan="6" align="center" valign="top">
									<table width="670" border="0" cellpadding="0" cellspacing="0" id="advanced_scenes">
									<tbody>
										<tr>
											<td><strong>Scene # </strong></td>
											<td><strong>D/N</strong></td>
											<td><strong>I/E</strong></td>
											<td><strong>Pages</strong></td>
											<td><strong>Set/Synopsis</strong></td>
											<td><strong>Location</strong></td>
											<td><strong>Characters</strong></td>
										</tr>
										<% int ad_scene_count = 0;
										if(cst.getAdvancedSchedule() != null) {
										for(AdSceneScheduleType asst : ast.getAdSceneSchedule()) { 
											ad_scene_count ++;%>
										<tr valign="top">
											<td><input name="ad_schedule_scene_<%= ad_scene_count %>" type="text" id="ad_schedule_scene_<%= ad_scene_count %>" size="4" value="<%= asst.getScene() %>"></td>
											<td><input name="ad_schedule_dn_<%= ad_scene_count %>" type="text" id="ad_schedule_dn_<%= ad_scene_count %>" size="4" value="<%= asst.getDN() %>"> </td>
											<td><input name="ad_schedule_inex_<%= ad_scene_count %>" type="text" id="ad_schedule_inex_<%= ad_scene_count %>" size="4" value="<%= asst.getINEX() %>"></td>
											<% PageTimeType ptt3 = asst.getPageTime();%>
											<td><input name="ad_schedule_pages_<%= ad_scene_count %>" type="text" id="ad_schedule_pages_<%= ad_scene_count %>" size="4" value="<%= ptt3.getNumber() %>"> <input name="ad_schedule_pagesnum_<%= ad_scene_count %>" type="text" id="ad_schedule_pagesnum_<%= ad_scene_count %>" size="2" value="<%= ptt3.getNumerator() %>">
											  <strong>/8pgs</strong></td>
											<td><input name="ad_schedule_setsynopsis_<%= ad_scene_count %>" type="text" id="ad_schedule_setsynopsis_<%= ad_scene_count %>" value="<%= asst.getSetSynopsis() %>" size="15"></td>
											<td><textarea name="ad_schedule_location_<%= ad_scene_count %>" id="ad_schedule_location_<%= ad_scene_count %>" cols="10"><%= asst.getLocation() %></textarea></td>
											<td><textarea name="ad_schedule_characters_<%= ad_scene_count %>" id="ad_schedule_characters_<%= ad_scene_count %>" cols="10"><%= asst.getCharacters() %></textarea></td>
										</tr>
										<% }
										}%>
									</tbody>
									</table>								
								</td>
								<td class="right">&nbsp;</td>
							</tr>
							<tr>
								<td width="15" class="left">&nbsp;</td>
								<td colspan="6" align="left" valign="top">
								  <input name="button42" type="button" onClick="addAdvancedSceneRow();" value="Insert Row">
                                  <input name="ad_scene_count" type="hidden" id="ad_scene_count" size="15" value="<%= ad_scene_count %>">                                </td>
								<td width="15" class="right">&nbsp;</td>
							</tr>
							</tbody>
							<tr>
								<td colspan="8" class="bottom">&nbsp;</td>
							</tr>
						</table>					
					</td>
				</tr>
				<%-- Additional Notes Table --%>
				<tr>
					<td>
						<table width="700" border="0" cellpadding="0" cellspacing="0" id="additional_notes">
							<tr>
								<td class="header-left">&nbsp;</td>
								<td class="header-middle">Additional Notes </td>
								<td class="header-right">&nbsp;</td>
							</tr>
							<tr valign="middle">
								<td width="15" height="90" class="left">&nbsp;</td>
								<td height="90" align="center"><textarea name="additional_notes" cols="80" id="additional_notes"><%= cst.getAdditionalNotes() %></textarea></td>
								<td width="15" height="90" class="right">&nbsp;</td>
							</tr>
							<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
						</table>					
					</td>
				</tr>
				<tr>
			  <td align="center">Final Submission 
		      <input name="final_submission" type="checkbox" id="final_submission" value="True" <% if(ucst.isFinalSubmission() == true) {out.print("checked");}%>></td>
		  </tr>
			</table>
			<p align="center">
			<input type="button" value="Print"  onclick="window.print()">
	  		<input type="submit" name="Save" value="Save" onclick="return validateFields('form1');">
	  		<input type="submit" name="Submission" value="Submission">
			<input type="hidden" name="workItemID" id="workItemID">
			<input type="hidden" name="userID" id="userID">
			<input type="hidden" name="sessionHandle" id="sessionHandle">
			<input type="hidden" name="JSESSIONID" id="JSESSIONID">
			<input type="hidden" name="submit" id="submit">
			</p>
		  </form>
		  <!-- LOAD -->
    <form method="post" action="Update_Call_Sheet.jsp?formType=load&workItemID=<%= request.getParameter("workItemID") %>&userID=<%= request.getParameter("userID") %>&sessionHandle=<%= request.getParameter("sessionHandle") %>&JSESSIONID=<%= request.getParameter("JSESSIONID") %>&submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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
	  </td>
  <td width="14" class="background_right">&nbsp;</td></tr>
	<tr><td colspan="3" class="background_bottom">&nbsp;</td></tr>
</table>
<%
if(request.getParameter("Submission") != null){
	//general information
	git.setProduction(request.getParameter("production"));
	git.setDate(XMLGregorianCalendarImpl.parse(request.getParameter("date")));
	git.setWeekday(request.getParameter("weekday"));
	git.setShootDayNo(new BigInteger (request.getParameter("shoot_day")));
	
	CallSheetType cs = new CallSheetType();
	
	//emergency information
	EmergencyInfoType ei = new EmergencyInfoType();
	ei.setFireAmbulance(request.getParameter("fire_ambulance"));
	ei.setHospital(request.getParameter("hospital"));
	ei.setPolice(request.getParameter("police"));
	cs.setEmergencyInfo(ei);
	
	//essential crew/contact
	cs.setDirector(request.getParameter("director"));
	cs.setProducer(request.getParameter("producer"));
	cs.setFirstAD(request.getParameter("first_ad"));
	String temp_pm = request.getParameter("production_manager");
	String pm_array[] = temp_pm.split("/");
	for(int i=0; i <= pm_array.length - 1; i++) {
		cs.getProductionManager().add(pm_array[i]);
	}
	
	//weather
	cs.setSunrise(request.getParameter("sunrise"));
	cs.setSunset(request.getParameter("sunset"));
	cs.setWeather(request.getParameter("forecast"));
	//call times
	CallTimesType ctt1 = new CallTimesType();
	CallType c1 = new CallType();
	c1.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("crew_call_time")));
	if(!(request.getParameter("crew_call_location").equals(""))){
		c1.setCallLoc(request.getParameter("crew_call_location"));
	}
	ctt1.setCrewCall(c1);
	
	CallType c2 = new CallType();
	c2.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("location_call_time")));
	if(!(request.getParameter("location_call_location").equals(""))){
		c2.setCallLoc(request.getParameter("location_call_location"));
	}
	ctt1.setLocationCall(c2);
	
	CallType c3 = new CallType();
	c3.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("makeup_call_time")));
	if(!(request.getParameter("makeup_call_location").equals(""))){
		c3.setCallLoc(request.getParameter("makeup_call_location"));
	}
	ctt1.setMakeupHair(c3);
	
	CallType c4 = new CallType();
	c4.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("wardrobe_call_time")));
	if(!(request.getParameter("wardrobe_call_location").equals(""))){
		c4.setCallLoc(request.getParameter("wardrobe_call_location"));
	}
	ctt1.setWardrobe(c4);
	
	CallType c5 = new CallType();
	c5.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("unit_call_time")));
	if(!(request.getParameter("unit_call_location").equals(""))){
		c5.setCallLoc(request.getParameter("unit_call_location"));
	}
	ctt1.setUnit(c5);
	int final_call_count = Integer.parseInt(request.getParameter("call_count"));
	for(int current_call = 1; current_call<=final_call_count; current_call ++) {
		OtherCallType oc = new OtherCallType();
		oc.setCall(request.getParameter("call_" + current_call));
		oc.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("call_time_" + current_call)));
		if(request.getParameter("call_location_" + current_call) != null){
			oc.setCallLoc(request.getParameter("call_location_" + current_call));
		}
		ctt1.getOther().add(oc);
	}
	BreakfastType b = new BreakfastType();
	b.setFrom(XMLGregorianCalendarImpl.parse(request.getParameter("breakfast_from")));
	b.setTo(XMLGregorianCalendarImpl.parse(request.getParameter("breakfast_to")));
	if(!(request.getParameter("breakfast_location").equals(""))) {
		b.setCallLoc(request.getParameter("breakfast_location"));
	}
	ctt1.setBreakfast(b);
	cs.setCallTimes(ctt1);
	//wrap times
	WrapTimesType wt = new WrapTimesType();
	wt.setEstWrap(request.getParameter("estimated_wrap"));
	if (request.getParameter("wrap_1") != null) {
		int final_others_count = Integer.parseInt(request.getParameter("others_count"));
		for(int current_others = 1; current_others<=final_others_count; current_others ++) {
			OtherWrapType ow = new OtherWrapType();
			ow.setWrap(request.getParameter("wrap_" + current_others));
			ow.setWrapTime(request.getParameter("wrap_time_" + current_others));
			wt.getOther().add(ow);
		}
	}
	cs.setWrapTimes(wt);
	//locations
	LocationType lt1 = new LocationType();
	int final_location_count = Integer.parseInt(request.getParameter("locations_count"));
	for(int current_location = 1; current_location<=final_location_count; current_location ++) {
		SingleLocationType slt1 = new SingleLocationType();
		slt1.setLocationName(request.getParameter("locations_name_" + current_location));
		slt1.setAddress(request.getParameter("locations_address_" + current_location));
		slt1.setContact(request.getParameter("locations_contact_" + current_location));
		slt1.setContactNo(request.getParameter("locations_contact_no_" + current_location));
		slt1.setUBDMapRef(request.getParameter("locations_UBD_" + current_location));
		slt1.setLocationNotes(request.getParameter("locations_notes_" + current_location));
		lt1.getSingleLocation().add(slt1);
	}
	cs.setLocation(lt1);
	//shooting schedule
	DailyScheduleType dst1 = new DailyScheduleType();
	dst1.setStartDayNotes(request.getParameter("start_day_notes"));
	int final_scene_count = Integer.parseInt(request.getParameter("scene_count"));
	for(int current_scene = 1; current_scene<=final_scene_count; current_scene ++) {
		SceneScheduleType sst1 = new SceneScheduleType();
		sst1.setScene(request.getParameter("ss" + current_scene + "_scene"));
		PageTimeType pt2 = new PageTimeType();
		pt2.setNumber(new BigInteger(request.getParameter("ss" + current_scene + "_pages")));
		pt2.setNumerator(new BigInteger(request.getParameter("ss" + current_scene + "_pagesnum")));
		sst1.setPageTime(pt2);
		sst1.setDN(request.getParameter("ss" + current_scene + "_dn"));
		sst1.setINEX(request.getParameter("ss" + current_scene + "_inex"));
		sst1.setSetLocation(request.getParameter("ss" + current_scene + "_setlocation"));
		sst1.setSynopsis(request.getParameter("ss" + current_scene + "_synopsis"));
		
		int final_artist_count = Integer.parseInt(request.getParameter("artist_count_"+ current_scene));
		for(int current_artist = 1; current_artist<=final_artist_count; current_artist ++) {
			ArtistTimeInfoType atit1 = new ArtistTimeInfoType();
			atit1.setCharacter(request.getParameter("ss" + current_scene + "_character_" + current_artist));
			atit1.setArtist(request.getParameter("ss" + current_scene + "_artist_" + current_artist));
			if(!(request.getParameter("ss" + current_scene + "_pickup_" + current_artist).equals(""))) {
				atit1.setPickup(request.getParameter("ss" + current_scene + "_pickup_" + current_artist));
			}
			if(!(request.getParameter("ss" + current_scene + "_makeup_" + current_artist).equals(""))){
				atit1.setMakeup(request.getParameter("ss" + current_scene + "_makeup_" + current_artist));
			}
			if(!(request.getParameter("ss" + current_scene + "_wardrobe_" + current_artist).equals(""))) {
				atit1.setWardrobe(request.getParameter("ss" + current_scene + "_wardrobe_" + current_artist));
			}
			atit1.setOnSet(request.getParameter("ss" + current_scene + "_onset_" + current_artist));
			sst1.getArtistTimeInfo().add(atit1);
		}
		
		sst1.setEstShootTimes(request.getParameter("ss" + current_scene + "_estshootingtime"));
		
		if(request.getParameter("ss" + current_scene + "_times")!= null) {
			MealBreakType mbt1 = new MealBreakType();
			mbt1.setMeal(request.getParameter("ss" + current_scene + "_meal"));
			mbt1.setBreak(request.getParameter("ss" + current_scene + "_times"));
			sst1.setMealBreak(mbt1);
		}
		dst1.getSceneSchedule().add(sst1);
	}
	dst1.setEndDayNotes(request.getParameter("end_day_notes"));
	PageTimeType pt1 = new PageTimeType();
	pt1.setNumber(new BigInteger(request.getParameter("total_script_pages")));
	pt1.setNumerator(new BigInteger(request.getParameter("total_script_pagesnum")));
	dst1.setTotalScriptPages(pt1);
	cs.setDailySchedule(dst1);
	//set requirements
	DailySetRequirementsType dsrt1 = new DailySetRequirementsType();
	int final_requirements_count = Integer.parseInt(request.getParameter("requirements_count"));
	for(int current_requirements = 1; current_requirements<=final_requirements_count; current_requirements ++) {
		SingleEntryType set1 = new SingleEntryType();
		set1.setItem(request.getParameter("sr" + current_requirements + "_item"));
		int final_description_count = Integer.parseInt(request.getParameter("description_count_" + current_requirements));
		for(int current_description = 1; current_description<=final_description_count; current_description ++) {
			DescriptionType dt1 = new DescriptionType();
			dt1.setScene(request.getParameter("sr" + current_requirements + "_scene_" + current_description));
			dt1.setRequirements(request.getParameter("sr" + current_requirements + "_requirements_" + current_description));
			set1.getDescription().add(dt1);
		}
		dsrt1.getSingleEntry().add(set1);
	}
	cs.setDailySetRequirements(dsrt1);
	//unit
	if (!(request.getParameter("unit").equals(""))) {
		cs.setUnit(request.getParameter("unit"));
	}
	//additional equipment
	if (!(request.getParameter("additional_equipment").equals(""))) {
		cs.setAdditionalEquipment(request.getParameter("additional_equipment"));
	}
	//additional crew
	if (!(request.getParameter("additional_crew").equals(""))) {
		cs.setAdditionalCrew(request.getParameter("additional_crew"));
	}
	//directions
	if (!(request.getParameter("directions").equals(""))) {
		cs.setDirections(request.getParameter("directions"));
	}
	//parking
	if (!(request.getParameter("parking").equals(""))) {
		cs.setParking(request.getParameter("parking"));
	}
	//catering
	CateringType cat1 =  new CateringType();
	int final_catering_count = Integer.parseInt(request.getParameter("catering_count"));
	for(int current_catering = 1; current_catering<=final_catering_count; current_catering ++) {
		SingleMealType smt1 = new SingleMealType();
		smt1.setMeal(request.getParameter("catering_meal_"+ current_catering));
		smt1.setBreak(request.getParameter("catering_time_"+ current_catering));
		smt1.setServeNo(request.getParameter("catering_numbers_"+ current_catering));
		smt1.setLocation(request.getParameter("catering_location_"+ current_catering));
		cat1.getSingleMeal().add(smt1);
	}
	cs.setCatering(cat1);
	//production notes
	if (!(request.getParameter("production_notes").equals(""))) {
		cs.setProductionNotes(request.getParameter("production_notes"));
	}
	//location crew notes
	if (!(request.getParameter("location_crew_notes").equals(""))) {
		cs.setLocationCrewNotes(request.getParameter("location_crew_notes"));
	}
	//lunch pickup
	if (!(request.getParameter("lunch_pickup").equals(""))) {
		cs.setLunchPickup(request.getParameter("lunch_pickup"));
	}
	//exposed rushes
	if (!(request.getParameter("exposed_rushes").equals(""))) {
		cs.setExposedRushes(request.getParameter("exposed_rushes"));
	}
	//rushes screening
	if (!(request.getParameter("rushes_screening").equals(""))) {
		cs.setRushesScreening(request.getParameter("rushes_screening"));
	}
	//crew agreements
	if (!(request.getParameter("crew_agreements").equals(""))) {
		cs.setCrewAgreements(request.getParameter("crew_agreements"));
	}
	//advanced schedule
	if(!(request.getParameter("ad_schedule_day_no").equals(""))) {
		AdvancedScheduleType ast1 = new AdvancedScheduleType();
		ast1.setShootDayNo(new BigInteger(request.getParameter("ad_schedule_day_no")));
		ast1.setShootDayDate(XMLGregorianCalendarImpl.parse(request.getParameter("ad_schedule_date")));
		ast1.setShootDayWeekday(request.getParameter("ad_schedule_weekday"));
		int final_adscene_count = Integer.parseInt(request.getParameter("ad_scene_count"));
		for(int current_adscene = 1; current_adscene<=final_adscene_count; current_adscene ++) {
			AdSceneScheduleType asst1 = new AdSceneScheduleType();
			asst1.setScene(request.getParameter("ad_schedule_scene_" + current_adscene));
			PageTimeType pt3 = new PageTimeType();
			pt3.setNumber(new BigInteger(request.getParameter("ad_schedule_pages_" + current_adscene)));
			pt3.setNumerator(new BigInteger(request.getParameter("ad_schedule_pagesnum_" + current_adscene)));
			asst1.setPageTime(pt3);
			asst1.setDN(request.getParameter("ad_schedule_dn_" + current_adscene));
			asst1.setINEX(request.getParameter("ad_schedule_inex_" + current_adscene));
			asst1.setSetSynopsis(request.getParameter("ad_schedule_setsynopsis_" + current_adscene));
			asst1.setLocation(request.getParameter("ad_schedule_location_" + current_adscene));
			asst1.setCharacters(request.getParameter("ad_schedule_characters_" + current_adscene));
			ast1.getAdSceneSchedule().add(asst1);
		}
		cs.setAdvancedSchedule(ast1);
	}
	//additional notes
	cs.setAdditionalNotes(request.getParameter("additional_notes"));
	//compile call sheet
	ucst.setCallSheet(cs);
	
	
	if (request.getParameter("final_submission")==null){
		ucst.setFinalSubmission(false);
	}else{
		ucst.setFinalSubmission(true);
	}
	

	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    File f = new File("./backup/CallSheet_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
    m.marshal(ucstElement,  f);//output to file
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(ucstElement, xmlOS);//out to ByteArray
	String result = xmlOS.toString().replaceAll("ns2:", "");
    
    String workItemID = new String(request.getParameter("workItemID"));
    String sessionHandle = new String(request.getParameter("sessionHandle"));
    String userID = new String(request.getParameter("userID"));
    String submit = new String(request.getParameter("submit"));
  
    session.setAttribute("inputData", result);//to be possibly replaced
    response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit));
    return;

}else if(request.getParameter("Save") != null){
	//general information
	git.setProduction(request.getParameter("production"));
	git.setDate(XMLGregorianCalendarImpl.parse(request.getParameter("date")));
	git.setWeekday(request.getParameter("weekday"));
	git.setShootDayNo(new BigInteger (request.getParameter("shoot_day")));
	
	CallSheetType cs = new CallSheetType();
	
	//emergency information
	EmergencyInfoType ei = new EmergencyInfoType();
	ei.setFireAmbulance(request.getParameter("fire_ambulance"));
	ei.setHospital(request.getParameter("hospital"));
	ei.setPolice(request.getParameter("police"));
	cs.setEmergencyInfo(ei);
	
	//essential crew/contact
	cs.setDirector(request.getParameter("director"));
	cs.setProducer(request.getParameter("producer"));
	cs.setFirstAD(request.getParameter("first_ad"));
	String temp_pm = request.getParameter("production_manager");
	String pm_array[] = temp_pm.split("/");
	for(int i=0; i <= pm_array.length - 1; i++) {
		cs.getProductionManager().add(pm_array[i]);
	}
	
	//weather
	cs.setSunrise(request.getParameter("sunrise"));
	cs.setSunset(request.getParameter("sunset"));
	cs.setWeather(request.getParameter("forecast"));
	//call times
	CallTimesType ctt1 = new CallTimesType();
	CallType c1 = new CallType();
	c1.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("crew_call_time")));
	if(!(request.getParameter("crew_call_location").equals(""))){
		c1.setCallLoc(request.getParameter("crew_call_location"));
	}
	ctt1.setCrewCall(c1);
	
	CallType c2 = new CallType();
	c2.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("location_call_time")));
	if(!(request.getParameter("location_call_location").equals(""))){
		c2.setCallLoc(request.getParameter("location_call_location"));
	}
	ctt1.setLocationCall(c2);
	
	CallType c3 = new CallType();
	c3.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("makeup_call_time")));
	if(!(request.getParameter("makeup_call_location").equals(""))){
		c3.setCallLoc(request.getParameter("makeup_call_location"));
	}
	ctt1.setMakeupHair(c3);
	
	CallType c4 = new CallType();
	c4.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("wardrobe_call_time")));
	if(!(request.getParameter("wardrobe_call_location").equals(""))){
		c4.setCallLoc(request.getParameter("wardrobe_call_location"));
	}
	ctt1.setWardrobe(c4);
	
	CallType c5 = new CallType();
	c5.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("unit_call_time")));
	if(!(request.getParameter("unit_call_location").equals(""))){
		c5.setCallLoc(request.getParameter("unit_call_location"));
	}
	ctt1.setUnit(c5);
	int final_call_count = Integer.parseInt(request.getParameter("call_count"));
	for(int current_call = 1; current_call<=final_call_count; current_call ++) {
		OtherCallType oc = new OtherCallType();
		oc.setCall(request.getParameter("call_" + current_call));
		oc.setCallTime(XMLGregorianCalendarImpl.parse(request.getParameter("call_time_" + current_call)));
		if(request.getParameter("call_location_" + current_call) != null){
			oc.setCallLoc(request.getParameter("call_location_" + current_call));
		}
		ctt1.getOther().add(oc);
	}
	BreakfastType b = new BreakfastType();
	b.setFrom(XMLGregorianCalendarImpl.parse(request.getParameter("breakfast_from")));
	b.setTo(XMLGregorianCalendarImpl.parse(request.getParameter("breakfast_to")));
	if(!(request.getParameter("breakfast_location").equals(""))) {
		b.setCallLoc(request.getParameter("breakfast_location"));
	}
	ctt1.setBreakfast(b);
	cs.setCallTimes(ctt1);
	//wrap times
	WrapTimesType wt = new WrapTimesType();
	wt.setEstWrap(request.getParameter("estimated_wrap"));
	if (request.getParameter("wrap_1") != null) {
		int final_others_count = Integer.parseInt(request.getParameter("others_count"));
		for(int current_others = 1; current_others<=final_others_count; current_others ++) {
			OtherWrapType ow = new OtherWrapType();
			ow.setWrap(request.getParameter("wrap_" + current_others));
			ow.setWrapTime(request.getParameter("wrap_time_" + current_others));
			wt.getOther().add(ow);
		}
	}
	cs.setWrapTimes(wt);
	//locations
	LocationType lt1 = new LocationType();
	int final_location_count = Integer.parseInt(request.getParameter("locations_count"));
	for(int current_location = 1; current_location<=final_location_count; current_location ++) {
		SingleLocationType slt1 = new SingleLocationType();
		slt1.setLocationName(request.getParameter("locations_name_" + current_location));
		slt1.setAddress(request.getParameter("locations_address_" + current_location));
		slt1.setContact(request.getParameter("locations_contact_" + current_location));
		slt1.setContactNo(request.getParameter("locations_contact_no_" + current_location));
		slt1.setUBDMapRef(request.getParameter("locations_UBD_" + current_location));
		slt1.setLocationNotes(request.getParameter("locations_notes_" + current_location));
		lt1.getSingleLocation().add(slt1);
	}
	cs.setLocation(lt1);
	//shooting schedule
	DailyScheduleType dst1 = new DailyScheduleType();
	dst1.setStartDayNotes(request.getParameter("start_day_notes"));
	int final_scene_count = Integer.parseInt(request.getParameter("scene_count"));
	for(int current_scene = 1; current_scene<=final_scene_count; current_scene ++) {
		SceneScheduleType sst1 = new SceneScheduleType();
		sst1.setScene(request.getParameter("ss" + current_scene + "_scene"));
		PageTimeType pt2 = new PageTimeType();
		pt2.setNumber(new BigInteger(request.getParameter("ss" + current_scene + "_pages")));
		pt2.setNumerator(new BigInteger(request.getParameter("ss" + current_scene + "_pagesnum")));
		sst1.setPageTime(pt2);
		sst1.setDN(request.getParameter("ss" + current_scene + "_dn"));
		sst1.setINEX(request.getParameter("ss" + current_scene + "_inex"));
		sst1.setSetLocation(request.getParameter("ss" + current_scene + "_setlocation"));
		sst1.setSynopsis(request.getParameter("ss" + current_scene + "_synopsis"));
		
		int final_artist_count = Integer.parseInt(request.getParameter("artist_count_"+ current_scene));
		for(int current_artist = 1; current_artist<=final_artist_count; current_artist ++) {
			ArtistTimeInfoType atit1 = new ArtistTimeInfoType();
			atit1.setCharacter(request.getParameter("ss" + current_scene + "_character_" + current_artist));
			atit1.setArtist(request.getParameter("ss" + current_scene + "_artist_" + current_artist));
			if(!(request.getParameter("ss" + current_scene + "_pickup_" + current_artist).equals(""))) {
				atit1.setPickup(request.getParameter("ss" + current_scene + "_pickup_" + current_artist));
			}
			if(!(request.getParameter("ss" + current_scene + "_makeup_" + current_artist).equals(""))){
				atit1.setMakeup(request.getParameter("ss" + current_scene + "_makeup_" + current_artist));
			}
			if(!(request.getParameter("ss" + current_scene + "_wardrobe_" + current_artist).equals(""))) {
				atit1.setWardrobe(request.getParameter("ss" + current_scene + "_wardrobe_" + current_artist));
			}
			atit1.setOnSet(request.getParameter("ss" + current_scene + "_onset_" + current_artist));
			sst1.getArtistTimeInfo().add(atit1);
		}
		
		sst1.setEstShootTimes(request.getParameter("ss" + current_scene + "_estshootingtime"));
		
		if(request.getParameter("ss" + current_scene + "_times")!= null) {
			MealBreakType mbt1 = new MealBreakType();
			mbt1.setMeal(request.getParameter("ss" + current_scene + "_meal"));
			mbt1.setBreak(request.getParameter("ss" + current_scene + "_times"));
			sst1.setMealBreak(mbt1);
		}
		dst1.getSceneSchedule().add(sst1);
	}
	dst1.setEndDayNotes(request.getParameter("end_day_notes"));
	PageTimeType pt1 = new PageTimeType();
	pt1.setNumber(new BigInteger(request.getParameter("total_script_pages")));
	pt1.setNumerator(new BigInteger(request.getParameter("total_script_pagesnum")));
	dst1.setTotalScriptPages(pt1);
	cs.setDailySchedule(dst1);
	//set requirements
	DailySetRequirementsType dsrt1 = new DailySetRequirementsType();
	int final_requirements_count = Integer.parseInt(request.getParameter("requirements_count"));
	for(int current_requirements = 1; current_requirements<=final_requirements_count; current_requirements ++) {
		SingleEntryType set1 = new SingleEntryType();
		set1.setItem(request.getParameter("sr" + current_requirements + "_item"));
		int final_description_count = Integer.parseInt(request.getParameter("description_count_" + current_requirements));
		for(int current_description = 1; current_description<=final_description_count; current_description ++) {
			DescriptionType dt1 = new DescriptionType();
			dt1.setScene(request.getParameter("sr" + current_requirements + "_scene_" + current_description));
			dt1.setRequirements(request.getParameter("sr" + current_requirements + "_requirements_" + current_description));
			set1.getDescription().add(dt1);
		}
		dsrt1.getSingleEntry().add(set1);
	}
	cs.setDailySetRequirements(dsrt1);
	//unit
	if (!(request.getParameter("unit").equals(""))) {
		cs.setUnit(request.getParameter("unit"));
	}
	//additional equipment
	if (!(request.getParameter("additional_equipment").equals(""))) {
		cs.setAdditionalEquipment(request.getParameter("additional_equipment"));
	}
	//additional crew
	if (!(request.getParameter("additional_crew").equals(""))) {
		cs.setAdditionalCrew(request.getParameter("additional_crew"));
	}
	//directions
	if (!(request.getParameter("directions").equals(""))) {
		cs.setDirections(request.getParameter("directions"));
	}
	//parking
	if (!(request.getParameter("parking").equals(""))) {
		cs.setParking(request.getParameter("parking"));
	}
	//catering
	CateringType cat1 =  new CateringType();
	int final_catering_count = Integer.parseInt(request.getParameter("catering_count"));
	for(int current_catering = 1; current_catering<=final_catering_count; current_catering ++) {
		SingleMealType smt1 = new SingleMealType();
		smt1.setMeal(request.getParameter("catering_meal_"+ current_catering));
		smt1.setBreak(request.getParameter("catering_time_"+ current_catering));
		smt1.setServeNo(request.getParameter("catering_numbers_"+ current_catering));
		smt1.setLocation(request.getParameter("catering_location_"+ current_catering));
		cat1.getSingleMeal().add(smt1);
	}
	cs.setCatering(cat1);
	//production notes
	if (!(request.getParameter("production_notes").equals(""))) {
		cs.setProductionNotes(request.getParameter("production_notes"));
	}
	//location crew notes
	if (!(request.getParameter("location_crew_notes").equals(""))) {
		cs.setLocationCrewNotes(request.getParameter("location_crew_notes"));
	}
	//lunch pickup
	if (!(request.getParameter("lunch_pickup").equals(""))) {
		cs.setLunchPickup(request.getParameter("lunch_pickup"));
	}
	//exposed rushes
	if (!(request.getParameter("exposed_rushes").equals(""))) {
		cs.setExposedRushes(request.getParameter("exposed_rushes"));
	}
	//rushes screening
	if (!(request.getParameter("rushes_screening").equals(""))) {
		cs.setRushesScreening(request.getParameter("rushes_screening"));
	}
	//crew agreements
	if (!(request.getParameter("crew_agreements").equals(""))) {
		cs.setCrewAgreements(request.getParameter("crew_agreements"));
	}
	//advanced schedule
	if(!(request.getParameter("ad_schedule_day_no").equals(""))) {
		AdvancedScheduleType ast1 = new AdvancedScheduleType();
		ast1.setShootDayNo(new BigInteger(request.getParameter("ad_schedule_day_no")));
		ast1.setShootDayDate(XMLGregorianCalendarImpl.parse(request.getParameter("ad_schedule_date")));
		ast1.setShootDayWeekday(request.getParameter("ad_schedule_weekday"));
		int final_adscene_count = Integer.parseInt(request.getParameter("ad_scene_count"));
		for(int current_adscene = 1; current_adscene<=final_adscene_count; current_adscene ++) {
			AdSceneScheduleType asst1 = new AdSceneScheduleType();
			asst1.setScene(request.getParameter("ad_schedule_scene_" + current_adscene));
			PageTimeType pt3 = new PageTimeType();
			pt3.setNumber(new BigInteger(request.getParameter("ad_schedule_pages_" + current_adscene)));
			pt3.setNumerator(new BigInteger(request.getParameter("ad_schedule_pagesnum_" + current_adscene)));
			asst1.setPageTime(pt3);
			asst1.setDN(request.getParameter("ad_schedule_dn_" + current_adscene));
			asst1.setINEX(request.getParameter("ad_schedule_inex_" + current_adscene));
			asst1.setSetSynopsis(request.getParameter("ad_schedule_setsynopsis_" + current_adscene));
			asst1.setLocation(request.getParameter("ad_schedule_location_" + current_adscene));
			asst1.setCharacters(request.getParameter("ad_schedule_characters_" + current_adscene));
			ast1.getAdSceneSchedule().add(asst1);
		}
		cs.setAdvancedSchedule(ast1);
	}
	//additional notes
	cs.setAdditionalNotes(request.getParameter("additional_notes"));
	//compile call sheet
	ucst.setCallSheet(cs);
	
	
	if (request.getParameter("final_submission")==null){
		ucst.setFinalSubmission(false);
	}else{
		ucst.setFinalSubmission(true);
	}


	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(ucstElement, xmlOS);//out to ByteArray

    response.setHeader("Content-Disposition", "attachment;filename=\"CallSheet_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
    response.setHeader("Content-Type", "text/xml");

    ServletOutputStream outs = response.getOutputStream();
    xmlOS.writeTo(outs);
    outs.close();
}
%>
</body>
</html>