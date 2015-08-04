<%@ page import="org.yawlfoundation.orderfulfilment.estimate_trailer_usage.*"%>
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
	xml = xml.replaceAll("<Estimate_Trailer_Usage", "<ns2:Estimate_Trailer_Usage xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Estimate_Trailer_Usage'");
	xml = xml.replaceAll("</Estimate_Trailer_Usage","</ns2:Estimate_Trailer_Usage");

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<script language="JavaScript" src="../calendar_eu.js"></script>
<link rel="stylesheet" href="../calendar.css">
<title>Estimate Trailer Usage</title>
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
            int endOfFile = result.indexOf("</ns2:Estimate_Trailer_Usage>");
            if(beginOfFile != -1 && endOfFile != -1)xml = result.substring(beginOfFile,endOfFile + 29);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.estimate_trailer_usage");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement etuElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	EstimateTrailerUsageType etut1 = (EstimateTrailerUsageType) etuElement1.getValue();
	OrderLinesType olt1 = etut1.getOrderLines();
	PackagesType pt1 = etut1.getPackages();

%>
<div id="logo"></div>
<h2>Estimate Trailer Usage  </h2>
<form id="EstimateTrailerUsage" name="EstimateTrailerUsage" method="post" novalidate="novalidate">

<fieldset>
	<legend>Order Details</legend>
	<div id="entry">
		<div id="label">Order Number </div>
		<div id="field"><input type="text" name="ordernumber" id="ordernumber" pattern="textValidation" <%if (etut1.getOrderNumber()!=null) out.println("value='"+etut1.getOrderNumber()+"'");%> /> </div>
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
			if(olt1!=null){
				for(LineType lt1: olt1.getLine()){
					out.println("<div id='entry'>");

					out.println("<div id='orderLineNumber'><input type='text' name='linenumber' id='linenumber' value='"+lt1.getLineNumber()+"' class=InputReadOnly pattern='' readonly/></div>");
					out.println("<div id='orderUnitCode'><input type='text' name='unitcode' id='unitcode' value='"+lt1.getUnitCode()+"' class='InputReadOnly' pattern='' readonly/></div>");
					out.println("<div id='orderUnitDescription'><textarea name='unitdescription' id='unitdescription' pattern='' title='Enter Unit Description [String]' class='InputReadOnly' pattern='' readonly>"+lt1.getUnitDescription()+"</textarea></div>");
					out.println("<div id='orderUnitQuantity'><input type='text' name='unitquantity' id='unitquantity' value='"+lt1.getUnitQuantity()+"' class='InputReadOnly' pattern='' readonly/></div>");
					out.println("<div id='orderAction'><input type='text' name='action' id='action' value='"+lt1.getAction()+"' class='InputReadOnly' pattern='' readonly/></div>");
					out.println("</div>");
				}
			}%>
		</div>
		<div id="entry"></div>
	</div>
</fieldset>

<fieldset>
	<legend>Packages</legend>
	<div id="packageLines">
		<div id="packageid">Package ID</div>
		<div id="packagesVolume">Volume</div>

		<div id="allPackages">

			<%
			if(pt1!=null){
				for(PackageType pt2 : pt1.getPackage()){
					out.println("<div id='packageEntry'>");
					out.println("<div id='packageid'><input type='text' name='packageID' id='packageID' pattern='onetypeValidation' title='Enter Package ID [String]' value='"+pt2.getPackageID()+"'/></div>");
					out.println("<div id='packagesVolume'><select name='volume' id='volume' title='Select the Appropriate Volume'>");
					out.println("<option value='25'");
					if(pt2.getVolume().equals("25")) out.println(" selected");
					out.println(">25</option><option value='50'");
					if(pt2.getVolume().equals("50")) out.println(" selected");
					out.println(">50</option><option value='Modified'");
					if(pt2.getVolume().equals("100")) out.println(" selected");
					out.println(">100</option><option value='Modified'");
					if(pt2.getVolume().equals("200")) out.println(" selected");
					out.println(">200</option></select></div>");
					out.println("<div id='packagesDelete'><input name='button' type='button' onClick='deletePackagesRow(this);' value='Delete' /></div>");
					out.println("</div>");
				}
			}else{%>
			<div id="packageEntry">
					<div id="packageid"><input name="packageID" id="packageID" type="text" title="Enter Package ID [String]"  pattern="textValidation"/>
				  </div>
				  <div id="packagesVolume">
					  <select name="volume" id="volume" title="Select the Appropriate Volume">
						<option value="25">25</option>
						<option value="50">50</option>
						<option value="100">100</option>
						<option value="200">200</option>
					  </select>
				  </div>
					<div id="packagesDelete"><input name="button" type="button" onClick="deletePackagesRow(this);" value="Delete" /></div>
				</div>
			<%}%>
		</div>

		<div id="packagesActions"><input name="button" type="button" onClick="addPackagesRow();" value="Insert Row" /></div>
	</div>
	<div id="entry">&nbsp;</div>
</fieldset>

<div id="entry">&nbsp;</div>
<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save" >
	<input type="submit" name="SaveToFile" value="Save To File" onClick="return validateForm('EstimateTrailerUsage');"/>
	&nbsp;
	<input type="submit" name="Complete" value="Complete" onClick="return validateForm('EstimateTrailerUsage');"/>

</div>

</form>

<!-- LOAD -->
    <form method="post" action="Estimate_Trailer_Usage.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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


	PackagesType pt = new PackagesType();
	String[] packageID = request.getParameterValues("packageID");
	String[] volume = request.getParameterValues("volume");

	for(int x = 0; x<packageID.length; x++){
		PackageType pack = new PackageType();
		pack.setPackageID(packageID[x]);
		pack.setVolume(new BigInteger(volume[x]));
		pt.getPackage().add(pack);
	}


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

	EstimateTrailerUsageType etut = new EstimateTrailerUsageType();
	etut.setOrderNumber(request.getParameter("ordernumber"));
	etut.setOrderLines(olt);
	etut.setPackages(pt);


	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.estimate_trailer_usage.ObjectFactory();

	JAXBElement etuElement = factory.createEstimateTrailerUsage(etut);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.estimate_trailer_usage" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(etuElement, xmlOS);//out to ByteArray

	if(request.getParameter("SaveToFile")!=null){
		response.setHeader("Content-Disposition", "attachment;filename=\"EstimateTrailerUsage.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save")!=null){
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Estimate_Trailer_Usage xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Estimate_Trailer_Usage\"","<Estimate_Trailer_Usage");
		xml = xml.replaceAll("</ns2:Estimate_Trailer_Usage","</Estimate_Trailer_Usage");

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
		xml = xml.replaceAll("<ns2:Estimate_Trailer_Usage xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Estimate_Trailer_Usage\"","<Estimate_Trailer_Usage");
		xml = xml.replaceAll("</ns2:Estimate_Trailer_Usage","</Estimate_Trailer_Usage");

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
