<%@ page import="au.edu.qut.yawl.worklist.model.WorklistController"%>
<table width="100%" border="0" cellspacing="0" cellpadding="0" background="./graphics/bg01.gif">
			<tr>
                <td bgcolor="#6699cc"/>
				<td bgcolor="#6699cc" align="center">
					<img src="./graphics/subtext.jpg"/>
				</td>
                <td bgcolor="#6699cc"/>
			</tr>
		</table>

<%

    String contextPath = request.getContextPath();
    String sHandle = (String) session.getAttribute("sessionHandle");

    ServletContext context = getServletContext();
    String ixURI = context.getInitParameter("InterfaceX_BackEnd");
    WorklistController worklistController = (WorklistController) context.getAttribute(
            "au.edu.qut.yawl.worklist.model.WorklistController");

    boolean isAdmin = ((sHandle != null) && (ixURI != null) &&
                      (worklistController.checkConnectionForAdmin(sHandle)));

%>

		<!-- Start Navigation Banner -->
		<table width="100%" border="0" bgcolor="#ffffff">
			<tr align="center">
				<td>
                   <% if (isAdmin) { %>
                      <table border="0" cellspacing="0" cellpadding="0" width="828">
                   <% } else { %>
                      <table border="0" cellspacing="0" cellpadding="0" width="727">
                   <% } %>
                        <tr align="center">
							<td width="1"  height="3" bgcolor="#336699"/>
							<td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>
                            <td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>
							<td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>
							<td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>
							<td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>
							<td width="120" bgcolor="#336699"/>
							<td width="1" bgcolor="#336699"/>

                         <% if (isAdmin) { %>
                            <td width="120" bgcolor="#336699"/>
                            <td width="1" bgcolor="#336699"/>
                         <% } %>
                        </tr>
						<tr>
							<td bgcolor="#336699" height="20"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a href="http://www.yawl-system.com" class="level3menu">YAWL Home</a>
							</td>
							<td bgcolor="#336699"/>
                            <td align="center" valign="middle" bgcolor="#ffffff">
								<a href="<%= contextPath %>/admin" class="level3menu">Administrate</a>
							</td>
                            <td bgcolor="#336699"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a href="<%= contextPath %>/viewSpecifications" class="level3menu">Workflow Specifications</a>
							</td>
							<td bgcolor="#336699"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a href="<%= contextPath %>/availableWork" class="level3menu">Available Work</a>
							</td>
							<td bgcolor="#336699"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a href="<%= contextPath %>/checkedOut" class="level3menu">Checked Out Work</a>
							</td>
							<td bgcolor="#336699"/>

                            <% if (isAdmin) { %>
                                <td align="center" valign="middle" bgcolor="#ffffff">
                                    <a href="/workletService/wsAdminTasks?sH=<%=sHandle %>" class="level3menu">Worklet Admin Tasks</a>
                                </td>
                                <td bgcolor="#336699"/>
                            <% } %>

                            <td align="center" valign="middle" bgcolor="#ffffff">
								<a href="<%= contextPath %>/logout" class="level3menu">Logout</a>
							</td>
							<td bgcolor="#336699"/>
						</tr>
						<tr>
							<td bgcolor="#336699" height="1"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
							<td bgcolor="#336699"/>
                            <td bgcolor="#336699"/>
                            <% if (isAdmin) { %>
  							    <td bgcolor="#336699"/>
                            <% } %>
                        </tr>
					</table>
                    <%
                        if (null != session.getAttribute("userid")){
                    %>

                    <b>Welcome to YAWL
                        <font color="blue">
                        <%= (String) session.getAttribute("userid") %>
                        </font>
                    </b>
                    <%
                        }
                        try{
                    %>
		            <!--End top level navigation links and search -->
