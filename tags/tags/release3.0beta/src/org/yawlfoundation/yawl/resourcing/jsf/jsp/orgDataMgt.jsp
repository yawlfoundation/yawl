<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="1.2"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html"
          xmlns:jsp="http://java.sun.com/JSP/Page"
          xmlns:ui="http://www.sun.com/web/ui">

    <!--
      ~ Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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
        <ui:page binding="#{orgDataMgt.page1}" id="page1">
            <ui:html binding="#{orgDataMgt.html1}" id="html1">
                <ui:head binding="#{orgDataMgt.head1}" id="head1"
                         title="YAWL #{ApplicationBean.yawlVersion}: Organisational Data Management">

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
                                        style="#{SessionBean.outerPanelTop}"
                                        styleClass="orgDataMgtContainerPanel">

                        <ui:tabSet binding="#{orgDataMgt.tabSet}"
                                   id="tabSet"
                                   styleClass="orgDataTabSet">

                            <ui:tab action="#{orgDataMgt.tabRoles_action}"
                                    binding="#{orgDataMgt.tabRoles}"
                                    id="tabRoles"
                                    style="#{SessionBean.initTabStyle}"
                                    text="Roles">

                                <ui:panelLayout binding="#{orgDataMgt.lpRoles}"
                                                id="lpRoles"
                                                styleClass="orgDataTabPanel">

                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{orgDataMgt.tabCapabilities_action}"
                                    binding="#{orgDataMgt.tabCapability}"
                                    id="tabCapability"
                                    text="Capabilities">

                                <ui:panelLayout binding="#{orgDataMgt.lpCapabilities}"
                                                id="lpCapabilities"
                                                styleClass="orgDataTabPanel">

                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{orgDataMgt.tabPositions_action}"
                                    binding="#{orgDataMgt.tabPosition}"
                                    id="tabPosition"
                                    text="Positions">

                                <ui:panelLayout binding="#{orgDataMgt.lpPositions}"
                                                id="lpPositions"
                                                styleClass="orgDataTabPanel">

                                </ui:panelLayout>
                            </ui:tab>

                            <ui:tab action="#{orgDataMgt.tabOrgGroups_action}"
                                    binding="#{orgDataMgt.tabOrgGroup}"
                                    id="tabOrgGroup"
                                    text="OrgGroups">

                                <ui:panelLayout
                                        binding="#{orgDataMgt.lpOrgGroups}"
                                        id="lpOrgGroups"
                                        styleClass="orgDataTabPanel">

                                </ui:panelLayout>
                            </ui:tab>
                        </ui:tabSet>

                            <div style="position: absolute;">
                                <jsp:directive.include file="pfOrgData.jspf"/>
                            </div>

                            <ui:button binding="#{orgDataMgt.btnExport}"
                                       action="#{orgDataMgt.btnExport_action}"
                                       id="btnExport"
                                       imageURL="/resources/dbExport.png"
                                       styleClass="exportButton"
                                       toolTip="Export Org Data to file"
                                       text=""/>


                            <ui:button binding="#{orgDataMgt.btnImport}"
                                       id="btnImport"
                                        action="#{orgDataMgt.btnImport_action}"
                                       imageURL="/resources/dbImport.png"
                                       styleClass="importButton"
                                       toolTip="Import Org Data from file"
                                       text=""/>

                        <ui:button binding="#{SessionBean.btnRefresh}"
                                   action="#{orgDataMgt.btnRefresh_action}"
                                   id="btnRefresh"
                                   imageURL="/resources/refresh.png"
                                   styleClass="refreshOrgDataButton"
                                   toolTip="Refresh"
                                   text=""/>

                            <ui:panelLayout binding="#{SessionBean.pnlUploadBlockout}"
                                            id="uploadBlockoutPanel"
                                            styleClass="transPanel"
                                            visible="#{SessionBean.orgDataUploadPanelVisible}" />

                            <ui:panelLayout binding="#{orgDataMgt.pnlUpload}"
                                            id="pnlUpload"
                                            styleClass="orgDataUploadPanel"
                                            style="position:absolute;"
                                            visible="#{SessionBean.orgDataUploadPanelVisible}" >

                                <ui:staticText binding="#{orgDataMgt.staticText1}"
                                               id="staticText1"
                                               styleClass="pageSubheading"
                                               style="position:absolute; left: 12px; top: 12px"
                                               text="Import Org Data"/>

                                <ui:upload binding="#{orgDataMgt.fileUpload}"
                                           columns="50" id="fileUpload"
                                           styleClass="fileUpload"
                                           style="left: 12px; top: 40px; position: absolute"
                                           immediate="true"
                                           valueChangeListener="#{orgDataMgt.fileUpload_processValueChange}"/>

                                <ui:button action="#{orgDataMgt.btnUpload_action}"
                                           binding="#{orgDataMgt.btnUpload}"
                                           id="btnUpload"
                                           style="left: 12px; top: 81px"
                                           styleClass="caseMgtButton"
                                           text="Import File"/>

                                <ui:button action="#{orgDataMgt.btnCancelUpload_action}"
                                           binding="#{orgDataMgt.btnCancelUpload}"
                                           id="btnCancelUpload"
                                           style="left: 125px; top: 81px"
                                           styleClass="caseMgtButton"
                                           text="Cancel"/>

                            </ui:panelLayout>

                            <div><jsp:include page="pfMsgPanel.jspf"/></div>
                        </ui:panelLayout>

                        </center>

                        <div><jsp:include page="pfFooter.jspf"/></div>

                        <ui:meta binding="#{orgDataMgt.metaRefresh}"
                                 httpEquiv="refresh"
                                 id="metaRefresh" />


                    </ui:form>
                </ui:body>
            </ui:html>
        </ui:page>
    </f:view>
</jsp:root>
