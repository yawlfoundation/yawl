<%@ page import="org.yawlfoundation.orderfulfilment.create_shipment_information_document.*"%>
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
	xml = xml.replaceAll("<Create_Shipment_Information_Document", "<ns2:Create_Shipment_Information_Document xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Create_Shipment_Information_Document'");
	xml = xml.replaceAll("</Create_Shipment_Information_Document","</ns2:Create_Shipment_Information_Document");

%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<script language="JavaScript" src="../calendar_eu.js"></script>
<link rel="stylesheet" href="../calendar.css">
<title>Create Shipment Information Document</title>
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
            int endOfFile = result.indexOf("</ns2:Create_Shipment_Information_Document>");
            if(beginOfFile != -1 && endOfFile != -1)xml = result.substring(beginOfFile,endOfFile + 43);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.create_shipment_information_document");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement csidElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	CreateShipmentInformationDocumentType csidt1 = (CreateShipmentInformationDocumentType) csidElement1.getValue();
	ShipmentInformationDocumentType sidt1 = csidt1.getShipmentInformationDocument();
%>
<div id="logo"></div>
<h2>Create Shipment Information Document </h2>
<form id="CreateShipmentInformationDocument" name="CreateShipmentInformationDocument" method="post" novalidate="novalidate">

<fieldset>
	<legend>Shipment Information</legend>
	<div id="entry">
		<div id="label">Shipment Number</div>
		<div id="field"><input name="shipmentnumber" type="text" id="shipmentnumber" pattern="textValidation" <%if (sidt1!=null) out.println("value='"+sidt1.getShipmentNumber()+"'");%>/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Order Number </div>
		<div id="field"><input type="text" name="ordernumber" id="ordernumber" pattern="textValidation" <%if (sidt1.getOrderNumber()!=null) out.println("value='"+sidt1.getOrderNumber()+"'");%>/> </div>
	</div>
	<div id="entry">
		<div id="label">Number of Packages </div>
		<div id="field"><input type="text" name="packages" id="packages" pattern="onetypeValidation" <%if (sidt1.getNumberOfPackages()!=null) out.println("value='"+sidt1.getNumberOfPackages()+"'");%>/> </div>
	</div>
	<div id="entry">
		<div id="label">Truckload</div>
	  <div id="field">
	    <input type="checkbox" name="truckload" id="truckload" value="true" <% if (sidt1 != null && sidt1.isTruckload() == true) out.println("checked");%>/>
	  </div>
	</div>
	<div id="entry">
		<div id="label">Authorization code </div>
		<div id="field"><input type="text" name="authorizationcode" id="authorizationcode" pattern="textValidation" <%if (sidt1.getAuthorizationCode()!=null) out.println("value='"+sidt1.getAuthorizationCode()+"'");%>/> </div>
	</div>
	<div id="entry">
		<div id="label">Consignee Number </div>
		<div id="field"><input type="text" name="consigneenumber" id="consigneenumber" pattern="zerotypeValidation" <%if (sidt1.getConsigneeNumber()!=null) out.println("value='"+sidt1.getConsigneeNumber()+"'");%>/> </div>
	</div>
	<div id="entry">&nbsp;</div>
</fieldset>

<div id="entry">&nbsp;</div>
<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save" >
	<input type="submit" name="SaveToFile" value="Save To File" onClick="return validateForm('CreateShipmentInformationDocument');"/>
	&nbsp;
	<input type="submit" name="Complete" value="Complete" onClick="return validateForm('CreateShipmentInformationDocument');"/>

</div>

</form>

<!-- LOAD -->
    <form method="post" action="Create_Shipment_Information_Document.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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

	boolean truckload = false;
	if(request.getParameter("truckload") != null) truckload = true;

	ShipmentInformationDocumentType sidt = new ShipmentInformationDocumentType();
	sidt.setShipmentNumber(request.getParameter("shipmentnumber"));
	sidt.setOrderNumber(request.getParameter("ordernumber"));
	sidt.setNumberOfPackages(new BigInteger(request.getParameter("packages")));
	sidt.setTruckload(truckload);
	sidt.setAuthorizationCode(request.getParameter("authorizationcode"));
	sidt.setConsigneeNumber(new BigInteger(request.getParameter("consigneenumber")));

	CreateShipmentInformationDocumentType csidt = new CreateShipmentInformationDocumentType();
	csidt.setShipmentInformationDocument(sidt);


	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.create_shipment_information_document.ObjectFactory();

	JAXBElement csidElement = factory.createCreateShipmentInformationDocument(csidt);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.create_shipment_information_document" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(csidElement, xmlOS);//out to ByteArray

	if(request.getParameter("SaveToFile")!=null){
		response.setHeader("Content-Disposition", "attachment;filename=\"CreateShipmentInformationDocument.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save")!=null){
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Create_Shipment_Information_Document xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Create_Shipment_Information_Document\"","<Create_Shipment_Information_Document");
		xml = xml.replaceAll("</ns2:Create_Shipment_Information_Document","</Create_Shipment_Information_Document");

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
		xml = xml.replaceAll("<ns2:Create_Shipment_Information_Document xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Create_Shipment_Information_Document\"","<Create_Shipment_Information_Document");
		xml = xml.replaceAll("</ns2:Create_Shipment_Information_Document","</Create_Shipment_Information_Document");

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
