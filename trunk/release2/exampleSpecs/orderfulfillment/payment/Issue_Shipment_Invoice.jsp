<%@ page import="org.yawlfoundation.orderfulfilment.issue_shipment_invoice.*"%>
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
	xml = xml.replaceAll("<Issue_Shipment_Invoice", "<ns2:Issue_Shipment_Invoice xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Issue_Shipment_Invoice'");
	xml = xml.replaceAll("</Issue_Shipment_Invoice","</ns2:Issue_Shipment_Invoice");

%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<title>Issue Shipment Invoice</title>
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
            int endOfFile = result.indexOf("</ns2:Issue_Shipment_Invoice>");
            if(beginOfFile != -1 && endOfFile != -1) xml = result.substring(beginOfFile,endOfFile + 29);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.issue_shipment_invoice");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement isiElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	IssueShipmentInvoiceType isit1 = (IssueShipmentInvoiceType) isiElement1.getValue();
	ShipmentInvoiceType sit1 = isit1.getShipmentInvoice();
	CompanyType ct1 = null;
	if(!(sit1==null)) {
		ct1 = sit1.getCompany();
	}
%>

<div id="logo"></div>
<h2>Issue Shipment Invoice</h2>
<form id="IssueShipmentInvoice" name="IssueShipmentInvoice" method="post" novalidate="novalidate">

<fieldset>
	<legend>Shipment Details</legend>
	<div id="entry">
		<div id="label">Order Number</div>
		<div id="field"><input name="ordernumber" type="text" id="ordernumber" pattern="textValidation" <%if (sit1!=null) out.println("value='"+sit1.getOrderNumber()+"'");%> /></div>
	</div>
	<div id="entry">
		<div id="label">Shipment Number</div>
		<div id="field"><input name="shipmentnumber" type="text" id="shipmentnumber" pattern="textValidation" <%if (sit1!=null) out.println("value='"+sit1.getShipmentNumber()+"'");%>/></div>
	</div>
	<div id="entry">
		<div id="label">Shipment Cost</div>
		<div id="field"><input name="shipmentcost" type="text" id="shipmentcost" pattern="doubleValidation" <%if (sit1!=null) out.println("value='"+sit1.getShipmentCost()+"'");%>/></div>
	</div>
	<div id="entry"></div>
</fieldset>

<fieldset>
	<legend>Company Details</legend>
	<div id="entry">
		<div id="name">Name</div>
		<div id="name"><input type="text" name="companyname" id="companyname" pattern="textValidation" <%if (ct1!=null) out.println("value='"+ct1.getName()+"'");%> /> </div>
	</div>
	<div id="entry">
		<div id="address">Address</div>
		<div id="address"><input type="text" name="companyaddress" id="companyaddress" pattern="textValidation" <%if (ct1!=null) out.println("value='"+ct1.getAddress()+"'");%> /> </div>
	</div>
	<div id="entry">
		<div id="city">City</div>
		<div id="state">State</div>
		<div id="postcode">Postcode</div>
	</div>
	<div id="entry">
		<div id="city"><input type="text" name="companycity" id="companycity" pattern="textValidation" <%if (ct1!=null) out.println("value='"+ct1.getCity()+"'");%> /> </div>
		<div id="state"><input type="text" name="companystate" id="companystate" pattern="textValidation" <%if (ct1!=null) out.println("value='"+ct1.getState()+"'");%> /> </div>
		<div id="postcode"><input type="text" name="companypostcode" id="companypostcode" pattern="textValidation" <%if (ct1!=null) out.println("value='"+ct1.getPostCode()+"'");%> /> </div>
	</div>
	<div id="entry">
		<div id="phone">Phone</div>
		<div id="fax">Fax</div>
		<div id="businessnumber">Business Number</div>
	</div>
	<div id="entry">
		<div id="phone"><input type="text" name="companyphone" id="companyphone" pattern="phoneValidation" <%if (ct1!=null) out.println("value='"+ct1.getPhone()+"'");%> /> </div>
		<div id="fax"><input type="text" name="companyfax" id="companyfax" pattern="textValidation" <%if (ct1!=null) out.println("value='"+ct1.getFax()+"'");%> /> </div>
		<div id="businessnumber"><input type="text" name="companybusinessnumber" id="companybusinessnumber" pattern="phoneValidation" <%if (ct1!=null) out.println("value='"+ct1.getBusinessNumber()+"'");%> /> </div>
	</div>
	<div id="entry"></div>
</fieldset>

<div id="entry">&nbsp;</div>

<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save"/>
	<input type="submit" name="SaveToFile" value="Save To File" onclick="return validateForm('IssueShipmentInvoice');"  />
	&nbsp;
	<input type="submit" name="Complete" value="Complete" onClick="return validateForm('IssueShipmentInvoice');"/>

</div>

</form>

<!-- LOAD -->
<form method="post" action="Issue_Shipment_Invoice.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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

	CompanyType ct = new CompanyType();
	ct.setName(request.getParameter("companyname"));
	ct.setAddress(request.getParameter("companyaddress"));
	ct.setCity(request.getParameter("companycity"));
	ct.setState(request.getParameter("companystate"));
	ct.setPostCode(request.getParameter("companypostcode"));
	ct.setPhone(request.getParameter("companyphone"));
	ct.setFax(request.getParameter("companyfax"));
	ct.setBusinessNumber(request.getParameter("companybusinessnumber"));

	ShipmentInvoiceType sit = new ShipmentInvoiceType();
	sit.setOrderNumber(request.getParameter("ordernumber"));
	sit.setShipmentNumber(request.getParameter("shipmentnumber"));
	sit.setCompany(ct);
	sit.setShipmentCost(Double.parseDouble(request.getParameter("shipmentcost")));

	IssueShipmentInvoiceType isit = new IssueShipmentInvoiceType();
	isit.setShipmentInvoice(sit);

	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.issue_shipment_invoice.ObjectFactory();

	JAXBElement isiElement = factory.createIssueShipmentInvoice(isit);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.issue_shipment_invoice" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(isiElement, xmlOS);//out to ByteArray

	if (request.getParameter("SaveToFile") != null){
		response.setHeader("Content-Disposition", "attachment;filename=\"IssueShipmentInvoice.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save") != null){
		// update the data list to be returned to the engine
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Issue_Shipment_Invoice xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Issue_Shipment_Invoice\"","<Issue_Shipment_Invoice");
		xml = xml.replaceAll("</ns2:Issue_Shipment_Invoice","</Issue_Shipment_Invoice");

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
		xml = xml.replaceAll("<ns2:Issue_Shipment_Invoice xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Issue_Shipment_Invoice\"","<Issue_Shipment_Invoice");
		xml = xml.replaceAll("</ns2:Issue_Shipment_Invoice","</Issue_Shipment_Invoice");

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
