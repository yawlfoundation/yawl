<%@ page import="org.yawlfoundation.yawl.worklist.WorkItemProcessor,
				 org.yawlfoundation.yawl.engine.interfce.WorkItemRecord,
	             org.yawlfoundation.yawl.engine.interfce.TaskInformation,
                 org.yawlfoundation.yawl.worklist.model.WorkListGUIUtils,
                 org.jdom.Element,
                 org.jdom.output.XMLOutputter"%>

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Work Item Processor</title>
        <!-- Include file to load init method -->
        <%@ include file="head.jsp"  %>        
	</head>
	<body>
        <!-- Include check login code -->
        <%@ include file="checkLogin.jsp" %>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp" %>
        <h3>Work Item Processing Page</h3>
        <%

            String workItemID = (String) request.getAttribute("workItemID");
            String submitType = (String) request.getAttribute("submit");

            Element outputData = (Element) request.getAttribute("outputData");
            Element inputData = (Element) request.getAttribute("inputData");
            sessionHandle = (String) session.getAttribute("sessionHandle");

            if (submitType == null) {
                submitType = request.getParameter("submit");
                workItemID = request.getParameter("workItemID");
            }

            if (submitType != null) {
                if (submitType.equals("Save Work Item") || submitType.equals("Add New Instance")) {
                    _worklistController.saveWorkItem(workItemID, new XMLOutputter().outputString(outputData));
                    if (submitType.equals("Add New Instance")) {
                        RequestDispatcher requestDispatcher =
                                application.getRequestDispatcher("/instanceAdder");
                        requestDispatcher.forward(request, response);
                    }
        %>
                <p>
                Work Item <font color="red"><%= workItemID %></font>
                has been saved to the worklist handler.
                </p>
                <%
                } else if(submitType.equals("Submit Work Item") || submitType.equals("Remove")){
                    String outcome = _worklistController.checkInWorkItem(
                            workItemID, inputData, outputData, sessionHandle);
                    if(_worklistController.successful(outcome)){
                        _worklistController.unsaveWorkItem(workItemID);
                    %>
                        <p>
                        Work Item <font color="blue"><%= workItemID %></font>
                        has been successfully checked back into the Engine.
                        </p>
                    <%
                    }else{
                        System.out.println(outcome);
                        if(outcome.indexOf("FAILED TO VALIDATE AGAINST SCHEMA = ") != -1){
                            outcome = WorkListGUIUtils.removeFailureTags(outcome);
                            outcome = WorkListGUIUtils.convertToEscapes(outcome);
                            %>
                            <font color="red"><%= outcome %></font>
                            <%
                        }
                        else if(outcome.indexOf("SCHEMA = ") != -1){
                            session.setAttribute("outcome", outcome);
                            application.getRequestDispatcher("/validationProblem")
                                    .forward(request, response);
                        } else {
                            outcome = WorkListGUIUtils.removeFailureTags(outcome);
                            outcome = WorkListGUIUtils.convertToEscapes(outcome);
                        %>
                        <p>
                        <font color="red"><%= outcome %></font>
                        </p>
                        <%
                        }
                    }
                } else if (submitType.equals("Suspend Task")){
                    String outcome = _worklistController.suspendWorkItem(workItemID, sessionHandle);
                    if(_worklistController.successful(outcome)){
                    %>
                        <p>
                        Work Item <font color="red"><%= workItemID %></font>
                        has been suspended.</p>
                    <%
                    } else {
                    %>
                        <p>
                        Failed to suspend Work Item <font color="red"><%= workItemID %></font>
                        because <%= outcome %>.
                        </p>
                    <%
                    }
                } else if (submitType.equals("Edit Work Item")){
					try{
						String userID = (String) session.getAttribute("userid");
						WorkItemProcessor wip = new WorkItemProcessor();
						WorkItemRecord item = _worklistController.getCachedWorkItem(workItemID);
						TaskInformation taskInfo = _worklistController.getTaskInformation(
			            	        item.getSpecificationID(), item.getTaskID(), sessionHandle);

						if (taskInfo.getAttribute("formtype")==null || !taskInfo.getAttribute("formtype").equalsIgnoreCase("pdf")) {
				
						 	wip.executeWorkItemPost( getServletContext(), workItemID, 
								sessionHandle, _worklistController, userID, session.getId() );
						
							String url = wip.getRedirectURL(getServletContext(), taskInfo, session.getId());
							response.sendRedirect(response.encodeURL(url));						
						}

					} catch(Exception e){
						// if form generation fails for any reason fall back to XML itemViewer.jsp page
						RequestDispatcher requestDispatcher = application.getRequestDispatcher("/itemViewer?error=true");
	                    requestDispatcher.forward(request, response);
					}
                }
                else if ("Raise Exception".equals(submitType)){
                     if (_ixURI != null) {
                         String url = _ixURI + "/workItemException?workItemID=" + workItemID ;
                         response.sendRedirect( response.encodeURL(url) );
                    }
                }        
            }
        %>
        <%@include file="footer.jsp"%>
    </body>
</html>
