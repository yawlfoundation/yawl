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
<%@ page import="org.yawlfoundation.sb.reviewtimesheetinfo.*"%>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="au.edu.qut.yawl.forms.InterfaceD_XForm"%>
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
<script type="text/javascript" src="scripts/common.js"></script>

</head>

<body onLoad="getParameters()">

<% 
	String xml = null;
	String inputData = null;
	
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
            int endOfFile = result.indexOf("</ns2:Review_AD_Report>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(beginOfFile, endOfFile + 23);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		xml = (String)session.getAttribute("outputData");
		inputData = xml;
		xml = xml.replaceAll("<Review_AD_Report", "<ns2:Review_AD_Report xmlns:ns2='http://www.yawlfoundation.org/sb/reviewTimeSheetInfo'");
		xml = xml.replaceAll("</Review_AD_Report","</ns2:Review_AD_Report");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.reviewtimesheetinfo");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement radrElement = (JAXBElement)u.unmarshal(xmlBA);	//creates the root element from XML file	            
	ReviewADReportType radr = (ReviewADReportType)radrElement.getValue();
				
	GeneralInfoType gi = radr.getGeneralInfo();
	TimeSheetInfoType tsi = radr.getTimeSheetInfo();
	
	// begin partial submission code
	Map<Object, Object> parameters = Collections.synchronizedMap(new TreeMap<Object, Object>());
    InterfaceD_XForm idx = new InterfaceD_XForm(getServletContext().getInitParameter("HTMLForms") + "/yawlFormServlet;jsessionid="+request.getParameter("JSESSIONID"));
	
    session.setAttribute("inputData", inputData);
	
	parameters.put("JSESSIONID", request.getParameter("JSESSIONID"));
    parameters.put("workItemID", request.getParameter("workItemID"));
    parameters.put("userID", request.getParameter("userID"));
    parameters.put("sessionHandle", request.getParameter("sessionHandle"));
    parameters.put("submit", request.getParameter("submit"));
	
    // send (post) data thru interfaceD
    idx.sendHTMLWorkItemData(parameters);
    //return;
%>
%>

<table width="700" height="100%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td height="14" colspan="3" class="background_top">&nbsp;</td></tr>
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td>
		<h1 align="center">2nd Assistant Director's Report </h1>      
		<form name="form1" method="post">			
			
		<table width="700" border="0" align="center">

			<tr><td>
				<table width='700' border='0' cellpadding='0' cellspacing='0'>
					<tr><td align='right' valign='top' class="header-left">&nbsp;</td>
				      <td colspan='6' valign='top' class="header-middle">General Info</td>
				      <td align='right' valign='top' class="header-right">&nbsp;</td>
				  </tr>
					<tr>
						<td class='left' width='14'>&nbsp;</td>
						<td><strong>Production</strong></td><td><input name='production' type='text' id='production' value='<%=gi.getProduction()%>' size="15" readonly></td>
						<td><strong>Date</strong></td><td><input name='date' type='text' id='date' value='<%=gi.getDate().getDay()+"-"+gi.getDate().getMonth()+"-"+gi.getDate().getYear()%>' size="15" readonly></td>
						<td><strong>Day</strong></td><td><input name='weekday' type='text' id='weekday' value='<%=gi.getWeekday()%>' size="15" readonly></td>
						<td class='right' width='14'>&nbsp;</td>
					</tr>
					<tr>
						<td class='left' width='14'>&nbsp;</td>
						<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='<%=radr.getProducer()%>' size="15" readonly></td>
						<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='<%=radr.getDirector()%>' size="15" readonly></td>
						<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='<%=gi.getShootDayNo()%>' size="15" readonly></td>
						<td class='right' width='14'>&nbsp;</td>
					</tr>
					<tr height='30'>
						<td class='left' width='14'>&nbsp;</td>
						<td><strong>Assistant Director</strong></td>
						<td><input name='assistant_director' type='text' id='assistant_director' value='<%=radr.getAssistantDirector()%>' size="15" readonly></td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td class='right' width='14'>&nbsp;</td>
					</tr>
					<tr height='30'><td colspan='8' class='bottom'>&nbsp;</td></tr>
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
						if(tsi != null){
							ArtistTimeSheetType atsA = tsi.getArtistTimeSheet();
							for(SingleArtistType satA : atsA.getSingleArtist()) {
								a++;
						%>
						<tr align="center">
							<td width="14" align="center" class="left" height="30">&nbsp;</td>
							<td align="center"><input name="<% out.print("artist_" + a); %>" type="text" id="<% out.print("artist_" + a); %>" size="15" value="<%= satA.getArtist()%>" readonly></td>
							<td align="center"><input name="<% out.print("artist_pu_" + a); %>" type="text" id="<% out.print("artist_pu_" + a); %>" size="6" value="<%= satA.getPU()%>" readonly></td>
							<td align="center"><input name="<% out.print("artist_muwdcall_scheduled_" + a); %>" type="text" id="<% out.print("artist_muwdcall_scheduled_" + a); %>" size="6" value="<%= satA.getMUWDCallScheduled()%>" readonly></td>
							<td align="center"><input name="<% out.print("artist_muwdcall_actual_" + a); %>" type="text" id="<% out.print("artist_muwdcall_actual_" + a); %>" size="6" value="<%= satA.getMUWDCallActualArrival()%>" readonly></td>
							<td align="center"><input name="<% out.print("artist_meal_" + a); %>" type="text" id="<% out.print("artist_meal_" + a); %>" size="6" value="<%= satA.getMealBreak()%>" readonly></td>
							<td align="center"><input name="<% out.print("artist_wrap_" + a); %>" type="text" id="<% out.print("artist_wrap_" + a); %>" size="6" value="<%= satA.getTimeWrap()%>" readonly></td>
							<td align="center"><input name="<% out.print("artist_travel_" + a); %>" type="text" id="<% out.print("artist_travel_" + a); %>" size="6" value="<%= satA.getTravel()%>" readonly></td>
							<td align="center"><input name="<% out.print("artist_signature_" + a); %>" type="text" id="<% out.print("artist_signature_" + a); %>" size="15" readonly></td>
							<td width="15" class="right">&nbsp;</td>
						</tr> 
						<% }
						}%>
					</tbody>
				    <tr valign="top">
				      <td class="left">&nbsp;</td>
			          <td colspan="8">&nbsp;</td>
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
							<td align="center"><input name="<% out.print("backgroundartist_" + b); %>" type="text" id="<% out.print("backgroundartist_" + b); %>" size="15" value="<%= satE.getArtist()%>" readonly></td>
							<td align="center"><input name="<% out.print("backgroundartist_pu_" + b); %>" type="text" id="<% out.print("backgroundartist_pu_" + b); %>" size="6" value="<%= satE.getPU()%>" readonly></td>
							<td align="center"><input name="<% out.print("backgroundartist_muwdcall_scheduled_" + b); %>" type="text" id="<% out.print("backgroundartist_muwdcall_scheduled_" + b); %>" size="6" value="<%= satE.getMUWDCallScheduled()%>" readonly></td>
							<td align="center"><input name="<% out.print("backgroundartist_muwdcall_actual_" + b); %>" type="text" id="<% out.print("backgroundartist_muwdcall_actual_" + b); %>" size="6" value="<%= satE.getMUWDCallActualArrival()%>" readonly></td>
							<td align="center"><input name="<% out.print("backgroundartist_meal_" + b); %>" type="text" id="<% out.print("backgroundartist_meal_" + b); %>" size="6" value="<%= satE.getMealBreak()%>" readonly></td>
							<td align="center"><input name="<% out.print("backgroundartist_wrap_" + b); %>" type="text" id="<% out.print("backgroundartist_wrap_" + b); %>" size="6" value="<%= satE.getTimeWrap()%>" readonly></td>
							<td align="center"><input name="<% out.print("backgroundartist_travel_" + b); %>" type="text" id="<% out.print("backgroundartist_travel_" + b); %>" size="6" value="<%= satE.getTravel()%>" readonly></td>
							<td align="center"><input name="<% out.print("backgroundartist_signature_" + b); %>" type="text" id="<% out.print("backgroundartist_signature_" + b); %>" size="15" readonly></td>
							<td width="15" class="right">&nbsp;</td>
						</tr> 
						<% }
						}%>

              </tbody>
              <tr align="center" valign="top">
                <td align="right" class="left">&nbsp;</td>
                <td colspan="8" align="left">&nbsp;</td>
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
							<td align="center"><input name="<% out.print("children_" + c); %>" type="text" id="<% out.print("children_" + c); %>" size="15" value="<%= sct.getChildren()%>" readonly></td>
							<td align="center"><input name="<% out.print("children_pu_" + c); %>" type="text" id="<% out.print("children_pu_" + c); %>" size="6" value="<%= sct.getPU()%>" readonly></td>
							<td align="center"><input name="<% out.print("children_muwdcall_scheduled_" + c); %>" type="text" id="<% out.print("children_muwdcall_scheduled_" + c); %>" size="6" value="<%= sct.getMUWDCallScheduled()%>" readonly></td>
							<td align="center"><input name="<% out.print("children_muwdcall_actual_" + c); %>" type="text" id="<% out.print("children_muwdcall_actual_" + c); %>" size="6" value="<%= sct.getMUWDCallActualArrival()%>" readonly></td>
							<td align="center"><input name="<% out.print("children_meal_" + c); %>" type="text" id="<% out.print("children_meal_" + c); %>" size="6" value="<%= sct.getMealBreak()%>" readonly></td>
							<td align="center"><input name="<% out.print("children_wrap_" + c); %>" type="text" id="<% out.print("children_wrap_" + c); %>" size="6" value="<%= sct.getTimeWrap()%>" readonly></td>
							<td align="center"><input name="<% out.print("children_travel_" + c); %>" type="text" id="<% out.print("children_travel_" + c); %>" size="6" value="<%= sct.getTravel()%>" readonly></td>
						    <td align="center"><textarea name="<% out.print("children_remarks_" + c); %>" id="<% out.print("children_remarks_" + c); %>" cols="15" readonly><%= sct.getRemarks()%></textarea></td>
							<td width="15" class="right">&nbsp;</td>
						</tr> 
						<% }
						}%>
						
						
					</tbody>
					<tr align="center" valign="top">
					  <td align="center" class="left">&nbsp;</td>
				      <td colspan="8" align="left">&nbsp;</td>
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
						if(tsi != null){
							CrewTimeSheetType crts = tsi.getCrewTimeSheet();
							for(SingleCrewType scrt : crts.getSingleCrew()) {
								d++;
						%>
						<tr>
						  <td align="center" class="left">&nbsp;</td>
						  <td align="center" valign="top">					             
                          <input name="<% out.print("crew_" + d); %>" type="text" id="<% out.print("crew_" + d); %>" size="15" value="<%=scrt.getCrew()%>" readonly></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_call_" + d); %>" type="text" id="<% out.print("crew_call_" + d); %>" size="5" value="<%=scrt.getCrewCall() %>" readonly></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_travelin_" + d); %>" type="text" id="<% out.print("crew_travelin_" + d); %>" size="5" value="<%=scrt.getTravelIn() %>" readonly></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_loccall_" + d); %>" type="text" id="<% out.print("crew_loccall_" + d); %>" size="5" value="<%=scrt.getLocationCall() %>" readonly></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_meal_" + d); %>" type="text" id="<% out.print("crew_meal_" + d); %>" size="5" value="<%=scrt.getMealBreak() %>" readonly></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_wrap_" + d); %>" type="text" id="<% out.print("crew_wrap_" + d); %>" size="5" value="<%=scrt.getWrap() %>" readonly></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_wraploc_" + d); %>" type="text" id="<% out.print("crew_wraploc_" + d); %>" size="5" value="<%=scrt.getWrapLoc() %>" readonly></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_departloc_" + d); %>" type="text" id="<% out.print("crew_departloc_" + d); %>" size="5" value="<%=scrt.getDepartLoc() %>" readonly></td>
						  <td align="center" valign="top"><input name="<% out.print("crew_travelout_" + d); %>" type="text" id="<% out.print("crew_travelout_" + d); %>" size="5" value="<%=scrt.getTravelOut() %>" readonly></td>
						  <td align="center" valign="top"><textarea name="<% out.print("crew_remarks_" + d); %>" cols="8" id="<% out.print("crew_remarks_" + d); %>" readonly><%=scrt.getRemarks() %></textarea></td>
						  <td align="center" class="right">&nbsp;</td>
					  </tr>
					  <%}
					  }%>
					  
			  		</tbody>
			  		<tr align="center" valign="top">
			  		  <td class="left">&nbsp;</td>
			  		  <td colspan="10" align="left">&nbsp;</td>
			  		  <td class="right">&nbsp;</td>
		  		  </tr>
			  		<tr align="center" valign="top">
			  		  <td class="left">&nbsp;</td>
			  		  <td colspan="10" align="left">&nbsp;</td>
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
					  	<td align="center" valign="top"><input name="<% out.print("meal_" + e); %>" type="text" id="<% out.print("meal_" + e); %>" size="6" value="<%=smt.getMeal() %>" readonly></td>
						<% FromToType ft = smt.getMealTimes();%>
					  	<td align="center" valign="top"><input name="<% out.print("meal_timefrom_" + e); %>" type="text" id="<% out.print("meal_timefrom_" + e); %>" size="6" value="<%=ft.getFrom() %>" readonly></td>
					  	<td align="center" valign="top"><input name="<% out.print("meal_timeto_" + e); %>" type="text" id="<% out.print("meal_timeto_" + e); %>" size="6" value="<%=ft.getTo() %>" readonly> 
						<input name="<% out.print("meal_duration_" + e); %>" type="hidden" id="<% out.print("meal_duration_" + e); %>" size="6" value="<%=smt.getDuration()%>"></td>
					  	<td align="center" valign="top"><input name="<% out.print("meal_numbers_" + e); %>" type="text" id="<% out.print("meal_numbers_" + e); %>" size="6" value="<%=smt.getNumbers() %>" readonly></td>
					  	<td align="center" valign="top"><input name="<% out.print("meal_location_" + e); %>" type="text" id="<% out.print("meal_location_" + e); %>" size="20" value="<%=smt.getLocation() %>" readonly></td>
					  	<td align="center" valign="top"><textarea name="<% out.print("meal_remarks_" + e); %>" cols="20" id="<% out.print("meal_remarks_" + e); %>" readonly><%=smt.getRemarks() %></textarea></td>
					  	<td width="15" align="center" class="right"></td>
					</tr>
					<% }
					}%>
				</tbody>
					<tr>
					  	<td width="15" align="center" class="left">&nbsp;</td>
					  	<td colspan="6" valign="top">&nbsp;</td>
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
					<tr><td width="15" class="left">&nbsp;</td><td align="center"><textarea name="livestock_other" cols="70" id="livestock_other" readonly><% if(tsi != null){ out.print(tsi.getLivestock()); } %></textarea></td><td width="15" class="right">&nbsp;</td></tr>
					<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
  			  </table>
			</td></tr>
			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0">
					<tr><td class="header-left">&nbsp;</td>
				      <td class="header-middle">Accidents/Delays</td>
				      <td class="header-right">&nbsp;</td>
				  </tr>
					<tr><td width="15" class="left">&nbsp;</td><td align="center"><textarea name="accidents" cols="70" id="accidents" readonly><% if(tsi != null){ out.print(tsi.getAccidentsDelays()); } %></textarea></td><td width="15" class="right">&nbsp;</td></tr>
					<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
  			  </table>
			</td></tr>
			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0">
					<tr><td class="header-left">&nbsp;</td>
				      <td class="header-middle">Major Props / Action Vehicles / Extra Equipment</td>
				      <td class="header-right">&nbsp;</td>
				  </tr>
					<tr><td width="15" class="left">&nbsp;</td><td align="center"><textarea name="major_props" cols="70" id="major_props" readonly><% if(tsi != null){ out.print(tsi.getMajorPropsActionVehiclesExtraEquipment()); } %></textarea></td><td width="15" class="right">&nbsp;</td></tr>
					<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
  			  </table>
			</td></tr>
			<tr><td>
				<table width="700" border="0" cellpadding="0" cellspacing="0">
					<tr><td class="header-left">&nbsp;</td>
				      <td class="header-middle">Additional Personnel </td>
				      <td class="header-right">&nbsp;</td>
				  </tr>
					<tr><td width="15" class="left">&nbsp;</td><td height="90" align="center"><textarea name="additional_personnel" cols="70" id="additional_personnel" readonly><% if(tsi != null){ out.print(tsi.getAdditionalPersonnel()); } %></textarea></td>
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
					<tr><td width="15" height="90" class="left">&nbsp;</td><td height="90" align="center"><textarea name="general_comments" cols="70" id="general_comments" readonly><% if(tsi != null){ out.print(tsi.getGeneralComments()); } %></textarea></td><td width="15" height="90" class="right">&nbsp;</td></tr>
					<tr><td colspan="3" class="bottom">&nbsp;</td></tr>
  			  </table>
			</td></tr>
		    <tr>
		      <td align="center">&nbsp;</td>
	      </tr>
	      <tr><td align="center">	
				<input type="hidden" name="artist_count" id="artist_count" value="<%if (a==0) {out.print("1");}else{out.print(a);}%>">
				<input type="hidden" name="extras_count" id="extras_count" value="<%if (b==0) {out.print("0");}else{out.print(b);}%>">
				<input type="hidden" name="child_count" id="child_count" value="<%if (c==0) {out.print("0");}else{out.print(c);}%>">
				<input type="hidden" name="crew_count" id="crew_count" value="<%if (d==0) {out.print("1");}else{out.print(d);}%>">
				<input type="hidden" name="meal_count" id="meal_count" value="<%if (e==0) {out.print("1");}else{out.print(e);}%>">
				<input type="hidden" name="workItemID" id="workItemID">
				<input type="hidden" name="userID" id="userID">
				<input type="hidden" name="sessionHandle" id="sessionHandle">
				<input type="hidden" name="JSESSIONID" id="JSESSIONID">
				<input type="hidden" name="submit" id="submit">
				<input type="button" value="Print" onclick="window.print()">
				<input type="submit" name="Save" value="Save">
				<input type="button" name="Worklist" value="Worklist" onclick="newDoc()">
        </td>
		</tr>	
	</table>
	</form>	
	</td>
	<td width="14" class="background_right">&nbsp;</td>
  </tr>
  <tr><td height="14" colspan="3" class="background_bottom">
