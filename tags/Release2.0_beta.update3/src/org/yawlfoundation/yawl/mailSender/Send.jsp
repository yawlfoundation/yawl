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
     java.io.*" %>
     


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
			
			String SMTP = request.getParameter("SMTP");
            String Login = null;
            String password =null;
            String To = null;
            String Alias = null;
            String object = null;
            String content = null;
            String fileLocation = null;
            String Path = "http://localhost:8080/MailSender/files/";
           	
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
				  
				  if(fileItem.getFieldName().equals("SMTP")) SMTP =  new String(fileItem.get());
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
		  		 if(fileItem.getFieldName().equals("fileLocation")) fileLocation = FilenameUtils.getName(fileItem.getName());
				 System.out.println(fileLocation);
				 String fileName = fileItem.getName();
				 File fullFile = new File(fileName);
				 File savedFile = new File(getServletContext().getRealPath("/files/"), fullFile.getName());
				 fileItem.write(savedFile);
						  
				}
			  }
			}
           	         	        	
           	
		MailSender _MailController = (MailSender) application.getAttribute("controller");
		System.out.println(SMTP);
		System.out.println(Login);
		System.out.println(password);
		System.out.println(To);
		System.out.println(Alias);
		System.out.println(object);
		System.out.println(content);
			_MailController.SendEmail(SMTP, Login, password, To, Alias, object, content, fileLocation);
			
			String redirectURL = "http://localhost:8080/resourceService/" + 
			"faces/userWorkQueues.jsp?workitem=" + wir.toXML();
			response.sendRedirect(response.encodeURL(redirectURL));
} catch  (Exception e){}
%>		
</body>
</html>