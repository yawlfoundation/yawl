<%@ page import="au.edu.qut.yawl.worklet.admin.AdministrationTask"%>

<!-- *  author Michael Adams
     *  BPM Group, QUT Australia
     *  m3.adams@qut.edu.au
     *  version 0.8, 04-09/2006  -->

<%
    String caseID = request.getParameter("caseID");
    String specID = _exceptionService.getSpecIDForCaseID(caseID);
    boolean isWorklet = _exceptionService.isWorkletCase(caseID) ;

    String title = request.getParameter("title");
    String scenario = request.getParameter("scenario");

    if ((title != null) && (scenario != null)) {
         _exceptionService.addAdministrationTask(caseID, title, scenario, null,
                                      AdministrationTask.TASKTYPE_REJECTED_SELECTION);
        // go back to YAWL spec list
        response.sendRedirect(response.encodeURL("/worklist/viewSpecifications") );
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
            location="/worklist/viewSpecifications";
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
                <td valign="top"><b>Reason for Rejection:</b></td>
                <td><TEXTAREA NAME="scenario" COLS=68 ROWS=4></TEXTAREA></td>
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