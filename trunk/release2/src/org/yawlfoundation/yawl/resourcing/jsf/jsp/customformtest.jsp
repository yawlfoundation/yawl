<%@ page import="org.jdom.Element" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.Marshaller" %>
<%@ page import="org.yawlfoundation.yawl.engine.interfce.WorkItemRecord" %>

<!-- *  A simple custom form example and usage guide.
     *  author Michael Adams
     *  version 2.0, 09/07/2008  -->

<%
    // The whole workitem record is passed via a request parameter as an XML string.
    // Turn it back into a WIR (for ease of use) and gets its data tree.
    String workItemXML = request.getParameter("workitem");
    WorkItemRecord wir;

    // workItemXML won't be null on the first call from the worklist handler
    if (workItemXML != null) {
        wir = Marshaller.unmarshalWorkItem(workItemXML) ;
        session.setAttribute("workitem", wir);                  // save it for the post
    }

    // if it is null, it's after a 'submit' and the request param is lost,
    // so retreive the wir from the session attribute saved earlier
    else {
        wir = (WorkItemRecord) session.getAttribute("workitem");
    }

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
                
                // pass the updated wir back to the calling worklist page;
                // must convert it back to XML in the process.
                String redirectURL = "http://localhost:8080/resourceService/" +
                                     "faces/userWorkQueues.jsp?workitem=" + wir.toXML();
                response.sendRedirect(response.encodeURL(redirectURL));
            }
            else {
                System.out.println("This workitem does not contain a variable called '"
                                   + varName + "'.");
            }
        }
        else {
            System.out.println("This workitem does not contain any data for updating.");
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