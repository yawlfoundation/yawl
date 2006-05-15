		<table width="100%" border="0" cellspacing="0" cellpadding="0" background="./graphics/bg01.gif">
			<tr>
                <!--  width="116" height="55"  -->
                <td valign="top" width="35%" bgcolor="#6699cc" />

                <!-- height="55"  -->
                <td align="center" valign="bottom" bgcolor="#6699cc" >
                    <OBJECT classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"
                    codebase=
                    "http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0"
                    WIDTH="209" HEIGHT="47" id="yawl" ALIGN="center">
                        <PARAM NAME="movie" VALUE="./graphics/yawl.swf" />
                        <PARAM NAME="quality" VALUE="high" />
                    </OBJECT>
                </td>

				<td width="30%" align="right" valign="top" bgcolor="#6699cc" />
			</tr>

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