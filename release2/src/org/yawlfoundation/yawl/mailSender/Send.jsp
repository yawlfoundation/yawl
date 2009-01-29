<%@ page import="org.jdom.Element" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.Marshaller" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.WorkItemRecord" %>
<%@ page import="org.yawlfoundation.yawl.mailSender.MailSender"%>

<%@ page import="org.apache.commons.fileupload.servlet.*,
    org.apache.commons.fileupload.disk.*,
     org.apache.commons.io.*,
     java.util.*,
     org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException,
     org.apache.commons.fileupload.*,
     java.io.*,
	 javax.xml.parsers.DocumentBuilder,
	 javax.xml.parsers.DocumentBuilderFactory,

	 org.w3c.dom.Node,
	 org.w3c.dom.NodeList"%>
     


<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<%
try{ 
			System.out.println("Send Called");
			String workItemXML = request.getParameter("workitem");
			WorkItemRecord wir;
			if (workItemXML != null) {
				wir = Marshaller.unmarshalWorkItem(workItemXML) ;
				session.setAttribute("workitem", wir);                  // save it for the post
			}   else {wir = (WorkItemRecord) session.getAttribute("workitem");}
			
			
			String Port = null;
			String SMTP = null;
            String Login = null;
            String password =null;
            String To = null;
            String Alias = null;
            String object = null;
            String content = null;
            String fileLocation = null;
           	
            
			File file = new File(getServletContext().getRealPath("/files/"), "SMTP.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            
			System.out.println("Root element " + doc.getDocumentElement().getNodeName());
            NodeList nodeLst = doc.getElementsByTagName("SMTP");
            for (int s = 0; s < nodeLst.getLength(); s++) {

                Node fstNode = nodeLst.item(s);
                
                if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
              
                  org.w3c.dom.Element fstElmnt = (org.w3c.dom.Element) fstNode;
                  NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("SMTP_Address");
                  org.w3c.dom.Element fstNmElmnt = (org.w3c.dom.Element) fstNmElmntLst.item(0);
                  NodeList fstNm = fstNmElmnt.getChildNodes();
                  System.out.println("SMTP_Address : "  + ((Node) fstNm.item(0)).getNodeValue());
				  SMTP = ((Node) fstNm.item(0)).getNodeValue();
                  NodeList lstNmElmntLst = fstElmnt.getElementsByTagName("Port");
                  org.w3c.dom.Element lstNmElmnt = (org.w3c.dom.Element) lstNmElmntLst.item(0);
                  NodeList lstNm = lstNmElmnt.getChildNodes();
                  System.out.println("Port : " + ((Node) lstNm.item(0)).getNodeValue());
				  Port=((Node) lstNm.item(0)).getNodeValue();
                }
          }
   
            
            
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
				  if(fileItem.getFieldName().equals("fileLocation")) fileLocation = FilenameUtils.getName(fileItem.getName());
				  if(fileItem.getFieldName().equals("Login")) Login =  new String(fileItem.get());
				  if(fileItem.getFieldName().equals("password")) password =  new String(fileItem.get());
				  if(fileItem.getFieldName().equals("To")) To =  new String(fileItem.get());;
				  if(fileItem.getFieldName().equals("Alias")) Alias =  new String(fileItem.get());
				  if(fileItem.getFieldName().equals("Object")) object =  new String(fileItem.get());
				  if(fileItem.getFieldName().equals("content")) content =  new String(fileItem.get());


				}
				else
				{
				  /* The file item contains an uploaded file */
		  		 if(fileItem.getFieldName().equals("fileLocation")) 
		  			 if(fileItem.getName().isEmpty());
		  			 else
		  			 {
		  				fileLocation = FilenameUtils.getName(fileItem.getName());
				 		System.out.println(fileLocation);
						String fileName = fileItem.getName();
						File fullFile = new File(fileName);
						File savedFile = new File(getServletContext().getRealPath("/files/"), fullFile.getName());
						fileItem.write(savedFile);
					};
				}
			  }
			}
           	         	        	
           	
		MailSender _MailController = (MailSender) application.getAttribute("controller");
		_MailController.SendEmail( SMTP, Port, Login, password, To, Alias, object, content, fileLocation);
			
			String redirectURL = "http://localhost:8080/resourceService/" + 
			"faces/userWorkQueues.jsp?workitem=" + wir.toXML();
			response.sendRedirect(response.encodeURL(redirectURL));
} catch  (Exception e){}
%>		
</body>
</html>