<%	    	    
	System.out.println("Welcome form outputData: "+(String)request.getParameter("outputData"));
	
	String inputData = request.getParameter("outputData");
	//inputData = inputData.replaceAll("<Fill_Out_AD_Report", "<ns2:Fill_Out_AD_Report xmlns:ns2='http://www.yawlfoundation.org/sb/timeSheetInfo'");
	//inputData = inputData.replaceAll("</Fill_Out_AD_Report","</ns2:Fill_Out_AD_Report");
	
   	session.setAttribute("inputData", inputData);
   	
   	String specID = new String(request.getParameter("workItemID"));
   	String sessionHandle = new String(request.getParameter("sessionHandle"));
   	String userID = new String(request.getParameter("userID"));
   	String submit = new String(request.getParameter("submit"));
   	
   	response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/yawlFormServlet?specID="+specID+"&workItemID=null&sessionHandle="+sessionHandle+"&userID="+userID+"&submit="+submit));
   	return;
%>