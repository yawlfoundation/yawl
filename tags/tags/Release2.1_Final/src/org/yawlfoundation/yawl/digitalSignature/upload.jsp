<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="org.apache.commons.fileupload.servlet.*,
    org.apache.commons.fileupload.disk.*,
     org.apache.commons.io.*,
     java.util.*,
     org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException,
     org.apache.commons.fileupload.*,
     org.yawlfoundation.yawl.digitalSignature.DigitalSignature,
     java.io.*" %>

<%@ page import="org.jdom.Element" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.Marshaller" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.WorkItemRecord" %>

<%--
  ~ Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
  ~ The YAWL Foundation is a collaboration of individuals and
  ~ organisations who are committed to improving workflow technology.
  ~
  ~ This file is part of YAWL. YAWL is free software: you can
  ~ redistribute it and/or modify it under the terms of the GNU Lesser
  ~ General Public License as published by the Free Software Foundation.
  ~
  ~ YAWL is distributed in the hope that it will be useful, but WITHOUT
  ~ ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  ~ or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
  ~ Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public
  ~ License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Uploading file</title>
</head>
<body>
	
	<%
	try{
			String Document = "Document";
			String Signature = "Signature";

			String password = null;
			String p12 = null;
			String Certificate = null;
		    String	Path = "http://localhost:8080/digitalSignature/files/";
			String workItemXML = request.getParameter("workitem");
			WorkItemRecord wir;
			if (workItemXML != null) {
				wir = Marshaller.unmarshalWorkItem(workItemXML) ;
				session.setAttribute("workitem", wir);                  // save it for the post
			}   else {wir = (WorkItemRecord) session.getAttribute("workitem");}
				

			if (ServletFileUpload.isMultipartContent(request))
			{
			  // Parse the HTTP request...
			  ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
			  List fileItemsList = servletFileUpload.parseRequest(request);
			  /* Process file items... */
			  Iterator it = fileItemsList.iterator();
			  while (it.hasNext())
			  {
				FileItem fileItem = (FileItem)it.next();
				if (fileItem.isFormField())
				{
				  /* The file item contains a simple name-value pair of a form field */
				  if(fileItem.getFieldName().equals("password")) password = new String(fileItem.get());
				  else if(fileItem.getFieldName().equals("x509")) Certificate = FilenameUtils.getName(fileItem.getName());
				  else if(fileItem.getFieldName().equals("P12")) p12 = FilenameUtils.getName(fileItem.getName());
				}
				else
				{
				  /* The file item contains an uploaded file */
		
					if(fileItem.getFieldName().equals("password")) password = new String(fileItem.get());
					else if(fileItem.getFieldName().equals("x509")) Certificate = FilenameUtils.getName(fileItem.getName());
					else if(fileItem.getFieldName().equals("P12")) p12 = FilenameUtils.getName(fileItem.getName());
					
    					String fileName = fileItem.getName();
						System.out.println(fileName);
						/* Save the uploaded file if its size is greater than 0. */
						/*if (fileItem.getSize() > 0){
							fileName = fileItem.getName();
						 	String dirName = "webapps/digitalSignature/files/";
						  	File saveTo = new File(dirName + fileName);
						    try { fileItem.write(saveTo);}
						       catch (Exception e){}*/
						File fullFile = new File(fileName);
						File savedFile = new File(getServletContext().getRealPath("/files/"), fullFile.getName());
						fileItem.write(savedFile);
						  
				}
			  }
			}
				Element data = wir.getDataList();
				DigitalSignature Controller = (DigitalSignature) application.getAttribute("controller");
				Controller.setP12AndPassword(p12, password, Certificate);
				String Result = Controller.ProgMain(data.getChild(Document));	
				System.out.println(Result);
			
				if (data != null) 
				{
				// repeat this part for each item to be updated
				Element dataitem = data.getChild(Signature);        // get data var
					if (dataitem != null) {
						dataitem.setText(Result);
						// update data var's value
						wir.getUpdatedData();

						// pass the updated wir back to the calling worklist page;
						// must convert it back to XML in the process.
						System.out.println(wir.getDataList().getChild(Signature).getText().toString());
						String redirectURL = "http://localhost:8080/resourceService/" +
													"faces/userWorkQueues.jsp?workitem=" + wir.toXML();
						response.sendRedirect(response.encodeURL(redirectURL));
				}else { System.out.println("This workitem does not contain any data for updating.");}
				
				  
			}	           
				            
		} catch (SizeLimitExceededException ex) {}

	
%>
</body>
</html>