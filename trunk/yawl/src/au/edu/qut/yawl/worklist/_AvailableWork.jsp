<%@ page import="java.util.Iterator,
                 au.edu.qut.yawl.worklist.model.WorkItemRecord,
                 au.edu.qut.yawl.worklist.model.TaskInformation,
                 java.util.List,
                 au.edu.qut.yawl.worklist.model.WorkListGUIUtils" %><%
    String workItemID  = request.getParameter("workItemID");
    if(workItemID != null){
        String msg = "";
        msg = _worklistController.checkOut(
                workItemID,
                (String)session.getAttribute("sessionHandle"));
//System.out.println("_AvailableWork.jsp:: check out item ["+workItemID+"] msg = " + msg);
        if(_worklistController.successful(msg)){
            //application.getRequestDispatcher("/checkedOut").forward(request, response);
        } else {
            request.setAttribute("failure", msg);
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
        <table border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td height="30" width="50" align="center"></td>
                <td width="1"/>
                <td width="150" align="center"><em>ID</em></td>
                <td bgcolor="#000000" width="1"/>
                <td width="180" align="center"><em>Task Description</em></td>
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
                    String id = item.getID();
                    String uniqueid = item.getUniqueID();
                    TaskInformation taskInfo = _worklistController.getTaskInformation(
                        item.getSpecificationID(),
                        item.getTaskID(),
                        (String)session.getAttribute("sessionHandle"));
                    if(taskInfo != null){
                    %>
                    <tr>
                        <td height="30" align="center"><input type="radio" name="workItemID"
                            value="<%= item.getID() %>"/></td>
                        <td/>
                        <td align="center">
                            <a
                              href="<%= contextPath %>/availableWork?workItemID=<%= item.getID() %>"><%= id %>
                            </a>
                        </td>
                        <td/>
                        <td align="center"><%= taskInfo.getTaskName() %></td>
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
                <td><input value=" Check Out " type="submit"
                    onClick="return isCompletedForm('availableForm', 'workItemID')"/></td>
                <td><input name=" Clear " type="reset"/></td>
            </tr>
        </table>
        </form>
        <%@include file="footer.jsp"%>
    </body>
</html>