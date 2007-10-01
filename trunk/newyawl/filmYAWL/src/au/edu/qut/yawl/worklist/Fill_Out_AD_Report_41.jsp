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
<%@ page import="org.yawlfoundation.sb.timesheetinfo.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page buffer="1024kb" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>2nd Assistant Director Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<!-- style sheet imports -->
<link href="graphics/style.css" rel="stylesheet" type="text/css" />
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js" ></script>
<script type="text/javascript" src="scripts/filloutADReport41.js" ></script>
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
            int endOfFile = result.indexOf("</ns2:Fill_Out_AD_Report>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(beginOfFile, endOfFile + 25);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		//String xml = request.getParameter("outputData");
		xml = (String)session.getAttribute("outputData");
        xml = xml.replaceAll("<Fill_Out_AD_Report", "<ns2:Fill_Out_AD_Report xmlns:ns2='http://www.yawlfoundation.org/sb/timeSheetInfo'");
		xml = xml.replaceAll("</Fill_Out_AD_Report","</ns2:Fill_Out_AD_Report");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.timesheetinfo");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement foadrElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
	FillOutADReportType foadr = (FillOutADReportType)foadrElement.getValue();
				
	GeneralInfoType gi = foadr.getGeneralInfo();
	TimeSheetInfoType tsi = foadr.getTimeSheetInfo();
%>

<table width="700" height="100%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td height="14" colspan="3" class="background_top">&nbsp;</td></tr>
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td>
		<h1 align="center">2nd Assistant Director's Report </h1>      
		<form name="form1" method="post" onSubmit="calculateDuration();">			
			
		<table width="700" border="0" align="center">

			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0">
					<tr><td align="right" valign="top" class="header-left">&nbsp;</td>
				      <td colspan="6" valign="top" class="header-middle">General Info</td>
				      <td align="right" valign="top" class="header-right">&nbsp;</td>
				  </tr>
					<tr>
						<td class="left" width="14">&nbsp;</td>
						<td><strong>Production</strong></td><td><input name="production" type="text" id="production" value="<%=gi.getProduction()%>" size="15" readonly></td>
						<td><strong>Date</strong></td><td><input name="date" type="text" id="date" value="<%=gi.getDate().getDay()+"-"+gi.getDate().getMonth()+"-"+gi.getDate().getYear()%>" size="15" readonly></td>
						<td><strong>Day</strong></td><td><input name="weekday" type="text" id="weekday" value="<%=gi.getWeekday()%>" size="15" readonly></td>
						<td class="right" width="14">&nbsp;</td>
					</tr>
					<tr>
						<td class="left" width="14">&nbsp;</td>
						<td><strong>Producer</strong></td><td><input name="producer" type="text" id="producer" value="<%=foadr.getProducer()%>" size="15" readonly></td>
						<td><strong>Director</strong></td><td><input name="director" type="text" id="director" value="<%=foadr.getDirector()%>" size="15" readonly></td>
						<td><strong>Shoot Day </strong></td><td><input name="shoot_day" type="text" id="shoot_day" value="<%=gi.getShootDayNo()%>" size="15" readonly></td>
						<td class="right" width="14">&nbsp;</td>
					</tr>
					<tr height="30">
						<td class="left" width="14">&nbsp;</td>
						<td><strong>Assistant Director</strong></td>
						<td><input name="assistant_director" type="text" id="assistant_director" value="<%=foadr.getAssistantDirector()%>" size="15" readonly></td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td class="right" width="14">&nbsp;</td>
					</tr>
					<tr height="30"><td colspan="8" class="bottom">&nbsp;</td></tr>
			  </table>
			</td></tr>
			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0" id="artist">
					<tbody>
					  <tr align="center" valign="top"><td align="right" valign="top" class="header-left">&nbsp;</td>
					    <td colspan="8" align="left" valign="top" class="header-middle">Artist Timesheet </td>
					    <td align="right" valign="top" class="header-right">&nbsp;</td>
					  </tr>
						<tr align="center" valign="top">
							<td width="14" height="27" rowspan="2" align="center" class="left">&nbsp;</td>
							<td rowspan="2" align="center"><strong>Artist</strong></td>
							<td rowspan="2" align="center"><strong>P/U</strong></td>
							<td colspan="2" align="center"><strong>MU/WD/Call</strong></td>
							<td rowspan="2" align="center"><strong>Meal </strong></td>
							<td rowspan="2" align="center"><strong> Wrap</strong></td>
							<td rowspan="2" align="center"><strong>Travel</strong></td>
							<td rowspan="2" align="center"><strong>Signature</strong></td>
							<td width="15" rowspan="2" class="right">&nbsp;</td>
						</tr>
						<tr valign="top">
						  	<td align="center"><strong>scheduled</strong></td>
						  	<td align="center"><strong>actual</strong></td>
				  		</tr>
						
						<%  int a=0;
						ArtistTimeSheetType atsA = tsi.getArtistTimeSheet();
						for(SingleArtistType satA : atsA.getSingleArtist()) {
							a++;
						%>
						<tr align="center">
							<td width="14" align="center" class="left" height="30">&nbsp;</td>
							<td align="center"><input name="<% out.print("artist_" + a); %>" type="text" id="<% out.print("artist_" + a); %>" size="15" value="<%= satA.getArtist()%>" title="Enter Artist Name. [String Value] (make sure this matches one of the artists on cast list)"></td>
							<td align="center"><input name="<% out.print("artist_pu_" + a); %>" type="text" id="<% out.print("artist_pu_" + a); %>" size="6" value="<%= satA.getPU()%>" title="Enter Pickup. [String Value]"></td>
							<td align="center"><input name="<% out.print("artist_muwdcall_scheduled_" + a); %>" type="text" id="<% out.print("artist_muwdcall_scheduled_" + a); %>" size="6" value="<%= satA.getMUWDCallScheduled()%>" title="Enter Scheduled Time. [Time Value HH:MM:SS]"></td>
							<td align="center"><input name="<% out.print("artist_muwdcall_actual_" + a); %>" type="text" id="<% out.print("artist_muwdcall_actual_" + a); %>" size="6" value="<%= satA.getMUWDCallActualArrival()%>" title="Enter Actual Time. [Time Value HH:MM:SS]"></td>
							<td align="center"><input name="<% out.print("artist_meal_" + a); %>" type="text" id="<% out.print("artist_meal_" + a); %>" size="6" value="<%= satA.getMealBreak()%>" title="Enter Meal Break. [Time Value HH:MM:SS] (please enter 00:00:00 if no meal is served)"></td>
							<td align="center"><input name="<% out.print("artist_wrap_" + a); %>" type="text" id="<% out.print("artist_wrap_" + a); %>" size="6" value="<%= satA.getTimeWrap()%>" title="Enter Time Wrap. [Time Value HH:MM:SS]"></td>
							<td align="center"><input name="<% out.print("artist_travel_" + a); %>" type="text" id="<% out.print("artist_travel_" + a); %>" size="6" value="<%= satA.getTravel()%>" title="Enter Travel. [Time Value HH:MM:SS]"></td>
                            <td align="center">
                                <applet code="signature.SignA.class" archive="SignA.jar" width="350" height="60" name="SignA" id="SignA" MAYSCRIPT>
                                    <param name="load_url" value="<%=satA.getSignatureUrl()%>">
                                    <param name="save_url" value="c:/">
                                    <param name="propertyRoot" value="artist" />
                                    <param name="index" value="<%=a%>" />                                    
                                </applet>
                                <input type="hidden" name="<% out.print("artist_signature_" + a); %>" id="<% out.print("artist_signature_" + a); %>" value="<%=satA.getSignatureUrl()%>" />
                            </td>                            
                            <td width="15" class="right">&nbsp;</td>
						</tr> 
						<% }%>
					</tbody>
				    <tr valign="top">
				      <td class="left">&nbsp;</td>
			          <td colspan="1"><input name="button102" type="button" onClick="addArtistRow();" value="Insert Row"></td>
			          <td colspan="1" align="left"><input name="button103" type="button" onClick="deleteArtistRow();" value="Delete Row"></td>
			          <td colspan="6">&nbsp;</td>
			          <td class="right">&nbsp;</td>
			      </tr>
			      <tr valign="top"><td colspan="10" class="bottom">&nbsp;</td></tr>
			  </table>
	  		</td></tr>
			<tr><td><table width="700" border="0" cellpadding="0" cellspacing="0" id="background_artist">
              <tbody>
                <tr align="center" valign="top">
                  <td align="right" valign="top" class="header-left">&nbsp;</td>
                  <td colspan="8" align="left" valign="top" class="header-middle">Background Actors  Timesheet </td>
                  <td align="right" valign="top" class="header-right">&nbsp;</td>
                </tr>
                <tr align="center" valign="top">
                  <td width="14" height="27" rowspan="2" align="center" class="left">&nbsp;</td>
                  <td rowspan="2" align="center"><strong>Artist</strong></td>
                  <td rowspan="2" align="center"><strong>P/U</strong></td>
                  <td colspan="2" align="center"><strong>MU/WD/Call</strong></td>
                  <td rowspan="2" align="center"><strong>Meal </strong></td>
                  <td rowspan="2" align="center"><strong> Wrap</strong></td>
                  <td rowspan="2" align="center"><strong>Travel</strong></td>
                  <td rowspan="2" align="center"><strong>Signature</strong></td>
                  <td width="15" rowspan="2" class="right">&nbsp;</td>
                </tr>
                <tr valign="top" height="30">
                  <td align="center"><strong>scheduled</strong></td>
                  <td align="center"><strong>actual</strong></td>
                </tr>
				
				<%  int b=0;
						if(tsi != null){
							ArtistTimeSheetType atsE = tsi.getExtrasTimeSheet();
							for(SingleArtistType satE : atsE.getSingleArtist()) {
								b++;
						%>
						<tr align="center" height="30">
							<td width="14" align="center" class="left">&nbsp;</td>
							<td align="center"><input name="<% out.print("backgroundartist_" + b); %>" type="text" id="<% out.print("backgroundartist_" + b); %>" size="15" value="<%= satE.getArtist()%>" ></td>
							<td align="center"><input name="<% out.print("backgroundartist_pu_" + b); %>" type="text" id="<% out.print("backgroundartist_pu_" + b); %>" size="6" value="<%= satE.getPU()%>" title="Enter Pickup. [String Value]"></td>
							<td align="center"><input name="<% out.print("backgroundartist_muwdcall_scheduled_" + b); %>" type="text" id="<% out.print("backgroundartist_muwdcall_scheduled_" + b); %>" size="6" value="<%= satE.getMUWDCallScheduled()%>" title="Enter Scheduled Time. [Time Value HH:MM:SS]"></td>
							<td align="center"><input name="<% out.print("backgroundartist_muwdcall_actual_" + b); %>" type="text" id="<% out.print("backgroundartist_muwdcall_actual_" + b); %>" size="6" value="<%= satE.getMUWDCallActualArrival()%>" title="Enter Actual Time. [Time Value HH:MM:SS]"></td>
							<td align="center"><input name="<% out.print("backgroundartist_meal_" + b); %>" type="text" id="<% out.print("backgroundartist_meal_" + b); %>" size="6" value="<%= satE.getMealBreak()%>" title="Enter Meal Break. [Time Value HH:MM:SS] (please enter 00:00:00 if no meal is served)"></td>
							<td align="center"><input name="<% out.print("backgroundartist_wrap_" + b); %>" type="text" id="<% out.print("backgroundartist_wrap_" + b); %>" size="6" value="<%= satE.getTimeWrap()%>" title="Enter Time Wrap. [Time Value HH:MM:SS]"></td>
							<td align="center"><input name="<% out.print("backgroundartist_travel_" + b); %>" type="text" id="<% out.print("backgroundartist_travel_" + b); %>" size="6" value="<%= satE.getTravel()%>" title="Enter Travel. [Time Value HH:MM:SS]"></td>
							<td>
                                <applet code="signature.SignA.class" archive="SignA.jar" width="350" height="60" name="SignA" id="SignA" MAYSCRIPT>
                                    <param name="load_url" value="<%=satE.getSignatureUrl()%>">
                                    <param name="save_url" value="c:/">
                                    <param name="propertyRoot" value="backgroundartist" />
                                    <param name="index" value="<%= b %>" />
                                </applet>
                                <input type="hidden" name="<% out.print("backgroundartist_signature_" + b); %>" id="<% out.print("backgroundartist_signature_" + b); %>" value="" />                           
                            <td width="15" class="right">&nbsp;</td>
						</tr> 
						<% }
						}%>

              </tbody>
              <tr align="center" valign="top">
                <td align="right" class="left">&nbsp;</td>
                <td colspan="1" align="left"><input name="button220" type="button" onClick="addBackgroundArtistRow();" value="Insert Row"></td>
                <td colspan="7" align="left"><input name="button221" type="button" onClick="deleteBackgroundArtistRow();" value="Delete Row"></td>
                <td align="right" class="right">&nbsp;</td>
              </tr>
              <tr align="center" valign="top">
                <td colspan="10" align="right" class="bottom">&nbsp;</td>
              </tr>
            </table>
			</td></tr>
			<tr><td>
				<table width="700" cellpadding="0" cellspacing="0" id="child">
					<tbody>
					  <tr align="center" valign="top"><td class="header-left">&nbsp;</td>
					    <td colspan="8" align="left" class="header-middle">Children Timesheet </td>
					    <td class="header-right">&nbsp;</td>
					  </tr>
						<tr align="center" valign="top">
							<td width="15" rowspan="2" class="left">&nbsp;</td>
							<td rowspan="2"><strong>Children</strong></td>
							<td rowspan="2"><strong>P/U</strong></td>
							<td colspan="2"><strong>MU/WD/Call</strong></td>
							<td rowspan="2"><strong>Meal</strong></td>
							<td rowspan="2"><strong>Wrap</strong></td>
							<td rowspan="2"><strong>Travel</strong></td>
							<td rowspan="2"><strong>Remarks<br>Rest breaks, meals)</strong></td>
							<td width="15" rowspan="2" class="right">&nbsp;</td>
						</tr>
						<tr valign="top">
					  		<td align="center"><strong>scheduled</strong></td>
					  		<td align="center"><strong>actual</strong></td>
				  		</tr> 
						
						<%  int c=0;
						if(tsi != null){
							ChildrenTimeSheetType cts = tsi.getChildrenTimeSheet();
							for(SingleChildrenType sct : cts.getSingleChildren()) {
								c++;
						%>
						<tr align="center" valign="top">
							<td width="14" align="center" class="left" >&nbsp;</td>
							<td align="center"><input name="<% out.print("children_" + c); %>" type="text" id="<% out.print("children_" + c); %>" size="15" value="<%= sct.getChildren()%>" pattern="any_text" title="Enter Child Name. [String Value]"></td>
							<td align="center"><input name="<% out.print("children_pu_" + c); %>" type="text" id="<% out.print("children_pu_" + c); %>" size="6" value="<%= sct.getPU()%>" pattern="any_text" title="Enter Pickup. [String Value]"></td>
							<td align="center"><input name="<% out.print("children_muwdcall_scheduled_" + c); %>" type="text" id="<% out.print("children_muwdcall_scheduled_" + c); %>" size="6" value="<%= sct.getMUWDCallScheduled()%>" pattern="time" title="Enter Scheduled Time. [Time Value HH:MM:SS]"></td>
							<td align="center"><input name="<% out.print("children_muwdcall_actual_" + c); %>" type="text" id="<% out.print("children_muwdcall_actual_" + c); %>" size="6" value="<%= sct.getMUWDCallActualArrival()%>" pattern="time" title="Enter Actual Time. [Time Value HH:MM:SS]"></td>
							<td align="center"><input name="<% out.print("children_meal_" + c); %>" type="text" id="<% out.print("children_meal_" + c); %>" size="6" value="<%= sct.getMealBreak()%>" pattern="time" title="Enter Meal Break. [Time Value HH:MM:SS] (please enter 00:00:00 if no meal is served)"></td>
							<td align="center"><input name="<% out.print("children_wrap_" + c); %>" type="text" id="<% out.print("children_wrap_" + c); %>" size="6" value="<%= sct.getTimeWrap()%>" pattern="time" title="Enter Wrap Time. [Time Value HH:MM:SS]"></td>
							<td align="center"><input name="<% out.print("children_travel_" + c); %>" type="text" id="<% out.print("children_travel_" + c); %>" size="6" value="<%= sct.getTravel()%>" pattern="time" title="Enter Travel Time. [Time Value HH:MM:SS]"></td>
						    <td align="center"><textarea name="<% out.print("children_remarks_" + c); %>" id="<% out.print("children_remarks_" + c); %>" cols="15" title="Enter Remarks. [String Value]"><%= sct.getRemarks()%></textarea></td>
							<td width="15" class="right">&nbsp;</td>
						</tr> 
						<% }
						}%>												
					</tbody>
					<tr align="center" valign="top">
					  <td align="center" class="left">&nbsp;</td>
				      <td colspan="1" align="left"><input name="button300" type="button" onClick="addChildRow();" value="Insert Row"></td>
				      <td colspan="7" align="left"><input name="button301" type="button" onClick="deleteChildRow();" value="Delete Row"></td>
				      <td align="center" class="right">&nbsp;</td>
				  </tr>
					<tr align="center" valign="top"><td colspan="10" class="bottom">&nbsp;</td></tr>
			  </table>
			</td></tr>
			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0" id="crew">
					<tbody>
					  <tr align="center" valign="top"><td class="header-left">&nbsp;</td>
					    <td colspan="10" align="left" class="header-middle">Crew Timesheet </td>
					    <td class="header-right">&nbsp;</td>
					  </tr>
						<tr align="center" valign="top">
							<td width="15" class="left">&nbsp;</td>
				  			<td><strong>Crew</strong></td>
							<td><strong>Call</strong></td>
							<td><strong>Travel In</strong></td>
							<td><strong>Loc Call</strong></td>
							<td><strong>Meal Break</strong></td>
							<td><strong>Wrap</strong></td>
							<td><strong>Wrap Loc</strong></td>
							<td><strong>Depart Loc</strong></td>
							<td><strong>Travel Out</strong></td>
							<td><strong>Remarks</strong></td>
							<td width="15" class="right">&nbsp;</td>
						</tr>
						
						<%  int d=0;
							CrewTimeSheetType crts = tsi.getCrewTimeSheet();
							for(SingleCrewType scrt : crts.getSingleCrew()) {
								d++;
						%>
						<tr>
						  <td align="center" class="left">&nbsp;</td>
						  <td align="center" valign="top">					        
						  <select name="<% out.print("crew_" + d); %>" id="<% out.print("crew_" + d); %>">
						        <option value="2nd AD" <% if(scrt.getCrew().equals("2nd AD")){ out.print("selected"); } %>>2nd AD</option>
						        <option value="Continuity" <% if(scrt.getCrew().equals("Continuity")){ out.print("selected"); } %>>Continuity</option>
						        <option value="Camera" <% if(scrt.getCrew().equals("Camera")){ out.print("selected"); } %>>Camera</option>
						        <option value="Sound" <% if(scrt.getCrew().equals("Sound")){ out.print("selected"); } %>>Sound</option>
						        <option value="Makeup/Hair" <% if(scrt.getCrew().equals("Makeup/Hair")){ out.print("selected"); } %>>Makeup/Hair</option>
						        <option value="Wardrobe" <% if(scrt.getCrew().equals("Wardrobe")){ out.print("selected"); } %>>Wardrobe</option>
						        <option value="Unit" <% if(scrt.getCrew().equals("Unit")){ out.print("selected"); } %>>Unit</option>
						        <option value="Grips" <% if(scrt.getCrew().equals("Grips")){ out.print("selected"); } %>>Grips</option>
						        <option value="Electrics" <% if(scrt.getCrew().equals("Electrics")){ out.print("selected"); } %>>Electrics</option>
						        <option value="Standby Props" <% if(scrt.getCrew().equals("Standby Props")){ out.print("selected"); } %>>Standby Props</option>
						        <option value="Other ..." <% if(!(scrt.getCrew().equals("2nd AD")) && !(scrt.getCrew().equals("Continuity")) && !(scrt.getCrew().equals("Camera")) && !(scrt.getCrew().equals("Sound"))  &&!(scrt.getCrew().equals("Makeup/Hair")) && !(scrt.getCrew().equals("Wardrobe")) && !(scrt.getCrew().equals("Unit")) && !(scrt.getCrew().equals("Grips")) && !(scrt.getCrew().equals("Electrics")) && !(scrt.getCrew().equals("Standby Props"))){ out.print("selected"); } %>>Other ...</option>
				              </select>
                            <br>					      
                          <input name="<% out.print("crew_other_" + d); %>" type="text" id="<% out.print("crew_other_" + d); %>" size="15" value="<% if(!(scrt.getCrew().equals("2nd AD")) && !(scrt.getCrew().equals("Continuity")) && !(scrt.getCrew().equals("Camera")) && !(scrt.getCrew().equals("Sound"))  &&!(scrt.getCrew().equals("Makeup/Hair")) && !(scrt.getCrew().equals("Wardrobe")) && !(scrt.getCrew().equals("Unit")) && !(scrt.getCrew().equals("Grips")) && !(scrt.getCrew().equals("Electrics")) && !(scrt.getCrew().equals("Standby Props"))){ out.print(scrt.getCrew()); }else{out.print("[If other, specify]");} %>"></td>
						  
						  <td align="center" valign="top"><input name="<% out.print("crew_call_" + d); %>" type="text" id="<% out.print("crew_call_" + d); %>" size="5" value="<%=scrt.getCrewCall() %>" pattern="time" title="Enter Call Time. [Time Value HH:MM:SS]"></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_travelin_" + d); %>" type="text" id="<% out.print("crew_travelin_" + d); %>" size="5" value="<%=scrt.getTravelIn() %>" pattern="time" title="Enter Travel In Time. [Time Value HH:MM:SS]"></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_loccall_" + d); %>" type="text" id="<% out.print("crew_loccall_" + d); %>" size="5" value="<%=scrt.getLocationCall() %>" pattern="time" title="Enter Location Call Time. [Time Value HH:MM:SS]"></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_meal_" + d); %>" type="text" id="<% out.print("crew_meal_" + d); %>" size="5" value="<%=scrt.getMealBreak() %>" pattern="time" title="Enter Meal Time. [Time Value HH:MM:SS] (please enter 00:00:00 if no meal is served)"></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_wrap_" + d); %>" type="text" id="<% out.print("crew_wrap_" + d); %>" size="5" value="<%=scrt.getWrap() %>" pattern="time" title="Enter Wrap Time. [Time Value HH:MM:SS]"></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_wraploc_" + d); %>" type="text" id="<% out.print("crew_wraploc_" + d); %>" size="5" value="<%=scrt.getWrapLoc() %>" pattern="time" title="Enter Wrap Location. [Time Value HH:MM:SS]"></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_departloc_" + d); %>" type="text" id="<% out.print("crew_departloc_" + d); %>" size="5" value="<%=scrt.getDepartLoc() %>" pattern="time" title="Enter Depart Location Time. [Time Value HH:MM:SS]"></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_travelout_" + d); %>" type="text" id="<% out.print("crew_travelout_" + d); %>" size="5" value="<%=scrt.getTravelOut() %>" pattern="time" title="Enter Travel Out Time. [Time Value HH:MM:SS]"></td>
						  <td align="center" valign="top"><textarea name="<% out.print("crew_remarks_" + d); %>" cols="8" id="<% out.print("crew_remarks_" + d); %>" title="Enter Remarks. [String Value]"><%=scrt.getRemarks() %></textarea></td>
						  <td align="center" class="right">&nbsp;</td>
					  </tr>
					  <%}%>
					  
			  		</tbody>
			  		<tr align="center" valign="top">
			  		  <td class="left">&nbsp;</td>
			  		  <td colspan="10" align="left">&nbsp;</td>
			  		  <td class="right">&nbsp;</td>
		  		  </tr>
			  		<tr align="center" valign="top">
			  		  <td class="left">&nbsp;</td>
			  		  <td colspan="1" align="left"><input name="button400" type="button" onClick="addCrewRow();" value="Insert Row"></td>
			  		  <td colspan="19" align="left"><input name="button401" type="button" onClick="deleteCrewRow();" value="Delete Row"></td>
			  		  <td class="right">&nbsp;</td>
			  		</tr>
			  		<tr align="center" valign="top">
			  		  <td colspan="12" class="bottom">&nbsp;</td>
		  		  </tr>
			  </table>
			</td></tr>
	
			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0" id="meal">
				<tbody>
					<tr align="center" valign="top"><td align="center" class="header-left">&nbsp;</td>
					  <td colspan="6" align="left" class="header-middle">Meal Info </td>
					  <td align="center" class="header-right">&nbsp;</td>
					</tr>
					<tr align="center" valign="top">
					  	<td width="15" class="left">&nbsp;</td>
					  	<td><strong>Meal</strong></td>
					  	<td colspan="2"><strong>Times</strong></td>
					  	<td><strong>NOs</strong></td>
					  	<td><strong>Location</strong></td>
					  	<td><strong>Remarks</strong></td>
					  	<td width="15" class="right">&nbsp;</td>
					</tr>
					
					<%  int e=0;
						if(tsi != null){
							MealInfoType mi = tsi.getMealInfo();
							for(SingleMealType smt : mi.getSingleMeal()) {
								e++;
						%>
					<tr>
						<td width="15" align="center" class="left">&nbsp;</td>
					  	<td align="center" valign="top"><select name="<% out.print("meal_" + e); %>" id="<% out.print("meal_" + e); %>">                                                                                                                                                                                                                                                                                                                                                         " id="                                                                                                                                                                                                                                                                                                                                                         ">
					  	  <option value="Breakfast" <% if(smt.getMeal().equals("Breakfast")){out.print("selected"); } %>>Breakfast</option>
					  	  <option value="Morning Tea" <% if(smt.getMeal().equals("Morning Tea")){out.print("selected"); } %>>Morning Tea</option>
					  	  <option value="Lunch" <% if(smt.getMeal().equals("Lunch")){out.print("selected"); } %>>Lunch</option>
					  	  <option value="Afternoon Tea" <% if(smt.getMeal().equals("Afternoon Tea")){out.print("selected"); } %>>Afternoon Tea</option>
					  	  <option value="Dinner" <% if(smt.getMeal().equals("Dinner")){out.print("selected"); } %>>Dinner</option>
					  	  <option value="Supper" <% if(smt.getMeal().equals("Supper")){out.print("selected"); } %>>Supper</option>
				  	    </select></td>
						<% FromToType ft = smt.getMealTimes();%>
					  	<td align="center" valign="top"><input name="<% out.print("meal_timefrom_" + e); %>" type="text" id="<% out.print("meal_timefrom_" + e); %>" size="6" value="<%=ft.getFrom() %>" pattern="time" title="Enter Start Time. [Time Value HH:MM:SS]"></td>
					  	<td align="center" valign="top"><input name="<% out.print("meal_timeto_" + e); %>" type="text" id="<% out.print("meal_timeto_" + e); %>" size="6" value="<%=ft.getTo() %>" pattern="time" title="Enter Finish Time. [Time Value HH:MM:SS]"> 
														<input name="<% out.print("meal_duration_" + e); %>" type="hidden" id="<% out.print("meal_duration_" + e); %>" size="6"></td>
					  	<td align="center" valign="top"><input name="<% out.print("meal_numbers_" + e); %>" type="text" id="<% out.print("meal_numbers_" + e); %>" size="6" value="<%=smt.getNumbers() %>" pattern="number" title="Enter Number of People. [Number Value]"></td>
					  	<td align="center" valign="top"><input name="<% out.print("meal_location_" + e); %>" type="text" id="<% out.print("meal_location_" + e); %>"  size="20" value="<%=smt.getLocation() %>" pattern="any_text" title="Enter Meal Location. [String Value]"></td>
					  	<td align="center" valign="top"><textarea name="<% out.print("meal_remarks_" + e); %>" cols="20" id="<% out.print("meal_remarks_" + e); %>" title="Enter Remarks. [String Value]"><%=smt.getRemarks() %></textarea></td>
					  	<td width="15" align="center" class="right"></td>
					</tr>
					<% }
					}else{%>
					<tr>
						<td width="15" align="center" class="left">&nbsp;</td>
					  	<td align="center" valign="top"><select name="meal_1" id="meal_1">                                                                                                                                                                                                                                                                                                                                                         " id="                                                                                                                                                                                                                                                                                                                                                         ">
					  	  <option value="Breakfast">Breakfast</option>
					  	  <option value="Morning Tea">Morning Tea</option>
					  	  <option value="Lunch">Lunch</option>
					  	  <option value="Afternoon Tea">Afternoon Tea</option>
					  	  <option value="Dinner">Dinner</option>
					  	  <option value="Supper">Supper</option>
				  	    </select></td>
					  	<td align="center" valign="top"><input name="meal_timefrom_1" type="text" id="meal_timefrom_1" size="6"></td>
					  	<td align="center" valign="top"><input name="meal_timeto_1" type="text" id="meal_timeto_1" size="6"><input name="meal_duration_1" type="hidden" id="meal_duration_1" size="6"></td>
					  	<td align="center" valign="top"><input name="meal_numbers_1" type="text" id="meal_numbers_1" size="6"></td>
					  	<td align="center" valign="top"><input name="meal_location_1" type="text" id="meal_location_1" size="20"></td>
					  	<td align="center" valign="top"><textarea name="meal_remarks_1" cols="20" id="meal_remarks_1"></textarea></td>
					  	<td width="15" align="center" class="right">&nbsp;</td>
					</tr>
					<%}%>
				</tbody>
					<tr>
					  	<td width="15" align="center" class="left">&nbsp;</td>
					  	<td colspan="1" valign="top"><input name="button500" type="button" onClick="addMealRow();" value="Insert Row"></td>
					  	<td colspan="5" valign="top"><input name="button501" type="button" onClick="deleteMealRow();" value="Delete Row"></td>
					  	<td width="15" align="center" class="right">&nbsp;</td>
					</tr>
					<tr><td colspan="8" align="center" class="bottom">&nbsp;</td>
					</tr>
  			  </table>
			</td></tr>
			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0">
					<tr><td class="header-left">&nbsp;</td>
				      <td class="header-middle">Livestock/Other</td>
				      <td class="header-right">&nbsp;</td>
				  </tr>
					<tr><td width="15" class="left">&nbsp;</td><td align="center"><textarea name="livestock_other" cols="70" id="livestock_other" title="Enter Livestock/Other Information - If Necessary. [String Value]"><% if(tsi.getLivestock() != null){ out.print(tsi.getLivestock()); } %></textarea></td><td width="15" class="right">&nbsp;</td></tr>
					<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
  			  </table>
			</td></tr>
			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0">
					<tr><td class="header-left">&nbsp;</td>
				      <td class="header-middle">Accidents/Delays</td>
				      <td class="header-right">&nbsp;</td>
				  </tr>
					<tr><td width="15" class="left">&nbsp;</td><td align="center"><textarea name="accidents" cols="70" id="accidents" title="Enter Accidents/Delays Information - If Necessary. [String Value]"><% if(tsi.getAccidentsDelays() != null){ out.print(tsi.getAccidentsDelays()); } %></textarea></td><td width="15" class="right">&nbsp;</td></tr>
					<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
  			  </table>
			</td></tr>
			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0">
					<tr><td class="header-left">&nbsp;</td>
				      <td class="header-middle">Major Props / Action Vehicles / Extra Equipment</td>
				      <td class="header-right">&nbsp;</td>
				  </tr>
					<tr><td width="15" class="left">&nbsp;</td><td align="center"><textarea name="major_props" cols="70" id="major_props" title="Enter Major Props / Action Vehicles / Extra Equipment Information - If Necessary. [String Value]"><% if(tsi.getMajorPropsActionVehiclesExtraEquipment() != null){ out.print(tsi.getMajorPropsActionVehiclesExtraEquipment()); } %></textarea></td><td width="15" class="right">&nbsp;</td></tr>
					<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
  			  </table>
			</td></tr>
			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0">
					<tr><td class="header-left">&nbsp;</td>
				      <td class="header-middle">Additional Personnel </td>
				      <td class="header-right">&nbsp;</td>
				  </tr>
					<tr><td width="15" class="left">&nbsp;</td><td height="90" align="center"><textarea name="additional_personnel" cols="70" id="additional_personnel" title="Enter Additional Personnel Information - If Necessary. [String Value]"><% if(tsi.getAdditionalPersonnel() != null){ out.print(tsi.getAdditionalPersonnel()); } %></textarea></td>
					<td width="15" class="right">&nbsp;</td></tr>
					<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
  			  </table>
			</td></tr>
			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0">
					<tr><td class="header-left">&nbsp;</td>
					  <td class="header-middle">General Comments<span class="style1"> (cast, delays, injuries, gear failure, late arrivals, etc)</span> </td>
					  <td class="header-right">&nbsp;</td>
					</tr>
					<tr><td width="15" height="90" class="left">&nbsp;</td><td height="90" align="center"><textarea name="general_comments" cols="70" id="general_comments" title="Enter General Comments - If Necessary. [String Value]"><% if(tsi.getGeneralComments() != null){ out.print(tsi.getGeneralComments()); } %></textarea></td><td width="15" height="90" class="right">&nbsp;</td></tr>
					<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
  			  </table>
			</td></tr>
		    <tr>
		      <td align="center">
	          	Partial Submission <input name="final_submission" id="radio" type="radio" value="partial" <% if(foadr.isFinalSubmission() == false) {out.print("checked");}%>>
				Final Submission <input name="final_submission" id="radio" type="radio" value="final" <% if(foadr.isFinalSubmission() == true) {out.print("checked");}%>></td>
	      </tr>
	      <tr><td align="center">	
		<input type="hidden" name="artist_count" id="artist_count" value="<%if (a==0) {out.print("0");}else{out.print(a);}%>">
		<input type="hidden" name="extras_count" id="extras_count" value="<%if (b==0) {out.print("0");}else{out.print(b);}%>">
		<input type="hidden" name="child_count" id="child_count" value="<%if (c==0) {out.print("0");}else{out.print(c);}%>">
		<input type="hidden" name="crew_count" id="crew_count" value="<%if (d==0) {out.print("0");}else{out.print(d);}%>">
		<input type="hidden" name="meal_count" id="meal_count" value="<%if (e==0) {out.print("0");}else{out.print(e);}%>">
		<input type="hidden" name="workItemID" id="workItemID">
        <input type="hidden" name="JSESSIONID" id="JSESSIONID">              
        <input type="hidden" name="userID" id="userID">
		<input type="hidden" name="sessionHandle" id="sessionHandle">
		<input type="hidden" name="specID" id="specID">
		<input type="hidden" name="submit" id="submit">
		<input type="button" value="Print"  onclick="window.print()">
		<input type="submit" name="Save" value="Save" onclick="return validateWithSignatories('form1');">
		<input type="submit" name="Submission" value="Submission" onclick="return validateWithSignatories('form1');">
        </td>
		</tr>	
	</table>
	</form>	
	</td>
	<td width="14" class="background_right">&nbsp;</td>
  </tr>
  <tr><td height="14" colspan="3" class="background_bottom">
  <!-- LOAD -->
    <form method="post" action="Fill_Out_AD_Report_41.jsp?formType=load&workItemID=<%= request.getParameter("workItemID") %>&userID=<%= request.getParameter("userID") %>&sessionHandle=<%= request.getParameter("sessionHandle") %>&JSESSIONID=<%= request.getParameter("JSESSIONID") %>&submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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
