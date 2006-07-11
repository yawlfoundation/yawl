<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>YAWL Cases</title>
        <!-- Include file to load init method -->
        <%@include file="head.jsp"%>
	</head>
	<body>
        <!-- Include check login code -->
        <%@include file="checkLogin.jsp"%>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp" %>
        <%
            String caseID = request.getParameter("caseID");
            String specID = request.getParameter("specID");
        %>
        <h3>Case Viewer for <%= caseID %></h3>
        <br/>
        <form action="" method="post">
        <table border="0" cellspacing="10" bgColor="LightGrey">
            <tr>
                <td>CaseID : </td>
                <td><%= caseID %></td>
            </tr>
            <tr>
                <td>SpecificationID : </td>
                <td><%= specID %></td>
            </tr>
            <%
            if(request.getMethod().equals("POST")){
                String submit = request.getParameter("submit");
                if("View State".equals(submit)){
                    String caseState = _worklistController.getCaseState(
                            caseID, sessionHandle);

            %>
            <%= caseState %>
            <%
                } else if ("Cancel Case".equals(submit)){
                    String result = _worklistController.cancelCase(caseID, sessionHandle);
                    if(_worklistController.successful(result)){
            %>
            <tr>
                <td colspan="2" valign="top">
                Successfully cancelled case.
                </td>
            </tr>
            <%
                    } else {
            %>
            <tr>
                <td colspan="2" valign="top">
                <font color="red">Failed to cancel case, REASON:
                <br/>
                <%= result %>
                </font>
                </td>
            </tr>
            <%
                    }
                }
                else if ("Raise Exception".equals(submit)){
                   if (_ixURI != null) {
                       String url = _ixURI + "/caseException?caseID=" + caseID ;
                       response.sendRedirect( response.encodeURL(url) );
                   }
                }
                else if ("Reject Worklet".equals(submit)){
                   if (_ixURI != null) {
                       System.out.println("rrrrrrejecting......");
                       String url = _ixURI + "/rejectWorklet?caseID=" + caseID ;
                       response.sendRedirect( response.encodeURL(url) );
                   }
                }
            } else { //request method is GET
            %>
            <tr>
                <td align="center">
                <input type="submit" name="submit" value="View State"/>
                </td>
                <td align="center">
                <input type="submit" name="submit" value="Cancel Case"/>
                </td>
             </tr>
                <%
                if (_ixURI != null) {
                %>
                   <tr>
                       <td align="center">
                           <input type="submit" name="submit" value="Raise Exception"/>
                       </td>
                       <td align="center">
                           <input type="submit" name="submit" value="Reject Worklet"/>
                       </td>
                   </tr>    
                <%
                }
            }
            %>
        </table>
        <input type="hidden" name="caseID" value="<%= caseID %>"/>
        <input type="hidden" name="specID" value="<%= specID %>"/>
        </form>

        <%@include file="footer.jsp"%>
    </body>
</html>
