<%@ page import="org.yawlfoundation.yawl.engine.interfce.*,
		org.yawlfoundation.yawl.elements.*"%>

<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
		<title>YAWL Administration and Monitoring Tool</title>
    </head>
	<body>
        <%@ include file="checkLogin.jsp"%>
        <%@ include file="YAWLnavigation.jsp"%>
    <%
       	String action = new String(request.getParameter("action"));
		if ("Add Service".equals(action)){
           	String serviceURI = request.getParameter("serviceURI");
       	    String serviceDocumentation = request.getParameter("serviceDocumentation");
        	if(null != serviceURI && ! "".equals(serviceURI)){
            	YAWLServiceReference service = new YAWLServiceReference();
           		service.set_yawlServiceID(serviceURI);
            	service.set_documentation(serviceDocumentation); 
            	String replyFromYAWL =iaClient.setYAWLService(service, sessionHandle);
                System.out.println("replyfromYAWL: "+ replyFromYAWL);
            	RequestDispatcher to3rdpartyPage = application.getRequestDispatcher("/3rdparty?upload=true");
            	to3rdpartyPage.forward(request, response);      
      		} else {
        	
            	RequestDispatcher to3rdpartyPageError = application.getRequestDispatcher("/3rdparty?uploaderror=true");
	           	to3rdpartyPageError.forward(request, response); 
    	    }
    	}
    	if ("Delete Selected Service".equals(action)) {
    		String serviceURI = request.getParameter("selectedService");
    		if(null != serviceURI && ! "".equals(serviceURI)){
    			String replyFromYAWL = iaClient.removeYAWLService(serviceURI, sessionHandle);
    			System.out.println("replyfromYAWL: "+ replyFromYAWL);
    			
    			RequestDispatcher to3rdpartyPage = application.getRequestDispatcher("/3rdparty?unload=true");
            	to3rdpartyPage.forward(request, response); 
            } else {
    			RequestDispatcher to3rdpartyPageError = application.getRequestDispatcher("/3rdparty?unloaderror=true");
	           	to3rdpartyPageError.forward(request, response); 
    		}
    	}
         
    %>
    <%@include file="footer.jsp"%>
    </body>
</html>