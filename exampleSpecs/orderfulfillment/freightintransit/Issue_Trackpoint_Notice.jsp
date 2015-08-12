<%@ page import="org.yawlfoundation.orderfulfilment.issue_trackpoint_notice.*"%>
<%@ page import="org.jdom2.Element" %>
<%@ page import="org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGatewayClient" %>
<%@ page import="org.yawlfoundation.yawl.util.*" %>
<%@ page import="com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl" %>
<%@ page import="java.math.BigInteger" %>
<%@ page import="javax.xml.bind.*" %>
<%@ page import="javax.xml.stream.*" %>
<%@ page import="javazoom.upload.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page buffer="1024kb" %>

<%

//-------------------------online code-------------------------//
	String itemid = request.getParameter("workitem");
	String handle = request.getParameter("handle");
	String url = "http://localhost:8080/resourceService/workqueuegateway";
	WorkQueueGatewayClient client = new WorkQueueGatewayClient(url);

	String wir = (String) session.getAttribute("wir");
	if (wir == null) {
		wir = client.getWorkItem(itemid, handle);
		session.setAttribute("wir", wir);
	}

	String source = (String) session.getAttribute("source");
    if (source == null) {
        session.setAttribute("source", request.getParameter("source"));
    }

	String xml = (String) wir.substring(wir.indexOf("<updateddata>")+13, wir.indexOf("</updateddata>"));
	if(xml.length()==0){
		xml = (String) wir.substring(wir.indexOf("<data>")+6,wir.indexOf("</data>"));
	}
	xml = xml.replaceAll("<Issue_Trackpoint_Notice", "<ns2:Issue_Trackpoint_Notice xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Issue_Trackpoint_Notice'");
	xml = xml.replaceAll("</Issue_Trackpoint_Notice","</ns2:Issue_Trackpoint_Notice");
	System.out.println("XML" + xml);
%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<title>Issue Trackpoint Notice</title>
</head>

<body>

