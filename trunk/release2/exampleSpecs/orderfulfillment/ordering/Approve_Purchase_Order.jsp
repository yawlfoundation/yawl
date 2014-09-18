<%@ page import="org.yawlfoundation.orderfulfilment.approve_purchase_order.*"%>
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
	xml = xml.replaceAll("<Approve_Purchase_Order", "<ns2:Approve_Purchase_Order xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Approve_Purchase_Order'");
	xml = xml.replaceAll("</Approve_Purchase_Order","</ns2:Approve_Purchase_Order");

%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<title>Approve Purchase Order</title>
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
            int endOfFile = result.indexOf("</ns2:Approve_Purchase_Order>");
            if(beginOfFile != -1 && endOfFile != -1) xml = result.substring(beginOfFile,endOfFile + 30);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.approve_purchase_order");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement apoElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	ApprovePurchaseOrderType apot1 = (ApprovePurchaseOrderType) apoElement1.getValue();
	PurchaseOrderType pot1 = apot1.getPOrder();
	CompanyType ct1 = null;
	OrderType ot1 = null;
	if(!(pot1==null)) {
		ct1 = pot1.getCompany();
		ot1 = pot1.getOrder();
	}
%>

<div id="logo"></div>
<h2>Approve Purchase Order</h2>
<form id="ApprovePurchaseOrder" name="ApprovePurchaseOrder" method="post" novalidate="novalidate">

<fieldset>
	<legend>Approval</legend>
	<div id="label">Approved</div>
	<div id="field"><input type="checkbox" name="approval" id="approval" value="true" <% if (apot1.isPOApproval()) out.println("checked");%>/></div>
	<div id="entry"></div>
</fieldset>


<fieldset>
	<legend>Company Details</legend>
	<div id="entry">
		<div id="name">Name</div>
		<div id="name"><input type="text" name="companyname" id="companyname"  <%if (ct1!=null) out.println("value='"+ct1.getName()+"'");%> class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry">
		<div id="address">Address</div>
		<div id="address"><input type="text" name="companyaddress" id="companyaddress" <%if (ct1!=null) out.println("value='"+ct1.getAddress()+"'");%> class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry">
		<div id="city">City</div>
		<div id="state">State</div>
		<div id="postcode">Postcode</div>
	</div>
	<div id="entry">
		<div id="city"><input type="text" name="companycity" id="companycity" <%if (ct1!=null) out.println("value='"+ct1.getCity()+"'");%> class="InputReadOnly" readonly/>
		</div>
		<div id="state"><input type="text" name="companystate" id="companystate" <%if (ct1!=null) out.println("value='"+ct1.getState()+"'");%> class="InputReadOnly" readonly/>
		</div>
		<div id="postcode"><input type="text" name="companypostcode" id="companypostcode" <%if (ct1!=null) out.println("value='"+ct1.getPostCode()+"'");%> class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry">
		<div id="phone">Phone</div>
		<div id="fax">Fax</div>
		<div id="businessnumber">Business Number</div>
	</div>
	<div id="entry">
		<div id="phone"><input type="text" name="companyphone" id="companyphone" <%if (ct1!=null) out.println("value='"+ct1.getPhone()+"'");%> class="InputReadOnly" readonly/>
		</div>
		<div id="fax"><input type="text" name="companyfax" id="companyfax" <%if (ct1!=null) out.println("value='"+ct1.getFax()+"'");%> class="InputReadOnly" readonly/>
		</div>
		<div id="businessnumber"><input type="text" name="companybusinessnumber" id="companybusinessnumber" <%if (ct1!=null) out.println("value='"+ct1.getBusinessNumber()+"'");%> class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry"></div>
</fieldset>

