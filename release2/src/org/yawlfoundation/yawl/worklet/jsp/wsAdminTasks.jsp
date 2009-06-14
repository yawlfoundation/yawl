<%@ page import="org.yawlfoundation.yawl.worklet.admin.AdministrationTask,
                 java.util.Iterator" %>

<!-- *  author Michael Adams
     *  version 0.8, 04-09/2006  -->

<%
    String wsTaskID = request.getParameter("wsTask");
    String buttonText = request.getParameter("submit");

    if ((buttonText != null) && (buttonText.equals("Back"))) {
        response.sendRedirect(response.encodeURL(_caseMgtURL));   
    }

    if (wsTaskID != null) {
        if (buttonText != null) {
            if (buttonText.equals("View Details")) {
                String url = "/workletService/wsAdminTaskDetail?id=" + wsTaskID;
                response.sendRedirect(response.encodeURL(url));
            }
            else if (buttonText.equals("Completed")){
                _exceptionService.completeAdminTask(wsTaskID);
            }
        }
    }    
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Worklet Service : Administration Tasks</title>

    <!-- Include file to load init method -->
    <%@ include file="wsHead.jsp" %>
</head>

<body>
<!-- Include YAWL Banner Information -->
<%@ include file="wsBanner.jsp" %>

<h3 align="center">Worklet Service Administration Tasks</h3>

<form method="post" action="" name="wsAdminTasks">

    <table border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td height="30" width="50" align="center"></td>
            <td width="1"/>
            <td width="250" align="center"><em>Title</em></td>
            <td bgcolor="#000000" width="1"/>
            <td width="100" align="center"><em>Case ID</em></td>
            <td bgcolor="#000000" width="1"/>
            <td width="300" align="center"><em>Task Type</em></td>
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
            String taskTypeStr = null;
            int taskType  ;
            AdministrationTask wsTask = null;
            Iterator list = _exceptionService.getAllAdminTasksAsList().iterator();

            while (list.hasNext()) {
                wsTask = (AdministrationTask) list.next();
                wsTaskID = wsTask.getID();
                taskType = wsTask.getTaskType();
                if (taskType == AdministrationTask.TASKTYPE_CASE_EXTERNAL_EXCEPTION)
                   taskTypeStr = "New Case-Level External Exception";
                else if (taskType == AdministrationTask.TASKTYPE_ITEM_EXTERNAL_EXCEPTION)
                   taskTypeStr = "New Item-Level External Exception";
                else if (taskType == AdministrationTask.TASKTYPE_REJECTED_SELECTION)
                   taskTypeStr = "Rejected Worklet Selection";
        %>
        <tr>
            <td height="30" align="center">
                <input type="radio" name="wsTask" value="<%= wsTaskID %>"/></td>
            <td/>
            <td align="center" width="250"><%= wsTask.getTitle() %></td>
            <td/>
            <td align="center" width="100"><%= wsTask.getCaseID() %></td>
            <td/>
            <td align="center" width="300"><%= taskTypeStr %></td>
        </tr>
        <%
            }

            if (wsTask == null) {                                 // no triggers found
        %>
        <tr>
            <td><font color="red">No outstanding administration tasks found.</font></td>
        </tr>
        <% }
        %>
    </table>

    <table border="0" cellspacing="20">
        <tr>
            <td><input value="Back" name="submit" type="submit"/></td>
            <td><input value="View Details" name="submit" type="submit"
                       onClick="return isCompletedForm('wsAdminTasks', 'wsTask')"/></td>
            <td><input value="Completed" name="submit" type="submit"
                       onClick="return isCompletedForm('wsAdminTasks', 'wsTask')"/></td>
        </tr>
    </table>
</form>
<%@ include file="wsFooter.jsp" %>
</body>
</html>