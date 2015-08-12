<%@ page import="org.yawlfoundation.orderfulfilment.authorize_loss_or_damage_claim.*"%>
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
	xml = xml.replaceAll("<Authorize_Loss_or_Damage_Claim", "<ns2:Authorize_Loss_or_Damage_Claim xmlns:ns2='http://www.yawlfoundation.org/OrderFulfilment/Authorize_Loss_or_Damage_Claim'");
	xml = xml.replaceAll("</Authorize_Loss_or_Damage_Claim","</ns2:Authorize_Loss_or_Damage_Claim");

%>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="../style.css">
<script type="text/javascript" src="../orderfulfilment.js"></script>
<script language="JavaScript" src="../calendar_eu.js"></script>
<link rel="stylesheet" href="../calendar.css">
<title>Authorize Loss or Damage Claim</title>
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
            int endOfFile = result.indexOf("</ns2:Authorize_Loss_or_Damage_Claim>");
            if(beginOfFile != -1 && endOfFile != -1)xml = result.substring(beginOfFile,endOfFile + 37);
		}
	}

	ByteArrayInputStream xmlBA = new ByteArrayInputStream(xml.getBytes("UTF-8"));
	JAXBContext jc1 = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.authorize_loss_or_damage_claim");
	Unmarshaller u = jc1.createUnmarshaller();
	JAXBElement alodcElement1 = (JAXBElement) u.unmarshal(xmlBA);	//creates the root element from XML file
	AuthorizeLossOrDamageClaimType alodct1 = (AuthorizeLossOrDamageClaimType) alodcElement1.getValue();
	LossOrDamageClaimApprovalType lodcat1 = alodct1.getLossOrDamageClaimApproval();
	LossOrDamageClaimType lodct1 = null;
	if(!(alodct1 == null)){
		lodct1 = lodcat1.getLossOrDamageClaim();
	}
%>

<div id="logo"></div>
<h2>Authorize Loss or Damage Claim</h2>
<form id="AuthorizeLossOrDamageClaim" name="AuthorizeLossOrDamageClaim" method="post" novalidate="novalidate">

<fieldset>
	<legend>Loss or Damage Claim</legend>
	<div id="entry">
		<div id="label">Order Number</div>
		<div id="field"><input name="ordernumber" type="text" id="ordernumber" pattern="textValidation" <%if (lodct1!=null) out.println("value='"+lodct1.getOrderNumber()+"'");%>/></div>
	</div>
	<div id="entry">
		<div id="label">Shipment Number</div>
		<div id="field"><input name="shipmentnumber" type="text" id="shipmentnumber" pattern="textValidation" <%if (lodct1!=null) out.println("value='"+lodct1.getShipmentNumber()+"'");%>/></div>
	</div>
	<div id="entry">
		<div id="label">Acceptance Date</div>
		<div id="field"><input name="acceptancedate" type="text" id="acceptancedate" pattern="realdateValidation" <%if (lodct1!=null) out.println("value='"+lodct1.getAcceptanceDate().getDay()+"-"+lodct1.getAcceptanceDate().getMonth()+"-"+lodct1.getAcceptanceDate().getYear()+"'");%>/>
		<script language="JavaScript">
			new tcal ({
				// form name
				'formname': 'LodgeLossOrDamageClaimReport',
				// input name
				'controlname': 'acceptancedate'
			});
		</script></div>
	</div>
	<div id="entry">
		<div id="label">Reason for Claim</div>
		<div id="field"><input name="reasonforclaim" type="text" id="reasonforclaim" pattern="textValidation" <%if (lodct1!=null) out.println("value='"+lodct1.getReasonForClaim()+"'");%>/></div>
	</div>
	<div id="entry"></div>
</fieldset>

<fieldset>
	<legend>Approval</legend>
	<div id="label">Approved</div>
	<div id="field"><input type="checkbox" name="approved" id="approved" value="true" <% if (lodcat1.isApproved()) out.println("checked");%>/></div>
	<div id="entry"></div>
