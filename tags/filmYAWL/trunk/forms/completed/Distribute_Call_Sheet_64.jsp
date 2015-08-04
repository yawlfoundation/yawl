<%	    	    
	System.out.println("Distribute Call Sheet outputData: "+ (String)session.getAttribute("outputData"));
	
	String inputData = (String)session.getAttribute("outputData");
	
   	session.setAttribute("inputData", inputData);
   	
   	String workItemID = new String(request.getParameter("workItemID"));
   	String sessionHandle = new String(request.getParameter("sessionHandle"));
   	String userID = new String(request.getParameter("userID"));
   	String submit = new String(request.getParameter("submit"));
		response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?workItemID="+workItemID+"&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit));
		return;
%>