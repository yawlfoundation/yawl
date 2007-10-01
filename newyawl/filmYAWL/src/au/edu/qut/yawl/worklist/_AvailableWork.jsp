<%@ page import="java.util.Iterator,
                 java.util.List,
				 au.edu.qut.yawl.worklist.model.*,
				 au.edu.qut.yawl.worklist.WorkItemProcessor,
				 au.edu.qut.yawl.forms.InstanceBuilder"%><%    
	String workItemID = (String) request.getAttribute("workItemID");
    String submitType = (String) request.getAttribute("submit");

	if (submitType == null) {
	    submitType =  request.getParameter("submit");
	}
	if (workItemID == null) {
		workItemID = request.getParameter("workItemID");
	}
	
    if ((submitType != null) && submitType.equals("Raise Exception")) {
    
        if (_ixURI != null) {
            String url = _ixURI + "/workItemException?workItemID=" + workItemID ;
            response.sendRedirect( response.encodeURL(url) );
            return;
        }
    }
    else if (workItemID != null) {
		if ((submitType != null) && submitType.equals("Cancel")) {
			// do nothing, received a workitem cancellation
		}
		else {
	        String sessionHandle = (String) session.getAttribute("sessionHandle");
	        String userID = (String) session.getAttribute("userid");
	        WorkItemRecord checkedOutItem = _worklistController.checkOut(
	                workItemID, sessionHandle);
	        
	        WorkItemProcessor wip = new WorkItemProcessor();
	
            TaskInformation taskInfo = _worklistController.getTaskInformation(
                    checkedOutItem.getSpecificationID(), checkedOutItem.getTaskID(),
                    sessionHandle);
            
	        //if (getServletContext().getInitParameter("debug").compareTo("true") == 0){
		        if (request.getParameter("FormType").compareTo("Xform") == 0) {
	
		            wip.executeWorkItemPost(getServletContext(), checkedOutItem.getID(),
		                    sessionHandle, _worklistController, userID, session.getId());
					
		            String url = wip.getRedirectURL(getServletContext(), taskInfo, session.getId());        
		            response.sendRedirect(response.encodeURL(url));
		            return;
		        } 
	        //}
	        else if (request.getParameter("FormType").compareTo("HTMLform") == 0) {
	        	
				String schema = wip.createSchema(workItemID, sessionHandle, _worklistController);

                SpecificationData specData = _worklistController.getSpecificationData(taskInfo.getSpecificationID(), sessionHandle);

				InstanceBuilder ib = new InstanceBuilder(schema, taskInfo.getDecompositionID(), checkedOutItem.getDataListString());

				
	        	String form = wip.getHTMLFormName(taskInfo);
	        	session.setAttribute("outputData", checkedOutItem.getDataListString());
				
	        	response.sendRedirect(response.encodeURL(getServletContext().getInitParameter("HTMLForms")+"/"+form+"?userID="+userID+"&workItemID="+checkedOutItem.getID()+"&sessionHandle="+sessionHandle+"&JSESSIONID="+session.getId()+"&submit=htmlForm"));
	        	return;
	        }
	        else {
	            request.setAttribute("failure", checkedOutItem);
	        }
	    }
    }
%><html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Available Work</title>
        <!-- Include file to load init method -->
        <%@include file="head.jsp"%>
	</head>
	
	<body>
        <!-- Include check login code -->
        <%@include file="checkLogin.jsp"%>
        <!-- Include YAWL Banner Information -->
        <%@include file="banner.jsp"%>
        <h3>Available Work Items</h3>
        <%
        if(request.getAttribute("failure") != null) {
            String message = (String) request.getAttribute("failure");
            message = WorkListGUIUtils.removeFailureTags(message);
            message = WorkListGUIUtils.convertToEscapes(message);
            %>
            <font color='red'><em><%= message %></em></font>
            <%
        }
        %>
        <form method="post" action="" name="availableForm">
        <table border="0" cellspacing="2" cellpadding="0">
            <tr>
                <td height="30" width="50" align="center"></td>
                <td width="1"/>
                <td width="180" align="center"><em>Task Name</em></td>
                <td bgcolor="#000000" width="1"/>
                <td width="180" align="center"><em>Task ID</em></td>
                <td bgcolor="#000000" width="1"/>
                <td width="180" align="center"><em>Status</em></td>
                <td bgcolor="#000000" width="1"/>
                <td width="180" align="center"><em>Enablement Time</em></td>
            </tr>
            <tr align="center">
                <td bgcolor="#000000" height="1"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
            </tr>
            <%
            if(session.getAttribute("sessionHandle") != null) {
                List workItems = _worklistController.getAvailableWork(
                        (String)session.getAttribute("userid"),
                        (String)session.getAttribute("sessionHandle"));
                Iterator iter = workItems.iterator();
                while(iter.hasNext()) {
                    WorkItemRecord item = (WorkItemRecord) iter.next();
                    //String id = item.getID();
                    String id = null;
                    TaskInformation taskInfo = _worklistController.getTaskInformation(
                        item.getSpecificationID(),
                        item.getTaskID(),
                        (String)session.getAttribute("sessionHandle"));
                    
                    if(taskInfo != null){
                    	id = taskInfo.getTaskName();
                    	
                        if (item.getStatus().equals("Suspended")) {
                    %>
                          <tr>
                              <td height="30" align="center">&nbsp;&nbsp;</td>
                              <td/>
                              <td align="center"><%= item.getID() %></td>
                    <% 
						} else { 
					%>
                             <td height="30" align="center">&nbsp;&nbsp;</td>
                             <td/>
                             <td align="center">
                                <a href="<%= contextPath %>/availableWork?workItemID=<%= item.getID() %>&FormType=HTMLform"><%= id %></a>
                             </td>
                    <% 
						} 
					%>
                        <td/>
                        <td align="center"><%= taskInfo.getTaskID() %></td>
                        <td/>
                        <td align="center"><%= item.getStatus() %></td>
                        <td/>
                        <td align="center"><%= item.getEnablementTime() %></td>
                    </tr>
                    <%
                    }
                }
            }
            %>
        </table>
        <table border="0" cellspacing="20">
            <tr>
                <td><input name=" Clear " type="reset"/></td>
                <%
                if (_ixURI != null) {
                %>
                    <td><input type="submit" name="submit" value="Raise Exception"
                        onClick="return isCompletedForm('availableForm', 'workItemID')"/></td>
                <%
                }
                %>
            </tr>
        </table>
        </form>
        <%@include file="footer.jsp"%>
    </body>
</html>