</td></tr>
</table>
	
<%
if(request.getParameter("Save") != null){

	int artist_count = Integer.parseInt(request.getParameter("artist_count"));
	int extras_count = Integer.parseInt(request.getParameter("extras_count"));
	int child_count = Integer.parseInt(request.getParameter("child_count"));
	int crew_count = Integer.parseInt(request.getParameter("crew_count"));
	int meal_count = Integer.parseInt(request.getParameter("meal_count"));
	
	ArtistTimeSheetType atsA = new ArtistTimeSheetType();
	for(int ck1=1; ck1<=artist_count; ck1++){//getting the artist information
		SingleArtistType saA = new SingleArtistType();
		saA.setArtist(request.getParameter("artist_"+ ck1));
		saA.setPU(request.getParameter("artist_pu_"+ ck1));
		saA.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_scheduled_"+ ck1)));
		saA.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_actual_"+ ck1)));
		saA.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("artist_meal_"+ ck1)));
		saA.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("artist_wrap_"+ ck1)));
		saA.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("artist_travel_"+ ck1)));
		atsA.getSingleArtist().add(saA);
	}
	
	ArtistTimeSheetType atsE = new ArtistTimeSheetType();
	for(int ck1=1; ck1<=artist_count; ck1++){//getting the artist information
		SingleArtistType saE = new SingleArtistType();
		saE.setArtist(request.getParameter("artist_"+ ck1));
		saE.setPU(request.getParameter("artist_pu_"+ ck1));
		saE.setMUWDCallScheduled(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_scheduled_"+ ck1)));
		saE.setMUWDCallActualArrival(XMLGregorianCalendarImpl.parse(request.getParameter("artist_muwdcall_actual_"+ ck1)));
		saE.setMealBreak(XMLGregorianCalendarImpl.parse(request.getParameter("artist_meal_"+ ck1)));
		saE.setTimeWrap(XMLGregorianCalendarImpl.parse(request.getParameter("artist_wrap_"+ ck1)));
		saE.setTravel(XMLGregorianCalendarImpl.parse(request.getParameter("artist_travel_"+ ck1)));
		atsE.getSingleArtist().add(saE);
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
		scw.setCrew(request.getParameter("crew_" + ck3));
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
	tsit.setArtistTimeSheet(atsA);
	tsit.setExtrasTimeSheet(atsE);
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
	
	
	radr.setProducer(request.getParameter("producer"));
	radr.setDirector(request.getParameter("director"));
	radr.setAssistantDirector(request.getParameter("assistant_director"));
	radr.setTimeSheetInfo(tsit);
	
	Marshaller m = jc.createMarshaller();
	m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
	
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(radrElement, xmlOS);//out to ByteArray

	response.setHeader("Content-Disposition", "attachment;filename=\"ReviewADReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
	response.setHeader("Content-Type", "text/xml");

	ServletOutputStream outs = response.getOutputStream();
	xmlOS.writeTo(outs);
	outs.close();
}			
%>		

</body>
</html>