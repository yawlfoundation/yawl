<%
	InterfaceA_EnvironmentBasedClient iaClient = new InterfaceA_EnvironmentBasedClient("http://131.181.70.9:8080/yawl/ia");
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