<%@ page import="org.yawlfoundation.yawl.worklet.WorkletService"%>

<!-- *  author Michael Adams
     *  version 2.0, 06/2009  -->

<!-- header -->
<table id="form1:headtable1" style="height: 69px" width="100%"
       cellspacing="0" cellpadding="0">
    <tbody>
        <tr>
            <td align="left" valign="bottom" width="40%" height="69px"
                background="./graphics/headbgleft.jpg">
                <p>&#160;
                    <a href="http://www.yawlfoundation.org/" target="_blank"
                       style="font-family:verdana; color: #3277ba; font-size:small">
                        www.yawlfoundation.org
                    </a>
                </p>
            </td>

            <td align="center" height="69px" width="409px">
                <img id="form1:headImage1" height="69px" width="409px" alt="YAWL 2.0"
                     border="0"
                     src="./graphics/newYAWL.jpg"/>
            </td>

            <td width="40%" align="right" valign="bottom" height="69px"
                background="./graphics/headbgright.jpg">
                <p style="font-family:verdana; color: #97cbfd; font-size:small">
                    <i>Leading the World in Process Innovation</i>
                    &#160;&#160;&#160;
                </p>
            </td>
        </tr>
    </tbody>
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



