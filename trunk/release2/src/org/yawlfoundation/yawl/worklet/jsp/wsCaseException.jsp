<%@ page import="org.yawlfoundation.yawl.engine.YSpecificationID" %>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.List" %>

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
    String caseID = request.getParameter("caseID");
    String triggerID = request.getParameter("trigger");
    YSpecificationID specID = _exceptionService.getSpecIDForCaseID(caseID);

    String submit = request.getParameter("submit");
    if ((submit != null) && (submit.equals("Cancel"))) {
        response.sendRedirect(response.encodeURL(_caseMgtURL));
        return;
    }

    if (triggerID != null) {
        if (triggerID.equals("newExternalException")) {
            String url = "/workletService/newCaseException?caseID=" + caseID;
            response.sendRedirect(response.encodeURL(url));
        }
        else {
            _exceptionService.raiseExternalException("case", caseID, triggerID);
            response.sendRedirect(response.encodeURL(_caseMgtURL));
        }
    }

%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Worklet Service : Raise Case-Level External Exception</title>

    <!-- Include file to load init method -->
    <%@ include file="wsHead.jsp" %>
</head>

<body>
     <!-- Include Banner Information -->
    <%@ include file="wsBanner.jsp" %>

    <form method="post" action="" name="caseException">

        <h3 align="center">Raise Case-Level Exception</h3>

        <table align="center" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td height="30" width="150" align="center">
                    <font color="blue"><b>Case ID</b></font></td>
                <td width="180" align="center">
                    <font color="blue"><b>Specification ID</b></font></td>
            </tr>
            <tr>
                <td height="30" width="150" align="center"><%= caseID%></td>
                <td width="180" align="center"><%= specID.toString()%></td>
                <td/>
            </tr>
        </table>

<%
    List triggers = _exceptionService.getExternalTriggersForCase(caseID);

    if (triggers == null) {
%>
        <p><font color="red">
            No case-level external exceptions currently defined for this case.
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
                           onClick="return isCompletedForm('caseException', 'trigger')"/>
                </td>
            </tr>
        </table>
    </form>

    <%@ include file="wsFooter.jsp" %>
</body>
</html>