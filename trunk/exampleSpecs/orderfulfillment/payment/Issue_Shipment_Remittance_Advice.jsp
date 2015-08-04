<%@ page import="org.yawlfoundation.orderfulfilment.issue_shipment_remittance_advice.*"%>
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
	xml = xml.replaceAll("<Issue_Shipment_Remittance_Advice", "<ns2:Issue_Shipment_Remittance_Advice xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Issue_Shipment_Remittance_Advice'");
	xml = xml.replaceAll("</Issue_Shipment_Remittance_Advice","</ns2:Issue_Shipment_Remittance_Advice");

%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<title>Issue Shipment Remittance Advice</title>
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
            int endOfFile = result.indexOf("</ns2:Issue_Shipment_Remittance_Advice>");
            if(beginOfFile != -1 && endOfFile != -1) xml = result.substring(beginOfFile,endOfFile + 39);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.issue_shipment_remittance_advice");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement israElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	IssueShipmentRemittanceAdviceType israt1 = (IssueShipmentRemittanceAdviceType) israElement1.getValue();
	ShipmentRemittanceAdviceType srat1 = israt1.getShipmentRemittanceAdvice();
%>

<div id="logo"></div>
<h2>Issue Shipment Remittance Advice</h2>
<form id="IssueShipmentRemittanceAdvice" name="IssueShipmentRemittanceAdvice" method="post" novalidate="novalidate">

<fieldset>
	<legend>Shipment Remittance Advice </legend>
	<div id="entry">
		<div id="label">Order Number</div>
		<div id="field"><input name="ordernumber" type="text" id="ordernumber" pattern="textValidation" <%if (srat1!=null) out.println("value='"+srat1.getOrderNumber()+"'");%> /></div>
	</div>
	<div id="entry">
		<div id="label">Amount Debited </div>
		<div id="field"><input name="amountdebited" type="text" id="amountdebited" pattern="doubleValidation" <%if (srat1!=null) out.println("value='"+srat1.getAmountDebited()+"'");%>/></div>
	</div>
	<div id="entry"></div>
</fieldset>

<div id="entry">&nbsp;</div>

<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save"/>
	<input type="submit" name="SaveToFile" onclick="return validateForm('IssueShipmentRemittanceAdvice');" value="Save To File" />
	&nbsp;
	<input type="submit" name="Complete" value="Complete" onClick="return validateForm('IssueShipmentRemittanceAdvice');"/>

</div>

</form>

<!-- LOAD -->
<form method="post" action="Issue_Shipment_Remittance_Advice.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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



	ShipmentRemittanceAdviceType srat = new ShipmentRemittanceAdviceType();
	srat.setOrderNumber(request.getParameter("ordernumber"));
	srat.setAmountDebited(Double.parseDouble(request.getParameter("amountdebited")));

	IssueShipmentRemittanceAdviceType israt = new IssueShipmentRemittanceAdviceType();
	israt.setShipmentRemittanceAdvice(srat);

	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.issue_shipment_remittance_advice.ObjectFactory();

	JAXBElement israElement = factory.createIssueShipmentRemittanceAdvice(israt);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.issue_shipment_remittance_advice" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(israElement, xmlOS);//out to ByteArray

	if (request.getParameter("SaveToFile") != null){
		response.setHeader("Content-Disposition", "attachment;filename=\"IssueShipmentRemittanceAdvice.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save") != null){
		// update the data list to be returned to the engine
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Issue_Shipment_Remittance_Advice xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Issue_Shipment_Remittance_Advice\"","<Issue_Shipment_Remittance_Advice");
		xml = xml.replaceAll("</ns2:Issue_Shipment_Remittance_Advice","</Issue_Shipment_Remittance_Advice");

		String result = client.updateWorkItemData(itemid, xml, handle);

		// If you want the workitem to complete when it posts back, set this attribute to true;
		// if it's false or commented out, the workitem will update and remain on the worklist's 'started' queue
		//session.setAttribute("complete_on_post", true);

		session.removeAttribute("wir");

		// Now we can redirect back to the worklist
		//String redirectURL = "http://localhost:8080/resourceService/faces/userWorkQueues.jsp";
		//response.sendRedirect(response.encodeURL(redirectURL));
		String redirectURL = (String) session.getAttribute("source");
        session.removeAttribute("source");
        response.sendRedirect(response.encodeURL(redirectURL));
	}else if (request.getParameter("Complete") != null){
		// update the data list to be returned to the engine
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Issue_Shipment_Remittance_Advice xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Issue_Shipment_Remittance_Advice\"","<Issue_Shipment_Remittance_Advice");
		xml = xml.replaceAll("</ns2:Issue_Shipment_Remittance_Advice","</Issue_Shipment_Remittance_Advice");

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
