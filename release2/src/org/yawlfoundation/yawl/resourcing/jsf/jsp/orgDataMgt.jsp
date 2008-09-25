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

                        <div style="top: 20px; position: relative">
                            <jsp:directive.include file="pfMenubar.jspf"/>
                        </div>
                        <center>
                        <ui:panelLayout binding="#{orgDataMgt.pnlContainer}"
                                        id="pnlContainer"
                                        styleClass="orgDataMgtContainerPanel">

                        <ui:tabSet binding="#{orgDataMgt.tabSet}"
                                   id="tabSet"
                                   styleClass="adminQueuesTabSet">

                            <ui:tab action="#{orgDataMgt.tabRoles_action}"
                                    binding="#{orgDataMgt.tabRoles}"
                                    id="tabRoles"
                                    style="#{SessionBean.initTabStyle}"
                                    text="Roles">

                                <ui:panelLayout binding="#{orgDataMgt.lpRoles}"
                                                id="lpRoles"
                                                styleClass="adminQueuesTabPanel">

                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{orgDataMgt.tabCapabilities_action}"
                                    binding="#{orgDataMgt.tabCapability}"
                                    id="tabCapability"
                                    text="Capabilities">

                                <ui:panelLayout binding="#{orgDataMgt.lpCapabilities}"
                                                id="lpCapabilities"
                                                styleClass="adminQueuesTabPanel">

                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{orgDataMgt.tabPositions_action}"
                                    binding="#{orgDataMgt.tabPosition}"
                                    id="tabPosition"
                                    text="Positions">

                                <ui:panelLayout binding="#{orgDataMgt.lpPositions}"
                                                id="lpPositions"
                                                styleClass="adminQueuesTabPanel">

                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{orgDataMgt.tabOrgGroups_action}"
                                    binding="#{orgDataMgt.tabOrgGroup}"
                                    id="tabOrgGroup"
                                    text="OrgGroups">

                                <ui:panelLayout
                                        binding="#{orgDataMgt.lpOrgGroups}"
                                        id="lpOrgGroups"
                                        styleClass="adminQueuesTabPanel">

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

                        <div style="position: absolute;">
                            <jsp:directive.include file="pfOrgData.jspf"/>
                        </div>

                        </ui:panelLayout>
                      </center>

                        <ui:panelLayout binding="#{SessionBean.messagePanel}"
                                        id="msgPanel"
                                        panelLayout="flow"/>

                        <ui:meta binding="#{orgDataMgt.metaRefresh}"
                                 httpEquiv="refresh"
                                 id="metaRefresh" />
                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
