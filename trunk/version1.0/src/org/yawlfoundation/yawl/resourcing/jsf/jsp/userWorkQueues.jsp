<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{userWorkQueues.page1}" id="page1">
            <ui:html binding="#{userWorkQueues.html1}" id="html1">
                <ui:head binding="#{userWorkQueues.head1}" id="head1" title="YAWL Worklist">

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
                    <div><jsp:directive.include file="pfHeader.jspf"/></div>
 
                        <ui:tabSet binding="#{userWorkQueues.tabSet}"
                                   id="tabSet"
                                   styleClass="queuesTabSet">

                            <ui:tab action="#{userWorkQueues.tabOffered_action}"
                                    binding="#{userWorkQueues.tabOffered}"
                                    id="tabOffered"
                                    style="#{SessionBean.initTabStyle}"
                                    text="#{SessionBean.initOfferedTabText}">

                                <ui:panelLayout binding="#{userWorkQueues.lpOffered}"
                                                id="lpOffered"
                                                styleClass="queuesTabPanel"
                                                style="background-color: rgb(227, 240, 252)">

                                    <ui:button action="#{userWorkQueues.btnAccept_action}"
                                               binding="#{userWorkQueues.btnAccept}"
                                               id="btnAccept"
                                               primary="true"
                                               styleClass="queuesButton"
                                               style="top: 20px"
                                               text="Accept Offer"/>

                                </ui:panelLayout>
                            </ui:tab>
                            
                            <ui:tab action="#{userWorkQueues.tabAllocated_action}"
                                    binding="#{userWorkQueues.tabAllocated}"
                                    id="tabAllocated"
                                    text="#{SessionBean.initAllocatedTabText}">

                                <ui:panelLayout binding="#{userWorkQueues.lpAllocated}"
                                                id="lpAllocated"
                                                styleClass="queuesTabPanel"
                                                style="background-color: rgb(251, 251, 242)">

                                    <ui:button action="#{userWorkQueues.btnStart_action}"
                                               binding="#{userWorkQueues.btnStart}"
                                               id="btnStart"
                                               styleClass="queuesButton"
                                               style="top: 20px"
                                               text="Start"/>

                                    <ui:button action="#{userWorkQueues.btnDeallocate_action}"
                                               binding="#{userWorkQueues.btnDeallocate}"
                                               id="btnDeallocate"
                                               styleClass="queuesButton"
                                               style="top: 60px"
                                               text="Deallocate"/>

                                    <ui:button action="#{userWorkQueues.btnDelegate_action}"
                                               binding="#{userWorkQueues.btnDelegate}"
                                               id="btnDelegate"
                                               styleClass="queuesButton"
                                               style="top: 100px"
                                               text="Delegate"/>
                                    
                                    <ui:button action="#{userWorkQueues.btnSkip_action}"
                                               binding="#{userWorkQueues.btnSkip}"
                                               id="btnSkip"
                                               styleClass="queuesButton"
                                               style="top: 140px"
                                               text="Skip"/>

                                    <ui:button action="#{userWorkQueues.btnPile_action}"
                                               binding="#{userWorkQueues.btnPile}"
                                               id="btnPile"
                                               styleClass="queuesButton"
                                               style="top: 180px"
                                               text="Pile"/>
                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{userWorkQueues.tabStarted_action}"
                                    binding="#{userWorkQueues.tabStarted}"
                                    id="tabStarted"
                                    text="#{SessionBean.initStartedTabText}">

                                <ui:panelLayout binding="#{userWorkQueues.lpStarted}"
                                                id="lpStarted"
                                                styleClass="queuesTabPanel"
                                                style="background-color: rgb(235, 252, 235)">

                                    <ui:button action="#{userWorkQueues.btnView_action}"
                                               binding="#{userWorkQueues.btnView}"
                                               id="btnView"
                                               styleClass="queuesButton"
                                               style="top: 20px"
                                               text="View/Edit"/>

                                    <ui:button action="#{userWorkQueues.btnSuspend_action}"
                                               binding="#{userWorkQueues.btnSuspend}"
                                               id="btnSuspend"
                                               styleClass="queuesButton"
                                               style="top: 60px"
                                               text="Suspend"/>

                                    <ui:button action="#{userWorkQueues.btnStateless_action}"
                                               binding="#{userWorkQueues.btnStateless}"
                                               id="btnStateless"
                                               styleClass="queuesButton"
                                               style="top: 100px"
                                               text="Reallocate s/l"/>

                                    <ui:button action="#{userWorkQueues.btnStateful_action}"
                                               binding="#{userWorkQueues.btnStateful}"
                                               id="btnStateful"
                                               styleClass="queuesButton"
                                               style="top: 140px"
                                               text="Reallocate s/f"/>

                                    <ui:button action="#{userWorkQueues.btnComplete_action}"
                                               binding="#{userWorkQueues.btnComplete}"
                                               id="btnComplete"
                                               styleClass="queuesButton"
                                               style="top: 180px"
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
                                        styleClass="queuesTabPanel"
                                        style="background-color: rgb(251, 240, 240)">

                                    <ui:button action="#{userWorkQueues.btnUnsuspend_action}"
                                               binding="#{userWorkQueues.btnUnsuspend}"
                                               id="btnUnsuspend"
                                               styleClass="queuesButton"
                                               style="top: 20px"
                                               text="Unsuspend"/>
                                </ui:panelLayout>
                            </ui:tab>
                        </ui:tabSet>

                        <ui:panelLayout binding="#{SessionBean.messagePanel}"
                                        id="msgPanel"
                                        panelLayout="flow"
                                        style="top: 395px; left: 150px; position: absolute"/>

                        <div style="height: 202px; left: 126px; top: 118px; position: absolute; width: 542px">
                            <jsp:directive.include file="pfQueueUI.jspf"/>
                        </div>

                         <div style="left: 0px; top: 72px; position: absolute">
                            <jsp:directive.include file="pfMenu.jspf"/>
                        </div>

                        <ui:meta binding="#{userWorkQueues.metaRefresh}"
                                 httpEquiv="refresh"
                                 id="metaRefresh" />
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
