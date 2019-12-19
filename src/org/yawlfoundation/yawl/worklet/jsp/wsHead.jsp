<%@ page import="org.yawlfoundation.yawl.worklet.exception.ExceptionService" %>
<%@ page import="org.yawlfoundation.yawl.worklet.WorkletService" %>
<%@ page import="org.yawlfoundation.yawl.worklet.admin.AdminTasksManager" %>
<%--
  ~ Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
  ~ The YAWL Foundation is a collaboration of individuals and
  ~ organisations who are committed to improving workflow technology.
  ~
  ~ This file is part of YAWL. YAWL is free software: you can
  ~ redistribute it and/or modify it under the terms of the GNU Lesser
  ~ General Public License as published by the Free Software Foundation.
  ~
  ~ YAWL is distributed in the hope that it will be useful, but WITHOUT
  ~ ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  ~ or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
  ~ Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public
  ~ License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
  --%>

<meta name="Pragma" content="no-cache"/>
<meta name="Cache-Control" content="no-cache"/>
<meta name="Expires" content="0"/>
<link rel="stylesheet" href="./graphics/common.css"/>
<link rel="shortcut icon" id="lnkFavIcon" type="image/x-icon"
      href="./graphics/favicon.ico"/>

<%!
    WorkletService _workletService = null;
    AdminTasksManager _adminTasksManager = null;
    String _worklistURL;
    String _adminlistURL;
    String _caseMgtURL;

    public void jspInit() {
        ServletContext context = getServletContext();
        _workletService = (WorkletService) context.getAttribute(
                "org.yawlfoundation.yawl.worklet.WorkletService");
        if (_workletService == null) {
            _workletService = WorkletService.getInstance();
            context.setAttribute("org.yawlfoundation.yawl.worklet.WorkletService",
                    _workletService);
        }
        _adminTasksManager = (AdminTasksManager) context.getAttribute(
                "org.yawlfoundation.yawl.worklet.admin.AdminTasksManager");
        if (_adminTasksManager == null) {
            _adminTasksManager = new AdminTasksManager();
            context.setAttribute("org.yawlfoundation.yawl.worklet.admin.AdminTasksManager",
                    _adminTasksManager);
        }

        // set resource service admin page urls
        String resourceServiceURL = _workletService.getResourceServiceURL();
        if (resourceServiceURL == null) {
            resourceServiceURL = "http://localhost:8080/resourceService";   // a default
        }
        if (!resourceServiceURL.endsWith("/")) {
            resourceServiceURL += "/";
        }
        _worklistURL = resourceServiceURL + "faces/userWorkQueues.jsp";
        _adminlistURL = resourceServiceURL + "faces/adminQueues.jsp";
        _caseMgtURL = resourceServiceURL + "faces/caseMgt.jsp";
    }
%>
<script language="JavaScript">

    function isCompletedForm(formNme, radioGroupName) {
        var oneChecked = false;
        var i = 0;
        //javascript or dom problem means one must access the properties of
        //a radio button different ways depending on whether one or more radios are in the group
        if (window.document[formNme].elements[radioGroupName].checked) {
            oneChecked = true;
        }
        else {
            while (i < window.document[formNme].elements[radioGroupName].length) {
                if (window.document[formNme].elements[radioGroupName][i].checked == true) {
                    oneChecked = true;
                }
                i++;
            }
        }
        if (!oneChecked) {
            alert("You need to select one item.");
        }
        return oneChecked;
    }

</script>
<style type="TEXT/CSS"><!--
.leftArea {
    color: DarkGrey;
    background: #E8E8E8;
}

body {
    scrollbar-arrow-color: WHITE;
    scrollbar-track-color: #D6D6D6;
    scrollbar-shadow-color: #D6D6D6;
    scrollbar-face-color: #135184;
    scrollbar-highlight-color: #D6D6D6;
    scrollbar-darkshadow-color: #135184;
    scrollbar-3dlight-color: #135184;
}

--></style>