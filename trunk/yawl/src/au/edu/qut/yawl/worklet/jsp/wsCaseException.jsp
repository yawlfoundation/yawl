<%@ page import="java.util.Iterator" %>

<%
    String caseID = request.getParameter("caseID");
    String triggerID = request.getParameter("trigger");
    String specID = _exceptionService.getSpecIDForCaseID(caseID);

    if (triggerID != null) {
        if (triggerID.equals("newExternalException")) {
            String url = "/workletService/newCaseException?caseID=" + caseID;
            response.sendRedirect(response.encodeURL(url));
        }
        else {
            _exceptionService.raiseExternalException("case", caseID, triggerID);
            response.sendRedirect(response.encodeURL("/worklist/availableWork") );
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
                <td width="180" align="center"><%= specID%></td>
                <td/>
            </tr>
        </table>

        <p>Select the type of exception that has occurred:</p>

        <table border="0" cellspacing="0" cellpadding="0">

            <%
                String trigger = null;
                Iterator list = _exceptionService.getExternalTriggersForCase(caseID).iterator();

                while (list.hasNext()) {
                    trigger = (String) list.next();
            %>

            <tr>
                <td height="30" width="30"/>
                <td>
                    <input type="radio" name="trigger" value="<%= trigger %>"/></td>
                <td width="3"/>
                <td><%= trigger %></td>
                <td/>
            </tr>

            <%
                }

                if (trigger == null) {                              // no triggers found
            %>

            <h4>
                <font color="red">No external exceptions currently defined for case.</font>
            </h4>

            <%
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
                          onClick="return isCompletedForm('caseException', 'trigger')"/>
                </td>
            </tr>
        </table>
    </form>

    <%@ include file="wsFooter.jsp" %>
</body>
</html>