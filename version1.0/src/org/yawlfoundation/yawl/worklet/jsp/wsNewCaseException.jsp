<%@ page import="org.yawlfoundation.yawl.worklet.admin.AdministrationTask"%>

<!-- *  author Michael Adams
     *  version 0.8, 04-09/2006  -->

<%
    String caseID = request.getParameter("caseID");
    String title = request.getParameter("title");
    String scenario = request.getParameter("scenario");
    String process = request.getParameter("process");

    String specID = _exceptionService.getSpecIDForCaseID(caseID);

    if ((title != null) && (scenario != null) && (process != null)) {
        _exceptionService.addAdministrationTask(caseID, title, scenario, process,
                                  AdministrationTask.TASKTYPE_CASE_EXTERNAL_EXCEPTION);
       // go back to YAWL worklist
       response.sendRedirect(response.encodeURL("/worklist/availableWork") );
     }

%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Worklet Service : Define New External Exception for Case</title>

    <!-- Include file to load init method -->
    <%@ include file="wsHead.jsp" %>
</head>

<body>
    <!-- Include  Banner Information -->
    <%@ include file="wsBanner.jsp" %>

    <h3 align="center">Define New Case-Level Exception</h3>

    <form method="post" action="" name="newCaseException">

        <table align="center" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td height="30" width="150" align="center">
                    <font color="blue"><b>Case ID</b></font></td>
                <td width="180" align="center">
                    <font color="blue"><b>Specification ID</b></font></td>
            </tr>
            <tr>
                <td height="30" width="150" align="center"><%= caseID%></td>
                <td width="180" align="center"><%= specID%></td>
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