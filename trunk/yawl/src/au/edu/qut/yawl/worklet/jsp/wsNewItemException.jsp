<%@ page import="au.edu.qut.yawl.worklet.admin.AdministrationTask"%>
<%@ page import="au.edu.qut.yawl.worklist.model.WorkItemRecord"%>

<%
    String itemID = request.getParameter("itemID");
    WorkItemRecord wir = _exceptionService.getWorkItemRecord(itemID);
    String taskName = _exceptionService.getDecompID(wir) ;
    String caseID = wir.getCaseID();

    String title = request.getParameter("title");
    String scenario = request.getParameter("scenario");
    String process = request.getParameter("process");

    if ((title != null) && (scenario != null) && (process != null)) {
        _exceptionService.addAdministrationTask(caseID, itemID, title, scenario, process,
                                  AdministrationTask.TASKTYPE_ITEM_EXTERNAL_EXCEPTION);
       // go back to YAWL worklist
       response.sendRedirect(response.encodeURL("/worklist/availableWork") );
     }

%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Worklet Service : Define New External Exception for Item</title>

    <!-- Include file to load init method -->
    <%@ include file="wsHead.jsp" %>
</head>

<body>
    <!-- Include  Banner Information -->
    <%@ include file="wsBanner.jsp" %>

    <h3 align="center">Define New Item-Level Exception</h3>

    <form method="post" action="" name="newItemException">

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

        <p>Please complete each of the fields below:</p>

        <table border="0" cellspacing="2" cellpadding="5">
            <tr>
                <td valign="top" width="150"><b>Proposed Title:</b></td>
                <td><INPUT NAME="title" SIZE="90"></td>
            </tr>
            <tr>
                <td valign="top"><b>Scenario:</b></td>
                <td><TEXTAREA NAME="scenario" COLS=68 ROWS=4></TEXTAREA></td>
            </tr>
            <tr>
                <td valign="top"><b>Process Description:</b></td>
                <td><TEXTAREA NAME="process" COLS=68 ROWS=4></TEXTAREA></td>
            </tr>
        </table>

        <table border="0" cellspacing="20" width=70%>
            <tr>
                <td align="center"><input value="Submit" type="submit"/></td>
            </tr>
        </table>
    </form>
    <%@ include file="wsFooter.jsp" %>
</body>
</html>