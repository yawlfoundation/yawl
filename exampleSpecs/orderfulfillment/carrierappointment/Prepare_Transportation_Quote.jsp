<%@ page import="org.yawlfoundation.orderfulfilment.prepare_transportation_quote.*"%>
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
	xml = xml.replaceAll("<Prepare_Transportation_Quote", "<ns2:Prepare_Transportation_Quote xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Prepare_Transportation_Quote'");
	xml = xml.replaceAll("</Prepare_Transportation_Quote","</ns2:Prepare_Transportation_Quote");

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<script language="JavaScript" src="../calendar_eu.js"></script>
<link rel="stylesheet" href="../calendar.css">
<title>Prepare Transportation Quote</title>
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
            int endOfFile = result.indexOf("</ns2:Prepare_Transportation_Quote>");
            if(beginOfFile != -1 && endOfFile != -1)xml = result.substring(beginOfFile,endOfFile + 35);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.prepare_transportation_quote");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement ptqElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	PrepareTransportationQuoteType ptqt1 = (PrepareTransportationQuoteType) ptqElement1.getValue();
	TransportationQuoteType tqt1 = ptqt1.getTransportationQuote();
%>
<div id="logo"></div>
<h2>Prepare Transportation Quote </h2>
<form id="PrepareTransportationQuote" name="PrepareTransportationQuote" method="post" novalidate="novalidate">

<fieldset>
	<legend>Transportation Quote</legend>
	<div id="entry">
		<div id="label">Order Number </div>
		<div id="field"><input type="text" name="ordernumber" id="ordernumber" pattern="textValidation" <%if (tqt1.getOrderNumber()!=null) out.println("value='"+tqt1.getOrderNumber()+"'");%>/> </div>
	</div>
	<div id="entry">
		<div id="label">Number of Packages </div>
		<div id="field"><input type="text" name="packages" id="packages" pattern="onetypeValidation" <%if (tqt1.getNumberOfPackages()!=null) out.println("value='"+tqt1.getNumberOfPackages()+"'");%>/> </div>
	</div>
	<div id="entry">
		<div id="label">Total Volume</div>
		<div id="field"><input name="totalvolume" type="text" id="totalvolume" pattern="zerotypeValidation" <%if (tqt1!=null) out.println("value='"+tqt1.getTotalVolume()+"'");%>/></div>
	</div>
	<div id="entry">
		<div id="label">Shipment Number</div>
		<div id="field"><input name="shipmentnumber" type="text" id="shipmentnumber" pattern="textValidation" <%if (tqt1!=null) out.println("value='"+tqt1.getShipmentNumber()+"'");%>/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Shipment Cost</div>
		<div id="field"><input name="shipmentcost" type="text" id="shipmentcost" pattern="doubleValidation" <%if (tqt1!=null) out.println("value='"+tqt1.getShipmentCost()+"'");%>/>
		</div>
	</div>
	<div id="entry">&nbsp;</div>
</fieldset>

<div id="entry">&nbsp;</div>
<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save" >
	<input type="submit" name="SaveToFile" value="Save To File" onClick="return validateForm('PrepareTransportationQuote');"/>
	&nbsp;
	<input type="submit" name="Complete" value="Complete" onClick="return validateForm('PrepareTransportationQuote');"/>

</div>

</form>

<!-- LOAD -->
    <form method="post" action="Prepare_Transportation_Quote.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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

	TransportationQuoteType tqt = new TransportationQuoteType();
	tqt.setOrderNumber(request.getParameter("ordernumber"));
	tqt.setNumberOfPackages(new BigInteger(request.getParameter("packages")));
	tqt.setTotalVolume(new BigInteger(request.getParameter("totalvolume")));
	tqt.setShipmentNumber(request.getParameter("shipmentnumber"));
	tqt.setShipmentCost(Double.parseDouble(request.getParameter("shipmentcost")));

	PrepareTransportationQuoteType ptqt = new PrepareTransportationQuoteType();
	ptqt.setTransportationQuote(tqt);

	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.prepare_transportation_quote.ObjectFactory();

	JAXBElement ptqElement = factory.createPrepareTransportationQuote(ptqt);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.prepare_transportation_quote" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(ptqElement, xmlOS);//out to ByteArray

	if(request.getParameter("SaveToFile")!=null){
		response.setHeader("Content-Disposition", "attachment;filename=\"PrepareTransportationQuote.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save")!=null){
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Prepare_Transportation_Quote xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Prepare_Transportation_Quote\"","<Prepare_Transportation_Quote");
		xml = xml.replaceAll("</ns2:Prepare_Transportation_Quote","</Prepare_Transportation_Quote");

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
		xml = xml.replaceAll("<ns2:Prepare_Transportation_Quote xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Prepare_Transportation_Quote\"","<Prepare_Transportation_Quote");
		xml = xml.replaceAll("</ns2:Prepare_Transportation_Quote","</Prepare_Transportation_Quote");

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
