<%
    String sessionHandle = (String) session.getAttribute("sessionHandle");

    if (sessionHandle == null) {

        // have come from YAWLXForms
        sessionHandle = (String) request.getAttribute("sessionHandle");
    	String userid = (String) request.getAttribute("userid");

        // restore session handle & userid
    	session.setAttribute("sessionHandle", sessionHandle);
    	session.setAttribute("userid", userid);
    }

    boolean connectionOK =  wqGateway.checkConnection(sessionHandle);

    if (sessionHandle == null || ! connectionOK) {
        session.setAttribute("pagetoget", request.getServletPath());
        application.getRequestDispatcher("/login").forward(request, response);
    }
%>