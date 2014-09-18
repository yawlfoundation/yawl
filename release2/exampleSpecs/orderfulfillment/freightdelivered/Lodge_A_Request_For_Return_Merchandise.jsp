<%@ page import="org.yawlfoundation.orderfulfilment.lodge_a_request_for_return_merchandise.*"%>
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
	xml = xml.replaceAll("<Lodge_a_request_for_Return_Merchandise", "<ns2:Lodge_a_request_for_Return_Merchandise xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Lodge_a_request_for_Return_Merchandise'");
	xml = xml.replaceAll("</Lodge_a_request_for_Return_Merchandise","</ns2:Lodge_a_request_for_Return_Merchandise");
%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<script language="JavaScript" src="../calendar_eu.js"></script>
<link rel="stylesheet" href="../calendar.css">
<title>Lodge a Request for Return Merchandise</title>
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
            int endOfFile = result.indexOf("</ns2:Lodge_a_Request_for_Return_Merchandise>");
            if(beginOfFile != -1 && endOfFile != -1)xml = result.substring(beginOfFile,endOfFile + 45);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.lodge_a_request_for_return_merchandise");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement larfrmElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	LodgeARequestForReturnMerchandiseType larfrmt1 = (LodgeARequestForReturnMerchandiseType) larfrmElement1.getValue();
	ReturnMerchandiseType rmt1 = larfrmt1.getReturnMerchandise();
	CheckedOrderLinesType colt1 = null;
	if(!(rmt1 == null)){
		colt1 = rmt1.getCheckedOrderLines();
	}
%>

<div id="logo"></div>
<h2>Lodge a Request for Return Merchandise</h2>
<form id="LodgeARequestForReturnMerchandise" name="LodgeARequestForReturnMerchandise" method="post" novalidate="novalidate">

<fieldset>
	<legend>Return Merchandise</legend>
	<div id="entry">
		<div id="label">Order Number</div>
		<div id="field"><input name="ordernumber" type="text" id="ordernumber" pattern="textValidation" <%if (rmt1!=null) out.println("value='"+rmt1.getOrderNumber()+"'");%>/></div>
	</div>
	<div id="entry">
		<div id="label">Shipment Number</div>
		<div id="field"><input name="shipmentnumber" type="text" id="shipmentnumber" pattern="textValidation" <%if (rmt1!=null) out.println("value='"+rmt1.getShipmentNumber()+"'");%>/></div>
	</div>
	<div id="entry">
		<div id="label">Acceptance Date</div>
		<div id="field"><input name="acceptancedate" type="text" id="acceptancedate" pattern="realdateValidation" <%if (rmt1!=null) out.println("value='"+rmt1.getAcceptanceDate().getDay()+"-"+rmt1.getAcceptanceDate().getMonth()+"-"+rmt1.getAcceptanceDate().getYear()+"'");%>/>
		<script language="JavaScript">
			new tcal ({
				// form name
				'formname': 'LodgeLossOrDamageClaimReport',
				// input name
				'controlname': 'acceptancedate'
			});
		</script></div>
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
			if(colt1!=null){
				for(LineType lt1: colt1.getLine()){
					out.println("<div id='entry'>");
					out.println("<div id='orderLineNumber'><input type='text' name='linenumber' id='linenumber' pattern='onetypeValidation' title='Enter Line Number [Integer]' value='"+lt1.getLineNumber()+"'/></div>");
					out.println("<div id='orderUnitCode'><input type='text' name='unitcode' id='unitcode' pattern='textValidation'  title='Enter Unit Code [String]' value='"+lt1.getUnitCode()+"'/></div>");
					out.println("<div id='orderUnitDescription'><textarea name='unitdescription' id='unitdescription' pattern='' title='Enter Unit Description [String]'>"+lt1.getUnitDescription()+"</textarea></div>");
					out.println("<div id='orderUnitQuantity'><input type='text' name='unitquantity' id='unitquantity' pattern='onetypeValidation' title='Enter Unit Quantity [Integer]' value='"+lt1.getUnitQuantity()+"'/></div>");
					out.println("<div id='orderAction'><select name='action' id='action' title='Select the Appropriate Action'>");
					out.println("<option value=''");
					if(lt1.getAction().equals("")) out.println(" selected");
					out.println("> </option><option value='Added'");
					if(lt1.getAction().equals("Added")) out.println(" selected");
					out.println(">Added</option><option value='Modified'");
					if(lt1.getAction().equals("Modified")) out.println(" selected");
					out.println(">Modified</option></select></div>");
					out.println("<div id='orderLinesDelete'><input name='button' type='button' onClick='deleteOrderRow(this);' value='Delete' /></div>");
					out.println("</div>");
				}
			}else{%>
				<div id="entry">
					<div id="orderLineNumber"><input name="linenumber" id="linenumber" type="text" title="Enter Line Number [Integer]" value="1" pattern="onetypeValidation"/>
				  </div>
					<div id="orderUnitCode"><input name="unitcode" id="unitcode" type="text" title="Enter Unit Code [String]"  pattern="textValidation"/></div>
				  <div id="orderUnitDescription"><textarea name="unitdescription" id="unitdescription" title="Enter Unit Description [String]"></textarea></div>
					<div id="orderUnitQuantity"><input name="unitquantity" id="unitquantity" type="text" title="Enter Unit Quantity [Integer]" pattern="onetypeValidation"/>
					</div>
				  <div id="orderAction">
					  <select name="action" id="action" title="Select the Appropriate Action">
						<option value=""> </option>
						<option value="Added">Added</option>
						<option value="Modified">Modified</option>
					  </select>
				  </div>
					<div id="orderLinesDelete"><input name="button" type="button" onClick="deleteOrderRow(this);" value="Delete" /></div>
				</div>

			<%}%>
		</div>

		<div id="orderLinesActions"><input name="button" type="button" onClick="addOrderRow();" value="Insert Row" /></div>

		<div id="entry">
			<div id="label">Selected</div>
			<div id="field"><input type="checkbox" name="selected" id="selected" value="true" <% if (colt1.isSelected()) out.println("checked");%>/></div>
		</div>
		<div id="entry"></div>
	</div>
	<div id="entry">&nbsp;</div>
	<div id="entry">
		<div id="label">Reason for Return</div>
		<div id="field"><input name="reasonforreturn" type="text" id="reasonforreturn" pattern="textValidation" <%if (rmt1!=null) out.println("value='"+rmt1.getReasonForReturn()+"'");%>/></div>
	</div>
	<div id="entry"></div>
