<%@ page import="org.yawlfoundation.yawl.worklet.admin.AdministrationTask"%>

<%--
  ~ Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
  ~ The YAWL Foundation is a collaboration of individuals and
  ~ organisations who are committed to improving workflow technology.
  ~
  ~ This file is part of YAWL. YAWL is free software: you can
  ~ redistribute it and/or modify it under the terms of the GNU Lesser
  ~ General Public License as published by the Free Software Foundation.
  ~
  ~ YAWL is distributed in the hope that it will be useful, but WITHOUT
  ~ ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  ~ or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
  ~ Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public
  ~ License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
  --%>

<!-- *  author Michael Adams
     *  version 0.8, 04-09/2006  -->

<%
    String wsTaskID = request.getParameter("id");
    String buttonText = request.getParameter("submit");
    AdministrationTask wsTask = _exceptionService.getAdminTask(wsTaskID);

    if (buttonText != null) {
        response.sendRedirect(response.encodeURL("/workletService/wsAdminTasks"));
        return;
    }    

    String taskTypeStr = null;
    int taskType = wsTask.getTaskType();

    if (taskType == AdministrationTask.TASKTYPE_CASE_EXTERNAL_EXCEPTION)
       taskTypeStr = "New Case-Level External Exception";
    else if (taskType == AdministrationTask.TASKTYPE_ITEM_EXTERNAL_EXCEPTION)
       taskTypeStr = "New Item-Level External Exception";
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