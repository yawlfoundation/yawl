<%@ page import="au.edu.qut.yawl.worklist.model.*"%>
<%
    String workItemID  = request.getParameter("workItemID");
    WorkItemRecord wir = _exceptionService.getWorkItemRecord(workItemID);
    String taskName = _exceptionService.getDecompID(wir) ;
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>External Exception for WorkItem</title>
    <!-- Include file to load init method -->
    <%@ include file="wsHead.jsp" %>

</head>

<body>
<!-- Include YAWL Banner Information -->
<%@ include file="wsBanner.jsp" %>
<tr align="center">

<h3>Item Exception Page</h3>
<table border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td height="30" width="50" align="center"></td>
        <td width="1"/>
        <td width="180" align="center"><em>WorkItem ID</em></td>
        <td bgcolor="#000000" width="1"/>
        <td width="180" align="center"><em>Specification ID</em></td>
        <td bgcolor="#000000" width="1"/>
        <td width="180" align="center"><em>Case ID</em></td>
        <td bgcolor="#000000" width="1"/>
        <td width="180" align="center"><em>Task Name</em></td>
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
    <tr>
        <td height="30" width="50" align="center"></td>
        <td/>
        <td height="30" align="center"><%= wir.getID()%></td>
        <td/>
        <td align="center"><%= wir.getSpecificationID()%></td>
        <td/>
        <td align="center"><%= wir.getCaseID()%></td>
        <td/>
        <td align="center"><%= taskName%></td>
    </tr>
</table>

<%@ include file="wsFooter.jsp" %>
</body>
</html>