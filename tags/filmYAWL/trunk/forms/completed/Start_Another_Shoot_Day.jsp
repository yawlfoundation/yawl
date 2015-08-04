<%	    	    
	System.out.println("Start Another Shoot Day outputData: "+(String)request.getParameter("outputData"));
	
	String inputData = request.getParameter("outputData");
	
   	session.setAttribute("inputData", inputData);
   	
   	String specID = new String(request.getParameter("workItemID"));
   	String sessionHandle = new String(request.getParameter("sessionHandle"));
   	String userID = new String(request.getParameter("userID"));
   	String submit = new String(request.getParameter("submit"));
		response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?specID="+specID+"&workItemID=null&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit));
		return;
%>