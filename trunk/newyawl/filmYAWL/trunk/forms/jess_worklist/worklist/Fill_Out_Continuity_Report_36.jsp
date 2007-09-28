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
<%@ page import="org.yawlfoundation.sb.continuityinfo.*"%>
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
<link href="graphics/style.css" rel="stylesheet" type="text/css" />
<link href="styles/common.css" rel="stylesheet" type="text/css" />

<!-- javascript imports -->
<script type="text/javascript" src="scripts/common.js" ></script>
<script type="text/javascript" src="scripts/fillOutContinuityReport36.js" ></script>
       
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
            int endOfFile = result.indexOf("</ns2:Fill_Out_Continuity_Report>");
            if(beginOfFile != -1 && endOfFile != -1){
                xml = result.substring(
                    beginOfFile,
                    endOfFile + 33);
				//System.out.println("xml: "+xml);
    		}
		}
	}
	else{
		xml = (String)session.getAttribute("outputData");
		xml = xml.replaceAll("<Fill_Out_Continuity_Report", "<ns2:Fill_Out_Continuity_Report xmlns:ns2='http://www.yawlfoundation.org/sb/continuityInfo'");
		xml = xml.replaceAll("</Fill_Out_Continuity_Report","</ns2:Fill_Out_Continuity_Report");
		//System.out.println("outputData xml: "+xml+" --- ");
	}
	
	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes());
	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.sb.continuityinfo");
	Unmarshaller u = jc.createUnmarshaller();
	JAXBElement focrElement = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file	            
	FillOutContinuityReportType focr = (FillOutContinuityReportType) focrElement.getValue();
	GeneralInfoType gi = focr.getGeneralInfo();
%>
				
