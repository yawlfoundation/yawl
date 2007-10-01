<%@ page import="org.jdom.output.XMLOutputter,
                 org.jdom.output.Format,
                 org.jdom.input.SAXBuilder,
                 org.jdom.Document,
                 java.io.StringReader,
                 au.edu.qut.yawl.worklist.model.WorkListGUIUtils"%><head>
		<title>Schema Validation Problem</title>
        <!-- Include file to load init method -->
        <%@ include file="head.jsp"  %>
	</head>
	<body>
        <!-- Include check login code -->
        <%@ include file="checkLogin.jsp" %>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp" %>
        <h3>Schema Validation Problem</h3>

            Submission of data has been unsucessful because:
            <%
                String outcome = (String) session.getAttribute("outcome");
                session.removeAttribute("outcome");

            //remove response guff from message
            outcome = WorkListGUIUtils.removeFailureTags(outcome);

            if(outcome.indexOf("<xsd:schema") != -1){
                String errorMsg = outcome.substring(
                        0,
                        outcome.indexOf("SCHEMA ="));
                %>
                <br/><%= errorMsg %><br/><br/>
                <%
                String schema = outcome.substring(
                    outcome.indexOf("<?xml"),
                    outcome.lastIndexOf("</xsd:schema>") + 13);
                String data = outcome.substring(
                        outcome.lastIndexOf("DATA = ") + 7,
                        outcome.lastIndexOf('>') + 1);
                String error = outcome.substring(
                        outcome.lastIndexOf("ERRORS ="),
                        outcome.length());

                XMLOutputter outputter = new XMLOutputter(
                        Format.getPrettyFormat());
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(new StringReader(schema));
                schema = outputter.outputString(doc);
                System.out.println("new XMLOutputter(Format.getPrettyFormat()).outputString(data) = " + (data));
                doc = builder.build(new StringReader(data));
                data = outputter.outputString(doc);
                %>
<table border="0" cellspacing="10" width="80%" bgcolor="blue">
<tr>
<td>
<table border="0" cellspacing="10" bgColor="LightGrey">
    <tr>
        <td>Schema for output</td>
    </tr>
    <tr>
        <td><textArea
            name="schema"
            readonly="readonly"
            rows="22" cols="49"
            style="font-size : 87%;"
            class="leftArea"><%= schema %></textArea></td>
    </tr>
</table>
</td>
<td>
<table>
    <tr>
        <td>
            <table border="0" cellspacing="10" bgColor="LightGrey">
                <tr>
                    <td>Output Data Submitted</td>
                </tr>
                <tr>
                    <td><textArea
                        name="outputdata"
                        readonly="readonly"
                        rows="9"
                        cols="49"
                        style="font-size : 87%;"><%= data %></textArea></td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td>
            <table border="0" cellspacing="10" bgColor="LightGrey">
                <tr>
                    <td>Validation Error</td>
                </tr>
                <tr>
                    <td><textArea
                        name="outputdata"
                        readonly="readonly"
                        rows="9"
                        cols="49"
                        style="font-size : 87%;"><%= error  %></textArea></td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</td>
</tr>
</table>
            <p>Usage Note: If the Schema has many complex types,
            or if you are unsure of how to get the data to pass
            validation then we recommend that you install the automatic
            forms generation module for YAWL.<br/>
            Otherwise you should copy the schema from this page and use
            it as a base in a schema aware XML development IDE (XML Spy
            provide a Home Edition for free - at time of writing).<p>
            <%
            } else {
                outcome = WorkListGUIUtils.convertToEscapes(outcome);
            %>
            <font color="red"><%= outcome %>.</font>
            <%
            }


        %>
        <%@include file="footer.jsp"%>
    </body>
</html>