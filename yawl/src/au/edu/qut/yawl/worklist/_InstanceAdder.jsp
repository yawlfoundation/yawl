<%@ page import="au.edu.qut.yawl.worklist.model.WorkItemRecord,
                 au.edu.qut.yawl.worklist.model.TaskInformation,
                 au.edu.qut.yawl.worklist.model.YParametersSchema,
                 au.edu.qut.yawl.elements.data.YParameter,
                 au.edu.qut.yawl.worklist.model.Marshaller"
 %><html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Instance Adder</title>
        <!-- Include file to load init method -->
        <%@ include file="head.jsp"  %>
	</head>
	<body>
        <!-- Include check login code -->
        <%@ include file="checkLogin.jsp" %>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp"%>

        <h3>Instance Adder Page</h3>
        <p>
        <%
        String source = request.getParameter("source");
        String workItemID = request.getParameter("workItemID");
        if(source == null){
            WorkItemRecord item = _worklistController.getRemotelyCachedWorkItem(workItemID);
//            sessionHandle = (String)session.getAttribute("sessionHandle");
            String specificationID = item.getSpecificationID();
            String taskID = item.getTaskID();
            TaskInformation taskInfo = _worklistController.getTaskInformation(
                    specificationID,
                    taskID,
                    sessionHandle);
            String instanceAddingPermissionQueryResult =
                    _worklistController.checkPermissionToAddInstances(
                            workItemID,
                            sessionHandle);
            if(_worklistController.successful(instanceAddingPermissionQueryResult)){
                YParametersSchema paramsSchema = taskInfo.getParamSchema();
                YParameter formalInputParam = paramsSchema.getFormalInputParam();
//System.out.println("_InstanceAdder.jsp:: formalInputParam = " + formalInputParam.toXML());
                String stringToAddTo = Marshaller.presentParam(formalInputParam);
//System.out.println("_InstanceAdder.jsp:: stringToAddTo = " + stringToAddTo);
                if(formalInputParam != null){
                    %>
                    <form action="<%= contextPath %>/instanceAdder" method="post">
                    <table border="0" cellspacing="10" bgColor="LightGrey">
                        <tr>
                            <td>WorkItem Prototype : </td>
                            <td><%= workItemID %></td>
                        </tr>
                        <tr>
                            <td width="300" valign="top">Each instance has one param that is relevant
                            to only that instance.
                            Please fill in the parameter we provided.</td>
                            <td><textArea
                            cols="45"
                            rows="10"
                            name="paramValForMICreation"
                            style="font-size : 87%;"><%=
                                stringToAddTo
                            %></textArea></td>
                        </tr>
                        <tr>
                            <td colspan="2" align="center">
                            <input name="source" type="submit" value="Create Instance"/>
                            </td>
                        </tr>
                    </table>
                    <input type="hidden" name="workItemID" value="<%= workItemID %>"/>
                    </form>
                    <%
                }
            } else{
                %>
                There was a problem creating a new instance for: <code><%= workItemID %></code>
                because:
                <br/>
                <font color="red"><%= instanceAddingPermissionQueryResult %></font>
                <%
            }
        } else {
            String paramValueForMICreation = request.getParameter("paramValForMICreation");
            String result = _worklistController.createNewInstance(
                    workItemID,
                    paramValueForMICreation,
                    sessionHandle);
            if(_worklistController.successful(result)){
                int b = result.indexOf("<success>") + 9;
                int e = result.indexOf("</success>");
                result = result.substring(b, e);
                WorkItemRecord item = _worklistController.addWorkItem(result);
            %>
           <table border="0" cellspacing="10" bgColor="LightGrey">
                <tr>
                    <td colspan="2">Work Item successfully created</td>
                </tr>
                <tr>
                    <td>WorkItemID : </td>
                    <td><%= item.getID() %></td>
                </tr>
                <tr>
                    <td>Status : </td>
                    <td><%= item.getStatus() %></td>
                </tr>
            </table>
            <%
            } else {
            %>
                Item creation unsuccessful:
                <br/>
                <font color="red"><%= result %></font>
            <%
            }
        }
        %>
        </p>
        <%@include file="footer.jsp"%>
    </body>
</html>