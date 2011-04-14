<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

<!--
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
  -->

    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{teamQueues.page1}" id="page1">
            <ui:html binding="#{teamQueues.html1}" id="html1">
                <ui:head binding="#{teamQueues.head1}" id="head1"
                         title="YAWL 2.2: View Team Queues">

                    <ui:link binding="#{teamQueues.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>
 
                </ui:head>
                <ui:body binding="#{teamQueues.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{teamQueues.form1}" id="form1">

                        <!-- include banner -->
                        <jsp:include page="pfHeader.jspf"/>

                        <div style="top: 20px; position: relative">
                            <jsp:directive.include file="pfMenubar.jspf"/>
                        </div>
                        <center>
                        <ui:panelLayout binding="#{customServices.pnlContainer}"
                                        id="pnlAQContainer"
                                        style="#{SessionBean.outerPanelTop}"
                                        styleClass="teamQueuesContainerPanel">

                            <ui:panelLayout binding="#{teamQueues.lpQueues}"
                                            id="lpWorklisted"
                                            styleClass="teamQueuesInnerPanel">

                                <ui:label binding="#{teamQueues.lblAssignedTo}"
                                          for="cbbAssignedTo"
                                          id="lblAssignedTo"
                                          styleClass="queuesLabelLeft"
                                          style="left: 317px; top: 213px"
                                          text="#{SessionBean.assignedToText}"/>

                                <ui:label binding="#{teamQueues.lblResourceState}"
                                          for="txtResourceState"
                                          id="lblResourceState"
                                          styleClass="queuesLabelRight"
                                          style="left: 479px; top: 213px"
                                          text="Resource State"/>

                                <ui:dropDown binding="#{teamQueues.cbbAssignedTo}"
                                             id="cbbAssignedTo"
                                             forgetValue="true"
                                             items="#{SessionBean.adminQueueAssignedList}"
                                             style="left: 317px; top: 230px; position: absolute; width: 145px"/>

                                <ui:textField binding="#{teamQueues.txtResourceState}"
                                              id="txtResourceState"
                                              readOnly="true"
                                              styleClass="queuesTextField"
                                              style="left: 479px; top: 230px"
                                              trim="false"
                                              text="#{SessionBean.resourceState}" />


                        <ui:panelLayout binding="#{teamQueues.rbGroup}"
                                        id="rbGroup"
                                        styleClass="teamQueuesRBGroup">

                            <ui:radioButton binding="#{teamQueues.rbTeam}"                                            
                                            id="rbTeam"
                                            name="rButtonGroup"
                                            styleClass="teamQueuesRadioButton"
                                            style="top: 10px"
                                            selected="#{SessionBean.teamRBSelected}"
                                            disabled="#{SessionBean.teamRBDisabled}"
                                            onClick="common_timeoutSubmitForm(this.form, 'rbTeam');"
                                            label="Team" />

                            <ui:radioButton binding="#{teamQueues.rbOrgGroup}"
                                            id="rbOrgGroup"
                                            name="rButtonGroup"
                                            styleClass="teamQueuesRadioButton"
                                            style="top: 40px"
                                            selected="#{SessionBean.orgGroupRBSelected}"
                                            disabled="#{SessionBean.orgGroupRBDisabled}"
                                            onClick="common_timeoutSubmitForm(this.form, 'rbOrgGroup');"
                                            label="OrgGroup" />

                        </ui:panelLayout>


                        <ui:button binding="#{SessionBean.btnRefresh}"
                                   action="#{teamQueues.btnRefresh_action}"
                                   id="btnRefreshQueue"
                                   imageURL="/resources/refresh.png"
                                   styleClass="refreshButton"
                                   style="top: 5px"
                                   toolTip="Refresh Queues"
                                   text=""/>

                                <div><jsp:include page="pfMsgPanel.jspf"/></div>

                            </ui:panelLayout>

                        <div>
                            <jsp:include page="pfQueueUI.jspf"/>
                        </div>

                     </ui:panelLayout>

                        </center>
                        <div><jsp:include page="pfFooter.jspf"/></div>
   
                       <ui:meta binding="#{teamQueues.metaRefresh}"
                                 httpEquiv="refresh"
                                 id="metaRefresh" />
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
