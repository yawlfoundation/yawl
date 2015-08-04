<%@ page import="org.yawlfoundation.yawl.engine.YSpecificationID"%>
<%@ page import="org.yawlfoundation.yawl.worklet.admin.AdministrationTask" %>

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
    YSpecificationID specID = _exceptionService.getSpecIDForCaseID(caseID);
    boolean isWorklet = _exceptionService.isWorkletCase(caseID) ;

    String submit = request.getParameter("submit");
    if ((submit != null) && (submit.equals("Cancel"))) {
        response.sendRedirect(response.encodeURL(_caseMgtURL));
        return;
    }    

    String title = request.getParameter("title");
    String scenario = request.getParameter("scenario");

    if ((title != null) && (title.length() > 0) &&
        (scenario != null) && (scenario.length() > 0)) {
         _exceptionService.addAdministrationTask(caseID, title, scenario, null,
                                      AdministrationTask.TASKTYPE_REJECTED_SELECTION);
        // go back to YAWL spec list
        response.sendRedirect(response.encodeURL(_caseMgtURL));
    }

%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Worklet Service : Reject Worklet Selection</title>

    <!-- Include file to load init method -->
    <%@ include file="wsHead.jsp" %>

<%  if (! isWorklet) { %>

       <script language="javascript">
           <!--
            alert("Can't reject this case - it is not a worklet!");
            location="/resourceService/faces/caseMgt.jsp";
           //-->
       </script>

<%  }  %>

</head>

<body>
     <!-- Include Banner Information -->
    <%@ include file="wsBanner.jsp" %>

    <form method="post" action="" name="rejectWorklet">

        <h3 align="center">Reject Worklet Selection</h3>

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

        <p>Please complete each of the fields below:</p>

        <table border="0" cellspacing="2" cellpadding="5">
            <tr>
                <td valign="top" width="150"><b>Proposed Title:</b></td>
                <td><INPUT NAME="title" SIZE="90"></td>
            </tr>
            <tr>
                <td valign="top"><b>Reason for Rejection:</b></td>
                <td><TEXTAREA NAME="scenario" COLS=68 ROWS=4></TEXTAREA></td>
            </tr>
        </table>

        <table border="0" cellspacing="0">
            <%--<tr><td height="30" width="100"/></tr>--%>
            <tr>
                <td>
                    <input type="submit" name="submit" value="Cancel"/>
                </td>
                <td>
                    <input type="submit" value="Submit"/>
                </td>
            </tr>
        </table>
     </form>

    <%@ include file="wsFooter.jsp" %>
</body>
</html>