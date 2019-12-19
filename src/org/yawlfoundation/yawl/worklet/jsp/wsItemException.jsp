<%@ page import="java.util.Iterator" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.WorkItemRecord"%>
<%@ page import="java.util.List"%>

<%--
  ~ Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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
    String triggerID = request.getParameter("trigger");
    String workItemID  = request.getParameter("workItemID");
    WorkItemRecord wir = _workletService.getExceptionService().getWorkItemRecord(workItemID);
    String taskName = wir.getTaskName() ;

    String submit = request.getParameter("submit");
    if ((submit != null) && (submit.equals("Cancel"))) {
        response.sendRedirect(response.encodeURL(_worklistURL));
        return;
    }

    if (triggerID != null) {
        if (triggerID.equals("newExternalException")) {
            String url = "/workletService/newItemException?itemID=" + workItemID;
            response.sendRedirect(response.encodeURL(url));
        }
        else {
            _workletService.getExceptionService().raiseExternalException("item", workItemID, triggerID);
            response.sendRedirect(response.encodeURL(_worklistURL));
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
            <td  width="180" align="center"><%= wir.getSpecURI()%></td>
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
        List triggers = _workletService.getExceptionService().getExternalTriggersForItem(workItemID);

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
            String trigger = null;
            Iterator list = triggers.iterator();
            while (list.hasNext()) {
                trigger = (String) list.next();
%>
                <tr>
                    <td height="30" width="30"/>
                    <td>
                        <input type="radio" name="trigger" value="<%= trigger %>"/>
                    </td>
                    <td width="3"/>
                    <td><%= trigger %></td>
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
            <tr><td height="30" width="100"/></tr>
            <tr>
                <td align="right" width="100">
                    <input type="submit" name="submit" value="Cancel"/>
                </td>
                <td align="center" width="100">
                    <input value="Submit" type="submit"
                          onClick="return isCompletedForm('itemException', 'trigger')"/>
                </td>
            </tr>
        </table>
    </form>

    <%@ include file="wsFooter.jsp" %>
</body>
</html>

