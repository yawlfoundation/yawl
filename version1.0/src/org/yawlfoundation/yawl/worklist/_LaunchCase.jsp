<%@ page import="java.util.Iterator,
                 org.yawlfoundation.yawl.engine.interfce.SpecificationData,
                 org.yawlfoundation.yawl.elements.data.YParameter,
                 org.yawlfoundation.yawl.engine.interfce.Marshaller,
                 org.yawlfoundation.yawl.elements.YSpecification,
			     org.yawlfoundation.yawl.worklist.WorkItemProcessor"%>

<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Launch Case</title>
        <!-- Include file to load init method -->
        <%@include file="head.jsp"%>
	</head>
	<body>
        <!-- Include check login code -->
        <%@include file="checkLogin.jsp"%>
        <!-- Include YAWL Banner Information -->
        <%@include file="banner.jsp" %>
        <h3>Launch Case</h3>
        <%
        if(request.getMethod().equals("GET")){
            String specID = request.getParameter("specID");
            String userID = (String) session.getAttribute("userid");
            SpecificationData specData = _worklistController.getSpecificationData(
                    specID, sessionHandle);
			WorkItemProcessor wip = new WorkItemProcessor();
			
			try{
				wip.executeCasePost(getServletContext(), specID, sessionHandle,
					_worklistController, userID, session.getId());

				String url = wip.getRedirectURL(getServletContext(), specData, session.getId());
				
				response.sendRedirect(response.encodeURL(url));
			}
	
			catch(Exception e){
	
	            if(specData != null){
        %>
        <form method="post" action="" name="caseLauncherForm">
            <table border="0" cellspacing="10" bgColor="LightGrey">
                <tr>
                    <td><b>Specification ID : </b></td>
                    <td><%= specID %></td>
                </tr>
                <%
                if(specData.getName() != null) {
                %>
                <tr>
                    <td><b>Specification Name : </b></td>
                    <td><%= specData.getName() %></td>
                </tr>
                <%
                }
                if(specData.getDocumentation() != null) {
                %>
                <tr>
                    <td><b>Documentation : </b></td>
                    <td><%= specData.getDocumentation() %></td>
                </tr>
                <%
                }
                StringBuffer paramsString = new StringBuffer();
                Iterator params = specData.getInputParams().iterator();
                while(params.hasNext()) {
                    YParameter inputParam = (YParameter) params.next();
                    paramsString.append(Marshaller.presentParam(inputParam));
                }
                if(paramsString.length() > 0){
                    if(specData.usesSimpleRootData()) {
                        paramsString.insert(0, "<data>");
                        paramsString.append("</data>");
                    }
                    else {
                        paramsString.insert(0, "<" + specData.getRootNetID() + ">");
                        paramsString.append("</" + specData.getRootNetID() + ">");
                    }

                %>
                <tr>
                    <td width="300" valign="top">Input Params : </td>
                    <td><textArea
                    cols="45"
                    rows="10"
                    name="caseData"
                    style="font-size : 87%;"><%= paramsString.toString()
                    %></textArea></td>
                </tr>
                <%
                }
                %>
                <tr>
                    <td colspan="2" align="center">
                    <input name="source" type="submit" value="Start Case"/>
                    </td>
                </tr>
            </table>
            <input type="hidden" name="specID" value="<%= specID %>"/>
        </form>
        <%
            }
	} // end catch
        } else {
            String caseData = request.getParameter("caseData");
			if (caseData == null){
				caseData = (String) request.getAttribute("caseData");
			}
            String specID = request.getParameter("specID");
			if (specID == null){
				specID = (String) request.getAttribute("specID");
			}

            String result = _worklistController.launchCase(specID, caseData, sessionHandle);
            if(_worklistController.successful(result)){
            %>
                <b>You successfully started a case (id := <%= result %>).</b>
            <%
            } else {
                if(result.indexOf("SCHEMA =") != -1){
                    session.setAttribute("outcome", result);
                    application.getRequestDispatcher("/validationProblem")
                            .forward(request, response);
                }
            %>
                <b><font color='red'>You were unsuccessful in starting a case.
                <br/>
                Reason: <%= result %>.</font></b>
            <%
            }
        }
        %>
        <%@include file="footer.jsp"%>
    </body>
</html>