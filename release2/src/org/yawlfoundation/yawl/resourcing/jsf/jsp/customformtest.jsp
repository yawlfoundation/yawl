<%@ page import="org.jdom.Element" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.WorkItemRecord" %>

<!-- *  A simple custom form example and usage guide.
     *  author Michael Adams
     *  version 2.0, 09/07/2008  -->

<%
    // The whole workitem record is passed via a session attribute.
    // get the WorkItemRecord and read its data tree.
    WorkItemRecord wir = (WorkItemRecord) session.getAttribute("workitem");
    Element data = wir.getDataList();

    // Here we read new values as entered on this form (see the html below)
    String input = request.getParameter("input");

    // if the value(s) on this form have been updated, post them back to the worklist.
    // Note: the variable used in this example is called 'var' - modify as required.
    String varName = "var";
    if (input != null) {
        if (data != null) {

            // repeat this part for each item to be updated
            Element dataitem = data.getChild(varName);        // get data var
            if (dataitem != null) {
                dataitem.setText(input);                      // update data var's value

                // once all the data in the workitem is updated, update the session attr.
                session.setAttribute("workitem", wir);

                // if you want the workitem to complete when it posts back, set this
                // attribute to true; if it's false or commented out, the workitem will
                // update and remain on the worklist's 'started' queue
                session.setAttribute("complete_on_post", true);

                // now we can redirect back to the worklist
                String redirectURL = "http://localhost:8080/resourceService/" +
                                     "faces/userWorkQueues.jsp";
                response.sendRedirect(response.encodeURL(redirectURL));
            }
            else {
                out.println("This workitem does not contain a variable called '"
                                   + varName + "'.");
            }
        }
        else {
            out.println("This workitem does not contain any data for updating.");
        }
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
                <td height="30" width="150" align="center">
                    <%=(wir != null) ? wir.getCaseID() : "null"%></td>
                <td/>
            </tr>
        </table>

        <p>Please enter a new value</p>

        <table border="0" cellspacing="2" cellpadding="5">
            <tr>
                <td valign="top" width="150"><b>Value:</b></td>
                <td><INPUT NAME="input"
                           VALUE="<%=(data != null) ? data.getChildText(varName) : "null"%>">
                </td>
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