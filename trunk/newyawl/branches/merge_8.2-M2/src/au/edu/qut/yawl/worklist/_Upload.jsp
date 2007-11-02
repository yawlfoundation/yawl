<%@ page import="au.edu.qut.yawl.worklist.model.WorkListGUIUtils,
                 java.io.*,
                 au.edu.qut.yawl.worklist.model.Marshaller"%><html
                 xmlns="http://www.w3.org/1999/xhtml">
    <head>
		<title>YAWL Upload</title>
        <!-- Include file to load init method -->
        <%@include file="head.jsp"%>
	</head>
	<body>
        <!-- Include check login code -->
        <%@ include file="checkLogin.jsp"%>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp"%>
        <br/>
        <br/>
        <br/>
        <br/>
        <br/>
<%
    StringBuffer result = new StringBuffer();
    ServletInputStream in = request.getInputStream();

    int i = in.read();
    while (i != -1) {
        result.append((char) i);
        i = in.read();
    }
    String filepath = result.substring(
            result.indexOf("filename=\"") + 10,
            result.indexOf("Content-Type") -2 );
    String filename = filepath.substring(
            filepath.lastIndexOf(File.separator) + 1,
            filepath.length() -1);

    int beginOfFile = result.indexOf("<?xml");
    int endOfFile = result.indexOf("</specificationSet>");
    if(beginOfFile != -1 && endOfFile != -1){
        String resultStr = result.substring(
            beginOfFile,
            endOfFile + 19);

        boolean mayAdminister = _worklistController.checkConnectionForAdmin(sessionHandle);
        if(sessionHandle != null && mayAdminister){
            String htmlErr = "";
            String replyFromYAWL =
                    _worklistController.uploadSpecification(resultStr, filename, sessionHandle);
            if(_worklistController.successful(replyFromYAWL)){
                RequestDispatcher toAdminPage = application.getRequestDispatcher("/admin");
                toAdminPage.forward(request, response);
            } else {
                htmlErr += WorkListGUIUtils.convertUploadErrorMsg(replyFromYAWL);
            }
            %>
            <table border="0" cellspacing="10" bgColor="LightGrey">
                <font color="red">  A problem occurred while uploading the specification.</font>
                <%= htmlErr %>
            </table>
            <%
        } else {
            %>
                <font color="red">You were not granted upload access to the engine.
                Try logging in as an administrator.</font>
            <%
        }
    } else {
    %>
        <%=
            "<font color=\"red\">Either no file was specified" +
            " or the specified file was not in the YAWL syntax.<font>"
        %>
    <%
    }
%>
    <%@include file="footer.jsp"%>
    </body>
</html>