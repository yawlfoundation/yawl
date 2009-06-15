<%@ page import="org.jdom.Element" %>
<%@ page import="org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGatewayClient" %>
<%@ page import="org.jdom.output.XMLOutputter" %>
<%@ page import="org.jdom.output.Format" %>
<%@ page import="org.jdom.input.SAXBuilder" %>
<%@ page import="java.io.StringReader" %>

<!-- *  A simple custom form example and usage guide.
     *  author Michael Adams
     *  version 2.0, 06/2009  -->

<%
    // the default worklist URL - change it to match your installed path.
    String redirectURL = "http://localhost:8080/resourceService/faces/userWorkQueues.jsp";

    // If the Cancel button has been clicked on the form below, clean up any session
    // attributes we set in our code below, then return directly to the worklist
    String submit = request.getParameter("submit");
    if ((submit != null) && (submit.equals("Cancel"))) {
        session.removeAttribute("itemXML");
        session.removeAttribute("workitem");
        session.removeAttribute("handle");

        response.sendRedirect(response.encodeURL(redirectURL));
        return;
    }

    // To retrieve the workitem xml, use a workqueue gateway client object.
    // The url shown is a default - change it to match your installed path.
    // If your forms are outside the resource service's webapp dir, place the
    // YResourceServiceClient.jar in the same location - it contains all the necessary
    // classes to communicate through the gateway
    String wqURL = "http://localhost:8080/resourceService/workqueuegateway";
    WorkQueueGatewayClient wqClient = new WorkQueueGatewayClient(wqURL);

    // get the workitem xml. If the form has refreshed, it will be stored in a session
    // attribute (see below). If not, get it from the gateway
    String itemXML = (String) session.getAttribute("itemXML");
    if (itemXML == null) {

        // The workitem id and user's session handle are passed as parameters. Use them
        // to get an xml record of the workitem.
        String itemid = request.getParameter("workitem");
        String handle = request.getParameter("handle");

        itemXML = wqClient.getWorkItem(itemid, handle);
        session.setAttribute("itemXML", itemXML);
        session.setAttribute("workitem", itemid);
        session.setAttribute("handle", handle);
    }

    // the workitem's data is found in the <data> 1st level element. Any appropriate
    // xml parsing method can be used. Here we use JDOM (and ignore possible exceptions
    // for simplicity).
    Element wir = new SAXBuilder().build(new StringReader(itemXML)).getRootElement();
    Element data = wir.getChild("data");

    // one level down from data is the actual workitem data tree
    Element wirData = (Element) data.getChildren().get(0);

    System.out.println(wirData.getChildText("var"));

    // if there was a problem getting the workitem's xml, the xml will contain an
    // error message instead. It can be tested like this:
    String error = null;
    if (! wqClient.successful(itemXML)) {
        // show the message to the user in an appropriate way. In this case, we'll
        // simply show it on the form below
        error = itemXML;
    }
    else {

        // Here we read new values as entered on this form (see the html below)
        String input = request.getParameter("input");

        // if the value(s) on this form have been updated, post them back to the worklist.
        // Note: the variable used in this example is called 'var' - modify as required.
        String varName = "var";
        if (input != null) {
            if (wirData != null) {

                // repeat this part for each item to be updated. Note that this example's
                // workitem only uses simple data type variables - if your form uses
                // complex data types, fill the data tree hierarchically according to
                // the data type's structure
                Element dataitem = wirData.getChild(varName);        // get data var
                if (dataitem != null) {
                    dataitem.setText(input);                  // update data var's value

                    // once the data updates are complete, update the workitem's data via
                    // the gateway.
                    String itemid = (String) session.getAttribute("workitem");
                    String handle = (String) session.getAttribute("handle");
                    String dataString = new XMLOutputter(Format.getCompactFormat()).outputString(wirData);
                    String result = wqClient.updateWorkItemData(itemid, dataString, handle);

                    // check all is ok - if so, close the form
                    if (wqClient.successful(result)) {

                        // clean up our stored session attributes
                        session.removeAttribute("itemXML");
                        session.removeAttribute("workitem");
                        session.removeAttribute("handle");

                        // now we can redirect back to the worklist.
                        // if you want the workitem to complete when it posts back, add
                        // the parameter below and set it to 'true'; if it's false or
                        // missing, the workitem will update but remain on the worklist's
                        // 'started' queue
                        redirectURL += "?complete=true";

                        response.sendRedirect(response.encodeURL(redirectURL));
                    }
                    else {
                        error = result;
                    }
                }
                else {
                    error = "This workitem does not contain a variable called '"
                                   + varName + "'.";
                }
            }
            else {
                error = "This workitem does not contain any data for updating.";
            }
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
                <td height="50" width="150" align="center">
                    <span style="color:red">
                        <%=(error != null) ? error : ""%></span>
                </td>
            </tr>
            <tr>
                <td height="30" width="150" align="center">
                    <span style="color:blue"><b>Case ID</b></span></td>
             </tr>
            <tr>
                <td height="30" width="150" align="center">
                    <%=(wir != null) ? wir.getChildText("caseid") : "null"%></td>
                <td/>
            </tr>
        </table>

        <p>Please enter a new value</p>

        <table border="0" cellspacing="2" cellpadding="5">
            <tr>
                <td valign="top" width="150"><b>Value:</b></td>
                <td><INPUT NAME="input"
                           VALUE="<%=(wirData != null) ? wirData.getChildText("var") : "null"%>">
                </td>
            </tr>
         </table>

        <table border="0" cellspacing="20" width="100">
            <tr>
                <td align="right">
                    <input type="submit" name="submit" value="Cancel"/>
                </td>
                <td align="center">
                    <input value="Submit" type="submit"/>
                </td>
            </tr>
        </table>
    </form>
</body>
</html>