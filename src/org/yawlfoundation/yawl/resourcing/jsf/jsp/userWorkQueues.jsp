<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

<!--
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
  -->

    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{userWorkQueues.page1}" id="page1">
            <ui:html binding="#{userWorkQueues.html1}" id="html1">
                <ui:head binding="#{userWorkQueues.head1}" id="head1"
                         title="#{SessionBean.title}">

                    <ui:link binding="#{userWorkQueues.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>
 
                </ui:head>
                <ui:body binding="#{userWorkQueues.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{userWorkQueues.form1}" id="form1">

                    <!-- include banner -->
                    <div><jsp:include page="pfHeader.jspf"/></div>
 
                    <div style="top: 20px; position: relative">
                        <jsp:directive.include file="pfMenubar.jspf"/>
                    </div>
                    <center>

                    <ui:panelLayout binding="#{userWorkQueues.pnlContainer}"
                                    id="pnlUQContainer"
                                    style="#{SessionBean.outerPanelTop}"
                                    styleClass="userQueuesContainerPanel">
                    
                        <ui:tabSet binding="#{userWorkQueues.tabSet}"
                                   id="tabSetUserQueues"
                                   styleClass="userQueuesTabSet">

                            <ui:tab action="#{userWorkQueues.tabOffered_action}"
                                    binding="#{userWorkQueues.tabOffered}"
                                    id="tabOffered"
                                    style="#{SessionBean.initTabStyle}"
                                    text="#{SessionBean.initOfferedTabText}">

                                <ui:panelLayout binding="#{userWorkQueues.lpOffered}"
                                                id="lpOffered"
                                                styleClass="userQueuesTabPanel">

                                    <ui:button action="#{userWorkQueues.btnAccept_action}"
                                               binding="#{userWorkQueues.btnAccept}"
                                               id="btnAccept"
                                               primary="true"
                                               styleClass="queuesButton"
                                               style="top: 15px"
                                               text="Accept Offer"/>

                                    <ui:button action="#{userWorkQueues.btnAcceptStart_action}"
                                               binding="#{userWorkQueues.btnAcceptStart}"
                                               id="btnAcceptStart"
                                               styleClass="queuesButton"
                                               style="top: 50px"
                                               text="Accept &amp; Start"/>

                                    <ui:button action="#{userWorkQueues.btnChain_action}"
                                               binding="#{userWorkQueues.btnChain}"
                                               id="btnChain"
                                               styleClass="queuesButton"
                                               style="top: 85px"
                                               text="Chain"/>

                                </ui:panelLayout>
                            </ui:tab>
                            
                            <ui:tab action="#{userWorkQueues.tabAllocated_action}"
                                    binding="#{userWorkQueues.tabAllocated}"
                                    id="tabAllocated"
                                    text="#{SessionBean.initAllocatedTabText}">

                                <ui:panelLayout binding="#{userWorkQueues.lpAllocated}"
                                                id="lpAllocated"
                                                styleClass="userQueuesTabPanel">

                                    <ui:button action="#{userWorkQueues.btnStart_action}"
                                               binding="#{userWorkQueues.btnStart}"
                                               id="btnStart"
                                               styleClass="queuesButton"
                                               style="top: 15px"
                                               text="Start"/>

                                    <ui:button action="#{userWorkQueues.btnDeallocate_action}"
                                               binding="#{userWorkQueues.btnDeallocate}"
                                               id="btnDeallocate"
                                               styleClass="queuesButton"
                                               style="top: 50px"
                                               text="Deallocate"/>

                                    <ui:button action="#{userWorkQueues.btnDelegate_action}"
                                               binding="#{userWorkQueues.btnDelegate}"
                                               id="btnDelegate"
                                               styleClass="queuesButton"
                                               style="top: 85px"
                                               text="Delegate"/>
                                    
                                    <ui:button action="#{userWorkQueues.btnSkip_action}"
                                               binding="#{userWorkQueues.btnSkip}"
                                               id="btnSkip"
                                               styleClass="queuesButton"
                                               style="top: 120px"
                                               text="Skip"/>

                                    <ui:button action="#{userWorkQueues.btnPile_action}"
                                               binding="#{userWorkQueues.btnPile}"
                                               id="btnPile"
                                               styleClass="queuesButton"
                                               style="top: 155px"
                                               text="Pile"/>
                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{userWorkQueues.tabStarted_action}"
                                    binding="#{userWorkQueues.tabStarted}"
                                    id="tabStarted"
                                    text="#{SessionBean.initStartedTabText}">

                                <ui:panelLayout binding="#{userWorkQueues.lpStarted}"
                                                id="lpStarted"
                                                styleClass="userQueuesTabPanel">

                                    <ui:button action="#{userWorkQueues.btnView_action}"
                                               binding="#{userWorkQueues.btnView}"
                                               id="btnView"
                                               styleClass="queuesButton"
                                               style="top: 15px"
                                               text="View/Edit"/>

                                    <ui:button action="#{userWorkQueues.btnSuspend_action}"
                                               binding="#{userWorkQueues.btnSuspend}"
                                               id="btnSuspend"
                                               styleClass="queuesButton"
                                               style="top: 50px"
                                               text="Suspend"/>

                                    <ui:button action="#{userWorkQueues.btnStateless_action}"
                                               binding="#{userWorkQueues.btnStateless}"
                                               id="btnStateless"
                                               styleClass="queuesButton"
                                               style="top: 85px"
                                               text="Reallocate s/l"/>

                                    <ui:button action="#{userWorkQueues.btnStateful_action}"
                                               binding="#{userWorkQueues.btnStateful}"
                                               id="btnStateful"
                                               styleClass="queuesButton"
                                               style="top: 120px"
                                               text="Reallocate s/f"/>

                                    <ui:button action="#{userWorkQueues.btnNewInstance_action}"
                                               binding="#{userWorkQueues.btnNewInstance}"
                                               id="btnNewInstance"
                                               styleClass="queuesButton"
                                               style="top: 155px"
                                               text="New Instance"/>

                                    <ui:button action="#{userWorkQueues.btnComplete_action}"
                                               binding="#{userWorkQueues.btnComplete}"
                                               id="btnComplete"
                                               styleClass="queuesButton"
                                               style="top: 190px"
                                               text="Complete"/>

                                </ui:panelLayout>
                            </ui:tab>
                            
                            <ui:tab action="#{userWorkQueues.tabSuspended_action}"
                                    binding="#{userWorkQueues.tabSuspended}"
                                    id="tabSuspended"
                                    text="#{SessionBean.initSuspendedTabText}">

                                <ui:panelLayout
                                        binding="#{userWorkQueues.lpSuspended}"
                                        id="lpSuspended"
                                        styleClass="userQueuesTabPanel">

                                    <ui:button action="#{userWorkQueues.btnUnsuspend_action}"
                                               binding="#{userWorkQueues.btnUnsuspend}"
                                               id="btnUnsuspend"
                                               styleClass="queuesButton"
                                               style="top: 15px"
                                               text="Unsuspend"/>
                                </ui:panelLayout>
                            </ui:tab>
                        </ui:tabSet>

                        <ui:button binding="#{SessionBean.btnRefresh}"
                                   action="#{userWorkQueues.btnRefresh_action}"
                                   id="btnRefresh"
                                   imageURL="/resources/refresh.png"
                                   styleClass="refreshButton"
                                   toolTip="Refresh Queues"
                                   text=""/>

                        <ui:button binding="#{userWorkQueues.btnVisualiser}"
                                   action="#{userWorkQueues.btnVisualise_action}"
                                   id="btnVisualiser"
                                   imageURL="/resources/visualise.png"
                                   styleClass="synchButton"
                                   toolTip="Show Visualizer"
                                   visible="#{ApplicationBean.visualizerEnabled}"
                                   text=""/>

                        <ui:button binding="#{userWorkQueues.btnException}"
                                   action="#{userWorkQueues.btnException_action}"
                                   id="btnException"
                                   imageURL="/resources/workletException.png"
                                   styleClass="synchButton"
                                   style="#{userWorkQueues.btnExceptionStyle}"
                                   toolTip="Raise Item-level Exception"
                                   visible="#{ApplicationBean.exceptionServiceEnabled}"
                                   text=""/>

                        <div>
                            <jsp:include page="pfQueueUI.jspf"/>
                        </div>

                            <div><jsp:include page="pfMsgPanel.jspf"/></div>
                         </ui:panelLayout>

                       </center>

                    <div><jsp:include page="pfFooter.jspf"/></div>

                        <ui:meta binding="#{userWorkQueues.metaRefresh}"
                                 httpEquiv="refresh"
                                 id="metaRefresh" />

                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
