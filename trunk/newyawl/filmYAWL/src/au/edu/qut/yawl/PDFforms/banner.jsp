		<table width="100%" border="0" cellspacing="0" cellpadding="0" background="./graphics/bg01.gif">
			<tr>
                <td bgcolor="#6699cc"/>
				<td bgcolor="#6699cc" align="center">
					<img src="./graphics/subtext.jpg"/>
				</td>
                <td bgcolor="#6699cc"/>
			</tr>
		</table>

		<!-- Start Navigation Banner -->
		<table width="100%" border="0" bgcolor="#ffffff">
			<tr align="center">
				<td>
					<table border="0" cellspacing="0" cellpadding="0" width="727">
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
						</tr>
                        <%
                            String contextPath = request.getContextPath();
                        %>
						<tr>
							<td bgcolor="#336699" height="20"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a href="http://www.yawl-system.com" class="level3menu">YAWL Home</a>
							</td>
							<td bgcolor="#336699"/>
                            <td align="center" valign="middle" bgcolor="#ffffff">
								<a href="/worklist/admin" class="level3menu">Administrate</a>
							</td>
                            <td bgcolor="#336699"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a href="/worklist/viewSpecifications" class="level3menu">Workflow Specifications</a>
							</td>
							<td bgcolor="#336699"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a href="/worklist/availableWork" class="level3menu">Available Work</a>
							</td>
							<td bgcolor="#336699"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a href="/worklist/checkedOut" class="level3menu">Checked Out Work</a>
							</td>
							<td bgcolor="#336699"/>
							<td align="center" valign="middle" bgcolor="#ffffff">
								<a href="/worklist/logout" class="level3menu">Logout</a>
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