<%
	if (MultipartFormDataRequest.isMultipartFormData(request)) {
         MultipartFormDataRequest mrequest = new MultipartFormDataRequest(request);
         String todo = null;
		 StringBuffer result = new StringBuffer();

         if (mrequest != null) todo = mrequest.getParameter("todo");

	     if ( (todo != null) && (todo.equalsIgnoreCase("upload")) ){
            Hashtable files = mrequest.getFiles();
            if ( (files != null) && (!files.isEmpty()) ){
                UploadFile file = (UploadFile) files.get("uploadfile");
				InputStream in = file.getInpuStream();

				int i = in.read();
				while (i != -1) {
					result.append((char) i);
					i = in.read();
				}
			}

            int beginOfFile = result.indexOf("<?xml");
            int endOfFile = result.indexOf("</ns2:Issue_Trackpoint_Notice>");
            if(beginOfFile != -1 && endOfFile != -1) xml = result.substring(beginOfFile,endOfFile + 30);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.issue_trackpoint_notice");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement itnElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	IssueTrackpointNoticeType itnt1 = (IssueTrackpointNoticeType) itnElement1.getValue();
	TrackpointNoticeType tnt1 = itnt1.getTrackpointNotice();
	TrackpointNoticesType tnt2 = itnt1.getTrackpointNotices();
%>

<div id="logo"></div>
<h2>Issue Trackpoint Notice </h2>
<form id="IssueTrackpointNotice" name="IssueTrackpointNotice" method="post" novalidate="novalidate">

<fieldset>
	<legend>Trackpoint Notices</legend>
	<div id="trackpointNotice">
		<div id="trackpointOrderNumber">Order Number</div>
		<div id="trackpointShipmentNumber">Shipment Number</div>
		<div id="trackpointTrackpoint">Trackpoint</div>
		<div id="trackpointArrivalTime">Arrival Time</div>
		<div id="trackpointDepartureTime">Departure Time</div>
		<div id="trackpointNotes">Notes</div>
		<div id="allLines">
		<%
			String arrivaltimestring1 = tnt1.getArrivalTime().toString();
			arrivaltimestring1 = arrivaltimestring1.substring(0,5);

			String departuretimestring1 = tnt1.getDepartureTime().toString();
			departuretimestring1 = departuretimestring1.substring(0,5);

				out.println("<div id='trackpointNoticeEntry'>");

				out.println("<div id='trackpointOrderNumber'><input type='text' name='ordernumber1' id='ordernumber1' pattern='textValidation' value='"+tnt1.getOrderNumber()+"' /></div>");
				out.println("<div id='trackpointShipmentNumber'><input type='text' name='shipmentnumber1' id='shipmentnumber1' pattern='textValidation' value='"+tnt1.getShipmentNumber()+"' /></div>");
				out.println("<div id='trackpointTrackpoint'><input type='text' name='trackpoint1' id='trackpoint1' pattern='textValidation' value='"+tnt1.getTrackpoint() +"' /></div>");
				out.println("<div id='trackpointArrivalTime'><input type='text' name='arrivaltime1' id='arrivaltime1' pattern='timeValidation' value='"+arrivaltimestring1+"' /></div>");
				out.println("<div id='trackpointDepartureTime'><input type='text' name='departuretime1' pattern='timeValidation' id='departuretime1' value='"+departuretimestring1+"' /></div>");
				out.println("<div id='trackpointNotes'><input type='text' name='notes1' id='notes1' pattern='textValidation' value='"+tnt1.getNotes()+"'/></div>");
				out.println("</div>");

			for(TrackpointNoticeType tnt3 : tnt2.getTrackpointNotice()){

			String arrivaltimestring2 = tnt3.getArrivalTime().toString();
			arrivaltimestring2 = arrivaltimestring2.substring(0,5);

			String departuretimestring2 = tnt3.getDepartureTime().toString();
			departuretimestring2 = departuretimestring2.substring(0,5);

				out.println("<div id='trackpointNoticeEntry'>");

				out.println("<div id='trackpointOrderNumber'><input type='text' name='ordernumber' id='ordernumber' pattern='textValidation' value='"+tnt3.getOrderNumber()+"' class='InputReadOnly' readonly/></div>");
				out.println("<div id='trackpointShipmentNumber'><input type='text' name='shipmentnumber' id='shipmentnumber' pattern='textValidation' value='"+tnt3.getShipmentNumber()+"' class='InputReadOnly' readonly/></div>");
				out.println("<div id='trackpointTrackpoint'><input type='text' name='trackpointN' id='trackpointN' pattern='textValidation' value='"+tnt3.getTrackpoint() +"' class='InputReadOnly' readonly/></div>");
				out.println("<div id='trackpointArrivalTime'><input type='text' name='arrivaltime' id='arrivaltime' pattern='timeValidation' value='"+arrivaltimestring2+"' class='InputReadOnly' readonly/></div>");
				out.println("<div id='trackpointDepartureTime'><input type='text' name='departuretime' pattern='timeValidation' id='departuretime' value='"+departuretimestring2+"' class='InputReadOnly' readonly/></div>");
				out.println("<div id='trackpointNotes'><input type='text' name='notes' id='notes' pattern='textValidation' value='"+tnt3.getNotes()+"' class='InputReadOnly' readonly/></div>");
				out.println("</div>");
			}%>
		</div>
		<div id="entry"></div>
	</div>

</fieldset>

<div id="entry">&nbsp;</div>

<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save" >
	<input type="submit" name="SaveToFile" value="Save To File" onclick="return validateForm('IssueTrackpointNotice');"/>
	&nbsp;
	<input type="submit" name="Complete" value="Complete" onclick="return validateForm('IssueTrackpointNotice');"/>

</div>

</form>

<!-- LOAD -->
<form method="post" action="Issue_Trackpoint_Notice.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
    <div id="entry">&nbsp;</div>
	<div><strong>Select a file to upload :</strong></div>
	<div><input type="file" name="uploadfile" size="50"></div>
	<div>
		<input type="hidden" name="todo" value="upload">
		<input type="submit" name="Submit" value="Upload">
		<input type="reset" name="Reset" value="Cancel">
	</div>
</form>
<!-- END LOAD -->


<%
if(request.getParameter("Cancel") != null){
	//String redirectURL = "http://localhost:8080/resourceService/faces/userWorkQueues.jsp";
	//response.sendRedirect(response.encodeURL(redirectURL));
	session.removeAttribute("wir");
	String redirectURL = (String) session.getAttribute("source");
    session.removeAttribute("source");
    response.sendRedirect(response.encodeURL(redirectURL));
}else if(request.getParameter("Save") != null || request.getParameter("SaveToFile")!=null || request.getParameter("Complete") != null){

	TrackpointNoticeType track1 = new TrackpointNoticeType();
	track1.setOrderNumber(request.getParameter("ordernumber1"));
	track1.setShipmentNumber(request.getParameter("shipmentnumber1"));
	track1.setTrackpoint(request.getParameter("trackpoint1"));
	track1.setArrivalTime(XMLGregorianCalendarImpl.parse(request.getParameter("arrivaltime1")+":00"));
	track1.setDepartureTime(XMLGregorianCalendarImpl.parse(request.getParameter("departuretime1")+":00"));
	track1.setNotes(request.getParameter("notes1"));

	TrackpointNoticesType tnt = new TrackpointNoticesType();
	String[] ordernumber = request.getParameterValues("ordernumber");
	String[] shipmentnumber = request.getParameterValues("shipmentnumber");
	String[] trackpoint = request.getParameterValues("trackpointN");
	String[] arrivaltime = request.getParameterValues("arrivaltime");
	String[] departuretime = request.getParameterValues("departuretime");
	String[] notes = request.getParameterValues("notes");

	if(ordernumber != null){
		for(int x = 0; x<ordernumber.length; x++){
			TrackpointNoticeType track = new TrackpointNoticeType();
			track.setOrderNumber(ordernumber[x]);
			track.setShipmentNumber(shipmentnumber[x]);
			track.setTrackpoint(trackpoint[x]);
			track.setArrivalTime(XMLGregorianCalendarImpl.parse(arrivaltime[x]+":00"));
			track.setDepartureTime(XMLGregorianCalendarImpl.parse(departuretime[x]+":00"));
			track.setNotes(notes[x]);
			tnt.getTrackpointNotice().add(track);
		}
	}



	IssueTrackpointNoticeType itnt = new IssueTrackpointNoticeType();
	itnt.setTrackpointNotice(track1);
	itnt.setTrackpointNotices(tnt);

	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.issue_trackpoint_notice.ObjectFactory();

	JAXBElement itnElement = factory.createIssueTrackpointNotice(itnt);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.issue_trackpoint_notice" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(itnElement, xmlOS);//out to ByteArray


	if (request.getParameter("SaveToFile") != null){
		response.setHeader("Content-Disposition", "attachment;filename=\"IssueTrackpointNotice.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save") != null){
		// update the data list to be returned to the engine
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Issue_Trackpoint_Notice xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Issue_Trackpoint_Notice\"","<Issue_Trackpoint_Notice");
		xml = xml.replaceAll("</ns2:Issue_Trackpoint_Notice","</Issue_Trackpoint_Notice");

		String result = client.updateWorkItemData(itemid, xml, handle);

		// If you want the workitem to complete when it posts back, set this attribute to true;
		// if it's false or commented out, the workitem will update and remain on the worklist's 'started' queue
		//session.setAttribute("complete_on_post", true);

		session.removeAttribute("wir");

		// Now we can redirect back to the worklist
		String redirectURL = (String) session.getAttribute("source");
        session.removeAttribute("source");
        response.sendRedirect(response.encodeURL(redirectURL));
	}else if (request.getParameter("Complete") != null){
		// update the data list to be returned to the engine
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Issue_Trackpoint_Notice xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Issue_Trackpoint_Notice\"","<Issue_Trackpoint_Notice");
		xml = xml.replaceAll("</ns2:Issue_Trackpoint_Notice","</Issue_Trackpoint_Notice");

		String result = client.updateWorkItemData(itemid, xml, handle);

		// If you want the workitem to complete when it posts back, set this attribute to true;
		// if it's false or commented out, the workitem will update and remain on the worklist's 'started' queue
		//session.setAttribute("complete_on_post", true);

		session.removeAttribute("wir");

		// Now we can redirect back to the worklist
		//String redirectURL = "http://localhost:8080/resourceService/faces/userWorkQueues.jsp?complete=true";
		//response.sendRedirect(response.encodeURL(redirectURL));
		String redirectURL = (String) session.getAttribute("source") + "?complete=true";
        session.removeAttribute("source");
        response.sendRedirect(response.encodeURL(redirectURL));
	}

}
%>
</body>
</html>
