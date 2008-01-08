<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2" xmlns:f="http://java.sun.com/jsf/core" xmlns:h="http://java.sun.com/jsf/html" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:ui="http://www.sun.com/web/ui">
    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{userWorkQueues.page1}" id="page1">
            <ui:html binding="#{userWorkQueues.html1}" id="html1">
                <ui:head binding="#{userWorkQueues.head1}" id="head1" title="YAWL Worklist">
                    <ui:link binding="#{userWorkQueues.link1}" id="link1" url="/resources/stylesheet.css"/>
                </ui:head>
                <ui:body binding="#{userWorkQueues.body1}" id="body1" style="-rave-layout: grid">
                    <ui:form binding="#{userWorkQueues.form1}" id="form1">
                        <ui:tabSet binding="#{userWorkQueues.tabSet}" id="tabSet" selected="tabOffered" style="border-width: 1px; border-style: solid; border-color: rgb(0, 0, 0) rgb(0, 0, 0) rgb(0, 0, 0) rgb(0, 0, 0); height: 262px; left: 168px; top: 150px; position: absolute; width: 694px">
                            <ui:tab action="#{userWorkQueues.tabOffered_action}" binding="#{userWorkQueues.tabOffered}" id="tabOffered" style="" text="#{SessionBean.initOfferedTabText}">
                                <ui:panelLayout binding="#{userWorkQueues.lpOffered}" id="lpOffered" style="border-width: 2px; border-style: solid; border-color: rgb(204, 204, 204) rgb(204, 204, 204) rgb(204, 204, 204) rgb(204, 204, 204); padding: 10px; background-color: rgb(227, 240, 252); height: 206px; position: relative; width: 669px; -rave-layout: grid">
                                    <ui:button action="#{userWorkQueues.btnAccept_action}" binding="#{userWorkQueues.btnAccept}" id="btnAccept" primary="true"
                                        style="font-size: 14px; left: 557px; top: 60px; position: absolute; width: 100px" text="Accept Offer"/>
                                </ui:panelLayout>
                            </ui:tab>
                            <ui:tab action="#{userWorkQueues.tabAllocated_action}" binding="#{userWorkQueues.tabAllocated}" id="tabAllocated" text="#{SessionBean.initAllocatedTabText}">
                                <ui:panelLayout binding="#{userWorkQueues.lpAllocated}" id="lpAllocated" style="border: 2px solid rgb(204, 204, 204); padding: 10px; background-color: rgb(251, 251, 242); height: 206px; position: relative; width: 671px">
                                    <ui:button action="#{userWorkQueues.btnStart_action}" binding="#{userWorkQueues.btnStart}" id="btnStart"
                                        style="font-size: 14px; left: 557px; top: 30px; position: absolute; width: 100px" text="Start"/>
                                    <ui:button action="#{userWorkQueues.btnDeallocate_action}" binding="#{userWorkQueues.btnDeallocate}" id="btnDeallocate"
                                        style="font-size: 14px; left: 557px; top: 66px; position: absolute; width: 100px" text="Deallocate"/>
                                    <ui:button action="#{userWorkQueues.btnDelegate_action}" binding="#{userWorkQueues.btnDelegate}" id="btnDelegate"
                                        style="font-size: 14px; left: 557px; top: 102px; position: absolute; width: 100px" text="Delegate"/>
                                    <ui:button action="#{userWorkQueues.btnSkip_action}" binding="#{userWorkQueues.btnSkip}" id="btnSkip"
                                        style="font-size: 14px; left: 557px; top: 138px; position: absolute; width: 100px" text="Skip"/>
                                    <ui:button action="#{userWorkQueues.btnPile_action}" binding="#{userWorkQueues.btnPile}" id="btnPile"
                                        style="font-size: 14px; left: 557px; top: 174px; position: absolute; width: 100px" text="Pile"/>
                                </ui:panelLayout>
                            </ui:tab>
                            <ui:tab action="#{userWorkQueues.tabStarted_action}" binding="#{userWorkQueues.tabStarted}" id="tabStarted" text="#{SessionBean.initStartedTabText}">
                                <ui:panelLayout binding="#{userWorkQueues.lpStarted}" id="lpStarted" style="border: 2px solid rgb(204, 204, 204); padding: 10px; background-color: rgb(235, 252, 235); height: 206px; position: relative; width: 671px">
                                    <ui:button action="#{userWorkQueues.btnSuspend_action}" binding="#{userWorkQueues.btnSuspend}" id="btnSuspend"
                                        style="font-size: 14px; left: 557px; top: 66px; position: absolute; width: 100px" text="Suspend"/>
                                    <ui:button action="#{userWorkQueues.btnStateless_action}" binding="#{userWorkQueues.btnStateless}" id="btnStateless"
                                        style="font-size: 14px; left: 557px; top: 102px; position: absolute; width: 100px" text="Reallocate"/>
                                    <ui:button action="#{userWorkQueues.btnComplete_action}" binding="#{userWorkQueues.btnComplete}" id="btnComplete"
                                        style="font-size: 14px; left: 557px; top: 138px; position: absolute; width: 100px" text="Complete"/>
                                    <ui:button action="#{userWorkQueues.btnView_action}" binding="#{userWorkQueues.btnView}" id="btnView"
                                        style="font-size: 14px; left: 557px; top: 30px; position: absolute; width: 100px" text="View"/>
                                </ui:panelLayout>
                            </ui:tab>
                            <ui:tab action="#{userWorkQueues.tabSuspended_action}" binding="#{userWorkQueues.tabSuspended}" id="tabSuspended" text="#{SessionBean.initSuspendedTabText}">
                                <ui:panelLayout binding="#{userWorkQueues.lpSuspended}" id="lpSuspended" style="border: 2px solid rgb(204, 204, 204); padding: 10px; background-color: rgb(251, 240, 240); height: 206px; position: relative; width: 671px">
                                    <ui:button action="#{userWorkQueues.btnUnsuspend_action}" binding="#{userWorkQueues.btnUnsuspend}" id="btnUnsuspend"
                                        style="font-size: 14px; left: 557px; top: 30px; position: absolute; width: 100px" text="Unsuspend"/>
                                </ui:panelLayout>
                            </ui:tab>
                        </ui:tabSet>
                        <div style="height: 202px; left: 168px; top: 192px; position: absolute; width: 502px">
                            <jsp:directive.include file="pfQueueUI.jspf"/>
                        </div>
                        <div style="left: 240px; top: 36px; position: absolute">
                            <jsp:directive.include file="pfHeader.jspf"/>
                        </div>
                         <div style="height: 268px; left: 6px; top: 150px; position: absolute; width: 154px">
                            <jsp:directive.include file="pfMenu.jspf"/>
                        </div>
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
