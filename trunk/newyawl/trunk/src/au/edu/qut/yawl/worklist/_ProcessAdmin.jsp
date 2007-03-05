<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
		<title>YAWL Administration</title>
        <!-- Include file to load init method -->
        <%@ include file="head.jsp"%>
	</head>
	<body>
        <!-- Include check login code -->
        <%@ include file="checkLogin.jsp"%>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp"%>
    <%
    boolean mayAdminister = _worklistController.checkConnectionForAdmin(sessionHandle);
    if(mayAdminister){
        String action = request.getParameter("action");
        if(" Unload Specification ".equals(action)){
            String specID = request.getParameter("specID");
            if(specID != null){
                String result = _worklistController.unloadSpecification(specID, sessionHandle);
                if(_worklistController.successful(result)){
                    RequestDispatcher toAdmin = application.getRequestDispatcher("/admin");
                    toAdmin.forward(request, response);
                } else {
                %>
                    <font color="red"><%= result %></font>
                <%
                }
            }
        } else if (" Add YAWL Service ".equals(action)){
            String serviceURI = request.getParameter("serviceURI");
            String serviceDocumentation = request.getParameter("serviceDocumentation");
            if(null != serviceURI && ! "".equals(serviceURI)){
                String result = _worklistController.addYAWLService(
                        serviceURI, serviceDocumentation, sessionHandle);
                if(_worklistController.successful(result)){
                    RequestDispatcher toAdmin = application.getRequestDispatcher("/admin");
                    toAdmin.forward(request, response);
                } else {
                %>
                    <font color="red"><%= result %></font>
                <%
                }
            }
        }else if (" Remove YAWL Service ".equals(action)){
            String serviceURI = request.getParameter("serviceURI");
            if(null != serviceURI && ! "".equals(serviceURI)){
                String result = _worklistController.removeYAWLService(
                        serviceURI, sessionHandle);
                if(_worklistController.successful(result)){
                    RequestDispatcher toAdmin = application.getRequestDispatcher("/admin");
                    toAdmin.forward(request, response);
                } else {
                %>
                    <font color="red"><%= result %></font>
                <%
                }
            }
        }
    }
    %>
    <%@include file="footer.jsp"%>
    </body>
</html>