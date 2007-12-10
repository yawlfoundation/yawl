<%@ page
        import="org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient" %><%
	InterfaceA_EnvironmentBasedClient iaClient = new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");
    String sessionHandle = (String)session.getAttribute("sessionHandle");
   
    boolean connectionOK = false;
    String loginsuccess = "<response>Permission";
    connectionOK = iaClient.checkConnection(sessionHandle).startsWith(loginsuccess);
    if(sessionHandle == null || ! connectionOK){
        RequestDispatcher rd = application.getRequestDispatcher("/login.jsp");
        String pageToGet = request.getServletPath();

        session.setAttribute("pagetoget", pageToGet);
        rd.forward(request, response);
    }
%>