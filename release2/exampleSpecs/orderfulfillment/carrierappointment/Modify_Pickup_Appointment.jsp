<%@ page import="org.yawlfoundation.orderfulfilment.modify_pickup_appointment.*"%>
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
	xml = xml.replaceAll("<Modify_Pickup_Appointment", "<ns2:Modify_Pickup_Appointment xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Modify_Pickup_Appointment'");
	xml = xml.replaceAll("</Modify_Pickup_Appointment","</ns2:Modify_Pickup_Appointment");
%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<script language="JavaScript" src="../calendar_eu.js"></script>
<link rel="stylesheet" href="../calendar.css">
<title>Modify Pickup Appointment</title>
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
            int endOfFile = result.indexOf("</ns2:Modify_Pickup_Appointment>");
            if(beginOfFile != -1 && endOfFile != -1)xml = result.substring(beginOfFile,endOfFile + 32);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.modify_pickup_appointment");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement mpaElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	ModifyPickupAppointmentType mpat1 = (ModifyPickupAppointmentType) mpaElement1.getValue();
	PickupInstructionsType pit1 = mpat1.getPickupInstructions();
%>
<div id="logo"></div>
<h2>Modify Pickup Appointment </h2>
<form id="ModifyPickupAppointment" name="ModifyPickupAppointment" method="post" novalidate="novalidate">

<fieldset>
	<div id="entry">
		<div id="label">Shipment Number</div>
		<div id="field"><input name="shipmentnumber" type="text" id="shipmentnumber" pattern="textValidation" <%if (pit1!=null) out.println("value='"+pit1.getShipmentNumber()+"'");%>/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Pickup Date</div>
		<div id="field"><input name="pickupdate" type="text" id="pickupdate" pattern="realdateValidation" <%if (pit1!=null) out.println("value='"+pit1.getPickupDate().getDay()+"-"+pit1.getPickupDate().getMonth()+"-"+pit1.getPickupDate().getYear()+"'");%>/>
		<script language="JavaScript">
			new tcal ({
				// form name
				'formname': 'ModifyPickupAppointment',
				// input name
				'controlname': 'pickupdate'
			});
		</script>
		</div>
	</div>
	<div id="entry">
		<div id="label">Pickup Instructions</div>
		<div id="field"><input name="pickupinstructions" type="text" id="pickupinstructions" pattern="textValidation" <%if (pit1!=null) out.println("value='"+pit1.getPickupInstructions()+"'");%>/></div>
	</div>
	<div id="entry">
		<div id="label">Pickup Spot</div>
		<div id="field"><input name="pickupspot" type="text" id="pickupspot" pattern="textValidation" <%if (pit1!=null) out.println("value='"+pit1.getPickupSpot()+"'");%>/>
		</div>
	</div>
	<div id="entry">&nbsp;</div>
</fieldset>

<div id="entry">&nbsp;</div>
<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save" >
	<input type="submit" name="SaveToFile" value="Save To File" onClick="return validateForm('ModifyPickupAppointment');"/>
	&nbsp;
	<input type="submit" name="Complete" value="Complete" onClick="return validateForm('ModifyPickupAppointment');"/>

</div>

</form>

<!-- LOAD -->
    <form method="post" action="Modify_Pickup_Appointment.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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

	java.text.SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
	java.text.SimpleDateFormat df2 = new SimpleDateFormat("dd-MM-yyyy");

	PickupInstructionsType pit = new PickupInstructionsType();
	pit.setShipmentNumber(request.getParameter("shipmentnumber"));
	pit.setPickupDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("pickupdate")))));
	pit.setPickupInstructions(request.getParameter("pickupinstructions"));
	pit.setPickupSpot(request.getParameter("pickupspot"));

	boolean truckload = false;
	if(request.getParameter("truckload") != null) truckload = true;

	ModifyPickupAppointmentType mpat = new ModifyPickupAppointmentType();
	mpat.setPickupInstructions(pit);


	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.modify_pickup_appointment.ObjectFactory();

	JAXBElement mpaElement = factory.createModifyPickupAppointment(mpat);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.modify_pickup_appointment" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(mpaElement, xmlOS);//out to ByteArray

	if(request.getParameter("SaveToFile")!=null){
		response.setHeader("Content-Disposition", "attachment;filename=\"ModifyPickupAppointment.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save")!=null){
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Modify_Pickup_Appointment xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Modify_Pickup_Appointment\"","<Modify_Pickup_Appointment");
		xml = xml.replaceAll("</ns2:Modify_Pickup_Appointment","</Modify_Pickup_Appointment");

		String result = client.updateWorkItemData(itemid, xml, handle);

		session.removeAttribute("wir");

		// Now we can redirect back to the worklist
		//String redirectURL = "http://localhost:8080/resourceService/faces/userWorkQueues.jsp";
		//response.sendRedirect(response.encodeURL(redirectURL));
		String redirectURL = (String) session.getAttribute("source");
        session.removeAttribute("source");
        response.sendRedirect(response.encodeURL(redirectURL));

	}else if (request.getParameter("Complete")!=null){
		// update the data list to be returned to the engine
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Modify_Pickup_Appointment xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Modify_Pickup_Appointment\"","<Modify_Pickup_Appointment");
		xml = xml.replaceAll("</ns2:Modify_Pickup_Appointment","</Modify_Pickup_Appointment");

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
