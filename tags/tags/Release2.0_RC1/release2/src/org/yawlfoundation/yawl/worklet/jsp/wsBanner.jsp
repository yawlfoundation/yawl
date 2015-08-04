<%@ page import="org.yawlfoundation.yawl.worklet.WorkletService"%>

<!-- *  author Michael Adams
     *  version 0.8, 04-09/2006  -->

<table width="100%" border="0" cellspacing="0" cellpadding="0" background="./graphics/bg01.gif">
    <tr>
        <!--  width="116" height="55"  -->
        <td valign="top" width="35%" bgcolor="#6699cc" >
           <img src="./graphics/logosmall.jpg"/>
        </td>

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
        <td valign="top" width="35%" bgcolor="#6699cc" >
           <img src="./graphics/wsText.jpg"/>
        </td>

        <td bgcolor="#6699cc" align="center">
            <img src="./graphics/subtext.jpg"/>
        </td>
        <td bgcolor="#6699cc"/>
    </tr>
</table>

<%
    String contextPath = request.getContextPath();
    String sHandle = (String) session.getAttribute("sessionHandle");

    if(sHandle == null){
    	sHandle = (String) request.getParameter("sH");
    	session.setAttribute("sessionHandle", sHandle); // restore session handle
    }

    boolean isAdmin = (sHandle != null) &&
                      (WorkletService.getInstance().isAdminSession(sHandle)) ;

%>

<table width="100%" border="0" bgcolor="#ffffff">
    <tr align="center">
        <td>
            <% if (isAdmin) { %>
                <table border="0" cellspacing="0" cellpadding="0" width="848">
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

                    <% if (isAdmin) { %>
                        <td align="center" valign="middle" bgcolor="#ffffff">
                            <a href="<%= contextPath %>/wsAdminTasks" class="level3menu">Worklet Admin Tasks</a>
                        </td>
					    <td bgcolor="#336699"/>
                    <% } %>

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

                    <% if (isAdmin) { %>
                        <td bgcolor="#336699"/>
                    <% } %>
                </tr>
			</table>
        </td>
     </tr>
 </table>


