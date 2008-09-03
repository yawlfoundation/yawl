<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{adminQueues.page1}" id="page1">
            <ui:html binding="#{adminQueues.html1}" id="html1">
                <ui:head binding="#{adminQueues.head1}" id="head1"
                         title="YAWL 2.0 Admin Worklist">

                    <ui:link binding="#{adminQueues.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>
 
                </ui:head>
                <ui:body binding="#{adminQueues.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{adminQueues.form1}" id="form1">

                        <!-- include banner -->
                        <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <ui:tabSet binding="#{adminQueues.tabSet}"
                                   id="tabSet"
                                   style="height: 328px"
                                   styleClass="queuesTabSet">

                            <ui:tab action="#{adminQueues.tabUnOffered_action}"
                                    binding="#{adminQueues.tabUnOffered}"
                                    id="tabUnOffered"
                                    style="#{SessionBean.initTabStyle}"
                                    text="#{SessionBean.initUnOfferedTabText}">

                                <ui:panelLayout binding="#{adminQueues.lpUnOffered}"
                                                id="lpUnOffered"
                                                styleClass="queuesTabPanel"
                                                style="height: 269px; background-color: rgb(227, 240, 252)">

                                    <ui:button action="#{adminQueues.btnOffer_action}"
                                               binding="#{adminQueues.btnOffer}"
                                               id="btnOffer"
                                               primary="true"
                                               styleClass="queuesButton"
                                               style="top: 20px"
                                               text="Offer"/>

                                    <ui:button action="#{adminQueues.btnAllocate_action}"
                                               binding="#{adminQueues.btnAllocate}"
                                               id="btnAllocate"
                                               styleClass="queuesButton"
                                               style="top: 60px"
                                               text="Allocate"/>

                                    <ui:button action="#{adminQueues.btnStart_action}"
                                               binding="#{adminQueues.btnStart}"
                                               id="btnStart"
                                               styleClass="queuesButton"
                                               style="top: 100px"
                                               text="Start"/>

                                </ui:panelLayout>
                            </ui:tab>
                            
                            <ui:tab action="#{adminQueues.tabWorklisted_action}"
                                    binding="#{adminQueues.tabWorklisted}"
                                    id="tabWorklisted"
                                    text="#{SessionBean.initWorklistedTabText}">

                                <ui:panelLayout binding="#{adminQueues.lpWorklisted}"
                                                id="lpWorklisted"
                                                styleClass="queuesTabPanel"
                                                style="height: 269px; background-color: rgb(235, 252, 235)">

                                    <ui:button action="#{adminQueues.btnReoffer_action}"
                                               binding="#{adminQueues.btnReoffer}"
                                               id="btnReoffer"
                                               primary="true"
                                               styleClass="queuesButton"
                                               style="top: 20px"
                                               text="Reoffer"/>

                                    <ui:button action="#{adminQueues.btnReallocate_action}"
                                               binding="#{adminQueues.btnReallocate}"
                                               id="btnReallocate"
                                               styleClass="queuesButton"
                                               style="top: 60px"
                                               text="Reallocate"/>

                                    <ui:button action="#{adminQueues.btnRestart_action}"
                                               binding="#{adminQueues.btnRestart}"
                                               id="btnRestart"
                                               styleClass="queuesButton"
                                               style="top: 100px"
                                               text="Restart"/>

                                    <ui:label binding="#{adminQueues.lblAssignedTo}"
                                              for="cbbAssignedTo"
                                              id="lblAssignedTo"
                                              styleClass="queuesLabelLeft"
                                              style="left: 217px; top: 213px"
                                              text="Assigned To"/>

                                    <ui:label binding="#{adminQueues.lblResourceState}"
                                              for="txtResourceState"
                                              id="lblResourceState"
                                              styleClass="queuesLabelRight"
                                              style="left: 379px; top: 213px"
                                              text="Resource State"/>

                                    <ui:dropDown binding="#{adminQueues.cbbAssignedTo}"
                                                 id="cbbAssignedTo"
                                                 forgetValue="true"
                                                 style="left: 217px; top: 230px; position: absolute; width: 145px"/>

                                    <ui:textField binding="#{adminQueues.txtResourceState}"
                                                  id="txtResourceState"
                                                  readOnly="true"
                                                  styleClass="queuesTextField"
                                                  style="left: 379px; top: 230px"
                                                  trim="false"/>

                                </ui:panelLayout>
                            </ui:tab>
                        </ui:tabSet>

                        <ui:button binding="#{SessionBean.btnRefresh}"
                                   action="#{adminQueues.btnRefresh_action}"
                                   id="btnRefresh"
                                   imageURL="/resources/refresh.png"
                                   styleClass="refreshButton"
                                   toolTip="Refresh Queues"
                                   text=""/>

                        <ui:checkbox binding="#{adminQueues.cbxDirectToMe}"
                                     id="cbxDirectToMe"
                                     label="Directly to me"
                                     styleClass="queuesCheckbox"
                                     selected="#{SessionBean.redirectToMe}"/>

                        <ui:panelLayout binding="#{SessionBean.messagePanel}"
                                        id="msgPanel"
                                        panelLayout="flow"/>
                        
                        
                        <div style="height: 202px; left: 126px; top: 118px; position: absolute; width: 542px">
                            <jsp:directive.include file="pfQueueUI.jspf"/>
                        </div>

                         <div style="left: 0px; top: 72px; position: absolute">
                            <jsp:directive.include file="pfMenu.jspf"/>
                        </div>

                        <ui:meta binding="#{adminQueues.metaRefresh}"
                                 httpEquiv="refresh"
                                 id="metaRefresh" />
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
