<%@ page import="au.edu.qut.yawl.worklist.model.WorkItemRecord,
                 au.edu.qut.yawl.worklist.model.TaskInformation"%>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Work Item Editor</title>
        <!-- Include file to load init method -->
        <%@ include file="head.jsp"  %>
	</head>
	<body>
	
        <!-- Include check login code -->
        <%@ include file="checkLogin.jsp" %>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp"%>
        <h3>Work Item Edit Page</h3>
        
        <%
   	    	String error = request.getParameter("error");
           	System.out.println("error = "+error);
           	
           	if (error.compareTo("true") == 0){
           	%>
           	<br/>
           	<font color="red">You have been sent to this page because an error occurred during form generation.</font>
           	<br/>
           	<br/>
           	<%
           	}
            WorkItemRecord item = _worklistController.getCachedWorkItem(request.getParameter("workItemID"));
System.out.println("_ItemViewer.jsp:: item = " + item.toXML());
            if(item != null){
                TaskInformation taskInfo = _worklistController.getTaskInformation(
                        item.getSpecificationID(),
                        item.getTaskID(),
                        sessionHandle);
                String workItemID = item.getID();
                %>
                <form method="post" action="<%= contextPath %>/workItemProcessor">
                <%
                String status = item.getStatus();
                if(! status.equals(WorkItemRecord.statusDeadlocked)){
                    String data = _worklistController.getDataForWorkItem(workItemID);
                    %>
                    <table border="0" cellspacing="10" bgColor="LightGrey">
                    <tr>
                        <td><em>ID : </em></td>
                        <td><%= workItemID %>
                        </td><td colspan="2"/>
                    </tr><tr>
                        <td><em>Task Name : </em></td>
                        <td><%= taskInfo.getTaskName() %></td>
                        <td colspan="2"/>
                    </tr><tr>
                        <td><em>Enablement Time : </em></td>
                        <td><%= item.getEnablementTime() %></td>
                        <td colspan="2"/>
                    </tr><tr>
                        <td><em>Check Out Time : </em></td>
                        <td><%= item.getStartTime() %></td>
                        <td colspan="2"/>
                    </tr><tr>
                        <td colspan="4"><hr/></td>
                    </tr><tr>
                        <td colspan="2"><b>Work Item Input</b></td>
                        <td colspan="2"><b>Work Item Output</b></td>
                    </tr><tr>
                        <td colspan="2">
                            <textArea
                            name="inputData"
                            readonly="readonly"
                            rows="15" cols="60"
                            style="font-size : 87%;" class="leftArea"><%=
                                item.getDataListString()
                            %></textArea>
                        </td>
                        <td colspan="2">
                            <textArea
                            name="outputData"
                            rows="15" cols="60"
                            style="font-size : 87%;"><%=
                            data != null ?
                                    data :
                                    _worklistController.getMarshalledOutputParamsForTask(
                                    item.getSpecificationID(),
                                    item.getTaskID(),
                                    (String)session.getAttribute("sessionHandle"))
                            %></textArea>
                        </td>
                    </tr>
                    </table>
                    <br/>
                    <p>
                        <input type="submit" name="submit" value="Submit Work Item"/>
                        <input type="submit" name="submit" value="Save Work Item"/>
                        <input type="submit" name="submit" value="Add New Instance"/>
                    </p>
                <%
                } else {
                //##########################################################################
                //                          Deadlocked Work Item
                //##########################################################################
                    %>
                    <table border="0" cellspacing="10" bgColor="LightGrey" width="60%">
                    <tr>
                        <td colspan="2"><font color="red">Deadlock Notification</font></td>
                    </tr>
                    <tr>
                        <td>Task : <%= item.getTaskID() %></td>
                        <td/>
                    </tr>
                    <tr>
                        <td>Case : <%= item.getCaseID() %></td>
                        <td/>
                    </tr>
                    <tr>
                        <td>Specification : <%= taskInfo != null ?
                                                taskInfo.getSpecificationID() :
                                                null %></td>

                        <td>Task Name : <%= taskInfo != null ?
                                                taskInfo.getTaskName() :
                                                null %></td>
                    </tr>
                    <tr>
                        <td colspan="2">
                        NOTE: Deadlocks are generally a sign of an unsound workflow design.
                        To remove this notification please click "Remove".
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                        <input type="submit" name="submit" value="Remove"/>
                        </td>
                    </tr>
                    </table>
                <%
                }
                %>
                <input type="hidden" name="workItemID" value="<%= workItemID %>"/>
                </form>
            <%
            }
            %>
        <%@include file="footer.jsp"%>
	</body>
</html>