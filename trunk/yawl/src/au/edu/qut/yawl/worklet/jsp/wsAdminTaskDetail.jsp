<%@ page import="au.edu.qut.yawl.worklet.admin.AdministrationTask"%>

<%
    String wsTaskID = request.getParameter("id");
    String buttonText = request.getParameter("submit");
    AdministrationTask wsTask = _exceptionService.getAdminTask(wsTaskID);

    if (buttonText != null)
        response.sendRedirect(response.encodeURL("/workletService/wsAdminTasks"));

    String taskTypeStr = null;
    int taskType = wsTask.getTaskType();
    if (taskType == AdministrationTask.TASKTYPE_NEW_EXTERNAL_TRIGGER)
        taskTypeStr = "New External Exception";
    else if (taskType == AdministrationTask.TASKTYPE_REJECTED_SELECTION)
        taskTypeStr = "Rejected Worklet Selection";

%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Worklet Service : Administration Task Detail</title>

    <!-- Include file to load init method -->
    <%@ include file="wsHead.jsp" %>
</head>

<body>
<!-- Include YAWL Banner Information -->
<%@ include file="wsBanner.jsp" %>


<h3 align="center">Worklet Administration Task Detail</h3>

<form method="post" action="" name="wsAdminTaskDetail">

    <table border="0" cellspacing="2" cellpadding="5">
        <tr>
            <td valign="top" width="150"><b>Task Type:</b></td>
            <td><%= taskTypeStr %></td>
        </tr>
        <tr>
            <td valign="top"><b>Title:</b></td>
            <td> <%= wsTask.getTitle() %></td>
        </tr>
        <tr>
            <td valign="top"><b>Scenario:</b></td>
            <td>  <%= wsTask.getScenario() %></td>
        </tr>
        <tr>
            <td valign="top"><b>Process Description:</b></td>
            <td><%= wsTask.getProcess() %></td>
        </tr>
    </table>

    <table border="0" cellspacing="20">
        <tr>
            <td><input value="Back" name="submit" type="submit" /></td>
        </tr>
    </table>
</form>
<%@ include file="wsFooter.jsp" %>
</body>
</html>