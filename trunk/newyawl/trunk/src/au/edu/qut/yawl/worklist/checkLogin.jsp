<%
//System.out.println("checkLogin.jsp::request.getRequestURL() = " + request.getRequestURL());
    String sessionHandle = (String)session.getAttribute("sessionHandle");
    if(sessionHandle == null){
    	// an example invocation (forward) is from YAWLFormServlet
    	//System.out.println("checkLogin.jsp::Have come from Chiba");
    	sessionHandle = (String) request.getAttribute("sessionHandle"); // have come from YAWLXForms
    	String userid = (String) request.getAttribute("userid");
    	
    	session.setAttribute("sessionHandle", sessionHandle); // restore session handle
    	session.setAttribute("userid", userid); // restore userid
    	//System.out.println("checkLogin.jsp::sessionHandle = " + sessionHandle);
    	//System.out.println("checkLogin.jsp::userid = " + userid);
    }
    boolean connectionOK = false;
    connectionOK = _worklistController.checkConnection(sessionHandle);
	//System.out.println("checkLogin.jsp::sessionHandle = " + sessionHandle);
	//System.out.println("checkLogin.jsp::connectionOK = " + connectionOK);
    if(sessionHandle == null || ! connectionOK){
        RequestDispatcher rd = application.getRequestDispatcher("/login");
        String pageToGet = request.getServletPath();

        session.setAttribute("pagetoget", pageToGet);
        rd.forward(request, response);
    }
%>