<fieldset>
	<legend>Order Details</legend>
	<div id="entry">
		<div id="label">Order Number</div>
		<div id="field"><input name="ordernumber" type="text" id="ordernumber" <%if (ot1!=null) out.println("value='"+ot1.getOrderNumber()+"'");%> class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Order Date</div>
		<div id="field"><input name="orderdate" type="text" id="orderdate" <%if (ot1!=null) out.println("value='"+ot1.getOrderDate().getDay()+"-"+ot1.getOrderDate().getMonth()+"-"+ot1.getOrderDate().getYear()+"'");%> class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Currency</div>
		<div id="field"><input name="ordercurrency" type="text" id="ordercurrency" <%if (ot1!=null) out.println("value='"+ot1.getCurrency()+"'");%>  class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Order Terms</div>
		<div id="field"><input name="orderterms" type="text" id="orderterms" <%if (ot1!=null) out.println("value='"+ot1.getOrderTerms()+"'");%> class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Revision Number</div>
		<div id="field"><input name="orderrevisionnumber" type="text" id="orderrevisionnumber" <%if (ot1!=null) out.println("value='"+ot1.getRevisionNumber()+"'");%> class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Remarks</div>
		<div id="field"><input name="orderremarks" type="text" id="orderremarks" <%if (ot1!=null) out.println("value='"+ot1.getRemarks()+"'");%> class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry">&nbsp;</div>

	<div id="orderLines">
		<div id="orderLineNumber">Line Number</div>
		<div id="orderUnitCode">Unit Code</div>
		<div id="orderUnitDescription">Unit Description</div>
		<div id="orderUnitQuantity">Unit Quantity</div>
		<div id="orderAction">Action</div>

		<div id="allLines">
			<%
			if(ot1!=null){
				OrderLinesType olt1 = ot1.getOrderLines();
				for(LineType lt1: olt1.getLine()){
					out.println("<div id='entry'>");

					out.println("<div id='orderLineNumber'><input type='text' name='linenumber' id='linenumber' value='"+lt1.getLineNumber()+"' class=InputReadOnly readonly/></div>");
					out.println("<div id='orderUnitCode'><input type='text' name='unitcode' id='unitcode' value='"+lt1.getUnitCode()+"' class='InputReadOnly' readonly/></div>");
					out.println("<div id='orderUnitDescription'><textarea name='unitdescription' id='unitdescription' pattern='' title='Enter Unit Description [String]' class='InputReadOnly' readonly>"+lt1.getUnitDescription()+"</textarea></div>");
					out.println("<div id='orderUnitQuantity'><input type='text' name='unitquantity' id='unitquantity' value='"+lt1.getUnitQuantity()+"' class='InputReadOnly' readonly/></div>");
					out.println("<div id='orderAction'><input type='text' name='action' id='action' value='"+lt1.getAction()+"' class='InputReadOnly' readonly/></div>");
					out.println("</div>");
				}
			}%>
		</div>
		<div id="entry"></div>
	</div>

</fieldset>

<fieldset>
	<legend>Purchase Details</legend>
	<div id="entry">
		<div id="label">Freight Cost</div>
		<div id="field"><input name="freightcost" type="text" id="freightcost" <% if (pot1 != null) out.println("value='"+pot1.getFreightCost()+"'");%> class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Delivery Location</div>
		<div id="field"><input name="deliverylocation" type="text" id="deliverylocation" <% if (pot1 != null) out.println("value='"+pot1.getDeliveryLocation()+"'");%> class="InputReadOnly" readonly/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Invoice Required</div>
		<div id="field"><input type="checkbox" name="invoicerequired" id="invoicerequired" value="true" <% if (pot1 != null && pot1.isInvoiceRequired() == true) out.println("checked");%> disabled/>
		</div>
	</div>
	<div id="entry">
		<div id="label">Pre-Paid</div>
		<div id="field"><input type="checkbox" name="prepaid" id="prepaid" value="true" <% if (pot1 != null && pot1.isPrePaid() == true) out.println("checked");%> disabled/>
		</div>
	</div>
	<div id="entry"></div>
