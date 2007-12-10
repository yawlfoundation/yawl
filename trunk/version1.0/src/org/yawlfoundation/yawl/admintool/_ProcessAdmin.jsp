<%@ page import="org.yawlfoundation.yawl.worklist.model.YOrgResource"%><html xmlns="http://www.w3.org/1999/xhtml">
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
        else if(action != null && action.indexOf(" Create") != -1){
            String userName = request.getParameter("userName");
            String password  = request.getParameter("password");
            String password2 = request.getParameter("password2");
            String properName = request.getParameter("properName");

            boolean failed = false;
            if(userName == null || userName.length() == 0){
                %>
                <font color="red">UserName must have a value.</font>
                <%
                failed = true;
            }
            if(password == null || password.length() < 4){
                %>
                <font color="red">Password must have a value, and be at least 4 chars.</font>
                <%
                failed = true;
            }
            if(!password2.equals(password)){
                %>
                <font color="red">The 'password' and the 'confirm password'
                fields must be equal.</font>
                <%
                failed = true;
            }
            if(! failed){
                String result = null;
                YOrgResource rsrc = null;
                try{
                    rsrc =  _worklistController.addResource(userName, properName, password);
                }catch (IllegalArgumentException e){
                    %>
                    <font color="red">
                    There was a problem creating the resource.
                    <%= e.getMessage()  %>
                    </font>
                    <%
                }
                if (" Create Admin ".equals(action)){
                    result =
                            _worklistController.createUser(userName, password, true, sessionHandle);
                } else if (" Create User ".equals(action)){
                    result =
                            _worklistController.createUser(userName, password, false, sessionHandle);
                }
                if(_worklistController.successful(result)){
                    RequestDispatcher toAdmin = application.getRequestDispatcher("/admin");
                    toAdmin.forward(request, response);
                }
                %>
                <font color="red"><%= result %></font>
                <%
            }
        }
    }
    %>
    <%@include file="footer.jsp"%>
    </body>
</html>