</fieldset>

<div id="entry">&nbsp;</div>

<div>
	<input type="submit" name="Cancel" value="Cancel" />
	<input type="submit" name="Save" value="Save">
	<input type="submit" name="SaveToFile" onclick="return validateForm('AuthorizeLossOrDamageClaim');" value="Save To File" />
	&nbsp;
	<input type="submit" name="Complete" value="Complete" onClick="return validateForm('AuthorizeLossOrDamageClaim');" />

</div>

</form>

<!-- LOAD -->
<form method="post" action="Authorize_Loss_Or_Damage_Claim.jsp?formType=load&amp;workitem=<%= itemid %>&amp;handle=<%= handle %>&amp;submit=htmlForm" name="upform" enctype="MULTIPART/FORM-DATA">
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

	LossOrDamageClaimType lodct = new LossOrDamageClaimType();
	lodct.setOrderNumber(request.getParameter("ordernumber"));
	lodct.setShipmentNumber(request.getParameter("shipmentnumber"));
	lodct.setAcceptanceDate(XMLGregorianCalendarImpl.parse(df1.format(df2.parse(request.getParameter("acceptancedate")))));
	lodct.setReasonForClaim(request.getParameter("reasonforclaim"));

	boolean approved = false;
	if(request.getParameter("approved") != null) approved = true;

	LossOrDamageClaimApprovalType lodcat = new LossOrDamageClaimApprovalType();
	lodcat.setLossOrDamageClaim(lodct);
	lodcat.setApproved(approved);

	AuthorizeLossOrDamageClaimType alodct = new AuthorizeLossOrDamageClaimType();
	alodct.setLossOrDamageClaimApproval(lodcat);


	ObjectFactory factory = new org.yawlfoundation.orderfulfilment.authorize_loss_or_damage_claim.ObjectFactory();

	JAXBElement alodcElement = factory.createAuthorizeLossOrDamageClaim(alodct);

	JAXBContext jc = JAXBContext.newInstance("org.yawlfoundation.orderfulfilment.authorize_loss_or_damage_claim" );
	Marshaller m = jc.createMarshaller();
	m.setProperty( javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

	ByteArrayOutputStream xmlOS = new ByteArrayOutputStream();
	m.marshal(alodcElement, xmlOS);//out to ByteArray

	if(request.getParameter("SaveToFile")!=null){
		response.setHeader("Content-Disposition", "attachment;filename=\"AuthorizeLossOrDamageClaim.xml\";");
		response.setHeader("Content-Type", "text/xml");

		ServletOutputStream outs = response.getOutputStream();
		xmlOS.writeTo(outs);
		outs.close();
	}else if (request.getParameter("Save")!=null){
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Authorize_Loss_or_Damage_Claim xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Authorize_Loss_or_Damage_Claim\"","<Authorize_Loss_or_Damage_Claim");
		xml = xml.replaceAll("</ns2:Authorize_Loss_or_Damage_Claim","</Authorize_Loss_or_Damage_Claim");

		String result = client.updateWorkItemData(itemid, xml, handle);

		session.removeAttribute("wir");

		// Now we can redirect back to the worklist
		String redirectURL = (String) session.getAttribute("source");
        session.removeAttribute("source");
        response.sendRedirect(response.encodeURL(redirectURL));

	}else if (request.getParameter("Complete")!=null){
		// update the data list to be returned to the engine
		xml = xmlOS.toString("UTF-8");
		xml = xml.replaceAll("<ns2:Authorize_Loss_or_Damage_Claim xmlns:ns2=\"http://www.yawlfoundation.org/OrderFulfilment/Authorize_Loss_or_Damage_Claim\"","<Authorize_Loss_or_Damage_Claim");
		xml = xml.replaceAll("</ns2:Authorize_Loss_or_Damage_Claim","</Authorize_Loss_or_Damage_Claim");

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