<table width="700" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr><td colspan="3" class="background_top">&nbsp;</td></tr>
  
  <tr>
    <td width="14" class="background_left">&nbsp;</td>
    <td><h1 align="center">Continuity Report </h1>
		<form name="form1" method="post">
    	<table width="800" border="0" align="center">
		
			<tr><td>
			
				<table width='700' border='0' cellspacing='0' cellpadding='0'>
					<tr><td class="header-left">&nbsp;</td>
					  <td colspan='6' class="header-middle">General Info </td>
					  <td class="header-right">&nbsp;</td>
					</tr>
					<tr>
						<td class='left' width='15'>&nbsp;</td>
						<td><strong>Production</strong></td><td><input name='production' type='text' id='production' value='<%=gi.getProduction()%>' readonly></td>
						<td><strong>Date</strong></td><td><input name='date' type='text' id='date' value='<%=gi.getDate().getDay()+"-"+gi.getDate().getMonth()+"-"+gi.getDate().getYear()%>' readonly></td>
						<td><strong>Day</strong></td><td><input name='weekday' type='text' id='weekday' value='<%=gi.getWeekday()%>' readonly></td>
						<td class='right' width='15'>&nbsp;</td>
					</tr>
					<tr>
						<td class='left' width='15'>&nbsp;</td>
						<td><strong>Producer</strong></td><td><input name='producer' type='text' id='producer' value='<%=focr.getProducer()%>' readonly></td>
						<td><strong>Director</strong></td><td><input name='director' type='text' id='director' value='<%=focr.getDirector()%>' readonly></td>
						<td><strong>Shoot Day </strong></td><td><input name='shoot_day' type='text' id='shoot_day' value='<%=gi.getShootDayNo()%>' readonly></td>
						<td class='right' width='15'>&nbsp;</td>
					</tr>
					<tr>
						<td class='left' width='15'>&nbsp;</td>
						<td><strong>D.O.P.</strong></td><td><input name='dop' type='text' id='dop' value='<%=focr.getDirectorOfPhotography()%>' readonly></td>
						<td><strong>Editor</strong></td><td><input name='editor' type='text' id='editor' value='<%=focr.getEditor()%>' readonly></td>
						<td><strong>Continuity</strong></td><td><input name='continuity' type='text' id='continuity' value='<%=focr.getContinuity()%>' readonly></td>
						<td class='right' width='15'>&nbsp;</td>
					</tr>	
					<tr><td colspan='8' class='bottom'>&nbsp;</td>
					</tr>
			  </table>
				
			</td></tr>
			<tr><td>
			
				<table width="700" border="0" cellpadding="0" cellspacing="0" id="table1">
					<tbody>
						<tr valign="top"><th class="header-left">&nbsp;</th>
						  <td colspan="8" class="header-middle">Continuity Info </td>
						  <th class="header-right">&nbsp;</th>
						</tr>
						<tr valign="top">
							<th width="15" class="left">&nbsp;</th>
							<th><strong>Cam</strong></th>
							<th><strong>Sound</strong></th>
							<th><strong>Scene</strong></th>
							<th><strong>Slate</strong></th>
							<th><strong>Take</strong></th>
							<th><strong>Print</strong></th>
							<th><strong>Duration</strong></th>
							<th><strong>Shot Description - Comments<br>(lens, focus, stop, filter)</strong></th>
							<th width="15" class="right">&nbsp;</th>
						</tr>
					<%
						int a=0;
						if (focr.getContinuityInfo() != null) {
						ContinuityInfoType cit = focr.getContinuityInfo();
								for(SceneInfoType sit : cit.getSceneInfo()){
									for(SlateInfoType slt : sit.getSlateInfo()){
										for(TakeInfoType tit : slt.getTakeInfo()){
										a++;
					%>
                    <tr valign="top">
                      <td width="15" align="center" class="left">&nbsp;</td>
                      <td align="center"><input name="<% out.print("cam_"+a);%>" type="text" id="<% out.print("cam_"+a);%>" value="<%=tit.getCamRoll()%>" size="5" pattern="any_text" title="Enter Camera Roll. [String Value]"></td>
                      <td align="center"><input name="<% out.print("sound_"+a);%>" type="text" id="<% out.print("sound_"+a);%>" value="<%=tit.getSoundRoll()%>" size="5" pattern="any_text" title="Enter Sound Roll. [String Value]"></td>
                      <td align="center"><input name="<% out.print("scene_"+a);%>" type="text" id="<% out.print("scene_"+a);%>" value="<%=sit.getScene()%>" size="5" pattern="any_text" title="Enter Scene Number. [String Value] (make sure this matches a scene number from the shooting schedule)"></td>
                      <td align="center"><input name="<% out.print("slate_"+a);%>" type="text" id="<% out.print("slate_"+a);%>" value="<%=slt.getSlate()%>" size="5" pattern="any_text" title="Enter Slate. [String Value]"></td>
                      <td align="center"><input name="<% out.print("take_"+a);%>" type="text" id="<% out.print("take_"+a);%>" value="<%=tit.getTake()%>" size="5" pattern="number" title="Enter Take Number. [Number Value]"></td>
					  <td align='center'><input name='<% out.print("print_"+a);%>' type='checkbox' id='<% out.print("print_"+a);%>' value='true' <% if(tit.isPrint() == true) {out.print("checked");}%>></td>
                      <td align="center"><input name="<% out.print("duration_"+a);%>" type="text" id="<% out.print("duration_"+a);%>" value="<%=tit.getDuration()%>"size="8" pattern="time" title="Enter Take Duration. [Time Value HH:MM:SS]"></td>
                      <td align="center"><textarea name="<% out.print("comments_"+a);%>" cols="30" id="<% out.print("comments_"+a);%>" pattern="any_text" title="Enter Shot Description. [String Value]"><%= tit.getComments()%></textarea></td>
                      <td width="15" class="right">&nbsp;</td>
                    </tr>
                    <% 
								}
							}
						}
					} else {
					%>
                    <tr valign="top">
                      <td width="15" align="center" class="left">&nbsp;</td>
                      <td align="center"><input name="cam_1" type="text" id="cam_1" value="" size="5"  pattern="any_text" title="Enter Camera Roll. [String Value]"></td>
                      <td align="center"><input name="sound_1" type="text" id="sound_1" value="" size="5" pattern="any_text" title="Enter Sound Roll. [String Value]"></td>
                      <td align="center"><input name="scene_1" type="text" id="scene_1" value="" size="5" pattern="any_text" title="Enter Scene Number. [String Value] (make sure this matches a scene number from the shooting schedule)"></td>
                      <td align="center"><input name="slate_1" type="text" id="slate_1" value="" size="5" pattern="any_text" title="Enter Slate. [String Value]"></td>
                      <td align="center"><input name="take_1" type="text" id="take_1" value="" size="5" pattern="number" title="Enter Take Number. [Number Value]"></td>
					  <td align='center'><input name="print_1" type='checkbox' id="print_1" value='true'></td>
                      <td align="center"><input name="duration_1" type="text" id="duration_1" value="" size="8" pattern="time" title="Enter Take Duration. [Time Value HH:MM:SS]"></td>
                      <td align="center"><textarea name="comments_1" cols="30" id="comments_1" pattern="any_text" title="Enter Shot Description. [String Value]"></textarea></td>
                      <td width="15" class="right">&nbsp;</td>
                    </tr>
                    <% 
					}
					%>
					</tbody>
					<tr valign="top"><th colspan="10" class="bottom">&nbsp;</th>
					</tr>
				</table>
				
			</td></tr>
		
			<tr><td>
				<input type="button" value="Insert Row" onClick="addRow();">
				<input type="button" value="Delete Row" onClick="deleteRow();">
				<input type="hidden" name="count" id="count" value="<%if (a==0) {out.print("1");}else{out.print(a);}%>">
				<input type="hidden" name="workItemID" id="workItemID">
				<input type="hidden" name="userID" id="userID">
				<input type="hidden" name="sessionHandle" id="sessionHandle">
				<input type="hidden" name="JSESSIONID" id="JSESSIONID">
				<input type="hidden" name="submit" id="submit">
			</td></tr>
			<tr>
			  <td align="center">Final Submission 
		      <input name="final_submission" type="checkbox" id="final_submission" value="True" <% if(focr.isFinalSubmission() == true) {out.print("checked");}%>></td>
		  </tr>
	  </table>
	  <p align="center">
	  <input type="button" value="Print"  onclick="window.print()">
	  <input type="submit" name="Save" value="Save" onclick="return validateFields('form1');">
	  <input type="submit" name="Submission" value="Submission"onclick="return validateFields('form1');"></p>
	</form>
	
	<!-- LOAD -->
    <form method="post" action="Fill_Out_Continuity_Report_36.jsp?formType=load&workItemID=<%= request.getParameter("workItemID") %>&userID=<%= request.getParameter("userID") %>&sessionHandle=<%= request.getParameter("sessionHandle") %>&JSESSIONID=<%= request.getParameter("JSESSIONID") %>&submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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
    <td width="14" class="background_right"></td>
  </tr>
  <tr>
    <td colspan="3" class="background_bottom"></td>
  </tr>
