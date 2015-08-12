<%@ page import="org.yawlfoundation.orderfulfilment.log_trackpoint_order_entry.*"%>
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
	xml = xml.replaceAll("<Log_Trackpoint_Order_Entry", "<ns2:Log_Trackpoint_Order_Entry xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Log_Trackpoint_Order_Entry'");
	xml = xml.replaceAll("</Log_Trackpoint_Order_Entry","</ns2:Log_Trackpoint_Order_Entry");

%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<script language="JavaScript" src="../calendar_eu.js"></script>
<link rel="stylesheet" href="../calendar.css">
<title>Log Trackpoint Order Entry</title>
</head>

<body>

<%
	if (MultipartFormDataRequest.isMultipartFormData(request)) {
         MultipartFormDataRequest mrequest = new MultipartFormDataRequest(request);
         String todo = null;
		 StringBuffer result = new StringBuffer();

         if (mrequest != null)todo = mrequest.getParameter("todo");

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
            int endOfFile = result.indexOf("</ns2:Log_Trackpoint_Order_Entry>");
            if(beginOfFile != -1 && endOfFile != -1)xml = result.substring(beginOfFile,endOfFile + 33);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.log_trackpoint_order_entry");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement ltoeElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	LogTrackpointOrderEntryType ltoet1 = (LogTrackpointOrderEntryType) ltoeElement1.getValue();
	TrackpointNoticeType tnt1 = ltoet1.getTrackpointNotice();

%>

<div id="logo"></div>
<h2>Log Trackpoint Order Entry</h2>
<form id="LogTrackpointOrderEntry" name="LogTrackpointOrderEntry" method="post" novalidate="novalidate">

<fieldset>
	<legend>Shipment Status Inquiry</legend>
	<div id="entry">
		<div id="label">Order Number</div>
		<div id="field"><input name="ordernumber" type="text" id="ordernumber" pattern="textValidation" <%if (tnt1!=null) out.println("value='"+tnt1.getOrderNumber()+"'");%>/></div>
	</div>
	<div id="entry">
		<div id="label">Shipment Number</div>
		<div id="field"><input name="shipmentnumber" type="text" id="shipmentnumber" pattern="textValidation" <%if (tnt1!=null) out.println("value='"+tnt1.getShipmentNumber()+"'");%>/></div>
	</div>
	<div id="entry">
		<div id="label">Trackpoint</div>
		<div id="field"><input name="trackpoint1" type="text" id="trackpoin1t" pattern="textValidation" <%if (tnt1!=null) out.println("value='"+tnt1.getTrackpoint()+"'");%>/></div>
	</div>
	<div id="entry">
		<div id="label">Arrival Time</div>
		<div id="field"><input name="arrivaltime" type="text" id="arrivaltime" pattern="timeValidation"
		<%if (tnt1!=null){
			String arrivaltimestring = tnt1.getArrivalTime().toString();
			arrivaltimestring = arrivaltimestring.substring(0,5);
			out.println("value='"+arrivaltimestring+"'");
		}%>
		/></div>
	</div>
	<div id="entry">
		<div id="label">Departure Time</div>
		<div id="field"><input name="departuretime" type="text" id="departuretime" pattern="timeValidation"
		<%if (tnt1!=null){
			String departuretimestring = tnt1.getDepartureTime().toString();
			departuretimestring = departuretimestring.substring(0,5);
			out.println("value='"+departuretimestring+"'");
		}%>
		/></div>
	</div>
	<div id="entry">
		<div id="label">Notes</div>
		<div id="field"><input name="notes" type="text" id="notes" pattern="textValidation" <%if (tnt1!=null) out.println("value='"+tnt1.getNotes()+"'");%>/></div>
	</div>
	<div id="entry"></div>
</fieldset>

<div id="entry">&nbsp;</div>

<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save">
	<input type="submit" name="SaveToFile" onclick="return validateForm('LogTrackpointOrderEntry');" value="Save To File" />
	&nbsp;
	<input type="submit" name="Complete" value="Complete" onClick="return validateForm('LogTrackpointOrderEntry');" />

</div>

</form>

<!-- LOAD -->
<form method="post" action="Log_Trackpoint_Order_Entry.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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
	session.removeAttribute("wir");
	String redirectURL = (String) session.getAttribute("source");
    session.removeAttribute("source");
    response.sendRedirect(response.encodeURL(redirectURL));

}else if(request.getParameter("Save") != null || request.getParameter("SaveToFile")!=null || request.getParameter("Complete") != null){
	java.text.SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
	java.text.SimpleDateFormat df2 = new SimpleDateFormat("dd-MM-yyyy");

	TrackpointNoticeType tnt = new TrackpointNoticeType();
	tnt.setOrderNumber(request.getParameter("ordernumber"));
	tnt.setShipmentNumber(request.getParameter("shipmentnumber"));
	tnt.setTrackpoint(request.getParameter("trackpoint1"));
	tnt.setArrivalTime(XMLGregorianCalendarImpl.parse(request.getParameter("arrivaltime")+":00"));
	tnt.setDepartureTime(XMLGregorianCalendarImpl.parse(request.getParameter("departuretime")+":00"));
	tnt.setNotes(request.getParameter("notes"));

	LogTrackpointOrderEntryType ltoet = new LogTrackpointOrderEntryType();
	ltoet.setTrackpointNotice(tnt);
	ltoet.setReport("no report");

	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.log_trackpoint_order_entry.ObjectFactory();

	JAXBElement ltoeElement = factory.createLogTrackpointOrderEntry(ltoet);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.log_trackpoint_order_entry" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(ltoeElement, xmlOS);//out to ByteArray

	if(request.getParameter("SaveToFile")!=null){
		response.setHeader("Content-Disposition", "attachment;filename=\"LogTrackpointOrderEntry.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save")!=null){
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Log_Trackpoint_Order_Entry xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Log_Trackpoint_Order_Entry\"","<Log_Trackpoint_Order_Entry");
		xml = xml.replaceAll("</ns2:Log_Trackpoint_Order_Entry","</Log_Trackpoint_Order_Entry");

		String result = client.updateWorkItemData(itemid, xml, handle);

		session.removeAttribute("wir");

		// Now we can redirect back to the worklist
		String redirectURL = (String) session.getAttribute("source");
        session.removeAttribute("source");
        response.sendRedirect(response.encodeURL(redirectURL));

	}else if (request.getParameter("Complete")!=null){
		// update the data list to be returned to the engine
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Log_Trackpoint_Order_Entry xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Log_Trackpoint_Order_Entry\"","<Log_Trackpoint_Order_Entry");
		xml = xml.replaceAll("</ns2:Log_Trackpoint_Order_Entry","</Log_Trackpoint_Order_Entry");

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
