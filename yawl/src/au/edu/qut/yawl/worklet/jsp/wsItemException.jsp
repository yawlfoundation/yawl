<%@ page import="java.util.Iterator" %>
<%@ page import="au.edu.qut.yawl.worklist.model.WorkItemRecord"%>
<%@ page import="java.util.List"%>

<!-- *  author Michael Adams
     *  BPM Group, QUT Australia
     *  m3.adams@qut.edu.au
     *  version 0.8, 04-09/2006  -->

<%
    String triggerID = request.getParameter("trigger");
    String workItemID  = request.getParameter("workItemID");
    WorkItemRecord wir = _exceptionService.getWorkItemRecord(workItemID);
    String taskName = _exceptionService.getDecompID(wir) ;

    if (triggerID != null) {
        if (triggerID.equals("newExternalException")) {
            String url = "/workletService/newItemException?itemID=" + workItemID;
            response.sendRedirect(response.encodeURL(url));
        }
        else {
            _exceptionService.raiseExternalException("item", workItemID, triggerID);
            response.sendRedirect(response.encodeURL("/worklist/availableWork") );
        }
    }
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Worklet Service : Raise Item-Level External Exception</title>

    <!-- Include file to load init method -->
    <%@ include file="wsHead.jsp" %>
</head>

<body>
     <!-- Include Banner Information -->
    <%@ include file="wsBanner.jsp" %>

    <form method="post" action="" name="itemException">

    <h3 align="center">Raise Item-Level Exception</h3>

    <table align="center"  border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td  height="30" width="180" align="center">
                <font color="blue"><b>WorkItem ID</b></font></td>
            <td width="180" align="center">
                <font color="blue"><b>Specification ID</b></font></td>
            <td width="180" align="center">
                <font color="blue"><b>Case ID</b></font></td>
            <td width="180" align="center">
               <font color="blue"><b>Task Name</b></font></td>
        </tr>
        <tr>
            <td height="30" width="180" align="center"><%= wir.getID()%></td>
            <td  width="180" align="center"><%= wir.getSpecificationID()%></td>
            <td  width="180" align="center"><%= wir.getCaseID()%></td>
            <td  width="180" align="center"><%= taskName%></td>
        </tr>
        <tr align="center">
            <td bgcolor="#000000" height="1"/>
            <td bgcolor="#000000"/>
            <td bgcolor="#000000"/>
            <td bgcolor="#000000"/>
        </tr>
    </table>

<%
        List<String> triggers = _exceptionService.getExternalTriggersForItem(workItemID);

        if (triggers == null) {
%>
            <p><font color="red">
                No item-level external exceptions currently defined for this task.
            </font></p>
<%
         }
%>

        <p>Select the type of exception that has occurred:</p>

        <table border="0" cellspacing="0" cellpadding="0">

<%
    if (triggers != null) {
        for (String trigger : triggers) {
%>
            <tr>
                <td height="30" width="30"/>
                <td>
                    <input type="radio" name="trigger" value="<%= trigger %>"/>
                </td>
                <td width="3"/>
                <td><%= trigger %>
                </td>
                <td/>
            </tr>
<%
        }
    }
%>
            <tr>
                <td height="30" width="50"/>
                <td>
                    <input type="radio" name="trigger" value="newExternalException"/></td>
                <td width="3"/>
                <td><em>New External Exception...</em></td>
                <td/>
            </tr>
        </table>

        <table border="0" cellspacing="0">
            <tr><td height="30" width="150"/></tr>
            <tr>
                <td align="center" width="150"><input value="Submit" type="submit"
                          onClick="return isCompletedForm('itemException', 'trigger')"/>
                </td>
            </tr>
        </table>
    </form>

    <%@ include file="wsFooter.jsp" %>
</body>
</html>