</fieldset>

<div id="entry">&nbsp;</div>

<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save">
	<input type="submit" name="SaveToFile" onclick="return validateForm('LodgeARequestForReturnMerchandise');" value="Save To File" />
    &nbsp;
	<input type="submit" name="Complete" value="Complete" onClick="return validateForm('LodgeARequestForReturnMerchandise');" />

</div>

</form>

<!-- LOAD -->
<form method="post" action="Lodge_A_Request_For_Return_Merchandise.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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
	CheckedOrderLinesType colt = new CheckedOrderLinesType();
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
		colt.getLine().add(lt);
	}

	boolean selected = false;
	if(request.getParameter("selected") != null) selected = true;

	colt.setSelected(selected);


	java.text.SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
	java.text.SimpleDateFormat df2 = new SimpleDateFormat("dd-MM-yyyy");

	ReturnMerchandiseType rmt = new ReturnMerchandiseType();
	rmt.setOrderNumber(request.getParameter("ordernumber"));
	rmt.setShipmentNumber(request.getParameter("shipmentnumber"));
	rmt.setAcceptanceDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("acceptancedate")))));
	rmt.setCheckedOrderLines(colt);
	rmt.setReasonForReturn(request.getParameter("reasonforreturn"));

	LodgeARequestForReturnMerchandiseType larfrmt = new LodgeARequestForReturnMerchandiseType();
	larfrmt.setReturnMerchandise(rmt);

	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.lodge_a_request_for_return_merchandise.ObjectFactory();

	JAXBElement larfrmElement = factory.createLodgeARequestForReturnMerchandise(larfrmt);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.lodge_a_request_for_return_merchandise" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(larfrmElement, xmlOS);//out to ByteArray

	if(request.getParameter("SaveToFile")!=null){
		response.setHeader("Content-Disposition", "attachment;filename=\"LodgeARequestForReturnMerchandise.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save")!=null){
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Lodge_a_request_for_Return_Merchandise xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Lodge_a_request_for_Return_Merchandise\"","<Lodge_a_request_for_Return_Merchandise");
		xml = xml.replaceAll("</ns2:Lodge_a_request_for_Return_Merchandise","</Lodge_a_request_for_Return_Merchandise");

		String result = client.updateWorkItemData(itemid, xml, handle);

		session.removeAttribute("wir");

		// Now we can redirect back to the worklist
		String redirectURL = (String) session.getAttribute("source");
        session.removeAttribute("source");
        response.sendRedirect(response.encodeURL(redirectURL));

	}else if (request.getParameter("Complete")!=null){
		// update the data list to be returned to the engine
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Lodge_a_request_for_Return_Merchandise xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Lodge_a_request_for_Return_Merchandise\"","<Lodge_a_request_for_Return_Merchandise");
		xml = xml.replaceAll("</ns2:Lodge_a_request_for_Return_Merchandise","</Lodge_a_request_for_Return_Merchandise");

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