</table>
<%
if(request.getParameter("Submission") != null){

	int count = Integer.parseInt(request.getParameter("count"));
	
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
		ti.setDuration(XMLGregorianCalendarImpl.parse(request.getParameter("duration_"+i)));
		
		if (request.getParameter("print_"+i)==null)
			ti.setPrint(false);
		else
			ti.setPrint(true);
			
		ti.setComments(request.getParameter("comments_"+i));
		ti.setCamRoll(request.getParameter("cam_"+i));
		ti.setSoundRoll(request.getParameter("sound_"+i));
		
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
		String[] st = key.split("\t");
		tempSceneNO = st[0];
		
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
	List<SceneInfoType> scl = new ArrayList<SceneInfoType>(scenes.values());//creates a list of the scenes and adds it to the continuityInfo facade
	ContinuityInfoType ci = new ContinuityInfoType();
	ci.getSceneInfo().addAll(scl);
	
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
	
	focr.setProducer(request.getParameter("producer"));
	focr.setDirector(request.getParameter("director"));
	focr.setDirectorOfPhotography(request.getParameter("dop"));
	focr.setEditor(request.getParameter("editor"));
	focr.setContinuity(request.getParameter("continuity"));
	focr.setContinuityInfo(ci);
	if (request.getParameter("final_submission")==null){
		focr.setFinalSubmission(false);
	}else{
		focr.setFinalSubmission(true);
	}
	
	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    File f = new File("./backup/ContinuityReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+".xml");
    m.marshal( focrElement,  f);//output to file
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(focrElement, xmlOS);//out to ByteArray
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

	int count = Integer.parseInt(request.getParameter("count"));
	
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
		ti.setDuration(XMLGregorianCalendarImpl.parse(request.getParameter("duration_"+i)));
		if (request.getParameter("print_"+i)==null)
			ti.setPrint(false);
		else
			ti.setPrint(true);
		ti.setComments(request.getParameter("comments_"+i));
		ti.setCamRoll(request.getParameter("cam_"+i));
		ti.setSoundRoll(request.getParameter("sound_"+i));
		
				
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
		String[] st = key.split("\t");
		tempSceneNO = st[0];
		
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
	
	
	
	List<SceneInfoType> scl = new ArrayList<SceneInfoType>(scenes.values());//creates a list of the scenes and adds it to the continuityInfo facade
	ContinuityInfoType ci = new ContinuityInfoType();
	ci.getSceneInfo().addAll(scl);
	
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
	
	focr.setProducer(request.getParameter("producer"));
	focr.setDirector(request.getParameter("director"));
	focr.setDirectorOfPhotography(request.getParameter("dop"));
	focr.setEditor(request.getParameter("editor"));
	focr.setContinuity(request.getParameter("continuity"));
	focr.setContinuityInfo(ci);
	if (request.getParameter("final_submission")==null){
		focr.setFinalSubmission(false);
	}else{
		focr.setFinalSubmission(true);
	}
	
	Marshaller m = jc.createMarshaller();
    m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    
	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
    m.marshal(focrElement, xmlOS);//out to ByteArray

    response.setHeader("Content-Disposition", "attachment;filename=\"ContinuityReport_"+new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date())+"_l.xml\";");
    response.setHeader("Content-Type", "text/xml");

    ServletOutputStream outs = response.getOutputStream();
    xmlOS.writeTo(outs);
    outs.close();
}
%>
</body>
</html>