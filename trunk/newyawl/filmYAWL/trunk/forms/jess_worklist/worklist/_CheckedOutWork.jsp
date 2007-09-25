<%@ page import="java.util.Iterator,
                 au.edu.qut.yawl.worklist.model.WorkItemRecord,
                 au.edu.qut.yawl.worklist.model.TaskInformation"%>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Checked Out Work</title>
        <!-- Include file to load init method -->
        <%@ include file="head.jsp"  %>

	</head>
	<body>
        <!-- Include check login code -->
        <%@ include file="checkLogin.jsp" %>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp"%>

        <h3>Checked Out Work Items</h3>
        <form method="post"
            action="<%= request.getContextPath() %>/workItemProcessor"
            name="checkedOutForm">
        <table cellpadding="0" cellspacing="2">
            <tr>
                <td height="30" width="50" align="left"></td>
                <td width="1"/>
                <td width="180" align="center"><em>Task Name</em></td>
                <td bgcolor="#000000" width="1"/>
                <td width="180" align="center"><em>Task ID</em></td>
                <td bgcolor="#000000" width="1"/>
                <td width="180" align="center"><em>Enablement Time</em></td>
                <td bgcolor="#000000" width="1"/>
                <td width="180" align="center"><em>Start Time</em></td>
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
            Iterator workItems = _worklistController.getActiveWork(
                (String)session.getAttribute("userid"),
                (String)session.getAttribute("sessionHandle")).iterator();
            while(workItems.hasNext()){
                WorkItemRecord item = (WorkItemRecord) workItems.next();
                TaskInformation taskInfo = _worklistController.getTaskInformation(
                    item.getSpecificationID(),
                    item.getTaskID(),
                    (String)session.getAttribute("sessionHandle"));
                String id = taskInfo.getTaskName();
            %>
                <tr>
                    <td height="30" align="center"><input type="radio" name="workItemID"
                        value="<%= id %>"/></td>
                    <td/>
                    
                    <td align="center">
                    <% if (getServletContext().getInitParameter("debug").compareTo("true") == 0){ %>
                    	XForm: <a href="<%= contextPath %>/workItemProcessor?submit=Edit Work Item&workItemID=<%= item.getID() %>&FormType=Xform"><%= id %></a>
                    	<br/><br/>
                    	<% } %>
                    	<a href="<%= contextPath %>/workItemProcessor?submit=Edit Work Item&workItemID=<%= item.getID() %>&FormType=HTMLform"><%= id %></a>
                    	<% if (getServletContext().getInitParameter("debug").compareTo("true") == 0){ %> <br/><br/> <% } %>
                    </td>
                    <td/>
                    
                    <td align="center"><%= taskInfo != null ?
                                            taskInfo.getTaskID() :
                                            null %></td>
                    <td/>
                    <td align="center"><%= item.getEnablementTime() %></td>
                    <td/>
                    <td align="center"><%= item.getStartTime() %></td>
                </tr>
            <%
            }
            %>
        </table>
        <br/>
        <p>
        <input type="submit" name="submit" value="Edit Work Item"
            onClick="return isCompletedForm('checkedOutForm', 'workItemID')"/>
        <input type="submit" name="submit" value="Add New Instance"
            onClick="return isCompletedForm('checkedOutForm', 'workItemID')"/>
        <input type="submit" name="submit" value="Suspend Task"
            onClick="return isCompletedForm('checkedOutForm', 'workItemID')"/>
        <input name=" Clear " type="reset"/>
        <%
        if (_ixURI != null) {
        %>
          <input type="submit" name="submit" value="Raise Exception"
               onClick="return isCompletedForm('availableForm', 'workItemID')"/>
        <%
         }
        %>

        </p>
        </form>
        <%@include file="footer.jsp"%>
    </body>
</html>
