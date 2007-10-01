<%@ page import="java.io.*, 
				au.edu.qut.yawl.engine.interfce.*"%>
		<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
		<title>YAWL Specification Upload & Unload</title>
        </head>
	<body>
        <!-- Include check login code -->
        <%@ include file="checkLogin.jsp"%>
        <!-- Include YAWL Banner Information -->
        <%@ include file="YAWLnavigation.jsp"%>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
<%
	String action = request.getParameter("action");
	if ("Upload".equals(action)){
	    StringBuffer result = new StringBuffer();
    	ServletInputStream in = request.getInputStream();

	    int i = in.read();
    	while (i != -1) {
        	result.append((char) i);
	        i = in.read();
    	}
	    String filepath = result.substring(
    	        result.indexOf("filename=\"") + 10,
        	    result.indexOf("Content-Type") -2 );
	    String filename = filepath.substring(
    	        filepath.lastIndexOf(File.separator) + 1,
        	    filepath.length() -1);

	    int beginOfFile = result.indexOf("<?xml");
    	int endOfFile = result.indexOf("</specificationSet>") + 19;
	    if(beginOfFile != -1){
    	    String resultStr = result.substring(
        	    beginOfFile,
            	endOfFile);

	    if(sessionHandle != null){
    	        String replyFromYAWL =iaClient.uploadSpecification(resultStr, filename, sessionHandle);
	
    	        RequestDispatcher toSpecPage = application.getRequestDispatcher("/specification?upload=true");
        	    toSpecPage.forward(request, response);           

	        } else {
    	        %>
                <%= "<font color=\"red\">You were not granted upload access to the engine." +
                "  Try logging in as an administrator.</font>" %>
            <%
        	}
	    } else {
    %>
        <%=
            "<font color=\"red\">Either no file was specified" +
            " or the specified file was not in the YAWL syntax.<font>"
        %>
    <%
    	}
    }
    if ("Delete Specification".equals(action)){
    	String specID = request.getParameter("selectedSpecification");
    	String replyFromYAWL =iaClient.unloadSpecification(specID, sessionHandle);
    	
    	RequestDispatcher toSpecPage = application.getRequestDispatcher("/specification?unload=true");
    	toSpecPage.forward(request, response);   
    }
    
%>
    <%@include file="footer.jsp"%>
    </body>
</html>