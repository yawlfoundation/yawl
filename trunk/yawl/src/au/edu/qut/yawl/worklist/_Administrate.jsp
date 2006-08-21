<%@ page import="java.util.Iterator,
                 au.edu.qut.yawl.worklist.model.SpecificationData,
                 java.util.List,
                 au.edu.qut.yawl.authentication.User,
                 java.util.Set,
                 au.edu.qut.yawl.elements.YAWLServiceReference,
                 au.edu.qut.yawl.elements.YSpecification"%><html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>YAWL Administration</title>
        <!-- Include file to load init method -->
        <%@include file="head.jsp"%>
	</head>
	<body>
        <!-- Include check login code -->
        <%@include file="checkLogin.jsp"%>
        <!-- Include YAWL Banner Information -->
        <%@ include file="banner.jsp"%>
        <h3>Administrate YAWL</h3>
        <%
        boolean mayAdminister = _worklistController.checkConnectionForAdmin(sessionHandle);
        if(sessionHandle == null || ! mayAdminister){
        %>
        <font color="red">
        Only the administrator has permission to view this page.
        </font>
        <%
        } else {
        %>
        <br/>
        <table bgcolor="lightGrey">
        <tr>
        <td><h5>Manage Specifications</h5></td>
        </tr><tr>
        <td align="left">
        <form action="<%= request.getContextPath() %>/upload" enctype="MULTIPART/FORM-DATA" method=post>
        Load YAWL Specification : <input type="file" name="filename" />
        <br />
        <input type="submit" value="Upload" />
        </form>
        </td>
        </tr><tr>
        <td>
        <form action="<%= request.getContextPath() %>/processAdmin" method="post" name="specsForm">
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
                        if(specification.getStatus().equals(YSpecification._loaded)){
//System.out.println("_ViewSpecification.jsp:: caseIDs = " + caseIDs);
                        %>
                        <tr>
                            <td height="30" align="center"><input type="radio" name="specID"
                                value="<%= specID %>"/></td>
                            <td/>
                            <td align="center"><%= specID %></td>
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
                                >View <%= specID %></a></td>
                            <td/>
                        </tr>
                        <%
                        }
                    }
                }
                %>
                <tr>
                    <td colspan="6">
                        <input value=" Unload Specification " type="submit"
                            name="action"
                            onClick="return isCompletedForm('specsForm', 'specID')"/>
                        <input name=" Clear " type="reset"/>
                    </td>
                </tr>
            </table>
        </form>
        </td>
        </tr>
        </table>
        <br/>
        <br/>
        <br/>

<!--        <form action="<%= request.getContextPath() %>/processAdmin" method="post">
        <table bgcolor="lightGrey" border="0" cellspacing="0" cellpadding="0" width="50%">
        <tr>
        <td><h5>Manage Users</h5></td>
        </tr>
        <tr>
        <td>
            User ID :
        </td>
        <td>
            <input type="text" name="userName"/>
        </td>
        </tr>
        <tr>
        <td>
            Personal Name :
        </td>
        <td>
            <input type="text" name="properName"/>
        </td>
        </tr>
        <tr>
        <td>
            Password :
        </td>
        <td>
            <input type="password" name="password"/>
        </td>
        </tr><tr>
        <td>
            Confirm Password :
        </td>
        <td>
            <input type="password" name="password2"/>
        </td>
        </tr><tr>
        <td>
            <input value=" Create Admin " type="submit" name="action"/>
        </td>
        <td>
            <input value=" Create User "  type="submit" name="action"/>
        </td>
        </tr>
        <tr>
            <td colspan="2">&nbsp;</td>
        </tr>
        <tr>
            </td>
            </tr>
        </table>
        </form> -->

        <br/>
        <br/>

        <a href="/adminTool">Edit Organisational Model</a>

        <br/>
	<br/>
        <br/>

        <table bgcolor="lightGrey" border="0" cellspacing="0" cellpadding="10" width="915">
        <tr>
        <td><h5>Registered YAWL Services</h5></td>
        </tr>
        <%
        Set yawlServices = _worklistController.getRegisteredYAWLServices(sessionHandle);
        for (Iterator iterator = yawlServices.iterator(); iterator.hasNext();) {
            YAWLServiceReference service = (YAWLServiceReference) iterator.next();
        %>
            <tr>
            <td><em>ServiceURI</em></td>
            <td><a href="<%= service.getURI() %>"><%= service.getURI() %></a>
            </tr>
            <tr><td><em>Documentation</em></td><td><%= service.getDocumentation() %></tr>
            <tr><td colspan="2"><hr width="70%"/></td></tr>
        <%
        }
        %>
        <tr><td colspan="2">&nbsp;<br/>&nbsp;</tr>
        <tr><td colspan="2"><hr width="100%"/></td></tr>
        <tr>
        <td><h5>Register New YAWL Service</h5></td>
        </tr>
        <form action="<%= request.getContextPath() %>/processAdmin" method="post">
        <tr>
            <td>YAWL Service URI</td>
            <td><input type="text" width="30" name="serviceURI"/></td>
        </tr>
        <tr>
            <td>Service Documentation</td>
            <td><input type="text" width="30" name="serviceDocumentation"/></td>
        </tr>
        <tr>
            <td >
                <input value=" Add YAWL Service " type="submit" name="action"/>
            </td>
            <td >
                <input value=" Remove YAWL Service " type="submit" name="action"/>
            </td>
        </tr>
        </form>
        </table>

        <br/>
        <br/>
	<br/>
	<a href="/PDFforms/generate.jsp">Design Form for Task</a>

        <br/>
        <br/>

        <br/>
        <%
            }
        %>

        <br/>
        <br/>
	<br/>
	<a href="/worklist/createID.jsp">Create Digital ID</a>

        <br/>
        <br/>
        <%@include file="footer.jsp"%>
    </body>
</html>