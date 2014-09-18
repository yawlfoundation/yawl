<%@ page import="org.yawlfoundation.orderfulfilment.produce_shipment_notice.*"%>
<%@ page import="javax.xml.datatype.*" %>
<%@ page import="org.jdom2.Element" %>
<%@ page import="org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGatewayClient" %>
<%@ page import="org.yawlfoundation.yawl.util.*" %>
<%@ page import="com.sun.org.apache.xerces.internal.jaxp.datatype.*" %>
<%@ page import="java.math.*" %>
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
	xml = xml.replaceAll("<Produce_Shipment_Notice", "<ns2:Produce_Shipment_Notice xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Produce_Shipment_Notice'");
	xml = xml.replaceAll("</Produce_Shipment_Notice","</ns2:Produce_Shipment_Notice");

%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<script language="JavaScript" src="../calendar_eu.js"></script>
<link rel="stylesheet" href="../calendar.css">
<title>Produce Shipment Notice</title>
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
            int endOfFile = result.indexOf("</ns2:Produce_Shipment_Notice>");
            if(beginOfFile != -1 && endOfFile != -1)xml = result.substring(beginOfFile,endOfFile + 30);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.produce_shipment_notice");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement psnElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	ProduceShipmentNoticeType psnt1 = (ProduceShipmentNoticeType) psnElement1.getValue();
	ShipmentNoticeType snt1 = psnt1.getShipmentNotice();
	PickupInstructionsType pit1 = null;
	DeliveryInstructionsType dit1 = null;
	if(!(snt1 == null)){
		 pit1 = snt1.getPickupInstructions();
		 dit1 = snt1.getDeliveryInstructions();
	}
%>
<div id="logo"></div>
<h2>Produce Shipment Notice </h2>
<form id="ProduceShipmentNotice" name="ProduceShipmentNotice" method="post" novalidate="novalidate">

<fieldset>
	<legend>Shipment Notice</legend>
	<div id="entry">
		<div id="label">Shipment Number</div>
		<div id="field"><input name="shipmentnumber" type="text" id="shipmentnumber" pattern="textValidation" <%if (snt1!=null) out.println("value='"+snt1.getShipmentNumber()+"'");%>/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Order Number </div>
		<div id="field"><input type="text" name="ordernumber" id="ordernumber" pattern="textValidation" <%if (snt1.getOrderNumber()!=null) out.println("value='"+snt1.getOrderNumber()+"'");%> /> </div>
	</div>
	<div id="entry">
		<div id="label">Number of Packages </div>
		<div id="field"><input type="text" name="packages" id="packages" pattern="onetypeValidation" <%if (snt1.getNumberOfPackages()!=null) out.println("value='"+snt1.getNumberOfPackages()+"'");%>/> </div>
	</div>
	<div id="entry">
		<div id="label">Truckload</div>
	  <div id="field">
	    <input type="checkbox" name="truckload" id="truckload" value="true" <% if (snt1 != null && snt1.isTruckload() == true) out.println("checked");%>/>
	  </div>
	</div>
	<div id="entry">
		<div id="label">Start Load Time </div>
		<div id="field"><input type="text" name="startload" id="startload" pattern="timeValidation"
		<%if (snt1.getStartLoad()!=null){
			String start = snt1.getStartLoad().toString();
			start = start.substring(0, 5);
			out.println("value='"+start+"'");
		};%>
		/> </div>
	</div>
	<div id="entry">
		<div id="label">End Load Time </div>
		<div id="field"><input type="text" name="endload" id="endload" pattern="timeValidation"
		<%if (snt1.getEndLoad()!=null){
		 	String end = snt1.getEndLoad().toString();
			end = end.substring(0, 5);
			out.println("value='"+end+"'");
		}%>
		 /> </div>
	</div>
	<div id="entry">
		<div id="label">Claims Deadline </div>
		<div id="field">
		<input type="text" name="claimsdeadlineyears" id="claimsdeadlineyears" pattern="zerotypeValidation" <%if (snt1.getClaimsDeadline()!=null) out.println("value='"+snt1.getClaimsDeadline().getYears()+"'");%> /> years
		<input type="text" name="claimsdeadlinemonths" id="claimsdeadlinemonths" pattern="zerotypeValidation" <%if (snt1.getClaimsDeadline()!=null) out.println("value='"+snt1.getClaimsDeadline().getMonths()+"'");%> />  months
		<input type="text" name="claimsdeadlinedays" id="claimsdeadlinedays" pattern="zerotypeValidation" <%if (snt1.getClaimsDeadline()!=null) out.println("value='"+snt1.getClaimsDeadline().getDays()+"'");%> /> days
		<input type="text" name="claimsdeadlinehours" id="claimsdeadlinehours" pattern="zerotypeValidation" <%if (snt1.getClaimsDeadline()!=null) out.println("value='"+snt1.getClaimsDeadline().getHours()+"'");%> /> hours
		<input type="text" name="claimsdeadlineminutes" id="claimsdeadlineminutes" pattern="zerotypeValidation" <%if (snt1.getClaimsDeadline()!=null) out.println("value='"+snt1.getClaimsDeadline().getMinutes()+"'");%> /> minutes
		<input type="text" name="claimsdeadlineseconds" id="claimsdeadlineseconds" pattern="zerotypeValidation" <%if (snt1.getClaimsDeadline()!=null) out.println("value='"+snt1.getClaimsDeadline().getSeconds()+"'");%> /> seconds
		</div>
	</div>
	<div id="entry">
		<div id="label">Driver Number </div>
		<div id="field"><input type="text" name="drivernumber" id="drivernumber" pattern="textValidation" <%if (snt1.getDriverNumber()!=null) out.println("value='"+snt1.getDriverNumber()+"'");%> /> </div>
	</div>
	<div id="entry">
		<div id="label">Driver Name </div>
		<div id="field"><input type="text" name="drivername" id="drivername" pattern="textValidation" <%if (snt1.getDriverName()!=null) out.println("value='"+snt1.getDriverName()+"'");%>/> </div>
	</div>
	<div id="entry">&nbsp;</div>
