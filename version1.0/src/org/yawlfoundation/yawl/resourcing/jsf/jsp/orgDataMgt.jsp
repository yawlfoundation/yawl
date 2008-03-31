<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

    <jsp:directive.page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"/>
    <f:view>
        <ui:page binding="#{orgDataMgt.page1}" id="page1">
            <ui:html binding="#{orgDataMgt.html1}" id="html1">
                <ui:head binding="#{orgDataMgt.head1}" id="head1"
                         title="YAWL 2.0: Organisational Data Management">

                    <ui:link binding="#{orgDataMgt.link1}" id="link1"
                             url="/resources/stylesheet.css"/>

                    <ui:link binding="#{ApplicationBean.favIcon}" id="lnkFavIcon"
                             rel="shortcut icon"
                            type="image/x-icon" url="/resources/favicon.ico"/>

                </ui:head>
                <ui:body binding="#{orgDataMgt.body1}" id="body1"
                         style="-rave-layout: grid">
                    <ui:form binding="#{orgDataMgt.form1}" id="form1">

                    <!-- include banner -->
                    <div><jsp:directive.include file="pfHeader.jspf"/></div>

                        <ui:tabSet binding="#{orgDataMgt.tabSet}"
                                   id="tabSet"
                                   style="height: 328px"
                                   styleClass="queuesTabSet">

                            <ui:tab action="#{orgDataMgt.tabRoles_action}"
                                    binding="#{orgDataMgt.tabRoles}"
                                    id="tabRoles"
                                    style="#{SessionBean.initTabStyle}"
                                    text="Roles">

                                <ui:panelLayout binding="#{orgDataMgt.lpRoles}"
                                                id="lpRoles"
                                                styleClass="queuesTabPanel"
                                                style="height: 269px; background-color: #98ccfe">

                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{orgDataMgt.tabCapabilities_action}"
                                    binding="#{orgDataMgt.tabCapability}"
                                    id="tabCapability"
                                    text="Capabilities">

                                <ui:panelLayout binding="#{orgDataMgt.lpCapabilities}"
                                                id="lpCapabilities"
                                                styleClass="queuesTabPanel"
                                                style="height: 269px; background-color: #98ccfe">

                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{orgDataMgt.tabPositions_action}"
                                    binding="#{orgDataMgt.tabPosition}"
                                    id="tabPosition"
                                    text="Positions">

                                <ui:panelLayout binding="#{orgDataMgt.lpPositions}"
                                                id="lpPositions"
                                                styleClass="queuesTabPanel"
                                                style="height: 269px; background-color: #98ccfe">

                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{orgDataMgt.tabOrgGroups_action}"
                                    binding="#{orgDataMgt.tabOrgGroup}"
                                    id="tabOrgGroup"
                                    text="OrgGroups">

                                <ui:panelLayout
                                        binding="#{orgDataMgt.lpOrgGroups}"
                                        id="lpOrgGroups"
                                        styleClass="queuesTabPanel"
                                        style="height: 269px; background-color: #98ccfe">

                                </ui:panelLayout>
                            </ui:tab>
                        </ui:tabSet>

                        <ui:button binding="#{SessionBean.btnRefresh}"
                                   action="#{orgDataMgt.btnRefresh_action}"
                                   id="btnRefresh"
                                   imageURL="/resources/refresh.png"
                                   styleClass="refreshButton"
                                   toolTip="Refresh Queues"
                                   text=""/>

                        <ui:panelLayout binding="#{SessionBean.messagePanel}"
                                        id="msgPanel"
                                        panelLayout="flow"/>

                        <div style="height: 302px; left: 126px; top: 118px; position: absolute; width: 542px">
                            <jsp:directive.include file="pfOrgData.jspf"/>
                        </div>

                         <div style="left: 0px; top: 72px; position: absolute">
                            <jsp:directive.include file="pfMenu.jspf"/>
                        </div>

                        <ui:meta binding="#{orgDataMgt.metaRefresh}"
                                 httpEquiv="refresh"
                                 id="metaRefresh" />
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
