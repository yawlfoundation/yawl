<%@ page import="java.util.Iterator,
                 au.edu.qut.yawl.elements.YSpecification,
                 au.edu.qut.yawl.worklist.model.SpecificationData,
                 java.util.List"
%><html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Active YAWL Specifications</title>
        <!-- Include file to load init method -->
        <%@include file="head.jsp"%>
	</head>
	<body>
        <!-- Include check login code -->
        <%@include file="checkLogin.jsp"%>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp"%>
        <h3>Active YAWL Specifications</h3>
        <form method="get" action="<%= contextPath %>/launchCase" name="specsForm">
        <table border="0" cellspacing="0" cellpadding="0" width="915">
            <tr>
                <td height="30" width="50" align="center"></td>
                <td width="1"/>
                <td width="150" align="center"><em>Specification ID</em></td>
                <td bgcolor="#000000" width="1"/>
                <td width="150" align="center"><em>Spec Name</em></td>
                <td bgcolor="#000000" width="1"/>
                <td width="300" align="center"><em>Documentation</em></td>
                <td bgcolor="#000000" width="1"/>
                <td width="180" align="center"><em>XML</em></td>
                <td bgcolor="#000000" width="1"/>
                <td width="100" align="center"><em>Cases</em></td>
            </tr>
            <tr align="center">
                <td bgcolor="#000000" height="1"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
                <td bgcolor="#000000"/>
            </tr>
            <%
            if(session.getAttribute("sessionHandle") != null) {
                Iterator specDataList = _worklistController.getSpecificationPrototypesList(
                    sessionHandle).iterator();
                while(specDataList.hasNext()) {
                    SpecificationData specification = (SpecificationData) specDataList.next();
                    List caseIDs = _worklistController.getCases(
                            specification.getID(), sessionHandle);
                    String specID = specification.getID();
//System.out.println("_ViewSpecification.jsp:: caseIDs = " + caseIDs);
                    %>
                    <tr>
                        <%
                        if(specification.getStatus().equals(YSpecification._loaded)){
                        %>
                        <td height="30" align="center"><input type="radio" name="specID"
                            value="<%= specID %>"/></td>
                        <td/>
                        <td align="center">
                            <a
                              href="<%= contextPath %>/launchCase?specID=<%= specID %>"><%= specID %>
                            </a>
                        </td>
                        <%
                        } else {
                        %>
                        <td height="30" align="center"/>
                        <td/>
                        <td align="center"><%= specID %></td>
                        <%
                        }
                        %>
                        <td/>
                        <td align="center">
                            <table cellpadding="5"><tr><td>
                            <%= specification.getName() %>
                            </tr></td></table>
                        </td>
                        <td/>
                        <td align="center">
                            <table cellpadding="5"><tr><td>
                            <%= specification.getDocumentation() %></td>
                            </tr></table></td>
                        <td/>
                        <td align="center"><a
                            href="<%= contextPath %>/specBrowser?specID=<%= specID %>"
                            >View <%= specID %></a>
                        <td/>
                        <td align="center">
                        <%
                        for (int i = 0; i < caseIDs.size(); i++) {
                        String caseID = (String) caseIDs.get(i);
                        %>
                        <a
                        href="<%= contextPath %>/caseViewer?caseID=<%= caseID %>&specID=<%= specID %>">
                            <%= caseID %></a>,
                        <%
                        }
                        %>
                        </td>
                    </tr>
                    <%

                }
            }
            %>
        </table>
        <table border="0" cellspacing="20">
            <tr>
                <td><input value=" Launch Case " type="submit"
                    onClick="return isCompletedForm('specsForm', 'specID')"/></td>
                <td><input name=" Clear " type="reset"/></td>
            </tr>
        </table>
        </form>
        <%@include file="footer.jsp"%>
    </body>
</html>