<!-- END LOAD --></td></tr>
</table>
	
<%
if(request.getParameter("Submission") != null){

	int artist_count = Integer.parseInt(request.getParameter("artist_count"));
	int extras_count = Integer.parseInt(request.getParameter("extras_count"));
	int child_count = Integer.parseInt(request.getParameter("child_count"));
	int crew_count = Integer.parseInt(request.getParameter("crew_count"));
	int meal_count = Integer.parseInt(request.getParameter("meal_count"));
	
	ArtistTimeSheetType atA = new ArtistTimeSheetType();
	for(int ck1=1; ck1<=artist_count; ck1++){//getting the artist information
		SingleArtistType saA = new SingleArtistType();
		saA.setArtist(request.getParameter("artist_"+ ck1));
		saA.setPU(request.getParameter("artist_pu_"+ ck1));
        saA.setSignatureUrl(request.getParameter("artist_signature_" + ck1));
        saA.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_scheduled_"+ ck1)));
		saA.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_actual_"+ ck1)));
		saA.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("artist_meal_"+ ck1)));
		saA.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("artist_wrap_"+ ck1)));
		saA.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("artist_travel_"+ ck1)));
		atA.getSingleArtist().add(saA);
	}
		
    ArtistTimeSheetType atE = new ArtistTimeSheetType();
    for(int ck1=1; ck1<=extras_count; ck1++){//getting the bgartist information
        SingleArtistType saE = new SingleArtistType();
        saE.setArtist(request.getParameter("backgroundartist_"+ ck1));
        saE.setPU(request.getParameter("backgroundartist_pu_"+ ck1));
        saE.setSignatureUrl(request.getParameter("backgroundartist_signature_" + ck1));
        saE.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("backgroundartist_muwdcall_scheduled_"+ ck1)));
        saE.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("backgroundartist_muwdcall_actual_"+ ck1)));
        saE.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("backgroundartist_meal_"+ ck1)));
        saE.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("backgroundartist_wrap_"+ ck1)));
        saE.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("backgroundartist_travel_"+ ck1)));
        atE.getSingleArtist().add(saE);
    }

    ChildrenTimeSheetType chts = new ChildrenTimeSheetType();
	for(int ck2=1; ck2<=child_count; ck2++){//getting the children information
		SingleChildrenType sch = new SingleChildrenType();
		sch.setChildren(request.getParameter("children_"+ ck2));
		sch.setPU(request.getParameter("children_pu_"+ ck2));
		sch.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("children_muwdcall_scheduled_"+ ck2)));
		sch.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("children_muwdcall_actual_"+ ck2)));
		sch.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("children_meal_"+ ck2)));
		sch.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("children_wrap_"+ ck2)));
		sch.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("children_travel_"+ ck2)));
		sch.setRemarks(request.getParameter("children_remarks_"+ ck2));
		chts.getSingleChildren().add(sch);
	}
	
	CrewTimeSheetType cwts = new CrewTimeSheetType();
	for(int ck3=1; ck3<=crew_count; ck3++){//getting the crew information
		SingleCrewType scw = new SingleCrewType();
		
		if(request.getParameter("crew_" + ck3).equals("Other ...")) {
			scw.setCrew(request.getParameter("crew_other_" + ck3));
		} else {
			scw.setCrew(request.getParameter("crew_" + ck3));
		}
		scw.setCrewCall(XMLGregorianCalendarImpl.parse(request.getParameter("crew_call_"+ ck3)));
		scw.setTravelIn(XMLGregorianCalendarImpl.parse(request.getParameter("crew_travelin_"+ ck3)));
		scw.setLocationCall(XMLGregorianCalendarImpl.parse(request.getParameter("crew_loccall_"+ ck3)));
		scw.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("crew_meal_"+ ck3)));
		scw.setWrap(XMLGregorianCalendarImpl.parse(request.getParameter("crew_wrap_"+ ck3)));
		scw.setWrapLoc(XMLGregorianCalendarImpl.parse(request.getParameter("crew_wraploc_"+ ck3)));
		scw.setDepartLoc(XMLGregorianCalendarImpl.parse(request.getParameter("crew_departloc_"+ ck3)));
		scw.setTravelOut(XMLGregorianCalendarImpl.parse(request.getParameter("crew_travelout_"+ ck3)));
		scw.setRemarks(request.getParameter("crew_remarks_"+ ck3));
		cwts.getSingleCrew().add(scw);
	}
	
	MealInfoType mit = new MealInfoType();
	for(int ck4=1; ck4<=meal_count; ck4++){//getting the meal information
		SingleMealType sm = new SingleMealType();
		sm.setMeal(request.getParameter("meal_"+ ck4));
		//times
		FromToType ftt = new FromToType();
		ftt.setFrom(XMLGregorianCalendarImpl.parse(request.getParameter("meal_timefrom_" + ck4)));
		ftt.setTo(XMLGregorianCalendarImpl.parse(request.getParameter("meal_timeto_" + ck4)));
		sm.setMealTimes(ftt);
		sm.setDuration(XMLGregorianCalendarImpl.parse(request.getParameter("meal_duration_" + ck4)));
		sm.setNumbers(new BigInteger(request.getParameter("meal_numbers_"+ ck4)));
		sm.setLocation(request.getParameter("meal_location_"+ ck4));
		sm.setRemarks(request.getParameter("meal_remarks_"+ ck4));
		mit.getSingleMeal().add(sm);
	}
	
	TimeSheetInfoType tsit = new TimeSheetInfoType();
	tsit.setArtistTimeSheet(atA);
	tsit.setExtrasTimeSheet(atE);
	tsit.setChildrenTimeSheet(chts);
	tsit.setCrewTimeSheet(cwts);
	tsit.setMealInfo(mit);
	tsit.setLivestock(request.getParameter("livestock_other"));
	tsit.setAccidentsDelays(request.getParameter("accidents"));
	tsit.setMajorPropsActionVehiclesExtraEquipment(request.getParameter("major_props"));
	tsit.setAdditionalPersonnel(request.getParameter("additional_personnel"));
	tsit.setGeneralComments(request.getParameter("general_comments"));
	
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
	
	
	foadr.setProducer(request.getParameter("producer"));
	foadr.setDirector(request.getParameter("director"));
	foadr.setAssistantDirector(request.getParameter("assistant_director"));
	foadr.setTimeSheetInfo(tsit);
	
	
	if (request.getParameter("final_submission").equals("partial")){
		foadr.setFinalSubmission(false);
	}else if (request.getParameter("final_submission").equals("final")){
		foadr.setFinalSubmission(true);
	}
	
	
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	File f = new File("./backup/ADReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
	m.marshal( foadrElement,  f);//output to file
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(foadrElement, xmlOS);//out to ByteArray
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

	int artist_count = Integer.parseInt(request.getParameter("artist_count"));
	int extras_count = Integer.parseInt(request.getParameter("extras_count"));
	int child_count = Integer.parseInt(request.getParameter("child_count"));
	int crew_count = Integer.parseInt(request.getParameter("crew_count"));
	int meal_count = Integer.parseInt(request.getParameter("meal_count"));
	
	ArtistTimeSheetType atA = new ArtistTimeSheetType();
	for(int ck1=1; ck1<=artist_count; ck1++){//getting the artist information
		SingleArtistType saA = new SingleArtistType();
		saA.setArtist(request.getParameter("artist_"+ ck1));
		saA.setPU(request.getParameter("artist_pu_"+ ck1));
        saA.setSignatureUrl(request.getParameter("artist_signature_" + ck1));
        saA.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_scheduled_"+ ck1)));
		saA.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_actual_"+ ck1)));
		saA.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("artist_meal_"+ ck1)));
		saA.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("artist_wrap_"+ ck1)));
		saA.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("artist_travel_"+ ck1)));
		atA.getSingleArtist().add(saA);
	}
	
    ArtistTimeSheetType atE = new ArtistTimeSheetType();
    for(int ck1=1; ck1<=extras_count; ck1++){//getting the bgartist information
        SingleArtistType saE = new SingleArtistType();
        saE.setArtist(request.getParameter("backgroundartist_"+ ck1));
        saE.setPU(request.getParameter("backgroundartist_pu_"+ ck1));
        saE.setSignatureUrl(request.getParameter("backgroundartist_signature_" + ck1));
        saE.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("backgroundartist_muwdcall_scheduled_"+ ck1)));
        saE.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("backgroundartist_muwdcall_actual_"+ ck1)));
        saE.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("backgroundartist_meal_"+ ck1)));
        saE.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("backgroundartist_wrap_"+ ck1)));
        saE.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("backgroundartist_travel_"+ ck1)));
        atE.getSingleArtist().add(saE);
    }
	
    ChildrenTimeSheetType chts = new ChildrenTimeSheetType();
	for(int ck2=1; ck2<=child_count; ck2++){//getting the children information
		SingleChildrenType sch = new SingleChildrenType();
		sch.setChildren(request.getParameter("children_"+ ck2));
		sch.setPU(request.getParameter("children_pu_"+ ck2));
		sch.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("children_muwdcall_scheduled_"+ ck2)));
		sch.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("children_muwdcall_actual_"+ ck2)));
		sch.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("children_meal_"+ ck2)));
		sch.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("children_wrap_"+ ck2)));
		sch.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("children_travel_"+ ck2)));
		sch.setRemarks(request.getParameter("children_remarks_"+ ck2));
		chts.getSingleChildren().add(sch);
	}
	
	CrewTimeSheetType cwts = new CrewTimeSheetType();
	for(int ck3=1; ck3<=crew_count; ck3++){//getting the crew information
		SingleCrewType scw = new SingleCrewType();
		
		if(request.getParameter("crew_" + ck3).equals("Other ...")) {
			scw.setCrew(request.getParameter("crew_other_" + ck3));
		} else {
			scw.setCrew(request.getParameter("crew_" + ck3));
		}
		scw.setCrewCall(XMLGregorianCalendarImpl.parse(request.getParameter("crew_call_"+ ck3)));
		scw.setTravelIn(XMLGregorianCalendarImpl.parse(request.getParameter("crew_travelin_"+ ck3)));
		scw.setLocationCall(XMLGregorianCalendarImpl.parse(request.getParameter("crew_loccall_"+ ck3)));
		scw.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("crew_meal_"+ ck3)));
		scw.setWrap(XMLGregorianCalendarImpl.parse(request.getParameter("crew_wrap_"+ ck3)));
		scw.setWrapLoc(XMLGregorianCalendarImpl.parse(request.getParameter("crew_wraploc_"+ ck3)));
		scw.setDepartLoc(XMLGregorianCalendarImpl.parse(request.getParameter("crew_departloc_"+ ck3)));
		scw.setTravelOut(XMLGregorianCalendarImpl.parse(request.getParameter("crew_travelout_"+ ck3)));
		scw.setRemarks(request.getParameter("crew_remarks_"+ ck3));
		cwts.getSingleCrew().add(scw);
	}
	
	MealInfoType mit = new MealInfoType();
	for(int ck4=1; ck4<=meal_count; ck4++){//getting the meal information
		SingleMealType sm = new SingleMealType();
		sm.setMeal(request.getParameter("meal_"+ ck4));
		//times
		FromToType ftt = new FromToType();
		ftt.setFrom(XMLGregorianCalendarImpl.parse(request.getParameter("meal_timefrom_" + ck4)));
		ftt.setTo(XMLGregorianCalendarImpl.parse(request.getParameter("meal_timeto_" + ck4)));
		sm.setMealTimes(ftt);
		sm.setDuration(XMLGregorianCalendarImpl.parse(request.getParameter("meal_duration_" + ck4)));
		sm.setNumbers(new BigInteger(request.getParameter("meal_numbers_"+ ck4)));
		sm.setLocation(request.getParameter("meal_location_"+ ck4));
		sm.setRemarks(request.getParameter("meal_remarks_"+ ck4));
		mit.getSingleMeal().add(sm);
	}
	
	TimeSheetInfoType tsit = new TimeSheetInfoType();
	tsit.setArtistTimeSheet(atA);
	tsit.setExtrasTimeSheet(atE);
	tsit.setChildrenTimeSheet(chts);
	tsit.setCrewTimeSheet(cwts);
	tsit.setMealInfo(mit);
	tsit.setLivestock(request.getParameter("livestock_other"));
	tsit.setAccidentsDelays(request.getParameter("accidents"));
	tsit.setMajorPropsActionVehiclesExtraEquipment(request.getParameter("major_props"));
	tsit.setAdditionalPersonnel(request.getParameter("additional_personnel"));
	tsit.setGeneralComments(request.getParameter("general_comments"));
	
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
	
	
	foadr.setProducer(request.getParameter("producer"));
	foadr.setDirector(request.getParameter("director"));
	foadr.setAssistantDirector(request.getParameter("assistant_director"));
	foadr.setTimeSheetInfo(tsit);
	
	if (request.getParameter("final_submission").equals("partial")){
		foadr.setFinalSubmission(false);
	}else if (request.getParameter("final_submission").equals("final")){
		foadr.setFinalSubmission(true);
	}
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(foadrElement, xmlOS);//out to ByteArray

	response.setHeader("Content-Disposition", "attachment;filename=\"ADReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
	response.setHeader("Content-Type", "text/xml");

	ServletOutputStream outs = response.getOutputStream();
	xmlOS.writeTo(outs);
	outs.close();
}			
%>		

</body>
</html>