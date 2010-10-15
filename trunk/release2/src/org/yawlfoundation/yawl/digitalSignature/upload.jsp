<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="org.apache.commons.fileupload.FileItem,
    org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException,
     org.apache.commons.fileupload.disk.DiskFileItemFactory,
     org.apache.commons.fileupload.servlet.ServletFileUpload,
     org.apache.commons.io.FilenameUtils,
     org.jdom.Element,
     org.jdom.output.Format,
     org.jdom.output.XMLOutputter" %>

<%@ page import="org.yawlfoundation.yawl.digitalSignature.DigitalSignature" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.Marshaller" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.WorkItemRecord" %>
<%@ page import="org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGatewayClient" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>

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
        try {
            String Document = "Document";
            String Signature = "Signature";

            String password = null;
            String p12 = null;
            String Certificate = null;
            String	Path = "http://localhost:8080/digitalSignature/files/";

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
            String workItemXML = (String) session.getAttribute("itemXML");
            WorkItemRecord wir = Marshaller.unmarshalWorkItem(workItemXML) ;

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

                    // once the data updates are complete, update the workitem's data via
                    // the gateway.
                    String itemid = (String) session.getAttribute("workitem");
                    String handle = (String) session.getAttribute("handle");
                    String dataString = new XMLOutputter(Format.getCompactFormat()).outputString(data);
                    String wqURL = "http://localhost:8080/resourceService/workqueuegateway";
                    WorkQueueGatewayClient wqClient = new WorkQueueGatewayClient(wqURL);
                    wqClient.updateWorkItemData(itemid, dataString, handle);
                }
                else {
                    System.out.println("This workitem does not contain any data for updating.");
                }

                String redirectURL = (String) session.getAttribute("redirectURL");
                if (redirectURL == null) {
                    redirectURL = "http://localhost:8080/resourceService/faces/userWorkQueues.jsp";
                }

                // clean up our stored session attributes
                session.removeAttribute("itemXML");
                session.removeAttribute("workitem");
                session.removeAttribute("handle");
                session.removeAttribute("redirectURL");

                response.sendRedirect(response.encodeURL(redirectURL));
            }

        } catch (SizeLimitExceededException ex) {}
%>
</body>
</html>