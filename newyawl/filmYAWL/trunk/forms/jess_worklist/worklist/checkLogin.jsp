<%
    String sessionHandle = (String)session.getAttribute("sessionHandle");
    if(sessionHandle == null){

    	sessionHandle = (String) request.getAttribute("sessionHandle"); // have come from YAWLXForms
    	String userid = (String) request.getAttribute("userid");
    	
    	session.setAttribute("sessionHandle", sessionHandle); // restore session handle
    	session.setAttribute("userid", userid); // restore userid
    }
    
    boolean connectionOK = _worklistController.checkConnection(sessionHandle);

    if(sessionHandle == null || ! connectionOK){
        RequestDispatcher rd = application.getRequestDispatcher("/login");
        String pageToGet = request.getServletPath();

        session.setAttribute("pagetoget", pageToGet);
        rd.forward(request, response);
    }
%>