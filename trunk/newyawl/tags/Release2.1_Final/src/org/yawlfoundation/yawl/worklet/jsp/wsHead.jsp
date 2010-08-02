<%@ page import="org.yawlfoundation.yawl.worklet.exception.ExceptionService"%>
<%@ page import="org.yawlfoundation.yawl.worklet.WorkletService"%>
<%--
  ~ Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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
<link rel="shortcut icon" id="lnkFavIcon" type="image/x-icon" href="./graphics/favicon.ico"/>

<%!
    ExceptionService _exceptionService = null ;
    String _engineURI ;

    String _rootResServiceURL = "http://localhost:8080/resourceService/faces/";
    String _worklistURL = _rootResServiceURL + "userWorkQueues.jsp";
    String _adminlistURL = _rootResServiceURL + "adminQueues.jsp";
    String _caseMgtURL = _rootResServiceURL + "caseMgt.jsp";

    public void jspInit(){
        ServletContext context = getServletContext();
        _exceptionService = (ExceptionService) context.getAttribute(
                    "org.yawlfoundation.yawl.worklet.exception.ExceptionService");
        if(_exceptionService == null) {
            _exceptionService =  ExceptionService.getInst();
            context.setAttribute("org.yawlfoundation.yawl.workllet.exception.ExceptionService",
                    _exceptionService);
        }
    }
%>
  <script language="JavaScript">

        function isCompletedForm(formNme, radioGroupName){
            var oneChecked = false;
            var i = 0;
            //javascript or dom problem means one must access the properties of
            //a radio button different ways depending on whether one or more radios are in the group
            if(window.document[formNme].elements[radioGroupName].checked){
                oneChecked = true;
            }
            else{
                while( i < window.document[formNme].elements[radioGroupName].length){
                    if(window.document[formNme].elements[radioGroupName][i].checked == true){
                        oneChecked = true;
                    }
                    i++;
                }
            }
            if(! oneChecked){
                alert("You need to select one item.");
            }
            return oneChecked;
        }

    </script>
    <style type="TEXT/CSS"><!--
    .leftArea	{
        color:DarkGrey;
        background:#E8E8E8;
    }
    body{
        scrollbar-arrow-color:WHITE;
        scrollbar-track-color:#D6D6D6;
        scrollbar-shadow-color:#D6D6D6;
        scrollbar-face-color:#135184;
        scrollbar-highlight-color:#D6D6D6;
        scrollbar-darkshadow-color:#135184;
        scrollbar-3dlight-color:#135184;
    }
    --></style>