</fieldset>

<fieldset>
	<legend>Pickup Instructions</legend>
	<div id="entry">
		<div id="label">Shipment Number</div>
		<div id="field"><input name="pickupshipmentnumber" type="text" id="pickupshipmentnumber" pattern="textValidation" <%if (pit1!=null) out.println("value='"+pit1.getShipmentNumber()+"'");%>/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Pickup Date</div>
		<div id="field"><input name="pickupdate" type="text" id="pickupdate" pattern="realdateValidation" <%if (pit1!=null) out.println("value='"+pit1.getPickupDate().getDay()+"-"+pit1.getPickupDate().getMonth()+"-"+pit1.getPickupDate().getYear()+"'");%>/>
		<script language="JavaScript">
			new tcal ({
				// form name
				'formname': 'ProduceShipmentNotice',
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

<fieldset>
	<legend>Delivery Instructions</legend>
	<div id="entry">
		<div id="label">Shipment Number</div>
		<div id="field"><input name="deliveryshipmentnumber" type="text" id="deliveryshipmentnumber" pattern="textValidation" <%if (dit1!=null) out.println("value='"+dit1.getShipmentNumber()+"'");%>/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Delivery Date</div>
		<div id="field"><input name="deliverydate" type="text" id="deliverydate" pattern="realdateValidation" <%if (dit1!=null) out.println("value='"+dit1.getDeliveryDate().getDay()+"-"+dit1.getDeliveryDate().getMonth()+"-"+dit1.getDeliveryDate().getYear()+"'");%>/>
		<script language="JavaScript">
			new tcal ({
				// form name
				'formname': 'ProduceShipmentNotice',
				// input name
				'controlname': 'deliverydate'
			});
		</script>
		</div>
	</div>
	<div id="entry">
		<div id="label">Delivery Instructions</div>
		<div id="field"><input name="deliveryinstructions" type="text" id="deliveryinstructions" pattern="textValidation" <%if (dit1!=null) out.println("value='"+dit1.getDeliveryInstructions()+"'");%>/></div>
	</div>
	<div id="entry">
		<div id="label">Delivery Location</div>
		<div id="field"><input name="deliverylocation" type="text" id="deliverylocation" pattern="textValidation" <%if (dit1!=null) out.println("value='"+dit1.getDeliveryLocation()+"'");%>/>
		</div>
	</div>
	<div id="entry">&nbsp;</div>
</fieldset>

<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save" >
	<input type="submit" name="SaveToFile" value="Save To File" onClick="return validateForm('ProduceShipmentNotice');"/>
	&nbsp;
	<input type="submit" name="Complete" value="Complete" onClick="return validateForm('ProduceShipmentNotice');"/>

</div>

</form>

<!-- LOAD -->
    <form method="post" action="Produce_Shipment_Notice.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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


	DeliveryInstructionsType dit = new DeliveryInstructionsType();
	dit.setShipmentNumber(request.getParameter("deliveryshipmentnumber"));
	dit.setDeliveryDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("deliverydate")))));
	dit.setDeliveryInstructions(request.getParameter("deliveryinstructions"));
	dit.setDeliveryLocation(request.getParameter("deliverylocation"));

	PickupInstructionsType pit = new PickupInstructionsType();
	pit.setShipmentNumber(request.getParameter("pickupshipmentnumber"));
	pit.setPickupDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("pickupdate")))));
	pit.setPickupInstructions(request.getParameter("pickupinstructions"));
	pit.setPickupSpot(request.getParameter("pickupspot"));

	boolean truckload = false;
	if(request.getParameter("truckload") != null) truckload = true;

	int years = Integer.valueOf(request.getParameter("claimsdeadlineyears")).intValue();
	int months = Integer.valueOf(request.getParameter("claimsdeadlinemonths")).intValue();
	int days = Integer.valueOf(request.getParameter("claimsdeadlinedays")).intValue();
	int hours = Integer.valueOf(request.getParameter("claimsdeadlinehours")).intValue();
	int minutes = Integer.valueOf(request.getParameter("claimsdeadlineminutes")).intValue();
	int seconds = Integer.valueOf(request.getParameter("claimsdeadlineseconds")).intValue();

	//String start = request.getParameter("startload");
	//XMLGregorianCalendar startXML = new XMLGregorianCalendar();
	//startXML.setHour(start.substring(0,2));
	//startXML.setMinute(start.substring(4,6));

	ShipmentNoticeType snt = new ShipmentNoticeType();
	snt.setShipmentNumber(request.getParameter("shipmentnumber"));
	snt.setOrderNumber(request.getParameter("ordernumber"));
	snt.setNumberOfPackages(new BigInteger(request.getParameter("packages")));
	snt.setTruckload(truckload);
	snt.setPickupInstructions(pit);
	snt.setStartLoad(XMLGregorianCalendarImpl.parse(request.getParameter("startload")+":00"));
	snt.setEndLoad(XMLGregorianCalendarImpl.parse(request.getParameter("endload")+":00"));
	snt.setDeliveryInstructions(dit);
	snt.setClaimsDeadline(DatatypeFactoryImpl.newInstance().newDuration(true, years, months, days, hours, minutes, seconds));
	snt.setDriverNumber(request.getParameter("drivernumber"));
	snt.setDriverName(request.getParameter("drivername"));


	ProduceShipmentNoticeType psnt = new ProduceShipmentNoticeType();
	psnt.setShipmentNotice(snt);

	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.produce_shipment_notice.ObjectFactory();

	JAXBElement psnElement = factory.createProduceShipmentNotice(psnt);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.produce_shipment_notice" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(psnElement, xmlOS);//out to ByteArray

	if(request.getParameter("SaveToFile")!=null){
		response.setHeader("Content-Disposition", "attachment;filename=\"ProduceShipmentNotice.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save")!=null){
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Produce_Shipment_Notice xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Produce_Shipment_Notice\"","<Produce_Shipment_Notice");
		xml = xml.replaceAll("</ns2:Produce_Shipment_Notice","</Produce_Shipment_Notice");

		String result = client.updateWorkItemData(itemid, xml, handle);
		session.removeAttribute("wir");

		// Now we can redirect back to the worklist
		String redirectURL = (String) session.getAttribute("source");
        session.removeAttribute("source");
        response.sendRedirect(response.encodeURL(redirectURL));

	}else if (request.getParameter("Complete")!=null){
		// update the data list to be returned to the engine
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Produce_Shipment_Notice xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Produce_Shipment_Notice\"","<Produce_Shipment_Notice");
		xml = xml.replaceAll("</ns2:Produce_Shipment_Notice","</Produce_Shipment_Notice");

		String result = client.updateWorkItemData(itemid, xml, handle);
		session.removeAttribute("wir");

		// Now we can redirect back to the worklist
		String redirectURL = (String) session.getAttribute("source") + "?complete=true";
        session.removeAttribute("source");
        response.sendRedirect(response.encodeURL(redirectURL));

	}
}
%>
</body>
</html>
