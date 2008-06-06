<%@ page import="org.jdom.Element" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.Marshaller" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.WorkItemRecord" %>

<!-- *  A simple custom form example and usage guide.
     *  author Michael Adams
     *  version 2.0, 28/05/2008  -->

<%
    // The whole workitem record is passed via a session attribute as an XML string.
    // Turn it back into a WIR (for ease of use) and gets its data tree.
    String workItemXML = (String) session.getAttribute("workitem");
    WorkItemRecord wir = Marshaller.unmarshalWorkItem(workItemXML) ;
    Element data = wir.getDataList();

    // Here we read new values as entered on this form
    String input = request.getParameter("input");

    // if the value(s) on this form have been updated, post them back to the worklist.
    // Note: the variable used in this example is called 'vary' - modify as required.
    if (input != null) {
        data.getChild("vary").setText(input);                // update data var's value

        // set the session attribute to the updated WIR; must convert it back to XML
        // in the process.
        session.setAttribute("workitem", wir.toXML());

        // go back to sending workqueues page
        String redirectURL = "http://localhost:8080/resourceService/faces/userWorkQueues.jsp";
        response.sendRedirect(response.encodeURL(redirectURL));
    }
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>A Simple Custom Form</title>

</head>

<body>

    <h3 align="center">The CaseID Passed</h3>

    <form method="post" action="" name="customformtest">

        <table align="center" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td height="30" width="150" align="center">
                    <font color="blue"><b>Case ID</b></font></td>
             </tr>
            <tr>
                <td height="30" width="150" align="center"><%=wir.getCaseID()%></td>
                <td/>
            </tr>
        </table>

        <p>Please enter a new value</p>

        <table border="0" cellspacing="2" cellpadding="5">
            <tr>
                <td valign="top" width="150"><b>Value:</b></td>
                <td><INPUT NAME="input" VALUE="<%=data.getChildText("vary")%>"></td>
            </tr>
         </table>

        <table border="0" cellspacing="20" width=70%>
            <tr>
                <td align="center"><input value="Submit" type="submit"/></td>
            </tr>
        </table>
    </form>
</body>
</html>