</fieldset>

<div id="entry">&nbsp;</div>

<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save" >
	<input type="submit" name="SaveToFile" value="Save To File" />
	&nbsp;
	<input type="submit" name="Complete" value="Complete"/>

</div>

</form>

<!-- LOAD -->
<form method="post" action="Approve_Purchase_Order.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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
	OrderLinesType olt = new OrderLinesType();
	String[] linenumber = request.getParameterValues("linenumber");
	String[] unitcode = request.getParameterValues("unitcode");
	String[] unitdescription = request.getParameterValues("unitdescription");
	String[] unitquantity = request.getParameterValues("unitquantity");
	String[] action = request.getParameterValues("action");

	for(int x = 0; x<linenumber.length; x++){
		LineType lt = new LineType();
		lt.setLineNumber(new BigInteger(linenumber[x]));
		lt.setUnitCode(unitcode[x]);
		lt.setUnitDescription(unitdescription[x]);
		lt.setUnitQuantity(new BigInteger(unitquantity[x]));
		lt.setAction(action[x]);
		olt.getLine().add(lt);
	}

	java.text.SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
	java.text.SimpleDateFormat df2 = new SimpleDateFormat("dd-MM-yyyy");

	OrderType ot = new OrderType();
	ot.setOrderNumber(request.getParameter("ordernumber"));
	ot.setOrderDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("orderdate")))));
	ot.setCurrency(request.getParameter("ordercurrency"));
	ot.setOrderTerms(request.getParameter("orderterms"));
	ot.setRevisionNumber(new BigInteger(request.getParameter("orderrevisionnumber")));
	ot.setRemarks(request.getParameter("orderremarks"));
	ot.setOrderLines(olt);

	CompanyType ct = new CompanyType();
	ct.setName(request.getParameter("companyname"));
	ct.setAddress(request.getParameter("companyaddress"));
	ct.setCity(request.getParameter("companycity"));
	ct.setState(request.getParameter("companystate"));
	ct.setPostCode(request.getParameter("companypostcode"));
	ct.setPhone(request.getParameter("companyphone"));
	ct.setFax(request.getParameter("companyfax"));
	ct.setBusinessNumber(request.getParameter("companybusinessnumber"));


	boolean invoicerequired = pot1.isInvoiceRequired();

	boolean prepaid = pot1.isPrePaid();

	PurchaseOrderType pot = new PurchaseOrderType();
	pot.setCompany(ct);
	pot.setOrder(ot);
	pot.setFreightCost(Double.parseDouble(request.getParameter("freightcost")));
	pot.setDeliveryLocation(request.getParameter("deliverylocation"));
	pot.setInvoiceRequired(invoicerequired);
	pot.setPrePaid(prepaid);

	boolean approval = false;
	if(request.getParameter("approval") != null) approval = true;

	ApprovePurchaseOrderType apot = new ApprovePurchaseOrderType();
	apot.setPOApproval(approval);
	apot.setPOrder(pot);

	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.approve_purchase_order.ObjectFactory();

	JAXBElement apoElement = factory.createApprovePurchaseOrder(apot);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.approve_purchase_order" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(apoElement, xmlOS);//out to ByteArray


	if (request.getParameter("SaveToFile") != null){
		response.setHeader("Content-Disposition", "attachment;filename=\"ApprovePurchaseOrder.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save") != null){
		// update the data list to be returned to the engine
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Approve_Purchase_Order xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Approve_Purchase_Order\"","<Approve_Purchase_Order");
		xml = xml.replaceAll("</ns2:Approve_Purchase_Order","</Approve_Purchase_Order");

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
		xml = xml.replaceAll("<ns2:Approve_Purchase_Order xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Approve_Purchase_Order\"","<Approve_Purchase_Order");
		xml = xml.replaceAll("</ns2:Approve_Purchase_Order","</Approve_Purchase_Order");

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
